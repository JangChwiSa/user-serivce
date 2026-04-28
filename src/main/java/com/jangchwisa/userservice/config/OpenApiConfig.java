package com.jangchwisa.userservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI userServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("JangChwiSa User Service API")
                        .description("Authentication, user profile, and internal user lookup APIs.")
                        .version("v1")
                        .contact(new Contact().name("JangChwiSa"))
                        .license(new License().name("Proprietary")));
    }
}
