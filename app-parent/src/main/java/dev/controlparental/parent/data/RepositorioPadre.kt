package dev.controlparental.parent.data

import dev.controlparental.parent.presentation.DatosPadre
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface RepositorioPadre {
    val datos: StateFlow<DatosPadre>
}

// Fuente de producción todavía no operativa: únicamente expone estado desconocido.
// No contiene claves, fakes, fixtures, endpoints ni operaciones para confirmar órdenes.
class RepositorioPadreNoOperativo : RepositorioPadre {
    override val datos: StateFlow<DatosPadre> = MutableStateFlow(DatosPadre()).asStateFlow()
}
