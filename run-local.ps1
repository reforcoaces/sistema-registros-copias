$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$jdkDir = Join-Path $root ".tools/jdk-17.0.18+8"
$mavenCmd = Join-Path $root ".tools/apache-maven-3.9.9/bin/mvn.cmd"

if (!(Test-Path $jdkDir)) {
    throw "JDK 17 nao encontrado em .tools. Execute o setup inicial."
}

if (!(Test-Path $mavenCmd)) {
    throw "Maven nao encontrado em .tools. Execute o setup inicial."
}

$env:JAVA_HOME = (Resolve-Path $jdkDir).Path
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

$startPort = 8080
$maxAttempts = 20
$port = $startPort

try {
    for ($i = 0; $i -lt $maxAttempts; $i++) {
        $candidate = $startPort + $i
        $listener = Get-NetTCPConnection -LocalPort $candidate -State Listen -ErrorAction SilentlyContinue
        if (-not $listener) {
            $port = $candidate
            break
        }
    }

    if ($port -ne $startPort) {
        Write-Host "Porta $startPort ocupada. Iniciando aplicacao na porta $port."
    }
} catch {
    # Em ambientes sem permissao para Get-NetTCPConnection, segue com 8080.
}

& $mavenCmd spring-boot:run "-Dspring-boot.run.arguments=--server.port=$port"
