# 码高少儿编程教育平台 - GitHub 部署指南

## 文件说明

- `magao-platform-dist.zip` (约 54MB) - 构建后的前端静态文件
- 解压后包含：`index.html`, `404.html`, `css/`, `js/`, `assets/`, `fonts/`, `images/`

## 部署步骤

### 方法一：GitHub Pages（推荐）

1. **创建 GitHub 仓库**
   - 打开 https://github.com/new
   - 仓库名：`magao-platform`
   - 选择 Public
   - 点击 Create repository

2. **上传文件**
   - 在仓库页面点击 "uploading an existing file"
   - 解压 `magao-platform-dist.zip`
   - 将解压后的所有文件（index.html, 404.html, css/, js/ 等）拖拽到上传区域
   - Commit changes

3. **开启 GitHub Pages**
   - 进入 Settings -> Pages
   - Source 选择 "Deploy from a branch"
   - Branch 选择 `main`，文件夹选择 `/(root)`
   - 点击 Save

4. **访问网站**
   - 等待 1-2 分钟
   - 访问：`https://你的用户名.github.io/magao-platform/`

### 方法二：Vercel（更简单，支持自动部署）

1. 打开 https://vercel.com/new
2. 选择 "Upload" 上传解压后的 dist 文件夹
3. 点击 Deploy
4. 自动获得 HTTPS 链接

### 方法三：Netlify

1. 打开 https://app.netlify.com/drop
2. 直接拖拽解压后的 dist 文件夹
3. 自动部署完成

## 注意事项

- 这是纯前端静态部署，API 功能（登录、数据获取等）会显示 Mock 数据
- 已配置 `base: './'` 和 `404.html`，支持页面刷新
- 如需更新，重新构建后重新上传即可
