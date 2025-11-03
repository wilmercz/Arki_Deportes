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
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import com.example.arki_deportes.R
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import com.example.arki_deportes.ui.components.CampeonatoSelector

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState


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
    val drawerVM: DrawerViewModel = viewModel()
    val campeonatos by drawerVM.campeonatos.collectAsState()


    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerTonalElevation = 0.dp
    ) {
        // Header
        DrawerHeader()

        Divider()

        // ═══════════════════════════════════════════════════════════════════
        // SELECTOR DE CAMPEONATO - ¡NUEVO!
        // ═══════════════════════════════════════════════════════════════════
        CampeonatoSelector(
            campeonatos = campeonatos,   // ✅ AHORA sí usa la lista en tiempo real
            modifier = Modifier.padding(top = 8.dp)
        )

        Divider(modifier = Modifier.padding(vertical = 8.dp))

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

        // SECCIÓN DE GESTIÓN
        Text(
            text = "GESTIÓN",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        DrawerMenuItem(
            icon = Icons.Default.EmojiEvents,
            label = "Campeonatos",
            isSelected = currentRoute.matchesRoute(AppDestinations.CAMPEONATO_LIST) ||
                    currentRoute.matchesRoute(AppDestinations.CAMPEONATO_FORM),
            onClick = {
                navigator.navigateToCampeonatoList()
                onCloseDrawer()
            }
        )

        DrawerMenuItem(
            icon = Icons.Default.Shield,
            label = "Equipos",
            isSelected = currentRoute.matchesRoute(AppDestinations.EQUIPO_LIST) ||
                    currentRoute.matchesRoute(AppDestinations.EQUIPO_FORM),
            onClick = {
                navigator.navigateToEquipoList()
                onCloseDrawer()
            }
        )

        DrawerMenuItem(
            icon = Icons.Default.Group,
            label = "Grupos",
            isSelected = currentRoute.matchesRoute(AppDestinations.GRUPO_LIST) ||
                    currentRoute.matchesRoute(AppDestinations.GRUPO_FORM),
            onClick = {
                navigator.navigateToGrupoList()
                onCloseDrawer()
            }
        )

        DrawerMenuItem(
            icon = Icons.Default.SportsScore,
            label = "Partidos",
            isSelected = currentRoute.matchesRoute(AppDestinations.PARTIDO_LIST) ||
                    currentRoute.matchesRoute(AppDestinations.PARTIDO_FORM),
            onClick = {
                navigator.navigateToPartidoList()
                onCloseDrawer()
            }
        )


        Divider(modifier = Modifier.padding(vertical = 8.dp))

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
            // Logo blanco más pequeño (48dp -> 46dp)
            Image(
                painter = painterResource(id = R.drawable.logo_blanco),
                contentDescription = "Logo ARKI Deportes",
                modifier = Modifier.size(46.dp),
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(Color.White)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Arki Deportes",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Sistema de Transmisión",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.7f)
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