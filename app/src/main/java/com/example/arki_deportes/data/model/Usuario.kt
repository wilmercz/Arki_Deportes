// app/src/main/java/com/example/arki_deportes/data/model/Usuario.kt

package com.example.arki_deportes.data.model

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * MODELO DE USUARIO - SISTEMA DE ACCESO MEJORADO
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Modelo de datos para el sistema de usuarios y autenticación.
 * Almacenado en Firebase Realtime Database fuera del nodo de campeonatos.
 *
 * Estructura en Firebase:
 * ```
 * AppConfig/
 *   └─ Usuarios/
 *       ├─ usuario1/
 *       │   ├─ usuario: "usuario1"
 *       │   ├─ password: "12345"
 *       │   ├─ autorizado: true
 *       │   ├─ nombre: "Juan Pérez"
 *       │   ├─ rol: "ADMINISTRADOR"
 *       │   ├─ fechaCreacion: 1730000000000
 *       │   ├─ ultimoAcceso: 1730000000000
 *       │   └─ permisos/
 *       │       ├─ codigoCampeonato: "COPA2025"
 *       │       └─ codigoPartido: "PART001"
 *       └─ usuario2/
 *           └─ ...
 * ```
 *
 * @property usuario Nombre de usuario único (ID)
 * @property password Contraseña del usuario
 * @property autorizado Si el usuario está autorizado para acceder
 * @property nombre Nombre completo del usuario
 * @property rol Rol del usuario (ADMINISTRADOR, OPERADOR, VISUALIZADOR)
 * @property fechaCreacion Timestamp de cuando se creó el usuario
 * @property ultimoAcceso Timestamp del último acceso exitoso
 * @property permisos Permisos específicos del usuario
 *
 * @author ARKI SISTEMAS
 * @version 2.0.0
 */
data class Usuario(
    // ═══════════════════════════════════════════════════════════════════════
    // CAMPOS PRINCIPALES
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Nombre de usuario (ID único)
     * Ejemplo: "admin", "operador1", "visualizador_copa"
     */
    var usuario: String = "",
    
    /**
     * Contraseña del usuario (se recomienda encriptar en versión futura)
     * Ejemplo: "12345", "MiPass2025!"
     */
    var password: String = "",
    
    /**
     * Si el usuario está autorizado para acceder
     * - true: Puede acceder con usuario y contraseña correcta
     * - false: No puede acceder aunque ingrese credenciales correctas
     * 
     * Útil para deshabilitar usuarios temporalmente sin eliminarlos
     */
    var autorizado: Boolean = true,
    
    // ═══════════════════════════════════════════════════════════════════════
    // INFORMACIÓN DEL USUARIO
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Nombre completo del usuario
     * Ejemplo: "Juan Pérez", "María González"
     */
    var nombre: String = "",
    
    /**
     * Rol del usuario en el sistema
     * Valores posibles: ADMINISTRADOR, OPERADOR, VISUALIZADOR
     */
    var rol: String = RolUsuario.OPERADOR,
    
    // ═══════════════════════════════════════════════════════════════════════
    // AUDITORÍA
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Timestamp de cuando se creó el usuario
     * Formato: Long (milisegundos desde epoch)
     */
    var fechaCreacion: Long = System.currentTimeMillis(),
    
    /**
     * Timestamp del último acceso exitoso
     * Formato: Long (milisegundos desde epoch)
     */
    var ultimoAcceso: Long = 0L,
    
    // ═══════════════════════════════════════════════════════════════════════
    // PERMISOS ESPECÍFICOS
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Permisos específicos del usuario
     */
    var permisos: PermisosUsuario = PermisosUsuario()
) {
    /**
     * Constructor sin argumentos requerido por Firebase
     */
    constructor() : this(
        usuario = "",
        password = "",
        autorizado = true,
        nombre = "",
        rol = RolUsuario.OPERADOR,
        fechaCreacion = System.currentTimeMillis(),
        ultimoAcceso = 0L,
        permisos = PermisosUsuario()
    )
    
    /**
     * Valida las credenciales del usuario
     */
    fun validarCredenciales(passwordIngresado: String): Boolean {
        return password == passwordIngresado && autorizado
    }
    
    /**
     * Verifica si el usuario puede acceder a un campeonato específico
     */
    fun puedeAccederCampeonato(codigoCampeonato: String?): Boolean {
        // Si no tiene restricción de campeonato, puede acceder a todos
        if (permisos.codigoCampeonato.isNullOrEmpty()) return true
        
        // Si tiene restricción, solo puede acceder al específico
        return permisos.codigoCampeonato == codigoCampeonato
    }
    
    /**
     * Verifica si el usuario puede acceder a un partido específico
     */
    fun puedeAccederPartido(codigoPartido: String?): Boolean {
        // Si no tiene restricción de partido, puede acceder a todos
        if (permisos.codigoPartido.isNullOrEmpty()) return true
        
        // Si tiene restricción, solo puede acceder al específico
        return permisos.codigoPartido == codigoPartido
    }
    
    /**
     * Actualiza el timestamp del último acceso
     */
    fun registrarAcceso() {
        ultimoAcceso = System.currentTimeMillis()
    }
}

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * PERMISOS DEL USUARIO
 * ═══════════════════════════════════════════════════════════════════════════
 */
data class PermisosUsuario(
    /**
     * Código del campeonato al que tiene acceso
     * - null o vacío: Acceso a todos los campeonatos
     * - Código específico: Solo acceso a ese campeonato
     * 
     * Ejemplo: "COPA2025", "LIBERTADORES2025"
     */
    var codigoCampeonato: String? = null,
    
    /**
     * Código del partido al que tiene acceso
     * - null o vacío: Acceso a todos los partidos
     * - Código específico: Solo acceso a ese partido (para tiempo real)
     * 
     * Ejemplo: "PART001", "PART_FINAL"
     */
    var codigoPartido: String? = null,
    
    /**
     * Permisos adicionales del usuario
     */
    var puedeCrear: Boolean = false,
    var puedeEditar: Boolean = false,
    var puedeEliminar: Boolean = false,
    var puedeVerTiempoReal: Boolean = true,
    var puedeGenerarReportes: Boolean = false
) {
    /**
     * Constructor sin argumentos requerido por Firebase
     */
    constructor() : this(
        codigoCampeonato = null,
        codigoPartido = null,
        puedeCrear = false,
        puedeEditar = false,
        puedeEliminar = false,
        puedeVerTiempoReal = true,
        puedeGenerarReportes = false
    )
}

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * ROLES DE USUARIO
 * ═══════════════════════════════════════════════════════════════════════════
 */
object RolUsuario {
    /**
     * Administrador: Acceso completo al sistema
     * - Puede crear, editar y eliminar usuarios
     * - Puede acceder a todos los campeonatos y partidos
     * - Puede generar reportes
     */
    const val ADMINISTRADOR = "ADMINISTRADOR"
    
    /**
     * Operador: Acceso de lectura/escritura
     * - Puede crear, editar y eliminar campeonatos, equipos, partidos
     * - Puede acceder al tiempo real
     * - Puede tener restricciones de campeonato/partido
     */
    const val OPERADOR = "OPERADOR"
    
    /**
     * Visualizador: Solo lectura
     * - Solo puede ver información
     * - Puede acceder al tiempo real
     * - Puede tener restricciones de campeonato/partido
     */
    const val VISUALIZADOR = "VISUALIZADOR"
    
    /**
     * Obtiene todos los roles disponibles
     */
    fun getTodosLosRoles(): List<String> {
        return listOf(ADMINISTRADOR, OPERADOR, VISUALIZADOR)
    }
    
    /**
     * Obtiene la descripción de un rol
     */
    fun getDescripcion(rol: String): String {
        return when (rol) {
            ADMINISTRADOR -> "Acceso completo al sistema"
            OPERADOR -> "Puede crear y editar contenido"
            VISUALIZADOR -> "Solo puede ver información"
            else -> "Rol desconocido"
        }
    }
}

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * RESULTADO DE AUTENTICACIÓN
 * ═══════════════════════════════════════════════════════════════════════════
 */
sealed class ResultadoAutenticacion {
    /**
     * Autenticación exitosa
     */
    data class Exito(val usuario: Usuario) : ResultadoAutenticacion()
    
    /**
     * Usuario o contraseña incorrectos
     */
    data object CredencialesInvalidas : ResultadoAutenticacion()
    
    /**
     * Usuario no autorizado (campo autorizado = false)
     */
    data object UsuarioNoAutorizado : ResultadoAutenticacion()
    
    /**
     * Usuario no encontrado en Firebase
     */
    data object UsuarioNoEncontrado : ResultadoAutenticacion()
    
    /**
     * Error de conexión o Firebase
     */
    data class Error(val mensaje: String) : ResultadoAutenticacion()
}
