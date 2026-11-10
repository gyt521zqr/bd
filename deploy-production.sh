#!/bin/bash
# ============================================================================
# Magao Platform - 生产环境一键部署脚本（方案2：Nginx 静态托管）
# ============================================================================
# 功能：
#   1. 编译 Vue 前端项目
#   2. 编译并部署 scratch-gui 静态资源
#   3. 编译并部署 scratch-jr 静态资源（可选）
#   4. 复制 Nginx 配置
#   5. 启动 Docker Compose 服务
# ============================================================================
# 使用方法：
#   chmod +x deploy-production.sh
#   ./deploy-production.sh
# ============================================================================

set -e

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

# 项目目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="${SCRIPT_DIR}"
DOCKER_DIR="${PROJECT_DIR}/docker"
FRONTEND_DIR="${PROJECT_DIR}/frontend"

# 部署域名（可通过环境变量 DOMAIN 或第一个参数指定，默认 www.magao.com）
# 用法：DOMAIN=your-domain.com ./deploy-production.sh  或  ./deploy-production.sh your-domain.com
DOMAIN="${1:-${DOMAIN:-www.magao.com}}"

# 部署目录
FRONTEND_DIST="/data/frontend/dist"
SCRATCH_GUI_DIR="/data/static/scratch-gui"
SCRATCH_JR_DIR="/data/static/scratch-jr"

# 函数：打印信息
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

log_step() {
    echo ""
    echo -e "${CYAN}==========================================${NC}"
    echo -e "${CYAN}  $1${NC}"
    echo -e "${CYAN}==========================================${NC}"
    echo ""
}

# 函数：检查命令是否存在
check_command() {
    if ! command -v "$1" &> /dev/null; then
        log_error "$1 未安装"
        return 1
    fi
    return 0
}

# ============================================================================
# 开始部署
# ============================================================================

clear
log_step "Magao Platform 生产环境部署（方案2：Nginx 静态托管）"

echo -e "项目目录: ${PROJECT_DIR}"
echo -e "前端目录: ${FRONTEND_DIR}"
echo -e "Docker目录: ${DOCKER_DIR}"
echo ""

# ============================================================================
# 前置检查
# ============================================================================
log_step "前置环境检查"

# 检查 Node.js
if check_command node; then
    NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
    if [ "$NODE_VERSION" -lt 18 ]; then
        log_error "Node.js 版本过低，需要 18+，当前: $(node -v)"
        exit 1
    fi
    log_info "Node.js 版本: $(node -v)"
fi

# 检查 npm
check_command npm && log_info "npm 版本: $(npm -v)"

# 检查 Git
check_command git && log_info "Git 已安装"

# 检查 Docker
check_command docker && log_info "Docker 已安装"

# 检查 Docker Compose
if check_command docker-compose; then
    log_info "Docker Compose 已安装"
elif docker compose version &> /dev/null; then
    log_info "Docker Compose (plugin) 已安装"
else
    log_warn "Docker Compose 未安装，将使用独立 Nginx 部署"
fi

# 检查 Nginx
check_command nginx && log_info "Nginx 已安装"

# ============================================================================
# 步骤 1：编译 Vue 前端
# ============================================================================
log_step "步骤 1/5：编译 Vue 前端项目"

cd "${FRONTEND_DIR}"

log_info "安装前端依赖..."
npm install

log_info "生产构建（域名: ${DOMAIN}）..."
VITE_APP_BASE_URL="https://${DOMAIN}" npm run build

if [ ! -d "dist" ]; then
    log_error "前端构建失败，dist 目录不存在"
    exit 1
fi

log_success "前端构建完成"
log_info "构建输出: $(du -sh dist | cut -f1)"

# ============================================================================
# 步骤 2：部署前端到 Nginx 目录
# ============================================================================
log_step "步骤 2/5：部署前端到 Nginx 静态目录"

log_info "创建目录: ${FRONTEND_DIST}"
sudo mkdir -p "${FRONTEND_DIST}"

log_info "复制前端文件..."
sudo rm -rf "${FRONTEND_DIST}"/*
sudo cp -r "${FRONTEND_DIR}/dist"/* "${FRONTEND_DIST}/"

log_info "设置权限..."
sudo chown -R nginx:nginx "${FRONTEND_DIST}" 2>/dev/null || sudo chown -R root:root "${FRONTEND_DIST}"
sudo chmod -R 755 "${FRONTEND_DIST}"

log_success "前端部署完成"
log_info "文件数量: $(find "${FRONTEND_DIST}" -type f | wc -l)"

# ============================================================================
# 步骤 3：编译并部署 Scratch GUI
# ============================================================================
log_step "步骤 3/5：编译并部署 Scratch GUI"

if [ -f "${DOCKER_DIR}/scratch-gui/deploy-scratch-gui.sh" ]; then
    log_info "运行 Scratch GUI 部署脚本..."
    bash "${DOCKER_DIR}/scratch-gui/deploy-scratch-gui.sh"
else
    log_warn "Scratch GUI 部署脚本不存在，跳过"
    log_warn "如需部署 Scratch 编辑器，请手动运行:"
    log_warn "  bash docker/scratch-gui/deploy-scratch-gui.sh"
fi

# ============================================================================
# 步骤 4：编译并部署 Scratch Jr（可选）
# ============================================================================
log_step "步骤 4/5：编译并部署 Scratch Jr（可选）"

if [ -f "${DOCKER_DIR}/scratch-jr/deploy-scratch-jr.sh" ]; then
    read -p "是否部署 Scratch Jr? (y/N): " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        log_info "运行 Scratch Jr 部署脚本..."
        bash "${DOCKER_DIR}/scratch-jr/deploy-scratch-jr.sh"
    else
        log_info "跳过 Scratch Jr 部署"
    fi
else
    log_warn "Scratch Jr 部署脚本不存在，跳过"
fi

# ============================================================================
# 步骤 5：启动服务
# ============================================================================
log_step "步骤 5/5：启动服务"

# 复制 Nginx 配置
log_info "复制 Nginx 配置（域名: ${DOMAIN}）..."
sudo mkdir -p /etc/nginx/conf.d
sudo cp "${DOCKER_DIR}/nginx/nginx.conf" /etc/nginx/nginx.conf
sudo cp "${DOCKER_DIR}/nginx/conf.d/"*.conf /etc/nginx/conf.d/
# 替换 production.conf 中的域名占位符
sudo sed -i "s/your-domain\.com www\.your-domain\.com/${DOMAIN} www.${DOMAIN}/g" /etc/nginx/conf.d/production.conf
log_info "Nginx 域名已替换为: ${DOMAIN}"

# 测试 Nginx 配置
log_info "测试 Nginx 配置..."
if sudo nginx -t; then
    log_success "Nginx 配置测试通过"
else
    log_error "Nginx 配置测试失败"
    exit 1
fi

# 检查 Docker Compose 方式还是独立 Nginx 方式
if [ -f "${DOCKER_DIR}/docker-compose.yml" ] && (command -v docker-compose &> /dev/null || docker compose version &> /dev/null); then
    log_info "使用 Docker Compose 启动服务..."
    cd "${DOCKER_DIR}"

    # 检查 .env 文件
    if [ ! -f ".env" ]; then
        if [ -f ".env.example" ]; then
            log_warn ".env 文件不存在，从 .env.example 复制"
            cp .env.example .env
            log_warn "请编辑 .env 文件填写实际配置后再启动"
            log_warn "当前使用默认配置继续..."
        else
            log_warn ".env.example 也不存在，使用默认配置"
        fi
    fi

    # 启动服务
    if command -v docker-compose &> /dev/null; then
        docker-compose -f docker-compose.yml up -d
    else
        docker compose -f docker-compose.yml up -d
    fi

    log_success "Docker Compose 服务已启动"
else
    log_info "使用独立 Nginx 启动..."
    sudo nginx -s reload 2>/dev/null || sudo nginx
    log_success "Nginx 已启动/重载"
fi

# ============================================================================
# 部署完成
# ============================================================================
log_step "部署完成"

echo -e "${GREEN}Magao Platform 部署成功！${NC}"
echo ""
echo -e "${CYAN}访问地址:${NC}"
echo "  主站首页:     http://your-domain/"
echo "  Scratch编辑器: http://your-domain/scratch-gui/"
echo "  Scratch Jr:   http://your-domain/scratch-jr/"
echo ""
echo -e "${CYAN}文件位置:${NC}"
echo "  前端资源:     ${FRONTEND_DIST}"
echo "  Scratch GUI:  ${SCRATCH_GUI_DIR}"
echo "  Scratch Jr:   ${SCRATCH_JR_DIR}"
echo ""
echo -e "${CYAN}常用命令:${NC}"
echo "  查看日志:     docker-compose -f docker/docker-compose.yml logs -f"
echo "  重启 Nginx:   sudo nginx -s reload"
echo "  更新 Scratch: bash docker/scratch-gui/deploy-scratch-gui.sh"
echo ""
echo -e "${CYAN}验证部署:${NC}"
echo "  curl -I http://localhost/"
echo "  curl -I http://localhost/scratch-gui/index.html"
echo "  curl -I http://localhost/api/health"
echo ""
echo "=========================================="
