-- ============================================
-- 修复测试用户密码
-- ============================================
-- 说明：此脚本用于更新测试用户的密码为正确的 BCrypt 哈希值
-- 原始密码：123456
-- ============================================

USE `mall_db`;

-- 更新所有测试用户的密码
-- BCrypt 哈希值对应的明文密码是 "123456"
UPDATE `users` 
SET `password` = '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
WHERE `username` IN ('admin', 'merchant', 'user');

-- 验证更新结果
SELECT id, username, email, status, created_at 
FROM `users` 
WHERE `username` IN ('admin', 'merchant', 'user');

SELECT '密码更新完成！请使用用户名 admin/merchant/user，密码 123456 登录' AS message;
