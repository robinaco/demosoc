resource "docker_container" "jenkins_task" {
  name  = local.jenkins_container
  image = docker_image.jenkins.name
  user  = "root"
  
  ports {
    internal = 8080
    external = 8080
    protocol = "tcp"
  }
  
  ports {
    internal = 50000
    external = 50000
    protocol = "tcp"
  }
  
  volumes {
    volume_name    = docker_volume.jenkins_home.name
    container_path = "/var/jenkins_home"
  }
  
  volumes {
    host_path      = var.docker_socket_path
    container_path = "/var/run/docker.sock"
  }
  
  env = [
    "DOCKER_HOST=unix:///var/run/docker.sock",
    "JENKINS_OPTS=--prefix=/jenkins",
  ]
  
  networks_advanced {
    name    = docker_network.main.name
    aliases = ["jenkins-service"]
  }
  
  restart = "unless-stopped"
  
  depends_on = [docker_image.jenkins]
}
