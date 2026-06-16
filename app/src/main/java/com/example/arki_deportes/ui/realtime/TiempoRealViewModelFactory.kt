package com.example.arki_deportes.ui.realtime

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.arki_deportes.data.repository.FirebaseCatalogRepository

class TiempoRealViewModelFactory(
    private val repository: FirebaseCatalogRepository,
    private val campeonatoId: String,
    private val partidoId: String,
    private val context: Context
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TiempoRealViewModel::class.java)) {
            return TiempoRealViewModel(repository, campeonatoId, partidoId, context) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}
