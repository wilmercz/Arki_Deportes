package com.example.arki_deportes.ui.catalogs.partidos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.arki_deportes.data.model.Partido
import com.example.arki_deportes.util.ShareUtils

/**
 * Pantalla de formulario/edición del partido.
 */
@Composable
fun PartidoFormScreen(
    partido: Partido,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Resumen del partido",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        SharePreviewCard(partido = partido)

        Button(
            onClick = {
                ShareUtils.sharePartidoViaWhatsApp(
                    context = context,
                    partido = partido,
                    chooserTitle = "Compartir partido"
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Outlined.Share, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Compartir por WhatsApp")
        }
    }
}

@Composable
private fun SharePreviewCard(partido: Partido) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        /* desactivado temporalmente para compilar
        Text(
            text = ShareUtils.buildPartidoShareMessage(partido),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp)
        )

         */
    }
}
