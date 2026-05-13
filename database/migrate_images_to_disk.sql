-- ============================================================
-- 迁移脚本: 将图片从数据库 BLOB 迁移到本地文件系统
-- 执行前请先停止应用，备份数据库
-- 执行后启动应用，图片将从磁盘读取
-- ============================================================

-- 1. 添加新列
ALTER TABLE `large_files`
    ADD COLUMN `file_path` VARCHAR(500) NULL COMMENT '文件系统路径' AFTER `filename`,
    ADD COLUMN `file_size` BIGINT NULL DEFAULT 0 COMMENT '文件大小(字节)' AFTER `file_path`,
    ADD COLUMN `content_type` VARCHAR(100) NULL COMMENT 'MIME类型' AFTER `file_size`;

-- 2. 更新元数据（文件大小和类型）
UPDATE `large_files`
SET
    `file_size` = LENGTH(`content`),
    `content_type` = CASE
        WHEN `filename` LIKE '%.png' THEN 'image/png'
        WHEN `filename` LIKE '%.jpg' OR `filename` LIKE '%.jpeg' THEN 'image/jpeg'
        WHEN `filename` LIKE '%.gif' THEN 'image/gif'
        WHEN `filename` LIKE '%.webp' THEN 'image/webp'
        ELSE 'application/octet-stream'
    END
WHERE `content` IS NOT NULL;

-- 3. 删除 BLOB 列（请确认文件导出成功后再执行）
-- ALTER TABLE `large_files` DROP COLUMN `content`;
