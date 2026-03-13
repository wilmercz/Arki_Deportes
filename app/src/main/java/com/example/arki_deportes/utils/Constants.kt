package com.example.arki_deportes.utils

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * CONSTANTS.KT - CONSTANTES DE LA APLICACIÓN
 * ═══════════════════════════════════════════════════════════════════════════
 */

object Constants {

    // ═══════════════════════════════════════════════════════════════════════
    // INFORMACIÓN DE LA EMPRESA
    // ═══════════════════════════════════════════════════════════════════════
    const val EMPRESA_NOMBRE = "ARKI SISTEMAS"
    const val EMPRESA_NOMBRE_COMPLETO = "ARKI SISTEMAS CIA. LTDA."
    const val EMPRESA_DESCRIPCION = "Soluciones tecnológicas innovadoras"
    const val EMPRESA_DIRECCION = "Puyo, Pastaza, Ecuador"
    const val EMPRESA_TELEFONO = "+593 99 123 4567"
    const val EMPRESA_EMAIL = "info@arkisistemas.com"
    const val EMPRESA_WEB = "www.arkisistemas.com"
    const val EMPRESA_ANIO_FUNDACION = "2020"
    const val EMPRESA_MISION = "Proporcionar soluciones tecnológicas innovadoras que impulsen el crecimiento de nuestros clientes."
    const val EMPRESA_VISION = "Ser la empresa líder en desarrollo de software en la región amazónica."
    const val EMPRESA_LOGO = "logo_arki"
    const val EMPRESA_COLOR_PRIMARY = 0xFF4A90E2
    const val EMPRESA_COLOR_SECONDARY = 0xFFFF8A3D

    // ═══════════════════════════════════════════════════════════════════════
    // INFORMACIÓN DE LA APLICACIÓN
    // ═══════════════════════════════════════════════════════════════════════
    const val APP_NOMBRE = "ARKI DEPORTES"
    const val APP_VERSION = "1.0.0"
    const val APP_VERSION_CODE = 1
    const val APP_FECHA_ACTUALIZACION = "20/01/2025"
    const val APP_DESCRIPCION = "Aplicación móvil para la gestión integral de campeonatos de fútbol."
    const val APP_DESARROLLADOR = "Equipo de Desarrollo ARKI"
    const val APP_ANIO_DESARROLLO = "2025"

    // ═══════════════════════════════════════════════════════════════════════
    // CONFIGURACIÓN FIREBASE
    // ═══════════════════════════════════════════════════════════════════════
    const val FIREBASE_NODO_RAIZ_DEFAULT = "ARKI_DEPORTES"

    object FirebaseCollections {
        const val ACCESO = "Acceso"
        const val CAMPEONATOS = "Campeonatos"
        const val GRUPOS = "GRUPOS"
        const val EQUIPOS = "EQUIPOS"
        const val PARTIDOS = "PARTIDOS"
        const val PARTIDO_ACTUAL = "PartidoActual"
        const val MENCIONES = "Menciones"
        const val EQUIPO_PRODUCCION = "EquipoProduccion"

        // Rutas de Producción / Configuración
        const val CONFIGURACION = "CONFIGURACION"
        const val AUDIOS = "AUDIOS"
        const val PUBLICIDAD_BANNER = "PUBLICIDAD_BANNER"
    }

    object EquipoProduccionPaths {
        const val DEFAULT = "default"
        const val CAMPEONATOS = "campeonatos"
    }

    object AccesoFields {
        const val PASSWORD = "password"
    }

    // ═══════════════════════════════════════════════════════════════════════
    // CONFIGURACIÓN DE LA APLICACIÓN
    // ═══════════════════════════════════════════════════════════════════════
    const val SHARED_PREFS_NAME = "app_config"
    const val PREF_KEY_NODO_RAIZ = "nodo_raiz"
    const val PREF_KEY_SESION_ACTIVA = "sesion_activa"
    const val DIAS_PARTIDOS_PASADOS = 7
    const val MAX_ESCUDO_SIZE_KB = 500
    const val NETWORK_TIMEOUT_SECONDS = 30L
    const val DATE_FORMAT = "dd/MM/yyyy"
    const val TIME_FORMAT = "HH:mm"
    const val DATETIME_FORMAT = "dd/MM/yyyy HH:mm:ss"

    object EtapasPartido {
        const val NINGUNO = 0
        const val CUARTOS = 1
        const val SEMIFINAL = 2
        const val FINAL = 3
        fun getTexto(etapa: Int): String {
            return when (etapa) {
                CUARTOS -> "Cuartos de Final"
                SEMIFINAL -> "Semifinal"
                FINAL -> "Final"
                else -> "Fase de Grupos"
            }
        }
    }

    object EstadosPartido {
        const val EN_JUEGO = "EnJuego"
        const val FINALIZADO = "Finalizado"
        const val PAUSADO = "Pausado"
        const val NO_INICIADO = "NoIniciado"
    }

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

    const val ORIGEN_MOBILE = "MOBILE"
    const val ORIGEN_DESKTOP = "DESKTOP"

    object NavArgs {
        const val PARTIDO_ID = "partidoId"
        const val CAMPEONATO_ID = "campeonatoId"
        const val EQUIPO_ID = "equipoId"
        const val GRUPO_ID = "grupoId"
    }

    const val PLACEHOLDER_ESCUDO = "ic_shield_placeholder"
    const val ERROR_IMAGE = "ic_error_image"

    object RedesSociales {
        const val FACEBOOK = "https://facebook.com/arkisistemas"
        const val TWITTER = "https://twitter.com/arkisistemas"
        const val INSTAGRAM = "https://instagram.com/arkisistemas"
        const val LINKEDIN = "https://linkedin.com/company/arkisistemas"
    }
}
