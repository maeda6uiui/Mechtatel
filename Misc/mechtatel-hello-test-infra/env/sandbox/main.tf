locals {
  env = "sandbox"

  vpc_cidr_block= "172.17.0.0/20"
  subnet = {
    public = {
      "1a" = {
        cidr_block        = cidrsubnet(local.vpc_cidr_block,4,0)
        availability_zone = "ap-northeast-1a"
      }
      "1c"={
        cidr_block=cidrsubnet(local.vpc_cidr_block,4,1)
        availability_zone="ap-northeast-1c"
      }
      "1d"={
        cidr_block=cidrsubnet(local.vpc_cidr_block,4,2)
        availability_zone="ap-northeast-1d"
      }
    }
  }
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

  cidr_block = local.vpc_cidr_block
}

module "instance" {
  source = "../../modules/instance"

  env = local.env

  vpc = module.network.vpc
  subnet = local.subnet
  security_group = {
    allow_ssh = {
      ipv4_cidrs = [var.ssh_cidr]
    }
  }
  route_table = module.network.route_table
  private_ips = {
    for k,v in local.subnet.public: k=>[
      cidrhost(v.cidr_block,10)
    ]
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
