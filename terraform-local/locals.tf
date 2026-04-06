locals {
  project_name   = var.project_name
  environment    = var.environment
  jenkins_image  = var.jenkins_image_name
  app_image      = var.app_image_name
  
  # Nombres completos de recursos
  network_name      = "${var.project_name}-network-${var.environment}"
  jenkins_container = "${var.project_name}-jenkins-${var.environment}"
  app_container     = "${var.project_name}-app-${var.environment}"
  
  # Tags comunes para todos los recursos
  common_tags = {
    "com.terraform.env" = var.environment
    "com.project"       = var.project_name
    "managed-by"        = "terraform"
  }
}
