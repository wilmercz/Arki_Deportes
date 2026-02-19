package com.example.arki_deportes.ui.series

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arki_deportes.data.model.Serie
import com.example.arki_deportes.data.repository.FirebaseCatalogRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class SerieListUiState(
    val series: List<Serie> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class SerieListViewModel(
    private val repository: FirebaseCatalogRepository = FirebaseCatalogRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(SerieListUiState())
    val uiState: StateFlow<SerieListUiState> = _uiState

    fun loadSeries(campeonatoId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Necesitaremos añadir una función observeSeries en el repositorio
                repository.observeSeries(campeonatoId).collect { lista ->
                    _uiState.update { it.copy(series = lista, isLoading = false) }
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
}