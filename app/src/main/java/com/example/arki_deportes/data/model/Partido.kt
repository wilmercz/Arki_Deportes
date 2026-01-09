// app/src/main/java/com/example/arki_deportes/data/model/Partido.kt

package com.example.arki_deportes.data.model

import com.example.arki_deportes.utils.SportType
import com.google.firebase.database.IgnoreExtraProperties
import kotlin.math.max

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * PARTIDO - MODELO DE DATOS EXTENDIDO
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Representa un partido en Firebase Realtime Database.
 * Incluye sistema de cronómetro resiliente basado en timestamps.
 *
 * @author ARKI SISTEMAS
 * @version 2.0.0 - Sistema de cronómetro resiliente
 */
@IgnoreExtraProperties
data class Partido(
    // ═══════════════════════════════════════════════════════════════════════
    // CAMPOS BÁSICOS (Ya existentes)
    // ═══════════════════════════════════════════════════════════════════════
    
    val CODIGOPARTIDO: String = "",
    val EQUIPO1: String = "",
    val EQUIPO2: String = "",
    val CAMPEONATOCODIGO: String = "",
    val CAMPEONATOTXT: String = "",
    val FECHAALTA: String = "",
    val FECHA_PARTIDO: String = "",
    val HORA_PARTIDO: String = "",
    val TEXTOFACEBOOK: String = "",
    val ESTADIO: String = "",
    val PROVINCIA: String = "",
    val TIEMPOJUEGO: String = "90",
    val GOLES1: String = "0",
    val GOLES2: String = "0",
    val ANIO: Int = 0,
    val CODIGOEQUIPO1: String = "",
    val CODIGOEQUIPO2: String = "",
    val TRANSMISION: Boolean = false,
    val ETAPA: Int = 0,
    val LUGAR: String = "",
    val TIMESTAMP_CREACION: Long = 0,
    val TIMESTAMP_MODIFICACION: Long = 0,
    val ORIGEN: String = "MOBILE",
    val DEPORTE: String = SportType.FUTBOL.id,
    
    // ═══════════════════════════════════════════════════════════════════════
    // SISTEMA DE CRONÓMETRO RESILIENTE (NUEVOS CAMPOS)
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Estado del partido
     * Valores:
     * - "NO_INICIADO" = Partido programado, no ha comenzado
     * - "PRIMER_TIEMPO" = Primer tiempo en curso
     * - "DESCANSO" = Entre tiempo (descanso)
     * - "SEGUNDO_TIEMPO" = Segundo tiempo en curso
     * - "FINALIZADO" = Partido terminado
     */
    val estado: String = "NO_INICIADO",
    
    /**
     * Timestamp cuando se inició el PRIMER tiempo (en milisegundos)
     * Se guarda al hacer click en [Iniciar Partido]
     * Ejemplo: 1736950800000 = 15/01/2026 15:00:00
     * 
     * IMPORTANTE: Este valor NO cambia aunque la app se cierre.
     * El tiempo actual se calcula: (now() - timestampInicio) + ajustes
     */
    val timestampInicioPrimerTiempo: Long? = null,
    
    /**
     * Timestamp cuando inició el SEGUNDO tiempo (en milisegundos)
     * Se guarda al hacer click en [Iniciar Segundo Tiempo]
     */
    val timestampInicioSegundoTiempo: Long? = null,
    
    /**
     * Timestamp cuando se pausó el cronómetro (en milisegundos)
     * null = cronómetro NO está pausado
     * Si tiene valor = cronómetro está pausado desde ese momento
     */
    val timestampPausa: Long? = null,
    
    /**
     * Segundos totales acumulados en pausas
     * Si pausas 30 segundos, luego 45, total = 75 segundos
     * Se usa para cálculo correcto del tiempo
     */
    val segundosEnPausa: Int = 0,
    
    /**
     * Ajuste manual del cronómetro en SEGUNDOS
     * Positivo = adelantar tiempo
     * Negativo = atrasar tiempo
     * 
     * CASO DE USO:
     * Corresponsal llega 23 minutos tarde:
     * - Pregunta al árbitro: "Minuto 23"
     * - ajusteManualSegundos = +1380 (23 * 60)
     * 
     * Durante partido nota desfase:
     * - Cronómetro app: 30:15
     * - Árbitro dice: 30:45
     * - Click [+30s] → ajusteManualSegundos += 30
     */
    val ajusteManualSegundos: Int = 0,
    
    /**
     * Usuario asignado al partido (corresponsal)
     * null = sin asignar, disponible para cualquier corresponsal
     * "juan" = asignado al corresponsal Juan
     */
    val usuarioAsignado: String? = null,
    
    /**
     * Timestamp cuando se asignó el usuario (en milisegundos)
     */
    val timestampAsignacion: Long? = null,
    
    /**
     * Indica si el partido permite edición de datos
     * true = se pueden anotar goles, tarjetas, cambios
     * false = solo lectura
     * 
     * Se activa cuando se inicia el cronómetro
     */
    val permiteEdicion: Boolean = false,
    
    /**
     * Timestamp cuando finalizó el partido (en milisegundos)
     */
    val timestampFinalizacion: Long? = null,
    
    /**
     * Minuto en que inició el segundo tiempo (para mostrar)
     * Por defecto 45 (en fútbol)
     */
    val minutoInicioSegundoTiempo: Int = 45

) {
    /**
     * Constructor sin argumentos requerido por Firebase
     */
    constructor() : this(
        CODIGOPARTIDO = "",
        EQUIPO1 = "",
        EQUIPO2 = "",
        estado = "NO_INICIADO"
    )
    
    // ═══════════════════════════════════════════════════════════════════════
    // MÉTODOS DE CÁLCULO DE TIEMPO
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Calcula el tiempo actual del cronómetro en SEGUNDOS
     * 
     * FÓRMULA:
     * tiempo = (ahora - timestampInicio) - pausasAcumuladas + ajusteManual
     * 
     * Este método garantiza que el tiempo sea correcto incluso si:
     * - La app se cierra y vuelve a abrir
     * - El usuario cambia de dispositivo
     * - Hay problemas de red
     * 
     * @return Tiempo en segundos
     */
    fun calcularTiempoActualSegundos(): Int {
        val timestampBase = when (estado) {
            "PRIMER_TIEMPO" -> timestampInicioPrimerTiempo
            "SEGUNDO_TIEMPO" -> timestampInicioSegundoTiempo
            else -> null
        }
        
        if (timestampBase == null) return 0
        
        // Si está en pausa, usar el timestamp de pausa
        // Si no, usar el tiempo actual
        val ahora = if (timestampPausa != null) {
            timestampPausa
        } else {
            System.currentTimeMillis()
        }
        
        // Calcular tiempo transcurrido en milisegundos
        val milisegundosTranscurridos = ahora - timestampBase
        
        // Convertir a segundos
        var segundosTranscurridos = (milisegundosTranscurridos / 1000).toInt()
        
        // Restar pausas acumuladas
        segundosTranscurridos -= segundosEnPausa
        
        // Aplicar ajuste manual
        segundosTranscurridos += ajusteManualSegundos
        
        // El tiempo no puede ser negativo
        return max(0, segundosTranscurridos)
    }
    
    /**
     * Formatea el tiempo como MM:SS
     * Ejemplo: 1425 segundos → "23:45"
     */
    fun getTiempoFormateado(): String {
        val segundosTotales = calcularTiempoActualSegundos()
        val minutos = segundosTotales / 60
        val segundos = segundosTotales % 60
        return String.format("%02d:%02d", minutos, segundos)
    }
    
    /**
     * Obtiene el minuto actual del partido
     * Ejemplo: 1425 segundos → 23 minutos
     */
    fun getMinutoActual(): Int {
        return calcularTiempoActualSegundos() / 60
    }

    
    /**
     * Obtiene el minuto con sufijo de tiempo
     * Ejemplo: "23' 1T" o "68' 2T"
     */
    fun getMinutoFormateado(): String {
        val minuto = getMinutoActual()
        val sufijo = when (estado) {
            "PRIMER_TIEMPO" -> "1T"
            "SEGUNDO_TIEMPO" -> {
                val minutoReal = minuto + minutoInicioSegundoTiempo
                return "$minutoReal' 2T"
            }
            else -> ""
        }
        return "$minuto' $sufijo"
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // VERIFICACIONES DE ESTADO
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Verifica si el partido está en curso (cronómetro corriendo)
     */
    fun estaEnCurso(): Boolean {
        return estado == "PRIMER_TIEMPO" || estado == "SEGUNDO_TIEMPO"
    }
    
    /**
     * Verifica si está pausado
     */
    fun estaPausado(): Boolean {
        return timestampPausa != null
    }
    
    /**
     * Verifica si no ha iniciado
     */
    fun noIniciado(): Boolean {
        return estado == "NO_INICIADO"
    }
    
    /**
     * Verifica si está en descanso
     */
    fun enDescanso(): Boolean {
        return estado == "DESCANSO"
    }
    
    /**
     * Verifica si ya finalizó
     */
    fun finalizado(): Boolean {
        return estado == "FINALIZADO"
    }
    
    /**
     * Verifica si es primer tiempo
     */
    fun esPrimerTiempo(): Boolean {
        return estado == "PRIMER_TIEMPO"
    }
    
    /**
     * Verifica si es segundo tiempo
     */
    fun esSegundoTiempo(): Boolean {
        return estado == "SEGUNDO_TIEMPO"
    }
    
    /**
     * Verifica si el partido está disponible para asignarse
     * (sin usuario asignado o finalizado)
     */
    fun estaDisponible(): Boolean {
        return usuarioAsignado == null && !finalizado()
    }
    
    /**
     * Verifica si el partido está ocupado por otro corresponsal
     */
    fun estaOcupado(nombreUsuarioActual: String?): Boolean {
        if (usuarioAsignado == null) return false
        return usuarioAsignado != nombreUsuarioActual
    }
    
    /**
     * Obtiene el nombre descriptivo del partido
     * Ejemplo: "Deportivo Puyo vs Alianza FC"
     */
    fun getNombrePartido(): String {
        return "$EQUIPO1 vs $EQUIPO2"
    }
    
    /**
     * Obtiene una descripción del estado actual
     */
    fun getDescripcionEstado(): String {
        return when (estado) {
            "NO_INICIADO" -> "Por iniciar"
            "PRIMER_TIEMPO" -> "Primer tiempo - ${getTiempoFormateado()}"
            "DESCANSO" -> "Descanso"
            "SEGUNDO_TIEMPO" -> "Segundo tiempo - ${getTiempoFormateado()}"
            "FINALIZADO" -> "Finalizado"
            else -> "Desconocido"
        }
    }
    
    /**
     * Verifica si el partido es del día de hoy
     */
    fun esDeHoy(): Boolean {
        if (FECHA_PARTIDO.isEmpty()) return false
        
        try {
            val hoy = java.time.LocalDate.now()
            val fechaPartido = java.time.LocalDate.parse(FECHA_PARTIDO)
            return fechaPartido.isEqual(hoy)
        } catch (e: Exception) {
            return false
        }
    }
    
    /**
     * Verifica si el partido está en el pasado (caducado)
     */
    fun estaCaducado(): Boolean {
        if (FECHA_PARTIDO.isEmpty()) return false
        
        try {
            val hoy = java.time.LocalDate.now()
            val fechaPartido = java.time.LocalDate.parse(FECHA_PARTIDO)
            return fechaPartido.isBefore(hoy)
        } catch (e: Exception) {
            return false
        }
    }

    // ─────────────────────────────────────────────────────────────
// Helpers de UI (para HomeScreen)
// ─────────────────────────────────────────────────────────────

    /** Nombre legible del deporte. */
    fun getDeporteTexto(): String = sportType().displayName

    /** Etiqueta del marcador según el deporte. */
    fun getMarcadorLabel(): String = sportType().scoreboardLabel

    /** Marcador para Home. Si no hay marcador cargado, devuelve "vs". */
    fun getMarcador(): String {
        val g1 = GOLES1.trim()
        val g2 = GOLES2.trim()

        // Si no hay datos reales aún (típico partido programado)
        if (g1.isBlank() && g2.isBlank()) return "vs"

        // Si vienen "0" y "0" por defecto, tú decides si ocultas o muestras.
        // Mantengo tu lógica de HomeScreen (oculta si devuelve "vs"):
        if (g1 == "0" && g2 == "0") return "vs"

        return "$g1 - $g2"
    }

    /** Etiqueta para el tiempo/duración configurada del partido (según deporte). */
    fun getTiempoJuegoLabel(): String {
        return when (sportType().id) {
            SportType.FUTBOL.id -> "Duración"
            // Si tienes otros deportes en SportType, puedes afinar aquí:
            // SportType.BASKET.id -> "Períodos"
            // SportType.CICLISMO.id -> "Distancia"
            // SportType.MOTOR.id -> "Vueltas"
            else -> "Tiempo"
        }
    }

    /** Descripción del tiempo/duración configurada (usa TIEMPOJUEGO). */
    fun getTiempoJuegoDescripcion(): String {
        val raw = TIEMPOJUEGO.trim()
        if (raw.isBlank()) return ""

        return when (sportType().id) {
            SportType.FUTBOL.id -> {
                // si es número, lo mostramos como minutos
                raw.toIntOrNull()?.let { "$it min" } ?: raw
            }
            else -> raw
        }
    }

    private fun sportType(): SportType = SportType.fromId(DEPORTE)

}
