output "vpc" {
  value = {
    id = aws_vpc.main.id
    cidr_block=aws_vpc.main.cidr_block
  }
}

output "subnets" {
  value = {
    public={
      for k,v in aws_subnet.public: k=>{
        id=v.id
        cidr_block=v.cidr_block
      }
    }
  }
}

output "route_tables" {
  value = {
    public = {
      id = aws_route_table.public.id
    }
  }
}

output "security_groups" {
  value = {
    main={
      id=aws_security_group.main.id
    }
  }
}
