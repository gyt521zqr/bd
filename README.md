# Magao Platform - 码高少儿编程教育平台

> 面向 6-18 岁青少年的在线编程教育平台，支持 Scratch/ScratchJr 可视化编程、Python/C++/GoC 代码编程、在线考试评测、课程学习、积分成长体系等全链路功能。

---

## 项目简介

Magao Platform 是一套完整的青少儿编程教育解决方案，采用前后端分离架构，涵盖从课程内容管理到在线考试评测的全流程。

平台支持两种编程模式：
- **可视化编程**：Scratch 3.0、ScratchJr 拖拽式积木编程
- **代码编程**：Python、C++、GoC，内置 Monaco Editor（VS Code 同款编辑器）

内置 Judge0 代码评测引擎，可对学员提交的代码进行自动化评测。同时提供完善的积分成长体系、勋章系统和班级管理功能。

### 核心功能

| 功能模块 | 说明 |
|---------|------|
| **课程管理** | 视频课程上传、章节管理、学习进度追踪、视频水印保护、弹幕互动 |
| **编程练习** | Scratch/ScratchJr 可视化编程、代码编辑器（Monaco）、Judge0 自动评测 |
| **在线考试** | 试卷管理、防作弊监控、自动阅卷、代码差异对比、成绩发布 |
| **题库中心** | 单选/多选/判断/填空/编程五种题型、批量导入导出、智能组卷 |
| **支付系统** | 微信支付 V3、支付宝当面付、订单管理、退款处理 |
| **成长体系** | 积分系统、等级勋章、排行榜、积分商城 |
| **班级管理** | 教师班级创建、作业布置、专属考试、学生进度查看 |
| **消息通知** | 站内消息、系统公告 |
| **营销活动** | 优惠券、会员体系 |
| **后台管理** | 数据概览、用户管理、课程管理、题库管理、考试管理、文件管理、营销管理、系统设置 |

---

## 技术栈

### 前端

| 技术 | 版本 | 说明 |
|------|------|------|
| Vue | 3.4.21 | 渐进式 JavaScript 框架 |
| TypeScript | 5.4.2 | 类型安全的 JavaScript 超集 |
| Vite | 5.2.0 | 下一代前端构建工具 |
| Vue Router | 4.3.0 | 官方路由管理器 |
| Pinia | 2.1.7 | Vue 3 官方状态管理库 |
| Element Plus | 2.6.3 | 基于 Vue 3 的组件库 |
| SCSS | 1.72.0 | CSS 预处理器 |
| Axios | 1.6.8 | HTTP 客户端 |
| Monaco Editor | 0.55.1 | VS Code 同款代码编辑器 |
| Blockly | 13.0.0 | Google 可视化编程库 |
| ECharts | 5.5.0 | 数据可视化图表库 |

### 后端

| 技术 | 版本 | 说明 |
|------|------|------|
| Spring Boot | 3.2.5 | Java Web 开发框架 |
| Java | 17 | 编程语言 |
| MyBatis-Plus | 3.5.5 | 增强版 ORM 框架 |
| MySQL | 8.0+ | 关系型数据库 |
| Redis | 7+ | 缓存与分布式锁 |
| RabbitMQ | 3.12 | 消息队列中间件 |
| MinIO | 8.5.9 | 对象存储服务 |
| JWT | 0.12.5 | JSON Web Token 认证 |
| SpringDoc | 2.5.0 | OpenAPI/Swagger 文档 |
| Hutool | 5.8.26 | Java 工具类库 |

### 基础设施

| 技术 | 说明 |
|------|------|
| Judge0 CE | 多语言代码评测沙箱引擎 |
| Nginx | 反向代理与静态资源服务 |
| Docker | 容器化部署 |
| Docker Compose | 多容器编排 |

---

## 访问地址

### 本地开发

| 端 | 地址 | 说明 |
|------|------|------|
| 前台首页 | http://localhost:5173/ | 学生端 |
| 管理后台 | http://localhost:5173/admin | 管理员 / 教师端 |
| 后端 API | http://localhost:8080 | Spring Boot 服务 |
| Swagger UI | http://localhost:8080/swagger-ui.html | API 文档 |
| 默认账号 | admin / admin123 | 超级管理员 |

### 生产部署

| 端 | 地址 | 说明 |
|------|------|------|
| 前台 / 后台 | https://<你的域名>/ | Nginx 托管前端静态资源并反代 `/api` |
| API 文档 | https://<你的域名>/api/... | 经 Nginx 反代到后端 |

---

## 目录结构

```
magao-platform/
├── backend/                          # 后端 Spring Boot 项目
│   ├── src/main/java/com/magao/backend/
│   │   ├── common/                   # 公共模块（注解、切面、配置、常量、DTO、异常、过滤器、结果封装、工具类）
│   │   ├── module/                   # 业务模块
│   │   │   ├── classmanage/          # 班级管理
│   │   │   ├── competition/          # 竞赛管理
│   │   │   ├── course/               # 课程管理
│   │   │   ├── exam/                 # 考试管理
│   │   │   ├── file/                 # 文件管理
│   │   │   ├── growth/               # 成长体系
│   │   │   ├── judge/                # 代码评测
│   │   │   ├── marketing/            # 营销活动
│   │   │   ├── message/              # 消息通知
│   │   │   ├── payment/              # 支付系统
│   │   │   ├── practice/             # 编程练习
│   │   │   ├── system/               # 系统管理
│   │   │   └── user/                 # 用户管理
│   │   ├── mq/                       # 消息队列（生产者/消费者）
│   │   └── MagaoApplication.java     # 启动类
│   ├── src/main/resources/
│   │   ├── mapper/                   # MyBatis XML 映射文件
│   │   ├── db/                       # 数据库初始化脚本
│   │   ├── application.yml           # 主配置
│   │   ├── application-dev.yml       # 开发环境配置
│   │   └── application-prod.yml      # 生产环境配置
│   └── pom.xml                       # Maven 依赖配置
│
├── frontend/                         # 前端 Vue 3 项目
│   ├── src/
│   │   ├── api/                      # API 接口定义（按模块组织）
│   │   ├── assets/styles/            # SCSS 样式资源（全局/变量/混合/重置）
│   │   ├── components/               # 组件库
│   │   │   ├── common/               # 通用组件（Header/Footer/Sidebar/Pagination等）
│   │   │   ├── course/               # 课程组件（VideoPlayer/Danmaku/Watermark）
│   │   │   ├── editor/               # 六大编辑器组件（统一外壳 + 子编辑器）
│   │   │   │   ├── BaseEditor.vue    # 统一外壳：顶栏信息条 + 三栏可拖拽 + 右栏标签页 + 底栏四件套
│   │   │   │   ├── EditorToolbar.vue # 工具条（格式化/字号/主题/模板库/复制/下载/全屏）
│   │   │   │   ├── EditorOutput.vue  # 运行输出 + 判题结果 + 错误行号跳转
│   │   │   │   └── child/            # 子编辑器（Python/C++/GoC/Scratch/ScratchJr）
│   │   │   ├── exam/                 # 考试组件（Paper/Timer/AntiCheat/CodeDiff）
│   │   │   ├── growth/               # 成长组件（Badge/Leaderboard/Medal/PointCounter）
│   │   │   └── payment/              # 支付组件（QRCode/OrderDetail）
│   │   ├── directives/               # 自定义指令（防抖/懒加载/水印）
│   │   ├── hooks/                    # 组合式函数（防作弊/自动保存/提交校验）
│   │   ├── layouts/                  # 布局组件（Admin/Default/Exam）
│   │   ├── router/                   # 路由配置与导航守卫
│   │   ├── stores/                   # Pinia 状态管理
│   │   ├── types/                    # TypeScript 类型定义
│   │   ├── utils/                    # 工具函数（请求/认证/存储/校验/格式化/错误中文化）
│   │   └── views/                    # 页面视图
│   │       ├── admin/                # 管理后台（28个页面）
│   │       ├── about/                # 关于我们
│   │       ├── class/                # 班级
│   │       ├── community/            # 创作社区
│   │       ├── course/               # 课程中心
│   │       ├── error/                # 错误页面
│   │       ├── exam/                 # 考试中心
│   │       ├── growth/               # 成长中心
│   │       ├── home/                 # 首页
│   │       ├── login/                # 登录注册
│   │       ├── marketing/            # 营销活动
│   │       ├── message/              # 消息中心
│   │       ├── news/                 # 新闻资讯
│   │       ├── payment/              # 支付订单
│   │       ├── practice/             # 编程练习
│   │       └── user/                 # 用户中心
│   ├── public/                       # 静态资源（Scratch GUI 入口）
│   ├── .env.development              # 开发环境变量
│   ├── .env.production               # 生产环境变量
│   ├── package.json
│   ├── tsconfig.json
│   └── vite.config.ts
│
├── docker/                           # Docker 部署配置
│   ├── docker-compose.yml            # 生产环境编排
│   ├── docker-compose.dev.yml        # 开发环境编排
│   ├── Dockerfile.backend            # 后端多阶段构建
│   ├── Dockerfile.frontend           # 前端多阶段构建
│   ├── nginx/                        # Nginx 配置
│   │   ├── nginx.conf                # 主配置
│   │   └── conf.d/                   # 站点配置
│   │       ├── frontend.conf         # 前端 SPA 路由
│   │       ├── api.conf              # API 反向代理
│   │       ├── minio.conf            # MinIO 代理
│   │       ├── production.conf       # 生产站点主配置
│   │       └── scratch.conf          # Scratch 静态资源
│   ├── rabbitmq/                     # RabbitMQ 插件配置
│   ├── scratch-gui/                  # Scratch GUI 部署脚本
│   └── scratch-jr/                   # Scratch Jr 部署脚本
│
├── docs/                             # 项目文档
│   ├── api/                          # API 文档
│   ├── architecture/                 # 架构文档
│   ├── db/                           # 数据库文档
│   ├── deploy/                       # 部署文档
│   ├── admin-manual.md               # 管理员操作手册
│   ├── deploy-guide.md               # 部署指南
│   ├── payment-guide.md              # 支付对接配置
│   ├── performance-tuning.md         # 性能调优
│   ├── student-guide.md              # 学员使用指南
│   └── teacher-guide.md              # 教师使用教程
│
├── scripts/
│   └── init-db.sql                   # 数据库初始化脚本
│
├── .env.example                      # 环境变量模板
├── deploy-production.sh              # 生产部署脚本
└── README.md                         # 本文件
```

---

## 环境要求

### 本地开发环境

| 工具 | 版本要求 | 说明 |
|------|---------|------|
| JDK | 17+ | 推荐 Eclipse Temurin 17 |
| Node.js | 18+ | 推荐 LTS 版本 |
| npm | 9+ | 随 Node.js 安装 |
| Maven | 3.8+ | Java 项目构建 |
| MySQL | 8.0+ | 数据库 |
| Redis | 7+ | 缓存服务 |
| Git | 最新版 | 版本管理 |

### Docker 部署环境

| 资源 | 最低要求 | 推荐配置 |
|------|---------|---------|
| Docker | 24.0+ | 最新版 |
| Docker Compose | 2.20+ | 最新版 |
| CPU | 4 核 | 8 核 |
| 内存 | 16 GB | 32 GB |
| 磁盘 | 50 GB SSD | 100 GB SSD |

---

## 快速开始 - 本地开发

### 1. 克隆项目

```bash
git clone <repository-url>
cd magao-platform
```

### 2. 配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env

# 编辑 .env 填写开发环境配置
# 主要配置项：MySQL/Redis/RabbitMQ/MinIO 的端口和密码
```

### 3. 启动基础设施（Docker）

```bash
# 启动 MySQL、Redis、RabbitMQ、MinIO
docker-compose -f docker/docker-compose.dev.yml up -d
```

### 4. 初始化数据库

```bash
# Docker 会自动执行 scripts/init-db.sql
# 如需手动执行：
mysql -h 127.0.0.1 -P 3306 -u root -p magao < scripts/init-db.sql
```

### 5. 启动后端

```bash
cd backend

# 编译项目
mvn clean install -DskipTests

# 启动 Spring Boot（开发模式）
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

后端服务地址：http://localhost:8080

### 6. 启动前端

```bash
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端开发地址：http://localhost:5173

---

## 构建与部署

### 前端构建

```bash
cd frontend
npm install
npm run build
```

构建产物输出到 `frontend/dist/` 目录，可直接部署到任意静态服务器。

### GitHub Pages 部署

```bash
cd frontend

# 构建（使用相对路径，支持子目录部署）
npm run build

# dist 目录即为部署内容
# 上传 dist 目录到 GitHub 仓库，开启 Pages 即可
```

### Docker 一键部署（生产环境）

```bash
cd magao-platform

# 复制并编辑环境变量
cp .env.example .env
vim .env

# 构建并启动
docker-compose -f docker/docker-compose.yml up -d --build
```

---

## 默认账号密码

| 服务 | 地址 | 账号 | 密码 | 说明 |
|------|------|------|------|------|
| 平台管理后台 | http://localhost:5173/admin | admin | admin123 | 超级管理员 |
| MySQL | localhost:3306 | root | root123456 | 数据库（本地 dev） |
| Redis | localhost:6379 | - | 无密码 | 缓存（本地 dev） |
| RabbitMQ 管理台 | http://localhost:15672 | admin | 见 .env | 消息队列 |
| MinIO 控制台 | http://localhost:9001 | 见 .env | 见 .env | 对象存储 |
| Judge0 | http://localhost:2358 | - | - | 代码评测 |

---

## API 文档

后端启动后，可通过以下地址访问 API 文档：

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **OpenAPI JSON**: http://localhost:8080/v3/api-docs
- **Actuator 健康检查**: http://localhost:8080/actuator/health

---

## 前端路由说明

### 前台路由

| 路径 | 页面 | 说明 |
|------|------|------|
| `/` | 首页 | 轮播图、热门赛事、核心功能、学习中心、学术资讯 |
| `/exam` | 赛事中心 | 编程赛事列表、搜索筛选 |
| `/exam/:id` | 赛事详情 | 赛事介绍、报名入口 |
| `/exam/register/:examId` | 赛事报名 | 报名表单 |
| `/course` | 课程中心 | 课程列表、分类筛选 |
| `/course/:id` | 课程详情 | 课程介绍、章节列表 |
| `/learn/:courseId/:chapterId` | 课程学习 | 视频播放、弹幕、学习进度 |
| `/practice` | 编程练习 | 题目列表、在线编程 |
| `/growth` | 成长中心 | 积分、勋章、排行榜 |
| `/user` | 用户中心 | 个人资料、设置、消息 |
| `/about` | 关于我们 | 平台介绍 |
| `/news` | 新闻资讯 | 学术动态 |

### 后台路由（需 admin/teacher 角色）

| 路径 | 页面 | 说明 |
|------|------|------|
| `/admin` | 数据概览 | Dashboard 统计面板 |
| `/admin/student` | 学员管理 | 学员 CRUD、会员设置、批量导入导出 |
| `/admin/teacher` | 教师管理 | 教师 CRUD、权限分配（6大模块） |
| `/admin/regcode` | 注册码管理 | 批量生成、导出、状态管理 |
| `/admin/course` | 课程管理 | 课程 CRUD、视频管理、上架下架 |
| `/admin/trial` | 试听预约 | 试听申请管理 |
| `/admin/exam` | 赛事管理 | 赛事 CRUD、报名名单、成绩导出 |
| `/admin/registration` | 报名管理 | 报名记录审核 |
| `/admin/question-bank` | 题库中心 | 五种题型、批量导入导出、组卷 |
| `/admin/paper` | 试卷中心 | 试卷创建、预览、发布 |
| `/admin/score` | 成绩管理 | 自动阅卷、成绩编辑、证书生成 |
| `/admin/practice` | 练习管理 | 练习题目管理 |
| `/admin/class` | 班级管理 | 班级 CRUD |
| `/admin/marketing` | 营销管理 | 优惠券、会员活动 |
| `/admin/file` | 文件管理 | 文件上传下载 |
| `/admin/settings` | 系统设置 | 平台配置 |

---

## 模块说明

### 后端模块

| 模块 | 职责 |
|------|------|
| 用户模块 | 注册登录、JWT 认证、角色权限（管理员/教师/学员/家长） |
| 课程模块 | 课程 CRUD、章节管理、视频上传、学习进度追踪 |
| 练习模块 | 编程题目管理（含样例 `examples`）、Scratch/代码提交、Judge0 自动评测、Scratch 封面 `coverUrl` 落库 |
| 考试模块 | 试卷管理、考试发布、防作弊监控、自动阅卷、成绩管理 |
| 支付模块 | 微信支付 V3、支付宝当面付、订单管理、退款处理 |
| 成长模块 | 积分系统、等级勋章、排行榜、积分商城 |
| 班级模块 | 教师班级管理、作业布置、学生进度查看 |
| 消息模块 | 站内消息、系统公告、消息推送 |
| 营销模块 | 优惠券、会员体系、活动管理 |
| 文件模块 | MinIO 文件上传/下载/管理、视频水印 |
| 系统模块 | 系统配置、数据字典、操作日志 |

### 前端模块

| 模块 | 职责 |
|------|------|
| 管理后台 | Dashboard、用户/课程/题库/考试/文件/营销/系统管理 |
| 课程中心 | 课程列表、课程详情、视频学习（含弹幕/水印/防录屏） |
| 编程练习 | Scratch/ScratchJr 编辑器、代码编辑器、题目列表、提交评测 |
| 在线考试 | 考试列表、考试答题（含防作弊监控）、成绩查看、证书下载 |
| 成长中心 | 我的积分、勋章展示、排行榜、积分商城 |
| 用户中心 | 个人信息、账号设置、消息中心、Scratch 作品管理 |
| 支付中心 | 订单列表、支付页面、支付结果 |
| 营销中心 | 优惠券领取、会员购买、活动参与 |

---

## 六大编程编辑器

平台内置六个浏览器端在线编辑器，全部在浏览器内执行代码，无需后端编译服务。由 `BaseEditor.vue` 统一外壳管理，按语言类型动态加载子编辑器。

### 编辑器一览

| 编辑器 | 语言标识 | 技术方案 | 运行方式 | 特点 |
|--------|---------|---------|---------|------|
| Python | `python` | Monaco + Pyodide (WASM) | Worker 执行 CPython | 支持标准输入、错误中文化 |
| Python Turtle | `pythonturtle` | Monaco + Pyodide | Worker 执行 + Canvas | 海龟画布（网格/坐标轴/缩放） |
| C++ | `cpp` | Monaco + C++→JS 转译器 | Worker 转译执行 | 标准输入、错误中文化 |
| GoC | `goc` | iframe 独立工程 | 转译执行 + Canvas | 海龟绘图、16 色调色板、错误中文化 |
| Scratch 3.0 | `scratch3` | iframe + Scratch GUI | VM greenFlag | 示例项目加载、导出 sb3、缩略图 |
| ScratchJr | `scratchjr` | iframe + ScratchJr | 积木运行 | 导出项目、缩略图 |

### 统一外壳（BaseEditor）

六个编辑器共用同一套外壳，保证练习与考试场景体验一致：

- **顶部考试信息条**：题号 / 倒计时（父级驱动显示，考试模式）/ 当前语言 / 已提交次数
- **三栏可拖拽布局**（代码类）：题面 + 代码区 + 结果区，分隔条可拖拽（宽度 28%–75% 钳制）
- **右栏标签页**：控制台（运行输出）/ 结果对比（期望 vs 实际）/ 图形输出（Turtle 画布，经 Teleport 挂载）
- **底部统一动作栏**：运行 / 提交 / 重置 / 草稿 四件套，顺序、颜色、位置一致
- **窄屏自适应**：`max-width:860px` 时三栏纵向堆叠、隐藏分隔条

### 编辑器能力清单

| 能力 | 说明 |
|------|------|
| 统一工具条 | 格式化 / 字号(12-20px) / 主题(暗↔亮) / 模板库 / 复制代码 / 下载 / 全屏 |
| 输出区增强 | 复制输出、行数/字符数统计、错误行号跳转（点击定位到代码并高亮） |
| 错误中文化 | `utils/errorTranslator.ts` 统一翻译 Python/C++/GoC 常见错误并提取行号 |
| 模板库 | 按语言分组的进阶示例（二分/DFS/质数/螺旋/五角星等，带 `^占位^` 选中填充） |
| 快捷操作 | Ctrl/⌘+Enter 运行、Ctrl/⌘+S 保存 |
| 样例填入/运行对比 | 练习页题目样例一键填入输入，即时 diff 期望输出 |
| Scratch 示例项目 | 从 `public/scratch/examples/*.json` 加载官方示例 |
| Scratch 导出 | 导出 sb3 / 项目文件、捕获 240×180 缩略图 |
| 新手引导浮层 | Scratch/ScratchJr 首次进入引导（localStorage 记忆） |

### 接入场景

| 场景 | 页面 | 说明 |
|------|------|------|
| 编程练习 | `views/practice/PracticeEditorPage.vue` | 完整外壳，支持提交判题 |
| 考试答卷 | `views/exam/ExamTakingPage.vue` | 外壳 + 倒计时 + 题号导航 |
| 竞赛房间 | `views/exam/ExamRoomPage.vue` | Scratch 竞赛，`hideSubmit` 隐藏底栏提交（走独立竞赛接口） |

---

## 开发规范

### 前端

- 使用 Vue 3 Composition API + `<script setup>` 语法
- 组件命名采用 PascalCase，页面组件以 `Page` 后缀结尾
- API 接口按模块组织在 `src/api/` 目录下
- 类型定义集中在 `src/types/` 目录
- 样式使用 SCSS，全局变量定义在 `assets/styles/variables.scss`
- 路由配置集中在 `src/router/routes.ts`

### 后端

- 采用分层架构：Controller -> Service -> Mapper -> Entity
- 统一返回结果封装：`Result<T>`
- 统一异常处理：`GlobalExceptionHandler`
- 接口文档通过注解自动生成（SpringDoc OpenAPI）
- 数据库操作使用 MyBatis-Plus，复杂查询使用 XML Mapper

---

## 相关文档

| 文档 | 说明 |
|------|------|
| [部署指南](docs/deploy/deploy-guide.md) | 服务器部署详细步骤 |
| [性能调优](docs/deploy/performance-tuning.md) | 高并发性能优化方案 |
| [支付对接](docs/deploy/payment-guide.md) | 微信支付/支付宝配置手册 |
| [管理员手册](docs/deploy/admin-manual.md) | 后台管理操作指南 |
| [教师教程](docs/deploy/teacher-guide.md) | 教师端使用教程 |
| [学员指南](docs/deploy/student-guide.md) | 学员端使用指南 |

---

## 许可证

本项目为私有项目，未经授权不得复制、分发或修改。

---

> 文档更新日期：2026-10-28。六大编辑器统一外壳、Scratch 封面落库、练习题样例字段等为近期新增能力。
