package com.example.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Categoria
import com.example.data.model.TipoContenido
import com.example.data.model.Titulo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMovieDialog(
    titulo: Titulo?,
    categorias: List<Categoria>,
    onDismiss: () -> Unit,
    onSave: (Titulo) -> Unit,
    onDelete: ((Titulo) -> Unit)? = null
) {
    var nombre by remember { mutableStateOf(titulo?.nombre ?: "") }
    var poster by remember { mutableStateOf(titulo?.poster ?: "") }
    var sinopsis by remember { mutableStateOf(titulo?.sinopsis ?: "") }
    var anio by remember { mutableStateOf(titulo?.anio?.toString() ?: "") }
    var streamUrl by remember { mutableStateOf(titulo?.streamUrl ?: "") }
    
    var expandedTipo by remember { mutableStateOf(false) }
    var selectedTipo by remember { mutableStateOf(titulo?.tipoContenido ?: TipoContenido.PELICULA) }
    
    var expandedCategoria by remember { mutableStateOf(false) }
    var selectedCategoriaId by remember { mutableStateOf(titulo?.categoriaId) }
    
    var oculto by remember { mutableStateOf(titulo?.oculto ?: false) }
    var esFavorito by remember { mutableStateOf(titulo?.esFavorito ?: false) }
    var orden by remember { mutableStateOf(titulo?.orden?.toString() ?: "0") }

    val selectedCategoria = categorias.find { it.id == selectedCategoriaId }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                Text(
                    text = if (titulo == null) "Nuevo Título" else "Editar Título",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = poster,
                        onValueChange = { poster = it },
                        label = { Text("URL del Poster") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = expandedTipo,
                        onExpandedChange = { expandedTipo = !expandedTipo }
                    ) {
                        OutlinedTextField(
                            value = selectedTipo.name,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Tipo de Contenido") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTipo) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedTipo,
                            onDismissRequest = { expandedTipo = false }
                        ) {
                            TipoContenido.values().forEach { tipo ->
                                DropdownMenuItem(
                                    text = { Text(tipo.name) },
                                    onClick = {
                                        selectedTipo = tipo
                                        expandedTipo = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = expandedCategoria,
                        onExpandedChange = { expandedCategoria = !expandedCategoria }
                    ) {
                        OutlinedTextField(
                            value = selectedCategoria?.nombre ?: "Seleccionar Categoría",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Categoría") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategoria) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedCategoria,
                            onDismissRequest = { expandedCategoria = false }
                        ) {
                            categorias.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.nombre) },
                                    onClick = {
                                        selectedCategoriaId = cat.id
                                        expandedCategoria = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = anio,
                        onValueChange = { if (it.isEmpty() || it.all { c -> c.isDigit() }) anio = it },
                        label = { Text("Año") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = streamUrl,
                        onValueChange = { streamUrl = it },
                        label = { Text("URL del Stream") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = sinopsis,
                        onValueChange = { sinopsis = it },
                        label = { Text("Sinopsis") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = orden,
                        onValueChange = { newValue ->
                            if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                                orden = newValue
                            }
                        },
                        label = { Text("Orden (número)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Oculto")
                        Switch(checked = oculto, onCheckedChange = { oculto = it })
                    }
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Favorito")
                        Switch(checked = esFavorito, onCheckedChange = { esFavorito = it })
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (titulo != null && onDelete != null) {
                        TextButton(
                            onClick = { onDelete(titulo) },
                            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                        ) {
                            Text("Eliminar")
                        }
                    }
                    Spacer(modifier = Modifier.weight(1f))
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar")
                    }
                    Button(
                        onClick = {
                            val newTitulo = Titulo(
                                id = titulo?.id ?: 0,
                                nombre = nombre,
                                poster = poster,
                                sinopsis = sinopsis,
                                anio = anio.toIntOrNull() ?: 2024,
                                categoriaId = selectedCategoriaId,
                                tipoContenido = selectedTipo,
                                fuenteLicencia = titulo?.fuenteLicencia ?: "",
                                esFavorito = esFavorito,
                                oculto = oculto,
                                orden = orden.toIntOrNull() ?: 0,
                                streamUrl = streamUrl
                            )
                            onSave(newTitulo)
                        },
                        enabled = nombre.isNotBlank() && streamUrl.isNotBlank()
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}
