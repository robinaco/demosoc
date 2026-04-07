# Define las versiones mínimas requeridas de Terraform y sus proveedores
# - Terraform: herramienta de Infrastructure as Code
# - AWS Provider: plugin que permite a Terraform comunicarse con AWS

# Define la versión mínima de Terraform requerida (1.0 o superior)
# Define el proveedor de AWS con su fuente y versión compatible (5.0 o superior)

terraform {
  required_version = ">= 1.0"
  
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
  }
}