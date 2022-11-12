resource "aws_sesv2_email_identity" "destination_email" {
  email_identity = var.destination_email
}

resource "aws_sesv2_email_identity_feedback_attributes" "destination_email" {
  email_identity           = aws_sesv2_email_identity.destination_email.email_identity
  email_forwarding_enabled = true
}

resource "aws_sesv2_email_identity" "source_email_domain" {
  email_identity = var.source_email_domain

  dkim_signing_attributes {
    next_signing_key_length = "RSA_1024_BIT"
  }
}

resource "aws_sesv2_email_identity_feedback_attributes" "source_email_domain" {
  email_identity           = aws_sesv2_email_identity.source_email_domain.email_identity
  email_forwarding_enabled = true
}
