package com.example.arki_deportes.data.repository

import android.util.Log
import com.example.arki_deportes.data.model.Campeonato
import com.example.arki_deportes.data.model.Equipo
import com.example.arki_deportes.data.model.Grupo
import com.example.arki_deportes.data.model.Partido
import com.example.arki_deportes.utils.Constants
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.Query
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import com.google.firebase.database.ServerValue

/**
 * Repositorio centralizado para operaciones CRUD sobre catálogos en Firebase.
 *
 * Gestiona campeonatos, grupos, equipos y partidos utilizando el nodo raíz
 * configurado en la aplicación. Ofrece métodos de lectura reactiva mediante
 * [Flow] y operaciones suspendidas para crear, actualizar y eliminar.
 */
class FirebaseCatalogRepository(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(),
    private val rootNode: String = Constants.FIREBASE_NODO_RAIZ_DEFAULT
) {

    private val rootReference: DatabaseReference
        get() = database.reference.child(rootNode)

    private fun campeonatosReference(): DatabaseReference =
        rootReference.child(Constants.FirebaseCollections.CAMPEONATOS)

    private fun gruposReference(): DatabaseReference =
        rootReference.child(Constants.FirebaseCollections.GRUPOS)

    private fun equiposReference(): DatabaseReference =
        rootReference.child(Constants.FirebaseCollections.EQUIPOS)

    private fun partidosReference(): DatabaseReference =
        rootReference.child(Constants.FirebaseCollections.PARTIDOS)

    // ─────────────────────────────────────────────────────────────────────────────
    // Observadores
    // ─────────────────────────────────────────────────────────────────────────────

    fun observeCampeonatos(): Flow<List<Campeonato>> = callbackFlow {
        val reference = campeonatosReference()
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val campeonatos = snapshot.children.mapNotNull { child ->
                    child.getValue(Campeonato::class.java)
                }
                trySend(campeonatos.sortedBy { it.CAMPEONATO })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        reference.addValueEventListener(listener)
        awaitClose { reference.removeEventListener(listener) }
    }

    fun observeGrupos(campeonatoCodigo: String? = null): Flow<List<Grupo>> = callbackFlow {
        val reference = gruposReference()
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val grupos = snapshot.children.mapNotNull { child ->
                    child.getValue(Grupo::class.java)
                }.filter { grupo ->
                    campeonatoCodigo.isNullOrBlank() || grupo.CODIGOCAMPEONATO == campeonatoCodigo
                }
                trySend(grupos.sortedBy { it.GRUPO })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        reference.addValueEventListener(listener)
        awaitClose { reference.removeEventListener(listener) }
    }

    fun observeEquipos(
        campeonatoCodigo: String? = null,
        grupoCodigo: String? = null
    ): Flow<List<Equipo>> = callbackFlow {
        val reference: Query = if (campeonatoCodigo.isNullOrBlank()) {
            equiposReference()
        } else {
            equiposReference().orderByChild("CODIGOCAMPEONATO").equalTo(campeonatoCodigo)
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val equipos = snapshot.children.mapNotNull { child ->
                    val equipo = child.getValue(Equipo::class.java)
                    val equipoGrupo = child.child("CODIGOGRUPO").getValue(String::class.java)
                    if (equipo != null && (grupoCodigo.isNullOrBlank() || equipoGrupo == grupoCodigo)) {
                        equipo
                    } else {
                        null
                    }
                }
                trySend(equipos.sortedBy { it.getNombreDisplay() })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        reference.addValueEventListener(listener)
        awaitClose { reference.removeEventListener(listener) }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Operaciones de lectura única
    // ─────────────────────────────────────────────────────────────────────────────

    suspend fun getCampeonato(codigo: String): Campeonato? {
        val snapshot = campeonatosReference().child(codigo).get().await()
        return snapshot.getValue(Campeonato::class.java)
    }

    suspend fun getGrupo(codigoGrupo: String): Grupo? {
        val snapshot = gruposReference().child(codigoGrupo).get().await()
        return snapshot.getValue(Grupo::class.java)
    }

    suspend fun getEquipo(codigoEquipo: String): Equipo? {
        val snapshot = equiposReference().child(codigoEquipo).get().await()
        return snapshot.getValue(Equipo::class.java)
    }

    // FirebaseCatalogRepository.kt

    suspend fun getPartido(campeonatoId: String, partidoId: String): Partido? {
        return try {
            if (campeonatoId.isBlank() || partidoId.isBlank()) {
                Log.e("FirebaseCatalogRepo", "❌ IDs vacíos: campeonatoId='$campeonatoId', partidoId='$partidoId'")
                return null
            }

            val path = "$rootNode/DatosFutbol/Campeonatos/$campeonatoId/Partidos/$partidoId"

            Log.d("FirebaseCatalogRepo", "╔═══════════════════════════════════════════════════════")
            Log.d("FirebaseCatalogRepo", "🔍 BUSCANDO PARTIDO EN FIREBASE")
            Log.d("FirebaseCatalogRepo", "   Ruta: $path")
            Log.d("FirebaseCatalogRepo", "╚═══════════════════════════════════════════════════════")

            val snap = database.reference
                .child(rootNode)
                .child("DatosFutbol")
                .child("Campeonatos")
                .child(campeonatoId)
                .child("Partidos")
                .child(partidoId)
                .get()
                .await()

            if (!snap.exists()) {
                Log.e("FirebaseCatalogRepo", "╔═══════════════════════════════════════════════════════")
                Log.e("FirebaseCatalogRepo", "❌ SNAPSHOT NO EXISTE")
                Log.e("FirebaseCatalogRepo", "   Path: $path")
                Log.e("FirebaseCatalogRepo", "   snap.exists() = false")
                Log.e("FirebaseCatalogRepo", "╚═══════════════════════════════════════════════════════")
                return null
            }

            Log.d("FirebaseCatalogRepo", "✅ Snapshot existe, parseando datos...")

            // Mostrar TODOS los datos crudos de Firebase
            Log.d("FirebaseCatalogRepo", "╔═══════════════════════════════════════════════════════")
            Log.d("FirebaseCatalogRepo", "📋 DATOS CRUDOS DE FIREBASE:")
            snap.children.forEach { child ->
                Log.d("FirebaseCatalogRepo", "   ${child.key} = ${child.value}")
            }
            Log.d("FirebaseCatalogRepo", "╚═══════════════════════════════════════════════════════")

            val partido = snap.getValue(Partido::class.java)

            if (partido == null) {
                Log.e("FirebaseCatalogRepo", "╔═══════════════════════════════════════════════════════")
                Log.e("FirebaseCatalogRepo", "❌ ERROR AL PARSEAR PARTIDO")
                Log.e("FirebaseCatalogRepo", "   snap.getValue(Partido::class.java) retornó null")
                Log.e("FirebaseCatalogRepo", "   Posibles causas:")
                Log.e("FirebaseCatalogRepo", "   - Nombres de campos no coinciden")
                Log.e("FirebaseCatalogRepo", "   - Tipos de datos incorrectos")
                Log.e("FirebaseCatalogRepo", "   - Constructor sin argumentos faltante")
                Log.e("FirebaseCatalogRepo", "╚═══════════════════════════════════════════════════════")
                return null
            }

            Log.d("FirebaseCatalogRepo", "╔═══════════════════════════════════════════════════════")
            Log.d("FirebaseCatalogRepo", "✅ PARTIDO PARSEADO EXITOSAMENTE")
            Log.d("FirebaseCatalogRepo", "   CODIGOPARTIDO: '${partido.CODIGOPARTIDO}'")
            Log.d("FirebaseCatalogRepo", "   CAMPEONATOCODIGO: '${partido.CAMPEONATOCODIGO}'")
            Log.d("FirebaseCatalogRepo", "   EQUIPO1: '${partido.EQUIPO1}'")
            Log.d("FirebaseCatalogRepo", "   EQUIPO2: '${partido.EQUIPO2}'")
            Log.d("FirebaseCatalogRepo", "   CODIGOEQUIPO1: '${partido.CODIGOEQUIPO1}'")
            Log.d("FirebaseCatalogRepo", "   CODIGOEQUIPO2: '${partido.CODIGOEQUIPO2}'")
            Log.d("FirebaseCatalogRepo", "   GOLES1: ${partido.GOLES1}")
            Log.d("FirebaseCatalogRepo", "   GOLES2: ${partido.GOLES2}")
            Log.d("FirebaseCatalogRepo", "   NUMERODETIEMPO: '${partido.NumeroDeTiempo}'")
            Log.d("FirebaseCatalogRepo", "   TIEMPOJUEGO: '${partido.TIEMPOJUEGO}'")
            Log.d("FirebaseCatalogRepo", "╚═══════════════════════════════════════════════════════")

            partido.copy(CAMPEONATOCODIGO = campeonatoId)

        } catch (e: Exception) {
            Log.e("FirebaseCatalogRepo", "╔═══════════════════════════════════════════════════════")
            Log.e("FirebaseCatalogRepo", "❌ EXCEPCIÓN EN getPartido")
            Log.e("FirebaseCatalogRepo", "   Mensaje: ${e.message}")
            Log.e("FirebaseCatalogRepo", "   Tipo: ${e.javaClass.simpleName}")
            e.printStackTrace()
            Log.e("FirebaseCatalogRepo", "╚═══════════════════════════════════════════════════════")
            null
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Operaciones de guardado
    // ─────────────────────────────────────────────────────────────────────────────

    suspend fun saveCampeonato(campeonato: Campeonato) {
        campeonatosReference().child(campeonato.CODIGO)
            .setValue(campeonato.toMap())
            .await()
    }

    suspend fun saveGrupo(grupo: Grupo) {
        gruposReference().child(grupo.CODIGOGRUPO)
            .setValue(grupo.toMap())
            .await()
    }

    suspend fun saveEquipo(equipo: Equipo) {
        equiposReference().child(equipo.CODIGOEQUIPO)
            .setValue(equipo.toMap())
            .await()
    }

    suspend fun savePartido(partido: Partido) {
        partidosReference().child(partido.CODIGOPARTIDO)
            .setValue(partido.toMap())
            .await()
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Operaciones de eliminación
    // ─────────────────────────────────────────────────────────────────────────────

    suspend fun deleteCampeonato(codigo: String) {
        campeonatosReference().child(codigo).removeValue().await()
    }

    suspend fun deleteGrupo(codigoGrupo: String) {
        gruposReference().child(codigoGrupo).removeValue().await()
    }

    suspend fun deleteEquipo(codigoEquipo: String) {
        equiposReference().child(codigoEquipo).removeValue().await()
    }

    suspend fun deletePartido(codigoPartido: String) {
        partidosReference().child(codigoPartido).removeValue().await()
    }

    /**
     * Observa un partido específico en tiempo real
     *
     * Ruta Firebase: /DatosFutbol/Campeonatos/{campeonatoId}/Partidos/{partidoId}
     * VB.NET Equivalente: RutaPartidoFB = RaizFireBase & "/DatosFutbol/Campeonatos/" & CodigoCampeonato & "/Partidos/" & CodigoPartido
     *
     * @param campeonatoId ID del campeonato
     * @param partidoId ID del partido
     * @return Flow que emite el partido cuando hay cambios
     */
    fun observePartido(campeonatoId: String, partidoId: String): Flow<Partido> = callbackFlow {
        val reference = database.reference
            .child(rootNode)
            .child("DatosFutbol")
            .child("Campeonatos")
            .child(campeonatoId)
            .child("Partidos")
            .child(partidoId)

        Log.d("FirebaseCatalogRepo", "👁️ Observando: $partidoId en $campeonatoId")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val partido = snapshot.getValue(Partido::class.java)
                if (partido != null) {
                    Log.d("FirebaseCatalogRepo", "📥 Actualizado: ${partido.EQUIPO1} vs ${partido.EQUIPO2}")
                    trySend(partido)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseCatalogRepo", "❌ Error: ${error.message}")
                close(error.toException())
            }
        }

        reference.addValueEventListener(listener)
        awaitClose { reference.removeEventListener(listener) }
    }


    /**
     * Actualiza campos específicos de un partido
     *
     * Ruta Firebase: /DatosFutbol/Campeonatos/{campeonatoId}/Partidos/{partidoId}
     * VB.NET Equivalente: FirebaseManager.EnqueueSet(RutaPartidoFB & "CAMPO", valor, prioridad)
     *
     * @param campeonatoId ID del campeonato
     * @param partidoId ID del partido
     * @param updates Mapa de campos a actualizar
     * @return Result con éxito o error
     */
    suspend fun updatePartidoFields(
        campeonatoId: String,
        partidoId: String,
        updates: Map<String, Any?>
    ): Result<Unit> = try {
        val reference = database.reference
            .child(rootNode)
            .child("DatosFutbol")
            .child("Campeonatos")
            .child(campeonatoId)
            .child("Partidos")
            .child(partidoId)

        Log.d("FirebaseCatalogRepo", "💾 Actualizando: $partidoId")
        Log.d("FirebaseCatalogRepo", "   Campeonato: $campeonatoId")
        Log.d("FirebaseCatalogRepo", "   Campos: ${updates.keys}")

        reference.updateChildren(updates).await()

        Log.d("FirebaseCatalogRepo", "✅ Actualizado exitosamente")
        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("FirebaseCatalogRepo", "❌ Error: ${e.message}", e)
        Result.failure(e)
    }

    /**
     * Sincroniza el partido al nodo PARTIDOACTUAL (para overlays)
     *
     * Ruta Firebase: /PARTIDOACTUAL
     * VB.NET Equivalente: Nodo especial para overlays web
     *
     * @param partido Partido a sincronizar
     * @return Result con éxito o error
     */
    suspend fun sincronizarPartidoActual(partido: Partido): Result<Unit> = try {
        val reference = database.reference
            .child(rootNode)
            .child("PARTIDOACTUAL")

        val data = mapOf(
            // Equipos / marcador
            "EQUIPO1" to partido.EQUIPO1,
            "EQUIPO2" to partido.EQUIPO2,
            "GOLES1" to partido.GOLES1,
            "GOLES2" to partido.GOLES2,
            "ESQUINAS1" to partido.ESQUINAS1,
            "ESQUINAS2" to partido.ESQUINAS2,

            // Disciplina
            "TARJETAS_AMARILLAS1" to partido.TAMARILLAS1,
            "TARJETAS_AMARILLAS2" to partido.TAMARILLAS2,
            "TARJETAS_ROJAS1" to partido.TROJAS1,
            "TARJETAS_ROJAS2" to partido.TROJAS2,

            // Cronómetro (la web calcula el transcurrido)
            "FECHA_PLAY" to partido.FECHA_PLAY,
            "HORA_PLAY" to partido.HORA_PLAY,
            "CRONOMETRANDO" to partido.estaEnCurso(),

            // Estado / tiempos
            "NumeroDeTiempo" to partido.NumeroDeTiempo,
            "TIEMPOSJUGADOS" to partido.TIEMPOSJUGADOS,
            "ESTADO" to partido.ESTADO,

            // Opcionales (los que pediste)
            "ESCUDO1_URL" to partido.BANDERAEQUIPO1,
            "ESCUDO2_URL" to partido.BANDERAEQUIPO2,
            "ETAPA" to partido.ETAPA,
            "GRUPONOMBRE" to partido.GRUPONOMBRE,

            // Debug
            "ULTIMA_ACTUALIZACION" to ServerValue.TIMESTAMP
        )

        reference.setValue(data).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
