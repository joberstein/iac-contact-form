variable "google_captcha_key" {
  type = string
  default = "1"
}

variable environment {
    type = string
    description = "The environment to deploy infrastructure to."
    default = "dev"

    validation {
      condition = contains(["dev", "prod"], var.environment)
      error_message = "ERROR: Valid types are \"dev\" and \"prod\""
    }
}