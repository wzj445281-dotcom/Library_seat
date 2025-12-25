# === 阶段 1: 构建 (Builder) ===
# 使用 Maven 镜像编译代码，不仅标准化构建环境，还能避免你本地 Java 版本不一致的问题
FROM maven:3.8.6-openjdk-11-slim AS builder
WORKDIR /app

# 1. 先复制 pom.xml 下载依赖 (利用 Docker 缓存层，加速后续构建)
COPY pom.xml .
# 建议配置阿里云 Maven 镜像 (如果构建慢的话)，这里使用默认
# RUN mvn dependency:go-offline -B

# 2. 复制源代码并打包
COPY src ./src
# -DskipTests 跳过测试用例，加快构建速度
RUN mvn clean package -DskipTests

# === 阶段 2: 运行 (Runtime) ===
# 使用轻量级 JRE 镜像运行，减小镜像体积
FROM openjdk:11-jre-slim
WORKDIR /app

# 设置时区为上海，避免日志时间差 8 小时
ENV TZ=Asia/Shanghai
RUN ln -snf /usr/share/zoneinfo/$TZ /etc/localtime && echo $TZ > /etc/timezone

# 从构建阶段复制生成的 Jar 包 (自动匹配 target 下的 jar)
COPY --from=builder /app/target/*.jar app.jar

# 暴露端口
EXPOSE 8080

# 启动命令
# -Djava.security.egd... 用于加快随机数生成，避免 Tomcat 启动卡顿
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]