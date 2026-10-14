package dev.controlparental.parent

import com.google.zxing.*
import com.google.zxing.common.HybridBinarizer
import com.google.zxing.qrcode.QRCodeReader
import dev.controlparental.parent.pairing.QrLocal
import dev.controlparental.protocol.*
import java.util.Base64
import kotlinx.serialization.json.*
import org.junit.Assert.*
import org.junit.Test

class EstrategiaQrTest {
    @Test fun presupuestoFijoDeOchoLecturasDelMismoContenido() {
        val json = javaClass.getResourceAsStream("/qr01_respuesta_publica.json")!!.use {
            Json.parseToJsonElement(it.readBytes().toString(Charsets.UTF_8)).jsonObject
        }
        val frame = Base64.getUrlDecoder().decode(json.getValue("frameB64").jsonPrimitive.content)
        val publica = ClaveVerificacion.desdeSpki(Rol.PARENT, json.getValue("publica").jsonPrimitive.content)
        val matriz = QrLocal.generar(frame.toString(Charsets.UTF_8))
        var gris = ByteArray(matriz.width * matriz.height) { if (matriz[it % matriz.width, it / matriz.width]) 0 else -1 }
        var correctos = 0
        repeat(4) { rotacion ->
            for (intensivo in listOf(false, true)) {
                val imagen = BinaryBitmap(HybridBinarizer(PlanarYUVLuminanceSource(gris,
                    matriz.width, matriz.height, 0, 0, matriz.width, matriz.height, false)))
                val hints = mutableMapOf<DecodeHintType, Any>(DecodeHintType.CHARACTER_SET to "UTF-8")
                if (intensivo) hints[DecodeHintType.TRY_HARDER] = true
                try {
                    val resultado = QRCodeReader().decode(imagen, hints)
                    assertArrayEquals(frame, resultado.text.toByteArray(Charsets.UTF_8))
                    CriptografiaProtocolo.verificarSobre(resultado.text.toByteArray(Charsets.UTF_8), publica, Proposito.OFFLINE)
                    correctos++
                    println("ESTRATEGIA grados=${rotacion * 90} intensivo=$intensivo resultado=BYTES_EXACTOS")
                } catch (e: ReaderException) {
                    println("ESTRATEGIA grados=${rotacion * 90} intensivo=$intensivo resultado=${e.javaClass.simpleName}")
                }
            }
            val anterior = gris; val lado = matriz.width
            gris = ByteArray(anterior.size) { anterior[(lado - 1 - it % lado) * lado + it / lado] }
        }
        assertEquals("Fixture fijo: dos fallos 0° y seis lecturas geométricas exactas", 6, correctos)
    }
}
