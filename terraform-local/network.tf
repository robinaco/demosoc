# Red principal (simula VPC)
resource "docker_network" "main" {
  name    = local.network_name
  driver  = "bridge"
  
  ipam_config {
    subnet  = "172.30.0.0/16"
    gateway = "172.30.0.1"
  }
  
  labels {
    label = "com.terraform.env"
    value = var.environment
  }
  labels {
    label = "com.simulation.aws.vpc"
    value = "vpc-${var.project_name}"
  }
}

# Subredes - DESACTIVADAS TEMPORALMENTE
# resource "docker_network" "subnet" {
#   count = var.subnet_count
#   ...
# }
