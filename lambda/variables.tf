variable "send_email_role_arn" {
    type = string
    description = "The arn of the IAM role that allows for sending emails through SES."
}

variable "google_captcha_key" {
  type = string
  description = "The value to set for the google captcha api key."
  sensitive = true
}

variable environment {
    type = string
    description = "The environment to deploy infrastructure to."
}

variable "destination_email" {
    type = string
    description = "The email address that SES will send emails to."
}

variable "source_email" {
    type = string
    description = "The email address that SES will send emails from."
}