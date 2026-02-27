package com.example.arki_deportes.ui.envivo

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arki_deportes.data.model.PartidoEnVivo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartidosEnVivoScreen(
    viewModel: PartidosEnVivoViewModel,
    onPartidoClick: (String, String) -> Unit, // campeonatoId, partidoId
    onOpenDrawer: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Partidos En Vivo") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Abrir menú")
                    }
                }
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (state.partidos.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No hay partidos jugándose en este momento.")
            }
        } else {
            LazyColumn(modifier = Modifier.padding(padding)) {
                items(state.partidos) { partido ->
                    PartidoVivoItem(partido, onClick = {
                        onPartidoClick(partido.CODIGOCAMPEONATO, partido.CODIGOPARTIDO)
                    })
                }
            }
        }
    }
}

@Composable
private fun PartidoVivoItem(partido: PartidoEnVivo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(partido.DEPORTE, style = MaterialTheme.typography.labelSmall)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(partido.EQUIPO1, fontWeight = FontWeight.Bold)
                Text("${partido.GOLES1} - ${partido.GOLES2}", fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text(partido.EQUIPO2, fontWeight = FontWeight.Bold)
            }
            Text("En juego: ${partido.HORA_PLAY}", style = MaterialTheme.typography.bodySmall)
        }
    }
}

