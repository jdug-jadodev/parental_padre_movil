package dev.controlparental.parent.ui

import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

// Pública únicamente; no se copia al portapapeles ni se registra en logs.
@Composable
internal fun IdentidadPadre(aviso: String, publica: String?, crear: () -> Unit, autenticar: () -> Unit) {
    Text(aviso)
    Button(onClick = crear) { Text("Crear o cargar identidad protegida") }
    OutlinedButton(onClick = autenticar, enabled = publica != null) { Text("Autenticar al adulto") }
    if (publica != null) Text("Clave pública SPKI (base64url): $publica")
}
