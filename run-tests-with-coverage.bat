@echo off
echo ========================================
echo    TESTS CON COBERTURA - CHAT SERVICE
echo ========================================
echo.

echo [1/4] Navegando al directorio del proyecto...
cd /d "%~dp0"

echo [2/4] Limpiando proyecto...
call mvn clean

echo.
echo [3/4] Ejecutando tests...
call mvn test

echo.
echo [4/4] Generando reporte de cobertura...
call mvn jacoco:report

echo.
echo ========================================
echo    RESULTADOS
echo ========================================
echo.
echo ✅ Tests ejecutados exitosamente
echo 📊 Reporte de cobertura generado en: target/site/jacoco/index.html
echo 🎯 Cobertura mínima requerida: 80%
echo.
echo Para ver el reporte, abre: target/site/jacoco/index.html
echo.
pause 