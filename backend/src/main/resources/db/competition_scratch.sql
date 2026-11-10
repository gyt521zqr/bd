-- ============================================================================
-- Magao Platform - 竞赛与 Scratch 模块增量 SQL 脚本
-- ============================================================================
-- 执行顺序：在已有数据库基础上执行此脚本
-- 包含：竞赛模块表、Scratch 自由创作作品表、索引优化、Redis Key 设计说明
-- ============================================================================

-- ============================================================================
-- 1. 竞赛信息表
-- ============================================================================
CREATE TABLE IF NOT EXISTS `competition_info` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title`           VARCHAR(200) NOT NULL                COMMENT '竞赛标题',
    `description`     TEXT                                  COMMENT '竞赛描述',
    `start_time`      DATETIME     NOT NULL                COMMENT '开始时间',
    `end_time`        DATETIME     NOT NULL                COMMENT '结束时间',
    `status`          TINYINT      NOT NULL DEFAULT 0      COMMENT '竞赛状态（0-未开始，1-进行中，2-已结束）',
    `allowed_block_categories` VARCHAR(500)                COMMENT '允许的积木分类（JSON数组，如 ["motion","control","operator"]）',
    `max_participants` INT         NOT NULL DEFAULT 500    COMMENT '最大参赛人数',
    `creator_id`      BIGINT       NOT NULL                COMMENT '创建者ID',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_creator_id` (`creator_id`),
    INDEX `idx_start_end_time` (`start_time`, `end_time`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='竞赛信息表';

-- ============================================================================
-- 2. 竞赛参赛者表
-- ============================================================================
CREATE TABLE IF NOT EXISTS `competition_participant` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `competition_id`  BIGINT       NOT NULL                COMMENT '竞赛ID',
    `user_id`         BIGINT       NOT NULL                COMMENT '用户ID',
    `join_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    `submit_time`     DATETIME                              COMMENT '提交时间',
    `score`           INT          NOT NULL DEFAULT 0      COMMENT '得分',
    `ranking`         INT          NOT NULL DEFAULT 0      COMMENT '排名',
    `violation_count` INT          NOT NULL DEFAULT 0      COMMENT '违规次数',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_competition_user` (`competition_id`, `user_id`, `deleted`),
    INDEX `idx_competition_id` (`competition_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_score` (`competition_id`, `score` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='竞赛参赛者表';

-- ============================================================================
-- 3. 竞赛提交表（按月分表）
--    主表结构定义，实际使用时按月创建分表：competition_submit_YYYYMM
--    分表示例：competition_submit_202401, competition_submit_202402, ...
-- ============================================================================
CREATE TABLE IF NOT EXISTS `competition_submit` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `competition_id`  BIGINT       NOT NULL                COMMENT '竞赛ID',
    `user_id`         BIGINT       NOT NULL                COMMENT '用户ID',
    `sb3_data`        MEDIUMTEXT                           COMMENT 'Scratch sb3 数据（JSON）',
    `judge_result`    TEXT                                  COMMENT '判题结果（JSON）',
    `submit_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    `ip_address`      VARCHAR(45)                           COMMENT '提交IP地址',
    PRIMARY KEY (`id`),
    INDEX `idx_competition_id` (`competition_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_submit_time` (`submit_time`),
    INDEX `idx_competition_user` (`competition_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='竞赛提交表（按月分表）';

-- ============================================================================
-- 分表存储过程：自动创建当月分表
-- ============================================================================
DELIMITER //
CREATE PROCEDURE IF NOT EXISTS `create_competition_submit_monthly`(IN `p_year_month` VARCHAR(6))
BEGIN
    DECLARE `v_table_name` VARCHAR(64);
    SET `v_table_name` = CONCAT('competition_submit_', `p_year_month`);

    SET @sql = CONCAT(
        'CREATE TABLE IF NOT EXISTS `', `v_table_name`, '` LIKE `competition_submit`'
    );
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;

    -- 添加分区索引
    SET @sql = CONCAT(
        'ALTER TABLE `', `v_table_name`, '` ADD INDEX `idx_submit_time` (`submit_time`)'
    );
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
END //
DELIMITER ;

-- ============================================================================
-- 4. Scratch 自由创作作品表
-- ============================================================================
CREATE TABLE IF NOT EXISTS `scratch_work` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`         BIGINT       NOT NULL                COMMENT '用户ID',
    `title`           VARCHAR(200) NOT NULL                COMMENT '作品名称',
    `description`     TEXT                                  COMMENT '作品描述',
    `sb3_file_key`    VARCHAR(500) NOT NULL                COMMENT 'sb3 文件在 MinIO 中的存储路径',
    `thumbnail_key`   VARCHAR(500)                          COMMENT '缩略图在 MinIO 中的存储路径',
    `file_size`       BIGINT       NOT NULL DEFAULT 0      COMMENT 'sb3 文件大小（字节）',
    `is_public`       TINYINT      NOT NULL DEFAULT 0      COMMENT '是否公开（0-私有，1-公开）',
    `like_count`      INT          NOT NULL DEFAULT 0      COMMENT '点赞数',
    `view_count`      INT          NOT NULL DEFAULT 0      COMMENT '浏览数',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         TINYINT      NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_is_public` (`is_public`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_user_time` (`user_id`, `create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Scratch自由创作作品表';

-- ============================================================================
-- Redis Key 设计说明
-- ============================================================================
-- 命名规范：模块:功能:标识
-- 过期策略：根据业务场景设置合理的 TTL
--
-- ==================== 竞赛模块 Redis Key ====================
--
-- 1. 竞赛信息缓存
--    Key:    competition:info:{competitionId}
--    Type:   String (JSON)
--    TTL:    30 分钟
--    说明:   缓存竞赛基本信息，减少数据库查询
--
-- 2. 竞赛参赛人数计数
--    Key:    competition:join_count:{competitionId}
--    Type:   String (Integer)
--    TTL:    竞赛结束后 24 小时
--    说明:   使用 INCR 原子操作计数，配合 max_participants 做人数限制
--
-- 3. 竞赛已参加用户集合
--    Key:    competition:joined_users:{competitionId}
--    Type:   Hash
--    Field:  userId
--    Value:  participantId
--    TTL:    竞赛结束后 24 小时
--    说明:   防止同一用户重复加入竞赛
--
-- 4. 竞赛实时排行榜
--    Key:    competition:ranking:{competitionId}
--    Type:   SortedSet
--    Member: userId
--    Score:  用户得分（取最高分）
--    TTL:    竞赛结束后 7 天
--    说明:   使用 ZADD/ZREVRANGE 实现实时排行榜，按分数降序排列
--    操作:   ZADD key score userId
--           ZREVRANGE key 0 99 WITHSCORES（获取 Top 100）
--           ZREVRANK key userId（获取用户排名）
--
-- 5. 竞赛提交分布式锁
--    Key:    lock:competition:submit:{competitionId}:{userId}
--    Type:   String
--    TTL:    60 秒
--    说明:   防止用户频繁提交作品，使用 SETNX 实现
--
-- ==================== Scratch 作品模块 Redis Key ====================
--
-- 6. Scratch 作品浏览计数
--    Key:    scratch:work:view:{workId}
--    Type:   String (Integer)
--    TTL:    1 小时
--    说明:   短时缓存浏览数，定时同步到数据库
--
-- 7. Scratch 作品点赞集合
--    Key:    scratch:work:liked:{workId}
--    Type:   Set
--    Member: userId
--    TTL:    24 小时
--    说明:   记录已点赞用户，防止重复点赞
-- ============================================================================
