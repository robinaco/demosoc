# Define valores locales y lógica condicional reutilizable
# - common_tags: etiquetas que se aplicarán a todos los recursos
# - task_config: configuración diferente según el ambiente (producción vs PR)
# Estas variables locales permiten mantener el código limpio y evitar duplicación, facilitando la gestión de configuraciones específicas para cada entorno.


locals {
  common_tags = {
    Environment = var.environment
    ManagedBy   = "Terraform"
    Project     = "demosoc"
    PrNumber    = var.pr_number != "" ? var.pr_number : "none"
  }

  is_production = var.environment == "production"
  is_pr         = can(regex("^pr-", var.environment))

  # Configuración según ambiente
  task_config = local.is_production ? {
    cpu           = 512
    memory        = 1024
    desired_count = 2
  } : {
    cpu           = 256
    memory        = 512
    desired_count = 1
  }
}
