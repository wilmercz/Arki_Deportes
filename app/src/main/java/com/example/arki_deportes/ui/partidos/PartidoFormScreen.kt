package com.example.arki_deportes.ui.partidos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.arki_deportes.data.model.Campeonato
import com.example.arki_deportes.data.model.Equipo
import com.example.arki_deportes.data.model.Grupo
import com.example.arki_deportes.utils.Constants
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import com.example.arki_deportes.data.model.Serie
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.SportsFootball
import androidx.compose.material3.CardDefaults
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.KeyboardHide
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.material.icons.filled.Place

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PartidoFormScreen(
    modifier: Modifier = Modifier,
    codigoPartido: String? = null,
    viewModel: PartidoFormViewModel = viewModel(),
    onBack: () -> Unit = {},
    onFinished: () -> Unit = onBack
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(codigoPartido) {
        viewModel.loadPartido(codigoPartido)
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it.text)
            viewModel.onMessageShown()
        }
    }

    LaunchedEffect(uiState.shouldClose) {
        if (uiState.shouldClose) {
            onFinished()
            viewModel.onCloseConsumed()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "Editar partido" else "Nuevo partido") },
                navigationIcon = {
                    TextButton(onClick = onBack) { Text("Cancelar") }
                },
                actions = {
                    IconButton(onClick = { keyboardController?.hide() }) {
                        Icon(Icons.Default.KeyboardHide, contentDescription = "Ocultar teclado")
                    }
                },
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (uiState.isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }


            // 💡 MEJORA: Si ya hay un campeonato seleccionado (del contexto o edición),
            // mostramos un indicador claro en lugar de repetir la elección.
            if (uiState.formData.campeonatoCodigo.isNotBlank()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsFootball,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.size(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Campeonato Seleccionado",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = uiState.formData.campeonatoNombre.ifBlank {
                                    // Búsqueda de respaldo si el nombre aún no carga en el form
                                    uiState.campeonatos.firstOrNull { it.CODIGO == uiState.formData.campeonatoCodigo }?.CAMPEONATO ?: "Cargando..."
                                },
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        // Permitimos cambiar el campeonato solo si es un partido NUEVO.
                        // En modo EDICIÓN no se debería cambiar el campeonato.
                        if (!uiState.isEditMode) {
                            TextButton(onClick = { viewModel.onCampeonatoSelected("") }) {
                                Text("Cambiar")
                            }
                        }
                    }
                }
            } else {
                // Si está vacío, mostramos el selector normal

                CampeonatoDropdown(
                    campeonatos = uiState.campeonatos,
                    selectedCodigo = uiState.formData.campeonatoCodigo,
                    onSelected = viewModel::onCampeonatoSelected,
                    showError = uiState.showValidationErrors && uiState.formData.campeonatoCodigo.isBlank()
                )
            }



            // 1. FILTROS DE SELECCIÓN
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Text("Asistente de Equipos", style = MaterialTheme.typography.titleSmall)

                    SerieDropdown(
                        series = uiState.series,
                        selectedCodigo = uiState.formData.serieCodigo,
                        onSelected = viewModel::onSerieSelected
                    )

                    GrupoDropdown(
                        grupos = uiState.grupos,
                        selectedCodigo = uiState.formData.grupoCodigo,
                        onSelected = viewModel::onGrupoSelected,
                        enabled = uiState.formData.serieCodigo.isNotBlank()
                    )
                }
            }

            // 2. LISTA DE EQUIPOS CON ASIGNACIÓN RÁPIDA (Como en VB.NET)
            LazyColumn(modifier = Modifier.heightIn(max = 300.dp)) {
                items(uiState.equipos) { equipo ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(equipo.EQUIPO, modifier = Modifier.weight(1f))
                        Button(onClick = { viewModel.asignarEquipo(equipo, 1) }, modifier = Modifier.padding(horizontal = 2.dp)) {
                            Text("EQ 1")
                        }
                        Button(onClick = { viewModel.asignarEquipo(equipo, 2) }, modifier = Modifier.padding(horizontal = 2.dp)) {
                            Text("EQ 2")
                        }
                    }
                }
            }


            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = uiState.formData.fechaPartido,
                    onValueChange = viewModel::onFechaChange,
                    label = { Text("Fecha del Partido") },
                    placeholder = { Text("yyyy-MM-dd") },
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.Event, contentDescription = null) },
                    isError = uiState.showValidationErrors && uiState.formData.fechaPartido.isBlank(),
                    supportingText = {
                        if (uiState.showValidationErrors && uiState.formData.fechaPartido.isBlank()) {
                            Text(Constants.Mensajes.CAMPO_OBLIGATORIO)
                        }
                    }
                )
                OutlinedTextField(
                    value = uiState.formData.horaPartido,
                    onValueChange = viewModel::onHoraChange,
                    label = { Text("Hora") },
                    placeholder = { Text("HH:mm") },
                    modifier = Modifier.weight(1f),
                    leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                    isError = uiState.showValidationErrors && uiState.formData.horaPartido.isBlank(),
                    supportingText = {
                        if (uiState.showValidationErrors && uiState.formData.horaPartido.isBlank()) {
                            Text(Constants.Mensajes.CAMPO_OBLIGATORIO)
                        }
                    }
                )
            }

            // 🗺️ Provincia (Se carga sola del campeonato)
            OutlinedTextField(
                value = uiState.formData.provincia,
                onValueChange = viewModel::onProvinciaChange,
                label = { Text("Provincia") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = { Icon(Icons.Default.Place, contentDescription = null) }
            )


            SugerenciaDropdownField(
                label = "Estadio/Cancha",
                value = uiState.formData.estadio,
                onValueChange = viewModel::onEstadioChange,
                sugerencias = uiState.estadiosSugeridos
            )

            SugerenciaDropdownField(
                label = "Lugar",
                value = uiState.formData.lugar,
                onValueChange = viewModel::onLugarChange,
                sugerencias = uiState.lugaresSugeridos
            )


            // 3. BOTÓN GENERAR TEXTO
            OutlinedButton(onClick = { viewModel.generarTextoSocial() }) {
                Icon(Icons.Default.AutoFixHigh, contentDescription = null)
                Text("Generar Texto Social")
            }

            OutlinedTextField(
                value = uiState.formData.textoFacebook,
                onValueChange = viewModel::onTextoFacebookChange,
                label = { Text("Texto para redes sociales") },
                modifier = Modifier.fillMaxWidth()
            )

            /* DESACTIVADO TEMPORALMENTE
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = uiState.formData.goles1,
                    onValueChange = viewModel::onGoles1Change,
                    label = { Text("Goles equipo 1") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = uiState.formData.goles2,
                    onValueChange = viewModel::onGoles2Change,
                    label = { Text("Goles equipo 2") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.weight(1f)
                )
            }

             */

            EtapaDropdown(
                etapa = uiState.formData.etapa,
                onEtapaChange = viewModel::onEtapaChange
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Transmisión en vivo")
                Switch(
                    checked = uiState.formData.transmision,
                    onCheckedChange = viewModel::onTransmisionChange,
                    colors = SwitchDefaults.colors()
                )
            }

            Spacer(modifier = Modifier.size(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {


                Button(
                    onClick = viewModel::savePartido,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(if (uiState.isSaving) "Guardando..." else "Guardar")
                }



                if (uiState.isEditMode) {
                    OutlinedButton(
                        onClick = viewModel::deletePartido,
                        enabled = !uiState.isDeleting,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text(if (uiState.isDeleting) "Eliminando..." else "Eliminar")
                    }
                }
            }

            if (uiState.isSaving || uiState.isDeleting) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SugerenciaDropdownField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    sugerencias: List<String>
) {
    var expanded by remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded && sugerencias.isNotEmpty(),
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            trailingIcon = { if(sugerencias.isNotEmpty()) ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
        )

        if (sugerencias.isNotEmpty()) {
            ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                sugerencias.forEach { sugerencia ->
                    DropdownMenuItem(
                        text = { Text(sugerencia) },
                        onClick = {
                            onValueChange(sugerencia)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SerieDropdown(
    series: List<Serie>,
    selectedCodigo: String,
    onSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = series.firstOrNull { it.CODIGOSERIE == selectedCodigo }
    val display = selected?.NOMBRESERIE ?: ""

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = display,
            onValueChange = {},
            readOnly = true,
            label = { Text("Seleccionar Serie") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            series.forEach { serie ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(serie.NOMBRESERIE) },
                    onClick = {
                        onSelected(serie.CODIGOSERIE)
                        expanded = false
                    }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CampeonatoDropdown(
    campeonatos: List<Campeonato>,
    selectedCodigo: String,
    onSelected: (String) -> Unit,
    showError: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = campeonatos.firstOrNull { it.CODIGO == selectedCodigo }
    val display = selected?.CAMPEONATO ?: ""

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = display,
            onValueChange = {},
            readOnly = true,
            label = { Text("Campeonato") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            isError = showError,
            supportingText = {
                if (showError) Text(Constants.Mensajes.CAMPO_OBLIGATORIO)
            }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            campeonatos.forEach { campeonato ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(campeonato.CAMPEONATO) },
                    onClick = {
                        onSelected(campeonato.CODIGO)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GrupoDropdown(
    grupos: List<Grupo>,
    selectedCodigo: String,
    onSelected: (String) -> Unit,
    enabled: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = grupos.firstOrNull { it.CODIGOGRUPO == selectedCodigo }
    val display = selected?.GRUPO ?: ""

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = it }
    ) {
        OutlinedTextField(
            value = display,
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text("Grupo (opcional)") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            grupos.forEach { grupo ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(grupo.GRUPO) },
                    onClick = {
                        //onSelected(grupo.CODIGOGRUPO)
                        onSelected(grupo.CODIGOGRUPO?.toString() ?: "")
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EquipoDropdown(
    label: String,
    equipos: List<Equipo>,
    selectedCodigo: String,
    onSelected: (String) -> Unit,
    showError: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val selected = equipos.firstOrNull { it.CODIGOEQUIPO == selectedCodigo }
    val display = selected?.getNombreDisplay() ?: ""

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = display,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            isError = showError,
            supportingText = {
                if (showError) Text(Constants.Mensajes.CAMPO_OBLIGATORIO)
            }
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            equipos.forEach { equipo ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(equipo.getNombreDisplay()) },
                    onClick = {
                        onSelected(equipo.CODIGOEQUIPO)
                        expanded = false
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EtapaDropdown(
    etapa: Int,
    onEtapaChange: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val etapas = listOf(
        Constants.EtapasPartido.NINGUNO to Constants.EtapasPartido.getTexto(Constants.EtapasPartido.NINGUNO),
        Constants.EtapasPartido.CUARTOS to Constants.EtapasPartido.getTexto(Constants.EtapasPartido.CUARTOS),
        Constants.EtapasPartido.SEMIFINAL to Constants.EtapasPartido.getTexto(Constants.EtapasPartido.SEMIFINAL),
        Constants.EtapasPartido.FINAL to Constants.EtapasPartido.getTexto(Constants.EtapasPartido.FINAL)
    )
    val selected = etapas.firstOrNull { it.first == etapa }?.second ?: etapas.first().second

    ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text("Etapa") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            etapas.forEach { (codigo, nombre) ->
                androidx.compose.material3.DropdownMenuItem(
                    text = { Text(nombre) },
                    onClick = {
                        onEtapaChange(codigo)
                        expanded = false
                    }
                )
            }
        }
    }
}
