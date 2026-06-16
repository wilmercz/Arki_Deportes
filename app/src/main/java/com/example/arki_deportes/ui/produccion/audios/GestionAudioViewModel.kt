package com.example.arki_deportes.ui.produccion.audios

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.arki_deportes.data.model.AudioResource
import com.example.arki_deportes.data.repository.CloudinaryUploader
import com.example.arki_deportes.data.repository.FirebaseCatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import android.provider.OpenableColumns
import android.util.Log

data class AudioUiState(
    val audios: List<AudioResource> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUploading: Boolean = false,
    val uploadProgress: String = "",
    val carpetasVinculadas: Map<String, String> = emptyMap() // Deporte -> URI de Carpeta
)

class GestionAudioViewModel(
    private val repository: FirebaseCatalogRepository,
    private val cloudinaryUploader: CloudinaryUploader,
    private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AudioUiState())
    val uiState: StateFlow<AudioUiState> = _uiState.asStateFlow()

    init {
        loadAudios()
        cargarConfiguracionCarpetas()
    }

    private fun loadAudios() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.observeAudios().collect { audios ->
                _uiState.update { it.copy(audios = audios, isLoading = false) }
            }
        }
    }

    private fun cargarConfiguracionCarpetas() {
        val prefs = context.getSharedPreferences("ConfigAudio", Context.MODE_PRIVATE)
        val vinculados = mutableMapOf<String, String>()
        listOf("FUTBOL", "BASQUET", "AUTOMOVILISMO", "CICLISMO", "GENERAL").forEach { dep ->
            prefs.getString("folder_$dep", null)?.let { uri ->
                vinculados[dep] = uri
            }
        }
        _uiState.update { it.copy(carpetasVinculadas = vinculados) }
    }

    fun vincularCarpetaLocal(deporte: String, uri: Uri) {
        viewModelScope.launch {
            try {
                // Persistir permiso de acceso a la carpeta
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
                
                val prefs = context.getSharedPreferences("ConfigAudio", Context.MODE_PRIVATE)
                prefs.edit().putString("folder_$deporte", uri.toString()).apply()
                cargarConfiguracionCarpetas()
            } catch (e: Exception) {
                Log.e("GestionAudioVM", "Error al vincular carpeta: ${e.message}")
            }
        }
    }

    fun desvincularCarpetaLocal(deporte: String) {
        viewModelScope.launch {
            val prefs = context.getSharedPreferences("ConfigAudio", Context.MODE_PRIVATE)
            prefs.edit().remove("folder_$deporte").apply()
            cargarConfiguracionCarpetas()
        }
    }

    /**
     * Sube múltiples archivos a Cloudinary y los registra en Firebase.
     */
    fun uploadAudiosMasivo(uris: List<Uri>, tipo: String, categoriaBase: String, deporte: String, customId: String? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true) }
            var exitos = 0
            
            uris.forEachIndexed { index, uri ->
                try {
                    _uiState.update { it.copy(uploadProgress = "${index + 1}/${uris.size}") }
                    
                    val rawName = getFileNameFromUri(uri) ?: "Audio_${System.currentTimeMillis()}"
                    val cleanName = cleanAudioName(rawName)
                    
                    val finalCat = if (uris.size > 1 && categoriaBase.isBlank()) cleanName else categoriaBase

                    val downloadUrl = cloudinaryUploader.uploadAudioFile(uri, cleanName)

                    val audio = AudioResource(
                        id = if (uris.size == 1) (customId ?: "") else "", 
                        nombre = cleanName,
                        url = downloadUrl,
                        tipo = tipo,
                        categoria = finalCat,
                        deporte = deporte
                    )
                    repository.saveAudio(audio)
                    exitos++
                } catch (e: Exception) {
                    Log.e("GestionAudioVM", "Error subiendo $uri: ${e.message}")
                }
            }
            
            _uiState.update { it.copy(isUploading = false, uploadProgress = "") }
        }
    }

    private fun getFileNameFromUri(uri: Uri): String? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst()) cursor.getString(nameIndex).substringBeforeLast(".") else null
        }
    }

    private fun cleanAudioName(name: String): String {
        return name.replace(Regex("^[0-9\\s._-]+"), "").trim().uppercase()
    }

    fun deleteAudio(audioId: String) {
        viewModelScope.launch {
            try {
                repository.deleteAudio(audioId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun playInOverlay(audio: AudioResource) {
        viewModelScope.launch {
            try {
                repository.reproducirAudioEnOverlay(audio)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al enviar a web: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

class GestionAudioViewModelFactory(
    private val repository: FirebaseCatalogRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GestionAudioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GestionAudioViewModel(
                repository = repository,
                cloudinaryUploader = CloudinaryUploader(context),
                context = context
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
