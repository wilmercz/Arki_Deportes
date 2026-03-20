package com.example.arki_deportes.ui.partidos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arki_deportes.data.model.Campeonato
import com.example.arki_deportes.data.model.Equipo
import com.example.arki_deportes.data.model.Grupo
import com.example.arki_deportes.data.model.Partido
import com.example.arki_deportes.data.repository.FirebaseCatalogRepository
import com.example.arki_deportes.ui.common.FormMessage
import com.example.arki_deportes.utils.Constants
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.arki_deportes.data.model.Serie
import com.example.arki_deportes.data.context.CampeonatoContext
import java.time.LocalTime

private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
private val TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())

/**
 * Datos del formulario de partidos.
 */
data class PartidoFormData(
    val codigoPartido: String = "",
    val campeonatoCodigo: String = "",
    val campeonatoNombre: String = "",
    val grupoCodigo: String = "",
    val equipo1Codigo: String = "",
    val equipo2Codigo: String = "",
    val fechaPartido: String = "",
    val horaPartido: String = "",
    val estadio: String = "",
    val provincia: String = "",
    val transmision: Boolean = false,
    val etapa: Int = Constants.EtapasPartido.NINGUNO,
    val goles1: Int = 0,
    val goles2: Int = 0,
    val textoFacebook: String = "",
    val lugar: String = "",
    val serieCodigo: String = "",
    val grupoNombre: String = "",
    val serieNombre: String = "",
    val tiempoJuego: String = "45",
    val deporte: String = "FUTBOL"
)

data class PartidoFormUiState(
    val formData: PartidoFormData = PartidoFormData(),
    val campeonatos: List<Campeonato> = emptyList(),
    val grupos: List<Grupo> = emptyList(),
    val equipos: List<Equipo> = emptyList(),
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val showValidationErrors: Boolean = false,
    val message: FormMessage? = null,
    val shouldClose: Boolean = false,
    val series: List<Serie> = emptyList(),
    val estadiosSugeridos: List<String> = emptyList(),
    val lugaresSugeridos: List<String> = emptyList()
)

class PartidoFormViewModel(
    private val repository: FirebaseCatalogRepository = FirebaseCatalogRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(PartidoFormUiState())
    val uiState: StateFlow<PartidoFormUiState> = _uiState

    private var originalPartido: Partido? = null
    private var campeonatosJob: Job? = null
    private var seriesJob: Job? = null

    private var gruposJob: Job? = null
    private var equiposJob: Job? = null

    init {
        observeCampeonatos()

        val hoy = LocalDate.now()
        val ahora = LocalTime.now()

        updateForm {
            copy(
                fechaPartido = hoy.format(DATE_FORMATTER),
                horaPartido = ahora.format(TIME_FORMATTER)
            )
        }

        CampeonatoContext.getCampeonatoActual()?.let { camp ->
            onCampeonatoSelected(camp.CODIGO)
            // Forzamos el nombre desde el contexto para que la UI lo muestre de inmediato
            updateForm { copy(campeonatoNombre = camp.CAMPEONATO, provincia = camp.PROVINCIA) }
        }
    }

    private fun subscribeToSeries(campeonatoCodigo: String) {
        seriesJob?.cancel()
        seriesJob = viewModelScope.launch {
            repository.observeSeries(campeonatoCodigo).collect { series ->
                _uiState.update { it.copy(series = series) }
            }
        }
    }

    // Al seleccionar Serie, cargamos sus grupos
    fun onSerieSelected(codigo: String) {
        val serie = _uiState.value.series.firstOrNull { it.CODIGOSERIE == codigo }
        val campId = _uiState.value.formData.campeonatoCodigo
        subscribeToGrupos(campId) 
        updateForm { copy(serieCodigo = codigo, serieNombre = serie?.NOMBRESERIE ?: "") }
    }

    // Asignación rápida
    fun asignarEquipo(equipo: Equipo, numero: Int) {
        updateForm {
            if (numero == 1) copy(equipo1Codigo = equipo.CODIGOEQUIPO)
            else copy(equipo2Codigo = equipo.CODIGOEQUIPO)
        }
        generarTextoSocial()
    }

    fun generarTextoSocial() {
        val form = _uiState.value.formData
        val campNombre = form.campeonatoNombre
        val e1 = _uiState.value.equipos.firstOrNull { it.CODIGOEQUIPO == form.equipo1Codigo }?.EQUIPO ?: ""
        val e2 = _uiState.value.equipos.firstOrNull { it.CODIGOEQUIPO == form.equipo2Codigo }?.EQUIPO ?: ""
        val fecha = form.fechaPartido
        val hora = form.horaPartido
        val estadio = form.estadio
        val lugar = form.lugar
        val provincia = form.provincia

        val hashtagCamp = "#${campNombre.replace(" ", "")}"
        val hashtagE1 = "#${e1.replace(" ", "")}"
        val hashtagE2 = "#${e2.replace(" ", "")}"

        val texto = """
            EN VIVO : $e1 🆚 $e2
            
            🏆 $campNombre 🏆
            ⚽ ¡PARTIDAZO! ⚽
            

            📅 Fecha: $fecha
            🕒 Hora: $hora
            🏟️ Estadio: $estadio
            📍 Ubicación: $lugar, $provincia
            
            $hashtagCamp $hashtagE1 $hashtagE2 #ArkiDeportes #FutbolEnVivo
        """.trimIndent()

        onTextoFacebookChange(texto)
    }


    fun loadPartido(codigoPartido: String?) {
        if (codigoPartido.isNullOrBlank()) {
            _uiState.update { it.copy(
                isEditMode = false,
                isLoading = false
            ) }
            originalPartido = null
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, message = null) }
            try {
                val campeonatoCodigo =
                    _uiState.value.formData.campeonatoCodigo.ifBlank {
                        com.example.arki_deportes.data.context.CampeonatoContext
                            .campeonatoActivo.value?.CODIGO.orEmpty()
                    }

                val partido = repository.getPartido(campeonatoCodigo, codigoPartido)

                if (partido != null) {
                    originalPartido = partido
                    subscribeToGrupos(partido.CAMPEONATOCODIGO)
                    subscribeToEquipos(partido.CAMPEONATOCODIGO, null)
                    _uiState.update {
                        it.copy(
                            formData = PartidoFormData(
                                codigoPartido = partido.CODIGOPARTIDO,
                                campeonatoCodigo = partido.CAMPEONATOCODIGO,
                                campeonatoNombre = partido.CAMPEONATOTXT,
                                equipo1Codigo = partido.CODIGOEQUIPO1,
                                equipo2Codigo = partido.CODIGOEQUIPO2,
                                fechaPartido = partido.FECHA_PARTIDO,
                                horaPartido = partido.HORA_PARTIDO,
                                estadio = partido.ESTADIO,
                                provincia = partido.PROVINCIA,
                                transmision = partido.TRANSMISION,
                                etapa = partido.ETAPA,
                                goles1 = partido.GOLES1,
                                goles2 = partido.GOLES2,
                                textoFacebook = partido.TEXTOFACEBOOK,
                                lugar = partido.LUGAR,
                                deporte = partido.DEPORTE,
                                tiempoJuego = partido.TIEMPOJUEGO
                            ),
                            isEditMode = true,
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = FormMessage("No se encontró el partido solicitado", isError = true)
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        message = FormMessage(e.message ?: Constants.Mensajes.ERROR_DESCONOCIDO, isError = true)
                    )
                }
            }
        }
    }

    private fun observeCampeonatos() {
        campeonatosJob?.cancel()
        campeonatosJob = viewModelScope.launch {
            repository.observeCampeonatos()
                .catch { error ->
                    _uiState.update {
                        it.copy(message = FormMessage(error.message ?: Constants.Mensajes.ERROR_DESCONOCIDO, isError = true))
                    }
                }
                .collect { campeonatos ->
                    _uiState.update { it.copy(campeonatos = campeonatos) }
                    
                    // 💡 REVALIDAR DEPORTE CUANDO LLEGAN LOS DATOS
                    val currentId = _uiState.value.formData.campeonatoCodigo
                    if (currentId.isNotBlank()) {
                        campeonatos.firstOrNull { it.CODIGO == currentId }?.let { camp ->
                            updateForm {
                                copy(
                                    campeonatoNombre = camp.CAMPEONATO,
                                    provincia = camp.PROVINCIA,
                                    deporte = camp.DEPORTE.uppercase(),
                                    tiempoJuego = camp.getTiempoJuegoStr()
                                )
                            }
                        }
                    }
                }
        }
    }

    private fun subscribeToGrupos(campeonatoCodigo: String) {
        gruposJob?.cancel()
        if (campeonatoCodigo.isBlank()) {
            _uiState.update { it.copy(grupos = emptyList()) }
            return
        }

        gruposJob = viewModelScope.launch {
            repository.observeGrupos(campeonatoCodigo)
                .catch { error ->
                    _uiState.update {
                        it.copy(message = FormMessage(error.message ?: Constants.Mensajes.ERROR_DESCONOCIDO, isError = true))
                    }
                }
                .collect { grupos ->
                    _uiState.update { it.copy(grupos = grupos) }
                }
        }
    }

    private fun subscribeToEquipos(campeonatoCodigo: String, grupoCodigo: String?) {
        equiposJob?.cancel()
        if (campeonatoCodigo.isBlank()) {
            _uiState.update { it.copy(equipos = emptyList()) }
            return
        }
        equiposJob = viewModelScope.launch {
            repository.observeEquipos(campeonatoCodigo, grupoCodigo)
                .catch { error ->
                    _uiState.update {
                        it.copy(message = FormMessage(error.message ?: Constants.Mensajes.ERROR_DESCONOCIDO, isError = true))
                    }
                }
                .collect { equipos ->
                    _uiState.update { it.copy(equipos = equipos) }
                }
        }
    }

    fun onCampeonatoSelected(codigo: String) {
        val campeonato = _uiState.value.campeonatos.firstOrNull { it.CODIGO == codigo }

        viewModelScope.launch {
            repository.observeEstadios(codigo).collect { lista ->
                _uiState.update { it.copy(estadiosSugeridos = lista) }
            }
        }
        viewModelScope.launch {
            repository.observeLugares(codigo).collect { lista ->
                _uiState.update { it.copy(lugaresSugeridos = lista) }
            }
        }

        subscribeToSeries(codigo)
        subscribeToGrupos(codigo)
        subscribeToEquipos(codigo, null)

        updateForm {
            copy(
                campeonatoCodigo = codigo,
                campeonatoNombre = campeonato?.CAMPEONATO ?: "",
                provincia = campeonato?.PROVINCIA ?: provincia,
                deporte = campeonato?.DEPORTE?.uppercase() ?: "FUTBOL",
                tiempoJuego = campeonato?.getTiempoJuegoStr() ?: "45",
                grupoCodigo = "",
                equipo1Codigo = "",
                equipo2Codigo = ""
            )
        }
    }

    fun onGrupoSelected(codigo: String) {
        val campeonatoCodigo = _uiState.value.formData.campeonatoCodigo
        subscribeToEquipos(campeonatoCodigo, codigo)
        updateForm {
            copy(
                grupoCodigo = codigo,
                equipo1Codigo = "",
                equipo2Codigo = ""
            )
        }
    }

    fun onEquipo1Selected(codigo: String) = updateForm { copy(equipo1Codigo = codigo) }

    fun onEquipo2Selected(codigo: String) = updateForm { copy(equipo2Codigo = codigo) }

    fun onFechaChange(value: String) = updateForm { copy(fechaPartido = value) }

    fun onHoraChange(value: String) = updateForm { copy(horaPartido = value) }

    fun onEstadioChange(value: String) = updateForm { copy(estadio = value) }

    fun onProvinciaChange(value: String) = updateForm { copy(provincia = value) }

    fun onTransmisionChange(value: Boolean) = updateForm { copy(transmision = value) }

    fun onEtapaChange(value: Int) = updateForm { copy(etapa = value) }

    fun onGoles1Change(value: String) =
        updateForm {
            copy(goles1 = value.toIntOrNull() ?: 0)
        }

    fun onGoles2Change(value: String) =
        updateForm {
            copy(goles2 = value.toIntOrNull() ?: 0)
        }

    fun onTextoFacebookChange(value: String) = updateForm { copy(textoFacebook = value) }

    fun onLugarChange(value: String) = updateForm { copy(lugar = value) }

    private fun updateForm(transform: PartidoFormData.() -> PartidoFormData) {
        _uiState.update {
            it.copy(
                formData = it.formData.transform(),
                showValidationErrors = false,
                message = null
            )
        }
    }

    fun onMessageShown() {
        _uiState.update { it.copy(message = null) }
    }

    fun onCloseConsumed() {
        _uiState.update { it.copy(shouldClose = false) }
    }

    fun savePartido() {
        val form = _uiState.value.formData
        val timestamp = System.currentTimeMillis()
        val equipo1 = _uiState.value.equipos.firstOrNull { it.CODIGOEQUIPO == form.equipo1Codigo }
        val equipo2 = _uiState.value.equipos.firstOrNull { it.CODIGOEQUIPO == form.equipo2Codigo }
        val campeonato = _uiState.value.campeonatos.firstOrNull { it.CODIGO == form.campeonatoCodigo }

        val codigo = if (form.codigoPartido.isNotBlank()) {
            form.codigoPartido
        } else {
            generateCodigo(equipo1?.EQUIPO ?: "EQUIPO1", equipo2?.EQUIPO ?: "EQUIPO2", timestamp)
        }

        val partido = Partido(
            CODIGOPARTIDO = codigo,
            EQUIPO1 = equipo1?.EQUIPO ?: "",
            EQUIPO2 = equipo2?.EQUIPO ?: "",
            CAMPEONATOCODIGO = form.campeonatoCodigo,
            CAMPEONATOTXT = campeonato?.CAMPEONATO ?: form.campeonatoNombre,
            FECHAALTA = originalPartido?.FECHAALTA ?: currentDate(),
            FECHA_PARTIDO = form.fechaPartido,
            HORA_PARTIDO = form.horaPartido,
            TEXTOFACEBOOK = form.textoFacebook,
            ESTADIO = form.estadio,
            PROVINCIA = form.provincia,
            TIEMPOJUEGO = form.tiempoJuego,
            GOLES1 = form.goles1, 
            GOLES2 = form.goles2, 
            DEPORTE = form.deporte,
            ANIO = deriveYear(form.fechaPartido),
            CODIGOEQUIPO1 = form.equipo1Codigo,
            CODIGOEQUIPO2 = form.equipo2Codigo,
            TRANSMISION = form.transmision,
            ETAPA = form.etapa,
            LUGAR = form.lugar,
            TIMESTAMP_CREACION = (originalPartido?.TIMESTAMP_CREACION ?: timestamp.toString()),
            TIMESTAMP_MODIFICACION = timestamp.toString(),
            ORIGEN = Constants.ORIGEN_MOBILE
        )

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                repository.savePartido(partido)
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        isEditMode = true,
                        shouldClose = true,
                        message = FormMessage(Constants.Mensajes.EXITO_GUARDAR)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        message = FormMessage(e.message ?: "Error al guardar", isError = true)
                    )
                }
            }
        }
    }


    fun deletePartido() {
        val codigo = _uiState.value.formData.codigoPartido
        val campeonatoId = _uiState.value.formData.campeonatoCodigo
        if (codigo.isBlank() || campeonatoId.isBlank() || _uiState.value.isDeleting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            try {
                repository.deletePartido(campeonatoId, codigo)
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        shouldClose = true,
                        message = FormMessage(Constants.Mensajes.EXITO_ELIMINAR)
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isDeleting = false,
                        message = FormMessage(e.message ?: Constants.Mensajes.ERROR_DESCONOCIDO, isError = true)
                    )
                }
            }
        }
    }


    private fun generateCodigo(equipo1: String, equipo2: String, timestamp: Long): String {
        val base1 = equipo1.uppercase(Locale.getDefault()).replace("[^A-Z0-9]".toRegex(), "_").trim('_')
        val base2 = equipo2.uppercase(Locale.getDefault()).replace("[^A-Z0-9]".toRegex(), "_").trim('_')
        val safe1 = if (base1.isBlank()) "EQUIPO1" else base1
        val safe2 = if (base2.isBlank()) "EQUIPO2" else base2
        return "${safe1}_${safe2}_${timestamp}"
    }

    private fun deriveYear(fecha: String): Int {
        return runCatching {
            if (fecha.length >= 4) fecha.substring(0, 4).toInt() else LocalDate.now().year
        }.getOrDefault(LocalDate.now().year)
    }

    private fun currentDate(): String = LocalDate.now().format(DATE_FORMATTER)

    fun onDeporteChange(value: String) {
        val normalized = if (value.uppercase() == "BALONCESTO") "BASQUET" else value.uppercase()
        updateForm { copy(deporte = normalized) }
        generarTextoSocial()
    }

}
