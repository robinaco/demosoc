# Obtiene información de la infraestructura existente en AWS
# - VPC por defecto: red virtual donde se desplegarán los recursos
# - Subnets: subredes dentro de la VPC
# - Región y Account ID: información de la cuenta AWS actual
# Estos datos son necesarios para configurar correctamente los recursos de ECS y otros servicios relacionados.


# Obtener VPC por defecto
data "aws_vpc" "default" {
  default = true
}

# Obtener subnets por defecto
data "aws_subnets" "default" {
  filter {
    name   = "vpc-id"
    values = [data.aws_vpc.default.id]
  }
}

# Obtener información de la región actual
data "aws_region" "current" {}

# Obtener account ID
data "aws_caller_identity" "current" {}
