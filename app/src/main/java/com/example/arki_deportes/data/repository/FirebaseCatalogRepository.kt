package com.example.arki_deportes.data.repository

import android.net.Uri
import android.util.Log
import com.example.arki_deportes.data.model.Campeonato
import com.example.arki_deportes.data.model.Equipo
import com.example.arki_deportes.data.model.Grupo
import com.example.arki_deportes.data.model.Partido
import com.example.arki_deportes.data.model.Serie
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
import java.text.Normalizer
import java.util.Locale
import com.example.arki_deportes.data.model.PartidoEnVivo
import com.example.arki_deportes.data.model.GolEvento
import com.example.arki_deportes.data.model.CambioEvento
import com.example.arki_deportes.data.model.Jugador
import com.example.arki_deportes.data.model.AudioResource
import com.example.arki_deportes.data.model.BannerResource
import com.example.arki_deportes.data.model.AnuncioPublicidad
import com.example.arki_deportes.data.model.LogoResource

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

    internal fun campeonatosReference(): DatabaseReference =
        rootReference.child("DatosFutbol").child(Constants.FirebaseCollections.CAMPEONATOS)

    private fun equiposPorCampeonatoReference(campeonatoId: String): DatabaseReference =
        campeonatosReference().child(campeonatoId).child("Equipos")


    private fun equiposReference(): DatabaseReference =
        rootReference.child(Constants.FirebaseCollections.EQUIPOS)

    private fun partidosReference(): DatabaseReference =
        rootReference.child(Constants.FirebaseCollections.PARTIDOS)

    // ─────────────────────────────────────────────────────────────────────────────
    // Observadores
    // ─────────────────────────────────────────────────────────────────────────────

    fun observeCampeonatos(): Flow<List<Campeonato>> = callbackFlow {
        val reference = rootReference
            .child("DatosFutbol")
            .child("Campeonatos")


        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val campeonatos = snapshot.children.mapNotNull { child ->
                    child.getValue(Campeonato::class.java)
                }
                trySend(campeonatos.sortedBy { it.CAMPEONATO })

                Log.d(
                    "CampeonatosDrawer",
                    "children=${snapshot.childrenCount}"
                )
            }

            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        reference.addValueEventListener(listener)
        awaitClose { reference.removeEventListener(listener) }

    }

    fun observeSeries(campeonatoId: String): Flow<List<Serie>> = callbackFlow {
        val ref = campeonatosReference().child(campeonatoId).child("Series")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val series = snapshot.children.mapNotNull { it.getValue(Serie::class.java) }
                trySend(series)
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }


    fun observeGrupos(campeonatoId: String): Flow<List<Grupo>> = callbackFlow {
        val ref = campeonatosReference().child(campeonatoId).child("Grupos")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val grupos = snapshot.children.mapNotNull { it.getValue(Grupo::class.java) }
                trySend(grupos.sortedBy { it.GRUPO })
            }
            override fun onCancelled(error: DatabaseError) {
                close(error.toException())
            }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    fun observeEquipos(
        campeonatoCodigo: String?,
        grupoCodigo: String? = null
    ): Flow<List<Equipo>> = callbackFlow {
        if (campeonatoCodigo.isNullOrBlank()) {
            trySend(emptyList())
            return@callbackFlow
        }

        val reference = equiposPorCampeonatoReference(campeonatoCodigo)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val equipos = snapshot.children.mapNotNull { child ->
                    val equipo = child.getValue(Equipo::class.java)
                    if (equipo != null && (grupoCodigo.isNullOrBlank() || equipo.CODIGOGRUPO == grupoCodigo)) {
                        equipo
                    } else null
                }
                trySend(equipos.sortedBy { it.getNombreDisplay() })
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        reference.addValueEventListener(listener)
        awaitClose { reference.removeEventListener(listener) }
    }

    /**
     * NUEVO: Observa los partidos de un campeonato específico
     */
    fun observePartidos(campeonatoId: String?): Flow<List<Partido>> = callbackFlow {
        if (campeonatoId.isNullOrBlank()) {
            trySend(emptyList())
            return@callbackFlow
        }

        val reference = campeonatosReference()
            .child(campeonatoId)
            .child("Partidos")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val partidos = snapshot.children.mapNotNull { it.getValue(Partido::class.java) }
                trySend(partidos)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
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

    suspend fun getSerie(campeonatoId: String, serieId: String): Serie? {
        val snapshot = campeonatosReference().child(campeonatoId).child("Series").child(serieId).get().await()
        return snapshot.getValue(Serie::class.java)
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

    suspend fun getEquipo(campeonatoId: String, codigoEquipo: String): Equipo? {
        val snapshot = equiposPorCampeonatoReference(campeonatoId).child(codigoEquipo).get().await()
        return snapshot.getValue(Equipo::class.java)
    }


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


    suspend fun saveEstadiosYLugares(
        campeonatoCodigo: String,
        estadiosString: String,
        lugaresString: String
    ) {
        val campRef = campeonatosReference().child(campeonatoCodigo)

        // Guardar Estadios
        if (estadiosString.isNotBlank()) {
            val listaEstadios = estadiosString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            listaEstadios.forEach { nombre ->
                val codigo = generarCodigoUnico(nombre)
                val data = mapOf("CODIGOCAMPEONATO" to campeonatoCodigo, "CODIGOESTADIO" to codigo, "ESTADIO" to nombre)
                campRef.child("Estadios").child(codigo).setValue(data).await()
            }
        }

        // Guardar Lugares
        if (lugaresString.isNotBlank()) {
            val listaLugares = lugaresString.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            listaLugares.forEach { nombre ->
                val codigo = generarCodigoUnico(nombre)
                val data = mapOf("CODIGOCAMPEONATO" to campeonatoCodigo, "CODIGOLUGAR" to codigo, "LUGAR" to nombre)
                campRef.child("Lugares").child(codigo).setValue(data).await()
            }
        }
    }


    /**
     * Guarda una Serie y crea automáticamente sus Grupos hijos.
     * Replicando la lógica de BtnAgregarSerie_Click de VB.NET
     */
    suspend fun saveSerieConGrupos(
        serie: Serie,
        gruposNombresRaw: String
    ) {
        val campRef = campeonatosReference().child(serie.CODIGOCAMPEONATO)

        // 1. Guardar la Serie
        campRef.child("Series").child(serie.CODIGOSERIE).setValue(serie.toMap()).await()

        // 2. Procesar y guardar Grupos
        if (gruposNombresRaw.isNotBlank()) {
            val nombres = gruposNombresRaw.split(",").map { it.trim() }.filter { it.isNotEmpty() }

            nombres.forEach { nombreGrupo ->
                val codigoGrupo = generarCodigoUnico("${serie.CODIGOSERIE}_GRUPO_$nombreGrupo")

                val nuevoGrupo = Grupo(
                    CODIGOCAMPEONATO = serie.CODIGOCAMPEONATO,
                    CODIGOSERIE = serie.CODIGOSERIE,
                    CODIGOGRUPO = codigoGrupo,
                    GRUPO = nombreGrupo.uppercase(),
                    NOMBRESERIE = serie.NOMBRESERIE,
                    ANIO = serie.ANIO,
                    FECHAALTA = serie.FECHAALTA,
                    ORIGEN = "MOBILE",
                    DESCRIPCION = "Grupo $nombreGrupo - ${serie.NOMBRESERIE}"
                )

                // Guardar el grupo en el nodo de Grupos del campeonato (mismo nivel que Series)
                campRef.child("Grupos").child(codigoGrupo).setValue(nuevoGrupo.toMap()).await()
            }
        }
    }


    suspend fun saveEquiposMasivo(campeonatoId: String, equipos: List<Equipo>) {
        val ref = equiposPorCampeonatoReference(campeonatoId)
        val updates = mutableMapOf<String, Any?>()
        equipos.forEach { updates[it.CODIGOEQUIPO] = it.toMap() }
        ref.updateChildren(updates).await()
    }


    private fun generarCodigoUnico(input: String): String {
        val normalized = Normalizer.normalize(input, Normalizer.Form.NFD)
        return Regex("""\p{InCombiningDiacriticalMarks}+""").replace(normalized, "")
            .uppercase(Locale.getDefault())
            .replace(Regex("[^A-Z0-9]"), "_")
            .replace(Regex("_{2,}"), "_")
            .trim('_')
    }


    suspend fun saveGrupo(grupo: Grupo) {
        val codCampeonato = grupo.CODIGOCAMPEONATO
        val codGrupo = grupo.CODIGOGRUPO?.toString() ?: ""

        if (codCampeonato.isBlank() || codGrupo.isBlank()) {
            Log.e("FirebaseCatalogRepo", "❌ No se puede guardar el grupo: IDs vacíos")
            return
        }

        campeonatosReference().child(codCampeonato).child("Grupos")
            .child(codGrupo)
            .setValue(grupo.toMap())
            .await()
    }

    suspend fun saveEquipo(equipo: Equipo) {
        equiposPorCampeonatoReference(equipo.CODIGOCAMPEONATO)
            .child(equipo.CODIGOEQUIPO)
            .setValue(equipo.toMap())
            .await()
    }

    suspend fun updateEquipoFields(campeonatoId: String, codigoEquipo: String, updates: Map<String, Any?>) {
        equiposPorCampeonatoReference(campeonatoId).child(codigoEquipo).updateChildren(updates).await()
    }

    suspend fun savePartido(partido: Partido) {
        campeonatosReference()
            .child(partido.CAMPEONATOCODIGO) // 2. Entramos al ID del campeonato
            .child("Partidos")               // 3. Entramos al nodo Partidos
            .child(partido.CODIGOPARTIDO)    // 4. Ponemos el ID del partido
            .setValue(partido.toMap())       // 5. Guardamos todos los campos (goles, equipos, etc.)
            .await()
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // Operaciones de eliminación
    // ─────────────────────────────────────────────────────────────────────────────

    suspend fun deleteCampeonato(codigo: String) {
        campeonatosReference().child(codigo).removeValue().await()
    }

    suspend fun deleteSerie(campeonatoId: String, serieId: String) {
        campeonatosReference().child(campeonatoId).child("Series").child(serieId).removeValue().await()
    }

    suspend fun deleteGrupo(campeonatoId: String, codigoGrupo: String) {
        campeonatosReference().child(campeonatoId).child("Grupos").child(codigoGrupo).removeValue().await()
    }


    suspend fun deleteEquipo(campeonatoId: String, codigoEquipo: String) {
        equiposPorCampeonatoReference(campeonatoId).child(codigoEquipo).removeValue().await()
    }

    /**
     * Elimina un partido de la base de datos
     * @param campeonatoId ID del campeonato al que pertenece el partido
     * @param codigoPartido ID del partido a eliminar
     */
    suspend fun deletePartido(campeonatoId: String, codigoPartido: String) {
        if (campeonatoId.isBlank() || codigoPartido.isBlank()) {
            Log.e("FirebaseCatalogRepo", "❌ No se puede eliminar el partido: IDs vacíos")
            return
        }

        campeonatosReference()
            .child(campeonatoId)
            .child("Partidos")
            .child(codigoPartido)
            .removeValue()
            .await()
            
        // También intentar removerlo de partidos en vivo por si acaso
        try {
            removerDePartidosJugandose(codigoPartido)
        } catch (e: Exception) {
            Log.e("FirebaseCatalogRepo", "⚠️ No se pudo remover de PartidosJugandose: ${e.message}")
        }
    }

    /**
     * Observa un partido específico en tiempo real
     *
     * Ruta Firebase: /DatosFutbol/Campeonatos/{campeonatoId}/Partidos/{partidoId}
     * VB.NET Equivalente: RaizFireBase & "/DatosFutbol/Campeonatos/" & CodigoCampeonato & "/Partidos/" & CodigoPartido
     *
     * @param campeonatoId ID del campeonato
     * @param partidoId ID del partido
     * @return Flow que emite el partido cuando hay cambios
     */
    // Cambiar el tipo de retorno a Flow<Partido?> (con signo de interrogación)
    fun observePartido(campeonatoId: String, partidoId: String): Flow<Partido?> = callbackFlow {
        val reference = database.reference
            .child(rootNode)
            .child("DatosFutbol")
            .child("Campeonatos")
            .child(campeonatoId)
            .child("Partidos")
            .child(partidoId)

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val partido = snapshot.getValue(Partido::class.java)
                // Emitimos el resultado, sea el objeto o null (si no existe)
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

        val esBasquet = partido.DEPORTE == "BASQUET"
        val esPenales = partido.MARCADOR_PENALES && !esBasquet
        val esFutbol = !esBasquet && !esPenales


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
            "TIEMPOJUEGO" to partido.TIEMPOJUEGO,
            // Opcionales (los que pediste)
            "ESCUDO1_URL" to partido.BANDERAEQUIPO1,
            "ESCUDO2_URL" to partido.BANDERAEQUIPO2,
            "ETAPA" to partido.ETAPA,
            "GRUPONOMBRE" to partido.GRUPONOMBRE,
            "MARCADOR_PENALES" to partido.MARCADOR_PENALES,
            "PENALES1" to partido.PENALES1,
            "PENALES2" to partido.PENALES2,
            "PENALES_INICIA" to partido.PENALES_INICIA,
            "PENALES_TURNO" to partido.PENALES_TURNO,
            "PENALES_TANDA" to partido.PENALES_TANDA,
            "PENALES_SERIE1" to partido.PENALES_SERIE1,
            "PENALES_SERIE2" to partido.PENALES_SERIE2,
            "DEPORTE" to partido.DEPORTE,
            "MARCADOR_FUTBOL" to esFutbol,
            "MARCADOR_PENALES" to esPenales,
            "MARCADOR_BASQUET" to esBasquet,
            // Debug
            "ULTIMA_ACTUALIZACION" to ServerValue.TIMESTAMP
        )

        reference.setValue(data).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    fun actualizarTransmision(
        campeonatoId: String,
        partidoId: String,
        activa: Boolean
    ) {
        val ref = database.reference
            .child("ARKI_DEPORTES")
            .child("DatosFutbol")
            .child("Campeonatos")
            .child(campeonatoId)
            .child("Partidos")
            .child(partidoId)

        ref.child("TRANSMISION").setValue(activa)
        ref.child("ULTIMA_ACTUALIZACION").setValue(ServerValue.TIMESTAMP)
    }


    suspend fun uploadEscudo(campeonatoId: String, equipoId: String, fileUri: Uri): String {
        if (campeonatoId.isBlank()) throw Exception("Debe seleccionar un campeonato primero")
        // Limpiamos IDs para evitar errores en la ruta de Storage
        val safeCampId = campeonatoId.replace(Regex("[^A-Za-z0-9]"), "_")
        val safeEquiId = equipoId.replace(Regex("[^A-Za-z0-9]"), "_").ifBlank { "NUEVO" }

        val storageRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference
        val fileRef = storageRef.child("ESCUDOS").child(safeCampId).child("${safeEquiId}_${System.currentTimeMillis()}.png")

        fileRef.putFile(fileUri).await()
        return fileRef.downloadUrl.await().toString()
    }


// ─────────────────────────────────────────────────────────────────────────────
// NUEVAS FUNCIONES PARA MONITOR DE PRODUCCIÓN
// ─────────────────────────────────────────────────────────────────────────────

    /**
     * Observa el nodo ligero para la lista rápida de partidos en vivo
     * ✅ CORREGIDO: Ahora usa rootReference para apuntar a /ARKI_DEPORTES/PartidosJugandose
     */
    fun observePartidosJugandoseGlobal(): Flow<List<PartidoEnVivo>> = callbackFlow {
        // 📍 Cambiamos database.reference por rootReference
        val reference = rootReference.child("PartidosJugandose")

        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val partidos = snapshot.children.mapNotNull { child ->
                    try {
                        child.getValue(PartidoEnVivo::class.java)
                    } catch (e: Exception) {
                        Log.e("FirebaseCatalogRepo", "❌ Error parseando partido en vivo: ${e.message}")
                        null
                    }
                }
                trySend(partidos)
            }

            override fun onCancelled(error: DatabaseError) {
                // 🛠️ MEJORA: Log del error y evitamos el crash fatal enviando lista vacía
                Log.e("FirebaseCatalogRepo", "⚠️ Error en PartidosJugandose: ${error.message}")
                trySend(emptyList())
            }
        }
        reference.addValueEventListener(listener)
        awaitClose { reference.removeEventListener(listener) }
    }

    /**
     * Observa los goles de un partido (Ruta original)
     */
    fun observeGoles(campeonatoId: String, partidoId: String): Flow<List<GolEvento>> = callbackFlow {
        val ref = campeonatosReference().child(campeonatoId).child("Partidos").child(partidoId).child("Goles")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val goles = snapshot.children.mapNotNull { it.getValue(GolEvento::class.java) }
                trySend(goles.sortedBy { it.MINUTO })
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    /**
     * Observa los cambios de un equipo (Ruta original)
     */
    fun observeCambios(campeonatoId: String, partidoId: String, equipoNum: Int): Flow<List<CambioEvento>> = callbackFlow {
        val ref = campeonatosReference().child(campeonatoId).child("Partidos").child(partidoId).child("CambiosEquipo$equipoNum")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val cambios = snapshot.children.mapNotNull { it.getValue(CambioEvento::class.java) }
                trySend(cambios)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    /**
     * Observa la plantilla de jugadores de un equipo
     */
    fun observeJugadores(campeonatoId: String, equipoId: String): Flow<List<Jugador>> = callbackFlow {
        val ref = campeonatosReference().child(campeonatoId).child("Equipos").child(equipoId).child("Jugadores")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val jugadores = snapshot.children.mapNotNull { it.getValue(Jugador::class.java) }
                trySend(jugadores.sortedBy { it.NUMERO.toIntOrNull() ?: 999 })
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // En FirebaseCatalogRepository.kt
    fun observeEstadios(campeonatoId: String): Flow<List<String>> = callbackFlow {
        val ref = campeonatosReference().child(campeonatoId).child("Estadios")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = snapshot.children.mapNotNull { it.child("ESTADIO").getValue(String::class.java) }
                trySend(lista)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    // En FirebaseCatalogRepository.kt
    fun observeLugares(campeonatoId: String): Flow<List<String>> = callbackFlow {
        val ref = campeonatosReference().child(campeonatoId).child("Lugares")
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lista = snapshot.children.mapNotNull { it.child("LUGAR").getValue(String::class.java) }
                trySend(lista)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }


    /**
     * Publica el partido en el nodo global /PartidosJugandose
     * VB.NET Equivalente: /PartidosJugandose/{CodigoPartido}/
     */
    suspend fun publicarEnPartidosJugandose(partido: Partido): Result<Unit> = try {
        val reference = rootReference
            .child("PartidosJugandose")
            .child(partido.CODIGOPARTIDO)

        val data = mapOf(
            "CODIGOPARTIDO" to partido.CODIGOPARTIDO,
            "CODIGOCAMPEONATO" to partido.CAMPEONATOCODIGO,
            "EQUIPO1" to partido.EQUIPO1,
            "EQUIPO2" to partido.EQUIPO2,
            "GOLES1" to partido.GOLES1,
            "GOLES2" to partido.GOLES2,
            "DEPORTE" to "FUTBOL",
            "TIEMPOSJUGADOS" to partido.TIEMPOSJUGADOS,
            "ESTADO" to partido.ESTADO, // 0 = Jugándose, 1 = Finalizado
            "FECHA_PLAY" to partido.FECHA_PLAY,
            "HORA_PLAY" to partido.HORA_PLAY,
            "ULTIMA_ACTUALIZACION" to ServerValue.TIMESTAMP,
            "DEPORTE" to partido.DEPORTE,
        )

        reference.setValue(data).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    /**
     * Remueve el partido de la lista de PartidosJugandose
     */
    suspend fun removerDePartidosJugandose(codigoPartido: String) {
        rootReference.child("PartidosJugandose").child(codigoPartido).removeValue().await()
    }

    /**
     * Actualiza los permisos de un usuario (para sincronizar autoasignación)
     */
    suspend fun actualizarPermisosUsuario(nombreUsuario: String, campeonatoId: String, partidoId: String) {
        val ref = database.reference        .child("AppConfig")
            .child("Usuarios")
            .child(nombreUsuario)
            .child("permisos")

        val updates = mapOf(
            "codigoCampeonato" to campeonatoId,
            "codigoPartido" to partidoId
        )

        ref.updateChildren(updates).await()
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // GESTIÓN DE AUDIOS Y MÚSICA
    // ─────────────────────────────────────────────────────────────────────────────

    fun audiosReference(): DatabaseReference =
        database.reference.child("CONFIGURACION_OVERLAYWEB").child("CONFIGURACION_MEDIA").child("AUDIOS")
    fun observeAudios(): Flow<List<AudioResource>> = callbackFlow {
        val ref = audiosReference()
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val audios = snapshot.children.mapNotNull { it.getValue(AudioResource::class.java) }
                trySend(audios)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun saveAudio(audio: AudioResource) {
        val id = if (audio.id.isBlank()) audiosReference().push().key ?: "" else audio.id
        val finalAudio = audio.copy(id = id)
        audiosReference().child(id).setValue(finalAudio.toMap()).await()
    }

    suspend fun deleteAudio(audioId: String) {
        audiosReference().child(audioId).removeValue().await()
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // GESTIÓN DE BANNERS Y PUBLICIDAD
    // ─────────────────────────────────────────────────────────────────────────────

    fun bannersReference(): DatabaseReference =
        database.reference.child("CONFIGURACION_OVERLAYWEB").child("CONFIGURACION_MEDIA").child("PUBLICIDAD_BANNER")

    fun observeBanners(): Flow<List<BannerResource>> = callbackFlow {
        val ref = bannersReference()
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val banners = snapshot.children.mapNotNull { it.getValue(BannerResource::class.java) }
                trySend(banners)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun saveBanner(banner: BannerResource) {
        val id = if (banner.id.isBlank()) bannersReference().push().key ?: "" else banner.id
        val finalBanner = banner.copy(id = id)
        bannersReference().child(id).setValue(finalBanner.toMap()).await()
    }

    suspend fun deleteBanner(bannerId: String) {
        bannersReference().child(bannerId).removeValue().await()
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // GESTIÓN DE LOGOS (NUEVO)
    // ─────────────────────────────────────────────────────────────────────────────

    fun logosReference(): DatabaseReference =
        database.reference.child("CONFIGURACION_OVERLAYWEB").child("CONFIGURACION_MEDIA").child("LOGOS_LISTA")

    fun logosOnAirReference(): DatabaseReference =
        rootReference.child("LOGOS_AI_AIRE")

    fun observeLogos(): Flow<List<LogoResource>> = callbackFlow {
        val ref = logosReference()
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val logos = snapshot.children.mapNotNull { it.getValue(LogoResource::class.java) }
                trySend(logos)
            }
            override fun onCancelled(error: DatabaseError) { close(error.toException()) }
        }
        ref.addValueEventListener(listener)
        awaitClose { ref.removeEventListener(listener) }
    }

    suspend fun saveLogo(logo: LogoResource) {
        val id = if (logo.id.isBlank()) logosReference().push().key ?: "" else logo.id
        val finalLogo = logo.copy(id = id)
        logosReference().child(id).setValue(finalLogo.toMap()).await()
    }

    suspend fun deleteLogo(logoId: String) {
        // Eliminar de lista global
        logosReference().child(logoId).removeValue().await()
        // Eliminar de Al Aire por si acaso estaba ahí
        logosOnAirReference().child(logoId).removeValue().await()
    }

    /**
     * Alterna el estado Al Aire de un logo.
     * Actualiza tanto la lista global como la lista de producción.
     */
    suspend fun toggleLogoOnAir(logo: LogoResource) {
        val newStatus = !logo.onAir
        val logoId = logo.id
        
        // 1. Actualizar en lista global
        logosReference().child(logoId).child("onAir").setValue(newStatus).await()
        
        // 2. Gestionar en lista Al Aire
        if (newStatus) {
            // Agregar a Al Aire
            logosOnAirReference().child(logoId).setValue(logo.copy(onAir = true).toMap()).await()
        } else {
            // Quitar de Al Aire
            logosOnAirReference().child(logoId).removeValue().await()
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // CONTROL DE OVERLAY (PRODUCCIÓN EN VIVO)
    // ─────────────────────────────────────────────────────────────────────────────

    fun overlayConfigReference(): DatabaseReference =
        database.reference.child("CONFIGURACION_OVERLAYWEB").child("CONFIGURACION_MEDIA")

    suspend fun publicarBannerEnOverlay(banner: BannerResource?) {
        val ref = overlayConfigReference().child("BANNER_ACTIVO")
        if (banner == null) {
            ref.removeValue().await()
        } else {
            ref.setValue(banner.toMap()).await()
        }
    }

    suspend fun reproducirAudioEnOverlay(audio: AudioResource?) {
        val ref = overlayConfigReference().child("AUDIO_PLAY")
        if (audio == null) {
            ref.removeValue().await()
        } else {
            val data = audio.toMap().toMutableMap()
            data["timestamp"] = ServerValue.TIMESTAMP
            ref.setValue(data).await()
        }
    }

    /**
     * Sincroniza el estado del cronómetro en dos rutas:
     * 1. Histórica: /DatosFutbol/Campeonatos/{cId}/Partidos/{pId}
     * 2. Rápida (Overlay): /PARTIDOACTUAL
     */
    suspend fun enviarEstadoCronometro(
        campeonatoId: String,
        partidoId: String,
        datosCrono: Map<String, Any>
    ): Result<Unit> = try {
        // 1. Ruta histórica (Partidos)
        val refPartido = campeonatosReference()
            .child(campeonatoId)
            .child("Partidos")
            .child(partidoId)
        
        refPartido.updateChildren(datosCrono).await()

        // 2. Ruta rápida (PARTIDOACTUAL para Overlay)
        val refActual = rootReference.child("PARTIDOACTUAL")
        refActual.updateChildren(datosCrono).await()

        Result.success(Unit)
    } catch (e: Exception) {
        Log.e("FirebaseCatalogRepo", "❌ Error al enviar estado cronómetro: ${e.message}")
        Result.failure(e)
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // NUEVO: CONTROL DE PUBLICIDAD (ARKI_DEPORTES/PUBLICIDAD)
    // ─────────────────────────────────────────────────────────────────────────────

    fun publicidadReference(): DatabaseReference = rootReference.child("PUBLICIDAD")

    suspend fun enviarAnuncioUnico(anuncio: AnuncioPublicidad) {
        publicidadReference().setValue(anuncio).await()
    }

    suspend fun enviarListaAnuncios(anuncios: List<AnuncioPublicidad>) {
        publicidadReference().setValue(anuncios).await()
    }

    suspend fun ocultarPublicidad() {
        val ordenApagado = mapOf("mostrar" to false)
        publicidadReference().setValue(ordenApagado).await()
    }
}
