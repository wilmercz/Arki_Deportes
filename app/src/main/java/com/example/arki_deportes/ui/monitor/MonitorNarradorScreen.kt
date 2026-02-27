package com.example.arki_deportes.ui.monitor

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arki_deportes.data.model.*
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorNarradorScreen(
    viewModel: MonitorNarradorViewModel,
    onBack: () -> Unit,
    onOpenDrawer: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    // Lógica para auto-ocultar la notificación después de 5 segundos
    LaunchedEffect(state.ultimoEvento) {
        if (state.ultimoEvento != null) {
            delay(5000)
            viewModel.limpiarNotificacion()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Monitor de Producción") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 1. BANNER DE NOTIFICACIÓN (SISTEMA DE EVENTOS)
                AnimatedVisibility(
                    visible = state.ultimoEvento != null,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.ultimoEvento ?: "",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                // 2. HEADER: MARCADOR GIGANTE
                state.partido?.let { partido ->
                    MarcadorGrande(partido)
                }

                // 3. SECCIONES MODULARES (Tabs)
                var selectedTab by remember { mutableStateOf(0) }
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("HISTORIAL") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("E1 PLANTILLA") })
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("E2 PLANTILLA") })
                }

                when (selectedTab) {
                    0 -> HistorialSeccion(state.goles, state.cambiosE1, state.cambiosE2)
                    1 -> PlantillaSeccion(state.jugadoresE1, state.cambiosE1)
                    2 -> PlantillaSeccion(state.jugadoresE2, state.cambiosE2)
                }
            }

            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun MarcadorGrande(partido: Partido) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(partido.getEstadoTexto(), style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(partido.EQUIPO1, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                Text("${partido.GOLES1} - ${partido.GOLES2}", fontSize = 48.sp, fontWeight = FontWeight.Black)
                Text(partido.EQUIPO2, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
            Text(
                text = "⏱️ ${partido.TIEMPOJUEGO}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HistorialSeccion(goles: List<GolEvento>, cambios1: List<CambioEvento>, cambios2: List<CambioEvento>) {
    // Combinar y ordenar eventos por tiempo (implementación simplificada)
    LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        items(goles) { gol ->
            EventoItem(icon = "⚽", titulo = "¡GOL!", subtitulo = gol.JUGADOR, tiempo = "${gol.MINUTO}'")
        }
        items(cambios1 + cambios2) { cambio ->
            EventoItem(icon = "🔄", titulo = "CAMBIO", subtitulo = "Entra ${cambio.ENTRA_NOMBRE}, sale ${cambio.SALE_NOMBRE}", tiempo = "--")
        }
    }
}

@Composable
private fun PlantillaSeccion(jugadores: List<Jugador>, cambios: List<CambioEvento>) {
    LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        items(jugadores) { jugador ->
            val salio = cambios.any { it.SALE_CODIGOJUGADOR == jugador.CODIGO }
            val entro = cambios.any { it.ENTRA_CODIGOJUGADOR == jugador.CODIGO }
            
            ListItem(
                headlineContent = { 
                    Text(
                        text = "${jugador.NUMERO} - ${jugador.JUGADOR}",
                        fontWeight = if (entro) FontWeight.Bold else FontWeight.Normal,
                        color = if (salio) Color.Gray else Color.Unspecified
                    ) 
                },
                supportingContent = { Text(jugador.POSICION) },
                trailingContent = {
                    if (salio) Icon(Icons.Default.ArrowDownward, contentDescription = null, tint = Color.Red)
                    if (entro) Icon(Icons.Default.ArrowUpward, contentDescription = null, tint = Color(0xFF2E7D32))
                },
                leadingContent = {
                    Badge(containerColor = if (jugador.TITULAR == "TITULAR") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer) {
                        Text(if (jugador.TITULAR == "TITULAR") "T" else "S")
                    }
                }
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f))
        }
    }
}

@Composable
private fun EventoItem(icon: String, titulo: String, subtitulo: String, tiempo: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Text(text = icon, fontSize = 24.sp)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(titulo, fontWeight = FontWeight.Bold)
            Text(subtitulo, style = MaterialTheme.typography.bodyMedium)
        }
        Text(tiempo, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.primary)
    }
}
