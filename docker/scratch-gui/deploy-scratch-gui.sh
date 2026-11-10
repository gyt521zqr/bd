#!/bin/bash
# ============================================================================
# Scratch GUI 3.0 编译 & 部署脚本（方案2：Nginx 独立静态托管）
# ============================================================================
# 功能：
#   1. 克隆/更新 scratch-gui 官方仓库
#   2. 安装依赖并编译生产版本
#   3. 注入 postMessage 桥接脚本（与前端 iframe 通信）
#   4. 部署到服务器 /data/static/scratch-gui/ 目录
#   5. 重载 Nginx 生效
# ============================================================================
# 使用方法：
#   chmod +x deploy-scratch-gui.sh
#   ./deploy-scratch-gui.sh
# ============================================================================

set -e

# ==================== 配置 ====================
SCRATCH_GUI_VERSION="v3.0.0"           # scratch-gui 版本标签
BUILD_DIR="/tmp/build-scratch-gui"     # 临时编译目录
OUTPUT_DIR="/data/static/scratch-gui"  # Nginx 静态托管目录（生产环境）
BRIDGE_SCRIPT="$(dirname "$0")/bridge.js"  # 桥接脚本路径

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

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
echo "  Scratch GUI 3.0 编译部署脚本"
echo "  版本: ${SCRATCH_GUI_VERSION}"
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
log_info "[1/6] 准备 scratch-gui 源码..."

if [ -d "${BUILD_DIR}/.git" ]; then
    log_info "仓库已存在，更新到 ${SCRATCH_GUI_VERSION}..."
    cd "${BUILD_DIR}"
    git fetch --tags
    git checkout "${SCRATCH_GUI_VERSION}" || git checkout tags/${SCRATCH_GUI_VERSION}
    git pull origin "${SCRATCH_GUI_VERSION}" 2>/dev/null || true
else
    log_info "克隆 scratch-gui 仓库..."
    rm -rf "${BUILD_DIR}"
    git clone --depth 1 --branch "${SCRATCH_GUI_VERSION}" \
        https://github.com/scratchfoundation/scratch-gui.git \
        "${BUILD_DIR}"
    cd "${BUILD_DIR}"
fi

log_success "源码准备完成"

# ==================== 步骤 2: 安装依赖 ====================
echo ""
log_info "[2/6] 安装依赖（可能需要几分钟）..."

# 使用 npm ci 如果存在 package-lock.json，否则使用 npm install
if [ -f "package-lock.json" ]; then
    npm ci
else
    npm install
fi

log_success "依赖安装完成"

# ==================== 步骤 3: 编译生产版本 ====================
echo ""
log_info "[3/6] 编译 scratch-gui（生产模式）..."

# 设置生产环境变量
export NODE_ENV=production

# 编译
npm run build

if [ ! -d "build" ]; then
    log_error "编译失败，build 目录不存在"
    exit 1
fi

log_success "编译完成"
log_info "编译输出: $(du -sh build | cut -f1)"
log_info "文件数量: $(find build -type f | wc -l)"

# ==================== 步骤 4: 注入桥接脚本 ====================
echo ""
log_info "[4/6] 注入 postMessage 桥接脚本..."

if [ -f "build/index.html" ]; then
    # 复制桥接脚本到 build 目录
    if [ -f "$BRIDGE_SCRIPT" ]; then
        cp "$BRIDGE_SCRIPT" "build/bridge.js"
        log_info "桥接脚本已复制到 build/bridge.js"
    else
        log_warn "桥接脚本不存在: $BRIDGE_SCRIPT，跳过注入"
        log_warn "前端 iframe 通信功能将不可用"
    fi

    # 注入 script 标签到 index.html（在 </body> 前）
    if [ -f "build/bridge.js" ]; then
        if grep -q "bridge.js" "build/index.html"; then
            log_info "桥接脚本已存在，跳过注入"
        else
            sed -i 's|</body>|<script src="bridge.js"></script>\n</body>|' build/index.html
            log_success "桥接脚本已注入到 build/index.html"
        fi
    fi
else
    log_warn "build/index.html 不存在，跳过桥接脚本注入"
fi

# ==================== 步骤 5: 部署到 Nginx 静态目录 ====================
echo ""
log_info "[5/6] 部署到 Nginx 静态目录..."

# 备份旧版本
if [ -d "${OUTPUT_DIR}" ] && [ "$(ls -A ${OUTPUT_DIR})" ]; then
    BACKUP_DIR="${OUTPUT_DIR}.backup.$(date +%Y%m%d_%H%M%S)"
    log_info "备份旧版本到: ${BACKUP_DIR}"
    sudo cp -r "${OUTPUT_DIR}" "${BACKUP_DIR}"
fi

# 清空目标目录并复制新文件
log_info "复制编译产物到 ${OUTPUT_DIR}..."
sudo rm -rf "${OUTPUT_DIR}"/*
sudo cp -r build/* "${OUTPUT_DIR}/"

# 设置权限
sudo chown -R nginx:nginx "${OUTPUT_DIR}" 2>/dev/null || sudo chown -R root:root "${OUTPUT_DIR}"
sudo chmod -R 755 "${OUTPUT_DIR}"

log_success "部署完成！"
log_info "部署路径: ${OUTPUT_DIR}"
log_info "文件数量: $(find "${OUTPUT_DIR}" -type f | wc -l)"
log_info "总大小: $(du -sh "${OUTPUT_DIR}" | cut -f1)"

# ==================== 步骤 6: 重载 Nginx ====================
echo ""
log_info "[6/6] 重载 Nginx..."

if command -v nginx &> /dev/null; then
    # 测试配置
    if sudo nginx -t; then
        sudo nginx -s reload
        log_success "Nginx 重载成功"
    else
        log_error "Nginx 配置测试失败，请检查配置"
        exit 1
    fi
else
    log_warn "Nginx 未安装，请手动重载"
fi

# ==================== 完成 ====================
echo ""
echo "=========================================="
echo -e "${GREEN}  Scratch GUI 部署完成！${NC}"
echo "=========================================="
echo ""
echo "  访问地址:"
echo "    - 编辑器: http://your-domain/scratch-gui/"
echo "    - iframe: /scratch-gui/index.html"
echo ""
echo "  文件位置:"
echo "    - 静态资源: ${OUTPUT_DIR}"
echo ""
echo "  更新方法:"
echo "    - 重新运行本脚本即可自动更新"
echo "    - 无需重新构建前端项目"
echo ""
echo "=========================================="
