package com.example.arki_deportes.ui.grupos

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arki_deportes.data.context.CampeonatoContext
import com.example.arki_deportes.data.model.Equipo
import com.example.arki_deportes.data.model.Grupo
import com.example.arki_deportes.data.model.Serie
import com.example.arki_deportes.data.repository.FirebaseCatalogRepository
import com.example.arki_deportes.utils.Constants
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * GRUPO LIST VIEW MODEL - GESTIÓN DINÁMICA DE GRUPOS
 * ═══════════════════════════════════════════════════════════════════════════
 */
private const val TAG = "GrupoListVM_DEBUG"

data class GrupoListUiState(
    val gruposDefiniciones: List<Grupo> = emptyList(),
    val equiposMaestro: List<Equipo> = emptyList(),
    val isLoading: Boolean = true,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val searchQuery: String = "",
    val campeonatoActivo: String? = null,
    val campeonatoNombre: String = "Seleccione un campeonato",
    val grupoSeleccionado: String = "A",
    val gruposDisponibles: List<String> = emptyList(),
    val showAddDialog: Boolean = false,
    val isAddingEquipo: Boolean = false,
    val seriesDisponibles: List<Serie> = emptyList(),
    val serieSeleccionada: Serie? = null,
    val idGrupoActual: String? = null
)

class GrupoListViewModel(
    private val repository: FirebaseCatalogRepository = FirebaseCatalogRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(GrupoListUiState())
    val uiState: StateFlow<GrupoListUiState> = _uiState

    private var equiposJob: Job? = null
    private var gruposJob: Job? = null

    init {
        observeCampeonatoContext()
    }

    private fun observeCampeonatoContext() {
        viewModelScope.launch {
            CampeonatoContext.campeonatoActivo.collect { campeonato ->
                val codigo = campeonato?.CODIGO
                _uiState.update {
                    it.copy(
                        campeonatoActivo = codigo,
                        campeonatoNombre = campeonato?.CAMPEONATO ?: "Seleccione un campeonato",
                        isLoading = true
                    )
                }

                if (codigo != null) {
                    loadSeries(codigo)
                    loadEquiposDelCampeonato(codigo)
                    observeDefinicionesDeGrupos(codigo)
                } else {
                    _uiState.update { it.copy(isLoading = false) }
                }
            }
        }
    }

    private fun loadSeries(campeonatoCodigo: String) {
        viewModelScope.launch {
            try {
                repository.observeSeries(campeonatoCodigo).collect { series ->
                    _uiState.update { it.copy(seriesDisponibles = series) }
                    if (_uiState.value.serieSeleccionada == null) {
                        series.firstOrNull()?.let { onSerieSeleccionada(it) }
                    }
                }
            } catch (e: Exception) { Log.e(TAG, "Error series: ${e.message}") }
        }
    }

    private fun loadEquiposDelCampeonato(campeonatoCodigo: String) {
        equiposJob?.cancel()
        equiposJob = viewModelScope.launch {
            repository.observeEquipos(campeonatoCodigo).collect { equipos ->
                _uiState.update { it.copy(equiposMaestro = equipos) }
            }
        }
    }

    private fun observeDefinicionesDeGrupos(campeonatoCodigo: String) {
        gruposJob?.cancel()
        gruposJob = viewModelScope.launch {
            repository.observeGrupos(campeonatoCodigo).collect { todos ->
                _uiState.update { it.copy(gruposDefiniciones = todos, isLoading = false) }
                actualizarGruposDisponibles()
                actualizarIdGrupoActual()
            }
        }
    }

    private fun actualizarGruposDisponibles() {
        val serie = _uiState.value.serieSeleccionada ?: return
        val letras = _uiState.value.gruposDefiniciones
            .filter { it.CODIGOSERIE == serie.CODIGOSERIE }
            .map { it.GRUPO }
            .distinct()
            .sorted()
        
        _uiState.update { it.copy(gruposDisponibles = letras) }
        
        if (letras.isNotEmpty() && _uiState.value.grupoSeleccionado !in letras) {
            onGrupoSeleccionado(letras.first())
        }
    }

    fun onSerieSeleccionada(serie: Serie) {
        _uiState.update { it.copy(serieSeleccionada = serie) }
        actualizarGruposDisponibles()
        actualizarIdGrupoActual()
    }

    fun onGrupoSeleccionado(letra: String) {
        _uiState.update { it.copy(grupoSeleccionado = letra) }
        actualizarIdGrupoActual()
    }

    private fun actualizarIdGrupoActual() {
        val serie = _uiState.value.serieSeleccionada ?: return
        val letra = _uiState.value.grupoSeleccionado
        
        val definicion = _uiState.value.gruposDefiniciones.find { 
            it.CODIGOSERIE == serie.CODIGOSERIE && it.GRUPO.equals(letra, ignoreCase = true) 
        }
        _uiState.update { it.copy(idGrupoActual = definicion?.CODIGOGRUPO?.toString()) }
    }

    fun getFilteredGrupos(): List<Grupo> {
        val idGrupo = _uiState.value.idGrupoActual ?: return emptyList()
        val query = _uiState.value.searchQuery.lowercase()

        return _uiState.value.equiposMaestro
            .filter { equipo ->
                equipo.CODIGOGRUPO == idGrupo &&
                (query.isBlank() || equipo.getNombreDisplay().lowercase().contains(query))
            }
            .map { equipo ->
                Grupo(
                    CODIGOCAMPEONATO = equipo.CODIGOCAMPEONATO,
                    CODIGOGRUPO = equipo.CODIGOGRUPO,
                    GRUPO = _uiState.value.grupoSeleccionado,
                    PROVINCIA = equipo.EQUIPO,
                    CODIGOPROVINCIA = equipo.CODIGOEQUIPO,
                    POSICION = equipo.POSICION,
                    NOMBRESERIE = equipo.NOMBRESERIE,
                    NOMBREEQUIPO = equipo.EQUIPO_NOMBRECOMPLETO
                )
            }
            .sortedBy { it.POSICION }
    }

    fun addEquipoToGrupo(equipo: Equipo) {
        val idGrupo = _uiState.value.idGrupoActual ?: return
        val serie = _uiState.value.serieSeleccionada ?: return
        val campeonatoId = _uiState.value.campeonatoActivo ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isAddingEquipo = true) }
            try {
                val updates = mapOf(
                    "CODIGOGRUPO" to idGrupo,
                    "NOMBRESERIE" to serie.NOMBRESERIE,
                    "POSICION" to (getFilteredGrupos().size + 1)
                )
                repository.updateEquipoFields(campeonatoId, equipo.CODIGOEQUIPO, updates)
                _uiState.update { it.copy(isAddingEquipo = false, showAddDialog = false, successMessage = "Equipo asignado") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isAddingEquipo = false, errorMessage = e.message) }
            }
        }
    }

    fun deleteEquipoFromGrupo(grupo: Grupo) {
        val campeonatoId = _uiState.value.campeonatoActivo ?: return
        viewModelScope.launch {
            try {
                repository.updateEquipoFields(campeonatoId, grupo.CODIGOPROVINCIA?.toString() ?: "", mapOf("CODIGOGRUPO" to "", "NOMBRESERIE" to ""))
                _uiState.update { it.copy(successMessage = "Equipo quitado del grupo") }
            } catch (e: Exception) { _uiState.update { it.copy(errorMessage = e.message) } }
        }
    }

    fun getEquiposNoEnGrupo(): List<Equipo> {
        return _uiState.value.equiposMaestro.filter { it.CODIGOGRUPO.isNullOrBlank() }
    }

    fun onSearchQueryChange(query: String) = _uiState.update { it.copy(searchQuery = query) }
    fun showAddEquipoDialog() = _uiState.update { it.copy(showAddDialog = true) }
    fun hideAddEquipoDialog() = _uiState.update { it.copy(showAddDialog = false) }
    fun clearMessages() = _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    fun refresh() = observeCampeonatoContext()
}
