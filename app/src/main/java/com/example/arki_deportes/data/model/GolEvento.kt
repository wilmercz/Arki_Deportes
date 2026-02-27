package com.example.arki_deportes.data.model

import com.google.firebase.database.IgnoreExtraProperties

/**
 * Mapea la tabla GOLES de VB.NET / Access
 */
@IgnoreExtraProperties
data class GolEvento(
    val CODIGO: String = "",
    val CODIGOPARTIDO: String = "",
    val CODIGOEQUIPO: String = "",
    val CODIGOJUGADOR: String = "",
    val JUGADOR: String = "",
    val MINUTO: String = "",
    val LINK_VIDEO: String = "",
    val FECHAALTA: String = ""
)
