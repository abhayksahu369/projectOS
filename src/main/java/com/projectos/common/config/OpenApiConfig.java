package com.projectos.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI projectOsOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("ProjectOS API")
                        .version("v1")
                        .description("A universal project management API for tracking goals, tasks, "
                                + "milestones, notes and progress across any kind of project."));
    }
}
