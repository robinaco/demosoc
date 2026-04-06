#!/bin/bash

echo "🛑 Destruyendo infraestructura ECS local..."
echo "=========================================="

cd terraform-local

terraform destroy -auto-approve

echo "✅ Infraestructura destruida"
echo ""
echo "Para limpiar volúmenes: docker volume prune"
echo "Para limpiar logs: rm -rf logs/"