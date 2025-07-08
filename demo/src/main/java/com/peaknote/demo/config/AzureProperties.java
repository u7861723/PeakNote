package com.peaknote.demo.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "azure")
public class AzureProperties {
    private String tenantId;
    private String clientId;
    private String clientSecret;
    private Graph graph;

    @Data
    public static class Graph {
        private String scope;
    }
}
