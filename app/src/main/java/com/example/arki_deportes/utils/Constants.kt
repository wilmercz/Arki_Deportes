// app/src/main/java/com/example/arki_deportes/utils/Constants.kt

package com.example.arki_deportes.utils

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * CONSTANTS.KT - CONSTANTES DE LA APLICACIÓN
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * @author ARKI SISTEMAS
 * @version 1.0.1 - Actualizado para estructura jerárquica
 */

object Constants {

    // ═══════════════════════════════════════════════════════════════════════
    // INFORMACIÓN DE LA EMPRESA
    // ═══════════════════════════════════════════════════════════════════════

    const val EMPRESA_NOMBRE = "Wilsoft Coorp."
    const val EMPRESA_NOMBRE_COMPLETO = "ARKI SISTEMAS CIA. LTDA."
    const val EMPRESA_DESCRIPCION = "Soluciones tecnológicas innovadoras"
    const val EMPRESA_DIRECCION = "Puyo, Pastaza, Ecuador"
    const val EMPRESA_TELEFONO = "+593 99 123 4567"
    const val EMPRESA_EMAIL = "info@arkisistemas.com"
    const val EMPRESA_WEB = "www.arkisistemas.com"
    const val EMPRESA_ANIO_FUNDACION = "2020"
    const val EMPRESA_MISION = "Proporcionar soluciones tecnológicas innovadoras que impulsen el crecimiento de nuestros clientes, mediante el desarrollo de software de alta calidad y un servicio excepcional."
    const val EMPRESA_VISION = "Ser la empresa líder en desarrollo de software en la región amazónica, reconocida por la excelencia en nuestros productos y la satisfacción de nuestros clientes."
    const val EMPRESA_LOGO = "logo_arki"
    const val EMPRESA_COLOR_PRIMARY = 0xFF4A90E2
    const val EMPRESA_COLOR_SECONDARY = 0xFFFF8A3D

    // ═══════════════════════════════════════════════════════════════════════
    // INFORMACIÓN DE LA APLICACIÓN
    // ═══════════════════════════════════════════════════════════════════════

    const val APP_NOMBRE = "Arki Deportes"
    const val APP_VERSION = "1.0.1"
    const val APP_VERSION_CODE = 2
    const val APP_FECHA_ACTUALIZACION = "26/10/2025"
    const val APP_DESCRIPCION = "Aplicación móvil para la gestión integral de campeonatos de fútbol. Permite crear y administrar campeonatos, equipos, partidos y visualizar información en tiempo real durante las transmisiones."
    const val APP_DESARROLLADOR = "Equipo de Desarrollo ARKI"
    const val APP_ANIO_DESARROLLO = "2025"

    // ═══════════════════════════════════════════════════════════════════════
    // CONFIGURACIÓN FIREBASE - ESTRUCTURA JERÁRQUICA
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * ⚠️ IMPORTANTE: Nodo raíz actualizado para estructura jerárquica
     *
     * Estructura en Firebase:
     * FutbolWC/
     *   ├─ CODIGO_CAMPEONATO_1/
     *   │   ├─ CAMPEONATO: "..."
     *   │   ├─ ANIO: 2025
     *   │   ├─ Partidos/
     *   │   │   └─ codigo_partido: {...}
     *   │   ├─ Equipos/
     *   │   │   └─ codigo_equipo: {...}
     *   │   └─ Grupos/
     *   │       └─ codigo_grupo: {...}
     */
    const val FIREBASE_NODO_RAIZ_DEFAULT = "FutbolWC"

    /** Nombre de las sub-colecciones dentro de cada campeonato */
    object FirebaseCollections {
        const val ACCESO = "Acceso"

        // ⚠️ NOTA: En estructura jerárquica, estos no son nodos raíz,
        // sino sub-nodos dentro de cada campeonato
        const val CAMPEONATOS = "FutbolWC"  // Raíz directa
        const val GRUPOS = "Grupos"          // Dentro de cada campeonato
        const val EQUIPOS = "Equipos"        // Dentro de cada campeonato
        const val PARTIDOS = "Partidos"      // Dentro de cada campeonato

        const val PARTIDO_ACTUAL = "PartidoActual"
        const val MENCIONES = "Menciones"
        const val EQUIPO_PRODUCCION = "EquipoProduccion"
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

    // ═══════════════════════════════════════════════════════════════════════
    // VALORES DE ETAPAS DE PARTIDOS
    // ═══════════════════════════════════════════════════════════════════════

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

    const val PLACEHOLDER_ESCUDO = "ic_shield_placeholder"
    const val ERROR_IMAGE = "ic_error_image"

    // ═══════════════════════════════════════════════════════════════════════
    // REDES SOCIALES
    // ═══════════════════════════════════════════════════════════════════════

    object RedesSociales {
        const val FACEBOOK = "https://facebook.com/arkisistemas"
        const val TWITTER = "https://twitter.com/arkisistemas"
        const val INSTAGRAM = "https://instagram.com/arkisistemas"
        const val LINKEDIN = "https://linkedin.com/company/arkisistemas"
    }

    object ProvinciasEcuador {
        val TODAS = listOf(
            "Azuay",
            "Bolívar",
            "Cañar",
            "Carchi",
            "Chimborazo",
            "Cotopaxi",
            "El Oro",
            "Esmeraldas",
            "Galápagos",
            "Guayas",
            "Imbabura",
            "Loja",
            "Los Ríos",
            "Manabí",
            "Morona Santiago",
            "Napo",
            "Orellana",
            "Pastaza",
            "Pichincha",
            "Santa Elena",
            "Santo Domingo de los Tsáchilas",
            "Sucumbíos",
            "Tungurahua",
            "Zamora Chinchipe"
        )
    }
}