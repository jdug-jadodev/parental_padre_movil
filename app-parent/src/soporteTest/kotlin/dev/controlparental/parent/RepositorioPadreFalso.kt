package dev.controlparental.parent

import dev.controlparental.parent.data.RepositorioPadre
import dev.controlparental.parent.presentation.DatosPadre
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// Se compila solo en pruebas JVM/instrumentadas, nunca en la aplicación.
class RepositorioPadreFalso(inicial: DatosPadre = DatosPadre()) : RepositorioPadre {
    private val mutable = MutableStateFlow(inicial)
    override val datos = mutable.asStateFlow()
    fun publicar(datos: DatosPadre) { mutable.value = datos }
}
