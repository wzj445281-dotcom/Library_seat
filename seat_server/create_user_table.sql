USE zhizuo;

-- ==========================================
-- 用户表
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
) ENGINE=InnoDB COMMENT='用户表';

-- 插入测试用户
INSERT INTO `user` (username, name, password, role) VALUES
('admin', '管理员', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'ADMIN'),
('test001', '测试用户', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 'USER');

-- 密码是 123456 (BCrypt加密)