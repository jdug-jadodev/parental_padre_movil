package dev.controlparental.parent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import dev.controlparental.parent.data.RepositorioPadre
import dev.controlparental.parent.data.RepositorioPadreNoOperativo
import dev.controlparental.parent.presentation.ParentViewModel

class AppContainer(private val repositorio: RepositorioPadre = RepositorioPadreNoOperativo()) {
    val fabrica: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass == ParentViewModel::class.java) { "ViewModel no soportado" }
            @Suppress("UNCHECKED_CAST")
            return ParentViewModel(repositorio) as T
        }
    }
}
