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
import com.example.arki_deportes.data.context.UsuarioContext
import com.example.arki_deportes.data.model.*
import kotlinx.coroutines.Dispatchers
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
    val marcadorFutbolVisible: Boolean = true, // 👈 AÑADE ESTA LÍNEA
    val nombreCampeonatoReal: String = "",
    val isLoading: Boolean = true,
    val partido: Partido? = null,
    val tiempoActual: String = "00:00",
    val error: String? = null,
    val modoTransmision: Boolean = true,
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
    val historiaPenales2: List<Int> = emptyList(),
    val banners: List<BannerResource> = emptyList(),
    val selectedBannerIds: Set<String> = emptySet(),
    // --- GESTIÓN DE AUDIO ---
    val audios: List<AudioResource> = emptyList(),
    val volumenAudio: Int = 50,
    val audioEstado: String = "STOP" // "PLAY", "STOP", "PAUSE"
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

    // 🚩 Bandera para asegurar que la sincronización inicial solo ocurra una vez
    private var isInitialSyncDone = false

    init {

        observarPartido()
        observarPenales()
        observarBanners()
        iniciarActualizadorDeTiempo()
        obtenerNombreCampeonato()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // OBSERVACIÓN DE DATOS
    // ═══════════════════════════════════════════════════════════════════════
    private fun obtenerNombreCampeonato() {
        viewModelScope.launch {
            try {
                // Leemos directamente del nodo del campeonato usando el ID que ya tenemos
                val camp = repository.getCampeonato(campeonatoId)
                _uiState.update { it.copy(nombreCampeonatoReal = camp?.CAMPEONATO ?: "Campeonato") }
            } catch (e: Exception) {
                Log.e(TAG, "Error al traer nombre camp: ${e.message}")
            }
        }
    }

    /**
     * Observa el partido en tiempo real desde Firebase
     * VB.NET Equivalente: Escuchar cambios en Firebase
     */
    private fun observarPartido() {

        viewModelScope.launch {
            val campeonato = repository.getCampeonato(campeonatoId)
            val nombreRealCamp = campeonato?.CAMPEONATO ?: "Campeonato"

            repository.observePartido(campeonatoId, partidoId)
                .catch { error ->
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

                    // 2. Si el partido no tiene el nombre, se lo inyectamos manualmente
                    if (partido != null) {
                        val partidoConNombre = if (partido.CAMPEONATOTXT.isBlank()) {
                            partido.copy(CAMPEONATOTXT = nombreRealCamp)
                        } else {
                            partido
                        }

                        _uiState.update {
                            it.copy(
                                partido = partidoConNombre,
                                isLoading = false,
                                error = null
                            )
                        }

                        // 🚀 3. SINCRONIZACIÓN INICIAL AUTOMÁTICA
                        // Si el modo está activo por defecto (true) y es la primera vez que detectamos datos
                        if (_uiState.value.modoTransmision && !isInitialSyncDone) {
                            isInitialSyncDone = true
                            Log.d(TAG, "📡 Ejecutando sincronización inicial automática para Overlay...")

                            // Aseguramos que el flag de transmisión en Firebase esté activo
                            repository.actualizarTransmision(campeonatoId, partidoId, true)

                            // Disparamos la publicación y sincronización
                            viewModelScope.launch {
                                repository.publicarEnPartidosJugandose(partidoConNombre)
                                sincronizarConOverlay(partidoConNombre)
                            }
                        }

                    } else {
                        // Si el partido es nulo (ej: fue eliminado)
                        _uiState.update { it.copy(isLoading = false) }
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
                    // 🛡️ SEGURIDAD: Solo actualizamos si el partido existe
                    if (partido != null) {
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
            if (partido.estaEnCurso() || partido.estaFinalizado()) return@launch

            _uiState.update { it.copy(actualizandoFirebase = true) }
            val ahora = Date()
            val cronoStr = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS", Locale.getDefault()).format(ahora)
            val cal = Calendar.getInstance().apply { time = ahora }
            val horaPlay = String.format("%02d-%02d-%02d", cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), cal.get(Calendar.SECOND))

            val esBasquet = partido.DEPORTE.equals("BASQUET", ignoreCase = true)
            // Determinar periodo según Deporte
            val (nuevoNT, tiemposJ) = if (partido.DEPORTE == "BASQUET") {
                when (partido.NumeroDeTiempo) {
                    "0T" -> "1T" to 1; "2T" -> "3T" to 2; "4T" -> "5T" to 3; "6T" -> "7T" to 4
                    "8T" -> "9T" to 5; "10T" -> "11T" to 6; "12T" -> "13T" to 7
                    else -> partido.NumeroDeTiempo to partido.TIEMPOSJUGADOS
                }
            } else {
                if (partido.NumeroDeTiempo == "0T") "1T" to 1 else "3T" to 2
            }

            val updates = mapOf(
                "Cronometro" to cronoStr, "FECHA_PLAY" to cronoStr, "HORA_PLAY" to horaPlay,
                "NumeroDeTiempo" to nuevoNT, "TIEMPOSJUGADOS" to tiemposJ, "ESTADO" to 0
            )

            repository.updatePartidoFields(campeonatoId, partidoId, updates).onSuccess {
                val pUpd = partido.copy(NumeroDeTiempo = nuevoNT, TIEMPOSJUGADOS = tiemposJ, ESTADO = 0, FECHA_PLAY = cronoStr)
                repository.publicarEnPartidosJugandose(pUpd)
                if (_uiState.value.modoTransmision) sincronizarConOverlay(pUpd)
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

    /*
    fun detenerCronometro() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            _uiState.update { it.copy(actualizandoFirebase = true) }

            val nuevoEstado = if (partido.DEPORTE == "BASQUET") {
                when (partido.NumeroDeTiempo) {
                    "1T" -> "2T"; "3T" -> "4T"; "5T" -> "6T"; "7T" -> "8T"
                    "9T" -> "10T"; "11T" -> "12T"; "13T" -> "14T"
                    else -> partido.NumeroDeTiempo
                }
            } else {
                if (partido.NumeroDeTiempo == "1T") "2T" else "4T"
            }

            val estaFinalizado = if (partido.DEPORTE == "BASQUET") {
                listOf("8T", "10T", "12T", "14T").contains(nuevoEstado)
            } else {
                nuevoEstado == "4T"
            }

            val updates = mapOf("NumeroDeTiempo" to nuevoEstado, "ESTADO" to if (estaFinalizado) 1 else 0)
            repository.updatePartidoFields(campeonatoId, partidoId, updates).onSuccess {
                val pUpd = partido.copy(NumeroDeTiempo = nuevoEstado, ESTADO = if (estaFinalizado) 1 else 0)
                repository.publicarEnPartidosJugandose(pUpd)
                if (_uiState.value.modoTransmision) sincronizarConOverlay(pUpd)
            }
            _uiState.update { it.copy(actualizandoFirebase = false) }
        }
    }
    */

    fun detenerCronometro() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            _uiState.update { it.copy(actualizandoFirebase = true) }

            val esBasquet = partido.DEPORTE.equals("BASQUET", ignoreCase = true)
            val nuevoEstado = if (esBasquet) {
                when (partido.NumeroDeTiempo) {
                    "1T" -> "2T"; "3T" -> "4T"; "5T" -> "6T"; "7T" -> "8T"
                    "9T" -> "10T"; "11T" -> "12T"; "13T" -> "14T"
                    else -> partido.NumeroDeTiempo
                }
            } else {
                if (partido.NumeroDeTiempo == "1T") "2T" else "4T"
            }

            // Solo finaliza automáticamente en Fútbol (4T) o al llegar al límite de extras en Básquet (14T)
            val estaFinalizado = if (esBasquet) nuevoEstado == "14T" else nuevoEstado == "4T"

            val updates = mapOf("NumeroDeTiempo" to nuevoEstado, "ESTADO" to if (estaFinalizado) 1 else 0)
            repository.updatePartidoFields(campeonatoId, partidoId, updates).onSuccess {
                val pUpd = partido.copy(NumeroDeTiempo = nuevoEstado, ESTADO = if (estaFinalizado) 1 else 0)
                repository.publicarEnPartidosJugandose(pUpd)
                if (_uiState.value.modoTransmision) sincronizarConOverlay(pUpd)
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
    /*
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
    */

    fun agregarGolEquipo1() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            if (!partido.estaEnCurso()) return@launch

            val nuevosGoles = partido.GOLES1 + 1
            val minuto = partido.calcularTiempoActualSegundos() / 60
            val marcador = "$nuevosGoles-${partido.GOLES2}"
            val textoAccion = "⚽ $minuto' ¡GOOOOL! ${partido.EQUIPO1.uppercase()} $marcador"

            val updates = mapOf("GOLES1" to nuevosGoles)
            repository.updatePartidoFields(campeonatoId, partidoId, updates)

            enviarAccionJugada(textoAccion)
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

    /*
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
    */
    fun agregarGolEquipo2() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            if (!partido.estaEnCurso()) return@launch

            val nuevosGoles = partido.GOLES2 + 1
            val minuto = partido.calcularTiempoActualSegundos() / 60
            val marcador = "$nuevosGoles-${partido.GOLES1}"

            val textoAccion = "⚽ $minuto' ¡GOOOOL! ${partido.EQUIPO2.uppercase()} $marcador"

            val updates = mapOf("GOLES2" to nuevosGoles)
            repository.updatePartidoFields(campeonatoId, partidoId, updates)

            enviarAccionJugada(textoAccion)
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
    /*
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
*/
    fun agregarAmarillaEquipo1() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            if (!partido.estaEnCurso()) return@launch

            val nuevasAmarillas = partido.TAMARILLAS1 + 1
            val minuto = partido.calcularTiempoActualSegundos() / 60
            val textoAccion = "🟨 $minuto' AMARILLA - ${partido.EQUIPO1.uppercase()} (Total: $nuevasAmarillas)"

            val updates = mapOf("TAMARILLAS1" to nuevasAmarillas)
            repository.updatePartidoFields(campeonatoId, partidoId, updates)

            enviarAccionJugada(textoAccion)
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
    /*
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
    */

    fun agregarAmarillaEquipo2() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            if (!partido.estaEnCurso()) return@launch

            val nuevasAmarillas = partido.TAMARILLAS2 + 1
            val minuto = partido.calcularTiempoActualSegundos() / 60
            val textoAccion = "🟨 $minuto' AMARILLA - ${partido.EQUIPO2.uppercase()} (Total: $nuevasAmarillas)"

            val updates = mapOf("TAMARILLAS2" to nuevasAmarillas)
            repository.updatePartidoFields(campeonatoId, partidoId, updates)

            enviarAccionJugada(textoAccion)
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
    /*
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
*/
    fun agregarRojaEquipo1() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            if (!partido.estaEnCurso()) return@launch

            val nuevasRojas = partido.TROJAS1 + 1
            val minuto = partido.calcularTiempoActualSegundos() / 60
            val textoAccion = "🟥 $minuto' ROJA - ${partido.EQUIPO1.uppercase()} (Total: $nuevasRojas)"

            val updates = mapOf("TROJAS1" to nuevasRojas)
            repository.updatePartidoFields(campeonatoId, partidoId, updates)

            enviarAccionJugada(textoAccion)
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
    /*
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
*/
    fun agregarRojaEquipo2() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            if (!partido.estaEnCurso()) return@launch

            val nuevasRojas = partido.TROJAS2 + 1
            val minuto = partido.calcularTiempoActualSegundos() / 60
            val textoAccion = "🟥 $minuto' ROJA - ${partido.EQUIPO2.uppercase()} (Total: $nuevasRojas)"

            val updates = mapOf("TROJAS2" to nuevasRojas)
            repository.updatePartidoFields(campeonatoId, partidoId, updates)

            enviarAccionJugada(textoAccion)
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
    /*
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
*/

    fun agregarEsquinaEquipo1() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            if (!partido.estaEnCurso()) return@launch

            val nuevasEsquinas = partido.ESQUINAS1 + 1
            val textoAccion = "🚩 ESQUINA - ${partido.EQUIPO1.uppercase()} (Total: $nuevasEsquinas)"

            // Ruta de audio local para que la Web o la App reproduzcan
            val rutaAudio = "D:\\SONIDOS DXT\\10 SPOT TIRO DE ESQUINA.mp3"

            val updates = mapOf("ESQUINAS1" to nuevasEsquinas)
            repository.updatePartidoFields(campeonatoId, partidoId, updates)

            enviarAccionJugada(textoAccion, rutaAudio)
        }
    }

    fun agregarEsquinaEquipo2() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            if (!partido.estaEnCurso()) return@launch

            val nuevasEsquinas = partido.ESQUINAS2 + 1
            val textoAccion = "🚩 ESQUINA - ${partido.EQUIPO2.uppercase()} (Total: $nuevasEsquinas)"

            val rutaAudio = "D:\\SONIDOS DXT\\10 SPOT TIRO DE ESQUINA.mp3"

            val updates = mapOf("ESQUINAS2" to nuevasEsquinas)
            repository.updatePartidoFields(campeonatoId, partidoId, updates)

            enviarAccionJugada(textoAccion, rutaAudio)
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
            _uiState.value.partido?.let { partido ->
                viewModelScope.launch {
                    repository.publicarEnPartidosJugandose(partido) // 👈 Publicar al activar switch
                    sincronizarConOverlay(partido)
                }
            }
        }
    }

    /**
     * Sincroniza los datos al nodo PARTIDOACTUAL (para overlay)
     */
    private fun sincronizarConOverlay(partidoASincronizar: Partido? = null) {
        viewModelScope.launch {
            // Usa el partido pasado como parámetro, o si no, el del estado.
            val partido = partidoASincronizar ?: _uiState.value.partido ?: return@launch

            Log.d(TAG, "Sincronizando con overlay... CRONOMETRANDO=${partido.estaEnCurso()}")

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


    /**
     * Función centralizada para enviar la acción jugada a Firebase (Ruta del partido)
     * VB.NET Equivalente: Call EscribirTimer(...) en LOWERTHIRDS\ACCION_JUGADA_MINUTO.txt
     */
    private fun enviarAccionJugada(texto: String, rutaAudio: String = "") {
        viewModelScope.launch {

            val esFutbol = _uiState.value.partido?.DEPORTE?.equals("FUTBOL", ignoreCase = true) == true
            /*
            val updates = mapOf(
                "ACCION_JUGADA_MINUTO" to texto,
                "ACCION_AUDIO_URL" to rutaAudio,
                "ULTIMA_ACTUALIZACION" to com.google.firebase.database.ServerValue.TIMESTAMP
            )
            */

            val updates = mutableMapOf<String, Any>(
                "ACCION_AUDIO_URL" to rutaAudio,
                "ULTIMA_ACTUALIZACION" to com.google.firebase.database.ServerValue.TIMESTAMP
            )


            // ✅ Solo enviamos el texto de la jugada si el deporte es FUTBOL
            if (esFutbol) {
                updates["ACCION_JUGADA_MINUTO"] = texto
            }

            repository.updatePartidoFields(campeonatoId, partidoId, updates)

            // 2. 🚀 ACTUALIZAR EN PARTIDOACTUAL (Para la Web)
            //actualizarPartidoActualAccion(texto, rutaAudio)

            // Si es Fútbol enviamos el texto, si es Básquet mandamos vacío para limpiar el overlay
            actualizarPartidoActualAccion(if (esFutbol) texto else "", rutaAudio)


            // 🎵 REPRODUCCIÓN DE AUDIO (Futura implementación)
            // a futuro la app leera desde firebase la ruta de audio local o en storage y lo reproducira
            if (rutaAudio.isNotEmpty()) {
                // reproducirAudioLocal(rutaAudio)
            }
        }
    }

    /**
     * Actualiza específicamente los campos de acción en el nodo PARTIDOACTUAL
     */
    private fun actualizarPartidoActualAccion(texto: String, rutaAudio: String) {
        viewModelScope.launch {
            try {
                val reference = com.google.firebase.database.FirebaseDatabase.getInstance().reference
                    .child("ARKI_DEPORTES")
                    .child("PARTIDOACTUAL")

                val updates = mapOf(
                    "ACCION_JUGADA_MINUTO" to texto,
                    "ACCION_AUDIO_URL" to rutaAudio,
                    "ULTIMA_ACTUALIZACION" to com.google.firebase.database.ServerValue.TIMESTAMP
                )

                reference.updateChildren(updates).await()
                Log.d(TAG, "✅ PARTIDOACTUAL: Acción enviada -> $texto")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error actualizando acción en PARTIDOACTUAL: ${e.message}")
            }
        }
    }


    /**
     * Finaliza la asignación del partido actual y libera el perfil del usuario.
     * Esto permite que el usuario pueda autoasignarse un nuevo partido desde el Home.*/
    fun finalizarPartido(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                val usuario = UsuarioContext.getUsuario() ?: return@launch
                val idUsuario = usuario.usuario // Usamos el ID de usuario (ej: "Carlos")

                Log.d(TAG, "🏁 Finalizando y liberando partido para: $idUsuario")

                // 1. Limpiar asignación en Firebase
                com.google.firebase.database.FirebaseDatabase.getInstance().reference
                    .child("AppConfig")
                    .child("Usuarios")
                    .child(idUsuario)
                    .child("permisos")
                    .updateChildren(mapOf(
                        "codigoCampeonato" to "NINGUNO",
                        "codigoPartido" to "NINGUNO"
                    )).await()

                // 2. Limpiar contexto local para que la App sepa que no hay partido
                UsuarioContext.limpiarPartidoAsignado()

                // 3. Ejecutar navegación (volver al Home)
                onSuccess()

            } catch (e: Exception) {
                Log.e(TAG, "❌ Error al liberar partido: ${e.message}")
                _uiState.update { it.copy(error = "Error al liberar partido: ${e.message}") }
            }
        }
    }


    /**
     * Observa la lista de banners publicitarios disponibles
     */
    private fun observarBanners() {
        viewModelScope.launch {
            repository.observeBanners()
                .catch { Log.e(TAG, "❌ Error observando banners: ${it.message}") }
                .collect { banners ->
                    _uiState.update { it.copy(banners = banners) }
                }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // GESTIÓN DE PUBLICIDAD
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Alterna la selección de un banner para envío secuencial
     */
    fun toggleBannerSelection(bannerId: String) {
        _uiState.update { state ->
            val newList = if (state.selectedBannerIds.contains(bannerId)) {
                state.selectedBannerIds - bannerId
            } else {
                state.selectedBannerIds + bannerId
            }
            state.copy(selectedBannerIds = newList)
        }
    }

    /**
     * Envía un único anuncio al overlay
     */
    fun enviarPublicidadUnica(banner: BannerResource) {
        viewModelScope.launch {
            val anuncio = AnuncioPublicidad(
                tipo = banner.tipo.lowercase(),
                contenido = when (banner.tipo.uppercase()) {
                    "VIDEO" -> banner.urlVideo
                    "HTML" -> banner.codigoHtml
                    else -> banner.urlImagen
                },
                duracion = 10
            )
            repository.enviarAnuncioUnico(anuncio)
        }
    }

    /**
     * Envía la lista de anuncios seleccionados en secuencia
     */
    fun enviarListaSecuencial() {
        val selectedIds = _uiState.value.selectedBannerIds
        if (selectedIds.isEmpty()) return

        viewModelScope.launch {
            val anuncios = _uiState.value.banners
                .filter { it.id in selectedIds }
                .sortedBy { selectedIds.indexOf(it.id) }
                .map { banner ->
                    AnuncioPublicidad(
                        tipo = banner.tipo.lowercase(),
                        contenido = when (banner.tipo.uppercase()) {
                            "VIDEO" -> banner.urlVideo
                            "HTML" -> banner.codigoHtml
                            else -> banner.urlImagen
                        },
                        duracion = 10
                    )
                }
            repository.enviarListaAnuncios(anuncios)
            // Limpiar selección después de enviar
            _uiState.update { it.copy(selectedBannerIds = emptySet()) }
        }
    }

    /**
     * Oculta la publicidad inmediatamente en el overlay
     */
    fun ocultarPublicidad() {
        viewModelScope.launch {
            repository.ocultarPublicidad()
        }
    }

    /**
     * Observa el catálogo de audios
     */
    private fun observarAudios() {
        viewModelScope.launch {
            repository.observeAudios()
                .catch { Log.e(TAG, "Error audios: ${it.message}") }
                .collect { lista ->
                    _uiState.update { it.copy(audios = lista) }
                }
        }
    }

    // --- COMANDOS DE AUDIO PARA FIREBASE ---

    private val audioControlRef = com.google.firebase.database.FirebaseDatabase.getInstance()
        .getReference("/ARKI_DEPORTES/CONTROL_AUDIO")

    fun reproducirAudio(audio: AudioResource) {
        viewModelScope.launch {
            // Si es música, actualizamos la URL y ponemos PLAY
            if (audio.tipo == "MUSICA") {
                audioControlRef.child("URL").setValue(audio.url)
                audioControlRef.child("ESTADO").setValue("PLAY")
                _uiState.update { it.copy(audioEstado = "PLAY") }
            } else {
                // Si es FX, enviamos un disparo único (puedes usar el repositorio)
                repository.reproducirAudioEnOverlay(audio)
            }
        }
    }

    fun pausarAudio() {
        audioControlRef.child("ESTADO").setValue("PAUSE")
        _uiState.update { it.copy(audioEstado = "PAUSE") }
    }

    fun detenerAudio() {
        audioControlRef.child("ESTADO").setValue("STOP")
        _uiState.update { it.copy(audioEstado = "STOP") }
    }

    fun cambiarVolumen(nuevoVolumen: Int) {
        val vol = nuevoVolumen.coerceIn(0, 100)
        audioControlRef.child("VOLUMEN").setValue(vol)
        _uiState.update { it.copy(volumenAudio = vol) }
    }


    fun enviarInfoAlOverlay(texto: String) {
        if (texto.isBlank()) return

        viewModelScope.launch {
            // Usamos la lógica de enviarAccionJugada pero con el texto de info
            val textoFinal = "📍 $texto"

            val updates = mapOf(
                "ACCION_JUGADA_MINUTO" to textoFinal,
                "ULTIMA_ACTUALIZACION" to com.google.firebase.database.ServerValue.TIMESTAMP
            )

            // Actualizamos en el nodo del partido
            repository.updatePartidoFields(campeonatoId, partidoId, updates)

            // Actualizamos en PARTIDOACTUAL (Overlay Web)
            actualizarPartidoActualAccion(textoFinal, "")
        }
    }

    fun toggleMarcadorFutbol_Basquet(mostrar: Boolean) {
        // ✅ 1. Actualizamos el estado local PRIMERO para reactividad inmediata
        _uiState.update { it.copy(marcadorFutbolVisible = mostrar) }

        // ✅ 2. Luego actualizamos Firebase según el deporte
        viewModelScope.launch {
            try {
                val deporte = _uiState.value.partido?.DEPORTE ?: "FUTBOL"
                val campoFirebase = if (deporte == "BASQUET") "MARCADOR_BASQUET" else "MARCADOR_FUTBOL"

                val reference = com.google.firebase.database.FirebaseDatabase.getInstance().reference
                    .child("ARKI_DEPORTES")
                    .child("PARTIDOACTUAL")

                reference.child(campoFirebase).setValue(mostrar).await()
                Log.d(TAG, "✅ Marcador web actualizado ($campoFirebase): $mostrar")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error actualizando marcador web: ${e.message}")
            }
        }
    }

}