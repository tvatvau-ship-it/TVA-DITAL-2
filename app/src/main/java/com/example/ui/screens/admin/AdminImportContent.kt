package com.example.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.AppViewModel

@Composable
fun AdminImportContent(viewModel: AppViewModel) {
    val trialUrl = "http://cdn-static-assets.net:80/playlist/3fNW2BYR2B/Ye38NWErCb/m3u_plus"
    var urlText by remember { mutableStateOf(trialUrl) }
    var replaceExisting by remember { mutableStateOf(true) }
    var isLoading by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CloudDownload,
            contentDescription = null,
            modifier = Modifier.size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        
        Text(
            text = "Importar Lista M3U / M3U8",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        
        Text(
            text = "Pegue la URL de su lista M3U para procesar e importar automáticamente todos los canales con sus categorías y logos.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.75f),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )

        // Preset Chips
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Listas sugeridas:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                FilterChip(
                    selected = urlText == trialUrl,
                    onClick = { urlText = trialUrl },
                    label = { Text("Lista de Prueba VIP (Argentina & Deportes)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
            ) {
                FilterChip(
                    selected = urlText == "https://www.m3u.cl/lista/AR.m3u",
                    onClick = { urlText = "https://www.m3u.cl/lista/AR.m3u" },
                    label = { Text("Lista M3U Chile/AR (Pública)") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.CloudDownload,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
        
        OutlinedTextField(
            value = urlText,
            onValueChange = { urlText = it },
            label = { Text("URL de la lista M3U") },
            placeholder = { Text("https://ejemplo.com/lista.m3u") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            enabled = !isLoading
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Reemplazar canales anteriores",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Switch(
                checked = replaceExisting,
                onCheckedChange = { replaceExisting = it },
                enabled = !isLoading
            )
        }
        
        Button(
            onClick = {
                isLoading = true
                statusMessage = null
                viewModel.importM3uFromUrl(
                    url = urlText.trim(),
                    clearExisting = replaceExisting
                ) { success, msg ->
                    isLoading = false
                    isError = !success
                    statusMessage = msg
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = urlText.isNotBlank() && !isLoading,
            shape = MaterialTheme.shapes.medium
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text("Descargando y procesando...")
            } else {
                Text("Importar Canales")
            }
        }

        OutlinedButton(
            onClick = {
                viewModel.reloadDefaultChannels(clearExisting = true)
                statusMessage = "¡Se han restablecido los canales por defecto (144 señales operativas)!"
                isError = false
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Restablecer Canales por Defecto")
        }

        // Status Card
        statusMessage?.let { msg ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isError) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = if (isError) Icons.Default.Error else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isError) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
        }
    }
}
