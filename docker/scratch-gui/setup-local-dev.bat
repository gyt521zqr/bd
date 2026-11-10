@echo off
chcp 65001 >nul
REM ============================================================================
REM Scratch GUI 本地开发环境快速设置脚本（Windows）
REM ============================================================================
REM 功能：
REM   1. 检查本地开发环境依赖
REM   2. 创建本地模拟的 scratch-gui 目录结构
REM   3. 启动前端开发服务器
REM ============================================================================

echo ==========================================
echo   Scratch GUI 本地开发环境设置
echo ==========================================
echo.

REM 检查 Node.js
echo [1/4] 检查 Node.js...
node -v >nul 2>&1
if errorlevel 1 (
    echo [错误] Node.js 未安装，请先安装 Node.js 18+
    exit /b 1
)
echo   Node.js 版本: 
for /f "tokens=*" %%a in ('node -v') do echo   %%a

REM 检查 npm
echo [2/4] 检查 npm...
npm -v >nul 2>&1
if errorlevel 1 (
    echo [错误] npm 未安装
    exit /b 1
)
echo   npm 版本:
for /f "tokens=*" %%a in ('npm -v') do echo   %%a

REM 创建本地模拟目录
echo [3/4] 创建本地模拟目录...
set "PROJECT_DIR=%~dp0..\.."
set "MOCK_DIR=%PROJECT_DIR%\frontend\public\scratch-gui"

if not exist "%MOCK_DIR%" mkdir "%MOCK_DIR%"
echo   模拟目录: %MOCK_DIR%

REM 检查 mock index.html 是否存在
if exist "%MOCK_DIR%\index.html" (
    echo   [OK] mock index.html 已存在
) else (
    echo   [警告] mock index.html 不存在，请确认前端 public 目录结构正确
)

REM 安装前端依赖并启动
echo [4/4] 启动前端开发服务器...
echo.
cd /d "%PROJECT_DIR%\frontend"

echo 正在安装依赖（如已安装会自动跳过）...
call npm install

echo.
echo ==========================================
echo   启动开发服务器...
echo   访问地址: http://localhost:3000
echo   练习页面: http://localhost:3000/practice
echo ==========================================
echo.

npm run dev
