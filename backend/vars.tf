variable "app_name" {
  type        = string
  description = "The name of the app to create a Terraform state bucket and Dynamo DB table for."
}

variable "owner" {
  type        = string
  description = "The username/name of the person who owns the AWS resources."
}
