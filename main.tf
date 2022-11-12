module "iam" {
  source = "./iam"
  environment = var.environment
}

module "lambda" {
  source = "./lambda"
  send_email_role_arn = module.iam.send_email_iam_role.arn
  google_captcha_key = var.google_captcha_key
  environment = var.environment
}