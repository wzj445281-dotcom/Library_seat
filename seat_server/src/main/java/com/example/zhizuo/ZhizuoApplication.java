package com.example.zhizuo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan({"com.example.zhizuo.core.mapper", "com.example.zhizuo.mapper"})
public class ZhizuoApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZhizuoApplication.class, args);
    }
}