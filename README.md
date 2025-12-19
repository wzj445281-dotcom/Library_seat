🎓 智座 (SmartSeat) - 智能图书馆空间管理系统 V2.0

不仅仅是预约，更是智能空间的决策者。
基于 Spring Boot + Python AI + 微服务架构的企业级解决方案。

📖 项目简介

智座 (SmartSeat) 是一套针对高校图书馆/自习室场景设计的智能管理系统。V2.0 版本在传统预约功能的基础上，进行了深度的架构升级与功能扩展。系统引入了 Python AI 服务进行大数据分析与风控，采用 Docker 全栈容器化部署，并实现了基于积分体系的商业闭环（资源兑换）。

它解决了传统选座系统的三大痛点："盲目选座"（不知哪里人少）、"恶意占座"（缺乏信用约束）以及 "系统崩溃"（早八点高并发）。

🏗 系统架构

采用 前后端分离 + 微服务协作 的架构设计：

前端交互层:

📱 微信小程序 (Native): 提供极简的用户预约、签到、AI对话体验。

💻 Web 管理大屏 (Vue/HTML): 炫酷的深色模式数据可视化大屏，实时监控座席状态与热力图。

后端服务层:

🛡️ Java 核心服务 (Spring Boot 2.7): 承载业务逻辑、用户鉴权、订单处理。

🧠 Python AI 服务 (Flask): 独立微服务，负责座位热度预测、用户信用风险评估。

中间件层:

MySQL 8.0: 业务数据持久化。

Redis + Redisson: 缓存热点数据，提供分布式锁解决超卖问题。

RabbitMQ: 利用死信队列（DLX）实现订单超时自动释放，削峰填谷。

运维层:

Docker Compose: 一键编排所有服务，环境零依赖。

✨ 核心功能亮点

1. 🤖 AI 赋能的智能体验

智能选座推荐: Python 服务分析历史数据，生成“座位热度图”，为用户推荐最安静或最热门的区域。

用户风控模型: 预约前通过 AI 评估用户历史违约记录，对高风险用户进行拦截或限制预约时长。

AI 智能助手: 小程序内置 AI 聊天机器人（集成 Google Gemini/大模型接口），回答用户关于图书馆规则的提问。

2. ⚡ 高并发稳健设计

分布式锁防超卖: 在抢座核心逻辑中使用 Redisson 分布式锁，确保同一座位在同一秒只能被一人抢到。

自动释放机制: 基于 RabbitMQ 延迟队列，用户预约后若未在规定时间内签到，系统自动释放座位，杜绝资源浪费。

3. 💳 完整的信用与激励闭环

信用分体系: 违约扣分，签到加分。信用分过低将限制功能。

资源/积分商城: 独有的商业逻辑，用户可用积分兑换“时间卡”或“优先选座权”，不仅是管理工具，更具运营价值。

工单/反馈系统: 用户可提交设施报修工单，管理员后台处理，形成服务闭环。

4. 📊 驾驶舱级监控大屏

实时动态: 管理员可通过 Web 页面实时查看当前入馆人数、座位占用率。

热力图可视化: 直观展示场馆内的人流密集区域。

🛠 技术栈清单

模块

技术选型

说明

后端框架

Spring Boot 2.7, MyBatis-Plus

快速开发，ORM 增强

AI 服务

Python 3.9, Flask/FastAPI, Scikit-learn

机器学习与接口服务

前端技术

微信小程序原生, HTML5, ECharts

多端交互与数据可视化

数据库

MySQL 8.0

关系型数据存储

缓存/锁

Redis, Redisson

高并发保障

消息队列

RabbitMQ

异步解耦，延迟任务

API 文档

Knife4j (Swagger 增强版)

接口文档自动化生成

部署容器

Docker, Docker Compose

容器化编排

安全框架

Spring Security + JWT

无状态认证鉴权

📂 目录结构说明

Library_seat-test2.0/
├── ai_service/             # 🧠 Python AI 预测服务
│   ├── predict_server.py   # 核心预测算法与 API
│   └── Dockerfile          # Python 环境构建
├── seat_server/            # 🛡️ Java 后端核心服务
│   ├── src/main/java/      # 业务代码 (Controller/Service/Dao)
│   ├── src/main/resources/
│   │   ├── static/         # 📊 Web 监控大屏 (monitor.html, admin.html)
│   │   └── init.sql        # 数据库初始化脚本
│   └── Dockerfile          # Java 环境构建
├── seat_client/            # 📱 微信小程序源码
│   ├── pages/              # 页面 (index, mine, ai_chat...)
│   └── utils/              # 工具类
├── docker-compose.yml      # 🐳 容器编排文件 (核心)
└── .env                    # 🔐 环境变量配置文件 (需自行创建)


🚀 快速启动指南

1. 环境准备

确保本地已安装 Docker Desktop。

2. 配置环境变量

在根目录下新建 .env 文件（参考 .env.example 或文档），配置数据库密码及 API Key。

3. 一键启动

在终端执行：

docker-compose up --build


系统将自动完成以下步骤：

启动 MySQL 并初始化表结构 (init.sql)。

启动 Redis 和 RabbitMQ。

构建并启动 Python AI 服务。

构建并启动 Java 后端服务。

4. 访问服务

Web 监控大屏: http://localhost:8080/monitor.html

API 文档: http://localhost:8080/doc.html

小程序调试: 使用“微信开发者工具”导入 seat_client 目录。

📸 功能截图

(此处可预留位置放置小程序截图、监控大屏截图)

📝 开发者

Full Stack Developer: [你的名字]
Focus: Microservices, AI Integration, High Concurrency System Design.