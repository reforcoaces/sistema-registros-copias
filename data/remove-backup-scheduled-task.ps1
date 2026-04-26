<#
.SYNOPSIS
  Remove a tarefa criada por install-backup-scheduled-task.ps1.
#>
[CmdletBinding()]
param()

$ErrorActionPreference = "Stop"
$taskName = "SistemaCopiasBackupJson"
Unregister-ScheduledTask -TaskName $taskName -Confirm:$false -ErrorAction SilentlyContinue
Write-Host "Removido (se existia): $taskName"
