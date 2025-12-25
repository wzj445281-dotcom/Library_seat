package com.example.zhizuo.config;

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
                        .title("瑞幸咖啡 API 文档")
                        .version("V2.0")
                        .description("基于 Spring Boot + 微信小程序的瑞幸咖啡在线点餐系统"));
    }
}