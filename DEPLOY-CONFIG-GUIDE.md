# 码高平台 - 火山引擎部署配置指南

> 本文档指导你完成 TOS 和 VOD 开通后的配置工作。

---

## 第一步：获取火山引擎密钥

### 1.1 获取 AccessKey 和 SecretKey

1. 登录 [火山引擎控制台](https://console.volcengine.com/)
2. 右上角头像 → **账号管理** → **密钥管理**
3. 点击 **创建密钥**
4. 复制 **AccessKey ID** 和 **Secret Access Key**

**注意**：SecretKey 只在创建时显示一次，请妥善保存。TOS 和 VOD 共用同一套密钥。

---

## 第二步：TOS 对象存储配置

### 2.1 创建存储桶（Bucket）

1. 进入 [TOS 控制台](https://console.volcengine.com/tos/)
2. 选择 **存储桶列表** → **创建存储桶**
3. 填写信息：
   - **存储桶名称**：`magao`（或你的自定义名称，建议全小写）
   - **区域**：选择离你服务器最近的区域，如 **华北（北京）**（`cn-beijing`）
   - **访问权限**：**私有**（推荐，通过预签名 URL 访问）
   - **存储类型**：**标准存储**
4. 点击 **确认创建**

### 2.2 获取 Endpoint

存储桶创建后，在存储桶详情页查看 **外网访问域名**，格式如：

```
https://tos-cn-beijing.volces.com
```

### 2.3 配置 CORS（跨域访问）

1. 在存储桶详情页，点击 **权限管理** → **跨域访问设置**
2. 点击 **添加规则**：
   - **来源 Origin**：`https://www.你的域名.com`
   - **请求方法**：勾选 `GET`、`PUT`、`POST`、`DELETE`
   - **允许头部**：`*`
   - **暴露头部**：`ETag`
   - **缓存时间**：`3600`
3. 点击 **保存**

### 2.4 TOS 配置汇总

| 配置项 | 值 | 说明 |
|--------|-----|------|
| `TOS_ENDPOINT` | `https://tos-cn-beijing.volces.com` | 存储桶外网访问域名 |
| `TOS_REGION` | `cn-beijing` | 区域 |
| `TOS_ACCESS_KEY` | `AKLT...` | AccessKey ID |
| `TOS_SECRET_KEY` | `TTE1...` | Secret Access Key |
| `TOS_BUCKET` | `magao` | 存储桶名称 |
| `TOS_PUBLIC_DOMAIN` | (可选) | CDN 加速域名，没有留空 |

---

## 第三步：VOD 视频点播配置

### 3.1 创建空间（Space）

1. 进入 [VOD 控制台](https://console.volcengine.com/vod/)
2. 选择 **空间管理** → **创建空间**
3. 填写信息：
   - **空间名称**：`magao-video`（或你的自定义名称）
   - **区域**：选择与 TOS 相同的区域，如 **华北（北京）**
4. 点击 **确认创建**

### 3.2 获取播放域名

1. 在 VOD 控制台 → **域名管理**
2. 系统会分配一个默认的播放域名，如：
   ```
   vod-player.magao-video.volcvod.com
   ```
3. 你也可以添加自定义域名（需要备案）

### 3.3 配置转码模板（可选）

1. 在 VOD 控制台 → **媒资管理** → **转码模板**
2. 使用默认模板即可，或创建自定义模板
3. 记录模板 ID（如果自定义了模板）

### 3.4 VOD 配置汇总

| 配置项 | 值 | 说明 |
|--------|-----|------|
| `VOD_ACCESS_KEY` | `AKLT...` | 与 TOS 共用同一套 AccessKey |
| `VOD_SECRET_KEY` | `TTE1...` | 与 TOS 共用同一套 SecretKey |
| `VOD_REGION` | `cn-north-1` | VOD 区域（北京一般为 `cn-north-1`） |
| `VOD_PLAY_DOMAIN` | `vod-player...volcvod.com` | VOD 播放域名 |
| `VOD_SPACE_NAME` | `magao-video` | VOD 空间名称 |

---

## 第四步：修改项目配置文件

获取以上所有信息后，需要修改以下文件：

### 4.1 创建 `.env` 文件

在 `magao-platform/` 目录下，复制 `.env.example` 为 `.env`：

```bash
cp .env.example .env
```

编辑 `.env`，填入实际值：

```env
# ========== MySQL ==========
MYSQL_ROOT_PASSWORD=你的强密码

# ========== Redis ==========
REDIS_PASSWORD=你的强密码

# ========== RabbitMQ ==========
RABBITMQ_PASSWORD=你的强密码

# ========== TOS ==========
TOS_ENDPOINT=https://tos-cn-beijing.volces.com
TOS_REGION=cn-beijing
TOS_ACCESS_KEY=你的AccessKey
TOS_SECRET_KEY=你的SecretKey
TOS_BUCKET=magao

# ========== VOD ==========
VOD_ACCESS_KEY=你的AccessKey
VOD_SECRET_KEY=你的SecretKey
VOD_REGION=cn-north-1
VOD_PLAY_DOMAIN=
VOD_SPACE_NAME=magao-video

# ========== 域名 ==========
DOMAIN=www.你的域名.com

# ========== JWT ==========
JWT_SECRET=随机字符串至少64位
```

### 4.2 修改前端域名配置

编辑 `frontend/.env.production`：

```env
VITE_APP_TITLE=码高少儿编程教育平台
VITE_APP_BASE_API=/api
VITE_APP_BASE_URL=https://www.你的域名.com
```

### 4.3 修改 Nginx 配置

编辑 `docker/nginx/conf.d/production.conf`，将第 16 行：

```nginx
server_name your-domain.com www.your-domain.com localhost _;
```

替换为你的实际域名：

```nginx
server_name www.你的域名.com 你的域名.com localhost _;
```

---

## 第五步：ECS 安全组配置

在火山引擎 ECS 控制台 → 安全组 → 配置规则，放行以下端口：

| 端口 | 协议 | 源地址 | 用途 |
|------|------|--------|------|
| 80 | TCP | 0.0.0.0/0 | HTTP |
| 443 | TCP | 0.0.0.0/0 | HTTPS |
| 8080 | TCP | 0.0.0.0/0 | 后端 API（调试） |
| 22 | TCP | 你的IP/32 | SSH（仅允许你的IP） |

---

## 第六步：部署到服务器

将以下文件上传到 ECS 服务器：

```
/opt/magao-platform/
  ├── .env                          # 环境变量配置
  ├── docker/
  │   ├── docker-compose.yml        # Docker 编排
  │   ├── nginx/
  │   │   ├── nginx.conf
  │   │   └── conf.d/
  │   │       └── production.conf   # 已修改 server_name
  │   └── ...
  ├── backend/
  │   ├── Dockerfile
  │   ├── pom.xml
  │   └── src/
  └── frontend/dist/                 # 构建产物 → 复制到 /data/frontend/dist/
```

执行部署命令：

```bash
cd /opt/magao-platform
docker-compose -f docker/docker-compose.yml up -d
```

---

## 配置检查清单

在部署前，请确认以下配置已正确填写：

- [ ] TOS_ACCESS_KEY 已填写
- [ ] TOS_SECRET_KEY 已填写
- [ ] TOS_BUCKET 已创建
- [ ] TOS CORS 已配置
- [ ] VOD_ACCESS_KEY 已填写
- [ ] VOD_SECRET_KEY 已填写
- [ ] VOD_SPACE_NAME 已创建
- [ ] 域名已解析到 ECS IP
- [ ] Nginx server_name 已修改为实际域名
- [ ] 前端 .env.production 中的域名已修改
- [ ] ECS 安全组已放行 80/443/8080
- [ ] MySQL/Redis/RabbitMQ 密码已设置为强密码
- [ ] JWT_SECRET 已设置为随机字符串
