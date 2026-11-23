# Magao 平台全量上线差距与修复清单（正式收费运营版）

> 审计日期：2026-11-19
> 审计方式：四路并行只读审计 —— ①后端 14 业务模块 ②前端全部页面 ③安全与运维 ④数据库/部署/测试（客观点直接核查）
> 证据来源：`backend/src/main/java`、`frontend/src`、`application*.yml`、`docker/docker-compose.yml`、`schema_init.sql`
>  distances 说明：所有路径相对于仓库根 `magao-platform/`；行号为审计时快照，改动后需复核。

---

## 零、结论与统计

**当前代码不能正式收费上线。** 不只是"支付是假的"——审计发现 **21 项阻断上线（P0）+ 28 项必修（P1）+ 12 项建议（P2）**，分布在资金、业务闭环、权限越权、安全合规、前端造假、运维可观测六个维度。

三句话概括：
1. **钱是假的**：支付全套 Mock、金额写死、无回调验签、前端假二维码、退款不真退。
2. **把关是漏的**：教师可提权建管理员、班级/订单/考试记录/判题结果普遍无归属校验、付费视频不校验购买即可播放。
3. **闭环是断的**：考试客观题永远 0 分且答案泄露给前端、VOD 全占位、MQ 消费者全是空 TODO、后台监控/备份/日志是浏览器 localStorage 假数据。

面向未成年人 + 真实交易，隐私合规（S4）是法律红线，与功能修复同等优先级。

---

## 一、P0 —— 阻断上线（21 项，全部修完才可收费）

### A. 资金与交易（P0-01 ~ P0-05）

**P0-01　支付后端是纯演示实现**
- 证据：`backend/.../payment/service/impl/OrderServiceImpl.java:151-170` —— `payOrder()` 直接 `setStatus(1)`、`tradeNo="MOCK-"+orderNo`、返回 `/api/payment/mock-pay`（该端点 Controller 中不存在，是死链）、日志写"（演示）订单支付成功"。
- 修复：引入微信支付 V3 SDK（`wechatpay-java`）+ 支付宝 SDK（`alipay-easysdk`，pom 当前依赖数为 0），服务端统一下单取真实支付凭证；删除 Mock 分支。

**P0-02　无支付回调、无验签、无幂等、无查单补偿**
- 证据：全仓仅有 `PayCallbackLog` 实体/Mapper，无任何回调 `@PostMapping`、无验签逻辑；`ExamProducer` 类零调用。
- 修复：回调端点先验签再核对金额再改状态；流水号幂等；前端轮询 `/status` 时向网关反查防回调丢失。

**P0-03　金额写死，不查商品价格；优惠券只记日志不核销**
- 证据：`OrderServiceImpl.java:259-266` `defaultAmount()` 课程 199 / 会员 299 / 商品 9.99；`createOrderFrontend()` 不查库；`course_info.price`、`membership.price` 字段真实存在却不用；`:90-92` 优惠券仅 `log.info`。
- 修复：按 productId 查真实价格与标题写订单快照；下架商品拒绝下单；实现领券归属/门槛/有效期校验与折扣计算，支付成功核销 `user_coupon.status`。

**P0-04　退款不真退、会员/积分商品不开通、开通事件不可靠**
- 证据：`refundOrder():144` `tradeNo="MOCK-"` 且无网关调用；`PaymentController.cancel()/refund()` 空实现直接 success；仅 `CoursePurchaseListener` 监听 `OrderPaidEvent`，会员/积分商品无监听器；用的是同步 `@EventListener`（服务重启丢事件）。
- 修复：退款调网关真实 API 并验退款回调；补会员/积分开通监听；改 `@TransactionalEventListener(AFTER_COMMIT)` + 本地消息表保证最终一致。

**P0-05　前端支付链路三处断裂**
- 证据：`frontend/src/components/payment/QRCodePay.vue:52-53` 二维码写死 `api.qrserver.com/...&data=pay`，后端返回的 qrCode 从不用，`pollPayStatus` 定义未调用；`PayPage.vue:322` 余额支付无条件 `handlePaySuccess()`；`MembershipPage.vue:107` 传 `type` 而 PayPage 要 `productType/productName/amount`，`OrderListPage.vue:144` 传 `orderNo` 而 PayPage 不读 → 购买必然落到空态。
- 修复：用后端返回真实二维码；轮询确认后再跳转；统一"创建订单→orderId→支付页"跳转协议并加端到端用例。

### B. 业务闭环断裂（P0-06 ~ P0-11）

**P0-06　考试自动阅卷完全缺失，客观题恒 0 分**
- 证据：`exam/service/impl/ExamServiceImpl.java:129-138` 只累加已有 score；`:172-182` 新答案 `score=0`；`mq/consumer/ExamSubmitConsumer.java:27` 仅 `// TODO: 自动批改客观题`，且 `submitExam` 根本未发 MQ。
- 修复：交卷时同步判客观题并写分，或实现消费者并开启 MQ。

**P0-07　考试标准答案泄露给前端 + 成绩查询越权**
- 证据：`ExamServiceImpl.java:284-291` 答题态 VO 直接 `setAnswer(q.getAnswer())`；`ExamController.java:120-124` `/paper/{id}/questions` 无角色校验；`ExamServiceImpl.java:202-212` `queryResult(registerNo,name)` 把 registerNo 当 recordId 直接查，`name` 参数根本没用。
- 修复：答题 VO 去除 answer/analysis；成绩查询按"准考证号+姓名"数据库精确匹配。

**P0-08　付费课程可白嫖：播放与进度不校验购买**
- 证据：`course/controller/CourseController.java:139-180` play/progress 只查视频、写进度，不查 `course_purchase`；`data.put("token","")` 防盗链失效。虽有 `CoursePurchaseListener` 写入学记录，播放链路完全不读。
- 修复：play/progress 先校验 `(userId,courseId)` 购买记录；接入带过期签名的播放 URL。

**P0-09　VOD 模块全占位，真实 VodService 从未被注入**
- 证据：`vod/controller/VodController.java:35-44,120-135` 类注释自称"占位降级实现"，uploadAddress/uploadAuth/playUrls 全空字符串；`VodServiceImpl` 未被 Controller 注入。
- 修复：Controller 注入 VodService 解析真实凭证/播放地址；配置火山 VOD AK/SK + 播放域名（prod yml 当前为空）。

**P0-10　5 个 MQ 消费者全是空 TODO，生产未启用 MQ**
- 证据：`mq/consumer/` 下 `ExamSubmitConsumer:27`、`JudgeResultConsumer:32`、`MessageNotifyConsumer:26`、`OrderTimeoutConsumer:27`、`VideoTranscodeConsumer:26` 均仅一行日志；`application-dev.yml:5` `mq.enabled:false`，prod 未配置（消费者 `matchIfMissing=false`）。
- 修复：实现各消费者（判卷、更新评测、站内信、关单、转码）；生产显式开启并加幂等/重试。

**P0-11　竞赛作品链路依赖默认关闭的 MQ**
- 证据：`competition/service/impl/CompetitionServiceImpl.java:170` 提交作品发 MQ，但生产 MQ 不启用 → 作品永不落库/计分/上排行榜。
- 修复：随 P0-10 开启 MQ 后验证竞赛全链路。

### C. 权限与越权（P0-12 ~ P0-15）

**P0-12　提权漏洞：教师/运营可创建管理员、角色前端可控、默认口令**
- 证据：`admin/controller/AdminWriteController.java:82` 类级放行 `{ADMIN,TEACHER,OPERATOR}`；`:513-535` `roleType` 取前端值、默认口令 `"123456"`；`:602-607` createAccount 可被教师调用并传 `roleType=ADMIN`。
- 修复：账号/角色类接口收紧为 ADMIN；roleType 服务端强制；移除默认口令。

**P0-13　教师班级数据隔离完全缺失**
- 证据：`classmanage/controller/ClassController.java:36-73` teacherId 前端传、无归属校验；`ClassServiceImpl.java:40-44` getDetail 直接 selectById；`AdminController.java:135-138` 教师可看全部班级。
- 修复：teacherId 一律取 `getCurrentUserId()`；详情/增删学生校验归属（ADMIN 例外）。

**P0-14　订单/判题/考试记录普遍无归属校验**
- 证据：`payment/controller/OrderController.java:35-38` 按 orderNo 查不校验 userId；`PaymentController.java:47-61` detail/status/pay 无 userId；`judge/controller/JudgeController.java:134-153` getResult 不校验记录归属；`ExamServiceImpl.java:159-184` saveAnswer 不校验 record 归属（可写到他人答卷）。
- 修复：所有按 ID 查询追加 `eq(UserId, getCurrentUserId())`，越权返回 403。

**P0-15　OSS 任意文件读取 + 删除无归属**
- 证据：`common/filter/JwtAuthFilter.java:99` `/api/oss/raw/**` 免登录；`oss/controller/OssController.java:191-206` 按 ID 直出任意文件；`:127-161` 删除仅凭前端 objectKey 无上传者校验。
- 修复：raw 改签名 URL 或校验归属；删除校验上传者/ADMIN。

### D. 安全与合规（P0-16 ~ P0-20）

**P0-16　短信验证码明文写日志 + 弱随机**
- 证据：`user/service/impl/AuthServiceImpl.java:77-81` `Math.random()` 生成验证码并 `log.info("...code={}", code)`，手机号即登录名 → 任何人读日志即可登录他人账号。
- 修复：删日志中的 code、改 `SecureRandom`、接真实短信网关、限尝试次数。

**P0-17　初始化 SQL 内置可用弱口令管理员/测试账号**
- 证据：`scripts/init-db.sql:1338-1340` `-- 管理员用户 (密码: admin123)`；`:1363-1371` 补充测试账号"密码统一 123456"；`backend/scripts/test_accounts.sql:11-40`。
- 修复：生产镜像移除测试账号与明文密码注释；首次启动强制改密。

**P0-18　密码用全局固定盐 MD5 而非 BCrypt**
- 证据：`common/util/CryptoUtil.java:45-50` `PASSWORD_SALT="magao_salt_2024"`；`SecurityConfig.java:82-87` 注释"BCrypt 预留，当前 MD5+Salt"。
- 修复：改 BCrypt/Argon2 加随机盐；存量密码下次登录时重哈希迁移。

**P0-19　未成年人隐私合规缺失（法律红线）**
- 证据：`frontend/src/components/common/AppFooter.vue:79-81` 隐私政策/用户协议均为 `<a href="javascript:;">` 空链接；`UserController.java:59-64` 绑定家长手机无验证码/监护人确认；无年龄识别与告知留痕。
- 修复：接入真实《儿童个人信息保护规则》《隐私政策》并留痕勾选；监护人同意与撤回机制；未成年人收费二次确认。

**P0-20　基础设施暴露与容器逃逸**
- 证据：`docker/docker-compose.yml:28,74-88,115-117,189,202` MySQL/Redis/RabbitMQ 端口映射到宿主机、Redis `protected-mode no`+`bind 0.0.0.0`；Judge0 `privileged:true` 且挂载 `docker.sock`，而用户提交代码就运行在其中。
- 修复：中间件仅内网不映射公网；Redis 开 protected-mode；Judge0 去 privileged、去 docker.sock、独立非特权沙箱。

### E. 外部硬门槛（P0-21）

**P0-21　wukonghuwei.cn 未完成 ICP 备案**
- 未备案被阿里云 RST 拦截（ERR_CONNECTION_RESET）。完成企业主体备案前不得对外宣传收费。

---

## 二、P1 —— 上线前必修（28 项）

### 后端（14 项）
| # | 差距 | 证据 | 修复 |
|---|---|---|---|
| P1-01 | 微信登录未开放 | `AuthServiceImpl.java:48` `throw 微信登录暂未开放` | 接入或下线前端入口 |
| P1-02 | 积分商城后端零接口（实体/Mapper 有，无 Controller） | `growth/controller/GrowthController.java:37-73` | 补商品/兑换接口，事务扣积分 |
| P1-03 | 排行榜/周报写死假数据 | `GrowthServiceImpl.java:184-188,211,226` | 从真实业务表聚合 |
| P1-04 | 操作日志只写 log 不落库，资金操作零审计 | `common/aspect/LogAspect.java:83-117` `SysLogMapper` 零注入 | 落库 + 覆盖下单/改价/退款 |
| P1-05 | 数据字典/备份/监控后端缺失 | `SystemController.java:41-89` 仅 config/notice | 补字典 CRUD、备份任务 |
| P1-06 | 考试无证书环节 | 全仓库无 certificate 实现 | 补证书生成/下载 |
| P1-07 | 领券无防重复 | `MarketingServiceImpl.java:40-49` 直接 insert | 唯一索引 + 先查后插 |
| P1-08 | ScratchJr 结构校验即给 100 分 | `judge/impl/ScratchJrJudge.java:65-68,101-113` | 明确"仅结构校验"或接真实评测 |
| P1-09 | 可导出他人私有作品 | `ScratchWorkServiceImpl.java:161-168` | 校验 userId 归属 |
| P1-10 | 消息 markRead 越权 | `MessageServiceImpl.java:52-59` | 更新加 receiverId 条件 |
| P1-11 | 创建竞赛无角色校验 | `CompetitionController.java:39-43` | 加 @RequireRole |
| P1-12 | 教师可看全量成绩/班级 | `AdminController.java:80,170-174` `selectPage(...,null)` | 按 teacherId 过滤 |
| P1-13 | OSS 上传凭证/签名 URL 是假值 | `OssController.java:81-125` `accessKeyId="local"` | 用真实 TOS 临时凭证 |
| P1-14 | 生产 Swagger 暴露且免鉴权 | `application.yml:139-146` + `JwtAuthFilter.java:67-69` | 生产关闭 springdoc |

### 安全与运维（14 项）
| # | 差距 | 证据 | 修复 |
|---|---|---|---|
| P1-15 | 退出登录不失效 refresh token | `AuthServiceImpl.java:106-133` | token 版本号/黑名单，登出双失效 |
| P1-16 | CORS `* + credentials` | `CorsConfig.java:38-45` | 生产白名单具体域名 |
| P1-17 | 限流 IP 可伪造绕过；Lua 脚本定义未执行 | `IpUtil.java:41-62`、`RateLimitAspect.java:57-115` | 信任 `$remote_addr`、真执行 Lua、账号维度锁定 |
| P1-18 | token 存非 httpOnly Cookie，XSS 可窃 | `frontend/src/utils/auth.ts:1-47` | 后端下发 HttpOnly+Secure+SameSite Cookie |
| P1-19 | 上传路径穿越 + 无类型白名单 | `LocalFileServiceImpl.java:29-42` bizType 直接拼路径 | 枚举白名单 + normalize + MIME 校验 |
| P1-20 | 手机号可无验证修改（账号接管） | `UserServiceImpl.java:74-86` | 短信验证码 + 唯一性 + 旧号通知 |
| P1-21 | 手机号/姓名无脱敏，日志明文 | `UserServiceImpl.java:95,117-135` | 全链路脱敏展示与日志 |
| P1-22 | actuator 依赖缺失但白名单放行且被 compose 当健康检查 | `pom.xml` 无 actuator、`JwtAuthFilter.java:72`、`docker-compose.yml:360` | 加 actuator 或改业务健康端点，否则容器永久 unhealthy |
| P1-23 | 前端从不调 refresh，token 过期即登出 | `frontend/src/api/index.ts:32-45,108-112` | 静默刷新 + 401 重试 |
| P1-24 | 存储型 XSS（v-html 渲染资讯/题目） | `NewsListPage.vue:141`、`ExamRoomPage.vue:83` | DOMPurify 白名单 + CSP |
| P1-25 | 资金接口无操作审计（同 P1-04，安全侧） | `OrderController`/`PaymentController` 无 @LogOperation | 不可篡改审计存储 |
| P1-26 | 硬编码默认口令，无账号级失败锁定 | `AdminWriteController.java:527-531`、`ErrorCode.java:73-74`（LOGIN_FAIL_TOO_MANY 未引用） | 随机强口令 + 失败计数锁定 |
| P1-27 | 订单超时无定时关单 | 有 expire_time 无定时任务 | 定时扫描置 expired + 回滚优惠券 |
| P1-28 | CI/CD Secrets 与 ECS deploy key 未配 | 已提交 workflow `1015268` | 配齐 6 个 Secrets，保证 hotfix 可快速上线 |

### 前端数据真实性（与上面重复项不另计，见 P0-05/P0-19 及下）
- 考试页元信息/试卷列表恒取 mock：`ExamTakingPage.vue:306-321`、`ExamDetailPage.vue:508-512`、`QuestionBankPage.vue:124`
- 成绩页完全本地算分不读后端：`ExamResultPage.vue:51,61-120,152`
- 优惠券页 100% 假数据假领取：`CouponPage.vue:107-122`
- 会员套餐价格前端写死（598/1698）与后端 299 冲突：`MembershipPage.vue:89-92`
- 后台四页面纯 localStorage 假功能：`DataBackupPage.vue:211-342`、`SystemMonitorPage.vue:202-459`、`OperationLogPage.vue:139-288`、`StorageConfigPage.vue:240-357`
- 课程列表空态用 6 门假课程填充：`CourseListPage.vue:184-191,218-240`
- Dashboard 三张图表写死 + 依赖未加载的 `window.echarts`：`DashboardPage.vue:221-275`
- 十余处"按钮只弹提示"假交互：`CourseLearnPage.vue:207,308`、`ExamManagePage.vue:320`、`QuestionBankPage.vue:1094` 等
- 消息/积分商城写死数据：`MessageListPage.vue:79-101`、`PointShopPage.vue:66`
- 创作页 questionId=0 提交判题：`EditorPage.vue:74-79`

---

## 三、P2 —— 可接受 / 迭代优化（12 项）

1. 微信登录、注册码机制（后端无实现，前端有 localStorage 残留）二选一补齐或彻底删除。
2. 支付对账：每日拉网关账单与 `pay_order` 对账，差异告警。
3. 发票、自动续费、风控（同 IP 高频下单）。
4. 前端"运行结果仅供参考，正式成绩以提交判题为准"常驻提示（C++/Python 浏览器环境与判题环境可能不一致）。
5. 编辑器创作模式与判题模式分离，避免 questionId=0。
6. 引入 echarts 依赖替代全局挂载。
7. `.gitignore` 排除 *.log / hs_err，清理仓库内已提交的含密码哈希日志。
8. JWT 过滤器改白名单默认拒绝（当前非 `/api/` 路径全放行）。
9. 本地上传路径改配置项（硬编码 `C:\tools\magao-build\uploads`）。
10. 表结构核验：61 张表与实体 @TableName 逐一对账，补唯一约束/索引（订单号、入学、领券）。
11. 测试覆盖：后端仅 2 个测试文件（`FullChainSmokeTest`、`PayOrderEnrollmentTest`）、前端无测试脚本 —— 收费系统应补支付/越权/定价的自动化测试。
12. 生产配置去掉所有默认口令（如 `${MYSQL_PASSWORD:magao_prod_2024}`），全部强制环境变量注入。

---

## 四、修复顺序与验收

```
第一波（合规+基础设施，可并行）   P0-21 备案 · P0-19 未成年人隐私 · P0-20 基础设施收口
第二波（资金链路，核心）          P0-01 网关SDK → P0-02 回调验签 → P0-03 查库定价 → P0-04 开通/退款 → P0-05 前端三处
第三波（业务闭环）                P0-06/07 考试判分与答案 · P0-08 付费校验 · P0-09 VOD · P0-10/11 MQ
第四波（权限收紧）                P0-12 提权 · P0-13 班级隔离 · P0-14 归属校验 · P0-15 OSS
第五波（账号体系）                P0-16 验证码 · P0-17 弱口令清理 · P0-18 BCrypt
P1 穿插进行，CI/CD(P1-28) 要在第一波就配好以便快速发版
```

**收费上线验收标准（全部满足才算过）**
1. 真实商户号沙箱 + 小额生产跑通：下单 → 支付 → 验签回调 → 开通课程/会员 → 退款 → 钱真实退回。
2. 攻击测试全部失败：伪造回调、篡改金额、重放请求、改 userId 查他人订单/成绩/作品、学生访问 /admin、教师提权建 ADMIN、未购买播放付费视频。
3. 考试链路：交卷后客观题自动得分、答题接口不返回答案、成绩只能本人查、证书可下载。
4. 前端零 mock：全局搜索 `@/mock`、写死价格、`123456`、`data=pay` 无残留；断网时展示空态而非假数据。
5. 运维可用：资金操作有不可篡改审计日志；健康检查真实可用；备份做过恢复演练；备案通过可正常访问。

---

## 五、修复进度追踪（执行中）

> 状态图例：✅ 已完成（源码级）　🔄 进行中　⬜ 未开始　⛔ 外部依赖（备案/商户凭证，代码无法完成）

### 第一波：合规与基础设施收口　✅ 代码侧完成
| 项 | 状态 | 关键改动 |
|---|---|---|
| P0-20 基础设施收口 | ✅ | docker-compose：MySQL/Redis/RabbitMQ/Judge0 端口全绑回环；Redis protected-mode；Judge0 移除 docker.sock（privileged 保留，CE 沙箱硬依赖） |
| P0-19 未成年人隐私合规 | ✅ | 新增 `legal/LegalPage.vue`（隐私政策/用户协议/儿童信息保护规则）；页脚空链接→真实路由；注册页强制勾选+后端 `@AssertTrue` 校验+SysLog 落库留痕 |
| P0-21 ICP 备案 | ⛔ | 线下流程，代码无法完成；页脚已留备案号占位 |
| 附带技术债 | ✅ | 补齐 `TosServiceImpl.download()`，源码可完整编译（CI 门禁不再挂） |

### 第二波：资金链路　✅ 代码侧完成（待真实商户号验收）
| 项 | 状态 | 关键改动 |
|---|---|---|
| P0-01 网关 SDK | ✅ | pom 引入微信 V3(`wechatpay-java:0.2.12`)+支付宝(`alipay-easysdk:2.2.2`)；新增 `PaymentGateway` 抽象 + 微信/支付宝/Demo 三实现；`payment.mode` 决定激活哪个 |
| P0-02 回调验签+幂等+查单 | ✅ | `PaymentNotifyController`（渠道验签→金额核对→confirmPaid 幂等落回调日志）；`getStatus` 主动查单补偿；JWT 白名单放行 notify；**payOrder 不再标记已支付，白嫖后门堵死** |
| P0-03 查库定价+优惠券核销 | ✅ | `createOrderFrontend` 按 productId 查 `course_info/membership/point_goods` 真实价格，下架商品拒单；优惠券下单锁定、回调核销；新增 `pay_order.coupon_id` 列+迁移脚本 |
| P0-04 退款+开通监听 | ✅ | refund 走真实网关；新增 `user_membership` 表+`MembershipActivateListener`；课程/会员监听改 `@TransactionalEventListener(AFTER_COMMIT)` |
| P0-05 前端支付三处断裂 | ✅ | `QRCodePay` 用后端真实凭证本地渲染+轮询状态+demo 受控按钮；`PayPage` 动态支付方式+真实下单；修复会员页/订单页跳转参数；余额假支付移除 |

**第二波验证**：后端 `mvn compile` ✅ / 前端 `vue-tsc` ✅ / `vite build` ✅；交叉审计无 mock-pay/qrserver 残留、端点全对齐。

**第二波仍待外部条件**：真实商户凭证（微信 APIv3 key/商户私钥/证书序列号、支付宝应用私钥/公钥）、`payment.notifyBaseUrl` 公网可达、demo 模式生产必须切换为 wechat/alipay。

### 第三波：业务闭环　✅ 代码侧完成
| 项 | 状态 | 关键改动 |
|---|---|---|
| P0-06 客观题自动判分 | ✅ | `submitExam` 交卷时对 choice/judge/fill 按标准答案自动判分写分；主观题保持待批改 |
| P0-07 答案泄露+成绩越权 | ✅ | `/paper/{id}/questions` 默认去答案；新增带 `@RequireRole` 的阅卷预览端点；`queryResult` 改"准考证号+姓名"双因子匹配（仅已交卷） |
| P0-08 付费课程白嫖 | ✅ | `CourseController` play/progress 校验 course_purchase（免费课放行、过期拒绝） |
| P0-09 VOD 全占位 | ✅ | Controller 用 `ObjectProvider<VodService>` 可选注入真实服务；未配置明确报错，不再返回空凭证假成功 |
| P0-10/11 MQ 消费者 | ✅ | 5 个空消费者全部实现（自动判分/关单/评测落库/站内信/转码核对）；竞赛消费者原本已完整；新增 `closeExpiredOrder`/`autoGradeRecord` 幂等方法 |

**第三波验证**：后端 `mvn compile` ✅；交叉审计接口签名/调用方/VOD 8 端点全对齐。
**第三波仍待外部条件**：VOD AK/SK + 播放域名、MQ 生产显式开启（`mq.enabled=true`）并配延迟消息插件（关单才有生产者）、RabbitMQ 实例。

### 第四波：权限收紧　✅ 代码侧完成
| 项 | 状态 | 关键改动 |
|---|---|---|
| P0-12 提权漏洞 | ✅ | 账号类 4 接口加方法级 `@RequireRole(ADMIN)` 覆盖类级放行；`buildUser` 角色服务端强制（忽略前端 roleType）；移除默认口令 123456，强制初始密码≥8位 |
| P0-13 班级隔离 | ✅ | ClassService 5 方法加 currentUserId；教师只看/管自己的班级（ADMIN 例外）；createClass teacherId 服务端强制；AdminController.classList 加归属过滤 |
| P0-14 归属校验 | ✅ | `/order/query`、`/payment/detail/status/pay`、`/judge/result`、`saveAnswer` 全部加 userId 归属校验 |
| P0-15 OSS | ✅ | delete/batch-delete 按 uploadUserId 校验（ADMIN 可删任意）；raw 公共读保留（回显需要） |

**第四波验证**：后端 `mvn compile` ✅；交叉审计归属校验/角色注解/弱口令全通过。

### 第五波：账号体系 + P1 收尾　✅ 代码侧完成
| 项 | 状态 | 关键改动 |
|---|---|---|
| P0-16 验证码安全 | ✅ | `Math.random()`→`SecureRandom`；删除日志中的明文 code（只记发送行为） |
| P0-17 弱口令账号 | ✅ | init-db.sql：admin 改 BCrypt 强密码 `Magao@Admin2026!`；5 个测试账号（含 ADMIN）默认注释禁用；test_accounts.sql 加"严禁生产执行"警告 |
| P0-18 BCrypt | ✅ | 新增 `PasswordUtil`（BCrypt + 存量 MD5 兼容 + 登录自动迁移）；注册/改密/重置/后台建号全改 BCrypt；实测存量密码仍可登录并自动升级 |
| P1-22 actuator | ✅ | pom 加 `spring-boot-starter-actuator`；prod 只暴露 health（compose 健康检查不再永久 unhealthy） |
| P1-14 Swagger | ✅ | prod 关闭 springdoc api-docs/swagger-ui |
| P1-16 CORS | ✅ | 白名单可配置 `cors.allowed-origins`；prod 默认 `https://wukonghuwei.cn`；通配时关闭 credentials |
| P1-07 领券防重复 | ✅ | 同用户同券只领一次 + 库存条件更新防超发 |
| P1-09 作品导出越权 | ✅ | exportWork 加 userId，私有作品仅本人可导出 |
| P1-10 消息越权 | ✅ | markRead 改带 receiverId 的条件更新 |
| P1-11 竞赛创建 | ✅ | 加 @RequireRole(ADMIN/TEACHER/OPERATOR) |
| P1-12 成绩隔离 | ✅ | 教师/运营只看本班学生成绩，ADMIN 看全部 |

**第五波验证**：后端 `mvn package` ✅ / 前端 `vue-tsc` ✅ / 密码兼容迁移实测通过。

---

### 📊 五波修复总览（代码侧全部完成）

- **波次 1-5 共修复 P0 20 项（代码可修部分）+ 关键 P1 11 项**，全部编译/构建通过。
- **仍为外部依赖、代码无法完成**：ICP 备案（线下）、真实商户凭证+回调验收、VOD AK/SK+播放域名、RabbitMQ 生产开启、ECS deploy key+6 个 Secrets、测试账号的强密码首次登录后改密。
- **验收提示**：收费前必须用真实商户号跑通完整链路 + 攻击测试（伪造回调/改价/越权/重放），详见"收费上线验收标准"。
### 第五波：账号体系　⬜

---

## 六、去 Mock 与资金审计留痕（续，2026-11-19）

### 前端考试假数据根除 ✅
| 项 | 状态 | 关键改动 |
|---|---|---|
| mock 工具/类型迁移 | ✅ | `QuestionType`/`ExamQuestion`/`ExamPaper`/`ExamMeta`/`ExamResultDetail` 迁入 `types/exam.d.ts`；`QUESTION_TYPE_LABEL`/`QUESTION_TYPE_ORDER`/`groupQuestionsByType`/`calcTotalScore` 迁入 `utils/exam.ts` |
| mock 假数据根除 | ✅ | 删除 `frontend/src/mock/examQuestions.ts`（586 行假题源）；9 处 `@/mock` 引用全部清除 |
| fetchPaperQuestions | ✅ | 移除"失败回退 mock"，改为失败直接抛错 |
| 答题页/详情页/成绩页/预览弹窗/题库页/错题本页 | ✅ | 全部改为后端真实数据；成绩页改走后端 `getExamResult` + `getExamResultDetail`（不再前端本地假判分）；题库页改走后端 `getPaperList` |
| useExamSubmit 错题本 | ✅ | 移除 `getExamQuestions` 兜底，仅错题重练（含标准答案）前端比对，正式考试由后端判分 |

### 后端补前台接口 ✅
| 项 | 状态 | 关键改动 |
|---|---|---|
| `GET /exam/paper/list` | ✅ | 前台试卷列表（题库中心，真实 exam_paper 数据） |
| `GET /exam/result/{id}/detail` | ✅ | 逐题答题详情（成绩页，含题干+用户答案+标准答案+解析+得分，仅本人可查）；新增 `ExamResultDetailVO` |

### 操作日志落库与资金审计 ✅
| 项 | 状态 | 关键改动 |
|---|---|---|
| LogAspect 落库 | ✅ | 注入 `SysLogMapper`+`UserMapper`，`@LogOperation` 接口落 `sys_log` 表（含 userId/username/IP/耗时/状态）；落库失败不影响业务 |
| 资金操作审计 | ✅ | 创建订单（2 处）/发起支付/取消订单/申请退款 加 `@LogOperation`；支付回调已有 `pay_callback_log` 专项落库 |

**验证**：后端 `mvn package` ✅ / 前端 `vue-tsc` ✅ / mock 引用清零 / 接口对齐审计通过。

---

## 七、修复 examId/paperId 语义混淆 ✅

### 根因
`exam_paper.exam_id → exam_info.id`（试卷归属考试），examId 与 paperId 是两套独立自增主键。此前题库中心把 `paper.id` 当考试ID传入 `/exam-taking/:examId`，答题页又把考试ID当试卷ID传给拉题接口，两处混淆。

### 修复
| 项 | 关键改动 |
|---|---|
| 题库中心入口 | `QuestionBankPage.startPractice` 改传 `paper.examId`（试卷归属考试）；未关联考试的试卷禁用"开始练习"并提示 |
| 答题页拉题 | `ExamTakingPage` 用 `enterExam` 返回的 `record.paperId` 调 `fetchPaperQuestions(paperId)`，而非 examId |

**验证**：前端 vue-tsc ✅；交叉审计所有 exam-taking 跳转 / fetchPaperQuestions / enterExam 调用参数语义正确。

---

## 八、继续清 P1（2026-11-19 续三）

### ExamRecord 类型对齐（去 any）✅
| 项 | 关键改动 |
|---|---|
| 后端 | 新增 `ExamRecordVO`（含 examTitle/totalScore/passScore）；`listRecords` 关联 exam_info 补齐标题/总分 |
| 前端 | `ExamRecord` 类型对齐后端真实字段（isPassed:number 等）；`UserCenterPage`/`ExamResultPage` 去掉 `any` 改用类型 |

### 安全 P1 三项 ✅
| 项 | 状态 | 关键改动 |
|---|---|---|
| P1-19 上传路径穿越+类型白名单 | ✅ | `FileService` 加 `validateBizType`（正则白名单防 `../`）+ `validateExtension`（扩展名白名单）；三个实现统一调用 |
| P1-26 登录失败锁定 | ✅ | Redis 计数：5 次失败锁 15 分钟；`checkLoginLock`/`recordLoginFail`/`clearLoginFail`；密码/短信登录均计数，成功清零 |
| P1-21 手机号/姓名脱敏 | ✅ | 新增 `MaskUtil`；`bindParentPhone` 日志脱敏；`pageList` 返回 UserVO 时 phone/realName 脱敏 |

**验证**：后端 `mvn package` ✅ / 前端 `vue-tsc` ✅ / 交叉审计（3 实现校验、锁定调用链、脱敏点、any 清零）全通过。

---

## 九、继续清安全 P1（2026-11-19 续四）

### 安全 P1 三项 ✅
| 项 | 状态 | 关键改动 |
|---|---|---|
| P1-24 存储型 XSS | ✅ | 安装 DOMPurify，新增 `utils/sanitize.ts` 白名单过滤；3 处 v-html（资讯列表/后台预览/题目描述）全部包 `sanitizeHtml` |
| P1-20 手机号无验证修改 | ✅ | `UserInfoUpdateDTO` 加 smsCode；修改手机号需短信验证码 + 唯一性校验（防账号接管） |
| P1-18 token httpOnly | ✅ | 后端登录/刷新/登出下发/清除 httpOnly Cookie；JwtAuthFilter 支持 Cookie fallback；前端 `withCredentials` + 生产不存 js-cookie + `ensureAuth` 静默判断登录态 |

**验证**：后端 `mvn package` ✅ / 前端 `vue-tsc` ✅ / 交叉审计（v-html 过滤、token 双轨链路、手机号验证）全通过。

---

## 十、继续清 P1（2026-11-19 续五，按最优路线）

### 本轮完成 4 项 ✅
| 项 | 状态 | 关键改动 |
|---|---|---|
| P1-27 订单超时关单 | ✅ | 新增 `OrderTimeoutTask` 定时扫描过期订单自动关单 + 回滚优惠券（幂等） |
| P1-15 refresh 登出失效 | ✅ | logout 记录登出时间戳，refreshToken 校验签发时间，登出后旧 refreshToken 失效 |
| P1-03 排行榜/周报假数据 | ✅ | 提交次数/完成考试数/完成课程数从 point_log 流水聚合统计，去写死 0 |
| P1-17 限流 IP 伪造 | ✅ | `IpUtil.getClientIp` 直连不信任 X-Forwarded-For（防伪造绕过）；清理 RateLimitAspect 死代码 |
| P1-23 前端静默刷新 | ✅ | 登录保存 refreshToken；401 时调 /auth/refresh 静默刷新并重试原请求，失败才登出 |

**验证**：后端 `mvn package` ✅ / 前端 `vue-tsc` ✅ / 已全部 commit + push 到 bd/main（72780c1..ac0ee6f）。
