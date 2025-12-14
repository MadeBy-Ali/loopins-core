package com.loopins.core.config;

import com.loopins.core.security.ServiceApiKeyFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final ServiceApiKeyFilter serviceApiKeyFilter;

    @Bean
    public FilterRegistrationBean<ServiceApiKeyFilter> serviceApiKeyFilterRegistration() {
        FilterRegistrationBean<ServiceApiKeyFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(serviceApiKeyFilter);
        registration.addUrlPatterns("/api/*");
        registration.setOrder(1);
        return registration;
    }
}

