// app/src/main/java/com/example/arki_deportes/data/model/ResultadoAutenticacion.kt

package com.example.arki_deportes.data.model

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * RESULTADO AUTENTICACIÓN
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Sealed class que representa los posibles resultados de un intento de login.
 *
 * Uso:
 * ```kotlin
 * authManager.login(usuario, password) { resultado ->
 *     when (resultado) {
 *         is ResultadoAutenticacion.Exito -> {
 *             val usuario = resultado.usuario
 *             // Navegar a home
 *         }
 *         is ResultadoAutenticacion.CredencialesInvalidas -> {
 *             // Mostrar error
 *         }
 *         // ... otros casos
 *     }
 * }
 * ```
 *
 * @author ARKI SISTEMAS
 * @version 1.0.0
 */
sealed class ResultadoAutenticacion {
    /**
     * Login exitoso
     * @param usuario Usuario autenticado
     */
    data class Exito(val usuario: Usuario) : ResultadoAutenticacion()

    /**
     * Usuario o contraseña incorrectos
     */
    object CredencialesInvalidas : ResultadoAutenticacion()

    /**
     * Usuario existe pero no está autorizado (autorizado = false)
     */
    object UsuarioNoAutorizado : ResultadoAutenticacion()

    /**
     * Error de conexión u otro error
     * @param mensaje Descripción del error
     */
    data class Error(val mensaje: String) : ResultadoAutenticacion()
}