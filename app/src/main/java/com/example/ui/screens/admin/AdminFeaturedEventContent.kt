package com.example.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.example.ui.AppViewModel
import com.example.ui.FeaturedEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminFeaturedEventContent(viewModel: AppViewModel) {
    val currentEvent by viewModel.featuredEvent.collectAsState()

    var title by remember { mutableStateOf(currentEvent?.title ?: "") }
    var imageUrl by remember { mutableStateOf(currentEvent?.imageUrl ?: "") }
    var time by remember { mutableStateOf(currentEvent?.time ?: "") }
    var streamUrl by remember { mutableStateOf(currentEvent?.streamUrl ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Configurar Evento Destacado (Hero Banner)", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text("Este evento aparecerá en grande arriba de todos los canales en la pantalla principal de TV en Vivo.", style = MaterialTheme.typography.bodyMedium)

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Título del Evento (ej. Boca vs River)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = imageUrl,
            onValueChange = { imageUrl = it },
            label = { Text("URL de Imagen (Flyer/Banner)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = time,
            onValueChange = { time = it },
            label = { Text("Hora (ej. Hoy 16:00 hs)") },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = streamUrl,
            onValueChange = { streamUrl = it },
            label = { Text("URL del streaming (.m3u8 a reproducir)") },
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (title.isNotBlank() && streamUrl.isNotBlank()) {
                        viewModel.saveFeaturedEvent(FeaturedEvent(title, imageUrl, time, streamUrl))
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Guardar Evento")
            }

            OutlinedButton(
                onClick = {
                    title = ""
                    imageUrl = ""
                    time = ""
                    streamUrl = ""
                    viewModel.saveFeaturedEvent(null)
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Quitar Evento")
            }
        }
    }
}
