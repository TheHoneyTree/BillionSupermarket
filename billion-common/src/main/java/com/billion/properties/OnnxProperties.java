package com.billion.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "billion.onnx")
@Data
public class OnnxProperties {

    private String device; //设备

}
