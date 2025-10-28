// app/src/main/java/com/example/arki_deportes/data/auth/AuthenticationManager.kt

package com.example.arki_deportes.data.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.arki_deportes.data.model.ResultadoAutenticacion
import com.example.arki_deportes.data.model.Usuario
import com.example.arki_deportes.utils.Constants
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * AUTHENTICATION MANAGER - GESTOR DE AUTENTICACIÓN
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Gestiona la autenticación de usuarios y la sesión activa.
 * Los usuarios se almacenan en Firebase fuera del nodo de campeonatos.
 *
 * Estructura en Firebase:
 * ```
 * AppConfig/
 *   └─ Usuarios/
 *       ├─ admin/
 *       │   ├─ usuario: "admin"
 *       │   ├─ password: "admin123"
 *       │   ├─ autorizado: true
 *       │   └─ ...
 *       └─ operador1/
 *           └─ ...
 * ```
 *
 * Uso:
 * ```kotlin
 * val authManager = AuthenticationManager(context, database)
 *
 * // Iniciar sesión
 * authManager.login("admin", "admin123") { resultado ->
 *     when (resultado) {
 *         is ResultadoAutenticacion.Exito -> {
 *             val usuario = resultado.usuario
 *             // Navegar a Home
 *         }
 *         is ResultadoAutenticacion.CredencialesInvalidas -> {
 *             // Mostrar error
 *         }
 *         // ... otros casos
 *     }
 * }
 *
 * // Verificar si hay sesión activa
 * if (authManager.haySesionActiva()) {
 *     authManager.obtenerUsuarioActual { usuario ->
 *         // Usuario está logueado
 *     }
 * }
 *
 * // Cerrar sesión
 * authManager.logout()
 * ```
 *
 * @param context Contexto de la aplicación
 * @param database Instancia de Firebase Database
 * @author ARKI SISTEMAS
 * @version 2.0.0
 */
class AuthenticationManager(
    private val context: Context,
    private val database: FirebaseDatabase
) {
    // ═══════════════════════════════════════════════════════════════════════
    // PROPIEDADES PRIVADAS
    // ═══════════════════════════════════════════════════════════════════════

    private val TAG = "AuthenticationManager"

    private val prefs: SharedPreferences = context.getSharedPreferences(
        Constants.SHARED_PREFS_NAME,
        Context.MODE_PRIVATE
    )

    // Claves para SharedPreferences
    private val PREF_USUARIO_ACTUAL = "usuario_actual"
    private val PREF_SESION_ACTIVA = "sesion_activa_v2"
    private val PREF_ULTIMO_ACCESO = "ultimo_acceso"

    // ═══════════════════════════════════════════════════════════════════════
    // AUTENTICACIÓN
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Inicia sesión con usuario y contraseña
     *
     * @param nombreUsuario Nombre de usuario
     * @param password Contraseña
     * @param callback Callback con el resultado de la autenticación
     */
    fun login(
        nombreUsuario: String,
        password: String,
        callback: (ResultadoAutenticacion) -> Unit
    ) {
        Log.d(TAG, "🔐 Intentando login para usuario: $nombreUsuario")

        // Validar campos vacíos
        if (nombreUsuario.isBlank() || password.isBlank()) {
            Log.w(TAG, "⚠️ Usuario o contraseña vacíos")
            callback(ResultadoAutenticacion.CredencialesInvalidas)
            return
        }

        // Buscar usuario en Firebase
        val reference = database.reference
            .child(Constants.FirebaseCollections.APP_CONFIG)
            .child(Constants.FirebaseCollections.USUARIOS)
            .child(nombreUsuario.trim())

        Log.d(TAG, "🔍 Buscando usuario en: /AppConfig/Usuarios/$nombreUsuario")

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Log.w(TAG, "❌ Usuario no encontrado en Firebase")
                    callback(ResultadoAutenticacion.UsuarioNoEncontrado)
                    return
                }

                try {
                    val usuario = snapshot.getValue(Usuario::class.java)

                    if (usuario == null) {
                        Log.e(TAG, "❌ Error al deserializar usuario")
                        callback(ResultadoAutenticacion.Error("Error al leer datos del usuario"))
                        return
                    }

                    Log.d(TAG, "✅ Usuario encontrado: ${usuario.usuario}")
                    Log.d(TAG, "   Autorizado: ${usuario.autorizado}")
                    Log.d(TAG, "   Rol: ${usuario.rol}")

                    // Validar contraseña
                    if (usuario.password != password) {
                        Log.w(TAG, "❌ Contraseña incorrecta")
                        callback(ResultadoAutenticacion.CredencialesInvalidas)
                        return
                    }

                    // Validar que esté autorizado
                    if (!usuario.autorizado) {
                        Log.w(TAG, "⛔ Usuario no autorizado")
                        callback(ResultadoAutenticacion.UsuarioNoAutorizado)
                        return
                    }

                    // Login exitoso
                    Log.d(TAG, "✅ Login exitoso para: ${usuario.usuario}")

                    // Registrar último acceso
                    usuario.registrarAcceso()
                    actualizarUltimoAcceso(usuario)

                    // Guardar sesión
                    guardarSesion(usuario)

                    callback(ResultadoAutenticacion.Exito(usuario))

                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error al procesar usuario", e)
                    callback(ResultadoAutenticacion.Error(e.message ?: "Error desconocido"))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "❌ Error de Firebase", error.toException())
                callback(ResultadoAutenticacion.Error(error.message))
            }
        })
    }

    /**
     * Cierra la sesión actual
     */
    fun logout() {
        Log.d(TAG, "🚪 Cerrando sesión")

        prefs.edit()
            .remove(PREF_USUARIO_ACTUAL)
            .putBoolean(PREF_SESION_ACTIVA, false)
            .apply()

        Log.d(TAG, "✅ Sesión cerrada")
    }

    // ═══════════════════════════════════════════════════════════════════════
    // GESTIÓN DE SESIÓN
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Verifica si hay una sesión activa
     */
    fun haySesionActiva(): Boolean {
        return prefs.getBoolean(PREF_SESION_ACTIVA, false)
    }

    /**
     * Obtiene el usuario actual de la sesión
     * Solo funciona si hay sesión activa
     */
    fun obtenerUsuarioActual(callback: (Usuario?) -> Unit) {
        if (!haySesionActiva()) {
            Log.w(TAG, "⚠️ No hay sesión activa")
            callback(null)
            return
        }

        val nombreUsuario = prefs.getString(PREF_USUARIO_ACTUAL, null)

        if (nombreUsuario == null) {
            Log.w(TAG, "⚠️ No hay usuario guardado")
            callback(null)
            return
        }

        // Obtener usuario actualizado de Firebase
        Log.d(TAG, "🔍 Obteniendo usuario actual: $nombreUsuario")

        val reference = database.reference
            .child(Constants.FirebaseCollections.APP_CONFIG)
            .child(Constants.FirebaseCollections.USUARIOS)
            .child(nombreUsuario)

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (!snapshot.exists()) {
                    Log.w(TAG, "❌ Usuario ya no existe en Firebase")
                    logout() // Cerrar sesión si el usuario fue eliminado
                    callback(null)
                    return
                }

                val usuario = snapshot.getValue(Usuario::class.java)

                if (usuario != null && !usuario.autorizado) {
                    Log.w(TAG, "⛔ Usuario desautorizado")
                    logout() // Cerrar sesión si fue desautorizado
                    callback(null)
                    return
                }

                Log.d(TAG, "✅ Usuario actual obtenido: ${usuario?.usuario}")
                callback(usuario)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "❌ Error al obtener usuario", error.toException())
                callback(null)
            }
        })
    }

    /**
     * Obtiene el nombre del usuario actual (sin consultar Firebase)
     */
    fun obtenerNombreUsuarioActual(): String? {
        return prefs.getString(PREF_USUARIO_ACTUAL, null)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // GESTIÓN DE USUARIOS (ADMIN)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Crea un nuevo usuario en Firebase
     * Solo debe ser usado por administradores
     */
    fun crearUsuario(usuario: Usuario, callback: (Boolean, String) -> Unit) {
        Log.d(TAG, "➕ Creando usuario: ${usuario.usuario}")

        if (usuario.usuario.isBlank()) {
            callback(false, "El nombre de usuario no puede estar vacío")
            return
        }

        val reference = database.reference
            .child(Constants.FirebaseCollections.APP_CONFIG)
            .child(Constants.FirebaseCollections.USUARIOS)
            .child(usuario.usuario.trim())

        // Verificar si ya existe
        reference.get().addOnSuccessListener { snapshot ->
            if (snapshot.exists()) {
                Log.w(TAG, "⚠️ El usuario ya existe")
                callback(false, "El usuario ya existe")
                return@addOnSuccessListener
            }

            // Crear usuario
            reference.setValue(usuario)
                .addOnSuccessListener {
                    Log.d(TAG, "✅ Usuario creado: ${usuario.usuario}")
                    callback(true, "Usuario creado exitosamente")
                }
                .addOnFailureListener { e ->
                    Log.e(TAG, "❌ Error al crear usuario", e)
                    callback(false, e.message ?: "Error desconocido")
                }
        }.addOnFailureListener { e ->
            Log.e(TAG, "❌ Error al verificar usuario", e)
            callback(false, e.message ?: "Error desconocido")
        }
    }

    /**
     * Actualiza un usuario existente
     */
    fun actualizarUsuario(usuario: Usuario, callback: (Boolean, String) -> Unit) {
        Log.d(TAG, "✏️ Actualizando usuario: ${usuario.usuario}")

        val reference = database.reference
            .child(Constants.FirebaseCollections.APP_CONFIG)
            .child(Constants.FirebaseCollections.USUARIOS)
            .child(usuario.usuario)

        reference.setValue(usuario)
            .addOnSuccessListener {
                Log.d(TAG, "✅ Usuario actualizado: ${usuario.usuario}")
                callback(true, "Usuario actualizado exitosamente")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Error al actualizar usuario", e)
                callback(false, e.message ?: "Error desconocido")
            }
    }

    /**
     * Elimina un usuario
     */
    fun eliminarUsuario(nombreUsuario: String, callback: (Boolean, String) -> Unit) {
        Log.d(TAG, "🗑️ Eliminando usuario: $nombreUsuario")

        val reference = database.reference
            .child(Constants.FirebaseCollections.APP_CONFIG)
            .child(Constants.FirebaseCollections.USUARIOS)
            .child(nombreUsuario)

        reference.removeValue()
            .addOnSuccessListener {
                Log.d(TAG, "✅ Usuario eliminado: $nombreUsuario")
                callback(true, "Usuario eliminado exitosamente")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Error al eliminar usuario", e)
                callback(false, e.message ?: "Error desconocido")
            }
    }

    /**
     * Obtiene todos los usuarios
     */
    fun obtenerTodosLosUsuarios(callback: (List<Usuario>) -> Unit) {
        Log.d(TAG, "📋 Obteniendo todos los usuarios")

        val reference = database.reference
            .child(Constants.FirebaseCollections.APP_CONFIG)
            .child(Constants.FirebaseCollections.USUARIOS)

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val usuarios = mutableListOf<Usuario>()

                for (childSnapshot in snapshot.children) {
                    val usuario = childSnapshot.getValue(Usuario::class.java)
                    if (usuario != null) {
                        usuarios.add(usuario)
                    }
                }

                Log.d(TAG, "✅ Usuarios obtenidos: ${usuarios.size}")
                callback(usuarios)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "❌ Error al obtener usuarios", error.toException())
                callback(emptyList())
            }
        })
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MÉTODOS PRIVADOS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Guarda la sesión del usuario en SharedPreferences
     */
    private fun guardarSesion(usuario: Usuario) {
        prefs.edit()
            .putString(PREF_USUARIO_ACTUAL, usuario.usuario)
            .putBoolean(PREF_SESION_ACTIVA, true)
            .putLong(PREF_ULTIMO_ACCESO, usuario.ultimoAcceso)
            .apply()

        Log.d(TAG, "💾 Sesión guardada localmente")
    }

    /**
     * Actualiza el último acceso del usuario en Firebase
     */
    private fun actualizarUltimoAcceso(usuario: Usuario) {
        val reference = database.reference
            .child(Constants.FirebaseCollections.APP_CONFIG)
            .child(Constants.FirebaseCollections.USUARIOS)
            .child(usuario.usuario)
            .child("ultimoAcceso")

        reference.setValue(usuario.ultimoAcceso)
            .addOnSuccessListener {
                Log.d(TAG, "✅ Último acceso actualizado en Firebase")
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "⚠️ No se pudo actualizar último acceso", e)
            }
    }
}