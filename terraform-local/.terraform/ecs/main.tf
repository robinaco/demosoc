# Recursos principales de la infraestructura
# - CloudWatch Log Group: almacena los logs de la aplicación
# - ECS Cluster: grupo lógico de servicios ECS
# - ECS Task Definition: define cómo ejecutar el contenedor (CPU, memoria, etc.)
# - ECS Service: mantiene el número deseado de contenedores ejecutándose

# CloudWatch Log Group
resource "aws_cloudwatch_log_group" "ecs" {
  name = "/ecs/app-${var.environment}"
  
  retention_in_days = local.is_production ? 30 : 7

  tags = local.common_tags
}

# ECS Cluster
resource "aws_ecs_cluster" "main" {
  name = "cluster-${var.environment}"
  
  setting {
    name  = "containerInsights"
    value = local.is_production ? "enabled" : "disabled"
  }

  tags = local.common_tags
}

# ECS Task Definition
resource "aws_ecs_task_definition" "app" {
  family                   = "app-${var.environment}"
  network_mode             = "awsvpc"
  requires_compatibilities = ["FARGATE"]
  cpu                      = local.task_config.cpu
  memory                   = local.task_config.memory
  execution_role_arn       = aws_iam_role.ecs_execution.arn
  task_role_arn           = aws_iam_role.ecs_task.arn

  container_definitions = jsonencode([{
    name  = "app"
    image = var.app_image
    portMappings = [{
      containerPort = var.container_port
      hostPort      = var.container_port
      protocol      = "tcp"
    }]
    environment = var.environment_vars
    logConfiguration = {
      logDriver = "awslogs"
      options = {
        "awslogs-group"         = aws_cloudwatch_log_group.ecs.name
        "awslogs-region"        = var.aws_region
        "awslogs-stream-prefix" = "ecs"
      }
    }
  }])

  tags = local.common_tags
}

# ECS Service
resource "aws_ecs_service" "app" {
  name            = "service-${var.environment}"
  cluster         = aws_ecs_cluster.main.id
  task_definition = aws_ecs_task_definition.app.arn
  desired_count   = var.desired_count != null ? var.desired_count : local.task_config.desired_count
  launch_type     = "FARGATE"

  network_configuration {
    subnets          = data.aws_subnets.default.ids
    security_groups  = [aws_security_group.app.id]
    assign_public_ip = true
  }

  lifecycle {
    ignore_changes = [
      task_definition
    ]
  }

  depends_on = [
    aws_iam_role_policy_attachment.ecs_execution_policy
  ]

  tags = local.common_tags
}