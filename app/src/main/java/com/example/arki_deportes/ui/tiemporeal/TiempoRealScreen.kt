package com.example.arki_deportes.ui.tiemporeal

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Sports
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.arki_deportes.ui.theme.Arki_DeportesTheme

@Composable
fun TiempoRealScreen(
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null,
    viewModel: TiempoRealViewModel = viewModel(factory = TiempoRealViewModel.Factory)
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by rememberSaveable { mutableStateOf(TiempoRealTab.Marcador) }

    TiempoRealContent(
        uiState = uiState,
        selectedTab = selectedTab,
        onTabSelected = { selectedTab = it },
        onQuickAction = viewModel::onQuickAction,
        onNavigateBack = onNavigateBack,
        modifier = modifier
    )
}

@Composable
private fun TiempoRealContent(
    uiState: TiempoRealUiState,
    selectedTab: TiempoRealTab,
    onTabSelected: (TiempoRealTab) -> Unit,
    onQuickAction: (TiempoRealQuickAction) -> Unit,
    onNavigateBack: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        TiempoRealHeader(
            marcador = uiState.marcador,
            ultimaActualizacion = uiState.ultimaActualizacionTexto,
            onNavigateBack = onNavigateBack
        )

        if (uiState.isLoading) {
            LinearProgressIndicator(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }

        uiState.errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                tonalElevation = 0.dp,
                shadowElevation = 0.dp
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(12.dp)
                )
            }
        }

        val tabs = remember { TiempoRealTab.entries.toList() }
        Spacer(modifier = Modifier.height(16.dp))
        TabRow(
            selectedTabIndex = tabs.indexOf(selectedTab).coerceAtLeast(0),
            modifier = Modifier.fillMaxWidth()
        ) {
            tabs.forEach { tab ->
                Tab(
                    selected = tab == selectedTab,
                    onClick = { onTabSelected(tab) },
                    text = { Text(tab.title) }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            when (selectedTab) {
                TiempoRealTab.Marcador -> MarcadorSection(
                    marcador = uiState.marcador,
                    recentActions = uiState.recentActions,
                    onQuickAction = onQuickAction
                )

                TiempoRealTab.Estadisticas -> EstadisticasSection(
                    marcador = uiState.marcador,
                    estadisticas = uiState.estadisticas
                )

                TiempoRealTab.Alineaciones -> AlineacionesSection(
                    marcador = uiState.marcador,
                    alineaciones = uiState.alineaciones
                )
            }
        }
    }
}

@Composable
private fun TiempoRealHeader(
    marcador: MarcadorUi,
    ultimaActualizacion: String?,
    onNavigateBack: (() -> Unit)?
) {
    val equipoLocal = marcador.equipoLocal.ifBlank { "Equipo 1" }
    val equipoVisitante = marcador.equipoVisitante.ifBlank { "Equipo 2" }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 20.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onNavigateBack != null) {
                IconButton(onClick = onNavigateBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Volver"
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }
            Column(modifier = Modifier.weight(1f, fill = false)) {
                Text(
                    text = "Panel en vivo",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                ultimaActualizacion?.let {
                    Text(
                        text = "Última actualización: $it",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "${marcador.estadoIcono} ${marcador.estado}".trim(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    EquipoResumen(nombre = equipoLocal, goles = marcador.golesLocal)
                    CronometroChip(
                        tiempo = marcador.cronometro,
                        cronometrando = marcador.cronometrando
                    )
                    EquipoResumen(nombre = equipoVisitante, goles = marcador.golesVisitante)
                }
            }
        }
    }
}

@Composable
private fun EquipoResumen(nombre: String, goles: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = nombre,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = goles.toString(),
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CronometroChip(tiempo: String, cronometrando: Boolean) {
    val chipColor = if (cronometrando) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = if (cronometrando) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    AssistChip(
        onClick = {},
        label = {
            Text(
                text = tiempo,
                style = MaterialTheme.typography.titleMedium
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Schedule,
                contentDescription = null,
                tint = contentColor
            )
        },
        colors = androidx.compose.material3.AssistChipDefaults.assistChipColors(
            containerColor = chipColor,
            labelColor = contentColor
        )
    )
}

@Composable
private fun MarcadorSection(
    marcador: MarcadorUi,
    recentActions: List<String>,
    onQuickAction: (TiempoRealQuickAction) -> Unit
) {
    val equipoLocal = marcador.equipoLocal.ifBlank { "Equipo 1" }
    val equipoVisitante = marcador.equipoVisitante.ifBlank { "Equipo 2" }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Marcador",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                EquipoResumen(nombre = equipoLocal, goles = marcador.golesLocal)
                CronometroChip(
                    tiempo = marcador.cronometro,
                    cronometrando = marcador.cronometrando
                )
                EquipoResumen(nombre = equipoVisitante, goles = marcador.golesVisitante)
            }

            if (marcador.mostrarPenales) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Penales",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "${marcador.penalesLocal ?: 0}",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${marcador.penalesVisitante ?: 0}",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                EquipoDisciplinaColumn(
                    titulo = equipoLocal,
                    amarillas = marcador.amarillasLocal,
                    rojas = marcador.rojasLocal
                )
                EquipoDisciplinaColumn(
                    titulo = equipoVisitante,
                    amarillas = marcador.amarillasVisitante,
                    rojas = marcador.rojasVisitante
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(24.dp))

    QuickActionsSection(
        marcador = marcador,
        onQuickAction = onQuickAction
    )

    if (recentActions.isNotEmpty()) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Acciones recientes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                recentActions.forEach { action ->
                    Text(
                        text = action,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EquipoDisciplinaColumn(titulo: String, amarillas: Int, rojas: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = Color(0xFFFFC107),
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = amarillas.toString(), style = MaterialTheme.typography.bodyMedium)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Report,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = rojas.toString(), style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
private fun QuickActionsSection(
    marcador: MarcadorUi,
    onQuickAction: (TiempoRealQuickAction) -> Unit
) {
    val equipoLocal = marcador.equipoLocal.ifBlank { "Local" }
    val equipoVisitante = marcador.equipoVisitante.ifBlank { "Visitante" }

    Text(
        text = "Acciones rápidas",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
    )
    Spacer(modifier = Modifier.height(12.dp))

    val acciones = listOf(
        QuickActionItem(
            action = TiempoRealQuickAction.GOL_LOCAL,
            icon = Icons.Default.SportsSoccer,
            label = "Gol $equipoLocal"
        ),
        QuickActionItem(
            action = TiempoRealQuickAction.GOL_VISITA,
            icon = Icons.Default.SportsSoccer,
            label = "Gol $equipoVisitante"
        ),
        QuickActionItem(
            action = TiempoRealQuickAction.PENAL_LOCAL,
            icon = Icons.Default.Sports,
            label = "Penal $equipoLocal"
        ),
        QuickActionItem(
            action = TiempoRealQuickAction.PENAL_VISITA,
            icon = Icons.Default.Sports,
            label = "Penal $equipoVisitante"
        ),
        QuickActionItem(
            action = TiempoRealQuickAction.AMARILLA_LOCAL,
            icon = Icons.Default.Warning,
            label = "Amarilla $equipoLocal"
        ),
        QuickActionItem(
            action = TiempoRealQuickAction.AMARILLA_VISITA,
            icon = Icons.Default.Warning,
            label = "Amarilla $equipoVisitante"
        ),
        QuickActionItem(
            action = TiempoRealQuickAction.ROJA_LOCAL,
            icon = Icons.Default.Report,
            label = "Roja $equipoLocal"
        ),
        QuickActionItem(
            action = TiempoRealQuickAction.ROJA_VISITA,
            icon = Icons.Default.Report,
            label = "Roja $equipoVisitante"
        )
    )

    acciones.chunked(2).forEach { fila ->
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            fila.forEach { item ->
                FilledTonalButton(
                    onClick = { onQuickAction(item.action) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null,
                        modifier = Modifier.padding(end = 6.dp)
                    )
                    Text(text = item.label)
                }
            }
            if (fila.size < 2) {
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun EstadisticasSection(
    marcador: MarcadorUi,
    estadisticas: EstadisticasUi
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Estadísticas",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (estadisticas.tieneDatos) {
                EstadisticaRow(
                    etiqueta = "Posesión",
                    valorLocal = estadisticas.posesionLocal,
                    valorVisitante = estadisticas.posesionVisitante,
                    esPorcentaje = true
                )
                EstadisticaRow(
                    etiqueta = "Corners",
                    valorLocal = estadisticas.cornersLocal,
                    valorVisitante = estadisticas.cornersVisitante
                )
                EstadisticaRow(
                    etiqueta = "Remates al arco",
                    valorLocal = estadisticas.rematesArcoLocal,
                    valorVisitante = estadisticas.rematesArcoVisitante
                )
                EstadisticaRow(
                    etiqueta = "Faltas",
                    valorLocal = estadisticas.faltasLocal,
                    valorVisitante = estadisticas.faltasVisitante
                )

                if (estadisticas.otrosDatos.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Divider()
                    Spacer(modifier = Modifier.height(12.dp))
                    estadisticas.otrosDatos.forEach { (clave, valor) ->
                        Text(
                            text = "$clave: $valor",
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            } else {
                Text(
                    text = "Las estadísticas aún no están disponibles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun EstadisticaRow(
    etiqueta: String,
    valorLocal: Int?,
    valorVisitante: Int?,
    esPorcentaje: Boolean = false
) {
    val total = when {
        valorLocal != null && valorVisitante != null -> valorLocal + valorVisitante
        else -> null
    }
    val progreso = if (total != null && total > 0 && valorLocal != null) {
        valorLocal.toFloat() / total
    } else null

    val textoLocal = valorLocal?.let { if (esPorcentaje) "$it%" else it.toString() } ?: "--"
    val textoVisitante = valorVisitante?.let { if (esPorcentaje) "$it%" else it.toString() } ?: "--"

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = textoLocal,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
            Column(
                modifier = Modifier.weight(2f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = etiqueta,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                progreso?.let {
                    LinearProgressIndicator(
                        progress = it,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            Text(
                text = textoVisitante,
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        Divider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    }
}

@Composable
private fun AlineacionesSection(
    marcador: MarcadorUi,
    alineaciones: AlineacionesUi
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Alineaciones",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(16.dp))

            if (alineaciones.tieneDatos) {
                TeamLineup(
                    titulo = "${marcador.equipoLocal.ifBlank { "Equipo local" }}",
                    jugadores = alineaciones.titularesLocal
                )
                Spacer(modifier = Modifier.height(12.dp))
                TeamLineup(
                    titulo = "${marcador.equipoVisitante.ifBlank { "Equipo visitante" }}",
                    jugadores = alineaciones.titularesVisitante
                )

                alineaciones.arbitro?.let { arbitro ->
                    Spacer(modifier = Modifier.height(12.dp))
                    InfoRow(
                        icon = Icons.Default.AccountCircle,
                        texto = "Árbitro: $arbitro"
                    )
                }

                alineaciones.estadio?.let { estadio ->
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow(
                        icon = Icons.Default.LocationOn,
                        texto = "Estadio: $estadio"
                    )
                }
            } else {
                Text(
                    text = "Las alineaciones aún no están disponibles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TeamLineup(titulo: String, jugadores: List<String>) {
    Text(
        text = titulo,
        style = MaterialTheme.typography.titleSmall,
        fontWeight = FontWeight.Medium
    )
    if (jugadores.isEmpty()) {
        Text(
            text = "Información no disponible",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(vertical = 4.dp)
        )
    } else {
        jugadores.forEach { jugador ->
            Text(
                text = "• $jugador",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(vertical = 2.dp)
            )
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = texto, style = MaterialTheme.typography.bodyMedium)
    }
}

enum class TiempoRealTab(val title: String) {
    Marcador("Marcador"),
    Estadisticas("Estadísticas"),
    Alineaciones("Alineaciones")
}

private data class QuickActionItem(
    val action: TiempoRealQuickAction,
    val icon: ImageVector,
    val label: String
)

@Preview
@Composable
private fun TiempoRealPreview() {
    val sampleState = TiempoRealUiState(
        marcador = MarcadorUi(
            equipoLocal = "Barcelona",
            equipoVisitante = "Independiente",
            golesLocal = 2,
            golesVisitante = 1,
            penalesLocal = 4,
            penalesVisitante = 5,
            amarillasLocal = 2,
            amarillasVisitante = 1,
            rojasLocal = 0,
            rojasVisitante = 1,
            cronometro = "67:45",
            cronometrando = true,
            estado = "EN JUEGO",
            estadoIcono = "🔴"
        ),
        estadisticas = EstadisticasUi(
            posesionLocal = 55,
            posesionVisitante = 45,
            cornersLocal = 6,
            cornersVisitante = 3,
            rematesArcoLocal = 8,
            rematesArcoVisitante = 4,
            faltasLocal = 10,
            faltasVisitante = 12
        ),
        alineaciones = AlineacionesUi(
            titularesLocal = listOf("Jugador A", "Jugador B", "Jugador C"),
            titularesVisitante = listOf("Jugador X", "Jugador Y", "Jugador Z"),
            arbitro = "Árbitro Principal",
            estadio = "Estadio Nacional"
        ),
        ultimaActualizacionTexto = "20/01/2025 21:45:12",
        isLoading = false,
        recentActions = listOf(
            "[21:40:01] Gol para Barcelona",
            "[21:35:12] Tarjeta amarilla para Independiente"
        )
    )

    Arki_DeportesTheme {
        TiempoRealContent(
            uiState = sampleState,
            selectedTab = TiempoRealTab.Marcador,
            onTabSelected = {},
            onQuickAction = {},
            onNavigateBack = {},
            modifier = Modifier
        )
    }
}
