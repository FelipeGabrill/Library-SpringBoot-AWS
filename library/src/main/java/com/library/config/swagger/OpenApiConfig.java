package com.library.config.swagger;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI libraryOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Library Management System API")
                        .description("""
                                RESTful API for a digital library management system built with Spring Boot and AWS.
                                Features:
                                - User registration with profile picture upload (S3)
                                - Category and book management with cover images
                                - Loan system with overdue detection and fine calculation
                                - Reservation system with automatic notification (SNS/SQS/SES)
                                - Password recovery via email
                                - OAuth2 authentication with JWT (password grant)

                                **Authentication:** obtain a token via `POST /oauth2/token`
                                (Basic Auth with clientId/clientSecret + body grant_type=password),
                                then click **Authorize** and paste the access_token.
                                """)
                        .version("v1")
                        .contact(new Contact()
                                .name("Felipe Gabriel")
                                .url("https://github.com/FelipeGabrill/Library-SpringBoot-AWS"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .components(new Components()
                        .addSecuritySchemes(SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("JWT access token obtained from /oauth2/token")));
    }

    @Bean
    public GroupedOpenApi libraryApi() {
        return GroupedOpenApi.builder()
                .group("library-api")
                .packagesToScan(
                        "com.library.controllers",
                        "com.library.dtos"
                )
                .pathsToMatch("/**")
                .build();
    }
}
