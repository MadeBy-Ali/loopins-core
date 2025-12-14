package com.loopins.core.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${server.servlet.context-path:/api}")
    private String contextPath;

    @Bean
    public OpenAPI loopinsCoreOpenAPI() {
        // Server configuration
        Server localServer = new Server()
                .url("http://localhost:" + serverPort + contextPath)
                .description("Local Development Server");

        // Security scheme for service-to-service authentication
        SecurityScheme serviceApiKeyScheme = new SecurityScheme()
                .type(SecurityScheme.Type.APIKEY)
                .in(SecurityScheme.In.HEADER)
                .name("X-SERVICE-KEY")
                .description("Service-to-service API key for protected endpoints (payment callbacks)");

        // Contact information
        Contact contact = new Contact()
                .name("Loopins Development Team")
                .email("dev@loopins.com")
                .url("https://loopins.com");

        // API information
        Info info = new Info()
                .title("Loopins Core")
                .version("1.0.0")
                .description("""
                        ## Loopins Core Service - Order Domain
                        
                        ### Protected Endpoints
                        This endpoints require `X-SERVICE-KEY` header for service-to-service authentication:
                        - `POST /orders/{orderId}/payment-confirmed`
                        - `POST /orders/{orderId}/payment-failed`
                        """);

        return new OpenAPI()
                .info(info)
                .servers(List.of(localServer))
                .addSecurityItem(new SecurityRequirement().addList("serviceApiKey"))
                .schemaRequirement("serviceApiKey", serviceApiKeyScheme);
    }
}

