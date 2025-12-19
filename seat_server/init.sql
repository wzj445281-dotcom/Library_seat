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

-- 插入测试数据
INSERT IGNORE INTO users (student_id, name, credit_score) VALUES ('2021001', '张三', 100), ('2021002', '李四', 50);
INSERT IGNORE INTO seats (label, grid_x, grid_y) VALUES ('A1', 1, 1), ('A2', 1, 2), ('A3', 1, 3), ('B1', 2, 1), ('B2', 2, 2);