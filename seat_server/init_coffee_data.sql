SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 强制修改数据库字符集为 utf8mb4
ALTER DATABASE zhizuo CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

USE zhizuo;

-- ==========================================
-- 1. 商品分类表 (Categories)
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
('☕️ 畅销榜单', 100),
('💻 编程技术', 90),
('📚 文学小说', 80),
('🎓 考研教材', 70),
('🥤 搭配饮品', 60);

-- ==========================================
-- 2. 商品表 (Products) - 原 Resources 表重构
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品/书籍表';

INSERT INTO `products` (category_id, name, description, price, original_price, stock, img_url) VALUES
(1, '深入理解Java虚拟机', 'Java开发者必读经典', 89.00, 109.00, 50, 'https://img.alicdn.com/bao/uploaded/i1/198280045/O1CN016d9E5Y2E7s5Z1q2_!!0-item_pic.jpg'),
(1, '三体全集', '刘慈欣科幻巨作', 56.00, 98.00, 100, 'https://img.alicdn.com/bao/uploaded/i2/2406931838/O1CN010101012406931838_!!0-item_pic.jpg'),
(2, 'Spring Boot实战', '快速上手微服务开发', 69.90, 89.00, 30, ''),
(5, '生椰拿铁', '读书伴侣，提神醒脑', 18.00, 29.00, 999, '');

-- ==========================================
-- 3. 销售订单表 (Orders) - O2O 核心交易
-- ==========================================
DROP TABLE IF EXISTS `orders`;
CREATE TABLE `orders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) NOT NULL COMMENT '订单号',
  `user_id` bigint(20) NOT NULL,
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总额',
  `pay_status` int(2) DEFAULT 0 COMMENT '0未支付 1已支付 2已退款',
  `status` varchar(20) NOT NULL DEFAULT 'PENDING',
  `delivery_type` int(2) NOT NULL COMMENT '0=门店自取, 1=外卖配送',
  `pickup_code` varchar(10) DEFAULT NULL COMMENT '取单码 (如 C-102)',
  `address_info` varchar(255) DEFAULT NULL COMMENT '座位号或配送地址',
  `pay_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='销售订单主表';

-- ==========================================
-- 4. 订单明细表 (Order Items)
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