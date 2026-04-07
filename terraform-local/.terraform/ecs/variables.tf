# Define las variables que recibe el módulo Terraform
# - app_image: URL de la imagen Docker a desplegar
# - environment: ambiente (pr-123, staging, production)
# - desired_count: número de contenedores a ejecutar
# - aws_region: región de AWS donde se desplegará la infraestructura
# - container_port: puerto expuesto por el contenedor
# - environment_vars: variables de entorno para la aplicación
# - use_localstack: si es true, se usará LocalStack para pruebas locales

variable "app_image" {
  description = "Imagen Docker de la aplicación"
  type        = string
}

variable "environment" {
  description = "Ambiente (pr-XXX, staging, production)"
  type        = string
  
  validation {
    condition     = can(regex("^(pr-|staging|production)", var.environment))
    error_message = "Environment debe ser pr-XXX, staging o production"
  }
}

variable "pr_number" {
  description = "Número del Pull Request"
  type        = string
  default     = ""
}

variable "desired_count" {
  description = "Número de instancias (sobrescribe configuración por defecto)"
  type        = number
  default     = null
}

variable "aws_region" {
  description = "Región de AWS"
  type        = string
  default     = "us-east-1"
}

variable "container_port" {
  description = "Puerto del contenedor"
  type        = number
  default     = 8080
}

variable "environment_vars" {
  description = "Variables de entorno para la aplicación"
  type = list(object({
    name  = string
    value = string
  }))
  default = []
}

variable "use_localstack" {
  description = "Si es true, usa LocalStack para pruebas locales"
  type        = bool
  default     = false
}