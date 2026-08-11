package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.filled.MovieFilter
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.Icons
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.animation.core.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.LocalIndication
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.data.model.Categoria
import com.example.data.model.Titulo
import com.example.ui.AppViewModel
import com.example.ui.components.shimmerEffect
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

@Composable
fun MoviesScreen(
    viewModel: AppViewModel,
    onNavigateToPlayer: (String) -> Unit
) {
    val categorias by viewModel.categoriasVod.collectAsState()
    val titulos by viewModel.titulos.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Int?>(null) }
    var isSearchExpanded by remember { mutableStateOf(false) }
    
    val titulosFavoritos = titulos.filter { it.esFavorito }
    val featuredTitulos = titulos.take(5)
    
    val filteredCategorias = if (selectedCategory != null) {
        categorias.filter { it.id == selectedCategory }
    } else {
        categorias
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(bottom = 80.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Cine & Series",
                            style = androidx.compose.ui.text.TextStyle(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        Color(0xFF00E5FF)
                                    )
                                ),
                                fontWeight = FontWeight.Black,
                                fontSize = MaterialTheme.typography.displaySmall.fontSize
                            )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Películas, series y contenido a la carta",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                        )
                    }

                    // Content Count Badge
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MovieFilter,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = "${titulos.size} TÍTULOS",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
        
        // Netflix Style Featured Billboard Hero Carousel
        if (featuredTitulos.isNotEmpty() && searchQuery.isEmpty() && selectedCategory == null) {
            item {
                val pagerState = rememberPagerState(pageCount = { featuredTitulos.size })
                Column(modifier = Modifier.padding(bottom = 24.dp)) {
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp),
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        pageSpacing = 16.dp
                    ) { page ->
                        val titulo = featuredTitulos[page]
                        var isFocused by remember { mutableStateOf(false) }
                        val scale by animateFloatAsState(if (isFocused) 1.08f else 1f, label = "scale")
                        val borderColor by animateColorAsState(if (isFocused) Color.White else Color.Transparent)
                        
                        Card(
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(scale)
                                .androidx.compose.ui.focus.onFocusChanged { isFocused = it.isFocused }
                                .clickable {
                                    if (!titulo.streamUrl.isNullOrEmpty()) {
                                        val encodedUrl = URLEncoder.encode(titulo.streamUrl, StandardCharsets.UTF_8.toString())
                                        onNavigateToPlayer(encodedUrl)
                                    }
                                },
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(if (isFocused) 4.dp else 0.dp, borderColor),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isFocused) 16.dp else 10.dp)
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                SubcomposeAsyncImage(
                                    model = titulo.poster,
                                    contentDescription = titulo.nombre,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize(),
                                    loading = { Box(modifier = Modifier.fillMaxSize().shimmerEffect()) }
                                )
                                // Dark Gradient Overlays
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(
                                            brush = Brush.verticalGradient(
                                                colors = listOf(
                                                    Color.Black.copy(alpha = 0.2f),
                                                    Color.Transparent,
                                                    Color.Black.copy(alpha = 0.95f)
                                                )
                                            )
                                        )
                                )
                                
                                // Top Badges
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.TopStart)
                                        .padding(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = MaterialTheme.colorScheme.primary
                                    ) {
                                        Text(
                                            text = "NUEVO",
                                            color = Color.White,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Black,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color.Black.copy(alpha = 0.6f)
                                    ) {
                                        Text(
                                            text = "4K ULTRA HD",
                                            color = Color.White.copy(alpha = 0.9f),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }

                                // Bottom Content Details & Netflix Play Button
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = titulo.nombre,
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "${titulo.anio} • Película • Audio Español",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.75f)
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    
                                    // Netflix Play Action Button
                                    var isPlayFocused by remember { mutableStateOf(false) }
                                    val playBorder by animateColorAsState(if (isPlayFocused) Color.White else Color.Transparent)
                                    Surface(
                                        onClick = {
                                            if (!titulo.streamUrl.isNullOrEmpty()) {
                                                val encodedUrl = URLEncoder.encode(titulo.streamUrl, StandardCharsets.UTF_8.toString())
                                                onNavigateToPlayer(encodedUrl)
                                            }
                                        },
                                        color = Color.White,
                                        contentColor = Color.Black,
                                        shape = RoundedCornerShape(10.dp),
                                        border = androidx.compose.foundation.BorderStroke(if (isPlayFocused) 4.dp else 0.dp, playBorder),
                                        modifier = Modifier
                                            .height(38.dp)
                                            .androidx.compose.ui.focus.onFocusChanged { isPlayFocused = it.isFocused }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Icon(
                                                imageVector = Icons.Rounded.PlayArrow,
                                                contentDescription = "Reproducir",
                                                tint = Color.Black,
                                                modifier = Modifier.size(20.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Reproducir",
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
            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 16.dp)) {
                if (isSearchExpanded) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Buscar películas o series...") },
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
        
        // Filter Chips
        if (categorias.isNotEmpty()) {
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                            var isFocused by remember { mutableStateOf(false) }
                            FilterChip(
                                modifier = Modifier.androidx.compose.ui.focus.onFocusChanged { isFocused = it.isFocused },
                                selected = selectedCategory == null,
                                onClick = { selectedCategory = null },
                                label = { Text("Todos") },
                                border = if (isFocused) FilterChipDefaults.filterChipBorder(enabled = true, selected = selectedCategory == null, borderColor = Color.White, borderWidth = 3.dp) else FilterChipDefaults.filterChipBorder(enabled = true, selected = selectedCategory == null),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                )
                            )
                    }
                    items(categorias) { cat ->
                            var isFocused by remember { mutableStateOf(false) }
                            FilterChip(
                                modifier = Modifier.androidx.compose.ui.focus.onFocusChanged { isFocused = it.isFocused },
                                selected = selectedCategory == cat.id,
                                onClick = { selectedCategory = cat.id },
                                label = { Text(cat.nombre) },
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
        if (titulosFavoritos.isNotEmpty() && searchQuery.isEmpty() && selectedCategory == null) {
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
                            text = "Favoritos",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(titulosFavoritos) { titulo ->
                            TituloCard(titulo = titulo, onClick = {
                                if (!titulo.streamUrl.isNullOrEmpty()) {
                                    val encodedUrl = URLEncoder.encode(titulo.streamUrl, StandardCharsets.UTF_8.toString())
                                    onNavigateToPlayer(encodedUrl)
                                }
                            })
                        }
                    }
                }
            }
        }

        if (categorias.isEmpty() || titulos.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillParentMaxSize().padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.MovieFilter,
                            contentDescription = "Sin títulos",
                            modifier = Modifier.size(72.dp),
                            tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.2f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Aún no hay contenido disponible.",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        } else {
            val validCategories = if (searchQuery.isNotEmpty()) categorias else filteredCategorias
            
            items(validCategories) { categoria ->
                val titulosCategoria = titulos.filter { 
                    it.categoriaId == categoria.id && it.nombre.contains(searchQuery, ignoreCase = true)
                }
                if (titulosCategoria.isNotEmpty()) {
                    TituloCategoriaRow(
                        categoria = categoria,
                        titulos = titulosCategoria,
                        onNavigateToPlayer = onNavigateToPlayer
                    )
                }
            }
            
            if (searchQuery.isNotEmpty() && validCategories.all { cat -> 
                titulos.filter { it.categoriaId == cat.id && it.nombre.contains(searchQuery, ignoreCase = true) }.isEmpty() 
            }) {
                item {
                    Box(
                        modifier = Modifier.fillParentMaxSize().padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron títulos para '$searchQuery'",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TituloCategoriaRow(
    categoria: Categoria,
    titulos: List<Titulo>,
    onNavigateToPlayer: (String) -> Unit
) {
    Column(modifier = Modifier.padding(bottom = 28.dp)) {
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
        }
        
        LazyRow(
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(titulos) { titulo ->
                TituloCard(titulo = titulo, onClick = {
                    if (titulo.streamUrl != null) {
                        val encodedUrl = URLEncoder.encode(titulo.streamUrl, StandardCharsets.UTF_8.toString())
                        onNavigateToPlayer(encodedUrl)
                    }
                })
            }
        }
    }
}

@Composable
fun TituloCard(titulo: Titulo, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    var isFocused by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(if (isPressed) 0.95f else if (isFocused) 1.08f else 1f, label = "scale")
    val borderColor by animateColorAsState(if (isFocused) Color.White else Color.Transparent)

    Card(
        modifier = Modifier
            .width(140.dp)
            .height(210.dp)
            .scale(scale)
            .androidx.compose.ui.focus.onFocusChanged { isFocused = it.isFocused }
            .clickable(
                interactionSource = interactionSource,
                indication = LocalIndication.current,
                onClick = onClick
            ),
        shape = RoundedCornerShape(14.dp),
        border = androidx.compose.foundation.BorderStroke(if (isFocused) 4.dp else 0.dp, borderColor),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isPressed) 2.dp else if (isFocused) 12.dp else 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            SubcomposeAsyncImage(
                model = titulo.poster,
                contentDescription = titulo.nombre,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize(),
                loading = {
                    Box(modifier = Modifier.fillMaxSize().shimmerEffect())
                },
                error = {
                    Box(
                        modifier = Modifier.fillMaxSize().padding(16.dp), 
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            titulo.nombre, 
                            color = Color.White.copy(alpha=0.5f), 
                            style = MaterialTheme.typography.bodySmall, 
                            maxLines = 2, 
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            )
            
            // Netflix-style Gradient Overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.6f)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.95f))
                        )
                    )
            )
            
            // Top Badge "N" or "HD"
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "HD",
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            Text(
                text = titulo.nombre,
                color = Color.White,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(10.dp)
            )
        }
    }
}
