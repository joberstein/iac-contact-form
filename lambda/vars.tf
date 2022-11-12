variable "send_email_role_arn" {
    type = string
    description = "The arn of the IAM role that allows for sending emails through SES."
}

variable "google_captcha_key" {
  type = string
  description = "The value to set for the google captcha api key."
}

variable environment {
    type = string
    description = "The environment to deploy infrastructure to."
}