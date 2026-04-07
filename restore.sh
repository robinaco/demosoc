#!/bin/bash
# restore.sh - Restaurar configuración de Jenkins

if [ -z "$1" ]; then
    echo "Uso: ./restore.sh <directorio-backup>"
    echo "Ejemplo: ./restore.sh ./jenkins-backup/20241207_120000"
    echo ""
    echo "Backups disponibles:"
    ls -la ./jenkins-backup/ 2>/dev/null || echo "No hay backups"
    exit 1
fi

BACKUP_PATH="$1"

if [ ! -d "$BACKUP_PATH" ]; then
    echo "Error: $BACKUP_PATH no existe"
    exit 1
fi

echo "estaurando configuración desde $BACKUP_PATH..."

# Detener Jenkins
echo "Deteniendo Jenkins..."
docker-compose stop jenkins-local

# Restaurar backup
echo "Copiando archivos de backup..."
docker cp $BACKUP_PATH/jenkins_home/. jenkins-local:/var/jenkins_home/ 2>/dev/null || echo "No se pudo restaurar"

# Iniciar Jenkins
echo "Iniciando Jenkins..."
docker-compose start jenkins-local

echo "Restauración completada"
echo "Jenkins: http://localhost:8080"
