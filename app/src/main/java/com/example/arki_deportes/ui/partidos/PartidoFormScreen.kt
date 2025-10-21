package com.example.arki_deportes.ui.partidos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
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

            CampeonatoDropdown(
                campeonatos = uiState.campeonatos,
                selectedCodigo = uiState.formData.campeonatoCodigo,
                onSelected = viewModel::onCampeonatoSelected,
                showError = uiState.showValidationErrors && uiState.formData.campeonatoCodigo.isBlank()
            )

            GrupoDropdown(
                grupos = uiState.grupos,
                selectedCodigo = uiState.formData.grupoCodigo,
                onSelected = viewModel::onGrupoSelected,
                enabled = uiState.formData.campeonatoCodigo.isNotBlank()
            )

            EquipoDropdown(
                label = "Equipo 1",
                equipos = uiState.equipos,
                selectedCodigo = uiState.formData.equipo1Codigo,
                onSelected = viewModel::onEquipo1Selected,
                showError = uiState.showValidationErrors && uiState.formData.equipo1Codigo.isBlank()
            )

            EquipoDropdown(
                label = "Equipo 2",
                equipos = uiState.equipos.filter { it.CODIGOEQUIPO != uiState.formData.equipo1Codigo },
                selectedCodigo = uiState.formData.equipo2Codigo,
                onSelected = viewModel::onEquipo2Selected,
                showError = uiState.showValidationErrors && uiState.formData.equipo2Codigo.isBlank()
            )

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = uiState.formData.fechaPartido,
                    onValueChange = viewModel::onFechaChange,
                    label = { Text("Fecha (yyyy-MM-dd)") },
                    modifier = Modifier.weight(1f),
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
                    label = { Text("Hora (HH:mm)") },
                    modifier = Modifier.weight(1f),
                    isError = uiState.showValidationErrors && uiState.formData.horaPartido.isBlank(),
                    supportingText = {
                        if (uiState.showValidationErrors && uiState.formData.horaPartido.isBlank()) {
                            Text(Constants.Mensajes.CAMPO_OBLIGATORIO)
                        }
                    }
                )
            }

            OutlinedTextField(
                value = uiState.formData.estadio,
                onValueChange = viewModel::onEstadioChange,
                label = { Text("Estadio/Cancha") },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.showValidationErrors && uiState.formData.estadio.isBlank(),
                supportingText = {
                    if (uiState.showValidationErrors && uiState.formData.estadio.isBlank()) {
                        Text(Constants.Mensajes.CAMPO_OBLIGATORIO)
                    }
                }
            )

            OutlinedTextField(
                value = uiState.formData.provincia,
                onValueChange = viewModel::onProvinciaChange,
                label = { Text("Provincia") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.formData.lugar,
                onValueChange = viewModel::onLugarChange,
                label = { Text("Lugar") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.formData.textoFacebook,
                onValueChange = viewModel::onTextoFacebookChange,
                label = { Text("Texto para redes sociales") },
                modifier = Modifier.fillMaxWidth()
            )

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
                        onSelected(grupo.CODIGOGRUPO)
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
