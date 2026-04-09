#!/bin/bash
set -euo pipefail

echo "Obteniendo task definition actual..."

aws ecs describe-task-definition \
  --task-definition "${ECS_TASK_FAMILY}" \
  --region "${AWS_REGION}" \
  --query 'taskDefinition' \
  > current-task-def.json

echo "Actualizando imagen..."

jq --arg IMAGE "${DOCKER_IMAGE}" --arg CONTAINER "${ECS_CONTAINER_NAME}" '
{
  family: .family,
  taskRoleArn: .taskRoleArn,
  executionRoleArn: .executionRoleArn,
  networkMode: .networkMode,
  containerDefinitions: (
    .containerDefinitions
    | map(if .name == $CONTAINER then .image = $IMAGE else . end)
  ),
  volumes: .volumes,
  placementConstraints: .placementConstraints,
  requiresCompatibilities: .requiresCompatibilities,
  cpu: .cpu,
  memory: .memory,
  runtimePlatform: .runtimePlatform
}
' current-task-def.json > new-task-def.json

echo "Registrando nueva task definition..."

NEW_TASK_DEF_ARN=$(aws ecs register-task-definition \
  --cli-input-json file://new-task-def.json \
  --region "${AWS_REGION}" \
  --query 'taskDefinition.taskDefinitionArn' \
  --output text)

echo "Actualizando servicio..."

aws ecs update-service \
  --cluster "${ECS_CLUSTER}" \
  --service "${ECS_SERVICE}" \
  --task-definition "${NEW_TASK_DEF_ARN}" \
  --region "${AWS_REGION}" \
  --force-new-deployment

echo "Esperando estabilidad..."

aws ecs wait services-stable \
  --cluster "${ECS_CLUSTER}" \
  --services "${ECS_SERVICE}" \
  --region "${AWS_REGION}"