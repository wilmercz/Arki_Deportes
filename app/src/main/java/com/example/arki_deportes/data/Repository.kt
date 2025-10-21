package com.example.arki_deportes.data

import com.example.arki_deportes.data.local.ConfigManager
import com.example.arki_deportes.data.model.Partido
import com.example.arki_deportes.data.model.PartidoActual
import com.example.arki_deportes.utils.Constants
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Repositorio principal encargado de leer información desde Firebase Realtime Database.
 */
class Repository(
    private val database: FirebaseDatabase,
    private val configManager: ConfigManager
) {

    private val nodoRaiz: String
        get() = configManager.obtenerNodoRaiz()

    /**
     * Se subscribe a los cambios del nodo PartidoActual y emite actualizaciones en vivo.
     */
    fun observePartidoActual(): Flow<PartidoActual> = callbackFlow {
        val reference = database.reference
            .child(nodoRaiz)
            .child(Constants.FirebaseCollections.PARTIDO_ACTUAL)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val partido = snapshot.getValue(PartidoActual::class.java)
                    ?.normalizado()
                    ?: PartidoActual.empty()
                trySend(partido)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        reference.addValueEventListener(listener)

        awaitClose { reference.removeEventListener(listener) }
    }

    /**
     * Obtiene la lista de partidos en un rango de días alrededor de la fecha de referencia.
     */
    suspend fun obtenerPartidosRango(
        fechaReferencia: LocalDate = LocalDate.now(),
        dias: Long = Constants.DIAS_PARTIDOS_PASADOS.toLong()
    ): List<Partido> = suspendCancellableCoroutine { continuation ->
        val reference = database.reference
            .child(nodoRaiz)
            .child(Constants.FirebaseCollections.PARTIDOS)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val start = fechaReferencia.minusDays(dias)
                val end = fechaReferencia.plusDays(dias)

                val partidos = snapshot.children
                    .mapNotNull { it.getValue(Partido::class.java) }
                    .filter { partido ->
                        val fecha = parseFecha(partido.FECHA_PARTIDO)
                        fecha != null && !fecha.isBefore(start) && !fecha.isAfter(end)
                    }
                    .sortedWith(
                        compareBy<Partido> { parseFechaHora(it) ?: LocalDateTime.MAX }
                            .thenBy { it.CODIGOPARTIDO }
                    )

                if (!continuation.isCompleted) {
                    continuation.resume(partidos)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                if (!continuation.isCompleted) {
                    continuation.resumeWithException(error.toException())
                }
            }
        }

        reference.addListenerForSingleValueEvent(listener)

        continuation.invokeOnCancellation { reference.removeEventListener(listener) }
    }

    private fun parseFecha(fecha: String): LocalDate? = try {
        if (fecha.isBlank()) {
            null
        } else {
            LocalDate.parse(fecha.trim(), DateTimeFormatter.ISO_LOCAL_DATE)
        }
    } catch (_: Exception) {
        null
    }

    private fun parseHora(hora: String): LocalTime? = try {
        if (hora.isBlank()) {
            null
        } else {
            LocalTime.parse(hora.trim(), timeFormatter)
        }
    } catch (_: Exception) {
        null
    }

    private fun parseFechaHora(partido: Partido): LocalDateTime? {
        val fecha = parseFecha(partido.FECHA_PARTIDO) ?: return null
        val hora = parseHora(partido.HORA_PARTIDO) ?: LocalTime.MIDNIGHT
        return LocalDateTime.of(fecha, hora)
    }

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
}
