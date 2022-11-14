locals {
  artifact_name = "sendEmail"
  filename = join("/", [
    path.module,
    local.artifact_name,
    "target",
    "${local.artifact_name}-jar-with-dependencies.jar"
  ])
}

resource "aws_lambda_function" "send_email_lambda" {
  function_name    = "${local.artifact_name}-${var.environment}"
  filename         = local.filename
  source_code_hash = filebase64sha256(local.filename)
  description      = "Sends an email out with SimpleEmailService."
  runtime          = "java11"
  handler          = "joberstein.portfolio.SendEmailHandler"
  role             = var.send_email_role_arn
  timeout          = 15
  memory_size      = 512
  publish          = true

  environment {
    variables = {
      GOOGLE_CAPTCHA_KEY = var.google_captcha_key
      SOURCE_EMAIL       = var.source_email
      DESTINATION_EMAIL  = var.destination_email
    }
  }
}
