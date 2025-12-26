locals {
  env = "sandbox"
}

data "aws_ami" "al2023_arm64" {
  most_recent = true
  owners      = ["amazon"]

  filter {
    name   = "name"
    values = ["al2023-ami-*-kernel-6.*-arm64"]
  }
  filter {
    name   = "architecture"
    values = ["arm64"]
  }
  filter {
    name   = "virtualization-type"
    values = ["hvm"]
  }
}

module "network" {
  source = "../../modules/network"

  cidr_block = "172.17.0.0/20"
}

module "instance" {
  source = "../../modules/instance"

  env = local.env

  vpc = module.network.vpc
  subnet = {
    public = {
      "main" = {
        cidr_block        = "172.17.0.0/24"
        availability_zone = "ap-northeast-1a"
      }
    }
  }
  security_group = {
    allow_ssh = {
      ipv4_cidrs = [var.ssh_cidr]
    }
  }
  route_table = module.network.route_table
  private_ips = ["172.17.0.10"]
  key_pair = {
    key_name   = "mechtatel-hello-test"
    public_key = file("mechtatel-hello-test.pub")
  }
  instance_config = {
    ami           = data.aws_ami.al2023_arm64.id
    instance_type = "g5g.xlarge"
    volume_size   = 24
  }
}
