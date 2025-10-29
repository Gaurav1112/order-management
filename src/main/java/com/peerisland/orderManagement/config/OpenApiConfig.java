package com.peerisland.orderManagement.config;

import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI orderManagementOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("🛒 Order Management System API")
                .description("Spring Boot REST API for managing E-commerce Orders, Status History, and Scheduling.")
                .version("1.0.0")
                .contact(new Contact()
                    .name("Kumar Gaurav")
                    .email("kgauravis016@gmail.com")
                    .url("https://github.com/Gaurav1112/"))
                .license(new License()
                    .name("Apache 2.0")
                    .url("https://springdoc.org")))
            .externalDocs(new ExternalDocumentation()
                .description("Project README / Documentation")
                .url("https://github.com/peerisland/order-management"));
    }
}
