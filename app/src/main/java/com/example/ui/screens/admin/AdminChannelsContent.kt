package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import com.example.ui.AppViewModel
import com.example.data.model.Canal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminChannelsContent(viewModel: AppViewModel) {
    val canales by viewModel.canales.collectAsState()
    val categorias by viewModel.categoriasTv.collectAsState()
    
    var showDialog by remember { mutableStateOf(false) }
    var selectedCanal by remember { mutableStateOf<Canal?>(null) }
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var expandedCategoryFilter by remember { mutableStateOf(false) }

    val filteredCanales = canales.filter { canal ->
        val matchesSearch = canal.nombre.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategoryId == null || canal.categoriaId == selectedCategoryId
        matchesSearch && matchesCategory
    }.sortedBy { it.orden }
    
    val groupedCanales = filteredCanales.groupBy { it.categoriaId }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Buscador y Filtro
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar canal...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Buscar") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                
                ExposedDropdownMenuBox(
                    expanded = expandedCategoryFilter,
                    onExpandedChange = { expandedCategoryFilter = !expandedCategoryFilter }
                ) {
                    IconButton(
                        onClick = { expandedCategoryFilter = true },
                        modifier = Modifier
                            .background(
                                color = if (selectedCategoryId != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .menuAnchor()
                    ) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filtrar por categoría",
                            tint = if (selectedCategoryId != null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    ExposedDropdownMenu(
                        expanded = expandedCategoryFilter,
                        onDismissRequest = { expandedCategoryFilter = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todas las categorías") },
                            onClick = {
                                selectedCategoryId = null
                                expandedCategoryFilter = false
                            }
                        )
                        categorias.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.nombre) },
                                onClick = {
                                    selectedCategoryId = cat.id
                                    expandedCategoryFilter = false
                                }
                            )
                        }
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                groupedCanales.forEach { (catId, canalesInCat) ->
                    val categoriaNombre = categorias.find { it.id == catId }?.nombre ?: "Sin categoría"
                    
                    item {
                        Text(
                            text = categoriaNombre,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                        )
                    }
                    
                    items(canalesInCat) { canal ->
                        AdminChannelCard(
                            canal = canal, 
                            categoriaNombre = categoriaNombre, 
                            onEdit = {
                                selectedCanal = canal
                                showDialog = true
                            }
                        )
                    }
                }
                
                if (filteredCanales.isEmpty()) {
                    item {
                        Text(
                            "No se encontraron canales.",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
        
        FloatingActionButton(
            onClick = { 
                selectedCanal = null
                showDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .padding(bottom = 80.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Añadir Canal")
        }
        
        if (showDialog) {
            AdminChannelDialog(
                canal = selectedCanal,
                categorias = categorias,
                onDismiss = { showDialog = false },
                onSave = { canal ->
                    if (selectedCanal == null) {
                        viewModel.addCanal(canal)
                    } else {
                        viewModel.updateCanal(canal)
                    }
                    showDialog = false
                },
                onDelete = { canal ->
                    viewModel.deleteCanal(canal.id)
                    showDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminChannelCard(canal: Canal, categoriaNombre: String, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onEdit,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Logo
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color.White)
            ) {
                SubcomposeAsyncImage(
                    model = canal.logoUrl,
                    contentDescription = canal.nombre,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize().padding(4.dp),
                    error = {
                        Icon(
                            imageVector = Icons.Default.Tv,
                            contentDescription = "No logo",
                            tint = Color.Gray,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = canal.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Categoría: $categoriaNombre",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = canal.streamUrl,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    maxLines = 1,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}
