// app/src/main/java/com/example/arki_deportes/data/context/DeporteContext.kt

package com.example.arki_deportes.data.context

import android.content.Context
import android.content.SharedPreferences
import com.example.arki_deportes.utils.SportType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * DEPORTE CONTEXT - GESTIÓN DEL DEPORTE ACTIVO
 * ═══════════════════════════════════════════════════════════════════════════
 * 
 * Singleton que gestiona el deporte seleccionado actualmente.
 * Determina qué ruta de Firebase se usa para leer/escribir datos.
 * 
 * RESPONSABILIDADES:
 * - Almacenar deporte activo
 * - Proporcionar ruta de Firebase según deporte
 * - Persistir selección entre sesiones
 * - Notificar cambios de deporte
 * 
 * RUTAS FIREBASE:
 * - FUTBOL      → ARKI_DEPORTES/DatosFutbol/Campeonatos/
 * - AUTOMOVILISMO → ARKI_DEPORTES/DatosAutomovilismo/Campeonatos/
 * - CICLISMO    → ARKI_DEPORTES/DatosCiclismo/Campeonatos/
 * - BALONCESTO  → ARKI_DEPORTES/DatosBasquet/Campeonatos/
 * 
 * @author ARKI SISTEMAS
 * @version 2.0.0
 */
object DeporteContext {

    // ═══════════════════════════════════════════════════════════════════════
    // STATE FLOWS
    // ═══════════════════════════════════════════════════════════════════════
    
    private val _deporteActivo = MutableStateFlow(SportType.FUTBOL)
    val deporteActivo: StateFlow<SportType> = _deporteActivo.asStateFlow()
    
    // ═══════════════════════════════════════════════════════════════════════
    // SHARED PREFERENCES
    // ═══════════════════════════════════════════════════════════════════════
    
    private var sharedPreferences: SharedPreferences? = null
    
    private const val PREFS_NAME = "arki_deporte_prefs"
    private const val KEY_DEPORTE_ID = "deporte_activo_id"
    
    /**
     * Inicializa el contexto con SharedPreferences
     * DEBE llamarse en Application.onCreate() o MainActivity.onCreate()
     */
    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        cargarDeportePersistido()
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // GESTIÓN DE DEPORTE
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Selecciona un deporte y persiste la selección
     */
    fun seleccionarDeporte(deporte: SportType) {
        _deporteActivo.value = deporte
        
        // Persistir usando numericId (Int)
        sharedPreferences?.edit()?.apply {
            putInt(KEY_DEPORTE_ID, deporte.numericId)
            apply()
        }
    }
    
    /**
     * Obtiene el deporte activo
     */
    fun getDeporteActivo(): SportType {
        return _deporteActivo.value
    }
    
    /**
     * Carga el deporte persistido
     */
    private fun cargarDeportePersistido() {
        val deporteNumericId = sharedPreferences?.getInt(KEY_DEPORTE_ID, SportType.FUTBOL.numericId)
        if (deporteNumericId != null) {
            _deporteActivo.value = SportType.fromId(deporteNumericId)
        }
    }
    
    /**
     * Resetea al deporte por defecto (Fútbol)
     */
    fun resetearDeporte() {
        seleccionarDeporte(SportType.FUTBOL)
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // RUTAS DE FIREBASE
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Obtiene la ruta base de Firebase para el deporte activo
     * Ejemplo: "ARKI_DEPORTES/DatosFutbol/Campeonatos"
     */
    fun obtenerRutaFirebase(): String {
        val deportePath = _deporteActivo.value.rutaFirebase
        return "ARKI_DEPORTES/$deportePath/Campeonatos"
    }
    
    /**
     * Obtiene solo el nodo del deporte (sin ARKI_DEPORTES)
     * Ejemplo: "DatosFutbol"
     */
    fun obtenerNodoDeporte(): String {
        return _deporteActivo.value.rutaFirebase
    }
    
    /**
     * Obtiene la ruta completa con campeonato específico
     */
    fun obtenerRutaCampeonato(codigoCampeonato: String): String {
        return "${obtenerRutaFirebase()}/$codigoCampeonato"
    }
    
    /**
     * Obtiene la ruta completa de equipos de un campeonato
     */
    fun obtenerRutaEquipos(codigoCampeonato: String): String {
        return "${obtenerRutaCampeonato(codigoCampeonato)}/Equipos"
    }
    
    /**
     * Obtiene la ruta completa de grupos de un campeonato
     */
    fun obtenerRutaGrupos(codigoCampeonato: String): String {
        return "${obtenerRutaCampeonato(codigoCampeonato)}/Grupos"
    }
    
    /**
     * Obtiene la ruta completa de partidos de un campeonato
     */
    fun obtenerRutaPartidos(codigoCampeonato: String): String {
        return "${obtenerRutaCampeonato(codigoCampeonato)}/Partidos"
    }
    
    /**
     * Obtiene la ruta completa de series de un campeonato
     */
    fun obtenerRutaSeries(codigoCampeonato: String): String {
        return "${obtenerRutaCampeonato(codigoCampeonato)}/Series"
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // INFORMACIÓN DEL DEPORTE
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Obtiene el nombre del deporte activo
     */
    fun getNombreDeporte(): String {
        return _deporteActivo.value.nombre
    }
    
    /**
     * Obtiene el emoji del deporte activo
     */
    fun getEmojiDeporte(): String {
        return _deporteActivo.value.emoji
    }
    
    /**
     * Obtiene el ID del deporte activo (String)
     * Ejemplo: "FUTBOL", "AUTOMOVILISMO"
     */
    fun getDeporteId(): String {
        return _deporteActivo.value.id
    }
    
    /**
     * Obtiene el ID numérico del deporte activo (Int)
     * Ejemplo: 1, 2, 3, 4
     */
    fun getDeporteNumericId(): Int {
        return _deporteActivo.value.numericId
    }
    
    /**
     * Verifica si el deporte activo es fútbol
     */
    fun esFutbol(): Boolean {
        return _deporteActivo.value == SportType.FUTBOL
    }
    
    /**
     * Verifica si el deporte activo es automovilismo
     */
    fun esAutomovilismo(): Boolean {
        return _deporteActivo.value == SportType.AUTOMOVILISMO
    }
    
    /**
     * Verifica si el deporte activo es ciclismo
     */
    fun esCiclismo(): Boolean {
        return _deporteActivo.value == SportType.CICLISMO
    }
    
    /**
     * Verifica si el deporte activo es baloncesto
     */
    fun esBaloncesto(): Boolean {
        return _deporteActivo.value == SportType.BALONCESTO
    }
    
    /**
     * Obtiene todos los deportes disponibles
     */
    fun getDeportesDisponibles(): List<SportType> {
        return SportType.getTodosLosDeportes()
    }
}
