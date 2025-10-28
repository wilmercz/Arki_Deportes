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
 * ✅ ESTRUCTURA CORRECTA (según Firebase):
 * ARKI_DEPORTES/
 *   └─ DatosFutbol/
 *       └─ Campeonatos/
 *           └─ CODIGO_CAMPEONATO_1/
 *               ├─ CAMPEONATO: "..."
 *               ├─ ANIO: 2025
 *               ├─ ACTIVO: true
 *               ├─ Partidos/
 *               │   └─ codigo_partido: {...}
 *               ├─ Equipos/
 *               │   └─ codigo_equipo: {...}
 *               └─ Grupos/
 *                   └─ codigo_grupo: {...}
 *
 * @author ARKI SISTEMAS
 * @version 2.0.2 - Corregida lectura segura de tipos para Equipos y Partidos
 */
class FirebaseCatalogRepository(
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(),
    // ✅ CORRECCIÓN APLICADA: Ruta completa incluyendo ARKI_DEPORTES/DatosFutbol/Campeonatos
    private val rootNode: String = "ARKI_DEPORTES/DatosFutbol/Campeonatos"
) {

    private val rootReference: DatabaseReference
        get() = database.reference.child(rootNode)

    // ═══════════════════════════════════════════════════════════════════════
    // FUNCIONES HELPER PARA LECTURA SEGURA DE TIPOS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Convierte un DataSnapshot a Equipo de forma segura, manejando conversiones de tipo.
     * Soluciona el error: "Failed to convert a value of type java.lang.String to long"
     */
    private fun DataSnapshot.toEquipoSafe(campeonatoCodigo: String): Equipo? {
        return try {
            Equipo(
                CODIGOEQUIPO = child("CODIGOEQUIPO").getValue(String::class.java) ?: "",
                EQUIPO = child("EQUIPO").getValue(String::class.java) ?: "",
                PROVINCIA = child("PROVINCIA").getValue(String::class.java) ?: "",
                FECHAALTA = child("FECHAALTA").getValue(String::class.java) ?: "",
                ESCUDOLOCAL = child("ESCUDOLOCAL").getValue(String::class.java) ?: "",
                ESCUDOLINK = child("ESCUDOLINK").getValue(String::class.java) ?: "",
                CODIGOCAMPEONATO = campeonatoCodigo,
                EQUIPO_NOMBRECOMPLETO = child("EQUIPO_NOMBRECOMPLETO").getValue(String::class.java) ?: "",
                // ✅ Manejo seguro de Long: acepta tanto String como Long
                TIMESTAMP_CREACION = when (val value = child("TIMESTAMP_CREACION").value) {
                    is Long -> value
                    is String -> value.toLongOrNull() ?: 0L
                    is Int -> value.toLong()
                    is Double -> value.toLong()
                    else -> 0L
                },
                TIMESTAMP_MODIFICACION = when (val value = child("TIMESTAMP_MODIFICACION").value) {
                    is Long -> value
                    is String -> value.toLongOrNull() ?: 0L
                    is Int -> value.toLong()
                    is Double -> value.toLong()
                    else -> 0L
                },
                ORIGEN = child("ORIGEN").getValue(String::class.java) ?: "MOBILE"
            )
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Convierte un DataSnapshot a Partido de forma segura, manejando conversiones de tipo.
     * Soluciona el error: "Failed to convert a value of type java.lang.String to long"
     */
    private fun DataSnapshot.toPartidoSafe(campeonatoCodigo: String): Partido? {
        return try {
            Partido(
                CODIGOPARTIDO = child("CODIGOPARTIDO").getValue(String::class.java) ?: "",
                EQUIPO1 = child("EQUIPO1").getValue(String::class.java) ?: "",
                EQUIPO2 = child("EQUIPO2").getValue(String::class.java) ?: "",
                CAMPEONATOCODIGO = campeonatoCodigo,
                CAMPEONATOTXT = child("CAMPEONATOTXT").getValue(String::class.java) ?: "",
                FECHAALTA = child("FECHAALTA").getValue(String::class.java) ?: "",
                FECHA_PARTIDO = child("FECHA_PARTIDO").getValue(String::class.java) ?: "",
                HORA_PARTIDO = child("HORA_PARTIDO").getValue(String::class.java) ?: "",
                TEXTOFACEBOOK = child("TEXTOFACEBOOK").getValue(String::class.java) ?: "",
                ESTADIO = child("ESTADIO").getValue(String::class.java) ?: "",
                PROVINCIA = child("PROVINCIA").getValue(String::class.java) ?: "",
                TIEMPOJUEGO = child("TIEMPOJUEGO").getValue(String::class.java) ?: "90",
                GOLES1 = child("GOLES1").getValue(String::class.java) ?: "0",
                GOLES2 = child("GOLES2").getValue(String::class.java) ?: "0",
                // ✅ Manejo seguro de Int: acepta tanto String como Int
                ANIO = when (val value = child("ANIO").value) {
                    is Int -> value
                    is String -> value.toIntOrNull() ?: 0
                    is Long -> value.toInt()
                    is Double -> value.toInt()
                    else -> 0
                },
                CODIGOEQUIPO1 = child("CODIGOEQUIPO1").getValue(String::class.java) ?: "",
                CODIGOEQUIPO2 = child("CODIGOEQUIPO2").getValue(String::class.java) ?: "",
                TRANSMISION = child("TRANSMISION").getValue(Boolean::class.java) ?: false,
                // ✅ Manejo seguro de Int para ETAPA
                ETAPA = when (val value = child("ETAPA").value) {
                    is Int -> value
                    is String -> value.toIntOrNull() ?: 0
                    is Long -> value.toInt()
                    is Double -> value.toInt()
                    else -> 0
                },
                LUGAR = child("LUGAR").getValue(String::class.java) ?: "",
                // ✅ Manejo seguro de Long para timestamps
                TIMESTAMP_CREACION = when (val value = child("TIMESTAMP_CREACION").value) {
                    is Long -> value
                    is String -> value.toLongOrNull() ?: 0L
                    is Int -> value.toLong()
                    is Double -> value.toLong()
                    else -> 0L
                },
                TIMESTAMP_MODIFICACION = when (val value = child("TIMESTAMP_MODIFICACION").value) {
                    is Long -> value
                    is String -> value.toLongOrNull() ?: 0L
                    is Int -> value.toLong()
                    is Double -> value.toLong()
                    else -> 0L
                },
                ORIGEN = child("ORIGEN").getValue(String::class.java) ?: "MOBILE",
                DEPORTE = child("DEPORTE").getValue(String::class.java) ?: "FUTBOL"
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun DataSnapshot.toGrupoSafe(campeonatoCodigo: String): Grupo? {
        return try {
            Grupo(
                CODIGOCAMPEONATO = campeonatoCodigo,
                CODIGOGRUPO = child("CODIGOGRUPO").getValue(String::class.java) ?: "",
                GRUPO = child("GRUPO").getValue(String::class.java) ?: "",
                // ... campos normales ...

                // ✅ Conversión segura de timestamps
                TIMESTAMP_CREACION = when (val value = child("TIMESTAMP_CREACION").value) {
                    is Long -> value
                    is String -> value.toLongOrNull() ?: 0L
                    is Int -> value.toLong()
                    is Double -> value.toLong()
                    else -> 0L
                },
                // ... etc
            )
        } catch (e: Exception) {
            null
        }
    }

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
                            grupoNode.toGrupoSafe(campeonatoCod)?.let { grupo ->
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
                        // ✅ pasar campeonatoCodigo (NO campeonatoCod)
                        grupoNode.toGrupoSafe(campeonatoCodigo)?.copy(
                            CODIGOCAMPEONATO = campeonatoCodigo
                        )
                    }

                    val ordenados = grupos.sortedWith(
                        kotlin.comparisons.compareBy<Grupo> { it.GRUPO }
                            .thenBy { it.POSICION }
                    )
                    trySend(ordenados)
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
    // OBSERVADORES DE EQUIPOS (CORREGIDO)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Observa los equipos de un campeonato específico o todos.
     * ✅ CORREGIDO: Usa toEquipoSafe() para evitar errores de conversión de tipos
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
                            // ✅ USO DE toEquipoSafe() en lugar de getValue()
                            equipoNode.toEquipoSafe(campeonatoCod)?.let { equipo ->
                                val equipoGrupo = equipoNode.child("CODIGOGRUPO").getValue(String::class.java)

                                // Filtrar por grupo si se especifica
                                if (grupoCodigo.isNullOrBlank() || equipoGrupo == grupoCodigo) {
                                    todosLosEquipos.add(equipo)
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
                        // ✅ USO DE toEquipoSafe() en lugar de getValue()
                        val equipo = equipoNode.toEquipoSafe(campeonatoCodigo)
                        val equipoGrupo = equipoNode.child("CODIGOGRUPO").getValue(String::class.java)

                        // Filtrar por grupo si se especifica
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
            equiposRef.addValueEventListener(listener)
            awaitClose { equiposRef.removeEventListener(listener) }
        }
    }

    // ═══════════════════════════════════════════════════════════════════════
    // OBSERVADORES DE PARTIDOS (CORREGIDO)
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * Observa los partidos de un campeonato específico o todos.
     * ✅ CORREGIDO: Usa toPartidoSafe() para evitar errores de conversión de tipos
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
                            // ✅ USO DE toPartidoSafe() en lugar de getValue()
                            partidoNode.toPartidoSafe(campeonatoCod)?.let { partido ->
                                todosLosPartidos.add(partido)
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
                        // ✅ USO DE toPartidoSafe() en lugar de getValue()
                        partidoNode.toPartidoSafe(campeonatoCodigo)
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
        // ✅ USO DE toEquipoSafe()
        return snapshot.toEquipoSafe(campeonatoCodigo)
    }

    suspend fun getPartido(campeonatoCodigo: String, codigoPartido: String): Partido? {
        val snapshot = rootReference.child(campeonatoCodigo).child("Partidos").child(codigoPartido).get().await()
        // ✅ USO DE toPartidoSafe()
        return snapshot.toPartidoSafe(campeonatoCodigo)
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