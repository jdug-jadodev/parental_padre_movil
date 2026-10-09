package dev.controlparental.parent.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

// Indicador independiente de AUTH_OK/PONG: no autoriza órdenes ni solicita otra red.
internal class RedAndroid(context: Context) {
    private val gestor = context.getSystemService(ConnectivityManager::class.java)
    private val mutable = MutableStateFlow(false)
    val validada = mutable.asStateFlow()
    private var observador: ConnectivityManager.NetworkCallback? = null

    @Synchronized fun iniciar() {
        if (observador != null) return
        val nuevo = object : ConnectivityManager.NetworkCallback() {
            private var actual: Network? = null
            override fun onAvailable(network: Network) = synchronized(this@RedAndroid) {
                if (observador === this) { actual = network; mutable.value = false }
            }
            override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) = synchronized(this@RedAndroid) {
                if (observador === this && actual == network) mutable.value =
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            }
            override fun onLost(network: Network) = synchronized(this@RedAndroid) {
                if (observador === this && actual == network) { actual = null; mutable.value = false }
            }
        }
        observador = nuevo
        try { gestor.registerDefaultNetworkCallback(nuevo) }
        catch (_: Exception) { observador = null; mutable.value = false }
    }

    @Synchronized fun detener() {
        val anterior = observador
        observador = null; mutable.value = false
        if (anterior != null) try { gestor.unregisterNetworkCallback(anterior) }
        catch (_: IllegalArgumentException) { /* Ya retirado; nunca se abre otra conexión. */ }
    }
}
