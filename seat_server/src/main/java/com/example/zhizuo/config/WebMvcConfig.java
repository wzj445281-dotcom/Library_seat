package com.example.zhizuo.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 获取当前项目根目录下的 uploads 文件夹路径
        String uploadPath = System.getProperty("user.dir") + File.separator + "uploads" + File.separator;

        // 确保目录存在
        File dir = new File(uploadPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // 映射 URL: /uploads/** -> 本地文件系统: project/uploads/
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath);
        
        // 配置静态资源处理器：/static/** -> classpath:/static/
        // 注意：Spring Boot 默认支持 classpath:/static/，但需要显式配置以确保正确工作
        // 添加多个可能的路径，以确保资源能被找到
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/", "classpath:/static/images/", "classpath:/static/css/", "classpath:/static/js/")
                .setCachePeriod(3600); // 缓存1小时
        
        // 配置根路径直接访问静态资源
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);
                
        // 配置静态资源处理器：直接映射HTML文件到根路径
        registry.addResourceHandler("/admin.html", "/product_manage.html", "/monitor.html", "/feedback_manage.html", "/store_order_manage.html")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);
    }

    /**
     * 配置CORS跨域，允许微信小程序访问
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*") // 允许所有来源（开发环境）
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}