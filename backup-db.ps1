# Backup diario de la base de datos heladeria_db. Guarda en .\backups y borra copias de mas de 30 dias.
$ErrorActionPreference = "Stop"

$backupDir = "C:\Users\MSI\Desktop\Heladeria\backups"
if (-not (Test-Path $backupDir)) {
    New-Item -ItemType Directory -Path $backupDir | Out-Null
}

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$file = "$backupDir\heladeria_db_$timestamp.sql"

if (-not $env:HELADERIA_DB_PASSWORD) {
    Write-Error "Falta la variable de entorno HELADERIA_DB_PASSWORD"
    exit 1
}
$env:PGPASSWORD = $env:HELADERIA_DB_PASSWORD
& "C:\Program Files\PostgreSQL\18\bin\pg_dump.exe" -U heladeria_app -h localhost -d heladeria_db -f $file
Remove-Item Env:\PGPASSWORD

if ($LASTEXITCODE -ne 0) {
    Write-Error "pg_dump fallo con codigo $LASTEXITCODE"
    exit 1
}

# Retencion: borrar backups de mas de 30 dias
Get-ChildItem $backupDir -Filter "heladeria_db_*.sql" |
    Where-Object { $_.LastWriteTime -lt (Get-Date).AddDays(-30) } |
    Remove-Item -Force

Write-Host "Backup creado: $file"
