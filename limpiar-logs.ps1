# Limpieza diaria de los logs del servicio. Borra los que NSSM va archivando en cada reinicio
# (service-<timestamp>.log / service-error-<timestamp>.log) cuando tienen mas de 30 dias.
# No toca service.log ni service-error.log: esos son los del proceso actual, siempre en uso.
$ErrorActionPreference = "Stop"

$logsDir = "C:\Users\MSI\Desktop\Heladeria\logs"

Get-ChildItem $logsDir -Filter "service*.log" |
    Where-Object { $_.Name -ne "service.log" -and $_.Name -ne "service-error.log" } |
    Where-Object { $_.LastWriteTime -lt (Get-Date).AddDays(-30) } |
    Remove-Item -Force

Write-Host "Limpieza de logs completada."
