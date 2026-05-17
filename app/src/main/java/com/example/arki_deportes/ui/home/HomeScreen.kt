package com.example.arki_deportes.ui.home

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.outlined.Watch
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
import kotlin.text.format
import java.text.SimpleDateFormat


/**
 * Entry point para la pantalla Home enlazado al ViewModel.
 */
@Composable
fun HomeRoute(
    viewModel: HomeViewModel,
    onNavigateToPartidos: () -> Unit,
    onNavigateToCampeonatos: () -> Unit,
    onNavigateToControlPartido: (String, String) -> Unit, // 👈 AÑADIDO
    modifier: Modifier = Modifier,
    onOpenDrawer: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val campeonatoActivo by CampeonatoContext.campeonatoActivo.collectAsState()
    val mostrarAsistente = !uiState.isLive || uiState.forzarBusqueda

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
        onCambiarPartido = { viewModel.toggleBusquedaManual(true) }, // 👈 AÑADIR ESTO
        onCancelarBusqueda = { viewModel.toggleBusquedaManual(false) }, // 👈 AÑADIR ESTO
        onLimpiarAsignacion = { viewModel.limpiarAsignacionManual() },
        campeonatoActivo = campeonatoActivo,
        onNavigateToControlPartido = onNavigateToControlPartido, // 👈 AÑADIDO
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
    onNavigateToControlPartido: (String, String) -> Unit,
    onLimpiarAsignacion: () -> Unit,
    onCambiarPartido: () -> Unit,      // 👈 AÑADIR
    onCancelarBusqueda: () -> Unit,
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
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // A. SI EL USUARIO TIENE UN PARTIDO (SEA CUANDO SEA)
                if (tieneAsignacionEnPerfil && !state.forzarBusqueda) {
                    item {
                        ActiveAssignmentCard(
                            partidoNombre = state.partidosDelCampeonato
                                .firstOrNull { it.CODIGOPARTIDO == usuario?.permisos?.codigoPartido }
                                ?.getNombrePartido() ?: "Cargando partido...",
                            esHoy = state.isLive, // Si es hoy, permite ir al control
                            onNavigateToControl = {
                                val camp = usuario?.permisos?.codigoCampeonato ?: ""
                                val part = usuario?.permisos?.codigoPartido ?: ""
                                onNavigateToControlPartido(camp, part)
                            },
                            onSearchAnother = onCambiarPartido, // Muestra el asistente abajo
                            onRelease = onLimpiarAsignacion // Borra la asignación
                        )
                    }
                }


                // B. SI NO TIENE PARTIDO O QUIERE CAMBIARLO
                if (!tieneAsignacionEnPerfil || state.forzarBusqueda) {
                    item {
                        // 🎯 ORDENAMIENTO TRIPLE: 1. Hoy, 2. Futuros, 3. Pasados
                        val hoy = remember { LocalDate.now() }
                        val partidosOrdenados = remember(state.partidosDelCampeonato) {
                            state.partidosDelCampeonato.sortedWith(
                                compareBy<Partido> {
                                    val pDate = parseFechaSeguro(it.FECHA_PARTIDO)
                                    when {
                                        pDate == hoy -> 0        // Prioridad 0: Hoy
                                        pDate.isAfter(hoy) -> 1  // Prioridad 1: Futuros
                                        else -> 2                // Prioridad 2: Pasados
                                    }
                                }
                                    .thenBy { parseFechaSeguro(it.FECHA_PARTIDO) } // Fecha ASC
                                    .thenBy { it.HORA_PARTIDO }                   // Hora ASC
                            )
                        }

                        AssignmentAssistantPanel(
                            campeonatos = state.campeonatos,
                            partidos = partidosOrdenados, // 👈 Usamos la lista con el nuevo orden
                            campeonatoSeleccionado = campeonatoActivo,
                            onSelectCampeonato = onSelectCampeonato,
                            onAsignarPartido = onAsignarPartido,
                            isLoading = state.isLoadingAsistente,
                            mensaje = state.mensajeAsistente,
                            onCancelar = if (state.forzarBusqueda) onCancelarBusqueda else null
                        )
                    }
                }

            }


        }
    }
}


/**
 * Parsea la fecha de forma segura para comparaciones de ordenamiento.
 */
private fun parseFechaSeguro(fechaStr: String?): LocalDate {
    if (fechaStr.isNullOrBlank()) return LocalDate.MAX
    val formatos = listOf("yyyy-MM-dd", "dd/MM/yyyy", "yyyy/MM/dd", "d/M/yyyy", "dd-MM-yyyy")
    for (formato in formatos) {
        try {
            return LocalDate.parse(fechaStr.trim(), DateTimeFormatter.ofPattern(formato))
        } catch (e: Exception) { continue }
    }
    return LocalDate.MAX // Si el formato no es válido, lo manda al final de la lista
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
    onCancelar: (() -> Unit)? = null,
    isLoading: Boolean,
    mensaje: String?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Al inicio de la Column dentro de AssignmentAssistantPanel
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Asistente de Asignación Rápida",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                if (onCancelar != null) {
                    IconButton(onClick = onCancelar) {
                        Icon(Icons.Default.Close, contentDescription = "Cerrar")
                    }
                }
            }


            Text(
                text = "Primero elige un torneo para ver sus partidos disponibles.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (campeonatos.isEmpty()) {
                Box(modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp), contentAlignment = Alignment.Center) {
                    Text("Cargando campeonatos...", style = MaterialTheme.typography.bodyMedium)
                }
            } else {
                CampeonatoSelector(
                    campeonatos = campeonatos,
                    campeonatoSeleccionado = campeonatoSeleccionado?.CODIGO,
                    onCampeonatoSeleccionado = onSelectCampeonato,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 0.dp)
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
                    Box(modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp))
                    }
                } else if (partidos.isEmpty()) {
                    Text(
                        text = "No se encontraron partidos para hoy o fechas cercanas.",
                        modifier = Modifier
                            .padding(vertical = 16.dp)
                            .fillMaxWidth(),
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
    val hoy = remember { LocalDate.now() }
    val pDate = remember(partido.FECHA_PARTIDO) { parseFechaSeguro(partido.FECHA_PARTIDO) }

    val esHoy = pDate == hoy
    val esFuturo = pDate.isAfter(hoy)
    val esCaducado = pDate.isBefore(hoy)

    // 🎨 LÓGICA DE COLORES (60% Transparencia)
    val itemBgColor = when {
        esHoy -> Color(0xFFE3F2FD).copy(alpha = 0.6f)    // Azul Celeste
        esFuturo -> Color(0xFFE0F2F1).copy(alpha = 0.6f) // Verde Agua
        else -> Color(0xFFFFEBEE).copy(alpha = 0.6f)     // Rojo Suave
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
        border = BorderStroke(
            0.5.dp,
            when {
                esHoy -> Color(0xFF2196F3).copy(alpha = 0.3f)
                esFuturo -> Color(0xFF009688).copy(alpha = 0.3f)
                else -> Color(0xFFF44336).copy(alpha = 0.3f)
            }
        )
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                // 1. Equipos
                // Nombre de equipos (En negrita si es hoy)
                Text(
                    text = "${partido.EQUIPO1} vs ${partido.EQUIPO2}",
                    fontWeight = if (esHoy) FontWeight.ExtraBold else FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (esCaducado) Color.Gray else Color.DarkGray
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
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.CalendarMonth,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (esCaducado) Color.Gray else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    val nombreDia = remember(partido.FECHA_PARTIDO) {
                        obtenerNombreDia(partido.FECHA_PARTIDO)
                    }


                    Text(
                        text = "${if (nombreDia.isNotBlank()) "$nombreDia, " else ""}${partido.FECHA_PARTIDO}",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (esCaducado) Color.Gray else Color.Unspecified
                    )

                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = if (esCaducado) Color.Gray.copy(alpha = 0.5f) else Color(0xFF1976D2),
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Watch,
                                contentDescription = null,
                                modifier = Modifier.size(12.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = partido.HORA_PARTIDO,
                                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                        }
                    }
                }


                // 3. Etiquetas de Estado (OPERADOR / DISPONIBILIDAD / CADUCADO)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    when {
                        esHoy -> StatusTag("HOY", Color(0xFF1976D2))
                        esFuturo -> StatusTag("PRÓXIMO", Color(0xFF388E3C))
                        else -> StatusTag("CADUCADO", Color(0xFFD32F2F))
                    }

                    val op = partido.OPERADOR.trim()
                    if (op.isNotBlank() && op != "NINGUNO") {
                        StatusTag("Op: $op", Color(0xFF5D4037))
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
                        modifier = Modifier.size(32.dp),
                        // 🔵 Azul si es hoy, 🟢 Verde si es futuro
                        tint = if (esHoy) Color(0xFF1976D2) else Color(0xFF388E3C)
                    )
                }
            } else {
                // Mantenemos el icono de bloqueo para los caducados
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = "No disponible",
                    tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                    modifier = Modifier
                        .padding(12.dp)
                        .size(24.dp)
                )
            }
        }
    }
}

private fun obtenerNombreDia(fecha: String): String {
    if (fecha.isBlank()) return ""
    return try {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale("es", "ES"))
        val date = sdf.parse(fecha)
        SimpleDateFormat("EEEE", Locale("es", "ES")).format(date!!).replaceFirstChar { it.uppercase() }
    } catch (e: Exception) {
        try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale("es", "ES"))
            val date = sdf.parse(fecha)
            SimpleDateFormat("EEEE", Locale("es", "ES")).format(date!!).replaceFirstChar { it.uppercase() }
        } catch (e2: Exception) { "" }
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
fun ActiveAssignmentCard(
    partidoNombre: String,
    esHoy: Boolean,
    onNavigateToControl: () -> Unit,
    onSearchAnother: () -> Unit,
    onRelease: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (esHoy) Color(0xFF1B5E20) else Color(0xFF455A64) // Verde si es hoy, Gris si es otro día
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = if (esHoy) "🔴 PARTIDO EN VIVO ASIGNADO" else "📅 ASIGNACIÓN PENDIENTE",
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.labelSmall
            )
            Text(partidoNombre, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)

            Spacer(Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (esHoy) {
                    Button(
                        onClick = onNavigateToControl,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                    ) { Text("CONTROLAR AHORA") }
                }

                OutlinedButton(
                    onClick = onSearchAnother,
                    border = BorderStroke(1.dp, Color.White),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) { Text("CAMBIAR") }

                IconButton(onClick = onRelease) {
                    Icon(Icons.Default.DeleteForever, "Liberar", tint = Color.White)
                }
            }
        }
    }
}

