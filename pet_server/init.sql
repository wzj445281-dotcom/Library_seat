-- ==========================================
-- 0. 初始化权限 (核心修复)
-- 允许 root 用户从任何 IP 远程连接 (适配 Docker 网络)
-- ==========================================
USE mysql;
CREATE USER IF NOT EXISTS 'root'@'%' IDENTIFIED BY '000000';
GRANT ALL PRIVILEGES ON *.* TO 'root'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;

-- ==========================================
-- 1. 创建数据库
-- ==========================================
CREATE DATABASE IF NOT EXISTS zhizuo DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE zhizuo;

-- 修改用户表结构
CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` varchar(20) NOT NULL COMMENT '学号',
  `name` varchar(20) NOT NULL COMMENT '姓名',
  `password` varchar(100) DEFAULT '123456' COMMENT '密码',
  `credit_score` int(11) DEFAULT '100' COMMENT '信用分',
  `role` varchar(10) DEFAULT 'STUDENT' COMMENT '角色: STUDENT/ADMIN', -- 新增字段
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_id` (`student_id`)
) ENGINE=InnoDB COMMENT='用户表';

-- 插入一个初始管理员账号
INSERT IGNORE INTO users (student_id, name, password, credit_score, role)
VALUES ('admin01', '系统管理员', '$2a$10$7R..', 100, 'ADMIN'); -- 密码需为BCrypt加密值

-- 3. 创建座位表
CREATE TABLE IF NOT EXISTS `seats` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `label` varchar(10) NOT NULL COMMENT '座位号',
  `grid_x` int(11) NOT NULL COMMENT 'X坐标',
  `grid_y` int(11) NOT NULL COMMENT 'Y坐标',
  `status` int(2) DEFAULT '1' COMMENT '1可用 0维修',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='座位表';

-- 4. 创建预约表
CREATE TABLE IF NOT EXISTS `reservation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `seat_id` bigint(20) NOT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `check_in_time` datetime DEFAULT NULL,
  `status` varchar(20) NOT NULL COMMENT '状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_seat_time` (`seat_id`, `start_time`, `end_time`)
) ENGINE=InnoDB COMMENT='预约记录';

-- 5. 座位热度统计表
CREATE TABLE IF NOT EXISTS `seat_heat_stats` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `seat_id` bigint(20) NOT NULL COMMENT '座位ID',
  `heat_score` double NOT NULL COMMENT '热度分',
  `prediction_date` date NOT NULL COMMENT '预测日期',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_seat_date` (`seat_id`, `prediction_date`)
) ENGINE=InnoDB COMMENT='座位热度AI预测表';

-- 6. 信用分变动日志表
CREATE TABLE IF NOT EXISTS `credit_logs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `type` varchar(10) NOT NULL COMMENT 'ADD/REDUCE',
  `score` int(11) NOT NULL COMMENT '变动分值',
  `reason` varchar(100) NOT NULL COMMENT '变动原因',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB COMMENT='信用分变动日志';

-- 7. 意见反馈/报修表
CREATE TABLE IF NOT EXISTS `feedback` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `content` varchar(500) NOT NULL COMMENT '反馈内容',
  `contact` varchar(50) DEFAULT NULL COMMENT '联系方式',
  `status` int(2) DEFAULT '0' COMMENT '0未处理 1已处理',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='反馈报修表';
-- ==========================================
-- 8. 智能工单表 (对应 WorkOrder.java)
-- ==========================================
CREATE TABLE IF NOT EXISTS `work_orders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ticket_no` varchar(32) NOT NULL COMMENT '工单编号',
  `user_id` bigint(20) NOT NULL,
  `category` varchar(20) NOT NULL COMMENT '分类: NOISE/REPAIR/CLEAN',
  `content` text COMMENT '工单内容',
  `snapshot_img` varchar(255) DEFAULT NULL COMMENT '现场照片',
  `priority` int(2) DEFAULT '0' COMMENT '优先级: 0低 1中 2高',
  `ai_analysis_result` varchar(500) DEFAULT NULL COMMENT 'AI分析摘要',
  `status` varchar(20) DEFAULT 'PENDING' COMMENT '状态: PENDING/PROCESSING/RESOLVED',
  `handler_id` bigint(20) DEFAULT NULL COMMENT '处理人ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_ticket_no` (`ticket_no`)
) ENGINE=InnoDB COMMENT='智能工单表';

-- ==========================================
-- 9. 物资表 (对应 Resources.java)
-- ==========================================
CREATE TABLE IF NOT EXISTS `resources` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '物资名称',
  `type` int(2) NOT NULL COMMENT '0=图书, 1=雨伞, 2=充电宝',
  `isbn` varchar(50) DEFAULT NULL,
  `author` varchar(50) DEFAULT NULL,
  `publisher` varchar(50) DEFAULT NULL,
  `category` varchar(20) DEFAULT NULL,
  `location_code` varchar(20) DEFAULT NULL COMMENT '库位',
  `stock` int(11) DEFAULT '0' COMMENT '当前库存',
  `total_stock` int(11) DEFAULT '0' COMMENT '总库存',
  `hourly_cost` double DEFAULT '0.0' COMMENT '每小时费用(积分)', -- 刚才Java代码里补加的字段
  `img_url` varchar(255) DEFAULT NULL,
  `status` int(2) DEFAULT '1' COMMENT '1上架 0下架',
  `version` int(11) DEFAULT '1' COMMENT '乐观锁版本号',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='物资资源表';

-- 插入一些初始物资数据
INSERT IGNORE INTO `resources` (name, type, stock, total_stock, hourly_cost) VALUES
('Java编程思想', 0, 5, 5, 0),
('共享充电宝', 2, 10, 10, 2.0),
('天堂伞', 1, 20, 20, 1.0);

-- ==========================================
-- 10. 物资订单表 (对应 ResourceOrder.java)
-- ==========================================
CREATE TABLE IF NOT EXISTS `resource_orders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `resource_id` bigint(20) NOT NULL,
  `seat_id` bigint(20) DEFAULT NULL COMMENT '配送时的座位ID',
  `delivery_type` int(2) DEFAULT '0' COMMENT '0=自取, 1=配送到座',
  `status` varchar(20) NOT NULL COMMENT '状态',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `start_use_time` datetime DEFAULT NULL,
  `return_time` datetime DEFAULT NULL,
  `total_cost` int(11) DEFAULT '0' COMMENT '总花费积分',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`)
) ENGINE=InnoDB COMMENT='物资流转订单';
-- 插入测试数据
INSERT IGNORE INTO users (student_id, name, credit_score) VALUES ('2021001', '张三', 100), ('2021002', '李四', 50);
INSERT IGNORE INTO seats (label, grid_x, grid_y) VALUES ('A1', 1, 1), ('A2', 1, 2), ('A3', 1, 3), ('B1', 2, 1), ('B2', 2, 2);

-- ==========================================
-- 12. 交易流水表 (新增，用于资金对账)
-- ==========================================
CREATE TABLE IF NOT EXISTS `transaction_flow` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `amount` decimal(10,2) NOT NULL COMMENT '变动金额(+/-)',
  `type` varchar(20) NOT NULL COMMENT '类型: RECHARGE(充值)/PAYMENT(支付)/REFUND(退款)',
  `order_no` varchar(64) DEFAULT NULL COMMENT '关联订单号',
  `description` varchar(255) DEFAULT NULL COMMENT '描述',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB COMMENT='交易流水表';