package com.example.arki_deportes.ui.menciones

import android.content.ActivityNotFoundException
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MencionesRoute(
    viewModel: MencionesViewModel,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null
) {
    val state by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(state.errorMessage, state.successMessage) {
        val message = state.errorMessage ?: state.successMessage
        if (message != null) {
            snackbarHostState.showSnackbar(message)
            viewModel.clearMessages()
        }
    }

    val onShare: (String) -> Unit = remember(context, snackbarHostState, scope) {
        { text ->
            if (text.isBlank()) {
                scope.launch { snackbarHostState.showSnackbar("No hay texto para compartir") }
            } else {
                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                    setPackage("com.whatsapp")
                }
                val shareIntent = Intent.createChooser(sendIntent, "Compartir mención")
                try {
                    context.startActivity(shareIntent)
                } catch (_: ActivityNotFoundException) {
                    scope.launch {
                        snackbarHostState.showSnackbar("WhatsApp no está instalado")
                    }
                }
            }
        }
    }

    val onCopy: (String) -> Unit = remember(clipboardManager, snackbarHostState, scope) {
        {
            if (it.isBlank()) {
                scope.launch { snackbarHostState.showSnackbar("No hay texto para copiar") }
            } else {
                clipboardManager.setText(AnnotatedString(it))
                scope.launch { snackbarHostState.showSnackbar("Texto copiado al portapapeles") }
            }
        }
    }

    MencionesScreen(
        state = state,
        onTextoChange = viewModel::onTextoChange,
        onToggleActivo = viewModel::onToggleActivo,
        onGuardar = viewModel::guardarMencion,
        onMove = viewModel::onMove,
        onDragEnd = viewModel::onDragEnd,
        onShare = onShare,
        onCopy = onCopy,
        snackbarHostState = snackbarHostState,
        modifier = modifier,
        onNavigateBack = onNavigateBack
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MencionesScreen(
    state: MencionesUiState,
    onTextoChange: (String, String) -> Unit,
    onToggleActivo: (String, Boolean) -> Unit,
    onGuardar: (String) -> Unit,
    onMove: (Int, Int) -> Unit,
    onDragEnd: () -> Unit,
    onShare: (String) -> Unit,
    onCopy: (String) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    onNavigateBack: (() -> Unit)? = null
) {
    val reorderState = rememberReorderableLazyListState(
        onMove = { from, to -> onMove(from.index, to.index) },
        onDragEnd = { _, _ -> onDragEnd() }
    )

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(text = "Menciones") },
                navigationIcon = onNavigateBack?.let {
                    {
                        IconButton(onClick = it) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver"
                            )
                        }
                    }
                },
                scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isOrderSaving) {
                LinearProgressIndicator(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                )
            }

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                state.menciones.isEmpty() -> {
                    EmptyMencionesState(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp)
                    )
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp)
                            .reorderable(reorderState)
                            .detectReorderAfterLongPress(reorderState),
                        state = reorderState.listState,
                        contentPadding = PaddingValues(vertical = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(
                            state.menciones,
                            key = { it.id }
                        ) { item ->
                            ReorderableItem(
                                reorderState,
                                key = item.id
                            ) { isDragging ->
                                val elevation by animateDpAsState(
                                    targetValue = if (isDragging) 8.dp else 2.dp,
                                    label = "mencion-card-elevation"
                                )
                                MencionCard(
                                    item = item,
                                    onTextoChange = { onTextoChange(item.id, it) },
                                    onToggleActivo = { onToggleActivo(item.id, it) },
                                    onGuardar = { onGuardar(item.id) },
                                    onShare = { onShare(item.texto) },
                                    onCopy = { onCopy(item.texto) },
                                    elevation = elevation,
                                    dragHandleModifier = Modifier.detectReorder(reorderState)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyMencionesState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "No hay menciones configuradas",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.size(12.dp))
        Text(
            text = "Crea tus menciones desde el panel web para administrarlas aquí.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun MencionCard(
    item: MencionItemUiState,
    onTextoChange: (String) -> Unit,
    onToggleActivo: (Boolean) -> Unit,
    onGuardar: () -> Unit,
    onShare: () -> Unit,
    onCopy: () -> Unit,
    elevation: androidx.compose.ui.unit.Dp,
    dragHandleModifier: Modifier = Modifier
) {
    val backgroundColor = if (item.activo) {
        MaterialTheme.colorScheme.surface
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultValue = elevation)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Filled.DragHandle,
                    contentDescription = "Mover mención",
                    modifier = dragHandleModifier
                )
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = item.tipo.ifBlank { "Sin tipo" },
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "#${item.orden + 1}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (item.isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.size(8.dp))
                    }
                    Switch(
                        checked = item.activo,
                        onCheckedChange = onToggleActivo,
                        enabled = !item.isSaving
                    )
                }
            }

            AssistChip(
                onClick = {},
                label = { Text(text = item.tipo.ifBlank { "General" }) },
                enabled = false,
                border = AssistChipDefaults.assistChipBorder(
                    borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                )
            )

            var textFieldValue by remember(item.id) { mutableStateOf(item.texto) }
            LaunchedEffect(item.texto) {
                if (textFieldValue != item.texto) {
                    textFieldValue = item.texto
                }
            }

            OutlinedTextField(
                value = textFieldValue,
                onValueChange = {
                    textFieldValue = it
                    onTextoChange(it)
                },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                maxLines = 6,
                enabled = !item.isSaving,
                label = { Text(text = "Texto de la mención") }
            )

            Text(
                text = "Última actualización: ${formatTimestamp(item.timestamp)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = onCopy,
                    enabled = item.texto.isNotBlank() && !item.isSaving
                ) {
                    Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = "Copiar")
                }

                OutlinedButton(
                    onClick = onShare,
                    enabled = item.texto.isNotBlank() && !item.isSaving
                ) {
                    Icon(imageVector = Icons.Filled.Share, contentDescription = null)
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = "WhatsApp")
                }

                Spacer(modifier = Modifier.weight(1f))

                AnimatedVisibility(visible = item.isDirty || item.isSaving) {
                    OutlinedButton(
                        onClick = onGuardar,
                        enabled = item.isDirty && !item.isSaving
                    ) {
                        if (item.isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(text = "Guardar")
                        }
                    }
                }
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    if (timestamp <= 0L) return "Sin registros"
    val formatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    return formatter.format(Date(timestamp))
}
