USE zhizuo;

-- ==========================================
-- 扩展订单表字段（支持优惠券功能）
-- ==========================================
-- 如果 orders 表已存在，执行此脚本添加新字段
-- 如果表不存在，请使用 init.sql 创建完整表结构

ALTER TABLE `orders` 
ADD COLUMN IF NOT EXISTS `coupon_id` bigint(20) DEFAULT NULL COMMENT '使用的优惠券ID (UserCoupon.id)' AFTER `address_info`,
ADD COLUMN IF NOT EXISTS `discount_amount` decimal(10,2) DEFAULT 0.00 COMMENT '优惠金额' AFTER `coupon_id`,
ADD COLUMN IF NOT EXISTS `original_amount` decimal(10,2) DEFAULT NULL COMMENT '订单原价（优惠前）' AFTER `discount_amount`;

-- 验证字段是否添加成功
-- SELECT COLUMN_NAME, DATA_TYPE, COLUMN_COMMENT 
-- FROM INFORMATION_SCHEMA.COLUMNS 
-- WHERE TABLE_SCHEMA = 'zhizuo' AND TABLE_NAME = 'orders' 
-- AND COLUMN_NAME IN ('coupon_id', 'discount_amount', 'original_amount');

