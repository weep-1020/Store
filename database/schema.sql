-- ============================================
-- 商城项目 RBAC 数据库（简化版）
-- ============================================

CREATE DATABASE IF NOT EXISTS `mall_db` 
DEFAULT CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE `mall_db`;

-- 1. 用户表
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `username` VARCHAR(50) NOT NULL,
    `password` VARCHAR(255) NOT NULL,
    `email` VARCHAR(100),
    `phone` VARCHAR(20),
    `status` TINYINT DEFAULT 1,
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_username` (`username`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 2. 角色表
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `role_name` VARCHAR(50) NOT NULL,
    `role_code` VARCHAR(50) NOT NULL,
    `description` VARCHAR(200),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 3. 权限表
DROP TABLE IF EXISTS `permissions`;
CREATE TABLE `permissions` (
    `id` BIGINT UNSIGNED NOT NULL AUTO_INCREMENT,
    `permission_name` VARCHAR(100) NOT NULL,
    `permission_code` VARCHAR(100) NOT NULL,
    `resource_type` VARCHAR(20) DEFAULT 'api',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_permission_code` (`permission_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 4. 用户-角色关联表
DROP TABLE IF EXISTS `user_roles`;
CREATE TABLE `user_roles` (
    `user_id` BIGINT UNSIGNED NOT NULL,
    `role_id` BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (`user_id`, `role_id`),
    KEY `idx_role_id` (`role_id`),
    CONSTRAINT `fk_ur_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_ur_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联';

-- 5. 角色-权限关联表
DROP TABLE IF EXISTS `role_permissions`;
CREATE TABLE `role_permissions` (
    `role_id` BIGINT UNSIGNED NOT NULL,
    `permission_id` BIGINT UNSIGNED NOT NULL,
    PRIMARY KEY (`role_id`, `permission_id`),
    KEY `idx_permission_id` (`permission_id`),
    CONSTRAINT `fk_rp_role` FOREIGN KEY (`role_id`) REFERENCES `roles` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_rp_permission` FOREIGN KEY (`permission_id`) REFERENCES `permissions` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联';

-- ============================================
-- 初始化数据
-- ============================================

-- 插入角色
INSERT INTO `roles` VALUES 
(1, '超级管理员', 'SUPER_ADMIN', '所有权限'),
(2, '商家', 'MERCHANT', '商品和订单管理'),
(3, '普通用户', 'USER', '浏览和购买');

-- 插入权限
INSERT INTO `permissions` VALUES 
(1, '查看商品', 'product:view', 'api'),
(2, '管理商品', 'product:manage', 'api'),
(3, '查看订单', 'order:view', 'api'),
(4, '管理订单', 'order:manage', 'api'),
(5, '用户管理', 'user:manage', 'api');

-- 分配权限给角色
-- 超级管理员：所有权限
INSERT INTO `role_permissions` VALUES 
(1, 1), (1, 2), (1, 3), (1, 4), (1, 5);

-- 商家：商品和订单管理
INSERT INTO `role_permissions` VALUES 
(2, 1), (2, 2), (2, 3), (2, 4);

-- 普通用户：只能查看
INSERT INTO `role_permissions` VALUES 
(3, 1), (3, 3);

-- 插入测试用户（密码: 123456）
INSERT INTO `users` VALUES 
(1, 'admin', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'admin@mall.com', '13800000001', 1, NOW()),
(2, 'merchant', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'shop@mall.com', '13800000002', 1, NOW()),
(3, 'user', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'user@mall.com', '13800000003', 1, NOW());

-- 分配角色
INSERT INTO `user_roles` VALUES 
(1, 1),
(2, 2),
(3, 3);

SELECT '数据库创建完成！' AS message;
