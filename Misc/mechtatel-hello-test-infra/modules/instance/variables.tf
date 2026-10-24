variable "env" {
  type = string
}

variable "vpc_id" {
  type = string
}

variable "subnets" {
  type = object({
    public = map(object({
      id = string
    }))
  })
}

variable "security_group_id" {
  type = string
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
