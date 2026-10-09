# Entorno de compilación — C00–C02, P00, P01, P03 y P02 parcial

P02 añade ZXing core 3.5.3 fijado, sin scanner remoto ni Play Services. Usa
PickVisualMedia de Activity 1.10.1 ya disponible, con fallback oficial a documentos;
no permisos de cámara/galería completa. Fuentes consultadas:
https://developer.android.com/training/data-storage/shared/photopicker
https://github.com/zxing/zxing/tree/zxing-3.5.3/core

## Herramientas observadas

| Herramienta | Versión/configuración |
|---|---|
| Sistema | Windows 11, amd64 |
| Android Studio | AI-253.32098.37.2534.15232325; directorio AndroidStudio2025.3.4 |
| JDK instalado y usado por Gradle | Oracle 21.0.6; compilación con destino JVM 17 |
| Gradle | Wrapper 8.13, distribución bin con SHA-256 fijado |
| Android Gradle Plugin | 8.11.1 |
| Kotlin y plugin Compose | 2.2.0 |
| Plugin serialization / kotlinx.serialization | 2.2.0 / 1.9.0 |
| Compose BOM | 2025.06.01 |
| Android | minSdk 30; compileSdk y targetSdk 36; Build Tools 35.0.0 |
| Node | 24.21.0 |
| npm | 11.19.0 |
| TypeScript | 5.9.3 |
| ws / tsx | 8.22.0 / 4.23.15 |
| Ajv (JSON Schema 2020-12) | 8.20.0 |
| Lifecycle ViewModel / runtime Compose | 2.9.1 |
| Coroutines core / test | 1.10.2 |

Las versiones Android están fijadas en `gradle/libs.versions.toml`. Las de Node
están fijadas en `relay/package.json` y `relay/package-lock.json`.
El JDK 21 ejecuta las herramientas; el bytecode del proyecto tiene destino 17.

SDK local detectado: `C:/Users/Usuario/AppData/Local/Android/Sdk`.
La ruta particular vive en `local.properties`, excluido de Git. En otro equipo,
Android Studio puede crear ese archivo al abrir el proyecto.

Compatibilidad AGP 8.11, Gradle 8.13 y API 36 consultada en la fuente oficial:
https://developer.android.com/build/releases/past-releases/agp-8-11-0-release-notes

APIs primarias consultadas para las pruebas Keystore y assets generados:
https://developer.android.com/privacy-and-security/keystore
https://developer.android.com/reference/tools/gradle-api/8.11/com/android/build/api/variant/SourceDirectories

C02 usa SHA256withECDSA del JDK y SHA-256/ECDSA DER de node:crypto. No añade
bibliotecas criptográficas ni implementa curvas. La prueba cruzada usa Node 24
y tsx en procesos locales, con claves efímeras solo en memoria.

Fuentes primarias consultadas para P00 (estado hoisted, ViewModel y recolección
de StateFlow respetando lifecycle):
https://developer.android.com/develop/ui/compose/state
https://developer.android.com/develop/ui/compose/state-hoisting

P00 conserva inyección manual y no añade Hilt ni biblioteca de navegación.
El recorrido simple vive en ParentViewModel. Reiniciar el proceso vuelve al
primer inicio; la persistencia de autoridad/vínculo corresponde a tareas futuras.

P01 usa APIs oficiales de plataforma API 30, sin dependencias adicionales:
https://developer.android.com/reference/android/security/keystore/KeyGenParameterSpec.Builder#setUserAuthenticationParameters(int,%20int)
https://developer.android.com/reference/android/hardware/biometrics/BiometricPrompt.Builder#setAllowedAuthenticators(int)

No requiere StrongBox ni afirma respaldo de hardware específico. El contrato de
KeyInfo se comprueba al usar la clave; propiedades reales y funcionamiento del
prompt con credencial/biometría quedan pendientes del teléfono.

P03 usa plugin serialization ya fijado en el catálogo y dependencia JSON
explícita en app-parent. No añade versiones ni librerías criptográficas nuevas.
Fuentes primarias consultadas para AtomicFile y AES-GCM de Android Keystore:
https://developer.android.com/reference/android/util/AtomicFile
https://developer.android.com/reference/android/security/keystore/KeyGenParameterSpec

AtomicFile no aporta bloqueo de concurrencia: AlmacenPadreAndroid conserva una
instancia por proceso y ParentStore serializa operaciones en una coroutine.
No se admite un segundo proceso escritor ni edición externa del archivo privado.

## Límites de la verificación

- La compilación local no demuestra funcionamiento en un teléfono.
- No se ha usado ADB ni instalado APK alguno: requiere aviso y habilitación del propietario.
- Python está disponible, pero `jsonschema` no está instalado. El verificador estructural permanece pendiente; no se instalaron paquetes globales.
- No se han comprobado biometría, Keystore, compatibilidad del hijo ni Render.
