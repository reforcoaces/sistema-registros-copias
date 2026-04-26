<#
.SYNOPSIS
  Copia orders.json e users.json para uma pasta de backup quando forem alterados.

.DESCRIPTION
  Vigia a pasta data (onde este script esta) e, ao detetar mudanca na data de
  modificacao de orders.json ou users.json, copia os ficheiros para BackupFolder.

  Mantenha a janela aberta enquanto quiser backups em tempo quase real.
  Para parar: Ctrl+C

.PARAMETER BackupFolder
  Destino das copias (por omissao: OneDrive\Documentos\base-de-dados).

.PARAMETER PollSeconds
  Intervalo entre verificacoes (por omissao 0.8 s).
#>
[CmdletBinding()]
param(
    [string] $BackupFolder = "C:\Users\diogosilveira\OneDrive\Documentos\base-de-dados",
    [double] $PollSeconds = 0.8
)

$ErrorActionPreference = "Stop"

$dataDir = $PSScriptRoot
$watchFiles = @("orders.json", "users.json")

if (-not (Test-Path $dataDir)) {
    throw "Pasta data nao encontrada: $dataDir"
}

New-Item -ItemType Directory -Path $BackupFolder -Force | Out-Null

$lastWrite = @{}
foreach ($name in $watchFiles) {
    $p = Join-Path $dataDir $name
    if (Test-Path $p) {
        $lastWrite[$name] = (Get-Item -LiteralPath $p).LastWriteTimeUtc
    } else {
        $lastWrite[$name] = $null
    }
}

function Copy-WatchedFiles {
    foreach ($name in $watchFiles) {
        $src = Join-Path $dataDir $name
        if (-not (Test-Path $src)) { continue }
        $dst = Join-Path $BackupFolder $name
        Copy-Item -LiteralPath $src -Destination $dst -Force
        Write-Host ("[{0}] Backup: {1} -> {2}" -f (Get-Date -Format "yyyy-MM-dd HH:mm:ss"), $name, $dst)
    }
}

Write-Host "A vigiar: $dataDir"
Write-Host "Destino:  $BackupFolder"
Write-Host "Ficheiros: $($watchFiles -join ', ')"
Write-Host "Copia inicial (se existirem)..."
Copy-WatchedFiles
Write-Host "Em vigia. Ctrl+C para parar.`n"

$pollMs = [int]([Math]::Max(200, $PollSeconds * 1000))

try {
    while ($true) {
        foreach ($name in $watchFiles) {
            $src = Join-Path $dataDir $name
            if (-not (Test-Path $src)) { continue }

            $wt = (Get-Item -LiteralPath $src).LastWriteTimeUtc
            if ($lastWrite[$name] -ne $wt) {
                $lastWrite[$name] = $wt
                $dst = Join-Path $BackupFolder $name
                Copy-Item -LiteralPath $src -Destination $dst -Force
                Write-Host ("[{0}] Alteracao: {1}" -f (Get-Date -Format "yyyy-MM-dd HH:mm:ss"), $name)
            }
        }
        Start-Sleep -Milliseconds $pollMs
    }
} finally {
    Write-Host "Monitorizacao terminada."
}
