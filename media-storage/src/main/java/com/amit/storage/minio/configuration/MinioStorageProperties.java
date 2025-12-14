package com.amit.storage.minio.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(value = "storage.minio")
public record MinioStorageProperties(
        String baseUrl,
        String accessKey,
        String secretKey,
        String bucket,
        boolean secure,
        boolean createBucketIfMissing,
        int connectTimeoutMs,
        int writeTimeoutMs,
        int readTimeoutMs,
        long maxFileSizeBytes,
        List<String> allowedMimeTypes) {
}
