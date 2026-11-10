# 码高少儿编程教育平台 — 生产部署指南

## 一、环境要求

| 依赖 | 最低版本 | 推荐版本 |
|------|---------|---------|
| Node.js | 18.x | 20.x LTS |
| npm | 9.x | 10.x |
| Nginx | 1.20 | 1.24+ |

## 二、构建步骤

```bash
# 1. 安装依赖
npm install

# 2. 生产构建
npm run build

# 3. 输出目录
# dist/
#   ├── index.html
#   ├── js/           (Vue + Element Plus + 业务代码)
#   ├── css/          (样式文件)
#   ├── images/       (图片资源)
#   ├── fonts/        (字体文件)
#   ├── assets/       (其他静态资源)
#   ├── scratch/      (Scratch 3.0 编辑器)
#   ├── scratch-jr/   (ScratchJr 编辑器)
#   ├── goc/          (GoC 编辑器)
#   ├── pyodide/      (Python WASM 运行时 ~10MB)
#   └── cpp/          (C++ 转译器)
```

## 三、环境变量配置

编辑 `.env.production`：

```bash
# 必须修改：替换为实际域名
VITE_APP_BASE_URL=https://your-domain.com

# 保持不变：API 前缀，由 Nginx 反向代理
VITE_APP_BASE_API=/api
```

## 四、Nginx 配置

```nginx
server {
    listen 443 ssl http2;
    server_name your-domain.com;

    # SSL 证书
    ssl_certificate     /path/to/cert.pem;
    ssl_certificate_key /path/to/key.pem;

    # 静态资源根目录（指向 dist/ 内容）
    root /usr/share/nginx/html;
    index index.html;

    # Gzip 压缩
    gzip on;
    gzip_types text/css application/javascript application/json image/svg+xml;
    gzip_min_length 1024;

    # 前端 SPA（History 模式）
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API 反向代理
    location /api/ {
        proxy_pass http://backend-server:8080/api/;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 静态资源缓存（带 hash 的文件长期缓存）
    location ~* \.(js|css|png|jpg|jpeg|gif|svg|woff2?|eot|ttf)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }

    # index.html 不缓存
    location = /index.html {
        add_header Cache-Control "no-cache, no-store, must-revalidate";
    }

    # Pyodide WASM 文件（较大，需单独缓存策略）
    location /pyodide/ {
        expires 30d;
        add_header Cache-Control "public";
    }
}

# HTTP → HTTPS 重定向
server {
    listen 80;
    server_name your-domain.com;
    return 301 https://$host$request_uri;
}
```

## 五、上线前检查清单

### 安全检查

- [x] Token Cookie 设置 `Secure` + `SameSite=Lax`（生产环境自动启用）
- [x] DEV 角色提升逻辑在生产构建中被 Tree-shaking 移除
- [x] `drop_console: true` 移除所有 `src/` 中的 console 语句
- [x] `drop_debugger: true` 移除所有 debugger 语句
- [x] `sourcemap: false` 不生成 Source Map
- [ ] 后端服务配置 Cookie `httpOnly` 属性（需后端配合）
- [ ] Nginx 配置 HTTPS 强制跳转
- [ ] Nginx 配置 HSTS 头（`Strict-Transport-Security`）
- [ ] Nginx 配置安全响应头（X-Content-Type-Options, X-Frame-Options 等）

### 功能检查

- [ ] 前台所有页面可正常访问
- [ ] 后台管理系统可正常登录和操作
- [ ] 六大编辑器可正常运行
- [ ] API 接口连通性正常
- [ ] 静态资源加载正常（图片、字体、WASM）

### 性能检查

- [ ] 首屏加载时间 < 3s
- [ ] JS Bundle 体积合理（element-plus + vue-vendor 已分包）
- [ ] Gzip 压缩已启用
- [ ] 静态资源缓存策略已配置

## 六、回滚方案

```bash
# 保留上一个版本的 dist 目录
cp -r dist/ dist-backup-$(date +%Y%m%d)/

# 回滚：替换 dist 目录内容
cp -r dist-backup-YYYYMMDD/* /usr/share/nginx/html/

# Nginx 无需重启（静态文件直接生效）
```

## 七、注意事项

1. **Pyodide 体积**：`public/pyodide/` 约 10MB，建议部署到 CDN 或开启长期缓存
2. **Scratch 资产**：Scratch 编辑器开发环境依赖 CDN 代理，生产环境建议预下载资产到本地
3. **localStorage 数据**：后台管理数据存储在浏览器 localStorage，不跨设备同步，上线前需评估是否需要后端持久化
4. **API 后端**：前端通过 `/api` 前缀访问后端，确保后端服务在 `backend-server:8080` 可达
