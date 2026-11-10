-- ============================================================================
-- Phase 1 增量迁移：确保课程购买记录表 / 视频学习进度表存在
-- 适用场景：线上存量库若基于较早版本 schema 初始化，可能缺少这两张表。
-- 新库直接以 schema_init.sql 初始化已包含，本脚本幂等，可重复执行。
-- 执行：mysql -u<user> -p<pass> <db> < scripts/sql/add_phase1_tables.sql
-- ============================================================================

CREATE TABLE IF NOT EXISTS `course_purchase` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     BIGINT        NOT NULL                COMMENT '用户ID',
    `course_id`   BIGINT        NOT NULL                COMMENT '课程ID',
    `order_id`    BIGINT        DEFAULT NULL            COMMENT '订单ID',
    `pay_amount`  DECIMAL(10,2) DEFAULT NULL            COMMENT '支付金额',
    `expire_time` DATETIME      DEFAULT NULL            COMMENT '过期时间',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     INT           NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_user_course` (`user_id`, `course_id`, `deleted`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程购买记录表';

CREATE TABLE IF NOT EXISTS `video_progress` (
    `id`               BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`          BIGINT   NOT NULL                COMMENT '用户ID',
    `video_id`         BIGINT   NOT NULL                COMMENT '视频ID',
    `watched_duration`  INT      DEFAULT 0              COMMENT '已观看时长（秒）',
    `is_completed`     INT      DEFAULT 0              COMMENT '是否已看完（0-否，1-是）',
    `last_watch_time`  DATETIME DEFAULT NULL            COMMENT '最后观看时间',
    `create_time`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_user_video` (`user_id`, `video_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_video_id` (`video_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频学习进度表';
