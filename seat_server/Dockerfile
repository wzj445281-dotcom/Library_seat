# 第一阶段：构建 (Maven Build)
FROM maven:3.8.6-openjdk-18 AS build
WORKDIR /app

# 优化：先复制 pom.xml 下载依赖，利用缓存
COPY pom.xml .
# 这一步会下载依赖，如果 pom.xml 没变，这层会被缓存
RUN mvn dependency:go-offline -B

# 复制源码并打包
COPY src ./src
RUN mvn clean package -DskipTests

# 第二阶段：运行 (Run)
FROM openjdk:18-slim
WORKDIR /app

# 从构建阶段复制 jar 包
COPY --from=build /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动命令
# 建议加上 -Djava.security.egd=file:/dev/./urandom 加快随机数生成
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]