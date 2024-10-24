package com.billion.config;

import com.billion.properties.MinioProperties;
import com.billion.utils.MinioUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class MinioConfiguration {
    @Autowired
    private MinioProperties minioProperties;

    @Bean
    @ConditionalOnMissingBean
    public MinioUtil minioUtil(MinioProperties minioProperties){
        log.info("创建Minio文件上传工具类对象...");
        return new MinioUtil(minioProperties.getEndpoint(),
                minioProperties.getAccesskey(),
                minioProperties.getSecretKey(),
                minioProperties.getBucketName());
    }
}
