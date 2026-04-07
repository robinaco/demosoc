#!/bin/bash
# build-local.sh - Construir y probar localmente

set -e

echo "Construyendo y probando localmente..."

# 1. Compilar
echo "Compilando aplicación..."
./gradlew clean build -x test

# 2. Construir imagen Docker
echo "Construyendo imagen Docker..."
docker build -t mi-crud-app:latest .

# 3. Ejecutar contenedor
echo "Ejecutando contenedor..."
docker rm -f mi-crud-app-local 2>/dev/null || true
docker run -d \
  --name mi-crud-app-local \
  -p 8083:8080 \
  mi-crud-app:latest

# 4. Esperar a que esté listo
echo "Esperando a que la app esté lista..."
sleep 5

# 5. Probar
echo "Probando endpoints..."
curl -s http://localhost:8083/api/personas | jq '.' || echo "No hay datos aún"

echo ""
echo "App corriendo en: http://localhost:8083/api/personas"
echo "Para ver logs: docker logs -f mi-crud-app-local"
echo "Para detener: docker stop mi-crud-app-local"
