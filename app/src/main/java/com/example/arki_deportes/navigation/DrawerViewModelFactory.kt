package com.example.arki_deportes.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.arki_deportes.data.repository.FirebaseCatalogRepository

class DrawerViewModelFactory(
    private val repository: FirebaseCatalogRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DrawerViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DrawerViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
