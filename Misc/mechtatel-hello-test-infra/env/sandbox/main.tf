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
      "1a" = {
        cidr_block        = "172.17.0.0/24"
        availability_zone = "ap-northeast-1a"
      }
      "1c"={
        cidr_block="172.17.1.0/24"
        availability_zone="ap-northeast-1c"
      }
      "1d"={
        cidr_block="172.17.2.0/24"
        availability_zone="ap-northeast-1d"
      }
    }
  }
  security_group = {
    allow_ssh = {
      ipv4_cidrs = [var.ssh_cidr]
    }
  }
  route_table = module.network.route_table
  private_ips = {
    "1a"=["172.17.0.10"]
    "1c"=["172.17.1.10"]
    "1d"=["172.17.2.10"]
  }
  key_pair = {
    key_name   = "mechtatel-hello-test"
    public_key = file("mechtatel-hello-test.pub")
  }
  instance_config = {
    ami           = data.aws_ami.al2023_arm64.id
    instance_type = "g5g.xlarge"
    volume_size   = 32
    subnet_key = "1c"
  }
}
