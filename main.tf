module "iam" {
  source      = "./iam"
  environment = var.environment
}

module "ses" {
  source              = "./ses"
  source_email_domain = var.source_email_domain
  destination_email   = var.destination_email
  environment         = var.environment
}

module "lambda" {
  source              = "./lambda"
  send_email_role_arn = module.iam.send_email_iam_role.arn
  google_captcha_key  = var.google_captcha_key
  environment         = var.environment
  source_email        = "${var.source_email_username}@${var.source_email_domain}"
  destination_email   = var.destination_email
}

module "api_gateway" {
  source                 = "./api-gateway"
  environment            = var.environment
  send_email_lambda_arn  = module.lambda.send_email.arn
  send_email_lambda_name = module.lambda.send_email.function_name
}
