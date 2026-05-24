package com.example.arki_deportes.data.model

import com.google.firebase.database.IgnoreExtraProperties

@IgnoreExtraProperties
data class TablaPosicionesItem(
    val EQUIPO_CODIGO: String = "",
    val EQUIPO_NOMBRE: String = "",
    val CAMPEONATO_CODIGO: String = "",
    val PJ: Int = 0,
    val PG: Int = 0,
    val PE: Int = 0,
    val PP: Int = 0,
    val GF: Int = 0,
    val GC: Int = 0,
    val DG: Int = 0,
    val PTS: Int = 0
) {
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "EQUIPO_CODIGO" to EQUIPO_CODIGO,
            "EQUIPO_NOMBRE" to EQUIPO_NOMBRE,
            "CAMPEONATO_CODIGO" to CAMPEONATO_CODIGO,
            "PJ" to PJ,
            "PG" to PG,
            "PE" to PE,
            "PP" to PP,
            "GF" to GF,
            "GC" to GC,
            "DG" to DG,
            "PTS" to PTS
        )
    }
}
