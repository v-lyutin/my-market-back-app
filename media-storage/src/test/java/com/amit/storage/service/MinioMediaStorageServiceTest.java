package com.amit.storage.service;

import com.amit.storage.exception.MediaStorageException;
import com.amit.storage.minio.configuration.MinioStorageProperties;
import com.amit.storage.minio.service.MinioMediaStorageService;
import com.amit.storage.strategy.KeyNamingStrategy;
import com.amit.storage.util.PathSpecification;
import io.minio.MinioClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.when;

@ExtendWith(value = MockitoExtension.class)
class MinioMediaStorageServiceTest {

    @Mock
    MinioClient minioClient;

    @Mock
    KeyNamingStrategy keyNamingStrategy;

    private MinioMediaStorageService minioMediaStorageService;

    @BeforeEach
    void init() {
        MinioStorageProperties minioStorageProperties = defaultMinioStorageProperties();
        this.minioMediaStorageService = buildNewMinioMediaStorageService(minioStorageProperties);
    }

    @Test
    @DisplayName(value = "Should store PNG and return generated key")
    void saveMediaFile_shouldStorePng() throws Exception {
        FilePart filePart = loadFileAsFilePart("sample.png", "sample.png");

        PathSpecification pathSpecification = itemPath("42", "original");
        when(this.keyNamingStrategy.buildKey(pathSpecification, "image/png"))
                .thenReturn("items/42/uuid/original.png");

        Mono<String> resultMono = this.minioMediaStorageService.saveMediaFile(filePart, pathSpecification);

        StepVerifier.create(resultMono)
                .assertNext(generatedKey -> assertEquals("items/42/uuid/original.png", generatedKey))
                .verifyComplete();

        verify(this.minioClient).putObject(argThat(args -> {
            assertEquals("images", args.bucket());
            assertEquals("items/42/uuid/original.png", args.object());
            return true;
        }));
    }

    @Test
    @DisplayName(value = "Should throw when file size exceeds limit")
    void saveMediaFile_shouldThrowWhenTooLarge() throws Exception {
        MinioMediaStorageService smallLimitService = buildNewMinioMediaStorageService(this.minioStoragePropertiesWithMaxSize(1));

        FilePart filePart = loadFileAsFilePart("sample.png", "sample.png");

        Mono<String> result = smallLimitService.saveMediaFile(filePart, itemPath("1", "original"));

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assertInstanceOf(MediaStorageException.class, throwable);
                    assertTrue(throwable.getMessage().toLowerCase().contains("validation"));
                })
                .verify();

        verifyNoInteractions(this.minioClient);
    }

    @Test
    @DisplayName(value = "Should throw when MIME is not allowed")
    void saveMediaFile_shouldThrowWhenMimeNotAllowed() throws Exception {
        MinioMediaStorageService jpegOnlyService = buildNewMinioMediaStorageService(
                this.minioStoragePropertiesWithMaxSizeWithAllowedMimes(List.of("image/jpeg"))
        );

        FilePart filePart = loadFileAsFilePart("sample.png", "sample.png");

        Mono<String> result = jpegOnlyService.saveMediaFile(filePart, itemPath("1", "original"));

        StepVerifier.create(result)
                .expectErrorSatisfies(throwable -> {
                    assertInstanceOf(MediaStorageException.class, throwable);
                    assertTrue(throwable.getMessage().toLowerCase().contains("validation"));
                })
                .verify();

        verifyNoInteractions(this.minioClient);
    }

    @Test
    @DisplayName(value = "Should ignore delete when key is null or blank")
    void deleteMediaFile_shouldIgnoreDeleteForBlank() {
        Mono<Void> nullKeyResult = this.minioMediaStorageService.deleteMediaFile(null);
        Mono<Void> blankKeyResult = this.minioMediaStorageService.deleteMediaFile("   ");

        StepVerifier.create(nullKeyResult).verifyComplete();
        StepVerifier.create(blankKeyResult).verifyComplete();

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

    private FilePart loadFileAsFilePart(String resourceName, String originalName) throws Exception {
        try (InputStream inputStream = getClass().getResourceAsStream("/test-files/" + resourceName)) {
            assertNotNull(inputStream, "Resource not found: " + resourceName);

            byte[] fileContentBytes = inputStream.readAllBytes();
            DefaultDataBufferFactory dataBufferFactory = new DefaultDataBufferFactory();
            DataBuffer dataBuffer = dataBufferFactory.wrap(fileContentBytes);

            FilePart filePart = mock(FilePart.class);
            when(filePart.filename()).thenReturn(originalName);
            when(filePart.content()).thenReturn(Flux.just(dataBuffer));

            return filePart;
        }
    }

    private PathSpecification itemPath(String itemId, String variant) {
        return PathSpecification.of("items", itemId, variant);
    }

    private String minioStorageServiceBucket() {
        return "images";
    }

}
