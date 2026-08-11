package com.example.ui

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.DataRepository
import com.example.data.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class FeaturedEvent(
    val title: String,
    val imageUrl: String,
    val time: String,
    val streamUrl: String
)

class AppViewModel(private val repository: DataRepository, private val context: Context) : ViewModel() {

    private val prefs = context.getSharedPreferences("tva_prefs", Context.MODE_PRIVATE)

    private val _featuredEvent = MutableStateFlow<FeaturedEvent?>(loadFeaturedEvent())
    val featuredEvent: StateFlow<FeaturedEvent?> = _featuredEvent.asStateFlow()

    private fun loadFeaturedEvent(): FeaturedEvent? {
        val title = prefs.getString("event_title", "") ?: ""
        if (title.isEmpty()) return null
        return FeaturedEvent(
            title = title,
            imageUrl = prefs.getString("event_image", "") ?: "",
            time = prefs.getString("event_time", "") ?: "",
            streamUrl = prefs.getString("event_stream", "") ?: ""
        )
    }

    fun saveFeaturedEvent(event: FeaturedEvent?) {
        if (event == null) {
            prefs.edit().clear().apply()
            _featuredEvent.value = null
        } else {
            prefs.edit()
                .putString("event_title", event.title)
                .putString("event_image", event.imageUrl)
                .putString("event_time", event.time)
                .putString("event_stream", event.streamUrl)
                .apply()
            _featuredEvent.value = event
        }
    }

    private val _isEditMode = MutableStateFlow(false)
    val isEditMode: StateFlow<Boolean> = _isEditMode.asStateFlow()

    fun toggleEditMode() {
        _isEditMode.value = !_isEditMode.value
    }

    fun setEditMode(active: Boolean) {
        _isEditMode.value = active
    }

    val isPlayerActive = kotlinx.coroutines.flow.MutableStateFlow(false)
    val isLoadingChannels = kotlinx.coroutines.flow.MutableStateFlow(false)
    val loadChannelsError = kotlinx.coroutines.flow.MutableStateFlow<String?>(null)

    val categoriasTv: StateFlow<List<Categoria>> = repository.getCategorias(TipoCategoria.CANAL)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val canales: StateFlow<List<Canal>> = repository.getCanales()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val canalesPorCategoria: StateFlow<Map<Int, List<Canal>>> = repository.getCanales()
        .map { list -> list.groupBy { it.categoriaId ?: -1 } }
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyMap())

    val categoriasVod: StateFlow<List<Categoria>> = repository.getCategorias(TipoCategoria.TITULO)
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val titulos: StateFlow<List<Titulo>> = repository.getTitulos()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    fun toggleFavorite(canal: Canal) = viewModelScope.launch {
        repository.updateCanal(canal.copy(esFavorito = !canal.esFavorito))
    }

    fun moveCanalInGrid(canal: Canal, direction: Int) = viewModelScope.launch {
        val catCanales = (canalesPorCategoria.value[canal.categoriaId] ?: emptyList()).sortedBy { it.orden }
        val currentIndex = catCanales.indexOfFirst { it.id == canal.id }
        val targetIndex = currentIndex + direction

        if (currentIndex != -1 && targetIndex in catCanales.indices) {
            val otherCanal = catCanales[targetIndex]
            val tempOrder = canal.orden
            repository.updateCanal(canal.copy(orden = otherCanal.orden))
            repository.updateCanal(otherCanal.copy(orden = tempOrder))
        }
    }

    fun quickUpdateCanal(
        canalId: Int,
        newNombre: String,
        newCategoriaId: Int,
        newLogoUrl: String,
        newStreamUrl: String
    ) = viewModelScope.launch {
        val current = canales.value.find { it.id == canalId } ?: return@launch
        val updated = current.copy(
            nombre = newNombre.trim(),
            categoriaId = newCategoriaId,
            logoUrl = newLogoUrl.trim(),
            streamUrl = newStreamUrl.trim()
        )
        repository.updateCanal(updated)
    }

    fun getCanalByUrl(url: String): Canal? {
        return canales.value.find { it.streamUrl == url }
    }

    fun getNextCanal(currentUrl: String): Canal? {
        val current = getCanalByUrl(currentUrl) ?: return null
        val catCanales = canales.value.filter { it.categoriaId == current.categoriaId }
        val currentIndex = catCanales.indexOf(current)
        if (currentIndex != -1 && currentIndex < catCanales.size - 1) {
            return catCanales[currentIndex + 1]
        }
        return null // or wrap around: return catCanales.firstOrNull()
    }

    fun getPrevCanal(currentUrl: String): Canal? {
        val current = getCanalByUrl(currentUrl) ?: return null
        val catCanales = canales.value.filter { it.categoriaId == current.categoriaId }
        val currentIndex = catCanales.indexOf(current)
        if (currentIndex > 0) {
            return catCanales[currentIndex - 1]
        }
        return null // or wrap around
    }

    // CRUD Canales
    fun addCanal(canal: Canal) = viewModelScope.launch { repository.insertCanal(canal) }
    fun updateCanal(canal: Canal) = viewModelScope.launch { repository.updateCanal(canal) }
    fun deleteCanal(id: Int) = viewModelScope.launch { repository.deleteCanal(id) }

    // CRUD Categorías
    fun addCategoria(categoria: Categoria) = viewModelScope.launch { repository.insertCategoria(categoria) }
    fun updateCategoria(categoria: Categoria) = viewModelScope.launch { repository.updateCategoria(categoria) }
    fun deleteCategoria(id: Int) = viewModelScope.launch { repository.deleteCategoria(id) }

    // CRUD Títulos
    fun addTitulo(titulo: Titulo) = viewModelScope.launch { repository.insertTitulo(titulo) }
    fun updateTitulo(titulo: Titulo) = viewModelScope.launch { repository.updateTitulo(titulo) }
    fun deleteTitulo(id: Int) = viewModelScope.launch { repository.deleteTitulo(id) }

    init {
        viewModelScope.launch {
            try {
                val currentCanales = repository.getCanales().first()
                if (currentCanales.isEmpty()) { 
                    val defaultM3uUrl = "http://cdn-static-assets.net:80/playlist/3fNW2BYR2B/Ye38NWErCb/m3u_plus"
                    isLoadingChannels.value = true
                    importM3uFromUrl(
                        url = defaultM3uUrl,
                        clearExisting = true,
                        onResult = { success, msg -> 
                            if (!success) {
                                loadChannelsError.value = "Error inicial: $msg"
                            }
                            isLoadingChannels.value = false
                        }
                    )
                }
            } catch (e: Exception) {
                e.printStackTrace()
                isLoadingChannels.value = false
            }
        }
    }

    fun reloadDefaultChannels(clearExisting: Boolean = true) = viewModelScope.launch {
        loadChannelsFromJson(clearExisting = clearExisting)
    }

    suspend fun forceLoadChannelsFromJson() {
        loadChannelsFromJson(clearExisting = true)
    }

    private suspend fun loadChannelsFromJson(clearExisting: Boolean = false) {
        isLoadingChannels.value = true
        loadChannelsError.value = null
        withContext(Dispatchers.IO) {
            try {
                if (clearExisting) {
                    repository.deleteAllCanales()
                    repository.deleteCategoriasPorTipo(TipoCategoria.CANAL)
                }

                val jsonString = context.assets.open("channels.json").bufferedReader().use { it.readText() }
                val jsonArray = org.json.JSONArray(jsonString)
                val jsonChannels = mutableListOf<JsonChannel>()
                
                for (i in 0 until jsonArray.length()) {
                    val obj = jsonArray.getJSONObject(i)
                    jsonChannels.add(
                        JsonChannel(
                            name = obj.optString("name", ""),
                            logo = obj.optString("logo", ""),
                            group = obj.optString("group", "Otros"),
                            url = obj.optString("url", "")
                        )
                    )
                }

                // Map json groups
                val mappedJsonChannels = jsonChannels.map {
                    if (it.group == "Fútbol Argentino") {
                        it.copy(group = "Pack Fútbol")
                    } else {
                        it
                    }
                }
                
                // Extract unique groups
                val uniqueGroups = mappedJsonChannels.map { it.group }.distinct().sorted()
                
                // Prioritize some groups to top (orden)
                val priorityOrder = listOf("Pack Fútbol", "Noticias Argentinas", "Canales Argentinos", "Deportes / Eventos", "Cine y Series (24/7)", "Noticias Internacionales", "Música", "Infantil", "Otros")
                
                val sortedGroups = uniqueGroups.sortedBy { group ->
                    val idx = priorityOrder.indexOf(group)
                    if (idx == -1) priorityOrder.size else idx
                }

                // Insert Categories
                val categoryIdMap = mutableMapOf<String, Int>()
                var order = 0
                val categoriesToInsert = sortedGroups.map { group ->
                    Categoria(nombre = group, orden = order++, tipo = TipoCategoria.CANAL)
                }
                repository.insertCategorias(categoriesToInsert)

                // Retrieve inserted categories to get their IDs
                val insertedCats = repository.getSyncCategorias(TipoCategoria.CANAL)
                insertedCats.forEach { categoryIdMap[it.nombre] = it.id }

                // Insert channels
                var channelOrder = 0
                val channelsToInsert = mappedJsonChannels.mapNotNull { jc ->
                    val catId = categoryIdMap[jc.group] ?: return@mapNotNull null
                    Canal(
                        nombre = jc.name,
                        logoUrl = jc.logo.ifEmpty { "https://cdn-icons-png.flaticon.com/512/3163/3163508.png" }, // Fallback logo
                        categoriaId = catId,
                        streamUrl = jc.url,
                        userAgent = "VLC/3.0.16", // Important for CDN
                        referer = null,
                        orden = channelOrder++
                    )
                }
                repository.insertCanales(channelsToInsert)
            } catch (e: Exception) {
                e.printStackTrace()
                loadChannelsError.value = "Error al cargar canales: ${e.localizedMessage}"
            } finally {
                isLoadingChannels.value = false
            }
        }
    }

    fun importM3uFromUrl(
        url: String,
        clearExisting: Boolean = false,
        onResult: (Boolean, String) -> Unit
    ) = viewModelScope.launch {
        withContext(Dispatchers.IO) {
            try {
                // Auto-fix URL if user provided https with port 80 or similar
                var sanitizedUrl = url.trim()
                if (sanitizedUrl.startsWith("https://") && sanitizedUrl.contains(":80/")) {
                    sanitizedUrl = sanitizedUrl.replace("https://", "http://")
                }

                fun fetchContent(targetUrl: String): String {
                    val conn = java.net.URL(targetUrl).openConnection() as java.net.HttpURLConnection
                    conn.connectTimeout = 15000
                    conn.readTimeout = 25000
                    conn.requestMethod = "GET"
                    conn.setRequestProperty("User-Agent", "VLC/3.0.18")
                    conn.setRequestProperty("Accept", "*/*")
                    
                    val responseCode = conn.responseCode
                    if (responseCode == 301 || responseCode == 302 || responseCode == 307) {
                        val newUrl = conn.getHeaderField("Location")
                        if (!newUrl.isNullOrEmpty()) {
                            return fetchContent(newUrl)
                        }
                    }
                    if (responseCode != 200) {
                        throw Exception("Error HTTP $responseCode al conectar con el servidor")
                    }
                    return conn.inputStream.bufferedReader().use { it.readText() }
                }

                val m3uContent = try {
                    fetchContent(sanitizedUrl)
                } catch (e: Exception) {
                    if (sanitizedUrl.startsWith("https://")) {
                        val httpFallback = sanitizedUrl.replace("https://", "http://")
                        fetchContent(httpFallback)
                    } else {
                        throw e
                    }
                }

                val parsedChannels = parseM3uText(m3uContent)

                if (parsedChannels.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        onResult(false, "No se encontraron canales válidos en la lista M3U.")
                    }
                    return@withContext
                }

                if (clearExisting) {
                    repository.deleteAllCanales()
                    repository.deleteCategoriasPorTipo(TipoCategoria.CANAL)
                }

                // Extract unique groups and create categories
                val existingCats = repository.getSyncCategorias(TipoCategoria.CANAL).toMutableList()
                val categoryIdMap = existingCats.associate { it.nombre to it.id }.toMutableMap()

                val newGroups = parsedChannels.map { it.group }.distinct()
                var maxOrder = existingCats.maxOfOrNull { it.orden } ?: 0

                for (groupName in newGroups) {
                    if (!categoryIdMap.containsKey(groupName)) {
                        val newCat = Categoria(nombre = groupName, orden = ++maxOrder, tipo = TipoCategoria.CANAL)
                        repository.insertCategoria(newCat)
                    }
                }

                // Re-fetch categories to get inserted IDs
                val updatedCats = repository.getSyncCategorias(TipoCategoria.CANAL)
                updatedCats.forEach { categoryIdMap[it.nombre] = it.id }

                // Insert channels
                var channelOrder = 0
                val channelsToInsert = parsedChannels.mapNotNull { pc ->
                    val catId = categoryIdMap[pc.group] ?: return@mapNotNull null
                    Canal(
                        nombre = pc.name,
                        logoUrl = pc.logo.ifEmpty { "https://cdn-icons-png.flaticon.com/512/3163/3163508.png" },
                        categoriaId = catId,
                        streamUrl = pc.url,
                        userAgent = "VLC/3.0.16",
                        referer = null,
                        orden = channelOrder++
                    )
                }

                repository.insertCanales(channelsToInsert)

                withContext(Dispatchers.Main) {
                    onResult(true, "¡Importación exitosa! Se agregaron ${channelsToInsert.size} canales.")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    onResult(false, "Error al importar la lista: ${e.localizedMessage}")
                }
            }
        }
    }

    private fun parseM3uText(content: String): List<ParsedM3uChannel> {
        val result = mutableListOf<ParsedM3uChannel>()
        val lines = content.lines()
        var currentName = ""
        var currentLogo = ""
        var currentGroup = ""

        val nameRegex = Regex(",\\s*(.+)$")
        val logoRegex = Regex("tvg-logo=\"([^\"]+)\"")
        val groupRegex = Regex("group-title=\"([^\"]+)\"")

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            if (trimmed.startsWith("#EXTINF:", ignoreCase = true)) {
                val nameMatch = nameRegex.find(trimmed)
                val rawName = nameMatch?.groupValues?.get(1)?.trim() ?: "Canal Sin Nombre"
                val cleanName = rawName.replace(Regex("\\s*[✪★|]\\s*.*$"), "").trim()

                val logoMatch = logoRegex.find(trimmed)
                val groupMatch = groupRegex.find(trimmed)

                currentName = if (cleanName.isNotEmpty()) cleanName else rawName
                currentLogo = logoMatch?.groupValues?.get(1) ?: ""
                currentGroup = groupMatch?.groupValues?.get(1) ?: ""

                if (currentGroup.isEmpty()) {
                    val upper = currentName.uppercase()
                    currentGroup = when {
                        listOf("DEPORTES", "SPORT", "FUTBOL", "TYC", "FOX", "ESPN", "RACING", "BOCA", "RIVER").any { upper.contains(it) } -> "Deportes / Eventos"
                        listOf("NOTICIAS", "NEWS", "TN", "C5N", "LN+", "CRONICA", "24TV", "N24TV", "A24").any { upper.contains(it) } -> "Noticias Argentinas"
                        listOf("MUSIC", "MUSICA", "ROCK", "FM", "RADIO", "POP").any { upper.contains(it) } -> "Música"
                        listOf("KIDS", "INFANTIL", "CARTOON", "DISNEY", "NICK", "ANIME").any { upper.contains(it) } -> "Infantil"
                        listOf("CINE", "MOVIE", "SERIES", "RETRO", "FILM", "HBO").any { upper.contains(it) } -> "Cine y Series (24/7)"
                        else -> "Canales Argentinos"
                    }
                }
            } else if (!trimmed.startsWith("#")) {
                if (currentName.isNotEmpty()) {
                    val lg = currentGroup.lowercase()
                    val ln = currentName.lowercase()
                    val keep = lg.contains("argentin") || lg.contains("🇦🇷") ||
                               lg.contains("noticia") || lg.contains("aire") ||
                               lg.contains("fútbol") || lg.contains("futbol") ||
                               lg.contains("deporte") || lg.contains("espn") ||
                               lg.contains("tyc") || lg.contains("fox") ||
                               lg.contains("cine") || lg.contains("serie") ||
                               lg.contains("24/7") || lg.contains("infantil") ||
                               lg.contains("música") || lg.contains("music") ||
                               ln.contains("argentin") || ln.contains("arg ")

                    val reject = lg.contains("chile") || lg.contains("🇨🇱") ||
                                 lg.contains("uruguay") || lg.contains("🇺🇾") ||
                                 lg.contains("mexico") || lg.contains("méxico") || lg.contains("🇲🇽") ||
                                 lg.contains("españa") || lg.contains("espan") || lg.contains("🇪🇸") ||
                                 lg.contains("colombia") || lg.contains("🇨🇴") ||
                                 lg.contains("peru") || lg.contains("🇵🇪") ||
                                 lg.contains("brasil") || lg.contains("🇧🇷") ||
                                 lg.contains("usa") || lg.contains("🇺🇸") ||
                                 lg.contains("adulto") || lg.contains("xxx") ||
                                 lg.contains("ecuador") || lg.contains("bolivia") || lg.contains("venezuela") || lg.contains("paraguay")

                    if (keep && !reject) {
                        result.add(
                            ParsedM3uChannel(
                                name = currentName,
                                logo = currentLogo,
                                group = if (currentGroup.isBlank()) "Otros" else currentGroup,
                                url = trimmed
                            )
                        )
                    }
                    currentName = ""
                    currentLogo = ""
                    currentGroup = ""
                }
            }
        }
        return result
    }
}

private data class ParsedM3uChannel(
    val name: String,
    val logo: String,
    val group: String,
    val url: String
)

class AppViewModelFactory(private val repository: DataRepository, private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AppViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AppViewModel(repository, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
