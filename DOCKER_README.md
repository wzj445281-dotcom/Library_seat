# 宠物服务与用品商城系统 - Docker 部署指南

## 快速启动

1. 确保已安装 Docker 和 Docker Compose

2. 复制环境变量文件并配置：
```bash
cp .env.example .env
# 编辑 .env 文件，配置数据库密码等
```

3. 构建并启动所有服务：
```bash
docker-compose up -d
```

4. 查看服务状态：
```bash
docker-compose ps
```

## 服务说明

- **MySQL (3306)**: 数据库服务
- **Redis (6379)**: 缓存服务
- **RabbitMQ (5672)**: 消息队列服务，管理界面 http://localhost:15673
- **Python AI 服务 (5000)**: 宠物健康咨询AI服务
- **Java 后端 (8080)**: 主业务服务

## 环境变量说明

| 变量名 | 说明 | 默认值 |
|--------|------|--------|
| DB_PASSWORD | MySQL数据库密码 | 123456 |
| REDIS_PASSWORD | Redis密码 | 123456 |
| RABBIT_USER | RabbitMQ用户名 | admin |
| RABBIT_PASSWORD | RabbitMQ密码 | 123456 |
| GEMINI_API_KEY | Gemini API密钥 | - |

## 常用命令

```bash
# 查看日志
docker-compose logs -f [服务名]

# 停止所有服务
docker-compose down

# 重新构建并启动
docker-compose up -d --build

# 进入容器
docker-compose exec [服务名] sh
```