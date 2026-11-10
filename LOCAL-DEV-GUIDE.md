# 码高少儿编程平台 - 本地开发环境搭建指南 (Mac)

本文档指导你在本地 Mac 上直接运行项目，不再依赖 TRAE 远程沙箱。

---

## 一、环境要求

| 工具 | 版本要求 | 安装方式 |
|------|---------|---------|
| Node.js | >= 18.x | `brew install node` 或去 https://nodejs.org 下载 |
| Java (JDK) | 17 | `brew install openjdk@17` |
| Maven | >= 3.8 | `brew install maven` |
| MySQL | 8.x | `brew install mysql` 或安装 MySQL官方版 |
| Redis | >= 6.x | `brew install redis` |
| Git | 最新版 | Mac 自带或 `brew install git` |

> 如果你不用后端 API（只看前端页面），只需安装 **Node.js** 即可。

---

## 二、快速开始（仅前端 - 最简方案）

如果你只需要预览前端页面和编辑器功能（不需要登录、保存等后端功能），这是最快的方案：

### 1. 下载项目到本地

在 Mac 终端执行：
```bash
# 从 TRAE 工作区复制项目（或用 git clone 你的仓库）
# 方法1: 如果你已配置远程仓库
git clone <你的仓库地址> ~/projects/magao-platform

# 方法2: 从 TRAE 导出项目文件，放到本地目录
```

### 2. 安装依赖并启动

```bash
cd ~/projects/magao-platform/frontend
npm install        # 安装依赖（首次需要 1-3 分钟）
npm run dev        # 启动 Vite 开发服务器
```

### 3. 访问页面

浏览器打开：**http://localhost:5173**

直接访问编辑器页面（无需登录）：
- http://localhost:5173/practice/12?lang=scratchjr
- http://localhost:5173/practice/12?lang=scratch
- http://localhost:5173/practice/12?lang=pythonturtle
- http://localhost:5173/practice/12?lang=python
- http://localhost:5173/practice/12?lang=goc
- http://localhost:5173/practice/12?lang=cpp

> 注意：没有后端时，API 调用会报错，但编辑器本身可以正常使用。

---

## 三、完整方案（前端 + 后端）

需要登录、保存作品、竞赛等功能时，需要同时运行后端。

### 1. 安装 MySQL

```bash
brew install mysql
brew services start mysql

# 安全初始化（设置 root 密码为 root123456）
mysql_secure_installation

# 创建数据库
mysql -u root -p
```

在 MySQL 中执行：
```sql
CREATE DATABASE magao_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE magao_platform;
SOURCE /path/to/magao-platform/schema_init.sql;
```

### 2. 安装 Redis

```bash
brew install redis
brew services start redis
```

### 3. 安装 RabbitMQ（可选）

```bash
brew install rabbitmq
brew services start rabbitmq
```

> RabbitMQ 用于消息队列，如果不用消息功能可以先跳过。跳过时需在后端配置中设置 `mq.enabled: false`。

### 4. 启动后端

```bash
cd ~/projects/magao-platform/backend
mvn clean install -DskipTests   # 编译（首次需要 3-5 分钟）
mvn spring-boot:run             # 启动后端服务
```

后端启动在 **http://localhost:8080**

### 5. 启动前端（另一个终端窗口）

```bash
cd ~/projects/magao-platform/frontend
npm install
npm run dev
```

### 6. 完整访问

浏览器打开 **http://localhost:5173**，此时前端会自动代理 `/api` 请求到后端 `http://localhost:8080`。

---

## 四、一键启动脚本（可选）

创建一个 `start-local.sh` 文件：

```bash
#!/bin/bash
# 码高平台本地一键启动脚本

echo "======================================"
echo "  码高少儿编程平台 - 本地开发环境"
echo "======================================"

# 检查依赖
echo ""
echo "[1/4] 检查环境依赖..."

if ! command -v node &> /dev/null; then
    echo "  ERROR: Node.js 未安装，请运行: brew install node"
    exit 1
fi
echo "  OK Node.js $(node -v)"

if ! command -v java &> /dev/null; then
    echo "  WARNING: Java 未安装，将仅启动前端"
    FRONTEND_ONLY=true
else
    echo "  OK Java $(java -version 2>&1 | head -1)"
    FRONTEND_ONLY=false
fi

# 检查 MySQL
if ! command -v mysql &> /dev/null; then
    echo "  WARNING: MySQL 未安装，将仅启动前端"
    FRONTEND_ONLY=true
else
    echo "  OK MySQL 已安装"
fi

# 检查 Redis
if redis-cli ping &> /dev/null; then
    echo "  OK Redis 运行中"
else
    echo "  WARNING: Redis 未运行"
fi

# 启动前端
echo ""
echo "[2/4] 启动前端 (Vite)..."
cd frontend
if [ ! -d "node_modules" ]; then
    echo "  首次运行，安装依赖..."
    npm install
fi
echo "  前端启动: http://localhost:5173"

if [ "$FRONTEND_ONLY" = true ]; then
    echo ""
    echo "======================================"
    echo "  仅前端模式启动"
    echo "  浏览器访问: http://localhost:5173"
    echo "======================================"
    npm run dev
    exit 0
fi

# 启动后端
echo ""
echo "[3/4] 启动后端 (Spring Boot)..."
cd ../backend
echo "  后端启动: http://localhost:8080"

echo ""
echo "[4/4] 全部服务启动完成"
echo "======================================"
echo "  前端: http://localhost:5173"
echo "  后端: http://localhost:8080"
echo "  API文档: http://localhost:8080/swagger-ui.html"
echo "======================================"

# 同时启动前端和后端
cd ../frontend
npm run dev &
cd ../backend
mvn spring-boot:run
```

使用方式：
```bash
chmod +x start-local.sh
./start-local.sh
```

---

## 五、常见问题

### Q: npm install 报错？
```bash
# 清除缓存重试
npm cache clean --force
rm -rf node_modules package-lock.json
npm install
```

### Q: 端口被占用？
```bash
# 查找占用端口的进程
lsof -i :5173    # 前端端口
lsof -i :8080    # 后端端口

# 杀掉进程
kill -9 <PID>
```

### Q: MySQL 连接失败？
- 检查 MySQL 是否运行: `brew services list`
- 检查密码是否正确（默认 root/root123456）
- 检查数据库是否已创建: `mysql -u root -p -e "SHOW DATABASES"`

### Q: 只想测试编辑器，不想装 MySQL/Redis？
直接跳到「二、快速开始（仅前端）」即可。编辑器页面不依赖后端。

---

## 六、项目结构

```
magao-platform/
├── frontend/              # 前端 (Vue 3 + Vite + Element Plus)
│   ├── src/
│   │   ├── components/
│   │   │   └── editor/    # 编辑器组件
│   │   │       ├── BaseEditor.vue
│   │   │       ├── EditorOutput.vue
│   │   │       └── child/
│   │   │           ├── ScratchChildEditor.vue      # Scratch 积木编辑器
│   │   │           ├── ScratchJrChildEditor.vue     # ScratchJr 积木编辑器
│   │   │           ├── PythonChildEditor.vue       # Python / Python Turtle
│   │   │           ├── GoCChildEditor.vue           # GoC 绘图
│   │   │           └── CppChildEditor.vue           # C++ 编辑器
│   │   ├── pages/
│   │   ├── router/
│   │   └── stores/
│   ├── vite.config.ts
│   └── package.json
│
├── backend/               # 后端 (Spring Boot 3.2 + Java 17)
│   ├── src/main/java/com/magao/backend/
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── application-dev.yml
│   └── pom.xml
│
├── schema_init.sql         # 数据库初始化脚本
└── start.sh                # 生产环境启动脚本
```

---

## 七、从 TRAE 迁移项目到本地

### 方法1: 直接下载
在 TRAE 工作区面板中，右键项目文件夹 -> 下载到本地

### 方法2: Git 仓库（推荐）
如果项目已在 Git 仓库，在本地 clone 即可：
```bash
git clone <仓库地址> ~/projects/magao-platform
```

### 方法3: 压缩导出
在 TRAE 终端执行（不包含 node_modules）：
```bash
cd /workspace
tar --exclude='node_modules' --exclude='target' --exclude='.git' \
    -czf magao-platform.tar.gz frontend/ backend/ schema_init.sql docs/
```
然后下载 `magao-platform.tar.gz` 到本地解压。
