package com.example.arki_deportes.data.model

import com.google.firebase.database.IgnoreExtraProperties

/**
 * Mapea la tabla FUTBOL_CAMBIOS de VB.NET / Access
 * Representa una sustitución de jugadores durante el partido.
 */
@IgnoreExtraProperties
data class CambioEvento(
    val CODIGOCAMBIO: String = "",
    val CODIGOCAMPEONATO: String = "",
    val CODIGOPARTIDO: String = "",
    val CODIGOEQUIPO: String = "",
    
    // Jugador que entra
    val ENTRA_NOMBRE: String = "",
    val ENTRA_NUMERO: String = "",
    val ENTRA_CODIGOJUGADOR: String = "",
    
    // Jugador que sale
    val SALE_NOMBRE: String = "",
    val SALE_NUMERO: String = "",
    val SALE_CODIGOJUGADOR: String = "",
    val MINUTO_DEL_CAMBIO: String = "",
    val FECHAALTA: String = ""
)
