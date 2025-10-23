// app/src/main/java/com/example/arki_deportes/navigation/DrawerContent.kt

package com.example.arki_deportes.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private fun String?.matchesRoute(route: String): Boolean {
    return this == route || this?.startsWith("$route/") == true
}

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * DRAWER CONTENT - CONTENIDO DEL MENÚ LATERAL
 * ═══════════════════════════════════════════════════════════════════════════
 */
@Composable
fun DrawerContent(
    navigator: AppNavigator,
    onCloseDrawer: () -> Unit,
    currentRoute: String? = null,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Header del Drawer
        DrawerHeader()

        Divider()

        // Menú principal
        DrawerMenuItem(
            icon = Icons.Default.Home,
            label = "Inicio",
            isSelected = currentRoute.matchesRoute(AppDestinations.HYBRID_HOME),
            onClick = {
                navigator.navigateToHybridHome()
                onCloseDrawer()
            }
        )

        DrawerMenuItem(
            icon = Icons.Default.VideoLibrary,
            label = "Tiempo Real",
            isSelected = currentRoute.matchesRoute(AppDestinations.REAL_TIME),
            onClick = {
                navigator.navigateToRealTime()
                onCloseDrawer()
            }
        )

        DrawerMenuItem(
            icon = Icons.Default.Category,
            label = "Catálogos",
            isSelected = currentRoute.matchesRoute(AppDestinations.CATALOGS),
            onClick = {
                navigator.navigateToCatalogs()
                onCloseDrawer()
            }
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Sección de gestión
        Text(
            text = "GESTIÓN",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        DrawerMenuItem(
            icon = Icons.Default.EmojiEvents,
            label = "Campeonatos",
            isSelected = currentRoute.matchesRoute(AppDestinations.CAMPEONATO_FORM),
            onClick = {
                navigator.navigateToCampeonatoForm()
                onCloseDrawer()
            }
        )

        DrawerMenuItem(
            icon = Icons.Default.Group,
            label = "Grupos",
            isSelected = currentRoute.matchesRoute(AppDestinations.GRUPO_FORM),
            onClick = {
                navigator.navigateToGrupoForm()
                onCloseDrawer()
            }
        )

        DrawerMenuItem(
            icon = Icons.Default.Shield,
            label = "Equipos",
            isSelected = currentRoute.matchesRoute(AppDestinations.EQUIPO_FORM),
            onClick = {
                navigator.navigateToEquipoForm()
                onCloseDrawer()
            }
        )

        DrawerMenuItem(
            icon = Icons.Default.SportsScore,
            label = "Partidos",
            isSelected = currentRoute.matchesRoute(AppDestinations.PARTIDO_FORM),
            onClick = {
                navigator.navigateToPartidoForm()
                onCloseDrawer()
            }
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Otras opciones
        Text(
            text = "OTRAS OPCIONES",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        DrawerMenuItem(
            icon = Icons.Default.Mic,
            label = "Menciones",
            isSelected = currentRoute.matchesRoute(AppDestinations.MENCIONES),
            onClick = {
                navigator.navigateToMenciones()
                onCloseDrawer()
            }
        )

        DrawerMenuItem(
            icon = Icons.Default.People,
            label = "Equipo Producción",
            isSelected = currentRoute.matchesRoute(AppDestinations.EQUIPO_PRODUCCION),
            onClick = {
                navigator.navigateToEquipoProduccion()
                onCloseDrawer()
            }
        )

        Spacer(modifier = Modifier.weight(1f))

        Divider()

        // Configuración y Cerrar Sesión
        DrawerMenuItem(
            icon = Icons.Default.Settings,
            label = "Configuración",
            isSelected = currentRoute.matchesRoute(AppDestinations.SETTINGS),
            onClick = {
                navigator.navigateToSettings()
                onCloseDrawer()
            }
        )

        DrawerMenuItem(
            icon = Icons.Default.ExitToApp,
            label = "Cerrar Sesión",
            onClick = {
                onLogout()
                navigator.navigateToLogin(clearBackStack = true)
                onCloseDrawer()
            }
        )
    }
}

/**
 * Header del Drawer con información de la app
 */
@Composable
private fun DrawerHeader() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer)
            .padding(24.dp)
    ) {
        Column {
            Icon(
                imageVector = Icons.Default.SportsSoccer,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Arki Deportes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                text = "Sistema de Transmisión",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Item individual del menú
 */
@Composable
private fun DrawerMenuItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surface
    }

    val contentColor = if (isSelected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyLarge,
            color = contentColor
        )
    }
}