package com.example.arki_deportes.ui.produccion

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.arki_deportes.utils.Constants
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Punto de entrada para el flujo de edición del equipo de producción.
 */
@Composable
fun EquipoProduccionRoute(
    viewModel: EquipoProduccionViewModel,
    onBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    onOpenDrawer: (() -> Unit)? = null
) {
    val state by viewModel.uiState.collectAsState()

    EquipoProduccionScreen(
        state = state,
        onModoSeleccionado = viewModel::onModoSeleccionado,
        onCampeonatoSeleccionado = viewModel::onCampeonatoSeleccionado,
        onNarradorChange = viewModel::onNarradorChange,
        onComentaristaChange = viewModel::onComentaristaChange,
        onBordeCampoChange = viewModel::onBordeCampoChange,
        onAnfitrionesChange = viewModel::onAnfitrionesChange,
        onGuardar = viewModel::guardarConfiguracion,
        onRetry = viewModel::recargar,
        onBack = onBack,
        modifier = modifier,
        onOpenDrawer = onOpenDrawer
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipoProduccionScreen(
    state: EquipoProduccionUiState,
    onModoSeleccionado: (EquipoProduccionMode) -> Unit,
    onCampeonatoSeleccionado: (String) -> Unit,
    onNarradorChange: (String) -> Unit,
    onComentaristaChange: (String) -> Unit,
    onBordeCampoChange: (String) -> Unit,
    onAnfitrionesChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    onOpenDrawer: (() -> Unit)? = null
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Equipo de producción") },
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
                        onBack != null -> {
                            IconButton(onClick = onBack) {
                                Icon(
                                    imageVector = Icons.Filled.ArrowBack,
                                    contentDescription = "Regresar"
                                )
                            }
                        }
                    }
                },
                scrollBehavior = scrollBehavior
            )
        }
    ) { paddingValues ->
        when {
            state.isLoading -> LoadingContent(paddingValues)
            shouldShowInitialError(state) -> ErrorContent(
                message = state.error ?: Constants.Mensajes.ERROR_DESCONOCIDO,
                onRetry = onRetry,
                paddingValues = paddingValues
            )
            else -> FormContent(
                state = state,
                onModoSeleccionado = onModoSeleccionado,
                onCampeonatoSeleccionado = onCampeonatoSeleccionado,
                onNarradorChange = onNarradorChange,
                onComentaristaChange = onComentaristaChange,
                onBordeCampoChange = onBordeCampoChange,
                onAnfitrionesChange = onAnfitrionesChange,
                onGuardar = onGuardar,
                paddingValues = paddingValues
            )
        }
    }
}

@Composable
private fun LoadingContent(paddingValues: PaddingValues) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit,
    paddingValues: PaddingValues
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = message,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Button(onClick = onRetry) {
                Text("Reintentar")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FormContent(
    state: EquipoProduccionUiState,
    onModoSeleccionado: (EquipoProduccionMode) -> Unit,
    onCampeonatoSeleccionado: (String) -> Unit,
    onNarradorChange: (String) -> Unit,
    onComentaristaChange: (String) -> Unit,
    onBordeCampoChange: (String) -> Unit,
    onAnfitrionesChange: (String) -> Unit,
    onGuardar: () -> Unit,
    paddingValues: PaddingValues
) {
    val scrollState = rememberScrollState()
    val formattedTimestamp = remember(state.ultimaActualizacion) {
        formatTimestamp(state.ultimaActualizacion)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        ModeSelector(state = state, onModoSeleccionado = onModoSeleccionado)

        if (state.modo == EquipoProduccionMode.Campeonato) {
            CampeonatoSelector(
                state = state,
                onCampeonatoSeleccionado = onCampeonatoSeleccionado
            )
        }

        if (state.isSelectionLoading || state.isSaving) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (state.error != null && !shouldShowInitialError(state)) {
            MessageCard(
                message = state.error,
                color = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        }

        if (!formattedTimestamp.isNullOrEmpty()) {
            Text(
                text = "Última actualización: $formattedTimestamp",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        FieldSection(
            label = "Narrador",
            value = state.narrador,
            onValueChange = onNarradorChange,
            placeholder = "Nombre del narrador principal"
        )

        FieldSection(
            label = "Comentarista",
            value = state.comentarista,
            onValueChange = onComentaristaChange,
            placeholder = "Nombre del comentarista"
        )

        FieldSection(
            label = "Borde de campo",
            value = state.bordeCampo,
            onValueChange = onBordeCampoChange,
            placeholder = "Reportero en borde de campo"
        )

        OutlinedTextField(
            value = state.anfitrionesInput,
            onValueChange = onAnfitrionesChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Anfitriones") },
            placeholder = { Text("Separar por coma o salto de línea") },
            minLines = 3,
            maxLines = 6,
            keyboardOptions = KeyboardOptions(
                capitalization = KeyboardCapitalization.Words,
                imeAction = ImeAction.Done
            ),
            supportingText = {
                Text("Ejemplo: Ana Torres, Luis García")
            }
        )

        if (state.mensajeExito != null) {
            MessageCard(
                message = state.mensajeExito,
                color = MaterialTheme.colorScheme.tertiaryContainer,
                contentColor = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Button(
            onClick = onGuardar,
            enabled = !state.isSaving &&
                (state.modo != EquipoProduccionMode.Campeonato || state.campeonatoSeleccionado != null),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (state.isSaving) {
                CircularProgressIndicator(
                    modifier = Modifier.height(20.dp),
                    strokeWidth = 2.dp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Guardar configuración")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ModeSelector(
    state: EquipoProduccionUiState,
    onModoSeleccionado: (EquipoProduccionMode) -> Unit
) {
    val opciones = listOf(
        EquipoProduccionMode.Default to "Predeterminado",
        EquipoProduccionMode.Campeonato to "Por campeonato"
    )

    val selectedIndex = opciones.indexOfFirst { it.first == state.modo }.takeIf { it >= 0 } ?: 0

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "Modo de configuración",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        TabRow(selectedTabIndex = selectedIndex) {
            opciones.forEach { (modo, etiqueta) ->
                Tab(
                    selected = modo == state.modo,
                    onClick = { onModoSeleccionado(modo) },
                    text = {
                        Text(
                            text = etiqueta,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CampeonatoSelector(
    state: EquipoProduccionUiState,
    onCampeonatoSeleccionado: (String) -> Unit
) {
    val campeonatos = state.campeonatos
    var expanded by remember { mutableStateOf(false) }
    val seleccionado = state.campeonatoSeleccionado

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "Campeonato",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = {
                if (campeonatos.isNotEmpty()) {
                    expanded = !expanded
                }
            }
        ) {
            OutlinedTextField(
                value = seleccionado?.CAMPEONATO ?: "",
                onValueChange = {},
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                readOnly = true,
                label = { Text("Selecciona un campeonato") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = TextFieldDefaults.outlinedTextFieldColors(
                    disabledTextColor = MaterialTheme.colorScheme.onSurface
                ),
                enabled = campeonatos.isNotEmpty(),
                supportingText = {
                    if (campeonatos.isEmpty()) {
                        Text("No hay campeonatos disponibles")
                    }
                }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                campeonatos.forEach { campeonato ->
                    DropdownMenuItem(
                        text = { Text(campeonato.CAMPEONATO) },
                        onClick = {
                            expanded = false
                            onCampeonatoSeleccionado(campeonato.CODIGO)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun FieldSection(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        label = { Text(label) },
        placeholder = { Text(placeholder) },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Words,
            imeAction = ImeAction.Next
        )
    )
}

@Composable
private fun MessageCard(
    message: String,
    color: Color,
    contentColor: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = color,
            contentColor = contentColor
        )
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

private fun formatTimestamp(timestamp: Long?): String? {
    if (timestamp == null || timestamp <= 0) return null
    return try {
        val formatter = DateTimeFormatter.ofPattern(
            Constants.DATETIME_FORMAT,
            Locale.getDefault()
        )
        val zonedDateTime = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault())
        formatter.format(zonedDateTime)
    } catch (_: Exception) {
        null
    }
}

private fun shouldShowInitialError(state: EquipoProduccionUiState): Boolean {
    if (state.error.isNullOrBlank()) return false
    if (state.isLoading || state.isSaving || state.isSelectionLoading) return false
    val withoutData = state.narrador.isBlank() &&
        state.comentarista.isBlank() &&
        state.bordeCampo.isBlank() &&
        state.anfitrionesInput.isBlank() &&
        state.ultimaActualizacion == null
    return withoutData
}
