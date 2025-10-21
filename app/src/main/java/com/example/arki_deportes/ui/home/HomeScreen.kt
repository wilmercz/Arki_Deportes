package com.example.arki_deportes.ui.home

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
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arki_deportes.data.model.Partido
import com.example.arki_deportes.data.model.PartidoActual
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
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    HomeScreen(
        state = uiState,
        onRefresh = { viewModel.refrescarPartidos() },
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    val swipeState = rememberSwipeRefreshState(isRefreshing = state.isRefreshing)
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(text = "Inicio") },
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
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                when {
                    state.isLoadingLive -> item { LoadingLiveCard() }
                    state.isLive && state.liveMatch != null -> item { LiveMatchCard(state.liveMatch) }
                    state.liveError != null -> item { ErrorMessage(message = state.liveError) }
                }

                if (state.partidos.isNotEmpty()) {
                    item { SectionTitle(text = "Partidos ±7 días") }
                    items(state.partidos) { partido ->
                        PartidoCard(partido = partido)
                    }
                } else if (!state.isRefreshing) {
                    item {
                        if (state.listError != null) {
                            ErrorMessage(message = state.listError)
                        } else {
                            EmptyMatchesMessage()
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LoadingLiveCard() {
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
            Text(
                text = "Cargando partido en vivo…",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.LiveTv,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
                Text(
                    text = "En vivo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TeamScore(
                    nombre = partido.EQUIPO1,
                    goles = partido.GOLES1,
                    modifier = Modifier.weight(1f)
                )
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = partido.getMarcador(),
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        text = partido.getTiempoNormalizado(),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                TeamScore(
                    nombre = partido.EQUIPO2,
                    goles = partido.GOLES2,
                    modifier = Modifier.weight(1f)
                )
            }

            SurfaceStatus(text = partido.getEstadoTexto(), icon = partido.getEstadoIcono())
        }
    }
}

@Composable
private fun TeamScore(nombre: String, goles: Int, modifier: Modifier = Modifier) {
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
            text = goles.toString(),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SurfaceStatus(text: String, icon: String) {
    androidx.compose.material3.Surface(
        color = MaterialTheme.colorScheme.error,
        contentColor = MaterialTheme.colorScheme.onError,
        shape = MaterialTheme.shapes.medium
    ) {
        Text(
            text = "$icon $text",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun PartidoCard(partido: Partido) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "${partido.EQUIPO1.ifBlank { "Por definir" }} vs ${partido.EQUIPO2.ifBlank { "Por definir" }}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = buildString {
                        append(formatFecha(partido.FECHA_PARTIDO))
                        val hora = formatHora(partido.HORA_PARTIDO)
                        if (hora.isNotBlank()) {
                            append(" · ")
                            append(hora)
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (partido.CAMPEONATOTXT.isNotBlank()) {
                Text(
                    text = partido.CAMPEONATOTXT,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            val etapa = Constants.EtapasPartido.getTexto(partido.ETAPA)
            if (partido.ETAPA != Constants.EtapasPartido.NINGUNO) {
                Text(
                    text = etapa,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            if (partido.TRANSMISION) {
                TransmissionBadge()
            }

            if (partido.getMarcador() != "vs") {
                Divider(color = MaterialTheme.colorScheme.surfaceVariant)
                Text(
                    text = "Marcador: ${partido.getMarcador()}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun TransmissionBadge() {
    androidx.compose.material3.Surface(
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        shape = MaterialTheme.shapes.small
    ) {
        Text(
            text = "En transmisión",
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
private fun EmptyMatchesMessage() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "No hay partidos dentro del rango configurado",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Actualiza la base de datos para ver nuevos encuentros",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun ErrorMessage(message: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "⚠️ $message",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Desliza hacia abajo para reintentar",
            style = MaterialTheme.typography.bodySmall,
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
            parsed.format(displayDateFormatter)
        }
    } catch (_: Exception) {
        fecha
    }
}

private fun formatHora(hora: String): String {
    return try {
        if (hora.isBlank()) "" else LocalTime.parse(hora.trim(), DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())).toString()
    } catch (_: Exception) {
        hora
    }
}

private val displayDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd 'de' MMMM", Locale.getDefault())
