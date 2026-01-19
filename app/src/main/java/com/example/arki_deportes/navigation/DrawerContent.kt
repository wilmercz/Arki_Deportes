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
import com.example.arki_deportes.ui.components.CampeonatoSelector
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import com.example.arki_deportes.data.repository.FirebaseCatalogRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

import com.example.arki_deportes.data.context.UsuarioContext
import com.example.arki_deportes.data.context.PartidoContext
import com.example.arki_deportes.data.context.CampeonatoContext
import com.example.arki_deportes.data.context.DeporteContext

import com.example.arki_deportes.utils.SportType
import com.example.arki_deportes.data.model.Partido


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
    onLogout: () -> Unit,
    catalogRepository: FirebaseCatalogRepository
) {
    val drawerVM: DrawerViewModel = viewModel(
        factory = DrawerViewModelFactory(catalogRepository)
    )
    val campeonatos by drawerVM.campeonatos.collectAsState()

    val scope = rememberCoroutineScope()


    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerTonalElevation = 0.dp
    ) {
        // Header del Drawer
        DrawerHeader()

        Divider()

        // ═══════════════════════════════════════════════════════════════════
        // SELECTOR DE CAMPEONATO - ¡NUEVO!
        // ═══════════════════════════════════════════════════════════════════
        if (campeonatos.isNotEmpty()) {
            CampeonatoSelector(
                campeonatos = campeonatos,
                modifier = Modifier.padding(top = 8.dp)
            )
        } else {
            Text(
                text = "Cargando campeonatos...",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }


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
                // Cierra el drawer de una para que se sienta rápido
                onCloseDrawer()

                scope.launch {
                    val user = UsuarioContext.getUsuario()

                    val partidoId = user?.permisos?.codigoPartido
                    val campeonatoId = user?.permisos?.codigoCampeonato

                    // Si no hay asignación → Home (lista/selección de partidos)
                    if (campeonatoId.isNullOrBlank() || campeonatoId == "NINGUNO" ||
                        partidoId.isNullOrBlank() || partidoId == "NINGUNO"
                    ) {
                        navigator.navigateToHybridHome()
                        return@launch
                    }

                    try {
                        // 1) Obtener partido
                        val partido = catalogRepository.getPartido(campeonatoId, partidoId)
                        if (partido == null) {
                            navigator.navigateToHybridHome()
                            return@launch
                        }

                        // 2) Verificar caducidad (misma regla del MainActivity)
                        val haCaducado = verificarCaducidadPartido(partido)
                        if (haCaducado) {
                            // Opcional: aquí podrías también limpiar el contexto local
                            UsuarioContext.limpiarPartidoAsignado()
                            navigator.navigateToHybridHome()
                            return@launch
                        }

                        // 3) Partido vigente → set contextos como en MainActivity
                        PartidoContext.setPartidoActivo(partido)

                        val campeonato = catalogRepository.getCampeonato(campeonatoId)
                        if (campeonato != null) {
                            CampeonatoContext.seleccionarCampeonato(campeonato)
                            val deporte = SportType.fromId(campeonato.DEPORTE)
                            DeporteContext.seleccionarDeporte(deporte)
                        }

                        // 4) Navegar a Tiempo Real con los ids asignados
                        navigator.navigateToTiempoReal(
                            campeonatoId = campeonatoId,
                            partidoId = partidoId,
                            clearBackStack = false
                        )
                    } catch (e: Exception) {
                        navigator.navigateToHybridHome()
                    }
                }
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
            isSelected = currentRoute.matchesRoute(AppDestinations.CAMPEONATO_LIST) ||
                    currentRoute.matchesRoute(AppDestinations.CAMPEONATO_FORM),
            onClick = {
                navigator.navigateToCampeonatoList()
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


private fun verificarCaducidadPartido(partido: Partido): Boolean {
    val fechaPartido = parsearFechaPartido(partido.FECHA_PARTIDO) ?: return false
    val fechaCaducidad = fechaPartido.plusDays(1)
    val fechaActual = LocalDate.now()
    return fechaActual.isAfter(fechaCaducidad)
}

private fun parsearFechaPartido(fechaStr: String): LocalDate? {
    if (fechaStr.isBlank()) return null

    val formatos = listOf(
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd"),
        DateTimeFormatter.ofPattern("d/M/yyyy"),
        DateTimeFormatter.ofPattern("M/d/yyyy"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy"),
        DateTimeFormatter.ofPattern("d-M-yyyy")
    )

    for (f in formatos) {
        try {
            return LocalDate.parse(fechaStr.trim(), f)
        } catch (_: DateTimeParseException) {}
    }
    return null
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