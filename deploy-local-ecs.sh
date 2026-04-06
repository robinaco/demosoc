#!/bin/bash

echo "Desplegando infraestructura ECS local con Terraform"
echo "======================================================"

# Variables de colores
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Verificar que estamos en el directorio correcto
if [ ! -f "Dockerfile" ]; then
    echo -e "${RED}Error: No se encuentra Dockerfile en el directorio actual${NC}"
    echo "Asegúrate de ejecutar este script desde la raíz de tu proyecto"
    exit 1
fi

# Verificar Docker
if ! command -v docker &> /dev/null; then
    echo -e "${RED}Docker no está instalado${NC}"
    exit 1
fi

# Verificar Terraform
if ! command -v terraform &> /dev/null; then
    echo -e "${RED}Terraform no está instalado${NC}"
    echo "Instálalo desde: https://developer.hashicorp.com/terraform/downloads"
    exit 1
fi

# Crear directorio de logs
mkdir -p terraform-local/logs

# Navegar al directorio de Terraform
cd terraform-local

# Inicializar Terraform
echo -e "${YELLOW} Inicializando Terraform...${NC}"
terraform init

# Validar configuración
echo -e "${YELLOW} Validando configuración...${NC}"
terraform validate

# Verificar si ya hay recursos
if terraform plan -detailed-exitcode &> /dev/null; then
    echo -e "${YELLOW} Actualizando infraestructura existente...${NC}"
else
    echo -e "${GREEN}🏗  Creando nueva infraestructura...${NC}"
fi

# Aplicar Terraform
echo -e "${YELLOW} Aplicando configuración...${NC}"
terraform apply -auto-approve

# Verificar resultado
if [ $? -eq 0 ]; then
    echo ""
    echo -e "${GREEN} Infraestructura desplegada exitosamente!${NC}"
    echo ""

    # Mostrar outputs
    terraform output

    echo ""
    echo -e "${YELLOW} Esperando que los servicios inicien (30 segundos)...${NC}"
    sleep 30

    # Verificar servicios
    echo ""
    echo -e "${GREEN}Verificando servicios:${NC}"

    # Verificar Jenkins
    if curl -s -o /dev/null -w "%{http_code}" http://localhost:8080 | grep -q "200\|302\|403"; then
        echo -e "  ${GREEN} Jenkins: http://localhost:8080${NC}"
    else
        echo -e "  ${RED} Jenkins no responde aún (espera un poco más)${NC}"
    fi

    # Verificar App
    if curl -s -o /dev/null -w "%{http_code}" http://localhost:8083/api/personas | grep -q "200"; then
        echo -e "  ${GREEN} Tu App: http://localhost:8083/api/personas${NC}"
    else
        echo -e "  ${RED} App no responde aún (espera un poco más)${NC}"
    fi

    # Verificar ALB
    if curl -s -o /dev/null -w "%{http_code}" http://localhost/health | grep -q "200"; then
        echo -e "  ${GREEN} Load Balancer: http://localhost${NC}"
    else
        echo -e "  ${RED} Load Balancer no responde${NC}"
    fi

    echo ""
    echo -e "${YELLOW} Password inicial de Jenkins:${NC}"
    docker exec $(docker ps -q -f name=mi-crud-app-jenkins) cat /var/jenkins_home/secrets/initialAdminPassword 2>/dev/null || echo "Jenkins aún iniciando..."

    echo ""
    echo -e "${GREEN} TODO LISTO!${NC}"
    echo "  Jenkins: http://localhost:8080"
    echo "  Tu App: http://localhost:8083/api/personas"
    echo "  ALB: http://localhost"
    echo ""
    echo "Comandos útiles:"
    echo "  Ver logs: docker logs -f $(docker ps -q -f name=mi-crud-app-jenkins)"
    echo "  Detener: terraform destroy"

else
    echo -e "${RED}❌ Error al desplegar infraestructura${NC}"
    exit 1
fi