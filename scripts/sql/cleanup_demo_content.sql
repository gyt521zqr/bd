-- ============================================================================
-- 历史演示内容校正（幂等，可重复执行）
-- ----------------------------------------------------------------------------
-- 适用：已经执行过旧版 scripts/init-db.sql 的数据库。
-- 新版 init-db.sql 已把种子数据改为中性内容；已存在的旧库需执行本脚本对齐。
--
-- 处理范围（均为「按精确旧值匹配」，不会误伤运营后来手工录入的真实内容）：
--   1. cms_banner  : 虚构赛事宣传标题
--   2. cms_news    : 虚构赛事新闻 / 官方证书、奖项、名师宣传 / 虚构浏览量
--   3. course_info : 虚构讲师姓名 / 虚构学员数 / 虚构评分
--   4. exam_info   : 冒用「全国大赛 / 码高杯 / 蓝桥杯 / CSP-J/S / 奥林匹克」
--                    等外部赛事品牌的练习赛名称
--
-- 执行时机：部署前在目标库执行一次；本地开发库同样适用。
-- 注意：这些练习赛均为平台自测用途，不代表任何官方赛事，也不授予第三方证书。
--      正式赛事信息须由运营在后台据实录入，不要改回带「全国/届/杯赛」的名称。
-- ============================================================================

-- 1. Banner ------------------------------------------------------------------
UPDATE `cms_banner`
   SET `title` = '赛事中心 · 查看练习赛', `update_time` = NOW()
 WHERE `title` = '全国青少年编程大赛报名中' AND `deleted` = 0;

-- 2. 资讯：虚构赛事宣传与虚构浏览量 -------------------------------------------
UPDATE `cms_news`
   SET `title` = '平台课程与练习赛使用说明',
       `summary` = '介绍如何在平台内选课、参加练习赛与查看成绩。',
       `content` = '<p>本平台提供编程课程学习与在线练习赛。学员可在「课程中心」选择课程，在「赛事中心」参加平台内练习赛，成绩可在个人中心查询。</p>',
       `author` = '平台运营', `source` = '平台公告', `view_count` = 0, `update_time` = NOW()
 WHERE `title` = '2026 全国青少年编程大赛报名启动' AND `deleted` = 0;

UPDATE `cms_news`
   SET `title` = 'Scratch 图形化编程入门指引',
       `summary` = '介绍 Scratch 图形化编程课程的学习顺序与适用年龄。',
       `content` = '<p>Scratch 课程通过动画故事、小游戏等趣味项目，帮助初学者建立编程思维，适合图形化编程入门阶段。</p>',
       `author` = '平台运营', `source` = '平台公告', `view_count` = 0, `update_time` = NOW()
 WHERE `title` = 'Scratch 3.0 编程启蒙课全新上线' AND `deleted` = 0;

UPDATE `cms_news`
   SET `title` = 'Python 学习路线说明',
       `summary` = '从基础语法到数据处理、算法练习的阶段性学习建议。',
       `content` = '<p>建议按「基础语法 → 函数与模块 → 数据处理 → 算法练习」的顺序学习，各阶段均配有对应课程与练习。</p>',
       `author` = '平台运营', `source` = '平台公告', `view_count` = 0, `update_time` = NOW()
 WHERE `title` = 'Python 学习路线图发布' AND `deleted` = 0;

UPDATE `cms_news`
   SET `title` = '在线练习赛须知',
       `summary` = '说明平台内练习赛的参与方式与注意事项。',
       `content` = '<p>平台内练习赛用于自测学习成果，请在规定时间内独立完成作答；练习赛成绩不代表任何官方赛事资格或奖项。</p>',
       `author` = '平台运营', `source` = '平台公告', `view_count` = 0, `update_time` = NOW()
 WHERE `title` = 'C++ 信息学奥赛集训营招募' AND `deleted` = 0;

-- 兼容更早版本的虚构新闻标题（若存在）
UPDATE `cms_news`
   SET `status` = 0, `update_time` = NOW()
 WHERE `deleted` = 0 AND (
      `title` LIKE '%码高杯%'
   OR `title` LIKE '%与华为签署%'
   OR `title` LIKE '%圆满收官%'
   OR `title` LIKE '%最具影响力教育品牌%'
   OR `title` LIKE '%教育行业盛典%'
 );

-- 3. 课程：虚构讲师 / 虚构学员数 / 虚构评分 -------------------------------------
UPDATE `course_info`
   SET `teacher_name` = '平台教研', `student_count` = 0, `rating` = 0, `update_time` = NOW()
 WHERE `deleted` = 0 AND `teacher_name` IN ('张老师', '李老师', '王老师', '赵老师', '刘老师', '陈老师');

-- 兜底：仅清零明显属于旧种子批次的虚构统计值，不动后续真实累计值
UPDATE `course_info`
   SET `student_count` = 0, `rating` = 0, `update_time` = NOW()
 WHERE `deleted` = 0 AND `student_count` IN (1256, 856, 2100, 680, 520, 430)
   AND `rating` IN (4.8, 4.9, 4.7, 4.6, 4.5);

UPDATE `course_info`
   SET `title` = 'C++算法入门', `subtitle` = '算法与数据结构基础',
       `description` = '系统学习 C++ 与基础算法，覆盖排序、递归、常用数据结构。',
       `tags` = '算法,进阶', `update_time` = NOW()
 WHERE `title` = 'C++算法竞赛' AND `deleted` = 0;

-- 4. 练习赛：去除外部赛事品牌措辞 ---------------------------------------------
UPDATE `exam_info` SET `title` = 'Python 算法阶段练习（一）',       `description` = '平台内自测练习，考查 Python 基础语法与算法能力。', `update_time` = NOW() WHERE `title` = '第六届全国少儿编程算法挑战赛' AND `deleted` = 0;
UPDATE `exam_info` SET `title` = 'Python Turtle 图形创意练习',       `description` = '平台内自测练习，考查 Turtle 图形绘制与创意表达。', `update_time` = NOW() WHERE `title` = '第五届"码高杯"Python创意编程大赛' AND `deleted` = 0;
UPDATE `exam_info` SET `title` = 'Scratch 图形化编程阶段练习',        `description` = '平台内自测练习，考查积木式编程基础概念。',       `update_time` = NOW() WHERE `title` = '第三届全国青少年Scratch图形化编程大赛' AND `deleted` = 0;
UPDATE `exam_info` SET `title` = 'GoC 图形编程阶段练习',             `description` = '平台内自测练习，考查图形绘制与循环、函数。',     `update_time` = NOW() WHERE `title` = 'GoC图形编程全国邀请赛' AND `deleted` = 0;
UPDATE `exam_info` SET `title` = 'C++ 算法与数据结构阶段练习',        `description` = '平台内自测练习，考查 C++ 基础算法与数据结构。',   `update_time` = NOW() WHERE `title` = '"CSP-J/S"青少年信息学奥林匹克竞赛' AND `deleted` = 0;
UPDATE `exam_info` SET `title` = '多语言编程综合练习',                `description` = '平台内自测练习，覆盖多种语言的综合题目。',       `update_time` = NOW() WHERE `title` = '"蓝桥杯"全国软件和信息技术专业人才大赛' AND `deleted` = 0;
UPDATE `exam_info` SET `title` = 'Python 数据处理阶段练习',           `description` = '平台内自测练习，考查 Python 数据处理基础。',     `update_time` = NOW() WHERE `title` = '全国青少年AI人工智能创新大赛' AND `deleted` = 0;
UPDATE `exam_info` SET `title` = 'C++ 算法进阶练习',                   `description` = '平台内自测练习，考查高阶算法解题。',             `update_time` = NOW() WHERE `title` = '"码高杯"C++算法精英赛' AND `deleted` = 0;
UPDATE `exam_info` SET `title` = 'Python 快速体验练习（一）',          `description` = '平台内快速体验练习，用于熟悉答题流程。',         `update_time` = NOW() WHERE `title` = 'Python编程体验赛（一）' AND `deleted` = 0;
UPDATE `exam_info` SET `title` = 'Scratch 快速体验练习',              `description` = '平台内快速体验练习。',                           `update_time` = NOW() WHERE `title` = 'Scratch创意体验赛' AND `deleted` = 0;
UPDATE `exam_info` SET `title` = 'C++ 快速体验练习',                   `description` = '平台内快速体验练习。',                           `update_time` = NOW() WHERE `title` = 'C++算法体验赛' AND `deleted` = 0;
UPDATE `exam_info` SET `title` = 'GoC 快速体验练习',                   `description` = '平台内快速体验练习。',                           `update_time` = NOW() WHERE `title` = 'GoC图形编程体验赛' AND `deleted` = 0;
UPDATE `exam_info` SET `title` = '编程基础快速体验练习',               `description` = '平台内快速体验练习。',                           `update_time` = NOW() WHERE `title` = 'AI人工智能体验赛' AND `deleted` = 0;

-- 5. 历史 Banner 死链（指向已下线的 /practice 路由）-----------------------------
UPDATE `cms_banner`
   SET `link_url` = '/course', `update_time` = NOW()
 WHERE `link_url` = '/practice' AND `deleted` = 0;
