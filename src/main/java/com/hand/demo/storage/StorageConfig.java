package com.hand.demo.storage;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class StorageConfig {

    @Bean
    @ConditionalOnProperty(name = "storage.type", havingValue = "local", matchIfMissing = true)
    public BlobStorage localBlobStorage(
            @Value("${app.images.storage-dir}") String storageDir,
            @Value("${app.images.public-base:/images/}") String publicBase,
            @Value("${app.base-url:}") String appBaseUrl
    ) throws IOException {
        return new LocalBlobStorage(storageDir, publicBase, appBaseUrl);
    }

    @Bean
    @ConditionalOnProperty(name = "storage.type", havingValue = "s3")
    public BlobStorage s3BlobStorage(
            @Value("${app.s3.bucket}") String bucket,
            @Value("${app.s3.region}") String region,
            @Value("${app.s3.prefix:images/}") String prefix,
            @Value("${app.images.public-base:/images/}") String publicBase,
            @Value("${app.cdn.base-url:}") String cdnBaseUrl
    ) {
        return new S3BlobStorage(bucket, region, prefix, publicBase, cdnBaseUrl);
    }
}
