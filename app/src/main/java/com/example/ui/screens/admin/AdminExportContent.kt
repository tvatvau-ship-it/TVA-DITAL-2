package com.example.ui.screens.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.AppViewModel
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun AdminExportContent(viewModel: AppViewModel) {
    val canales by viewModel.canales.collectAsState()
    val categorias by viewModel.categoriasTv.collectAsState()
    val featuredEvent by viewModel.featuredEvent.collectAsState()

    val clipboardManager = LocalClipboardManager.current
    var exportJson by remember { mutableStateOf("") }
    var copied by remember { mutableStateOf(false) }

    LaunchedEffect(canales, categorias, featuredEvent) {
        try {
            val root = JSONObject()

            // Evento Destacado
            val eventObj = JSONObject()
            if (featuredEvent != null) {
                eventObj.put("title", featuredEvent!!.title)
                eventObj.put("imageUrl", featuredEvent!!.imageUrl)
                eventObj.put("time", featuredEvent!!.time)
                eventObj.put("streamUrl", featuredEvent!!.streamUrl)
            }
            root.put("featuredEvent", eventObj)

            // Canales
            val catMap = categorias.associateBy { it.id }
            val canalesArray = JSONArray()
            for (c in canales) {
                val cObj = JSONObject()
                cObj.put("name", c.nombre)
                cObj.put("logo", c.logoUrl)
                cObj.put("group", catMap[c.categoriaId]?.nombre ?: "Otros")
                cObj.put("url", c.streamUrl)
                canalesArray.put(cObj)
            }
            root.put("canales", canalesArray)

            exportJson = root.toString(4)
        } catch (e: Exception) {
            exportJson = "Error generando JSON: ${e.message}"
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Guardar Cambios en el APK", style = MaterialTheme.typography.titleLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Los cambios visuales que haces en la app se guardan solo en este dispositivo. Para que estos cambios vengan por defecto en el APK que le vas a vender a tus clientes, haz lo siguiente:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text("1. Acomoda los canales y el evento destacado como quieras.\n2. Toca el botón 'Copiar Código' aquí abajo.\n3. Pega ese código en el chat para que yo (la IA) lo inserte en el código fuente de la app.\n4. ¡Listo! Al compilar el APK, nacerá con esa configuración permanente.", style = MaterialTheme.typography.bodySmall)
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = {
                clipboardManager.setText(AnnotatedString(exportJson))
                copied = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copiar")
            Spacer(modifier = Modifier.width(8.dp))
            Text(if (copied) "¡Copiado! Pégalo en el chat de IA" else "Copiar Código")
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        OutlinedTextField(
            value = exportJson,
            onValueChange = {},
            readOnly = true,
            modifier = Modifier.fillMaxSize(),
            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace, fontSize = 10.sp)
        )
    }
}
