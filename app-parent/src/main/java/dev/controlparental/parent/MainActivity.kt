package dev.controlparental.parent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.controlparental.parent.presentation.ParentViewModel
import dev.controlparental.parent.ui.AplicacionPadre

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val modelo = ViewModelProvider(this, AppContainer().fabrica)[ParentViewModel::class.java]
        setContent {
            val estado by modelo.estado.collectAsStateWithLifecycle()
            MaterialTheme { AplicacionPadre(estado, modelo::navegar, modelo::volver) }
        }
    }
}
