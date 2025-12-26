SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 确保数据库存在并使用正确的字符集
CREATE DATABASE IF NOT EXISTS `zhizuo` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `zhizuo`;

-- ==========================================
-- 1. 用户表 (User)
-- ==========================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `username` varchar(50) NOT NULL COMMENT '用户名/学号',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `name` varchar(50) NOT NULL COMMENT '真实姓名',
  `password` varchar(255) NOT NULL COMMENT '密码',
  `points` int(11) DEFAULT 0 COMMENT '积分',
  `role` varchar(20) DEFAULT 'USER' COMMENT '角色',
  `balance` decimal(10,2) DEFAULT 0.00 COMMENT '余额',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 初始化测试用户 (密码: 123456)
INSERT INTO `user` (username, name, password, role) VALUES
('admin', '管理员', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ADMIN'),
('test001', '测试用户', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'USER');

-- ==========================================
-- 2. 用户收货地址表 (User Address)
-- ==========================================
DROP TABLE IF EXISTS `user_address`;
CREATE TABLE `user_address` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `name` varchar(50) NOT NULL COMMENT '收货人姓名',
  `phone` varchar(20) NOT NULL COMMENT '收货人电话',
  `province` varchar(50) DEFAULT NULL COMMENT '省份',
  `city` varchar(50) DEFAULT NULL COMMENT '城市',
  `district` varchar(50) DEFAULT NULL COMMENT '区县',
  `detail` varchar(255) NOT NULL COMMENT '详细地址',
  `is_default` tinyint(1) DEFAULT 0 COMMENT '是否默认地址 0:否 1:是',
  `latitude` decimal(10,7) DEFAULT NULL COMMENT '纬度',
  `longitude` decimal(10,7) DEFAULT NULL COMMENT '经度',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_is_default` (`user_id`, `is_default`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收货地址表';

-- ==========================================
-- 3. 优惠券模板表 (Coupon)
-- ==========================================
DROP TABLE IF EXISTS `coupon`;
CREATE TABLE `coupon` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `title` varchar(64) NOT NULL COMMENT '优惠券名称',
  `amount` decimal(10,2) NOT NULL COMMENT '优惠金额',
  `min_point` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '使用门槛，0代表无门槛',
  `stock` int(11) NOT NULL DEFAULT '0' COMMENT '剩余库存',
  `total_stock` int(11) NOT NULL DEFAULT '0' COMMENT '总库存',
  `status` tinyint(4) DEFAULT '1' COMMENT '状态 1:上架 0:下架',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券模板表';

INSERT INTO `coupon` (`id`, `title`, `amount`, `min_point`, `stock`, `total_stock`, `status`)
VALUES (1, '咖啡新人立减券', 10.00, 0.00, 100, 100, 1);

-- ==========================================
-- 4. 用户领券记录表 (User Coupon)
-- ==========================================
DROP TABLE IF EXISTS `user_coupon`;
CREATE TABLE `user_coupon` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `coupon_id` bigint(20) NOT NULL COMMENT '关联的模板ID',
  `order_id` bigint(20) DEFAULT NULL COMMENT '使用该券的订单ID',
  `status` tinyint(4) DEFAULT '0' COMMENT '0:未使用 1:已使用 2:已过期',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `use_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_user_status` (`user_id`,`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表';

-- ==========================================
-- 5. 商品分类表 (Categories)
-- ==========================================
DROP TABLE IF EXISTS `categories`;
CREATE TABLE `categories` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '分类名称',
  `sort` int(11) DEFAULT '0' COMMENT '排序优先级',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

INSERT INTO `categories` (name, sort) VALUES
('☕️ 大师咖啡', 100),
('🥥 生椰家族', 90),
('🧊 瑞纳冰', 80),
('🍰 烘焙轻食', 70),
('🥤 经典饮品', 60);

-- ==========================================
-- 6. 商品表 (Products)
-- ==========================================
DROP TABLE IF EXISTS `products`;
CREATE TABLE `products` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `category_id` bigint(20) NOT NULL COMMENT '关联分类ID',
  `name` varchar(100) NOT NULL COMMENT '商品名称',
  `description` varchar(255) DEFAULT NULL COMMENT '简短描述',
  `price` decimal(10,2) NOT NULL COMMENT '售价',
  `original_price` decimal(10,2) DEFAULT NULL COMMENT '原价/划线价',
  `stock` int(11) NOT NULL DEFAULT 0 COMMENT '库存',
  `sales` int(11) DEFAULT 0 COMMENT '销量',
  `img_url` varchar(255) DEFAULT NULL COMMENT '封面图',
  `status` int(2) DEFAULT 1 COMMENT '1上架 0下架',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_category` (`category_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

INSERT INTO `products` (category_id, name, description, price, original_price, stock, img_url) VALUES
-- 1. 大师咖啡
(1, '标准美式', '经典风味，提神醒脑', 13.00, 26.00, 999, '/assets/images/american.jpg'),
(1, '拿铁', '奶香浓郁，口感丝滑', 16.00, 29.00, 999, '/assets/images/latte.jpg'),
(1, '卡布奇诺', '绵密奶泡，丰富层次', 18.00, 31.00, 999, '/assets/images/cappuccino.jpg'),
-- 2. 生椰家族
(2, '生椰拿铁', 'YYDS，冷榨生椰浆', 18.00, 29.00, 999, '/assets/images/coconut_latte.jpg'),
(2, '椰云拿铁', '口感如云朵般绵密', 19.00, 32.00, 999, '/assets/images/coconut_cloud.jpg'),
-- 3. 瑞纳冰
(3, '抹茶瑞纳冰', '清新抹茶，冰爽夏日', 20.00, 35.00, 999, '/assets/images/matcha_ice.jpg'),
(3, '巧克力瑞纳冰', '浓郁巧克力，甜蜜享受', 20.00, 35.00, 999, '/assets/images/choco_ice.jpg');

-- ==========================================
-- 7. 用户收藏表 (User Favorite)
-- ==========================================
DROP TABLE IF EXISTS `user_favorite`;
CREATE TABLE `user_favorite` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` varchar(64) NOT NULL COMMENT '用户ID',
  `product_id` bigint(20) NOT NULL COMMENT '商品ID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_product` (`user_id`,`product_id`) COMMENT '防止重复收藏'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户收藏表';

-- ==========================================
-- 8. 销售订单表 (Orders)
-- ==========================================
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) NOT NULL COMMENT '订单号',
  `user_id` bigint(20) NOT NULL,
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总额(实付)',
  `pay_status` int(2) DEFAULT 0 COMMENT '0未支付 1已支付 2已退款',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING' COMMENT 'PENDING, PAID, READY, COMPLETED',
  `delivery_type` int(2) NOT NULL COMMENT '0=门店自取, 1=外卖配送',
  `pickup_code` varchar(10) DEFAULT NULL COMMENT '取单码 (如 C-102)',
  `address_info` varchar(255) DEFAULT NULL COMMENT '自取地址或配送地址',

  -- 优惠券相关字段
  `coupon_id` bigint(20) DEFAULT NULL COMMENT '使用的优惠券ID',
  `discount_amount` decimal(10,2) DEFAULT 0.00 COMMENT '优惠金额',
  `original_amount` decimal(10,2) DEFAULT NULL COMMENT '订单原价',

  `pay_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售订单主表';

-- ==========================================
-- 9. 订单明细表 (Order Items)
-- ==========================================
DROP TABLE IF EXISTS `order_items`;
CREATE TABLE `order_items` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_id` bigint(20) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `product_name` varchar(100) NOT NULL,
  `price` decimal(10,2) NOT NULL COMMENT '购买时单价',
  `quantity` int(11) NOT NULL COMMENT '数量',
  PRIMARY KEY (`id`),
  KEY `idx_order_id` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

SET FOREIGN_KEY_CHECKS = 1;