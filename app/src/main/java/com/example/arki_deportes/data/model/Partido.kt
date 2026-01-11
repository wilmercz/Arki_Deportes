// app/src/main/java/com/example/arki_deportes/data/model/Partido.kt

package com.example.arki_deportes.data.model

import com.google.firebase.database.IgnoreExtraProperties
import java.text.SimpleDateFormat
import java.util.*

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * PARTIDO - MODELO COMPATIBLE 100% CON VB.NET
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Modelo simplificado que replica EXACTAMENTE la clase Partido de VB.NET
 * Elimina variables temporales que solo existen en memoria de VB.NET
 *
 * @author ARKI SISTEMAS
 * @version 4.0.0 - Modelo limpio VB.NET
 */
@IgnoreExtraProperties
data class Partido(
    // ═══════════════════════════════════════════════════════════════════════
    // IDENTIFICACIÓN
    // ═══════════════════════════════════════════════════════════════════════

    val CODIGOPARTIDO: String = "",
    val CAMPEONATOCODIGO: String = "",

    // ═══════════════════════════════════════════════════════════════════════
    // EQUIPOS
    // ═══════════════════════════════════════════════════════════════════════

    val Equipo1: String = "",
    val Equipo2: String = "",
    val CodigoEquipo1: String = "",
    val CodigoEquipo2: String = "",

    /**
     * Ruta de la imagen del escudo (opcional)
     * VB.NET: Ruta local del archivo de imagen
     * Kotlin: Puede ser URL de Firebase Storage o ruta local
     *
     * Nota: NO es crítico para funcionalidad básica
     */
    val BanderaEquipo1: String? = null,
    val BanderaEquipo2: String? = null,

    // ═══════════════════════════════════════════════════════════════════════
    // FECHA DEL PARTIDO
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Fecha del partido
     * VB.NET: Public Property Fecha() As String
     */
    val Fecha: String? = null,

    // ═══════════════════════════════════════════════════════════════════════
    // CRONÓMETRO (SISTEMA VB.NET)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * DateTime cuando se presionó INICIAR
     * VB.NET: Cronometro = Now
     * Firebase: Se guarda como FECHA_PLAY
     *
     * Formato: "yyyy-MM-dd HH:mm:ss"
     * Ejemplo: "2025-01-10 15:30:00"
     */
    val Cronometro: String? = null,

    /**
     * Copia del Cronometro en Firebase
     * VB.NET: FirebaseManager.EnqueueSet("FECHA_PLAY", Cronometro)
     */
    val FECHA_PLAY: String? = null,

    /**
     * Hora en formato "H-M-S"
     * VB.NET: Hour(Cronometro) & "-" & Minute(Cronometro) & "-" & Second(Cronometro)
     * Ejemplo: "15-30-0"
     */
    val HORA_PLAY: String? = null,

    /**
     * Número del tiempo actual
     * "0T" = No iniciado
     * "1T" = Primer tiempo
     * "2T" = Descanso (entretiempo)
     * "3T" = Segundo tiempo
     * "4T" = Finalizado
     *
     * VB.NET: Public Property NumeroDeTiempo() As String = "0T"
     */
    val NumeroDeTiempo: String = "0T",

    /**
     * Duración de cada tiempo en minutos
     * VB.NET: Public Property TiempodeJuego() As Integer = 45
     *
     * Por defecto 45 minutos para fútbol
     */
    val TiempodeJuego: Int = 45,

    /**
     * Qué tiempo se está jugando
     * 0 = No iniciado
     * 1 = Primer tiempo
     * 2 = Segundo tiempo
     *
     * VB.NET: TIEMPOSJUGADOS en Firebase
     */
    val TIEMPOSJUGADOS: Int = 0,

    /**
     * Estado del partido
     * 0 = En juego
     * 1 = Finalizado
     *
     * VB.NET: ESTADO en Firebase
     */
    val ESTADO: Int = 0,

    /**
     * Tiempo transcurrido (calculado por timer)
     * VB.NET: Se actualiza cada segundo en Firebase
     * Formato: "MM:SS" o "HH:MM:SS"
     * Ejemplo: "23:45", "90:00"
     */
    val TIEMPOJUEGO: String = "00:00",

    // ═══════════════════════════════════════════════════════════════════════
    // MARCADOR - GOLES
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Goles del equipo 1
     * VB.NET: Public Property Goles1() As Integer? = 0
     * Firebase: GOLES1 (como String)
     */
    val GOLES1: String = "0",

    /**
     * Goles del equipo 2
     * VB.NET: Public Property Goles2() As Integer? = 0
     * Firebase: GOLES2 (como String)
     */
    val GOLES2: String = "0",

    // ═══════════════════════════════════════════════════════════════════════
    // ESQUINAS (CORNERS)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Esquinas del equipo 1
     * VB.NET: Public Property Esquinas1() As Integer = 0
     * Firebase: ESQUINAS1 (como String)
     */
    val ESQUINAS1: String = "0",

    /**
     * Esquinas del equipo 2
     * VB.NET: Public Property Esquinas2() As Integer = 0
     * Firebase: ESQUINAS2 (como String)
     */
    val ESQUINAS2: String = "0",

    // ═══════════════════════════════════════════════════════════════════════
    // TARJETAS AMARILLAS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Tarjetas amarillas equipo 1
     * VB.NET: Public Property Amarillas1() As Integer = 0
     * Firebase: TAMARILLAS1 (con "T", como String)
     */
    val TAMARILLAS1: String = "0",

    /**
     * Tarjetas amarillas equipo 2
     * VB.NET: Public Property Amarillas2() As Integer = 0
     * Firebase: TAMARILLAS2 (con "T", como String)
     */
    val TAMARILLAS2: String = "0",

    // ═══════════════════════════════════════════════════════════════════════
    // TARJETAS ROJAS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Tarjetas rojas equipo 1
     * VB.NET: Public Property Rojas1() As Integer = 0
     * Firebase: TROJAS1 (con "T", como String)
     */
    val TROJAS1: String = "0",

    /**
     * Tarjetas rojas equipo 2
     * VB.NET: Public Property Rojas2() As Integer = 0
     * Firebase: TROJAS2 (con "T", como String)
     */
    val TROJAS2: String = "0",

    // ═══════════════════════════════════════════════════════════════════════
    // PENALES (FUTURO)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Penales equipo 1 (si aplica)
     * VB.NET: Public Property Penales1() As Integer = 0
     */
    val Penales1: Int = 0,

    /**
     * Penales equipo 2 (si aplica)
     * VB.NET: Public Property Penales2() As Integer = 0
     */
    val Penales2: Int = 0,

    // ═══════════════════════════════════════════════════════════════════════
    // ETAPA DEL CAMPEONATO
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Etapa del campeonato
     * 0 = Grupos / Fase regular
     * 1 = Cuartos de final
     * 2 = Semifinal
     * 3 = Final
     *
     * VB.NET: Public Property Etapa() As Integer = 0
     */
    val Etapa: Int = 0,

    // ═══════════════════════════════════════════════════════════════════════
    // OTROS DATOS (Desde BD, no en clase VB pero sí en Firebase)
    // ═══════════════════════════════════════════════════════════════════════

    val ESTADIO: String? = null,
    val LUGAR: String? = null,
    val FECHA_PARTIDO: String? = null,
    val HORA_PARTIDO: String? = null,

    // ═══════════════════════════════════════════════════════════════════════
    // CONTROL DE CORRESPONSAL
    // ═══════════════════════════════════════════════════════════════════════

    val usuarioAsignado: String? = null,
    val timestampAsignacion: Long? = null

) {
    /**
     * Constructor sin argumentos (requerido por Firebase)
     */
    constructor() : this(
        CODIGOPARTIDO = "",
        CAMPEONATOCODIGO = "",
        Equipo1 = "",
        Equipo2 = "",
        NumeroDeTiempo = "0T"
    )

    // ═══════════════════════════════════════════════════════════════════════
    // MÉTODOS AUXILIARES
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Obtiene el nombre del partido
     */
    fun getNombrePartido(): String = "$Equipo1 vs $Equipo2"

    /**
     * Verifica si el partido está finalizado
     * VB.NET: NumeroDeTiempo = "4T"
     */
    fun estaFinalizado(): Boolean = NumeroDeTiempo == "4T" || ESTADO == 1

    /**
     * Verifica si el partido está en curso
     * VB.NET: NumeroDeTiempo = "1T" Or "3T"
     */
    fun estaEnCurso(): Boolean = NumeroDeTiempo == "1T" || NumeroDeTiempo == "3T"

    /**
     * Verifica si está en descanso
     * VB.NET: NumeroDeTiempo = "2T"
     */
    fun estaEnDescanso(): Boolean = NumeroDeTiempo == "2T"

    /**
     * Verifica si no ha iniciado
     * VB.NET: NumeroDeTiempo = "0T"
     */
    fun noIniciado(): Boolean = NumeroDeTiempo == "0T"

    /**
     * Obtiene el texto del estado actual
     */
    fun getEstadoTexto(): String {
        return when (NumeroDeTiempo) {
            "0T" -> "No iniciado"
            "1T" -> "Primer Tiempo"
            "2T" -> "Descanso"
            "3T" -> "Segundo Tiempo"
            "4T" -> "Finalizado"
            else -> "Desconocido"
        }
    }

    /**
     * Calcula el tiempo transcurrido en segundos
     * Basado en: (Now - Cronometro)
     */
    fun calcularTiempoActualSegundos(): Int {
        val fechaInicio = FECHA_PLAY ?: Cronometro ?: return parsearTiempoJuego()

        try {
            val formato = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            val fechaDate = formato.parse(fechaInicio) ?: return parsearTiempoJuego()

            val ahora = System.currentTimeMillis()
            val inicio = fechaDate.time
            val diferencia = ahora - inicio

            return (diferencia / 1000).toInt()
        } catch (e: Exception) {
            return parsearTiempoJuego()
        }
    }

    /**
     * Parsea TIEMPOJUEGO cuando viene en formato "MM:SS"
     */
    private fun parsearTiempoJuego(): Int {
        if (TIEMPOJUEGO.contains(":")) {
            val partes = TIEMPOJUEGO.split(":")
            return when (partes.size) {
                2 -> {
                    val minutos = partes[0].toIntOrNull() ?: 0
                    val segundos = partes[1].toIntOrNull() ?: 0
                    (minutos * 60) + segundos
                }
                else -> 0
            }
        }
        return 0
    }

    /**
     * Formatea segundos a "MM:SS"
     */
    fun formatearTiempo(segundos: Int): String {
        val minutos = segundos / 60
        val segs = segundos % 60
        return String.format("%02d:%02d", minutos, segs)
    }

    /**
     * Convierte String a Int para contadores
     */
    fun getGoles1Int(): Int = GOLES1.toIntOrNull() ?: 0
    fun getGoles2Int(): Int = GOLES2.toIntOrNull() ?: 0
    fun getAmarillas1Int(): Int = TAMARILLAS1.toIntOrNull() ?: 0
    fun getAmarillas2Int(): Int = TAMARILLAS2.toIntOrNull() ?: 0
    fun getRojas1Int(): Int = TROJAS1.toIntOrNull() ?: 0
    fun getRojas2Int(): Int = TROJAS2.toIntOrNull() ?: 0
    fun getEsquinas1Int(): Int = ESQUINAS1.toIntOrNull() ?: 0
    fun getEsquinas2Int(): Int = ESQUINAS2.toIntOrNull() ?: 0

    /**
     * Convierte el Partido a Map para guardarlo en Firebase
     * SOLO incluye campos que existen en este modelo
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "CODIGOPARTIDO" to CODIGOPARTIDO,
            "CAMPEONATOCODIGO" to CAMPEONATOCODIGO,
            "Equipo1" to Equipo1,
            "Equipo2" to Equipo2,
            "CodigoEquipo1" to CodigoEquipo1,
            "CodigoEquipo2" to CodigoEquipo2,
            "BanderaEquipo1" to BanderaEquipo1,
            "BanderaEquipo2" to BanderaEquipo2,
            "Fecha" to Fecha,
            "Cronometro" to Cronometro,
            "FECHA_PLAY" to FECHA_PLAY,
            "HORA_PLAY" to HORA_PLAY,
            "NumeroDeTiempo" to NumeroDeTiempo,
            "TiempodeJuego" to TiempodeJuego,
            "TIEMPOSJUGADOS" to TIEMPOSJUGADOS,
            "ESTADO" to ESTADO,
            "TIEMPOJUEGO" to TIEMPOJUEGO,
            "GOLES1" to GOLES1,
            "GOLES2" to GOLES2,
            "ESQUINAS1" to ESQUINAS1,
            "ESQUINAS2" to ESQUINAS2,
            "TAMARILLAS1" to TAMARILLAS1,
            "TAMARILLAS2" to TAMARILLAS2,
            "TROJAS1" to TROJAS1,
            "TROJAS2" to TROJAS2,
            "Penales1" to Penales1,
            "Penales2" to Penales2,
            "Etapa" to Etapa,
            "ESTADIO" to ESTADIO,
            "LUGAR" to LUGAR,
            "FECHA_PARTIDO" to FECHA_PARTIDO,
            "HORA_PARTIDO" to HORA_PARTIDO,
            "usuarioAsignado" to usuarioAsignado,
            "timestampAsignacion" to timestampAsignacion
        )
    }
}

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * FUNCIONES AUXILIARES PARA CREAR MAPAS DE ACTUALIZACIÓN
 * ═══════════════════════════════════════════════════════════════════════════
 */

/**
 * Crea el mapa para iniciar el partido (BOTÓN INICIAR)
 *
 * VB.NET Equivalente:
 * ```
 * .Cronometro = Now
 * FirebaseManager.EnqueueSet("FECHA_PLAY", .Cronometro, 0)
 * FirebaseManager.EnqueueSet("HORA_PLAY", Hour(.Cronometro) & "-" & Minute(.Cronometro) & "-" & Second(.Cronometro), 2)
 * FirebaseManager.EnqueueSet("TIEMPOSJUGADOS", 1, 2)
 * FirebaseManager.EnqueueSet("ESTADO", 0, 2)
 * ```
 */
fun crearMapaIniciarPartido(primerTiempo: Boolean = true): Map<String, Any> {
    val ahora = Date()
    val formato = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val cal = Calendar.getInstance()
    cal.time = ahora

    val cronometroStr = formato.format(ahora)
    val horaPlay = "${cal.get(Calendar.HOUR_OF_DAY)}-${cal.get(Calendar.MINUTE)}-${cal.get(Calendar.SECOND)}"

    return mapOf(
        "Cronometro" to cronometroStr,
        "FECHA_PLAY" to cronometroStr,
        "HORA_PLAY" to horaPlay,
        "NumeroDeTiempo" to if (primerTiempo) "1T" else "3T",
        "TIEMPOSJUGADOS" to if (primerTiempo) 1 else 2,
        "ESTADO" to 0,
        "TIEMPOJUEGO" to "00:00"
    )
}

/**
 * Crea el mapa para pasar a descanso (FIN PRIMER TIEMPO)
 *
 * VB.NET Equivalente:
 * ```
 * DatosPartido.NumeroDeTiempo = "2T"
 * ```
 */
fun crearMapaDescanso(): Map<String, Any> {
    return mapOf(
        "NumeroDeTiempo" to "2T"
    )
}

/**
 * Crea el mapa para finalizar el partido (FIN SEGUNDO TIEMPO)
 *
 * VB.NET Equivalente:
 * ```
 * DatosPartido.NumeroDeTiempo = "4T"
 * ActualizarEstadoPartido(DatosPartido.CodigoPartido, True)
 * ```
 */
fun crearMapaFinalizarPartido(): Map<String, Any> {
    return mapOf(
        "NumeroDeTiempo" to "4T",
        "ESTADO" to 1
    )
}

/**
 * Crea el mapa para actualizar el tiempo de juego
 * VB.NET: Se actualiza cada segundo en el timer
 */
fun crearMapaTiempoJuego(minutos: Int, segundos: Int): Map<String, Any> {
    return mapOf(
        "TIEMPOJUEGO" to String.format("%02d:%02d", minutos, segundos)
    )
}

