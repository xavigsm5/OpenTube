@echo off
echo ========================================
echo    Script de Compilacion OpenTube
echo ========================================
echo.

echo [1/5] Deteniendo procesos Java...
taskkill /F /IM java.exe 2>nul
taskkill /F /IM studio64.exe 2>nul
timeout /t 3 /nobreak >nul

echo [2/5] Eliminando cache corrupto de Gradle...
rd /s /q "%USERPROFILE%\.gradle\caches\transforms-3" 2>nul
rd /s /q "%USERPROFILE%\.gradle\daemon" 2>nul
timeout /t 2 /nobreak >nul

echo [3/5] Limpiando directorios de compilacion local...
rd /s /q "build" 2>nul
rd /s /q "app\build" 2>nul
rd /s /q ".gradle" 2>nul

echo [4/5] Esperando a que se liberen los archivos...
timeout /t 3 /nobreak >nul

echo [5/5] Compilando APK con JDK correcto...
echo.
set JAVA_HOME=C:\Program Files\Android\Android Studio\jbr
call gradlew.bat clean assembleDebug --no-daemon --warning-mode all

echo.
echo ========================================
if %ERRORLEVEL% NEQ 0 goto fail

echo    COMPILACION EXITOSA!
echo    APK: app\build\outputs\apk\debug\app-debug.apk
goto end

:fail
echo    COMPILACION FALLIDA - Revisa los errores arriba

:end
echo ========================================
pause
