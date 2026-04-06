# Construir imagen de Jenkins localmente
resource "docker_image" "jenkins" {
  name = "${var.jenkins_image_name}:latest"
  
  build {
    context    = "../"
    dockerfile = "../Dockerfile.jenkins"
    tag        = ["${var.jenkins_image_name}:latest"]
  }
  
  triggers = {
    always_rebuild = timestamp()
  }
}

# Construir imagen de tu aplicación (Gradle)
resource "docker_image" "app" {
  name = "${var.app_image_name}:latest"
  
  build {
    context    = "../"
    dockerfile = "../Dockerfile"
    tag        = ["${var.app_image_name}:latest"]
  }
  
  triggers = {
    always_rebuild = timestamp()
  }
}
