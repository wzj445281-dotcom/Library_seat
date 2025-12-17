# =========================================================
# 阶段一：构建 (使用 Debian 版镜像，解决 SSL 握手失败问题)
# =========================================================
FROM maven:3.8.6-eclipse-temurin-8 AS build
WORKDIR /app

# [关键步骤] 强行覆盖 Maven 全局配置，强制使用阿里云镜像
# 这样无论 pom.xml 怎么写，都会走阿里云，速度起飞且不会报错
RUN echo '<?xml version="1.0" encoding="UTF-8"?> \
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" \
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" \
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd"> \
    <mirrors> \
        <mirror> \
            <id>aliyunmaven</id> \
            <mirrorOf>*</mirrorOf> \
            <name>阿里云公共仓库</name> \
            <url>https://maven.aliyun.com/repository/public</url> \
        </mirror> \
    </mirrors> \
</settings>' > /usr/share/maven/conf/settings.xml

# 复制 pom.xml 和源码
COPY pom.xml .
COPY src ./src

# 执行构建 (跳过测试)
RUN mvn clean package -DskipTests

# =========================================================
# 阶段二：运行 (使用轻量级 Alpine 镜像运行，减小体积)
# =========================================================
FROM eclipse-temurin:8-jre-alpine
WORKDIR /app

# 从构建阶段复制生成的 jar 包
COPY --from=build /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]