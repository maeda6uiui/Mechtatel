resource "aws_subnet" "public" {
  for_each = var.subnet.public

  vpc_id            = var.vpc.id
  availability_zone = each.value.availability_zone
  cidr_block        = each.value.cidr_block
}

resource "aws_route_table_association" "public" {
  for_each = var.subnet.public

  subnet_id      = aws_subnet.public[each.key].id
  route_table_id = var.route_table.public.id
}

resource "aws_security_group" "public" {
  vpc_id = var.vpc.id
}

resource "aws_vpc_security_group_ingress_rule" "allow_ingress_traffic_ssh_ipv4" {
  for_each = toset(var.security_group.allow_ssh.ipv4_cidrs)

  security_group_id = aws_security_group.public.id
  cidr_ipv4         = each.value
  from_port         = 22
  ip_protocol       = "tcp"
  to_port           = 22
}

resource "aws_vpc_security_group_egress_rule" "allow_all_egress_traffic_ipv4" {
  security_group_id = aws_security_group.public.id
  cidr_ipv4         = "0.0.0.0/0"
  ip_protocol       = "-1"
}

resource "aws_network_interface" "main" {
  subnet_id   = aws_subnet.public["main"].id
  private_ips = var.private_ips
}

resource "aws_key_pair" "main" {
  key_name   = var.key_pair.key_name
  public_key = var.key_pair.public_key
}

resource "aws_instance" "main" {
  ami           = var.instance_config.ami
  instance_type = var.instance_config.instance_type

  primary_network_interface {
    network_interface_id = aws_network_interface.main.id
  }

  root_block_device {
    volume_size = var.instance_config.volume_size
  }

  key_name = aws_key_pair.main.key_name

  instance_market_options {
    market_type = "spot"
    spot_options {
      spot_instance_type             = "one-time"
      instance_interruption_behavior = "terminate"
    }
  }

  user_data = file("user_data.sh")

  tags = {
    Name = "mechtatel-hello-test-${var.env}"
  }
}

resource "aws_network_interface_sg_attachment" "public" {
  security_group_id    = aws_security_group.public.id
  network_interface_id = aws_instance.main.primary_network_interface_id
}

resource "aws_eip" "main" {
  instance = aws_instance.main.id
  domain   = "vpc"
}
