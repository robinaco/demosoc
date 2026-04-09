#!/bin/bash
set -euo pipefail

echo "Getting current task definition..."

aws ecs describe-task-definition \
  --task-definition "${ECS_TASK_FAMILY}" \
  --region "${AWS_REGION}" \
  --query 'taskDefinition' \
  > current-task-def.json

echo "Updating image..."

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

echo "Registering new task definition..."

NEW_TASK_DEF_ARN=$(aws ecs register-task-definition \
  --cli-input-json file://new-task-def.json \
  --region "${AWS_REGION}" \
  --query 'taskDefinition.taskDefinitionArn' \
  --output text)

echo "Updating service..."

aws ecs update-service \
  --cluster "${ECS_CLUSTER}" \
  --service "${ECS_SERVICE}" \
  --task-definition "${NEW_TASK_DEF_ARN}" \
  --region "${AWS_REGION}" \
  --force-new-deployment

echo "Waiting for stability..."

aws ecs wait services-stable \
  --cluster "${ECS_CLUSTER}" \
  --services "${ECS_SERVICE}" \
  --region "${AWS_REGION}"