provider "aws" {
  default_tags {
    tags = {
      source  = "Terraform"
      repo    = var.app_name
      owner   = var.owner
      managed = false
    }
  }
}