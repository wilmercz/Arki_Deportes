package com.example.arki_deportes.ui.produccion.audios

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.arki_deportes.data.model.AudioResource
import com.example.arki_deportes.data.repository.FirebaseCatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AudioUiState(
    val audios: List<AudioResource> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUploading: Boolean = false
)

class GestionAudioViewModel(
    private val repository: FirebaseCatalogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AudioUiState())
    val uiState: StateFlow<AudioUiState> = _uiState.asStateFlow()

    init {
        loadAudios()
    }

    private fun loadAudios() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.observeAudios().collect { audios ->
                _uiState.update { it.copy(audios = audios, isLoading = false) }
            }
        }
    }

    fun uploadAudio(uri: Uri, fileName: String, tipo: String, categoria: String, deporte: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true) }
            try {
                val downloadUrl = repository.uploadAudioFile(uri, fileName)
                val audio = AudioResource(
                    nombre = fileName,
                    url = downloadUrl,
                    tipo = tipo,
                    categoria = categoria,
                    deporte = deporte
                )
                repository.saveAudio(audio)
                _uiState.update { it.copy(isUploading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isUploading = false, error = e.message) }
            }
        }
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

class GestionAudioViewModelFactory(private val repository: FirebaseCatalogRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GestionAudioViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GestionAudioViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
