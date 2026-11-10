# 悟空监护 / magao-platform 部署检查清单（Deploy Checklist）

> 用途：每次发布/上线前，按顺序执行，确保「数据库 schema、后端接口、前端构建、鉴权链路、全链路」一致且畅通。
> 适用：本地开发、测试环境、生产（ECS 47.108.207.180，Docker + GitHub Actions 监听 main 分支）。
> 约定：本文以 dev profile 为例，生产请替换为 `prod`/对应环境变量，且**不要**把 dev 的短信验证码明文日志带到生产。

---

## 0. 前置依赖（一次性确认）
- [ ] JDK 17、Maven 3.9+、Node 22+
- [ ] MySQL 可达（dev: 127.0.0.1:3306 / 库 `magao_platform`）
- [ ] Redis 可达（dev: 127.0.0.1:6379）— 登录态/短信码/限流均依赖
- [ ] 对象存储 / VOD 凭据（如启用，需在环境变量配置；未配置时 VodController 已优雅降级）

---

## 1. 数据库迁移（**最先做，避免缺列导致 5xx**）
顺序执行项目自带、幂等的迁移脚本（缺任一都会直接打断鉴权或业务）：

```bash
MYSQL="mysql -h<host> -P<port> -u<user> -p<pass> magao_platform"
$MYSQL < scripts/sql/add_parent_phone.sql     # sys_user.parent_phone（绑家长，缺则注册/登录 500）
$MYSQL < scripts/sql/add_phase1_tables.sql    # course_purchase / video_progress（课程购买/进度）
# 其他历史迁移：schema_init.sql（首次建库）、以及 Phase 0~3 涉及的新表
```

校验（任选其一）：
```sql
SELECT COLUMN_NAME FROM information_schema.columns
  WHERE table_name='sys_user' AND column_name='parent_phone';   -- 应返回 1 行
SHOW TABLES LIKE 'course_purchase';   -- 应存在
SHOW TABLES LIKE 'video_progress';    -- 应存在
```

⚠️ **曾经的事故**：本地库未执行 `add_parent_phone.sql`，导致 `POST /api/auth/register` 与 `/api/auth/login` 返回
`BadSqlGrammarException: Unknown column 'parent_phone'`，整条鉴权链 500。上线务必把迁移纳入 CI/CD 或首次建库脚本。

---

## 2. 后端编译与测试
```bash
cd backend
mvn -o compile                     # 编译，应 BUILD SUCCESS
mvn -o test                        # 跑单测/集成测试（含支付→入学链 PayOrderEnrollmentTest）
# 全链路鉴权冒烟（带 token 调各模块受保护接口，断言非 404/5xx）：
mvn -o test -Dtest=FullChainSmokeTest
mvn -o package -DskipTests         # 打 jar，产出 target/magao-backend-1.0.0.jar
```
预期：编译 0 错；`PayOrderEnrollmentTest` 通过（下单→写 `course_purchase`+`student_count+1`，幂等）；`FullChainSmokeTest` 全绿。
`FullChainSmokeTest` 另含 `publicSiteConfigShouldBeAccessibleWithoutToken`：断言 `/api/system/site-config`、
`/api/system/notices`、`/api/system/config/{key}`、`/api/banner`、`/api/homepage/config` 在**无 Token** 时返回 200，
防止 JwtAuthFilter / SecurityConfig 白名单漏配导致前台游客拉不到导航、公告、轮播。

---

## 3. 前端构建
```bash
cd frontend
npm install
npm run build                      # 生产构建，应 BUILD SUCCESS（dist/ 生成）
```
注意：
- 历史上偶发 monaco-editor `commonjs--resolver` worker `JS heap out of memory`（`vue-tsc` 已过，仅 vite 阶段崩），
  非代码缺陷。处理：确认无遗留 Java/Node worker 进程占用内存 → `NODE_OPTIONS=--max-old-space-size=8192` 重跑。
- `dist/` 文件数较多时，Vite 默认清空 outDir 会触发批量删除守卫；可用
  `npx vite build --no-emptyOutDir`（直接覆盖产物）或 `find dist -mindepth 1 -delete` 预清后再构建。
- 确认 `dist/` 中已无已下线的 `PracticeListPage`（旧「题库练习」套件已删除）。

---

## 4. 启动与就绪探针
```bash
# 后端（dev 举例）
java -jar backend/target/magao-backend-1.0.0.jar --spring.profiles.active=dev --server.port=8080
# 就绪：日志出现 "Started MagaoApplication" 且
curl -s -o /dev/null -w "%{http_code}\n" "http://localhost:8080/api/news?page=1&size=1"   # 期望 200（公开接口）
```
容器化部署时：确认 `Dockerfile` 暴露端口、`docker-compose`/Actions 已挂载正确 profile 与 secrets。

---

## 5. 契约校验（前端 API ↔ 后端路由）
```bash
cd <项目根>
NO_PROXY=localhost,127.0.0.1 no_proxy=localhost,127.0.0.1 \
  bash scripts/contract-check.sh http://localhost:8080
```
预期：**通过 156 / 失败 0**（原 151 项 + 站点公开配置/CMS 5 项，无 404 漂移，分页信封结构正确）。
说明：脚本以 401 视为「路由存在」、404 为「漂移」。仅验证路由存在，不验证鉴权后业务逻辑。
新增的 5 项（`/api/system/site-config`、`/api/system/notices`、`/api/system/config/register_enabled`、
`/api/banner`、`/api/homepage/config`）应返回 **200 而非 401**，因为它们必须对游客开放。

---

## 6. 鉴权 + 全链路联调（带 token）
dev 环境验证码会写入 Redis 明文键 `user:sms:<phone>`，可用此流程做端到端验证：
1. `POST /api/auth/sms-code?phone=<手机号>` → 成功
2. 从 Redis 读取 `user:sms:<phone>` 得到 6 位码
3. `POST /api/auth/register` {phone, smsCode, nickname, password} → 200
4. `POST /api/auth/login` {username:phone, password, loginType:"password"} → 拿到 `accessToken`
5. 携带 `Authorization: Bearer <token>` 调受保护接口（如 `/api/user/profile`、`/api/course/list`、
   `/api/message/list`、`/api/admin/dashboard/stats` 等）→ 期望 2xx（管理员接口对普通用户 403 属正常）

> 等价自动化见 `FullChainSmokeTest`（步骤 6 的脚本化版本，无需手工操作）。

---

## 7. 生产环境专项
- [ ] 域名：`wukonghuwei.cn`（主）/ `bmdjs.cn` 已完成 ICP 备案，否则阿里云 RST 拦截（ERR_CONNECTION_RESET）
- [ ] SSL：`acme.sh + Aliyun DNS API`，RAM 子账号 `wukong-cert` 已授权 `AliyunDNSFullAccess`，证书已部署并自动续期
- [ ] 生产 `.env` / secrets：数据库、Redis、OSS、VOD 凭据**不要**提交到仓库
- [ ] 短信服务：生产应接真实短信网关，关闭 dev 的「验证码明文日志」
- [ ] 限流/防刷：IP 级短信发送 60s 限流在生产仍然生效，注意压测与批量脚本避开
- [ ] 回滚方案：保留上一版本 jar / 镜像 tag，数据库迁移需可前向兼容

---

## 8. 站点配置初始化（首次上线必做）
站点级信息/开关全部存 `sys_config` 表，由 `GET /api/system/site-config` 聚合下发给前台；
后台在「系统设置 → 基本设置 / 导航与开关」编辑，无需改代码或重新构建。

| 配置键 | 含义 | 缺省值 |
|---|---|---|
| `site_name` | 平台名称（导航/页脚） | 码高教育 |
| `site_logo` | Logo 图片 URL | 空（用文字 Logo） |
| `icp` | 备案号 | 空（页脚显示「待备案」） |
| `contact_phone` / `contact_email` / `contact_address` | 客服信息 | 空（用前端兜底值） |
| `copyright` | 版权文案 | Copyright 2024-2026 码高少儿编程教育平台 |
| `nav_menu` | 顶部导航 JSON 数组 `[{label,path,icon?}]` | 空 → 用前端 DEFAULT_NAV |
| `register_enabled` | 开放注册（false 时隐藏入口并拦截 /register） | true |
| `maintenance_mode` | 维护模式（前台全屏遮罩） | false |

> 生产上线前请在「系统设置」填入真实 **备案号、站名、客服信息**；`maintenance_mode` 务必保持 false。
> 首页轮播由后台「Banner管理」维护（`position=home`、`status=1`、按 `sortOrder`），
> 首页区块开关在「首页配置 → 显示设置」控制（`bannerAutoPlay/bannerInterval/showCourseCategory/showNews/showPartner`）。

---

## 9. 发布后回归（建议）
- [ ] 公开页（首页/资讯/赛事列表）可访问，游客能看到导航、公告条、Banner
- [ ] 后台改站名/备案号 → 刷新前台页脚即时生效；改导航 JSON → 顶部菜单即时变化
- [ ] 注册→登录→购买课程→支付成功→入学记录写入（订单详情可见）整链可用
- [ ] 后台管理端各列表/详情可加载，无 5xx
- [ ] 监控：后端日志无新增 `BadSqlGrammarException` / `NullPointerException`

---

### 附：本次（2026-11-09）核查结论速记
- 后端编译/测试 ✅；前端构建 ✅；契约 151/0 ✅；服务启动 ✅
- 修复：`add_parent_phone.sql` + `add_phase1_tables.sql` 未对本地库执行 → 补执行，鉴权链恢复
- 测试副作用：Redis 有短信码/限流键、DB `course_purchase` 有集成测试 user=8888888 记录，属本地开发数据，可忽略
