package com.ironinstruction.api.utils;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import com.ironinstruction.api.security.SecurityConstants;

import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

public class AwsS3Manager {
    private S3Presigner presigner; 
    private S3Client client;
    private String bucketName;

    public AwsS3Manager(String bucketName) {
        this.presigner = S3Presigner.create();
        this.client = S3Client.builder().build();
        this.bucketName = bucketName;
    }

    public void setBucketName(String bucketName) {
        this.bucketName = bucketName;
    }

    public void deleteObject(String fileName) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
            .bucket(this.bucketName)
            .key(fileName)
            .build();

        client.deleteObject(deleteObjectRequest);
    }

    public String newPresignedGetUrl(String fileName) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
            .bucket(this.bucketName)
            .key(fileName)
            .build();

        GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(SecurityConstants.URL_EXPIRATION_TIME_MINUTES))
            .getObjectRequest(getObjectRequest)
            .build();

        return presigner.presignGetObject(getObjectPresignRequest).url().toString();
    }

    public String newPresignedPutUrl(String fileName) {
        PutObjectRequest objectRequest = PutObjectRequest.builder()
            .bucket(this.bucketName)
            .key(fileName)
            .contentType("video/mp4")
            .build();

       PutObjectPresignRequest presignRequest = PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(SecurityConstants.URL_EXPIRATION_TIME_MINUTES))
            .putObjectRequest(objectRequest)
            .build();

        return this.presigner.presignPutObject(presignRequest).url().toString();
    }
}
