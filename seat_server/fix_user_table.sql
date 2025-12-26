-- ==========================================
-- 修复 user 表结构：添加缺失的 name 字段
-- ==========================================
USE zhizuo;

-- 方案1：直接添加 name 字段（推荐）
-- 注意：如果字段已存在会报错，可以忽略错误继续执行
ALTER TABLE `user` ADD COLUMN `name` varchar(50) DEFAULT NULL COMMENT '真实姓名' AFTER `phone`;

-- 如果上面的语句报错说字段已存在，说明字段已经存在，可以跳过
-- 如果报错说语法错误，可能是 MySQL 版本问题，使用下面的方式：

-- 方案2：如果表中有 nickname 字段但没有 name 字段，先添加 name 字段，然后迁移数据
-- ALTER TABLE `user` ADD COLUMN `name` varchar(50) DEFAULT NULL COMMENT '真实姓名' AFTER `phone`;
-- UPDATE `user` SET `name` = `nickname` WHERE `name` IS NULL AND `nickname` IS NOT NULL;

-- 验证表结构（执行后查看结果）
-- DESCRIBE `user`;

