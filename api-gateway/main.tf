data "aws_caller_identity" "current" {}
data "aws_region" "current" {}

locals {
  account_id = data.aws_caller_identity.current.account_id
  region     = data.aws_region.current.name
}

resource "aws_apigatewayv2_api" "messages" {
  name          = "Messages API (${var.environment})"
  description   = "Sends messages to a recipient."
  protocol_type = "HTTP"

  cors_configuration {
    allow_methods = [
      "POST",
      "OPTIONS"
    ]
    allow_headers = [
      "authorization",
      "content-type",
      "x-api-key",
      "x-amz-date",
      "x-amz-security-token",
    ]
    allow_origins = [
      "*"
    ]
  }
}

resource "aws_apigatewayv2_stage" "messages_api_stage" {
  api_id      = aws_apigatewayv2_api.messages.id
  name        = var.environment
  auto_deploy = true
}

resource "aws_apigatewayv2_integration" "send_message" {
  api_id                 = aws_apigatewayv2_api.messages.id
  description            = "Integration with the 'sendEmail' lambda"
  integration_type       = "AWS_PROXY"
  integration_method     = "POST"
  integration_uri        = var.send_email_lambda_arn
  payload_format_version = "2.0"
}

resource "aws_apigatewayv2_route" "send_message" {
  api_id    = aws_apigatewayv2_api.messages.id
  route_key = "POST /messages"
  target    = "integrations/${aws_apigatewayv2_integration.send_message.id}"
}

resource "aws_lambda_permission" "messages" {
  statement_id  = "AllowExecutionFromAPIGateway"
  action        = "lambda:InvokeFunction"
  function_name = var.send_email_lambda_name
  principal     = "apigateway.amazonaws.com"
  source_arn    = "arn:aws:execute-api:${local.region}:${local.account_id}:${aws_apigatewayv2_api.messages.id}/*/*/messages"
}
