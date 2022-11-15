# iac-contact-form

Reusable infrastructure for implementing a contact form on a website.

This infrastructure is used by my [portfolio](https://github.com/joberstein/portfolio-v3).

This repo utilizes Terraform to deploy infrastructure to AWS.

## Components

- **HTTP API**: a public API that clients can call make requests to
- **IAM Role**: a role that allows for execution of the `sendEmail` lambda
- **Email Identities**: 
    - A domain that represents the sender email (you'll need to follow the instructions for verifying with Easy DKIM)
    - An email address for the recipient. You'll need to verify recipient email address once the infrastructure deploys. I use my own email address so that the contact form information forwards to me.
- **Lambda Function**: sends an email to a recipient using SES. It also verifies the captcha passed in from the API using Google's reCaptcha v3.

## Setup

All commands listed in this section should be run from the project root.

1. Install [maven](https://maven.apache.org/install.html).

2. Build the 'sendEmail' lambda (parenthesis prevent changing your current working directory):
```bash
(cd lambda/sendEmail && mvn clean install)
```

3. Install [aws](https://docs.aws.amazon.com/cli/latest/userguide/getting-started-install.html).

4. Configure aws and add your credentials per your AWS account:
```bash
aws configure
```

5. Create a set of [reCAPTCHA v3](https://developers.google.com/recaptcha/docs/v3) keys.

6. Install [terraform](https://developer.hashicorp.com/terraform/tutorials/aws-get-started/install-cli#install-terraform).

7. Adjust all variable values in the following files as necessary (or override them in the plan/apply commands later):
- terraform.tfvars
- dev.tfvars
- prod.tfvars
- backend/terraform.tfvars

8. Set up the remote (s3) backend:
```bash
(cd backend && terraform init)
```

9. Initialize the repo main infrastructure:
```bash
terraform init
```

10. If you'd like to utilize workspaces, you can use the following script to create (if not exists) and switch workspaces:
```bash
./scripts/switch-workspace.sh [dev|prod]
```

11. Test the infrastructure with a plan or apply:
```bash
# If using workspaces
terraform [plan|apply] -var-file=$(terraform workspace show).tfvars
```

## Terraform Contract

### Backend Infrastructure

**Inputs**

| variable | type | description | required | allowed_values |
| --- | --- | --- | --- | --- |
| app_name | string | The name of the app to create a Terraform state bucket and Dynamo DB table for | true | any |
| owner | string | The username/name of the person who owns the AWS resources | false | any |

**Outputs**

N/A

### Main Infrastructure

**Inputs**

| variable | type | description | required | allowed_values |
| --- | --- | --- | --- | --- |
| destination_email | string | The email address that SES will send emails to | true | any |
| environment | string | The environment to deploy infrastructure to | true | `dev`, `prod` |
| google_captcha_key | string | A Google reCaptcha v3 server key (private) that is valid for the `source_email_domain` | true | any |
| owner | string | The username/name of the person who owns the AWS resources | false | any |
| source_email_domain | string | The domain of the email address that SES will send emails from | true | any |
| source_email_username | string | The username of the email address that SES will send emails from | true | any |

**Outputs**

| variable | type | description |
| --- | --- | --- |
| api_gateway_endpoint | string | The base url (excludes the path) of the messages API. You'll want to append `/${var.environment}/messages` depending on the environment you've deployed to.
| send_email_version | string | The currently published version of the 'sendEmail' lambda function |

## Messages API Contract

### Methods

| method | path | request | response |
| --- | --- | --- | --- |
| `POST` | `/messages` | `ContactRequest` | `ContactResponse` |

### Models

**ContactRequest**

| attribute | type | description | required |
| --- | --- | --- | --- |
| body | string | The body of the email message to send | true |
| captcha | string | The client-side reCAPTCHA v3 value | true |
| from | string | The sender's name | false |
| replyToAddress | string | The sender's email address | true |
| subject | string | The subject of the email message to send | true |

**ContactResponse**
| attribute | type | description |
| --- | --- | --- |
| resultId | string | The SES message id if the send was successful |

| status | description |
| --- | --- |
| 200 | The email was sent successfully to the recipient |
| 400 | The request was malformed |
| 403 | The captcha verifcation failed |
| 500 | Could not send the email, or the SES message id is blank |

## Contributing

### Hooks

This project's hooks are managed by [pre-commit](https://pre-commit.com). To setup the hooks, complete the following steps:

1. Install [pre-commit](https://pre-commit.com/#install). 

2. Install the [hooks](https://git-scm.com/book/en/v2/Customizing-Git-Git-Hooks):
```bash
pre-commit install
```

Configured hooks:
- **giticket**: Appends `Closes #<number>` when a branch is formatted
  as `#\d+` (e.g. #2)
- **commitlint**: Runs the `commitlint` package against the commit
  message to enforce the conventional commit standard
- **semantic-release**: Runs the `semantic-release` package to
  perform a release of this repo

### Structure

Every non-private, top-level directory, execpt scripts, is host to Terraform configuration.

Each of these directories is structured as follows:
- main.tf
- variables.tf `*`
- outputs.tf `*`
- provider.tf `^`
- backend.tf `^ *`
- *.tfvars `^ *`

`^` = Only present in `/` or `/backend`

`*` = Optional

### Lambda

This module is slightly different from its siblings because it also contains a lambda function.

The function is a Maven project (Java), and is published on a release if the output JAR's contents has changed.

The lambda function utilizes reCAPTCHA v3 verification; if the server key doesn't correspond with the request parameter, the lambda will return a 403 forbidden error. 

I created two sets of captcha keys - one for prod, one for localhost. Each set has a client (public) and server (private) key. The client key is used with a website integration, while the server key should be passed to the Terraform as a variable.

### Backend

This repo makes use of Terraform's remote S3 backend. It also uses DynamoDB for state locking and consistency.

Creating these resources (S3 bucket and DynamoDB table) is usually a manual step because there is no remote backend without them.

In an effort to keep things reproducible, I have separated these resources into the '/backend' directory, which can be initialized and applied separately from the rest of the repo infrastructure. In fact, this backend infrastructure must be applied before the rest of the infrastructure can be initialized.

Although the backend resources are stored in a local backend, I've set them up to include a 'managed = false' tag to identify that they may need to be modified manually in the future.

## Validation

### Terraform
Travis runs infrastructure validation, but you can run it locally too:
```
terraform validate
```

### Commits
Travis runs commitlint to verify that all the commits on the current branch are valid.

Commits must conform to the conventional commit standard to trigger a deploy.

## Deploy

This repository uses [semantic-release](https://github.com/semantic-release/semantic-release) 
to publish a new version of the lambda function if necessary, and apply the Terraform infrastructure.

A deploy initiates with each merge to the master branch if semantic-release detects that a new version should be deployed. Semantic-release determines this by analyzing the commit history.

To perform a dry-run of semantic-release on the current branch, you can add an `args` option to the pre-commit hook, but remember to revert your changes:

```
  - repo: https://github.com/joberstein/precommit-semantic-release
      rev: v1.0.1
      hooks:
          - id: semantic-release
            additional_dependencies:
              - "@semantic-release/exec"
            stages:
              - manual
            args:
              - -d
              - -b <current branch if not 'master'>
```

Then, run the following commands in the terminal from the project root:

```
git add .pre-commit-config.yaml
export GITHUB_TOKEN=<gh-personal-access-token>
pre-commit run --hook-stage manual semantic-release -v
```