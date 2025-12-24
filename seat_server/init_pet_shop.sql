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
CREATE DATABASE IF NOT EXISTS pet_shop DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_general_ci;
USE pet_shop;

-- ==========================================
-- 2. 用户表 (修改原用户表)
-- ==========================================
CREATE TABLE IF NOT EXISTS `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `student_id` varchar(20) NOT NULL COMMENT '学号',
  `name` varchar(20) NOT NULL COMMENT '姓名',
  `password` varchar(100) DEFAULT '123456' COMMENT '密码',
  `points` int(11) DEFAULT '100' COMMENT '会员积分',
  `role` varchar(10) DEFAULT 'USER' COMMENT '角色: USER/ADMIN',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_student_id` (`student_id`)
) ENGINE=InnoDB COMMENT='用户表';

-- 插入一个初始管理员账号
INSERT IGNORE INTO users (student_id, name, password, points, role)
VALUES ('admin01', '系统管理员', '$2a$10$7R..', 100, 'ADMIN');

-- ==========================================
-- 3. 商品表 (原物资表修改)
-- ==========================================
CREATE TABLE IF NOT EXISTS `products` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '商品名称',
  `price` decimal(10,2) NOT NULL COMMENT '售价',
  `stock` int(11) NOT NULL DEFAULT '0' COMMENT '库存',
  `category` int(2) NOT NULL COMMENT '0=主粮, 1=零食, 2=玩具, 3=医疗',
  `image_url` varchar(255) DEFAULT NULL COMMENT '商品图片',
  `description` text COMMENT '商品详情',
  `status` int(2) DEFAULT '1' COMMENT '1上架 0下架',
  `version` int(11) DEFAULT '1' COMMENT '乐观锁版本号',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='商品表';

-- 插入一些初始商品数据
INSERT IGNORE INTO `products` (name, price, stock, category, description) VALUES
('皇家狗粮成犬专用', 89.90, 50, 0, '适合成犬的均衡营养狗粮，富含蛋白质和维生素'),
('猫条美味零食', 15.50, 100, 1, '猫咪最爱零食，多种口味可选'),
('互动逗猫棒', 25.80, 30, 2, '增强与猫咪互动的趣味玩具'),
('宠物驱虫药', 45.00, 40, 3, '体内外驱虫，安全有效');

-- ==========================================
-- 4. 服务工位表 (原座位表修改)
-- ==========================================
CREATE TABLE IF NOT EXISTS `service_station` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `label` varchar(10) NOT NULL COMMENT '工位号',
  `grid_x` int(11) NOT NULL COMMENT 'X坐标',
  `grid_y` int(11) NOT NULL COMMENT 'Y坐标',
  `service_type` int(2) NOT NULL COMMENT '1=洗澡位, 2=美容台, 3=诊疗室',
  `base_price` decimal(10,2) NOT NULL COMMENT '基础服务费',
  `status` int(2) DEFAULT '1' COMMENT '1可用 0维修',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='服务工位表';

-- 插入一些初始工位数据
INSERT IGNORE INTO `service_station` (label, grid_x, grid_y, service_type, base_price) VALUES
('W1', 1, 1, 1, 30.00),
('W2', 1, 2, 1, 30.00),
('G1', 2, 1, 2, 50.00),
('G2', 2, 2, 2, 50.00),
('M1', 3, 1, 3, 80.00);

-- ==========================================
-- 5. 宠物表 (新增)
-- ==========================================
CREATE TABLE IF NOT EXISTS `pet` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '关联用户',
  `name` varchar(20) NOT NULL COMMENT '宠物名',
  `type` int(2) NOT NULL COMMENT '0=猫, 1=狗',
  `weight` decimal(5,2) NOT NULL COMMENT '体重(用于计算洗澡价格)',
  `birthday` date DEFAULT NULL COMMENT '生日',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB COMMENT='宠物表';

-- ==========================================
-- 6. 商城订单表 (原物资订单表修改)
-- ==========================================
CREATE TABLE IF NOT EXISTS `shop_order` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `quantity` int(11) NOT NULL DEFAULT '1' COMMENT '购买数量',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `status` int(2) NOT NULL DEFAULT '0' COMMENT '0=待支付, 1=已支付, 2=已发货, 3=已完成, 4=已取消',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `pay_time` datetime DEFAULT NULL COMMENT '支付时间',
  `ship_time` datetime DEFAULT NULL COMMENT '发货时间',
  `complete_time` datetime DEFAULT NULL COMMENT '完成时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`)
) ENGINE=InnoDB COMMENT='商城订单表';

-- ==========================================
-- 7. 服务预约表 (原预约表修改)
-- ==========================================
CREATE TABLE IF NOT EXISTS `service_appointment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `pet_id` bigint(20) NOT NULL COMMENT '关联宠物',
  `station_id` bigint(20) NOT NULL,
  `start_time` datetime NOT NULL,
  `end_time` datetime NOT NULL,
  `check_in_time` datetime DEFAULT NULL,
  `status` varchar(20) NOT NULL COMMENT '状态',
  `total_price` decimal(10,2) DEFAULT NULL COMMENT '服务总价',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_station_time` (`station_id`, `start_time`, `end_time`)
) ENGINE=InnoDB COMMENT='服务预约记录';

-- ==========================================
-- 8. 服务工位繁忙度统计表 (原座位热度统计表修改)
-- ==========================================
CREATE TABLE IF NOT EXISTS `station_busy_stats` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `station_id` bigint(20) NOT NULL COMMENT '工位ID',
  `busy_score` double NOT NULL COMMENT '繁忙度分',
  `prediction_date` date NOT NULL COMMENT '预测日期',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_station_date` (`station_id`, `prediction_date`)
) ENGINE=InnoDB COMMENT='服务工位繁忙度AI预测表';

-- ==========================================
-- 9. 积分变动日志表 (原信用分变动日志表修改)
-- ==========================================
CREATE TABLE IF NOT EXISTS `point_logs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `type` varchar(10) NOT NULL COMMENT 'ADD/REDUCE',
  `points` int(11) NOT NULL COMMENT '变动分值',
  `reason` varchar(100) NOT NULL COMMENT '变动原因',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB COMMENT='积分变动日志';

-- ==========================================
-- 10. 意见反馈/报修表 (保持不变)
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

-- ==========================================
-- 11. 智能工单表 (保持不变)
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

-- 插入测试数据
INSERT IGNORE INTO users (student_id, name, points) VALUES ('2021001', '张三', 100), ('2021002', '李四', 50);
INSERT IGNORE INTO pet (user_id, name, type, weight, birthday) VALUES
(1, '小黄', 1, 8.5, '2022-05-15'),
(1, '咪咪', 0, 4.2, '2023-02-20'),
(2, '旺财', 1, 15.0, '2021-11-10');