variable "environment" {
  type        = string
  description = "The environment to deploy infrastructure to."
}

variable "send_email_lambda_arn" {
  type        = string
  description = "The arn of the sendEmail lambda function."
}

variable "send_email_lambda_name" {
  type        = string
  description = "The name of the sendEmail lambda function."
}