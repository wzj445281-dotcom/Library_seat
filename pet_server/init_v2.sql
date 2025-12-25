USE zhizuo;

-- ==========================================
-- 1. 资源中心表 (重构: 增加图书专业字段)
-- ==========================================
DROP TABLE IF EXISTS `resources`;
CREATE TABLE `resources` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL COMMENT '资源名称/书名',
  `type` int(2) NOT NULL COMMENT '0=图书, 1=雨伞, 2=充电宝',

  -- 图书特有字段
  `isbn` varchar(20) DEFAULT NULL COMMENT 'ISBN号',
  `author` varchar(50) DEFAULT NULL COMMENT '作者',
  `publisher` varchar(50) DEFAULT NULL COMMENT '出版社',
  `category` varchar(20) DEFAULT NULL COMMENT '分类(如: 计算机, 文学)',
  `location_code` varchar(50) DEFAULT NULL COMMENT '索书号/库位(如: A-1-05)',

  -- 库存管理
  `stock` int(11) NOT NULL DEFAULT 0 COMMENT '当前可借',
  `total_stock` int(11) NOT NULL DEFAULT 0 COMMENT '总库存',

  `img_url` varchar(255) DEFAULT NULL,
  `status` int(2) DEFAULT 1 COMMENT '1上架 0下架',
  `version` int(11) DEFAULT 0 COMMENT '乐观锁',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_type` (`type`),
  KEY `idx_name` (`name`)
) ENGINE=InnoDB COMMENT='图书馆资源中心';

-- ==========================================
-- 2. 借阅履约订单 (O2O 核心)
-- ==========================================
DROP TABLE IF EXISTS `resource_orders`;
CREATE TABLE `resource_orders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_no` varchar(32) NOT NULL COMMENT '借阅单号',
  `user_id` bigint(20) NOT NULL,
  `resource_id` bigint(20) NOT NULL,

  -- 履约信息
  `delivery_type` int(2) NOT NULL COMMENT '0=自取, 1=配送到座',
  `seat_id` bigint(20) DEFAULT NULL COMMENT '配送时的目标座位ID',

  -- 状态流转: PENDING(待履约), DELIVERING(配送中), USING(借阅中), RETURNED(已归还)
  `status` varchar(20) NOT NULL,

  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `return_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_order_no` (`order_no`),
  KEY `idx_user_status` (`user_id`, `status`)
) ENGINE=InnoDB COMMENT='借阅履约单';

-- 初始化测试数据
INSERT INTO resources (name, type, stock, total_stock, location_code, author) VALUES
('深入理解Java虚拟机', 0, 5, 5, 'TP312/J', '周志明'),
('共享充电宝(快充)', 2, 20, 20, 'Cab-01', 'Anker'),
('以及雨伞', 1, 10, 10, 'Cab-02', '天堂伞');