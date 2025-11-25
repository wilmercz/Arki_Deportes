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
import android.util.Log
import com.example.arki_deportes.data.model.Serie  // ⬅️ NUEVO
import kotlinx.coroutines.Dispatchers       // ⬅️ NUEVO
import kotlinx.coroutines.withContext       // ⬅️ NUEVO
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

    companion object {
        private const val TAG = "FirebaseCatalogRepo"
    }
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
        fun anyToString(path: String, default: String = ""): String {
            val v = child(path).value ?: return default
            return v.toString() // convierte Int/Long/Boolean a texto sin fallar
        }
        fun anyToInt(path: String, default: Int = 0): Int {
            val v = child(path).value ?: return default
            return when (v) {
                is Int -> v
                is Long -> v.toInt()
                is Double -> v.toInt()
                is String -> v.toIntOrNull() ?: default
                is Boolean -> if (v) 1 else 0
                else -> default
            }
        }
        fun anyToLong(path: String, default: Long = 0L): Long {
            val v = child(path).value ?: return default
            return when (v) {
                is Long -> v
                is Int -> v.toLong()
                is Double -> v.toLong()
                is String -> v.toLongOrNull() ?: default
                is Boolean -> if (v) 1L else 0L
                else -> default
            }
        }
        fun anyToBool(path: String, default: Boolean = false): Boolean {
            val v = child(path).value ?: return default
            return when (v) {
                is Boolean -> v
                is String -> v.equals("true", ignoreCase = true) || v == "1"
                is Number -> v.toInt() != 0
                else -> default
            }
        }

        return try {
            Partido(
                CODIGOPARTIDO = anyToString("CODIGOPARTIDO"),
                EQUIPO1 = anyToString("EQUIPO1"),
                EQUIPO2 = anyToString("EQUIPO2"),
                CAMPEONATOCODIGO = campeonatoCodigo,
                CAMPEONATOTXT = anyToString("CAMPEONATOTXT"),
                FECHAALTA = anyToString("FECHAALTA"),
                FECHA_PARTIDO = anyToString("FECHA_PARTIDO"),
                HORA_PARTIDO = anyToString("HORA_PARTIDO"),
                TEXTOFACEBOOK = anyToString("TEXTOFACEBOOK"),
                ESTADIO = anyToString("ESTADIO"),
                PROVINCIA = anyToString("PROVINCIA"),
                TIEMPOJUEGO = anyToString("TIEMPOJUEGO", "90"), // puede llegar como número -> toString()
                GOLES1 = anyToString("GOLES1", "0"),
                GOLES2 = anyToString("GOLES2", "0"),
                ANIO = anyToInt("ANIO", 0),
                CODIGOEQUIPO1 = anyToString("CODIGOEQUIPO1"),
                CODIGOEQUIPO2 = anyToString("CODIGOEQUIPO2"),
                TRANSMISION = anyToBool("TRANSMISION", false),
                ETAPA = anyToInt("ETAPA", 0),
                LUGAR = anyToString("LUGAR"),
                TIMESTAMP_CREACION = anyToLong("TIMESTAMP_CREACION", 0L),
                TIMESTAMP_MODIFICACION = anyToLong("TIMESTAMP_MODIFICACION", 0L),
                ORIGEN = anyToString("ORIGEN", "MOBILE"),
                DEPORTE = anyToString("DEPORTE", "FUTBOL")
            )
        } catch (e: Exception) {
            android.util.Log.e(
                "OBS_PARTIDOS",
                "❌ Error mapeando partido key=${key} -> ${e.message}",
                e
            )
            null
        }
    }


    private fun DataSnapshot.toGrupoSafe(campeonatoCodigo: String): Grupo? {
        return try {
            val map = value as? Map<*, *> ?: emptyMap<String, Any?>()

            fun s(vararg keys: String): String =
                keys.asSequence()
                    .mapNotNull { k -> map[k]?.toString()?.trim() }
                    .firstOrNull()
                    .orEmpty()

            fun i(vararg keys: String): Int =
                keys.asSequence()
                    .mapNotNull { k ->
                        when (val v = map[k]) {
                            is Number -> v.toInt()
                            is String -> v.toIntOrNull()
                            else -> null
                        }
                    }.firstOrNull() ?: 0

            fun l(vararg keys: String): Long =
                keys.asSequence()
                    .mapNotNull { k ->
                        when (val v = map[k]) {
                            is Number -> v.toLong()
                            is String -> v.toLongOrNull()
                            else -> null
                        }
                    }.firstOrNull() ?: 0L

            val codigoGrupo = s("CODIGOGRUPO", "CodigoGrupo", "codigoGrupo")
                .ifBlank { key ?: "" }

            val grupoLetra = s("GRUPO", "Grupo", "grupo")
            val provincia = s("PROVINCIA", "Provincia", "provincia")
            val nombreEquipoRaw = s("NOMBREEQUIPO", "NombreEquipo", "EQUIPO", "Equipo", "nombre")

            // Fallback inteligente para el nombre visible
            val nombreVisible = when {
                nombreEquipoRaw.isNotBlank() -> nombreEquipoRaw
                provincia.isNotBlank()        -> provincia
                codigoGrupo.contains('_')     -> codigoGrupo.substringAfter('_').trim()
                else                          -> ""
            }

            Grupo(
                CODIGOCAMPEONATO = campeonatoCodigo,
                CODIGOGRUPO = codigoGrupo,
                GRUPO = grupoLetra,
                POSICION = i("POSICION", "Posicion", "posicion", "orden"),

                // Texto y códigos con fallback
                PROVINCIA = provincia,
                CODIGOPROVINCIA = s("CODIGOPROVINCIA", "CodigoProvincia", "codigoProvincia"),
                NOMBREEQUIPO = nombreVisible,
                CODIGOEQUIPO = s("CODIGOEQUIPO", "CodigoEquipo", "codigoEquipo")
                    .ifBlank { s("CODIGOPROVINCIA", "CodigoProvincia", "codigoProvincia") },

                // (Opcionales: incluye solo si existen en tu data class)
                ///PUNTOS = i("PUNTOS", "Puntos", "puntos"),
                //PP = i("PP", "pp"),
                //SINCRONIZADO = i("SINCRONIZADO", "Sincronizado", "sync"),
                ANIO = i("ANIO", "Anio", "año", "anio", "year"),
                TIMESTAMP_CREACION = l("TIMESTAMP_CREACION", "timestamp_creacion", "createdAt"),
                TIMESTAMP_MODIFICACION = l("TIMESTAMP_MODIFICACION", "timestamp_modificacion", "updatedAt"),
                ORIGEN = s("ORIGEN", "origen")
            )
        } catch (_: Exception) {
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
    // Utilidad segura: imprime URL del Query/Ref
    private fun logRef(tag: String, prefix: String, query: Query) {
        Log.d(tag, "$prefix url=${query.toString()}")
    }
    /**
     * Observa los partidos de un campeonato específico o todos.
     * ✅ CORREGIDO: Usa toPartidoSafe() para evitar errores de conversión de tipos
     * @param campeonatoCodigo Código del campeonato, o null para ver todos
     */
    fun observePartidos(campeonatoCodigo: String? = null): Flow<List<Partido>> = callbackFlow {
        val codigoLimpio = campeonatoCodigo?.trim()

        if (codigoLimpio.isNullOrEmpty()) {
            Log.d("OBS_PARTIDOS", "🟡 GLOBAL (sin filtro)")
            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // ❌ NO usar snapshot.ref.path
                    logRef("OBS_PARTIDOS", "GLOBAL onDataChange ref:", snapshot.ref)
                    Log.d("OBS_PARTIDOS", "GLOBAL children(campos campeonatos)=${snapshot.childrenCount}")

                    val todos = mutableListOf<Partido>()
                    var vistos = 0
                    snapshot.children.forEach { campNode ->
                        vistos++
                        val campCod = campNode.key ?: return@forEach
                        val partidosNode = when {
                            campNode.hasChild("Partidos") -> campNode.child("Partidos")
                            campNode.hasChild("PARTIDOS") -> campNode.child("PARTIDOS")
                            campNode.hasChild("partidos") -> campNode.child("partidos")
                            else -> null
                        }
                        if (partidosNode == null) {
                            Log.d("OBS_PARTIDOS", "Camp=$campCod SIN nodo Partidos")
                            return@forEach
                        }
                        // Log seguro
                        logRef("OBS_PARTIDOS", "Camp=$campCod nodoPartidos:", partidosNode.ref)
                        Log.d("OBS_PARTIDOS", "hijos=${partidosNode.childrenCount}")

                        partidosNode.children.forEach { pNode ->
                            val p = pNode.toPartidoSafe(campCod)
                            if (p == null) {
                                Log.d("OBS_PARTIDOS", "NULL map key=${pNode.key} value=${pNode.value}")
                            } else {
                                todos.add(p)
                                Log.d("OBS_PARTIDOS", "OK ${p.CODIGOPARTIDO} @ ${p.FECHA_PARTIDO}")
                            }
                        }
                    }
                    Log.d("OBS_PARTIDOS", "RESUMEN GLOBAL: campeonatosVistos=$vistos, enviados=${todos.size}")
                    trySend(todos.sortedByDescending { it.FECHA_PARTIDO })
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.d("OBS_PARTIDOS", "GLOBAL cancel: ${error.message}")
                    close(error.toException())
                }
            }
            rootReference.addValueEventListener(listener)
            awaitClose { rootReference.removeEventListener(listener) }

        } else {
            Log.d("OBS_PARTIDOS", "🟢 ONE (filtro) camp='$codigoLimpio'")
            val partidosRef = rootReference.child(codigoLimpio).child("Partidos")
            logRef("OBS_PARTIDOS", "ONE suscribir ref:", partidosRef)

            val listener = object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    // ❌ NO usar snapshot.ref.path
                    logRef("OBS_PARTIDOS", "ONE onDataChange ref:", snapshot.ref)
                    Log.d("OBS_PARTIDOS", "ONE hijos=${snapshot.childrenCount}")

                    val partidos = snapshot.children.mapNotNull { pNode ->
                        pNode.toPartidoSafe(codigoLimpio).also {
                            if (it == null) Log.d("OBS_PARTIDOS", "NULL map key=${pNode.key} value=${pNode.value}")
                            else Log.d("OBS_PARTIDOS", "OK ${it.CODIGOPARTIDO} @ ${it.FECHA_PARTIDO}")
                        }
                    }
                    Log.d("OBS_PARTIDOS", "RESUMEN ONE: enviados=${partidos.size}")
                    trySend(partidos.sortedByDescending { it.FECHA_PARTIDO })
                }
                override fun onCancelled(error: DatabaseError) {
                    Log.d("OBS_PARTIDOS", "ONE cancel: ${error.message}")
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


    // ══════════════════════════════════════════════════════════════
// FUNCIONES DE SERIES
// ══════════════════════════════════════════════════════════════

    /**
     * Obtiene todas las series de un campeonato específico
     */
    suspend fun getSeriesByCampeonato(codigoCampeonato: String): Result<List<Serie>> {
        return withContext(Dispatchers.IO) {
            try {
                val seriesRef = rootReference  // ⬅️ CAMBIADO
                    .child(codigoCampeonato)
                    .child("Series")

                val snapshot = seriesRef.get().await()

                val series = mutableListOf<Serie>()
                snapshot.children.forEach { serieSnapshot ->
                    serieSnapshot.getValue(Serie::class.java)?.let {
                        series.add(it)
                    }
                }

                Result.success(series.sortedBy { it.NOMBRESERIE })
            } catch (e: Exception) {
                Log.e(TAG, "Error obteniendo series", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Obtiene una serie específica por su código
     */
    suspend fun getSerieById(
        codigoCampeonato: String,
        codigoSerie: String
    ): Result<Serie?> {
        return withContext(Dispatchers.IO) {
            try {
                val serieRef = rootReference  // ⬅️ CAMBIADO
                    .child(codigoCampeonato)
                    .child("Series")
                    .child(codigoSerie)

                val snapshot = serieRef.get().await()
                val serie = snapshot.getValue(Serie::class.java)

                Result.success(serie)
            } catch (e: Exception) {
                Log.e(TAG, "Error obteniendo serie", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Crea una nueva serie en Firebase
     */
    suspend fun createSerie(
        codigoCampeonato: String,
        serie: Serie
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                // Generar código único si no existe
                val codigoSerie = if (serie.CODIGOSERIE.isBlank()) {
                    "SERIE_${serie.NOMBRESERIE}_${System.currentTimeMillis()}"
                } else {
                    serie.CODIGOSERIE
                }

                val serieRef = rootReference  // ⬅️ CAMBIADO
                    .child(codigoCampeonato)
                    .child("Series")
                    .child(codigoSerie)

                val serieActualizada = serie.copy(
                    CODIGOSERIE = codigoSerie,
                    CODIGOCAMPEONATO = codigoCampeonato,
                    TIMESTAMP_CREACION = System.currentTimeMillis(),
                    TIMESTAMP_MODIFICACION = System.currentTimeMillis()
                )

                serieRef.setValue(serieActualizada.toMap()).await()

                Log.d(TAG, "Serie creada: $codigoSerie")
                Result.success(codigoSerie)
            } catch (e: Exception) {
                Log.e(TAG, "Error creando serie", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Actualiza una serie existente
     */
    suspend fun updateSerie(
        codigoCampeonato: String,
        serie: Serie
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val serieRef = rootReference  // ⬅️ CAMBIADO
                    .child(codigoCampeonato)
                    .child("Series")
                    .child(serie.CODIGOSERIE)

                val updates = serie.toMap().apply {
                    put("TIMESTAMP_MODIFICACION", System.currentTimeMillis())
                }

                serieRef.updateChildren(updates).await()

                Log.d(TAG, "Serie actualizada: ${serie.CODIGOSERIE}")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error actualizando serie", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Elimina una serie
     */
    suspend fun deleteSerie(
        codigoCampeonato: String,
        codigoSerie: String
    ): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val serieRef = rootReference  // ⬅️ CAMBIADO
                    .child(codigoCampeonato)
                    .child("Series")
                    .child(codigoSerie)

                serieRef.removeValue().await()

                Log.d(TAG, "Serie eliminada: $codigoSerie")
                Result.success(Unit)
            } catch (e: Exception) {
                Log.e(TAG, "Error eliminando serie", e)
                Result.failure(e)
            }
        }
    }

    /**
     * Obtiene grupos de una serie específica
     */
    suspend fun getGruposBySerie(
        codigoCampeonato: String,
        codigoSerie: String
    ): Result<List<Grupo>> {
        return withContext(Dispatchers.IO) {
            try {
                val gruposRef = rootReference  // ⬅️ CAMBIADO
                    .child(codigoCampeonato)
                    .child("Grupos")

                val snapshot = gruposRef.get().await()

                val grupos = mutableListOf<Grupo>()
                snapshot.children.forEach { grupoSnapshot ->
                    grupoSnapshot.getValue(Grupo::class.java)?.let { grupo ->
                        if (grupo.CODIGOSERIE == codigoSerie) {
                            grupos.add(grupo)
                        }
                    }
                }

                Result.success(grupos.sortedBy { it.GRUPO })
            } catch (e: Exception) {
                Log.e(TAG, "Error obteniendo grupos de serie", e)
                Result.failure(e)
            }
        }
    }
}