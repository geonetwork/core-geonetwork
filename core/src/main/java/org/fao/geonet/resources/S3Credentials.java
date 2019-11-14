package org.fao.geonet.resources;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.auth.DefaultAWSCredentialsProviderChain;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;

public class S3Credentials {
    private AmazonS3 client = null;
    private AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard();

    private String keyPrefix = System.getenv("AWS_S3_PREFIX");
    private String bucket = System.getenv("AWS_S3_BUCKET");
    private String accessKey = null;
    private String secretKey = null;
    private String region = System.getenv("AWS_DEFAULT_REGION");
    private String endpoint = System.getenv("AWS_S3_ENDPOINT");

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public void setKeyPrefix(String keyPrefix) {
        if (keyPrefix.endsWith("/")) {
            this.keyPrefix = keyPrefix;
        } else {
            this.keyPrefix = keyPrefix + "/";
        }
    }

    public void setBucket(String bucket) {
        this.bucket = bucket;
    }

    @PostConstruct
    public void init() {
        if (accessKey != null && secretKey != null) {
            builder.withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKey, secretKey)));
            accessKey = null;
            secretKey = null;
        } else {
            builder.withCredentials(DefaultAWSCredentialsProviderChain.getInstance());
        }
        if (region != null) {
            if (endpoint != null) {
                builder.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, region));
            } else {
                builder.withRegion(region);
            }
        }
        client = builder.build();
        builder = null;
        if (bucket == null) {
            throw new RuntimeException("Missing the bucket configuration");
        }
    }

    @Nonnull
    public AmazonS3 getClient() {
        return client;
    }

    @Nonnull
    public String getBucket() {
        return bucket;
    }

    public String getKeyPrefix() {
        return keyPrefix;
    }
}
