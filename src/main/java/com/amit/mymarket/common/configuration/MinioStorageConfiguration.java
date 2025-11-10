package com.amit.mymarket.common.configuration;

import com.amit.mymarket.common.service.strategy.KeyNamingStrategy;
import com.amit.mymarket.common.service.strategy.KeyNamingStrategyEnum;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import okhttp3.OkHttpClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(value = MinioStorageProperties.class)
public class MinioStorageConfiguration {

    @Bean
    public MinioClient minioClient(MinioStorageProperties minioStorageProperties) {
        OkHttpClient httpClient = new OkHttpClient.Builder()
                .connectTimeout(minioStorageProperties.connectTimeoutMs(), TimeUnit.MILLISECONDS)
                .writeTimeout(minioStorageProperties.writeTimeoutMs(), TimeUnit.MILLISECONDS)
                .readTimeout(minioStorageProperties.readTimeoutMs(), TimeUnit.MILLISECONDS)
                .build();
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioStorageProperties.baseUrl())
                .credentials(minioStorageProperties.accessKey(), minioStorageProperties.secretKey())
                .httpClient(httpClient)
                .build();
        if (minioStorageProperties.createBucketIfMissing()) {
            try {
                boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder()
                        .bucket(minioStorageProperties.bucket())
                        .build());
                if (!bucketExists) {
                    minioClient.makeBucket(MakeBucketArgs.builder()
                            .bucket(minioStorageProperties.bucket())
                            .build());
                }
            } catch (Exception exception) {
                throw new IllegalStateException("Failed to ensure MinIO bucket exists: " + minioStorageProperties.bucket(), exception);
            }
        }
        return minioClient;
    }

    @Bean
    public KeyNamingStrategy keyNamingStrategy() {
        return KeyNamingStrategyEnum.DEFAULT_STRATEGY;
    }

}
