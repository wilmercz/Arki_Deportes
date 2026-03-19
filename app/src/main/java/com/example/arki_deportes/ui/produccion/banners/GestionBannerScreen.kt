package com.example.arki_deportes.ui.produccion.banners

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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.arki_deportes.data.model.BannerResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionBannerScreen(
    viewModel: GestionBannerViewModel,
    onBack: () -> Unit,
    onOpenDrawer: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Banners") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar Banner")
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier
            .padding(padding)
            .fillMaxSize()) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            } else if (uiState.banners.isEmpty()) {
                Text(
                    text = "No hay banners registrados",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(uiState.banners) { banner ->
                        BannerItem(
                            banner = banner,
                            onDelete = { viewModel.deleteBanner(banner.id) },
                            onTransmit = { viewModel.publishInOverlay(banner) }
                        )
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
                        Text("Guardando recurso...")
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddBannerDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { banner, imgUri, vidUri ->
                viewModel.saveBanner(banner, imgUri, vidUri)
                showAddDialog = false
            },
            existingBanners = uiState.banners // 🚩 AGREGA ESTA LÍNEA AQUÍ
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
fun BannerItem(banner: BannerResource, onDelete: () -> Unit, onTransmit: () -> Unit) {
    ListItem(
        headlineContent = { Text(banner.nombre) },
        supportingContent = { Text("Tipo: ${banner.tipo} - ${if (banner.activo) "Activo" else "Inactivo"}") },
        leadingContent = {
            Icon(
                imageVector = when(banner.tipo) {
                    "IMAGEN" -> Icons.Default.Image
                    "VIDEO" -> Icons.Default.Movie
                    "HTML" -> Icons.Default.Code
                    else -> Icons.Default.AdUnits
                },
                contentDescription = null
            )
        },
        trailingContent = {
            Row {
                IconButton(onClick = onTransmit) {
                    Icon(Icons.Default.Upload, contentDescription = "Enviar a Overlay", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddBannerDialog(
    onDismiss: () -> Unit,
    onConfirm: (BannerResource, Uri?, Uri?) -> Unit,
    existingBanners: List<BannerResource>
) {
    var nombre by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("IMAGEN") }
    var urlExistente by remember { mutableStateOf("") }
    var modoSubida by remember { mutableStateOf(true) }

    var codigoHtml by remember { mutableStateOf("") }
    var fechaInicio by remember { mutableStateOf("") }
    var fechaFin by remember { mutableStateOf("") }

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var videoUri by remember { mutableStateOf<Uri?>(null) }

    val imageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { imageUri = it }
    val videoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { videoUri = it }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configurar Recurso") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre descriptivo") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text("Tipo de Recurso:", style = MaterialTheme.typography.labelLarge)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = tipo == "IMAGEN", onClick = { tipo = "IMAGEN" })
                        Text("Imagen")
                        Spacer(modifier = Modifier.width(8.dp))
                        RadioButton(selected = tipo == "VIDEO", onClick = { tipo = "VIDEO" })
                        Text("Video")
                        Spacer(modifier = Modifier.width(8.dp))
                        RadioButton(selected = tipo == "HTML", onClick = { tipo = "HTML" })
                        Text("HTML")
                    }
                }

                // --- LÓGICA PARA IMAGEN Y VIDEO (SUBIDA O CATÁLOGO) ---
                if (tipo != "HTML") {
                    item {
                        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                            SegmentedButton(
                                selected = modoSubida,
                                onClick = { modoSubida = true },
                                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                            ) { Text("Subir Nuevo") }
                            SegmentedButton(
                                selected = !modoSubida,
                                onClick = { modoSubida = false },
                                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                            ) { Text("Catálogo") }
                        }
                    }

                    if (modoSubida) {
                        item {
                            Button(
                                onClick = {
                                    if(tipo == "IMAGEN") imageLauncher.launch("image/*")
                                    else videoLauncher.launch("video/*")
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                val seleccionado = if(tipo == "IMAGEN") imageUri != null else videoUri != null
                                Icon(if(seleccionado) Icons.Default.CheckCircle else Icons.Default.FileUpload, null)
                                Spacer(Modifier.width(8.dp))
                                Text(if (seleccionado) "Archivo Seleccionado" else "Elegir Archivo Local")
                            }
                        }
                    } else {
                        // --- VISTA DE CATÁLOGO (REUTILIZAR) ---
                        item { Text("Selecciona de recursos existentes:", style = MaterialTheme.typography.labelMedium) }
                        items(existingBanners.filter { it.tipo == tipo }.distinctBy { it.urlImagen.ifBlank { it.urlVideo } }) { banner ->
                            val url = if (banner.tipo == "IMAGEN") banner.urlImagen else banner.urlVideo
                            if (url.isNotBlank()) {
                                Card(
                                    onClick = { urlExistente = url; if (nombre.isBlank()) nombre = banner.nombre },
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (urlExistente == url) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    ListItem(
                                        headlineContent = { Text(banner.nombre, maxLines = 1) },
                                        leadingContent = {
                                            if (banner.tipo == "IMAGEN") AsyncImage(model = url, contentDescription = null, modifier = Modifier.size(40.dp))
                                            else Icon(Icons.Default.Movie, null)
                                        },
                                        trailingContent = { if (urlExistente == url) Icon(Icons.Default.CheckCircle, null, tint = MaterialTheme.colorScheme.primary) }
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // --- LÓGICA PARA HTML (SOLO TEXTO) ---
                    item {
                        OutlinedTextField(
                            value = codigoHtml,
                            onValueChange = { codigoHtml = it },
                            label = { Text("Código HTML / Scripts") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 4
                        )
                    }
                }

                // Campos de fecha (Opcionales para todos)
                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = fechaInicio, onValueChange = { fechaInicio = it }, label = { Text("Inicio") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = fechaFin, onValueChange = { fechaFin = it }, label = { Text("Fin") }, modifier = Modifier.weight(1f))
                    }
                }
            }
        },
        confirmButton = {
            // Validación inteligente: permite guardar si hay archivo local, url de catálogo o código HTML
            val esValido = nombre.isNotBlank() && (
                    (tipo == "HTML" && codigoHtml.isNotBlank()) ||
                            (modoSubida && (imageUri != null || videoUri != null)) ||
                            (!modoSubida && urlExistente.isNotBlank())
                    )
            TextButton(
                enabled = esValido,
                onClick = {
                    val finalBanner = BannerResource(
                        nombre = nombre,
                        tipo = tipo,
                        codigoHtml = codigoHtml,
                        fechaInicio = fechaInicio,
                        fechaFin = fechaFin,
                        urlImagen = if (!modoSubida && tipo == "IMAGEN") urlExistente else "",
                        urlVideo = if (!modoSubida && tipo == "VIDEO") urlExistente else ""
                    )
                    onConfirm(finalBanner, if (modoSubida) imageUri else null, if (modoSubida) videoUri else null)
                }
            ) { Text("Vincular Recurso") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}

