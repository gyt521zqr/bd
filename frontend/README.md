# 码高少儿编程教育平台 (Magao Platform)

> 面向 6-18 岁青少年的编程教育赛事一体化平台，涵盖六大浏览器端编程编辑器、课程商城、赛事中心、创作社区与全功能后台管理系统。

## 目录

- [项目简介](#项目简介)
- [技术栈](#技术栈)
- [快速开始](#快速开始)
- [项目结构](#项目结构)
- [核心架构](#核心架构)
- [六大编程编辑器](#六大编程编辑器)
- [路由系统](#路由系统)
- [状态管理](#状态管理)
- [API 层设计](#api-层设计)
- [数据持久化机制](#数据持久化机制)
- [后台管理系统](#后台管理系统)
- [组合式函数](#组合式函数)
- [组件库](#组件库)
- [构建与部署](#构建与部署)
- [安全机制](#安全机制)
- [常见问题](#常见问题)
- [维护指南](#维护指南)

---

## 项目简介

码高少儿编程教育平台是基于 Vue 3 + TypeScript + Vite 构建的单页应用，采用 Element Plus UI 框架。平台核心特色：

- **六大浏览器端编程编辑器**：ScratchJr、Scratch 3.0、Python Turtle、Python、GoC、C++，全部在浏览器端执行代码，不依赖后端编译服务
- **双轨持久化机制**：localStorage 优先 + API 自动同步降级，后台管理系统在后端未就绪时仍可独立运行
- **完整前后台架构**：前台 34 个学生页面 + 后台 28 个管理页面 + 6 大编辑器
- **考试防作弊系统**：全屏考试模式 + 切屏检测 + 倒计时管理

## 技术栈

| 类别 | 技术 | 版本 |
|------|------|------|
| 前端框架 | Vue 3 (Composition API) | ^3.4.21 |
| 类型系统 | TypeScript (strict mode) | ^5.4.2 |
| 构建工具 | Vite | ^5.2.0 |
| UI 框架 | Element Plus (中文 locale) | ^2.6.3 |
| 状态管理 | Pinia | ^2.1.7 |
| 路由 | Vue Router (History 模式) | ^4.3.0 |
| 代码编辑器 | Monaco Editor | ^0.55.1 |
| 图表库 | ECharts | ^5.5.0 |
| HTTP 客户端 | Axios | ^1.6.8 |
| 积木编程 | Blockly | ^13.0.0 |
| CSS 预处理 | Sass (SCSS) | ^1.72.0 |
| 代码压缩 | Terser | ^5.48.0 |
| 工具库 | @vueuse/core, dayjs, js-cookie, nprogress, html2canvas, screenfull, xlsx | — |

## 快速开始

### 环境要求

- Node.js >= 18.x（推荐 20.x LTS）
- npm >= 9.x

### 安装与运行

```bash
# 安装依赖
npm install

# 启动开发服务器（默认端口 5173）
npm run dev

# 类型检查 + 生产构建
npm run build

# 预览生产构建产物
npm run preview
```

### 开发服务器地址

- 应用首页：`http://localhost:5173/`
- API 代理：`/api` → `http://localhost:8080`（后端服务）

### 环境变量

| 文件 | 用途 |
|------|------|
| `.env.development` | 开发环境，API 代理到 localhost:8080 |
| `.env.production` | 生产环境，API 由 Nginx 反向代理，需替换 `VITE_APP_BASE_URL` 为实际域名 |

关键变量：

```
VITE_APP_TITLE=码高少儿编程教育平台
VITE_APP_BASE_API=/api
VITE_APP_BASE_URL=http://localhost:3000  # 开发环境
```

---

## 项目结构

```
frontend/
├── public/                         # 静态资源（不经过构建处理）
│   ├── favicon.svg                 # 网站图标
│   ├── cpp/                        # C++ 转译器 Worker
│   │   └── worker.js               # C++ → JS 转译引擎 + 执行引擎
│   ├── goc/                        # GoC 海龟绘图编辑器（独立工程）
│   │   ├── index.html              # GoC 页面入口
│   │   ├── editor.js               # Monaco + Canvas + Worker 管理
│   │   ├── worker.js               # C++ 转译器 + 海龟 API
│   │   ├── turtle.h                # 海龟绘图 C++ 头文件
│   │   └── styles.css              # GoC 编辑器样式
│   ├── pyodide/                    # Python 运行时
│   │   └── worker.js               # Pyodide Worker（CPython WASM v0.26.2）
│   ├── scratch/                    # Scratch 3.0 编辑器（官方 GUI 独立打包）
│   │   ├── index.html              # Scratch 页面入口
│   │   ├── scratch-gui.js          # Scratch GUI 主程序
│   │   ├── scratch-gui-standalone.js # 独立版
│   │   ├── extension-worker.js     # 扩展 Worker
│   │   ├── assets/                 # SVG/PNG 资产文件
│   │   ├── libraries/              # 角色/背景/造型/音效库
│   │   ├── static/blocks-media/    # 积木图标资源
│   │   └── chunks/                 # 多语言 + WASM 模块
│   └── scratch-jr/                 # ScratchJr 编辑器
│       ├── editor.html             # 编辑器页面
│       ├── app.bundle.js           # 应用打包文件
│       ├── css/                    # 13 个样式文件
│       ├── js/                     # common.js / jquery / qiniu
│       ├── pnglibrary/             # ~140 个角色 PNG
│       ├── svglibrary/             # ~130 个 SVG
│       ├── sounds/                 # 音效文件
│       └── localizations/          # 多语言 JSON
│
├── src/
│   ├── api/                        # API 请求模块（21 个文件）
│   │   ├── index.ts                # Axios 实例 + 拦截器
│   │   ├── user.ts                 # 用户认证
│   │   ├── course.ts               # 课程
│   │   ├── exam.ts                 # 考试/赛事
│   │   ├── practice.ts             # 编程练习
│   │   ├── judge.ts                # 代码评测
│   │   ├── scratch.ts              # Scratch 作品
│   │   ├── competition.ts          # 竞赛
│   │   ├── payment.ts              # 支付/订单
│   │   ├── marketing.ts            # 营销
│   │   ├── oss.ts                  # 对象存储
│   │   ├── vod.ts                  # 视频点播
│   │   ├── growth.ts               # 成长体系
│   │   ├── news.ts                 # 资讯
│   │   ├── category.ts             # 分类
│   │   ├── class.ts                # 班级
│   │   ├── file.ts                 # 文件管理
│   │   ├── message.ts              # 消息
│   │   ├── homepage.ts             # 首页配置
│   │   ├── about.ts                # 关于我们配置
│   │   └── partner.ts              # 合作伙伴
│   │
│   ├── components/                 # 可复用组件（39 个）
│   │   ├── common/                 # 通用组件（12 个）
│   │   ├── course/                 # 课程组件（5 个）
│   │   ├── editor/                 # 编辑器组件（11 个）
│   │   ├── exam/                   # 考试组件（5 个）
│   │   ├── growth/                 # 成长组件（4 个）
│   │   └── payment/                # 支付组件（2 个）
│   │
│   ├── composables/                # 组合式函数
│   │   ├── useCrud.ts              # 通用 CRUD 管理逻辑
│   │   └── useOperationLog.ts      # 操作日志记录
│   │
│   ├── directives/                 # 自定义指令
│   │   ├── debounce.ts             # 防抖指令
│   │   └── watermark.ts            # 水印指令
│   │
│   ├── hooks/                      # 自定义 Hooks
│   │   ├── useAntiCheat.ts         # 防作弊逻辑
│   │   ├── useAutoSave.ts          # 自动保存
│   │   └── useSubmitValidation.ts  # 提交验证
│   │
│   ├── layouts/                    # 布局组件
│   │   ├── DefaultLayout.vue       # 前台布局（导航 + 页脚）
│   │   ├── AdminLayout.vue         # 后台布局（侧边栏 + 顶栏）
│   │   └── ExamLayout.vue          # 考试全屏布局
│   │
│   ├── router/                     # 路由配置
│   │   ├── index.ts                # 路由实例（History 模式）
│   │   ├── routes.ts               # 路由表定义
│   │   └── guards.ts               # 路由守卫（权限校验）
│   │
│   ├── stores/                     # Pinia 状态管理
│   │   ├── index.ts                # 统一导出
│   │   ├── user.ts                 # 用户状态
│   │   ├── app.ts                  # 全局 UI 状态
│   │   └── exam.ts                 # 考试运行时状态
│   │
│   ├── styles/
│   │   └── global.scss             # 全局样式（重置 + Element Plus 增强 + 响应式）
│   │
│   ├── types/                      # TypeScript 类型定义
│   │   ├── index.ts                # 统一导出
│   │   ├── api.d.ts                # API 通用类型
│   │   ├── user.d.ts               # 用户类型
│   │   ├── course.d.ts             # 课程类型
│   │   ├── exam.d.ts               # 考试类型
│   │   ├── practice.d.ts           # 练习类型
│   │   ├── payment.d.ts            # 支付类型
│   │   └── editor.ts               # 编辑器类型
│   │
│   ├── utils/                      # 工具函数
│   │   ├── auth.ts                 # Token 管理（Cookie）
│   │   ├── localStore.ts           # localStorage 封装 + STORAGE_KEYS
│   │   ├── format.ts               # 格式化工具
│   │   ├── storage.ts              # 通用存储工具
│   │   └── validator.ts            # 验证工具
│   │
│   ├── views/                      # 页面视图
│   │   ├── home/                   # 首页
│   │   ├── login/                  # 登录/注册
│   │   ├── course/                 # 课程（列表/详情/学习）
│   │   ├── exam/                   # 赛事（列表/详情/报名/考试/成绩）
│   │   ├── practice/               # 编程练习
│   │   ├── community/              # 创作社区
│   │   ├── growth/                 # 成长体系
│   │   ├── marketing/              # 营销（会员/优惠券）
│   │   ├── payment/                # 支付
│   │   ├── message/                # 消息中心
│   │   ├── news/                   # 资讯
│   │   ├── about/                  # 关于我们
│   │   ├── user/                   # 用户中心
│   │   ├── error/                  # 404 页面
│   │   └── admin/                  # 后台管理（28 个页面）
│   │
│   ├── App.vue                     # 根组件
│   ├── main.ts                     # 应用入口
│   └── env.d.ts                    # 环境变量类型声明
│
├── index.html                      # HTML 入口
├── vite.config.ts                  # Vite 构建配置
├── tsconfig.json                   # TypeScript 配置
├── tsconfig.node.json              # Node 环境 TS 配置
├── .env.development                # 开发环境变量
├── .env.production                 # 生产环境变量
├── DEPLOYMENT.md                   # 部署指南
└── package.json
```

---

## 核心架构

### 三套布局系统

| 布局 | 路径前缀 | 用途 | 特点 |
|------|---------|------|------|
| `DefaultLayout` | `/` | 前台学生端 | 顶部导航 + 面包屑 + 内容区 + 页脚 + 悬浮客服 |
| `AdminLayout` | `/admin` | 后台管理端 | 深色侧边栏 + 顶栏（通知/全屏/用户）+ 内容区，支持折叠 |
| `ExamLayout` | `/exam-taking` | 在线考试 | 全屏无干扰，隐藏所有导航元素 |

### API 降级策略

大部分前台页面采用 Mock 降级机制：API 请求失败时回退到内置默认数据且不弹错误提示。`useCrud` 组合式函数实现了 API + localStorage 双模式：

```
API 请求成功 → 使用 API 数据
API 请求失败 → 读取 localStorage 数据 → 写入 localStorage
增删改操作   → 先写 localStorage → 尝试 API 同步
```

### Monaco Editor Worker 配置

`main.ts` 中通过 Vite `?worker` 语法预加载 5 个 Monaco Worker（editor、json、css、html、ts），在 `window.MonacoEnvironment.getWorker` 中按 label 分发，确保代码编辑器不阻塞主线程。

---

## 六大编程编辑器

所有编辑器由 `BaseEditor.vue` 统一管理，根据语言类型动态加载子组件。

### 技术方案对比

| 编辑器 | 技术栈 | 运行方式 | WebWorker | WASM | 绘图 | 超时保护 |
|--------|--------|---------|-----------|------|------|---------|
| ScratchJr | iframe + 官方 ScratchJr | iframe 内积木运行 | 否 | 否 | 内置舞台 | 10s 加载 |
| Scratch 3.0 | iframe + Scratch GUI (React) | VM greenFlag | 间接 | 否 | WebGL 舞台 | 30s VM |
| Python Turtle | Monaco + Pyodide | Worker 执行 Python | 是 | 是 | Canvas 480x360 | 15s 运行 |
| Python | Monaco + Pyodide | Worker 执行 Python | 是 | 是 | 无 | 15s 运行 |
| GoC | iframe + Monaco + 转译器 | C++→JS 转译执行 | 是 | 否 | Canvas 600x500 | 10s+15s 双层 |
| C++ | Monaco + 转译器 | C++→JS 转译执行 | 是 | 否 | 无 | 15s 运行 |

### 编辑器组件结构

```
components/editor/
├── BaseEditor.vue              # 公共父组件（统一布局/工具栏/输出面板）
├── EditorOutput.vue            # 运行输出 + 判题结果展示
├── EditorToolbar.vue           # 工具栏（运行/提交/保存/格式化/全屏/重置）
├── MonacoCodeEditor.vue        # Monaco 封装（Python/C++ 语法高亮）
├── ScratchEditor.vue           # Scratch iframe 封装
├── ScratchJrEditor.vue         # ScratchJr iframe 封装
└── child/
    ├── CppChildEditor.vue      # C++（Monaco + WebWorker 转译）
    ├── GoCChildEditor.vue      # GoC（iframe 嵌入独立工程）
    ├── PythonChildEditor.vue   # Python（Monaco + Pyodide + Turtle 画布）
    ├── ScratchChildEditor.vue  # Scratch（iframe + postMessage）
    └── ScratchJrChildEditor.vue # ScratchJr（iframe + postMessage）
```

### GoC 海龟 API

`turtle.h` 提供以下接口，坐标系规则：初始朝向向右（0 度），顺时针为正：

```
fd() bk() rt() lt() pencolor() pensize() penup() pendown()
speed() circle() hideturtle() showturtle()
```

### Worker 消息协议

所有 Worker 采用统一的消息格式：

```typescript
// 主线程 → Worker
{ type: 'run', code: string }

// Worker → 主线程
{ type: 'ready' }
{ type: 'stdout', text: string }
{ type: 'stderr', text: string }
{ type: 'done', success: boolean, error?: string, duration: number }
{ type: 'turtle', command: TurtleCommand }  // Python Turtle / GoC 专用
```

---

## 路由系统

### 路由区块

| 区块 | 路径前缀 | 布局 | 页面数 | 权限 |
|------|---------|------|--------|------|
| 登录/注册 | `/login`, `/register` | 无 | 2 | 公开 |
| 前台页面 | `/` | DefaultLayout | 30 | 登录 |
| 考试全屏 | `/exam-taking/:examId` | ExamLayout | 1 | 登录 |
| 后台管理 | `/admin` | AdminLayout | 28 | admin/teacher |
| 404 | `/:pathMatch(.*)*` | 无 | 1 | 公开 |

### 路由 meta 字段

```typescript
interface RouteMeta {
  title: string              // 页面标题
  roles?: UserRole[]         // 角色权限 ['admin', 'teacher']
  keepAlive?: boolean        // 是否缓存组件
  hideFooter?: boolean       // 是否隐藏页脚
}
```

### 路由守卫

- **白名单**：`/login`、`/register` 无需登录
- **NProgress 进度条**：导航开始/结束自动控制
- **角色校验**：基于 `meta.roles` 做权限判断
- **DEV 角色提升**：开发环境 `student` 自动提升为 `admin`，生产构建通过 Tree-shaking 移除
- **未登录重定向**：跳转 `/login?redirect=原路径`
- **已登录访问登录页**：重定向到首页

---

## 状态管理

### Store 概览

| Store | 文件 | 职责 |
|-------|------|------|
| `useUserStore` | `stores/user.ts` | Token、用户信息、角色判断、登录/登出 |
| `useAppStore` | `stores/app.ts` | 侧边栏折叠、全屏、设备类型、主题 |
| `useExamStore` | `stores/exam.ts` | 考试运行时：倒计时、切屏检测、作答数据 |

### useUserStore

```typescript
// 状态
token: Ref<string>           // 生产环境仅从 Cookie 读取，开发环境回退 'dev-token'
userInfo: Ref<UserInfo|null> // 持久化到 localStorage

// 计算属性
roles: ComputedRef<UserRole[]>    // 兼容 roles 数组 / role 单值 / admin 用户名
isLoggedIn: ComputedRef<boolean>
isAdmin: ComputedRef<boolean>
isTeacher: ComputedRef<boolean>

// 方法
login(loginForm): Promise<void>
fetchUserInfo(): Promise<void>    // 静默模式，失败不弹窗
logout(): Promise<void>
updateUserInfo(data): void
hasAnyRole(...roles): boolean
```

### useExamStore

```typescript
// 防作弊
maxSwitchCount: number = 3        // 最大切屏次数
switchCount: Ref<number>          // 当前切屏次数
isSwitchExceeded: ComputedRef<boolean>

// 倒计时
remainingTime: Ref<number>        // 剩余秒数
formattedTime: ComputedRef<{hours, minutes, seconds}>
isTimeout: ComputedRef<boolean>

// 作答
answers: Ref<Map<number, ExamAnswer>>
answeredCount: ComputedRef<number>
```

---

## API 层设计

### Axios 实例配置

```typescript
// src/api/index.ts
const service = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API,  // /api
  timeout: 30000,
  headers: { 'Content-Type': 'application/json;charset=UTF-8' }
})
```

### 请求拦截器

- 自动添加 `Authorization: Bearer <token>`
- 支持 `silent` 选项：静默失败不弹窗

### 响应拦截器

| 状态码 | 处理 |
|--------|------|
| 200 | 正常返回数据 |
| 401 | 清除 Token + 跳转登录页（DEV 模式跳过） |
| 403 | 提示无权限 |
| 500 | 提示服务器错误 |
| 400/404/408/502/503 | 显示对应错误消息 |

### 通用响应类型

```typescript
interface Result<T> {
  code: number
  message: string
  data: T
}

interface PageResult<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
}
```

### API 模块清单

共 21 个 API 模块，约 120 个接口函数：

| 模块 | 文件 | 主要接口 |
|------|------|---------|
| 用户 | `user.ts` | login, register, getUserInfo, changePassword, sendSmsCode, uploadAvatar |
| 课程 | `course.ts` | getCourseList, getCourseDetail, purchaseCourse, getCourseChapters, getVideoUrl |
| 考试 | `exam.ts` | getExamList, enterExam, submitExam, getExamResult, reportSwitchScreen |
| 练习 | `practice.ts` | getPracticeList, getPracticeDetail, submitCode, getSubmitRecords |
| 评测 | `judge.ts` | submitJudge, getJudgeResult, runCode, saveDraft, loadDraft |
| Scratch | `scratch.ts` | saveWork, loadWorks, loadWork, deleteWork, exportSb3 |
| 竞赛 | `competition.ts` | createCompetition, getCompetitionList, submitCompetitionWork |
| 支付 | `payment.ts` | createOrder, getOrderList, payOrder, pollPayStatus, cancelOrder |
| 营销 | `marketing.ts` | getMembershipPlans, purchaseMembership, getCouponList, receiveCoupon |
| OSS | `oss.ts` | getUploadToken, getSignedUrl, deleteOSSFile, getOSSFileList |
| VOD | `vod.ts` | getUploadAuth, getPlayInfo, getVideoList, deleteVideo |
| 成长 | `growth.ts` | getPointInfo, getPointRecords, getMedalList, getLeaderboard |
| 资讯 | `news.ts` | getNewsList, createNews, updateNews, deleteNews |
| 分类 | `category.ts` | getCategories, createCategory, updateCategory, deleteCategory |
| 班级 | `class.ts` | getClassList, getClassStudents, getHomeworkList, createClass |
| 文件 | `file.ts` | uploadFile, downloadFile, deleteFile, getFileList |
| 消息 | `message.ts` | getMessageList, markMessageRead |
| 首页 | `homepage.ts` | getHomepageConfig, updateHomepageConfig |
| 关于 | `about.ts` | getAboutConfig, updateAboutConfig |
| 合作伙伴 | `partner.ts` | getPartners, createPartner, updatePartner, deletePartner |

---

## 数据持久化机制

### 双轨制持久化

| 机制 | 存储位置 | 用途 | 管理文件 |
|------|---------|------|---------|
| Cookie | 浏览器 | Token（`magao_token`，7 天过期） | `utils/auth.ts` |
| localStorage | 浏览器 | 35 个业务数据键（`magao_` 前缀） | `utils/localStore.ts` |

### localStorage 键名常量

`STORAGE_KEYS` 定义在 `utils/localStore.ts` 中，集中管理所有存储键：

```typescript
export const STORAGE_KEYS = {
  // 用户
  STUDENT_LIST: 'student_list',
  TEACHER_LIST: 'teacher_list',
  ADMIN_ACCOUNT_LIST: 'admin_account_list',
  REG_CODE_LIST: 'reg_code_list',
  // 课程
  COURSE_LIST: 'course_list',
  CATEGORIES: 'categories',
  TRIAL_LIST: 'trial_list',
  // 考试
  EXAM_LIST: 'exam_list',
  REGISTRATION_LIST: 'registration_list',
  QUESTION_LIST: 'question_list',
  PAPER_LIST: 'paper_list',
  SCORE_LIST: 'score_list',
  BANNER_LIST: 'banner_list',
  // 练习
  PRACTICE_QUESTIONS: 'practice_questions',
  // 内容
  NEWS_LIST: 'news_list',
  HOMEPAGE_CONFIG: 'homepage_config',
  ABOUT_CONFIG: 'about_config',
  // 系统
  CLASS_LIST: 'class_list',
  FILE_LIST: 'file_list',
  MARKETING: 'marketing',
  SYSTEM_SETTINGS: 'system_settings',
  NOTIFICATION_LIST: 'notification_list',
  OPERATION_LOG_LIST: 'operation_log',
  DATA_BACKUP_LIST: 'data_backup_list',
  // 配置
  STORAGE_CONFIG / OSS_CONFIG / VOD_CONFIG / POINTS_CONFIG / NOTIFY_CONFIG / SITE_CONFIG
  // 其他
  PARTNERS / HOMEPAGE_BANNERS / FRIEND_LINKS
} as const
```

### localStore API

```typescript
setLocalData<T>(key: string, data: T): void
getLocalData<T>(key: string, fallback: T): T
getLocalData<T>(key: string): T | null
removeLocalData(key: string): void
hasLocalData(key: string): boolean

// 兼容别名
setLocal / getLocal / removeLocal
```

---

## 后台管理系统

### 功能模块（7 大模块 / 28 个页面）

| 模块 | 页面数 | 包含页面 |
|------|--------|---------|
| 数据概览 | 1 | DashboardPage（动态聚合 localStorage 真实数据 + ECharts 图表） |
| 用户中心 | 4 | 学员管理、教师管理、注册码管理、管理员账号 |
| 课程中心 | 3 | 课程管理、分类管理、试听预约 |
| 竞赛中心 | 6 | 赛事管理、Banner 管理、报名管理、题库中心、试卷中心、成绩管理 |
| 内容管理 | 3 | 资讯管理、首页配置、关于我们配置 |
| 练习中心 | 1 | 练习题目管理 |
| 系统管理 | 10 | 班级、文件、营销、合作伙伴、云存储配置、通知管理、操作日志、数据备份、系统监控、系统设置 |

### 标准化管理能力

所有 28 个后台页面均具备：

- **统计卡片**：实时聚合 localStorage 数据
- **搜索筛选**：多条件组合搜索
- **分页组件**：统一分页交互
- **批量操作**：多选删除
- **CSV 导出**：UTF-8 BOM 编码，Excel 兼容
- **详情查看**：弹窗展示完整信息
- **LocalStorage 持久化**：所有 CRUD 操作自动持久化
- **操作日志**：自动记录到 `operation_log`

### 角色权限

| 角色 | 权限 |
|------|------|
| `admin` | 全部 28 个后台页面 |
| `teacher` | 课程、赛事、报名、题库、试卷、成绩、练习、班级、试听 |
| `student` | 仅前台页面（生产环境无后台访问权限） |

---

## 组合式函数

### useCrud\<T\>

通用 CRUD 管理逻辑，支持 API + Mock 双模式：

```typescript
const crud = useCrud<ExamItem>({
  apiBase: '/exam',           // API 基础路径
  mockData: mockExams,         // Mock 兜底数据
  idField: 'id',               // 主键字段
  searchFields: ['title'],     // 搜索字段
  defaultPageSize: 10,         // 默认每页条数
  createDefault: () => ({...}) // 新建默认值
})

// 使用
crud.fetchList()               // 加载列表
crud.handleSearch()            // 搜索
crud.handleCreate()            // 新建
crud.handleEdit(row)           // 编辑
crud.submitForm()              // 提交表单
crud.handleDelete(row)         // 删除
crud.handleBatchDelete()       // 批量删除
crud.handleToggleStatus(row)   // 切换状态
```

### useOperationLog

操作日志记录，持久化到 localStorage（最多 500 条）：

```typescript
const { logCreate, logUpdate, logDelete, logExport, logImport } = useOperationLog('赛事管理')

logCreate('创建赛事：2026码高杯')
logDelete('删除赛事：ID=123')
logExport('导出赛事数据')
```

支持的操作类型：`create` `update` `delete` `login` `export` `import` `config` `batch_delete` `status_change`

---

## 组件库

### 通用组件（common/）

| 组件 | 用途 |
|------|------|
| `AppHeader.vue` | 顶部导航栏 |
| `AppFooter.vue` | 页脚 |
| `BreadcrumbNav.vue` | 面包屑导航 |
| `Pagination.vue` | 分页组件 |
| `ConfirmDialog.vue` | 确认对话框 |
| `FileUpload.vue` | 文件上传 |
| `ImageUploader.vue` | 图片上传器 |
| `ImagePreview.vue` | 图片预览 |
| `CourseCardNew.vue` | 课程卡片 |
| `CompetitionCard.vue` | 竞赛卡片 |
| `FeatureCard.vue` | 特性卡片 |
| `CustomerService.vue` | 悬浮客服 |

### 课程组件（course/）

| 组件 | 用途 |
|------|------|
| `VideoPlayer.vue` | 视频播放器 |
| `DanmakuLayer.vue` | 弹幕层 |
| `WatermarkOverlay.vue` | 水印覆盖层 |
| `ChapterList.vue` | 章节列表 |
| `CourseCard.vue` | 课程卡片 |

### 考试组件（exam/）

| 组件 | 用途 |
|------|------|
| `AntiCheatMonitor.vue` | 防作弊监控 |
| `ExamPaper.vue` | 试卷 |
| `ExamTimer.vue` | 考试计时器 |
| `ExamResult.vue` | 考试结果 |
| `CodeDiffViewer.vue` | 代码差异查看器 |

---

## 构建与部署

### Vite 构建配置

```typescript
// vite.config.ts 关键配置
{
  plugins: [vue(), scratchAssetProxyPlugin()],  // 自定义 Scratch 资产代理插件
  resolve: { alias: { '@': '/src' } },
  server: {
    port: 5173,
    proxy: { '/api': { target: 'http://localhost:8080', changeOrigin: true } },
    watch: { ignored: ['**/public/scratch/**', '**/public/scratchjr/**', ...] }
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          'element-plus': ['element-plus'],
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          'icons': ['@element-plus/icons-vue']
        }
      }
    },
    minify: 'terser',
    terserOptions: { compress: { drop_console: true, drop_debugger: true } },
    sourcemap: false,
    chunkSizeWarningLimit: 500
  }
}
```

### 构建产物

```
dist/
├── index.html              # 入口 HTML
├── js/                     # JavaScript（按 chunk 分包）
├── css/                    # 样式文件
├── images/                 # 图片资源
├── fonts/                  # 字体文件
├── assets/                 # 其他静态资源
├── scratch/                # Scratch 3.0 编辑器
├── scratch-jr/             # ScratchJr 编辑器
├── goc/                    # GoC 编辑器
├── pyodide/                # Python WASM 运行时（~10MB）
└── cpp/                    # C++ 转译器
```

### 部署步骤

1. 修改 `.env.production` 中的 `VITE_APP_BASE_URL` 为实际域名
2. 执行 `npm run build`
3. 将 `dist/` 目录部署到 Web 服务器
4. 配置 Nginx 反向代理（详见 `DEPLOYMENT.md`）

### Nginx 配置要点

```nginx
# SPA History 模式
location / {
  try_files $uri $uri/ /index.html;
}

# API 反向代理
location /api/ {
  proxy_pass http://backend-server:8080/api/;
}

# 静态资源长期缓存
location ~* \.(js|css|png|jpg|gif|svg|woff2?)$ {
  expires 1y;
  add_header Cache-Control "public, immutable";
}

# index.html 不缓存
location = /index.html {
  add_header Cache-Control "no-cache, no-store, must-revalidate";
}
```

---

## 安全机制

### Token 安全

| 措施 | 说明 | 文件 |
|------|------|------|
| Secure | 生产环境 Cookie 仅通过 HTTPS 传输 | `utils/auth.ts` |
| SameSite=Lax | 防 CSRF，允许顶层导航 | `utils/auth.ts` |
| httpOnly | 需后端在 Set-Cookie 响应头中配置 | 后端配合 |
| Bearer Token | 请求头自动添加 `Authorization: Bearer <token>` | `api/index.ts` |

### DEV 模式安全

- `import.meta.env.DEV` 在生产构建时被 Vite 静态替换为 `false`
- DEV 角色提升逻辑（student → admin）在生产构建中通过 Tree-shaking 完全移除
- DEV 模式下 401 错误不跳转登录页，生产环境正常处理

### 代码执行安全

| 编辑器 | 安全措施 |
|--------|---------|
| C++ | WebWorker 隔离 + 执行次数上限 100 万 + 15s 超时 + use strict |
| GoC | WebWorker 隔离 + 命令数上限 10 万 + 10s+15s 双层超时 |
| Python | WebWorker 隔离 + Pyodide WASM 沙箱 + 15s 超时 + Worker 重建 |
| Scratch | iframe sandbox 权限限制 + 30s VM 超时 |
| ScratchJr | iframe sandbox + 10s 加载超时 + 提交后锁定 |

### 构建安全

- `drop_console: true` — 移除所有 console 语句
- `drop_debugger: true` — 移除所有 debugger 语句
- `sourcemap: false` — 不生成 Source Map
- TypeScript strict mode — 全项目类型安全

---

## 常见问题

### 1. 开发服务器启动后文件监听报错（ENOSPC）

文件监听数量达到系统限制。已在 `vite.config.ts` 中配置忽略大型静态目录：

```typescript
watch: {
  ignored: ['**/public/scratch/**', '**/public/scratchjr/**', '**/public/goc/**', ...]
}
```

如仍报错，执行：`echo fs.inotify.max_user_watches=524288 | sudo tee -a /etc/sysctl.conf && sudo sysctl -p`

### 2. 生产构建内存不足

```bash
NODE_OPTIONS="--max-old-space-size=4096" npm run build
```

### 3. Scratch 编辑器资产加载失败

开发环境通过 `scratchAssetProxyPlugin` 自动从 S3 CDN 下载缺失资产并生成占位符。生产环境建议预下载资产到本地或配置 CDN 代理。

### 4. Pyodide 加载缓慢

Pyodide WASM 核心约 10MB，从 CDN 加载。生产环境建议：
- 将 Pyodide 文件托管到自有 CDN
- 配置长期缓存（30 天）
- 使用 `preload` 预加载

### 5. 后台管理数据丢失

后台数据存储在浏览器 localStorage 中，清除浏览器缓存会导致数据丢失。建议定期使用后台「数据备份」功能导出数据。

### 6. API 请求 401 循环跳转

检查后端 Token 有效性。开发环境下 `isDevMode` 会跳过 401 处理，生产环境正常跳转登录页。

---

## 维护指南

### 新增后台管理页面

1. 在 `src/views/admin/` 创建页面组件，使用 `useCrud` 组合式函数
2. 在 `src/router/routes.ts` 的 `/admin` children 中添加路由
3. 在 `src/layouts/AdminLayout.vue` 的菜单中添加导航项
4. 在 `src/utils/localStore.ts` 的 `STORAGE_KEYS` 中添加存储键名
5. 在 `src/composables/useOperationLog.ts` 的 `LogModule` 类型中添加模块名

### 新增 API 模块

1. 在 `src/api/` 创建模块文件，导出接口函数
2. 在 `src/types/` 添加对应的类型定义
3. 使用 `import service from '@/api'` 获取 Axios 实例
4. 支持 `silent` 选项：`service.get('/url', { silent: true })`

### 新增编辑器语言

1. 在 `src/types/editor.ts` 的 `EditorLang` 类型中添加语言标识
2. 在 `src/components/editor/child/` 创建子编辑器组件
3. 在 `src/components/editor/BaseEditor.vue` 的组件映射中注册
4. 如需 WebWorker，在 `public/` 下创建对应目录和 `worker.js`

### 修改路由权限

1. 在 `src/router/routes.ts` 的路由 `meta.roles` 中指定角色
2. 路由守卫会自动校验 `meta.roles` 与用户角色的匹配

### TypeScript 类型检查

```bash
# 仅类型检查（不构建）
npx vue-tsc --noEmit

# 类型检查 + 构建
npm run build
```

### 代码风格

- 使用 TypeScript strict mode
- 组件使用 `<script setup lang="ts">` 语法
- 样式使用 SCSS，遵循 BEM 命名或 scoped 隔离
- 路径别名 `@` 指向 `src/`

---

## 相关文档

- [部署指南](computer:///workspace/frontend/DEPLOYMENT.md) — 构建步骤、Nginx 配置、安全检查清单、回滚方案
- [全功能测绘与上线评估报告](computer:///workspace/magao-launch-assessment/magao-launch-assessment.html) — 62 个功能页面详细评估

---

## 版本信息

- **版本**：v1.0.0
- **技术支持**：码高教育科技
- **更新日期**：2026-07-30
