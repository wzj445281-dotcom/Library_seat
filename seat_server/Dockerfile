# --- 第一阶段：构建阶段 ---
FROM maven:3.8.6-openjdk-11 AS build
WORKDIR /app

# 【关键修改】写入阿里云 Maven 镜像配置到 settings.xml
# 这解决了 Connection reset 和下载依赖卡住的问题
RUN mkdir -p /root/.m2 && \
    echo '<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0" \
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" \
      xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd"> \
      <mirrors> \
        <mirror> \
          <id>aliyunmaven</id> \
          <mirrorOf>*</mirrorOf> \
          <name>Aliyun Public</name> \
          <url>https://maven.aliyun.com/repository/public</url> \
        </mirror> \
      </mirrors> \
    </settings>' > /root/.m2/settings.xml

# 复制 pom.xml 和源代码
COPY pom.xml .
COPY src ./src

# 执行打包 (跳过测试以加快速度)
RUN mvn clean package -DskipTests

# --- 第二阶段：运行阶段 ---
FROM openjdk:11-jre-slim
WORKDIR /app

# 从构建阶段复制生成的 jar 包
COPY --from=build /app/target/*.jar app.jar

# 设置时区
RUN ln -sf /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && echo 'Asia/Shanghai' > /etc/timezone

# 暴露 8080 端口
EXPOSE 8080

# 启动命令
ENTRYPOINT ["java", "-jar", "app.jar"]