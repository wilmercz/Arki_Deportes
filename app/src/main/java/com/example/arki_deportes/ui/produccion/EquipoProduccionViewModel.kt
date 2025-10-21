package com.example.arki_deportes.ui.produccion

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.arki_deportes.data.Repository
import com.example.arki_deportes.data.model.Campeonato
import com.example.arki_deportes.data.model.EquipoProduccion
import com.example.arki_deportes.utils.Constants
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Modos disponibles para editar la configuración del equipo de producción.
 */
enum class EquipoProduccionMode {
    Default,
    Campeonato
}

/**
 * Estado de la pantalla de edición del equipo de producción.
 */
data class EquipoProduccionUiState(
    val isLoading: Boolean = true,
    val isSelectionLoading: Boolean = false,
    val isSaving: Boolean = false,
    val narrador: String = "",
    val comentarista: String = "",
    val bordeCampo: String = "",
    val anfitrionesInput: String = "",
    val modo: EquipoProduccionMode = EquipoProduccionMode.Default,
    val campeonatos: List<Campeonato> = emptyList(),
    val campeonatoSeleccionado: Campeonato? = null,
    val ultimaActualizacion: Long? = null,
    val mensajeExito: String? = null,
    val error: String? = null
)

/**
 * ViewModel encargado de administrar las configuraciones del equipo de producción.
 */
class EquipoProduccionViewModel(
    private val repository: Repository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(EquipoProduccionUiState())
    val uiState: StateFlow<EquipoProduccionUiState> = _uiState.asStateFlow()

    private var defaultConfig: EquipoProduccion = EquipoProduccion.empty()
    private val configuracionesPorCampeonato = mutableMapOf<String, EquipoProduccion>()

    init {
        recargar()
    }

    /**
     * Reintenta la carga inicial de datos.
     */
    fun recargar() {
        viewModelScope.launch(dispatcher) {
            cargarDatosIniciales()
        }
    }

    private suspend fun cargarDatosIniciales() {
        _uiState.update {
            it.copy(
                isLoading = true,
                error = null,
                mensajeExito = null,
                campeonatos = emptyList(),
                campeonatoSeleccionado = null,
                modo = EquipoProduccionMode.Default
            )
        }
        configuracionesPorCampeonato.clear()

        try {
            val campeonatos = repository.obtenerCampeonatos()
            val defaultEquipo = repository.obtenerEquipoProduccion()

            defaultConfig = defaultEquipo

            _uiState.update {
                it.copy(
                    isLoading = false,
                    campeonatos = campeonatos,
                    mensajeExito = null,
                    error = null
                )
            }

            applyConfig(defaultEquipo, defaultEquipo.timestamp.takeIf { ts -> ts > 0 })
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    error = e.message ?: Constants.Mensajes.ERROR_DESCONOCIDO
                )
            }
        }
    }

    /**
     * Actualiza el modo de edición seleccionado.
     */
    fun onModoSeleccionado(modo: EquipoProduccionMode) {
        if (_uiState.value.modo == modo) return

        _uiState.update { it.copy(modo = modo, mensajeExito = null) }

        when (modo) {
            EquipoProduccionMode.Default -> {
                applyConfig(defaultConfig, defaultConfig.timestamp.takeIf { it > 0 })
            }

            EquipoProduccionMode.Campeonato -> {
                val seleccionado = _uiState.value.campeonatoSeleccionado
                if (seleccionado != null) {
                    viewModelScope.launch(dispatcher) {
                        cargarConfiguracionParaCampeonato(seleccionado.CODIGO)
                    }
                } else {
                    applyConfig(defaultConfig, defaultConfig.timestamp.takeIf { it > 0 })
                }
            }
        }
    }

    /**
     * Selecciona un campeonato específico para editar su configuración.
     */
    fun onCampeonatoSeleccionado(codigo: String) {
        val campeonato = _uiState.value.campeonatos.find { it.CODIGO == codigo }
        if (campeonato == null) {
            _uiState.update { it.copy(campeonatoSeleccionado = null) }
            return
        }

        if (_uiState.value.campeonatoSeleccionado?.CODIGO == codigo) return

        _uiState.update {
            it.copy(
                campeonatoSeleccionado = campeonato,
                mensajeExito = null
            )
        }

        viewModelScope.launch(dispatcher) {
            cargarConfiguracionParaCampeonato(campeonato.CODIGO)
        }
    }

    fun onNarradorChange(valor: String) {
        _uiState.update { it.copy(narrador = valor, mensajeExito = null) }
    }

    fun onComentaristaChange(valor: String) {
        _uiState.update { it.copy(comentarista = valor, mensajeExito = null) }
    }

    fun onBordeCampoChange(valor: String) {
        _uiState.update { it.copy(bordeCampo = valor, mensajeExito = null) }
    }

    fun onAnfitrionesChange(valor: String) {
        _uiState.update { it.copy(anfitrionesInput = valor, mensajeExito = null) }
    }

    /**
     * Guarda la configuración actual en Firebase.
     */
    fun guardarConfiguracion() {
        val estadoActual = _uiState.value
        val codigoCampeonato = when (estadoActual.modo) {
            EquipoProduccionMode.Default -> null
            EquipoProduccionMode.Campeonato -> estadoActual.campeonatoSeleccionado?.CODIGO
        }

        if (estadoActual.modo == EquipoProduccionMode.Campeonato && codigoCampeonato == null) {
            _uiState.update {
                it.copy(error = "Selecciona un campeonato para guardar la configuración")
            }
            return
        }

        val equipo = construirEquipoDesdeFormulario(estadoActual)
        val timestamp = System.currentTimeMillis()

        viewModelScope.launch(dispatcher) {
            _uiState.update {
                it.copy(isSaving = true, mensajeExito = null, error = null)
            }

            try {
                repository.guardarEquipoProduccion(equipo, codigoCampeonato)

                val almacenado = equipo.normalized().copy(timestamp = timestamp)

                if (codigoCampeonato == null) {
                    defaultConfig = almacenado
                } else {
                    configuracionesPorCampeonato[codigoCampeonato] = almacenado
                }

                _uiState.update {
                    it.copy(
                        isSaving = false,
                        narrador = almacenado.narrador,
                        comentarista = almacenado.comentarista,
                        bordeCampo = almacenado.bordeCampo,
                        anfitrionesInput = almacenado.anfitriones.joinToString("\n"),
                        ultimaActualizacion = almacenado.timestamp,
                        mensajeExito = Constants.Mensajes.EXITO_GUARDAR,
                        error = null
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = e.message ?: Constants.Mensajes.ERROR_DESCONOCIDO
                    )
                }
            }
        }
    }

    private suspend fun cargarConfiguracionParaCampeonato(codigo: String) {
        _uiState.update { it.copy(isSelectionLoading = true, error = null) }

        try {
            val configuracion = configuracionesPorCampeonato[codigo]
                ?: repository.obtenerEquipoProduccion(codigo).also {
                    configuracionesPorCampeonato[codigo] = it
                }

            val configuracionMostrar = if (configuracion.isEmpty()) {
                defaultConfig
            } else {
                configuracion
            }

            val timestamp = when {
                configuracion.isEmpty() -> defaultConfig.timestamp.takeIf { it > 0 }
                configuracion.timestamp > 0 -> configuracion.timestamp
                else -> null
            }

            applyConfig(configuracionMostrar, timestamp)

            _uiState.update { it.copy(isSelectionLoading = false) }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(
                    isSelectionLoading = false,
                    error = e.message ?: Constants.Mensajes.ERROR_DESCONOCIDO
                )
            }
        }
    }

    private fun applyConfig(config: EquipoProduccion, timestamp: Long?) {
        _uiState.update {
            it.copy(
                narrador = config.narrador,
                comentarista = config.comentarista,
                bordeCampo = config.bordeCampo,
                anfitrionesInput = config.anfitriones.joinToString("\n"),
                ultimaActualizacion = timestamp,
                mensajeExito = null,
                error = null
            )
        }
    }

    private fun construirEquipoDesdeFormulario(estado: EquipoProduccionUiState): EquipoProduccion {
        val anfitriones = estado.anfitrionesInput
            .split(',', '\n', ';')
            .map { it.trim() }
            .filter { it.isNotEmpty() }

        return EquipoProduccion(
            narrador = estado.narrador.trim(),
            comentarista = estado.comentarista.trim(),
            bordeCampo = estado.bordeCampo.trim(),
            anfitriones = anfitriones,
            timestamp = estado.ultimaActualizacion ?: 0L
        )
    }
}

/**
 * Factory para proveer dependencias al [EquipoProduccionViewModel].
 */
class EquipoProduccionViewModelFactory(
    private val repository: Repository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EquipoProduccionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EquipoProduccionViewModel(repository, dispatcher) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
