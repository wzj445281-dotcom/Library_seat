# 瑞幸咖啡购买小程序 (Ruixing Coffee Mini Program)

"不仅仅是好咖啡，更是智能点餐体验。"

📖 项目简介

瑞幸咖啡 (Ruixing Coffee) 是一个基于微信小程序开发的在线点餐系统，集成了新零售咖啡点单、智能推荐和订单管理功能。

本项目采用 Spring Boot + 微信小程序原生框架开发，支持完整的 O2O 电商流程。系统不仅支持饮品管理和订单处理，还通过 DeepSeek 大模型提供智能客服服务，并结合 Python 数据分析实现门店客流的实时监控与预测。

🚀 核心功能

☕ **在线菜单**：浏览瑞幸咖啡全系列产品，支持分类展示、商品详情、收藏功能。

🛒 **自助下单**：支持选择口味、甜度及加料，购物车管理，优惠券使用，地址管理。

📦 **订单管理**：实时查看订单进度、历史记录、订单详情及取餐码。

🤖 **AI 智能助手**：集成 DeepSeek 大模型，提供自然语言对话，解答营业时间、推荐饮品等问题。

📍 **门店查询**：定位并选择最近的瑞幸门店，支持到店自取和外卖配送。

🔵 **瑞幸蓝 UI 风格**：全套界面采用经典的 Luckin Blue 配色，专业且现代。

🐳 **全栈容器化**：基于 Docker Compose 的一键编排，整合 Java、Python、MySQL、Redis、RabbitMQ 五大服务。

🛠 技术栈

模块

技术选型

后端核心

Java 1.8+, Spring Boot 2.7, MyBatis Plus

AI 服务

Python 3.9, Flask, Pandas, DeepSeek API

前端界面

HTML5, Vue.js 2, Bootstrap 5, ECharts

数据库

MySQL 8.0

中间件

Redis 6.2 (缓存), RabbitMQ 3.8 (消息队列)

部署运维

Docker, Docker Compose

⚡ 快速开始 (Quick Start)

1. 环境准备

确保您的本地环境已安装：

Docker Desktop (包含 Docker Compose)

2. 获取代码

git clone https://github.com/wzj445281-dotcom/Ruixing.git
cd bookSeats


3. 配置 API Key (重要)

打开根目录下的 docker-compose.yml 文件，找到 ai-service 部分，填入您的 DeepSeek API Key：

ai-service:
environment:
- DEEPSEEK_API_KEY=sk-your-actual-key-here # <--- 替换这里


4. 一键启动

在项目根目录下执行：

docker-compose up --build -d


等待所有容器状态变为 Started (通常需要 1-2 分钟)。

🖥️ 访问地址

为防止端口冲突，本项目采用了非默认端口映射，请使用以下地址访问：

功能模块

访问地址

说明

后台管理首页

http://localhost:8081/admin.html

核心入口，查看数据概览

AI 监控大屏

http://localhost:8081/monitor.html

实时客流预测演示

菜单管理

http://localhost:8081/product_manage.html

商品 CRUD 操作

AI 对话接口

POST http://localhost:8081/api/app/ai/chat

可通过 Postman 测试

RabbitMQ 管理

http://localhost:15673

账号: guest / 密码: guest

数据库连接信息 (用于 Navicat/DBeaver):

Host: localhost

Port: 3307 (注意不是 3306)

Username: root

Password: root

Database: zhizuo_db

📂 项目结构

ruixing-coffee/
├── ai_service/             # Python AI 微服务
│   ├── predict_server.py   # AI 核心逻辑 (DeepSeek + 预测)
│   ├── Dockerfile          # Python 镜像构建
│   └── requirements.txt    # Python 依赖
├── seat_server/            # Java Spring Boot 后端
│   ├── src/main/java/      # 业务逻辑代码
│   ├── src/main/resources/
│   │   └── static/         # 前端静态页面 (admin.html, monitor.html)
│   └── Dockerfile          # Java 镜像构建
├── seat_client/            # 微信小程序前端
│   ├── pages/              # 页面文件
│   ├── components/         # 组件
│   ├── api/                # API 接口封装
│   └── utils/              # 工具函数
├── docker-compose.yml      # 容器编排配置 (防冲突版)
└── README.md               # 项目文档


❓ 常见问题

Q: 启动时提示端口被占用？
A: 本项目已将对外端口修改为 8081, 3307, 6380 等，通常不会冲突。如果仍有冲突，请修改 docker-compose.yml 中左侧的端口号。

Q: AI 对话没有反应？
A:

请检查 docker-compose.yml 中是否填入了正确的 DEEPSEEK_API_KEY。

检查 Python 服务日志：docker-compose logs -f ai-service。

Q: 页面显示 403 Forbidden？
A: 请确保后端 SecurityConfig.java 中已放行对应的 HTML 页面路径（如 /admin.html）。

📝 License

MIT License. Copyright (c) 2025 Ruixing Coffee Team.