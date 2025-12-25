package com.petsaas;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.petsaas.core.mapper")
public class PetsaasApplication {
    public static void main(String[] args) {
        SpringApplication.run(PetsaasApplication.class, args);
    }
}