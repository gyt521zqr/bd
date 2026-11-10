-- ============================================================================
-- 站点配置初始化 / 校正（幂等，可重复执行）
-- ----------------------------------------------------------------------------
-- 用途：
--   1. 补齐 GET /api/system/site-config 消费的 sys_config 键，避免前台启动时
--      因配置缺失而回退到写死值；
--   2. 纠正 init-db.sql 初始种子中与前端不一致的 site_name；
--   3. 修复 cms_banner 历史种子里指向已下线 /practice 路由的死链。
--
-- 执行时机：部署前在目标库执行一次（本地开发库同样适用）。
-- 注意：
--   - 使用 ON DUPLICATE KEY UPDATE，已存在的人工配置会被本脚本覆盖为推荐值。
--     若已在后台「系统设置」自定义过，请先删除对应行或按需调整 VALUES。
--   - icp 与联系方式刻意留空：不由脚本预置未经核实的信息，请上线前在后台
--     「系统设置」据实填写；前台在这些字段为空时显示「暂未公布」而非编造内容。
--   - maintenance_mode 必须保持 'false'，否则前台会显示全屏维护遮罩。
--   - 已执行过旧版 init-db.sql 的库，请另外执行同目录 cleanup_demo_content.sql
--     清除历史演示新闻 / 虚构赛事名 / 虚构运营数字。
-- ============================================================================

INSERT INTO `sys_config` (`config_key`, `config_value`, `description`, `config_group`, `deleted`) VALUES
  ('site_name',        '码高教育',                                   '站点名称（顶部导航与页脚）',       'site',   0),
  ('site_logo',        '',                                          '站点 Logo 图片地址，留空用文字 Logo', 'site',   0),
  ('icp',              '',                                          'ICP 备案号，备案完成后后台填写',   'site',   0),
  ('contact_phone',    '',                                          '客服电话，后台据实填写',          'site',   0),
  ('contact_email',    '',                                          '客服邮箱，后台据实填写',          'site',   0),
  ('contact_address',  '',                                          '联系地址，后台据实填写',          'site',   0),
  ('copyright',        'Copyright 2024-2026 码高少儿编程教育平台',    '页脚版权信息',                    'site',   0),
  ('nav_menu',         '[{"label":"首页","path":"/"},{"label":"赛事中心","path":"/exam"},{"label":"题库中心","path":"/question-bank"},{"label":"课程中心","path":"/course"},{"label":"创作社区","path":"/community"},{"label":"会员中心","path":"/membership"},{"label":"关于我们","path":"/about"}]',
                                                                    '顶部导航菜单 JSON 数组',          'site',   0),
  ('register_enabled', 'true',                                      '是否开放注册',                    'system', 0),
  ('maintenance_mode', 'false',                                     '维护模式开关（务必保持 false）',   'system', 0)
ON DUPLICATE KEY UPDATE
  `config_value` = VALUES(`config_value`),
  `description`  = VALUES(`description`),
  `config_group` = VALUES(`config_group`),
  `deleted`      = 0,
  `update_time`  = NOW();

-- 历史 Banner 种子曾指向已下线的 /practice 路由，统一修正为课程中心
UPDATE `cms_banner`
   SET `link_url` = '/course', `update_time` = NOW()
 WHERE `link_url` = '/practice' AND `deleted` = 0;
