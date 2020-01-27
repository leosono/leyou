package com.leyou.upload.config;

/**
 * @author leoso
 * @create 2020-01-01 19:37
 */

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;

import java.util.List;

@Data
@ConfigurationProperties(prefix="ly.upload")
public class UploadProperties {
    private String baseUrl;
    private List<String> allowTypes;
}
