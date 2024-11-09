package net.andersentech.gcs_test;

// Google Cloud Dependencies 
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersion;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.google.gson.JsonObject;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.io.IOException;

// AWS S3 Dependencies 
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import java.util.List;

public class GCSTest {

    public static void listBucketObjects(String projectId, String secretId, String versionId, String bucketName) throws IOException {

        try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {

            // Build the name from the version.
            SecretVersionName secretVersionName = SecretVersionName.of(projectId, secretId, versionId);

            // Print information about the version of the secret
            SecretVersion version = client.getSecretVersion(secretVersionName);
            System.out.printf("Secret version %s, state %s.\n", version.getName(), version.getState());

            // Get the specified version of the secret
            AccessSecretVersionResponse response = client.accessSecretVersion(version.getName());

            // Convert the response to a jsonElement
            JsonElement secretJsonElement = JsonParser.parseString(response.getPayload().getData().toStringUtf8());

            // Create a jsonObject from the jsonElement, allowing access to the fields inside the secret
            JsonObject secretJsonObject = secretJsonElement.getAsJsonObject();

            // Print out the full contents of the HMAC secret
            System.out.printf("Contents of secret: [%s, %s]\n", secretJsonObject.get("access_key").getAsString(),
                secretJsonObject.get("secret").getAsString());

            // Setup the HMAC credentials for use with the S3 client
            BasicAWSCredentials gcsCreds  = new BasicAWSCredentials(secretJsonObject.get("access_key").getAsString(),
                secretJsonObject.get("secret").getAsString());

            // Configure the S3 client to use the credentials and proper API endpoint
            AmazonS3 s3 = AmazonS3ClientBuilder.standard().withEndpointConfiguration(
                new AwsClientBuilder.EndpointConfiguration("https://storage.googleapis.com", "auto"))
                .withCredentials(new AWSStaticCredentialsProvider(gcsCreds))
                .build();

            // List the objects in the bucket
            ListObjectsV2Result result = s3.listObjectsV2(bucketName);

            // Convert the result to a list
            List<S3ObjectSummary> objects = result.getObjectSummaries();

            for (S3ObjectSummary os : objects) {

                System.out.printf("* Object Name: %s\n", os.getKey());
            }
        }
    }

    public static void main(String[] args) {

        // Define the variables we need to connect to Secret Manager and test GCS connectivity
        String projectId = "PROJECT ID HERE";
        String secretId = "SECRET NAME HERE";
        String versionId = "SECRET VERSION HERE";
        String bucketName = "GCS BUCKET NAME HERE";

        try {

            listBucketObjects(projectId, secretId, versionId, bucketName);
        } catch (Exception e) {

            // Print out any exception we may run into
            System.out.printf(e.toString() + "\n");
        }
    }
}