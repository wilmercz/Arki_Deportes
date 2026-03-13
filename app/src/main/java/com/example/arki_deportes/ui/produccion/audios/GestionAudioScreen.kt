package com.example.arki_deportes.ui.produccion.audios

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.arki_deportes.data.model.AudioResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionAudioScreen(
    viewModel: GestionAudioViewModel,
    onBack: () -> Unit,
    onOpenDrawer: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Audios") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar Audio")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.audios.isEmpty()) {
                Text(
                    text = "No hay audios registrados",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    val grouped = uiState.audios.groupBy { it.tipo }
                    grouped.forEach { (tipo, audios) ->
                        item {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = if (tipo == "FX") "Efectos (FX)" else "Música",
                                    modifier = Modifier.padding(8.dp),
                                    style = MaterialTheme.typography.titleMedium
                                )
                            }
                        }
                        items(audios) { audio ->
                            AudioItem(
                                audio = audio,
                                onDelete = { viewModel.deleteAudio(audio.id) },
                                onTransmit = { viewModel.playInOverlay(audio) }
                            )
                        }
                    }
                }
            }

            if (uiState.isUploading) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Subiendo archivo...")
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddAudioDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { uri, name, tipo, cat, dep ->
                viewModel.uploadAudio(uri, name, tipo, cat, dep)
                showAddDialog = false
            }
        )
    }

    uiState.error?.let { error ->
        AlertDialog(
            onDismissRequest = { viewModel.clearError() },
            title = { Text("Error") },
            text = { Text(error) },
            confirmButton = {
                TextButton(onClick = { viewModel.clearError() }) {
                    Text("OK")
                }
            }
        )
    }
}

@Composable
fun AudioItem(audio: AudioResource, onDelete: () -> Unit, onTransmit: () -> Unit) {
    ListItem(
        headlineContent = { Text(audio.nombre) },
        supportingContent = { Text("${audio.categoria} - ${audio.deporte}") },
        leadingContent = {
            Icon(
                imageVector = if (audio.tipo == "FX") Icons.Default.GraphicEq else Icons.Default.MusicNote,
                contentDescription = null
            )
        },
        trailingContent = {
            Row {
                IconButton(onClick = onTransmit) {
                    Icon(Icons.Default.PlayArrow, contentDescription = "Transmitir", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAudioDialog(
    onDismiss: () -> Unit,
    onConfirm: (Uri, String, String, String, String) -> Unit
) {
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("FX") }
    var categoria by remember { mutableStateOf("") }
    var deporte by remember { mutableStateOf("FUTBOL") }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedUri = uri
        uri?.let { fileName = it.lastPathSegment ?: "audio_file" }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Subir Nuevo Audio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { launcher.launch("audio/*") }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (selectedUri == null) "Seleccionar Archivo" else "Cambiar Archivo")
                }
                if (selectedUri != null) {
                    Text("Archivo: $fileName", style = MaterialTheme.typography.bodySmall)
                }
                
                Text("Tipo:", style = MaterialTheme.typography.labelLarge)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = tipo == "FX", onClick = { tipo = "FX"; if (categoria.isEmpty()) categoria = "CORTINA" })
                    Text("FX")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = tipo == "MUSICA", onClick = { tipo = "MUSICA"; if (categoria.isEmpty()) categoria = "FUTBOL" })
                    Text("Música")
                }

                if (tipo == "FX") {
                    OutlinedTextField(
                        value = categoria,
                        onValueChange = { categoria = it },
                        label = { Text("Categoría (ej: FX_TIROS_ESQUINA)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Text("Deporte:", style = MaterialTheme.typography.labelLarge)
                    val deportes = listOf("FUTBOL", "AUTOMOVILISMO", "CICLISMO", "BASQUET")
                    var expanded by remember { mutableStateOf(false) }
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = !expanded }
                    ) {
                        OutlinedTextField(
                            value = deporte,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Deporte") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            deportes.forEach { d ->
                                DropdownMenuItem(
                                    text = { Text(d) },
                                    onClick = { deporte = d; categoria = d; expanded = false }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = selectedUri != null && categoria.isNotBlank(),
                onClick = { selectedUri?.let { onConfirm(it, fileName, tipo, categoria, deporte) } }
            ) {
                Text("Subir")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
