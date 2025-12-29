output "ip_address" {
  value = aws_eip.main.public_ip
}
