package com.example.arki_deportes.ui.equipos

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
import com.example.arki_deportes.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EquipoFormScreen(
    modifier: Modifier = Modifier,
    codigoEquipo: String? = null,
    viewModel: EquipoFormViewModel = viewModel(),
    onBack: () -> Unit = {},
    onFinished: () -> Unit = onBack
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(codigoEquipo) {
        viewModel.loadEquipo(codigoEquipo)
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
                title = { Text(if (uiState.isEditMode) "Editar equipo" else "Nuevo equipo") },
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
                selectedCodigo = uiState.formData.codigoCampeonato,
                onSelected = viewModel::onCampeonatoSelected,
                showError = uiState.showValidationErrors && uiState.formData.codigoCampeonato.isBlank()
            )

            OutlinedTextField(
                value = uiState.formData.nombreCorto,
                onValueChange = viewModel::onNombreCortoChange,
                label = { Text("Nombre corto") },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.showValidationErrors && uiState.formData.nombreCorto.isBlank(),
                supportingText = {
                    if (uiState.showValidationErrors && uiState.formData.nombreCorto.isBlank()) {
                        Text(Constants.Mensajes.CAMPO_OBLIGATORIO)
                    }
                }
            )

            OutlinedTextField(
                value = uiState.formData.nombreCompleto,
                onValueChange = viewModel::onNombreCompletoChange,
                label = { Text("Nombre completo") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.formData.provincia,
                onValueChange = viewModel::onProvinciaChange,
                label = { Text("Provincia") },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.showValidationErrors && uiState.formData.provincia.isBlank(),
                supportingText = {
                    if (uiState.showValidationErrors && uiState.formData.provincia.isBlank()) {
                        Text(Constants.Mensajes.CAMPO_OBLIGATORIO)
                    }
                }
            )

            OutlinedTextField(
                value = uiState.formData.fechaAlta,
                onValueChange = viewModel::onFechaAltaChange,
                label = { Text("Fecha alta (yyyy-MM-dd)") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.formData.escudoLocal,
                onValueChange = viewModel::onEscudoLocalChange,
                label = { Text("Archivo escudo local") },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.formData.escudoLink,
                onValueChange = viewModel::onEscudoLinkChange,
                label = { Text("URL escudo") },
                modifier = Modifier.fillMaxWidth(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
            )

            Spacer(modifier = Modifier.size(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = viewModel::saveEquipo,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(if (uiState.isSaving) "Guardando..." else "Guardar")
                }

                if (uiState.isEditMode) {
                    OutlinedButton(
                        onClick = viewModel::deleteEquipo,
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
