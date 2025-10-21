// app/src/main/java/com/example/arki_deportes/utils/Constants.kt

package com.example.arki_deportes.utils

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * CONSTANTS.KT - CONSTANTES DE LA APLICACIÓN
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Este archivo contiene todas las constantes utilizadas en la aplicación,
 * incluyendo información de la empresa, configuraciones y valores por defecto.
 *
 * ⚠️ IMPORTANTE: Para cambiar la información de la empresa, modifica las
 * constantes en la sección "INFORMACIÓN DE LA EMPRESA"
 *
 * @author ARKI SISTEMAS
 * @version 1.0.0
 * @since 2025-01-20
 */

object Constants {

    // ═══════════════════════════════════════════════════════════════════════
    // INFORMACIÓN DE LA EMPRESA
    // ═══════════════════════════════════════════════════════════════════════
    // 🔧 EDITAR AQUÍ la información de tu empresa

    /** Nombre de la empresa desarrolladora */
    const val EMPRESA_NOMBRE = "ARKI SISTEMAS"

    /** Nombre completo de la empresa */
    const val EMPRESA_NOMBRE_COMPLETO = "ARKI SISTEMAS CIA. LTDA."

    /** Eslogan o descripción de la empresa */
    const val EMPRESA_DESCRIPCION = "Soluciones tecnológicas innovadoras"

    /** Dirección de la empresa */
    const val EMPRESA_DIRECCION = "Puyo, Pastaza, Ecuador"

    /** Teléfono de contacto */
    const val EMPRESA_TELEFONO = "+593 99 123 4567"

    /** Correo electrónico */
    const val EMPRESA_EMAIL = "info@arkisistemas.com"

    /** Sitio web */
    const val EMPRESA_WEB = "www.arkisistemas.com"

    /** Año de fundación */
    const val EMPRESA_ANIO_FUNDACION = "2020"

    /** Misión de la empresa */
    const val EMPRESA_MISION = "Proporcionar soluciones tecnológicas innovadoras que impulsen el crecimiento de nuestros clientes, mediante el desarrollo de software de alta calidad y un servicio excepcional."

    /** Visión de la empresa */
    const val EMPRESA_VISION = "Ser la empresa líder en desarrollo de software en la región amazónica, reconocida por la excelencia en nuestros productos y la satisfacción de nuestros clientes."

    /** Logo de la empresa (nombre del recurso drawable) */
    const val EMPRESA_LOGO = "logo_arki"

    /** Color principal de la empresa (Hexadecimal) - Azul corporativo */
    const val EMPRESA_COLOR_PRIMARY = 0xFF4A90E2

    /** Color secundario de la empresa (Hexadecimal) - Naranja corporativo */
    const val EMPRESA_COLOR_SECONDARY = 0xFFFF8A3D

    // ═══════════════════════════════════════════════════════════════════════
    // INFORMACIÓN DE LA APLICACIÓN
    // ═══════════════════════════════════════════════════════════════════════

    /** Nombre de la aplicación */
    const val APP_NOMBRE = "FUTBOLWC Manager"

    /** Versión de la aplicación */
    const val APP_VERSION = "1.0.0"

    /** Código de versión */
    const val APP_VERSION_CODE = 1

    /** Fecha de última actualización */
    const val APP_FECHA_ACTUALIZACION = "20/01/2025"

    /** Descripción de la aplicación */
    const val APP_DESCRIPCION = "Aplicación móvil para la gestión integral de campeonatos de fútbol. Permite crear y administrar campeonatos, equipos, partidos y visualizar información en tiempo real durante las transmisiones."

    /** Desarrollador principal */
    const val APP_DESARROLLADOR = "Equipo de Desarrollo ARKI"

    /** Año de desarrollo */
    const val APP_ANIO_DESARROLLO = "2025"

    // ═══════════════════════════════════════════════════════════════════════
    // CONFIGURACIÓN FIREBASE
    // ═══════════════════════════════════════════════════════════════════════

    /** Nodo raíz por defecto en Firebase Realtime Database */
    const val FIREBASE_NODO_RAIZ_DEFAULT = "ARKI_DEPORTES"

    /** Nombre de las colecciones en Firebase */
    object FirebaseCollections {
        const val ACCESO = "Acceso"
        const val CAMPEONATOS = "CAMPEONATOS"
        const val GRUPOS = "GRUPOS"
        const val EQUIPOS = "EQUIPOS"
        const val PARTIDOS = "PARTIDOS"
        const val PARTIDO_ACTUAL = "PartidoActual"
        const val EQUIPO_PRODUCCION = "EquipoProduccion"
    }

    /** Sub-rutas internas del nodo EquipoProduccion */
    object EquipoProduccionPaths {
        const val DEFAULT = "default"
        const val CAMPEONATOS = "campeonatos"
    }

    /** Campos de la colección Acceso */
    object AccesoFields {
        const val PASSWORD = "password"
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CONFIGURACIÓN DE LA APLICACIÓN
    // ═══════════════════════════════════════════════════════════════════════

    /** Nombre del archivo de preferencias compartidas */
    const val SHARED_PREFS_NAME = "app_config"

    /** Clave para el nodo raíz en SharedPreferences */
    const val PREF_KEY_NODO_RAIZ = "nodo_raiz"

    /** Clave para el estado de sesión */
    const val PREF_KEY_SESION_ACTIVA = "sesion_activa"

    /** Días hacia atrás para mostrar partidos pasados */
    const val DIAS_PARTIDOS_PASADOS = 7

    /** Tamaño máximo de imagen de escudo (en KB) */
    const val MAX_ESCUDO_SIZE_KB = 500

    /** Tiempo de espera para operaciones de red (en segundos) */
    const val NETWORK_TIMEOUT_SECONDS = 30L

    /** Formato de fecha por defecto */
    const val DATE_FORMAT = "dd/MM/yyyy"

    /** Formato de hora por defecto */
    const val TIME_FORMAT = "HH:mm"

    /** Formato de fecha y hora completo */
    const val DATETIME_FORMAT = "dd/MM/yyyy HH:mm:ss"

    // ═══════════════════════════════════════════════════════════════════════
    // VALORES DE ETAPAS DE PARTIDOS
    // ═══════════════════════════════════════════════════════════════════════

    object EtapasPartido {
        const val NINGUNO = 0
        const val CUARTOS = 1
        const val SEMIFINAL = 2
        const val FINAL = 3

        /** Convierte el código numérico a texto legible */
        fun getTexto(etapa: Int): String {
            return when (etapa) {
                CUARTOS -> "Cuartos de Final"
                SEMIFINAL -> "Semifinal"
                FINAL -> "Final"
                else -> "Fase de Grupos"
            }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ESTADOS DE PARTIDO EN VIVO
    // ═══════════════════════════════════════════════════════════════════════

    object EstadosPartido {
        const val EN_JUEGO = "EnJuego"
        const val FINALIZADO = "Finalizado"
        const val PAUSADO = "Pausado"
        const val NO_INICIADO = "NoIniciado"
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MENSAJES Y TEXTOS COMUNES
    // ═══════════════════════════════════════════════════════════════════════

    object Mensajes {
        const val ERROR_CONEXION = "Error de conexión. Verifica tu internet."
        const val ERROR_DESCONOCIDO = "Ocurrió un error desconocido."
        const val EXITO_GUARDAR = "Guardado exitosamente"
        const val EXITO_ELIMINAR = "Eliminado exitosamente"
        const val EXITO_ACTUALIZAR = "Actualizado exitosamente"
        const val CONFIRMAR_ELIMINAR = "¿Estás seguro de eliminar este elemento?"
        const val CAMPO_OBLIGATORIO = "Este campo es obligatorio"
        const val CARGANDO = "Cargando..."
        const val SIN_DATOS = "No hay datos disponibles"
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ORIGEN DE DATOS
    // ═══════════════════════════════════════════════════════════════════════

    const val ORIGEN_MOBILE = "MOBILE"
    const val ORIGEN_DESKTOP = "DESKTOP"

    // ═══════════════════════════════════════════════════════════════════════
    // KEYS PARA NAVEGACIÓN
    // ═══════════════════════════════════════════════════════════════════════

    object NavArgs {
        const val PARTIDO_ID = "partidoId"
        const val CAMPEONATO_ID = "campeonatoId"
        const val EQUIPO_ID = "equipoId"
        const val GRUPO_ID = "grupoId"
    }

    // ═══════════════════════════════════════════════════════════════════════
    // ICONOS Y RECURSOS
    // ═══════════════════════════════════════════════════════════════════════

    /** Placeholder cuando no hay escudo */
    const val PLACEHOLDER_ESCUDO = "ic_shield_placeholder"

    /** Icono de error al cargar imagen */
    const val ERROR_IMAGE = "ic_error_image"

    // ═══════════════════════════════════════════════════════════════════════
    // REDES SOCIALES (Opcional - para compartir contenido)
    // ═══════════════════════════════════════════════════════════════════════

    object RedesSociales {
        const val FACEBOOK = "https://facebook.com/arkisistemas"
        const val TWITTER = "https://twitter.com/arkisistemas"
        const val INSTAGRAM = "https://instagram.com/arkisistemas"
        const val LINKEDIN = "https://linkedin.com/company/arkisistemas"
    }
}