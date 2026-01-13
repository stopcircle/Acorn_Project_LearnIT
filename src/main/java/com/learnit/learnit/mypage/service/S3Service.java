package com.learnit.learnit.mypage.service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket:learnit-certificates-prod}")
    private String bucket;

    public void uploadFile(String path, String fileName, byte[] fileData) {
        String fullPath = path + fileName;
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(fileData.length);
        metadata.setContentType("application/pdf");

        try (InputStream inputStream = new ByteArrayInputStream(fileData)) {
            amazonS3.putObject(new PutObjectRequest(bucket, fullPath, inputStream, metadata));
            log.info("S3 Upload success: {}", fullPath);
        } catch (Exception e) {
            log.error("S3 Upload failed: {}", e.getMessage());
        }
    }
}
