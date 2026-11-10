-- 仅新增 exam_question 表（不触碰其他表）
DROP TABLE IF EXISTS `exam_question`;
CREATE TABLE `exam_question` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `type`           VARCHAR(20)  NOT NULL                COMMENT '题型: choice/fill/judge/code/scratch/scratchjr',
    `title`          TEXT         NOT NULL                COMMENT '题干',
    `options`        LONGTEXT     DEFAULT NULL            COMMENT '选项（JSON数组，选择题）',
    `answer`         TEXT         DEFAULT NULL            COMMENT '正确答案',
    `analysis`       TEXT         DEFAULT NULL            COMMENT '解析',
    `score`          INT          DEFAULT 0               COMMENT '分值',
    `language`       VARCHAR(20)  DEFAULT NULL            COMMENT '编程语言（代码题/Scratch题）',
    `template_code`  LONGTEXT     DEFAULT NULL            COMMENT '模板代码（代码题）',
    `create_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`    DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`        INT       NOT NULL DEFAULT 0         COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_type` (`type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试题目表';
