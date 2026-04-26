<#
.SYNOPSIS
  Se orders.json ou users.json mudaram desde a ultima execucao, copia para a pasta de backup.

.DESCRIPTION
  Corre no Agendador (ex.: a cada 5 minutos). NAO corre no mesmo instante em que a app grava:
  so na proxima execucao da tarefa e se o ficheiro tiver LastWriteTime diferente do guardado.

  Para copia imediata a cada gravacao pela aplicacao, configure app.backup.folder (ou
  APP_BACKUP_FOLDER) no application.properties / ambiente — a propria app copia apos salvar.

  Na primeira execucao deste script grava apenas o estado e NAO copia.
  Depois, so copia se a data de modificacao de algum dos ficheiros tiver mudado.

.PARAMETER BackupFolder
  Destino das copias.
#>
[CmdletBinding()]
param(
    [string] $BackupFolder = "C:\Users\diogosilveira\OneDrive\Documentos\base-de-dados"
)

$ErrorActionPreference = "Stop"

$dataDir = $PSScriptRoot
$statePath = Join-Path $dataDir ".backup-sync-state.json"
$map = [ordered]@{
    orders = "orders.json"
    users  = "users.json"
}

function Get-WriteTicks([string] $filePath) {
    if (-not (Test-Path -LiteralPath $filePath)) { return $null }
    return [string]((Get-Item -LiteralPath $filePath).LastWriteTimeUtc.Ticks)
}

function Read-State {
    if (-not (Test-Path -LiteralPath $statePath)) { return $null }
    try {
        return Get-Content -LiteralPath $statePath -Encoding UTF8 -Raw | ConvertFrom-Json
    } catch {
        return $null
    }
}

function Write-State([System.Collections.IDictionary] $ticksByKey) {
    ($ticksByKey | ConvertTo-Json -Compress) | Set-Content -LiteralPath $statePath -Encoding UTF8 -NoNewline
}

$current = [ordered]@{}
foreach ($entry in $map.GetEnumerator()) {
    $key = $entry.Key
    $name = $entry.Value
    $path = Join-Path $dataDir $name
    $current[$key] = Get-WriteTicks $path
}

$prev = Read-State

if ($null -eq $prev) {
    Write-State -ticksByKey $current
    Write-Host ("[{0}] Estado inicial gravado. Sem copia nesta primeira execucao." -f (Get-Date -Format "yyyy-MM-dd HH:mm:ss"))
    exit 0
}

$changed = $false
foreach ($key in $map.Keys) {
    $before = $prev.$key
    $after = $current[$key]
    if ($before -ne $after) {
        $changed = $true
        break
    }
}

if (-not $changed) {
    Write-Host ("[{0}] Sem alteracoes em orders.json / users.json. Nada a copiar." -f (Get-Date -Format "yyyy-MM-dd HH:mm:ss"))
    exit 0
}

New-Item -ItemType Directory -Path $BackupFolder -Force | Out-Null

foreach ($entry in $map.GetEnumerator()) {
    $name = $entry.Value
    $src = Join-Path $dataDir $name
    if (-not (Test-Path -LiteralPath $src)) { continue }
    $dst = Join-Path $BackupFolder $name
    Copy-Item -LiteralPath $src -Destination $dst -Force
    Write-Host ("[{0}] Copiado: {1}" -f (Get-Date -Format "yyyy-MM-dd HH:mm:ss"), $name)
}

Write-State -ticksByKey $current
Write-Host ("[{0}] Backup concluido." -f (Get-Date -Format "yyyy-MM-dd HH:mm:ss"))
exit 0
