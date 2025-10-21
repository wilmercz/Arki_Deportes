// app/src/main/java/com/example/arki_deportes/ui/theme/Color.kt

package com.example.arki_deportes.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * COLOR.KT - PALETA DE COLORES
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Define los colores utilizados en toda la aplicación.
 * Basados en los colores corporativos de ARKI SISTEMAS.
 *
 * 🎨 Para cambiar los colores, modifica los valores hexadecimales aquí.
 *
 * @author ARKI SISTEMAS
 * @version 1.0.0
 */

// ═══════════════════════════════════════════════════════════════════════════
// COLORES PRINCIPALES DE LA EMPRESA (ARKI SISTEMAS)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Azul corporativo - Color principal de ARKI SISTEMAS
 * #4a90e2
 * Usado para: AppBar, botones principales, elementos destacados, encabezados
 */
val ArkiBluePrimary = Color(0xFF4A90E2)

/**
 * Azul oscuro - Variante oscura del azul principal
 * Usado para: Hover en botones, estados presionados
 */
val ArkiBlueDark = Color(0xFF3A7BC8)

/**
 * Azul claro - Variante clara del azul principal
 * Usado para: Backgrounds suaves, estados hover, chips
 */
val ArkiBlueLight = Color(0xFF6FA8E8)

/**
 * Naranja corporativo - Color secundario de ARKI SISTEMAS
 * #ff8a3d
 * Usado para: Acentos, botones secundarios, iconos importantes, badges
 */
val ArkiOrangeSecondary = Color(0xFFFF8A3D)

/**
 * Naranja oscuro - Variante oscura del naranja
 * Usado para: Hover en botones naranjas, estados activos
 */
val ArkiOrangeDark = Color(0xFFE57A2D)

/**
 * Naranja claro - Variante clara del naranja
 * Usado para: Backgrounds de alertas, notificaciones suaves
 */
val ArkiOrangeLight = Color(0xFFFFA667)

// ═══════════════════════════════════════════════════════════════════════════
// COLORES DE ESTADOS
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Rojo - Para errores y alertas
 */
val ErrorRed = Color(0xFFD32F2F)

/**
 * Rojo claro - Para backgrounds de error
 */
val ErrorRedLight = Color(0xFFFFCDD2)

/**
 * Verde - Para éxito
 */
val SuccessGreen = Color(0xFF388E3C)

/**
 * Verde claro - Para backgrounds de éxito
 */
val SuccessGreenLight = Color(0xFFC8E6C9)

/**
 * Naranja - Para advertencias
 */
val WarningOrange = Color(0xFFF57C00)

/**
 * Azul - Para información
 */
val InfoBlue = Color(0xFF1976D2)

// ═══════════════════════════════════════════════════════════════════════════
// COLORES DE FONDO Y SUPERFICIE (Base Blanco y Negro)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Blanco puro - Fondo principal de la aplicación
 * La app usa fondo blanco como base
 */
val BackgroundLight = Color(0xFFFFFFFF)

/**
 * Gris muy claro - Para cards y superficies elevadas
 */
val SurfaceLight = Color(0xFFF8F9FA)

/**
 * Gris súper claro - Para separadores y divisores sutiles
 */
val CardLight = Color(0xFFFAFAFA)

/**
 * Negro casi puro - Fondo en modo oscuro (opcional)
 */
val BackgroundDark = Color(0xFF121212)

/**
 * Gris oscuro - Superficie en modo oscuro (opcional)
 */
val SurfaceDark = Color(0xFF1E1E1E)

/**
 * Gris medio oscuro - Para cards en modo oscuro (opcional)
 */
val CardDark = Color(0xFF2C2C2C)

// ═══════════════════════════════════════════════════════════════════════════
// COLORES DE TEXTO (Negro como base)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Negro - Texto principal
 * La app usa letras negras sobre fondo blanco
 */
val TextPrimary = Color(0xFF000000)

/**
 * Gris oscuro - Texto secundario
 * Para subtítulos y texto de menor importancia
 */
val TextSecondary = Color(0xFF666666)

/**
 * Gris medio - Texto deshabilitado
 * Para campos inactivos o placeholder
 */
val TextDisabled = Color(0xFF999999)

/**
 * Blanco - Texto sobre fondos de color (azul/naranja)
 * Para texto en botones y AppBar
 */
val TextOnColor = Color(0xFFFFFFFF)

/**
 * Blanco - Texto en modo oscuro (opcional)
 */
val TextPrimaryDark = Color(0xFFFFFFFF)

/**
 * Gris claro - Texto secundario en modo oscuro (opcional)
 */
val TextSecondaryDark = Color(0xFFB0B0B0)

// ═══════════════════════════════════════════════════════════════════════════
// COLORES ESPECÍFICOS DE FÚTBOL
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Verde césped - Para fondos relacionados con fútbol
 */
val FootballGreen = Color(0xFF2E7D32)

/**
 * Amarillo tarjeta - Para tarjetas amarillas
 */
val YellowCard = Color(0xFFFFEB3B)

/**
 * Rojo tarjeta - Para tarjetas rojas
 */
val RedCard = Color(0xFFE53935)

/**
 * Azul estadio - Para información de estadios
 */
val StadiumBlue = Color(0xFF1565C0)

/**
 * Dorado - Para trofeos y campeonatos
 */
val TrophyGold = Color(0xFFFFD700)

// ═══════════════════════════════════════════════════════════════════════════
// COLORES DE DIVIDERS Y BORDES
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Gris muy claro - Para dividers en modo claro
 */
val DividerLight = Color(0xFFE0E0E0)

/**
 * Gris oscuro - Para dividers en modo oscuro
 */
val DividerDark = Color(0xFF424242)

/**
 * Gris medio - Para bordes
 */
val BorderGray = Color(0xFFBDBDBD)

// ═══════════════════════════════════════════════════════════════════════════
// GRADIENTES (Para elementos decorativos con colores corporativos)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Gradiente azul corporativo
 * Uso en Compose: Brush.verticalGradient(GradientBlue)
 * Para headers, cards destacadas
 */
val GradientBlue = listOf(
    ArkiBluePrimary,
    ArkiBlueLight
)

/**
 * Gradiente naranja corporativo
 * Para botones de acción importantes, badges
 */
val GradientOrange = listOf(
    ArkiOrangeSecondary,
    ArkiOrangeLight
)

/**
 * Gradiente combinado (azul a naranja)
 * Para elementos especiales y destacados
 */
val GradientCorporate = listOf(
    ArkiBluePrimary,
    ArkiOrangeSecondary
)

// ═══════════════════════════════════════════════════════════════════════════
// COLORES LEGACY (del tema por defecto de Android - mantener por compatibilidad)
// ═══════════════════════════════════════════════════════════════════════════

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)