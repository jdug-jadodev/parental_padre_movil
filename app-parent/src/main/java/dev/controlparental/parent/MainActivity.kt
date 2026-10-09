package dev.controlparental.parent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.controlparental.protocol.Protocolo

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent { MaterialTheme { PantallaInicial() } }
    }
}

// C00 solo presenta la base; ninguna confirmación ni autoridad se simula aquí.
@Composable
internal fun PantallaInicial() {
    Scaffold { margenes ->
        Column(
            modifier = Modifier.fillMaxSize().padding(margenes).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(stringResource(R.string.inicio_titulo), style = MaterialTheme.typography.headlineSmall)
            Text(stringResource(R.string.inicio_descripcion))
            Text(stringResource(R.string.inicio_aviso))
            Text(stringResource(R.string.inicio_protocolo, Protocolo.VERSION))
        }
    }
}
