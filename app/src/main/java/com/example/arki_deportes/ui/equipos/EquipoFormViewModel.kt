package com.example.arki_deportes.ui.equipos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arki_deportes.data.model.Campeonato
import com.example.arki_deportes.data.model.Equipo
import com.example.arki_deportes.data.repository.FirebaseCatalogRepository
import com.example.arki_deportes.ui.common.FormMessage
import com.example.arki_deportes.utils.Constants
import com.example.arki_deportes.utils.EcuadorProvincias
import com.example.arki_deportes.utils.Validations
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import com.example.arki_deportes.data.context.CampeonatoContext

private val DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())

data class EquipoFormData(
    val codigoCampeonato: String = "",
    val codigoEquipo: String = "",
    val nombreCorto: String = "",
    val nombreCompleto: String = "",
    val provincia: String = "",
    val fechaAlta: String = LocalDate.now().format(DATE_FORMATTER),
    val escudoLocal: String = "",
    val escudoLink: String = ""
)

data class EquipoFormUiState(
    val formData: EquipoFormData = EquipoFormData(),
    val campeonatos: List<Campeonato> = emptyList(),
    val isEditMode: Boolean = false,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val isDeleting: Boolean = false,
    val showValidationErrors: Boolean = false,
    val message: FormMessage? = null,
    val shouldClose: Boolean = false,
    val isImporting: Boolean = false,
    val showImportConfirmation: Boolean = false
)

class EquipoFormViewModel(
    private val repository: FirebaseCatalogRepository = FirebaseCatalogRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(EquipoFormUiState())
    val uiState: StateFlow<EquipoFormUiState> = _uiState

    private var observeCampeonatosJob: Job? = null
    private var originalEquipo: Equipo? = null

    init {
        observeCampeonatos()
    }

    /**
     * Carga los datos de un equipo existente para edición
     */
    fun loadEquipo(campeonatoId: String?, codigoEquipo: String?) {
        if (campeonatoId.isNullOrBlank() || codigoEquipo.isNullOrBlank()) {
            _uiState.update { it.copy(isEditMode = false, formData = EquipoFormData(codigoCampeonato = campeonatoId ?: "")) }
            originalEquipo = null
            return
        }

        // ✅ Usamos variables locales no nulas para asegurar que el compilador reconozca los tipos
        val safeCampId: String = campeonatoId
        val safeEquiId: String = codigoEquipo

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                // Forzamos el tipo Equipo? para evitar que se infiera como Any
                val equipo: Equipo? = repository.getEquipo(safeCampId, safeEquiId)
                if (equipo != null) {
                    originalEquipo = equipo
                    _uiState.update {
                        it.copy(
                            isEditMode = true,
                            formData = EquipoFormData(
                                codigoCampeonato = equipo.CODIGOCAMPEONATO,
                                codigoEquipo = equipo.CODIGOEQUIPO,
                                nombreCorto = equipo.EQUIPO,
                                nombreCompleto = equipo.NOMBRECOMPLETO,
                                provincia = equipo.PROVINCIA,
                                fechaAlta = equipo.FECHAALTA,
                                escudoLocal = equipo.ESCUDOLOCAL,
                                escudoLink = equipo.ESCUDOLINK
                            ),
                            isLoading = false
                        )
                    }
                } else {
                    _uiState.update { 
                        it.copy(isLoading = false, message = FormMessage("No se encontró el equipo", isError = true)) 
                    }
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(isLoading = false, message = FormMessage(e.message ?: "Error al cargar equipo", isError = true)) 
                }
            }
        }
    }

    private fun observeCampeonatos() {
        observeCampeonatosJob?.cancel()
        observeCampeonatosJob = viewModelScope.launch {
            repository.observeCampeonatos()
                .catch { error ->
                    _uiState.update {
                        it.copy(message = FormMessage(error.message ?: Constants.Mensajes.ERROR_DESCONOCIDO, isError = true))
                    }
                }
                .collect { campeonatos ->
                    _uiState.update { it.copy(campeonatos = campeonatos) }
                }
        }
    }

    fun onCampeonatoSelected(codigo: String) {
        updateForm { copy(codigoCampeonato = codigo) }
    }

    fun setCampeonatoId(id: String) {
        if (_uiState.value.formData.codigoCampeonato.isBlank()) {
            updateForm { copy(codigoCampeonato = id) }
        }
    }

    fun onNombreCortoChange(value: String) = updateForm { copy(nombreCorto = value) }
    fun onNombreCompletoChange(value: String) = updateForm { copy(nombreCompleto = value) }
    fun onProvinciaChange(value: String) = updateForm { copy(provincia = value) }
    fun onEscudoLocalChange(value: String) = updateForm { copy(escudoLocal = value) }
    fun onEscudoLinkChange(value: String) = updateForm { copy(escudoLink = value) }
    fun onFechaAltaChange(value: String) = updateForm { copy(fechaAlta = value) }

    private fun updateForm(transform: EquipoFormData.() -> EquipoFormData) {
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

    fun showImportConfirmation() {
        if (_uiState.value.formData.codigoCampeonato.isBlank()) {
            _uiState.update { it.copy(message = FormMessage("Seleccione un campeonato primero", isError = true)) }
            return
        }
        _uiState.update { it.copy(showImportConfirmation = true) }
    }

    fun hideImportConfirmation() {
        _uiState.update { it.copy(showImportConfirmation = false) }
    }

    fun importarProvincias() {
        val campeonatoId = _uiState.value.formData.codigoCampeonato
        if (campeonatoId.isBlank()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isImporting = true, showImportConfirmation = false) }
            try {
                val fechaHoy = currentDate()
                val timestamp = System.currentTimeMillis()
                
                val nuevosEquipos = EcuadorProvincias.LISTA.map { prov ->
                    val codEquipo = "PROV_${prov.nombre.replace(" ", "_")}_$timestamp"
                    Equipo(
                        CODIGOEQUIPO = codEquipo,
                        EQUIPO = abreviarNombreLargo(prov.nombre),
                        PROVINCIA = prov.nombre,
                        FECHAALTA = fechaHoy,
                        CODIGOCAMPEONATO = campeonatoId,
                        EQUIPO_NOMBRECOMPLETO = prov.nombre,
                        NOMBRECOMPLETO = prov.nombre,
                        ORIGEN = Constants.ORIGEN_MOBILE,
                        TIMESTAMP_CREACION = timestamp,
                        TIMESTAMP_MODIFICACION = timestamp
                    )
                }

                repository.saveEquiposMasivo(campeonatoId, nuevosEquipos)
                
                _uiState.update { 
                    it.copy(
                        isImporting = false, 
                        shouldClose = true,
                        message = FormMessage("Se han importado ${nuevosEquipos.size} provincias correctamente")
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isImporting = false, 
                        message = FormMessage("Error al importar: ${e.message}", isError = true)
                    ) 
                }
            }
        }
    }

    fun saveEquipo() {
        val form = _uiState.value.formData
        val timestamp = System.currentTimeMillis()
        val codigo = if (form.codigoEquipo.isNotBlank()) {
            form.codigoEquipo
        } else {
            generateCodigo(form.nombreCorto, timestamp)
        }

        val equipo = Equipo(
            CODIGOEQUIPO = codigo,
            EQUIPO = abreviarNombreLargo(form.nombreCorto.ifBlank { form.nombreCompleto }),
            PROVINCIA = form.provincia,
            FECHAALTA = form.fechaAlta.ifBlank { currentDate() },
            ESCUDOLOCAL = form.escudoLocal,
            ESCUDOLINK = form.escudoLink,
            CODIGOCAMPEONATO = form.codigoCampeonato,
            EQUIPO_NOMBRECOMPLETO = form.nombreCompleto.ifBlank { form.nombreCorto },
            NOMBRECOMPLETO = form.nombreCompleto.ifBlank { form.nombreCorto },
            TIMESTAMP_CREACION = originalEquipo?.TIMESTAMP_CREACION ?: timestamp,
            TIMESTAMP_MODIFICACION = timestamp,
            ORIGEN = Constants.ORIGEN_MOBILE
        )

        val validationError = Validations.validarEquipo(equipo)
        if (validationError != null) {
            _uiState.update {
                it.copy(
                    showValidationErrors = true,
                    message = FormMessage(validationError, isError = true)
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                repository.saveEquipo(equipo)
                originalEquipo = equipo
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
                        message = FormMessage(e.message ?: Constants.Mensajes.ERROR_DESCONOCIDO, isError = true)
                    )
                }
            }
        }
    }

    fun deleteEquipo() {
        val codigo = _uiState.value.formData.codigoEquipo
        val campeonatoId = _uiState.value.formData.codigoCampeonato
        if (codigo.isBlank() || campeonatoId.isBlank() || _uiState.value.isDeleting) return

        viewModelScope.launch {
            _uiState.update { it.copy(isDeleting = true) }
            try {
                repository.deleteEquipo(campeonatoId, codigo)
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

    private fun generateCodigo(nombre: String, timestamp: Long): String {
        val base = nombre.uppercase(Locale.getDefault())
            .replace("[^A-Z0-9]".toRegex(), "_")
            .replace("__+".toRegex(), "_")
            .trim('_')
        val safeBase = if (base.isBlank()) "EQUIPO" else base
        return "${safeBase}_${timestamp}"
    }

    /**
     * Aplica reglas de abreviación para nombres de provincias/equipos muy largos
     * según lo solicitado por el usuario.
     */
    private fun abreviarNombreLargo(nombre: String): String {
        val n = nombre.trim()
        return when {
            // Reglas específicas solicitadas
            n.contains("SANTO DOMINGO", ignoreCase = true) -> "STO. DOMINGO"
            n.contains("ZAMORA CHINCHIPE", ignoreCase = true) -> "ZAMORA CH."
            n.contains("MORONA SANTIAGO", ignoreCase = true) -> "MORONA S."
            n.contains("SANTA ELENA", ignoreCase = true) -> "SANTA ELENA"

            // No truncar si el nombre es razonable (ej: AZUAY tiene 5 letras, no debe ser AZU)
            n.length <= 12 -> n

            // Truncado inteligente solo para nombres extremadamente largos que no están en la lista
            n.length > 15 -> n.take(12) + ".."
            else -> n
        }
    }

    private fun currentDate(): String = LocalDate.now().format(DATE_FORMATTER)

    fun onImageSelected(uri: android.net.Uri?) {
        if (uri == null) return

        // ✅ Buscamos el ID en el form, y si no está, lo tomamos del contexto global
        val campeonatoId = _uiState.value.formData.codigoCampeonato.ifBlank {
            CampeonatoContext.campeonatoActivo.value?.CODIGO ?: ""
        }

        if (campeonatoId.isBlank()) {
            _uiState.update { it.copy(message = FormMessage("Por favor, seleccione un campeonato en el menú lateral primero", isError = true)) }
            return
        }

        val equipoId = _uiState.value.formData.codigoEquipo.ifBlank { "NUEVO" }

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            try {
                val downloadUrl = repository.uploadEscudo(campeonatoId, equipoId, uri)
                onEscudoLinkChange(downloadUrl) // Actualiza la caja de texto automáticamente
                _uiState.update { it.copy(isSaving = false, message = FormMessage("Escudo subido correctamente")) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isSaving = false, message = FormMessage("Error al subir: ${e.message}", isError = true)) }
            }
        }
    }
}
