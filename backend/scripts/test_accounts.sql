-- ============================================================
-- 测试账号初始化脚本（magao_platform 考试中心 / 悟空监护）
-- ============================================================
-- 使用方式（任选其一）：
--   1) 命令行：  mysql -h<host> -P3306 -u<user> -p magao_platform < backend/scripts/test_accounts.sql
--   2) 客户端（Navicat / IDEA Database）连到业务库后直接执行本文件
--
-- 密码策略：
--   本系统密码存储 = MD5(明文密码 + 固定盐 "magao_salt_2024")
--   登录接口：POST /api/auth/login  { loginType:"password", username, password }
--   下方账号统一密码为 123456，其哈希值：
--     123456 -> 4ed16b251bcf11bb8abf16a74a54c04b
--
-- 表结构说明（依据 entity：sys_user + BaseEntity）：
--   id            自增主键（此处显式指定大号段 9001xx，避免与已有数据冲突；
--                  若你的主键为 AUTO_INCREMENT 且不想显式指定，可删除 id 列与值）
--   create_time / update_time 由 MyBatis-Plus 在【应用层】填充，原生 SQL 需手动给值（用 NOW()）
--   deleted       逻辑删除，默认 0
--   status        1=启用 0=禁用
--   role_type     STUDENT / TEACHER / OPERATOR / ADMIN
--
-- 如你的实际表存在额外 NOT NULL 列（如 tenant_id / org_id），请按需补列。
-- 若 username / phone 已存在唯一冲突，请先 DELETE FROM sys_user WHERE username IN (...); 再执行。
-- ============================================================

USE magao_platform;

INSERT INTO sys_user
  (id, username, password, nickname, real_name, phone, email, role_type, status, deleted, create_time, update_time)
VALUES
  (900101, 'student01', '4ed16b251bcf11bb8abf16a74a54c04b', '测试学生一', '测试学生一', '13800000001', 'student01@test.com', 'STUDENT', 1, 0, NOW(), NOW()),
  (900102, 'student02', '4ed16b251bcf11bb8abf16a74a54c04b', '测试学生二', '测试学生二', '13800000002', 'student02@test.com', 'STUDENT', 1, 0, NOW(), NOW()),
  (900103, 'student03', '4ed16b251bcf11bb8abf16a74a54c04b', '测试学生三', '测试学生三', '13800000003', 'student03@test.com', 'STUDENT', 1, 0, NOW(), NOW());

-- 教师 / 管理员账号（密码同为 123456）：
INSERT INTO sys_user
  (id, username, password, nickname, real_name, phone, email, role_type, status, deleted, create_time, update_time)
VALUES
  (900201, 'teacher01', '4ed16b251bcf11bb8abf16a74a54c04b', '测试教师一', '测试教师一', '13800000201', 'teacher01@test.com', 'TEACHER', 1, 0, NOW(), NOW()),
  (900301, 'admin01',   '4ed16b251bcf11bb8abf16a74a54c04b', '测试管理员', '测试管理员', '13800000301', 'admin01@test.com',   'ADMIN',   1, 0, NOW(), NOW());
