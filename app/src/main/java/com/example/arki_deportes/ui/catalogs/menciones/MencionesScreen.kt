package com.example.arki_deportes.ui.catalogs.menciones

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.arki_deportes.util.ShareUtils

/** Modelo simple para representar una mención dentro del catálogo. */
data class MencionUi(
    val titulo: String,
    val descripcion: String,
    val enlace: String? = null,
    val hashtags: List<String> = emptyList()
)

/**
 * Pantalla que lista las menciones disponibles.
 */
@Composable
fun MencionesScreen(
    menciones: List<MencionUi>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    if (menciones.isEmpty()) {
        EmptyMencionesState(modifier = modifier)
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(menciones) { mencion ->
            MencionCard(
                mencion = mencion,
                onShare = {
                    ShareUtils.shareMentionViaWhatsApp(
                        context = context,
                        title = mencion.titulo,
                        description = mencion.descripcion,
                        link = mencion.enlace,
                        hashtags = mencion.hashtags,
                        chooserTitle = "Compartir mención"
                    )
                }
            )
        }
    }
}

@Composable
private fun MencionCard(
    mencion: MencionUi,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = mencion.titulo,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = mencion.descripcion,
                style = MaterialTheme.typography.bodyMedium
            )
            mencion.enlace?.takeIf { it.isNotBlank() }?.let { enlace ->
                Text(
                    text = enlace,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            val preview = ShareUtils.buildMentionShareMessage(
                title = mencion.titulo,
                description = mencion.descripcion,
                link = mencion.enlace,
                hashtags = mencion.hashtags
            )
            Text(
                text = preview,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(
                onClick = onShare,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Outlined.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Compartir por WhatsApp")
            }
        }
    }
}

@Composable
private fun EmptyMencionesState(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Aún no hay menciones registradas.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
