package com.example.arki_deportes.ui.envivo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.arki_deportes.data.model.PartidoEnVivo
import com.example.arki_deportes.data.repository.FirebaseCatalogRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PartidosEnVivoUiState(
    val partidos: List<PartidoEnVivo> = emptyList(),
    val isLoading: Boolean = true
)

class PartidosEnVivoViewModel(
    private val repository: FirebaseCatalogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PartidosEnVivoUiState())
    val uiState: StateFlow<PartidosEnVivoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observePartidosJugandoseGlobal().collect { lista ->
                _uiState.update { it.copy(partidos = lista, isLoading = false) }
            }
        }
    }
}

/**
 * Factory para proveer el repositorio al ViewModel.
 */
class PartidosEnVivoViewModelFactory(
    private val repository: FirebaseCatalogRepository
) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PartidosEnVivoViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return PartidosEnVivoViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}