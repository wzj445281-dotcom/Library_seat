SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- 确保字符集正确
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
('☕️ 大师咖啡', 100),
('🥥 生椰家族', 90),
('🧊 瑞纳冰', 80),
('🍰 烘焙轻食', 70),
('🥤 经典饮品', 60);

-- ==========================================
-- 2. 商品表 (Products)
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

-- ✅ 重点修改：这里使用了您 assets/images 下的本地图片路径
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
-- 3. 销售订单表 (Orders)
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