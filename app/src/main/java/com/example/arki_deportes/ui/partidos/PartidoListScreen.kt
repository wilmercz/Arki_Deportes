package com.example.arki_deportes.ui.partidos

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.arki_deportes.data.model.Partido
import com.example.arki_deportes.utils.Constants
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import androidx.compose.material.icons.filled.CheckCircle


/**
 * ═══════════════════════════════════════════════════════════════════════════
 * PARTIDO LIST SCREEN - PANTALLA DE LISTA DE PARTIDOS
 * ═══════════════════════════════════════════════════════════════════════════
 */

@Composable
fun PartidoListRoute(
    viewModel: PartidoListViewModel = viewModel(),
    onNavigateBack: (() -> Unit)? = null,
    onOpenDrawer: (() -> Unit)? = null,
    onCreatePartido: () -> Unit,
    onEditPartido: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    PartidoListScreen(
        uiState = uiState,
        onRefresh = { viewModel.refresh() },
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onDeletePartido = viewModel::deletePartido,
        onConsolidarPartido = viewModel::consolidarPartido,
        getFilteredPartidos = viewModel::getFilteredPartidos,
        onNavigateBack = onNavigateBack,
        onOpenDrawer = onOpenDrawer,
        onCreatePartido = onCreatePartido,
        onEditPartido = onEditPartido,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartidoListScreen(
    uiState: PartidoListUiState,
    onRefresh: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onDeletePartido: (String) -> Unit,
    onConsolidarPartido: (String, Int, Int) -> Unit,
    getFilteredPartidos: () -> List<Partido>,
    onNavigateBack: (() -> Unit)?,
    onOpenDrawer: (() -> Unit)?,
    onCreatePartido: () -> Unit,
    onEditPartido: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = uiState.isRefreshing)
    
    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            snackbarHostState.showSnackbar(error)
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "Partidos") },
                navigationIcon = {
                    when {
                        onOpenDrawer != null -> {
                            IconButton(onClick = onOpenDrawer) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = "Abrir menú"
                                )
                            }
                        }
                        onNavigateBack != null -> {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Regresar"
                                )
                            }
                        }
                    }
                },
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreatePartido,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Crear partido"
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = onRefresh,
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                SearchBar(
                    searchQuery = uiState.searchQuery,
                    onSearchQueryChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                when {
                    uiState.isLoading -> {
                        LoadingState()
                    }
                    uiState.partidos.isEmpty() -> {
                        EmptyPartidosState(onCreatePartido = onCreatePartido)
                    }
                    else -> {
                        val filteredPartidos = getFilteredPartidos()
                        
                        if (filteredPartidos.isEmpty()) {
                            EmptySearchState()
                        } else {
                            PartidosList(
                                partidos = filteredPartidos,
                                onEditPartido = onEditPartido,
                                onDeletePartido = onDeletePartido,
                                onConsolidarPartido = onConsolidarPartido
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        modifier = modifier,
        placeholder = { Text("Buscar partidos...") },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null
            )
        },
        singleLine = true
    )
}

@Composable
private fun PartidosList(
    partidos: List<Partido>,
    onEditPartido: (String) -> Unit,
    onDeletePartido: (String) -> Unit,
    onConsolidarPartido: (String, Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = partidos,
            key = { it.CODIGOPARTIDO }
        ) { partido ->
            PartidoCard(
                partido = partido,
                onEdit = { onEditPartido(partido.CODIGOPARTIDO) },
                onDelete = { onDeletePartido(partido.CODIGOPARTIDO) },
                onConsolidar = { g1, g2 -> onConsolidarPartido(partido.CODIGOPARTIDO, g1, g2) }
            )
        }
    }
}

@Composable
private fun PartidoCard(
    partido: Partido,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onConsolidar: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showConsolidarDialog by remember { mutableStateOf(false) }

    val esFinalizado = partido.ESTADO == 1
    val colorTitulo = if (esFinalizado) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEdit() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    // Título con Goles
                    Text(
                        text = buildString {
                            append(partido.EQUIPO1.ifBlank { "Por definir" })
                            append(" (${partido.GOLES1})")
                            append(" vs ")
                            append("(${partido.GOLES2}) ")
                            append(partido.EQUIPO2.ifBlank { "Por definir" })
                        },
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = colorTitulo
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))

                    // Etiqueta de Estado
                    Surface(
                        color = if (esFinalizado) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                        contentColor = if (esFinalizado) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onPrimaryContainer,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = if (esFinalizado) "FINALIZADO" else "POR JUGARSE",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Opciones"
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Editar") },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = { Icon(Icons.Default.Edit, null) }
                        )

                        DropdownMenuItem(
                            text = { Text("Finalizar y Consolidar") },
                            onClick = {
                                showMenu = false
                                showConsolidarDialog = true
                            },
                            leadingIcon = { Icon(Icons.Default.CheckCircle, null) }
                        )

                        DropdownMenuItem(
                            text = { Text("Eliminar") },
                            onClick = {
                                showMenu = false
                                showDeleteDialog = true
                            },
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (partido.CAMPEONATOTXT.isNotBlank()) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = partido.CAMPEONATOTXT,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(4.dp))
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = buildString {
                        append(formatFecha(partido.FECHA_PARTIDO))
                        if (partido.HORA_PARTIDO.isNotBlank()) {
                            append(" · ")
                            append(partido.HORA_PARTIDO)
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (partido.ETAPA != Constants.EtapasPartido.NINGUNO) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = Constants.EtapasPartido.getTexto(partido.ETAPA),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }


    //  dialogos
    if (showConsolidarDialog) {
        ConsolidarMarcadorDialog(
            partido = partido,
            onConfirm = { g1, g2 ->
                showConsolidarDialog = false
                onConsolidar(g1, g2)
            },
            onDismiss = { showConsolidarDialog = false }
        )
    }

    if (showDeleteDialog) {
        DeleteConfirmationDialog(
            partidoDescripcion = "${partido.EQUIPO1} vs ${partido.EQUIPO2}",
            onConfirm = {
                showDeleteDialog = false
                onDelete()
            },
            onDismiss = { showDeleteDialog = false }
        )
    }
}


@Composable
fun ConsolidarMarcadorDialog(
    partido: Partido,
    onConfirm: (Int, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var g1 by remember { mutableStateOf(partido.GOLES1.toString()) }
    var g2 by remember { mutableStateOf(partido.GOLES2.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Pitido Final: ${partido.EQUIPO1} vs ${partido.EQUIPO2}") },
        text = {
            Column {
                Text("Confirma el marcador final para actualizar la tabla de posiciones:")
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 16.dp)) {
                    OutlinedTextField(value = g1, onValueChange = { g1 = it }, label = { Text(partido.EQUIPO1) }, modifier = Modifier.weight(1f))
                    Text("-", Modifier.padding(horizontal = 8.dp))
                    OutlinedTextField(value = g2, onValueChange = { g2 = it }, label = { Text(partido.EQUIPO2) }, modifier = Modifier.weight(1f))
                }
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(g1.toIntOrNull() ?: 0, g2.toIntOrNull() ?: 0) }) {
                Text("Confirmar y Actualizar Tabla")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

@Composable
private fun DeleteConfirmationDialog(
    partidoDescripcion: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Eliminar partido") },
        text = {
            Text("¿Estás seguro de que deseas eliminar el partido \"$partidoDescripcion\"? Esta acción no se puede deshacer.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Eliminar", color = MaterialTheme.colorScheme.error)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
private fun LoadingState() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun EmptyPartidosState(onCreatePartido: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No hay partidos registrados",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Crea tu primer partido para comenzar",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        TextButton(onClick = onCreatePartido) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text("Crear partido")
        }
    }
}

@Composable
private fun EmptySearchState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No se encontraron partidos",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Intenta con otros términos de búsqueda",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

private fun formatFecha(fecha: String): String {
    return try {
        if (fecha.isBlank()) {
            "Fecha por confirmar"
        } else {
            val parsed = LocalDate.parse(fecha.trim(), DateTimeFormatter.ISO_LOCAL_DATE)
            parsed.format(DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault()))
        }
    } catch (_: Exception) {
        fecha
    }
}
