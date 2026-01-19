// app/src/main/java/com/example/arki_deportes/data/model/Usuario.kt

package com.example.arki_deportes.data.model

import com.google.firebase.database.IgnoreExtraProperties

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * USUARIO - MODELO DE DATOS
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Representa un usuario del sistema con sus permisos y roles.
 *
 * Estructura en Firebase:
 * ```
 * AppConfig/
 *   └── Usuarios/
 *       └── [USUARIO]/
 *           ├── usuario: "Carlos"
 *           ├── nombre: "Carlos Chacha"
 *           ├── password: "123456"
 *           ├── rol: "OPERADOR"
 *           ├── autorizado: true
 *           ├── ultimoAcceso: 1763838840275
 *           └── permisos/
 *               ├── codigoCampeonato: "PROVINCIAL_2025"
 *               ├── codigoPartido: "PARTIDO_ABC"
 *               ├── verEstadisticas: true
 *               └── editarPartidos: true
 * ```
 *
 * @author ARKI SISTEMAS
 * @version 2.0.0
 */
@IgnoreExtraProperties
data class Usuario(
    /**
     * Nombre de usuario (único)
     * Ejemplo: "Carlos", "Admin", "JuanCorresponsal"
     */
    val usuario: String = "",

    /**
     * Nombre completo del usuario
     * Ejemplo: "Carlos Chacha"
     */
    val nombre: String = "",

    /**
     * Contraseña en texto plano
     * TODO: Implementar hash en futuras versiones
     */
    val password: String = "",

    /**
     * Rol del usuario
     * Valores: "ADMIN", "OPERADOR", "CORRESPONSAL"
     */
    val rol: String = "",

    /**
     * Si el usuario está autorizado para acceder
     * false = usuario deshabilitado
     */
    val autorizado: Boolean = false,

    /**
     * Timestamp de último acceso (milisegundos)
     * Se actualiza automáticamente en cada login
     */
    val ultimoAcceso: Long = 0,

    /**
     * Permisos específicos del usuario
     * Define campeonatos/partidos a los que tiene acceso
     */
    val permisos: Permisos = Permisos()
) {
    /**
     * Verifica si el usuario es administrador
     */
    fun esAdmin(): Boolean = rol.equals("ADMIN", ignoreCase = true)

    /**
     * Verifica si el usuario es operador
     */
    fun esOperador(): Boolean = rol.equals("OPERADOR", ignoreCase = true)

    /**
     * Verifica si el usuario es corresponsal
     */
    fun esCorresponsal(): Boolean = rol.equals("CORRESPONSAL", ignoreCase = true)

    /**
     * Verifica si tiene un partido específico asignado
     * Típico de corresponsales
     */
    fun tienePartidoAsignado(): Boolean {
        return !permisos.codigoPartido.isNullOrEmpty() &&
                permisos.codigoPartido != "NINGUNO"
    }

    /**
     * Verifica si tiene acceso a todos los campeonatos
     */
    fun tieneAccesoTotal(): Boolean {
        return permisos.codigoCampeonato.isNullOrEmpty()
    }

    /**
     * Obtiene el nombre a mostrar en la UI
     */
    fun getNombreDisplay(): String {
        return nombre.ifBlank { usuario }
    }

    companion object {
        /**
         * Crea una instancia vacía
         */
        fun empty() = Usuario()
    }
}

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * PERMISOS - MODELO DE DATOS
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Define los permisos granulares de un usuario.
 */
@IgnoreExtraProperties
data class Permisos(
    /**
     * Código del campeonato al que tiene acceso
     * - null = Acceso a todos los campeonatos (ADMIN/OPERADOR)
     * - "NINGUNO" = Sin acceso a campeonatos
     * - "PROVINCIAL_2025_..." = Acceso solo a ese campeonato
     */
    val codigoCampeonato: String? = null,

    /**
     * Código del partido asignado
     * - null = Sin partido específico (ADMIN/OPERADOR)
     * - "NINGUNO" = Sin partido asignado
     * - "PARTIDO_ABC_..." = Partido específico (CORRESPONSAL)
     */
    val codigoPartido: String? = null,

    /**
     * Si puede ver estadísticas avanzadas
     */
    val verEstadisticas: Boolean = true,

    /**
     * Si puede editar/crear partidos
     * false para corresponsales (solo control en vivo)
     */
    val editarPartidos: Boolean = false,

    /**
     * Si puede gestionar usuarios (solo ADMIN)
     */
    val gestionarUsuarios: Boolean = false
) {
    companion object {
        fun empty() = Permisos()
    }
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