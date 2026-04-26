<#
.SYNOPSIS
  Cria tarefa no Windows para correr sync-data-backup-scheduled.ps1 de 5 em 5 minutos.
#>
[CmdletBinding()]
param(
    [string] $BackupFolder = "C:\Users\diogosilveira\OneDrive\Documentos\base-de-dados"
)

$ErrorActionPreference = "Stop"

$taskName = "SistemaCopiasBackupJson"
$dataDir = Split-Path -Parent $MyInvocation.MyCommand.Path
$syncScript = Join-Path $dataDir "sync-data-backup-scheduled.ps1"

if (-not (Test-Path -LiteralPath $syncScript)) {
    throw "Script nao encontrado: $syncScript"
}

$bfEsc = $BackupFolder -replace '"', '`"'
$arg = "-NoProfile -ExecutionPolicy Bypass -WindowStyle Hidden -File `"$syncScript`" -BackupFolder `"$bfEsc`""
$action = New-ScheduledTaskAction -Execute "powershell.exe" -Argument $arg -WorkingDirectory $dataDir

$userId = [System.Security.Principal.WindowsIdentity]::GetCurrent().Name
$principal = New-ScheduledTaskPrincipal -UserId $userId -LogonType Interactive -RunLevel Limited

$settings = New-ScheduledTaskSettingsSet `
    -AllowStartIfOnBatteries `
    -DontStopIfGoingOnBatteries `
    -StartWhenAvailable `
    -MultipleInstances IgnoreNew `
    -ExecutionTimeLimit (New-TimeSpan -Minutes 2)

# Repeticao: Windows costuma rejeitar Duration infinita; 10 anos e suficiente.
$start = Get-Date
$trigger = New-ScheduledTaskTrigger -Once -At $start `
    -RepetitionInterval (New-TimeSpan -Minutes 5) `
    -RepetitionDuration (New-TimeSpan -Days 3650)

Unregister-ScheduledTask -TaskName $taskName -Confirm:$false -ErrorAction SilentlyContinue

Register-ScheduledTask `
    -TaskName $taskName `
    -Action $action `
    -Trigger $trigger `
    -Settings $settings `
    -Principal $principal `
    -Description "Backup de data/orders.json e data/users.json para pasta externa se houver alteracao."

Write-Host "Tarefa registada: $taskName (a cada 5 minutos)"
Write-Host "Script: $syncScript"
Write-Host "Destino: $BackupFolder"
Write-Host "Remover: .\remove-backup-scheduled-task.ps1"
