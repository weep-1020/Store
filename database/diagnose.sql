-- ============================================
-- 数据库初始化和诊断脚本
-- ============================================

-- 1. 检查数据库是否存在
SELECT '检查数据库...' AS step;
SHOW DATABASES LIKE 'mall_db';

-- 2. 使用数据库
USE `mall_db`;

-- 3. 检查 users 表是否存在
SELECT '检查 users 表...' AS step;
SHOW TABLES LIKE 'users';

-- 4. 查看表结构
DESCRIBE `users`;

-- 5. 查看所有用户数据
SELECT '当前用户数据...' AS step;
SELECT id, username, email, phone, status, created_at 
FROM `users`;

-- 6. 检查特定用户的密码哈希
SELECT '测试用户密码哈希值...' AS step;
SELECT username, LEFT(password, 20) AS password_hash_preview 
FROM `users` 
WHERE username IN ('admin', 'merchant', 'user');

-- 7. 统计用户数量
SELECT '用户总数...' AS step;
SELECT COUNT(*) AS total_users FROM `users`;

SELECT '诊断完成！' AS message;
