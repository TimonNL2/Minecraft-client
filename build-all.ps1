param([switch]$GameTests)
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    $localJdk = Join-Path $PSScriptRoot '.tools/jdk25/jdk-25.0.4.1+1'
    if (Test-Path -LiteralPath (Join-Path $localJdk 'bin/java.exe')) { $env:JAVA_HOME = $localJdk }
    $env:GRADLE_USER_HOME = Join-Path $PSScriptRoot '.gradle-user-home'
    foreach ($mcVersion in @('26.3')) {
        $renderers = if ($GameTests) { @('vanilla', 'sodium') } else { @('vanilla') }
        foreach ($renderer in $renderers) {
            $buildTasks = if ($GameTests) { @('runClientGameTest', 'collectRelease') } else { @('collectRelease') }
            & (Join-Path $PSScriptRoot 'gradlew.bat') "-Pminecraft_version=$mcVersion" "-Ptest_renderer=$renderer" @buildTasks --console=plain
            if ($LASTEXITCODE -ne 0) { throw "Build/test failed for Minecraft $mcVersion with $renderer." }
            if ($GameTests) {
                $evidence = Join-Path $PSScriptRoot "verification/local/$mcVersion-$renderer"
                New-Item -ItemType Directory -Force $evidence | Out-Null
                Copy-Item -Path (Join-Path $PSScriptRoot 'build/run/clientGameTest/screenshots/*.png') -Destination $evidence
                Copy-Item -LiteralPath (Join-Path $PSScriptRoot 'build/run/clientGameTest/logs/latest.log') -Destination (Join-Path $evidence 'client-test.log')
            }
        }
    }
    Get-ChildItem (Join-Path $PSScriptRoot 'dist') -Filter '*.jar' | Sort-Object Name | ForEach-Object {
        '{0}  {1}' -f (Get-FileHash $_.FullName -Algorithm SHA256).Hash.ToLowerInvariant(), $_.Name
    } | Set-Content -LiteralPath (Join-Path $PSScriptRoot 'dist/SHA256SUMS.txt')
} finally {
    Pop-Location
}
