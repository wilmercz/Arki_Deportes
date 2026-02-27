package com.example.arki_deportes.data.model

import com.google.firebase.database.IgnoreExtraProperties

/**
 * Mapea la tabla JUGADORES de VB.NET / Access
 */
@IgnoreExtraProperties
data class Jugador(
    val CODIGO: String = "",
    val CODIGOEQUIPO: String = "",
    val NUMERO: String = "",
    val JUGADOR: String = "",
    val POSICION: String = "",
    val TITULAR: String = "TITULAR", // TITULAR o SUPLENTE
    val FECHAALTA: String = "",
    val FOTO: String = ""
)
