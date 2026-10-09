package dev.controlparental.parent.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.controlparental.parent.data.RepositorioPadre
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

class ParentViewModel(repositorio: RepositorioPadre) : ViewModel() {
    private val recorrido = MutableStateFlow(listOf(PantallaPadre.PRIMER_INICIO))
    val estado: StateFlow<ParentUiState> = combine(recorrido, repositorio.datos) { paginas, datos ->
        ParentUiState(paginas.last(), datos, paginas.size > 1)
    }.stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000),
        ParentUiState(datos = repositorio.datos.value),
    )

    fun navegar(destino: PantallaPadre) {
        recorrido.update { paginas ->
            val anterior = paginas.indexOf(destino)
            when {
                paginas.last() == destino -> paginas
                anterior >= 0 -> paginas.take(anterior + 1)
                else -> paginas + destino
            }
        }
    }

    fun volver() {
        recorrido.update { if (it.size > 1) it.dropLast(1) else it }
    }
}
