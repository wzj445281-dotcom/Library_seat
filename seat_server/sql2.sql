DROP TABLE IF EXISTS `credit_logs`;
CREATE TABLE `credit_logs` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `type` varchar(20) NOT NULL COMMENT '类型: ADD=增加, REDUCE=扣除',
  `score` int(11) NOT NULL DEFAULT 0 COMMENT '变动积分/信用分',
  `reason` varchar(255) DEFAULT NULL COMMENT '变动原因',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='积分/信用分变动日志';