package com.example.arki_deportes.ui.menciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.arki_deportes.data.Repository
import com.example.arki_deportes.data.model.Mencion
import com.example.arki_deportes.utils.Constants
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Representa el estado de la pantalla de Menciones.
 */
data class MencionesUiState(
    val isLoading: Boolean = true,
    val menciones: List<MencionItemUiState> = emptyList(),
    val isOrderSaving: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

/**
 * Estado de cada mención mostrado en la UI.
 */
data class MencionItemUiState(
    val id: String,
    val texto: String,
    val tipo: String,
    val activo: Boolean,
    val orden: Long,
    val timestamp: Long,
    val isDirty: Boolean = false,
    val isSaving: Boolean = false
) {

    // ✅ Agregar esta propiedad calculada
    val hasChanges: Boolean
        get() = isDirty

    fun toDomain(): Mencion = Mencion(
        texto = texto,
        tipo = tipo,
        activo = activo,
        orden = orden,
        timestamp = timestamp,
        id = id
    )
}

/**
 * ViewModel encargado de gestionar las menciones configuradas en Firebase.
 */
class MencionesViewModel(
    private val repository: Repository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    private val _uiState = MutableStateFlow(MencionesUiState())
    val uiState: StateFlow<MencionesUiState> = _uiState.asStateFlow()

    init {
        observarMenciones()
    }

    private fun observarMenciones() {
        viewModelScope.launch(dispatcher) {
            repository.observeMenciones()
                .catch { throwable ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = throwable.message ?: Constants.Mensajes.ERROR_DESCONOCIDO
                        )
                    }
                }
                .collect { menciones ->
                    _uiState.update { state ->
                        val actuales = state.menciones
                        val ordenadas = menciones.sortedBy { it.orden }
                        val uiItems = ordenadas.map { mencion ->
                            val existente = actuales.find { it.id == mencion.id }
                            if (existente != null && existente.isDirty) {
                                existente
                            } else {
                                mencion.toUiState()
                            }
                        }
                        state.copy(
                            isLoading = false,
                            menciones = uiItems,
                            errorMessage = null
                        )
                    }
                }
        }
    }

    fun onTextoChange(id: String, nuevoTexto: String) {
        _uiState.update { state ->
            state.copy(
                menciones = state.menciones.map { item ->
                    if (item.id == id) {
                        item.copy(texto = nuevoTexto, isDirty = true)
                    } else {
                        item
                    }
                },
                successMessage = null,
                errorMessage = null
            )
        }
    }

    fun onToggleActivo(id: String, activo: Boolean) {
        val actual = _uiState.value.menciones.find { it.id == id } ?: return
        val actualizado = actual.copy(activo = activo, isSaving = true)

        _uiState.update { state ->
            state.copy(
                menciones = state.menciones.map { item ->
                    if (item.id == id) actualizado else item
                },
                successMessage = null,
                errorMessage = null
            )
        }

        viewModelScope.launch(dispatcher) {
            try {
                repository.actualizarMencion(
                    actualizado.toDomain().copy(timestamp = System.currentTimeMillis())
                )
                _uiState.update { state ->
                    state.copy(
                        menciones = state.menciones.map { item ->
                            if (item.id == id) item.copy(isSaving = false) else item
                        },
                        successMessage = "Estado actualizado"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        menciones = state.menciones.map { item ->
                            if (item.id == id) actual else item
                        },
                        errorMessage = e.message ?: Constants.Mensajes.ERROR_DESCONOCIDO
                    )
                }
            }
        }
    }

    fun guardarMencion(id: String) {
        val actual = _uiState.value.menciones.find { it.id == id } ?: return
        val textoLimpio = actual.texto.trim()
        if (textoLimpio.isEmpty()) {
            _uiState.update {
                it.copy(errorMessage = "El texto de la mención no puede estar vacío")
            }
            return
        }

        val guardando = actual.copy(texto = textoLimpio, isSaving = true, isDirty = false)

        _uiState.update { state ->
            state.copy(
                menciones = state.menciones.map { item ->
                    if (item.id == id) guardando else item
                },
                successMessage = null,
                errorMessage = null
            )
        }

        viewModelScope.launch(dispatcher) {
            try {
                repository.actualizarMencion(
                    guardando.toDomain().copy(timestamp = System.currentTimeMillis())
                )
                _uiState.update { state ->
                    state.copy(
                        menciones = state.menciones.map { item ->
                            if (item.id == id) item.copy(isSaving = false, isDirty = false) else item
                        },
                        successMessage = "Mención actualizada"
                    )
                }
            } catch (e: Exception) {
                _uiState.update { state ->
                    state.copy(
                        menciones = state.menciones.map { item ->
                            if (item.id == id) actual.copy(isDirty = true) else item
                        },
                        errorMessage = e.message ?: Constants.Mensajes.ERROR_DESCONOCIDO
                    )
                }
            }
        }
    }

    fun onMove(fromIndex: Int, toIndex: Int) {
        val current = _uiState.value.menciones
        if (current.isEmpty()) return
        if (fromIndex !in current.indices) return

        _uiState.update { state ->
            val lista = state.menciones.toMutableList()
            val item = lista.removeAt(fromIndex)
            val destino = toIndex.coerceIn(0, lista.size)
            lista.add(destino, item)
            val reindexada = lista.mapIndexed { index, mencion ->
                mencion.copy(orden = index.toLong())
            }
            state.copy(menciones = reindexada)
        }
    }

    fun onDragEnd() {
        val menciones = _uiState.value.menciones
        if (menciones.isEmpty()) return

        viewModelScope.launch(dispatcher) {
            _uiState.update { it.copy(isOrderSaving = true, successMessage = null) }
            try {
                val dominios = menciones.mapIndexed { index, item ->
                    item.copy(orden = index.toLong()).toDomain()
                }
                repository.actualizarOrdenMenciones(dominios)
                _uiState.update {
                    it.copy(isOrderSaving = false, successMessage = "Orden actualizado")
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isOrderSaving = false,
                        errorMessage = e.message ?: Constants.Mensajes.ERROR_DESCONOCIDO
                    )
                }
            }
        }
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(errorMessage = null, successMessage = null)
        }
    }

    private fun Mencion.toUiState(): MencionItemUiState = MencionItemUiState(
        id = id,
        texto = texto,
        tipo = tipo,
        activo = activo,
        orden = orden,
        timestamp = timestamp
    )
}

/**
 * Factory para proveer dependencias al MencionesViewModel.
 */
class MencionesViewModelFactory(
    private val repository: Repository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MencionesViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MencionesViewModel(repository, dispatcher) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
