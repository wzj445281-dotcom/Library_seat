-- ==========================================
-- 宠物服务与用品商城系统数据库初始化脚本
-- ==========================================

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
CREATE DATABASE IF NOT EXISTS pet_mall DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE pet_mall;

-- ==========================================
-- 2. 用户表 (修改原用户表)
-- ==========================================
CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '用户名/账号',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `name` varchar(20) NOT NULL COMMENT '昵称/真实姓名',
  `password` varchar(100) DEFAULT '123456' COMMENT '密码',
  `points` int(11) DEFAULT '0' COMMENT '会员积分 (原信用分)',
  `role` varchar(10) DEFAULT 'USER' COMMENT '角色: USER/ADMIN/DOCTOR',
  `balance` decimal(10,2) DEFAULT '0.00' COMMENT '钱包余额',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB COMMENT='用户/会员表';

-- 插入初始管理员和宠物医生
INSERT IGNORE INTO users (username, name, password, points, role) VALUES  
('admin', '系统管理员', '$2a$10$7R..', 9999, 'ADMIN'), 
('doctor_wang', '王医生', '$2a$10$7R..', 100, 'DOCTOR');

-- ==========================================
-- 3. 服务工位表 (原 Seats 改为 ServiceSlot)
-- 对应实体: ServiceSlot (洗澡位、美容台、诊室)
-- ==========================================
CREATE TABLE IF NOT EXISTS `service_slots` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '工位名称 (如: 洗护1号位)',
  `service_type` varchar(20) NOT NULL COMMENT '类型: BATH(洗澡), GROOM(美容), MEDICAL(医疗)',
  `price` decimal(10, 2) DEFAULT '0.00' COMMENT '该工位的基础服务费',
  `status` int(2) DEFAULT '1' COMMENT '1可用 0维护中',
  `description` varchar(255) DEFAULT NULL COMMENT '工位描述',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='服务工位表';

-- 插入初始工位数据
INSERT IGNORE INTO `service_slots` (name, service_type, price) VALUES  
('洗护区-01', 'BATH', 50.00), 
('洗护区-02', 'BATH', 50.00), 
('美容台-A', 'GROOM', 120.00), 
('诊疗室-VIP', 'MEDICAL', 200.00);

-- ==========================================
-- 4. 商品表 (原 Resources 改为 Products)
-- 核心卖点: 狗粮、猫砂、玩具
-- ==========================================
CREATE TABLE IF NOT EXISTS `products` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '商品名称',
  `category` varchar(50) NOT NULL COMMENT '分类: FOOD(主粮), SNACK(零食), TOY(玩具), MEDICINE(药品)',
  `price` decimal(10, 2) NOT NULL COMMENT '销售价格',
  `stock` int(11) DEFAULT '0' COMMENT '当前库存',
  `sales` int(11) DEFAULT '0' COMMENT '累计销量',
  `img_url` varchar(255) DEFAULT NULL COMMENT '商品图片',
  `detail` text COMMENT '商品详情描述',
  `status` int(2) DEFAULT '1' COMMENT '1上架 0下架',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='宠物商品表';

-- 插入初始商品
INSERT IGNORE INTO `products` (name, category, price, stock) VALUES 
('皇家幼犬粮 10kg', 'FOOD', 320.00, 50), 
('PIDAN 混合猫砂', 'CLEAN', 29.90, 200), 
('宠物洁齿骨', 'SNACK', 15.00, 100);

-- ==========================================
-- 5. 服务预约记录 (原 Reservation 改为 ServiceBooking)
-- ==========================================
CREATE TABLE IF NOT EXISTS `service_bookings` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `booking_no` varchar(32) NOT NULL COMMENT '预约单号',
  `user_id` bigint(20) NOT NULL,
  `slot_id` bigint(20) NOT NULL COMMENT '预约的工位ID',
  `pet_name` varchar(50) DEFAULT NULL COMMENT '宠物昵称',
  `pet_type` varchar(20) DEFAULT NULL COMMENT '宠物种类 (Dog/Cat)',
  `appointment_time` datetime NOT NULL COMMENT '预约时间',
  `duration_minutes` int(11) DEFAULT 60 COMMENT '预计耗时(分钟)',
  `status` varchar(20) NOT NULL COMMENT '状态: PENDING, CONFIRMED, COMPLETED, CANCELLED',
  `total_price` decimal(10, 2) DEFAULT '0.00' COMMENT '服务费用',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_booking_no` (`booking_no`),
  KEY `idx_slot_time` (`slot_id`, `appointment_time`)
) ENGINE=InnoDB COMMENT='服务预约订单';

-- ==========================================
-- 6. 商品订单表 (原 ResourceOrder 改为 ProductOrder)
-- ==========================================
CREATE TABLE IF NOT EXISTS `product_orders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) NOT NULL COMMENT '订单编号',
  `user_id` bigint(20) NOT NULL,
  `total_amount` decimal(10, 2) NOT NULL COMMENT '订单总金额',
  `status` varchar(20) NOT NULL COMMENT '状态: UNPAID, PAID, SHIPPED, COMPLETED',
  `address` varchar(255) DEFAULT NULL COMMENT '收货地址',
  `pay_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`)
) ENGINE=InnoDB COMMENT='商品销售订单';

-- ==========================================
-- 7. 订单明细表 (新增，用于记录一单买了什么)
-- ==========================================
CREATE TABLE IF NOT EXISTS `order_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NOT NULL COMMENT '关联订单ID',
  `product_id` bigint(20) NOT NULL,
  `product_name` varchar(100) NOT NULL,
  `quantity` int(11) NOT NULL COMMENT '购买数量',
  `price` decimal(10, 2) NOT NULL COMMENT '购买时的单价',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB COMMENT='订单详情表';

-- ==========================================
-- 8. 宠物健康档案 (原 work_orders 改造)
-- 用于 AI 医生建议记录 或 线下就诊记录
-- ==========================================
CREATE TABLE IF NOT EXISTS `pet_health_records` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `pet_name` varchar(50) DEFAULT NULL COMMENT '宠物名称',
  `symptoms` text COMMENT '症状描述',
  `diagnosis` text COMMENT 'AI医生诊断结果',
  `prescription` text COMMENT '处方/建议',
  `doctor_id` bigint(20) DEFAULT NULL COMMENT '接诊医生ID',
  `record_date` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '就诊日期',
  `status` varchar(20) DEFAULT 'ACTIVE' COMMENT '状态',
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB COMMENT='宠物健康档案';

-- ==========================================
-- 9. 意见反馈/报修表 (保持不变)
-- ==========================================
CREATE TABLE IF NOT EXISTS `feedback` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `content` varchar(500) NOT NULL COMMENT '反馈内容',
  `contact` varchar(50) DEFAULT NULL COMMENT '联系方式',
  `status` int(2) DEFAULT '0' COMMENT '0未处理 1已处理',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='反馈报修表';