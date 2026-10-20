# Instala el backend como servicio de Windows (arranque automatico al iniciar el sistema).
# Correr en una PowerShell como Administrador: .\instalar-servicio.ps1

$nssm = "C:\Users\MSI\AppData\Local\Microsoft\WinGet\Packages\NSSM.NSSM_Microsoft.Winget.Source_8wekyb3d8bbwe\nssm-2.24-101-g897c7ad\win64\nssm.exe"
$java = "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot\bin\java.exe"
$jar  = "C:\Users\MSI\Desktop\Heladeria\backend\target\backend-0.0.1-SNAPSHOT.jar"

& $nssm install HeladeriaBackend $java "-jar `"$jar`""
& $nssm set HeladeriaBackend AppDirectory "C:\Users\MSI\Desktop\Heladeria\backend"
& $nssm set HeladeriaBackend Start SERVICE_AUTO_START
& $nssm set HeladeriaBackend AppStdout "C:\Users\MSI\Desktop\Heladeria\logs\service.log"
& $nssm set HeladeriaBackend AppStderr "C:\Users\MSI\Desktop\Heladeria\logs\service-error.log"
& $nssm set HeladeriaBackend AppRotateFiles 1
& $nssm set HeladeriaBackend AppRotateBytes 5242880
& $nssm start HeladeriaBackend

Write-Host ""
Write-Host "Listo. Verificando estado..." -ForegroundColor Green
Start-Sleep -Seconds 3
Get-Service HeladeriaBackend
