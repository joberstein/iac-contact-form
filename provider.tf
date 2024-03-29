terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 4.16"
    }
  }

  required_version = ">= 1.2.0"
}

provider "aws" {
  default_tags {
    tags = {
      source      = "Terraform"
      repo        = "iac-contact-form"
      owner       = var.owner
      environment = var.environment
      managed     = true
    }
  }
}
