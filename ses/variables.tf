variable "destination_email" {
  type        = string
  description = "The email address that SES will send emails to."
}

variable "source_email_domain" {
  type        = string
  description = "The domain of the email address that SES will send emails from."
}