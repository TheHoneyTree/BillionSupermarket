package com.billion.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@ConfigurationProperties(prefix = "billion.minio")
@Component
public class MinioProperties {
    private String endpoint;
    private String accesskey;
    private String secretKey;
    private String bucketName;
}
