# 阶段一：基础镜像 (推荐用 JDK 17 或 8，取决于你的项目)
FROM openjdk:17-jdk-alpine

# 设置工作目录
WORKDIR /app

# 将 Maven 打包好的 jar 复制进去
# 注意：在此之前你需要先在本地运行 mvn clean package
COPY target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动命令
# 优化点：增加 -Djava.security.egd 用于加快随机数生成，避免启动卡顿
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]