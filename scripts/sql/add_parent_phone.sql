-- 为已存在的 sys_user 表补充 parent_phone 列（家长手机号）
-- 适用场景：生产环境已建库，但未包含该列时执行一次。
-- 幂等：已存在则跳过，可重复执行。
-- 用法：mysql -h <host> -u <user> -p <db> < scripts/sql/add_parent_phone.sql

DROP PROCEDURE IF EXISTS _add_parent_phone;
DELIMITER $$
CREATE PROCEDURE _add_parent_phone()
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns
    WHERE table_schema = DATABASE()
      AND table_name = 'sys_user'
      AND column_name = 'parent_phone'
  ) THEN
    ALTER TABLE `sys_user`
      ADD COLUMN `parent_phone` VARCHAR(20) DEFAULT NULL COMMENT '家长手机号' AFTER `phone`;
  END IF;
END$$
DELIMITER ;
CALL _add_parent_phone();
DROP PROCEDURE IF EXISTS _add_parent_phone;
