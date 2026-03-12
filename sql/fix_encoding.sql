-- 修复数据库编码脚本
-- 运行前请确保 MySQL 服务已启动

-- 1. 设置客户端连接编码
SET NAMES utf8mb4;
SET CHARACTER SET utf8mb4;

-- 2. 修改数据库默认字符集
ALTER DATABASE fooddb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE fooddb;

-- 3. 强制修改所有表的字符集
ALTER TABLE `user` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `shop` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `review` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `collection` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `category` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
ALTER TABLE `shop_tag` CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 4. 重新插入测试数据（如果之前的步骤没有数据）
-- 注意：如果已经有数据，这一步可以跳过，但如果数据乱码了，最好清空重置
-- TRUNCATE TABLE `user`;
-- TRUNCATE TABLE `shop`;
-- TRUNCATE TABLE `review`;
-- TRUNCATE TABLE `collection`;
-- TRUNCATE TABLE `shop_tag`;

-- 如果需要重置数据，取消上面 TRUNCATE 的注释，并执行 init.sql
