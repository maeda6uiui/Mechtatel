variable "env" {
  type        = string
  description = "Environment name (e.g. sandbox)"
}

variable "vpc_id" {
  type        = string
  description = "ID of the VPC"
}

variable "subnets" {
  type = object({
    public = map(object({
      id = string
    }))
  })
  description = "Subnets"
}

variable "security_group_id" {
  type        = string
  description = "Security group ID for the EC2 instance"
}

variable "private_ips" {
  type        = map(list(string))
  description = "Private IP addresses for the EC2 instance"
}

variable "key_pair" {
  type = object({
    key_name   = string
    public_key = string
  })
  description = "Information of the key pair for SSH connection"
}

variable "instance_config" {
  type = object({
    ami           = string
    instance_type = string
    volume_size   = number
    subnet_key    = string
  })
  description = "Configuration values for the EC2 instance"
}
