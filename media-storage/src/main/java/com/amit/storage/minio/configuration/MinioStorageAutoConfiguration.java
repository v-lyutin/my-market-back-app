package com.amit.storage.minio.configuration;

import com.amit.storage.minio.service.MinioMediaStorageService;
import com.amit.storage.minio.service.MinioMediaUrlResolver;
import com.amit.storage.service.MediaStorageService;
import com.amit.storage.service.MediaUrlResolver;
import com.amit.storage.strategy.KeyNamingStrategy;
import com.amit.storage.strategy.KeyNamingStrategyEnum;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import okhttp3.OkHttpClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.Resource;

import java.util.concurrent.TimeUnit;

@AutoConfiguration
@ConditionalOnClass(value = MinioClient.class)
@ConditionalOnProperty(prefix = "storage.minio", name = "enabled", matchIfMissing = true)
@EnableConfigurationProperties(value = MinioStorageProperties.class)
public class MinioStorageAutoConfiguration {

    private final MinioStorageProperties minioStorageProperties;

    @Value(value = "classpath:minio/policies/read-only.json")
    private Resource readOnlyPolicy;

    public MinioStorageAutoConfiguration(MinioStorageProperties minioStorageProperties) {
        this.minioStorageProperties = minioStorageProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public MinioClient minioClient() {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(this.minioStorageProperties.connectTimeoutMs(), TimeUnit.MILLISECONDS)
                .writeTimeout(this.minioStorageProperties.writeTimeoutMs(), TimeUnit.MILLISECONDS)
                .readTimeout(this.minioStorageProperties.readTimeoutMs(), TimeUnit.MILLISECONDS)
                .build();
        MinioClient minioClient = MinioClient.builder()
                .endpoint(this.minioStorageProperties.baseUrl())
                .credentials(this.minioStorageProperties.accessKey(), this.minioStorageProperties.secretKey())
                .httpClient(okHttpClient)
                .build();
        this.ensureBucketAndPolicy(minioClient, this.minioStorageProperties);
        return minioClient;
    }

    @Bean
    @ConditionalOnMissingBean
    public KeyNamingStrategy keyNamingStrategy() {
        return KeyNamingStrategyEnum.DEFAULT_STRATEGY;
    }

    @Bean
    @ConditionalOnMissingBean
    public MediaStorageService mediaStorageService(MinioClient minioClient, MinioStorageProperties minioStorageProperties, KeyNamingStrategy keyNamingStrategy) {
        return new MinioMediaStorageService(minioClient, minioStorageProperties, keyNamingStrategy);
    }

    @Bean
    @ConditionalOnMissingBean
    public MediaUrlResolver minioMediaUrlResolver(MinioStorageProperties minioStorageProperties) {
        return new MinioMediaUrlResolver(minioStorageProperties);
    }

    private void ensureBucketAndPolicy(MinioClient minioClient, MinioStorageProperties minioStorageProperties) {
        if (!minioStorageProperties.createBucketIfMissing()) {
            return;
        }
        try {
            boolean bucketExists = minioClient.bucketExists(BucketExistsArgs.builder().bucket(minioStorageProperties.bucket()).build());
            if (!bucketExists) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioStorageProperties.bucket()).build());
            }
            MinioBucketPolicyApplier.applyPolicy(minioClient, minioStorageProperties.bucket(), this.readOnlyPolicy);
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to initialize MinIO bucket/policy: " + minioStorageProperties.bucket(), exception);
        }
    }

}
