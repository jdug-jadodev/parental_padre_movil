"""Inspección dirigida local de APK ORD01; no instala ni certifica ausencia universal de secretos."""
import hashlib
import json
from pathlib import Path
import zipfile

RAIZ = Path(__file__).resolve().parent.parent
marcadores = [b"CP2_PUBLICO_EFIMERO_SIN_AUTORIDAD", b"cp2_node_publico.json",
              b"interop_cp2_jvm_publica.json", b"fake-solo-test"]
descriptores = set()
for carpeta in ["core-protocol/build/classes/kotlin/test",
                "app-parent/build/tmp/kotlin-classes/debugUnitTest"]:
    base = RAIZ / carpeta
    assert base.is_dir(), "Compilar primero los tests JVM"
    for archivo in base.rglob("*.class"):
        nombre = archivo.relative_to(base).as_posix()[:-6]
        descriptores.add(("L" + nombre + ";").encode())
assert descriptores
for ruta in ["core-protocol/src/test/resources/cp2_node_publico.json",
             "pruebas/interop_cp2_jvm_publica.json"]:
    muestra = json.loads((RAIZ / ruta).read_bytes())
    for campo in ["parentPublicKeyB64", "childPublicKeyB64", "pairFrameUtf8",
                  "authParentFrameUtf8", "authChildFrameUtf8"]:
        marcadores.append(muestra[campo].encode())
esperados = [b"Ldev/controlparental/protocol/cp2/MensajesPadreCp2;",
             b"Ldev/controlparental/parent/pairing/PrepararMensajeCp2;",
             b"Ldev/controlparental/parent/security/AutoridadPadre;"]
for variante, nombre in [("debug", "app-parent-debug.apk"),
                         ("release", "app-parent-release-unsigned.apk")]:
    apk = RAIZ / "app-parent/build/outputs/apk" / variante / nombre
    encontrados = []
    dex = b""
    with zipfile.ZipFile(apk) as paquete:
        for entrada in paquete.namelist():
            datos = paquete.read(entrada)
            if entrada.endswith(".dex"):
                dex += datos
            if any(m in datos or m in entrada.encode() for m in marcadores):
                encontrados.append(entrada)
    clases_test = sum(d in dex for d in descriptores)
    assert all(d in dex for d in esperados), "Falta código ORD01 esperado"
    assert not encontrados and clases_test == 0, "Material de test encontrado"
    print(nombre, "SHA256=" + hashlib.sha256(apk.read_bytes()).hexdigest(),
          "descriptoresTest=" + str(len(descriptores)), "coincidenciasTest=0 marcadores=0 codigoORD01=3/3")
