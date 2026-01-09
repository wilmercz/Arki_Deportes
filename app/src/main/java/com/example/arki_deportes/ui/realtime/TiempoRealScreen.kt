package com.example.arki_deportes.ui.realtime

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.width
import com.example.arki_deportes.data.model.PartidoActual
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

//PANTALLA POR MEJORAR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TiempoRealRoute(
    viewModel: TiempoRealViewModel,
    onNavigateBack: (() -> Unit)? = null,
    onOpenDrawer: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    TiempoRealScreen(
        state = state,
        onNavigateBack = onNavigateBack,
        onOpenDrawer = onOpenDrawer,
        onRetry = viewModel::reintentar,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TiempoRealScreen(
    state: TiempoRealUiState,
    onNavigateBack: (() -> Unit)? = null,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenDrawer: (() -> Unit)? = null
) {
    val topAppBarState = rememberTopAppBarState()
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "Tiempo real") },
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
                            TextButton(onClick = onNavigateBack) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = null
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Volver")
                                }
                            }
                        }
                    }
                },
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(topAppBarState)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            when {
                state.isLoading -> {
                    item { LoadingCard() }
                }
                state.error != null -> {
                    item { ErrorCard(message = state.error, onRetry = onRetry) }
                }
                state.partido != null -> {
                    item { LiveScoreCard(partido = state.partido, isLive = state.isLive) }
                }
                else -> {
                    item { EmptyStateCard() }
                }
            }
        }
    }
}

@Composable
private fun LoadingCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator()
                Text(text = "Cargando información en vivo…")
            }
        }
    }
}

@Composable
private fun ErrorCard(message: String, onRetry: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "⚠️ Error al cargar el partido en vivo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(text = message)
            Button(onClick = onRetry) {
                Text("Reintentar")
            }
        }
    }
}

@Composable
private fun EmptyStateCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "No hay partido en vivo",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Cuando el nodo PartidoActual tenga datos, aparecerán aquí automáticamente.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun LiveScoreCard(partido: PartidoActual, isLive: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        text = partido.getDeporteTexto(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = partido.getEstadoTexto(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (isLive) {
                    Surface(
                        color = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                        shape = MaterialTheme.shapes.small
                    ) {
                        Text(
                            text = "En vivo",
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ScoreTeam(
                    nombre = partido.EQUIPO1,
                    valor = partido.GOLES1,
                    etiqueta = partido.getAnotacionesLabel(),
                    modifier = Modifier.weight(1f)
                )
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = partido.getMarcadorLabel(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = partido.getMarcador(),
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = partido.getTiempoLabel(),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = partido.TIEMPO_TRANSCURRIDO.ifBlank { "00:00" },
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }
                ScoreTeam(
                    nombre = partido.EQUIPO2,
                    valor = partido.GOLES2,
                    etiqueta = partido.getAnotacionesLabel(),
                    modifier = Modifier.weight(1f)
                )
            }

            if (partido.muestraEstadisticasDisciplina()) {
                DisciplinaryRow(partido)
            }

            val ultimaActualizacion = rememberFormattedTimestamp(partido.ULTIMA_ACTUALIZACION)
            if (ultimaActualizacion != null) {
                Text(
                    text = "Última actualización: $ultimaActualizacion",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ScoreTeam(
    nombre: String,
    valor: Int,
    etiqueta: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = nombre.ifBlank { "Por confirmar" },
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = valor.toString(),
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelMedium,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun DisciplinaryRow(partido: PartidoActual) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Tarjetas",
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = partido.EQUIPO1.ifBlank { "Equipo 1" }, fontWeight = FontWeight.SemiBold)
                Text(text = "🟨 ${partido.TARJETAS_AMARILLAS1}")
                Text(text = "🟥 ${partido.TARJETAS_ROJAS1}")
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = partido.EQUIPO2.ifBlank { "Equipo 2" }, fontWeight = FontWeight.SemiBold)
                Text(text = "🟨 ${partido.TARJETAS_AMARILLAS2}")
                Text(text = "🟥 ${partido.TARJETAS_ROJAS2}")
            }
        }
    }
}

@Composable
private fun rememberFormattedTimestamp(timestamp: Long): String? {
    if (timestamp <= 0) return null
    val formatter = remember(timestamp) {
        DateTimeFormatter.ofPattern("dd MMM yyyy · HH:mm", Locale.getDefault())
    }
    val dateTime = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault())
    return formatter.format(dateTime)
}
