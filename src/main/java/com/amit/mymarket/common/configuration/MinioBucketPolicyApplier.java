package com.amit.mymarket.common.configuration;

import io.minio.MinioClient;
import io.minio.SetBucketPolicyArgs;
import org.springframework.core.io.Resource;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;

public final class MinioBucketPolicyApplier {

    public static void applyPolicy(MinioClient minioClient, String bucket, Resource policyResource) {
        try {
            String raw = StreamUtils.copyToString(policyResource.getInputStream(), StandardCharsets.UTF_8);
            String json = raw.replace("${bucket}", bucket);
            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucket)
                            .config(json)
                            .build()
            );
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to set bucket policy for bucket: " + bucket, exception);
        }
    }

    private MinioBucketPolicyApplier() {
        throw new UnsupportedOperationException();
    }

}
