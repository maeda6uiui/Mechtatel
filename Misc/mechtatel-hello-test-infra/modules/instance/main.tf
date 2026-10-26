resource "aws_network_interface" "main" {
  for_each = var.subnets.public

  subnet_id   = each.value.id
  private_ips = var.private_ips[each.key]
}

resource "aws_key_pair" "main" {
  key_name   = var.key_pair.key_name
  public_key = var.key_pair.public_key
}

resource "aws_instance" "main" {
  ami           = var.instance_config.ami
  instance_type = var.instance_config.instance_type

  primary_network_interface {
    network_interface_id = aws_network_interface.main[var.instance_config.subnet_key].id
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

  tags = {
    Name = "mechtatel-hello-test-${var.env}"
  }
}

resource "aws_network_interface_sg_attachment" "public" {
  security_group_id    = var.security_group_id
  network_interface_id = aws_instance.main.primary_network_interface_id
}

resource "aws_eip" "main" {
  instance = aws_instance.main.id
  domain   = "vpc"
}
