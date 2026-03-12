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
            repository.observePartidosJugandoseGlobal()
                .flatMapLatest { listaLigera ->
                    if (listaLigera.isEmpty()) {
                        // Si no hay nada, quitamos el loading de inmediato
                        _uiState.update { it.copy(isLoading = false, partidos = emptyList()) }
                        flowOf(emptyList<Partido>())
                    } else {
                        val flows = listaLigera.map { item ->
                            repository.observePartido(item.CODIGOCAMPEONATO, item.CODIGOPARTIDO)
                        }
                        combine(flows) { detailedArray ->
                            // 🛠️ FILTRAR PARTIDOS ROTOS:
                            // Solo nos quedamos con los que NO son nulos
                            detailedArray.filterNotNull().mapIndexed { index, partido ->
                                // Aseguramos que el objeto tenga el ID del campeonato
                                partido.copy(CAMPEONATOCODIGO = partido.CAMPEONATOCODIGO.ifBlank {
                                    listaLigera.find { it.CODIGOPARTIDO == partido.CODIGOPARTIDO }?.CODIGOCAMPEONATO ?: ""
                                })
                            }
                        }
                    }
                }
                .collect { partidosValidos ->
                    _uiState.update {
                        it.copy(
                            partidos = partidosValidos,
                            isLoading = false // ✅ Aquí se apaga el círculo
                        )
                    }
                }
        }
    }

    /**
     * Elimina un partido específico de la lista de partidos jugándose
     */
    fun eliminarPartidoDeEnVivo(codigoPartido: String) {
        viewModelScope.launch {
            repository.removerDePartidosJugandose(codigoPartido)
        }
    }

    /**
     * Elimina todos los partidos finalizados de la lista de partidos jugándose
     */
    fun eliminarFinalizados() {
        viewModelScope.launch {
            _uiState.value.partidos.filter { it.ESTADO == 1 }.forEach { partido ->
                repository.removerDePartidosJugandose(partido.CODIGOPARTIDO)
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
