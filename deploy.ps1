# Compila el frontend, lo copia al backend, y empaqueta un JAR único listo para producción.
# Uso: .\deploy.ps1

$ErrorActionPreference = "Stop"
$root = $PSScriptRoot

$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
$env:PATH = "C:\Program Files\nodejs;" + $env:PATH

Write-Host "1/4 - Compilando frontend (Angular, produccion)..." -ForegroundColor Cyan
Set-Location "$root\frontend"
npx ng build --configuration production

Write-Host "2/4 - Copiando build al backend..." -ForegroundColor Cyan
$staticDir = "$root\backend\src\main\resources\static"
if (Test-Path $staticDir) {
    Remove-Item "$staticDir\*" -Recurse -Force
} else {
    New-Item -ItemType Directory -Path $staticDir | Out-Null
}
Copy-Item "$root\frontend\dist\frontend\browser\*" $staticDir -Recurse -Force

Write-Host "3/4 - Empaquetando backend (Maven)..." -ForegroundColor Cyan
Set-Location "$root\backend"
.\mvnw.cmd clean package -DskipTests

Write-Host "4/4 - Listo." -ForegroundColor Green
$jar = Get-ChildItem "$root\backend\target\*.jar" | Where-Object { $_.Name -notlike "*sources*" } | Select-Object -First 1
Write-Host "JAR generado: $($jar.FullName)"
Write-Host "Para correrlo: java -jar `"$($jar.FullName)`""
