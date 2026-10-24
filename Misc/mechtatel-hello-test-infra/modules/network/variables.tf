variable "vpc_cidr_block" {
  type = string
}

variable "subnets" {
  type = object({
    public = map(object({
      cidr_block        = string
      availability_zone = string
    }))
  })
  validation {
    condition = contains(keys(var.subnets.public),"main")
    error_message = "Definition of the `main` subnet must be provided."
  }
}

variable "allow_ssh_cidr_blocks" {
  type = list(string)
  default = []
}
