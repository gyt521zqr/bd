# Magao Platform 功能清单文档（最新版）

> 版本：v1.0.0（含 C++/Python 编辑器 stdin 输入支持）
> 平台定位：面向 6-18 岁青少年的在线编程教育平台

---

## 一、平台概览

| 维度 | 内容 |
|------|------|
| 目标用户 | 6-18 岁青少年学员、教师、家长、平台管理员 |
| 支持编程模式 | 可视化积木编程（Scratch / ScratchJr）+ 代码编程（Python / C++ / GoC） |
| 代码评测 | 内置 Judge0 多语言评测引擎，支持自动评测 |
| 学习闭环 | 课程学习 → 编程练习 → 在线考试 → 成绩证书 → 积分勋章成长 |

---

## 二、编程编辑器（核心功能）

前端编辑器组件位于 `frontend/src/components/editor/child/`：

| 编辑器 | 组件 | 技术 | 说明 |
|--------|------|------|------|
| Scratch 积木 | `ScratchChildEditor.vue` | Scratch GUI | 拖拽式积木编程 |
| ScratchJr 积木 | `ScratchJrChildEditor.vue` | ScratchJr | 低龄图形化编程 |
| Python | `PythonChildEditor.vue` | Pyodide（WASM） | 浏览器内运行 Python |
| Python Turtle | `PythonChildEditor.vue` | Pyodide | 海龟绘图模式 |
| GoC | `GoCChildEditor.vue` | 自研 | C++ 简化绘图语言 |
| C++ | `CppChildEditor.vue` | 自研转译器 + Monaco | 浏览器内运行 C++ 子集 |

### 2.1 stdin 标准输入支持（本次新增）

| 语言 | 输入方式 | 实现 |
|------|----------|------|
| C++ | 编辑器下方「输入 (stdin)」面板，多个值用空格或换行分隔 | `cin >> x` 转译为 `_cinRead()`，从 token 缓冲区按序读取（数字自动转 Number，字符串原样） |
| Python | 编辑器下方「输入 (stdin)」面板，每行对应一次 `input()` 调用 | `input()` 从 stdin 缓冲区按行读取 |

- 输入会随运行通过 `postMessage({ stdin })` 传给 Web Worker。
- 未填写输入时保持向后兼容：`cin` 读 0、`input()` 返回空字符串。
- 已验证：C++ 输入 `3 5` → `sum = 8`；C++ 输入 `Bob 30` → `Hello Bob, age 30`；Python 输入 `Alice` / `25` 两行 → `Hello Alice, you are 25 years old!`。

---

## 三、前台功能模块（`frontend/src/views/`）

| 模块 | 页面目录 | 功能 |
|------|----------|------|
| 首页 | `home/` | 轮播图、热门赛事、核心功能、学习中心、学术资讯 |
| 赛事中心 | `exam/` | 赛事列表、赛事详情、报名入口与表单 |
| 课程中心 | `course/` | 课程列表、分类筛选、课程详情、章节学习 |
| 课程学习 | `course/`（learn） | 视频播放、弹幕互动、学习进度追踪 |
| 编程练习 | `practice/` | 题目列表、在线编程、提交评测 |
| 成长中心 | `growth/` | 我的积分、勋章展示、排行榜、积分商城 |
| 考试中心 | `exam/` | 考试列表、在线答题（防作弊监控）、成绩查看、证书下载 |
| 用户中心 | `user/` | 个人资料、账号设置、消息中心、Scratch 作品管理 |
| 支付中心 | `payment/` | 订单列表、支付页面、支付结果 |
| 营销中心 | `marketing/` | 优惠券领取、会员购买、活动参与 |
| 消息中心 | `message/` | 站内消息、系统公告 |
| 班级 | `class/` | 教师班级、作业、学生进度 |
| 创作社区 | `community/` | 作品分享浏览 |
| 新闻资讯 | `news/` | 学术动态 |
| 关于我们 | `about/` | 平台介绍 |
| 登录注册 | `login/` | 登录、注册 |
| 错误页 | `error/` | 404 等 |

---

## 四、管理后台功能（`frontend/src/views/admin/`，共 28 个页面）

| 页面 | 功能 |
|------|------|
| `DashboardPage` | 数据概览统计面板 |
| `StudentManagePage` | 学员 CRUD、会员设置、批量导入导出 |
| `TeacherManagePage` | 教师 CRUD、权限分配 |
| `RegCodeManagePage` | 注册码批量生成、导出、状态管理 |
| `CourseManagePage` | 课程 CRUD、视频管理、上下架 |
| `CategoryManagePage` | 课程分类管理 |
| `TrialManagePage` | 试听预约管理 |
| `ExamManagePage` | 赛事 CRUD、报名名单、成绩导出 |
| `RegistrationManagePage` | 报名记录审核 |
| `QuestionBankPage` | 题库中心（五种题型、批量导入导出、组卷） |
| `PaperManagePage` | 试卷创建、预览、发布 |
| `ScoreManagePage` | 自动阅卷、成绩编辑、证书生成 |
| `PracticeManagePage` | 练习题目管理 |
| `ClassManagePage` | 班级管理 |
| `MarketingManagePage` | 优惠券、会员活动 |
| `FileManagePage` | 文件上传下载管理 |
| `StorageConfigPage` | 对象存储配置 |
| `BannerManagePage` | 首页轮播管理 |
| `HomepageConfigPage` | 首页配置 |
| `NewsManagePage` | 新闻资讯管理 |
| `PartnerManagePage` | 合作伙伴管理 |
| `AboutConfigPage` | 关于我们配置 |
| `NotificationManagePage` | 通知管理 |
| `AdminAccountManagePage` | 管理员账号管理 |
| `OperationLogPage` | 操作日志 |
| `SystemMonitorPage` | 系统监控 |
| `SystemSettingsPage` | 平台系统设置 |
| `DataBackupPage` | 数据备份 |

---

## 五、后端业务模块（`backend/src/main/java/com/magao/backend/module/`）

| 模块包 | 职责 |
|--------|------|
| `user` | 注册登录、JWT 认证、角色权限（管理员/教师/学员/家长） |
| `course` | 课程 CRUD、章节管理、视频上传、学习进度 |
| `practice` | 编程题目管理、Scratch/代码提交、Judge0 评测 |
| `exam` | 试卷管理、考试发布、防作弊监控、自动阅卷、成绩管理 |
| `competition` | 赛事/竞赛管理、报名 |
| `classmanage` | 教师班级、作业布置、学生进度 |
| `growth` | 积分系统、等级勋章、排行榜、积分商城 |
| `payment` | 微信支付 V3、支付宝当面付、订单、退款 |
| `marketing` | 优惠券、会员体系、活动 |
| `message` | 站内消息、系统公告、消息推送 |
| `file` | 对象存储上传/下载/管理、视频水印 |
| `judge` | Judge0 代码评测对接 |
| `vod` | 火山引擎视频点播 |
| `cms` | 内容管理（新闻、公告、Banner、关于我们等） |
| `system` | 系统配置、数据字典、操作日志、监控、备份 |
| `mq`（`backend/.../mq/`） | 消息队列生产者/消费者 |

### 5.1 公共能力（`backend/.../common/`）

- 统一返回封装 `Result<T>`、全局异常处理 `GlobalExceptionHandler`
- 参数校验（JSR-380）、AOP 切面（日志/限流/防重复提交）
- JWT 过滤器、认证工具类、Redis 工具、对象存储抽象（MinIO / TOS）

---

## 六、题型与评测

| 题型 | 说明 |
|------|------|
| 单选题 | 单选项 |
| 多选题 | 多选项 |
| 判断题 | 对/错 |
| 填空题 | 文本填空 |
| 编程题 | 代码提交，Judge0 自动评测（支持多语言） |

---

## 七、技术栈

### 前端
Vue 3.4 · TypeScript 5.4 · Vite 5.2 · Vue Router 4.3 · Pinia 2.1 · Element Plus 2.6 · Monaco Editor 0.55 · Blockly 13 · ECharts 5.5 · Axios 1.6 · SCSS

### 后端
Spring Boot 3.2.5 · Java 17 · MyBatis-Plus 3.5.5 · MySQL 8 · Redis 7 · RabbitMQ · MinIO 8.5.9 / 火山引擎 TOS · JJWT 0.12.5 · SpringDoc 2.5.0 · Hutool 5.8.26

### 基础设施
Judge0 CE（代码评测）· Nginx（反向代理）· Docker / Docker Compose（部署）

---

## 八、关键特性亮点

- **浏览器内零依赖运行**：Python（Pyodide WASM）、C++（自研转译器）无需后端即可运行调试，且支持标准输入。
- **Scratch 素材本地代理**：Vite 插件优先使用本地缓存，缺失时从 CDN 下载，离线场景自动生成占位符。
- **可视化 + 代码双模编程**：同一平台覆盖积木与真代码，适配不同年龄段。
- **完整教学闭环**：课程、练习、考试、成绩、证书、成长体系一体化。
- **多角色权限**：管理员 / 教师 / 学员 / 家长。
