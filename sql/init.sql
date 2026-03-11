-- =============================================
-- 美食推荐系统数据库建表脚本
-- 数据库: fooddb
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS fooddb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE fooddb;

-- =============================================
-- 1. 用户表
-- =============================================
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '用户ID',
    `openid` VARCHAR(64) DEFAULT NULL COMMENT '微信openid',
    `nickname` VARCHAR(100) DEFAULT NULL COMMENT '用户昵称',
    `avatar` VARCHAR(255) DEFAULT NULL COMMENT '头像URL',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_openid` (`openid`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =============================================
-- 2. 店铺表
-- =============================================
DROP TABLE IF EXISTS `shop`;
CREATE TABLE `shop` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '店铺ID',
    `name` VARCHAR(200) NOT NULL COMMENT '店铺名称',
    `address` VARCHAR(255) DEFAULT NULL COMMENT '店铺地址',
    `latitude` DOUBLE DEFAULT NULL COMMENT '纬度',
    `longitude` DOUBLE DEFAULT NULL COMMENT '经度',
    `category` VARCHAR(100) DEFAULT NULL COMMENT '店铺分类',
    `score` DOUBLE DEFAULT 0 COMMENT '评分',
    `review_count` INT DEFAULT 0 COMMENT '评论数',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='店铺表';

-- =============================================
-- 3. 评论表
-- =============================================
DROP TABLE IF EXISTS `review`;
CREATE TABLE `review` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '评论ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `shop_id` BIGINT NOT NULL COMMENT '店铺ID',
    `rating` INT NOT NULL COMMENT '评分(1-5)',
    `content` TEXT COMMENT '评论内容',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='评论表';

-- =============================================
-- 4. 收藏表
-- =============================================
DROP TABLE IF EXISTS `collection`;
CREATE TABLE `collection` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '收藏ID',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `shop_id` BIGINT NOT NULL COMMENT '店铺ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_shop` (`user_id`, `shop_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='收藏表';

-- =============================================
-- 初始化测试数据 (12条店铺数据)
-- =============================================
INSERT INTO `shop` (`name`, `address`, `latitude`, `longitude`, `category`, `score`, `review_count`) VALUES
('川味火锅', '朝阳区建国路88号', 39.9042, 116.4074, '火锅', 4.8, 128),
('兰州拉面', '海淀区中关村大街1号', 39.9890, 116.3063, '面馆', 4.5, 86),
('粤菜馆', '东城区王府井大街138号', 39.9139, 116.4094, '粤菜', 4.7, 95),
('寿司太郎', '朝阳区三里屯太古里', 39.9358, 116.4475, '日料', 4.9, 156),
('湘菜王', '西城区西单北大街120号', 39.9137, 116.3732, '湘菜', 4.6, 72),
('麦当劳', '各区均有分店', 39.9042, 116.4074, '快餐', 4.2, 320),
('海底捞火锅', '朝阳区望京SOHO', 39.9955, 116.4711, '火锅', 4.8, 245),
('全聚德烤鸭', '东城区前门大街30号', 39.9010, 116.4107, '北京菜', 4.5, 188),
('沙县小吃', '海淀区知春路28号', 39.9856, 116.3195, '小吃', 4.0, 45),
('西贝莜面村', '朝阳区大望路SKP', 39.9085, 116.4915, '西北菜', 4.6, 112),
('绿茶餐厅', '朝阳区三里屯通盈中心', 39.9360, 116.4480, '江浙菜', 4.4, 78),
('必胜客', '各区均有分店', 39.9042, 116.4074, '西餐', 4.3, 265);
