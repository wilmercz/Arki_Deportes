package com.example.arki_deportes.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * EMPTY STATE - COMPONENTE PARA LISTAS VACÍAS
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Muestra un mensaje amigable cuando no hay datos para mostrar,
 * con opción de incluir un botón de acción.
 */
@Composable
fun EmptyState(
    icon: ImageVector = Icons.Default.Info,
    title: String = "No hay datos",
    message: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(32.dp)
        ) {
            // Ícono
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                modifier = Modifier.size(72.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Título
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Mensaje
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            // Botón de acción (opcional)
            if (actionText != null && onActionClick != null) {
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onActionClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Text(text = actionText)
                }
            }
        }
    }
}

/**
 * Empty State específico para cuando no hay campeonato seleccionado
 */
@Composable
fun NoCampeonatoSeleccionadoEmptyState(
    onOpenDrawer: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.EmojiEvents,
        title = "Selecciona un campeonato",
        message = "Para ver los datos, primero debes seleccionar un campeonato desde el menú lateral.",
        actionText = "Abrir Menú",
        onActionClick = onOpenDrawer,
        modifier = modifier
    )
}

/**
 * Empty State específico para cuando el campeonato no tiene datos
 */
@Composable
fun CampeonatoSinDatosEmptyState(
    campeonatoNombre: String,
    tipoDato: String, // "equipos", "grupos", "partidos"
    onAgregar: () -> Unit,
    modifier: Modifier = Modifier
) {
    EmptyState(
        icon = Icons.Default.Info,
        title = "Sin $tipoDato",
        message = "El campeonato \"$campeonatoNombre\" aún no tiene $tipoDato registrados.",
        actionText = "Agregar ${tipoDato.replaceFirstChar { it.uppercase() }}",
        onActionClick = onAgregar,
        modifier = modifier
    )
}
