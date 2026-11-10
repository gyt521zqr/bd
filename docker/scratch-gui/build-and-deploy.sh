#!/bin/bash
# ============================================================================
# Scratch GUI 编译 & 部署脚本
# ============================================================================
# 功能：
# 1. 克隆官方 scratch-gui 仓库
# 2. 安装依赖并编译
# 3. 注入 postMessage 桥接脚本
# 4. 将编译产物复制到 Nginx 静态目录
# ============================================================================

set -e

# ==================== 配置 ====================
SCRATCH_GUI_VERSION="v3.0.0"           # scratch-gui 版本
BUILD_DIR="./build-scratch-gui"        # 临时编译目录
OUTPUT_DIR="/data/static/scratch-gui"  # Nginx 静态目录（生产环境）
BRIDGE_SCRIPT="./docker/scratch-gui/bridge.js"  # 桥接脚本路径

echo "=========================================="
echo "  Scratch GUI 编译部署脚本"
echo "  版本: ${SCRATCH_GUI_VERSION}"
echo "=========================================="

# ==================== 步骤 1: 克隆仓库 ====================
if [ ! -d "${BUILD_DIR}" ]; then
  echo "[1/5] 克隆 scratch-gui 仓库..."
  git clone --depth 1 --branch ${SCRATCH_GUI_VERSION} \
    https://github.com/scratchfoundation/scratch-gui.git \
    ${BUILD_DIR}
else
  echo "[1/5] 仓库已存在，跳过克隆"
fi

cd ${BUILD_DIR}

# ==================== 步骤 2: 安装依赖 ====================
echo "[2/5] 安装依赖..."
npm install

# ==================== 步骤 3: 编译 ====================
echo "[3/5] 编译 scratch-gui（生产模式）..."
npm run build

# ==================== 步骤 4: 注入桥接脚本 ====================
echo "[4/5] 注入 postMessage 桥接脚本..."

# 在 build/index.html 的 </body> 前注入 bridge.js
if [ -f "build/index.html" ]; then
  # 复制桥接脚本到 build 目录
  cp "${BRIDGE_SCRIPT}" "build/bridge.js"

  # 注入 script 标签
  sed -i 's|</body>|<script src="bridge.js"></script>\n</body>|' build/index.html

  echo "  桥接脚本已注入到 build/index.html"
else
  echo "  警告: build/index.html 不存在，跳过桥接脚本注入"
fi

# ==================== 步骤 5: 部署到 Nginx 静态目录 ====================
echo "[5/5] 部署到 Nginx 静态目录..."

if [ -d "build" ]; then
  # 创建目标目录（如果不存在）
  sudo mkdir -p ${OUTPUT_DIR}

  # 清空目标目录并复制新文件
  sudo rm -rf ${OUTPUT_DIR}/*
  sudo cp -r build/* ${OUTPUT_DIR}/

  # 设置权限
  sudo chown -R nginx:nginx ${OUTPUT_DIR}
  sudo chmod -R 755 ${OUTPUT_DIR}

  echo "  部署完成！文件已复制到 ${OUTPUT_DIR}"
  echo "  文件数量: $(find ${OUTPUT_DIR} -type f | wc -l)"
  echo "  总大小: $(du -sh ${OUTPUT_DIR} | cut -f1)"
else
  echo "  错误: build 目录不存在"
  exit 1
fi

# ==================== 完成 ====================
echo ""
echo "=========================================="
echo "  编译部署完成！"
echo "  访问地址: http://your-domain/scratch-gui/"
echo "  重载 Nginx: sudo nginx -s reload"
echo "=========================================="
