package com.amit.mymarket.common.service.impl;

import com.amit.mymarket.common.configuration.MinioStorageProperties;
import com.amit.mymarket.common.service.MediaStorageService;
import com.amit.mymarket.common.service.exception.MediaStorageException;
import com.amit.mymarket.common.service.strategy.KeyNamingStrategy;
import com.amit.mymarket.common.service.util.MediaFileValidator;
import com.amit.mymarket.common.service.util.MimeTypeResolver;
import com.amit.mymarket.common.service.util.PathSpecification;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.errors.ErrorResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

@Service
public class MinioMediaStorageService implements MediaStorageService {

    private final MinioClient minioClient;

    private final MinioStorageProperties minioStorageProperties;

    private final KeyNamingStrategy keyNamingStrategy;

    @Autowired
    public MinioMediaStorageService(MinioClient minioClient, MinioStorageProperties minioStorageProperties, KeyNamingStrategy keyNamingStrategy) {
        this.minioClient = minioClient;
        this.minioStorageProperties = minioStorageProperties;
        this.keyNamingStrategy = keyNamingStrategy;
    }

    @Override
    public String saveMediaFile(MultipartFile file, PathSpecification pathSpecification) {
        try {
            if (file == null || file.isEmpty()) {
                throw new MediaStorageException("Empty file");
            }
            long size = file.getSize();
            MediaFileValidator.validateFileSize(size, this.minioStorageProperties.maxFileSizeBytes());

            String mimeType = MimeTypeResolver.resolve(file);
            MediaFileValidator.validateMimeType(mimeType, this.minioStorageProperties.allowedMimeTypes());

            String key = this.keyNamingStrategy.buildKey(pathSpecification, mimeType);

            try (InputStream inputStream = file.getInputStream()) {
                PutObjectArgs.Builder builder = PutObjectArgs.builder()
                        .bucket(this.minioStorageProperties.bucket())
                        .object(key)
                        .contentType(mimeType);

                if (size >= 0) {
                    builder.stream(inputStream, size, -1);
                } else {
                    builder.stream(inputStream, -1, 5 * 1024 * 1024);
                }
                this.minioClient.putObject(builder.build());
                return key;
            }
        } catch (IllegalArgumentException exception) {
            throw new MediaStorageException("Validation error: " + exception.getMessage(), exception);
        } catch (Exception exception) {
            throw new MediaStorageException("Failed to store object", exception);
        }
    }

    @Override
    public void deleteMediaFile(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        try {
            this.minioClient.removeObject(RemoveObjectArgs.builder()
                    .bucket(this.minioStorageProperties.bucket())
                    .object(key)
                    .build());
        } catch (ErrorResponseException exception) {
            String errorCode = exception.errorResponse().code();
            if (!"NoSuchKey".equalsIgnoreCase(errorCode) && !"NoSuchObject".equalsIgnoreCase(errorCode)) {
                throw new MediaStorageException("Failed to delete object: " + key, exception);
            }
        } catch (Exception exception) {
            throw new MediaStorageException("Failed to delete object: " + key, exception);
        }
    }

    @Override
    public String buildPublicUrl(String key) {
        if (key == null) {
            return null;
        }
        String minioBaseUrl = this.minioStorageProperties.baseUrl();
        if (minioBaseUrl == null || minioBaseUrl.isBlank()) {
            return key;
        }
        String trimmedBaseUrl = minioBaseUrl.endsWith("/") ? minioBaseUrl.substring(0, minioBaseUrl.length() - 1) : minioBaseUrl;
        return trimmedBaseUrl + "/" + this.minioStorageProperties.bucket() + "/" + key;
    }

}
