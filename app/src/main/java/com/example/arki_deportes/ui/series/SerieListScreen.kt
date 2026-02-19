package com.example.arki_deportes.ui.series

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SerieListScreen(
    campeonatoId: String,
    viewModel: SerieListViewModel = viewModel(),
    onAddSerie: () -> Unit,
    onEditSerie: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(campeonatoId) {
        viewModel.loadSeries(campeonatoId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Series del Campeonato") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Regresar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddSerie) {
                Icon(Icons.Default.Add, contentDescription = "Añadir Serie")
            }
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.series.isEmpty()) {
                Text(
                    text = "No hay series configuradas.\nPulsa + para crear la primera.",
                    modifier = Modifier.align(Alignment.Center).padding(16.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.series) { serie ->
                        ListItem(
                            headlineContent = { Text(serie.NOMBRESERIE) },
                            supportingContent = { Text(serie.getReglaTexto()) },
                            trailingContent = { Icon(Icons.Default.ChevronRight, contentDescription = null) },
                            modifier = Modifier.clickable { onEditSerie(serie.CODIGOSERIE) }
                        )
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
                    }
                }
            }
        }
    }
}