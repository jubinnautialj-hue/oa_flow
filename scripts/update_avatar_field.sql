-- ============================================
-- 数据库升级脚本 - 添加用户头像字段
-- 执行时间: 2026-05-11
-- ============================================

USE `oa_flow`;

-- 检查并添加 avatar 字段到 sys_user 表
SET @dbname = DATABASE();
SET @tablename = 'sys_user';
SET @columnname = 'avatar';
SET @preparedStatement = (SELECT IF(
  (
    SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
    WHERE
      (table_schema = @dbname)
      AND (table_name = @tablename)
      AND (column_name = @columnname)
  ) > 0,
  'SELECT 1',
  CONCAT('ALTER TABLE ', @tablename, ' ADD COLUMN ', @columnname, ' VARCHAR(255) NULL COMMENT ''头像路径''')
));
PREPARE alterIfNotExists FROM @preparedStatement;
EXECUTE alterIfNotExists;
DEALLOCATE PREPARE alterIfNotExists;

SELECT '数据库升级完成!' AS '消息';
