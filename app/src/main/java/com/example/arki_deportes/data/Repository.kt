package com.example.arki_deportes.data

import android.util.Log
import com.example.arki_deportes.data.local.ConfigManager
import com.example.arki_deportes.data.model.Mencion
import com.example.arki_deportes.data.model.Campeonato
import com.example.arki_deportes.data.model.EquipoProduccion
import com.example.arki_deportes.data.model.Partido
import com.example.arki_deportes.data.model.PartidoActual
import com.example.arki_deportes.utils.Constants
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
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

    companion object {
        private val TAG = Repository::class.java.simpleName

        private val fechaFormatters = listOf(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
        )
    }

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
     * Observa en tiempo real la lista de menciones configuradas.
     */
    fun observeMenciones(): Flow<List<Mencion>> = callbackFlow {
        val reference = database.reference
            .child(nodoRaiz)
            .child(Constants.FirebaseCollections.MENCIONES)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val menciones = snapshot.children
                    .mapNotNull { child ->
                        val mencion = child.getValue(Mencion::class.java)
                        val id = child.key ?: return@mapNotNull null
                        mencion?.copy(id = id)
                    }
                    .sortedBy { it.orden }
                trySend(menciones)
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }

        reference.addValueEventListener(listener)

        awaitClose { reference.removeEventListener(listener) }
    }

    /**
     * Actualiza el orden de las menciones en Firebase.
     */
    suspend fun actualizarOrdenMenciones(menciones: List<Mencion>) {
        suspendCancellableCoroutine<Unit> { continuation ->
            val updates = menciones
                .filter { it.id.isNotBlank() }
                .associate { mencion ->
                    "${mencion.id}/orden" to mencion.orden
                }

            if (updates.isEmpty()) {
                if (!continuation.isCompleted) {
                    continuation.resume(Unit)
                }
                return@suspendCancellableCoroutine
            }

            val reference = database.reference
                .child(nodoRaiz)
                .child(Constants.FirebaseCollections.MENCIONES)

            reference.updateChildren(updates)
                .addOnSuccessListener {
                    if (!continuation.isCompleted) {
                        continuation.resume(Unit)
                    }
                }
                .addOnFailureListener { exception ->
                    if (!continuation.isCompleted) {
                        continuation.resumeWithException(exception)
                    }
                }

            continuation.invokeOnCancellation {
                // Cancelación manejada por Firebase internamente
            }
        }
    }

    /**
     * Obtiene la lista completa de campeonatos registrados en Firebase.
     */
    suspend fun obtenerCampeonatos(): List<Campeonato> = suspendCancellableCoroutine { continuation ->
        val reference = database.reference
            .child(nodoRaiz)
            .child(Constants.FirebaseCollections.CAMPEONATOS)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val campeonatos = snapshot.children
                    .mapNotNull { it.getValue(Campeonato::class.java) }
                    .sortedBy { it.CAMPEONATO.uppercase(Locale.getDefault()) }

                if (!continuation.isCompleted) {
                    continuation.resume(campeonatos)
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

    /**
     * Obtiene la configuración del equipo de producción. Si no existe retorna
     * un objeto vacío.
     */
    suspend fun obtenerEquipoProduccion(campeonatoCodigo: String? = null): EquipoProduccion =
        suspendCancellableCoroutine { continuation ->
            val reference = equipoProduccionReference(campeonatoCodigo)

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val equipo = snapshot.getValue(EquipoProduccion::class.java)
                        ?: EquipoProduccion.empty()

                    if (!continuation.isCompleted) {
                        continuation.resume(equipo)
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

    /**
     * Guarda la configuración del equipo de producción en el nodo indicado.
     */
    suspend fun guardarEquipoProduccion(
        equipo: EquipoProduccion,
        campeonatoCodigo: String? = null
    ): Unit = suspendCancellableCoroutine { continuation ->
        val reference = equipoProduccionReference(campeonatoCodigo)
        val data = equipo.normalized().copy(timestamp = System.currentTimeMillis())

        reference.setValue(data)
            .addOnSuccessListener {
                if (!continuation.isCompleted) {
                    continuation.resume(Unit)
                }
            }
            .addOnFailureListener { error ->
                if (!continuation.isCompleted) {
                    continuation.resumeWithException(error)
                }
            }

        continuation.invokeOnCancellation {
            // Cancelación manejada por Firebase internamente
        }
    }

    /**
     * Actualiza una mención individual en Firebase.
     */
    suspend fun actualizarMencion(mencion: Mencion): Unit =
        suspendCancellableCoroutine { continuation ->
            if (mencion.id.isBlank()) {
                if (!continuation.isCompleted) {
                    continuation.resumeWithException(
                        IllegalArgumentException("El ID de la mención no puede estar vacío")
                    )
                }
                return@suspendCancellableCoroutine
            }

            val reference = database.reference
                .child(nodoRaiz)
                .child(Constants.FirebaseCollections.MENCIONES)
                .child(mencion.id)

            reference.setValue(mencion)
                .addOnSuccessListener {
                    if (!continuation.isCompleted) {
                        continuation.resume(Unit)
                    }
                }
                .addOnFailureListener { exception ->
                    if (!continuation.isCompleted) {
                        continuation.resumeWithException(exception)
                    }
                }

            continuation.invokeOnCancellation {
                // Cancelación manejada por Firebase internamente
            }
        }

    /**
     * Referencia privada al nodo de EquipoProduccion según el campeonato.
     */
    private fun equipoProduccionReference(campeonatoCodigo: String?): DatabaseReference {
        val baseReference = database.reference
            .child(nodoRaiz)
            .child(Constants.FirebaseCollections.EQUIPO_PRODUCCION)

        return if (campeonatoCodigo.isNullOrBlank()) {
            baseReference.child(Constants.EquipoProduccionPaths.DEFAULT)
        } else {
            baseReference
                .child(Constants.EquipoProduccionPaths.CAMPEONATOS)
                .child(campeonatoCodigo)
        }
    }

    /**
     * Obtiene la lista de partidos en un rango de días alrededor de la fecha de referencia.
     */
    suspend fun obtenerPartidosRango(
        fechaReferencia: LocalDate = LocalDate.now(),
        dias: Long = 7L
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

                val total = snapshot.childrenCount
                val descartados = total - partidos.size.toLong()
                Log.d(
                    TAG,
                    "obtenerPartidosRango: total=$total, enRango=${partidos.size}, descartados=$descartados"
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


    /**
     * Parsea fecha y hora de un partido para ordenamiento
     */
    private fun parseFecha(fecha: String?): LocalDate? {
        val texto = fecha?.trim().orEmpty()
        if (texto.isBlank()) return null

        val formatos = listOf(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy")
        )

        formatos.forEach { formatter ->
            runCatching { return LocalDate.parse(texto, formatter) }
        }

        Log.w(TAG, "No se pudo parsear la fecha: $texto")
        return null
    }

    /**
     * Parsea una hora en formato HH:mm o HH:mm:ss.
     */
    private fun parseHora(hora: String?): LocalTime? {
        val texto = hora?.trim().orEmpty()
        if (texto.isBlank()) return null

        val formatos = listOf(
            DateTimeFormatter.ofPattern("HH:mm"),
            DateTimeFormatter.ofPattern("HH:mm:ss")
        )

        formatos.forEach { formatter ->
            runCatching { return LocalTime.parse(texto, formatter) }
        }

        return null
    }

    private fun parseFechaHora(partido: Partido): LocalDateTime? {
        val fecha = parseFecha(partido.FECHA_PARTIDO) ?: return null
        val hora = parseHora(partido.HORA_PARTIDO) ?: LocalTime.MIDNIGHT
        return LocalDateTime.of(fecha, hora)
    }
}