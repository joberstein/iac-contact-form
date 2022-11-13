terraform {
  backend "s3" {
    bucket               = "iac-contact-form-state"
    workspace_key_prefix = "env"
    key                  = "terraform.tfstate"
    region               = "us-east-1"
  }
}