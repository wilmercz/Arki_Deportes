// app/src/main/java/com/example/arki_deportes/data/context/UsuarioContext.kt

package com.example.arki_deportes.data.context

import android.content.Context
import android.content.SharedPreferences
import com.example.arki_deportes.data.model.Usuario
import com.example.arki_deportes.data.model.RolUsuario
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * USUARIO CONTEXT - GESTIÓN DEL USUARIO ACTUAL
 * ═══════════════════════════════════════════════════════════════════════════
 * 
 * Singleton que gestiona el usuario autenticado y sus permisos.
 * Persiste información básica en SharedPreferences para recuperación rápida.
 * 
 * RESPONSABILIDADES:
 * - Almacenar usuario actual
 * - Verificar permisos
 * - Determinar tipo de usuario (Admin, Corresponsal, etc.)
 * - Persistir datos básicos entre sesiones
 * 
 * @author ARKI SISTEMAS
 * @version 2.0.0
 */
object UsuarioContext {

    // ═══════════════════════════════════════════════════════════════════════
    // STATE FLOWS
    // ═══════════════════════════════════════════════════════════════════════
    
    private val _usuarioActual = MutableStateFlow<Usuario?>(null)
    val usuarioActual: StateFlow<Usuario?> = _usuarioActual.asStateFlow()
    
    // ═══════════════════════════════════════════════════════════════════════
    // SHARED PREFERENCES
    // ═══════════════════════════════════════════════════════════════════════
    
    private var sharedPreferences: SharedPreferences? = null
    
    private const val PREFS_NAME = "arki_usuario_prefs"
    private const val KEY_USUARIO = "usuario"
    private const val KEY_NOMBRE = "nombre"
    private const val KEY_ROL = "rol"
    private const val KEY_PARTIDO_ASIGNADO = "partido_asignado"
    private const val KEY_CAMPEONATO_ASIGNADO = "campeonato_asignado"
    
    /**
     * Inicializa el contexto con SharedPreferences
     * DEBE llamarse en Application.onCreate() o MainActivity.onCreate()
     */
    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        cargarUsuarioPersistido()
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // GESTIÓN DE USUARIO
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Establece el usuario actual y persiste información básica
     */
    fun setUsuario(usuario: Usuario) {
        _usuarioActual.value = usuario
        
        // Persistir datos básicos
        sharedPreferences?.edit()?.apply {
            putString(KEY_USUARIO, usuario.usuario)
            putString(KEY_NOMBRE, usuario.nombre)
            putString(KEY_ROL, usuario.rol)
            putString(KEY_PARTIDO_ASIGNADO, usuario.permisos.codigoPartido)
            putString(KEY_CAMPEONATO_ASIGNADO, usuario.permisos.codigoCampeonato)
            apply()
        }
    }
    
    /**
     * Obtiene el usuario actual
     */
    fun getUsuario(): Usuario? {
        return _usuarioActual.value
    }
    
    /**
     * Obtiene el nombre de usuario (ID)
     */
    fun getNombreUsuario(): String? {
        return _usuarioActual.value?.usuario
    }
    
    /**
     * Carga el usuario persistido (solo datos básicos)
     * El usuario completo se carga desde Firebase en el login
     */
    private fun cargarUsuarioPersistido() {
        val usuario = sharedPreferences?.getString(KEY_USUARIO, null)
        val nombre = sharedPreferences?.getString(KEY_NOMBRE, null)
        val rol = sharedPreferences?.getString(KEY_ROL, null)
        
        // Solo cargamos si hay datos persistidos
        // El usuario completo se recarga en el login desde Firebase
        if (usuario != null && nombre != null && rol != null) {
            // Crear usuario básico temporal
            // Se recargará completo desde Firebase
        }
    }
    
    /**
     * Limpia el usuario actual (logout)
     */
    fun limpiarUsuario() {
        _usuarioActual.value = null
        sharedPreferences?.edit()?.clear()?.apply()
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // VERIFICACIONES DE PERMISOS
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Verifica si el usuario actual es administrador
     */
    fun esAdministrador(): Boolean {
        return _usuarioActual.value?.rol == RolUsuario.ADMINISTRADOR
    }
    
    /**
     * Verifica si el usuario es un corresponsal con partido asignado
     */
    fun esCorresponsal(): Boolean {
        val usuario = _usuarioActual.value ?: return false
        return !usuario.permisos.codigoPartido.isNullOrEmpty()
    }
    
    /**
     * Verifica si el usuario es un operador (puede editar)
     */
    fun esOperador(): Boolean {
        return _usuarioActual.value?.rol == RolUsuario.OPERADOR
    }
    
    /**
     * Verifica si el usuario es solo visualizador
     */
    fun esVisualizador(): Boolean {
        return _usuarioActual.value?.rol == RolUsuario.VISUALIZADOR
    }
    
    /**
     * Verifica si el usuario puede editar datos
     */
    fun puedeEditar(): Boolean {
        val usuario = _usuarioActual.value ?: return false
        return usuario.rol == RolUsuario.ADMINISTRADOR || 
               usuario.rol == RolUsuario.OPERADOR ||
               usuario.permisos.puedeEditar
    }
    
    /**
     * Verifica si el usuario puede crear entidades
     */
    fun puedeCrear(): Boolean {
        val usuario = _usuarioActual.value ?: return false
        return usuario.rol == RolUsuario.ADMINISTRADOR || 
               usuario.permisos.puedeCrear
    }
    
    /**
     * Verifica si el usuario puede eliminar entidades
     */
    fun puedeEliminar(): Boolean {
        val usuario = _usuarioActual.value ?: return false
        return usuario.rol == RolUsuario.ADMINISTRADOR || 
               usuario.permisos.puedeEliminar
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // PERMISOS ESPECÍFICOS
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Obtiene el código del partido asignado al usuario
     * @return Código del partido o null si no tiene asignado
     */
    fun getPartidoAsignado(): String? {
        return _usuarioActual.value?.permisos?.codigoPartido
    }
    
    /**
     * Obtiene el código del campeonato asignado al usuario
     * @return Código del campeonato o null si tiene acceso a todos
     */
    fun getCampeonatoAsignado(): String? {
        return _usuarioActual.value?.permisos?.codigoCampeonato
    }
    
    /**
     * Verifica si el usuario tiene acceso a un campeonato específico
     */
    fun tieneAccesoCampeonato(codigoCampeonato: String?): Boolean {
        val usuario = _usuarioActual.value ?: return false
        
        // Admin tiene acceso a todo
        if (esAdministrador()) return true
        
        // Si no tiene restricción, tiene acceso a todo
        val campeonatoAsignado = usuario.permisos.codigoCampeonato
        if (campeonatoAsignado.isNullOrEmpty()) return true
        
        // Si tiene restricción, verificar que coincida
        return campeonatoAsignado == codigoCampeonato
    }
    
    /**
     * Verifica si el usuario tiene acceso a un partido específico
     */
    fun tieneAccesoPartido(codigoPartido: String?): Boolean {
        val usuario = _usuarioActual.value ?: return false
        
        // Admin tiene acceso a todo
        if (esAdministrador()) return true
        
        // Si no tiene restricción, tiene acceso a todo
        val partidoAsignado = usuario.permisos.codigoPartido
        if (partidoAsignado.isNullOrEmpty()) return true
        
        // Si tiene restricción, verificar que coincida
        return partidoAsignado == codigoPartido
    }
    
    /**
     * Actualiza el partido asignado al usuario
     * Útil cuando el corresponsal se auto-asigna un partido
     */
    fun actualizarPartidoAsignado(codigoPartido: String) {
        val usuario = _usuarioActual.value ?: return
        usuario.permisos.codigoPartido = codigoPartido
        _usuarioActual.value = usuario.copy()
        
        // Persistir
        sharedPreferences?.edit()?.apply {
            putString(KEY_PARTIDO_ASIGNADO, codigoPartido)
            apply()
        }
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // INFORMACIÓN DEL USUARIO
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Obtiene el nombre completo del usuario
     */
    fun getNombreCompleto(): String {
        return _usuarioActual.value?.nombre ?: "Usuario"
    }
    
    /**
     * Obtiene el rol del usuario
     */
    fun getRol(): String {
        return _usuarioActual.value?.rol ?: RolUsuario.VISUALIZADOR
    }
    
    /**
     * Obtiene una descripción del rol
     */
    fun getDescripcionRol(): String {
        return when (_usuarioActual.value?.rol) {
            RolUsuario.ADMINISTRADOR -> "Administrador"
            RolUsuario.OPERADOR -> "Operador"
            RolUsuario.VISUALIZADOR -> "Visualizador"
            else -> "Usuario"
        }
    }
    
    /**
     * Verifica si hay un usuario autenticado
     */
    fun estaAutenticado(): Boolean {
        return _usuarioActual.value != null
    }
}
