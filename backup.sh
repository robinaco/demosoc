#!/bin/bash
# backup.sh - Backup de configuración de Jenkins

set -e

BACKUP_DIR="./jenkins-backup/$(date +%Y%m%d_%H%M%S)"

echo "Creando backup en $BACKUP_DIR"
mkdir -p $BACKUP_DIR

# Backup de configuración de Jenkins
echo "Copiando configuración de Jenkins..."
docker cp jenkins-local:/var/jenkins_home $BACKUP_DIR/ 2>/dev/null || echo "No se pudo copiar"

# Backup de jobs
echo "Respaldando jobs..."
docker exec jenkins-local tar -czf /tmp/jobs-backup.tar.gz /var/jenkins_home/jobs 2>/dev/null || true
docker cp jenkins-local:/tmp/jobs-backup.tar.gz $BACKUP_DIR/ 2>/dev/null || true

echo "Backup completado: $BACKUP_DIR"

# Mantener solo últimos 5 backups
echo "Limpiando backups antiguos..."
ls -1d ./jenkins-backup/* 2>/dev/null | head -n -5 | xargs rm -rf 2>/dev/null || true

echo "Backups disponibles:"
ls -la ./jenkins-backup/
