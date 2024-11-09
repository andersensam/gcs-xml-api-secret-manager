# gcs-xml-api-secret-manager

Access the GCS S3-compatible XML API from Java, using Secret Manager

This repo provides a simple example on how one may go about accessing the GCS API from Java without hardcoding the HMAC credentials anywhere.

## Building

Use `mvn` to build the package, including the dependencies for both GCS and AWS S3

## Authentication

This sample uses either `GOOGLE_APPLICATION_CREDENTIALS` or the Service Account running on the underlying GCE/GKE deployment.

Ensure that your Service Account has the proper authentication scopes before attempting this.

The IAM guide for Secret Manager is [available here](https://cloud.google.com/secret-manager/docs/access-control)

When using the GCE Service Account, ensure that the proper OAuth scopes are available to the instance. [See this](https://cloud.google.com/secret-manager/docs/accessing-the-api#oauth-scopes).