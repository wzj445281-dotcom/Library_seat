📚 智座 (SmartSeat) - 智能图书馆座位预约系统 (V2.0)
基于 Spring Boot + Python AI + Docker 的企业级微服务毕业设计 > 集成了座位热度预测、用户风控、高并发抢座、实时数据大屏的完整解决方案。

🌟 项目简介
《智座》不仅仅是一个预约工具，更是一个智能空间管理平台。本项目从传统的单体架构升级为微服务架构，引入了 Python 机器学习模块进行数据分析，并采用 Docker 容器化部署。

核心亮点（答辩加分项）：

🧠 AI 驱动决策：基于 Python 的机器学习算法，预测座位“热度”与用户“违约风险”。

🛡️ 高并发防撞：使用 Redisson 分布式锁 解决超卖问题。

⚡ 异步削峰：利用 RabbitMQ 死信队列 实现订单超时自动释放，解耦核心业务。

📊 可视化监控：提供深色科技风的 Web 实时监控大屏，展示 AI 热力图与业务指标。

🐳 容器化部署：全栈 Docker Compose 一键编排，解决环境依赖痛点。

🏗️ 系统架构
Plaintext

[微信小程序] <--> [Nginx/API网关] <--> [Spring Boot 后端] <--> [MySQL]
^     |   |
|     |   v
[REST API] | [Redis (缓存/锁)]
|     v
[Python AI 服务] <--> [RabbitMQ (消息队列)]
🛠️ 技术栈
后端核心: Java 8, Spring Boot 2.7, MyBatis-Plus

AI 服务: Python 3.9, Flask (提供 HTTP 预测接口)

数据库: MySQL 8.0

缓存/锁: Redis, Redisson

消息队列: RabbitMQ (延迟队列插件/DLX模式)

前端: 微信小程序 (原生)

监控: HTML5 + Vue.js + ECharts

运维: Docker, Docker Compose

📂 目录结构 (Docker 版)
我们在根目录下拆分了服务，以适应微服务部署：

Plaintext

Library_seat/
├── ai_service/              # [新增] Python AI 预测服务
│   ├── Dockerfile           # Python 镜像构建文件
│   └── predict_server.py    # AI 核心逻辑 (热度预测+风控)
├── seat_server/             # Java 后端服务
│   ├── src/main/resources/static/monitor.html # [新增] 监控大屏
│   ├── Dockerfile           # Java 镜像构建文件 (含阿里云加速)
│   ├── init.sql             # 数据库初始化脚本
│   └── pom.xml
├── seat_client/             # 微信小程序前端代码
└── docker-compose.yml       # [核心] 容器编排文件
🚀 快速启动 (Docker 方式 - 推荐)
无需在本地安装 MySQL、Redis 或 RabbitMQ，只需安装 Docker Desktop。

1. 准备工作
   确保本地没有占用以下端口的服务（如有请停止）：3306, 6379, 5672, 8080, 5000。

2. 一键启动
   在项目根目录下打开终端（PowerShell 或 CMD），执行：

Bash

docker-compose up --build
注意：

系统会自动构建 Java 和 Python 镜像。

Java 镜像构建已内置 阿里云 Maven 镜像，下载速度极快。

首次启动可能需要 1-2 分钟，直到控制台看到 Started ZhizuoApplication。

🖥️ 功能验证与演示
启动成功后，请按以下顺序进行答辩演示：

1. 📊 查看智能监控大屏
   访问浏览器：http://localhost:8080/monitor.html

展示内容：实时预约数据、用户注册量、以及右侧炫酷的 AI 座位热度热力图。

2. 🤖 触发 AI 预测 (模拟数据流)
   为了演示数据变化，手动触发一次 AI 计算：

方式：使用 Postman 发送 POST 请求，或直接浏览器访问（需后端支持 GET）： http://localhost:8080/api/admin/demo/trigger-ai-sync

效果：刷新监控大屏，热力图颜色会发生变化，证明 Java 成功调用了 Python 服务。

3. 📱 小程序智能选座
   打开微信开发者工具导入 seat_client：

修改 config.js 中的 BASE_URL 为 http://localhost:8080/api。

进入选座页面，点击右下角 "🤖 智能推荐" 按钮。

效果：系统会提示 "AI 为您推荐了最安静的座位：xx"，并自动选中。

4. 📝 查看 API 文档
   访问：http://localhost:8080/doc.html

使用 Knife4j 生成的接口文档，专业且美观。

5. 🐰 查看消息队列
   访问：http://localhost:15672

账号/密码：guest / guest

展示：可以看到 seat-order-delay-queue 等队列，证明超时自动释放功能已就绪。

💡 主要代码修改点 (Summary)
在本次升级中，我们主要完成了以下工作：

AI 服务独立 (ai_service)：

编写 predict_server.py，暴露 /predict (热度) 和 /predict/risk (风控) 接口。

实现了基于随机特征（靠窗、周末）的模拟算法。

Java 后端增强：

AppSeatController: 集成 AI 热度数据，返回给前端。

ReservationServiceImpl:

引入 Redisson 分布式锁，防止座位超卖。

引入 AiRiskService，在预约前检查用户信用风险。

引入 ReservationProducer，发送 RabbitMQ 延迟消息。

Dockerfile 优化: 替换为 eclipse-temurin 镜像并注入阿里云配置，解决 SSL 握手失败问题。

前端交互：

小程序新增“智能推荐”悬浮按钮。

新增 Web 端 monitor.html 单页面监控看板。

❓ 常见问题
Q: 启动时报端口被占用?

A: 请先关闭本地电脑上安装的 MySQL、Redis 或 RabbitMQ 服务，Docker 需要使用这些端口。

Q: AI 热力图没有数据?

A: 请先调用一次 /api/admin/demo/trigger-ai-sync 接口生成数据，或者检查数据库 init.sql 是否执行成功。

Q: 为什么不用本地 RabbitMQ?

A: Windows 本地 RabbitMQ 经常出现 Erlang 版本冲突或端口不监听 (Connection refused)，使用 Docker 版本最稳定。