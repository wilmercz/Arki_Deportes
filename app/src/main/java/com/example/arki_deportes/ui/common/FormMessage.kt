package com.example.arki_deportes.ui.common

/**
 * Representa un mensaje mostrado en la UI de los formularios.
 *
 * @param text Texto a mostrar (éxito o error)
 * @param isError Indica si debe mostrarse como mensaje de error
 */
data class FormMessage(
    val text: String,
    val isError: Boolean = false
)
