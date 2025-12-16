智座 (Zhizuo) - 高校图书馆可视化座位预约系统

一个基于 Spring Boot + MyBatis Plus + 微信小程序的图书馆座位资源实时调度系统。

📖 项目简介

智座 旨在解决高校图书馆座位资源紧张、占座现象严重的问题。系统采用 前后端分离 架构，通过 可视化网格选座、信用分奖惩机制 以及 自动化冲突检测，实现座位资源的高效利用和公平分配。

目前版本已完成核心业务闭环，包括用户认证、选座预约、签到核销、违规自动扣分及管理后台接口。

✨ 核心功能

📱 用户端 (微信小程序)

安全认证：支持学号/密码登录、注册，采用 JWT + Spring Security 进行无状态认证。

可视化选座：基于 CSS Grid 的动态网格布局，直观展示座位状态（空闲、占用、维修、已选）。

预约管理：

支持自定义时段预约。

冲突检测：毫秒级并发冲突检测，防止超卖。

我的预约：查看历史记录，支持手动签到和取消预约。

信用中心：展示个人信用分及用户信息。

🛡️ 管理端 (后端 API 已就绪)

数据大屏：统计总用户数、今日预约量、座位利用率及高风险用户（低信用）监控。

用户管理：分页查询学生列表（支持学号搜索），信用分重置（处理申诉）。

座位管理：批量一键生成座位、单个座位状态控制（开启/维修）。

⚙️ 核心机制 (后端逻辑)

信用奖惩体系：

正常履约：签到成功 +1 分（上限 110）。

违规爽约：迟到 15 分钟未签到，系统自动标记违约并 -5 分。

临时取消：距离开始不足 30 分钟取消，扣 2 分。

门槛限制：信用分 < 60 分将被禁止预约。

自动化任务：集成 Spring Task，每分钟自动扫描违约订单。

🛠 技术栈

后端 (Server)

核心框架：Spring Boot 2.7

ORM 框架：MyBatis Plus 3.5 (极大简化 CRUD)

安全框架：Spring Security + JWT (无状态鉴权)

数据库：MySQL 8.0 / 5.7

工具库：Lombok, Hutool (可选)

前端 (Client)

框架：微信小程序原生开发 (Native)

架构：组件化设计，封装统一的 Request 网络请求拦截器（自动携带 Token、处理 403）。

样式：Flex + Grid 布局，极简扁平化 UI。

📂 项目结构

本项目采用 DDD (领域驱动设计) 分层思想进行重构：

src/main/java/com/example/zhizuo
├── ZhizuoApplication.java       // 启动类 (开启定时任务、Mapper扫描)
├── common                       // [公共模块]
│   ├── ApiResponse.java         // 统一响应体
│   └── util/JwtUtil.java        // JWT 工具类
├── config                       // [配置模块]
│   ├── SecurityConfig.java      // 安全链配置 (放行登录接口)
│   ├── JwtFilter.java           // Token 过滤器
│   └── MybatisPlusConfig.java   // 分页插件配置
├── core                         // [核心业务层] (心脏)
│   ├── entity                   // 数据库实体 (User, Seat, Reservation)
│   ├── mapper                   // 数据访问层
│   └── service                  // 业务逻辑层 (含事务控制、定时任务)
└── api                          // [接口层] (对外暴露)
    ├── admin                    // -> 管理后台专用接口
    └── app                      // -> 微信小程序专用接口


🚀 快速开始

1. 环境准备

JDK 1.8+

MySQL 5.7+

Maven 3.6+

微信开发者工具

2. 数据库设置

创建数据库 zhizuo 并运行以下 SQL 初始化表结构：

CREATE DATABASE IF NOT EXISTS zhizuo DEFAULT CHARSET utf8mb4;
USE zhizuo;

-- 用户表
CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` varchar(20) NOT NULL UNIQUE COMMENT '学号',
  `name` varchar(20) NOT NULL,
  `password` varchar(100) NOT NULL COMMENT '加密密码',
  `credit_score` int(11) DEFAULT '100',
  PRIMARY KEY (`id`)
);

-- 座位表
CREATE TABLE `seats` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `label` varchar(10) NOT NULL,
  `grid_x` int(11) NOT NULL,
  `grid_y` int(11) NOT NULL,
  `status` int(2) DEFAULT '1' COMMENT '1可用 0维修',
  PRIMARY KEY (`id`)
);

-- 预约表
CREATE TABLE `reservation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `seat_id` bigint(20) NOT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `check_in_time` datetime DEFAULT NULL,
  `status` varchar(20) NOT NULL COMMENT 'RESERVED, CHECKED_IN, CANCELLED, COMPLETED, VIOLATION',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
);


3. 后端启动

修改 src/main/resources/application.yml 中的数据库账号密码。

运行 ZhizuoApplication.main()。

后端默认运行在 http://localhost:8080。

4. 前端启动

打开微信开发者工具，导入 seat-client 目录。

修改 config/config.js 中的 BASE_URL 为你后端的 IP 地址。

编译运行，使用测试账号注册并登录。

🔮 未来规划

[ ] 高并发优化：引入 Redis + Redisson 分布式锁解决抢座超卖问题。

[ ] AI 助手：接入 DeepSeek 大模型，实现自然语言智能选座。

[ ] Web 管理后台：基于 Vue + ElementUI 开发可视化的管理前端。

[ ] 消息推送：接入微信订阅消息，实现签到提醒。

📄 许可证

MIT License