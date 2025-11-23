package com.amit.mymarket.common.service.minio;

import com.amit.mymarket.common.configuration.MinioStorageProperties;
import com.amit.mymarket.common.service.MediaUrlResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public final class MinioMediaUrlResolver implements MediaUrlResolver {

    private final MinioStorageProperties minioStorageProperties;

    @Autowired
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
