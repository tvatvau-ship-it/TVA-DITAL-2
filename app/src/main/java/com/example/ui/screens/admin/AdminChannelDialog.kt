package com.example.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Canal
import com.example.data.model.Categoria

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminChannelDialog(
    canal: Canal?,
    categorias: List<Categoria>,
    onDismiss: () -> Unit,
    onSave: (Canal) -> Unit,
    onDelete: ((Canal) -> Unit)? = null
) {
    var nombre by remember { mutableStateOf(canal?.nombre ?: "") }
    var logoUrl by remember { mutableStateOf(canal?.logoUrl ?: "") }
    var streamUrl by remember { mutableStateOf(canal?.streamUrl ?: "") }
    var userAgent by remember { mutableStateOf(canal?.userAgent ?: "") }
    var referer by remember { mutableStateOf(canal?.referer ?: "") }
    var oculto by remember { mutableStateOf(canal?.oculto ?: false) }
    var esFavorito by remember { mutableStateOf(canal?.esFavorito ?: false) }
    var orden by remember { mutableStateOf(canal?.orden?.toString() ?: "0") }
    
    var expandedCategoria by remember { mutableStateOf(false) }
    var selectedCategoriaId by remember { mutableStateOf(canal?.categoriaId) }
    
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
                    text = if (canal == null) "Nuevo Canal" else "Editar Canal",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = logoUrl,
                    onValueChange = { logoUrl = it },
                    label = { Text("URL del Logo") },
                    modifier = Modifier.fillMaxWidth()
                )
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
                    value = streamUrl,
                    onValueChange = { streamUrl = it },
                    label = { Text("URL del Stream") },
                    modifier = Modifier.fillMaxWidth()
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
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text("Oculto")
                    Switch(checked = oculto, onCheckedChange = { oculto = it })
                }
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                ) {
                    Text("Favorito")
                    Switch(checked = esFavorito, onCheckedChange = { esFavorito = it })
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (canal != null && onDelete != null) {
                        TextButton(
                            onClick = { onDelete(canal) },
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
                            val newCanal = Canal(
                                id = canal?.id ?: 0,
                                nombre = nombre,
                                logoUrl = logoUrl,
                                streamUrl = streamUrl,
                                userAgent = userAgent,
                                referer = referer,
                                oculto = oculto,
                                esFavorito = esFavorito,
                                categoriaId = selectedCategoriaId ?: 0,
                                orden = orden.toIntOrNull() ?: 0
                            )
                            onSave(newCanal)
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
