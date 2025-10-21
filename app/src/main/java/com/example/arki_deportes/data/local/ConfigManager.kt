// app/src/main/java/com/example/arki_deportes/data/local/ConfigManager.kt

package com.example.arki_deportes.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.arki_deportes.utils.Constants

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * CONFIG MANAGER - GESTOR DE CONFIGURACIÓN LOCAL
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Gestiona la configuración local de la aplicación usando SharedPreferences.
 * Almacena valores como el nodo raíz de Firebase, estado de sesión, etc.
 *
 * Uso:
 * ```kotlin
 * val configManager = ConfigManager(context)
 * val nodoRaiz = configManager.obtenerNodoRaiz()
 * configManager.guardarNodoRaiz("MI_NUEVO_NODO")
 * ```
 *
 * @param context Contexto de la aplicación
 * @author ARKI SISTEMAS
 * @version 1.0.0
 */
class ConfigManager(context: Context) {

    // ═══════════════════════════════════════════════════════════════════════
    // PROPIEDADES PRIVADAS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Instancia de SharedPreferences para almacenar datos localmente
     */
    private val prefs: SharedPreferences = context.getSharedPreferences(
        Constants.SHARED_PREFS_NAME,
        Context.MODE_PRIVATE
    )

    /**
     * Editor para modificar las preferencias
     */
    private val editor: SharedPreferences.Editor
        get() = prefs.edit()

    // ═══════════════════════════════════════════════════════════════════════
    // NODO RAÍZ DE FIREBASE
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Guarda el nombre del nodo raíz de Firebase
     *
     * @param nodoRaiz Nombre del nodo raíz (ej: "ARKI_DEPORTES")
     *
     * Ejemplo:
     * ```kotlin
     * configManager.guardarNodoRaiz("MI_PROYECTO_2025")
     * ```
     */
    fun guardarNodoRaiz(nodoRaiz: String) {
        editor.putString(Constants.PREF_KEY_NODO_RAIZ, nodoRaiz.trim().uppercase())
            .apply()
    }

    /**
     * Obtiene el nombre del nodo raíz guardado
     * Si no existe, devuelve el valor por defecto
     *
     * @return Nombre del nodo raíz (por defecto: "ARKI_DEPORTES")
     *
     * Ejemplo:
     * ```kotlin
     * val nodo = configManager.obtenerNodoRaiz() // "ARKI_DEPORTES"
     * ```
     */
    fun obtenerNodoRaiz(): String {
        return prefs.getString(
            Constants.PREF_KEY_NODO_RAIZ,
            Constants.FIREBASE_NODO_RAIZ_DEFAULT
        ) ?: Constants.FIREBASE_NODO_RAIZ_DEFAULT
    }

    /**
     * Restablece el nodo raíz al valor por defecto
     *
     * Ejemplo:
     * ```kotlin
     * configManager.resetearNodoRaiz()
     * val nodo = configManager.obtenerNodoRaiz() // "ARKI_DEPORTES"
     * ```
     */
    fun resetearNodoRaiz() {
        editor.putString(
            Constants.PREF_KEY_NODO_RAIZ,
            Constants.FIREBASE_NODO_RAIZ_DEFAULT
        ).apply()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // SESIÓN DE USUARIO
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Guarda el estado de la sesión (iniciada o no)
     *
     * @param activa true si la sesión está activa, false si no
     *
     * Ejemplo:
     * ```kotlin
     * // Usuario inició sesión correctamente
     * configManager.guardarEstadoSesion(true)
     * ```
     */
    fun guardarEstadoSesion(activa: Boolean) {
        editor.putBoolean(Constants.PREF_KEY_SESION_ACTIVA, activa)
            .apply()
    }

    /**
     * Verifica si hay una sesión activa
     *
     * @return true si la sesión está activa, false si no
     *
     * Ejemplo:
     * ```kotlin
     * if (configManager.haySesionActiva()) {
     *     // Ir directamente al Home
     * } else {
     *     // Mostrar pantalla de login
     * }
     * ```
     */
    fun haySesionActiva(): Boolean {
        return prefs.getBoolean(Constants.PREF_KEY_SESION_ACTIVA, false)
    }

    /**
     * Cierra la sesión actual
     * Elimina el estado de sesión activa
     *
     * Ejemplo:
     * ```kotlin
     * // Usuario presiona "Cerrar Sesión"
     * configManager.cerrarSesion()
     * // Navegar a LoginScreen
     * ```
     */
    fun cerrarSesion() {
        editor.putBoolean(Constants.PREF_KEY_SESION_ACTIVA, false)
            .apply()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CONFIGURACIONES ADICIONALES
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Guarda la última vez que se sincronizaron los datos
     *
     * @param timestamp Timestamp en milisegundos
     */
    fun guardarUltimaSincronizacion(timestamp: Long) {
        editor.putLong("ultima_sincronizacion", timestamp).apply()
    }

    /**
     * Obtiene el timestamp de la última sincronización
     *
     * @return Timestamp en milisegundos, o 0 si nunca se ha sincronizado
     */
    fun obtenerUltimaSincronizacion(): Long {
        return prefs.getLong("ultima_sincronizacion", 0L)
    }

    /**
     * Guarda si es la primera vez que se abre la app
     *
     * @param esPrimeraVez true si es primera vez, false si no
     */
    fun guardarPrimeraVez(esPrimeraVez: Boolean) {
        editor.putBoolean("primera_vez", esPrimeraVez).apply()
    }

    /**
     * Verifica si es la primera vez que se abre la app
     *
     * @return true si es primera vez, false si no
     */
    fun esPrimeraVez(): Boolean {
        return prefs.getBoolean("primera_vez", true)
    }

    /**
     * Limpia toda la configuración guardada
     * CUIDADO: Esto borra TODOS los datos de SharedPreferences
     *
     * Ejemplo:
     * ```kotlin
     * configManager.limpiarTodo()
     * ```
     */
    fun limpiarTodo() {
        editor.clear().apply()
    }

    /**
     * Limpia solo el caché, mantiene configuraciones importantes
     * (como nodo raíz y sesión)
     */
    fun limpiarCache() {
        editor.remove("ultima_sincronizacion").apply()
    }
}