USE zhizuo;

-- 1. 清理并插入用户数据
DELETE FROM `user` WHERE id IN (1, 2);
INSERT INTO `user` (`id`, `username`, `nickname`, `phone`, `password`, `balance`, `credits`, `role`) VALUES
(1, 'admin', '测试管理员', '13800138000', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 9999.00, 1000, 'ADMIN'),
(2, 'user', '瑞幸路人甲', '13900139000', '$2a$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2.uheWG/igi', 100.00, 100, 'USER');

-- 2. 清理并插入分类数据
TRUNCATE TABLE `categories`;
INSERT INTO `categories` (`id`, `name`, `sort`) VALUES
(1, '大师咖啡', 100),
(2, '生椰家族', 90),
(3, '瑞纳冰', 80),
(4, '经典甜点', 70);

-- 3. 清理并插入商品数据
TRUNCATE TABLE `products`;
INSERT INTO `products` (`id`, `category_id`, `name`, `description`, `img_url`, `price`, `original_price`, `stock`, `sales`, `status`) VALUES
(1, 1, '标准美式', '经典意式浓缩，水与咖啡的黄金比例', '/static/images/product/american.jpg', 13.00, 26.00, 999, 0, 1),
(2, 1, '拿铁', '浓缩咖啡与香醇牛奶的经典融合', '/static/images/product/latte.jpg', 16.00, 29.00, 999, 0, 1),
(3, 1, '卡布奇诺', '丰富的奶泡，口感绵密', '/static/images/product/cappuccino.jpg', 16.00, 29.00, 999, 0, 1),
(4, 2, '生椰拿铁', 'YYDS！冷榨生椰浆注入浓缩咖啡', '/static/images/product/coconut_latte.jpg', 18.00, 32.00, 500, 0, 1),
(5, 2, '椰云拿铁', '如云朵般绵密的口感', '/static/images/product/coconut_cloud.jpg', 19.00, 32.00, 500, 0, 1),
(6, 3, '抹茶瑞纳冰', '清新抹茶风味冰沙', '/static/images/product/matcha_ice.jpg', 22.00, 35.00, 200, 0, 1),
(7, 3, '巧克力瑞纳冰', '浓郁巧克力风味', '/static/images/product/choco_ice.jpg', 22.00, 35.00, 200, 0, 1);

-- 4. 清理并插入座位数据
TRUNCATE TABLE `seat`;
INSERT INTO `seat` (`id`, `seat_code`, `floor`, `type`, `capacity`, `status`, `has_power`, `x_axis`, `y_axis`) VALUES
(1, 'A01', 1, 'NORMAL', 2, 0, 1, 1, 1),
(2, 'A02', 1, 'NORMAL', 2, 0, 1, 2, 1),
(3, 'A03', 1, 'NORMAL', 2, 1, 1, 3, 1),
(4, 'B01', 1, 'SOFA', 4, 0, 1, 1, 2),
(5, 'B02', 1, 'SOFA', 4, 0, 1, 2, 2),
(6, 'C01', 1, 'WINDOW', 2, 0, 1, 3, 2),
(7, 'C02', 1, 'WINDOW', 2, 2, 1, 4, 2);

-- 5. 清理并插入优惠券数据
TRUNCATE TABLE `coupon`;
INSERT INTO `coupon` (`id`, `name`, `type`, `value`, `min_amount`, `start_time`, `end_time`, `status`) VALUES
(1, '新人全场5折券', 1, 5.0, 0.0, NOW(), DATE_ADD(NOW(), INTERVAL 30 DAY), 1),
(2, '满30减15', 2, 15.0, 30.0, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY), 1),
(3, '咖啡买一赠一', 3, 0.0, 0.0, NOW(), DATE_ADD(NOW(), INTERVAL 14 DAY), 1);

-- 6. 清理并插入用户优惠券数据
TRUNCATE TABLE `user_coupon`;
INSERT INTO `user_coupon` (`user_id`, `coupon_id`, `status`) VALUES
(1, 1, 0),
(1, 2, 0),
(2, 1, 0);

