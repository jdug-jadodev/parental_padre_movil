package dev.controlparental.parent.security

import android.app.Activity
import android.app.KeyguardManager
import android.hardware.biometrics.BiometricManager
import android.hardware.biometrics.BiometricPrompt
import android.os.CancellationSignal

// Prompt oficial API 30+: sin PIN propio, biometría débil ni simulación en producción.
internal class LocalAuthentication(private val activity: Activity, private val sesion: SesionAutoridad) {
    private var cancelacion: CancellationSignal? = null

    fun cancelar() {
        sesion.invalidar()
        val anterior = cancelacion
        cancelacion = null
        anterior?.cancel()
    }

    fun solicitar(resultado: (Boolean) -> Unit) {
        cancelar()
        if (!activity.getSystemService(KeyguardManager::class.java).isDeviceSecure) {
            resultado(false)
            return
        }
        val ticket = sesion.solicitar()
        val signal = CancellationSignal()
        cancelacion = signal
        try {
            BiometricPrompt.Builder(activity)
                .setTitle("Autenticar al adulto")
                .setDescription("Usa la credencial del teléfono o biometría fuerte. La autoridad caduca en 60 segundos.")
                .setAllowedAuthenticators(BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL)
                .build().authenticate(signal, activity.mainExecutor, object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        if (cancelacion !== signal) return
                        cancelacion = null
                        resultado(sesion.completar(ticket))
                    }
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        if (cancelacion !== signal) return
                        cancelacion = null
                        sesion.invalidar()
                        resultado(false)
                    }
                })
        } catch (_: Exception) {
            cancelar()
            resultado(false)
        }
    }
}
