package com.cruvs.backend.service;

import io.minio.*;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class StorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucketName;

    @PostConstruct
    public void initBucket() {
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("Initialized MinIO bucket: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("Failed to initialize MinIO bucket", e);
        }
    }

    public void uploadFile(String key, byte[] content, String contentType) {
        try (InputStream is = new ByteArrayInputStream(content)) {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(key)
                            .stream(is, content.length, -1)
                            .contentType(contentType)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error uploading to MinIO", e);
            throw new RuntimeException("Failed to upload file to storage", e);
        }
    }

    public byte[] downloadFile(String key) {
        try (InputStream is = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(key)
                        .build()
        )) {
            return is.readAllBytes();
        } catch (Exception e) {
            log.error("Error downloading from MinIO", e);
            throw new RuntimeException("Failed to download file from storage", e);
        }
    }

    public void deleteFile(String key) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(key)
                            .build()
            );
        } catch (Exception e) {
            log.error("Error deleting from MinIO", e);
            throw new RuntimeException("Failed to delete file from storage", e);
        }
    }
}
