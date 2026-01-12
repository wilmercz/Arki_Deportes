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

        Log.d(TAG, "═══════════════════════════════════════")
        Log.d(TAG, "🔍 OBTENIENDO PARTIDOS")
        Log.d(TAG, "   Fecha referencia: $fechaReferencia")
        Log.d(TAG, "   Rango: ±$dias días")
        Log.d(TAG, "═══════════════════════════════════════")

        val reference = database.reference
            .child(nodoRaiz)
            .child("DatosFutbol")
            .child("Campeonatos")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val start = fechaReferencia.minusDays(dias)
                val end = fechaReferencia.plusDays(dias)

                val todosPartidos = mutableListOf<Partido>()

                Log.d(TAG, "📥 Snapshot recibido: ${snapshot.childrenCount} campeonatos")
                Log.d(TAG, "")

                snapshot.children.forEachIndexed { index, campeonatoSnapshot ->
                    val campeonatoId = campeonatoSnapshot.key ?: return@forEachIndexed
                    Log.d(TAG, "📂 Campeonato ${index + 1}: $campeonatoId")

                    val partidosSnapshot = campeonatoSnapshot.child("Partidos")
                    val partidosCount = partidosSnapshot.childrenCount
                    Log.d(TAG, "   └─ Partidos en este campeonato: $partidosCount")

                    partidosSnapshot.children.forEachIndexed { pIndex, partidoSnapshot ->
                        try {
                            val partido = partidoSnapshot.getValue(Partido::class.java)

                            if (partido != null) {
                                // ✅ NUEVA LÓGICA: Usar Equipo1/Equipo2 o CodigoEquipo1/CodigoEquipo2
                                val equipo1 = if (partido.Equipo1.isNotBlank()) {
                                    partido.Equipo1
                                } else {
                                    partido.CodigoEquipo1.ifBlank { "Equipo 1" }
                                }

                                val equipo2 = if (partido.Equipo2.isNotBlank()) {
                                    partido.Equipo2
                                } else {
                                    partido.CodigoEquipo2.ifBlank { "Equipo 2" }
                                }

                                // ✅ NUEVA LÓGICA: Si no hay FECHA_PARTIDO, usar FECHAALTA o incluir siempre
                                val fechaPartido = partido.FECHA_PARTIDO?.trim()

                                if (fechaPartido.isNullOrBlank()) {
                                    // No tiene fecha: incluir de todas formas
                                    Log.d(TAG, "   ⚽ $equipo1 vs $equipo2 (sin fecha → incluido)")
                                    todosPartidos.add(partido)
                                } else {
                                    // Tiene fecha: verificar rango
                                    val fecha = parseFecha(fechaPartido)
                                    if (fecha != null && !fecha.isBefore(start) && !fecha.isAfter(end)) {
                                        Log.d(TAG, "   ⚽ $equipo1 vs $equipo2 (fecha: $fechaPartido)")
                                        todosPartidos.add(partido)
                                    } else {
                                        Log.d(TAG, "   ⏭️ $equipo1 vs $equipo2 (fuera de rango)")
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.e(TAG, "   ❌ Error parseando partido ${pIndex + 1}: ${e.message}")
                        }
                    }

                    Log.d(TAG, "")
                }

                // Ordenar por fecha
                val partidosOrdenados = todosPartidos.sortedWith(
                    compareBy<Partido> { parseFechaHora(it) ?: LocalDateTime.MAX }
                        .thenBy { it.CODIGOPARTIDO }
                )

                Log.d(TAG, "═══════════════════════════════════════")
                Log.d(TAG, "📊 RESUMEN ANTES DE FILTRAR")
                Log.d(TAG, "   Campeonatos procesados: ${snapshot.childrenCount}")
                Log.d(TAG, "   Total partidos encontrados: ${partidosOrdenados.size}")
                Log.d(TAG, "═══════════════════════════════════════")
                Log.d(TAG, "")
                Log.d(TAG, "═══════════════════════════════════════")
                Log.d(TAG, "✅ RESULTADO FINAL")
                Log.d(TAG, "   Total encontrados: ${partidosOrdenados.size}")
                Log.d(TAG, "   Después de filtrar: ${partidosOrdenados.size}")
                Log.d(TAG, "   Rango: $start a $end")
                Log.d(TAG, "═══════════════════════════════════════")

                if (!continuation.isCompleted) {
                    continuation.resume(partidosOrdenados)
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