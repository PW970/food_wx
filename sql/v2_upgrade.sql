-- =============================================
-- V2 数据库变更脚本
-- =============================================

USE fooddb;

-- 1. 扩展 shop 表，添加新字段
ALTER TABLE `shop` 
ADD COLUMN `cover_image` VARCHAR(500) DEFAULT NULL COMMENT '封面图片',
ADD COLUMN `phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
ADD COLUMN `business_hours` VARCHAR(200) DEFAULT NULL COMMENT '营业时间',
ADD COLUMN `per_capita` DECIMAL(10,2) DEFAULT NULL COMMENT '人均消费',
ADD COLUMN `description` TEXT DEFAULT NULL COMMENT '店铺描述',
ADD COLUMN `status` VARCHAR(20) DEFAULT 'OPEN' COMMENT '状态(OPEN/CLOSED)',
ADD COLUMN `category_id` BIGINT DEFAULT NULL COMMENT '分类ID';

-- 添加索引
ALTER TABLE `shop` ADD KEY `idx_category_id` (`category_id`);

-- 2. 新建分类表
DROP TABLE IF EXISTS `category`;
CREATE TABLE `category` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '分类ID',
    `name` VARCHAR(50) NOT NULL COMMENT '分类名称',
    `icon` VARCHAR(500) DEFAULT NULL COMMENT '分类图标',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_name` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='店铺分类表';

-- 初始化一些分类数据
INSERT INTO `category` (`name`, `icon`) VALUES
('火锅', 'https://example.com/icons/hotpot.png'),
('川菜', 'https://example.com/icons/sichuan.png'),
('粤菜', 'https://example.com/icons/cantonese.png'),
('日料', 'https://example.com/icons/japanese.png'),
('西餐', 'https://example.com/icons/western.png'),
('快餐', 'https://example.com/icons/fastfood.png'),
('小吃', 'https://example.com/icons/snack.png'),
('面馆', 'https://example.com/icons/noodles.png');

-- 3. 新建店铺标签表
DROP TABLE IF EXISTS `shop_tag`;
CREATE TABLE `shop_tag` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '标签ID',
    `shop_id` BIGINT NOT NULL COMMENT '店铺ID',
    `tag_name` VARCHAR(50) NOT NULL COMMENT '标签名称',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_shop_id` (`shop_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='店铺标签表';
