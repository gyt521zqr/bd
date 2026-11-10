-- 成长体系演示种子数据（开发/演示用，可重复执行：先清理再插入）
USE magao_platform;

-- 1) 勋章
DELETE FROM medal;
INSERT INTO medal (id, create_time, update_time, deleted, medal_name, icon_url, description, condition_type, condition_value, sort_order) VALUES
 (1, NOW(), NOW(), 0, '初出茅庐', '/icons/medal1.png', '完成第一次学习', 2, 1, 1),
 (2, NOW(), NOW(), 0, '勤学不辍', '/icons/medal2.png', '连续学习 7 天', 1, 7, 2),
 (3, NOW(), NOW(), 0, '考试达人', '/icons/medal3.png', '通过 3 场考试', 3, 3, 3),
 (4, NOW(), NOW(), 0, '积分富翁', '/icons/medal4.png', '累计积分达 1000', 4, 1000, 4),
 (5, NOW(), NOW(), 0, '全明星', '/icons/medal5.png', '等级达到 10 级', 2, 10, 5);

-- 2) 补充学员账号（id 2~6）
INSERT IGNORE INTO sys_user (id, username, password, nickname, real_name, phone, email, avatar, role_type, status, create_time, update_time, deleted)
VALUES
 (2, 'stu02', 'devpwd', '学员小美', '小美', '13800000002', 'mei@example.com', '/avatars/2.png', 'STUDENT', 1, NOW(), NOW(), 0),
 (3, 'stu03', 'devpwd', '学员小明', '小明', '13800000003', 'ming@example.com', '/avatars/3.png', 'STUDENT', 1, NOW(), NOW(), 0),
 (4, 'stu04', 'devpwd', '学员小红', '小红', '13800000004', 'hong@example.com', '/avatars/4.png', 'STUDENT', 1, NOW(), NOW(), 0),
 (5, 'stu05', 'devpwd', '学员阿强', '阿强', '13800000005', 'qiang@example.com', '/avatars/5.png', 'STUDENT', 1, NOW(), NOW(), 0),
 (6, 'stu06', 'devpwd', '学员婷婷', '婷婷', '13800000006', 'ting@example.com', '/avatars/6.png', 'STUDENT', 1, NOW(), NOW(), 0);

-- 3) 用户积分（含已有 user 1）
DELETE FROM user_point;
INSERT INTO user_point (id, user_id, total_point, total_earned, total_spent, update_time) VALUES
 (1, 1, 60, 60, 0, NOW()),
 (2, 2, 320, 320, 0, NOW()),
 (3, 3, 880, 880, 0, NOW()),
 (4, 4, 150, 150, 0, NOW()),
 (5, 5, 540, 540, 0, NOW()),
 (6, 6, 210, 210, 0, NOW());

-- 4) 用户等级
DELETE FROM user_level;
INSERT INTO user_level (id, user_id, level, level_name, experience, next_level_exp, update_time) VALUES
 (1, 1, 2, '进阶学员', 40, 100, NOW()),
 (2, 2, 3, '资深学员', 200, 500, NOW()),
 (3, 3, 5, '学霸', 600, 1000, NOW()),
 (4, 4, 1, '初学者', 100, 200, NOW()),
 (5, 5, 4, '高手', 400, 800, NOW()),
 (6, 6, 2, '进阶学员', 100, 200, NOW());

-- 5) 积分日志（user 1：今天+10、本周-3天+20、本月-20天+30）
DELETE FROM point_log;
INSERT INTO point_log (id, user_id, change_point, after_point, change_type, description, biz_id, create_time) VALUES
 (1, 1, 10, 10, 2, '完成每日学习', NULL, NOW()),
 (2, 1, 20, 30, 2, '完成课程练习', NULL, DATE_SUB(NOW(), INTERVAL 3 DAY)),
 (3, 1, 30, 60, 3, '通过考试', NULL, DATE_SUB(NOW(), INTERVAL 20 DAY));

-- 6) 已获得勋章（user 1 获得前 2 个）
DELETE FROM user_medal;
INSERT INTO user_medal (id, user_id, medal_id, obtain_time) VALUES
 (1, 1, 1, NOW()),
 (2, 1, 2, NOW());
