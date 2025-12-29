output "vpc" {
  value = {
    id = aws_vpc.main.id
  }
}

output "route_table" {
  value = {
    public = {
      id = aws_route_table.public.id
    }
  }
}
