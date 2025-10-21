package com.example.arki_deportes.utils

/**
 * Define los deportes soportados por la aplicación junto con la información
 * necesaria para adaptar los formularios y pantallas.
 */
enum class SportType(
    val id: String,
    val displayName: String,
    val teamScoreLabel: String,
    val scoreboardLabel: String,
    val scheduleDurationLabel: String,
    val liveTimeLabel: String,
    val durationUnitSuffix: String,
    val showDisciplinaryStats: Boolean
) {
    FUTBOL(
        id = "FUTBOL",
        displayName = "Fútbol",
        teamScoreLabel = "Goles",
        scoreboardLabel = "Marcador",
        scheduleDurationLabel = "Tiempo de juego (minutos)",
        liveTimeLabel = "Tiempo transcurrido",
        durationUnitSuffix = "min",
        showDisciplinaryStats = true
    ),
    BALONCESTO(
        id = "BALONCESTO",
        displayName = "Baloncesto",
        teamScoreLabel = "Puntos",
        scoreboardLabel = "Puntuación",
        scheduleDurationLabel = "Número de cuartos",
        liveTimeLabel = "Cuarto en juego",
        durationUnitSuffix = "cuartos",
        showDisciplinaryStats = false
    );

    companion object {
        /** Obtiene el [SportType] correspondiente al identificador proporcionado. */
        fun fromId(id: String?): SportType {
            if (id.isNullOrBlank()) return FUTBOL
            return values().firstOrNull { it.id.equals(id, ignoreCase = true) } ?: FUTBOL
        }

        /** Lista de opciones disponibles para mostrarlas en formularios. */
        fun options(): List<SportType> = values().toList()
    }
}

/** Identifica si el valor de cadena coincide con el deporte indicado. */
fun String.matchesSport(sportType: SportType): Boolean = sportType.id.equals(this, ignoreCase = true)
