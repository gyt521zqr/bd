#!/bin/bash
# ============================================
# 码高教育平台 - 一键启动脚本
# ============================================
# 用法:
#   ./start.sh          # 启动所有服务
#   ./start.sh --build  # 先构建再启动
#   ./start.sh --stop   # 停止所有服务
#   ./start.sh --status # 查看服务状态
# ============================================

set -e

PROJECT_ROOT="$(cd "$(dirname "$0")" && pwd)"
FRONTEND_DIR="$PROJECT_ROOT/frontend"
BACKEND_DIR="$PROJECT_ROOT/backend"
BACKEND_JAR="$BACKEND_DIR/target/magao-backend-1.0.0.jar"
LOG_DIR="/tmp/magao-logs"

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

log_info()  { echo -e "${GREEN}[INFO]${NC} $1"; }
log_warn()  { echo -e "${YELLOW}[WARN]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# 检查服务状态
check_services() {
    echo "==================================="
    echo "  服务状态检查"
    echo "==================================="
    
    local all_ok=true
    
    # MySQL
    if mysqladmin ping -u root -proot123456 --silent 2>/dev/null; then
        log_info "MySQL     : 运行中 (3306)"
    else
        log_error "MySQL     : 未运行"
        all_ok=false
    fi
    
    # Redis
    if redis-cli ping 2>/dev/null | grep -q PONG; then
        log_info "Redis     : 运行中 (6379)"
    else
        log_error "Redis     : 未运行"
        all_ok=false
    fi
    
    # RabbitMQ
    if rabbitmqctl status 2>/dev/null | grep -q "Status of node"; then
        log_info "RabbitMQ  : 运行中 (5672)"
    else
        log_error "RabbitMQ  : 未运行"
        all_ok=false
    fi
    
    # Backend
    if curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/ 2>/dev/null | grep -q 200; then
        log_info "后端API   : 运行中 (8080)"
    else
        log_error "后端API   : 未运行"
        all_ok=false
    fi
    
    echo "==================================="
    if $all_ok; then
        log_info "所有服务运行正常"
    else
        log_warn "部分服务未启动"
    fi
}

# 构建前端并同步到后端
build_frontend() {
    log_info "开始构建前端..."
    cd "$FRONTEND_DIR"
    NODE_OPTIONS="--max-old-space-size=2048" npx vite build --minify esbuild
    log_info "同步静态文件到后端..."
    rm -rf "$BACKEND_DIR/src/main/resources/static/"*
    cp -r dist/* "$BACKEND_DIR/src/main/resources/static/"
    log_info "前端构建完成"
}

# 构建后端JAR
build_backend() {
    log_info "开始构建后端JAR..."
    cd "$BACKEND_DIR"
    mvn package -DskipTests -q
    log_info "后端构建完成: $BACKEND_JAR"
}

# 全量构建
full_build() {
    build_frontend
    build_backend
}

# 停止服务
stop_services() {
    log_info "停止所有服务..."
    # 停止Python预览服务器
    pkill -f "preview_server.py" 2>/dev/null || true
    # 停止Vite
    pkill -f "npx vite" 2>/dev/null || true
    # 停止Java后端
    pkill -f "magao-backend" 2>/dev/null || true
    sleep 2
    log_info "所有服务已停止"
}

# 启动后端（唯一的服务入口）
start_backend() {
    # 如果JAR不存在，先构建
    if [ ! -f "$BACKEND_JAR" ]; then
        log_warn "JAR文件不存在，开始构建..."
        build_backend
    fi
    
    # 检查是否已在运行
    if curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/ 2>/dev/null | grep -q 200; then
        log_info "后端已在运行中"
        return
    fi
    
    mkdir -p "$LOG_DIR"
    log_info "启动后端服务 (8080)..."
    cd "$BACKEND_DIR"
    nohup java -jar "$BACKEND_JAR" --spring.profiles.active=dev \
        > "$LOG_DIR/backend.log" 2>&1 &
    
    # 等待启动
    for i in $(seq 1 30); do
        if curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/ 2>/dev/null | grep -q 200; then
            log_info "后端启动成功 (${i}s)"
            return
        fi
        sleep 1
    done
    log_error "后端启动超时，查看日志: $LOG_DIR/backend.log"
    tail -20 "$LOG_DIR/backend.log"
    exit 1
}

# 启动Vite开发服务器（可选，用于前端热更新开发）
start_vite() {
    if lsof -i:5173 >/dev/null 2>&1; then
        log_info "Vite已在运行中"
        return
    fi
    
    log_info "启动Vite开发服务器 (5173)..."
    cd "$FRONTEND_DIR"
    nohup npx vite --host 0.0.0.0 --port 5173 \
        > "$LOG_DIR/vite.log" 2>&1 &
    
    sleep 3
    if curl -s -o /dev/null -w "%{http_code}" http://localhost:5173/ 2>/dev/null | grep -q 200; then
        log_info "Vite启动成功"
    else
        log_error "Vite启动失败，查看日志: $LOG_DIR/vite.log"
    fi
}

# 主入口
case "${1:-}" in
    --build)
        full_build
        start_backend
        ;;
    --stop)
        stop_services
        ;;
    --status)
        check_services
        ;;
    --vite)
        start_vite
        log_info "Vite开发服务器: http://localhost:5173"
        log_info "API代理目标:    http://localhost:8080"
        ;;
    --frontend)
        build_frontend
        build_backend
        ;;
    "")
        start_backend
        log_info ""
        log_info "========================================="
        log_info "  码高教育平台已启动"
        log_info "  地址: http://localhost:8080"
        log_info "  账号: admin / admin123"
        log_info "========================================="
        ;;
    *)
        echo "用法: $0 [--build|--stop|--status|--vite|--frontend]"
        echo ""
        echo "  (无参数)    启动后端服务"
        echo "  --build     先构建前后端再启动"
        echo "  --stop      停止所有服务"
        echo "  --status    检查服务状态"
        echo "  --vite      启动Vite开发服务器（热更新）"
        echo "  --frontend  仅构建前后端（不启动）"
        exit 1
        ;;
esac
