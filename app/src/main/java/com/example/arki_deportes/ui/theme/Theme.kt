// app/src/main/java/com/example/arki_deportes/ui/theme/Theme.kt

package com.example.arki_deportes.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * THEME.KT - TEMA DE LA APLICACIÓN
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Define el tema visual de la aplicación usando Material Design 3.
 * Usa los colores corporativos de ARKI SISTEMAS:
 * - Azul #4a90e2 como color principal
 * - Naranja #ff8a3d como color secundario
 * - Fondo blanco con texto negro
 *
 * @author ARKI SISTEMAS
 * @version 1.0.0
 */

// ═══════════════════════════════════════════════════════════════════════════
// ESQUEMA DE COLORES MODO OSCURO
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Paleta de colores para modo oscuro (opcional)
 * La app está diseñada principalmente para modo claro
 */
private val DarkColorScheme = darkColorScheme(
    primary = ArkiBluePrimary,
    secondary = ArkiOrangeSecondary,
    tertiary = ArkiBlueLight,

    background = BackgroundDark,
    surface = SurfaceDark,

    onPrimary = TextOnColor,
    onSecondary = TextOnColor,
    onTertiary = TextOnColor,
    onBackground = TextPrimaryDark,
    onSurface = TextPrimaryDark,

    error = ErrorRed,
    onError = TextOnColor
)

// ═══════════════════════════════════════════════════════════════════════════
// ESQUEMA DE COLORES MODO CLARO (PRINCIPAL)
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Paleta de colores para modo claro
 * Este es el tema principal de la aplicación
 *
 * Estructura:
 * - Fondo: Blanco
 * - Texto: Negro
 * - Elementos destacados: Azul corporativo
 * - Acentos: Naranja corporativo
 */
private val LightColorScheme = lightColorScheme(
    // Colores principales
    primary = ArkiBluePrimary,           // Azul #4a90e2
    onPrimary = TextOnColor,             // Blanco sobre azul
    primaryContainer = ArkiBlueLight,    // Azul claro para containers
    onPrimaryContainer = TextPrimary,    // Negro sobre azul claro

    // Colores secundarios
    secondary = ArkiOrangeSecondary,     // Naranja #ff8a3d
    onSecondary = TextOnColor,           // Blanco sobre naranja
    secondaryContainer = ArkiOrangeLight,// Naranja claro para containers
    onSecondaryContainer = TextPrimary,  // Negro sobre naranja claro

    // Colores terciarios (variante del azul)
    tertiary = ArkiBlueDark,
    onTertiary = TextOnColor,
    tertiaryContainer = ArkiBlueLight,
    onTertiaryContainer = TextPrimary,

    // Fondo y superficies
    background = BackgroundLight,        // Blanco
    onBackground = TextPrimary,          // Negro sobre blanco
    surface = SurfaceLight,              // Gris muy claro
    onSurface = TextPrimary,             // Negro sobre superficie
    surfaceVariant = CardLight,          // Para cards
    onSurfaceVariant = TextSecondary,    // Gris oscuro

    // Colores de error
    error = ErrorRed,
    onError = TextOnColor,
    errorContainer = ErrorRedLight,
    onErrorContainer = TextPrimary,

    // Bordes y contornos
    outline = BorderGray,
    outlineVariant = DividerLight,

    // Otros
    surfaceTint = ArkiBluePrimary,
    inverseSurface = TextPrimary,
    inverseOnSurface = BackgroundLight,
    inversePrimary = ArkiBlueLight,
    scrim = TextPrimary
)

// ═══════════════════════════════════════════════════════════════════════════
// FUNCIÓN PRINCIPAL DEL TEMA
// ═══════════════════════════════════════════════════════════════════════════

/**
 * Tema principal de la aplicación
 *
 * @param darkTheme Si debe usar tema oscuro (por defecto: según sistema)
 * @param dynamicColor Si debe usar colores dinámicos de Android 12+ (por defecto: false)
 * @param content Contenido de la aplicación
 *
 * Uso:
 * ```kotlin
 * setContent {
 *     Arki_DeportesTheme {
 *         // Tu contenido aquí
 *     }
 * }
 * ```
 */
@Composable
fun Arki_DeportesTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Los colores dinámicos están desactivados por defecto para mantener
    // los colores corporativos de ARKI SISTEMAS
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Seleccionar el esquema de colores
    val colorScheme = when {
        // Colores dinámicos de Android 12+ (Material You)
        // Solo si dynamicColor = true
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        // Tema oscuro
        darkTheme -> DarkColorScheme

        // Tema claro (por defecto)
        else -> LightColorScheme
    }

    // Configurar la barra de estado y navegación
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    // Aplicar el tema
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}