📚 智座 (SmartSeat) - 高校座位智能调度系统

基于 Spring Boot + WeChat Mini Program 的高校图书馆座位预约管理系统。

Version: 2.0.0 (架构升级版)

📖 项目简介

《智座》是为了解决高校图书馆“占座难、管理难、利用率低”等痛点而设计的智能调度系统。系统采用前后端分离架构，实现了用户实名认证、可视化选座、预约签到、防占座机制以及座位热度分析等功能。

🛠 技术栈 (Tech Stack)

后端 (Server)

核心框架: Spring Boot 2.7

ORM 框架: MyBatis-Plus

数据库: MySQL 8.0

权限安全: Spring Security + JWT

高并发/缓存 (V2.0特性): Redis, Redisson (分布式锁)

接口文档: Knife4j (Swagger 3)

工具库: Hutool, Lombok

前端 (Client)

平台: 微信小程序 (Native)

UI: 原生 WXML + WXSS (Flex/Grid 布局)

交互: 实时可视化选座

🌟 核心功能

可视化选座: 还原真实图书馆布局，支持空闲/占用/维修状态展示。

智能预约: 支持指定时间段预约，通过分布式锁防止高并发下的座位超卖。

信用体系: 预约签到与违约扣分机制，规范用户行为。

热度分析: (V2.0) 基于历史数据分析座位热度，提供热门座位标识 🔥。

管理后台: 提供座位管理、用户管理及数据统计 API。

📂 目录结构

SmartSeat/
├── seat_server/         # 后端工程 (Spring Boot)
│   ├── src/main/java/   # Java 源代码
│   ├── src/main/resources/ 
│   │   ├── application.yml # 配置文件
│   │   └── mapper/      # MyBatis XML
│   └── init.sql         # 数据库初始化脚本
│
└── seat_client/         # 前端工程 (微信小程序)
    ├── pages/           # 小程序页面 (index, mine, login)
    ├── api/             # API 接口封装
    └── utils/           # 工具类


🚀 快速开始 (Quick Start)

1. 后端启动

确保已安装 MySQL 8.0 和 Redis。

在 MySQL 中创建数据库 zhizuo，并执行 seat_server/init.sql 脚本。

修改 application.yml 中的数据库账号密码及 Redis 配置。

使用 IDEA 打开 seat_server，运行 ZhizuoApplication。

访问 http://localhost:8080/doc.html 查看接口文档。

2. 前端启动

下载并安装 [微信开发者工具]。

导入 seat_client 文件夹。

修改 config/config.js 中的 baseUrl 为你的本机 IP (例如 http://127.0.0.1:8080/api)。

编译运行即可体验。

📝 开发日志

V1.0: 完成基础 CRUD，实现基本的查座、预约功能。

V2.0:

引入 Redisson 分布式锁解决抢座并发问题。

集成 Knife4j 生成在线接口文档。

优化代码结构，引入 DTO/VO 转换。

增加可视化热度图展示。

Author: [你的名字/GitHubID]
License: MIT