-- 创建数据库
CREATE DATABASE IF NOT EXISTS zhizuo DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE zhizuo;

-- 1. 用户表 (存储学号、姓名、信用分)
CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` varchar(20) NOT NULL COMMENT '学号',
  `name` varchar(20) NOT NULL COMMENT '姓名',
  `password` varchar(100) DEFAULT '123456' COMMENT '密码(实训简化)',
  `credit_score` int(11) DEFAULT '100' COMMENT '信用分',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_id` (`student_id`)
) ENGINE=InnoDB COMMENT='用户表';

-- 2. 座位表 (存储坐标、状态)
CREATE TABLE `seats` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `label` varchar(10) NOT NULL COMMENT '座位号显示(A1)',
  `grid_x` int(11) NOT NULL COMMENT 'X坐标',
  `grid_y` int(11) NOT NULL COMMENT 'Y坐标',
  `status` int(2) DEFAULT '1' COMMENT '1可用 0维修',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='座位表';

-- 3. 预约记录表 (核心流转表)
CREATE TABLE `reservation` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `seat_id` bigint(20) NOT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `check_in_time` datetime DEFAULT NULL,
  `status` varchar(20) NOT NULL COMMENT '状态: RESERVED, CHECKED_IN, COMPLETED, CANCELLED, VIOLATION',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_seat_time` (`seat_id`, `start_time`, `end_time`) -- 索引优化冲突查询
) ENGINE=InnoDB COMMENT='预约记录';

-- 初始化测试数据
INSERT INTO users (student_id, name, credit_score) VALUES ('2021001', '张三', 100), ('2021002', '李四', 50);
INSERT INTO seats (label, grid_x, grid_y) VALUES ('A1', 1, 1), ('A2', 1, 2), ('B1', 2, 1);