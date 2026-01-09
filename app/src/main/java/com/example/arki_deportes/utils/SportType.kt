// app/src/main/java/com/example/arki_deportes/utils/SportType.kt

package com.example.arki_deportes.utils

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * SPORT TYPE - TIPOS DE DEPORTES SOPORTADOS
 * ═══════════════════════════════════════════════════════════════════════════
 * 
 * Define los deportes soportados por la aplicación junto con la información
 * necesaria para adaptar los formularios, pantallas y rutas de Firebase.
 * 
 * ESTRUCTURA EN FIREBASE:
 * ARKI_DEPORTES/
 *   ├── DatosFutbol/Campeonatos/...
 *   ├── DatosAutomovilismo/Campeonatos/...
 *   ├── DatosCiclismo/Campeonatos/...
 *   └── DatosBasquet/Campeonatos/...
 * 
 * @author ARKI SISTEMAS
 * @version 2.0.0 - Multi-deporte con rutas de Firebase
 */
enum class SportType(
    /**
     * Identificador único del deporte
     * Usado en Firebase y en toda la aplicación
     */
    val id: String,
    
    /**
     * ID numérico para conversiones y comparaciones rápidas
     */
    val numericId: Int,
    
    /**
     * Nombre para mostrar en la UI
     */
    val nombre: String,
    
    /**
     * Nombre en inglés (para compatibilidad)
     */
    val displayName: String,
    
    /**
     * Emoji representativo del deporte
     */
    val emoji: String,
    
    /**
     * Ruta en Firebase (sin ARKI_DEPORTES/)
     * Ejemplo: "DatosFutbol"
     */
    val rutaFirebase: String,
    
    /**
     * Etiqueta para el marcador de equipo
     * Ejemplo: "Goles", "Puntos", "Tiempo"
     */
    val teamScoreLabel: String,
    
    /**
     * Etiqueta para el marcador general
     */
    val scoreboardLabel: String,
    
    /**
     * Etiqueta para duración programada
     */
    val scheduleDurationLabel: String,
    
    /**
     * Etiqueta para tiempo en vivo
     */
    val liveTimeLabel: String,
    
    /**
     * Sufijo para unidad de duración
     */
    val durationUnitSuffix: String,
    
    /**
     * Indica si muestra estadísticas disciplinarias (tarjetas)
     */
    val showDisciplinaryStats: Boolean,
    
    /**
     * Color principal del deporte (para UI)
     */
    val colorPrimario: String
) {
    
    // ═══════════════════════════════════════════════════════════════════════
    // FÚTBOL ⚽
    // ═══════════════════════════════════════════════════════════════════════
    FUTBOL(
        id = "FUTBOL",
        numericId = 1,
        nombre = "Fútbol",
        displayName = "Fútbol",
        emoji = "⚽",
        rutaFirebase = "DatosFutbol",
        teamScoreLabel = "Goles",
        scoreboardLabel = "Marcador",
        scheduleDurationLabel = "Tiempo de juego (minutos)",
        liveTimeLabel = "Tiempo transcurrido",
        durationUnitSuffix = "min",
        showDisciplinaryStats = true,
        colorPrimario = "#4CAF50"  // Verde
    ),
    
    // ═══════════════════════════════════════════════════════════════════════
    // AUTOMOVILISMO 🏁
    // ═══════════════════════════════════════════════════════════════════════
    AUTOMOVILISMO(
        id = "AUTOMOVILISMO",
        numericId = 2,
        nombre = "Automovilismo",
        displayName = "Automovilismo",
        emoji = "🏁",
        rutaFirebase = "DatosAutomovilismo",
        teamScoreLabel = "Tiempo",
        scoreboardLabel = "Clasificación",
        scheduleDurationLabel = "Distancia (km)",
        liveTimeLabel = "Tiempo transcurrido",
        durationUnitSuffix = "km",
        showDisciplinaryStats = false,
        colorPrimario = "#F44336"  // Rojo
    ),
    
    // ═══════════════════════════════════════════════════════════════════════
    // CICLISMO 🚴
    // ═══════════════════════════════════════════════════════════════════════
    CICLISMO(
        id = "CICLISMO",
        numericId = 3,
        nombre = "Ciclismo",
        displayName = "Ciclismo",
        emoji = "🚴",
        rutaFirebase = "DatosCiclismo",
        teamScoreLabel = "Tiempo",
        scoreboardLabel = "Clasificación General",
        scheduleDurationLabel = "Distancia (km)",
        liveTimeLabel = "Etapa en curso",
        durationUnitSuffix = "km",
        showDisciplinaryStats = false,
        colorPrimario = "#FF9800"  // Naranja
    ),
    
    // ═══════════════════════════════════════════════════════════════════════
    // BALONCESTO 🏀
    // ═══════════════════════════════════════════════════════════════════════
    BALONCESTO(
        id = "BALONCESTO",
        numericId = 4,
        nombre = "Baloncesto",
        displayName = "Baloncesto",
        emoji = "🏀",
        rutaFirebase = "DatosBasquet",
        teamScoreLabel = "Puntos",
        scoreboardLabel = "Puntuación",
        scheduleDurationLabel = "Número de cuartos",
        liveTimeLabel = "Cuarto en juego",
        durationUnitSuffix = "cuartos",
        showDisciplinaryStats = false,
        colorPrimario = "#2196F3"  // Azul
    );
    
    // ═══════════════════════════════════════════════════════════════════════
    // COMPANION OBJECT - MÉTODOS ESTÁTICOS
    // ═══════════════════════════════════════════════════════════════════════
    
    companion object {
        
        /**
         * Obtiene el SportType correspondiente al ID de texto
         * @param id Identificador del deporte (ej: "FUTBOL", "AUTOMOVILISMO")
         * @return SportType correspondiente, por defecto FUTBOL
         */
        fun fromId(id: String?): SportType {
            if (id.isNullOrBlank()) return FUTBOL
            return values().firstOrNull { 
                it.id.equals(id, ignoreCase = true) 
            } ?: FUTBOL
        }
        
        /**
         * Obtiene el SportType correspondiente al ID numérico
         * @param numericId ID numérico del deporte (1, 2, 3, 4)
         * @return SportType correspondiente, por defecto FUTBOL
         */
        fun fromId(numericId: Int): SportType {
            return values().firstOrNull { 
                it.numericId == numericId 
            } ?: FUTBOL
        }
        
        /**
         * Obtiene el SportType desde la ruta de Firebase
         * @param rutaFirebase Ruta de Firebase (ej: "DatosFutbol")
         * @return SportType correspondiente, por defecto FUTBOL
         */
        fun fromRutaFirebase(rutaFirebase: String): SportType {
            return values().firstOrNull { 
                it.rutaFirebase.equals(rutaFirebase, ignoreCase = true) 
            } ?: FUTBOL
        }
        
        /**
         * Lista de todos los deportes disponibles
         */
        fun options(): List<SportType> = values().toList()
        
        /**
         * Lista de todos los deportes disponibles
         * (Alias de options() para compatibilidad)
         */
        fun getTodosLosDeportes(): List<SportType> = values().toList()
        
        /**
         * Obtiene solo los deportes implementados actualmente
         * Útil durante desarrollo incremental
         */
        fun getDeportesImplementados(): List<SportType> {
            return listOf(FUTBOL, AUTOMOVILISMO, CICLISMO)
        }
        
        /**
         * Obtiene deportes con cronómetro de tiempo real
         */
        fun getDeportesConCronometro(): List<SportType> {
            return listOf(FUTBOL, BALONCESTO)
        }
        
        /**
         * Obtiene deportes con clasificación por tiempos
         */
        fun getDeportesConTiempos(): List<SportType> {
            return listOf(AUTOMOVILISMO, CICLISMO)
        }
    }
}

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * EXTENSION FUNCTIONS
 * ═══════════════════════════════════════════════════════════════════════════
 */

/**
 * Identifica si el valor de cadena coincide con el deporte indicado
 */
fun String.matchesSport(sportType: SportType): Boolean = 
    sportType.id.equals(this, ignoreCase = true)

/**
 * Convierte un String a SportType
 */
fun String.toSportType(): SportType = SportType.fromId(this)

/**
 * Obtiene el emoji del deporte desde un String
 */
fun String.getSportEmoji(): String {
    return SportType.fromId(this).emoji
}

/**
 * Obtiene el nombre del deporte desde un String
 */
fun String.getSportName(): String {
    return SportType.fromId(this).nombre
}

/**
 * Obtiene la ruta de Firebase desde un String de deporte
 */
fun String.getFirebasePath(): String {
    return SportType.fromId(this).rutaFirebase
}
