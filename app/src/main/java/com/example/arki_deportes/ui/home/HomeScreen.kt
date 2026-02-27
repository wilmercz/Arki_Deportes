package com.example.arki_deportes.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arki_deportes.data.context.CampeonatoContext
import com.example.arki_deportes.data.context.UsuarioContext
import com.example.arki_deportes.data.model.Campeonato
import com.example.arki_deportes.data.model.Partido
import com.example.arki_deportes.data.model.PartidoActual
import com.example.arki_deportes.ui.components.CampeonatoSelector
import com.example.arki_deportes.utils.Constants
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Entry point para la pantalla Home enlazado al ViewModel.
 */
@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    onNavigateToPartidos: () -> Unit,
    onNavigateToCampeonatos: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenDrawer: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val campeonatoActivo by CampeonatoContext.campeonatoActivo.collectAsState()

    // Cargar partidos si ya hay un campeonato en el contexto al entrar
    LaunchedEffect(campeonatoActivo) {
        campeonatoActivo?.let { 
            viewModel.cargarPartidosDeCampeonato(it.CODIGO)
        }
    }

    HomeScreen(
        state = uiState,
        onRefresh = { 
            viewModel.refrescarPartidos()
        },
        onSearchMatches = onNavigateToPartidos,
        onSelectCampeonato = { camp -> 
            CampeonatoContext.seleccionarCampeonato(camp)
            viewModel.cargarPartidosDeCampeonato(camp.CODIGO)
        },
        onAsignarPartido = { p -> viewModel.asignarPartido(p.CAMPEONATOCODIGO, p.CODIGOPARTIDO) },
        onLimpiarAsignacion = { viewModel.limpiarAsignacionManual() },
        campeonatoActivo = campeonatoActivo,
        modifier = modifier,
        onOpenDrawer = onOpenDrawer
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    onRefresh: () -> Unit,
    onSearchMatches: () -> Unit,
    onSelectCampeonato: (Campeonato) -> Unit,
    onAsignarPartido: (Partido) -> Unit,
    onLimpiarAsignacion: () -> Unit,
    campeonatoActivo: Campeonato?,
    modifier: Modifier = Modifier,
    onOpenDrawer: (() -> Unit)? = null
) {
    val swipeState = rememberSwipeRefreshState(isRefreshing = state.isRefreshing)
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val usuario = UsuarioContext.getUsuario()
    
    // Verificar si hay una asignación caducada en el perfil del usuario
    val tieneAsignacionEnPerfil = !usuario?.permisos?.codigoPartido.isNullOrBlank() && 
                                 usuario?.permisos?.codigoPartido != "NINGUNO"
    val mostrarAvisoCaducado = tieneAsignacionEnPerfil && !state.isLive

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(text = "Inicio") },
                navigationIcon = {
                    onOpenDrawer?.let { open ->
                        IconButton(onClick = open) {
                            Icon(imageVector = Icons.Filled.Menu, contentDescription = "Abrir menú")
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        SwipeRefresh(
            state = swipeState,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. PARTIDO EN VIVO
                if (state.isLive && state.liveMatch != null) {
                    item { LiveMatchCard(state.liveMatch) }
                }

                // 2. AVISO DE PARTIDO CADUCADO
                if (mostrarAvisoCaducado) {
                    item {
                        ExpiredAssignmentCard(
                            campeonatoNombre = usuario?.permisos?.codigoCampeonato ?: "",
                            partidoNombre = usuario?.permisos?.codigoPartido ?: "",
                            onClear = onLimpiarAsignacion
                        )
                    }
                }

                // 3. ASISTENTE DE ASIGNACIÓN
                item {
                    AssignmentAssistantPanel(
                        campeonatos = state.campeonatos,
                        partidos = state.partidosDelCampeonato,
                        campeonatoSeleccionado = campeonatoActivo,
                        onSelectCampeonato = onSelectCampeonato,
                        onAsignarPartido = onAsignarPartido,
                        isLoading = state.isLoadingAsistente,
                        mensaje = state.mensajeAsistente
                    )
                }

                // 4. LISTA DE PARTIDOS GENERALES (±7 días)
                if (state.partidos.isNotEmpty()) {
                    item { SectionTitle(text = "Próximos Encuentros (±7 días)") }
                    items(state.partidos) { partido ->
                        PartidoCard(partido = partido)
                    }
                }
            }
        }
    }
}

@Composable
private fun ExpiredAssignmentCard(
    campeonatoNombre: String,
    partidoNombre: String,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.7f),
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.error)
    ) {
        Column(modifier = Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "ASIGNACIÓN CADUCADA", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tenías asignado el partido: \n$partidoNombre",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onClear,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Limpiar Asignación Vieja")
            }
        }
    }
}

@Composable
private fun AssignmentAssistantPanel(
    campeonatos: List<Campeonato>,
    partidos: List<Partido>,
    campeonatoSeleccionado: Campeonato?,
    onSelectCampeonato: (Campeonato) -> Unit,
    onAsignarPartido: (Partido) -> Unit,
    isLoading: Boolean,
    mensaje: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Asistente de Asignación Rápida",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "Primero elige un torneo para ver sus partidos disponibles.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            if (campeonatos.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(56.dp), contentAlignment = Alignment.Center) {
                    Text("Cargando campeonatos...", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                CampeonatoSelector(
                    campeonatos = campeonatos,
                    campeonatoSeleccionado = campeonatoSeleccionado?.CODIGO,
                    onCampeonatoSeleccionado = onSelectCampeonato,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 0.dp)
                )
            }

            if (campeonatoSeleccionado != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Partidos de ${campeonatoSeleccionado.CAMPEONATO}:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (isLoading) {
                    Box(modifier = Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                } else if (partidos.isEmpty()) {
                    Text(
                        text = "No se encontraron partidos para hoy o fechas cercanas.",
                        modifier = Modifier.padding(vertical = 16.dp).fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    partidos.forEach { partido ->
                        QuickMatchItem(partido = partido, onAsignar = { onAsignarPartido(partido) })
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            mensaje?.let {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = it, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun QuickMatchItem(partido: Partido, onAsignar: () -> Unit) {
    val esCaducado = remember(partido.FECHA_PARTIDO) {
        verificarSiEstaCaducado(partido.FECHA_PARTIDO)
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (!esCaducado) Modifier.clickable { onAsignar() } else Modifier),
        color = if (esCaducado)
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        else
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            if (esCaducado) MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
        )
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                // 1. Equipos
                Text(
                    text = "${partido.EQUIPO1} vs ${partido.EQUIPO2}",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (esCaducado) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f) else Color.Unspecified
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 2. Ubicación y Fecha
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (esCaducado) Color.Gray else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = partido.ESTADIO.ifBlank { "Sin estadio" },
                        style = MaterialTheme.typography.labelSmall,
                        color = if (esCaducado) Color.Gray else Color.Unspecified
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (esCaducado) Color.Gray else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${partido.FECHA_PARTIDO} ${partido.HORA_PARTIDO}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (esCaducado) Color.Gray else Color.Unspecified
                    )
                }

                // 3. Etiquetas de Estado (OPERADOR / DISPONIBILIDAD / CADUCADO)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (esCaducado) {
                        StatusTag(text = "CADUCADO", color = MaterialTheme.colorScheme.error)
                    } else {
                        val operador = partido.OPERADOR.trim()
                        if (operador.isBlank() || operador == "NINGUNO") {
                            StatusTag(text = "DISPONIBLE", color = Color(0xFF2E7D32)) // Verde bosque
                        } else {
                            StatusTag(text = "Operador: $operador", color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }

/*
                // 4. Etiqueta de Caducado (ahora al final)
                if (esCaducado) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "CADUCADO",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onError,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }*/
            }

            // Icono de acción a la derecha
            if (!esCaducado) {
                IconButton(onClick = onAsignar) {
                    Icon(
                        imageVector = Icons.Default.AddCircle,
                        contentDescription = "Asignar",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            } else {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = "No disponible",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                    modifier = Modifier.padding(12.dp).size(24.dp)
                )
            }
        }
    }
}


@Composable
private fun StatusTag(text: String, color: Color) {
    Surface(
        color = color,
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text.uppercase(),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = Color.White,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun LiveMatchCard(partido: PartidoActual) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Icon(imageVector = Icons.Rounded.LiveTv, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Text(text = "En vivo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            Text(text = "Fútbol", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                TeamScore(nombre = partido.EQUIPO1, valor = partido.GOLES1, etiqueta = "Goles", modifier = Modifier.weight(1f))
                Column(modifier = Modifier.padding(horizontal = 16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "Marcador", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                    Text(text = "${partido.GOLES1} - ${partido.GOLES2}", fontSize = 34.sp, fontWeight = FontWeight.Black)
                    Text(text = "Tiempo", style = MaterialTheme.typography.labelMedium)
                    Text(text = partido.getTiempoNormalizado(), style = MaterialTheme.typography.bodyMedium, textAlign = TextAlign.Center)
                }
                TeamScore(nombre = partido.EQUIPO2, valor = partido.GOLES2, etiqueta = "Goles", modifier = Modifier.weight(1f))
            }
            SurfaceStatus(text = partido.getEstadoTexto(), icon = partido.getEstadoIcono())
        }
    }
}

@Composable
private fun TeamScore(nombre: String, valor: Int, etiqueta: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(text = nombre.ifBlank { "Equipo" }, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center)
        Text(text = valor.toString(), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(text = etiqueta, style = MaterialTheme.typography.labelMedium, textAlign = TextAlign.Center)
    }
}

@Composable
private fun SurfaceStatus(text: String, icon: String) {
    Surface(color = MaterialTheme.colorScheme.error, contentColor = MaterialTheme.colorScheme.onError, shape = MaterialTheme.shapes.medium) {
        Text(text = "$icon $text", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun PartidoCard(partido: Partido) {
    Card(modifier = Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(text = "${partido.EQUIPO1.ifBlank { "Por definir" }} vs ${partido.EQUIPO2.ifBlank { "Por definir" }}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(imageVector = Icons.Outlined.CalendarMonth, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Text(text = "${partido.FECHA_PARTIDO} · ${partido.HORA_PARTIDO}", style = MaterialTheme.typography.bodyMedium)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
            Text(text = "Estado: ${partido.getEstadoTexto()}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        }
    }
}

private fun verificarSiEstaCaducado(fechaStr: String?): Boolean {
    if (fechaStr.isNullOrBlank()) return false
    val formatos = listOf(
        DateTimeFormatter.ofPattern("yyyy-MM-dd"),
        DateTimeFormatter.ofPattern("dd/MM/yyyy"),
        DateTimeFormatter.ofPattern("yyyy/MM/dd"),
        DateTimeFormatter.ofPattern("d/M/yyyy"),
        DateTimeFormatter.ofPattern("dd-MM-yyyy")
    )
    
    val hoy = LocalDate.now()
    for (formato in formatos) {
        try {
            val fechaPartido = LocalDate.parse(fechaStr.trim(), formato)
            val fechaLimite = fechaPartido.plusDays(1)
            return hoy.isAfter(fechaLimite)
        } catch (e: Exception) { continue }
    }
    return false
}
