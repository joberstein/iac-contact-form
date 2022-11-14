output "api_gateway_endpoint" {
    value = module.api_gateway.messages_api.api_endpoint
}

output "send_email_version" {
    value = module.lambda.send_email.version
}