package com.example.arki_deportes.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsRoute(
    nodeValue: String,
    onNodeValueChange: (String) -> Unit,
    onSaveNode: () -> Unit,
    onResetNode: () -> Unit,
    onLogout: () -> Unit,
    onOpenDrawer: (() -> Unit)? = null,
    onNavigateBack: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    lastSyncTimestamp: Long? = null
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val formattedSync = remember(lastSyncTimestamp) { formatTimestamp(lastSyncTimestamp) }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "Configuración") },
                navigationIcon = {
                    when {
                        onOpenDrawer != null -> {
                            IconButton(onClick = onOpenDrawer) {
                                Icon(
                                    imageVector = Icons.Filled.Menu,
                                    contentDescription = "Abrir menú"
                                )
                            }
                        }
                        onNavigateBack != null -> {
                            IconButton(onClick = onNavigateBack) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Regresar"
                                )
                            }
                        }
                    }
                },
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Firebase",
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Actualiza el nodo raíz utilizado para leer y escribir datos en Firebase.",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value = nodeValue,
                        onValueChange = { onNodeValueChange(it.uppercase(Locale.getDefault())) },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text(text = "Nodo raíz de Firebase") },
                        singleLine = true
                    )

                    formattedSync?.let { sync ->
                        Text(
                            text = "Última sincronización registrada: $sync",
                            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
                            color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Button(
                            onClick = {
                                onSaveNode()
                                scope.launch { snackbarHostState.showSnackbar("Nodo actualizado correctamente") }
                            },
                            enabled = nodeValue.isNotBlank(),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Guardar cambios")
                        }
                        OutlinedButton(
                            onClick = {
                                onResetNode()
                                scope.launch { snackbarHostState.showSnackbar("Nodo restablecido al valor por defecto") }
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(text = "Restablecer")
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Sesión",
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Cierra la sesión actual y elimina las credenciales almacenadas en el dispositivo.",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                        color = androidx.compose.material3.MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = onLogout,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Cerrar sesión")
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long?): String? {
    if (timestamp == null || timestamp <= 0) return null
    return runCatching {
        val instant = Instant.ofEpochMilli(timestamp)
        DateTimeFormatter
            .ofPattern("dd MMM yyyy - HH:mm", Locale.getDefault())
            .withZone(ZoneId.systemDefault())
            .format(instant)
    }.getOrNull()
}
