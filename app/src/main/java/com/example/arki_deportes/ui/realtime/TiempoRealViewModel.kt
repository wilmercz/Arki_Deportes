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
import com.example.arki_deportes.data.context.UsuarioContext
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.*
import com.example.arki_deportes.ui.realtime.components.MotorCronometro
import android.media.MediaPlayer

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * TIEMPO REAL VIEW MODEL
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * ViewModel para control de partido en tiempo real
 * Replica la funcionalidad de VB.NET FrmControl
 */
data class TiempoRealUiState(
    val marcadorFutbolVisible: Boolean = true,
    val nombreCampeonatoReal: String = "",
    val isLoading: Boolean = true,
    val partido: Partido? = null,
    val tiempoActual: String = "00:00",
    val error: String? = null,
    val modoTransmision: Boolean = true,
    val actualizandoFirebase: Boolean = false,
    val penalesActivos: Boolean = false,
    val equipoQueInicia: Int = 1,
    val equipoEnTurno: Int = 1,
    val tandaActual: Int = 1,
    val historiaPenales1: List<Int> = emptyList(),
    val historiaPenales2: List<Int> = emptyList(),
    val banners: List<BannerResource> = emptyList(),
    val selectedBannerIds: Set<String> = emptySet(),
    val audios: List<AudioResource> = emptyList(),
    val volumenAudio: Int = 50,
    val audioEstado: String = "STOP", // "PLAY", "STOP", "PAUSE"
    val cronoPausado: Boolean = false,
    val lowerThirdVisible: Boolean = true,
    val ultimaAccionTexto: String = "",
    val mostrarPortada: Boolean = false,
    val reproduccionLocal: Boolean = true,
    val audioPosicionActual: Long = 0L,
    val audioDuracionTotal: Long = 0L,
    val tablaPosiciones: List<TablaPosicionesItem> = emptyList(),
    val mostrarTablaPosiciones: Boolean = false,
    val mostrarComparativa: Boolean = false,
    val equipoProduccion: EquipoProduccion = EquipoProduccion(),
    val otrosPartidos: List<Partido> = emptyList(),
    val textosPredefinidos: List<String> = emptyList()
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
    private var isInitialSyncDone = false
    private val motorCronometro = MotorCronometro()
    private var musicPlayer: MediaPlayer? = null
    private var progressJob: Job? = null

    init {
        obtenerNombreCampeonato()
        observarPartido()
        observarSoloTercio()
        observarBanners()
        observarAudios()
        observarPortada()
        iniciarActualizadorDeTiempo()
        observarTablaPosicionesDesdeOverlay()
        observarEquipoProduccion()
        observarOtrosPartidos()
        cargarTextosPredefinidos()
    }

    private fun obtenerNombreCampeonato() {
        viewModelScope.launch {
            try {
                val camp = repository.getCampeonato(campeonatoId)
                _uiState.update { it.copy(nombreCampeonatoReal = camp?.CAMPEONATO ?: "Campeonato") }
            } catch (e: Exception) { Log.e(TAG, "Error: ${e.message}") }
        }
    }

    private fun observarPartido() {
        viewModelScope.launch {
            repository.observePartido(campeonatoId, partidoId)
                .catch { error -> _uiState.update { it.copy(error = error.message, isLoading = false) } }
                .collect { partido ->
                    if (partido != null) {
                        val nombreCamp = _uiState.value.nombreCampeonatoReal
                        val partidoConNombre = if (partido.CAMPEONATOTXT.isBlank()) partido.copy(CAMPEONATOTXT = nombreCamp) else partido

                        val fPlay = partido.FECHA_PLAY
                        if (fPlay != null && fPlay != "") {

                            // 🔥 CORRECCIÓN CRÍTICA: FECHA_PLAY puede venir como Long (ms) o String (fecha formateada)
                            val inicioMillis = when (fPlay) {
                                is Long -> fPlay
                                is Number -> fPlay.toLong()
                                is String -> fPlay.toLongOrNull() ?: partido.parseFechaInicioMillis(fPlay) ?: 0L
                                else -> 0L
                            }

                            fun toLongSafe(v: Any?): Long = when (v) { is Long -> v; is Number -> v.toLong(); is String -> v.toLongOrNull() ?: 0L; else -> 0L }
                            val dataMotor = mapOf(
                                "FECHA_PLAY" to inicioMillis, //"FECHA_PLAY" to fPlay,
                                "CRONO_PAUSA_ACUMULADA" to toLongSafe(partido.CRONO_PAUSA_ACUMULADA),
                                "CRONO_OFFSET" to toLongSafe(partido.CRONO_OFFSET),
                                "CRONO_EN_PAUSA" to partido.CRONO_EN_PAUSA,
                                "CRONO_FINALIZADO" to partido.CRONO_FINALIZADO,
                                "CRONO_INICIO_PAUSA" to toLongSafe(partido.CRONO_INICIO_PAUSA)
                            )
                            motorCronometro.cargarDesdeFirebase(dataMotor)
                        }

                        _uiState.update {
                            it.copy(
                                partido = partidoConNombre,
                                isLoading = false,
                                penalesActivos = partido.MARCADOR_PENALES,
                                equipoQueInicia = partido.PENALES_INICIA.coerceIn(1, 2),
                                equipoEnTurno = partido.PENALES_TURNO.coerceIn(1, 2),
                                tandaActual = partido.PENALES_TANDA.coerceAtLeast(1),
                                historiaPenales1 = partido.PENALES_SERIE1,
                                historiaPenales2 = partido.PENALES_SERIE2,
                                error = null
                            )
                        }

                        if (_uiState.value.modoTransmision && !isInitialSyncDone) {
                            isInitialSyncDone = true
                            repository.actualizarTransmision(campeonatoId, partidoId, true)
                            viewModelScope.launch {
                                repository.publicarEnPartidosJugandose(partidoConNombre)
                                sincronizarConOverlay(partidoConNombre)
                                repository.sincronizarTablaAOverlay(campeonatoId, activa = false)
                            }
                        }
                    } else {
                        _uiState.update { it.copy(isLoading = false) }
                    }
                }
        }
    }

    private fun iniciarActualizadorDeTiempo() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                if (_uiState.value.partido?.estaEnCurso() == true) actualizarTiempoDisplay()
            }
        }
    }

    private fun actualizarTiempoDisplay() {
        val partido = _uiState.value.partido ?: return
        val transcurrido = motorCronometro.obtenerTiempoSegundos().toInt()
        val tiempoFormateado = if (partido.DEPORTE.equals("BASQUET", true)) {
            val totalSegundos = (partido.TIEMPOJUEGO.toIntOrNull() ?: 10) * 60
            val restante = (totalSegundos - transcurrido).coerceAtLeast(0)
            String.format("%02d:%02d", restante / 60, restante % 60)
        } else {
            String.format("%02d:%02d", transcurrido / 60, transcurrido % 60)
        }
        _uiState.update { it.copy(tiempoActual = tiempoFormateado) }
    }

    fun iniciarPartido() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            if (partido.estaFinalizado()) return@launch
            _uiState.update { it.copy(actualizandoFirebase = true) }

            val (nuevoNT, tiemposJ) = if (partido.DEPORTE == "BASQUET") {
                when (partido.NumeroDeTiempo) {
                    "0T" -> "1T" to 1; "2T" -> "3T" to 2; "4T" -> "5T" to 3; "6T" -> "7T" to 4
                    "8T" -> "9T" to 5; "10T" -> "11T" to 6; "12T" -> "13T" to 7
                    else -> partido.NumeroDeTiempo to partido.TIEMPOSJUGADOS
                }
            } else {
                if (partido.NumeroDeTiempo == "0T") "1T" to 1 else "3T" to 2
            }

            motorCronometro.iniciar()

            // 🔥 Actualización local inmediata para que el reloj empiece a correr YA en la UI
            val partidoActualizado = partido.copy(
                NumeroDeTiempo = nuevoNT,
                TIEMPOSJUGADOS = tiemposJ,
                ESTADO = 0,
                FECHA_PLAY = motorCronometro.toFirebaseMap()["FECHA_PLAY"]
            )
            _uiState.update { it.copy(partido = partidoActualizado, cronoPausado = false) }
            actualizarTiempoDisplay()


            val finalUpdates = motorCronometro.toFirebaseMap() + mapOf(
                "NumeroDeTiempo" to nuevoNT, "TIEMPOSJUGADOS" to tiemposJ, "ESTADO" to 0,
                "HORA_PLAY" to SimpleDateFormat("HH-mm-ss", Locale.getDefault()).format(Date())
            )

            repository.enviarEstadoCronometro(campeonatoId, partidoId, finalUpdates).onSuccess {
                repository.publicarEnPartidosJugandose(partido.copy(NumeroDeTiempo = nuevoNT, TIEMPOSJUGADOS = tiemposJ, ESTADO = 0))
                _uiState.update { it.copy(cronoPausado = false) }
                if (_uiState.value.modoTransmision) sincronizarConOverlay()
            }
            _uiState.update { it.copy(actualizandoFirebase = false) }
        }
    }

    fun detenerCronometro() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            _uiState.update { it.copy(actualizandoFirebase = true) }

            val esBasquet = partido.DEPORTE.equals("BASQUET", true)
            val nuevoEstado = if (esBasquet) {
                when (partido.NumeroDeTiempo) {
                    "1T" -> "2T"; "3T" -> "4T"; "5T" -> "6T"; "7T" -> "8T"
                    "9T" -> "10T"; "11T" -> "12T"; "13T" -> "14T"
                    else -> partido.NumeroDeTiempo
                }
            } else {
                if (partido.NumeroDeTiempo == "1T") "2T" else "4T"
            }

            val estaFinalizado = if (esBasquet) nuevoEstado == "14T" else nuevoEstado == "4T"
            motorCronometro.finalizar()

            if (_uiState.value.reproduccionLocal) { musicPlayer?.stop(); musicPlayer?.release(); musicPlayer = null }

            val finalUpdates = motorCronometro.toFirebaseMap() + mapOf("NumeroDeTiempo" to nuevoEstado, "ESTADO" to if (estaFinalizado) 1 else 0)

            repository.enviarEstadoCronometro(campeonatoId, partidoId, finalUpdates).onSuccess {
                repository.publicarEnPartidosJugandose(partido.copy(NumeroDeTiempo = nuevoEstado, ESTADO = if (estaFinalizado) 1 else 0))
                _uiState.update { it.copy(cronoPausado = true) }
            }
            _uiState.update { it.copy(actualizandoFirebase = false) }
        }
    }

    fun togglePausa() {
        viewModelScope.launch {
            if (_uiState.value.partido?.estaFinalizado() == true) return@launch
            if (motorCronometro.estaEnPausa()) motorCronometro.reanudar() else motorCronometro.pausar()
            repository.enviarEstadoCronometro(campeonatoId, partidoId, motorCronometro.toFirebaseMap())
            _uiState.update { it.copy(cronoPausado = motorCronometro.estaEnPausa()) }
        }
    }

    fun reiniciarPartido() {
        viewModelScope.launch {
            if (_uiState.value.partido == null) return@launch
            _uiState.update { it.copy(actualizandoFirebase = true) }
            motorCronometro.iniciar()
            val updates: Map<String, Any?> = motorCronometro.toFirebaseMap() + mapOf(
                "Cronometro" to null, "FECHA_PLAY" to 0L, "HORA_PLAY" to null,
                "NumeroDeTiempo" to "0T", "TIEMPOSJUGADOS" to 0, "ESTADO" to 0, "TIEMPOJUEGO" to "00:00"
            )
            repository.enviarEstadoCronometro(campeonatoId, partidoId, updates).onSuccess {
                _uiState.update { it.copy(tiempoActual = "00:00", cronoPausado = false) }
            }
            _uiState.update { it.copy(actualizandoFirebase = false) }
        }
    }

    fun ajustarTiempo(segundos: Int) {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            val ajusteReal = if (partido.DEPORTE.equals("BASQUET", true)) -segundos else segundos
            motorCronometro.agregarOffset(ajusteReal)
            repository.enviarEstadoCronometro(campeonatoId, partidoId, motorCronometro.toFirebaseMap()).onSuccess {
                actualizarTiempoDisplay()
                if (_uiState.value.modoTransmision) sincronizarConOverlay()
            }
        }
    }

    fun agregarGolEquipo1() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            if (!partido.estaEnCurso()) return@launch
            val nuevos = partido.GOLES1 + 1
            enviarAccionJugada("⚽ ${partido.calcularTiempoActualSegundos() / 60}' ¡GOOOOOL! ${partido.EQUIPO1.uppercase()} $nuevos-${partido.GOLES2}")
            repository.updatePartidoFields(campeonatoId, partidoId, mapOf("GOLES1" to nuevos))
        }
    }

    fun restarGolEquipo1() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            repository.updatePartidoFields(campeonatoId, partidoId, mapOf("GOLES1" to (partido.GOLES1 - 1).coerceAtLeast(0)))
            if (_uiState.value.modoTransmision) sincronizarConOverlay()
        }
    }

    fun agregarGolEquipo2() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            if (!partido.estaEnCurso()) return@launch
            val nuevos = partido.GOLES2 + 1
            enviarAccionJugada("⚽ ${partido.calcularTiempoActualSegundos() / 60}' ¡GOOOOOL! ${partido.EQUIPO2.uppercase()} $nuevos-${partido.GOLES1}")
            repository.updatePartidoFields(campeonatoId, partidoId, mapOf("GOLES2" to nuevos))
        }
    }

    fun restarGolEquipo2() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            repository.updatePartidoFields(campeonatoId, partidoId, mapOf("GOLES2" to (partido.GOLES2 - 1).coerceAtLeast(0)))
            if (_uiState.value.modoTransmision) sincronizarConOverlay()
        }
    }

    fun agregarAmarillaEquipo1() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            if (!partido.estaEnCurso()) return@launch
            enviarAccionJugada("🟨 ${partido.calcularTiempoActualSegundos() / 60}' AMARILLA - ${partido.EQUIPO1.uppercase()}")
            repository.updatePartidoFields(campeonatoId, partidoId, mapOf("TAMARILLAS1" to partido.TAMARILLAS1 + 1))
        }
    }

    fun restarAmarillaEquipo1() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            repository.updatePartidoFields(campeonatoId, partidoId, mapOf("TAMARILLAS1" to (partido.TAMARILLAS1 - 1).coerceAtLeast(0)))
            if (_uiState.value.modoTransmision) sincronizarConOverlay()
        }
    }

    fun agregarAmarillaEquipo2() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            if (!partido.estaEnCurso()) return@launch
            enviarAccionJugada("🟨 ${partido.calcularTiempoActualSegundos() / 60}' AMARILLA - ${partido.EQUIPO2.uppercase()}")
            repository.updatePartidoFields(campeonatoId, partidoId, mapOf("TAMARILLAS2" to partido.TAMARILLAS2 + 1))
        }
    }

    fun restarAmarillaEquipo2() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            repository.updatePartidoFields(campeonatoId, partidoId, mapOf("TAMARILLAS2" to (partido.TAMARILLAS2 - 1).coerceAtLeast(0)))
            if (_uiState.value.modoTransmision) sincronizarConOverlay()
        }
    }

    fun agregarRojaEquipo1() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            if (!partido.estaEnCurso()) return@launch
            enviarAccionJugada("🟥 ${partido.calcularTiempoActualSegundos() / 60}' ROJA - ${partido.EQUIPO1.uppercase()}")
            repository.updatePartidoFields(campeonatoId, partidoId, mapOf("TROJAS1" to partido.TROJAS1 + 1))
        }
    }

    fun restarRojaEquipo1() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            repository.updatePartidoFields(campeonatoId, partidoId, mapOf("TROJAS1" to (partido.TROJAS1 - 1).coerceAtLeast(0)))
            if (_uiState.value.modoTransmision) sincronizarConOverlay()
        }
    }

    fun agregarRojaEquipo2() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            if (!partido.estaEnCurso()) return@launch
            enviarAccionJugada("🟥 ${partido.calcularTiempoActualSegundos() / 60}' ROJA - ${partido.EQUIPO2.uppercase()}")
            repository.updatePartidoFields(campeonatoId, partidoId, mapOf("TROJAS2" to partido.TROJAS2 + 1))
        }
    }

    fun restarRojaEquipo2() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            repository.updatePartidoFields(campeonatoId, partidoId, mapOf("TROJAS2" to (partido.TROJAS2 - 1).coerceAtLeast(0)))
            if (_uiState.value.modoTransmision) sincronizarConOverlay()
        }
    }

    fun agregarEsquinaEquipo1() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            if (!partido.estaEnCurso()) return@launch
            enviarAccionJugada("🚩 ESQUINA - ${partido.EQUIPO1.uppercase()}", _uiState.value.audios.find { it.id == "FUTBOL_FX_ESQUINA" }?.url ?: "")
            repository.updatePartidoFields(campeonatoId, partidoId, mapOf("ESQUINAS1" to partido.ESQUINAS1 + 1))
        }
    }

    fun agregarEsquinaEquipo2() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            if (!partido.estaEnCurso()) return@launch
            enviarAccionJugada("🚩 ESQUINA - ${partido.EQUIPO2.uppercase()}", _uiState.value.audios.find { it.id == "FUTBOL_FX_ESQUINA" }?.url ?: "")
            repository.updatePartidoFields(campeonatoId, partidoId, mapOf("ESQUINAS2" to partido.ESQUINAS2 + 1))
        }
    }

    fun restarEsquinaEquipo1() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            repository.updatePartidoFields(campeonatoId, partidoId, mapOf("ESQUINAS1" to (partido.ESQUINAS1 - 1).coerceAtLeast(0)))
            if (_uiState.value.modoTransmision) sincronizarConOverlay()
        }
    }

    fun restarEsquinaEquipo2() {
        viewModelScope.launch {
            val partido = _uiState.value.partido ?: return@launch
            repository.updatePartidoFields(campeonatoId, partidoId, mapOf("ESQUINAS2" to (partido.ESQUINAS2 - 1).coerceAtLeast(0)))
            if (_uiState.value.modoTransmision) sincronizarConOverlay()
        }
    }

    fun toggleModoTransmision() {
        val nuevo = !_uiState.value.modoTransmision
        _uiState.update { it.copy(modoTransmision = nuevo) }
        repository.actualizarTransmision(campeonatoId, partidoId, nuevo)
        if (nuevo) _uiState.value.partido?.let { p -> viewModelScope.launch { repository.publicarEnPartidosJugandose(p); sincronizarConOverlay(p) } }
    }

    private fun sincronizarConOverlay(p: Partido? = null) {
        viewModelScope.launch { repository.sincronizarPartidoActual(p ?: _uiState.value.partido ?: return@launch, _uiState.value.tiempoActual) }
    }

    fun activarPenales(ini: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(actualizandoFirebase = true) }
            val updates = mapOf("MARCADOR_PENALES" to true, "PENALES_INICIA" to ini, "PENALES_TURNO" to ini, "PENALES_TANDA" to 1, "PENALES_SERIE1" to emptyList<Int>(), "PENALES_SERIE2" to emptyList<Int>(), "ULTIMA_ACTUALIZACION" to com.google.firebase.database.ServerValue.TIMESTAMP)
            repository.updatePartidoFields(campeonatoId, partidoId, updates).onSuccess {
                actualizarPanelOverlay("penales")
                if (_uiState.value.modoTransmision) sincronizarConOverlay()
            }
            _uiState.update { it.copy(actualizandoFirebase = false) }
        }
    }

    fun desactivarPenales() {
        viewModelScope.launch {
            _uiState.update { it.copy(actualizandoFirebase = true) }
            repository.updatePartidoFields(campeonatoId, partidoId, mapOf("MARCADOR_PENALES" to false)).onSuccess {
                actualizarPanelOverlay("marcador")
                if (_uiState.value.modoTransmision) sincronizarConOverlay()
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


    fun anotarGolPenal() {
        viewModelScope.launch {
            val p = _uiState.value.partido ?: return@launch
            val t = _uiState.value.equipoEnTurno
            val nt = if (t == 1) 2 else 1
            val up = if (t == 1) mapOf("PENALES1" to p.PENALES1 + 1, "PENALES_SERIE1" to _uiState.value.historiaPenales1 + 1, "PENALES_TURNO" to nt) else mapOf("PENALES2" to p.PENALES2 + 1, "PENALES_SERIE2" to _uiState.value.historiaPenales2 + 1, "PENALES_TURNO" to nt)
            repository.updatePartidoFields(campeonatoId, partidoId, up).onSuccess { if (_uiState.value.modoTransmision) sincronizarConOverlay() }
        }
    }

    fun anotarFalloPenal() {
        viewModelScope.launch {
            val t = _uiState.value.equipoEnTurno
            val nt = if (t == 1) 2 else 1
            val up = if (t == 1) mapOf("PENALES_SERIE1" to _uiState.value.historiaPenales1 + 0, "PENALES_TURNO" to nt) else mapOf("PENALES_SERIE2" to _uiState.value.historiaPenales2 + 0, "PENALES_TURNO" to nt)
            repository.updatePartidoFields(campeonatoId, partidoId, up).onSuccess { if (_uiState.value.modoTransmision) sincronizarConOverlay() }
        }
    }

    fun finalizarYResetearPenales() {
        viewModelScope.launch {
            _uiState.update { it.copy(actualizandoFirebase = true) }
            val up = mapOf("PENALES1" to 0, "PENALES2" to 0, "PENALES_INICIA" to 1, "PENALES_TANDA" to 1, "PENALES_TURNO" to 1, "PENALES_SERIE1" to emptyList<Int>(), "PENALES_SERIE2" to emptyList<Int>(), "MARCADOR_PENALES" to false)
            repository.updatePartidoFields(campeonatoId, partidoId, up).onSuccess {
                actualizarPanelOverlay("marcador")
                actualizarPartidoActualPenales(0, 0, 1, 1, 1, emptyList(), emptyList(), false)
                if (_uiState.value.modoTransmision) sincronizarConOverlay()
            }
            _uiState.update { it.copy(actualizandoFirebase = false) }
        }
    }

    private fun actualizarPanelOverlay(p: String) {
        viewModelScope.launch { try { com.google.firebase.database.FirebaseDatabase.getInstance().reference.child("CONFIGURACION_OVERLAYWEB").child("PANEL_ACTIVO").setValue(p).await() } catch (e: Exception) {} }
    }

    private fun actualizarPartidoActualPenales(p1: Int, p2: Int, ini: Int, tanda: Int, turno: Int, s1: List<Int>, s2: List<Int>, act: Boolean) {
        viewModelScope.launch { try { com.google.firebase.database.FirebaseDatabase.getInstance().reference.child("ARKI_DEPORTES").child("PARTIDOACTUAL").updateChildren(mapOf("PENALES1" to p1, "PENALES2" to p2, "PENALES_INICIA" to ini, "PENALES_TANDA" to tanda, "PENALES_TURNO" to turno, "PENALES_SERIE1" to s1, "PENALES_SERIE2" to s2, "MARCADOR_PENALES" to act)).await() } catch (e: Exception) {} }
    }

    private fun enviarAccionJugada(txt: String, audio: String = "") {
        viewModelScope.launch {
            val p = _uiState.value.partido ?: return@launch
            val esF = p.DEPORTE.equals("FUTBOL", true)
            val esL = _uiState.value.reproduccionLocal
            if (esL && audio.isNotBlank()) playLocalAudio(audio, false)
            val up = mutableMapOf<String, Any>()
            if (!esL && audio.isNotBlank()) up["ACCION_AUDIO_URL"] = audio
            if (esF) up["ACCION_JUGADA_MINUTO"] = txt
            repository.updatePartidoFields(campeonatoId, partidoId, up)
            val tf = if (esF) txt else ""; val v = if (esF) true else _uiState.value.lowerThirdVisible
            _uiState.update { it.copy(ultimaAccionTexto = tf, lowerThirdVisible = v) }
            actualizarPartidoActualAccion(tf, if (esL) "" else audio, v)
            if (_uiState.value.modoTransmision) sincronizarConOverlay()
        }
    }

    fun toggleLowerThird(v: Boolean) {
        viewModelScope.launch { _uiState.update { it.copy(lowerThirdVisible = v) }; actualizarPartidoActualAccion(_uiState.value.ultimaAccionTexto, "", v) }
    }

    private fun actualizarPartidoActualAccion(txt: String, aud: String, v: Boolean) {
        viewModelScope.launch { try { com.google.firebase.database.FirebaseDatabase.getInstance().reference.child("ARKI_DEPORTES").child("PARTIDOACTUAL").updateChildren(mapOf("ACCION_JUGADA_MINUTO" to txt, "ACCION_AUDIO_URL" to aud, "MOSTRAR_TERCIO" to v)).await() } catch (e: Exception) {} }
    }

    fun finalizarPartido(onS: () -> Unit) {
        viewModelScope.launch { try { val id = UsuarioContext.getUsuario()?.usuario ?: return@launch; com.google.firebase.database.FirebaseDatabase.getInstance().reference.child("AppConfig").child("Usuarios").child(id).child("permisos").updateChildren(mapOf("codigoCampeonato" to "NINGUNO", "codigoPartido" to "NINGUNO")).await(); UsuarioContext.limpiarPartidoAsignado(); onS() } catch (e: Exception) {} }
    }

    private fun observarBanners() { viewModelScope.launch { repository.observeBanners().catch {}.collect { b -> _uiState.update { it.copy(banners = b) } } } }
    fun toggleBannerSelection(id: String) { _uiState.update { s -> s.copy(selectedBannerIds = if (s.selectedBannerIds.contains(id)) s.selectedBannerIds - id else s.selectedBannerIds + id) } }
    fun enviarPublicidadUnica(b: BannerResource) { viewModelScope.launch { repository.enviarAnuncioUnico(AnuncioPublicidad(b.tipo.lowercase(), when (b.tipo.uppercase()) { "VIDEO" -> b.urlVideo; "HTML" -> b.codigoHtml; else -> b.urlImagen }, 10)) } }
    fun enviarListaSecuencial() { val ids = _uiState.value.selectedBannerIds; if (ids.isEmpty()) return; viewModelScope.launch { val ads = _uiState.value.banners.filter { it.id in ids }.sortedBy { ids.indexOf(it.id) }.map { b -> AnuncioPublicidad(b.tipo.lowercase(), when (b.tipo.uppercase()) { "VIDEO" -> b.urlVideo; "HTML" -> b.codigoHtml; else -> b.urlImagen }, 10) }; repository.enviarListaAnuncios(ads); _uiState.update { it.copy(selectedBannerIds = emptySet()) } } }
    fun ocultarPublicidad() { viewModelScope.launch { repository.ocultarPublicidad() } }
    private fun observarAudios() { viewModelScope.launch { repository.observeAudios().catch {}.collect { l -> _uiState.update { it.copy(audios = l) } } } }
    fun buscarPosicionAudio(ms: Float) { if (_uiState.value.reproduccionLocal) { musicPlayer?.seekTo(ms.toInt()); _uiState.update { it.copy(audioPosicionActual = ms.toLong()) } } }
    fun reproducirAudio(a: AudioResource) { if (_uiState.value.reproduccionLocal) { playLocalAudio(a.url, a.tipo == "MUSICA"); if (a.tipo == "MUSICA") { _uiState.update { it.copy(audioEstado = "PLAY") }; iniciarSeguimientoProgreso() } } else { viewModelScope.launch { if (a.tipo == "MUSICA") { val ref = com.google.firebase.database.FirebaseDatabase.getInstance().getReference("/ARKI_DEPORTES/CONTROL_AUDIO"); ref.child("URL").setValue(a.url); ref.child("ESTADO").setValue("PLAY"); _uiState.update { it.copy(audioEstado = "PLAY") } } else repository.reproducirAudioEnOverlay(a) } } }
    fun pausarAudio() { if (_uiState.value.reproduccionLocal) musicPlayer?.pause() else com.google.firebase.database.FirebaseDatabase.getInstance().getReference("/ARKI_DEPORTES/CONTROL_AUDIO").child("ESTADO").setValue("PAUSE"); _uiState.update { it.copy(audioEstado = "PAUSE") } }
    fun detenerAudio() { if (_uiState.value.reproduccionLocal) { musicPlayer?.stop(); musicPlayer?.release(); musicPlayer = null; _uiState.update { it.copy(audioPosicionActual = 0, audioDuracionTotal = 0) } } else com.google.firebase.database.FirebaseDatabase.getInstance().getReference("/ARKI_DEPORTES/CONTROL_AUDIO").child("ESTADO").setValue("STOP"); _uiState.update { it.copy(audioEstado = "STOP") } }
    fun cambiarVolumen(v: Int) { val vol = v.coerceIn(0, 100); if (_uiState.value.reproduccionLocal) musicPlayer?.setVolume(vol / 100f, vol / 100f) else com.google.firebase.database.FirebaseDatabase.getInstance().getReference("/ARKI_DEPORTES/CONTROL_AUDIO").child("VOLUMEN").setValue(vol); _uiState.update { it.copy(volumenAudio = vol) } }
    private fun iniciarSeguimientoProgreso() { progressJob?.cancel(); progressJob = viewModelScope.launch { while (true) { musicPlayer?.let { p -> if (p.isPlaying) _uiState.update { it.copy(audioPosicionActual = p.currentPosition.toLong(), audioDuracionTotal = p.duration.toLong()) } }; delay(500) } } }
    fun enviarInfoAlOverlay(txt: String) { if (txt.isBlank()) return; viewModelScope.launch { _uiState.update { it.copy(ultimaAccionTexto = txt, lowerThirdVisible = true) }; actualizarPartidoActualAccion(txt, "", true); if (_uiState.value.modoTransmision) sincronizarConOverlay() } }
    fun toggleMarcadorFutbol_Basquet(m: Boolean) { _uiState.update { it.copy(marcadorFutbolVisible = m) }; viewModelScope.launch { try { com.google.firebase.database.FirebaseDatabase.getInstance().reference.child("ARKI_DEPORTES").child("PARTIDOACTUAL").child(if (_uiState.value.partido?.DEPORTE == "BASQUET") "MARCADOR_BASQUET" else "MARCADOR_FUTBOL").setValue(m).await() } catch (e: Exception) {} } }
    private fun observarSoloTercio() { viewModelScope.launch { val ref = com.google.firebase.database.FirebaseDatabase.getInstance().reference.child("ARKI_DEPORTES").child("PARTIDOACTUAL").child("MOSTRAR_TERCIO"); ref.addValueEventListener(object : com.google.firebase.database.ValueEventListener { override fun onDataChange(s: com.google.firebase.database.DataSnapshot) { _uiState.update { it.copy(lowerThirdVisible = s.getValue(Boolean::class.java) ?: false) } }; override fun onCancelled(e: com.google.firebase.database.DatabaseError) {} }) } }
    fun togglePortada() { viewModelScope.launch { try { com.google.firebase.database.FirebaseDatabase.getInstance().reference.child("ARKI_DEPORTES").child("PARTIDOACTUAL").child("MOSTRAR_PORTADA").setValue(!_uiState.value.mostrarPortada).await() } catch (e: Exception) {} } }
    private fun observarPortada() { viewModelScope.launch { val ref = com.google.firebase.database.FirebaseDatabase.getInstance().reference.child("ARKI_DEPORTES").child("PARTIDOACTUAL").child("MOSTRAR_PORTADA"); ref.addValueEventListener(object : com.google.firebase.database.ValueEventListener { override fun onDataChange(s: com.google.firebase.database.DataSnapshot) { _uiState.update { it.copy(mostrarPortada = s.getValue(Boolean::class.java) ?: false) } }; override fun onCancelled(e: com.google.firebase.database.DatabaseError) {} }) } }
    fun toggleReproduccionLocal(a: Boolean) { _uiState.update { it.copy(reproduccionLocal = a) }; if (!a) { musicPlayer?.stop(); musicPlayer?.release(); musicPlayer = null } }
    private fun playLocalAudio(u: String, m: Boolean) { if (u.isBlank()) return; viewModelScope.launch(Dispatchers.IO) { try { if (m) { musicPlayer?.stop(); musicPlayer?.release(); musicPlayer = MediaPlayer().apply { setDataSource(u); prepare(); start(); isLooping = true; setVolume(_uiState.value.volumenAudio/100f, _uiState.value.volumenAudio/100f) } } else MediaPlayer().apply { setDataSource(u); prepare(); start(); setOnCompletionListener { it.release() } } } catch (e: Exception) {} } }
    private fun observarTablaPosicionesDesdeOverlay() { viewModelScope.launch { repository.observeTablaPosicionesOverlay().collect { l -> _uiState.update { it.copy(tablaPosiciones = l) } } }; viewModelScope.launch { repository.observeTablaPosicionesVisible().collect { v -> _uiState.update { it.copy(mostrarTablaPosiciones = v) } } }; viewModelScope.launch { repository.observeComparativaVisible().collect { v -> _uiState.update { it.copy(mostrarComparativa = v) } } } }
    fun toggleTablaPosiciones() { viewModelScope.launch { repository.toggleVisibilidadTablaOverlay(!_uiState.value.mostrarTablaPosiciones) } }
    fun sincronizarTablaManual() { viewModelScope.launch { repository.sincronizarTablaAOverlay(campeonatoId, _uiState.value.mostrarTablaPosiciones) } }
    fun toggleComparativa() { viewModelScope.launch { repository.toggleComparativaOverlay(!_uiState.value.mostrarComparativa) } }
    private fun observarEquipoProduccion() { viewModelScope.launch { repository.observeEquipoProduccion(campeonatoId).collect { e -> _uiState.update { it.copy(equipoProduccion = e) } } } }
    private fun observarOtrosPartidos() { viewModelScope.launch { repository.observePartidos(campeonatoId).collect { p -> _uiState.update { it.copy(otrosPartidos = p.filter { it.CODIGOPARTIDO != partidoId }) } } } }
    fun enviarResultadoOtroPartido(p: Partido) { enviarInfoAlOverlay("RESULTADO: ${p.EQUIPO1} ${p.GOLES1} - ${p.GOLES2} ${p.EQUIPO2}") }
    fun actualizarCampoProduccion(c: String, v: String) { viewModelScope.launch { repository.actualizarEquipoProduccion(campeonatoId, c, v) } }
    private fun cargarTextosPredefinidos() { viewModelScope.launch { repository.observeTextosPredefinidos(campeonatoId).collect { t -> _uiState.update { it.copy(textosPredefinidos = t) } } } }

    fun generarTextoSocial() {
        val p = _uiState.value.partido ?: return
        val campNombre = _uiState.value.nombreCampeonatoReal
        val texto = """
            EN VIVO : ${p.EQUIPO1} 🆚 ${p.EQUIPO2}
            
            🏆 $campNombre 🏆
            ⚽ ¡PARTIDAZO! ⚽
            
            📅 Fecha: ${p.FECHA_PARTIDO}
            🕒 Hora: ${p.HORA_PARTIDO}
            🏟️ Estadio: ${p.ESTADIO}
            📍 Ubicación: ${p.LUGAR}, ${p.PROVINCIA}
            
            #${campNombre.replace(" ", "")} #${p.EQUIPO1.replace(" ", "")} #${p.EQUIPO2.replace(" ", "")} #ArkiDeportes #FutbolEnVivo
        """.trimIndent()
        
        viewModelScope.launch {
            repository.updatePartidoFields(campeonatoId, partidoId, mapOf("TEXTOFACEBOOK" to texto))
        }
    }


    override fun onCleared() { super.onCleared() ; timerJob?.cancel(); musicPlayer?.release() }
}
