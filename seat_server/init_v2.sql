USE zhizuo;

-- ==========================================
-- 模块一：O2O 物资与资源表
-- ==========================================
CREATE TABLE IF NOT EXISTS `resources` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL COMMENT '物资名称',
  `type` int(2) NOT NULL COMMENT '0=图书, 1=雨伞, 2=充电宝',
  `stock` int(11) NOT NULL DEFAULT 0 COMMENT '当前库存',
  `total_stock` int(11) NOT NULL DEFAULT 0 COMMENT '总库存',
  `location` varchar(50) COMMENT '库位/书架号',
  `hourly_cost` int(11) DEFAULT 0 COMMENT '每小时消耗(积分/币)',
  `img_url` varchar(255) COMMENT '图片地址',
  `status` int(2) DEFAULT 1 COMMENT '1上架 0下架',
  `version` int(11) DEFAULT 0 COMMENT '乐观锁版本号',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='物资资源表';

CREATE TABLE IF NOT EXISTS `resource_orders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) NOT NULL COMMENT '业务订单号',
  `user_id` bigint(20) NOT NULL,
  `resource_id` bigint(20) NOT NULL,
  `seat_id` bigint(20) DEFAULT NULL COMMENT '关联座位ID(如配送到座)',
  `delivery_type` int(2) NOT NULL COMMENT '0=自取, 1=配送到座',
  `status` varchar(20) NOT NULL COMMENT 'CREATED, PICKING, DELIVERING, USING, RETURNED, PAID',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `start_use_time` datetime DEFAULT NULL COMMENT '开始使用时间',
  `return_time` datetime DEFAULT NULL COMMENT '归还时间',
  `total_cost` int(11) DEFAULT 0 COMMENT '最终消费金额',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_status` (`user_id`, `status`)
) ENGINE=InnoDB COMMENT='物资流转订单';

-- ==========================================
-- 模块二：智能工单 (替代原 Feedback)
-- ==========================================
DROP TABLE IF EXISTS `feedback`; -- 移除旧表或数据迁移
CREATE TABLE IF NOT EXISTS `work_orders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `ticket_no` varchar(32) NOT NULL COMMENT '工单编号',
  `user_id` bigint(20) NOT NULL,
  `category` varchar(20) COMMENT 'NOISE(噪音), REPAIR(报修), CLEAN(清洁), EMERGENCY(紧急)',
  `content` varchar(500),
  `snapshot_img` varchar(255) COMMENT '现场照片URL',
  `priority` int(2) DEFAULT 0 COMMENT 'AI判定优先级: 0普通 1中等 2紧急',
  `ai_analysis_result` varchar(255) COMMENT 'AI分析摘要',
  `status` varchar(20) DEFAULT 'PENDING' COMMENT 'PENDING, DISPATCHED, PROCESSING, RESOLVED',
  `handler_id` bigint(20) DEFAULT NULL COMMENT '处理人ID(管理员)',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='智能工单表';

-- ==========================================
-- 模块三：积分商城与钱包
-- ==========================================
CREATE TABLE IF NOT EXISTS `products` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(50) NOT NULL,
  `description` varchar(255),
  `price` int(11) NOT NULL COMMENT '兑换价格(积分)',
  `benefit_type` varchar(50) COMMENT '权益策略: VIP_PASS(免预约), TIME_CARD(加时卡), COFFEE(咖啡券)',
  `stock` int(11) DEFAULT 0,
  `is_virtual` tinyint(1) DEFAULT 1 COMMENT '是否虚拟商品',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB COMMENT='积分商城商品表';

CREATE TABLE IF NOT EXISTS `user_wallet` (
  `user_id` bigint(20) NOT NULL,
  `coin_balance` int(11) DEFAULT 0 COMMENT '虚拟币余额(充值获取)',
  `points_balance` int(11) DEFAULT 0 COMMENT '积分余额(行为获取)',
  `frozen_coin` int(11) DEFAULT 0 COMMENT '冻结资金(进行中订单)',
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`)
) ENGINE=InnoDB COMMENT='用户资产钱包';

-- 初始化一些数据
INSERT INTO resources (name, type, stock, hourly_cost) VALUES ('Java编程思想', 0, 5, 0), ('共享充电宝', 2, 20, 2), ('雨伞', 1, 10, 1);
INSERT INTO products (name, price, benefit_type, stock) VALUES ('免预约入馆券', 500, 'VIP_PASS', 100), ('4小时加时卡', 200, 'TIME_CARD', 999);