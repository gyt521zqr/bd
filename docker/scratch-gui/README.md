# Scratch GUI 部署方案（方案2：Nginx 独立静态托管）

## 架构概述

对标 52coding 线上生产环境架构：

```
用户浏览器
    │
    ▼
┌─────────────────────────────────────────────────────────────┐
│                         Nginx (80/443)                       │
│  ┌─────────────┐  ┌─────────────────┐  ┌─────────────────┐  │
│  │  /          │  │  /scratch-gui/  │  │  /scratch-jr/   │  │
│  │  Vue SPA    │  │  静态资源托管   │  │  静态资源托管   │  │
│  │  /data/     │  │  /data/static/  │  │  /data/static/  │  │
│  │  frontend/  │  │  scratch-gui/   │  │  scratch-jr/    │  │
│  │  dist/      │  │                 │  │                 │  │
│  └─────────────┘  └─────────────────┘  └─────────────────┘  │
│  ┌─────────────┐  ┌─────────────────┐                        │
│  │  /api/      │  │  /ws/ /sse/     │                        │
│  │  反向代理   │  │  WebSocket      │                        │
│  │  backend    │  │  代理           │                        │
│  └─────────────┘  └─────────────────┘                        │
└─────────────────────────────────────────────────────────────┘
```

## 核心优势

1. **前端打包体积无增加** - scratch-gui 不打包进 dist，独立部署
2. **更新便捷** - 更新 scratch 编辑器只需替换静态文件夹，无需重新构建前端
3. **Nginx 全局缓存** - 千人并发加载速度更快
4. **完全对标 52coding** - 稳定支撑 500~1000 人同时竞赛

## 目录结构

```
/data/
├── frontend/
│   └── dist/              # Vue 前端编译产物
│       ├── index.html
│       ├── assets/
│       └── static/
└── static/
    ├── scratch-gui/       # Scratch 3.0 编辑器静态资源
    │   ├── index.html     # 入口（已注入 bridge.js）
    │   ├── bridge.js      # postMessage 桥接脚本
    │   ├── static/
    │   │   ├── css/
    │   │   ├── js/
    │   │   └── media/
    │   └── ...
    └── scratch-jr/        # Scratch Jr 编辑器静态资源
        ├── index.html
        └── ...
```

## 部署步骤

### 1. 服务器准备

确保服务器已安装：
- Node.js 18+
- Git
- Nginx
- Docker & Docker Compose（如使用容器化部署）

### 2. 编译并部署 Scratch GUI

```bash
# 进入项目目录
cd /path/to/magao-platform

# 运行部署脚本（自动完成：克隆→编译→注入桥接→部署→重载Nginx）
bash docker/scratch-gui/deploy-scratch-gui.sh
```

脚本会自动：
1. 克隆 scratchfoundation/scratch-gui 仓库
2. 安装依赖并编译生产版本
3. 注入 postMessage 桥接脚本（bridge.js）
4. 复制到 `/data/static/scratch-gui/`
5. 重载 Nginx

### 3. 编译并部署 Scratch Jr（可选）

```bash
# 运行 Scratch Jr 部署脚本
bash docker/scratch-jr/deploy-scratch-jr.sh
```

### 4. 部署前端 Vue 项目

```bash
cd frontend

# 安装依赖
npm install

# 生产构建
npm run build

# 复制到 Nginx 目录
sudo mkdir -p /data/frontend/dist
sudo cp -r dist/* /data/frontend/dist/
```

### 5. 启动服务

**方式 A：Docker Compose（推荐生产环境）**

```bash
cd docker

# 复制环境变量模板
cp .env.example .env
# 编辑 .env 填写实际配置

# 启动所有服务
docker-compose -f docker-compose.yml up -d
```

**方式 B：独立 Nginx（已有 Nginx 环境）**

```bash
# 复制 Nginx 配置到服务器
sudo cp docker/nginx/nginx.conf /etc/nginx/nginx.conf
sudo cp docker/nginx/conf.d/*.conf /etc/nginx/conf.d/

# 测试配置
sudo nginx -t

# 重载 Nginx
sudo nginx -s reload
```

## 配置验证

部署完成后，验证以下地址：

```bash
# 1. 主站首页
curl -I http://your-domain/

# 2. Scratch GUI 编辑器
curl -I http://your-domain/scratch-gui/index.html

# 3. Scratch GUI 静态资源（JS/CSS缓存头）
curl -I http://your-domain/scratch-gui/static/js/main.js

# 4. 后端 API
curl http://your-domain/api/health

# 5. WebSocket
curl -I http://your-domain/ws/
```

## 更新 Scratch 编辑器

```bash
# 只需重新运行部署脚本，无需重新构建前端
cd /path/to/magao-platform
bash docker/scratch-gui/deploy-scratch-gui.sh
```

## 本地开发环境

开发时无需部署真实 scratch-gui，使用 mock 版本：

```bash
# mock 文件已包含在 frontend/public/scratch-gui/index.html
# Vite dev server 会自动提供

# 启动开发服务器
cd frontend
npm run dev

# 访问 http://localhost:3000/practice
# Scratch 编辑器会加载 mock 版本（功能演示用）
```

如需在开发环境测试真实 scratch-gui：

```bash
# 方法1：将编译产物复制到 public 目录
cp -r /data/static/scratch-gui/* frontend/public/scratch-gui/

# 方法2：使用 Vite 代理到已部署的服务器
# 在 vite.config.ts 中添加：
# '/scratch-gui': { target: 'http://your-domain', changeOrigin: true }
```

## 故障排查

### iframe 显示 "Scratch 编辑器资源未就绪"

- 检查 `/data/static/scratch-gui/index.html` 是否存在
- 检查 Nginx 配置中 `location /scratch-gui/` 是否正确
- 检查 bridge.js 是否已注入到 index.html

### 跨域错误

- 确认父页面和 iframe 同域（或 Nginx 中正确配置 CORS）
- 检查 `X-Frame-Options` 是否为 `SAMEORIGIN`

### 素材无法加载

- 检查 `static/` 目录是否正确复制到 `/data/static/scratch-gui/`
- 检查 Nginx 静态资源缓存配置

### 竞赛模式积木限制不生效

- 检查 bridge.js 是否正确加载
- 检查浏览器控制台是否有 `window.vm` 相关错误
