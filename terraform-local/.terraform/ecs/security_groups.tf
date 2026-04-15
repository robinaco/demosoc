# Define reglas de firewall (Security Groups)
# - app: grupo de seguridad que controla el tráfico hacia/desde la aplicación
# - Reglas de ingreso: qué tráfico entra (ej. puerto 8080 desde cualquier IP)
# - Reglas de egreso: qué tráfico sale (todo permitido)


# Security Group para la aplicación
resource "aws_security_group" "app" {
  name        = "sg-${var.environment}"
  description = "Security group for ${var.environment}"
  vpc_id      = data.aws_vpc.default.id

  tags = merge(local.common_tags, {
    Name = "sg-${var.environment}"
  })
}

# Reglas de ingreso
resource "aws_security_group_rule" "app_ingress_http" {
  type              = "ingress"
  from_port         = var.container_port
  to_port           = var.container_port
  protocol          = "tcp"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.app.id
  description       = "HTTP from anywhere"
}

# Reglas de egreso
resource "aws_security_group_rule" "app_egress_all" {
  type              = "egress"
  from_port         = 0
  to_port           = 0
  protocol          = "-1"
  cidr_blocks       = ["0.0.0.0/0"]
  security_group_id = aws_security_group.app.id
  description       = "Allow all outbound"
}
