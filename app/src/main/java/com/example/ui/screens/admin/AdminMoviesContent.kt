package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.AppViewModel
import com.example.data.model.Titulo
import com.example.data.model.TipoContenido

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMoviesContent(viewModel: AppViewModel) {
    val titulos by viewModel.titulos.collectAsState()
    val categorias by viewModel.categoriasVod.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var selectedTitulo by remember { mutableStateOf<Titulo?>(null) }

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryId by remember { mutableStateOf<Int?>(null) }
    var expandedCategoryFilter by remember { mutableStateOf(false) }

    val filteredTitulos = titulos.filter { titulo ->
        val matchesSearch = titulo.nombre.contains(searchQuery, ignoreCase = true)
        val matchesCategory = selectedCategoryId == null || titulo.categoriaId == selectedCategoryId
        matchesSearch && matchesCategory
    }.sortedWith(compareBy({ it.orden }, { it.nombre }))
    
    val groupedTitulos = filteredTitulos.groupBy { it.categoriaId }

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
                    placeholder = { Text("Buscar título...") },
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
                groupedTitulos.forEach { (catId, titulosInCat) ->
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
                    
                    items(titulosInCat) { titulo ->
                        AdminMovieCard(
                            titulo = titulo, 
                            onEdit = {
                                selectedTitulo = titulo
                                showDialog = true
                            }
                        )
                    }
                }
                
                if (filteredTitulos.isEmpty()) {
                    item {
                        Text(
                            "No se encontraron títulos.",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
        
        FloatingActionButton(
            onClick = { 
                selectedTitulo = null
                showDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .padding(bottom = 80.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Añadir Título")
        }
        
        if (showDialog) {
            AdminMovieDialog(
                titulo = selectedTitulo,
                categorias = categorias,
                onDismiss = { showDialog = false },
                onSave = { titulo ->
                    if (selectedTitulo == null) {
                        viewModel.addTitulo(titulo)
                    } else {
                        viewModel.updateTitulo(titulo)
                    }
                    showDialog = false
                },
                onDelete = { titulo ->
                    viewModel.deleteTitulo(titulo.id)
                    showDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMovieCard(titulo: Titulo, onEdit: () -> Unit) {
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
            Icon(
                imageVector = Icons.Default.Movie,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (titulo.tipoContenido == com.example.data.model.TipoContenido.SERIE) "Serie" else "Película",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}
