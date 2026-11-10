# Magao Platform - 生产环境部署指南（方案2：Nginx 独立静态托管）

## 架构概述

本部署方案**完全对标 52coding 线上生产架构**，核心特点：

- **Nginx 直接托管**前端 dist + Scratch 静态文件（无独立 Scratch 容器）
- **前端打包体积无增加**，更新 Scratch 仅替换服务器静态文件夹
- **Nginx 全局缓存**，千人并发加载编辑器速度更快
- **稳定支撑 500~1000 人同时竞赛**

```
┌─────────────────────────────────────────────────────────────┐
│                         用户浏览器                            │
└─────────────────────────────────────────────────────────────┘
                              │
                              ▼
┌─────────────────────────────────────────────────────────────┐
│                      Nginx (端口 80)                         │
│  ┌─────────────┐  ┌─────────────────┐  ┌─────────────────┐ │
│  │ /           │  │ /scratch-gui/   │  │ /scratch-jr/    │ │
│  │ Vue SPA     │  │ 静态文件托管    │  │ 静态文件托管    │ │
│  │ /data/frontend/dist │ /data/static/.. │ /data/static/.. │ │
│  └─────────────┘  └─────────────────┘  └─────────────────┘ │
│  ┌─────────────────────────────────────────────────────────┐│
│  │ /api/ -> backend:8080 (Spring Boot)                    ││
│  │ /ws/  -> backend:8080 (WebSocket)                      ││
│  │ /minio/ -> minio:9000 (对象存储)                       ││
│  └─────────────────────────────────────────────────────────┘│
└─────────────────────────────────────────────────────────────┘
                              │
        ┌─────────────────────┼─────────────────────┐
        ▼                     ▼                     ▼
   ┌─────────┐          ┌─────────┐          ┌──────────┐
   │ Backend │          │  MySQL  │          │  Redis   │
   │ (Java)  │          │ (8.0)   │          │  (7)     │
   └─────────┘          └─────────┘          └──────────┘
        │
   ┌────┴────┐
   ▼         ▼
┌──────┐  ┌────────┐
│Judge0│  │RabbitMQ│
│(3实例)│  │(3.12)  │
└──────┘  └────────┘
```

---

## 目录结构

```
/data/
├── frontend/
│   └── dist/                    # Vue 前端编译产物
├── static/
│   ├── scratch-gui/             # Scratch 3.0 静态文件
│   │   ├── index.html
│   │   ├── static/
│   │   ├── bridge.js            # postMessage 桥接脚本
│   │   └── ...
│   └── scratch-jr/              # Scratch Jr 静态文件
│       ├── index.html
│       └── ...
└── docker/                      # Docker Compose 项目目录
    ├── docker-compose.yml
    ├── nginx/
    │   ├── nginx.conf
    │   └── conf.d/
    │       ├── production.conf   # 主站点配置（本方案核心）
    │       ├── api.conf          # API 代理配置
    │       ├── frontend.conf     # 前端 SPA 配置
    │       ├── minio.conf        # MinIO 代理配置
    │       └── scratch.conf      # Scratch 静态托管配置
    ├── scratch-gui/
    │   ├── deploy-scratch-gui.sh # Scratch GUI 部署脚本
    │   └── bridge.js             # postMessage 桥接脚本
    └── scratch-jr/
        └── deploy-scratch-jr.sh  # Scratch Jr 部署脚本
```

---

## 前置要求

### 服务器配置

| 组件 | 版本 | 说明 |
|------|------|------|
| OS | Ubuntu 22.04 LTS | 推荐 |
| Docker | 24.0+ | 容器运行时 |
| Docker Compose | 2.20+ | 编排工具 |
| Nginx | 1.24+ | Web 服务器 |
| Node.js | 18+ | Scratch 编译需要 |
| Git | 2.40+ | 源码克隆 |
| 内存 | 32GB+ | 生产环境 |
| CPU | 16核+ | 生产环境 |
| 磁盘 | 200GB+ SSD | 生产环境 |

### 域名配置

```
code.52coding.net     -> 服务器公网 IP
```

---

## 部署步骤

### 第一步：准备服务器目录

```bash
# 创建静态资源目录
sudo mkdir -p /data/frontend/dist
sudo mkdir -p /data/static/scratch-gui
sudo mkdir -p /data/static/scratch-jr

# 设置权限
sudo chown -R $USER:$USER /data
sudo chmod -R 755 /data
```

### 第二步：部署前端 dist

```bash
# 在本地/CI 环境编译前端
cd /path/to/magao-platform/frontend
npm install
npm run build

# 将 dist 上传到服务器
rsync -avz --delete dist/ root@your-server:/data/frontend/dist/
```

### 第三步：部署 Scratch GUI 3.0

```bash
cd /path/to/magao-platform/docker/scratch-gui
chmod +x deploy-scratch-gui.sh
./deploy-scratch-gui.sh
```

脚本会自动：
1. 克隆 scratch-gui 官方仓库
2. 安装依赖并编译
3. 注入 postMessage 桥接脚本
4. 部署到 `/data/static/scratch-gui/`
5. 重载 Nginx

### 第四步：部署 Scratch Jr

```bash
cd /path/to/magao-platform/docker/scratch-jr
chmod +x deploy-scratch-jr.sh
./deploy-scratch-jr.sh
```

### 第五步：部署 Nginx 配置

```bash
# 复制 Nginx 配置到服务器
sudo mkdir -p /etc/nginx/conf.d
sudo cp /path/to/magao-platform/docker/nginx/nginx.conf /etc/nginx/nginx.conf
sudo cp /path/to/magao-platform/docker/nginx/conf.d/production.conf /etc/nginx/conf.d/

# 测试配置
sudo nginx -t

# 重载 Nginx
sudo nginx -s reload
```

### 第六步：启动 Docker 服务

```bash
cd /path/to/magao-platform/docker

# 复制环境变量模板
cp .env.example .env
# 编辑 .env 填写实际配置

# 启动所有服务（除 Nginx 外）
docker-compose -f docker-compose.yml up -d mysql redis rabbitmq minio judge0-1 judge0-2 judge0-3 backend

# 查看日志
docker-compose -f docker-compose.yml logs -f
```

### 第七步：验证部署

```bash
# 检查 Nginx 配置
curl -I http://localhost/
curl -I http://localhost/scratch-gui/
curl -I http://localhost/scratch-jr/
curl -I http://localhost/api/actuator/health

# 检查静态文件
curl http://localhost/scratch-gui/index.html | head -20
```

---

## iframe 地址说明

前端代码中 iframe 的 src 地址：

```javascript
// Scratch 3.0
const scratchGuiSrc = '/scratch-gui/index.html?mode=contest&contestId=1001';

// Scratch Jr
const scratchJrSrc = '/scratch-jr/index.html?mode=contest&contestId=1001';
```

Nginx 会将这些路径映射到对应的静态文件目录：
- `/scratch-gui/` -> `/data/static/scratch-gui/`
- `/scratch-jr/` -> `/data/static/scratch-jr/`

---

## 更新操作

### 更新前端

```bash
# 重新编译并上传 dist
cd frontend
npm run build
rsync -avz --delete dist/ root@your-server:/data/frontend/dist/
```

### 更新 Scratch GUI

```bash
# 重新运行部署脚本即可
cd docker/scratch-gui
./deploy-scratch-gui.sh
```

**无需重新构建前端项目！**

### 更新 Scratch Jr

```bash
cd docker/scratch-jr
./deploy-scratch-jr.sh
```

---

## 性能优化

### Nginx 缓存策略

| 资源类型 | 缓存时间 | Cache-Control |
|---------|---------|---------------|
| JS/CSS (带 hash) | 1年 | public, immutable |
| 图片/字体 | 30天 | public |
| 音频文件 | 30天 | public |
| HTML 文件 | 不缓存 | no-cache |

### 并发支持

- Nginx: worker_processes auto, worker_connections 2048
- Backend: Tomcat 800 线程, 10000 最大连接
- MySQL: max-connections=2000
- Redis: maxmemory 4gb

---

## 故障排查

### Scratch 编辑器无法加载

```bash
# 检查静态文件是否存在
ls -la /data/static/scratch-gui/

# 检查 Nginx 配置
curl -v http://localhost/scratch-gui/index.html

# 查看 Nginx 错误日志
sudo tail -f /var/log/nginx/error.log
```

### 前端 404

```bash
# 检查 dist 目录
ls -la /data/frontend/dist/

# 检查 SPA 路由配置
curl http://localhost/non-existent-path
# 应该返回 index.html
```

### API 无法访问

```bash
# 检查后端服务
curl http://localhost/api/actuator/health

# 检查 Docker 容器
docker ps | grep backend
docker logs magao-backend
```

---

## 安全建议

1. **启用 HTTPS**: 使用 Let's Encrypt 或商业证书
2. **防火墙**: 仅开放 80/443 端口
3. **定期备份**: `/data/static/` 和数据库
4. **监控**: 配置 Prometheus + Grafana 监控
5. **日志**: 定期清理 Nginx 日志

---

## 与 52coding 架构对比

| 特性 | 本方案 | 52coding |
|------|--------|----------|
| Nginx 托管静态文件 | 是 | 是 |
| 前端 dist 独立 | 是 | 是 |
| Scratch 独立静态目录 | 是 | 是 |
| API 反向代理 | 是 | 是 |
| 静态资源缓存 | 7d/30d/1y | 类似 |
| 支持千人并发 | 是 | 是 |
| iframe 嵌入 | 是 | 是 |
| postMessage 通信 | 是 | 是 |

---

## 联系方式

如有部署问题，请联系开发团队。
