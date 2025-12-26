USE zhizuo;

SET NAMES utf8mb4;

-- =============================================
-- 瑞幸咖啡数据初始化 SQL
-- 根据实际表结构调整
-- =============================================

-- =============================================
-- 1. 确保 user 表有账号密码字段
-- =============================================
-- 如果字段已存在，这些语句会报错，可忽略
ALTER TABLE `user` 
  MODIFY COLUMN `username` varchar(50) NOT NULL COMMENT '用户名/学号',
  MODIFY COLUMN `password` varchar(255) NOT NULL COMMENT '密码';

-- =============================================
-- 2. 清理旧测试数据 (可选，避免主键冲突)
-- =============================================
-- 注意：执行前请备份数据！
-- TRUNCATE TABLE `products`;
-- TRUNCATE TABLE `categories`;
-- TRUNCATE TABLE `seat`;
-- TRUNCATE TABLE `coupon`;
-- TRUNCATE TABLE `user_coupon`;
-- DELETE FROM `user` WHERE id IN (1, 2);

-- =============================================
-- 3. 插入测试用户 (包含普通用户和管理员)
-- =============================================
-- 注意：密码使用 BCrypt 加密，这里使用明文 123456 的加密值
-- 如果需要修改密码，请使用 PasswordEncoder 生成新的加密值
INSERT INTO `user` (`id`, `username`, `nickname`, `phone`, `password`, `balance`, `credits`, `role`, `create_time`) VALUES
(1, 'admin', '测试管理员', '13800138000', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 9999.00, 1000, 'ADMIN', NOW()),
(2, 'user', '瑞幸路人甲', '13900139000', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 100.00, 100, 'USER', NOW())
ON DUPLICATE KEY UPDATE 
  `username` = VALUES(`username`),
  `nickname` = VALUES(`nickname`),
  `phone` = VALUES(`phone`),
  `password` = VALUES(`password`),
  `balance` = VALUES(`balance`),
  `credits` = VALUES(`credits`),
  `role` = VALUES(`role`);

-- 密码说明：$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi 对应明文密码 "123456"

-- =============================================
-- 4. 插入商品分类
-- =============================================
-- 清空并重新插入分类数据
TRUNCATE TABLE `categories`;

INSERT INTO `categories` (`id`, `name`, `sort`, `create_time`) VALUES
(1, '大师咖啡', 100, NOW()),
(2, '生椰家族', 90, NOW()),
(3, '瑞纳冰', 80, NOW()),
(4, '经典甜点', 70, NOW());

-- =============================================
-- 5. 插入商品数据 (关联分类ID)
-- =============================================
-- 清空并重新插入商品数据
TRUNCATE TABLE `products`;

INSERT INTO `products` (`id`, `category_id`, `name`, `description`, `img_url`, `price`, `original_price`, `stock`, `sales`, `status`, `create_time`) VALUES
-- 大师咖啡
(1, 1, '标准美式', '经典意式浓缩，水与咖啡的黄金比例', '/static/images/product/american.jpg', 13.00, 26.00, 999, 0, 1, NOW()),
(2, 1, '拿铁', '浓缩咖啡与香醇牛奶的经典融合', '/static/images/product/latte.jpg', 16.00, 29.00, 999, 0, 1, NOW()),
(3, 1, '卡布奇诺', '丰富的奶泡，口感绵密', '/static/images/product/cappuccino.jpg', 16.00, 29.00, 999, 0, 1, NOW()),

-- 生椰家族
(4, 2, '生椰拿铁', 'YYDS！冷榨生椰浆注入浓缩咖啡', '/static/images/product/coconut_latte.jpg', 18.00, 32.00, 500, 0, 1, NOW()),
(5, 2, '椰云拿铁', '如云朵般绵密的口感', '/static/images/product/coconut_cloud.jpg', 19.00, 32.00, 500, 0, 1, NOW()),

-- 瑞纳冰
(6, 3, '抹茶瑞纳冰', '清新抹茶风味冰沙', '/static/images/product/matcha_ice.jpg', 22.00, 35.00, 200, 0, 1, NOW()),
(7, 3, '巧克力瑞纳冰', '浓郁巧克力风味', '/static/images/product/choco_ice.jpg', 22.00, 35.00, 200, 0, 1, NOW());

-- =============================================
-- 6. 插入座位数据 (用于选座功能)
-- =============================================
-- 注意：根据实际 Seat 实体类，字段为 grid_x, grid_y, label, status, heat_score
-- 如果表结构不同，请根据实际情况调整
TRUNCATE TABLE `seat`;

-- 方案1：如果座位表使用 grid_x, grid_y, label 字段（根据 Seat 实体类，推荐使用此方案）
INSERT INTO `seat` (`id`, `grid_x`, `grid_y`, `label`, `status`, `heat_score`) VALUES
(1, 1, 1, 'A01', 1, 0.0), -- 1=可用
(2, 2, 1, 'A02', 1, 0.0),
(3, 3, 1, 'A03', 2, 0.0), -- 2=已占用
(4, 1, 2, 'B01', 1, 0.0),
(5, 2, 2, 'B02', 1, 0.0),
(6, 3, 2, 'C01', 1, 0.0),
(7, 4, 2, 'C02', 3, 0.0); -- 3=维修中

-- 方案2：如果座位表使用 store_id, seat_number, type 字段（如果表结构不同，取消注释下面代码，注释掉上面代码）
-- INSERT INTO `seat` (`id`, `store_id`, `seat_number`, `type`, `status`, `create_time`) VALUES
-- (1, 1, 'A01', 1, 0, NOW()), -- 1=普通座, 0=空闲
-- (2, 1, 'A02', 1, 0, NOW()),
-- (3, 1, 'A03', 1, 1, NOW()), -- 1=占用
-- (4, 1, 'B01', 2, 0, NOW()), -- 2=沙发座
-- (5, 1, 'B02', 2, 0, NOW()),
-- (6, 1, 'C01', 3, 0, NOW()), -- 3=窗边座
-- (7, 1, 'C02', 3, 2, NOW()); -- 2=维护中

-- =============================================
-- 7. 插入优惠券数据
-- =============================================
-- 注意：根据实际 Coupon 实体类，字段为 title, amount, min_point, stock, total_stock, status
TRUNCATE TABLE `coupon`;

INSERT INTO `coupon` (`id`, `title`, `amount`, `min_point`, `stock`, `total_stock`, `status`, `create_time`) VALUES
(1, '新人全场5折券', 5.0, 0.0, 100, 100, 1, NOW()), -- 5元无门槛券
(2, '满30减15', 15.0, 30.0, 50, 50, 1, NOW()),     -- 满30减15
(3, '咖啡买一赠一', 0.0, 0.0, 20, 20, 1, NOW());   -- 特殊券，金额为0表示买一赠一

-- =============================================
-- 8. 给测试用户发几张券 (user_coupon)
-- =============================================
-- 注意：根据实际 UserCoupon 实体类，字段为 user_id (String类型), coupon_id, status
TRUNCATE TABLE `user_coupon`;

INSERT INTO `user_coupon` (`user_id`, `coupon_id`, `status`, `create_time`) VALUES
('1', 1, 0, NOW()), -- 0=未使用
('1', 2, 0, NOW()),
('2', 1, 0, NOW());

-- =============================================
-- 使用说明：
-- 1. 执行此 SQL 前，请确保数据库 zhizuo 已存在
-- 2. 如果表不存在，请先执行相应的建表 SQL
-- 3. 密码字段使用 BCrypt 加密，默认密码为 "123456"
-- 4. 如果座位表结构不同，请根据实际情况调整第6部分的 SQL
-- 5. 执行后，原有的测试数据将被替换为瑞幸咖啡数据
-- =============================================

