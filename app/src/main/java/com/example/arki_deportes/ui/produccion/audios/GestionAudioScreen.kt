package com.example.arki_deportes.ui.produccion.audios

import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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

    // 📂 Launcher para vincular carpeta local (SAF DocumentTree)
    // El usuario elige una carpeta (ej: "FUTBOL") y la app guardará el permiso persistente
    val folderLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocumentTree()
    ) { uri ->
        // Aquí podrías abrir otro diálogo para preguntar a qué DEPORTE asociar la carpeta
        // Por simplicidad, usaremos un diálogo rápido o asumiremos el deporte actual
        uri?.let { viewModel.vincularCarpetaLocal("FUTBOL", it) } 
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
                    // 📁 BOTÓN NUEVO: Vincular Carpeta del Dispositivo
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
                    
                    // 📁 SECCIÓN NUEVA: Carpetas vinculadas
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
                        items(uiState.carpetasVinculadas.toList()) { (deporte, uri) ->
                            ListItem(
                                headlineContent = { Text("Carpeta $deporte") },
                                supportingContent = { Text(uri, maxLines = 1) },
                                leadingContent = { Icon(Icons.Default.Folder, null) }
                            )
                        }
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
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        // 🎯 Mostramos el progreso masivo
                        Text("Subiendo archivos (${uiState.uploadProgress})...")
                    }
                }
            }
        }
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

    // Catálogo de FX sugeridos por deporte
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

    // 🎯 CAMBIADO A: GetMultipleContents() para subida masiva
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        selectedUris = uris
        if (uris.size == 1) {
            // Lógica de nombre inteligente para un solo archivo
            val cursor = context.contentResolver.query(uris[0], null, null, null, null)
            cursor?.use { c ->
                val nameIndex = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (c.moveToFirst()) {
                    val fullPath = c.getString(nameIndex)
                    val realFileName = fullPath.substringBeforeLast(".")
                    if (categoria.isBlank() || (tipo == "MUSICA" && categoria.startsWith("MUSICA_"))) {
                        categoria = realFileName
                    }
                }
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Subir Audios") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // 1. Selección de Deporte
                Text("Deporte:", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
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

                // 2. Selección de Tipo
                Text("Tipo:", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = tipo == "FX", onClick = { tipo = "FX"; customId = null; categoria = "" })
                    Text("FX")
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = tipo == "MUSICA", onClick = { tipo = "MUSICA"; customId = null; categoria = "" })
                    Text("Música")
                }

                // 3. Botones Rápidos si es FX y solo un archivo
                if (tipo == "FX" && selectedUris.size <= 1) {
                    Text("Acciones Rápidas:", style = MaterialTheme.typography.labelMedium)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        sugerenciasFX[deporte]?.forEach { (label, id) ->
                            FilterChip(
                                selected = customId == id,
                                onClick = { 
                                    customId = id
                                    categoria = label
                                },
                                label = { Text(label) }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = categoria,
                    onValueChange = { categoria = it },
                    label = { Text(if(selectedUris.size > 1) "Categoría para el lote" else "Nombre / Categoría") },
                    placeholder = { Text("Ej: Musica de Ambiente") },
                    modifier = Modifier.fillMaxWidth()
                )

                // 4. Selección de Archivos
                Divider()
                Button(
                    onClick = { launcher.launch("audio/*") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.AudioFile, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (selectedUris.isEmpty()) "Seleccionar Archivos" else "${selectedUris.size} archivos elegidos")
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = selectedUris.isNotEmpty() && (categoria.isNotBlank() || selectedUris.size > 1),
                onClick = { onConfirm(selectedUris, tipo, categoria, deporte, customId) }
            ) {
                Text("SUBIR TODO", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("CANCELAR") }
        }
    )
}
