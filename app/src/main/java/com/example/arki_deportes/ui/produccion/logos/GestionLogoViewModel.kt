package com.example.arki_deportes.ui.produccion.logos

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.arki_deportes.data.model.LogoResource
import com.example.arki_deportes.data.repository.CloudinaryUploader
import com.example.arki_deportes.data.repository.FirebaseCatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LogoUiState(
    val logos: List<LogoResource> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUploading: Boolean = false
)

class GestionLogoViewModel(
    private val repository: FirebaseCatalogRepository,
    private val cloudinaryUploader: CloudinaryUploader
) : ViewModel() {

    private val _uiState = MutableStateFlow(LogoUiState())
    val uiState: StateFlow<LogoUiState> = _uiState.asStateFlow()

    init {
        loadLogos()
    }

    private fun loadLogos() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.observeLogos().collect { logos ->
                _uiState.update { it.copy(logos = logos, isLoading = false) }
            }
        }
    }

    fun saveLogo(nombre: String, imageUri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true) }
            try {
                val imageUrl = cloudinaryUploader.uploadLogo(
                    fileUri = imageUri,
                    fileName = "${nombre.lowercase().replace(" ", "_")}_logo"
                )
                
                val newLogo = LogoResource(
                    nombre = nombre,
                    url = imageUrl,
                    onAir = false
                )

                repository.saveLogo(newLogo)
                _uiState.update { it.copy(isUploading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isUploading = false, error = e.message) }
            }
        }
    }

    fun deleteLogo(logoId: String) {
        viewModelScope.launch {
            try {
                repository.deleteLogo(logoId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun toggleOnAir(logo: LogoResource) {
        viewModelScope.launch {
            try {
                repository.toggleLogoOnAir(logo)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al cambiar estado Al Aire: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

class GestionLogoViewModelFactory(
    private val repository: FirebaseCatalogRepository,
    private val context: Context
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GestionLogoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GestionLogoViewModel(
                repository = repository,
                cloudinaryUploader = CloudinaryUploader(context)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
