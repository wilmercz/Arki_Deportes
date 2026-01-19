package com.example.arki_deportes.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.arki_deportes.data.repository.FirebaseCatalogRepository   // <-- repo
import com.example.arki_deportes.data.model.Campeonato                // <-- model
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DrawerViewModel(
    private val repository: FirebaseCatalogRepository = FirebaseCatalogRepository()
) : ViewModel() {
    private val _campeonatos = MutableStateFlow<List<Campeonato>>(emptyList())
    val campeonatos: StateFlow<List<Campeonato>> = _campeonatos

    init {
        viewModelScope.launch {
            repository.observeCampeonatos().collect { _campeonatos.value = it }
        }
    }
}
