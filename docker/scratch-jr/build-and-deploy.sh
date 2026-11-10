#!/bin/bash
# ============================================================================
# ScratchJr Web 构建和部署脚本
# ============================================================================
# 基于 ScratchJr-Desktop (jfo8000/ScratchJr-Desktop) 进行 Web 化改造
# 将 Electron 桌面应用转换为可在浏览器中运行的静态网站
# ============================================================================

set -e

# ==================== 配置 ====================
SCRATCHJR_DESKTOP_REPO="https://github.com/jfo8000/ScratchJr-Desktop.git"
BUILD_DIR="./build-scratchjr"
OUTPUT_DIR="/data/static/scratch-jr"
BROWSER_CLIENT="../browserClient.js"

echo "=========================================="
echo "  ScratchJr Web 构建部署脚本"
echo "=========================================="

# ==================== 步骤 1: 克隆仓库 ====================
if [ ! -d "${BUILD_DIR}" ]; then
  echo "[1/6] 克隆 ScratchJr-Desktop 仓库..."
  git clone --depth 1 ${SCRATCHJR_DESKTOP_REPO} ${BUILD_DIR}
else
  echo "[1/6] 仓库已存在，跳过克隆"
fi

cd ${BUILD_DIR}

# ==================== 步骤 2: 安装依赖 ====================
echo "[2/6] 安装依赖..."
npm install

# ==================== 步骤 3: 替换 ElectronDesktopInterface ====================
echo "[3/6] 替换 ElectronDesktopInterface 为 BrowserDesktopInterface..."

# 复制 browserClient.js 到 src/ 目录
cp ${BROWSER_CLIENT} src/browserClient.js

# 修改 index.html：将 electronClient.js 替换为 browserClient.js
if [ -f "src/app/index.html" ]; then
  sed -i 's|src="../electronClient.js"|src="../browserClient.js"|g' src/app/index.html
  echo "  已修改 index.html，注入 browserClient.js"
fi

# ==================== 步骤 4: 复制资源文件 ====================
echo "[4/6] 复制资源文件到输出目录..."

# 创建输出目录
mkdir -p ${OUTPUT_DIR}/assets

# 复制 ScratchJr 核心文件
cp -r src/app/src/* ${OUTPUT_DIR}/
cp -r src/app/CSS ${OUTPUT_DIR}/assets/ 2>/dev/null || true
cp -r src/app/JSON ${OUTPUT_DIR}/assets/ 2>/dev/null || true
cp -r src/app/SVG ${OUTPUT_DIR}/assets/ 2>/dev/null || true
cp -r src/app/sounds ${OUTPUT_DIR}/assets/ 2>/dev/null || true
cp -r src/app/samples ${OUTPUT_DIR}/assets/ 2>/dev/null || true

# 复制 browserClient.js
cp src/browserClient.js ${OUTPUT_DIR}/

# ==================== 步骤 5: 复制 sql.js ====================
echo "[5/6] 复制 sql.js（SQLite 的 JavaScript 编译版本）..."

# sql.js 已在 node_modules 中
if [ -f "node_modules/sql.js/dist/sql-wasm.js" ]; then
  cp node_modules/sql.js/dist/sql-wasm.js ${OUTPUT_DIR}/
  cp node_modules/sql.js/dist/sql-wasm.wasm ${OUTPUT_DIR}/
  echo "  已复制 sql-wasm.js 和 sql-wasm.wasm"
elif [ -f "node_modules/sql.js/js/sql.js" ]; then
  cp node_modules/sql.js/js/sql.js ${OUTPUT_DIR}/
  cp node_modules/sql.js/sql-wasm.wasm ${OUTPUT_DIR}/ 2>/dev/null || true
  echo "  已复制 sql.js"
fi

# ==================== 步骤 6: 创建 Web 入口 HTML ====================
echo "[6/6] 创建 Web 入口 HTML..."

cat > ${OUTPUT_DIR}/index.html << 'HTMLEOF'
<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0, user-scalable=no">
  <title>ScratchJr Web</title>
  <style>
    * { margin: 0; padding: 0; box-sizing: border-box; }
    html, body { width: 100%; height: 100%; overflow: hidden; }
    #scratchjr-container { width: 100%; height: 100%; }
    #splash {
      position: fixed; inset: 0;
      display: flex; align-items: center; justify-content: center;
      background: linear-gradient(135deg, #4c97ff 0%, #855cd6 100%);
      color: white; font-family: sans-serif; font-size: 24px;
      z-index: 99999;
    }
    #splash .logo { font-size: 48px; margin-bottom: 16px; }
    #splash .loading { animation: pulse 1.5s infinite; }
    @keyframes pulse { 0%, 100% { opacity: 1; } 50% { opacity: 0.5; } }
  </style>
</head>
<body>
  <div id="splash">
    <div style="text-align:center;">
      <div class="logo">🐱</div>
      <div>ScratchJr</div>
      <div class="loading">加载中...</div>
    </div>
  </div>
  <div id="scratchjr-container"></div>

  <!-- 加载 sql.js（SQLite 的 JavaScript 编译版本） -->
  <script src="sql-wasm.js"></script>

  <!-- 加载 BrowserDesktopInterface（替代 ElectronDesktopInterface） -->
  <script src="browserClient.js"></script>

  <!-- 加载 ScratchJr 核心代码 -->
  <script>
    // 等待 sql.js 和 BrowserDesktopInterface 就绪后启动 ScratchJr
    function waitForReady() {
      if (typeof window.tablet !== 'undefined' && window.tablet.initialized) {
        // 加载 ScratchJr 主入口
        var script = document.createElement('script');
        script.src = './iPad/iOS.js';
        script.onload = function () {
          var entryScript = document.createElement('script');
          entryScript.src = './appEntry.js';
          entryScript.onload = function () {
            console.log('[ScratchJrWeb] ScratchJr 启动完成');
          };
          document.head.appendChild(entryScript);
        };
        document.head.appendChild(script);
      } else {
        setTimeout(waitForReady, 100);
      }
    }
    waitForReady();
  </script>
</body>
</html>
HTMLEOF

# 设置权限
sudo chown -R nginx:nginx ${OUTPUT_DIR}
sudo chmod -R 755 ${OUTPUT_DIR}

echo ""
echo "=========================================="
echo "  构建部署完成！"
echo "  访问地址: http://your-domain/scratch-jr/"
echo "  重载 Nginx: sudo nginx -s reload"
echo "=========================================="
