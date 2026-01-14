// app/src/main/java/com/example/arki_deportes/ui/realtime/TiempoRealViewModel.kt

package com.example.arki_deportes.ui.realtime

import kotlinx.coroutines.tasks.await
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
import androidx.lifecycle.viewModelScope
import com.example.arki_deportes.data.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
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
    val actualizandoFirebase: Boolean = false,
    /**
     * Indica si el modo penales está activo
     * Corresponde a MARCADOR_PENALES en Firebase
     */
    val penalesActivos: Boolean = false,

    /**
     * Equipo que INICIÓ la tanda (1 o 2)
     * Permanente durante toda la tanda
     * Corresponde a PENALES_INICIA en Firebase
     */
    val equipoQueInicia: Int = 1,

    /**
     * Equipo en TURNO actual (1 o 2)
     * Alterna automáticamente después de cada tiro
     * Corresponde a PENALES_TURNO en Firebase
     */
    val equipoEnTurno: Int = 1,

    /**
     * Número de tanda actual (1, 2, 3...)
     * Se incrementa en muerte súbita
     * Corresponde a PENALES_TANDA en Firebase
     */
    val tandaActual: Int = 1,

    /**
     * Historial de tiros equipo 1 (tanda actual)
     * [1, 0, 1...] donde 1=gol, 0=fallo
     * Corresponde a PENALES_SERIE1 en Firebase
     */
    val historiaPenales1: List<Int> = emptyList(),

    /**
     * Historial de tiros equipo 2 (tanda actual)
     * [1, 1, 0...] donde 1=gol, 0=fallo
     * Corresponde a PENALES_SERIE2 en Firebase
     */
    val historiaPenales2: List<Int> = emptyList()
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
        Log.d(TAG, "╔═══════════════════════════════════════════════════════")
        Log.d(TAG, "🎬 INICIALIZANDO TiempoRealViewModel")
        Log.d(TAG, "   campeonatoId: '$campeonatoId'")
        Log.d(TAG, "   partidoId: '$partidoId'")
        Log.d(TAG, "╚═══════════════════════════════════════════════════════")

        observarPartido()
        observarPenales()
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
        Log.d(TAG, "🔍 Iniciando observación del partido...")

        viewModelScope.launch {
            repository.observePartido(campeonatoId, partidoId)
                .catch { error ->
                    Log.e(TAG, "╔═══════════════════════════════════════════════════════")
                    Log.e(TAG, "❌ ERROR EN observePartido")
                    Log.e(TAG, "   Mensaje: ${error.message}")
                    Log.e(TAG, "   Tipo: ${error.javaClass.simpleName}")
                    error.printStackTrace()
                    Log.e(TAG, "╚═══════════════════════════════════════════════════════")

                    _uiState.update {
                        it.copy(
                            error = error.message,
                            isLoading = false
                        )
                    }
                }
                .collect { partido ->
                    Log.d(TAG, "╔═══════════════════════════════════════════════════════")
                    Log.d(TAG, "📥 PARTIDO ACTUALIZADO (Flow)")
                    Log.d(TAG, "   CODIGOPARTIDO: '${partido.CODIGOPARTIDO}'")
                    Log.d(TAG, "   EQUIPO1: '${partido.EQUIPO1}'")
                    Log.d(TAG, "   EQUIPO2: '${partido.EQUIPO2}'")
                    Log.d(TAG, "   GOLES1: ${partido.GOLES1}")
                    Log.d(TAG, "   GOLES2: ${partido.GOLES2}")
                    Log.d(TAG, "   NUMERODETIEMPO: '${partido.NumeroDeTiempo}'")
                    Log.d(TAG, "   TIEMPOJUEGO: '${partido.TIEMPOJUEGO}'")
                    Log.d(TAG, "   estaEnCurso: ${partido.estaEnCurso()}")
                    Log.d(TAG, "   estaFinalizado: ${partido.estaFinalizado()}")
                    Log.d(TAG, "╚═══════════════════════════════════════════════════════")

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
     * Observa los cambios en el sistema de penales desde Firebase
     * Actualiza el UiState con los datos de penales
     */
    private fun observarPenales() {
        viewModelScope.launch {
            repository.observePartido(campeonatoId, partidoId)
                .catch { error ->
                    Log.e(TAG, "❌ Error observando penales: ${error.message}")
                }
                .collect { partido ->
                    _uiState.update { state ->
                        state.copy(
                            penalesActivos = partido.MARCADOR_PENALES,
                            equipoQueInicia = partido.PENALES_INICIA.coerceIn(1, 2),
                            equipoEnTurno = partido.PENALES_TURNO.coerceIn(1, 2),
                            tandaActual = partido.PENALES_TANDA.coerceAtLeast(1),
                            historiaPenales1 = partido.PENALES_SERIE1,
                            historiaPenales2 = partido.PENALES_SERIE2
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
            Log.d(TAG, "╔═══════════════════════════════════════════════════════")
            Log.d(TAG, "⏱️ TIMER INICIADO")
            Log.d(TAG, "╚═══════════════════════════════════════════════════════")

            var tickCount = 0

            while (true) {
                delay(1000) // Cada segundo
                tickCount++

                val partido = _uiState.value.partido

                // ═══════════════════════════════════════════════════════
                // 🔍 LOG CADA 5 SEGUNDOS (para no saturar)
                // ═══════════════════════════════════════════════════════
                if (tickCount % 5 == 0) {
                    Log.d(TAG, "─────────────────────────────────────────────────────")
                    Log.d(TAG, "⏱️ TICK #$tickCount")
                    Log.d(TAG, "   partido != null: ${partido != null}")

                    if (partido != null) {
                        val numeroTiempo = partido.getNumeroDeTiempoEfectivo()
                        val enCurso = partido.estaEnCurso()

                        Log.d(TAG, "   NumeroDeTiempo efectivo: '$numeroTiempo'")
                        Log.d(TAG, "   ESTADO: ${partido.ESTADO}")
                        Log.d(TAG, "   TIEMPOSJUGADOS: ${partido.TIEMPOSJUGADOS}")
                        Log.d(TAG, "   estaEnCurso(): $enCurso")

                        if (enCurso) {
                            val fechaPlay = partido.FECHA_PLAY
                            Log.d(TAG, "   FECHA_PLAY: '$fechaPlay'")

                            val segundos = partido.calcularTiempoActualSegundos()
                            Log.d(TAG, "   Segundos transcurridos: $segundos")

                            val tiempoFormateado = partido.formatearTiempo(segundos)
                            Log.d(TAG, "   Tiempo formateado: $tiempoFormateado")
                        } else {
                            Log.d(TAG, "   ⚠️ Partido NO está en curso - Timer no actualiza")
                        }
                    }
                    Log.d(TAG, "─────────────────────────────────────────────────────")
                }

                // ═══════════════════════════════════════════════════════
                // ACTUALIZACIÓN DEL DISPLAY
                // ═══════════════════════════════════════════════════════
                if (partido != null && partido.estaEnCurso()) {
                    // ✅ Solo actualizar el display
                    actualizarTiempoDisplay()

                    // ✅ Solo sincronizar overlay si está activo
                    if (_uiState.value.modoTransmision) {
                        sincronizarConOverlay()
                    }
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
        val tiempoFormateado = partido.formatearTiempo(segundos)

        // Log solo cada 10 segundos para no saturar
        if (segundos % 10 == 0) {
            Log.d(TAG, "📺 Display actualizado: $tiempoFormateado (${segundos}s)")
        }

        _uiState.update {
            it.copy(tiempoActual = tiempoFormateado)
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

            Log.d(TAG, "╔═══════════════════════════════════════════════════════")
            Log.d(TAG, "🚀 INICIANDO PARTIDO")
            Log.d(TAG, "   Estado actual: ${partido.NumeroDeTiempo}")
            Log.d(TAG, "╚═══════════════════════════════════════════════════════")

            // Validar que no esté ya jugando
            if (partido.estaEnCurso()) {
                Log.w(TAG, "⚠️ El partido ya está en curso")
                return@launch
            }

            // Validar que no esté finalizado
            if (partido.estaFinalizado()) {
                Log.w(TAG, "⚠️ El partido ya finalizó")
                return@launch
            }

            _uiState.update { it.copy(actualizandoFirebase = true) }

            // Determinar si es primer o segundo tiempo
            val primerTiempo = partido.NumeroDeTiempo == "0T"

            // ✅ Crear mapa con los campos correctos
            val ahora = Date()
            val formato = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
            val cronometroStr = formato.format(ahora)

            val cal = Calendar.getInstance()
            cal.time = ahora
            val horaPlay = String.format(
                "%02d-%02d-%02d",
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                cal.get(Calendar.SECOND)
            )

            // Obtener duración configurada (por defecto 45)
            val duracionTiempo = if (partido.TIEMPOJUEGO > "0") {
                partido.TIEMPOJUEGO
            } else {
                "45"
            }

            val updates = mapOf(
                // ═══════════════════════════════════════════════════════
                // CRONÓMETRO - Momento de inicio
                // ═══════════════════════════════════════════════════════
                "Cronometro" to cronometroStr,
                "FECHA_PLAY" to cronometroStr,
                "HORA_PLAY" to horaPlay,

                // ═══════════════════════════════════════════════════════
                // ESTADO DEL TIEMPO
                // ═══════════════════════════════════════════════════════
                "NumeroDeTiempo" to if (primerTiempo) "1T" else "3T",
                "TIEMPOSJUGADOS" to if (primerTiempo) 1 else 2,

                // ═══════════════════════════════════════════════════════
                // ESTADO DEL PARTIDO
                // ═══════════════════════════════════════════════════════
                "ESTADO" to 0,

                // ═══════════════════════════════════════════════════════
                // DURACIÓN (45 minutos, o lo configurado)
                // ⚠️ NO es el tiempo transcurrido
                // ═══════════════════════════════════════════════════════
                "TiempodeJuego" to duracionTiempo
            )

            Log.d(TAG, "📤 Actualizando Firebase:")
            Log.d(TAG, "   Cronometro: $cronometroStr")
            Log.d(TAG, "   HORA_PLAY: $horaPlay")
            Log.d(TAG, "   NumeroDeTiempo: ${if (primerTiempo) "1T" else "3T"}")
            Log.d(TAG, "   TIEMPOSJUGADOS: ${if (primerTiempo) 1 else 2}")
            Log.d(TAG, "   TiempodeJuego: $duracionTiempo (duración)")

            val result = repository.updatePartidoFields(campeonatoId, partidoId, updates)

            result.onSuccess {
                Log.d(TAG, "╔═══════════════════════════════════════════════════════")
                Log.d(TAG, "✅ Partido iniciado exitosamente")
                Log.d(TAG, "╚═══════════════════════════════════════════════════════")

                Log.d(TAG, "📍 Llamando actualizarPanelOverlay...")
                actualizarPanelOverlay("marcador")

                if (_uiState.value.modoTransmision) {
                    Log.d(TAG, "📡 Sincronizando con overlay...")
                    sincronizarConOverlay()
                }
            }.onFailure { error ->
                Log.e(TAG, "╔═══════════════════════════════════════════════════════")
                Log.e(TAG, "❌ ERROR AL INICIAR PARTIDO")
                Log.e(TAG, "   Mensaje: ${error.message}")
                Log.e(TAG, "   Stack trace: ${error.stackTraceToString()}")
                Log.e(TAG, "╚═══════════════════════════════════════════════════════")
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


    fun reiniciarPartido() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch

            _uiState.update { it.copy(actualizandoFirebase = true) }

            val updates = mapOf(
                "Cronometro" to null,
                "FECHA_PLAY" to null,
                "HORA_PLAY" to null,
                "NumeroDeTiempo" to "0T",
                "TIEMPOSJUGADOS" to 0,
                "ESTADO" to 0
            )

            val result = repository.updatePartidoFields(campeonatoId, partidoId, updates)

            result.onSuccess {
                Log.d(TAG, "✅ Partido reiniciado (campos de inicio borrados)")
                // Para que el display vuelva inmediato a 00:00
                _uiState.update { it.copy(tiempoActual = "00:00") }
            }.onFailure { e ->
                Log.e(TAG, "❌ Error reiniciando: ${e.message}", e)
                _uiState.update { it.copy(error = e.message) }
            }

            _uiState.update { it.copy(actualizandoFirebase = false) }
        }
    }


    /**
     * Ajusta el tiempo modificando FECHA_PLAY
     *
     * ✅ ESTRATEGIA:
     * - Calcular nueva FECHA_PLAY = Now - nuevoTiempoEnSegundos
     * - Esto hará que (Now - FECHA_PLAY) = nuevoTiempoEnSegundos
     */
    fun ajustarTiempo(segundos: Int) {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch

            if (!partido.estaEnCurso()) {
                Log.w(TAG, "⚠️ No se puede ajustar: partido no en curso")
                return@launch
            }

            val tiempoActual = partido.calcularTiempoActualSegundos()
            val nuevoTiempo = (tiempoActual + segundos).coerceAtLeast(0)

            Log.d(TAG, "╔═══════════════════════════════════════════════════════")
            Log.d(TAG, "⚙️ AJUSTANDO TIEMPO")
            Log.d(TAG, "   Tiempo actual: ${tiempoActual}s")
            Log.d(TAG, "   Ajuste: ${segundos}s")
            Log.d(TAG, "   Nuevo tiempo: ${nuevoTiempo}s")
            Log.d(TAG, "╚═══════════════════════════════════════════════════════")

            // Calcular nueva FECHA_PLAY
            val ahora = System.currentTimeMillis()
            val nuevoInicio = ahora - (nuevoTiempo * 1000)

            val formato = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault())
            val nuevaFechaPlay = formato.format(Date(nuevoInicio))

            val cal = Calendar.getInstance()
            cal.timeInMillis = nuevoInicio
            val nuevaHoraPlay = String.format(
                "%02d-%02d-%02d",
                cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE),
                cal.get(Calendar.SECOND)
            )

            Log.d(TAG, "   Nueva FECHA_PLAY: $nuevaFechaPlay")
            Log.d(TAG, "   Nueva HORA_PLAY: $nuevaHoraPlay")

            val updates = mapOf(
                "FECHA_PLAY" to nuevaFechaPlay,
                "HORA_PLAY" to nuevaHoraPlay,
                "Cronometro" to nuevaFechaPlay
            )

            val result = repository.updatePartidoFields(campeonatoId, partidoId, updates)

            result.onSuccess {
                Log.d(TAG, "✅ Tiempo ajustado exitosamente")
                if (_uiState.value.modoTransmision) {
                    sincronizarConOverlay()
                }
            }.onFailure { error ->
                Log.e(TAG, "❌ Error ajustando tiempo: ${error.message}")
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
                "GOLES1" to goles
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
                "GOLES1" to goles
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
                "GOLES2" to goles
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
                "GOLES2" to goles
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
                "TAMARILLAS1" to amarillas
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
                "TAMARILLAS1" to amarillas
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
                "TAMARILLAS2" to amarillas
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
                "TAMARILLAS2" to amarillas
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
                "TROJAS1" to rojas
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
                "TROJAS1" to rojas
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
                "TROJAS2" to rojas
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
                "TROJAS2" to rojas
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
                "ESQUINAS1" to esquinas
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
                "ESQUINAS1" to esquinas
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
                "ESQUINAS2" to esquinas
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
                "ESQUINAS2" to esquinas
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


        // 🔥 ESTO ES LO QUE FALTABA
                repository.actualizarTransmision(
                    campeonatoId = campeonatoId,
                    partidoId = partidoId,
                    activa = nuevoModo
                )


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

    /******** TANDA DE PENALES
     * Activa el modo penales
     *
     * ✅ ESCRIBE EN FIREBASE:
     * - MARCADOR_PENALES = true (crítico para overlay web)
     * - PENALES_INICIA = equipoInicia (quién cobra primero)
     * - PENALES_TURNO = equipoInicia (inicialmente igual)
     * - PENALES_TANDA = 1 (primera tanda)
     * - ULTIMA_ACTUALIZACION = ServerValue.TIMESTAMP
     *
     * @param equipoInicia 1 o 2 - equipo que inicia la tanda
     */
    fun activarPenales(equipoInicia: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(actualizandoFirebase = true) }

            Log.d(TAG, "╔═══════════════════════════════════════════════════════╗")
            Log.d(TAG, "🎯 ACTIVANDO MODO PENALES")
            Log.d(TAG, "   Equipo que inicia: $equipoInicia")
            Log.d(TAG, "╚═══════════════════════════════════════════════════════╝")

            val updates = mapOf(
                "MARCADOR_PENALES" to true,  // ← ✅ CRÍTICO: Overlay web lee esto
                "PENALES_INICIA" to equipoInicia,
                "PENALES_TURNO" to equipoInicia,
                "PENALES_TANDA" to 1,
                "PENALES_SERIE1" to emptyList<Int>(),
                "PENALES_SERIE2" to emptyList<Int>(),
                "ULTIMA_ACTUALIZACION" to com.google.firebase.database.ServerValue.TIMESTAMP
            )

            val result = repository.updatePartidoFields(campeonatoId, partidoId, updates)

            result.onSuccess {
                Log.d(TAG, "✅ Modo penales activado correctamente")

                // ✅ NUEVO: Actualizar panel del overlay
                actualizarPanelOverlay("penales")  // ← AGREGAR ESTA LÍNEA

                // Sincronizar con overlay si está activo
                if (_uiState.value.modoTransmision) {
                    sincronizarConOverlay()
                }
            }.onFailure { error ->
                Log.e(TAG, "❌ Error activando penales: ${error.message}")
                _uiState.update { it.copy(error = error.message) }
            }

            _uiState.update { it.copy(actualizandoFirebase = false) }
        }
    }

    /**
     * Desactiva el modo penales
     *
     * ✅ ESCRIBE EN FIREBASE:
     * - MARCADOR_PENALES = false (crítico para overlay web)
     * - ULTIMA_ACTUALIZACION = ServerValue.TIMESTAMP
     *
     * ⚠️ NO resetea los contadores ni el historial
     * Los datos se mantienen por si se reactiva
     */
    fun desactivarPenales() {
        viewModelScope.launch {
            _uiState.update { it.copy(actualizandoFirebase = true) }

            Log.d(TAG, "╔═══════════════════════════════════════════════════════╗")
            Log.d(TAG, "⚪ DESACTIVANDO MODO PENALES")
            Log.d(TAG, "╚═══════════════════════════════════════════════════════╝")

            val updates = mapOf(
                "MARCADOR_PENALES" to false,  // ← ✅ CRÍTICO: Overlay web lee esto
                "ULTIMA_ACTUALIZACION" to com.google.firebase.database.ServerValue.TIMESTAMP
            )

            val result = repository.updatePartidoFields(campeonatoId, partidoId, updates)

            result.onSuccess {
                Log.d(TAG, "✅ Modo penales desactivado correctamente")

                // ✅ NUEVO: Actualizar panel del overlay
                actualizarPanelOverlay("marcador")  // ← AGREGAR ESTA LÍNEA

                // Sincronizar con overlay si está activo
                if (_uiState.value.modoTransmision) {
                    sincronizarConOverlay()
                }
            }.onFailure { error ->
                Log.e(TAG, "❌ Error desactivando penales: ${error.message}")
                _uiState.update { it.copy(error = error.message) }
            }

            _uiState.update { it.copy(actualizandoFirebase = false) }
        }
    }

    /**
     * Cambia el equipo que INICIA la tanda (corrección)
     *
     * @param nuevoEquipo 1 o 2
     */
    fun cambiarEquipoInicia(nuevoEquipo: Int) {
        viewModelScope.launch {
            Log.d(TAG, "🔄 Cambiando equipo inicial a: $nuevoEquipo")

            val updates = mapOf(
                "PENALES_INICIA" to nuevoEquipo,
                "ULTIMA_ACTUALIZACION" to com.google.firebase.database.ServerValue.TIMESTAMP
            )

            repository.updatePartidoFields(campeonatoId, partidoId, updates)
        }
    }

    /**
     * Cambia el turno manualmente (corrección)
     *
     * @param nuevoTurno 1 o 2
     */
    fun cambiarTurno(nuevoTurno: Int) {
        viewModelScope.launch {
            Log.d(TAG, "🔄 Cambiando turno a equipo: $nuevoTurno")

            val updates = mapOf(
                "PENALES_TURNO" to nuevoTurno,
                "ULTIMA_ACTUALIZACION" to com.google.firebase.database.ServerValue.TIMESTAMP
            )

            repository.updatePartidoFields(campeonatoId, partidoId, updates)

            if (_uiState.value.modoTransmision) {
                sincronizarConOverlay()
            }
        }
    }

    /**
     * Registra un GOL en el penal
     *
     * ✅ Incrementa el contador del equipo en turno
     * ✅ Agrega 1 al historial
     * ✅ Cambia el turno automáticamente
     */
    fun anotarGolPenal() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            val equipoEnTurno = _uiState.value.equipoEnTurno

            Log.d(TAG, "╔═══════════════════════════════════════════════════════╗")
            Log.d(TAG, "✅ GOL DE PENAL - Equipo $equipoEnTurno")
            Log.d(TAG, "╚═══════════════════════════════════════════════════════╝")

            val nuevoTurno = if (equipoEnTurno == 1) 2 else 1

            val updates = if (equipoEnTurno == 1) {
                mapOf(
                    "PENALES1" to (partido.PENALES1 + 1),  // ← Incrementar contador
                    "PENALES_SERIE1" to (_uiState.value.historiaPenales1 + 1),  // ← Agregar 1 (gol)
                    "PENALES_TURNO" to nuevoTurno,  // ← Cambiar turno
                    "ULTIMA_ACTUALIZACION" to com.google.firebase.database.ServerValue.TIMESTAMP
                )
            } else {
                mapOf(
                    "PENALES2" to (partido.PENALES2 + 1),
                    "PENALES_SERIE2" to (_uiState.value.historiaPenales2 + 1),
                    "PENALES_TURNO" to nuevoTurno,
                    "ULTIMA_ACTUALIZACION" to com.google.firebase.database.ServerValue.TIMESTAMP
                )
            }

            val result = repository.updatePartidoFields(campeonatoId, partidoId, updates)

            result.onSuccess {
                Log.d(TAG, "✅ Gol registrado. Turno cambió a equipo $nuevoTurno")

                if (_uiState.value.modoTransmision) {
                    sincronizarConOverlay()
                }
            }.onFailure { error ->
                Log.e(TAG, "❌ Error registrando gol: ${error.message}")
            }
        }
    }

    /**
     * Registra un FALLO en el penal
     *
     * ✅ NO incrementa el contador (solo goles cuentan)
     * ✅ Agrega 0 al historial
     * ✅ Cambia el turno automáticamente
     */
    fun anotarFalloPenal() {
        viewModelScope.launch {
            val equipoEnTurno = _uiState.value.equipoEnTurno

            Log.d(TAG, "╔═══════════════════════════════════════════════════════╗")
            Log.d(TAG, "❌ FALLO DE PENAL - Equipo $equipoEnTurno")
            Log.d(TAG, "╚═══════════════════════════════════════════════════════╝")

            val nuevoTurno = if (equipoEnTurno == 1) 2 else 1

            val updates = if (equipoEnTurno == 1) {
                mapOf(
                    // ⚠️ NO incrementar Penales1 (solo goles cuentan)
                    "PENALES_SERIE1" to (_uiState.value.historiaPenales1 + 0),  // ← Agregar 0 (fallo)
                    "PENALES_TURNO" to nuevoTurno,
                    "ULTIMA_ACTUALIZACION" to com.google.firebase.database.ServerValue.TIMESTAMP
                )
            } else {
                mapOf(
                    // ⚠️ NO incrementar Penales2
                    "PENALES_SERIE2" to (_uiState.value.historiaPenales2 + 0),
                    "PENALES_TURNO" to nuevoTurno,
                    "ULTIMA_ACTUALIZACION" to com.google.firebase.database.ServerValue.TIMESTAMP
                )
            }

            val result = repository.updatePartidoFields(campeonatoId, partidoId, updates)

            result.onSuccess {
                Log.d(TAG, "✅ Fallo registrado. Turno cambió a equipo $nuevoTurno")

                if (_uiState.value.modoTransmision) {
                    sincronizarConOverlay()
                }
            }.onFailure { error ->
                Log.e(TAG, "❌ Error registrando fallo: ${error.message}")
            }
        }
    }

    /**
     * Corrección manual: incrementa contador de penales equipo 1
     */
    fun agregarPenalManualEquipo1() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch

            Log.d(TAG, "➕ Corrección manual: +1 penal equipo 1")

            val updates = mapOf(
                "Penales1" to (partido.PENALES1 + 1),
                "ULTIMA_ACTUALIZACION" to com.google.firebase.database.ServerValue.TIMESTAMP
            )

            repository.updatePartidoFields(campeonatoId, partidoId, updates)

            if (_uiState.value.modoTransmision) {
                sincronizarConOverlay()
            }
        }
    }

    /**
     * Corrección manual: decrementa contador de penales equipo 1
     */
    fun restarPenalManualEquipo1() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            val nuevoPenales = (partido.PENALES1 - 1).coerceAtLeast(0)

            Log.d(TAG, "➖ Corrección manual: -1 penal equipo 1")

            val updates = mapOf(
                "Penales1" to nuevoPenales,
                "ULTIMA_ACTUALIZACION" to com.google.firebase.database.ServerValue.TIMESTAMP
            )

            repository.updatePartidoFields(campeonatoId, partidoId, updates)

            if (_uiState.value.modoTransmision) {
                sincronizarConOverlay()
            }
        }
    }

    /**
     * Corrección manual: incrementa contador de penales equipo 2
     */
    fun agregarPenalManualEquipo2() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch

            Log.d(TAG, "➕ Corrección manual: +1 penal equipo 2")

            val updates = mapOf(
                "Penales2" to (partido.PENALES2 + 1),
                "ULTIMA_ACTUALIZACION" to com.google.firebase.database.ServerValue.TIMESTAMP
            )

            repository.updatePartidoFields(campeonatoId, partidoId, updates)

            if (_uiState.value.modoTransmision) {
                sincronizarConOverlay()
            }
        }
    }

    /**
     * Corrección manual: decrementa contador de penales equipo 2
     */
    fun restarPenalManualEquipo2() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            val nuevoPenales = (partido.PENALES2 - 1).coerceAtLeast(0)

            Log.d(TAG, "➖ Corrección manual: -1 penal equipo 2")

            val updates = mapOf(
                "Penales2" to nuevoPenales,
                "ULTIMA_ACTUALIZACION" to com.google.firebase.database.ServerValue.TIMESTAMP
            )

            repository.updatePartidoFields(campeonatoId, partidoId, updates)

            if (_uiState.value.modoTransmision) {
                sincronizarConOverlay()
            }
        }
    }

    /**
     * Inicia una nueva tanda de penales (muerte súbita)
     *
     * ✅ Incrementa PENALES_TANDA
     * ✅ Limpia PENALES_SERIE1 y PENALES_SERIE2
     * ✅ Resetea turno al equipo que inició
     * ⚠️ NO resetea Penales1 ni Penales2 (contadores globales)
     */
    fun nuevaTandaPenales() {
        viewModelScope.launch {
            val tandaActual = _uiState.value.tandaActual
            val equipoQueInicia = _uiState.value.equipoQueInicia

            Log.d(TAG, "╔═══════════════════════════════════════════════════════╗")
            Log.d(TAG, "🔄 NUEVA TANDA DE PENALES (MUERTE SÚBITA)")
            Log.d(TAG, "   Tanda actual: $tandaActual → ${tandaActual + 1}")
            Log.d(TAG, "   Comienza equipo: $equipoQueInicia")
            Log.d(TAG, "╚═══════════════════════════════════════════════════════╝")

            val updates = mapOf(
                "PENALES_TANDA" to (tandaActual + 1),  // ← Incrementar contador
                "PENALES_SERIE1" to emptyList<Int>(),  // ← Limpiar historial
                "PENALES_SERIE2" to emptyList<Int>(),  // ← Limpiar historial
                "PENALES_TURNO" to equipoQueInicia,    // ← Resetear turno
                "ULTIMA_ACTUALIZACION" to com.google.firebase.database.ServerValue.TIMESTAMP
            )

            val result = repository.updatePartidoFields(campeonatoId, partidoId, updates)

            result.onSuccess {
                Log.d(TAG, "✅ Nueva tanda iniciada (tanda #${tandaActual + 1})")

                if (_uiState.value.modoTransmision) {
                    sincronizarConOverlay()
                }
            }.onFailure { error ->
                Log.e(TAG, "❌ Error iniciando nueva tanda: ${error.message}")
            }
        }
    }


    /**
     * Actualiza el campo __PANEL_ACTIVO__ en /CONFIGURACION_OVERLAYWEB
     * Este campo indica al overlay web qué panel debe mostrar
     *
     * @param panel "marcador" para mostrar el marcador normal, "penales" para modo penales
     */
    private fun actualizarPanelOverlay(panel: String) {
        viewModelScope.launch {
            try {
                val reference = com.google.firebase.database.FirebaseDatabase.getInstance().reference
                    .child("CONFIGURACION_OVERLAYWEB")
                    .child("PANEL_ACTIVO")  // ✅ CORRECTO: sin guiones bajos

                reference.setValue(panel).await()
                Log.d(TAG, "✅ Panel overlay actualizado a: $panel")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error actualizando panel overlay: ${e.message}")
            }
        }
    }


// ┌─────────────────────────────────────────────────────────────────────────┐
// │ FUNCIÓN 2: Finalizar y resetear penales                                 │
// │ Insertar después de la función nuevaTandaPenales()                      │
// └─────────────────────────────────────────────────────────────────────────┘

    /**
     * Finaliza y resetea completamente la tanda de penales
     *
     * ✅ RESETEA TODO:
     * - Contadores de goles (PENALES1, PENALES2) → 0
     * - Configuración (PENALES_INICIA, PENALES_TANDA, PENALES_TURNO) → valores por defecto
     * - Historial (PENALES_SERIE1, PENALES_SERIE2) → listas vacías
     * - Desactiva el modo penales (MARCADOR_PENALES) → false
     * - Actualiza el panel del overlay → "marcador"
     *
     * ⚠️ Esta acción es IRREVERSIBLE y requiere confirmación del usuario
     */
    fun finalizarYResetearPenales() {
        viewModelScope.launch {
            _uiState.update { it.copy(actualizandoFirebase = true) }

            Log.d(TAG, "╔══════════════════════════════════════════════════════╗")
            Log.d(TAG, "🔄 FINALIZANDO Y RESETEANDO TANDA DE PENALES")
            Log.d(TAG, "   Se resetearán todos los contadores y configuración")
            Log.d(TAG, "╚══════════════════════════════════════════════════════╝")

            val updates = mapOf(
                // ✅ Resetear contadores de goles
                "PENALES1" to 0,
                "PENALES2" to 0,

                // ✅ Resetear configuración
                "PENALES_INICIA" to 1,
                "PENALES_TANDA" to 1,
                "PENALES_TURNO" to 1,

                // ✅ Limpiar historial
                "PENALES_SERIE1" to emptyList<Int>(),
                "PENALES_SERIE2" to emptyList<Int>(),

                // ✅ Desactivar modo penales
                "MARCADOR_PENALES" to false,

                "ULTIMA_ACTUALIZACION" to com.google.firebase.database.ServerValue.TIMESTAMP
            )

            val result = repository.updatePartidoFields(campeonatoId, partidoId, updates)

            result.onSuccess {
                Log.d(TAG, "✅ Tanda de penales finalizada y reseteada correctamente")

                // Actualizar configuración del overlay
                actualizarPanelOverlay("marcador")

                // ✅ NUEVO: Actualizar PARTIDOACTUAL directamente
                actualizarPartidoActualPenales(
                    penales1 = 0,
                    penales2 = 0,
                    penalesInicia = 1,
                    penalesTanda = 1,
                    penalesTurno = 1,
                    serie1 = emptyList(),
                    serie2 = emptyList(),
                    marcadorPenales = false
                )

                // Sincronizar con overlay si está activo
                if (_uiState.value.modoTransmision) {
                    sincronizarConOverlay()
                }
            }.onFailure { error ->
                Log.e(TAG, "❌ Error finalizando penales: ${error.message}")
                _uiState.update { it.copy(error = error.message) }
            }

            _uiState.update { it.copy(actualizandoFirebase = false) }
        }
    }


    private fun actualizarPartidoActualPenales(
        penales1: Int,
        penales2: Int,
        penalesInicia: Int,
        penalesTanda: Int,
        penalesTurno: Int,
        serie1: List<Int>,
        serie2: List<Int>,
        marcadorPenales: Boolean
    ) {
        viewModelScope.launch {
            try {
                val reference = com.google.firebase.database.FirebaseDatabase.getInstance().reference
                    .child("ARKI_DEPORTES")
                    .child("PARTIDOACTUAL")

                val updates = mapOf(
                    "PENALES1" to penales1,
                    "PENALES2" to penales2,
                    "PENALES_INICIA" to penalesInicia,
                    "PENALES_TANDA" to penalesTanda,
                    "PENALES_TURNO" to penalesTurno,
                    "PENALES_SERIE1" to serie1,
                    "PENALES_SERIE2" to serie2,
                    "MARCADOR_PENALES" to marcadorPenales,
                    "ULTIMA_ACTUALIZACION" to com.google.firebase.database.ServerValue.TIMESTAMP
                )

                reference.updateChildren(updates).await()
                Log.d(TAG, "✅ PARTIDOACTUAL actualizado directamente")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error actualizando PARTIDOACTUAL: ${e.message}")
            }
        }
    }

}