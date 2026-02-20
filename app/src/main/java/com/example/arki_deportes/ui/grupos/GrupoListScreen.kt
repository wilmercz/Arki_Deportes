package com.example.arki_deportes.ui.grupos

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.arki_deportes.data.model.Equipo
import com.example.arki_deportes.data.model.Grupo
import com.example.arki_deportes.data.model.Serie
import com.example.arki_deportes.data.model.placeLabel
import com.example.arki_deportes.data.model.placeEmoji
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * GRUPO LIST SCREEN - GESTIÓN DE EQUIPOS POR GRUPO (CORREGIDA)
 * ═══════════════════════════════════════════════════════════════════════════
 */

@Composable
fun GrupoListRoute(
    viewModel: GrupoListViewModel = viewModel(),
    onNavigateBack: (() -> Unit)? = null,
    onOpenDrawer: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    GrupoListScreen(
        uiState = uiState,
        onRefresh = { viewModel.refresh() },
        onSearchQueryChange = viewModel::onSearchQueryChange,
        onSerieSeleccionada = viewModel::onSerieSeleccionada,
        onGrupoSeleccionado = viewModel::onGrupoSeleccionado,
        onShowAddDialog = viewModel::showAddEquipoDialog,
        onHideAddDialog = viewModel::hideAddEquipoDialog,
        onAddEquipoToGrupo = viewModel::addEquipoToGrupo,
        onDeleteEquipoFromGrupo = viewModel::deleteEquipoFromGrupo,
        onClearMessages = viewModel::clearMessages,
        getFilteredGrupos = viewModel::getFilteredGrupos,
        getEquiposNoEnGrupo = viewModel::getEquiposNoEnGrupo,
        onNavigateBack = onNavigateBack,
        onOpenDrawer = onOpenDrawer,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrupoListScreen(
    uiState: GrupoListUiState,
    onRefresh: () -> Unit,
    onSearchQueryChange: (String) -> Unit,
    onSerieSeleccionada: (Serie) -> Unit,
    onGrupoSeleccionado: (String) -> Unit,
    onShowAddDialog: () -> Unit,
    onHideAddDialog: () -> Unit,
    onAddEquipoToGrupo: (Equipo) -> Unit,
    onDeleteEquipoFromGrupo: (Grupo) -> Unit,
    onClearMessages: () -> Unit,
    getFilteredGrupos: () -> List<Grupo>,
    getEquiposNoEnGrupo: () -> List<Equipo>,
    onNavigateBack: (() -> Unit)?,
    onOpenDrawer: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val snackbarHostState = remember { SnackbarHostState() }
    
    // ✅ Corregido el error de isRefreshing
    val swipeRefreshState = rememberSwipeRefreshState(isRefreshing = uiState.isLoading)

    LaunchedEffect(uiState.errorMessage, uiState.successMessage) {
        uiState.errorMessage?.let { snackbarHostState.showSnackbar(it); onClearMessages() }
        uiState.successMessage?.let { snackbarHostState.showSnackbar(it); onClearMessages() }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text(text = "Asignación de Grupos")
                        Text(text = uiState.campeonatoNombre, style = MaterialTheme.typography.labelSmall)
                    }
                },
                navigationIcon = {
                    if (onOpenDrawer != null) {
                        IconButton(onClick = onOpenDrawer) { Icon(Icons.Filled.Menu, "Menú") }
                    } else if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Atrás") }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onShowAddDialog) {
                Icon(Icons.Default.Add, "Asignar Equipo")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        SwipeRefresh(
            state = swipeRefreshState,
            onRefresh = onRefresh,
            modifier = Modifier.fillMaxSize().padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                SerieSelector(
                    serieSeleccionada = uiState.serieSeleccionada,
                    seriesDisponibles = uiState.seriesDisponibles,
                    onSerieSeleccionada = onSerieSeleccionada,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                )

                GrupoSelector(
                    grupoSeleccionado = uiState.grupoSeleccionado,
                    gruposDisponibles = uiState.gruposDisponibles,
                    onGrupoSeleccionado = onGrupoSeleccionado,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp)
                )

                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    placeholder = { Text("Buscar en el grupo...") },
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    singleLine = true
                )

                if (uiState.isLoading) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
                } else {
                    val filtered = getFilteredGrupos()
                    if (filtered.isEmpty()) {
                        EmptyState(uiState.grupoSeleccionado)
                    } else {
                        GruposList(
                            grupos = filtered,
                            grupoSeleccionado = uiState.grupoSeleccionado,
                            onDeleteEquipo = onDeleteEquipoFromGrupo
                        )
                    }
                }
            }
        }
    }

    if (uiState.showAddDialog) {
        AddEquipoDialog(
            equiposDisponibles = getEquiposNoEnGrupo(),
            grupoSeleccionado = uiState.grupoSeleccionado,
            isLoading = uiState.isAddingEquipo,
            onDismiss = onHideAddDialog,
            onConfirm = onAddEquipoToGrupo
        )
    }
}

@Composable
private fun GruposList(
    grupos: List<Grupo>,
    grupoSeleccionado: String,
    onDeleteEquipo: (Grupo) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Text(
                text = "EQUIPOS ASIGNADOS - GRUPO $grupoSeleccionado",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // ✅ Usamos itemsIndexed para obtener el contador automático
        itemsIndexed(grupos) { index, grupo ->
            GrupoCard(
                numeroItem = index + 1, // Pasamos el contador (índice + 1)
                grupo = grupo,
                onDelete = { onDeleteEquipo(grupo) }
            )
        }
    }
}

@Composable
private fun GrupoCard(
    numeroItem: Int, // ✅ Nuevo parámetro para el contador
    grupo: Grupo,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val nombreEquipo = grupo.obtenerNombreEquipo()
    val codigoEquipo = grupo.obtenerCodigoEquipo()

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // ✅ Ahora muestra el número de item (1, 2, 3...) en lugar de grupo.POSICION
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "#",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = numeroItem.toString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nombreEquipo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                if (codigoEquipo.isNotBlank() && codigoEquipo != "0") {
                    Text(
                        text = "ID: $codigoEquipo",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = { showMenu = true }) {
                Icon(Icons.Default.MoreVert, null)
            }

            DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                DropdownMenuItem(
                    text = { Text("Quitar del grupo") },
                    onClick = { showMenu = false; showDeleteDialog = true },
                    leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Quitar equipo") },
            text = { Text("¿Deseas quitar a $nombreEquipo del grupo?") },
            confirmButton = {
                TextButton(onClick = { showDeleteDialog = false; onDelete() }) {
                    Text("Quitar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun EmptyState(letra: String) {
    Box(Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(Icons.Default.Groups, null, Modifier.size(64.dp), MaterialTheme.colorScheme.outline)
            Spacer(Modifier.height(16.dp))
            Text("Grupo $letra vacío", style = MaterialTheme.typography.titleMedium)
            Text("Usa el botón + para empezar", textAlign = TextAlign.Center)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SerieSelector(serieSeleccionada: Serie?, seriesDisponibles: List<Serie>, onSerieSeleccionada: (Serie) -> Unit, modifier: Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Card(modifier = modifier) {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            TextField(
                value = serieSeleccionada?.NOMBRESERIE ?: "Seleccione Serie",
                onValueChange = {}, readOnly = true, label = { Text("Serie") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                seriesDisponibles.forEach { serie ->
                    DropdownMenuItem(text = { Text(serie.NOMBRESERIE) }, onClick = { onSerieSeleccionada(serie); expanded = false })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GrupoSelector(grupoSeleccionado: String, gruposDisponibles: List<String>, onGrupoSeleccionado: (String) -> Unit, modifier: Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Card(modifier = modifier) {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }, modifier = Modifier.fillMaxWidth().padding(8.dp)) {
            TextField(
                value = "Grupo $grupoSeleccionado",
                onValueChange = {}, readOnly = true, label = { Text("Letra de Grupo") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                modifier = Modifier.menuAnchor().fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                gruposDisponibles.forEach { letra ->
                    DropdownMenuItem(text = { Text("Grupo $letra") }, onClick = { onGrupoSeleccionado(letra); expanded = false })
                }
            }
        }
    }
}

@Composable
private fun AddEquipoDialog(equiposDisponibles: List<Equipo>, grupoSeleccionado: String, isLoading: Boolean, onDismiss: () -> Unit, onConfirm: (Equipo) -> Unit) {
    var selectedEquipo by remember { mutableStateOf<Equipo?>(null) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Asignar a Grupo $grupoSeleccionado") },
        text = {
            Column {
                if (equiposDisponibles.isEmpty()) {
                    Text("No hay equipos disponibles para asignar.")
                } else {
                    LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                        items(equiposDisponibles) { equipo ->
                            Card(
                                onClick = { selectedEquipo = equipo },
                                colors = CardDefaults.cardColors(containerColor = if (selectedEquipo == equipo) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant),
                                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(text = equipo.getNombreDisplay(), fontWeight = FontWeight.Bold)
                                    Text(text = equipo.PROVINCIA, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
                if (isLoading) LinearProgressIndicator(modifier = Modifier.fillMaxWidth().padding(top = 8.dp))
            }
        },
        confirmButton = { Button(onClick = { selectedEquipo?.let { onConfirm(it) } }, enabled = selectedEquipo != null && !isLoading) { Text("Añadir") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
