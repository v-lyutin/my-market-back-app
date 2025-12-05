package com.amit.mymarket.common.service.minio;

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
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayInputStream;
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
    public Mono<String> saveMediaFile(FilePart file, PathSpecification pathSpecification) {
        return Mono.defer(() -> {
            if (file == null || file.filename().isBlank()) {
                return Mono.error(new MediaStorageException("Empty file"));
            }

            return DataBufferUtils.join(file.content())
                    .switchIfEmpty(Mono.error(new MediaStorageException("Empty file")))
                    .flatMap(buffer -> {
                        byte[] bytes = toByteArray(buffer);
                        return this.validateAndSave(bytes, file.filename(), pathSpecification);
                    });
        });
    }

    @Override
    public Mono<Void> deleteMediaFile(String key) {
        if (!StringUtils.hasText(key)) {
            return Mono.empty();
        }
        return Mono.fromRunnable(() -> {
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
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    private byte[] toByteArray(DataBuffer buffer) {
        try {
            int size = buffer.readableByteCount();
            byte[] bytes = new byte[size];
            buffer.read(bytes);
            return bytes;
        } finally {
            DataBufferUtils.release(buffer);
        }
    }

    private Mono<String> validateAndSave(byte[] bytes,
                                         String filename,
                                         PathSpecification pathSpecification) {
        long fileSize = bytes.length;

        try {
            MediaFileValidator.validateFileSize(fileSize, this.minioStorageProperties.maxFileSizeBytes());
            String mimeType = MimeTypeResolver.resolve(filename, bytes);
            MediaFileValidator.validateMimeType(mimeType, this.minioStorageProperties.allowedMimeTypes());
            String key = this.keyNamingStrategy.buildKey(pathSpecification, mimeType);

            return this.uploadContentToMinio(bytes, fileSize, mimeType, key);
        } catch (IllegalArgumentException exception) {
            return Mono.error(new MediaStorageException("Validation error: " + exception.getMessage(), exception));
        } catch (Exception exception) {
            return Mono.error(new MediaStorageException("Failed to store object", exception));
        }
    }

    private Mono<String> uploadContentToMinio(byte[] bytes, long fileSize, String mimeType, String key) {
        return Mono.fromCallable(() -> {
                    try (InputStream inputStream = new ByteArrayInputStream(bytes)) {
                        PutObjectArgs args = PutObjectArgs.builder()
                                .bucket(this.minioStorageProperties.bucket())
                                .object(key)
                                .contentType(mimeType)
                                .stream(inputStream, fileSize, -1)
                                .build();

                        this.minioClient.putObject(args);
                        return key;
                    } catch (Exception exception) {
                        throw new MediaStorageException("Failed to store object", exception);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic());
    }

}
