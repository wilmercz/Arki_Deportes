package com.example.arki_deportes.ui.series

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
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
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
import com.example.arki_deportes.data.model.Serie
import com.example.arki_deportes.utils.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SerieFormScreen(
    modifier: Modifier = Modifier,
    codigoSerie: String? = null,
    viewModel: SerieFormViewModel = viewModel(),
    onBack: () -> Unit = {},
    onFinished: () -> Unit = onBack
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(codigoSerie) {
        viewModel.loadSerie(codigoSerie)
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { message ->
            snackbarHostState.showSnackbar(message.text)
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
                title = { Text(if (uiState.isEditMode) "Editar serie" else "Nueva serie") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Cancelar")
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

            if (uiState.isEditMode) {
                OutlinedTextField(
                    value = uiState.formData.codigo,
                    onValueChange = {},
                    enabled = false,
                    label = { Text("Código") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Selector de campeonato
            CampeonatoDropdown(
                campeonatos = uiState.campeonatos,
                selectedCodigo = uiState.formData.codigoCampeonato,
                onSelected = viewModel::onCampeonatoSelected,
                showError = uiState.showValidationErrors && uiState.formData.codigoCampeonato.isBlank(),
                enabled = !uiState.isEditMode // No permitir cambiar campeonato en modo edición
            )

            OutlinedTextField(
                value = uiState.formData.nombreSerie,
                onValueChange = viewModel::onNombreSerieChange,
                label = { Text("Nombre de la serie (ej: A, B, C)") },
                isError = uiState.showValidationErrors && uiState.formData.nombreSerie.isBlank(),
                supportingText = {
                    if (uiState.showValidationErrors && uiState.formData.nombreSerie.isBlank()) {
                        Text(Constants.Mensajes.CAMPO_OBLIGATORIO)
                    } else {
                        Text("Una letra o nombre corto que identifique la serie")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = uiState.formData.descripcion,
                onValueChange = viewModel::onDescripcionChange,
                label = { Text("Descripción") },
                supportingText = { Text("Opcional: descripción detallada de la serie") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )

            OutlinedTextField(
                value = uiState.formData.cantGrupos,
                onValueChange = viewModel::onCantGruposChange,
                label = { Text("Cantidad de grupos") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                isError = uiState.showValidationErrors && 
                         (uiState.formData.cantGrupos.toIntOrNull() ?: 0) <= 0,
                supportingText = {
                    if (uiState.showValidationErrors && (uiState.formData.cantGrupos.toIntOrNull() ?: 0) <= 0) {
                        Text("Debe ser mayor a 0")
                    } else {
                        Text("Número de grupos en esta serie")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            // Regla de clasificación
            ReglaClasificacionDropdown(
                selectedRegla = uiState.formData.reglaClasificacion,
                onSelected = viewModel::onReglaClasificacionChange
            )

            // Equipos que clasifican (calculado automáticamente)
            OutlinedTextField(
                value = uiState.formData.equiposClasifican,
                onValueChange = {},
                enabled = false,
                label = { Text("Equipos que clasifican") },
                supportingText = { Text("Calculado automáticamente según la regla") },
                modifier = Modifier.fillMaxWidth()
            )

            if (uiState.isEditMode) {
                OutlinedTextField(
                    value = uiState.formData.fechaAlta,
                    onValueChange = {},
                    enabled = false,
                    label = { Text("Fecha de alta") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.size(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = viewModel::saveSerie,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(if (uiState.isSaving) "Guardando..." else "Guardar")
                }

                if (uiState.isEditMode) {
                    OutlinedButton(
                        onClick = viewModel::deleteSerie,
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
    showError: Boolean,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val selectedCampeonato = campeonatos.find { it.CODIGO == selectedCodigo }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { if (enabled) expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedCampeonato?.CAMPEONATO ?: "",
            onValueChange = {},
            readOnly = true,
            enabled = enabled,
            label = { Text("Campeonato") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            isError = showError,
            supportingText = {
                if (showError) {
                    Text(Constants.Mensajes.CAMPO_OBLIGATORIO)
                } else if (!enabled) {
                    Text("No se puede cambiar el campeonato en modo edición")
                }
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            campeonatos.forEach { campeonato ->
                DropdownMenuItem(
                    text = {
                        Column {
                            Text(
                                text = campeonato.CAMPEONATO,
                                style = androidx.compose.material3.MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "${campeonato.PROVINCIA} • ${campeonato.FECHAINICIO}",
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                            )
                        }
                    },
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
private fun ReglaClasificacionDropdown(
    selectedRegla: String,
    onSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val reglas = Serie.REGLAS_CLASIFICACION
    val selectedReglaTexto = reglas.find { it.first == selectedRegla }?.second ?: "Seleccione regla"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selectedReglaTexto,
            onValueChange = {},
            readOnly = true,
            label = { Text("Regla de clasificación") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            supportingText = { Text("Define qué equipos clasifican a la siguiente fase") },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            reglas.forEach { (codigo, texto) ->
                DropdownMenuItem(
                    text = { Text(texto) },
                    onClick = {
                        onSelected(codigo)
                        expanded = false
                    }
                )
            }
        }
    }
}
