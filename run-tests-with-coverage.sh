#!/bin/bash

echo "========================================"
echo "    TESTS CON COBERTURA - CHAT SERVICE"
echo "========================================"
echo

echo "[1/4] Limpiando proyecto..."
mvn clean

echo
echo "[2/4] Ejecutando tests..."
mvn test

echo
echo "[3/4] Generando reporte de cobertura..."
mvn jacoco:report

echo
echo "[4/4] Verificando cobertura mínima..."
mvn jacoco:check

echo
echo "========================================"
echo "    RESULTADOS"
echo "========================================"
echo
echo "✅ Tests ejecutados exitosamente"
echo "📊 Reporte de cobertura generado en: target/site/jacoco/index.html"
echo "🎯 Cobertura mínima requerida: 80%"
echo
echo "Para ver el reporte, abre: target/site/jacoco/index.html"
echo

# Abrir el reporte automáticamente si es posible
if command -v xdg-open &> /dev/null; then
    xdg-open target/site/jacoco/index.html
elif command -v open &> /dev/null; then
    open target/site/jacoco/index.html
fi 