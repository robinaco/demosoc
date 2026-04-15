 #Define valores de salida que Terraform muestra después de ejecutar
# - cluster_name: nombre del cluster creado
# - service_name: nombre del servicio
# - app_url: URL o instrucciones para acceder a la aplicación
# Estos outputs facilitan la verificación de que los recursos se han creado correctamente y proporcionan información útil para acceder a la aplicación desplegada en ECS.


output "cluster_name" {
  description = "Nombre del cluster ECS"
  value       = aws_ecs_cluster.main.name
}

output "service_name" {
  description = "Nombre del servicio ECS"
  value       = aws_ecs_service.app.name
}

output "task_definition_arn" {
  description = "ARN de la task definition"
  value       = aws_ecs_task_definition.app.arn
}

output "security_group_id" {
  description = "ID del Security Group"
  value       = aws_security_group.app.id
}

output "cloudwatch_log_group" {
  description = "Grupo de logs de CloudWatch"
  value       = aws_cloudwatch_log_group.ecs.name
}

output "app_url" {
  description = "URL de la aplicación"
  value = local.is_production ?
    "https://console.aws.amazon.com/ecs/home?region=${var.aws_region}#/clusters/${aws_ecs_cluster.main.name}/services/${aws_ecs_service.app.name}/details" :
    "ECS Service: ${aws_ecs_service.app.name} - Check AWS Console for IP"
}

output "environment_info" {
  description = "Información del ambiente"
  value = {
    environment   = var.environment
    is_production = local.is_production
    is_pr         = local.is_pr
    cpu           = local.task_config.cpu
    memory        = local.task_config.memory
    desired_count = var.desired_count != null ? var.desired_count : local.task_config.desired_count
  }
}
