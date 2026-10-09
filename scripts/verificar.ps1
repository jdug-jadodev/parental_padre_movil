param(
    [ValidateSet('todo', 'nucleo', 'app', 'relay')]
    [string]$Modulo = 'todo'
)

$ErrorActionPreference = 'Stop'
$raiz = Split-Path $PSScriptRoot -Parent

function Ejecutar {
    param([string]$Programa, [string[]]$Argumentos)
    & $Programa @Argumentos
    if ($LASTEXITCODE -ne 0) {
        throw "Falló $Programa con código $LASTEXITCODE."
    }
}

Push-Location $raiz
try {
    if ($Modulo -in @('todo', 'nucleo')) {
        Ejecutar '.\gradlew.bat' @(':core-protocol:test', '--console=plain')
    }
    if ($Modulo -in @('todo', 'app')) {
        Ejecutar '.\gradlew.bat' @(
            ':app-parent:testDebugUnitTest', ':app-parent:assembleDebug',
            ':app-parent:assembleDebugAndroidTest', ':app-parent:lintDebug', '--console=plain'
        )
    }
    if ($Modulo -in @('todo', 'relay')) {
        Push-Location 'relay'
        try {
            Ejecutar 'npm.cmd' @('ci')
            Ejecutar 'npm.cmd' @('run', 'check')
            Ejecutar 'npm.cmd' @('run', 'build')
            Ejecutar 'npm.cmd' @('test')
        } finally { Pop-Location }
    }
    if ($Modulo -eq 'todo') {
        Ejecutar '.\gradlew.bat' @(':core-protocol:testInterop', '--console=plain')
    }
    Ejecutar 'node' @('pruebas/verificar_vectores.mjs')
    Write-Host 'Verificación local terminada. No se ejecutaron pruebas en un dispositivo.'
} finally { Pop-Location }
