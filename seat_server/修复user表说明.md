# 修复 user 表 name 字段缺失问题

## 问题描述
后端注册时出现错误：`Unknown column 'name' in 'field list'`

说明数据库 `user` 表中缺少 `name` 字段，但代码中尝试插入该字段。

## 解决方案

### 方法1：执行修复 SQL（推荐）

执行 `fix_user_table.sql` 文件：

```bash
mysql -uroot -p000000 zhizuo < seat_server/fix_user_table.sql
```

或者在数据库工具中直接执行：

```sql
USE zhizuo;
ALTER TABLE `user` ADD COLUMN `name` varchar(50) DEFAULT NULL COMMENT '真实姓名' AFTER `phone`;
```

### 方法2：重新创建表（会清空数据）

如果方法1失败，可以重新创建表结构：

```bash
mysql -uroot -p000000 zhizuo < seat_server/create_user_table.sql
```

**注意：这会清空 user 表中的所有数据！**

### 方法3：检查现有表结构

如果表中有 `nickname` 字段但没有 `name` 字段，可以：

1. 添加 `name` 字段
2. 将 `nickname` 的数据复制到 `name`
3. 可选：删除 `nickname` 字段

```sql
USE zhizuo;
-- 添加 name 字段
ALTER TABLE `user` ADD COLUMN `name` varchar(50) DEFAULT NULL COMMENT '真实姓名' AFTER `phone`;
-- 迁移数据
UPDATE `user` SET `name` = `nickname` WHERE `name` IS NULL AND `nickname` IS NOT NULL;
```

## 验证

执行以下 SQL 验证表结构：

```sql
DESCRIBE `user`;
```

应该能看到 `name` 字段。

## 修复后的表结构

`user` 表应该包含以下字段：
- `id` - 主键
- `username` - 用户名
- `phone` - 手机号
- `name` - 真实姓名（**这是需要添加的字段**）
- `password` - 密码
- `points` - 积分
- `role` - 角色
- `balance` - 余额
- `create_time` - 创建时间
- `update_time` - 更新时间

