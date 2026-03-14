package com.example.arki_deportes.ui.produccion.banners

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.arki_deportes.data.model.BannerResource
import com.example.arki_deportes.data.repository.CloudinaryUploader
import com.example.arki_deportes.data.repository.FirebaseCatalogRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class BannerUiState(
    val banners: List<BannerResource> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val isUploading: Boolean = false
)

class GestionBannerViewModel(
    private val repository: FirebaseCatalogRepository,
    private val cloudinaryUploader: CloudinaryUploader   // ← nuevo
) : ViewModel() {

    private val _uiState = MutableStateFlow(BannerUiState())
    val uiState: StateFlow<BannerUiState> = _uiState.asStateFlow()

    init {
        loadBanners()
    }

    private fun loadBanners() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            repository.observeBanners().collect { banners ->
                _uiState.update { it.copy(banners = banners, isLoading = false) }
            }
        }
    }

    fun saveBanner(banner: BannerResource, imageUri: Uri? = null, videoUri: Uri? = null) {
        viewModelScope.launch {
            _uiState.update { it.copy(isUploading = true) }
            try {
                var finalBanner = banner

                // ── Subida imagen a Cloudinary (reemplaza uploadBannerMedia_Storage) ──
                if (imageUri != null) {
                    val imageUrl = cloudinaryUploader.uploadBannerMedia(
                        fileUri = imageUri,
                        folder = "IMAGENES",
                        fileName = "${banner.nombre}_img"
                    )
                    finalBanner = finalBanner.copy(urlImagen = imageUrl)
                }

                // ── Subida video a Cloudinary (reemplaza uploadBannerMedia_Storage) ──
                if (videoUri != null) {
                    val videoUrl = cloudinaryUploader.uploadBannerMedia(
                        fileUri = videoUri,
                        folder = "VIDEOS",
                        fileName = "${banner.nombre}_vid"
                    )
                    finalBanner = finalBanner.copy(urlVideo = videoUrl)
                }

                repository.saveBanner(finalBanner)
                _uiState.update { it.copy(isUploading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isUploading = false, error = e.message) }
            }
        }
    }

    fun deleteBanner(bannerId: String) {
        viewModelScope.launch {
            try {
                repository.deleteBanner(bannerId)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }

    fun publishInOverlay(banner: BannerResource?) {
        viewModelScope.launch {
            try {
                repository.publicarBannerEnOverlay(banner)
            } catch (e: Exception) {
                _uiState.update { it.copy(error = "Error al enviar a web: ${e.message}") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}

class GestionBannerViewModelFactory(
    private val repository: FirebaseCatalogRepository,
    private val context: Context                         // ← nuevo (para CloudinaryUploader)
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GestionBannerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return GestionBannerViewModel(
                repository = repository,
                cloudinaryUploader = CloudinaryUploader(context)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}