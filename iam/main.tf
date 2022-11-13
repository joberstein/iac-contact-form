locals {
  artifact_name = "sendEmail"
}

data "aws_iam_policy_document" "service_lambda" {
  statement {
    actions = [
        "sts:AssumeRole"
    ]
    
    principals {
      type = "Service"
      identifiers = [
        "lambda.amazonaws.com"
      ]
    }
  }
}

data "aws_iam_policy" "lambda_basic_execution" {
  path_prefix = "/service-role/"
  name        = "AWSLambdaBasicExecutionRole"
}

data "aws_iam_policy" "ses_full_access" {
  name = "AmazonSESFullAccess"
}

resource "aws_iam_role" "send_email_role" {
  name               = "${local.artifact_name}Role-${var.environment}"
  description        = "Allows the '${local.artifact_name}' lambda to execute and utilize Amazon SES."
  assume_role_policy = data.aws_iam_policy_document.service_lambda.json
  managed_policy_arns = [
    data.aws_iam_policy.lambda_basic_execution.arn,
    data.aws_iam_policy.ses_full_access.arn,
  ]
}
