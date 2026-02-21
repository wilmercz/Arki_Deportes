package com.example.arki_deportes.ui.equipos

import androidx.activity.result.launch
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Icon
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
//import androidx.glance.appwidget.compose
import com.example.arki_deportes.utils.EcuadorProvincias
import coil.compose.AsyncImage
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import com.example.arki_deportes.data.context.CampeonatoContext


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

    val context = LocalContext.current

    // Launcher para seleccionar imagen de la galería
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: android.net.Uri? ->
        viewModel.onImageSelected(uri)
    }

    val campeonatoActivo by CampeonatoContext.campeonatoActivo.collectAsState()

    /* CODIGO VIEJO
    LaunchedEffect(codigoEquipo) {
        // Obtenemos el campeonato activo del contexto o del estado
        val campId = viewModel.uiState.value.formData.codigoCampeonato
        viewModel.loadEquipo(campId, codigoEquipo)
    }
    */

    // ✅ Sincronizamos el ViewModel con el contexto global
    LaunchedEffect(campeonatoActivo) {
        campeonatoActivo?.CODIGO?.let { viewModel.setCampeonatoId(it) }
    }

    LaunchedEffect(codigoEquipo) {
        if (codigoEquipo != null) {
            // Priorizamos el ID que ya tenga el form, sino usamos el del contexto global
            val campId = viewModel.uiState.value.formData.codigoCampeonato.ifBlank {
                campeonatoActivo?.CODIGO
            }
            viewModel.loadEquipo(campId, codigoEquipo)
        }
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

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                )
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text("Campeonato Activo", style = MaterialTheme.typography.labelMedium)
                        Text(
                            text = campeonatoActivo?.CAMPEONATO ?: "Ninguno seleccionado",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }


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

            ProvinciaDropdown(
                value = uiState.formData.provincia,
                onValueChange = viewModel::onProvinciaChange,
                showError = uiState.showValidationErrors && uiState.formData.provincia.isBlank()
            )

/*
            OutlinedTextField(
                value = uiState.formData.fechaAlta,
                onValueChange = viewModel::onFechaAltaChange,
                label = { Text("Fecha alta (yyyy-MM-dd)") },
                modifier = Modifier.fillMaxWidth()
            )
*/
            OutlinedTextField(
                value = uiState.formData.escudoLocal,
                onValueChange = viewModel::onEscudoLocalChange,
                label = { Text("Archivo escudo local") },
                modifier = Modifier.fillMaxWidth()
            )



            Column(modifier = Modifier.fillMaxWidth()) {
                Text(text = "Escudo del Equipo", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Miniatura (Usa Coil para cargar la imagen)
                    Card(
                        modifier = Modifier.size(80.dp),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            if (uiState.formData.escudoLink.isNotBlank()) {
                                AsyncImage(
                                    model = uiState.formData.escudoLink,
                                    contentDescription = "Miniatura escudo",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Fit
                                )
                            } else {
                                Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = MaterialTheme.colorScheme.outline)
                            }
                        }
                    }

                    // Botón para subir
                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CloudUpload, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Subir Escudo")
                    }
                }


                Spacer(modifier = Modifier.height(8.dp))


                // Caja de texto (mantenemos el enlace por si se quiere pegar manualmente)
                OutlinedTextField(
                    value = uiState.formData.escudoLink,
                    onValueChange = viewModel::onEscudoLinkChange,
                    label = { Text("URL del escudo (automático)") },
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                    trailingIcon = {
                        if (uiState.formData.escudoLink.isNotBlank()) {
                            IconButton(onClick = { viewModel.onEscudoLinkChange("") }) {
                                Icon(Icons.Default.Clear, "Limpiar")
                            }
                        }
                    }
                )
            }


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

            if (!uiState.isEditMode && uiState.formData.codigoCampeonato.isNotBlank()) {Spacer(modifier = Modifier.size(8.dp))
                Divider()
                Spacer(modifier = Modifier.size(8.dp))

                OutlinedButton(
                    onClick = viewModel::showImportConfirmation,
                    enabled = !uiState.isImporting,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (uiState.isImporting) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Importando...")
                    } else {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.size(8.dp))
                        Text("Importar 24 Provincias como Equipos")
                    }
                }
            }

            // Diálogo de confirmación
            if (uiState.showImportConfirmation) {
                AlertDialog(
                    onDismissRequest = viewModel::hideImportConfirmation,
                    title = { Text("¿Importar Provincias?") },
                    text = { Text("Se agregarán las 24 provincias del Ecuador como equipos para este campeonato. ¿Deseas continuar?") },
                    confirmButton = {
                        TextButton(onClick = viewModel::importarProvincias) { Text("Confirmar") }
                    },
                    dismissButton = {
                        TextButton(onClick = viewModel::hideImportConfirmation) { Text("Cancelar") }
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
private fun ProvinciaDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    showError: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val provincias = remember { EcuadorProvincias.LISTA.map { it.nombre } }

    // Filtramos las opciones basadas en lo que el usuario escribe
    val filteredOptions = remember(value) {
        if (value.isEmpty()) provincias
        else provincias.filter { it.contains(value, ignoreCase = true) }
    }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it }
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = {
                onValueChange(it)
                expanded = true
            },
            label = { Text("Provincia") },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            isError = showError,
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            supportingText = {
                if (showError) Text(Constants.Mensajes.CAMPO_OBLIGATORIO)
            }
        )

        if (filteredOptions.isNotEmpty()) {
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                filteredOptions.forEach { provincia ->
                    DropdownMenuItem(
                        text = { Text(provincia) },
                        onClick = {
                            onValueChange(provincia)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}