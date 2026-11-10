#!/bin/bash
# ============================================================================
# Scratch GUI 本地开发环境快速设置脚本（Linux/macOS）
# ============================================================================
# 功能：
#   1. 检查本地开发环境依赖
#   2. 创建本地模拟的 scratch-gui 目录结构
#   3. 启动前端开发服务器
# ============================================================================

set -e

# 颜色输出
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m'

echo "=========================================="
echo "  Scratch GUI 本地开发环境设置"
echo "=========================================="
echo ""

# 获取项目目录
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(cd "${SCRIPT_DIR}/../.." && pwd)"
MOCK_DIR="${PROJECT_DIR}/frontend/public/scratch-gui"

# [1/4] 检查 Node.js
echo -e "${BLUE}[1/4]${NC} 检查 Node.js..."
if ! command -v node &> /dev/null; then
    echo -e "${RED}[错误]${NC} Node.js 未安装，请先安装 Node.js 18+"
    exit 1
fi

NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
if [ "$NODE_VERSION" -lt 18 ]; then
    echo -e "${RED}[错误]${NC} Node.js 版本过低，需要 18+，当前: $(node -v)"
    exit 1
fi
echo -e "  ${GREEN}Node.js 版本: $(node -v)${NC}"

# [2/4] 检查 npm
echo -e "${BLUE}[2/4]${NC} 检查 npm..."
if ! command -v npm &> /dev/null; then
    echo -e "${RED}[错误]${NC} npm 未安装"
    exit 1
fi
echo -e "  ${GREEN}npm 版本: $(npm -v)${NC}"

# [3/4] 创建本地模拟目录
echo -e "${BLUE}[3/4]${NC} 创建本地模拟目录..."
mkdir -p "${MOCK_DIR}"
echo -e "  模拟目录: ${MOCK_DIR}"

if [ -f "${MOCK_DIR}/index.html" ]; then
    echo -e "  ${GREEN}[OK] mock index.html 已存在${NC}"
else
    echo -e "  ${YELLOW}[警告] mock index.html 不存在${NC}"
    echo -e "  请确认前端 public 目录结构正确"
fi

# [4/4] 启动前端开发服务器
echo -e "${BLUE}[4/4]${NC} 启动前端开发服务器..."
echo ""

cd "${PROJECT_DIR}/frontend"

echo "正在安装依赖（如已安装会自动跳过）..."
npm install

echo ""
echo "=========================================="
echo -e "  ${GREEN}启动开发服务器...${NC}"
echo "  访问地址: http://localhost:3000"
echo "  练习页面: http://localhost:3000/practice"
echo "=========================================="
echo ""

npm run dev
