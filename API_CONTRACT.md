# API 契约登记簿（前后端单一真相源）

> 本文件是 magao 平台**前后端接口的唯一真相源**。任何一端改动接口，必须先更新本文件，
> 再对齐另一端，最后运行 `scripts/contract-check.sh` 防止漂移。
> 适用工程：`magao-platform`（前端 Vue3+TS / 后端 Spring Boot 3 + MyBatis-Plus）。

---

## 0. 同步铁律（改接口前必读）

1. **路径顺序**：资源操作型一律 `/{resource}/{id}/{action}`（如 `/exam/{id}/enter`）；
   集合型一律 `/{resource}/{action}`（无 id）。前端已按此习惯散落调用，**后端必须对齐后端注解**，
   不要改前端。
2. **分页信封**：列表接口统一返回 `PageResult`，结构为
   `{ list, total, pages, pageNum, pageSize }`。**前端只读 `data.list`**。
   后端通过 `GlobalPageResultAdvice` 切面把 MyBatis `Page` 自动包成 `PageResult`，
   各 Controller 直接 `return Result.success(page)` 即可，无需手工包装。
3. **统一响应**：`Result<T> { code, message, data, timestamp }`，成功 `code=200`。
4. **鉴权双重白名单**：公开接口必须**同时**出现在两处，否则仍会被 JWT 过滤器拦截（返回 401）：
   - `common/config/SecurityConfig.java` 的 `requestMatchers(...).permitAll()`
   - `common/filter/JwtAuthFilter.java` 的 `WHITE_LIST`
   当前已放行的公开前缀：`/api/auth/**`、`/api/news/**`、`/api/categories/**`、
   `/api/partners/**`、`/api/homepage/**`、`/api/about/**`、`/api/banner/**`、
   `/api/course/list`、`/api/course/detail/**`、`/api/course/categories`、
   `/api/exam/list`、`/api/exam/detail/**`、`/api/exam/result/query`。
5. **运行端口**：后端固定 `8080`（`java -jar ... --server.port=8080`），与前端 `vite.config.ts`
   代理目标一致；本地联调必须保证后端在该端口可达。
6. **题库单一真相源**：考试题目内容由后端 `exam_question` 表提供（见 §3），前端做题/预览/错题本
   优先请求后端，失败回退本地 `src/mock/examQuestions.ts`（仅作兜底，不作为真相源）。

---

## 1. 考试模块（已对齐 / 已补后端）

基址 `/api/exam`。以下端点**后端已存在且与前端 `src/api/exam.ts` 一致**：

| 方法 | 路径 | 说明 | 鉴权 | 状态 |
|---|---|---|---|---|
| GET | `/list` | 考试列表（分页） | 公开 | ✅ |
| GET | `/{id}` | 考试详情 | 公开 | ✅ |
| POST | `/{id}/enter` | 进入考试（返回 recordId） | 登录 | ✅ 已修段序 |
| POST | `/{id}/answer` | 保存答案 | 登录 | ✅ |
| POST | `/{id}/submit` | 提交考试 | 登录 | ✅ 已修段序 |
| GET | `/result/{id}` | 考试成绩 | 登录 | ✅ |
| GET | `/records` | 我的考试记录（分页） | 登录 | ✅ 新增 |
| GET | `/result/query` | 成绩查询（参赛号+姓名） | **公开** | ✅ 新增并已双白名单放行 |
| POST | `/{id}/switch-screen` | 上报切屏事件 | 登录 | ✅ 新增 |
| GET | `/grading/tasks` | 批改任务列表（分页） | 教师/管理员 | ✅ 新增 |
| POST | `/grading/grade` | 批改某题（recordId+questionId+score） | 教师/管理员 | ✅ 新增 |
| GET | `/paper/{paperId}/questions` | 试卷题目（题库单源，返回 `ExamQuestionVO[]`） | 登录 | ✅ 新增 |

> 历史坑：原先 `enter/submit` 段序写成 `/enter/{id}`、`/submit/{id}`，与前端不符 → 404，已改为 `{id}/enter`、`{id}/submit`。

---

## 2. 题库单源（P1b 已完成 — 后端单源）

- 表：`exam_question`（题目内容：type/title/options/answer/analysis/score/language/template_code/score）
  已建；种子由 `scripts/gen-exam-questions-seed.mjs` 从前端 `src/mock/examQuestions.ts` 抽取生成
  `scripts/seed_exam_questions.sql` 灌库（189 道去重题，覆盖试卷 201–218，与 `exam_paper_question` 关联）。
- 接口：`GET /api/exam/paper/{paperId}/questions`（登录）已上线，返回 `ExamQuestionVO[]`
  （`options` 为字符串数组，`templateCode` 为字符串），SQL 走 `exam_paper_question` 关联按 `sort_order` 排序。
- 校验：`scripts/contract-check.sh` 已纳入该路由（401=命中，404=漂移）；数据层经 MySQL 直查验证
  （每套题数与前端 `examPaperList.questionCount` 完全一致，选择题 `options` 为合法 JSON 数组）。
- **题号对齐**：`exam_question.id` 直接复用前端 mock 的题号（如试卷 201 → 1031–1042），仅对
  两处题号冲突（1061/1062 同号不同内容）改派合成 id（900001/900002）。这样错题本、答案落库
  与前端 mock 同源，切换后端不串题。
- 前端改造（任务 #92，已完成）：新增 `src/api/exam.ts#fetchPaperQuestions(paperId)`（后端优先、失败回退 mock）；
  `ExamTakingPage.vue` 与 `PaperPreviewDialog.vue` 已改为"先请求后端、失败回退本地题源"；`vue-tsc` 类型检查通过。

---

## 2.1 对象存储 OSS（P2 已补齐）

- 控制器 `module/oss/controller/OssController.java`，基址 `/api/oss`，复用既有 `file` 模块
  `FileService`（本地存储实现 `LocalFileServiceImpl`，落盘 `C:\tools\magao-build\uploads`）。
- 端点（与 `frontend/src/api/oss.ts` 完全对齐）：

| 方法 | 路径 | 说明 | 鉴权 | 状态 |
|---|---|---|---|---|
| POST | `/upload` | 文件上传（formData: file/type/id）→ `{url,objectKey,size}` | 登录 | ✅ |
| GET | `/upload-token` | 直传凭证（本地存储占位，返回服务端上传入口） | 登录 | ✅ |
| POST | `/signed-url` | 签名URL → `{url,expiration}` | 登录 | ✅ |
| POST | `/batch-signed-urls` | 批量签名URL → `Record<key,{url,expiration}>` | 登录 | ✅ |
| DELETE | `/file` | 删除文件（body: `{objectKey}`） | 登录 | ✅ |
| POST | `/batch-delete` | 批量删除 → `{deleted[],failed[]}` | 登录 | ✅ |
| GET | `/list` | 文件列表 → `{files[],nextMarker,isTruncated}` | 登录 | ✅ |
| GET | `/raw/{fileId}` | 公共读取（图片回显/下载，免登录） | **公开** | ✅ 已双白名单放行 |

- 说明：`objectKey` 即 `file_record.id`；`upload-token` 在本地存储下返回占位凭证（真实阿里云直传需
  AccessKey，不在本地范围）。上传后回显 URL 统一为 `/api/oss/raw/{id}`，由 `/raw` 端点免登录流式返回。

---

## 3. 其他模块对齐状态（全局扫描结论）

| 模块 | 后端状态 | 主要缺口 |
|---|---|---|
| 用户/认证 | ✅ 已补齐(Phase 0) | 新增 `POST /auth/register`（短信验证码注册→自动 STUDENT 角色）、`PUT /user/password`、`PUT /user/info`、`POST /user/bind-parent`；`sendSmsCode` 放开"仅存量用户"限制以支持注册发码；`sys_user` 新增 `parent_phone` 列（迁移脚本 `scripts/sql/add_parent_phone.sql`） |
| 课程 | ✅ 已补齐(Phase 1) | 新增 `POST /course/purchase`（复用支付订单，返回 orderId）、`POST /course/progress`（video_progress 落地）、`GET /course/categories`（复用 cms 分类 type=course）、`GET /course/video/{id}/play`（返回真实 videoUrl 供播放） |
| 练习 | ✅ 已补齐(Phase 1) | 新增 `GET /practice/detail/{id}`（段序别名）、`POST /practice/submit`（改为 `@RequestBody` JSON 绑定，对齐 submitCode）、`GET /practice/records`（当前用户分页）、`GET /practice/result/{id}`（映射 SubmitResult）、`GET /practice/tags`（返回标签数组） |
| 竞赛 | ✅ 已补齐(Phase 1) | 新增 `GET /competition/detail/{id}`（段序别名）、`POST /competition/submit`（无 id 段，competitionId 取自 body）、`GET /competition/{id}/results`（排行榜映射前端 CompetitionRanking 形状：rank/nickname/avatar/score/submitTime/timeUsed） |
| 消息 | ✅ 已补齐(Phase 3) | 新增 `PUT /message/read-all`、`DELETE /message/{id}`（仅本人），`MessageService` 增加 `markAllRead`/`deleteMessage` |
| 营销 | ✅ 已补齐(Phase 3) | 新增 `GET /marketing/membership/plans`（对齐前端）、`POST /marketing/membership/purchase`（创建会员订单）、`POST /marketing/coupons/{couponId}/receive`（路径对齐）、`GET /marketing/coupons/available`（可用优惠券，映射 CouponItem）；保留原 `/memberships`、`/coupon/receive`、`/coupons` 兼容 |
| VOD | ✅ 已重构(Phase 3) | 原 `VodController` 带 `@ConditionalOnProperty(vod.enabled=true)` 且路径与前端不符 → 全部 404。已改为**自包含**、去掉条件注解、对齐前端 `vod.ts` 全部路径（upload-auth/refresh-upload-auth/play-info/batch-play-info/list/play-auth/transcode-status/{id}），未配置火山引擎时返回占位/空数据（保证路由命中、页面可渲染）。课程视频真实播放走 `GET /course/video/{id}/play` |
| 支付 | ✅ 已补齐 | 新增 `PaymentController`（`/api/payment/*`）对齐前端 7 端点；复用既有 `pay_order`/`pay_refund` 表与 `OrderService`，VO 枚举转换（productType/status/payMethod 字符串↔整数），支付/退款状态机完整 |
| 成长积分 | ✅ 已补齐 | 新增 `GrowthController`（`/api/growth/*`）对齐前端 6 端点；复用既有 `user_point`/`point_log`/`medal`/`user_medal`/`user_level` 表与 `GrowthService`，并补 `seed_growth.sql` 演示数据 |
| OSS 对象存储 | ✅ 已补齐 | 7 端点全部对齐 `src/api/oss.ts`；复用 file 模块 `FileService`（本地存储），新增 `/api/oss/raw/{id}` 公共读取（已双白名单放行） |
| 后台管理 | ✅ 已补齐(列表+写+接线) | 聚合 `AdminController`（读）+ `AdminWriteController`（写，19 资源）。前端桥接层 `src/api/admin.ts`（`createAdminApi` 工厂 + 19 实例）已建；**全部 16 个管理页**已从 mock/localStorage 改为后端优先+兜底实时读写（vue-tsc 0 错误）：内容集群 5 页（资讯/分类/Banner/伙伴/通知）+ 核心 3 页（课程/班级/赛事）+ 业务 8 页（报名/题库/试卷/成绩/练习/文件/营销套餐/优惠券）+ 学员/教师/账号（只读读）+ 仪表盘（dashboard/stats）。**config/system 评估（2026-10-31）**：regcode/trial/homepage-config/about-config/storage-config/data-backup/operation-log/system-monitor 维持本地 mock（无 CRUD 表/属运维工具）；学员/教师/账号写操作已补（密码用登录同款盐值哈希） |
| 评测 Judge | 对齐 | — |
| CMS（资讯/分类/伙伴/关于/首页/Banner） | 对齐 | — |

> 凡标注"整段缺失"的模块（OSS / 成长 / 支付 / admin），按"先补后端"决策补齐，
> 补后端时务必遵守 §0 的路径顺序、分页信封、双白名单三条规定。

---

## 5. 后台管理前端接线（admin 控制台真·同步）

### 5.1 桥接层 `src/api/admin.ts`
统一管理端 API，对齐 `AdminController`(读) + `AdminWriteController`(写)。
- `createAdminApi<T>(resource)` 工厂，暴露 `list / create / update(id) / remove(id) / batchRemove(ids)`。
- `list` → `GET /api/admin/{resource}/list`（营销套餐/优惠券无 `/list` 后缀，单独处理）。
- 写 → `POST .../create`、`PUT .../{id}`、`DELETE .../{id}`、`POST .../batch-delete`（body `{ ids }`）。
- 预置 19 个资源实例：`student/teacher/account/course/class/exam/registration/question/paper/score/practice/banner/news/category/partner/notification/file/plan/coupon`。

### 5.2 已接线页面（后端优先 + mock 兜底；vue-tsc 0 错误）
| 页面 | 资源 | 读 | 写(增改删/批量) | 种子数据 |
|---|---|---|---|---|
| NewsManagePage | news | ✅ | ✅ | cms_news ×2 |
| CategoryManagePage | category | ✅ | ✅ | cms_category ×3 |
| BannerManagePage | banner | ✅ | ✅ | cms_banner ×2 |
| PartnerManagePage | partner | ✅ | ✅ | cms_partner ×2 |
| NotificationManagePage | notification | ✅ | ✅ | sys_notice ×2 |
| CourseManagePage | course | ✅ | ✅ | course_info ×3 |
| ClassManagePage | class | ✅ | ✅ | teach_class ×2 |
| ExamManagePage | exam(赛事) | ✅ | ✅ | competition_info ×2 |
| RegistrationManagePage | registration | ✅ | ✅(状态) | competition_participant ×? |
| QuestionBankPage | question | ✅ | ✅ | exam_question ×189 |
| PaperManagePage | paper | ✅ | ✅ | exam_paper ×18 |
| ScoreManagePage | score | ✅ | ✅ | exam_record ×? |
| PracticeManagePage | practice | ✅ | ✅ | practice_question ×? |
| FileManagePage | file | ✅ | ✅(删除) | file_record ×? |
| MarketingManagePage | plan/coupon | ✅ | ✅ | membership ×?/coupon ×? |
| StudentManagePage | student | ✅ | ✅(增改删/状态/重置密码→sys_user) | 会员档案(school/grade/memberStatus...)本地(无表) |
| TeacherManagePage | teacher | ✅ | ✅(增改删/状态/重置密码→sys_user) | 科目/职称/简介/权限本地(无表) |
| AdminAccountManagePage | account | ✅ | ✅(增改删/状态/重置密码→sys_user) | role 标签 admin/operator↔后端 ADMIN/OPERATOR |
| DashboardPage | — | ✅ `/admin/dashboard/stats` | 只读统计 | — |

> 注：Course/Class/Exam/Question 等实体字段名与前端 mock 列名不完全一致（如 Course 实体无 `ageRange/videoCount/hasTrial`，`status` 为整型；Exam 实体无 `duration/totalScore/questionCount` 等）。前端已做兼容映射（如 Class 的 `mapClassItem` 把整型 status→enabled/disabled、实体 `className`↔展示 `name`），真实数据可显示对齐字段；未对齐字段在带 JWT 拉取真实数据时显示为空，属**字段映射待细化**缺口（见 5.3）。Mock 兜底保证无 JWT/离线时页面始终有数据。

### 5.3 已知缺口（"全做"计划剩余项）
- **字段映射待细化**：course/class/exam/question/registration/paper/score/practice/file/plan/coupon 前端列名与后端实体字段名存在不一致（多为实体缺字段或命名差异）。当前桥接层已按"后端优先、失败回退 mock"接通，真实数据可显示部分字段；要全部列正确显示需在后端补 VO/转换或在 `admin.ts` 增加前端适配层。此为显示层优化，不阻塞功能。
- **学员/教师/账号写操作已落库（2026-10-31 修复）**：`AdminWriteController` 新增 12 个写端点（student/teacher/account 各 create/update/delete/batch-delete），共用 `sys_user` 表，按路径决定 `roleType`（STUDENT/TEACHER），account 由 DTO.roleType 决定（ADMIN/OPERATOR）；密码用与登录一致的 `CryptoUtil.md5WithSalt(pwd, "magao_salt_2024")` 哈希（空密码默认 123456，更新仅非空密码重算），不破坏登录体系；`buildUser` 已改为"仅写入非空 DTO 字段"，局部更新（如仅切状态/仅重置密码）不会把昵称/手机等列置空。富档案字段（学员 school/grade/memberStatus、教师 subject/title/intro/permissions 等）后端无独立表，仍本地展示，属已知缺口，不影响核心身份落库。
- **config/system 页评估（2026-10-31）**：9 页中仅 `SystemSettingsPage` 有后端形态——其原 `GET /admin/settings` 返回 `List<SysConfig>`（与前端期望扁平对象不匹配）且无写接口；已改为 `GET` 返回扁平 KV 映射 + 新增 `PUT /admin/settings` upsert（复用 `SystemService.setConfigValue`，值按类型反序列化布尔/数值/字符串）。其余 8 页（regcode/trial/homepage-config/about-config/storage-config/data-backup/operation-log/system-monitor）为运维/监控/本地配置类，无对应 CRUD 表，维持本地 mock/localStorage，不接桥接层。
- **Phase 0 用户/账户闭环（2026-11-08 修复）**：`AuthController` 新增 `POST /auth/register`（校验短信验证码→写入 `sys_user`，角色 STUDENT，密码用 `CryptoUtil.md5WithSalt(pwd,"magao_salt_2024")` 与登录同款哈希）；`UserController` 新增 `PUT /user/password`（复用既有 `UserService.changePassword`）、`PUT /user/info`（部分字段更新，仅非空列）、`POST /user/bind-parent`（写 `parent_phone`）；`sendSmsCode` 移除"用户必须已存在"校验，使新手机号可注册发码。新增 DTO：`RegisterDTO`/`PasswordChangeDTO`/`UserInfoUpdateDTO`/`BindParentDTO`。`sys_user` 增加 `parent_phone` 列（`schema_init.sql` 已含，存量库用 `scripts/sql/add_parent_phone.sql` 幂等迁移）。前端 `src/api/user.ts` 的 4 个调用现已全部对后端路由。
- **contract-check.sh 已同步**：含 student/teacher/account 写路由、`PUT /admin/settings`、Phase 0 四路由，现共约 149 项（新增 Phase 1 课程/练习/竞赛 11 项 + Phase 3 消息/营销/VOD 12 项），运行 `bash scripts/contract-check.sh` 应 0 漂移。
- **Phase 1 课程/练习/竞赛（2026-11-08 修复）**：改后端对齐前端（前端零改动）。课程补 `/purchase`（复用 `OrderService.createOrderFrontend(userId,"course",courseId,couponId)` 返回 orderId）、`/progress`（落 `video_progress`，watchedDuration+isCompleted）、`/categories`（复用 cms `CategoryService.listByType("course")`，返回 `{id,name,icon}`）、`/video/{id}/play`（返回 `CourseVideo.videoUrl` 真实播放地址）。练习补 `/detail/{id}` 段序别名、`/submit` 改 `@RequestBody` JSON、`/records`、`/result/{id}`（映射 SubmitResult）、`/tags`（固定标签数组）。竞赛补 `/detail/{id}` 段序别名、`/submit` 无 id 段（competitionId 取自 body）、`/{id}/results`（排行榜映射前端形状）。相关表 `video_progress`/`course_purchase` 已在 `schema_init.sql`，存量库用 `scripts/sql/add_phase1_tables.sql` 幂等补齐。
- **Phase 3 消息/营销/VOD（2026-11-08 修复）**：消息补 `PUT /message/read-all`、`DELETE /message/{id}`（仅本人，Service 增 `markAllRead`/`deleteMessage`）。营销补 `GET /marketing/membership/plans`、`POST /marketing/membership/purchase`（会员订单）、`POST /marketing/coupons/{couponId}/receive`、`GET /marketing/coupons/available`（映射 CouponItem，过滤 status=1 且未过期）。VOD 重写为自包含、去掉 `@ConditionalOnProperty`，对齐前端全部路径，未配置火山引擎时返回占位/空（保证路由命中、应用可启动）；**已知限制**：真实云端 VOD（上传/转码/加密播放）需 `vod.enabled=true` + 火山 AccessKey/SecretKey/SpaceName，接入后可将占位逻辑替换为 `VodService` 实现。课程视频真实播放不受影响（走 `/course/video/{id}/play`）。
- **遗留跟随项（非阻塞）**：①课程购买仅创建订单，支付成功后的"报名/入学"写 `course_purchase` 需在 `OrderService.payOrder` 成功后挂钩（当前订单流已闭环，入学绑定为增强项）；②`/practice/tags` 为固定标签数组（题库未维护 tags 列），如需动态标签可后续给 `practice_question` 加 `tags` 列。

---

## 4. 改动工作流（强制）

1. 任一方要改/加接口 → 先改本文件对应表格（路径/方法/请求体/响应体/负责人）。
2. 后端按本文件实现/修改 Controller（注意双重白名单、分页信封、段序）。
3. 前端按本文件实现/修改 `src/api/*.ts` 与类型。
4. 跑 `bash scripts/contract-check.sh`，确认无新增 404、分页含 `list`。
5. 提交时 PR 描述须附"契约变更说明"。
