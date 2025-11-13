package com.amit.mymarket.unit.common.service;

import com.amit.mymarket.common.configuration.MinioStorageProperties;
import com.amit.mymarket.common.service.impl.MinioMediaStorageService;
import com.amit.mymarket.common.service.exception.MediaStorageException;
import com.amit.mymarket.common.service.strategy.KeyNamingStrategy;
import com.amit.mymarket.common.service.util.PathSpecification;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(value = MockitoExtension.class)
class MinioMediaStorageServiceTest {

    @Mock
    MinioClient minioClient;

    @Mock
    KeyNamingStrategy keyNamingStrategy;

    private MinioStorageProperties minioStorageProperties;

    private MinioMediaStorageService minioMediaStorageService;

    @BeforeEach
    void init() {
        this.minioStorageProperties = defaultMinioStorageProperties();
        this.minioMediaStorageService = buildNewMinioMediaStorageService(this.minioStorageProperties);
    }

    @Test
    @DisplayName(value = "Should store PNG and return generated key")
    void saveMediaFile_shouldStorePng() throws Exception {
        MockMultipartFile file = loadFile("sample.png", "sample.png");

        PathSpecification path = itemPath("42", "original");
        when(this.keyNamingStrategy.buildKey(path, "image/png"))
                .thenReturn("items/42/uuid/original.png");

        String key = this.minioMediaStorageService.saveMediaFile(file, path);

        assertEquals("items/42/uuid/original.png", key);
        ArgumentCaptor<PutObjectArgs> args = ArgumentCaptor.forClass(PutObjectArgs.class);
        verify(minioClient).putObject(args.capture());
        assertEquals("images", args.getValue().bucket());
        assertEquals("items/42/uuid/original.png", args.getValue().object());
        assertEquals("image/png", args.getValue().contentType());
    }

    @Test
    @DisplayName(value = "Should throw when file size exceeds limit")
    void saveMediaFile_shouldThrowWhenTooLarge() throws Exception {
        MinioMediaStorageService smallLimitService = buildNewMinioMediaStorageService(this.minioStoragePropertiesWithMaxSize(1));
        MockMultipartFile file = loadFile("sample.png", "sample.png");

        MediaStorageException exception = assertThrows(
                MediaStorageException.class,
                () -> smallLimitService.saveMediaFile(file, itemPath("1", "original"))
        );
        assertTrue(exception.getMessage().toLowerCase().contains("validation"));
        verifyNoInteractions(minioClient);
    }

    @Test
    @DisplayName(value = "Should throw when MIME is not allowed")
    void saveMediaFile_shouldThrowWhenMimeNotAllowed() throws Exception {
        MinioMediaStorageService jpegOnlyService = buildNewMinioMediaStorageService(this.minioStoragePropertiesWithMaxSizeWithAllowedMimes(List.of("image/jpeg")));
        MockMultipartFile file = loadFile("sample.png", "sample.png");

        MediaStorageException exception = assertThrows(
                MediaStorageException.class,
                () -> jpegOnlyService.saveMediaFile(file, itemPath("1", "original"))
        );
        assertTrue(exception.getMessage().toLowerCase().contains("validation"));
        verifyNoInteractions(this.minioClient);
    }

    @Test
    @DisplayName(value = "Should build public URL (base without trailing slash)")
    void buildPublicUrl_shouldBuildPublicUrl() {
        String url = this.minioMediaStorageService.buildPublicUrl("items/42/uuid/original.png");
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
        MinioMediaStorageService service = buildNewMinioMediaStorageService(trailing);

        String url = service.buildPublicUrl("k/p");
        assertEquals("http://localhost:9000/images/k/p", url);
    }

    @Test
    @DisplayName(value = "Should return null when key is null")
    void buildPublicUrl_shouldReturnNullForNullKey() {
        assertNull(this.minioMediaStorageService.buildPublicUrl(null));
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
        MinioMediaStorageService service = this.buildNewMinioMediaStorageService(minioStoragePropertiesWithBlankBaseUrl);

        assertEquals("k/p", service.buildPublicUrl("k/p"));
    }

    @Test
    @DisplayName(value = "Should call removeObject on delete for non-empty key")
    void deleteMediaFile_shouldDelete() throws Exception {
        this.minioMediaStorageService.deleteMediaFile("items/42/uuid/original.png");

        ArgumentCaptor<RemoveObjectArgs> args = ArgumentCaptor.forClass(RemoveObjectArgs.class);
        verify(this.minioClient).removeObject(args.capture());
        assertEquals("images", args.getValue().bucket());
        assertEquals("items/42/uuid/original.png", args.getValue().object());
    }

    @Test
    @DisplayName(value = "Should ignore delete when key is null or blank")
    void deleteMediaFile_shouldIgnoreDeleteForBlank() {
        this.minioMediaStorageService.deleteMediaFile(null);
        this.minioMediaStorageService.deleteMediaFile("   ");
        verifyNoInteractions(this.minioClient);
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

    private MinioStorageProperties minioStoragePropertiesWithMaxSize(long maxBytes) {
        MinioStorageProperties properties = this.defaultMinioStorageProperties();
        return new MinioStorageProperties(
                properties.baseUrl(),
                properties.accessKey(),
                properties.secretKey(),
                properties.bucket(),
                properties.secure(),
                properties.createBucketIfMissing(),
                properties.connectTimeoutMs(),
                properties.writeTimeoutMs(),
                properties.readTimeoutMs(),
                maxBytes,
                properties.allowedMimeTypes()
        );
    }

    private MinioStorageProperties minioStoragePropertiesWithMaxSizeWithAllowedMimes(List<String> allowedMimeTypes) {
        MinioStorageProperties properties = this.defaultMinioStorageProperties();
        return new MinioStorageProperties(
                properties.baseUrl(),
                properties.accessKey(),
                properties.secretKey(),
                properties.bucket(),
                properties.secure(),
                properties.createBucketIfMissing(),
                properties.connectTimeoutMs(),
                properties.writeTimeoutMs(),
                properties.readTimeoutMs(),
                properties.maxFileSizeBytes(),
                allowedMimeTypes
        );
    }

    private MinioMediaStorageService buildNewMinioMediaStorageService(MinioStorageProperties minioStorageProperties) {
        return new MinioMediaStorageService(minioClient, minioStorageProperties, keyNamingStrategy);
    }

    private MockMultipartFile loadFile(String resourceName, String originalName) throws Exception {
        try (InputStream inputStream = getClass().getResourceAsStream("/test-files/" + resourceName)) {
            assertNotNull(inputStream, "Resource not found: " + resourceName);
            return new MockMultipartFile("file", originalName, null, inputStream.readAllBytes());
        }
    }

    private PathSpecification itemPath(String itemId, String variant) {
        return PathSpecification.of("items", itemId, variant);
    }

}
