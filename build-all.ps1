param([switch]$GameTests)
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    $localJdk = Join-Path $PSScriptRoot '.tools/jdk25/jdk-25.0.4.1+1'
    if (Test-Path -LiteralPath (Join-Path $localJdk 'bin/java.exe')) { $env:JAVA_HOME = $localJdk }
    $env:GRADLE_USER_HOME = Join-Path $PSScriptRoot '.gradle-user-home'
    foreach ($mcVersion in @('26.2', '26.3')) {
        & (Join-Path $PSScriptRoot 'gradlew.bat') "-Pminecraft_version=$mcVersion" collectRelease --console=plain
        if ($LASTEXITCODE -ne 0) { throw "Build voor Minecraft $mcVersion mislukt." }
        if ($GameTests) {
            & (Join-Path $PSScriptRoot 'gradlew.bat') "-Pminecraft_version=$mcVersion" runClientGameTest --console=plain
            if ($LASTEXITCODE -ne 0) { throw "Speltest voor Minecraft $mcVersion mislukt." }
        }
    }
} finally {
    Pop-Location
}
