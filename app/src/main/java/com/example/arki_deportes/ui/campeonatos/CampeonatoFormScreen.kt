package com.example.arki_deportes.ui.campeonatos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardHide
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
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.arki_deportes.utils.Constants
import com.example.arki_deportes.ui.common.DatePickerField
import com.example.arki_deportes.utils.SportType
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material3.IconButton
import androidx.compose.ui.platform.LocalSoftwareKeyboardController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampeonatoFormScreen(
    modifier: Modifier = Modifier,
    codigoCampeonato: String? = null,
    viewModel: CampeonatoFormViewModel = viewModel(),
    onBack: () -> Unit = {},
    onFinished: () -> Unit = onBack,
    onNavigateToSeries: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val keyboardController = LocalSoftwareKeyboardController.current

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

            if (uiState.isEditMode) {
                OutlinedTextField(
                    value = uiState.formData.codigo,
                    onValueChange = {},
                    enabled = false,
                    label = { Text("Código") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Selector de Deporte
            var expanded by remember { mutableStateOf(false) }
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }
            ) {
                OutlinedTextField(
                    value = SportType.fromId(uiState.formData.deporte).displayName,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Tipo de Deporte") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor()
                )
                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    SportType.options().forEach { sportType ->
                        DropdownMenuItem(
                            text = { Text(sportType.displayName) },
                            onClick = {
                                viewModel.onDeporteChange(sportType.id)
                                expanded = false
                            }
                        )
                    }
                }
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

            // Campos dinámicos según el deporte
            val isMotorSport = uiState.formData.deporte.let { 
                it == SportType.AUTOMOVILISMO.id || it == SportType.MOTOCICLISMO.id || it == SportType.CICLISMO.id 
            }

            if (isMotorSport) {
                OutlinedTextField(
                    value = uiState.formData.circuito,
                    onValueChange = viewModel::onCircuitoChange,
                    label = { Text("Circuitos") },
                    supportingText = { Text("Separados por comas") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = uiState.formData.lugar,
                    onValueChange = viewModel::onLugarChange,
                    label = { Text("Lugares") },
                    supportingText = { Text("Separados por comas") },
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = uiState.formData.vueltas,
                        onValueChange = viewModel::onVueltasChange,
                        label = { Text("Vueltas") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = uiState.formData.mangas,
                        onValueChange = viewModel::onMangasChange,
                        label = { Text("Mangas") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f)
                    )
                }
                OutlinedTextField(
                    value = uiState.formData.duracion,
                    onValueChange = viewModel::onDuracionChange,
                    label = { Text("Duración Carrera") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                OutlinedTextField(
                    value = uiState.formData.estadios,
                    onValueChange = viewModel::onEstadiosChange,
                    label = { Text("Estadios") },
                    supportingText = { Text("Separados por comas") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.formData.lugar,
                    onValueChange = viewModel::onLugarChange,
                    label = { Text("Lugares") },
                    placeholder = { Text("Lugar 1, Lugar 2...") },
                    supportingText = { Text("Separados por comas") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = uiState.formData.tiempoJuego,
                    onValueChange = viewModel::onTiempoJuegoChange,
                    label = { Text("Tiempo de Juego (minutos)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = uiState.formData.alias,
                onValueChange = viewModel::onAliasChange,
                label = { Text("Alias") },
                modifier = Modifier.fillMaxWidth()
            )

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

            // BOTÓN NUEVO FUERA DEL ROW PARA QUE SE VEA BIEN
            if (uiState.isEditMode) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { onNavigateToSeries(uiState.formData.codigo) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Layers, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Gestionar Series y Grupos")
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
