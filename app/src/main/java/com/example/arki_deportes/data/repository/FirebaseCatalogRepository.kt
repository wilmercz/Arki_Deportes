package com.example.arki_deportes.data.repository

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

    fun observePartidos(campeonatoCodigo: String? = null): Flow<List<Partido>> = callbackFlow {
        val reference: Query = if (campeonatoCodigo.isNullOrBlank()) {
            partidosReference()
        } else {
            partidosReference().orderByChild("CAMPEONATOCODIGO").equalTo(campeonatoCodigo)
        }

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val partidos = snapshot.children.mapNotNull { child ->
                    child.getValue(Partido::class.java)
                }
                trySend(partidos.sortedBy { it.FECHA_PARTIDO })
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

    suspend fun getPartido(codigoPartido: String): Partido? {
        val snapshot = partidosReference().child(codigoPartido).get().await()
        return snapshot.getValue(Partido::class.java)
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
}
