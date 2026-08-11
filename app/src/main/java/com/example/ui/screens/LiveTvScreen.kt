package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.TvOff
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.compose.AsyncImage
import com.example.ui.FeaturedEvent
import com.example.data.model.Canal
import com.example.data.model.Categoria
import com.example.ui.AppViewModel
import com.example.ui.components.shimmerEffect
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun LiveTvScreen(
    viewModel: AppViewModel,
    onNavigateToPlayer: (String) -> Unit
) {
    val categorias by viewModel.categoriasTv.collectAsState()
    val canalesPorCategoria by viewModel.canalesPorCategoria.collectAsState()
    val todosCanales by viewModel.canales.collectAsState()
    val isEditMode by viewModel.isEditMode.collectAsState()
    val featuredEvent by viewModel.featuredEvent.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Int?>(null) }
    var editingCanal by remember { mutableStateOf<Canal?>(null) }
    var isSearchExpanded by remember { mutableStateOf(false) }
    
    val canalesFavoritos = remember(todosCanales) { todosCanales.filter { it.esFavorito } }
    val featuredCanales: List<Canal> = todosCanales.take(5)
    
    val filteredCategorias = remember(categorias, selectedCategory) {
        if (selectedCategory != null) {
            categorias.filter { it.id == selectedCategory }
        } else {
            categorias
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(bottom = 80.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Edit Mode Active Sticky Banner
            if (isEditMode) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Column {
                                    Text(
                                        text = "🛠️ Modo Edición Directo",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Text(
                                        text = "Toca un canal para cambiar nombre/categoría o usá ◀ ▶ para mover su posición.",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                            Button(
                                onClick = { viewModel.setEditMode(false) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary
                                )
                            ) {
                                Text("Finalizar")
                            }
                        }
                    }
                }
            }

            // Featured Event Banner
            if (featuredEvent != null) {
                item {
                    FeaturedEventBanner(event = featuredEvent!!, onNavigateToPlayer = onNavigateToPlayer)
                }
            }

            // Hero Header
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "DKN TV",
                                    style = androidx.compose.ui.text.TextStyle(
                                        brush = Brush.horizontalGradient(
                                            colors = listOf(
                                                MaterialTheme.colorScheme.primary,
                                                MaterialTheme.colorScheme.secondary
                                            )
                                        ),
                                        fontWeight = FontWeight.Black,
                                        fontSize = MaterialTheme.typography.displaySmall.fontSize
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = if (isEditMode) "Edita tus canales en tiempo real" else "Transmisión en directo y entretenimiento",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                            )
                        }

                        // Live Badge or Edit Toggle Button
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            var isFocused by remember { mutableStateOf(false) }
                            FilterChip(
                                modifier = Modifier.padding(end = 8.dp).androidx.compose.ui.focus.onFocusChanged { isFocused = it.isFocused },
                                selected = isEditMode,
                                onClick = { viewModel.toggleEditMode() },
                                label = { Text(if (isEditMode) "Salir Edición" else "Editar") },
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                border = if (isFocused) FilterChipDefaults.filterChipBorder(enabled = true, selected = isEditMode, borderColor = Color.White, borderWidth = 3.dp) else FilterChipDefaults.filterChipBorder(enabled = true, selected = isEditMode)
                            )

                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = Color(0xFFE53935).copy(alpha = 0.15f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE53935).copy(alpha = 0.4f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFFE53935))
                                    )
                                    Text(
                                        text = "${todosCanales.size} CANALES",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color(0xFFFF5252)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Featured Live Banner (when not searching and not in edit mode)
            if (featuredCanales.isNotEmpty() && searchQuery.isEmpty() && selectedCategory == null && !isEditMode) {
                item {
                    val pagerState = rememberPagerState(pageCount = { featuredCanales.size })
                    Column(modifier = Modifier.padding(bottom = 24.dp)) {
                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            pageSpacing = 16.dp
                        ) { page ->
                            val canal = featuredCanales[page]
                            var isFocused by remember { mutableStateOf(false) }
                            val scale by animateFloatAsState(if (isFocused) 1.08f else 1f, label = "scale")
                            val borderColor by animateColorAsState(if (isFocused) Color.White else Color.Transparent)
                            
                            Card(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .scale(scale)
                                    .androidx.compose.ui.focus.onFocusChanged { isFocused = it.isFocused }
                                    .clickable {
                                        val encodedUrl = URLEncoder.encode(canal.streamUrl, StandardCharsets.UTF_8.toString())
                                        onNavigateToPlayer(encodedUrl)
                                    },
                                shape = RoundedCornerShape(20.dp),
                                border = androidx.compose.foundation.BorderStroke(if (isFocused) 4.dp else 0.dp, borderColor),
                                elevation = CardDefaults.cardElevation(defaultElevation = if (isFocused) 16.dp else 10.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    SubcomposeAsyncImage(
                                        model = canal.logoUrl,
                                        contentDescription = canal.nombre,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize(),
                                        loading = { Box(modifier = Modifier.fillMaxSize().shimmerEffect()) }
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                brush = Brush.verticalGradient(
                                                    colors = listOf(
                                                        Color.Black.copy(alpha = 0.3f),
                                                        Color.Transparent,
                                                        Color.Black.copy(alpha = 0.95f)
                                                    )
                                                )
                                            )
                                    )
                                    
                                    // Top Live Badge
                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(16.dp),
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFFE50914)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(8.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White)
                                            )
                                            Text(
                                                text = "DESTACADO",
                                                color = Color.White,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Black
                                            )
                                        }
                                    }

                                    // Bottom Title & Action Button
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(16.dp)
                                    ) {
                                        Text(
                                            text = canal.nombre,
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.ExtraBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        
                                        var isPlayFocused by remember { mutableStateOf(false) }
                                        val playBorder by animateColorAsState(if (isPlayFocused) Color.White else Color.Transparent)
                                        Surface(
                                            onClick = {
                                                val encodedUrl = URLEncoder.encode(canal.streamUrl, StandardCharsets.UTF_8.toString())
                                                onNavigateToPlayer(encodedUrl)
                                            },
                                            color = Color.White,
                                            contentColor = Color.Black,
                                            shape = RoundedCornerShape(10.dp),
                                            border = androidx.compose.foundation.BorderStroke(if (isPlayFocused) 4.dp else 0.dp, playBorder),
                                            modifier = Modifier
                                                .height(36.dp)
                                                .androidx.compose.ui.focus.onFocusChanged { isPlayFocused = it.isFocused }
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Rounded.PlayArrow,
                                                    contentDescription = "Ver Ahora",
                                                    tint = Color.Black,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "Sintonizar Canal",
                                                    fontWeight = FontWeight.Bold,
                                                    style = MaterialTheme.typography.labelLarge
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
            
            // Search Bar
            item {
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 12.dp)) {
                    if (isSearchExpanded) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Buscar canales por nombre...") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                            trailingIcon = {
                                IconButton(onClick = {
                                    searchQuery = ""
                                    isSearchExpanded = false
                                }) {
                                    Icon(Icons.Default.Close, contentDescription = "Cerrar Buscar")
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.Transparent
                            ),
                            singleLine = true
                        )
                    } else {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            var isSearchFocused by remember { mutableStateOf(false) }
                            Surface(
                                modifier = Modifier.androidx.compose.ui.focus.onFocusChanged { isSearchFocused = it.isFocused },
                                shape = CircleShape,
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isSearchFocused) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null,
                                onClick = { isSearchExpanded = true }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Abrir Búsqueda",
                                    modifier = Modifier.padding(12.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
            
            // Filter Chips with Channel Counts
            if (categorias.isNotEmpty()) {
                item {
                    LazyRow(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            var isFocused by remember { mutableStateOf(false) }
                            FilterChip(
                                modifier = Modifier.androidx.compose.ui.focus.onFocusChanged { isFocused = it.isFocused },
                                selected = selectedCategory == null,
                                onClick = { selectedCategory = null },
                                label = { Text("Todos (${todosCanales.size})") },
                                border = if (isFocused) FilterChipDefaults.filterChipBorder(enabled = true, selected = selectedCategory == null, borderColor = Color.White, borderWidth = 3.dp) else FilterChipDefaults.filterChipBorder(enabled = true, selected = selectedCategory == null),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                        items(categorias) { cat ->
                            val count = (canalesPorCategoria[cat.id] ?: emptyList()).size
                            var isFocused by remember { mutableStateOf(false) }
                            FilterChip(
                                modifier = Modifier.androidx.compose.ui.focus.onFocusChanged { isFocused = it.isFocused },
                                selected = selectedCategory == cat.id,
                                onClick = { selectedCategory = cat.id },
                                label = { Text("${cat.nombre} ($count)") },
                                border = if (isFocused) FilterChipDefaults.filterChipBorder(enabled = true, selected = selectedCategory == cat.id, borderColor = Color.White, borderWidth = 3.dp) else FilterChipDefaults.filterChipBorder(enabled = true, selected = selectedCategory == cat.id),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                        }
                    }
                }
            }
            
            // Favorites Row
            if (canalesFavoritos.isNotEmpty() && searchQuery.isEmpty() && selectedCategory == null && !isEditMode) {
                item {
                    Column(modifier = Modifier.padding(bottom = 16.dp, top = 8.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Default.Favorite,
                                contentDescription = "Favoritos",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Mis Canales Favoritos",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onBackground
                            )
                        }
                        LazyRow(
                            contentPadding = PaddingValues(horizontal = 24.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            items(canalesFavoritos) { canal ->
                                CanalCard(
                                    canal = canal,
                                    isEditMode = false,
                                    onToggleFavorite = { viewModel.toggleFavorite(canal) },
                                    onMoveLeft = {},
                                    onMoveRight = {},
                                    onEditChannel = {},
                                    onClick = {
                                        val encodedUrl = URLEncoder.encode(canal.streamUrl, StandardCharsets.UTF_8.toString())
                                        onNavigateToPlayer(encodedUrl)
                                    }
                                )
                            }
                        }
                    }
                }
            }

            if (categorias.isEmpty() || canalesPorCategoria.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillParentMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.TvOff,
                                contentDescription = "Sin canales",
                                modifier = Modifier.size(72.dp),
                                tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                text = "Aún no hay canales guardados.",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            } else {
                val validCategories = if (searchQuery.isNotEmpty()) categorias else filteredCategorias
                
                items(validCategories) { categoria ->
                    val canalesCategoria = (canalesPorCategoria[categoria.id] ?: emptyList())
                        .sortedBy { it.orden }
                        .filter { it.nombre.contains(searchQuery, ignoreCase = true) }
                    
                    if (canalesCategoria.isNotEmpty()) {
                        CategoriaRow(
                            categoria = categoria,
                            canales = canalesCategoria,
                            isEditMode = isEditMode,
                            onToggleFavorite = { viewModel.toggleFavorite(it) },
                            onMoveLeft = { viewModel.moveCanalInGrid(it, -1) },
                            onMoveRight = { viewModel.moveCanalInGrid(it, 1) },
                            onEditChannel = { editingCanal = it },
                            onNavigateToPlayer = onNavigateToPlayer
                        )
                    }
                }
            }
        }

        // Dialog for Quick Channel Editing directly from grid
        editingCanal?.let { canal ->
            QuickEditChannelDialog(
                canal = canal,
                categorias = categorias,
                onDismiss = { editingCanal = null },
                onSave = { newName, catId, logoUrl, streamUrl ->
                    viewModel.quickUpdateCanal(canal.id, newName, catId, logoUrl, streamUrl)
                    editingCanal = null
                },
                onDelete = {
                    viewModel.deleteCanal(canal.id)
                    editingCanal = null
                }
            )
        }
    }
}

@Composable
fun CategoriaRow(
    categoria: Categoria,
    canales: List<Canal>,
    isEditMode: Boolean,
    onToggleFavorite: (Canal) -> Unit,
    onMoveLeft: (Canal) -> Unit,
    onMoveRight: (Canal) -> Unit,
    onEditChannel: (Canal) -> Unit,
    onNavigateToPlayer: (String) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 24.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(20.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
            Text(
                text = categoria.nombre,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "(${canales.size})",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(canales, key = { it.id }) { canal ->
                CanalCard(
                    canal = canal,
                    isEditMode = isEditMode,
                    onToggleFavorite = { onToggleFavorite(canal) },
                    onMoveLeft = { onMoveLeft(canal) },
                    onMoveRight = { onMoveRight(canal) },
                    onEditChannel = { onEditChannel(canal) },
                    onClick = {
                        if (isEditMode) {
                            onEditChannel(canal)
                        } else {
                            val encodedUrl = URLEncoder.encode(canal.streamUrl, StandardCharsets.UTF_8.toString())
                            onNavigateToPlayer(encodedUrl)
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun CanalCard(
    canal: Canal,
    isEditMode: Boolean,
    onToggleFavorite: () -> Unit,
    onMoveLeft: () -> Unit,
    onMoveRight: () -> Unit,
    onEditChannel: () -> Unit,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var isFocused by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(if (isPressed) 0.96f else if (isFocused) 1.08f else 1f, label = "scale")
    val borderColor by animateColorAsState(if (isFocused) Color.White else Color.Transparent)
    
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        modifier = Modifier
            .width(160.dp)
            .scale(scale)
            .androidx.compose.ui.focus.onFocusChanged { isFocused = it.isFocused }
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (isEditMode) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) else Color.Transparent
            ),
            border = if (isFocused) androidx.compose.foundation.BorderStroke(4.dp, borderColor) else if (isEditMode) androidx.compose.foundation.BorderStroke(1.5.dp, MaterialTheme.colorScheme.primary) else null,
            elevation = CardDefaults.cardElevation(defaultElevation = if (isPressed) 2.dp else if (isFocused) 12.dp else 8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.surfaceVariant,
                                MaterialTheme.colorScheme.background
                            )
                        )
                    )
            ) {
                // Main Content: Logo
                SubcomposeAsyncImage(
                    model = coil.request.ImageRequest.Builder(androidx.compose.ui.platform.LocalContext.current)
                        .data(canal.logoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = canal.nombre,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    loading = {
                        Box(modifier = Modifier.fillMaxSize().shimmerEffect())
                    },
                    error = {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.TvOff,
                                contentDescription = "Error",
                                tint = MaterialTheme.colorScheme.primary.copy(alpha=0.5f),
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                )
                
                // Top Left: Favorite or Edit Badge
                if (!isEditMode) {
                    IconButton(
                        onClick = onToggleFavorite,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .size(32.dp)
                    ) {
                        Icon(
                            imageVector = if (canal.esFavorito) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favorito",
                            tint = if (canal.esFavorito) Color(0xFFFF5252) else Color.White.copy(alpha = 0.6f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = onEditChannel,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .size(32.dp)
                            .background(MaterialTheme.colorScheme.primary, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar",
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Top Right: Live Badge
                if (!isEditMode) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .padding(horizontal = 6.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFFF3B30).copy(alpha = alpha))
                        )
                        Text(
                            text = "VIVO",
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Reorder controls overlay when in edit mode
                if (isEditMode) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .background(Color.Black.copy(alpha = 0.8f))
                            .padding(vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onMoveLeft, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.ChevronLeft, contentDescription = "Mover Izquierda", tint = Color.White)
                        }
                        Text(
                            text = "MOVER",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = onMoveRight, modifier = Modifier.size(28.dp)) {
                            Icon(Icons.Default.ChevronRight, contentDescription = "Mover Derecha", tint = Color.White)
                        }
                    }
                } else {
                    // Bottom Left: Play Icon
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(8.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
                            .padding(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = "Play",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(6.dp))
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = canal.nombre,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            if (isEditMode) {
                IconButton(onClick = onEditChannel, modifier = Modifier.size(20.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(14.dp))
                }
            }
        }
        Text(
            text = if (isEditMode) "Toca para editar" else "En directo",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            maxLines = 1,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickEditChannelDialog(
    canal: Canal,
    categorias: List<Categoria>,
    onDismiss: () -> Unit,
    onSave: (newName: String, catId: Int, logoUrl: String, streamUrl: String) -> Unit,
    onDelete: () -> Unit
) {
    var nombre by remember { mutableStateOf(canal.nombre) }
    var categoriaId by remember { mutableIntStateOf(canal.categoriaId ?: (categorias.firstOrNull()?.id ?: 1)) }
    var logoUrl by remember { mutableStateOf(canal.logoUrl) }
    var streamUrl by remember { mutableStateOf(canal.streamUrl) }
    var expandedCategoryDropdown by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Canal", fontWeight = FontWeight.Bold) },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del Canal") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expandedCategoryDropdown,
                    onExpandedChange = { expandedCategoryDropdown = !expandedCategoryDropdown }
                ) {
                    val currentCatName = categorias.find { it.id == categoriaId }?.nombre ?: "Sin categoría"
                    OutlinedTextField(
                        value = currentCatName,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Categoría") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoryDropdown) },
                        modifier = Modifier.fillMaxWidth().menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategoryDropdown,
                        onDismissRequest = { expandedCategoryDropdown = false }
                    ) {
                        categorias.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.nombre) },
                                onClick = {
                                    categoriaId = cat.id
                                    expandedCategoryDropdown = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = logoUrl,
                    onValueChange = { logoUrl = it },
                    label = { Text("URL del Logo") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = streamUrl,
                    onValueChange = { streamUrl = it },
                    label = { Text("URL de la Transmisión (M3U8)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nombre.isNotBlank() && streamUrl.isNotBlank()) {
                        onSave(nombre, categoriaId, logoUrl, streamUrl)
                    }
                }
            ) {
                Text("Guardar Cambios")
            }
        },
        dismissButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Eliminar")
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        }
    )
}


@Composable
fun FeaturedEventBanner(event: FeaturedEvent, onNavigateToPlayer: (String) -> Unit) {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isFocused) 1.08f else 1f, label = "scale")
    val borderColor by animateColorAsState(if (isFocused) Color.White else Color.Transparent)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(220.dp)
            .scale(scale)
            .androidx.compose.ui.focus.onFocusChanged { isFocused = it.isFocused }
            .clickable { 
                val encoded = URLEncoder.encode(event.streamUrl, StandardCharsets.UTF_8.toString())
                onNavigateToPlayer(encoded) 
            },
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(if (isFocused) 4.dp else 0.dp, borderColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isFocused) 12.dp else 8.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = event.imageUrl.ifBlank { "https://images.unsplash.com/photo-1540747913346-19e32dc3e97e?q=80&w=1000&auto=format&fit=crop" },
                contentDescription = event.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            
            // Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f)),
                            startY = 100f
                        )
                    )
            )

            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text(
                    text = "🌟 EVENTO DESTACADO",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Black,
                    style = MaterialTheme.typography.labelMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.title,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.headlineMedium
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = event.time,
                    color = Color.LightGray,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            // Play Button indicator
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(64.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "Play",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }
    }
}
