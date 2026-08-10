package com.example.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.model.Categoria
import com.example.data.model.TipoCategoria

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCategoryDialog(
    categoria: Categoria?,
    onDismiss: () -> Unit,
    onSave: (Categoria) -> Unit,
    onDelete: ((Categoria) -> Unit)? = null
) {
    var nombre by remember { mutableStateOf(categoria?.nombre ?: "") }
    var expandedTipo by remember { mutableStateOf(false) }
    var selectedTipo by remember { mutableStateOf(categoria?.tipo ?: TipoCategoria.CANAL) }
    var orden by remember { mutableStateOf(categoria?.orden?.toString() ?: "0") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = if (categoria == null) "Nueva Categoría" else "Editar Categoría",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre de la Categoría") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))

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
                        TipoCategoria.values().forEach { tipo ->
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
                
                Spacer(modifier = Modifier.height(16.dp))
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
                
                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    if (categoria != null && onDelete != null) {
                        TextButton(
                            onClick = { onDelete(categoria) },
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
                            val newCat = Categoria(
                                id = categoria?.id ?: 0,
                                nombre = nombre,
                                tipo = selectedTipo,
                                orden = orden.toIntOrNull() ?: 0
                            )
                            onSave(newCat)
                        },
                        enabled = nombre.isNotBlank()
                    ) {
                        Text("Guardar")
                    }
                }
            }
        }
    }
}
