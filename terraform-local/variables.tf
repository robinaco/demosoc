variable "project_name" {
  description = "Nombre del proyecto"
  type        = string
  default     = "mi-crud-app"
}

variable "environment" {
  description = "Ambiente de despliegue"
  type        = string
  default     = "local"
}

variable "jenkins_image_name" {
  description = "Nombre de la imagen de Jenkins"
  type        = string
  default     = "jenkins-local"
}

variable "app_image_name" {
  description = "Nombre de la imagen de la aplicación"
  type        = string
  default     = "mi-crud-app"
}

variable "docker_socket_path" {
  description = "Ruta del socket Docker"
  type        = string
  default     = "/var/run/docker.sock"
}

variable "subnet_count" {
  description = "Número de subredes a crear"
  type        = number
  default     = 2
}# Comentario
