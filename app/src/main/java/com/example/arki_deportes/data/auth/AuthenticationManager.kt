// app/src/main/java/com/example/arki_deportes/data/auth/AuthenticationManager.kt

package com.example.arki_deportes.data.auth

import android.util.Log
import com.example.arki_deportes.data.local.ConfigManager
import com.example.arki_deportes.data.model.Usuario
import com.example.arki_deportes.data.model.ResultadoAutenticacion
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * AUTHENTICATION MANAGER
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Gestiona la autenticación de usuarios contra Firebase Realtime Database.
 * Valida credenciales, verifica permisos y actualiza última conexión.
 *
 * @author ARKI SISTEMAS
 * @version 2.0.0
 */
class AuthenticationManager(
    private val database: FirebaseDatabase,
    private val configManager: ConfigManager
) {
    private val TAG = "AuthenticationManager"

    /**
     * Autentica un usuario contra Firebase
     *
     * Ruta Firebase: /AppConfig/Usuarios/[USUARIO]
     *
     * @param usuario Nombre de usuario
     * @param password Contraseña en texto plano
     * @param callback Función que recibe el resultado de la autenticación
     */
    fun login(
        usuario: String,
        password: String,
        callback: (ResultadoAutenticacion) -> Unit
    ) {
        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "🔐 Iniciando login")
        Log.d(TAG, "   Usuario: '$usuario'")
        Log.d(TAG, "   Password length: ${password.length}")
        Log.d(TAG, "═══════════════════════════════════════")

        // Validar campos vacíos
        if (usuario.isBlank() || password.isBlank()) {
            Log.w(TAG, "⚠️ Campos vacíos")
            callback(ResultadoAutenticacion.CredencialesInvalidas)
            return
        }

        // Ruta en Firebase
        val reference = database.reference
            .child("AppConfig")
            .child("Usuarios")
            .child(usuario)

        Log.d(TAG, "📍 Ruta Firebase: AppConfig/Usuarios/$usuario")

        reference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d(TAG, "📥 Respuesta de Firebase recibida")
                Log.d(TAG, "   Existe: ${snapshot.exists()}")

                if (!snapshot.exists()) {
                    Log.w(TAG, "❌ Usuario no encontrado en Firebase")
                    callback(ResultadoAutenticacion.CredencialesInvalidas)
                    return
                }

                try {
                    // Deserializar usuario
                    val usuarioFirebase = snapshot.getValue(Usuario::class.java)

                    if (usuarioFirebase == null) {
                        Log.e(TAG, "❌ Error al parsear datos del usuario")
                        callback(ResultadoAutenticacion.Error("Datos de usuario corruptos"))
                        return
                    }

                    Log.d(TAG, "✅ Usuario parseado correctamente")
                    Log.d(TAG, "   Nombre: ${usuarioFirebase.nombre}")
                    Log.d(TAG, "   Rol: ${usuarioFirebase.rol}")
                    Log.d(TAG, "   Autorizado: ${usuarioFirebase.autorizado}")

                    // Verificar si está autorizado
                    if (!usuarioFirebase.autorizado) {
                        Log.w(TAG, "🚫 Usuario no autorizado")
                        callback(ResultadoAutenticacion.UsuarioNoAutorizado)
                        return
                    }

                    // Verificar contraseña
                    if (usuarioFirebase.password != password) {
                        Log.w(TAG, "❌ Contraseña incorrecta")
                        Log.d(TAG, "   Esperada: ${usuarioFirebase.password}")
                        Log.d(TAG, "   Recibida: $password")
                        callback(ResultadoAutenticacion.CredencialesInvalidas)
                        return
                    }

                    // ✅ Login exitoso
                    Log.d(TAG, "✅ ¡LOGIN EXITOSO!")
                    Log.d(TAG, "   Usuario: ${usuarioFirebase.nombre}")
                    Log.d(TAG, "   Rol: ${usuarioFirebase.rol}")

                    // Actualizar último acceso
                    actualizarUltimoAcceso(usuario)

                    // Retornar éxito
                    callback(ResultadoAutenticacion.Exito(usuarioFirebase))

                } catch (e: Exception) {
                    Log.e(TAG, "❌ Error al procesar login: ${e.message}", e)
                    callback(ResultadoAutenticacion.Error("Error: ${e.message}"))
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "❌ Error de Firebase: ${error.message}")
                callback(ResultadoAutenticacion.Error("Error de conexión: ${error.message}"))
            }
        })
    }

    /**
     * Actualiza el timestamp de último acceso del usuario
     *
     * @param usuario Nombre de usuario
     */
    private fun actualizarUltimoAcceso(usuario: String) {
        val timestamp = System.currentTimeMillis()

        database.reference
            .child("AppConfig")
            .child("Usuarios")
            .child(usuario)
            .child("ultimoAcceso")
            .setValue(timestamp)
            .addOnSuccessListener {
                Log.d(TAG, "✅ Último acceso actualizado: $timestamp")
            }
            .addOnFailureListener { error ->
                Log.e(TAG, "⚠️ Error al actualizar último acceso: ${error.message}")
            }
    }

    /**
     * Cierra la sesión actual (borra datos locales)
     */
    fun logout() {
        Log.d(TAG, "🚪 Cerrando sesión...")
        configManager.cerrarSesion()
    }
}