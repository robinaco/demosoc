output "jenkins_url" {
  description = "URL de Jenkins"
  value       = "http://localhost:8080"
}

output "app_url" {
  description = "URL de tu aplicación CRUD"
  value       = "http://localhost:8083/api/personas"
}

output "jenkins_container_id" {
  description = "ID del contenedor de Jenkins"
  value       = docker_container.jenkins_task.id
}

output "app_container_id" {
  description = "ID del contenedor de tu app"
  value       = docker_container.app_task.id
}

output "get_jenkins_password" {
  description = "Comando para obtener el password de Jenkins"
  value       = "docker exec ${docker_container.jenkins_task.name} cat /var/jenkins_home/secrets/initialAdminPassword"
}

output "network_info" {
  description = "Información de la red simulada"
  value = {
    name    = docker_network.main.name
    subnet  = "172.30.0.0/16"
    gateway = "172.30.0.1"
  }
}

output "volumes_info" {
  description = "Volúmenes creados"
  value = {
    jenkins_home = docker_volume.jenkins_home.name
  }
}
