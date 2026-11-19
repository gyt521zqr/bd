-- ============================================================================
-- 迁移脚本 v2 —— 正式收费上线（P0-01~P0-04）
-- 适用：已有 v1 数据库的增量升级；全新库请用 schema_init.sql / init-db.sql
-- 执行：mysql -uroot -p magao_platform < scripts/migration_v2_payment.sql
-- 注意：执行前先备份！mysql -uroot -p magao_platform > backup_before_v2.sql
-- ============================================================================

-- 1. 订单表增加优惠券关联列（下单锁定 / 支付核销）
ALTER TABLE `pay_order`
    ADD COLUMN `coupon_id` BIGINT DEFAULT NULL COMMENT '使用的优惠券ID（付费核销用）' AFTER `trade_no`;

-- 2. 用户优惠券状态补充"锁定中"(3) 语义说明；已存在 status 列，无需改结构。
--    status: 0-未使用, 1-已使用, 2-已过期, 3-已锁定(下单待支付)

-- 3. 支付回调日志表增加唯一约束防重放（业务层也做幂等，双保险）
--    说明：若历史数据存在重复 order_no+trade_no，请先清理后再加索引
-- ALTER TABLE `pay_callback_log` ADD UNIQUE INDEX `uk_order_trade` (`order_no`, `trade_no`);

-- 4. 订单号唯一索引确保幂等（若 v1 已存在则忽略）
-- ALTER TABLE `pay_order` ADD UNIQUE INDEX `uk_order_no` (`order_no`);

SELECT 'migration v2 payment done' AS result;

-- 5. 用户会员权益表（付费买会员后生效）
CREATE TABLE IF NOT EXISTS `user_membership` (
    `id`          BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id`     BIGINT       NOT NULL                COMMENT '用户ID',
    `plan_id`     BIGINT       NOT NULL                COMMENT '会员套餐ID',
    `order_id`    BIGINT       DEFAULT NULL            COMMENT '开通订单ID',
    `start_time`  DATETIME     NOT NULL                COMMENT '生效时间',
    `expire_time` DATETIME     NOT NULL                COMMENT '到期时间',
    `status`      INT          NOT NULL DEFAULT 1      COMMENT '状态（0-已过期，1-生效中）',
    `create_time` DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE INDEX `uk_user_plan` (`user_id`, `plan_id`),
    INDEX `idx_expire` (`expire_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户会员权益表';
