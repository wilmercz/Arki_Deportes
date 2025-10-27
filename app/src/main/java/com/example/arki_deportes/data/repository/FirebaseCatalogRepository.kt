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
 * ═══════════════════════════════════════════════════════════════════════════
 * FIREBASE CATALOG REPOSITORY - ESTRUCTURA JERÁRQUICA
 * ═══════════════════════════════════════════════════════════════════════════
 *
 * Repositorio adaptado para trabajar con estructura jerárquica de Firebase:
 *
 * FutbolWC/
 *   ├─ CODIGO_CAMPEONATO_1/
 *   │   ├─ CAMPEONATO: "..."
 *   │   ├─ ANIO: 2025
 *   │   ├─ Partidos/
 *   │   │   └─ codigo_partido: {...}
 *   │   ├─ Equipos/
 *   │   │   └─ codigo_equipo: {...}
 *   │   └─ Grupos/
 *   │       └─ codigo_grupo: {...}
 *
 * @author ARKI SISTEMAS
 * @version 2.0.0 - Adaptado para estructura jerárquica
 */
class FirebaseCatalogRepository(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(),
    private val rootNode: String = "DatosFutbol"  // ← Cambiado a tu nodo raíz
) {

    private val rootReference: DatabaseReference
        get() = database.reference.child(rootNode)

    // ═══════════════════════════════════════════════════════════════════════
    // OBSERVADORES DE CAMPEONATOS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Observa todos los campeonatos.
     * Lee los nodos principales donde cada uno representa un campeonato.
     */
    fun observeCampeonatos(): Flow<List<Campeonato>> = callbackFlow {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val campeonatos = snapshot.children.mapNotNull { campeonatoNode ->
                    // Construir el campeonato desde los campos directos del nodo
                    val codigo = campeonatoNode.key ?: return@mapNotNull null

                    try {
                        Campeonato(
                            CODIGO = codigo,
                            CAMPEONATO = campeonatoNode.child("CAMPEONATO").getValue(String::class.java) ?: "",
                            ANIO = campeonatoNode.child("ANIO").getValue(Int::class.java) ?: 0,
                            DEPORTE = campeonatoNode.child("DEPORTE").getValue(String::class.java) ?: "Futbol",
                            PROVINCIA = campeonatoNode.child("PROVINCIA").getValue(String::class.java) ?: "",
                            ORIGEN = campeonatoNode.child("ORIGEN").getValue(String::class.java) ?: "MOBILE"
                        )
                    } catch (e: Exception) {
                        null
                    }
                }
                trySend(campeonatos.sortedBy { it.CAMPEONATO })
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        rootReference.addValueEventListener(listener)
        awaitClose { rootReference.removeEventListener(listener) }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // OBSERVADORES DE GRUPOS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Observa los grupos de un campeonato específico o todos.
     * @param campeonatoCodigo Código del campeonato, o null para ver todos
     */
    fun observeGrupos(campeonatoCodigo: String? = null): Flow<List<Grupo>> = callbackFlow {
        if (campeonatoCodigo.isNullOrBlank()) {
            // Ver TODOS los grupos de TODOS los campeonatos
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val todosLosGrupos = mutableListOf<Grupo>()

                    snapshot.children.forEach { campeonatoNode ->
                        val campeonatoCod = campeonatoNode.key ?: return@forEach
                        val gruposNode = campeonatoNode.child("Grupos")

                        gruposNode.children.forEach { grupoNode ->
                            grupoNode.getValue(Grupo::class.java)?.let { grupo ->
                                todosLosGrupos.add(grupo.copy(CODIGOCAMPEONATO = campeonatoCod))
                            }
                        }
                    }

                    trySend(todosLosGrupos.sortedBy { it.GRUPO })
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            }
            rootReference.addValueEventListener(listener)
            awaitClose { rootReference.removeEventListener(listener) }

        } else {
            // Ver grupos de UN campeonato específico
            val gruposRef = rootReference.child(campeonatoCodigo).child("Grupos")
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val grupos = snapshot.children.mapNotNull { grupoNode ->
                        grupoNode.getValue(Grupo::class.java)?.copy(
                            CODIGOCAMPEONATO = campeonatoCodigo
                        )
                    }
                    trySend(grupos.sortedBy { it.GRUPO })
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            }
            gruposRef.addValueEventListener(listener)
            awaitClose { gruposRef.removeEventListener(listener) }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // OBSERVADORES DE EQUIPOS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Observa los equipos de un campeonato específico o todos.
     * @param campeonatoCodigo Código del campeonato, o null para ver todos
     * @param grupoCodigo Código del grupo para filtrar (opcional)
     */
    fun observeEquipos(
        campeonatoCodigo: String? = null,
        grupoCodigo: String? = null
    ): Flow<List<Equipo>> = callbackFlow {
        if (campeonatoCodigo.isNullOrBlank()) {
            // Ver TODOS los equipos de TODOS los campeonatos
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val todosLosEquipos = mutableListOf<Equipo>()

                    snapshot.children.forEach { campeonatoNode ->
                        val campeonatoCod = campeonatoNode.key ?: return@forEach
                        val equiposNode = campeonatoNode.child("Equipos")

                        equiposNode.children.forEach { equipoNode ->
                            equipoNode.getValue(Equipo::class.java)?.let { equipo ->
                                val equipoGrupo = equipoNode.child("CODIGOGRUPO").getValue(String::class.java)

                                // Filtrar por grupo si se especifica
                                if (grupoCodigo.isNullOrBlank() || equipoGrupo == grupoCodigo) {
                                    todosLosEquipos.add(equipo.copy(CODIGOCAMPEONATO = campeonatoCod))
                                }
                            }
                        }
                    }

                    trySend(todosLosEquipos.sortedBy { it.getNombreDisplay() })
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            }
            rootReference.addValueEventListener(listener)
            awaitClose { rootReference.removeEventListener(listener) }

        } else {
            // Ver equipos de UN campeonato específico
            val equiposRef = rootReference.child(campeonatoCodigo).child("Equipos")
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val equipos = snapshot.children.mapNotNull { equipoNode ->
                        val equipo = equipoNode.getValue(Equipo::class.java)
                        val equipoGrupo = equipoNode.child("CODIGOGRUPO").getValue(String::class.java)

                        // Filtrar por grupo si se especifica
                        if (equipo != null && (grupoCodigo.isNullOrBlank() || equipoGrupo == grupoCodigo)) {
                            equipo.copy(CODIGOCAMPEONATO = campeonatoCodigo)
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
            equiposRef.addValueEventListener(listener)
            awaitClose { equiposRef.removeEventListener(listener) }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // OBSERVADORES DE PARTIDOS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Observa los partidos de un campeonato específico o todos.
     * @param campeonatoCodigo Código del campeonato, o null para ver todos
     */
    fun observePartidos(campeonatoCodigo: String? = null): Flow<List<Partido>> = callbackFlow {
        if (campeonatoCodigo.isNullOrBlank()) {
            // Ver TODOS los partidos de TODOS los campeonatos
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val todosLosPartidos = mutableListOf<Partido>()

                    snapshot.children.forEach { campeonatoNode ->
                        val campeonatoCod = campeonatoNode.key ?: return@forEach
                        val partidosNode = campeonatoNode.child("Partidos")

                        partidosNode.children.forEach { partidoNode ->
                            partidoNode.getValue(Partido::class.java)?.let { partido ->
                                todosLosPartidos.add(partido.copy(CAMPEONATOCODIGO = campeonatoCod))
                            }
                        }
                    }

                    trySend(todosLosPartidos.sortedByDescending { it.FECHA_PARTIDO })
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            }
            rootReference.addValueEventListener(listener)
            awaitClose { rootReference.removeEventListener(listener) }

        } else {
            // Ver partidos de UN campeonato específico
            val partidosRef = rootReference.child(campeonatoCodigo).child("Partidos")
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val partidos = snapshot.children.mapNotNull { partidoNode ->
                        partidoNode.getValue(Partido::class.java)?.copy(
                            CAMPEONATOCODIGO = campeonatoCodigo
                        )
                    }
                    trySend(partidos.sortedByDescending { it.FECHA_PARTIDO })
                }

                override fun onCancelled(error: DatabaseError) {
                    close(error.toException())
                }
            }
            partidosRef.addValueEventListener(listener)
            awaitClose { partidosRef.removeEventListener(listener) }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // OPERACIONES DE LECTURA ÚNICA
    // ═══════════════════════════════════════════════════════════════════════

    suspend fun getCampeonato(codigo: String): Campeonato? {
        val snapshot = rootReference.child(codigo).get().await()
        return try {
            Campeonato(
                CODIGO = codigo,
                CAMPEONATO = snapshot.child("CAMPEONATO").getValue(String::class.java) ?: "",
                ANIO = snapshot.child("ANIO").getValue(Int::class.java) ?: 0,
                DEPORTE = snapshot.child("DEPORTE").getValue(String::class.java) ?: "Futbol",
                PROVINCIA = snapshot.child("PROVINCIA").getValue(String::class.java) ?: "",
                ORIGEN = snapshot.child("ORIGEN").getValue(String::class.java) ?: "MOBILE"
            )
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getAllCampeonatos(): List<Campeonato> {
        val snapshot = rootReference.get().await()
        return snapshot.children.mapNotNull { campeonatoNode ->
            val codigo = campeonatoNode.key ?: return@mapNotNull null
            try {
                Campeonato(
                    CODIGO = codigo,
                    CAMPEONATO = campeonatoNode.child("CAMPEONATO").getValue(String::class.java) ?: "",
                    ANIO = campeonatoNode.child("ANIO").getValue(Int::class.java) ?: 0,
                    DEPORTE = campeonatoNode.child("DEPORTE").getValue(String::class.java) ?: "Futbol",
                    PROVINCIA = campeonatoNode.child("PROVINCIA").getValue(String::class.java) ?: "",
                    ORIGEN = campeonatoNode.child("ORIGEN").getValue(String::class.java) ?: "MOBILE"
                )
            } catch (e: Exception) {
                null
            }
        }
    }

    suspend fun getGrupo(campeonatoCodigo: String, codigoGrupo: String): Grupo? {
        val snapshot = rootReference.child(campeonatoCodigo).child("Grupos").child(codigoGrupo).get().await()
        return snapshot.getValue(Grupo::class.java)?.copy(CODIGOCAMPEONATO = campeonatoCodigo)
    }

    suspend fun getEquipo(campeonatoCodigo: String, codigoEquipo: String): Equipo? {
        val snapshot = rootReference.child(campeonatoCodigo).child("Equipos").child(codigoEquipo).get().await()
        return snapshot.getValue(Equipo::class.java)?.copy(CODIGOCAMPEONATO = campeonatoCodigo)
    }

    suspend fun getPartido(campeonatoCodigo: String, codigoPartido: String): Partido? {
        val snapshot = rootReference.child(campeonatoCodigo).child("Partidos").child(codigoPartido).get().await()
        return snapshot.getValue(Partido::class.java)?.copy(CAMPEONATOCODIGO = campeonatoCodigo)
    }

    // ═══════════════════════════════════════════════════════════════════════
    // OPERACIONES DE GUARDADO
    // ═══════════════════════════════════════════════════════════════════════

    suspend fun saveCampeonato(campeonato: Campeonato) {
        rootReference.child(campeonato.CODIGO)
            .setValue(campeonato.toMap())
            .await()
    }

    suspend fun saveGrupo(grupo: Grupo) {
        if (grupo.CODIGOCAMPEONATO.isBlank()) {
            throw IllegalArgumentException("El grupo debe tener un código de campeonato")
        }
        rootReference.child(grupo.CODIGOCAMPEONATO)
            .child("Grupos")
            .child(grupo.CODIGOGRUPO)
            .setValue(grupo.toMap())
            .await()
    }

    suspend fun saveEquipo(equipo: Equipo) {
        if (equipo.CODIGOCAMPEONATO.isBlank()) {
            throw IllegalArgumentException("El equipo debe tener un código de campeonato")
        }
        rootReference.child(equipo.CODIGOCAMPEONATO)
            .child("Equipos")
            .child(equipo.CODIGOEQUIPO)
            .setValue(equipo.toMap())
            .await()
    }

    suspend fun savePartido(partido: Partido) {
        if (partido.CAMPEONATOCODIGO.isBlank()) {
            throw IllegalArgumentException("El partido debe tener un código de campeonato")
        }
        rootReference.child(partido.CAMPEONATOCODIGO)
            .child("Partidos")
            .child(partido.CODIGOPARTIDO)
            .setValue(partido.toMap())
            .await()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // OPERACIONES DE ELIMINACIÓN
    // ═══════════════════════════════════════════════════════════════════════

    suspend fun deleteCampeonato(codigo: String) {
        rootReference.child(codigo).removeValue().await()
    }

    suspend fun deleteGrupo(campeonatoCodigo: String, codigoGrupo: String) {
        rootReference.child(campeonatoCodigo)
            .child("Grupos")
            .child(codigoGrupo)
            .removeValue()
            .await()
    }

    suspend fun deleteEquipo(campeonatoCodigo: String, codigoEquipo: String) {
        rootReference.child(campeonatoCodigo)
            .child("Equipos")
            .child(codigoEquipo)
            .removeValue()
            .await()
    }

    suspend fun deletePartido(campeonatoCodigo: String, codigoPartido: String) {
        rootReference.child(campeonatoCodigo)
            .child("Partidos")
            .child(codigoPartido)
            .removeValue()
            .await()
    }

    // ═══════════════════════════════════════════════════════════════════════
    // MÉTODOS DE COMPATIBILIDAD (para ViewModels que no tienen campeonatoCodigo)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Busca y elimina un grupo por su código en todos los campeonatos
     */
    suspend fun deleteGrupo(codigoGrupo: String) {
        val snapshot = rootReference.get().await()
        snapshot.children.forEach { campeonatoNode ->
            val grupoRef = campeonatoNode.ref.child("Grupos").child(codigoGrupo)
            if (grupoRef.get().await().exists()) {
                grupoRef.removeValue().await()
                return
            }
        }
    }

    /**
     * Busca y elimina un equipo por su código en todos los campeonatos
     */
    suspend fun deleteEquipo(codigoEquipo: String) {
        val snapshot = rootReference.get().await()
        snapshot.children.forEach { campeonatoNode ->
            val equipoRef = campeonatoNode.ref.child("Equipos").child(codigoEquipo)
            if (equipoRef.get().await().exists()) {
                equipoRef.removeValue().await()
                return
            }
        }
    }

    /**
     * Busca y elimina un partido por su código en todos los campeonatos
     */
    suspend fun deletePartido(codigoPartido: String) {
        val snapshot = rootReference.get().await()
        snapshot.children.forEach { campeonatoNode ->
            val partidoRef = campeonatoNode.ref.child("Partidos").child(codigoPartido)
            if (partidoRef.get().await().exists()) {
                partidoRef.removeValue().await()
                return
            }
        }
    }
}