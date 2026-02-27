package com.example.arki_deportes.ui.envivo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.arki_deportes.data.model.Partido
import com.example.arki_deportes.data.model.PartidoEnVivo
import com.example.arki_deportes.data.repository.FirebaseCatalogRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

data class PartidosEnVivoUiState(
    val partidos: List<Partido> = emptyList(),
    val isLoading: Boolean = true
)

@OptIn(ExperimentalCoroutinesApi::class)
class PartidosEnVivoViewModel(
    private val repository: FirebaseCatalogRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PartidosEnVivoUiState())
    val uiState: StateFlow<PartidosEnVivoUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            // 1. Observamos el índice ligero de partidos jugándose
            repository.observePartidosJugandoseGlobal()
                .flatMapLatest { listaLigera ->
                    if (listaLigera.isEmpty()) {
                        flowOf(emptyList<Partido>())
                    } else {
                        // 2. Por cada ID, creamos un observador a la RUTA REAL completa
                        val flows = listaLigera.map { itemLigero ->
                            repository.observePartido(itemLigero.CODIGOCAMPEONATO, itemLigero.CODIGOPARTIDO)
                        }
                        // Combinamos todos los flujos de partidos individuales en una sola lista
                        combine(flows) { detailedArray ->
                            detailedList(detailedArray, listaLigera)
                        }
                    }
                }
                .collect { partidosDetallados ->
                    _uiState.update { it.copy(partidos = partidosDetallados, isLoading = false) }
                }
        }
    }

    /**
     * Auxiliar para mantener el orden y asegurar que tenemos los datos completos
     */
    private fun detailedList(detailedArray: Array<Partido>, listaLigera: List<PartidoEnVivo>): List<Partido> {
        return detailedArray.toList().mapIndexed { index, partido ->
            // Nos aseguramos de inyectar el código de campeonato si faltara
            partido.copy(CAMPEONATOCODIGO = listaLigera[index].CODIGOCAMPEONATO)
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
