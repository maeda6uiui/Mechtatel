terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~>6.0"
    }
  }

  backend "s3" {
    bucket = "maeda6uiui-sandbox-infra"
    region = "ap-northeast-1"
    key    = "mechtatel-hello-test.tfstate"
  }

  required_version = "~>1.9"
}

provider "aws" {
  region = "ap-northeast-1"
}
