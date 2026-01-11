// app/src/main/java/com/example/arki_deportes/ui/realtime/TiempoRealViewModel.kt

package com.example.arki_deportes.ui.realtime

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arki_deportes.data.model.*
import com.example.arki_deportes.data.repository.FirebaseCatalogRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * TIEMPO REAL VIEW MODEL
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * ViewModel para control de partido en tiempo real
 * Replica la funcionalidad de VB.NET FrmControl
 *
 * @author ARKI SISTEMAS
 * @version 1.0.0
 */

/**
 * Estado de la pantalla de tiempo real
 */
data class TiempoRealUiState(
    val isLoading: Boolean = true,
    val partido: Partido? = null,
    val tiempoActual: String = "00:00",
    val error: String? = null,
    val modoTransmision: Boolean = false,
    val actualizandoFirebase: Boolean = false
)

class TiempoRealViewModel(
    private val repository: FirebaseCatalogRepository,
    private val campeonatoId: String,
    private val partidoId: String
) : ViewModel() {

    private val TAG = "TiempoRealViewModel"

    private val _uiState = MutableStateFlow(TiempoRealUiState())
    val uiState: StateFlow<TiempoRealUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        observarPartido()
        iniciarActualizadorDeTiempo()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // OBSERVACIÓN DE DATOS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Observa el partido en tiempo real desde Firebase
     * VB.NET Equivalente: Escuchar cambios en Firebase
     */
    private fun observarPartido() {
        viewModelScope.launch {
            repository.observePartido(campeonatoId, partidoId)
                .catch { error ->
                    Log.e(TAG, "Error observando partido: ${error.message}")
                    _uiState.update {
                        it.copy(
                            error = error.message,
                            isLoading = false
                        )
                    }
                }
                .collect { partido ->
                    Log.d(TAG, "Partido actualizado: ${partido.getNombrePartido()}")
                    _uiState.update {
                        it.copy(
                            partido = partido,
                            isLoading = false,
                            error = null
                        )
                    }
                }
        }
    }

    /**
     * Timer que actualiza el tiempo cada segundo
     * VB.NET Equivalente: TimerCronometro_Tick
     */
    private fun iniciarActualizadorDeTiempo() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000) // Cada segundo

                val partido = _uiState.value.partido
                if (partido != null && partido.estaEnCurso()) {
                    actualizarTiempoDisplay()
                    actualizarTiempoFirebase()
                }
            }
        }
    }

    /**
     * Actualiza el display del cronómetro
     * VB.NET Equivalente: Actualizar LblCronometro.Text
     */
    private fun actualizarTiempoDisplay() {
        val partido = _uiState.value.partido ?: return

        val segundos = partido.calcularTiempoActualSegundos()
        _uiState.update {
            it.copy(tiempoActual = partido.formatearTiempo(segundos))
        }
    }

    /**
     * Actualiza TIEMPOJUEGO en Firebase cada segundo
     * VB.NET Equivalente: Actualizar cada segundo en timer
     */
    private suspend fun actualizarTiempoFirebase() {
        val partido = _uiState.value.partido ?: return

        val segundos = partido.calcularTiempoActualSegundos()
        val minutos = segundos / 60
        val segs = segundos % 60

        val updates = mapOf(
            "TIEMPOJUEGO" to String.format("%02d:%02d", minutos, segs)
        )

        repository.updatePartidoFields(campeonatoId, partidoId, updates)

        // Si modo transmisión activo, sincronizar con overlay
        if (_uiState.value.modoTransmision) {
            sincronizarConOverlay()
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ACCIONES - CRONÓMETRO
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Inicia el partido (BOTÓN INICIAR)
     *
     * VB.NET Equivalente: IniciarCronometroFutbol()
     * - Si NumeroDeTiempo = "0T" → cambia a "1T" (primer tiempo)
     * - Si NumeroDeTiempo = "2T" → cambia a "3T" (segundo tiempo)
     * - Cronometro = Now
     * - TimerCronometro.Enabled = True
     */
    fun iniciarPartido() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch

            Log.d(TAG, "Iniciando partido. Estado actual: ${partido.NumeroDeTiempo}")

            // Validar que no esté ya jugando
            if (partido.estaEnCurso()) {
                Log.w(TAG, "El partido ya está en curso")
                return@launch
            }

            // Validar que no esté finalizado
            if (partido.estaFinalizado()) {
                Log.w(TAG, "El partido ya finalizó")
                return@launch
            }

            _uiState.update { it.copy(actualizandoFirebase = true) }

            // Determinar si es primer o segundo tiempo
            val primerTiempo = partido.NumeroDeTiempo == "0T"
            val updates = crearMapaIniciarPartido(primerTiempo)

            val result = repository.updatePartidoFields(campeonatoId, partidoId, updates)

            result.onSuccess {
                Log.d(TAG, "Partido iniciado exitosamente")
                if (_uiState.value.modoTransmision) {
                    sincronizarConOverlay()
                }
            }.onFailure { error ->
                Log.e(TAG, "Error al iniciar partido: ${error.message}")
                _uiState.update { it.copy(error = error.message) }
            }

            _uiState.update { it.copy(actualizandoFirebase = false) }
        }
    }

    /**
     * Detiene el cronómetro y pasa al siguiente estado
     *
     * VB.NET Equivalente: FinalizarCronometroFutbol()
     * - Si "1T" → cambia a "2T" (descanso)
     * - Si "3T" → cambia a "4T" (finalizado)
     */
    fun detenerCronometro() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch

            Log.d(TAG, "Deteniendo cronómetro. Estado: ${partido.NumeroDeTiempo}")

            _uiState.update { it.copy(actualizandoFirebase = true) }

            val updates = when (partido.NumeroDeTiempo) {
                "1T" -> {
                    // Termina primer tiempo → Descanso
                    Log.d(TAG, "Fin del primer tiempo → Descanso")
                    crearMapaDescanso()
                }
                "3T" -> {
                    // Termina segundo tiempo → Finalizado
                    Log.d(TAG, "Fin del segundo tiempo → Finalizado")
                    crearMapaFinalizarPartido()
                }
                else -> {
                    Log.w(TAG, "Estado inválido para detener: ${partido.NumeroDeTiempo}")
                    _uiState.update { it.copy(actualizandoFirebase = false) }
                    return@launch
                }
            }

            val result = repository.updatePartidoFields(campeonatoId, partidoId, updates)

            result.onSuccess {
                Log.d(TAG, "Cronómetro detenido exitosamente")
                if (_uiState.value.modoTransmision) {
                    sincronizarConOverlay()
                }
            }.onFailure { error ->
                Log.e(TAG, "Error al detener cronómetro: ${error.message}")
                _uiState.update { it.copy(error = error.message) }
            }

            _uiState.update { it.copy(actualizandoFirebase = false) }
        }
    }

    /**
     * Ajusta el tiempo manualmente (botones +/-)
     *
     * VB.NET Equivalente: btnMasCronometro / btnMenosCronometro
     * Ajusta el TIEMPOJUEGO sumando o restando segundos
     */
    fun ajustarTiempo(segundos: Int) {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch

            val tiempoActual = partido.calcularTiempoActualSegundos()
            val nuevoTiempo = (tiempoActual + segundos).coerceAtLeast(0)

            val minutos = nuevoTiempo / 60
            val segs = nuevoTiempo % 60

            Log.d(TAG, "Ajustando tiempo: ${segundos}s. Nuevo: ${minutos}:${segs}")

            val updates = mapOf(
                "TIEMPOJUEGO" to String.format("%02d:%02d", minutos, segs)
            )

            repository.updatePartidoFields(campeonatoId, partidoId, updates)

            if (_uiState.value.modoTransmision) {
                sincronizarConOverlay()
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ACCIONES - MARCADOR (GOLES)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Agrega un gol al equipo 1
     *
     * VB.NET Equivalente: BtnGolMAsEQ1_Click
     * .Goles1 = .Goles1 + 1
     * ActualizarCampo_Partidos_BD("GOLES1", "", .Goles1, "")
     * FirebaseManager.EnqueueSet(RutaPartidoFB & "GOLES1", .Goles1, 2)
     */
    fun agregarGolEquipo1() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch

            if (!partido.estaEnCurso()) {
                Log.w(TAG, "No se puede agregar gol: partido no en curso")
                return@launch
            }

            val goles = partido.getGoles1Int() + 1

            Log.d(TAG, "Agregando gol equipo 1: $goles")

            val updates = mapOf(
                "GOLES1" to goles.toString()
            )

            repository.updatePartidoFields(campeonatoId, partidoId, updates)

            if (_uiState.value.modoTransmision) {
                sincronizarConOverlay()
            }
        }
    }

    /**
     * Resta un gol al equipo 1 (botón -)
     */
    fun restarGolEquipo1() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            val goles = (partido.getGoles1Int() - 1).coerceAtLeast(0)

            Log.d(TAG, "Restando gol equipo 1: $goles")

            val updates = mapOf(
                "GOLES1" to goles.toString()
            )

            repository.updatePartidoFields(campeonatoId, partidoId, updates)

            if (_uiState.value.modoTransmision) {
                sincronizarConOverlay()
            }
        }
    }

    /**
     * Agrega un gol al equipo 2
     *
     * VB.NET Equivalente: BtnGolMAsEQ2_Click
     */
    fun agregarGolEquipo2() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch

            if (!partido.estaEnCurso()) {
                Log.w(TAG, "No se puede agregar gol: partido no en curso")
                return@launch
            }

            val goles = partido.getGoles2Int() + 1

            Log.d(TAG, "Agregando gol equipo 2: $goles")

            val updates = mapOf(
                "GOLES2" to goles.toString()
            )

            repository.updatePartidoFields(campeonatoId, partidoId, updates)

            if (_uiState.value.modoTransmision) {
                sincronizarConOverlay()
            }
        }
    }

    /**
     * Resta un gol al equipo 2 (botón -)
     */
    fun restarGolEquipo2() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            val goles = (partido.getGoles2Int() - 1).coerceAtLeast(0)

            Log.d(TAG, "Restando gol equipo 2: $goles")

            val updates = mapOf(
                "GOLES2" to goles.toString()
            )

            repository.updatePartidoFields(campeonatoId, partidoId, updates)

            if (_uiState.value.modoTransmision) {
                sincronizarConOverlay()
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ACCIONES - TARJETAS AMARILLAS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Agrega una tarjeta amarilla al equipo 1
     * VB.NET: ActualizarCampo_Partidos_BD("TAMARILLAS1", "", DatosPartido.Amarillas1, "")
     */
    fun agregarAmarillaEquipo1() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            val amarillas = partido.getAmarillas1Int() + 1

            Log.d(TAG, "Agregando amarilla equipo 1: $amarillas")

            val updates = mapOf(
                "TAMARILLAS1" to amarillas.toString()
            )

            repository.updatePartidoFields(campeonatoId, partidoId, updates)

            if (_uiState.value.modoTransmision) {
                sincronizarConOverlay()
            }
        }
    }

    /**
     * Resta una tarjeta amarilla al equipo 1
     */
    fun restarAmarillaEquipo1() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            val amarillas = (partido.getAmarillas1Int() - 1).coerceAtLeast(0)

            Log.d(TAG, "Restando amarilla equipo 1: $amarillas")

            val updates = mapOf(
                "TAMARILLAS1" to amarillas.toString()
            )

            repository.updatePartidoFields(campeonatoId, partidoId, updates)

            if (_uiState.value.modoTransmision) {
                sincronizarConOverlay()
            }
        }
    }

    /**
     * Agrega una tarjeta amarilla al equipo 2
     */
    fun agregarAmarillaEquipo2() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            val amarillas = partido.getAmarillas2Int() + 1

            Log.d(TAG, "Agregando amarilla equipo 2: $amarillas")

            val updates = mapOf(
                "TAMARILLAS2" to amarillas.toString()
            )

            repository.updatePartidoFields(campeonatoId, partidoId, updates)

            if (_uiState.value.modoTransmision) {
                sincronizarConOverlay()
            }
        }
    }

    /**
     * Resta una tarjeta amarilla al equipo 2
     */
    fun restarAmarillaEquipo2() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            val amarillas = (partido.getAmarillas2Int() - 1).coerceAtLeast(0)

            Log.d(TAG, "Restando amarilla equipo 2: $amarillas")

            val updates = mapOf(
                "TAMARILLAS2" to amarillas.toString()
            )

            repository.updatePartidoFields(campeonatoId, partidoId, updates)

            if (_uiState.value.modoTransmision) {
                sincronizarConOverlay()
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ACCIONES - TARJETAS ROJAS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Agrega una tarjeta roja al equipo 1
     * VB.NET: ActualizarCampo_Partidos_BD("TROJAS1", "", DatosPartido.Rojas1, "")
     */
    fun agregarRojaEquipo1() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            val rojas = partido.getRojas1Int() + 1

            Log.d(TAG, "Agregando roja equipo 1: $rojas")

            val updates = mapOf(
                "TROJAS1" to rojas.toString()
            )

            repository.updatePartidoFields(campeonatoId, partidoId, updates)

            if (_uiState.value.modoTransmision) {
                sincronizarConOverlay()
            }
        }
    }

    /**
     * Resta una tarjeta roja al equipo 1
     */
    fun restarRojaEquipo1() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            val rojas = (partido.getRojas1Int() - 1).coerceAtLeast(0)

            Log.d(TAG, "Restando roja equipo 1: $rojas")

            val updates = mapOf(
                "TROJAS1" to rojas.toString()
            )

            repository.updatePartidoFields(campeonatoId, partidoId, updates)

            if (_uiState.value.modoTransmision) {
                sincronizarConOverlay()
            }
        }
    }

    /**
     * Agrega una tarjeta roja al equipo 2
     */
    fun agregarRojaEquipo2() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            val rojas = partido.getRojas2Int() + 1

            Log.d(TAG, "Agregando roja equipo 2: $rojas")

            val updates = mapOf(
                "TROJAS2" to rojas.toString()
            )

            repository.updatePartidoFields(campeonatoId, partidoId, updates)

            if (_uiState.value.modoTransmision) {
                sincronizarConOverlay()
            }
        }
    }

    /**
     * Resta una tarjeta roja al equipo 2
     */
    fun restarRojaEquipo2() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            val rojas = (partido.getRojas2Int() - 1).coerceAtLeast(0)

            Log.d(TAG, "Restando roja equipo 2: $rojas")

            val updates = mapOf(
                "TROJAS2" to rojas.toString()
            )

            repository.updatePartidoFields(campeonatoId, partidoId, updates)

            if (_uiState.value.modoTransmision) {
                sincronizarConOverlay()
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ACCIONES - ESQUINAS (CORNERS)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Agrega una esquina al equipo 1
     * VB.NET: ActualizarCampo_Partidos_BD("ESQUINAS1", "", DatosPartido.Esquinas1, "")
     */
    fun agregarEsquinaEquipo1() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            val esquinas = partido.getEsquinas1Int() + 1

            Log.d(TAG, "Agregando esquina equipo 1: $esquinas")

            val updates = mapOf(
                "ESQUINAS1" to esquinas.toString()
            )

            repository.updatePartidoFields(campeonatoId, partidoId, updates)

            if (_uiState.value.modoTransmision) {
                sincronizarConOverlay()
            }
        }
    }

    /**
     * Resta una esquina al equipo 1
     */
    fun restarEsquinaEquipo1() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            val esquinas = (partido.getEsquinas1Int() - 1).coerceAtLeast(0)

            Log.d(TAG, "Restando esquina equipo 1: $esquinas")

            val updates = mapOf(
                "ESQUINAS1" to esquinas.toString()
            )

            repository.updatePartidoFields(campeonatoId, partidoId, updates)

            if (_uiState.value.modoTransmision) {
                sincronizarConOverlay()
            }
        }
    }

    /**
     * Agrega una esquina al equipo 2
     */
    fun agregarEsquinaEquipo2() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            val esquinas = partido.getEsquinas2Int() + 1

            Log.d(TAG, "Agregando esquina equipo 2: $esquinas")

            val updates = mapOf(
                "ESQUINAS2" to esquinas.toString()
            )

            repository.updatePartidoFields(campeonatoId, partidoId, updates)

            if (_uiState.value.modoTransmision) {
                sincronizarConOverlay()
            }
        }
    }

    /**
     * Resta una esquina al equipo 2
     */
    fun restarEsquinaEquipo2() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            val esquinas = (partido.getEsquinas2Int() - 1).coerceAtLeast(0)

            Log.d(TAG, "Restando esquina equipo 2: $esquinas")

            val updates = mapOf(
                "ESQUINAS2" to esquinas.toString()
            )

            repository.updatePartidoFields(campeonatoId, partidoId, updates)

            if (_uiState.value.modoTransmision) {
                sincronizarConOverlay()
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // SINCRONIZACIÓN CON OVERLAY
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Activa/desactiva el modo transmisión (overlay)
     */
    fun toggleModoTransmision() {
        val nuevoModo = !_uiState.value.modoTransmision
        _uiState.update { it.copy(modoTransmision = nuevoModo) }

        Log.d(TAG, "Modo transmisión: $nuevoModo")

        if (nuevoModo) {
            sincronizarConOverlay()
        }
    }

    /**
     * Sincroniza los datos al nodo PARTIDOACTUAL (para overlay)
     */
    private fun sincronizarConOverlay() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch

            Log.d(TAG, "Sincronizando con overlay...")

            val result = repository.sincronizarPartidoActual(partido)

            result.onSuccess {
                Log.d(TAG, "Overlay sincronizado exitosamente")
            }.onFailure { error ->
                Log.e(TAG, "Error sincronizando overlay: ${error.message}")
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CICLO DE VIDA
    // ═══════════════════════════════════════════════════════════════════════

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        Log.d(TAG, "ViewModel limpiado")
    }
}