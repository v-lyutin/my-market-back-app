package com.amit.mymarket.common.configuration;

import com.amit.mymarket.common.service.strategy.KeyNamingStrategy;
import com.amit.mymarket.common.service.strategy.KeyNamingStrategyEnum;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableConfigurationProperties(value = MinioStorageProperties.class)
public class MinioStorageConfiguration {

    @Value("classpath:minio/policies/read-only.json")
    private Resource readOnlyPolicy;

    @Value("${storage.minio.public-read-enabled}")
    private boolean publicReadEnabled;

    @Bean
    public MinioClient minioClient(MinioStorageProperties minioStorageProperties) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(minioStorageProperties.connectTimeoutMs(), TimeUnit.MILLISECONDS)
                .writeTimeout(minioStorageProperties.writeTimeoutMs(), TimeUnit.MILLISECONDS)
                .readTimeout(minioStorageProperties.readTimeoutMs(), TimeUnit.MILLISECONDS)
                .build();
        MinioClient minioClient = MinioClient.builder()
                .endpoint(minioStorageProperties.baseUrl())
                .credentials(minioStorageProperties.accessKey(), minioStorageProperties.secretKey())
                .httpClient(okHttpClient)
                .build();
        this.ensureBucketAndPolicy(minioClient, minioStorageProperties);
        return minioClient;
    }

    @Bean
    public KeyNamingStrategy keyNamingStrategy() {
        return KeyNamingStrategyEnum.DEFAULT_STRATEGY;
    }

    private void ensureBucketAndPolicy(MinioClient minioClient, MinioStorageProperties minioStorageProperties) {
        if (!minioStorageProperties.createBucketIfMissing() && !this.publicReadEnabled) {
            return;
        }
        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioStorageProperties.bucket()).build());
            if (!bucketExists && minioStorageProperties.createBucketIfMissing()) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioStorageProperties.bucket()).build());
            }
            if (this.publicReadEnabled) {
                MinioBucketPolicyApplier.applyPolicy(minioClient, minioStorageProperties.bucket(), readOnlyPolicy);
            }
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to initialize MinIO bucket/policy: " + minioStorageProperties.bucket(), exception);
        }
    }

}
