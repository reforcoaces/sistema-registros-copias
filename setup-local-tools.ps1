param(
    [switch]$Reinstall
)

$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$toolsDir = Join-Path $root ".tools"
$downloadsDir = Join-Path $toolsDir "downloads"
$mavenDir = Join-Path $toolsDir "apache-maven-3.9.9"
$jdkDir = Join-Path $toolsDir "jdk-17.0.18+8"

New-Item -ItemType Directory -Force -Path $downloadsDir | Out-Null

$mavenZip = Join-Path $downloadsDir "apache-maven-3.9.9-bin.zip"
$jdkZip = Join-Path $downloadsDir "jdk17.zip"

if ((Test-Path $mavenDir) -and (Test-Path $jdkDir) -and -not $Reinstall) {
    Write-Host "Ferramentas locais ja instaladas em .tools (nada a fazer)."
    Write-Host "Use ./setup-local-tools.ps1 -Reinstall para reinstalar."
    exit 0
}

if ($Reinstall) {
    try {
        if (Test-Path $mavenDir) { Remove-Item -Recurse -Force $mavenDir }
        if (Test-Path $jdkDir) { Remove-Item -Recurse -Force $jdkDir }
    } catch {
        throw "Nao foi possivel reinstalar porque Java/Maven parecem estar em uso. Feche terminais/processos e tente novamente."
    }
}

if (!(Test-Path $mavenZip)) {
    Invoke-WebRequest -Uri "https://archive.apache.org/dist/maven/maven-3/3.9.9/binaries/apache-maven-3.9.9-bin.zip" -OutFile $mavenZip
}

if (!(Test-Path $jdkZip)) {
    Invoke-WebRequest -Uri "https://api.adoptium.net/v3/binary/latest/17/ga/windows/x64/jdk/hotspot/normal/eclipse" -OutFile $jdkZip
}

Expand-Archive -Path $mavenZip -DestinationPath $toolsDir -Force
Expand-Archive -Path $jdkZip -DestinationPath $toolsDir -Force

Write-Host "Ferramentas locais instaladas em .tools com sucesso."
