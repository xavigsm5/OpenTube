# Script para limpiar procesos y cachés antes de compilar en Android Studio
# Ejecuta este script ANTES de abrir Android Studio

Write-Host "🧹 Limpiando procesos y cachés de Gradle..." -ForegroundColor Cyan

# 1. Detener TODOS los procesos Java y Android Studio
Write-Host "`n1️⃣ Deteniendo procesos Java y Android Studio..." -ForegroundColor Yellow
Get-Process java,studio64,gradle -ErrorAction SilentlyContinue | Stop-Process -Force -ErrorAction SilentlyContinue
Start-Sleep -Seconds 3

# Verificar y detener procesos que quedaron
$javaProcesses = Get-Process java -ErrorAction SilentlyContinue
if ($javaProcesses) {
    Write-Host "   ⚠️ Deteniendo $($javaProcesses.Count) procesos Java adicionales..." -ForegroundColor Yellow
    $javaProcesses | Stop-Process -Force -ErrorAction SilentlyContinue
    Start-Sleep -Seconds 2
}

# 2. Eliminar carpetas de caché de Gradle
Write-Host "`n2️⃣ Eliminando cachés de Gradle..." -ForegroundColor Yellow
Remove-Item -Path "$env:USERPROFILE\.gradle\caches" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "$env:USERPROFILE\.gradle\native" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path "$env:USERPROFILE\.gradle\daemon" -Recurse -Force -ErrorAction SilentlyContinue

# 3. Limpiar carpetas build del proyecto
Write-Host "`n3️⃣ Limpiando carpetas build del proyecto..." -ForegroundColor Yellow
Set-Location "C:\Users\rexit\Desktop\OpenTube"
Remove-Item -Path ".\build" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path ".\.gradle" -Recurse -Force -ErrorAction SilentlyContinue
Remove-Item -Path ".\app\build" -Recurse -Force -ErrorAction SilentlyContinue

# 4. Esperar que se liberen todos los archivos
Write-Host "`n4️⃣ Esperando que se liberen archivos..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

# 5. Verificación final
Write-Host "`n✅ LISTO PARA COMPILAR" -ForegroundColor Green
Write-Host "`nAhora puedes:" -ForegroundColor Cyan
Write-Host "  1. Abrir Android Studio" -ForegroundColor White
Write-Host "  2. File → Open → C:\Users\rexit\Desktop\OpenTube" -ForegroundColor White
Write-Host "  3. Esperar Gradle Sync" -ForegroundColor White
Write-Host "  4. Build → Build APK(s)" -ForegroundColor White
Write-Host "`n📦 Tu APK estará en: app\build\outputs\apk\debug\app-debug.apk`n" -ForegroundColor Yellow

# Verificar que no hay procesos Java
$remainingJava = Get-Process java -ErrorAction SilentlyContinue
if ($remainingJava) {
    Write-Host "⚠️ ADVERTENCIA: Todavía hay $($remainingJava.Count) procesos Java corriendo" -ForegroundColor Red
    Write-Host "   Ejecútalo de nuevo antes de abrir Android Studio" -ForegroundColor Red
} else {
    Write-Host "✓ No hay procesos Java corriendo" -ForegroundColor Green
}
