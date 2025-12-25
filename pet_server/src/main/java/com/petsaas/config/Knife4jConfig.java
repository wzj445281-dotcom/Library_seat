package com.petsaas.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class Knife4jConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("智座系统 API 文档")
                        .version("V2.0")
                        .description("基于 Spring Boot + Vue/UniApp 的高校座位智能调度系�?));
    }
}