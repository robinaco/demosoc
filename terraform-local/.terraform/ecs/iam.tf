# Define roles y políticas de seguridad (IAM)
# - ecs_execution: rol que permite a ECS descargar la imagen y configurar logs
# - ecs_task: rol que permite a la aplicación acceder a otros servicios AWS
# Se adjuntan políticas predefinidas de AWS para cada rol, asegurando que tengan los permisos necesarios para funcionar correctamente sin otorgar permisos excesivos.


# IAM Role para ECS Execution
resource "aws_iam_role" "ecs_execution" {
  name = "ecs-execution-${var.environment}"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = {
        Service = "ecs-tasks.amazonaws.com"
      }
    }]
  })

  tags = local.common_tags
}

# IAM Role para ECS Task
resource "aws_iam_role" "ecs_task" {
  name = "ecs-task-${var.environment}"

  assume_role_policy = jsonencode({
    Version = "2012-10-17"
    Statement = [{
      Action = "sts:AssumeRole"
      Effect = "Allow"
      Principal = {
        Service = "ecs-tasks.amazonaws.com"
      }
    }]
  })

  tags = local.common_tags
}

# Attach policies
resource "aws_iam_role_policy_attachment" "ecs_execution_policy" {
  role       = aws_iam_role.ecs_execution.name
  policy_arn = "arn:aws:iam::aws:policy/service-role/AmazonECSTaskExecutionRolePolicy"
}

# Policy para logs de CloudWatch
resource "aws_iam_role_policy_attachment" "ecs_task_logs" {
  role       = aws_iam_role.ecs_task.name
  policy_arn = "arn:aws:iam::aws:policy/CloudWatchLogsFullAccess"
}