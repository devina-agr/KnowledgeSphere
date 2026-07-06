package com.example.knowledgesphere.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.*;

@Configuration
public class SwaggerConfig {

    @Bean
    OpenAPI openAPI(){

        return new OpenAPI()

                .info(

                        new Info()

                                .title("KnowledgeSphere API")

                                .version("1.0")

                                .description(

                                        "Enterprise AI Knowledge Platform"

                                )

                );

    }

}