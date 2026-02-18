package com.accolite.util;



import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI parserServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Parser Service API")
                        .description("File parsing microservice that publishes records to Kafka")
                        .version("v1.0"));
    }
}

