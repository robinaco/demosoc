variable "app_image" {
  description = "Imagen Docker de la aplicación"
  type        = string
}

variable "environment" {
  description = "Ambiente (pr-XXX, staging, production)"
  type        = string
}

variable "pr_number" {
  description = "Número del Pull Request"
  type        = string
}

variable "desired_count" {
  description = "Número de instancias"
  type        = number
  default     = 1
}

variable "aws_region" {
  description = "Región de AWS"
  type        = string
  default     = "us-east-1"
}