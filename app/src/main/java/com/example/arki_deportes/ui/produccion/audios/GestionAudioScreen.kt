package com.example.arki_deportes.ui.produccion.audios

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.documentfile.provider.DocumentFile
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
    var showSelectSportForFolderDialog by remember { mutableStateOf<Uri?>(null) }

    val deportes = listOf("FUTBOL", "BASQUET", "AUTOMOVILISMO", "CICLISMO", "GENERAL")

    // 📂 Launcher para vincular carpeta local persistente
    val folderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let { showSelectSportForFolderDialog = it } 
    }

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
                    IconButton(onClick = { folderLauncher.launch(null) }) {
                        Icon(Icons.Default.FolderSpecial, contentDescription = "Vincular Carpeta Local")
                    }
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
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    
                    if (uiState.carpetasVinculadas.isNotEmpty()) {
                        item {
                            Surface(color = MaterialTheme.colorScheme.tertiaryContainer, modifier = Modifier.fillMaxWidth()) {
                                Text(
                                    "Carpetas Locales Vinculadas",
                                    modifier = Modifier.padding(8.dp),
                                    style = MaterialTheme.typography.labelMedium
                                )
                            }
                        }
                        items(uiState.carpetasVinculadas.toList()) { (deporte, uriString) ->
                            val uri = Uri.parse(uriString)
                            ListItem(
                                headlineContent = { Text("Deporte: $deporte") },
                                supportingContent = { Text(uri.path ?: uriString, maxLines = 1) },
                                leadingContent = { Icon(Icons.Default.Folder, null, tint = MaterialTheme.colorScheme.primary) },
                                trailingContent = {
                                    Row {
                                        // Selector de deporte para cambiar vinculación rápida
                                        var expanded by remember { mutableStateOf(false) }
                                        Box {
                                            IconButton(onClick = { expanded = true }) {
                                                Icon(Icons.Default.SwapHoriz, contentDescription = "Cambiar Deporte")
                                            }
                                            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                                                deportes.forEach { d ->
                                                    DropdownMenuItem(
                                                        text = { Text(d) },
                                                        onClick = {
                                                            viewModel.vincularCarpetaLocal(d, uri)
                                                            if (d != deporte) {
                                                                viewModel.desvincularCarpetaLocal(deporte)
                                                            }
                                                            expanded = false
                                                        }
                                                    )
                                                }
                                            }
                                        }
                                        IconButton(onClick = { viewModel.desvincularCarpetaLocal(deporte) }) {
                                            Icon(Icons.Default.LinkOff, contentDescription = "Desvincular", tint = MaterialTheme.colorScheme.error)
                                        }
                                    }
                                }
                            )
                        }
                        item { Divider(modifier = Modifier.padding(vertical = 8.dp)) }
                    }

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
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Subiendo archivos (${uiState.uploadProgress})...", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }

    // Dialogo para elegir deporte al vincular nueva carpeta
    showSelectSportForFolderDialog?.let { uri ->
        var selectedDep by remember { mutableStateOf("GENERAL") }
        AlertDialog(
            onDismissRequest = { showSelectSportForFolderDialog = null },
            title = { Text("Vincular Carpeta") },
            text = {
                Column {
                    Text("Selecciona el deporte para esta carpeta:")
                    Spacer(Modifier.height(8.dp))
                    deportes.forEach { dep ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selected = selectedDep == dep, onClick = { selectedDep = dep })
                            Text(dep)
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.vincularCarpetaLocal(selectedDep, uri)
                    showSelectSportForFolderDialog = null
                }) { Text("Vincular") }
            },
            dismissButton = {
                TextButton(onClick = { showSelectSportForFolderDialog = null }) { Text("Cancelar") }
            }
        )
    }

    if (showAddDialog) {
        AddAudioDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { uris, tipo, cat, dep, customId ->
                viewModel.uploadAudiosMasivo(uris, tipo, cat, dep, customId)
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

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AddAudioDialog(
    onDismiss: () -> Unit,
    onConfirm: (List<Uri>, String, String, String, String?) -> Unit
) {
    val context = LocalContext.current
    var selectedUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var tipo by remember { mutableStateOf("FX") }
    var categoria by remember { mutableStateOf("") }
    var deporte by remember { mutableStateOf("FUTBOL") }
    var customId by remember { mutableStateOf<String?>(null) }

    val sugerenciasFX = mapOf(
        "FUTBOL" to listOf(
            "ESQUINA" to "FUTBOL_FX_ESQUINA",
            "CORTINA" to "FUTBOL_FX_CORTINA",
            "GOL" to "FUTBOL_FX_GOL",
            "INICIO" to "FUTBOL_FX_INICIO",
            "TIEMPO JUEGO" to "FUTBOL_FX_TIEMPOJUEGO",
            "MARCADOR" to "FUTBOL_FX_MARCADOR"
        ),
        "BASQUET" to listOf(
            "CANASTA" to "BASQUET_FX_CANASTA",
            "CORTINA" to "BASQUET_FX_CORTINA",
            "TIEMPO FUERA" to "BASQUET_FX_TIMEOUT"
        ),
        "GENERAL" to listOf(
            "CORTINA GEN." to "FX_CORTINA_GEN",
            "ALERTA" to "FX_ALERTA"
        )
    )

    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        selectedUris = uris
        if (uris.size == 1) {
            val cursor = context.contentResolver.query(uris[0], null, null, null, null)
            cursor?.use { c ->
                val nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (c.moveToFirst()) {
                    val realFileName = c.getString(nameIndex).substringBeforeLast(".")
                    if (categoria.isBlank()) categoria = realFileName
                }
            }
        }
    }

    val folderUploadLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        uri?.let { treeUri ->
            val folderFiles = mutableListOf<Uri>()
            val rootDoc = DocumentFile.fromTreeUri(context, treeUri)
            rootDoc?.listFiles()?.forEach { file ->
                val name = file.name?.lowercase() ?: ""
                if (file.isFile && (name.endsWith(".mp3") || name.endsWith(".wav") || name.endsWith(".m4a") || name.endsWith(".ogg"))) {
                    folderFiles.add(file.uri)
                }
            }
            selectedUris = folderFiles
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Preparar Subida") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Deporte:", style = MaterialTheme.typography.labelLarge)
                val deportes = listOf("FUTBOL", "BASQUET", "AUTOMOVILISMO", "CICLISMO", "GENERAL")
                var expandedDep by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expandedDep,
                    onExpandedChange = { expandedDep = !expandedDep }
                ) {
                    OutlinedTextField(
                        value = deporte,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDep) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(expanded = expandedDep, onDismissRequest = { expandedDep = false }) {
                        deportes.forEach { d ->
                            DropdownMenuItem(text = { Text(d) }, onClick = { 
                                deporte = d
                                expandedDep = false 
                            })
                        }
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = tipo == "FX", onClick = { tipo = "FX"; customId = null; categoria = "" })
                    Text("Efecto (FX)")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = tipo == "MUSICA", onClick = { tipo = "MUSICA"; customId = null; categoria = "" })
                    Text("Música")
                }

                if (tipo == "FX" && selectedUris.size <= 1) {
                    Text("Acciones Rápidas:", style = MaterialTheme.typography.labelMedium)
                    FlowRow(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        sugerenciasFX[deporte]?.forEach { (label, id) ->
                            FilterChip(
                                selected = customId == id,
                                onClick = { customId = id; categoria = label },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = categoria,
                    onValueChange = { categoria = it },
                    label = { Text(if(selectedUris.size > 1) "Categoría (para el lote)" else "Nombre / Categoría") },
                    modifier = Modifier.fillMaxWidth()
                )

                Divider()
                Text("Seleccionar Origen:", style = MaterialTheme.typography.labelLarge)
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { fileLauncher.launch(arrayOf("audio/*")) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.AudioFile, null)
                        Spacer(Modifier.width(4.dp))
                        Text("Archivos")
                    }
                    Button(
                        onClick = { folderUploadLauncher.launch(null) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.DriveFolderUpload, null)
                        Spacer(Modifier.width(4.dp))
                        Text("Carpeta")
                    }
                }

                if (selectedUris.isNotEmpty()) {
                    Text("${selectedUris.size} archivos seleccionados", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedUris, tipo, categoria, deporte, customId) },
                enabled = selectedUris.isNotEmpty() && categoria.isNotBlank()
            ) { Text("Subir Todo") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
