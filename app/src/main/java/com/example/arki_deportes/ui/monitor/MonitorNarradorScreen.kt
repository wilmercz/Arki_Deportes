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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arki_deportes.data.model.*
import kotlinx.coroutines.delay
import androidx.compose.ui.text.style.TextAlign

/**
 * Representa un evento genérico en el historial para ordenamiento
 */
private sealed class EventoPartido {
    abstract val fechaAlta: String
    abstract val equipoNombre: String

    data class Gol(
        val datos: GolEvento,
        override val equipoNombre: String,
        override val fechaAlta: String = datos.FECHAALTA
    ) : EventoPartido()

    data class Cambio(
        val datos: CambioEvento,
        override val equipoNombre: String,
        override val fechaAlta: String = datos.FECHAALTA
    ) : EventoPartido()
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MonitorNarradorScreen(
    viewModel: MonitorNarradorViewModel,
    onBack: () -> Unit,
    onOpenDrawer: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }

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

                state.partido?.let { partido ->
                    MarcadorGrande(
                        partido = partido,
                        goles1 = state.marcadorE1,
                        goles2 = state.marcadorE2,
                        tiempoCorriendo = state.tiempoActual,
                        onEquipo1Click = { selectedTab = 1 },
                        onEquipo2Click = { selectedTab = 2 },
                        selectedTab = selectedTab
                    )
                }

                TabRow(selectedTabIndex = selectedTab) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("HISTORIAL") })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { 
                        Text(state.partido?.EQUIPO1?.take(12) ?: "EQUIPO 1") 
                    })
                    Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { 
                        Text(state.partido?.EQUIPO2?.take(12) ?: "EQUIPO 2") 
                    })
                }

                when (selectedTab) {
                    0 -> HistorialSeccion(
                        partido = state.partido,
                        goles = state.goles,
                        cambios1 = state.cambiosE1,
                        cambios2 = state.cambiosE2
                    )
                    1 -> EquipoDetalleSeccion(
                        partido = state.partido,
                        nombreEquipo = state.partido?.EQUIPO1 ?: "",
                        jugadores = state.jugadoresE1,
                        cambios = state.cambiosE1,
                        goles = state.goles
                    )
                    2 -> EquipoDetalleSeccion(
                        partido = state.partido,
                        nombreEquipo = state.partido?.EQUIPO2 ?: "",
                        jugadores = state.jugadoresE2,
                        cambios = state.cambiosE2,
                        goles = state.goles
                    )
                }
            }

            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MarcadorGrande(
    partido: Partido, 
    goles1: Int, 
    goles2: Int, 
    tiempoCorriendo: String,
    onEquipo1Click: () -> Unit,
    onEquipo2Click: () -> Unit,
    selectedTab: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    color = if (partido.estaEnCurso()) MaterialTheme.colorScheme.primary else Color.Gray,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = partido.getNumeroDeTiempoEfectivo(),
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Black
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = tiempoCorriendo,
                    fontSize = 42.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (partido.estaEnCurso()) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                    letterSpacing = 2.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Equipo 1 Clicable
                Surface(
                    onClick = onEquipo1Click,
                    modifier = Modifier.weight(1f),
                    color = if (selectedTab == 1) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = partido.EQUIPO1, 
                        modifier = Modifier.padding(8.dp), 
                        textAlign = TextAlign.Center, 
                        fontSize = 16.sp, 
                        fontWeight = if (selectedTab == 1) FontWeight.ExtraBold else FontWeight.Bold, 
                        maxLines = 2,
                        color = if (selectedTab == 1) MaterialTheme.colorScheme.primary else Color.Unspecified
                    )
                }

                Text(
                    text = "$goles1 - $goles2", 
                    fontSize = 54.sp, 
                    fontWeight = FontWeight.Black, 
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                // Equipo 2 Clicable
                Surface(
                    onClick = onEquipo2Click,
                    modifier = Modifier.weight(1f),
                    color = if (selectedTab == 2) MaterialTheme.colorScheme.primary.copy(alpha = 0.1f) else Color.Transparent,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = partido.EQUIPO2, 
                        modifier = Modifier.padding(8.dp), 
                        textAlign = TextAlign.Center, 
                        fontSize = 16.sp, 
                        fontWeight = if (selectedTab == 2) FontWeight.ExtraBold else FontWeight.Bold, 
                        maxLines = 2,
                        color = if (selectedTab == 2) MaterialTheme.colorScheme.primary else Color.Unspecified
                    )
                }
            }
        }
    }
}

@Composable
private fun HistorialSeccion(
    partido: Partido?,
    goles: List<GolEvento>,
    cambios1: List<CambioEvento>,
    cambios2: List<CambioEvento>
) {
    val eventos = remember(goles, cambios1, cambios2, partido) {
        val lista = mutableListOf<EventoPartido>()
        
        lista.addAll(goles.map { gol ->
            val nombreEquipo = when (gol.CODIGOEQUIPO) {
                partido?.CODIGOEQUIPO1 -> partido.EQUIPO1
                partido?.CODIGOEQUIPO2 -> partido.EQUIPO2
                else -> "Equipo desconocido"
            }
            EventoPartido.Gol(gol, nombreEquipo)
        })
        
        lista.addAll(cambios1.map { cambio ->
            EventoPartido.Cambio(cambio, partido?.EQUIPO1 ?: "Equipo 1")
        })
        
        lista.addAll(cambios2.map { cambio ->
            EventoPartido.Cambio(cambio, partido?.EQUIPO2 ?: "Equipo 2")
        })
        
        lista.sortedByDescending { it.fechaAlta }
    }

    if (eventos.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No hay eventos registrados aún", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(eventos) { evento ->
                when (evento) {
                    is EventoPartido.Gol -> EventoGolItem(evento.datos, evento.equipoNombre)
                    is EventoPartido.Cambio -> EventoCambioItem(evento.datos, evento.equipoNombre)
                }
            }
        }
    }
}

@Composable
private fun EquipoDetalleSeccion(
    partido: Partido?,
    nombreEquipo: String,
    jugadores: List<Jugador>,
    cambios: List<CambioEvento>,
    goles: List<GolEvento>
) {
    val codigoEquipo = if (nombreEquipo == partido?.EQUIPO1) partido.CODIGOEQUIPO1 else partido?.CODIGOEQUIPO2
    val golesEquipo = goles.filter { it.CODIGOEQUIPO == codigoEquipo }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        // Bloque de Goles del equipo
        if (golesEquipo.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9).copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("GOLES", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold, color = Color(0xFF2E7D32))
                        golesEquipo.forEach { gol ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("⚽", fontSize = 16.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("${gol.JUGADOR} (${gol.MINUTO}')", style = MaterialTheme.typography.titleSmall)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        // Resumen de cambios
        if (cambios.isNotEmpty()) {
            item {
                Text(
                    text = "Cambios realizados: ${cambios.size}", 
                    style = MaterialTheme.typography.labelMedium, 
                    modifier = Modifier.padding(8.dp),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        item {
            Text(
                text = "PLANTILLA",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(8.dp)
            )
        }

        items(jugadores) { jugador ->
            val salio = cambios.any { it.SALE_CODIGOJUGADOR == jugador.CODIGO }
            val entro = cambios.any { it.ENTRA_CODIGOJUGADOR == jugador.CODIGO }
            
            ListItem(
                headlineContent = { 
                    Text(text = "${jugador.NUMERO} - ${jugador.JUGADOR}", 
                        fontWeight = if (entro) FontWeight.Bold else FontWeight.Normal, 
                        color = if (salio) Color.Gray else Color.Unspecified) 
                },
                supportingContent = { Text(jugador.POSICION) },
                trailingContent = {
                    Row {
                        if (salio) Icon(Icons.Default.ArrowDownward, null, tint = Color.Red, modifier = Modifier.size(20.dp))
                        if (entro) Icon(Icons.Default.ArrowUpward, null, tint = Color(0xFF2E7D32), modifier = Modifier.size(20.dp))
                    }
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
private fun EventoGolItem(gol: GolEvento, equipoNombre: String) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(text = "⚽", fontSize = 28.sp)
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text("¡GOL! - $equipoNombre", fontWeight = FontWeight.Black, color = Color(0xFF2E7D32), style = MaterialTheme.typography.bodySmall)
            Text(gol.JUGADOR, style = MaterialTheme.typography.titleMedium)
        }
        Text("${gol.MINUTO}'", fontWeight = FontWeight.Black, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
private fun EventoCambioItem(cambio: CambioEvento, equipoNombre: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🔄", fontSize = 20.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = equipoNombre, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.weight(1f))
                Text(text = if(cambio.MINUTO_DEL_CAMBIO.isNotEmpty()) "${cambio.MINUTO_DEL_CAMBIO}'" else "", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusTag(text = "SALE", color = Color.Red)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "${cambio.SALE_NUMERO} - ${cambio.SALE_NOMBRE}", style = MaterialTheme.typography.bodyMedium)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        StatusTag(text = "ENTRA", color = Color(0xFF2E7D32))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "${cambio.ENTRA_NUMERO} - ${cambio.ENTRA_NOMBRE}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusTag(text: String, color: Color) {
    Surface(color = color, shape = RoundedCornerShape(4.dp)) {
        Text(text = text, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
    }
}
