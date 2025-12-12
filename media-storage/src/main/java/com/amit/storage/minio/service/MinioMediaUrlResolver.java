package com.amit.storage.minio.service;

import com.amit.storage.minio.configuration.MinioStorageProperties;
import com.amit.storage.service.MediaUrlResolver;

public final class MinioMediaUrlResolver implements MediaUrlResolver {

    private final MinioStorageProperties minioStorageProperties;

    public MinioMediaUrlResolver(MinioStorageProperties minioStorageProperties) {
        this.minioStorageProperties = minioStorageProperties;
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
