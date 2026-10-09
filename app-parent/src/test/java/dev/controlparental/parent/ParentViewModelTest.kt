package dev.controlparental.parent

import dev.controlparental.parent.data.RepositorioPadreNoOperativo
import dev.controlparental.parent.presentation.*
import androidx.lifecycle.ViewModelStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ParentViewModelTest {
    @Test fun recorridoYRetrocesoNoCreanVinculoNiEvidencia() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val almacen = ViewModelStore()
        try {
            val modelo = ParentViewModel(RepositorioPadreFalso())
            almacen.put("prueba", modelo)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { modelo.estado.collect {} }
            for (pantalla in listOf(PantallaPadre.VINCULAR, PantallaPadre.CONTROL, PantallaPadre.RECUPERACION)) {
                modelo.navegar(pantalla)
                runCurrent()
                assertEquals(pantalla, modelo.estado.value.pantalla)
                assertEquals(DatosPadre(), modelo.estado.value.datos)
            }
            modelo.volver()
            runCurrent()
            assertEquals(PantallaPadre.CONTROL, modelo.estado.value.pantalla)
            modelo.navegar(PantallaPadre.PRIMER_INICIO)
            runCurrent()
            modelo.volver()
            runCurrent()
            assertFalse(modelo.estado.value.puedeVolver)
        } finally { almacen.clear(); runCurrent(); Dispatchers.resetMain() }
    }

    @Test fun repositorioActualizaEstadoSinConfundirAusenciaConBloqueo() = runTest {
        Dispatchers.setMain(StandardTestDispatcher(testScheduler))
        val almacen = ViewModelStore()
        try {
            val repositorio = RepositorioPadreFalso()
            val modelo = ParentViewModel(repositorio)
            almacen.put("prueba", modelo)
            backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { modelo.estado.collect {} }
            repositorio.publicar(DatosPadre(relay = ConexionRelay.CONECTADO, presencia = PresenciaHijo.AUSENTE))
            runCurrent()
            assertEquals(PresenciaHijo.AUSENTE, modelo.estado.value.datos.presencia)
            assertNull(modelo.estado.value.datos.evidencia)
            modelo.navegar(PantallaPadre.RECUPERACION)
            modelo.navegar(PantallaPadre.RECUPERACION)
            modelo.volver()
            runCurrent()
            assertEquals(PantallaPadre.PRIMER_INICIO, modelo.estado.value.pantalla)
        } finally { almacen.clear(); runCurrent(); Dispatchers.resetMain() }
    }

    @Test fun produccionSoloExponeDatosDesconocidos() {
        assertEquals(DatosPadre(), RepositorioPadreNoOperativo().datos.value)
    }
}
