-- 1. 优惠券模板表 (运营配置的活动) 
CREATE TABLE `coupon` ( 
  `id` bigint(20) NOT NULL AUTO_INCREMENT, 
  `title` varchar(64) NOT NULL COMMENT '优惠券名称，如：新人专享券', 
  `amount` decimal(10,2) NOT NULL COMMENT '优惠金额', 
  `min_point` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '使用门槛，0代表无门槛', 
  `stock` int(11) NOT NULL DEFAULT '0' COMMENT '剩余库存', 
  `total_stock` int(11) NOT NULL DEFAULT '0' COMMENT '总库存', 
  `status` tinyint(4) DEFAULT '1' COMMENT '状态 1:上架 0:下架', 
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP, 
  PRIMARY KEY (`id`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='优惠券模板表'; 

-- 2. 用户领券记录表 (用户实际持有的券) 
CREATE TABLE `user_coupon` ( 
  `id` bigint(20) NOT NULL AUTO_INCREMENT, 
  `user_id` varchar(64) NOT NULL COMMENT '用户ID', 
  `coupon_id` bigint(20) NOT NULL COMMENT '关联的模板ID', 
  `order_id` bigint(20) DEFAULT NULL COMMENT '使用该券的订单ID', 
  `status` tinyint(4) DEFAULT '0' COMMENT '0:未使用 1:已使用 2:已过期', 
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP, 
  `use_time` datetime DEFAULT NULL, 
  PRIMARY KEY (`id`), 
  -- 联合索引：方便查询用户有哪些券 
  KEY `idx_user_status` (`user_id`,`status`) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户优惠券表'; 

-- 初始化一条测试数据：10元无门槛券，库存100张 
INSERT INTO `coupon` (`id`, `title`, `amount`, `min_point`, `stock`, `total_stock`, `status`) 
VALUES (1, '咖啡新人立减券', 10.00, 0.00, 100, 100, 1);