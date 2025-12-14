package com.loopins.core.client.config;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfig {

    @Value("${fulfillment.service.api-key}")
    private String apiKey;

    /**
     * Adds API key header to all Feign requests for service-to-service authentication.
     */
    @Bean
    public RequestInterceptor apiKeyRequestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("X-SERVICE-KEY", apiKey);
            requestTemplate.header("Content-Type", "application/json");
        };
    }
}

