resource "docker_container" "app_task" {
  name  = local.app_container
  image = docker_image.app.name
  
  ports {
    internal = 8080
    external = 8083
    protocol = "tcp"
  }
  
  env = [
    "SPRING_PROFILES_ACTIVE=${var.environment}",
  ]
  
  networks_advanced {
    name    = docker_network.main.name
    aliases = ["app-service"]
  }
  
  restart = "unless-stopped"
  
  depends_on = [docker_image.app]
}
