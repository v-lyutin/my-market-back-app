package com.amit.mymarket.common.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(value = "storage.minio")
public record MinioStorageProperties(

        @Value(value = "base-url")
        String baseUrl,

        @Value(value = "access-key")
        String accessKey,

        @Value(value = "secret-key")
        String secretKey,

        @Value(value = "bucket")
        String bucket,

        @Value(value = "secure")
        boolean secure,

        @Value(value = "create-bucket-if-missing")
        boolean createBucketIfMissing,

        @Value(value = "connect-timeout-ms")
        int connectTimeoutMs,

        @Value(value = "write-timeout-ms")
        int writeTimeoutMs,

        @Value(value = "read-timeout-ms")
        int readTimeoutMs,

        @Value(value = "max-file-size-bytes")
        long maxFileSizeBytes,

        @Value(value = "allowed-mime-types")
        List<String> allowedMimeTypes) {
}
