package com.example.arki_deportes.ui.campeonatos

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
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.arki_deportes.utils.Constants
import com.example.arki_deportes.ui.common.DatePickerField

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampeonatoFormScreen(
    modifier: Modifier = Modifier,
    codigoCampeonato: String? = null,
    viewModel: CampeonatoFormViewModel = viewModel(),
    onBack: () -> Unit = {},
    onFinished: () -> Unit = onBack
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(codigoCampeonato) {
        viewModel.loadCampeonato(codigoCampeonato)
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
                title = { Text(if (uiState.isEditMode) "Editar campeonato" else "Nuevo campeonato") },
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

            OutlinedTextField(
                value = uiState.formData.nombre,
                onValueChange = viewModel::onNombreChange,
                label = { Text("Nombre del campeonato") },
                isError = uiState.showValidationErrors && uiState.formData.nombre.isBlank(),
                supportingText = {
                    if (uiState.showValidationErrors && uiState.formData.nombre.isBlank()) {
                        Text(Constants.Mensajes.CAMPO_OBLIGATORIO)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                DatePickerField(
                    value = uiState.formData.fechaInicio,
                    onValueChange = viewModel::onFechaInicioChange,
                    label = "Fecha inicio",
                    modifier = Modifier.weight(1f),
                    isError = uiState.showValidationErrors && uiState.formData.fechaInicio.isBlank(),
                    supportingText = {
                        if (uiState.showValidationErrors && uiState.formData.fechaInicio.isBlank()) {
                            Text(Constants.Mensajes.CAMPO_OBLIGATORIO)
                        }
                    }
                )

                DatePickerField(
                    value = uiState.formData.fechaFin,
                    onValueChange = viewModel::onFechaFinChange,
                    label = "Fecha fin",
                    modifier = Modifier.weight(1f),
                    isError = uiState.showValidationErrors && uiState.formData.fechaFin.isBlank(),
                    supportingText = {
                        if (uiState.showValidationErrors && uiState.formData.fechaFin.isBlank()) {
                            Text(Constants.Mensajes.CAMPO_OBLIGATORIO)
                        }
                    }
                )
            }

            OutlinedTextField(
                value = uiState.formData.provincia,
                onValueChange = viewModel::onProvinciaChange,
                label = { Text("Provincia") },
                isError = uiState.showValidationErrors && uiState.formData.provincia.isBlank(),
                supportingText = {
                    if (uiState.showValidationErrors && uiState.formData.provincia.isBlank()) {
                        Text(Constants.Mensajes.CAMPO_OBLIGATORIO)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            /* AÑO
            OutlinedTextField(
                value = uiState.formData.anio,
                onValueChange = viewModel::onAnioChange,
                label = { Text("Año") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )*/

            OutlinedTextField(
                value = uiState.formData.hashtags,
                onValueChange = viewModel::onHashtagsChange,
                label = { Text("Hashtags adicionales") },
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
                    onClick = viewModel::saveCampeonato,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.Save, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(if (uiState.isSaving) "Guardando..." else "Guardar")
                }

                if (uiState.isEditMode) {
                    OutlinedButton(
                        onClick = viewModel::deleteCampeonato,
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
