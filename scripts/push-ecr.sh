#!/bin/bash
set -e

echo "Login a ECR..."
aws ecr get-login-password --region ${AWS_REGION} | \
docker login --username AWS --password-stdin ${AWS_ACCOUNT_ID}.dkr.ecr.${AWS_REGION}.amazonaws.com

echo "Tagging imagen..."
docker tag ${IMAGE_NAME}:${IMAGE_TAG} ${DOCKER_IMAGE}

echo "Push imagen..."
docker push ${DOCKER_IMAGE}