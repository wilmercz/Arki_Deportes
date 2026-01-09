// app/src/main/java/com/example/arki_deportes/data/context/PartidoContext.kt

package com.example.arki_deportes.data.context

import android.content.Context
import android.content.SharedPreferences
import com.example.arki_deportes.data.model.Partido
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ═══════════════════════════════════════════════════════════════════════════
 * PARTIDO CONTEXT - GESTIÓN DEL PARTIDO ACTIVO
 * ═══════════════════════════════════════════════════════════════════════════
 * 
 * Singleton que gestiona el partido que está siendo seguido en tiempo real.
 * Especialmente útil para corresponsales con partido asignado.
 * 
 * RESPONSABILIDADES:
 * - Almacenar partido activo
 * - Persistir código del partido para recuperación
 * - Notificar cambios en el partido
 * 
 * @author ARKI SISTEMAS
 * @version 2.0.0
 */
object PartidoContext {

    // ═══════════════════════════════════════════════════════════════════════
    // STATE FLOWS
    // ═══════════════════════════════════════════════════════════════════════
    
    private val _partidoActivo = MutableStateFlow<Partido?>(null)
    val partidoActivo: StateFlow<Partido?> = _partidoActivo.asStateFlow()
    
    // ═══════════════════════════════════════════════════════════════════════
    // SHARED PREFERENCES
    // ═══════════════════════════════════════════════════════════════════════
    
    private var sharedPreferences: SharedPreferences? = null
    
    private const val PREFS_NAME = "arki_partido_prefs"
    private const val KEY_CODIGO_PARTIDO = "codigo_partido_activo"
    
    /**
     * Inicializa el contexto con SharedPreferences
     * DEBE llamarse en Application.onCreate() o MainActivity.onCreate()
     */
    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // GESTIÓN DE PARTIDO
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Establece el partido activo y persiste su código
     */
    fun setPartidoActivo(partido: Partido?) {
        _partidoActivo.value = partido
        
        // Persistir código del partido
        sharedPreferences?.edit()?.apply {
            if (partido != null) {
                putString(KEY_CODIGO_PARTIDO, partido.CODIGOPARTIDO)
            } else {
                remove(KEY_CODIGO_PARTIDO)
            }
            apply()
        }
    }
    
    /**
     * Obtiene el partido activo
     */
    fun getPartidoActivo(): Partido? {
        return _partidoActivo.value
    }
    
    /**
     * Obtiene el código del partido activo persistido
     * Útil para recuperar partido después de crash
     */
    fun getCodigoPartidoPersistido(): String? {
        return sharedPreferences?.getString(KEY_CODIGO_PARTIDO, null)
    }
    
    /**
     * Limpia el partido activo
     */
    fun limpiarPartido() {
        _partidoActivo.value = null
        sharedPreferences?.edit()?.remove(KEY_CODIGO_PARTIDO)?.apply()
    }
    
    // ═══════════════════════════════════════════════════════════════════════
    // INFORMACIÓN DEL PARTIDO
    // ═══════════════════════════════════════════════════════════════════════
    
    /**
     * Obtiene el código del partido activo
     */
    fun getCodigoPartido(): String? {
        return _partidoActivo.value?.CODIGOPARTIDO
    }
    
    /**
     * Obtiene el nombre descriptivo del partido
     * Ejemplo: "Deportivo Puyo vs Alianza FC"
     */
    fun getNombrePartido(): String? {
        val partido = _partidoActivo.value ?: return null
        return "${partido.EQUIPO1} vs ${partido.EQUIPO2}"
    }
    
    /**
     * Obtiene el código del campeonato del partido
     */
    fun getCodigoCampeonato(): String? {
        return _partidoActivo.value?.CAMPEONATOCODIGO
    }
    
    /**
     * Verifica si hay un partido activo
     */
    fun hayPartidoActivo(): Boolean {
        return _partidoActivo.value != null
    }
    
    /**
     * Verifica si el partido está en curso (cronómetro iniciado)
     */
    fun partidoEnCurso(): Boolean {
        val partido = _partidoActivo.value ?: return false
        return partido.estado == "PRIMER_TIEMPO" || 
               partido.estado == "SEGUNDO_TIEMPO"
    }
    
    /**
     * Verifica si el partido está finalizado
     */
    fun partidoFinalizado(): Boolean {
        return _partidoActivo.value?.estado == "FINALIZADO"
    }
    
    /**
     * Obtiene el estado actual del partido
     */
    fun getEstadoPartido(): String {
        return _partidoActivo.value?.estado ?: "NO_INICIADO"
    }
    
    /**
     * Verifica si el partido permite edición
     */
    fun permiteEdicion(): Boolean {
        return _partidoActivo.value?.permiteEdicion == true
    }
    
    /**
     * Obtiene el usuario asignado al partido
     */
    fun getUsuarioAsignado(): String? {
        return _partidoActivo.value?.usuarioAsignado
    }
    
    /**
     * Verifica si el partido está asignado al usuario actual
     */
    fun esPartidoDelUsuario(nombreUsuario: String?): Boolean {
        if (nombreUsuario == null) return false
        return _partidoActivo.value?.usuarioAsignado == nombreUsuario
    }
}
