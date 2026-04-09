#!/bin/bash
# setup.sh - Configuración inicial del entorno

set -e

echo "Configurando entorno de desarrollo..."

# 1. Construir imagen de Jenkins
echo "Construyendo imagen de Jenkins con Terraform y plugins..."
docker build -f Dockerfile.jenkins -t jenkins-local:latest .

# 2. Levantar servicios
echo "Levantando contenedores..."
docker-compose up -d

# 3. Esperar a que Jenkins esté listo
echo "Esperando a que Jenkins inicie..."
until curl -s http://localhost:8080/login | grep -q "jenkins"; do
    sleep 2
done

# 4. Obtener password inicial
echo ""
echo "========================================="
echo "Password inicial de Jenkins:"
docker exec jenkins-local cat /var/jenkins_home/secrets/initialAdminPassword
echo "========================================="
echo ""

# 5. Crear backup de configuración inicial
echo "Creando backup de configuración inicial..."
docker exec jenkins-local mkdir -p /backup
docker exec jenkins-local cp -r /var/jenkins_home/* /backup/ 2>/dev/null || true

# 6. Esperar a LocalStack
echo "Esperando a LocalStack..."
until curl -s http://localhost:4566/_localstack/health | grep -q '"ecs":"available"'; do
    sleep 2
done

echo "Entorno listo!"
echo ""
echo "Jenkins: http://localhost:8080"
echo "LocalStack: http://localhost:4566"
echo ""
echo "Para restaurar configuración si se pierde:"
echo "   ./restore.sh ./jenkins-backup/YYYYMMDD_HHMMSS"
