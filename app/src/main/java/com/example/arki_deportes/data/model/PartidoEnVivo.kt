package com.example.arki_deportes.data.model

import com.google.firebase.database.IgnoreExtraProperties

/**
 * Modelo ligero para el nodo /PartidosJugandose
 * Se usa para la lista rápida de partidos en vivo.
 */
@IgnoreExtraProperties
data class PartidoEnVivo(
    val CODIGOPARTIDO: String = "",
    val CODIGOCAMPEONATO: String = "",
    val DEPORTE: String = "FUTBOL",
    val EQUIPO1: String = "",
    val EQUIPO2: String = "",
    val GOLES1: Int = 0,
    val GOLES2: Int = 0,
    val TIEMPOSJUGADOS: Int = 0,
    val ESTADO: Int = 0,
    val FECHA_PLAY: String = "",
    val HORA_PLAY: String = ""
)
