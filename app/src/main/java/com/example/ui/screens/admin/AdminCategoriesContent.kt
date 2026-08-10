package com.example.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.FilterList
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
import com.example.data.model.Categoria
import com.example.data.model.TipoCategoria

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCategoriesContent(viewModel: AppViewModel) {
    val categoriasTv by viewModel.categoriasTv.collectAsState()
    val categoriasVod by viewModel.categoriasVod.collectAsState()
    val todasCategorias = categoriasTv + categoriasVod
    
    var showDialog by remember { mutableStateOf(false) }
    var selectedCategoria by remember { mutableStateOf<Categoria?>(null) }
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedTipoCategoria by remember { mutableStateOf<TipoCategoria?>(null) }
    var expandedTypeFilter by remember { mutableStateOf(false) }

    val filteredCategorias = todasCategorias.filter { cat ->
        val matchesSearch = cat.nombre.contains(searchQuery, ignoreCase = true)
        val matchesType = selectedTipoCategoria == null || cat.tipo == selectedTipoCategoria
        matchesSearch && matchesType
    }.sortedWith(compareBy({ it.orden }, { it.nombre }))
    
    val groupedCategorias = filteredCategorias.groupBy { it.tipo }

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
                    placeholder = { Text("Buscar categoría...") },
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
                    expanded = expandedTypeFilter,
                    onExpandedChange = { expandedTypeFilter = !expandedTypeFilter }
                ) {
                    IconButton(
                        onClick = { expandedTypeFilter = true },
                        modifier = Modifier
                            .background(
                                color = if (selectedTipoCategoria != null) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .menuAnchor()
                    ) {
                        Icon(
                            Icons.Default.FilterList,
                            contentDescription = "Filtrar por tipo",
                            tint = if (selectedTipoCategoria != null) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    ExposedDropdownMenu(
                        expanded = expandedTypeFilter,
                        onDismissRequest = { expandedTypeFilter = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Todos los tipos") },
                            onClick = {
                                selectedTipoCategoria = null
                                expandedTypeFilter = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Canales TV") },
                            onClick = {
                                selectedTipoCategoria = TipoCategoria.CANAL
                                expandedTypeFilter = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Películas/Series") },
                            onClick = {
                                selectedTipoCategoria = TipoCategoria.TITULO
                                expandedTypeFilter = false
                            }
                        )
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
                groupedCategorias.forEach { (tipo, categoriasInTipo) ->
                    item {
                        Text(
                            text = if (tipo == TipoCategoria.CANAL) "Canales TV" else "Películas / Series",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp)
                        )
                    }
                    
                    items(categoriasInTipo) { categoria ->
                        AdminCategoryCard(
                            categoria = categoria, 
                            onEdit = {
                                selectedCategoria = categoria
                                showDialog = true
                            }
                        )
                    }
                }
                
                if (filteredCategorias.isEmpty()) {
                    item {
                        Text(
                            "No se encontraron categorías.",
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }
            }
        }
        
        FloatingActionButton(
            onClick = { 
                selectedCategoria = null
                showDialog = true
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
                .padding(bottom = 80.dp),
            containerColor = MaterialTheme.colorScheme.primary
        ) {
            Icon(Icons.Default.Add, contentDescription = "Añadir Categoría")
        }
        
        if (showDialog) {
            AdminCategoryDialog(
                categoria = selectedCategoria,
                onDismiss = { showDialog = false },
                onSave = { cat ->
                    if (selectedCategoria == null) {
                        viewModel.addCategoria(cat)
                    } else {
                        viewModel.updateCategoria(cat)
                    }
                    showDialog = false
                },
                onDelete = { cat ->
                    viewModel.deleteCategoria(cat.id)
                    showDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCategoryCard(categoria: Categoria, onEdit: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onEdit,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Category,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = categoria.nombre,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tipo: ${categoria.tipo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                )
            }
        }
    }
}
