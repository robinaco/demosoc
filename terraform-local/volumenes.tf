resource "docker_volume" "jenkins_home" {
  name = "${var.project_name}-jenkins-home-${var.environment}"
  
  labels {
    label = "com.terraform.env"
    value = var.environment
  }
  labels {
    label = "com.simulation.aws.efs"
    value = "fs-${var.project_name}-jenkins"
  }
  labels {
    label = "com.simulation.aws.volume-type"
    value = "efs"
  }
}
