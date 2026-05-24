package com.example.arki_deportes.ui.tablaposiciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.arki_deportes.data.repository.FirebaseCatalogRepository

class TablaPosicionesFormViewModelFactory(
    private val repository: FirebaseCatalogRepository,
    private val campeonatoId: String
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TablaPosicionesFormViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TablaPosicionesFormViewModel(repository, campeonatoId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
