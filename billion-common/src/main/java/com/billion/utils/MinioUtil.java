package com.billion.utils;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Slf4j
@Data
@AllArgsConstructor
public class MinioUtil {
    private String endpoint;
    private String accesskey;
    private String secretKey;
    private String bucketName;

    public String upload(byte[] bytes, String objectName) {
        MinioClient minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accesskey, secretKey)
                .build();

        try {
            InputStream inputStream = new ByteArrayInputStream(bytes);
            PutObjectArgs putObjectArgs = PutObjectArgs.builder()
                    .bucket(bucketName)
                    .stream(inputStream, inputStream.available(), -1)
                    .object(objectName)
                    .build();
            // 创建PutObject请求。
            minioClient.putObject(putObjectArgs);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //文件访问路径规则 http://endpoint/bucketName/objectName
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder
                .append(endpoint)
                .append("/")
                .append(bucketName)
                .append("/")
                .append(objectName);

        log.info("文件上传到:{}", stringBuilder.toString());

        return stringBuilder.toString();
    }
}
