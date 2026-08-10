package com.example.ui.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.ui.PlayerView
import androidx.media3.ui.AspectRatioFrameLayout
import coil.compose.AsyncImage
import com.example.ui.AppViewModel
import kotlinx.coroutines.delay

@Composable
fun VideoPlayerScreen(
    streamUrl: String,
    isLive: Boolean,
    viewModel: AppViewModel,
    onNavigateToPlayer: (String, Boolean) -> Unit,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentActivity = context as? Activity
    
    var player by remember { mutableStateOf<ExoPlayer?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    var showOverlay by remember { mutableStateOf(false) }
    var controlsVisible by remember { mutableStateOf(true) }
    var isLocked by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    var resizeMode by remember { mutableIntStateOf(AspectRatioFrameLayout.RESIZE_MODE_FIT) }
    var hasError by remember { mutableStateOf(false) }
    var retryCount by remember { mutableIntStateOf(0) }
    
    val currentCanal = if (isLive) viewModel.getCanalByUrl(streamUrl) else null
    val currentTitle = if (isLive) currentCanal?.nombre else viewModel.titulos.collectAsState().value.find { it.streamUrl == streamUrl }?.nombre
    
    var toastMessage by remember { mutableStateOf<String?>(null) }

    // Lock Screen to Landscape automatically when entering Player
    DisposableEffect(Unit) {
        val originalOrientation = currentActivity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
        currentActivity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        onDispose {
            currentActivity?.requestedOrientation = originalOrientation
        }
    }

    DisposableEffect(streamUrl, retryCount) {
        val loadControl = DefaultLoadControl.Builder()
            .setBufferDurationsMs(
                1000, // Min buffer for live HLS
                5000, // Max buffer
                250,  // Start playback instantly after 250ms
                500   // Resume after rebuffer
            )
            .setPrioritizeTimeOverSizeThresholds(true)
            .build()
            
        val dataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("VLC/3.0.18")
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(10000)
            .setReadTimeoutMs(15000)
            
        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)

        val exoPlayer = ExoPlayer.Builder(context)
            .setLoadControl(loadControl)
            .setMediaSourceFactory(mediaSourceFactory)
            .build().apply {
            setMediaItem(MediaItem.fromUri(streamUrl))
            prepare()
            playWhenReady = true
            addListener(object : androidx.media3.common.Player.Listener {
                override fun onPlaybackStateChanged(playbackState: Int) {
                    isLoading = playbackState == androidx.media3.common.Player.STATE_BUFFERING
                    if (playbackState == androidx.media3.common.Player.STATE_READY) {
                        hasError = false
                    }
                }
                override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                    if (retryCount < 3) {
                        toastMessage = "Reconectando señal (Intento ${retryCount + 1})..."
                        retryCount++
                    } else {
                        hasError = true
                        isLoading = false
                    }
                }
            })
        }
        player = exoPlayer
        onDispose {
            exoPlayer.release()
        }
    }

    // Auto-hide controls
    LaunchedEffect(controlsVisible) {
        if (controlsVisible) {
            delay(4000)
            controlsVisible = false
        }
    }

    LaunchedEffect(toastMessage) {
        if (toastMessage != null) {
            delay(2500)
            toastMessage = null
        }
    }

    LaunchedEffect(isMuted) {
        player?.volume = if (isMuted) 0f else 1f
    }

    fun switchChannel(next: Boolean) {
        if (!isLive) return
        val newCanal = if (next) viewModel.getNextCanal(streamUrl) else viewModel.getPrevCanal(streamUrl)
        if (newCanal != null) {
            toastMessage = "Cambiando a: ${newCanal.nombre}"
            onNavigateToPlayer(newCanal.streamUrl, true)
        } else {
            toastMessage = if (next) "Último canal de la lista" else "Primer canal de la lista"
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        player?.let { exo ->
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { 
                        if (!isLocked) controlsVisible = !controlsVisible 
                    },
                factory = { ctx ->
                    PlayerView(ctx).apply {
                        this.player = exo
                        useController = false // Custom Compose Overlays for maximum polish
                        keepScreenOn = true
                    }
                },
                update = { view ->
                    view.player = exo
                    view.resizeMode = resizeMode
                }
            )
        }
        
        if (isLoading && !hasError) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 3.dp
            )
        }
        
        // Error State with Manual Retry
        if (hasError) {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.85f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.align(Alignment.Center).padding(24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(Icons.Default.Warning, contentDescription = "Error", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(48.dp))
                    Text("Error al conectar con la señal del canal", color = Color.White, fontWeight = FontWeight.Bold)
                    Text("El servidor del canal no responde o se encuentra en mantenimiento.", style = MaterialTheme.typography.bodySmall, color = Color.LightGray)
                    Button(
                        onClick = { 
                            hasError = false
                            isLoading = true
                            retryCount = 0
                            player?.prepare()
                            player?.play()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reintentar Transmisión")
                    }
                }
            }
        }
        
        // Top Title Overlay
        AnimatedVisibility(
            visible = controlsVisible && !showOverlay && !isLocked,
            enter = fadeIn() + slideInVertically { -it },
            exit = fadeOut() + slideOutVertically { -it },
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        brush = androidx.compose.ui.graphics.Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.85f), Color.Transparent)
                        )
                    )
                    .padding(top = 16.dp, bottom = 32.dp, start = 64.dp, end = 64.dp)
            ) {
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (isLive) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFE53935)
                        ) {
                            Text(
                                text = "EN VIVO",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    Text(
                        text = currentTitle ?: "Reproduciendo...",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )

                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color.White.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "HD 1080p",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }

                    if (isLive && currentCanal != null) {
                        IconButton(onClick = { viewModel.toggleFavorite(currentCanal) }) {
                            Icon(
                                imageVector = if (currentCanal.esFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Favorito",
                                tint = if (currentCanal.esFavorito) Color(0xFFFF5252) else Color.White
                            )
                        }
                    }
                }
            }
        }
        
        // Zapping Overlay (Previous / Next Channel Arrow Controls)
        if (isLive && controlsVisible && !isLocked && !showOverlay) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledIconButton(
                    onClick = { switchChannel(false) },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.6f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.size(54.dp)
                ) {
                    Icon(Icons.Default.SkipPrevious, contentDescription = "Canal Anterior", modifier = Modifier.size(32.dp))
                }

                FilledIconButton(
                    onClick = { switchChannel(true) },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.6f),
                        contentColor = Color.White
                    ),
                    modifier = Modifier.size(54.dp)
                ) {
                    Icon(Icons.Default.SkipNext, contentDescription = "Siguiente Canal", modifier = Modifier.size(32.dp))
                }
            }
        }

        // Right Side Controls (Mute, Aspect Ratio, Lock)
        AnimatedVisibility(
            visible = (controlsVisible || isLocked) && !showOverlay,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.CenterEnd).padding(end = 16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                if (!isLocked) {
                    FilledIconButton(
                        onClick = { isMuted = !isMuted },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.6f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp, contentDescription = "Mute")
                    }
                    FilledIconButton(
                        onClick = { 
                            resizeMode = when (resizeMode) {
                                AspectRatioFrameLayout.RESIZE_MODE_FIT -> {
                                    toastMessage = "Pantalla: Estirar 16:9"
                                    AspectRatioFrameLayout.RESIZE_MODE_FILL
                                }
                                AspectRatioFrameLayout.RESIZE_MODE_FILL -> {
                                    toastMessage = "Pantalla: Zoom"
                                    AspectRatioFrameLayout.RESIZE_MODE_ZOOM
                                }
                                else -> {
                                    toastMessage = "Pantalla: Ajustar"
                                    AspectRatioFrameLayout.RESIZE_MODE_FIT
                                }
                            }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.6f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.AspectRatio, contentDescription = "Aspect Ratio")
                    }
                }
                FilledIconButton(
                    onClick = { 
                        isLocked = !isLocked
                        controlsVisible = !isLocked
                    },
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = if (isLocked) MaterialTheme.colorScheme.error.copy(alpha=0.8f) else Color.Black.copy(alpha = 0.6f),
                        contentColor = Color.White
                    )
                ) {
                    Icon(if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen, contentDescription = "Lock")
                }
            }
        }

        // Notification Toast Overlay
        AnimatedVisibility(
            visible = toastMessage != null,
            enter = fadeIn() + slideInVertically { it / 2 },
            exit = fadeOut() + slideOutVertically { it / 2 },
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 32.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.Black.copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ) {
                Text(
                    text = toastMessage ?: "",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp)
                )
            }
        }

        // Channel Grid Button
        if (isLive) {
            AnimatedVisibility(
                visible = controlsVisible && !showOverlay && !isLocked,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 24.dp)
            ) {
                FloatingActionButton(
                    onClick = { showOverlay = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.GridView, contentDescription = "Grilla de canales")
                }
            }
        }

        // Back Button
        AnimatedVisibility(
            visible = controlsVisible && !showOverlay && !isLocked,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(16.dp)
        ) {
            FilledIconButton(
                onClick = onNavigateBack,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = Color.Black.copy(alpha = 0.6f),
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
            }
        }

        // Interactive Slide-Out Channel Drawer Overlay
        AnimatedVisibility(
            visible = showOverlay,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
                    .clickable { showOverlay = false }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(0.55f)
                        .align(Alignment.CenterEnd)
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.95f))
                        .clickable { /* consume click */ }
                ) {
                    val categorias = viewModel.categoriasTv.collectAsState().value
                    val canales = viewModel.canales.collectAsState().value
                    
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        Text(
                            text = "Grilla Rápida de Canales",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(categorias) { categoria ->
                                val catCanales = canales.filter { it.categoriaId == categoria.id }
                                if (catCanales.isNotEmpty()) {
                                    Column {
                                        Text(
                                            text = categoria.nombre,
                                            color = MaterialTheme.colorScheme.onSurface,
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(bottom = 8.dp)
                                        )
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                            items(catCanales) { canal ->
                                                val isCurrent = canal.streamUrl == streamUrl
                                                Card(
                                                    modifier = Modifier
                                                        .width(110.dp)
                                                        .height(75.dp)
                                                        .clickable {
                                                            if (!isCurrent) {
                                                                showOverlay = false
                                                                onNavigateToPlayer(canal.streamUrl, true)
                                                            }
                                                        },
                                                    shape = RoundedCornerShape(10.dp),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                                                    )
                                                ) {
                                                    Box(modifier = Modifier.fillMaxSize().padding(6.dp)) {
                                                        AsyncImage(
                                                            model = canal.logoUrl,
                                                            contentDescription = canal.nombre,
                                                            contentScale = ContentScale.Fit,
                                                            modifier = Modifier.fillMaxSize()
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

