variable "env" {
  type = string
}

variable "vpc" {
  type = object({
    id = string
  })
}

variable "subnet" {
  type = object({
    public = map(object({
      cidr_block        = string
      availability_zone = string
    }))
  })
}

variable "security_group" {
  type = object({
    allow_ssh = object({
      ipv4_cidrs = list(string)
    })
  })
}

variable "route_table" {
  type = object({
    public = object({
      id = string
    })
  })
}

variable "private_ips" {
  type = map(list(string))
}

variable "key_pair" {
  type = object({
    key_name   = string
    public_key = string
  })
}

variable "instance_config" {
  type = object({
    ami           = string
    instance_type = string
    volume_size   = number
    subnet_key    = string
  })
}
