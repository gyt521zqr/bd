-- ============================================================
-- 在线编程模块重构 - 数据库增量脚本
-- 日期：2026-06-18
-- 说明：兼容 Scratch3/Python/C++/GoC 四种语言的存储结构
-- ============================================================

-- 1. practice_record 表新增字段
-- 支持存储 Scratch 素材文件和判题详细结果
ALTER TABLE `practice_record`
    ADD COLUMN `scratch_assets` JSON NULL COMMENT 'Scratch素材文件列表（JSON数组）' AFTER `scratch_json`,
    ADD COLUMN `judge_detail` JSON NULL COMMENT '判题详细结果（JSON，含各用例详情）' AFTER `status`;

-- 2. exam_answer 表新增字段
-- 支持存储 Scratch 素材文件和判题详细结果
ALTER TABLE `exam_answer`
    ADD COLUMN `scratch_assets` JSON NULL COMMENT 'Scratch素材文件列表（JSON数组）' AFTER `scratch_json`,
    ADD COLUMN `judge_detail` JSON NULL COMMENT '判题详细结果（JSON，含各用例详情）' AFTER `status`;

-- 3. practice_draft 表结构优化（如果不存在则创建）
CREATE TABLE IF NOT EXISTS `practice_draft` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id` BIGINT NOT NULL COMMENT '用户ID',
    `question_id` BIGINT NOT NULL COMMENT '题目ID',
    `language` VARCHAR(20) NOT NULL COMMENT '编程语言：scratch3/python/cpp/goc',
    `code` TEXT NULL COMMENT '代码文本（Python/C++/GoC）',
    `scratch_json` LONGTEXT NULL COMMENT 'Scratch项目JSON',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_question` (`user_id`, `question_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='练习草稿表';

-- 4. practice_question 表新增模板代码字段（如果不存在）
ALTER TABLE `practice_question`
    ADD COLUMN IF NOT EXISTS `template_code` TEXT NULL COMMENT '模板代码' AFTER `test_cases`;

-- 5. exam_paper_question 表新增语言字段（如果不存在）
ALTER TABLE `exam_paper_question`
    ADD COLUMN IF NOT EXISTS `language` VARCHAR(20) NULL COMMENT '编程语言（代码题）' AFTER `question_type`,
    ADD COLUMN IF NOT EXISTS `template_code` TEXT NULL COMMENT '模板代码（代码题）' AFTER `language`;

-- 6. 确认语言枚举值覆盖（practice_question.language 字段）
-- 现有值应为：PYTHON/CPP/GOC/SCRATCH_JR/SCRATCH3
-- 如果缺少 SCRATCH3，添加检查约束
-- ALTER TABLE `practice_question` MODIFY COLUMN `language` VARCHAR(20) NOT NULL COMMENT '编程语言：PYTHON/CPP/GOC/SCRATCH_JR/SCRATCH3';

-- ============================================================
-- 判题记录索引优化
-- ============================================================

-- 7. practice_record 添加复合索引（用户+题目+状态）
ALTER TABLE `practice_record`
    ADD INDEX `idx_user_question_status` (`user_id`, `question_id`, `status`);

-- 8. exam_answer 添加复合索引（考试记录+题目）
ALTER TABLE `exam_answer`
    ADD INDEX `idx_record_question` (`record_id`, `question_id`);

-- ============================================================
-- 在线编程模块 - 编辑器增强增量脚本
-- 日期：2026-10-27
-- 说明：1) practice_question 增加样例输入输出；2) practice_record 增加 Scratch 封面 URL
-- 使用 information_schema 幂等守卫，兼容 MySQL 8（避免 ADD COLUMN IF NOT EXISTS 非标准语法）
-- ============================================================

-- 9. practice_question 新增样例输入输出字段（examples）
SET @db = DATABASE();
SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'practice_question' AND COLUMN_NAME = 'examples') = 0,
    'ALTER TABLE `practice_question` ADD COLUMN `examples` TEXT NULL COMMENT ''样例输入输出JSON数组：[{input,output}]'' AFTER `hint`',
    'SELECT 1'
));
PREPARE stmt_examples FROM @stmt;
EXECUTE stmt_examples;
DEALLOCATE PREPARE stmt_examples;

-- 10. practice_record 新增 Scratch 作品封面缩略图 URL 字段（cover_url）
SET @stmt = (SELECT IF(
    (SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = @db AND TABLE_NAME = 'practice_record' AND COLUMN_NAME = 'cover_url') = 0,
    'ALTER TABLE `practice_record` ADD COLUMN `cover_url` VARCHAR(512) NULL COMMENT ''Scratch作品封面缩略图URL（对象存储）'' AFTER `judge0_token`',
    'SELECT 1'
));
PREPARE stmt_cover FROM @stmt;
EXECUTE stmt_cover;
DEALLOCATE PREPARE stmt_cover;

