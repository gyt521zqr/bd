# Magao Platform 配置说明文档（最新版）

> 版本：v1.0.0（含 C++/Python 编辑器 stdin 输入支持）
> 适用：本地开发 / Docker 生产部署
> 生成时间：随本文件所在代码快照

---

## 一、项目基本信息

| 项目 | 内容 |
|------|------|
| 项目名称 | Magao Platform（码高少儿编程教育平台） |
| 版本 | 1.0.0 |
| 架构 | 前后端分离（Vue 3 SPA + Spring Boot REST API） |
| 后端坐标 | `com.magao:magao-backend:1.0.0` |
| 后端启动类 | `com.magao.backend.MagaoApplication` |

---

## 二、端口一览（本地开发默认值）

| 服务 | 端口 | 说明 |
|------|------|------|
| 前端开发服务器 | 5173 | Vite dev server，`/api` 代理到 8080 |
| 后端服务 | 8080 | Spring Boot，context-path 为 `/` |
| MySQL | 3306 | 数据库，库名 `magao_platform` |
| Redis | 6379 | 缓存 / 分布式锁 |
| RabbitMQ | 5672 | 消息队列（AMQP）；默认 dev 关闭 `mq.enabled=false` |
| RabbitMQ 管理台 | 15672 | 生产启用 |
| MinIO | 9000 / 9001 | 对象存储（默认 dev 禁用，生产用 TOS 替代） |
| Judge0 | 2358 | 代码评测服务 |
| Nginx | 80 / 443 | 生产反向代理 |

---

## 三、前端配置

### 3.1 关键依赖版本（`frontend/package.json`）

| 依赖 | 版本 |
|------|------|
| vue | ^3.4.21 |
| vue-router | ^4.3.0 |
| pinia | ^2.1.7 |
| element-plus | ^2.6.3 |
| monaco-editor | ^0.55.1 |
| blockly | ^13.0.0 |
| echarts | ^5.5.0 |
| axios | ^1.6.8 |
| vite | ^5.2.0 |
| typescript | ^5.4.2 |
| sass | ^1.72.0 |

### 3.2 npm 脚本

| 命令 | 作用 |
|------|------|
| `npm run dev` | 启动开发服务器（端口 5173） |
| `npm run build` | 类型检查 + 构建（输出 `dist/`） |
| `npm run preview` | 预览构建产物 |

### 3.3 `frontend/vite.config.ts` 要点

| 配置项 | 值 |
|--------|----|
| `base` | `/` |
| 路径别名 | `@` → `frontend/src` |
| dev 服务器端口 | `5173` |
| `/api` 代理 | `http://localhost:8080`（`changeOrigin: true`） |
| 构建输出分片 | element-plus / vue-vendor / icons 独立分包 |
| 压缩 | terser，`drop_console` + `drop_debugger` |
| 特殊插件 | `scratchAssetProxyPlugin`（Scratch 素材本地代理，缺失时从 CDN 下载或生成占位符） |

### 3.4 环境变量

**`frontend/.env.development`**

```
VITE_APP_TITLE=码高少儿编程教育平台
VITE_APP_BASE_API=/api
VITE_APP_BASE_URL=http://localhost:3000
```

**`frontend/.env.production`**

```
VITE_APP_TITLE=码高少儿编程教育平台
VITE_APP_BASE_API=/api
VITE_APP_BASE_URL=https://www.magao.com   # 替换为实际域名
```

---

## 四、后端配置

### 4.1 `application.yml`（公共配置）

| 配置项 | 值 | 说明 |
|--------|----|------|
| `server.port` | 8080 | 服务端口 |
| `server.servlet.context-path` | `/` | 上下文路径 |
| `spring.profiles.active` | `dev` | 默认激活开发环境 |
| `spring.servlet.multipart.max-file-size` | 100MB | 单文件上传上限 |
| `spring.servlet.multipart.max-request-size` | 200MB | 单请求上限 |
| `spring.jackson.time-zone` | GMT+8 | 时区 |
| `spring.jackson.date-format` | `yyyy-MM-dd HH:mm:ss` | 日期格式 |
| `mybatis-plus.mapper-locations` | `classpath*:mapper/**/*.xml` | Mapper XML 路径 |
| `mybatis-plus.logic-delete-field` | `deleted` | 逻辑删除字段 |
| `jwt.secret` | `magao-platform-jwt-secret-key-2024-...` | JWT 密钥（生产需更换） |
| `jwt.access-token-expire` | 7200000（2h） | Access Token 有效期 |
| `jwt.refresh-token-expire` | 604800000（7d） | Refresh Token 有效期 |
| `jwt.header` / `jwt.prefix` | `Authorization` / `Bearer ` | Token 头 |
| `springdoc.swagger-ui.path` | `/swagger-ui.html` | API 文档地址 |
| `minio.enabled` | false | 默认禁用 MinIO |
| `tos.enabled` | true | 默认启用火山引擎 TOS |
| `vod.enabled` | false | 默认禁用视频点播 |
| `judge0.api-url` | `http://localhost:2358` | 评测服务地址 |
| `judge0.timeout` | 30（秒） | 评测超时 |

### 4.2 `application-dev.yml`（开发环境）

| 配置项 | 值 |
|--------|----|
| `mq.enabled` | false（本地未装 RabbitMQ 时跳过 MQ Bean） |
| MySQL URL | `jdbc:mysql://localhost:3306/magao_platform?...serverTimezone=GMT%2B8&allowPublicKeyRetrieval=true` |
| MySQL 账号 | `root` / `root123456` |
| HikariCP | min-idle 5，max-pool-size 20 |
| Redis | `localhost:6379`，无密码，db 0 |
| RabbitMQ | `localhost:5672`，`guest/guest` |

### 4.3 `application-prod.yml`（生产环境）

| 配置项 | 值 |
|--------|----|
| MySQL 主机 | `magao-mysql:3306`（Docker 服务名） |
| MySQL 密码 | `${MYSQL_PASSWORD:magao_prod_2024}` |
| Redis 主机 | `magao-redis:6379`，密码 `${REDIS_PASSWORD:...}` |
| RabbitMQ 主机 | `magao-rabbitmq:5672`，`${RABBITMQ_USER:admin}` |
| HikariCP | min-idle 10，max-pool-size 50 |
| TOS | `enabled: true`，endpoint/region/ak/sk 由环境变量注入 |
| VOD | `enabled: true` |
| Judge0 | `http://magao-judge0:2358`，timeout 60 |
| SQL 日志 | 关闭（NoLoggingImpl） |

---

## 五、根目录环境变量模板（`.env.example`）

| 分组 | 关键变量 |
|------|----------|
| MySQL | `MYSQL_PORT`、`MYSQL_ROOT_PASSWORD`、`MYSQL_DATABASE` |
| Redis | `REDIS_PORT`、`REDIS_PASSWORD` |
| RabbitMQ | `RABBITMQ_PORT`、`RABBITMQ_MGMT_PORT`、`RABBITMQ_USER`、`RABBITMQ_PASSWORD`、`RABBITMQ_VHOST` |
| TOS | `TOS_ENDPOINT`、`TOS_REGION`、`TOS_ACCESS_KEY`、`TOS_SECRET_KEY`、`TOS_BUCKET`、`TOS_PUBLIC_DOMAIN` |
| VOD | `VOD_ACCESS_KEY`、`VOD_SECRET_KEY`、`VOD_REGION`、`VOD_PLAY_DOMAIN`、`VOD_SPACE_NAME` |
| Judge0 | `JUDGE0_PORT` |
| 服务端口 | `BACKEND_PORT`、`NGINX_HTTP_PORT`、`NGINX_HTTPS_PORT` |
| 域名/证书 | `DOMAIN`、`SSL_CERT_PATH`、`SSL_KEY_PATH` |
| 微信支付 | `WECHAT_PAY_APP_ID`、`MCH_ID`、`API_KEY_V3`、`SERIAL_NO`、`NOTIFY_URL` |
| 支付宝 | `ALIPAY_APP_ID`、`ALIPAY_PRIVATE_KEY`、`ALIPAY_PUBLIC_KEY`、`ALIPAY_NOTIFY_URL` |
| JWT | `JWT_SECRET`、`JWT_EXPIRATION` |
| 上传 | `FILE_UPLOAD_MAX_SIZE`、`FILE_ALLOWED_TYPES` |
| 邮件 | `MAIL_HOST`、`MAIL_PORT`、`MAIL_USERNAME`、`MAIL_PASSWORD`、`MAIL_FROM` |

> 使用方式：`cp .env.example .env`，将占位符改为实际值；`.env` 已在 `.gitignore` 中排除。

---

## 六、数据库

| 项 | 值 |
|----|----|
| 库名 | `magao_platform` |
| 字符集 | `utf8mb4` / `utf8mb4_unicode_ci` |
| 初始化脚本 | `schema_init.sql`、`scripts/init-db.sql`、`backend/src/main/resources/db/*.sql` |
| 逻辑删除 | `deleted` 字段（0=未删，1=已删） |
| 主键策略 | 自增（`id-type: auto`） |

初始化命令：

```bash
mysql -u root -proot123456 -e "CREATE DATABASE IF NOT EXISTS magao_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
mysql -u root -proot123456 magao_platform < schema_init.sql
```

---

## 七、默认账号与访问地址（本地）

| 服务 | 地址 | 账号 | 密码 |
|------|------|------|------|
| 前端 | http://localhost:5173 | - | - |
| 管理后台 | http://localhost:5173/admin | admin | admin123 |
| 后端 API 文档 | http://localhost:8080/swagger-ui.html | - | - |
| OpenAPI JSON | http://localhost:8080/v3/api-docs | - | - |
| MySQL | localhost:3306 | root | root123456 |

---

## 八、本地启动命令

```bash
# 仅前端（预览编辑器）
cd frontend && npm install && npm run dev

# 完整（前端 + 后端）
cd backend && mvn clean install -DskipTests && mvn spring-boot:run   # 终端1
cd frontend && npm install && npm run dev                             # 终端2
```

或使用一键脚本：`./quick-start.sh`（使用预编译 jar，需先有 `backend/target/magao-backend-1.0.0.jar`）。

---

## 九、本次快照的配置/功能变更

| 变更 | 涉及文件 | 说明 |
|------|----------|------|
| C++ 编辑器支持 stdin | `frontend/public/cpp/worker.js`、`CppChildEditor.vue` | `cin >> x` 转译为 `_cinRead()`，从 stdin 缓冲区按空格/换行分词读取 |
| Python 编辑器支持 stdin | `frontend/public/pyodide/worker.js`、`PythonChildEditor.vue` | `input()` 从 stdin 缓冲区按行读取 |
| 新增输入面板 | 两个 `*ChildEditor.vue` | 编辑器下方 textarea，运行时通过 postMessage 传递 `stdin` |

> 兼容性：未填写 stdin 时，`_cinRead()` 返回 0、`input()` 返回空字符串，与改动前行为一致。

---

## 十、编辑器页面直达地址（本地）

| 编辑器 | 地址 |
|--------|------|
| ScratchJr | `http://localhost:5173/practice/12?lang=scratchjr` |
| Scratch | `http://localhost:5173/practice/12?lang=scratch` |
| Python Turtle | `http://localhost:5173/practice/12?lang=pythonturtle` |
| Python | `http://localhost:5173/practice/12?lang=python` |
| GoC | `http://localhost:5173/practice/12?lang=goc` |
| C++ | `http://localhost:5173/practice/12?lang=cpp` |
