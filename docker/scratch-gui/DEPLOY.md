# Scratch GUI 部署指南

## 问题说明

前端页面显示 **"Scratch 编辑器资源未就绪，请确认 scratch-gui 已正确部署"**，说明 scratch-gui 编译产物尚未放置到服务器。

## 快速部署步骤

### 方法一：自动脚本部署（推荐）

```bash
# 进入项目目录
cd /path/to/magao-platform

# 运行自动部署脚本
bash docker/scratch-gui/build-and-deploy.sh
```

### 方法二：手动部署

#### 1. 编译 scratch-gui

```bash
# 克隆仓库
git clone --depth 1 https://github.com/scratchfoundation/scratch-gui.git
cd scratch-gui

# 安装依赖
npm install

# 生产编译
npm run build
```

#### 2. 复制到 Nginx 静态目录

```bash
# 创建目录
sudo mkdir -p /data/static/scratch-gui

# 复制编译产物
sudo cp -r build/* /data/static/scratch-gui/

# 复制桥接脚本
sudo cp docker/scratch-gui/bridge.js /data/static/scratch-gui/

# 注入桥接脚本到 index.html
sudo sed -i 's|</body>|<script src="bridge.js"></script>\n</body>|' /data/static/scratch-gui/index.html

# 设置权限
sudo chown -R nginx:nginx /data/static/scratch-gui
sudo chmod -R 755 /data/static/scratch-gui
```

#### 3. 重载 Nginx

```bash
sudo nginx -s reload
```

### 方法三：开发环境快速测试

在本地开发时，可将编译产物放到前端 public 目录：

```bash
# 编译完成后
cp -r scratch-gui/build/* frontend/public/scratch-gui/
cp docker/scratch-gui/bridge.js frontend/public/scratch-gui/

# 修改 index.html 注入桥接脚本
```

这样 Vite dev server 会直接提供这些静态文件。

## 验证部署

部署完成后，访问以下地址验证：

```
http://your-domain/scratch-gui/index.html
```

应能看到 scratch-gui 界面（而非 404）。

## 文件结构

部署后 `/data/static/scratch-gui/` 目录应包含：

```
/data/static/scratch-gui/
├── index.html          # 主入口（已注入 bridge.js）
├── bridge.js           # postMessage 桥接脚本
├── static/
│   ├── css/
│   ├── js/
│   └── media/
└── ...
```

## 常见问题

### 1. iframe 加载后一直显示加载中
- 检查 `bridge.js` 是否正确注入到 `index.html`
- 检查浏览器控制台是否有 postMessage 错误

### 2. 跨域错误
- 确认 Nginx 配置中 `X-Frame-Options` 设置为 `SAMEORIGIN`
- 确认父页面和 iframe 同域

### 3. 素材无法加载
- 检查 `static/` 目录是否正确复制
- 检查 Nginx 静态资源缓存配置
