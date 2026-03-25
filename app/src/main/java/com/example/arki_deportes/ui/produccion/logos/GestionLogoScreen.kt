package com.example.arki_deportes.ui.produccion.logos

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.arki_deportes.data.model.LogoResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GestionLogoScreen(
    viewModel: GestionLogoViewModel,
    onOpenDrawer: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Logos") },
                navigationIcon = {
                    IconButton(onClick = onOpenDrawer) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddDialog = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar Logo")
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
            } else if (uiState.logos.isEmpty()) {
                Text(
                    text = "No hay logos registrados",
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.logos) { logo ->
                        LogoItem(
                            logo = logo,
                            onDelete = { viewModel.deleteLogo(logo.id) },
                            onToggleAir = { viewModel.toggleOnAir(logo) }
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
                        Text("Subiendo Logo a Cloudinary...")
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddLogoDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { nombre, uri ->
                viewModel.saveLogo(nombre, uri)
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
fun LogoItem(logo: LogoResource, onDelete: () -> Unit, onToggleAir: () -> Unit) {
    Card(
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (logo.onAir) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        ListItem(
            headlineContent = { Text(logo.nombre) },
            supportingContent = { 
                Text(
                    text = if (logo.onAir) "🔴 AL AIRE (Overlay)" else "⚪ En catálogo",
                    color = if (logo.onAir) Color(0xFFD32F2F) else MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelMedium
                )
            },
            leadingContent = {
                AsyncImage(
                    model = logo.url,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp).padding(4.dp),
                    contentScale = ContentScale.Fit
                )
            },
            trailingContent = {
                Row {
                    IconButton(onClick = onToggleAir) {
                        Icon(
                            imageVector = if (logo.onAir) Icons.Default.VisibilityOff else Icons.Default.Podcasts,
                            contentDescription = "Transmitir",
                            tint = if (logo.onAir) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                        )
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLogoDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Uri) -> Unit
) {
    var nombre by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { imageUri = it }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuevo Logo de Medio") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = nombre,
                    onValueChange = { nombre = it },
                    label = { Text("Nombre del Medio/Radio") },
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = { launcher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(if (imageUri != null) Icons.Default.CheckCircle else Icons.Default.FileUpload, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (imageUri != null) "Imagen Seleccionada" else "Elegir Logo (Imagen)")
                }
                
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        contentScale = ContentScale.Fit
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = nombre.isNotBlank() && imageUri != null,
                onClick = { onConfirm(nombre, imageUri!!) }
            ) { Text("Guardar y Subir") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } }
    )
}
