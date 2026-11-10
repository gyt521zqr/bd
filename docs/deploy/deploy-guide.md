# Magao Platform - 部署指南

> 本文档详细说明 Magao Platform 在生产环境中的部署流程，包括服务器要求、Docker 安装、域名 HTTPS 配置、存储扩容、数据库备份等内容。

---

## 目录

1. [服务器要求](#1-服务器要求)
2. [Docker 安装](#2-docker-安装)
3. [项目部署](#3-项目部署)
4. [域名与 HTTPS 配置](#4-域名与-https-配置)
5. [MinIO 存储扩容方案](#5-minio-存储扩容方案)
6. [Judge0 沙箱扩容方案](#6-judge0-沙箱扩容方案)
7. [数据库备份配置](#7-数据库备份配置)
8. [日志管理](#8-日志管理)
9. [常见问题 FAQ](#9-常见问题-faq)

---

## 1. 服务器要求

### 1.1 最低配置

| 项目 | 要求 |
|------|------|
| CPU | 4 核 |
| 内存 | 16 GB |
| 系统盘 | 50 GB SSD |
| 数据盘 | 100 GB SSD（推荐独立挂载） |
| 操作系统 | Ubuntu 22.04 LTS / CentOS 8+ / Debian 11+ |
| 网络 | 公网 IP，带宽 10Mbps+ |

### 1.2 推荐配置（支持 1000 并发）

| 项目 | 要求 |
|------|------|
| CPU | 8 核+ |
| 内存 | 32 GB |
| 系统盘 | 100 GB SSD |
| 数据盘 | 500 GB SSD（独立挂载） |
| 操作系统 | Ubuntu 22.04 LTS |
| 网络 | 公网 IP，带宽 50Mbps+ |

### 1.3 端口开放清单

| 端口 | 服务 | 说明 |
|------|------|------|
| 80 | Nginx | HTTP（重定向到 HTTPS） |
| 443 | Nginx | HTTPS |
| 3306 | MySQL | 仅内网访问，不对外开放 |
| 6379 | Redis | 仅内网访问，不对外开放 |
| 5672 | RabbitMQ | 仅内网访问，不对外开放 |
| 15672 | RabbitMQ | 管理控制台，可选对外开放 |
| 9000 | MinIO API | 通过 Nginx 代理访问 |
| 9001 | MinIO Console | 通过 Nginx 代理访问 |
| 2358 | Judge0 | 仅内网访问，不对外开放 |
| 8080 | Backend | 通过 Nginx 代理访问 |

### 1.4 系统初始化

```bash
# 更新系统
sudo apt update && sudo apt upgrade -y

# 设置时区
sudo timedatectl set-timezone Asia/Shanghai

# 安装基础工具
sudo apt install -y curl wget git vim htop net-tools

# 配置 swap（如果内存不足）
sudo fallocate -l 4G /swapfile
sudo chmod 600 /swapfile
sudo mkswap /swapfile
sudo swapon /swapfile
echo '/swapfile none swap sw 0 0' | sudo tee -a /etc/fstab

# 调整文件描述符限制
echo '* soft nofile 65535' | sudo tee -a /etc/security/limits.conf
echo '* hard nofile 65535' | sudo tee -a /etc/security/limits.conf
echo 'fs.file-max = 2097152' | sudo tee -a /etc/sysctl.conf
sudo sysctl -p
```

---

## 2. Docker 安装

### 2.1 安装 Docker Engine

```bash
# 卸载旧版本（如有）
sudo apt remove docker docker-engine docker.io containerd runc

# 安装依赖
sudo apt install -y ca-certificates curl gnupg lsb-release

# 添加 Docker 官方 GPG 密钥
sudo mkdir -p /etc/apt/keyrings
curl -fsSL https://download.docker.com/linux/ubuntu/gpg | sudo gpg --dearmor -o /etc/apt/keyrings/docker.gpg

# 添加 Docker 仓库
echo \
  "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.gpg] https://download.docker.com/linux/ubuntu \
  $(lsb_release -cs) stable" | sudo tee /etc/apt/sources.list.d/docker.list > /dev/null

# 安装 Docker Engine
sudo apt update
sudo apt install -y docker-ce docker-ce-cli containerd.io docker-buildx-plugin docker-compose-plugin

# 将当前用户加入 docker 组（免 sudo）
sudo usermod -aG docker $USER
newgrp docker

# 验证安装
docker --version
docker compose version
```

### 2.2 配置 Docker

```bash
# 创建 Docker 配置目录
sudo mkdir -p /etc/docker

# 配置 Docker daemon
sudo tee /etc/docker/daemon.json <<EOF
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m",
    "max-file": "3"
  },
  "storage-driver": "overlay2",
  "live-restore": true,
  "max-concurrent-downloads": 10,
  "max-concurrent-uploads": 5,
  "default-ulimits": {
    "nofile": {
      "Name": "nofile",
      "Hard": 65535,
      "Soft": 65535
    }
  }
}
EOF

# 重启 Docker
sudo systemctl daemon-reload
sudo systemctl restart docker
sudo systemctl enable docker
```

### 2.3 配置数据盘挂载（推荐）

如果服务器有独立数据盘，建议将 Docker 数据目录挂载到数据盘：

```bash
# 假设数据盘为 /dev/vdb
sudo fdisk /dev/vdb  # 创建分区
sudo mkfs.ext4 /dev/vdb1
sudo mkdir -p /data
sudo mount /dev/vdb1 /data

# 自动挂载
echo '/dev/vdb1 /data ext4 defaults 0 2' | sudo tee -a /etc/fstab

# 迁移 Docker 数据目录
sudo systemctl stop docker
sudo rsync -aP /var/lib/docker/ /data/docker/
sudo rm -rf /var/lib/docker

# 修改 Docker 配置
sudo sed -i 's|"data-root":.*|"data-root": "/data/docker",|' /etc/docker/daemon.json

sudo systemctl start docker
```

---

## 3. 项目部署

### 3.1 获取代码

```bash
# 克隆项目
cd /data
git clone <repository-url> magao-platform
cd magao-platform
```

### 3.2 配置环境变量

```bash
# 复制环境变量模板
cp .env.example .env

# 编辑配置（务必修改所有密码和密钥）
vim .env
```

**重要提示**：
- 所有密码必须使用强密码（至少 16 位，包含大小写字母、数字和特殊字符）
- JWT_SECRET 必须使用随机字符串（至少 64 位）
- 生产环境不要使用示例中的默认密码

### 3.3 生成强密码

```bash
# 生成随机密码（Linux）
openssl rand -hex 16   # 32 位随机字符串
openssl rand -hex 32   # 64 位随机字符串（用于 JWT_SECRET）
```

### 3.4 构建前端产物

```bash
# 方式一：使用 Docker 构建
docker run --rm \
  -v magao-frontend-dist:/dist \
  -v $(pwd)/frontend:/app \
  -w /app \
  node:18-alpine \
  sh -c "npm ci --registry=https://registry.npmmirror.com && npm run build && cp -r dist/* /dist/"

# 方式二：本地构建（需要 Node.js 18）
cd frontend
npm ci
npm run build
# 将 dist 目录内容复制到 Docker volume
docker volume create magao-frontend-dist
docker run --rm -v magao-frontend-dist:/dist -v $(pwd)/dist:/src alpine cp -r /src/* /dist/
```

### 3.5 启动服务

```bash
cd /data/magao-platform

# 构建并启动所有服务（后台运行）
docker compose -f docker/docker-compose.yml up -d --build

# 查看服务状态
docker compose -f docker/docker-compose.yml ps

# 查看启动日志
docker compose -f docker/docker-compose.yml logs -f
```

### 3.6 验证部署

```bash
# 检查各服务健康状态
docker compose -f docker/docker-compose.yml ps --format "table {{.Name}}\t{{.Status}}\t{{.Ports}}"

# 检查后端健康
curl http://localhost:8080/actuator/health

# 检查前端
curl -I http://localhost

# 检查 MinIO
curl http://localhost:9000/minio/health/live
```

### 3.7 服务管理命令

```bash
# 停止所有服务
docker compose -f docker/docker-compose.yml down

# 停止并清除数据卷（危险操作！）
docker compose -f docker/docker-compose.yml down -v

# 重启单个服务
docker compose -f docker/docker-compose.yml restart backend

# 查看单个服务日志
docker compose -f docker/docker-compose.yml logs -f --tail=100 backend

# 重新构建并启动
docker compose -f docker/docker-compose.yml up -d --build backend
```

---

## 4. 域名与 HTTPS 配置

### 4.1 域名解析

在域名服务商处添加以下 DNS 解析记录：

| 类型 | 主机记录 | 记录值 | 说明 |
|------|---------|--------|------|
| A | @ | 服务器公网 IP | 主域名 |
| A | api | 服务器公网 IP | API 子域名（可选） |
| A | minio | 服务器公网 IP | MinIO 子域名（可选） |

### 4.2 安装 SSL 证书（Let's Encrypt）

```bash
# 安装 certbot
sudo apt install -y certbot python3-certbot-nginx

# 申请证书（替换 your-domain.com）
sudo certbot certonly --standalone -d your-domain.com -d www.your-domain.com

# 证书文件位置
# /etc/letsencrypt/live/your-domain.com/fullchain.pem
# /etc/letsencrypt/live/your-domain.com/privkey.pem
```

### 4.3 配置 Nginx HTTPS

创建 HTTPS 配置文件：

```bash
# 创建 SSL 配置
vim docker/nginx/conf.d/ssl.conf
```

写入以下内容：

```nginx
server {
    listen 80;
    server_name your-domain.com www.your-domain.com;

    # HTTP 重定向到 HTTPS
    return 301 https://$server_name$request_uri;
}

server {
    listen 443 ssl http2;
    server_name your-domain.com www.your-domain.com;

    # SSL 证书配置
    ssl_certificate /etc/nginx/certs/fullchain.pem;
    ssl_certificate_key /etc/nginx/certs/privkey.pem;

    # SSL 安全参数
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers ECDHE-ECDSA-AES128-GCM-SHA256:ECDHE-RSA-AES128-GCM-SHA256:ECDHE-ECDSA-AES256-GCM-SHA384:ECDHE-RSA-AES256-GCM-SHA384;
    ssl_prefer_server_ciphers off;
    ssl_session_cache shared:SSL:10m;
    ssl_session_timeout 1d;
    ssl_session_tickets off;

    # HSTS
    add_header Strict-Transport-Security "max-age=63072000; includeSubDomains; preload" always;

    # 前端 SPA
    location / {
        root /usr/share/nginx/html;
        try_files $uri $uri/ /index.html;
    }

    # API 代理
    location /api/ {
        proxy_pass http://backend:8080/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_http_version 1.1;
        proxy_set_header Connection "";
    }

    # WebSocket 代理
    location /ws/ {
        proxy_pass http://backend:8080/ws/;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_read_timeout 3600s;
        proxy_buffering off;
    }

    # MinIO 代理
    location /minio/ {
        rewrite ^/minio/(.*)$ /$1 break;
        proxy_pass http://minio_api;
        proxy_set_header Host $http_host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        client_max_body_size 5G;
        proxy_buffering off;
    }

    # MinIO 控制台
    location /minio-console/ {
        rewrite ^/minio-console/(.*)$ /$1 break;
        proxy_pass http://minio_console;
        proxy_set_header Host $http_host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
    }
}
```

### 4.4 挂载证书到 Docker

```bash
# 复制证书到项目目录
mkdir -p docker/nginx/certs
sudo cp /etc/letsencrypt/live/your-domain.com/fullchain.pem docker/nginx/certs/
sudo cp /etc/letsencrypt/live/your-domain.com/privkey.pem docker/nginx/certs/

# 修改 docker-compose.yml 中 nginx 服务的证书挂载
# 确保 nginx_certs volume 指向正确的证书目录
```

### 4.5 证书自动续期

```bash
# 添加 certbot 自动续期定时任务
sudo crontab -e

# 添加以下行（每天凌晨 2 点检查续期）
0 2 * * * certbot renew --quiet --deploy-hook "cp /etc/letsencrypt/live/your-domain.com/*.pem /data/magao-platform/docker/nginx/certs/ && docker compose -f /data/magao-platform/docker/docker-compose.yml exec nginx nginx -s reload"
```

---

## 5. MinIO 存储扩容方案

### 5.1 单机扩容（增加存储空间）

```bash
# 方法一：扩展数据盘
# 1. 在云服务商控制台扩容数据盘
# 2. 在服务器上扩展文件系统
sudo growpart /dev/vdb 1
sudo resize2fs /dev/vdb1

# 方法二：更换存储目录
# 修改 docker-compose.yml 中 MinIO 的 volume 挂载路径
# 将 minio_data 挂载到更大的磁盘分区
```

### 5.2 分布式扩容（多节点）

当单机存储不足以满足需求时，可采用 MinIO 分布式部署：

```yaml
# docker-compose.minio-cluster.yml
version: "3.8"

services:
  minio1:
    image: minio/minio:latest
    command: server http://minio{1...4}/data{1...2} --console-address ":9001"
    volumes:
      - /data/minio/data1-1:/data1
      - /data/minio/data1-2:/data2
    environment:
      MINIO_ROOT_USER: ${MINIO_ROOT_USER}
      MINIO_ROOT_PASSWORD: ${MINIO_ROOT_PASSWORD}
    networks:
      - minio-cluster

  minio2:
    image: minio/minio:latest
    command: server http://minio{1...4}/data{1...2} --console-address ":9001"
    volumes:
      - /data/minio/data2-1:/data1
      - /data/minio/data2-2:/data2
    environment:
      MINIO_ROOT_USER: ${MINIO_ROOT_USER}
      MINIO_ROOT_PASSWORD: ${MINIO_ROOT_PASSWORD}
    networks:
      - minio-cluster

  # minio3, minio4 类似配置...

networks:
  minio-cluster:
    driver: overlay
```

### 5.3 存储容量规划

| 用户规模 | 预估存储需求 | 推荐配置 |
|---------|------------|---------|
| 1000 用户 | 50 GB | 单机 100GB SSD |
| 5000 用户 | 200 GB | 单机 500GB SSD |
| 10000+ 用户 | 500 GB+ | 分布式 4 节点 |

### 5.4 MinIO Bucket 策略配置

```bash
# 安装 MinIO Client
wget https://dl.min.io/client/mc/release/linux-amd64/mc -O /usr/local/bin/mc
chmod +x /usr/local/bin/mc

# 配置 MinIO 连接
mc alias set magao http://localhost:9000 ${MINIO_ROOT_USER} ${MINIO_ROOT_PASSWORD}

# 创建 Bucket
mc mb magao/course-videos
mc mb magao/user-avatars
mc mb magao/exam-files
mc mb magao/practice-submissions

# 设置公开读取策略（用户头像等）
mc anonymous set download magao/user-avatars

# 设置私有策略（课程视频等）
mc anonymous set none magao/course-videos
```

---

## 6. Judge0 沙箱扩容方案

### 6.1 单实例调优

通过调整 docker-compose.yml 中 Judge0 的环境变量来提升单实例性能：

```yaml
environment:
  ENABLE_NETWORK: "false"
  CPU_TIME_LIMIT: 5
  MEMORY_LIMIT: 262144          # 256MB
  EXTRA_POOL_SIZE: 50           # 增加额外进程池（默认 30）
  RUNNER_MAX_PROCESSES: 100     # 最大评测进程数
  RUNNER_MAX_QUEUE_SIZE: 200    # 最大队列长度
```

### 6.2 多实例水平扩容

通过 Nginx 负载均衡多个 Judge0 实例：

```yaml
# docker-compose.yml 中添加多个 Judge0 实例
services:
  judge0-1:
    image: judge0/judge0-ce:latest
    privileged: true
    environment:
      ENABLE_NETWORK: "false"
      CPU_TIME_LIMIT: 5
      MEMORY_LIMIT: 262144
      EXTRA_POOL_SIZE: 30
    volumes:
      - judge0_data_1:/var/judge0
      - /var/run/docker.sock:/var/run/docker.sock
    networks:
      - magao-network

  judge0-2:
    image: judge0/judge0-ce:latest
    privileged: true
    environment:
      ENABLE_NETWORK: "false"
      CPU_TIME_LIMIT: 5
      MEMORY_LIMIT: 262144
      EXTRA_POOL_SIZE: 30
    volumes:
      - judge0_data_2:/var/judge0
      - /var/run/docker.sock:/var/run/docker.sock
    networks:
      - magao-network
```

在 Nginx 配置中添加 Judge0 负载均衡：

```nginx
upstream judge0_cluster {
    least_conn;  # 最少连接优先
    server judge0-1:2358 max_fails=3 fail_timeout=30s;
    server judge0-2:2358 max_fails=3 fail_timeout=30s;
}
```

### 6.3 Judge0 资源规划

| 并发评测需求 | 实例数 | CPU | 内存 |
|------------|--------|-----|------|
| 10 并发 | 1 | 2 核 | 4 GB |
| 30 并发 | 2 | 4 核 | 8 GB |
| 50+ 并发 | 3+ | 6 核+ | 12 GB+ |

---

## 7. 数据库备份配置

### 7.1 自动备份脚本

创建备份脚本 `/data/scripts/backup.sh`：

```bash
#!/bin/bash
# Magao Platform 数据库自动备份脚本

# 配置
BACKUP_DIR="/data/backups/mysql"
MYSQL_CONTAINER="magao-mysql"
MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD}"
MYSQL_DATABASE="magao"
RETENTION_DAYS=30

# 创建备份目录
mkdir -p ${BACKUP_DIR}

# 生成备份文件名（含日期时间）
BACKUP_FILE="${BACKUP_DIR}/magao_$(date +%Y%m%d_%H%M%S).sql.gz"

# 执行备份
docker exec ${MYSQL_CONTAINER} mysqldump \
  -u root \
  -p${MYSQL_ROOT_PASSWORD} \
  --single-transaction \
  --routines \
  --triggers \
  --events \
  ${MYSQL_DATABASE} | gzip > ${BACKUP_FILE}

# 检查备份结果
if [ $? -eq 0 ]; then
    echo "[$(date)] Backup successful: ${BACKUP_FILE}"
    echo "[$(date)] File size: $(du -h ${BACKUP_FILE} | cut -f1)"
else
    echo "[$(date)] Backup FAILED!"
    exit 1
fi

# 清理过期备份
find ${BACKUP_DIR} -name "magao_*.sql.gz" -mtime +${RETENTION_DAYS} -delete
echo "[$(date)] Cleaned up backups older than ${RETENTION_DAYS} days"
```

### 7.2 配置定时备份

```bash
# 赋予执行权限
chmod +x /data/scripts/backup.sh

# 添加 crontab 定时任务
crontab -e

# 每天凌晨 3 点执行全量备份
0 3 * * * /data/scripts/backup.sh >> /data/backups/backup.log 2>&1

# 每周日凌晨 4 点执行备份并上传到远程存储（可选）
0 4 * * 0 /data/scripts/backup.sh && /data/scripts/upload-backup.sh >> /data/backups/backup.log 2>&1
```

### 7.3 备份恢复

```bash
# 恢复最新备份
LATEST_BACKUP=$(ls -t /data/backups/mysql/magao_*.sql.gz | head -1)
gunzip < ${LATEST_BACKUP} | docker exec -i magao-mysql mysql -u root -p${MYSQL_ROOT_PASSWORD} magao

# 恢复指定备份
gunzip < /data/backups/mysql/magao_20240101_030000.sql.gz | docker exec -i magao-mysql mysql -u root -p${MYSQL_ROOT_PASSWORD} magao
```

### 7.4 远程备份（可选）

```bash
# 使用 rclone 同步备份到云存储（阿里云 OSS / 腾讯云 COS / AWS S3）
# 安装 rclone
curl https://rclone.org/install.sh | sudo bash

# 配置远程存储
rclone config

# 同步备份
rclone sync /data/backups/mysql remote:magao-backups/mysql
```

---

## 8. 日志管理

### 8.1 Docker 日志配置

已在 daemon.json 中配置了日志轮转：

```json
{
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m",
    "max-file": "3"
  }
}
```

### 8.2 应用日志查看

```bash
# 查看后端日志
docker compose -f docker/docker-compose.yml logs -f --tail=500 backend

# 查看 Nginx 访问日志
docker exec magao-nginx tail -f /var/log/nginx/access.log

# 查看 Nginx 错误日志
docker exec magao-nginx tail -f /var/log/nginx/error.log

# 查看 MySQL 慢查询日志
docker exec magao-mysql cat /var/lib/mysql/mysql-slow.log
```

### 8.3 日志清理

```bash
# 清理 Docker 系统日志
docker system prune -f --volumes

# 清理超过 7 天的日志
find /var/lib/docker/containers/ -name "*-json.log" -mtime +7 -delete
```

---

## 9. 常见问题 FAQ

### Q1: Docker Compose 启动失败，提示端口被占用

```bash
# 查看端口占用
sudo lsof -i :3306
sudo lsof -i :6379
sudo lsof -i :80

# 停止占用端口的服务
sudo systemctl stop mysql    # 如果系统自带 MySQL
sudo systemctl stop nginx    # 如果系统自带 Nginx
```

### Q2: MySQL 容器启动失败，数据目录权限问题

```bash
# 检查数据卷权限
docker volume inspect magao-mysql-data

# 重建数据卷
docker compose -f docker/docker-compose.yml down -v
docker volume rm magao-mysql-data
docker compose -f docker/docker-compose.yml up -d
```

### Q3: Backend 启动后无法连接 MySQL

```bash
# 检查 MySQL 是否健康
docker compose -f docker/docker-compose.yml ps mysql

# 查看 MySQL 日志
docker compose -f docker/docker-compose.yml logs mysql

# 手动测试连接
docker exec magao-mysql mysqladmin -u root -p ping
```

### Q4: Judge0 评测超时或失败

```bash
# 检查 Judge0 日志
docker compose -f docker/docker-compose.yml logs judge0

# 检查 Docker Socket 挂载
docker exec magao-judge0 ls -la /var/run/docker.sock

# 增加 CPU_TIME_LIMIT 和 MEMORY_LIMIT
```

### Q5: MinIO 上传文件失败

```bash
# 检查 MinIO 状态
docker compose -f docker/docker-compose.yml ps minio

# 检查存储空间
docker exec magao-minio df -h /data

# 检查 Bucket 是否存在
mc ls magao/
```

### Q6: Nginx 502 Bad Gateway

```bash
# 检查后端是否运行
docker compose -f docker/docker-compose.yml ps backend

# 检查后端健康
curl http://localhost:8080/actuator/health

# 检查 Nginx 配置语法
docker exec magao-nginx nginx -t
```

### Q7: 内存不足导致 OOM

```bash
# 查看容器资源使用
docker stats

# 调整 docker-compose.yml 中的资源限制
# 减少 JVM 堆内存：JAVA_OPTS="-Xms1g -Xmx2g"
# 减少 MySQL buffer pool：--innodb-buffer-pool-size=2G
```

### Q8: 如何更新部署

```bash
cd /data/magao-platform

# 拉取最新代码
git pull origin main

# 重新构建并启动
docker compose -f docker/docker-compose.yml up -d --build

# 查看更新状态
docker compose -f docker/docker-compose.yml ps
docker compose -f docker/docker-compose.yml logs -f --tail=50
```

### Q9: 如何回滚部署

```bash
# 回滚到上一个版本
git checkout <previous-commit-hash>

# 重新构建
docker compose -f docker/docker-compose.yml up -d --build

# 如果数据库有变更，需要执行回滚 SQL
docker exec -i magao-mysql mysql -u root -p magao < scripts/rollback.sql
```

### Q10: 前端页面空白或 404

```bash
# 检查前端文件是否正确挂载
docker exec magao-nginx ls -la /usr/share/nginx/html/

# 如果为空，重新构建前端
docker run --rm \
  -v magao-frontend-dist:/dist \
  -v $(pwd)/frontend:/app \
  -w /app \
  node:18-alpine \
  sh -c "npm ci && npm run build && cp -r dist/* /dist/"

# 重启 Nginx
docker compose -f docker/docker-compose.yml restart nginx
```
