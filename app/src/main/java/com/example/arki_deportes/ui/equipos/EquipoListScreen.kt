package com.example.arki_deportes.ui.equipos

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.arki_deportes.data.model.Equipo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipoListRoute(
    viewModel: EquipoListViewModel = viewModel(),
    onNavigateBack: (() -> Unit)? = null,
    onOpenDrawer: (() -> Unit)? = null,
    onCreateEquipo: () -> Unit,
    onEditEquipo: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val pullRefreshState = rememberPullToRefreshState()

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessages()
        }
    }

    if (pullRefreshState.isRefreshing) {
        LaunchedEffect(true) {
            viewModel.refresh()
        }
    }

    LaunchedEffect(uiState.isRefreshing) {
        if (!uiState.isRefreshing) pullRefreshState.endRefresh()
    }

    Scaffold(
        modifier = modifier.nestedScroll(pullRefreshState.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { 
                    Column {
                        Text("Equipos")
                        Text(
                            uiState.campeonatoNombre, 
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                navigationIcon = {
                    if (onOpenDrawer != null) {
                        IconButton(onClick = onOpenDrawer) {
                            Icon(Icons.Default.Menu, "Menú")
                        }
                    } else if (onNavigateBack != null) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onCreateEquipo) {
                Icon(Icons.Default.Add, "Agregar equipo")
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                SearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange
                )

                if (uiState.isLoading && !uiState.isRefreshing) {
                    LoadingState()
                } else {
                    val filteredEquipos = viewModel.getFilteredEquipos()
                    if (filteredEquipos.isEmpty()) {
                        EmptyState(
                            isSearch = uiState.searchQuery.isNotBlank(),
                            onCreateClick = onCreateEquipo
                        )
                    } else {
                        EquiposList(
                            equipos = filteredEquipos,
                            gruposMap = uiState.gruposMap, // Pasamos el mapa de grupos
                            onEdit = onEditEquipo,
                            onDelete = viewModel::deleteEquipo
                        )
                    }
                }
            }
            PullToRefreshContainer(
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}

@Composable
fun SearchBar(query: String, onQueryChange: (String) -> Unit) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        placeholder = { Text("Buscar equipo o provincia...") },
        leadingIcon = { Icon(Icons.Default.Search, null) },
        singleLine = true,
        shape = MaterialTheme.shapes.medium
    )
}

@Composable
fun EquiposList(
    equipos: List<Equipo>,
    gruposMap: Map<String, String>, // Recibimos el mapa
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    LazyColumn(
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(equipos, key = { it.CODIGOEQUIPO }) { equipo ->
            EquipoItem(equipo, gruposMap, onEdit, onDelete) // Lo pasamos al item
        }
    }
}

@Composable
fun EquipoItem(
    equipo: Equipo,
    gruposMap: Map<String, String>, // Recibimos el mapa
    onEdit: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onEdit(equipo.CODIGOEQUIPO) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.Shield,
                contentDescription = null,
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    equipo.getNombreDisplay(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    equipo.PROVINCIA.ifBlank { "Sin provincia" },
                    style = MaterialTheme.typography.bodyMedium
                )
                
                // ✅ Mostramos el nombre legible del grupo usando el mapa
                if (equipo.CODIGOGRUPO.isNotBlank()) {
                    val nombreGrupo = gruposMap[equipo.CODIGOGRUPO] ?: "Grupo asignado"
                    Text(
                        "En grupo: $nombreGrupo",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, null)
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Editar") },
                        onClick = { showMenu = false; onEdit(equipo.CODIGOEQUIPO) },
                        leadingIcon = { Icon(Icons.Default.Edit, null) }
                    )
                    DropdownMenuItem(
                        text = { Text("Eliminar") },
                        onClick = { showMenu = false; showDeleteConfirm = true },
                        leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }
                    )
                }
            }
        }
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Eliminar Equipo") },
            text = { Text("¿Estás seguro de eliminar a ${equipo.EQUIPO}? Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(onClick = { onDelete(equipo.CODIGOEQUIPO); showDeleteConfirm = false }) {
                    Text("Eliminar", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun EmptyState(isSearch: Boolean, onCreateClick: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = if (isSearch) Icons.Default.Search else Icons.Outlined.Groups,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (isSearch) "No se encontraron equipos" else "No hay equipos registrados",
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )
        if (!isSearch) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onCreateClick) {
                Text("Agregar el primer equipo")
            }
        }
    }
}
