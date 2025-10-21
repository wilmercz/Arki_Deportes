package com.example.arki_deportes.ui.tiemporeal

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.arki_deportes.data.local.ConfigManager
import com.example.arki_deportes.data.model.PartidoActual
import com.example.arki_deportes.utils.Constants
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class TiempoRealViewModel(application: Application) : AndroidViewModel(application) {

    private val configManager = ConfigManager(application)
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val partidoReference: DatabaseReference = database.reference
        .child(configManager.obtenerNodoRaiz())
        .child(Constants.FirebaseCollections.PARTIDO_ACTUAL)

    private val eventTimeFormatter = SimpleDateFormat("HH:mm:ss", Locale.getDefault())

    private val _uiState = MutableStateFlow(TiempoRealUiState())
    val uiState: StateFlow<TiempoRealUiState> = _uiState.asStateFlow()

    private val listener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val partido = snapshot.getValue(PartidoActual::class.java)
            _uiState.update { current ->
                if (partido == null || !partido.hayPartido()) {
                    current.copy(
                        marcador = MarcadorUi(),
                        estadisticas = EstadisticasUi(),
                        alineaciones = AlineacionesUi(),
                        ultimaActualizacionTexto = null,
                        errorMessage = "Sin datos del partido actual.",
                        isLoading = false
                    )
                } else {
                    current.copy(
                        marcador = mapMarcador(partido, snapshot),
                        estadisticas = mapEstadisticas(snapshot),
                        alineaciones = mapAlineaciones(snapshot),
                        ultimaActualizacionTexto = formatTimestamp(partido.ULTIMA_ACTUALIZACION),
                        errorMessage = null,
                        isLoading = false
                    )
                }
            }
        }

        override fun onCancelled(error: DatabaseError) {
            _uiState.update {
                it.copy(
                    errorMessage = error.message,
                    isLoading = false
                )
            }
        }
    }

    init {
        observePartidoActual()
    }

    private fun observePartidoActual() {
        _uiState.update { it.copy(isLoading = true, errorMessage = null) }
        partidoReference.addValueEventListener(listener)
    }

    private fun mapMarcador(partido: PartidoActual, snapshot: DataSnapshot?): MarcadorUi {
        return MarcadorUi(
            equipoLocal = partido.EQUIPO1,
            equipoVisitante = partido.EQUIPO2,
            golesLocal = partido.GOLES1,
            golesVisitante = partido.GOLES2,
            penalesLocal = snapshot?.getIntChild("PENALES1"),
            penalesVisitante = snapshot?.getIntChild("PENALES2"),
            amarillasLocal = partido.TARJETAS_AMARILLAS1,
            amarillasVisitante = partido.TARJETAS_AMARILLAS2,
            rojasLocal = partido.TARJETAS_ROJAS1,
            rojasVisitante = partido.TARJETAS_ROJAS2,
            cronometro = normalizarTiempo(partido.TIEMPO_TRANSCURRIDO),
            cronometrando = partido.CRONOMETRANDO,
            estado = partido.getEstadoTexto(),
            estadoIcono = partido.getEstadoIcono()
        )
    }

    private fun mapEstadisticas(snapshot: DataSnapshot?): EstadisticasUi {
        if (snapshot == null) return EstadisticasUi()
        return EstadisticasUi(
            posesionLocal = snapshot.getIntChild("POSESION1"),
            posesionVisitante = snapshot.getIntChild("POSESION2"),
            cornersLocal = snapshot.getIntChild("CORNERS1"),
            cornersVisitante = snapshot.getIntChild("CORNERS2"),
            rematesArcoLocal = snapshot.getIntChild("REMATES_ARCO1") ?: snapshot.getIntChild("REMATES1"),
            rematesArcoVisitante = snapshot.getIntChild("REMATES_ARCO2") ?: snapshot.getIntChild("REMATES2"),
            faltasLocal = snapshot.getIntChild("FALTAS1"),
            faltasVisitante = snapshot.getIntChild("FALTAS2"),
            otrosDatos = snapshot.getMapChild("OTRAS_ESTADISTICAS")
        )
    }

    private fun mapAlineaciones(snapshot: DataSnapshot?): AlineacionesUi {
        if (snapshot == null) return AlineacionesUi()
        return AlineacionesUi(
            titularesLocal = snapshot.getStringListChild("TITULARES1"),
            titularesVisitante = snapshot.getStringListChild("TITULARES2"),
            arbitro = snapshot.getStringChild("ARBITRO"),
            estadio = snapshot.getStringChild("ESTADIO")
        )
    }

    private fun normalizarTiempo(valor: String): String {
        val trimmed = valor.trim()
        if (trimmed.isBlank()) return "00:00"
        val segmentos = Regex("\\d+").findAll(trimmed).map { it.value }.toList()
        return when {
            segmentos.size >= 2 -> {
                val minutos = segmentos[0].toIntOrNull() ?: 0
                val segundos = segmentos[1].toIntOrNull() ?: 0
                String.format(Locale.getDefault(), "%02d:%02d", minutos, segundos)
            }
            segmentos.size == 1 -> {
                val minutos = segmentos[0].toIntOrNull() ?: 0
                String.format(Locale.getDefault(), "%02d:00", minutos)
            }
            else -> "00:00"
        }
    }

    private fun formatTimestamp(timestamp: Long): String? {
        if (timestamp <= 0) return null
        val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
        return formatter.format(Date(timestamp))
    }

    fun onQuickAction(action: TiempoRealQuickAction) {
        val state = _uiState.value
        val equipoLocal = state.marcador.equipoLocal.ifBlank { "Local" }
        val equipoVisitante = state.marcador.equipoVisitante.ifBlank { "Visitante" }
        val descripcion = when (action) {
            TiempoRealQuickAction.GOL_LOCAL -> "Gol para $equipoLocal"
            TiempoRealQuickAction.GOL_VISITA -> "Gol para $equipoVisitante"
            TiempoRealQuickAction.PENAL_LOCAL -> "Penal a favor de $equipoLocal"
            TiempoRealQuickAction.PENAL_VISITA -> "Penal a favor de $equipoVisitante"
            TiempoRealQuickAction.AMARILLA_LOCAL -> "Tarjeta amarilla para $equipoLocal"
            TiempoRealQuickAction.AMARILLA_VISITA -> "Tarjeta amarilla para $equipoVisitante"
            TiempoRealQuickAction.ROJA_LOCAL -> "Tarjeta roja para $equipoLocal"
            TiempoRealQuickAction.ROJA_VISITA -> "Tarjeta roja para $equipoVisitante"
        }
        val marcaTiempo = eventTimeFormatter.format(Date())
        _uiState.update {
            val nuevasAcciones = listOf("[$marcaTiempo] $descripcion") + it.recentActions
            it.copy(recentActions = nuevasAcciones.take(6))
        }
    }

    override fun onCleared() {
        super.onCleared()
        partidoReference.removeEventListener(listener)
    }

    private fun DataSnapshot.getIntChild(key: String): Int? {
        val value = child(key).value ?: return null
        return when (value) {
            is Int -> value
            is Long -> value.toInt()
            is Double -> value.toInt()
            is Float -> value.toInt()
            is String -> value.toIntOrNull()
            else -> null
        }
    }

    private fun DataSnapshot.getStringChild(key: String): String? {
        val value = child(key).value ?: return null
        val texto = value.toString().trim()
        return texto.takeIf { it.isNotEmpty() }
    }

    private fun DataSnapshot.getStringListChild(key: String): List<String> {
        val value = child(key).value ?: return emptyList()
        return when (value) {
            is List<*> -> value.filterIsInstance<String>().map { it.trim() }.filter { it.isNotEmpty() }
            is Map<*, *> -> value.values.filterIsInstance<String>().map { it.trim() }.filter { it.isNotEmpty() }
            is String -> value.split("\n", ";", ",").map { it.trim() }.filter { it.isNotEmpty() }
            else -> emptyList()
        }
    }

    private fun DataSnapshot.getMapChild(key: String): Map<String, String> {
        val value = child(key).value ?: return emptyMap()
        return when (value) {
            is Map<*, *> -> value.entries.mapNotNull { (mapKey, mapValue) ->
                val parsedKey = (mapKey as? String)?.trim()?.takeIf { it.isNotEmpty() }
                val parsedValue = mapValue?.toString()?.trim()?.takeIf { it.isNotEmpty() }
                if (parsedKey != null && parsedValue != null) {
                    parsedKey to parsedValue
                } else {
                    null
                }
            }.toMap()
            else -> emptyMap()
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application =
                    this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY] as Application
                TiempoRealViewModel(application)
            }
        }
    }
}

data class TiempoRealUiState(
    val marcador: MarcadorUi = MarcadorUi(),
    val estadisticas: EstadisticasUi = EstadisticasUi(),
    val alineaciones: AlineacionesUi = AlineacionesUi(),
    val ultimaActualizacionTexto: String? = null,
    val errorMessage: String? = null,
    val isLoading: Boolean = true,
    val recentActions: List<String> = emptyList()
)

data class MarcadorUi(
    val equipoLocal: String = "",
    val equipoVisitante: String = "",
    val golesLocal: Int = 0,
    val golesVisitante: Int = 0,
    val penalesLocal: Int? = null,
    val penalesVisitante: Int? = null,
    val amarillasLocal: Int = 0,
    val amarillasVisitante: Int = 0,
    val rojasLocal: Int = 0,
    val rojasVisitante: Int = 0,
    val cronometro: String = "00:00",
    val cronometrando: Boolean = false,
    val estado: String = "",
    val estadoIcono: String = ""
) {
    val mostrarPenales: Boolean get() = penalesLocal != null || penalesVisitante != null
}

data class EstadisticasUi(
    val posesionLocal: Int? = null,
    val posesionVisitante: Int? = null,
    val cornersLocal: Int? = null,
    val cornersVisitante: Int? = null,
    val rematesArcoLocal: Int? = null,
    val rematesArcoVisitante: Int? = null,
    val faltasLocal: Int? = null,
    val faltasVisitante: Int? = null,
    val otrosDatos: Map<String, String> = emptyMap()
) {
    val tieneDatos: Boolean
        get() = listOf(
            posesionLocal,
            posesionVisitante,
            cornersLocal,
            cornersVisitante,
            rematesArcoLocal,
            rematesArcoVisitante,
            faltasLocal,
            faltasVisitante
        ).any { it != null } || otrosDatos.isNotEmpty()
}

data class AlineacionesUi(
    val titularesLocal: List<String> = emptyList(),
    val titularesVisitante: List<String> = emptyList(),
    val arbitro: String? = null,
    val estadio: String? = null
) {
    val tieneDatos: Boolean
        get() = titularesLocal.isNotEmpty() || titularesVisitante.isNotEmpty() ||
            !arbitro.isNullOrEmpty() || !estadio.isNullOrEmpty()
}

enum class TiempoRealQuickAction {
    GOL_LOCAL,
    GOL_VISITA,
    PENAL_LOCAL,
    PENAL_VISITA,
    AMARILLA_LOCAL,
    AMARILLA_VISITA,
    ROJA_LOCAL,
    ROJA_VISITA
}
