#!/bin/bash
# ============================================================================
# Scratch Jr 编译 & 部署脚本（方案2：Nginx 独立静态托管）
# ============================================================================
# 功能：
#   1. 克隆/更新 ScratchJr-Desktop 仓库
#   2. 应用 BrowserDesktopInterface 补丁（替代 Electron 接口）
#   3. 编译 Web 版本
#   4. 部署到服务器 /data/static/scratch-jr/ 目录
#   5. 重载 Nginx 生效
# ============================================================================
# 使用方法：
#   chmod +x deploy-scratch-jr.sh
#   ./deploy-scratch-jr.sh
# ============================================================================

set -e

# ==================== 配置 ====================
BUILD_DIR="/tmp/build-scratch-jr"      # 临时编译目录
OUTPUT_DIR="/data/static/scratch-jr"   # Nginx 静态托管目录（生产环境）
PATCH_DIR="$(dirname "$0")/patches"    # 补丁文件目录

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# ==================== 前置检查 ====================

echo ""
echo "=========================================="
echo "  Scratch Jr 编译部署脚本"
echo "  输出目录: ${OUTPUT_DIR}"
echo "=========================================="
echo ""

# 检查 Node.js
if ! command -v node &> /dev/null; then
    log_error "Node.js 未安装，请先安装 Node.js 18+"
    exit 1
fi

NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
if [ "$NODE_VERSION" -lt 18 ]; then
    log_error "Node.js 版本过低，需要 18+，当前: $(node -v)"
    exit 1
fi
log_info "Node.js 版本: $(node -v)"

# 检查 Git
if ! command -v git &> /dev/null; then
    log_error "Git 未安装"
    exit 1
fi

# 检查 Nginx
if ! command -v nginx &> /dev/null; then
    log_warn "Nginx 未安装，部署后请手动重载 Nginx"
else
    log_info "Nginx 版本: $(nginx -v 2>&1 | head -1)"
fi

# 创建输出目录
if [ ! -d "$OUTPUT_DIR" ]; then
    log_info "创建输出目录: $OUTPUT_DIR"
    sudo mkdir -p "$OUTPUT_DIR"
fi

# ==================== 步骤 1: 克隆/更新仓库 ====================
echo ""
log_info "[1/6] 准备 ScratchJr-Desktop 源码..."

if [ -d "${BUILD_DIR}/.git" ]; then
    log_info "仓库已存在，更新到最新..."
    cd "${BUILD_DIR}"
    git pull origin main 2>/dev/null || git pull origin master 2>/dev/null || true
else
    log_info "克隆 ScratchJr-Desktop 仓库..."
    rm -rf "${BUILD_DIR}"
    git clone --depth 1 \
        https://github.com/jfo8000/ScratchJr-Desktop.git \
        "${BUILD_DIR}"
    cd "${BUILD_DIR}"
fi

log_success "源码准备完成"

# ==================== 步骤 2: 应用浏览器补丁 ====================
echo ""
log_info "[2/6] 应用 BrowserDesktopInterface 补丁..."

# 创建 BrowserDesktopInterface.js（替代 Electron 接口）
cat > src/utils/BrowserDesktopInterface.js << 'BROWSER_INTERFACE_EOF'
/**
 * BrowserDesktopInterface - 浏览器环境替代 Electron 的 DesktopInterface
 * 用于将 ScratchJr-Desktop 打包为纯 Web 静态文件
 */

class BrowserDesktopInterface {
  constructor() {
    this.db = null;
    this.dbName = 'scratchjr_db';
    this.dbVersion = 1;
    this.initDB();
  }

  // 初始化 IndexedDB
  initDB() {
    return new Promise((resolve, reject) => {
      const request = indexedDB.open(this.dbName, this.dbVersion);
      request.onerror = () => reject(request.error);
      request.onsuccess = () => {
        this.db = request.result;
        resolve(this.db);
      };
      request.onupgradeneeded = (event) => {
        const db = event.target.result;
        if (!db.objectStoreNames.contains('projects')) {
          db.createObjectStore('projects', { keyPath: 'id' });
        }
        if (!db.objectStoreNames.contains('assets')) {
          db.createObjectStore('assets', { keyPath: 'name' });
        }
      };
    });
  }

  // 项目操作
  async saveProject(projectId, data) {
    await this.initDB();
    return new Promise((resolve, reject) => {
      const tx = this.db.transaction(['projects'], 'readwrite');
      const store = tx.objectStore('projects');
      const request = store.put({ id: projectId, data: data, timestamp: Date.now() });
      request.onsuccess = () => resolve(true);
      request.onerror = () => reject(request.error);
    });
  }

  async loadProject(projectId) {
    await this.initDB();
    return new Promise((resolve, reject) => {
      const tx = this.db.transaction(['projects'], 'readonly');
      const store = tx.objectStore('projects');
      const request = store.get(projectId);
      request.onsuccess = () => resolve(request.result?.data || null);
      request.onerror = () => reject(request.error);
    });
  }

  async deleteProject(projectId) {
    await this.initDB();
    return new Promise((resolve, reject) => {
      const tx = this.db.transaction(['projects'], 'readwrite');
      const store = tx.objectStore('projects');
      const request = store.delete(projectId);
      request.onsuccess = () => resolve(true);
      request.onerror = () => reject(request.error);
    });
  }

  async listProjects() {
    await this.initDB();
    return new Promise((resolve, reject) => {
      const tx = this.db.transaction(['projects'], 'readonly');
      const store = tx.objectStore('projects');
      const request = store.getAll();
      request.onsuccess = () => resolve(request.result || []);
      request.onerror = () => reject(request.error);
    });
  }

  // 资源文件操作
  async saveAsset(name, data) {
    await this.initDB();
    return new Promise((resolve, reject) => {
      const tx = this.db.transaction(['assets'], 'readwrite');
      const store = tx.objectStore('assets');
      const request = store.put({ name: name, data: data, timestamp: Date.now() });
      request.onsuccess = () => resolve(true);
      request.onerror = () => reject(request.error);
    });
  }

  async loadAsset(name) {
    await this.initDB();
    return new Promise((resolve, reject) => {
      const tx = this.db.transaction(['assets'], 'readonly');
      const store = tx.objectStore('assets');
      const request = store.get(name);
      request.onsuccess = () => resolve(request.result?.data || null);
      request.onerror = () => reject(request.error);
    });
  }

  // 文件系统操作（浏览器环境模拟）
  async writeFile(path, data) {
    localStorage.setItem('scratchjr_file_' + path, JSON.stringify(data));
    return true;
  }

  async readFile(path) {
    const data = localStorage.getItem('scratchjr_file_' + path);
    return data ? JSON.parse(data) : null;
  }

  // postMessage 通信（与父页面）
  postMessage(type, payload) {
    if (window.parent !== window) {
      window.parent.postMessage({ type, payload }, '*');
    }
  }

  // 监听父页面消息
  onMessage(callback) {
    window.addEventListener('message', (event) => {
      if (event.data && event.data.type) {
        callback(event.data.type, event.data.payload);
      }
    });
  }
}

// 导出全局实例
window.BrowserDesktopInterface = BrowserDesktopInterface;
window.desktopInterface = new BrowserDesktopInterface();

// 通知父页面就绪
window.addEventListener('load', () => {
  setTimeout(() => {
    if (window.parent !== window) {
      window.parent.postMessage({ type: 'READY', payload: { success: true } }, '*');
    }
  }, 500);
});
BROWSER_INTERFACE_EOF

log_success "BrowserDesktopInterface 补丁已应用"

# ==================== 步骤 3: 修改入口文件 ====================
echo ""
log_info "[3/6] 修改入口文件，注入 BrowserDesktopInterface..."

# 在 index.html 中注入 BrowserDesktopInterface
if [ -f "index.html" ]; then
    if ! grep -q "BrowserDesktopInterface.js" "index.html"; then
        sed -i 's|</head>|<script src="src/utils/BrowserDesktopInterface.js"></script>\n</head>|' index.html
        log_success "BrowserDesktopInterface 已注入到 index.html"
    else
        log_info "BrowserDesktopInterface 已存在，跳过注入"
    fi
else
    log_warn "index.html 不存在"
fi

# ==================== 步骤 4: 安装依赖 ====================
echo ""
log_info "[4/6] 安装依赖..."

if [ -f "package-lock.json" ]; then
    npm ci
else
    npm install
fi

log_success "依赖安装完成"

# ==================== 步骤 5: 编译 Web 版本 ====================
echo ""
log_info "[5/6] 编译 Scratch Jr Web 版本..."

# 如果项目有 build 脚本则执行，否则手动复制静态文件
if grep -q '"build"' package.json; then
    npm run build
    BUILD_OUTPUT="build"
else
    # 手动构建：复制必要文件到输出目录
    log_info "项目无 build 脚本，手动收集静态文件..."
    BUILD_OUTPUT="dist"
    mkdir -p "${BUILD_OUTPUT}"
    
    # 复制核心文件
    cp -r src "${BUILD_OUTPUT}/" 2>/dev/null || true
    cp -r assets "${BUILD_OUTPUT}/" 2>/dev/null || true
    cp -r lib "${BUILD_OUTPUT}/" 2>/dev/null || true
    cp index.html "${BUILD_OUTPUT}/" 2>/dev/null || true
    cp -r node_modules "${BUILD_OUTPUT}/" 2>/dev/null || true
fi

if [ ! -d "$BUILD_OUTPUT" ]; then
    log_error "编译失败，输出目录不存在"
    exit 1
fi

log_success "编译完成"
log_info "编译输出: $(du -sh "$BUILD_OUTPUT" | cut -f1)"
log_info "文件数量: $(find "$BUILD_OUTPUT" -type f | wc -l)"

# ==================== 步骤 6: 部署到 Nginx 静态目录 ====================
echo ""
log_info "[6/6] 部署到 Nginx 静态目录..."

# 备份旧版本
if [ -d "${OUTPUT_DIR}" ] && [ "$(ls -A ${OUTPUT_DIR})" ]; then
    BACKUP_DIR="${OUTPUT_DIR}.backup.$(date +%Y%m%d_%H%M%S)"
    log_info "备份旧版本到: ${BACKUP_DIR}"
    sudo cp -r "${OUTPUT_DIR}" "${BACKUP_DIR}"
fi

# 清空目标目录并复制新文件
log_info "复制编译产物到 ${OUTPUT_DIR}..."
sudo rm -rf "${OUTPUT_DIR}"/*
sudo cp -r "${BUILD_OUTPUT}"/* "${OUTPUT_DIR}/"

# 设置权限
sudo chown -R nginx:nginx "${OUTPUT_DIR}" 2>/dev/null || sudo chown -R root:root "${OUTPUT_DIR}"
sudo chmod -R 755 "${OUTPUT_DIR}"

log_success "部署完成！"
log_info "部署路径: ${OUTPUT_DIR}"
log_info "文件数量: $(find "${OUTPUT_DIR}" -type f | wc -l)"
log_info "总大小: $(du -sh "${OUTPUT_DIR}" | cut -f1)"

# ==================== 重载 Nginx ====================
echo ""
log_info "重载 Nginx..."

if command -v nginx &> /dev/null; then
    if sudo nginx -t; then
        sudo nginx -s reload
        log_success "Nginx 重载成功"
    else
        log_error "Nginx 配置测试失败"
        exit 1
    fi
else
    log_warn "Nginx 未安装，请手动重载"
fi

# ==================== 完成 ====================
echo ""
echo "=========================================="
echo -e "${GREEN}  Scratch Jr 部署完成！${NC}"
echo "=========================================="
echo ""
echo "  访问地址:"
echo "    - 编辑器: http://your-domain/scratch-jr/"
echo "    - iframe: /scratch-jr/index.html"
echo ""
echo "  文件位置:"
echo "    - 静态资源: ${OUTPUT_DIR}"
echo ""
echo "=========================================="
