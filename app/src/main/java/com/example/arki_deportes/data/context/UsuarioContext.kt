// app/src/main/java/com/example/arki_deportes/data/context/UsuarioContext.kt

package com.example.arki_deportes.data.context

import com.example.arki_deportes.data.model.Usuario

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * USUARIO CONTEXT
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Singleton que mantiene el estado global del usuario autenticado.
 * Permite acceso desde cualquier parte de la app.
 *
 * Uso:
 * ```kotlin
 * // Establecer usuario después del login
 * UsuarioContext.setUsuario(usuario)
 *
 * // Obtener usuario actual
 * val usuario = UsuarioContext.getUsuario()
 *
 * // Verificar rol
 * if (UsuarioContext.esAdmin()) {
 *     // Mostrar opciones de admin
 * }
 * ```
 *
 * @author ARKI SISTEMAS
 * @version 1.0.0
 */
object UsuarioContext {
    /**
     * Usuario actualmente autenticado
     */
    private var usuarioActual: Usuario? = null

    /**
     * Establece el usuario actual
     * Llamar después de un login exitoso
     */
    fun setUsuario(usuario: Usuario) {
        usuarioActual = usuario
    }

    /**
     * Obtiene el usuario actual
     * @return Usuario autenticado o null si no hay sesión
     */
    fun getUsuario(): Usuario? = usuarioActual

    /**
     * Verifica si hay un usuario autenticado
     */
    fun hayUsuario(): Boolean = usuarioActual != null

    /**
     * Limpia el usuario actual (logout)
     */
    fun limpiar() {
        usuarioActual = null
    }

    /**
     * Verifica si el usuario actual es ADMIN
     */
    fun esAdmin(): Boolean = usuarioActual?.esAdmin() ?: false

    /**
     * Verifica si el usuario actual es OPERADOR
     */
    fun esOperador(): Boolean = usuarioActual?.esOperador() ?: false

    /**
     * Verifica si el usuario actual es CORRESPONSAL
     */
    fun esCorresponsal(): Boolean = usuarioActual?.esCorresponsal() ?: false

    /**
     * Obtiene el nombre del usuario para mostrar
     */
    fun getNombreUsuario(): String {
        return usuarioActual?.getNombreDisplay() ?: "Usuario"
    }

    /**
     * Obtiene el rol del usuario
     */
    fun getRol(): String {
        return usuarioActual?.rol ?: ""
    }

    /**
     * Limpia el partido asignado del usuario actual en el contexto local
     */
    fun limpiarPartidoAsignado() {
        usuarioActual?.let { usuario ->
            val permisosActualizados = usuario.permisos.copy(
                codigoCampeonato = null,
                codigoPartido = null
            )
            usuarioActual = usuario.copy(permisos = permisosActualizados)

            android.util.Log.d("UsuarioContext", "✅ Partido asignado limpiado del contexto local")
        }
    }


}

