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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
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
    onConfirm: (BannerResource, Uri?, Uri?) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var tipo by remember { mutableStateOf("IMAGEN") }
    var codigoHtml by remember { mutableStateOf("") }
    var fechaInicio by remember { mutableStateOf("") }
    var fechaFin by remember { mutableStateOf("") }
    
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var videoUri by remember { mutableStateOf<Uri?>(null) }

    val imageLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> imageUri = uri }

    val videoLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? -> videoUri = uri }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Banner Publicitario") },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                item {
                    OutlinedTextField(
                        value = nombre,
                        onValueChange = { nombre = it },
                        label = { Text("Nombre del Banner") },
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

                if (tipo == "IMAGEN") {
                    item {
                        Button(onClick = { imageLauncher.launch("image/*") }, modifier = Modifier.fillMaxWidth()) {
                            Text(if (imageUri == null) "Seleccionar Imagen" else "Imagen Seleccionada ✅")
                        }
                    }
                }

                if (tipo == "VIDEO") {
                    item {
                        Button(onClick = { videoLauncher.launch("video/*") }, modifier = Modifier.fillMaxWidth()) {
                            Text(if (videoUri == null) "Seleccionar Video MP4" else "Video Seleccionado ✅")
                        }
                    }
                }

                if (tipo == "HTML") {
                    item {
                        OutlinedTextField(
                            value = codigoHtml,
                            onValueChange = { codigoHtml = it },
                            label = { Text("Código HTML / Scripts") },
                            modifier = Modifier.fillMaxWidth(),
                            minLines = 3
                        )
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = fechaInicio,
                            onValueChange = { fechaInicio = it },
                            label = { Text("Inicio (opcional)") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = fechaFin,
                            onValueChange = { fechaFin = it },
                            label = { Text("Fin (opcional)") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = nombre.isNotBlank() && (imageUri != null || videoUri != null || codigoHtml.isNotBlank()),
                onClick = {
                    val banner = BannerResource(
                        nombre = nombre,
                        tipo = tipo,
                        codigoHtml = codigoHtml,
                        fechaInicio = fechaInicio,
                        fechaFin = fechaFin
                    )
                    onConfirm(banner, imageUri, videoUri)
                }
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
