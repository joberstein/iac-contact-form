variable "destination_email" {
  type        = string
  description = "The email address that SES will send emails to."
}

variable "environment" {
  type        = string
  description = "The environment to deploy infrastructure to."

  validation {
    condition     = contains(["dev", "prod"], var.environment)
    error_message = "ERROR: Valid types are \"dev\" and \"prod\""
  }
}

variable "google_captcha_key" {
  type      = string
  sensitive = true
}

variable "source_email_domain" {
  type        = string
  description = "The domain of the email address that SES will send emails from."
}

variable "source_email_username" {
  type        = string
  description = "The username of the email address that SES will send emails from."
}
