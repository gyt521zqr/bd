-- ============================================================================
-- Magao Platform - 全量建表脚本
-- 根据后端 Entity 类自动生成
-- 数据库: MySQL / magao_platform
-- 生成日期: 2026-07-25
-- ============================================================================
-- BaseEntity 公共字段: id(BIGINT AUTO_INCREMENT), create_time, update_time, deleted
-- ============================================================================

USE `magao_platform`;

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================================================
-- 1. sys_user - 用户表
-- Entity: User extends BaseEntity
-- ============================================================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `username`          VARCHAR(64)  NOT NULL                COMMENT '用户名（登录账号）',
    `password`          VARCHAR(255) NOT NULL                COMMENT '密码（加密存储）',
    `nickname`          VARCHAR(64)  DEFAULT NULL            COMMENT '昵称',
    `real_name`         VARCHAR(64)  DEFAULT NULL            COMMENT '真实姓名',
    `phone`             VARCHAR(20)  DEFAULT NULL            COMMENT '手机号',
    `parent_phone`      VARCHAR(20)  DEFAULT NULL            COMMENT '家长手机号',
    `email`             VARCHAR(128) DEFAULT NULL            COMMENT '邮箱',
    `avatar`            VARCHAR(500) DEFAULT NULL            COMMENT '头像URL',
    `role_type`         VARCHAR(32)  DEFAULT NULL            COMMENT '角色类型: STUDENT-学生, TEACHER-教师, OPERATOR-运营, ADMIN-管理员',
    `status`            INT          NOT NULL DEFAULT 1      COMMENT '状态（0-禁用，1-启用）',
    `wechat_open_id`    VARCHAR(128) DEFAULT NULL            COMMENT '微信OpenID（微信登录用）',
    `last_login_time`   DATETIME     DEFAULT NULL            COMMENT '最后登录时间',
    `last_login_ip`     VARCHAR(45)  DEFAULT NULL            COMMENT '最后登录IP',
    `create_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           INT          NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_username` (`username`, `deleted`),
    INDEX `idx_phone` (`phone`),
    INDEX `idx_role_type` (`role_type`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- ============================================================================
-- 2. sys_role - 角色表
-- Entity: Role extends BaseEntity
-- ============================================================================
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_code`   VARCHAR(64)  NOT NULL                COMMENT '角色编码（如：STUDENT, TEACHER, ADMIN）',
    `role_name`   VARCHAR(128) NOT NULL                COMMENT '角色名称',
    `description` VARCHAR(500) DEFAULT NULL            COMMENT '角色描述',
    `sort_order`  INT          DEFAULT 0              COMMENT '排序号',
    `status`      INT          NOT NULL DEFAULT 1      COMMENT '状态（0-禁用，1-启用）',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     INT          NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_role_code` (`role_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- ============================================================================
-- 3. sys_user_role - 用户角色关联表
-- Entity: UserRole (implements Serializable, 无BaseEntity)
-- ============================================================================
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     BIGINT   NOT NULL                COMMENT '用户ID',
    `role_id`     BIGINT   NOT NULL                COMMENT '角色ID',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_user_role` (`user_id`, `role_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- ============================================================================
-- 4. sys_user_login_log - 用户登录日志表
-- Entity: UserLoginLog (implements Serializable)
-- ============================================================================
DROP TABLE IF EXISTS `sys_user_login_log`;
CREATE TABLE `sys_user_login_log` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '日志ID',
    `user_id`       BIGINT       DEFAULT NULL            COMMENT '用户ID',
    `username`      VARCHAR(64)  DEFAULT NULL            COMMENT '用户名',
    `login_ip`      VARCHAR(45)  DEFAULT NULL            COMMENT '登录IP',
    `login_location` VARCHAR(128) DEFAULT NULL           COMMENT '登录地点',
    `browser`       VARCHAR(128) DEFAULT NULL           COMMENT '浏览器',
    `os`            VARCHAR(128) DEFAULT NULL           COMMENT '操作系统',
    `status`        INT          DEFAULT NULL            COMMENT '登录状态（0-失败，1-成功）',
    `message`       VARCHAR(500) DEFAULT NULL           COMMENT '登录消息',
    `login_time`    DATETIME     DEFAULT NULL            COMMENT '登录时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_login_time` (`login_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户登录日志表';

-- ============================================================================
-- 5. sys_dict - 数据字典表
-- Entity: SysDict extends BaseEntity
-- ============================================================================
DROP TABLE IF EXISTS `sys_dict`;
CREATE TABLE `sys_dict` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `dict_type`   VARCHAR(128) NOT NULL                COMMENT '字典类型',
    `dict_name`   VARCHAR(128) NOT NULL                COMMENT '字典名称',
    `status`      INT          NOT NULL DEFAULT 1      COMMENT '状态（0-停用，1-启用）',
    `remark`      VARCHAR(500) DEFAULT NULL            COMMENT '备注',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     INT          NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_dict_type` (`dict_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据字典表';

-- ============================================================================
-- 6. sys_dict_item - 数据字典项表
-- Entity: SysDictItem (implements Serializable)
-- ============================================================================
DROP TABLE IF EXISTS `sys_dict_item`;
CREATE TABLE `sys_dict_item` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `dict_id`     BIGINT      NOT NULL                COMMENT '字典ID',
    `dict_label`  VARCHAR(128) NOT NULL               COMMENT '字典标签',
    `dict_value`  VARCHAR(256) NOT NULL               COMMENT '字典值',
    `sort_order`  INT         DEFAULT 0               COMMENT '排序号',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_dict_id` (`dict_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='数据字典项表';

-- ============================================================================
-- 7. sys_config - 系统配置表
-- Entity: SysConfig extends BaseEntity
-- ============================================================================
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `config_key`   VARCHAR(128) NOT NULL                COMMENT '配置键',
    `config_value` TEXT          DEFAULT NULL            COMMENT '配置值',
    `description`  VARCHAR(500) DEFAULT NULL           COMMENT '配置描述',
    `config_group` VARCHAR(64)  DEFAULT NULL            COMMENT '配置分组',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      INT          NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_config_key` (`config_key`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- ============================================================================
-- 8. sys_log - 系统日志表
-- Entity: SysLog (implements Serializable)
-- ============================================================================
DROP TABLE IF EXISTS `sys_log`;
CREATE TABLE `sys_log` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `module`          VARCHAR(64)  DEFAULT NULL            COMMENT '操作模块',
    `operation_type`  VARCHAR(64)  DEFAULT NULL            COMMENT '操作类型',
    `description`     VARCHAR(500) DEFAULT NULL            COMMENT '操作描述',
    `user_id`         BIGINT       DEFAULT NULL            COMMENT '操作用户ID',
    `username`        VARCHAR(64)  DEFAULT NULL            COMMENT '操作用户名',
    `request_url`     VARCHAR(500) DEFAULT NULL            COMMENT '请求URL',
    `request_method`  VARCHAR(16)  DEFAULT NULL            COMMENT '请求方法',
    `request_params`  TEXT          DEFAULT NULL            COMMENT '请求参数',
    `response_result` TEXT          DEFAULT NULL            COMMENT '返回结果',
    `ip`              VARCHAR(45)  DEFAULT NULL            COMMENT 'IP地址',
    `cost_time`       INT          DEFAULT NULL            COMMENT '耗时（毫秒）',
    `status`          INT          DEFAULT NULL            COMMENT '操作状态（0-失败，1-成功）',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_module` (`module`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统日志表';

-- ============================================================================
-- 9. sys_notice - 系统公告表
-- Entity: SysNotice extends BaseEntity
-- ============================================================================
DROP TABLE IF EXISTS `sys_notice`;
CREATE TABLE `sys_notice` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title`       VARCHAR(200) NOT NULL                COMMENT '公告标题',
    `content`     TEXT          DEFAULT NULL            COMMENT '公告内容',
    `notice_type` INT          DEFAULT NULL            COMMENT '公告类型（1-通知，2-公告）',
    `status`      INT          NOT NULL DEFAULT 1      COMMENT '状态（0-关闭，1-开启）',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     INT          NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统公告表';

-- ============================================================================
-- 10. cms_banner - Banner表
-- Entity: Banner extends BaseEntity
-- ============================================================================
DROP TABLE IF EXISTS `cms_banner`;
CREATE TABLE `cms_banner` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title`       VARCHAR(200) NOT NULL                COMMENT '标题',
    `image_url`   VARCHAR(500) NOT NULL                COMMENT '图片地址',
    `link_url`    VARCHAR(500) DEFAULT NULL            COMMENT '跳转链接',
    `position`    VARCHAR(64)  DEFAULT NULL            COMMENT '位置：home-首页等',
    `sort_order`  INT          DEFAULT 0              COMMENT '排序号',
    `status`      INT          NOT NULL DEFAULT 1      COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     INT          NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_position` (`position`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Banner表';

-- ============================================================================
-- 11. cms_partner - 合作伙伴表
-- Entity: Partner extends BaseEntity
-- ============================================================================
DROP TABLE IF EXISTS `cms_partner`;
CREATE TABLE `cms_partner` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`        VARCHAR(200) NOT NULL                COMMENT '合作伙伴名称',
    `logo`        VARCHAR(500) DEFAULT NULL            COMMENT 'Logo图片',
    `website`     VARCHAR(500) DEFAULT NULL            COMMENT '官网地址',
    `description` VARCHAR(500) DEFAULT NULL            COMMENT '描述',
    `sort_order`  INT          DEFAULT 0              COMMENT '排序号',
    `status`      INT          NOT NULL DEFAULT 1      COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     INT          NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='合作伙伴表';

-- ============================================================================
-- 12. cms_category - 分类表
-- Entity: Category extends BaseEntity
-- ============================================================================
DROP TABLE IF EXISTS `cms_category`;
CREATE TABLE `cms_category` (
    `id`          BIGINT      NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `name`        VARCHAR(128) NOT NULL               COMMENT '分类名称',
    `type`        VARCHAR(64) DEFAULT NULL            COMMENT '分类类型：news-资讯，course-课程，practice-练习',
    `parent_id`   BIGINT      DEFAULT 0               COMMENT '父分类ID',
    `sort_order`  INT         DEFAULT 0               COMMENT '排序号',
    `status`      INT         NOT NULL DEFAULT 1       COMMENT '状态：0-禁用，1-启用',
    `create_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     INT         NOT NULL DEFAULT 0       COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_type` (`type`),
    INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='分类表';

-- ============================================================================
-- 13. cms_news - 资讯表
-- Entity: News extends BaseEntity
-- ============================================================================
DROP TABLE IF EXISTS `cms_news`;
CREATE TABLE `cms_news` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title`       VARCHAR(200) NOT NULL                COMMENT '标题',
    `summary`     VARCHAR(500) DEFAULT NULL            COMMENT '摘要',
    `content`     LONGTEXT     DEFAULT NULL            COMMENT '内容（富文本）',
    `cover_image` VARCHAR(500) DEFAULT NULL            COMMENT '封面图',
    `category_id` BIGINT       DEFAULT NULL            COMMENT '分类ID',
    `author`      VARCHAR(128) DEFAULT NULL            COMMENT '作者',
    `source`      VARCHAR(128) DEFAULT NULL            COMMENT '来源',
    `view_count`  INT          DEFAULT 0              COMMENT '浏览量',
    `sort_order`  INT          DEFAULT 0              COMMENT '排序号',
    `is_pinned`   INT          DEFAULT 0              COMMENT '是否置顶：0-否，1-是',
    `status`      INT          NOT NULL DEFAULT 0      COMMENT '状态：0-草稿，1-已发布，2-下架',
    `publish_time` DATETIME     DEFAULT NULL            COMMENT '发布时间',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     INT          NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_category_id` (`category_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_publish_time` (`publish_time`),
    INDEX `idx_is_pinned` (`is_pinned`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='资讯表';

-- ============================================================================
-- 14. course_info - 课程信息表
-- Entity: Course extends BaseEntity
-- ============================================================================
DROP TABLE IF EXISTS `course_info`;
CREATE TABLE `course_info` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title`         VARCHAR(200) NOT NULL                COMMENT '课程标题',
    `subtitle`      VARCHAR(200) DEFAULT NULL            COMMENT '课程副标题',
    `cover_image`   VARCHAR(500) DEFAULT NULL            COMMENT '课程封面图URL',
    `description`   VARCHAR(1000) DEFAULT NULL           COMMENT '课程简介',
    `content`       LONGTEXT     DEFAULT NULL            COMMENT '课程详情（富文本）',
    `teacher_id`    BIGINT       DEFAULT NULL            COMMENT '讲师ID',
    `teacher_name`  VARCHAR(128) DEFAULT NULL            COMMENT '讲师名称',
    `price`         DECIMAL(10,2) DEFAULT 0.00          COMMENT '课程价格',
    `original_price` DECIMAL(10,2) DEFAULT 0.00        COMMENT '原价',
    `status`        INT          NOT NULL DEFAULT 0      COMMENT '课程状态（0-草稿，1-已发布，2-已下架，3-已归档）',
    `category_id`  BIGINT       DEFAULT NULL            COMMENT '课程分类ID',
    `tags`          VARCHAR(500) DEFAULT NULL            COMMENT '课程标签（逗号分隔）',
    `total_lessons` INT          DEFAULT 0              COMMENT '总课时数',
    `total_duration` INT         DEFAULT 0              COMMENT '总时长（秒）',
    `student_count` INT          DEFAULT 0              COMMENT '学习人数',
    `rating`        DECIMAL(2,1) DEFAULT 0.0           COMMENT '评分',
    `sort_order`    INT          DEFAULT 0              COMMENT '排序号',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       INT          NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_teacher_id` (`teacher_id`),
    INDEX `idx_category_id` (`category_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_rating` (`rating`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程信息表';


-- ============================================================================
-- 16. course_video - 课程视频表（无章节层级，视频直接挂载课程）
-- Entity: CourseVideo extends BaseEntity
-- ============================================================================
DROP TABLE IF EXISTS `course_video`;
CREATE TABLE `course_video` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `course_id`   BIGINT       NOT NULL                COMMENT '课程ID',
    `title`       VARCHAR(200) NOT NULL                COMMENT '视频名称',
    `video_url`   VARCHAR(500) DEFAULT NULL            COMMENT '视频地址',
    `sort_order`  INT          DEFAULT 0              COMMENT '排序号',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     INT          NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_course_id` (`course_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程视频表';

-- ============================================================================
-- 17. course_purchase - 课程购买记录表
-- Entity: CoursePurchase extends BaseEntity
-- ============================================================================
DROP TABLE IF EXISTS `course_purchase`;
CREATE TABLE `course_purchase` (
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

-- ============================================================================
-- 18. video_comment - 视频评论表
-- Entity: VideoComment extends BaseEntity
-- ============================================================================
DROP TABLE IF EXISTS `video_comment`;
CREATE TABLE `video_comment` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `video_id`    BIGINT       NOT NULL                COMMENT '视频ID',
    `user_id`     BIGINT       NOT NULL                COMMENT '用户ID',
    `username`    VARCHAR(64)  DEFAULT NULL            COMMENT '用户名',
    `avatar`      VARCHAR(500) DEFAULT NULL            COMMENT '用户头像',
    `content`     TEXT          NOT NULL                COMMENT '评论内容',
    `parent_id`   BIGINT       DEFAULT 0              COMMENT '父评论ID（0为顶级评论）',
    `like_count`  INT          DEFAULT 0              COMMENT '点赞数',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     INT          NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_video_id` (`video_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_parent_id` (`parent_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频评论表';

-- ============================================================================
-- 19. video_danmaku - 视频弹幕表
-- Entity: VideoDanmaku (implements Serializable)
-- ============================================================================
DROP TABLE IF EXISTS `video_danmaku`;
CREATE TABLE `video_danmaku` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `video_id`     BIGINT       NOT NULL                COMMENT '视频ID',
    `user_id`      BIGINT       DEFAULT NULL            COMMENT '用户ID',
    `username`     VARCHAR(64)  DEFAULT NULL            COMMENT '用户名',
    `content`      VARCHAR(500) NOT NULL                COMMENT '弹幕内容',
    `time_position` INT          DEFAULT NULL            COMMENT '弹幕出现时间（秒）',
    `color`        VARCHAR(16)  DEFAULT NULL            COMMENT '弹幕颜色',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_video_id` (`video_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='视频弹幕表';

-- ============================================================================
-- 20. video_progress - 视频学习进度表
-- Entity: VideoProgress (implements Serializable)
-- ============================================================================
DROP TABLE IF EXISTS `video_progress`;
CREATE TABLE `video_progress` (
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

-- ============================================================================
-- 21. teach_class - 班级表
-- Entity: TeachClass extends BaseEntity
-- ============================================================================
DROP TABLE IF EXISTS `teach_class`;
CREATE TABLE `teach_class` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `class_name`      VARCHAR(128) NOT NULL                COMMENT '班级名称',
    `description`     VARCHAR(500) DEFAULT NULL            COMMENT '班级描述',
    `teacher_id`      BIGINT       NOT NULL                COMMENT '教师ID',
    `course_id`       BIGINT       NOT NULL                COMMENT '课程ID',
    `max_student`     INT          DEFAULT 50              COMMENT '最大学生数',
    `current_student` INT          DEFAULT 0              COMMENT '当前学生数',
    `status`          INT          NOT NULL DEFAULT 0      COMMENT '状态（0-未开班，1-进行中，2-已结课）',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         INT          NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_teacher_id` (`teacher_id`),
    INDEX `idx_course_id` (`course_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班级表';

-- ============================================================================
-- 22. class_student - 班级学生关联表
-- Entity: ClassStudent (implements Serializable)
-- ============================================================================
DROP TABLE IF EXISTS `class_student`;
CREATE TABLE `class_student` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `class_id`    BIGINT   NOT NULL                COMMENT '班级ID',
    `user_id`     BIGINT   NOT NULL                COMMENT '用户ID',
    `join_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    `status`      INT      NOT NULL DEFAULT 0      COMMENT '状态（0-正常，1-已退出）',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_class_user` (`class_id`, `user_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='班级学生关联表';

-- ============================================================================
-- 23. class_schedule - 课程表（课表）
-- Entity: ClassSchedule (implements Serializable)
-- ============================================================================
DROP TABLE IF EXISTS `class_schedule`;
CREATE TABLE `class_schedule` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `class_id`    BIGINT       NOT NULL                COMMENT '班级ID',
    `class_date`  DATETIME     NOT NULL                COMMENT '上课日期',
    `time_slot`   VARCHAR(64)  DEFAULT NULL            COMMENT '上课时间段',
    `topic`       VARCHAR(200) DEFAULT NULL            COMMENT '主题',
    `classroom`   VARCHAR(128) DEFAULT NULL            COMMENT '教室',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_class_id` (`class_id`),
    INDEX `idx_class_date` (`class_date`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='课程安排表';

-- ============================================================================
-- 24. practice_question - 练习题表
-- Entity: PracticeQuestion extends BaseEntity
-- ============================================================================
DROP TABLE IF EXISTS `practice_question`;
CREATE TABLE `practice_question` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title`         VARCHAR(200) NOT NULL                COMMENT '题目标题',
    `description`   LONGTEXT     DEFAULT NULL            COMMENT '题目描述',
    `question_type` INT          NOT NULL DEFAULT 1      COMMENT '题目类型（1-编程题）',
    `language`      INT          DEFAULT NULL            COMMENT '编程语言',
    `difficulty`    INT          DEFAULT NULL            COMMENT '难度等级（1-简单，2-中等，3-困难）',
    `time_limit`    INT          DEFAULT NULL            COMMENT '时间限制（秒）',
    `memory_limit`  INT          DEFAULT NULL            COMMENT '内存限制（MB）',
    `template_code` TEXT          DEFAULT NULL            COMMENT '模板代码',
    `answer`        TEXT          DEFAULT NULL            COMMENT '参考答案',
    `hint`          VARCHAR(500) DEFAULT NULL            COMMENT '提示信息',
    `pass_rate`     DECIMAL(5,2) DEFAULT NULL            COMMENT '通过率',
    `submit_count`  INT          DEFAULT 0              COMMENT '提交次数',
    `pass_count`    INT          DEFAULT 0              COMMENT '通过次数',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       INT          NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_question_type` (`question_type`),
    INDEX `idx_language` (`language`),
    INDEX `idx_difficulty` (`difficulty`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='练习题表';

-- ============================================================================
-- 25. test_case - 测试用例表
-- Entity: TestCase (implements Serializable)
-- ============================================================================
DROP TABLE IF EXISTS `test_case`;
CREATE TABLE `test_case` (
    `id`              BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `question_id`     BIGINT   NOT NULL                COMMENT '题目ID',
    `input`           TEXT     NOT NULL                COMMENT '输入数据',
    `expected_output` TEXT     NOT NULL                COMMENT '预期输出',
    `is_example`      INT      DEFAULT 0              COMMENT '是否为示例用例（0-否，1-是）',
    `score`           INT      DEFAULT NULL           COMMENT '分值',
    `create_time`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='测试用例表';

-- ============================================================================
-- 26. practice_record - 练习提交记录表
-- Entity: PracticeRecord extends BaseEntity
-- 包含 update_editor.sql 增量字段: scratch_json, scratch_assets, judge_detail
-- ============================================================================
DROP TABLE IF EXISTS `practice_record`;
CREATE TABLE `practice_record` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`         BIGINT       NOT NULL                COMMENT '用户ID',
    `question_id`     BIGINT       NOT NULL                COMMENT '题目ID',
    `code`            TEXT          DEFAULT NULL            COMMENT '提交代码',
    `language`        INT          DEFAULT NULL            COMMENT '编程语言',
    `judge_status`    INT          DEFAULT 0              COMMENT '评测状态（0-待评测，1-评测中，2-通过，3-答案错误等）',
    `execute_time`    INT          DEFAULT NULL            COMMENT '执行时间（毫秒）',
    `memory_usage`    INT          DEFAULT NULL            COMMENT '内存消耗（KB）',
    `pass_case_count` INT          DEFAULT 0              COMMENT '通过用例数',
    `total_case_count` INT         DEFAULT 0              COMMENT '总用例数',
    `judge_result`    TEXT          DEFAULT NULL            COMMENT '评测结果详情（JSON）',
    `judge0_token`    VARCHAR(128) DEFAULT NULL           COMMENT 'Judge0提交ID',
    `scratch_json`   LONGTEXT     DEFAULT NULL            COMMENT 'Scratch项目JSON',
    `scratch_assets`  JSON         DEFAULT NULL            COMMENT 'Scratch素材文件列表（JSON数组）',
    `judge_detail`    JSON         DEFAULT NULL            COMMENT '判题详细结果（JSON，含各用例详情）',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         INT          NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_question_id` (`question_id`),
    INDEX `idx_user_question_status` (`user_id`, `question_id`, `judge_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='练习提交记录表';

-- ============================================================================
-- 27. practice_draft - 练习草稿表
-- Entity: PracticeDraft (implements Serializable)
-- ============================================================================
DROP TABLE IF EXISTS `practice_draft`;
CREATE TABLE `practice_draft` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键',
    `user_id`     BIGINT       NOT NULL                COMMENT '用户ID',
    `question_id` BIGINT       NOT NULL                COMMENT '题目ID',
    `code`        TEXT          DEFAULT NULL            COMMENT '代码文本',
    `language`    INT          DEFAULT NULL            COMMENT '编程语言',
    `scratch_json` LONGTEXT     DEFAULT NULL            COMMENT 'Scratch项目JSON',
    `updated_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
    `created_at`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_user_question` (`user_id`, `question_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='练习草稿表';

-- ============================================================================
-- 28. scratch_project - Scratch项目表
-- Entity: ScratchProject extends BaseEntity
-- ============================================================================
DROP TABLE IF EXISTS `scratch_project`;
CREATE TABLE `scratch_project` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`       BIGINT       NOT NULL                COMMENT '用户ID',
    `project_name`  VARCHAR(200) NOT NULL                COMMENT '项目名称',
    `description`   VARCHAR(500) DEFAULT NULL            COMMENT '项目描述',
    `project_url`   VARCHAR(500) DEFAULT NULL            COMMENT '项目文件URL（.sb3文件）',
    `thumbnail_url` VARCHAR(500) DEFAULT NULL            COMMENT '缩略图URL',
    `is_public`     INT          DEFAULT 0              COMMENT '是否公开（0-私有，1-公开）',
    `like_count`    INT          DEFAULT 0              COMMENT '点赞数',
    `view_count`    INT          DEFAULT 0              COMMENT '浏览数',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       INT          NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_is_public` (`is_public`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Scratch项目表';

-- ============================================================================
-- 29. scratch_work - Scratch自由创作作品表
-- Entity: ScratchWork extends BaseEntity
-- ============================================================================
DROP TABLE IF EXISTS `scratch_work`;
CREATE TABLE `scratch_work` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`       BIGINT       NOT NULL                COMMENT '用户ID',
    `title`         VARCHAR(200) NOT NULL                COMMENT '作品名称',
    `description`   TEXT          DEFAULT NULL            COMMENT '作品描述',
    `sb3_file_key`  VARCHAR(500) NOT NULL                COMMENT 'sb3文件在MinIO中的存储路径',
    `thumbnail_key` VARCHAR(500) DEFAULT NULL            COMMENT '缩略图在MinIO中的存储路径',
    `file_size`     BIGINT       DEFAULT 0              COMMENT 'sb3文件大小（字节）',
    `is_public`     INT          DEFAULT 0              COMMENT '是否公开（0-私有，1-公开）',
    `like_count`    INT          DEFAULT 0              COMMENT '点赞数',
    `view_count`    INT          DEFAULT 0              COMMENT '浏览数',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       INT          NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_is_public` (`is_public`),
    INDEX `idx_create_time` (`create_time`),
    INDEX `idx_user_time` (`user_id`, `create_time` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Scratch自由创作作品表';

-- ============================================================================
-- 30. exam_info - 考试信息表
-- Entity: ExamInfo extends BaseEntity
-- ============================================================================
DROP TABLE IF EXISTS `exam_info`;
CREATE TABLE `exam_info` (
    `id`                BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title`             VARCHAR(200) NOT NULL                COMMENT '考试标题',
    `description`       VARCHAR(1000) DEFAULT NULL           COMMENT '考试描述',
    `exam_type`         INT          NOT NULL                COMMENT '考试类型（1-随堂测验，2-单元测试，3-期中考试，4-期末考试，5-竞赛）',
    `course_id`         BIGINT       DEFAULT NULL            COMMENT '课程ID',
    `teacher_id`        BIGINT       DEFAULT NULL            COMMENT '创建教师ID',
    `start_time`        DATETIME     DEFAULT NULL            COMMENT '开始时间',
    `end_time`          DATETIME     DEFAULT NULL            COMMENT '结束时间',
    `duration`          INT          DEFAULT NULL            COMMENT '考试时长（分钟）',
    `total_score`       INT          DEFAULT NULL            COMMENT '总分',
    `pass_score`        INT          DEFAULT NULL            COMMENT '及格分数',
    `status`            INT          NOT NULL DEFAULT 0      COMMENT '考试状态（0-未开始，1-进行中，2-已结束，3-已批改）',
    `anti_cheat`        INT          DEFAULT 0              COMMENT '防作弊开关（0-关闭，1-开启）',
    `max_switch_screen` INT          DEFAULT 0              COMMENT '最大切屏次数',
    `show_answer`       INT          DEFAULT 0              COMMENT '是否显示答案（0-不显示，1-考试结束后显示）',
    `create_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           INT          NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_course_id` (`course_id`),
    INDEX `idx_teacher_id` (`teacher_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_start_end_time` (`start_time`, `end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试信息表';

-- ============================================================================
-- 31. exam_paper - 试卷表
-- Entity: ExamPaper extends BaseEntity
-- ============================================================================
DROP TABLE IF EXISTS `exam_paper`;
CREATE TABLE `exam_paper` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `paper_name`      VARCHAR(200) NOT NULL                COMMENT '试卷名称',
    `exam_id`         BIGINT       DEFAULT NULL            COMMENT '考试ID',
    `total_score`     INT          DEFAULT NULL            COMMENT '总分',
    `pass_score`      INT          DEFAULT NULL            COMMENT '及格分',
    `question_count`  INT          DEFAULT NULL            COMMENT '题目数量',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         INT          NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_exam_id` (`exam_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='试卷表';

-- ============================================================================
-- 32. exam_paper_question - 试卷题目关联表
-- Entity: ExamPaperQuestion (implements Serializable)
-- ============================================================================
DROP TABLE IF EXISTS `exam_paper_question`;
CREATE TABLE `exam_paper_question` (
    `id`            BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `paper_id`      BIGINT   NOT NULL                COMMENT '试卷ID',
    `question_id`   BIGINT   NOT NULL                COMMENT '题目ID',
    `question_type` INT      DEFAULT NULL            COMMENT '题目类型',
    `language`      VARCHAR(20) DEFAULT NULL         COMMENT '编程语言（代码题）',
    `template_code` TEXT     DEFAULT NULL            COMMENT '模板代码（代码题）',
    `score`         INT      DEFAULT NULL           COMMENT '分值',
    `sort_order`    INT      DEFAULT 0              COMMENT '排序号',
    `create_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_paper_id` (`paper_id`),
    INDEX `idx_question_id` (`question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='试卷题目关联表';

-- ============================================================================
-- 32.1 exam_question - 考试题目表（题库单源，P1b 新增）
-- Entity: ExamQuestion extends BaseEntity
-- ============================================================================
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

-- ============================================================================
-- 33. exam_record - 考试记录表
-- Entity: ExamRecord extends BaseEntity
-- ============================================================================
DROP TABLE IF EXISTS `exam_record`;
CREATE TABLE `exam_record` (
    `id`                 BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `exam_id`           BIGINT   NOT NULL                COMMENT '考试ID',
    `user_id`           BIGINT   NOT NULL                COMMENT '用户ID',
    `paper_id`          BIGINT   DEFAULT NULL            COMMENT '试卷ID',
    `start_time`        DATETIME DEFAULT NULL            COMMENT '开始答题时间',
    `submit_time`       DATETIME DEFAULT NULL            COMMENT '提交时间',
    `status`            INT      NOT NULL DEFAULT 0      COMMENT '考试状态（0-未开始，1-答题中，2-已交卷，3-已批改）',
    `score`             INT      DEFAULT NULL            COMMENT '得分',
    `is_passed`        INT      DEFAULT NULL            COMMENT '是否及格（0-不及格，1-及格）',
    `switch_screen_count` INT    DEFAULT 0              COMMENT '切屏次数',
    `leave_page_count`  INT      DEFAULT 0              COMMENT '离开页面次数',
    `violation_flag`    INT      DEFAULT 0              COMMENT '违规标记（0-正常，1-有违规）',
    `create_time`       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`           INT      NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_exam_id` (`exam_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_paper_id` (`paper_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试记录表';

-- ============================================================================
-- 34. exam_answer - 考试答案表
-- Entity: ExamAnswer (implements Serializable)
-- 包含 update_editor.sql 增量字段: scratch_json, scratch_assets, judge_detail
-- ============================================================================
DROP TABLE IF EXISTS `exam_answer`;
CREATE TABLE `exam_answer` (
    `id`              BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `record_id`       BIGINT   NOT NULL                COMMENT '考试记录ID',
    `question_id`     BIGINT   NOT NULL                COMMENT '题目ID',
    `user_answer`    TEXT     DEFAULT NULL            COMMENT '用户答案',
    `code`            TEXT     DEFAULT NULL            COMMENT '提交代码',
    `language`        INT      DEFAULT NULL            COMMENT '编程语言',
    `judge_status`    INT      DEFAULT NULL            COMMENT '评测状态',
    `score`           INT      DEFAULT NULL            COMMENT '得分',
    `is_correct`      INT      DEFAULT NULL            COMMENT '是否正确（0-错误，1-正确，2-部分正确）',
    `grade_status`    INT      DEFAULT NULL            COMMENT '批改状态（0-未批改，1-已批改）',
    `scratch_json`    LONGTEXT DEFAULT NULL            COMMENT 'Scratch项目JSON',
    `scratch_assets`  JSON     DEFAULT NULL            COMMENT 'Scratch素材文件列表（JSON数组）',
    `judge_detail`    JSON     DEFAULT NULL            COMMENT '判题详细结果（JSON，含各用例详情）',
    `create_time`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    INDEX `idx_record_id` (`record_id`),
    INDEX `idx_question_id` (`question_id`),
    INDEX `idx_record_question` (`record_id`, `question_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试答案表';

-- ============================================================================
-- 35. exam_grading_task - 考试批改任务表
-- Entity: ExamGradingTask extends BaseEntity
-- ============================================================================
DROP TABLE IF EXISTS `exam_grading_task`;
CREATE TABLE `exam_grading_task` (
    `id`           BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `exam_id`      BIGINT   NOT NULL                COMMENT '考试ID',
    `teacher_id`   BIGINT   NOT NULL                COMMENT '批改教师ID',
    `status`       INT      NOT NULL DEFAULT 0      COMMENT '批改状态（0-待批改，1-批改中，2-已完成）',
    `graded_count` INT      DEFAULT 0              COMMENT '已批改数量',
    `total_count`  INT      DEFAULT 0              COMMENT '总数量',
    `create_time`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      INT      NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_exam_id` (`exam_id`),
    INDEX `idx_teacher_id` (`teacher_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试批改任务表';

-- ============================================================================
-- 36. exam_grading_detail - 考试批改详情表
-- Entity: ExamGradingDetail (implements Serializable)
-- ============================================================================
DROP TABLE IF EXISTS `exam_grading_detail`;
CREATE TABLE `exam_grading_detail` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `task_id`     BIGINT       NOT NULL                COMMENT '任务ID',
    `record_id`   BIGINT       NOT NULL                COMMENT '考试记录ID',
    `answer_id`   BIGINT       NOT NULL                COMMENT '答案ID',
    `score`       INT          DEFAULT NULL            COMMENT '批改得分',
    `comment`     VARCHAR(500) DEFAULT NULL            COMMENT '批改评语',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_task_id` (`task_id`),
    INDEX `idx_record_id` (`record_id`),
    INDEX `idx_answer_id` (`answer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试批改详情表';

-- ============================================================================
-- 37. exam_grading_review - 考试批改复核表
-- Entity: ExamGradingReview (implements Serializable)
-- ============================================================================
DROP TABLE IF EXISTS `exam_grading_review`;
CREATE TABLE `exam_grading_review` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `detail_id`      BIGINT       NOT NULL                COMMENT '批改详情ID',
    `reviewer_id`    BIGINT       NOT NULL                COMMENT '复核人ID',
    `review_result`  INT          DEFAULT 0              COMMENT '复核结果（0-待复核，1-同意，2-驳回修改）',
    `review_comment`  VARCHAR(500) DEFAULT NULL            COMMENT '复核评语',
    `create_time`    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_detail_id` (`detail_id`),
    INDEX `idx_reviewer_id` (`reviewer_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试批改复核表';

-- ============================================================================
-- 38. exam_snapshot - 考试快照表
-- Entity: ExamSnapshot (implements Serializable)
-- ============================================================================
DROP TABLE IF EXISTS `exam_snapshot`;
CREATE TABLE `exam_snapshot` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `record_id`    BIGINT       NOT NULL                COMMENT '考试记录ID',
    `user_id`      BIGINT       NOT NULL                COMMENT '用户ID',
    `snapshot_type` INT         NOT NULL                COMMENT '快照类型（1-屏幕截图，2-摄像头拍照）',
    `snapshot_url` VARCHAR(500) DEFAULT NULL            COMMENT '快照URL',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_record_id` (`record_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试快照表';

-- ============================================================================
-- 39. exam_violation_log - 考试违规日志表
-- Entity: ExamViolationLog (implements Serializable)
-- ============================================================================
DROP TABLE IF EXISTS `exam_violation_log`;
CREATE TABLE `exam_violation_log` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `record_id`       BIGINT       NOT NULL                COMMENT '考试记录ID',
    `user_id`         BIGINT       NOT NULL                COMMENT '用户ID',
    `violation_type`  INT          NOT NULL                COMMENT '违规类型（1-切屏，2-离开页面，3-复制粘贴，4-其他）',
    `description`     VARCHAR(500) DEFAULT NULL            COMMENT '违规描述',
    `violation_time`  DATETIME     NOT NULL                COMMENT '违规时间',
    PRIMARY KEY (`id`),
    INDEX `idx_record_id` (`record_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='考试违规日志表';

-- ============================================================================
-- 40. competition_info - 竞赛信息表
-- Entity: CompetitionInfo extends BaseEntity
-- ============================================================================
DROP TABLE IF EXISTS `competition_info`;
CREATE TABLE `competition_info` (
    `id`                       BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `title`                    VARCHAR(200) NOT NULL                COMMENT '竞赛标题',
    `description`               TEXT          DEFAULT NULL            COMMENT '竞赛描述',
    `start_time`               DATETIME     NOT NULL                COMMENT '开始时间',
    `end_time`                 DATETIME     NOT NULL                COMMENT '结束时间',
    `status`                   INT          NOT NULL DEFAULT 0      COMMENT '竞赛状态（0-未开始，1-进行中，2-已结束）',
    `allowed_block_categories` VARCHAR(500) DEFAULT NULL            COMMENT '允许的积木分类（JSON数组）',
    `max_participants`         INT          NOT NULL DEFAULT 500    COMMENT '最大参赛人数',
    `creator_id`               BIGINT       NOT NULL                COMMENT '创建者ID',
    `create_time`              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`              DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`                  INT          NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_creator_id` (`creator_id`),
    INDEX `idx_start_end_time` (`start_time`, `end_time`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='竞赛信息表';

-- ============================================================================
-- 41. competition_participant - 竞赛参赛者表
-- Entity: CompetitionParticipant extends BaseEntity
-- ============================================================================
DROP TABLE IF EXISTS `competition_participant`;
CREATE TABLE `competition_participant` (
    `id`              BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `competition_id`  BIGINT   NOT NULL                COMMENT '竞赛ID',
    `user_id`         BIGINT   NOT NULL                COMMENT '用户ID',
    `join_time`       DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '加入时间',
    `submit_time`     DATETIME DEFAULT NULL            COMMENT '提交时间',
    `score`           INT      NOT NULL DEFAULT 0      COMMENT '得分',
    `ranking`         INT      NOT NULL DEFAULT 0      COMMENT '排名',
    `violation_count` INT      NOT NULL DEFAULT 0      COMMENT '违规次数',
    `create_time`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         INT      NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_competition_user` (`competition_id`, `user_id`, `deleted`),
    INDEX `idx_competition_id` (`competition_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_score` (`competition_id`, `score` DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='竞赛参赛者表';

-- ============================================================================
-- 42. competition_submit - 竞赛提交表（按月分表主表）
-- Entity: CompetitionSubmit (implements Serializable)
-- ============================================================================
DROP TABLE IF EXISTS `competition_submit`;
CREATE TABLE `competition_submit` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `competition_id` BIGINT      NOT NULL                COMMENT '竞赛ID',
    `user_id`       BIGINT       NOT NULL                COMMENT '用户ID',
    `sb3_data`      MEDIUMTEXT   DEFAULT NULL            COMMENT 'Scratch sb3数据（JSON）',
    `judge_result`  TEXT         DEFAULT NULL            COMMENT '判题结果（JSON）',
    `submit_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
    `ip_address`    VARCHAR(45)  DEFAULT NULL            COMMENT '提交IP地址',
    PRIMARY KEY (`id`),
    INDEX `idx_competition_id` (`competition_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_submit_time` (`submit_time`),
    INDEX `idx_competition_user` (`competition_id`, `user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='竞赛提交表（按月分表）';

-- ============================================================================
-- 43. pay_order - 支付订单表
-- Entity: PayOrder extends BaseEntity
-- ============================================================================
DROP TABLE IF EXISTS `pay_order`;
CREATE TABLE `pay_order` (
    `id`          BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `order_no`    VARCHAR(64)   NOT NULL                COMMENT '订单号',
    `user_id`     BIGINT        NOT NULL                COMMENT '用户ID',
    `product_type` INT           NOT NULL                COMMENT '商品类型（1-课程，2-会员，3-积分商品）',
    `product_id`  BIGINT        NOT NULL                COMMENT '商品ID',
    `product_name` VARCHAR(200)  DEFAULT NULL            COMMENT '商品名称',
    `amount`      DECIMAL(10,2) NOT NULL                COMMENT '订单金额',
    `pay_amount`  DECIMAL(10,2) DEFAULT 0.00            COMMENT '实付金额',
    `pay_channel` INT           DEFAULT NULL            COMMENT '支付渠道（1-微信，2-支付宝，3-余额，4-优惠券）',
    `status`       INT           NOT NULL DEFAULT 0      COMMENT '订单状态（0-待支付，1-已支付，2-已取消，3-退款中，4-已退款，5-已过期）',
    `pay_time`    DATETIME      DEFAULT NULL            COMMENT '支付时间',
    `expire_time` DATETIME      DEFAULT NULL            COMMENT '过期时间',
    `trade_no`    VARCHAR(128)  DEFAULT NULL            COMMENT '第三方交易号',
    `create_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     INT           NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_order_no` (`order_no`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_status` (`status`),
    INDEX `idx_pay_time` (`pay_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付订单表';

-- ============================================================================
-- 44. pay_refund - 退款记录表
-- Entity: PayRefund extends BaseEntity
-- ============================================================================
DROP TABLE IF EXISTS `pay_refund`;
CREATE TABLE `pay_refund` (
    `id`           BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `refund_no`    VARCHAR(64)   NOT NULL                COMMENT '退款单号',
    `order_id`     BIGINT        NOT NULL                COMMENT '订单ID',
    `order_no`     VARCHAR(64)   NOT NULL                COMMENT '订单号',
    `user_id`      BIGINT        NOT NULL                COMMENT '用户ID',
    `refund_amount` DECIMAL(10,2) NOT NULL               COMMENT '退款金额',
    `reason`       VARCHAR(500)  DEFAULT NULL           COMMENT '退款原因',
    `status`       INT           NOT NULL DEFAULT 0      COMMENT '退款状态（0-待处理，1-已退款，2-已拒绝）',
    `refund_time`  DATETIME      DEFAULT NULL            COMMENT '退款时间',
    `trade_no`     VARCHAR(128)  DEFAULT NULL           COMMENT '第三方退款单号',
    `create_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`      INT           NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_refund_no` (`refund_no`),
    INDEX `idx_order_id` (`order_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='退款记录表';

-- ============================================================================
-- 45. pay_callback_log - 支付回调日志表
-- Entity: PayCallbackLog (implements Serializable)
-- ============================================================================
DROP TABLE IF EXISTS `pay_callback_log`;
CREATE TABLE `pay_callback_log` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `pay_channel`   INT          DEFAULT NULL            COMMENT '支付渠道',
    `order_no`      VARCHAR(64)  DEFAULT NULL            COMMENT '订单号',
    `callback_data` TEXT          DEFAULT NULL            COMMENT '回调参数（JSON）',
    `status`        INT          DEFAULT NULL            COMMENT '处理状态（0-待处理，1-处理成功，2-处理失败）',
    `error_msg`     VARCHAR(500) DEFAULT NULL            COMMENT '错误信息',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_order_no` (`order_no`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='支付回调日志表';

-- ============================================================================
-- 46. membership - 会员表
-- Entity: Membership extends BaseEntity
-- ============================================================================
DROP TABLE IF EXISTS `membership`;
CREATE TABLE `membership` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `membership_name` VARCHAR(128) NOT NULL                COMMENT '会员名称',
    `level`           INT           NOT NULL                COMMENT '会员等级（1-月度，2-季度，3-年度）',
    `price`           DECIMAL(10,2) NOT NULL               COMMENT '价格',
    `valid_days`      INT           NOT NULL               COMMENT '有效期（天）',
    `discount`         INT           DEFAULT NULL            COMMENT '折扣比例（如80表示8折）',
    `status`           INT           NOT NULL DEFAULT 1      COMMENT '状态（0-下架，1-上架）',
    `description`      VARCHAR(500)  DEFAULT NULL           COMMENT '描述',
    `create_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`          INT           NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_level` (`level`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='会员表';

-- ============================================================================
-- 47. coupon - 优惠券表
-- Entity: Coupon extends BaseEntity
-- ============================================================================
DROP TABLE IF EXISTS `coupon`;
CREATE TABLE `coupon` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `coupon_name`    VARCHAR(128) NOT NULL                COMMENT '优惠券名称',
    `coupon_type`    INT           NOT NULL                COMMENT '优惠券类型（1-满减券，2-折扣券，3-无门槛券）',
    `min_amount`     DECIMAL(10,2) DEFAULT NULL           COMMENT '满减门槛金额',
    `discount_value` DECIMAL(10,2) DEFAULT NULL           COMMENT '优惠金额/折扣值',
    `total_count`    INT           DEFAULT NULL            COMMENT '发放总量',
    `received_count` INT           DEFAULT 0              COMMENT '已领取数量',
    `valid_start_time` DATETIME    DEFAULT NULL           COMMENT '有效期开始',
    `valid_end_time`   DATETIME    DEFAULT NULL           COMMENT '有效期结束',
    `status`          INT         NOT NULL DEFAULT 1      COMMENT '状态（0-停用，1-启用）',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         INT         NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_coupon_type` (`coupon_type`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='优惠券表';

-- ============================================================================
-- 48. group_buy_activity - 团购活动表
-- Entity: GroupBuyActivity extends BaseEntity
-- ============================================================================
DROP TABLE IF EXISTS `group_buy_activity`;
CREATE TABLE `group_buy_activity` (
    `id`            BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `activity_name` VARCHAR(200) NOT NULL                COMMENT '活动名称',
    `course_id`     BIGINT        NOT NULL                COMMENT '关联课程ID',
    `original_price` DECIMAL(10,2) NOT NULL              COMMENT '原价',
    `group_price`   DECIMAL(10,2) NOT NULL                COMMENT '团购价',
    `group_size`    INT           NOT NULL                COMMENT '成团人数',
    `start_time`    DATETIME      DEFAULT NULL            COMMENT '开始时间',
    `end_time`      DATETIME      DEFAULT NULL            COMMENT '结束时间',
    `status`        INT           NOT NULL DEFAULT 0      COMMENT '状态（0-未开始，1-进行中，2-已结束）',
    `create_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       INT           NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_course_id` (`course_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团购活动表';

-- ============================================================================
-- 49. group_buy_record - 团购记录表
-- Entity: GroupBuyRecord (implements Serializable)
-- ============================================================================
DROP TABLE IF EXISTS `group_buy_record`;
CREATE TABLE `group_buy_record` (
    `id`           BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `activity_id`  BIGINT       NOT NULL                COMMENT '活动ID',
    `user_id`      BIGINT       NOT NULL                COMMENT '用户ID',
    `leader_id`    BIGINT       NOT NULL                COMMENT '团长用户ID',
    `group_no`     VARCHAR(64)  NOT NULL                COMMENT '团购组号',
    `status`       INT          NOT NULL DEFAULT 0      COMMENT '参团状态（0-待成团，1-已成团，2-已过期）',
    `create_time`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_activity_id` (`activity_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_group_no` (`group_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='团购记录表';

-- ============================================================================
-- 50. user_coupon - 用户优惠券表
-- Entity: UserCoupon (implements Serializable)
-- ============================================================================
DROP TABLE IF EXISTS `user_coupon`;
CREATE TABLE `user_coupon` (
    `id`           BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`      BIGINT   NOT NULL                COMMENT '用户ID',
    `coupon_id`    BIGINT   NOT NULL                COMMENT '优惠券ID',
    `status`       INT      NOT NULL DEFAULT 0      COMMENT '使用状态（0-未使用，1-已使用，2-已过期）',
    `use_time`     DATETIME DEFAULT NULL            COMMENT '使用时间',
    `order_id`    BIGINT   DEFAULT NULL            COMMENT '使用的订单ID',
    `create_time`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_coupon_id` (`coupon_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户优惠券表';

-- ============================================================================
-- 51. file_record - 文件记录表
-- Entity: FileRecord extends BaseEntity
-- ============================================================================
DROP TABLE IF EXISTS `file_record`;
CREATE TABLE `file_record` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `file_name`     VARCHAR(256) NOT NULL                COMMENT '文件名',
    `original_name` VARCHAR(256) DEFAULT NULL            COMMENT '原始文件名',
    `file_path`     VARCHAR(500) NOT NULL                COMMENT '文件路径（MinIO objectName）',
    `file_url`      VARCHAR(500) DEFAULT NULL            COMMENT '文件URL',
    `file_size`     BIGINT       DEFAULT 0              COMMENT '文件大小（字节）',
    `file_type`     VARCHAR(128) DEFAULT NULL            COMMENT '文件类型（MIME）',
    `extension`     VARCHAR(32)  DEFAULT NULL            COMMENT '文件扩展名',
    `bucket`        VARCHAR(128) DEFAULT NULL            COMMENT '存储桶',
    `upload_user_id` BIGINT       DEFAULT NULL            COMMENT '上传用户ID',
    `biz_type`      VARCHAR(64)  DEFAULT NULL            COMMENT '业务类型（avatar-头像，course-课程，exam-考试等）',
    `biz_id`        BIGINT       DEFAULT NULL            COMMENT '业务ID',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       INT          NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_bucket` (`bucket`),
    INDEX `idx_biz_type` (`biz_type`),
    INDEX `idx_upload_user_id` (`upload_user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='文件记录表';

-- ============================================================================
-- 52. message - 消息表
-- Entity: Message extends BaseEntity
-- ============================================================================
DROP TABLE IF EXISTS `message`;
CREATE TABLE `message` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `receiver_id` BIGINT       NOT NULL                COMMENT '接收用户ID',
    `sender_id`   BIGINT       NOT NULL                COMMENT '发送用户ID（0表示系统消息）',
    `title`       VARCHAR(200) DEFAULT NULL            COMMENT '消息标题',
    `content`     TEXT          NOT NULL                COMMENT '消息内容',
    `message_type` INT         NOT NULL                COMMENT '消息类型（1-系统通知，2-课程通知，3-考试通知，4-互动消息）',
    `is_read`     INT          NOT NULL DEFAULT 0      COMMENT '是否已读（0-未读，1-已读）',
    `read_time`   DATETIME     DEFAULT NULL            COMMENT '阅读时间',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`     INT          NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_receiver_id` (`receiver_id`),
    INDEX `idx_sender_id` (`sender_id`),
    INDEX `idx_is_read` (`is_read`),
    INDEX `idx_message_type` (`message_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息表';

-- ============================================================================
-- 53. message_template - 消息模板表
-- Entity: MessageTemplate extends BaseEntity
-- ============================================================================
DROP TABLE IF EXISTS `message_template`;
CREATE TABLE `message_template` (
    `id`            BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `template_code` VARCHAR(128) NOT NULL                COMMENT '模板编码',
    `template_name` VARCHAR(128) NOT NULL                COMMENT '模板名称',
    `content`       TEXT          NOT NULL                COMMENT '模板内容（支持变量占位符）',
    `message_type`  INT          NOT NULL                COMMENT '消息类型',
    `send_channel`  INT          NOT NULL                COMMENT '发送渠道（1-站内信，2-短信，3-邮件）',
    `status`        INT          NOT NULL DEFAULT 1      COMMENT '状态（0-禁用，1-启用）',
    `create_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`       INT          NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_template_code` (`template_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='消息模板表';

-- ============================================================================
-- 54. sms_log - 短信发送日志表
-- Entity: SmsLog (implements Serializable)
-- ============================================================================
DROP TABLE IF EXISTS `sms_log`;
CREATE TABLE `sms_log` (
    `id`               BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `phone`            VARCHAR(20)  NOT NULL                COMMENT '手机号',
    `template_code`    VARCHAR(128) DEFAULT NULL            COMMENT '模板编码',
    `content`          TEXT          DEFAULT NULL            COMMENT '发送内容',
    `status`           INT          DEFAULT NULL            COMMENT '发送状态（0-失败，1-成功）',
    `third_party_msg_id` VARCHAR(128) DEFAULT NULL          COMMENT '第三方返回消息ID',
    `error_msg`        VARCHAR(500) DEFAULT NULL            COMMENT '错误信息',
    `create_time`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_phone` (`phone`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='短信发送日志表';

-- ============================================================================
-- 55. medal - 勋章表
-- Entity: Medal extends BaseEntity
-- ============================================================================
DROP TABLE IF EXISTS `medal`;
CREATE TABLE `medal` (
    `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `medal_name`      VARCHAR(128) NOT NULL                COMMENT '勋章名称',
    `icon_url`        VARCHAR(500) DEFAULT NULL            COMMENT '勋章图标URL',
    `description`     VARCHAR(500) DEFAULT NULL            COMMENT '勋章描述',
    `condition_type`  INT          NOT NULL                COMMENT '获取条件类型（1-学习天数，2-完成课程，3-考试分数，4-积分）',
    `condition_value` INT          NOT NULL                COMMENT '获取条件值',
    `sort_order`      INT          DEFAULT 0              COMMENT '排序号',
    `create_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`         INT          NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_condition_type` (`condition_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='勋章表';

-- ============================================================================
-- 56. user_medal - 用户勋章表
-- Entity: UserMedal (implements Serializable)
-- ============================================================================
DROP TABLE IF EXISTS `user_medal`;
CREATE TABLE `user_medal` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     BIGINT   NOT NULL                COMMENT '用户ID',
    `medal_id`    BIGINT   NOT NULL                COMMENT '勋章ID',
    `obtain_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '获取时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_user_medal` (`user_id`, `medal_id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_medal_id` (`medal_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户勋章表';

-- ============================================================================
-- 57. user_level - 用户等级表
-- Entity: UserLevel (implements Serializable)
-- ============================================================================
DROP TABLE IF EXISTS `user_level`;
CREATE TABLE `user_level` (
    `id`              BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`         BIGINT   NOT NULL                COMMENT '用户ID',
    `level`           INT      NOT NULL DEFAULT 1      COMMENT '等级',
    `level_name`      VARCHAR(64) DEFAULT NULL         COMMENT '等级名称',
    `experience`      INT      NOT NULL DEFAULT 0      COMMENT '当前经验值',
    `next_level_exp`  INT      DEFAULT NULL            COMMENT '升级所需经验值',
    `update_time`     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户等级表';

-- ============================================================================
-- 58. user_point - 用户积分表
-- Entity: UserPoint (implements Serializable)
-- ============================================================================
DROP TABLE IF EXISTS `user_point`;
CREATE TABLE `user_point` (
    `id`            BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`       BIGINT   NOT NULL                COMMENT '用户ID',
    `total_point`   INT      NOT NULL DEFAULT 0      COMMENT '当前积分',
    `total_earned`  INT      NOT NULL DEFAULT 0      COMMENT '累计获得积分',
    `total_spent`   INT      NOT NULL DEFAULT 0      COMMENT '累计消费积分',
    `update_time`   DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户积分表';

-- ============================================================================
-- 59. point_log - 积分日志表
-- Entity: PointLog (implements Serializable)
-- ============================================================================
DROP TABLE IF EXISTS `point_log`;
CREATE TABLE `point_log` (
    `id`           BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`      BIGINT   NOT NULL                COMMENT '用户ID',
    `change_point` INT      NOT NULL                COMMENT '积分变动值（正数增加，负数减少）',
    `after_point`  INT      NOT NULL                COMMENT '变动后积分',
    `change_type`  INT      NOT NULL                COMMENT '变动类型（1-签到，2-学习，3-考试，4-兑换，5-管理员调整）',
    `description`  VARCHAR(500) DEFAULT NULL        COMMENT '变动描述',
    `biz_id`       BIGINT   DEFAULT NULL            COMMENT '关联业务ID',
    `create_time`  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_change_type` (`change_type`),
    INDEX `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分日志表';

-- ============================================================================
-- 60. point_goods - 积分商品表
-- Entity: PointGoods extends BaseEntity
-- ============================================================================
DROP TABLE IF EXISTS `point_goods`;
CREATE TABLE `point_goods` (
    `id`              BIGINT        NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `goods_name`      VARCHAR(200) NOT NULL                COMMENT '商品名称',
    `image_url`       VARCHAR(500) DEFAULT NULL            COMMENT '商品图片',
    `need_point`      INT           NOT NULL                COMMENT '所需积分',
    `original_price`  DECIMAL(10,2) DEFAULT NULL           COMMENT '原价',
    `stock`           INT           NOT NULL DEFAULT 0      COMMENT '库存数量',
    `exchanged_count` INT           NOT NULL DEFAULT 0      COMMENT '已兑换数量',
    `status`           INT          NOT NULL DEFAULT 1      COMMENT '状态（0-下架，1-上架）',
    `description`     VARCHAR(500)  DEFAULT NULL           COMMENT '商品描述',
    `create_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time`      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted`          INT           NOT NULL DEFAULT 0      COMMENT '逻辑删除标识（0-未删除，1-已删除）',
    PRIMARY KEY (`id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分商品表';

-- ============================================================================
-- 61. point_exchange - 积分兑换记录表
-- Entity: PointExchange (implements Serializable)
-- ============================================================================
DROP TABLE IF EXISTS `point_exchange`;
CREATE TABLE `point_exchange` (
    `id`          BIGINT   NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     BIGINT   NOT NULL                COMMENT '用户ID',
    `goods_id`    BIGINT   NOT NULL                COMMENT '商品ID',
    `cost_point`  INT      NOT NULL                COMMENT '消耗积分',
    `status`      INT      NOT NULL DEFAULT 0      COMMENT '兑换状态（0-待发货，1-已发货，2-已完成）',
    `create_time` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    INDEX `idx_user_id` (`user_id`),
    INDEX `idx_goods_id` (`goods_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='积分兑换记录表';

-- ============================================================================
-- 分表存储过程：自动创建当月竞赛提交分表
-- ============================================================================
DELIMITER //
DROP PROCEDURE IF EXISTS `create_competition_submit_monthly` //
CREATE PROCEDURE `create_competition_submit_monthly`(IN `p_year_month` VARCHAR(6))
BEGIN
    DECLARE `v_table_name` VARCHAR(64);
    SET `v_table_name` = CONCAT('competition_submit_', `p_year_month`);

    SET @sql = CONCAT(
        'CREATE TABLE IF NOT EXISTS `', `v_table_name`, '` LIKE `competition_submit`'
    );
    PREPARE stmt FROM @sql;
    EXECUTE stmt;
    DEALLOCATE PREPARE stmt;
END //
DELIMITER ;

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================================================
-- 初始数据
-- ============================================================================

-- 管理员角色
INSERT INTO `sys_role` (`id`, `role_code`, `role_name`, `description`, `sort_order`, `status`, `create_time`, `update_time`, `deleted`) VALUES
(1, 'ADMIN', '系统管理员', '系统最高权限管理员', 1, 1, NOW(), NOW(), 0),
(2, 'TEACHER', '教师', '教师角色，可创建课程、考试等', 2, 1, NOW(), NOW(), 0),
(3, 'OPERATOR', '运营', '运营角色，管理内容、营销活动等', 3, 1, NOW(), NOW(), 0),
(4, 'STUDENT', '学生', '学生角色，学习课程、参加考试等', 4, 1, NOW(), NOW(), 0);

-- 管理员用户 (密码: admin123 -> BCrypt加密)
INSERT INTO `sys_user` (`id`, `username`, `password`, `nickname`, `real_name`, `phone`, `email`, `avatar`, `role_type`, `status`, `create_time`, `update_time`, `deleted`) VALUES
(1, 'admin', 'f8f1a873a8be12ed67a20765f55dc705', '系统管理员', '管理员', '13800000000', 'admin@magao.com', NULL, 'ADMIN', 1, NOW(), NOW(), 0);

-- 管理员角色关联
INSERT INTO `sys_user_role` (`id`, `user_id`, `role_id`, `create_time`) VALUES
(1, 1, 1, NOW());

-- 用户积分初始化
INSERT INTO `user_point` (`id`, `user_id`, `total_point`, `total_earned`, `total_spent`, `update_time`) VALUES
(1, 1, 0, 0, 0, NOW());

-- 用户等级初始化
INSERT INTO `user_level` (`id`, `user_id`, `level`, `level_name`, `experience`, `next_level_exp`, `update_time`) VALUES
(1, 1, 1, '初级学员', 0, 100, NOW());

-- 基础系统配置
INSERT INTO `sys_config` (`id`, `config_key`, `config_value`, `description`, `config_group`, `create_time`, `update_time`, `deleted`) VALUES
(1, 'site_name', 'Magao编程教育平台', '站点名称', 'site', NOW(), NOW(), 0),
(2, 'site_description', '青少年编程学习平台', '站点描述', 'site', NOW(), NOW(), 0),
(3, 'default_avatar', '/static/default-avatar.png', '默认头像地址', 'user', NOW(), NOW(), 0),
(4, 'register_enabled', 'true', '是否开启注册', 'system', NOW(), NOW(), 0),
(5, 'upload_max_size', '104857600', '上传文件最大大小（字节）', 'file', NOW(), NOW(), 0);
