package com.amit.storage.service;

import com.amit.storage.minio.configuration.MinioStorageProperties;
import com.amit.storage.minio.service.MinioMediaUrlResolver;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MinioMediaUrlResolverTest {

    private MinioStorageProperties minioStorageProperties;

    private MinioMediaUrlResolver minioMediaUrlResolver;

    @BeforeEach
    void init() {
        this.minioStorageProperties = defaultMinioStorageProperties();
        this.minioMediaUrlResolver = this.buildNewMinioMediaUrlResolver(this.minioStorageProperties);
    }

    @Test
    @DisplayName(value = "Should build public URL (base without trailing slash)")
    void buildPublicUrl_shouldBuildPublicUrl() {
        String url = this.minioMediaUrlResolver.buildPublicUrl("items/42/uuid/original.png");
        assertEquals("http://localhost:9000/images/items/42/uuid/original.png", url);
    }

    @Test
    @DisplayName(value = "Should trim trailing slash in base URL")
    void buildPublicUrl_shouldTrimTrailingSlash() {
        MinioStorageProperties trailing = new MinioStorageProperties(
                "http://localhost:9000/",
                this.minioStorageProperties.accessKey(),
                this.minioStorageProperties.secretKey(),
                this.minioStorageProperties.bucket(),
                this.minioStorageProperties.secure(),
                this.minioStorageProperties.createBucketIfMissing(),
                this.minioStorageProperties.connectTimeoutMs(),
                this.minioStorageProperties.writeTimeoutMs(),
                this.minioStorageProperties.readTimeoutMs(),
                this.minioStorageProperties.maxFileSizeBytes(),
                this.minioStorageProperties.allowedMimeTypes()
        );
        MinioMediaUrlResolver mediaUrlResolver = buildNewMinioMediaUrlResolver(trailing);

        String url = mediaUrlResolver.buildPublicUrl("k/p");
        assertEquals("http://localhost:9000/images/k/p", url);
    }

    @Test
    @DisplayName(value = "Should return null when key is null")
    void buildPublicUrl_shouldReturnNullForNullKey() {
        assertNull(this.minioMediaUrlResolver.buildPublicUrl(null));
    }

    @Test
    @DisplayName(value = "Should return key when base URL is blank")
    void buildPublicUrl_shouldReturnKeyWhenBaseUrlBlank() {
        MinioStorageProperties minioStoragePropertiesWithBlankBaseUrl = new MinioStorageProperties(
                "",
                this.minioStorageProperties.accessKey(),
                this.minioStorageProperties.secretKey(),
                this.minioStorageProperties.bucket(),
                this.minioStorageProperties.secure(),
                this.minioStorageProperties.createBucketIfMissing(),
                this.minioStorageProperties.connectTimeoutMs(),
                this.minioStorageProperties.writeTimeoutMs(),
                this.minioStorageProperties.readTimeoutMs(),
                this.minioStorageProperties.maxFileSizeBytes(),
                this.minioStorageProperties.allowedMimeTypes()
        );
        MinioMediaUrlResolver mediaUrlResolver = this.buildNewMinioMediaUrlResolver(minioStoragePropertiesWithBlankBaseUrl);

        assertEquals("k/p", mediaUrlResolver.buildPublicUrl("k/p"));
    }

    private MinioStorageProperties defaultMinioStorageProperties() {
        return new MinioStorageProperties(
                "http://localhost:9000",
                "minio",
                "minio123",
                "images",
                false,
                false,
                5_000,
                60_000,
                60_000,
                10 * 1024 * 1024,
                List.of("image/png", "image/jpeg", "image/webp")
        );
    }

    private MinioMediaUrlResolver buildNewMinioMediaUrlResolver(MinioStorageProperties minioStorageProperties) {
        return new MinioMediaUrlResolver(minioStorageProperties);
    }

}
