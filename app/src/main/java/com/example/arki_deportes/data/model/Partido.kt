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
    var CAMPEONATOTXT: String = "",

    // ═══════════════════════════════════════════════════════════════════════
    // EQUIPOS
    // ═══════════════════════════════════════════════════════════════════════

    val EQUIPO1: String = "",
    val EQUIPO2: String = "",
    val CODIGOEQUIPO1: String = "",
    val CODIGOEQUIPO2: String = "",

    /**
     * Ruta de la imagen del escudo (opcional)
     * VB.NET: Ruta local del archivo de imagen
     * Kotlin: Puede ser URL de Firebase Storage o ruta local
     *
     * Nota: NO es crítico para funcionalidad básica
     */
    val BANDERAEQUIPO1: String = "",
    val BANDERAEQUIPO2: String = "",

    // ═══════════════════════════════════════════════════════════════════════
    // FECHA DEL PARTIDO
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Fecha del partido
     * VB.NET: Public Property Fecha() As String
     */
    val FECHA: String = "",

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
    val Cronometro: String = "",

    /**
     * Copia del Cronometro en Firebase
     * VB.NET: FirebaseManager.EnqueueSet("FECHA_PLAY", Cronometro)
     */
    val FECHA_PLAY: String = "",

    /**
     * Hora en formato "H-M-S"
     * VB.NET: Hour(Cronometro) & "-" & Minute(Cronometro) & "-" & Second(Cronometro)
     * Ejemplo: "15-30-0"
     */
    val HORA_PLAY: String = "",

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
    val TIEMPOJUEGO: String = "45",

// ═══════════════════════════════════════════════════════════════════════
// MARCADOR - GOLES
// ═══════════════════════════════════════════════════════════════════════

    /**
     * Goles del equipo 1
     * Firebase: GOLES1 (como Int)
     */
    val GOLES1: Int = 0,

    /**
     * Goles del equipo 2
     * Firebase: GOLES2 (como Int)
     */
    val GOLES2: Int = 0,

// ═══════════════════════════════════════════════════════════════════════
// ESQUINAS (CORNERS)
// ═══════════════════════════════════════════════════════════════════════

    /**
     * Esquinas del equipo 1
     * Firebase: ESQUINAS1 (como Int)
     */
    val ESQUINAS1: Int = 0,

    /**
     * Esquinas del equipo 2
     * Firebase: ESQUINAS2 (como Int)
     */
    val ESQUINAS2: Int = 0,

// ═══════════════════════════════════════════════════════════════════════
// TARJETAS AMARILLAS
// ═══════════════════════════════════════════════════════════════════════

    /**
     * Tarjetas amarillas equipo 1
     * Firebase: TAMARILLAS1 (como Int)
     */
    val TAMARILLAS1: Int = 0,

    /**
     * Tarjetas amarillas equipo 2
     * Firebase: TAMARILLAS2 (como Int)
     */
    val TAMARILLAS2: Int = 0,

// ═══════════════════════════════════════════════════════════════════════
// TARJETAS ROJAS
// ═══════════════════════════════════════════════════════════════════════

    /**
     * Tarjetas rojas equipo 1
     * Firebase: TROJAS1 (como Int)
     */
    val TROJAS1: Int = 0,

    /**
     * Tarjetas rojas equipo 2
     * Firebase: TROJAS2 (como Int)
     */
    val TROJAS2: Int = 0,

    // ═══════════════════════════════════════════════════════════════════════
    // PENALES (FUTURO)
    // ═══════════════════════════════════════════════════════════════════════
    /**
     * Indica si el modo penales está ACTIVO
     * ✅ Crítico: El overlay web lee este campo para cambiar de panel
     * true = Mostrar panel de penales
     * false = Mostrar marcador normal
     *
     * Firebase: MARCADOR_PENALES
     */
    val MARCADOR_PENALES: Boolean = false,

    /**
     * Contador de penales convertidos (equipo 1)
     * Solo se incrementa con GOLES, no con fallos
     * NO se resetea en nueva tanda (muerte súbita)
     *
     * Firebase: Penales1
     */
    val PENALES1: Int = 0,

    /**
     * Contador de penales convertidos (equipo 2)
     * Solo se incrementa con GOLES, no con fallos
     * NO se resetea en nueva tanda (muerte súbita)
     *
     * Firebase: Penales2
     */
    val PENALES2: Int = 0,

    /**
     * ¿Qué equipo INICIÓ la tanda de penales?
     * 1 = Equipo 1 inicia
     * 2 = Equipo 2 inicia
     *
     * ✅ Permanente: No cambia durante la tanda
     * ✅ Se usa para resetear turno en nueva tanda
     * ✅ Permite reconstruir estado si app se cierra
     *
     * Firebase: PENALES_INICIA
     */
    val PENALES_INICIA: Int = 1,

    /**
     * ¿Qué equipo cobra AHORA? (turno actual)
     * 1 = Turno del equipo 1
     * 2 = Turno del equipo 2
     *
     * ✅ Variable: Alterna automáticamente después de cada tiro
     * ✅ Puede corregirse manualmente si el operador se equivoca
     *
     * Firebase: PENALES_TURNO
     */
    val PENALES_TURNO: Int = 1,

    /**
     * Número de tanda actual
     * 1 = Primera tanda (5 tiros cada uno)
     * 2, 3, 4... = Muerte súbita
     *
     * ✅ Se incrementa cada vez que hay empate y se inicia nueva tanda
     *
     * Firebase: PENALES_TANDA
     */
    val PENALES_TANDA: Int = 1,

    /**
     * Historial de tiros del equipo 1 (TANDA ACTUAL)
     * Lista de enteros: 1=gol, 0=fallo
     *
     * ✅ Más eficiente que String ("GOL"/"FALLO")
     * ✅ Sin problemas de mayúsculas/minúsculas
     * ✅ Se resetea en nueva tanda (muerte súbita)
     *
     * Ejemplo: [1, 0, 1, 1, 0] = GOL, FALLO, GOL, GOL, FALLO
     *
     * Firebase: PENALES_SERIE1
     */
    val PENALES_SERIE1: List<Int> = emptyList(),

    /**
     * Historial de tiros del equipo 2 (TANDA ACTUAL)
     * Lista de enteros: 1=gol, 0=fallo
     *
     * Firebase: PENALES_SERIE2
     */
    val PENALES_SERIE2: List<Int> = emptyList(),

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
    val ETAPA: Int = 0,

    // ═══════════════════════════════════════════════════════════════════════
    // OTROS DATOS (Desde BD, no en clase VB pero sí en Firebase)
    // ═══════════════════════════════════════════════════════════════════════

    val ESTADIO: String = "",
    val LUGAR: String = "",
    val FECHA_PARTIDO: String = "",
    val HORA_PARTIDO: String = "",

    // ═══════════════════════════════════════════════════════════════════════
    // CONTROL DE CORRESPONSAL
    // ═══════════════════════════════════════════════════════════════════════

    val usuarioAsignado: String = "",
    val timestampAsignacion: Long? = null,

    // ==============================
// Fechas / auditoría
// ==============================
    var FECHAALTA: String = "",
    var TIMESTAMP_CREACION: String = "",
    var TIMESTAMP_MODIFICACION: String = "",

// ==============================
// Redes / publicación
// ==============================
    var TEXTOFACEBOOK: String = "",
    var LINK: String = "",

// ==============================
// Ubicación
// ==============================
    var PROVINCIA: String = "",
    var CANTON: String = "",
    var PARROQUIA: String = "",

// ==============================
// Arbitraje
// ==============================
    var ARBITRO1: String = "",

// ==============================
// Temporada / transmisión
// ==============================
    var ANIO: Int = 0,
    var TRANSMISION: Boolean = false,

// ==============================
// Fase / numeración
// ==============================
    var FASE: String = "",
    var NUMERO_PARTIDO: Int = 0,

// ==============================
// Ganador
// ==============================
    var NOMBREGANADOR: String = "",
    var CODIGOGANADOR: String = "",

// ==============================
// Serie / grupo
// ==============================
    var SERIECODIGO: String = "",
    var SERIENOMBRE: String = "",
    var GRUPOCODIGO: String = "",
    var GRUPONOMBRE: String = "",

// ==============================
// Sincronización / control
// ==============================
    var SINCRONIZADO: Boolean = false,
    var HASH_REGISTRO: String = "",
    var LLAVE: String = "",
    var ORIGEN: String = "",
    var ORIGEN_DESCRIPCION: String = ""

) {
    /**
     * Constructor sin argumentos (requerido por Firebase)
     */
    constructor() : this(
        CODIGOPARTIDO = "",
        CAMPEONATOCODIGO = "",
        EQUIPO1 = "",
        EQUIPO2 = "",
        NumeroDeTiempo = "0T"
    )

    // ═══════════════════════════════════════════════════════════════════════
    // MÉTODOS AUXILIARES
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Obtiene el nombre del partido
     */
    fun getNombrePartido(): String = "$EQUIPO1 vs $EQUIPO2"

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
        // ✅ Elegir FECHA_PLAY solo si tiene valor real; si no, usar Cronometro
        val fechaInicioStr = when {
            FECHA_PLAY.isNotBlank() -> FECHA_PLAY.trim()
            Cronometro.isNotBlank() -> Cronometro.trim()
            else -> return 0
        }

        val inicioMillis = parseFechaInicioMillis(fechaInicioStr) ?: return 0

        val diff = System.currentTimeMillis() - inicioMillis
        if (diff <= 0) return 0

        return (diff / 1000L).toInt()
    }

    private fun parseFechaInicioMillis(texto: String): Long? {
        val t = texto.trim()
        if (t.isBlank()) return null

        // Si viniera con Z (UTC), lo soportamos también
        val patrones = listOf(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSS",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss"
        )

        for (pat in patrones) {
            try {
                val sdf = java.text.SimpleDateFormat(pat, java.util.Locale.US).apply {
                    isLenient = false
                    timeZone = if (t.endsWith("Z")) java.util.TimeZone.getTimeZone("UTC")
                    else java.util.TimeZone.getDefault()
                }
                val d = sdf.parse(t) ?: continue
                return d.time
            } catch (_: Exception) {
                // seguir probando otros formatos
            }
        }
        return null
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
     * Obtiene el NumeroDeTiempo efectivo
     *
     * ✅ LÓGICA VB.NET:
     * - Si ESTADO = 0 (en curso):
     *   - TIEMPOSJUGADOS = 1 → "1T"
     *   - TIEMPOSJUGADOS = 2 → "3T"
     *   - Otro valor → "0T"
     * - Si ESTADO = 1 (finalizado) → "4T"
     * - Si NumeroDeTiempo ya tiene valor → usar ese valor
     */
    fun getNumeroDeTiempoEfectivo(): String {
        // 1. Si NumeroDeTiempo ya tiene un valor válido, usarlo
        if (NumeroDeTiempo.isNotBlank() && NumeroDeTiempo != "0T") {
            return NumeroDeTiempo
        }

        // 2. Calcular desde ESTADO y TIEMPOSJUGADOS (compatible VB.NET)
        val resultado = when {
            // Si está finalizado
            ESTADO == 1 -> "4T"

            // Si está en curso (ESTADO = 0)
            ESTADO == 0 -> {
                when (TIEMPOSJUGADOS) {
                    1 -> "1T"  // Primer tiempo
                    2 -> "3T"  // Segundo tiempo
                    else -> "0T"  // No iniciado
                }
            }

            // Por defecto
            else -> NumeroDeTiempo.ifBlank { "0T" }
        }

        return resultado
    }


    /**
     * Convierte campos a Int (ahora son directos)
     */
    fun getGoles1Int(): Int = GOLES1
    fun getGoles2Int(): Int = GOLES2
    fun getAmarillas1Int(): Int = TAMARILLAS1
    fun getAmarillas2Int(): Int = TAMARILLAS2
    fun getRojas1Int(): Int = TROJAS1
    fun getRojas2Int(): Int = TROJAS2
    fun getEsquinas1Int(): Int = ESQUINAS1
    fun getEsquinas2Int(): Int = ESQUINAS2

    /**
     * Valida que los campos de penales sean consistentes
     */
    fun validarPenales(): Boolean {
        // PENALES_INICIA y PENALES_TURNO deben ser 1 o 2
        if (PENALES_INICIA !in 1..2 || PENALES_TURNO !in 1..2) return false

        // PENALES_TANDA debe ser >= 1
        if (PENALES_TANDA < 1) return false

        // Las series solo pueden contener 0 o 1
        if (PENALES_SERIE1.any { it !in 0..1 }) return false
        if (PENALES_SERIE2.any { it !in 0..1 }) return false

        return true
    }

    /**
     * Cuenta goles en la serie actual
     */
    fun contarGolesSerieEquipo1(): Int = PENALES_SERIE1.count { it == 1 }
    fun contarGolesSerieEquipo2(): Int = PENALES_SERIE2.count { it == 1 }

    /**
     * Convierte el Partido a Map para guardarlo en Firebase
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "CODIGOPARTIDO" to CODIGOPARTIDO,
            "CAMPEONATOCODIGO" to CAMPEONATOCODIGO,
            "EQUIPO1" to EQUIPO1,
            "EQUIPO2" to EQUIPO2,
            "CODIGOEQUIPO1" to CODIGOEQUIPO1,
            "CODIGOEQUIPO2" to CODIGOEQUIPO2,
            "BANDERAEQUIPO1" to BANDERAEQUIPO1,
            "BANDERAEQUIPO2" to BANDERAEQUIPO2,
            "FECHA" to FECHA,
            "Cronometro" to Cronometro,
            "FECHA_PLAY" to FECHA_PLAY,
            "HORA_PLAY" to HORA_PLAY,
            "NumeroDeTiempo" to NumeroDeTiempo,
            "TIEMPOSJUGADOS" to TIEMPOSJUGADOS,
            "ESTADO" to ESTADO,
            "TIEMPOJUEGO" to TIEMPOJUEGO,
            "GOLES1" to GOLES1,           // ← Ahora Int directo
            "GOLES2" to GOLES2,           // ← Ahora Int directo
            "ESQUINAS1" to ESQUINAS1,     // ← Ahora Int directo
            "ESQUINAS2" to ESQUINAS2,     // ← Ahora Int directo
            "TAMARILLAS1" to TAMARILLAS1, // ← Ahora Int directo
            "TAMARILLAS2" to TAMARILLAS2, // ← Ahora Int directo
            "TROJAS1" to TROJAS1,         // ← Ahora Int directo
            "TROJAS2" to TROJAS2,         // ← Ahora Int directo
            "MARCADOR_PENALES" to MARCADOR_PENALES,
            "PENALES1" to PENALES1,
            "PENALES2" to PENALES2,
            "PENALES_INICIA" to PENALES_INICIA,
            "PENALES_TURNO" to PENALES_TURNO,
            "PENALES_TANDA" to PENALES_TANDA,
            "PENALES_SERIE1" to PENALES_SERIE1,
            "PENALES_SERIE2" to PENALES_SERIE2,
            "Etapa" to ETAPA,
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
fun crearMapaIniciarPartido(primerTiempo: Boolean = true, duracionTiempo: Int = 45): Map<String, Any> {
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

    return mapOf(
        // Cronómetro - Momento de inicio
        "Cronometro" to cronometroStr,
        "FECHA_PLAY" to cronometroStr,
        "HORA_PLAY" to horaPlay,

        // Estado del tiempo
        "NumeroDeTiempo" to if (primerTiempo) "1T" else "3T",
        "TIEMPOSJUGADOS" to if (primerTiempo) 1 else 2,

        // Estado del partido
        "ESTADO" to 0,

        // Duración configurada (NO tiempo transcurrido)
        "TiempodeJuego" to duracionTiempo
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
 * Crea el mapa para finalizar con ganador
 */
fun crearMapaFinalizarConGanador(nombreGanador: String, codigoGanador: String): Map<String, Any> {
    return mapOf(
        "NumeroDeTiempo" to "4T",
        "ESTADO" to 1,
        "NOMBREGANADOR" to nombreGanador,
        "CODIGOGANADOR" to codigoGanador
    )
}



