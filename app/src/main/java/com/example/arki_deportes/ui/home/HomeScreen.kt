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
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
    modifier: Modifier = Modifier,
    onOpenDrawer: (() -> Unit)? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    HomeScreen(
        state = uiState,
        onRefresh = { viewModel.refrescarPartidos() },
        modifier = modifier,
        onOpenDrawer = onOpenDrawer
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    state: HomeUiState,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    onOpenDrawer: (() -> Unit)? = null
) {
    val swipeState = rememberSwipeRefreshState(isRefreshing = state.isRefreshing)
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text(text = "Inicio") },
                navigationIcon = {
                    onOpenDrawer?.let { open ->
                        IconButton(onClick = open) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Abrir menú"
                            )
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
                contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // ─────────────────────────────────────────────────────────
                // Partido en vivo
                // ─────────────────────────────────────────────────────────
                when {
                    state.isLoadingLive -> item { LoadingLiveCard() }
                    state.isLive && state.liveMatch != null -> item { LiveMatchCard(state.liveMatch) }
                    state.liveError != null -> item { ErrorMessage(message = state.liveError) }
                }

                // ─────────────────────────────────────────────────────────
                // Lista de partidos
                // ─────────────────────────────────────────────────────────
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
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

            Text(
                text = partido.getDeporteTexto(),
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                TeamScore(
                    nombre = partido.EQUIPO1,
                    valor = partido.GOLES1,
                    etiqueta = partido.getAnotacionesLabel(),
                    modifier = Modifier.weight(1f)
                )

                Column(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = partido.getMarcadorLabel(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = partido.getMarcador(),
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Black
                    )

                    Text(
                        text = partido.getTiempoLabel(),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = partido.getTiempoNormalizado(),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                }

                TeamScore(
                    nombre = partido.EQUIPO2,
                    valor = partido.GOLES2,
                    etiqueta = partido.getAnotacionesLabel(),
                    modifier = Modifier.weight(1f)
                )
            }

            SurfaceStatus(text = partido.getEstadoTexto(), icon = partido.getEstadoIcono())

            if (partido.muestraEstadisticasDisciplina()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.3f))
                DisciplinaryStats(partido)
            }
        }
    }
}

@Composable
private fun TeamScore(nombre: String, valor: Int, etiqueta: String, modifier: Modifier = Modifier) {
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
            style = MaterialTheme.typography.headlineMedium,
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
private fun SurfaceStatus(text: String, icon: String) {
    Surface(
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
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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

            Text(
                text = partido.getDeporteTexto(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
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
                        append(formatFecha(partido.FECHA))
                        val hora = formatHora(partido.HORA_PLAY) // no hay HORA_PARTIDO en el modelo
                        if (hora.isNotBlank()) {
                            append(" · ")
                            append(hora)
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            val tiempoJuego = partido.getTiempoJuegoDescripcion()
            if (tiempoJuego.isNotBlank()) {
                Text(
                    text = "${partido.getTiempoJuegoLabel()}: $tiempoJuego",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Etapa (usa el campo real Etapa)
            val etapaTexto = Constants.EtapasPartido.getTexto(partido.ETAPA)
            if (partido.ETAPA != Constants.EtapasPartido.NINGUNO) {
                Text(
                    text = etapaTexto,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            // Marcador
            if (partido.getMarcador() != "vs") {
                HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant)
                Text(
                    text = "${partido.getMarcadorLabel()}: ${partido.getMarcador()}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun DisciplinaryStats(partido: PartidoActual) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
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

/**
 * Fecha tolerante:
 * - intenta ISO yyyy-MM-dd
 * - intenta dd/MM/yyyy
 * - si falla, devuelve el texto original o "Fecha por confirmar"
 */
private fun formatFecha(fecha: String?): String {
    val raw = fecha?.trim().orEmpty()
    if (raw.isBlank()) return "Fecha por confirmar"

    // 1) ISO_LOCAL_DATE
    try {
        val parsed = LocalDate.parse(raw, DateTimeFormatter.ISO_LOCAL_DATE)
        return parsed.format(displayDateFormatter)
    } catch (_: Exception) {}

    // 2) dd/MM/yyyy
    try {
        val df = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.getDefault())
        val parsed = LocalDate.parse(raw, df)
        return parsed.format(displayDateFormatter)
    } catch (_: Exception) {}

    return raw
}

/**
 * Hora tolerante:
 * - HORA_PLAY suele venir tipo "15-30-0"
 * - o puede venir "HH:mm"
 */
private fun formatHora(hora: String?): String {
    val raw = hora?.trim().orEmpty()
    if (raw.isBlank()) return ""

    // Si viene "15-30-0"
    if (raw.contains("-")) {
        val parts = raw.split("-")
        if (parts.size >= 2) {
            val hh = parts[0].padStart(2, '0')
            val mm = parts[1].padStart(2, '0')
            return "$hh:$mm"
        }
        return raw
    }

    // Si viene "HH:mm"
    return try {
        LocalTime.parse(raw, DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())).toString()
    } catch (_: Exception) {
        raw
    }
}

private val displayDateFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("dd 'de' MMMM", Locale.getDefault())

// ─────────────────────────────────────────────────────────────────────────────
// Extensions UI para Partido (sin tocar el modelo)
// ─────────────────────────────────────────────────────────────────────────────

private fun Partido.getDeporteTexto(): String = "Fútbol"

private fun Partido.getMarcadorLabel(): String = "Marcador"

private fun Partido.getMarcador(): String {
    val g1 = GOLES1  // ← Int directo (sin .trim())
    val g2 = GOLES2  // ← Int directo (sin .trim())
    return if (TIEMPOSJUGADOS == 0 && g1 == 0 && g2 == 0) "vs" else "$g1 - $g2"
    //                                      ↑ Comparar con 0 (Int) no "0" (String)
}

private fun Partido.getTiempoJuegoLabel(): String = "Tiempo"

private fun Partido.getTiempoJuegoDescripcion(): String {
    val t = TIEMPOJUEGO.trim()
    return if (t.isBlank() || t == "00:00") "" else t
}
