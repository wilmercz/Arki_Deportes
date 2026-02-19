package com.example.arki_deportes.ui.series

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.arki_deportes.data.model.Serie

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SerieFormScreen(
    campeonatoId: String,
    serieId: String? = null,
    viewModel: SerieFormViewModel = viewModel(),
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(campeonatoId, serieId) {
        viewModel.loadSerie(campeonatoId, serieId)
    }

    LaunchedEffect(uiState.message) {
        uiState.message?.let {
            snackbarHostState.showSnackbar(it.text)
            viewModel.onMessageShown()
        }
    }

    LaunchedEffect(uiState.shouldClose) {
        if (uiState.shouldClose) {
            onBack()
            viewModel.onCloseConsumed()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.isEditMode) "Editar Serie" else "Nueva Serie") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("Cancelar")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { viewModel.saveSerie() }) {
                if (uiState.isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp))
                } else {
                    Icon(Icons.Default.Save, contentDescription = "Guardar Serie")
                }
            }
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedTextField(
                    value = uiState.formData.nombreSerie,
                    onValueChange = viewModel::onNombreChange,
                    label = { Text("Nombre de la Serie (Ej: Fase de Grupos)") },
                    isError = uiState.showValidationErrors && uiState.formData.nombreSerie.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.formData.descripcion,
                    onValueChange = viewModel::onDescripcionChange,
                    label = { Text("Descripción (opcional)") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Selector para Regla de Clasificación
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        readOnly = true,
                        value = Serie.REGLAS_CLASIFICACION.find { it.first == uiState.formData.reglaClasificacion }?.second ?: "",
                        onValueChange = {},
                        label = { Text("Regla de Clasificación") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                    ) {
                        Serie.REGLAS_CLASIFICACION.forEach { (codigo, texto) ->
                            DropdownMenuItem(
                                text = { Text(texto) },
                                onClick = {
                                    viewModel.onReglaChange(codigo)
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = uiState.formData.gruposRaw,
                    onValueChange = viewModel::onGruposRawChange,
                    label = { Text("Grupos") },
                    placeholder = { Text("A, B, C, D...") },
                    supportingText = { Text("Separados por comas") },
                    isError = uiState.showValidationErrors && uiState.formData.gruposRaw.isBlank(),
                    modifier = Modifier.fillMaxWidth()
                )

                if (uiState.isEditMode) {
                    Spacer(modifier = Modifier.height(24.dp))
                    OutlinedButton(
                        onClick = { viewModel.deleteSerie() },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Eliminar Serie")
                    }
                }
            }
        }
    }
}
