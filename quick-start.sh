#!/bin/bash
# ============================================================
#  Magao Platform - Quick Start (No Compilation Needed)
#  Uses pre-compiled jar - starts in 30 seconds
# ============================================================

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo ""
echo -e "${BLUE}============================================================${NC}"
echo -e "${BLUE}   Magao Platform - Quick Start${NC}"
echo -e "${BLUE}============================================================${NC}"
echo ""

# ========================================
# STEP 1: Check prerequisites
# ========================================
echo -e "${YELLOW}[1/5] Checking prerequisites...${NC}"

# Check Java
if ! command -v java &> /dev/null; then
    echo -e "${RED}  Java not found! Please install Java 17:${NC}"
    echo -e "${YELLOW}  brew install openjdk@17${NC}"
    exit 1
fi
JAVA_VER=$(java -version 2>&1 | head -1)
echo -e "  Java: $JAVA_VER"

# Set JAVA_HOME if not set
if [ -z "$JAVA_HOME" ]; then
    export JAVA_HOME=$(/usr/libexec/java_home 2>/dev/null || echo "")
    if [ -z "$JAVA_HOME" ]; then
        if [[ $(uname -m) == "arm64" ]]; then
            export JAVA_HOME="/opt/homebrew/opt/openjdk@17"
        else
            export JAVA_HOME="/usr/local/opt/openjdk@17"
        fi
        export PATH="$JAVA_HOME/bin:$PATH"
    fi
fi

# Check MySQL
if ! command -v mysql &> /dev/null; then
    echo -e "${RED}  MySQL not found! Please install:${NC}"
    echo -e "${YELLOW}  brew install mysql${NC}"
    exit 1
fi

# Start MySQL if needed
if ! mysql -u root -proot123456 -e "SELECT 1" &>/dev/null; then
    echo -e "${YELLOW}  Starting MySQL...${NC}"
    brew services start mysql 2>/dev/null || mysql.server start 2>/dev/null || true
    sleep 3
    # Set password if connecting without password works
    if mysql -u root -e "SELECT 1" &>/dev/null; then
        mysql -u root -e "ALTER USER 'root'@'localhost' IDENTIFIED BY 'root123456'; FLUSH PRIVILEGES;" 2>/dev/null || true
        sleep 1
    fi
fi

if mysql -u root -proot123456 -e "SELECT 1" &>/dev/null; then
    echo -e "${GREEN}  MySQL: OK${NC}"
else
    echo -e "${RED}  Cannot connect to MySQL. Please set root password:${NC}"
    echo -e "${YELLOW}  mysql -u root -e \"ALTER USER 'root'@'localhost' IDENTIFIED BY 'root123456';\"${NC}"
    exit 1
fi

# Check Redis
if ! command -v redis-cli &> /dev/null; then
    echo -e "${RED}  Redis not found! Please install:${NC}"
    echo -e "${YELLOW}  brew install redis${NC}"
    exit 1
fi

if ! redis-cli ping &>/dev/null; then
    echo -e "${YELLOW}  Starting Redis...${NC}"
    brew services start redis 2>/dev/null || redis-server --daemonize yes 2>/dev/null || true
    sleep 2
fi
echo -e "${GREEN}  Redis: $(redis-cli ping 2>/dev/null || echo 'starting...')${NC}"

# Check Node.js
if ! command -v node &> /dev/null; then
    echo -e "${RED}  Node.js not found! Please install:${NC}"
    echo -e "${YELLOW}  brew install node@22${NC}"
    exit 1
fi
echo -e "  Node.js: $(node -v)"
echo ""

# ========================================
# STEP 2: Initialize database
# ========================================
echo -e "${YELLOW}[2/5] Initializing database...${NC}"

mysql -u root -proot123456 -e "CREATE DATABASE IF NOT EXISTS magao_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>/dev/null
echo -e "${GREEN}  Database 'magao_platform' ready${NC}"

# Import schema if tables don't exist
TABLE_COUNT=$(mysql -u root -proot123456 magao_platform -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='magao_platform';" -s 2>/dev/null || echo "0")
if [ "$TABLE_COUNT" -lt 5 ]; then
    echo -e "${YELLOW}  Importing schema...${NC}"
    if [ -f "$SCRIPT_DIR/schema_init.sql" ]; then
        mysql -u root -proot123456 magao_platform < "$SCRIPT_DIR/schema_init.sql" 2>/dev/null
        echo -e "${GREEN}  Schema imported${NC}"
    fi
    # Import additional SQL
    for sql_file in "$SCRIPT_DIR/backend/src/main/resources/db/"*.sql; do
        [ -f "$sql_file" ] && mysql -u root -proot123456 magao_platform < "$sql_file" 2>/dev/null || true
    done
else
    echo -e "${GREEN}  Database already has $TABLE_COUNT tables${NC}"
fi
echo ""

# ========================================
# STEP 3: Start backend (pre-compiled jar)
# ========================================
echo -e "${YELLOW}[3/5] Starting backend...${NC}"

# Find the jar file
JAR_FILE=""
for f in "$SCRIPT_DIR/backend/target/magao-backend-"*.jar; do
    if [ -f "$f" ] && [[ ! "$f" == *.original ]]; then
        JAR_FILE="$f"
        break
    fi
done

if [ -z "$JAR_FILE" ]; then
    echo -e "${RED}  Backend jar not found!${NC}"
    echo -e "${YELLOW}  Please run mac-setup.sh first to build the backend${NC}"
    exit 1
fi

# Kill existing process on port 8080
lsof -ti:8080 | xargs kill -9 2>/dev/null || true
sleep 1

# Start backend
export SPRING_PROFILES_ACTIVE=dev
export JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"
nohup java -Dfile.encoding=UTF-8 -jar "$JAR_FILE" > "$SCRIPT_DIR/backend/backend.log" 2>&1 &
BACKEND_PID=$!

echo -e "${YELLOW}  Backend starting (PID: $BACKEND_PID)...${NC}"

# Wait for backend to be ready
for i in $(seq 1 30); do
    if curl -s http://localhost:8080/api/health &>/dev/null || curl -s -o /dev/null -w "%{http_code}" http://localhost:8080 2>/dev/null | grep -q "200\|302\|404"; then
        echo -e "${GREEN}  Backend ready at http://localhost:8080${NC}"
        break
    fi
    if [ $i -eq 30 ]; then
        echo -e "${YELLOW}  Backend still starting. Check backend/backend.log${NC}"
    fi
    sleep 2
done
echo ""

# ========================================
# STEP 4: Start frontend
# ========================================
echo -e "${YELLOW}[4/5] Starting frontend...${NC}"

FRONTEND_DIR="$SCRIPT_DIR/frontend"
cd "$FRONTEND_DIR"

# Kill existing process on port 5173
lsof -ti:5173 | xargs kill -9 2>/dev/null || true
sleep 1

# Install deps if needed
if [ ! -d "node_modules" ]; then
    echo -e "${YELLOW}  Installing frontend dependencies...${NC}"
    npm install 2>&1 | tail -5
fi

# Start frontend
nohup npm run dev > "$SCRIPT_DIR/frontend/frontend.log" 2>&1 &
FRONTEND_PID=$!

echo -e "${YELLOW}  Frontend starting (PID: $FRONTEND_PID)...${NC}"
sleep 3
echo -e "${GREEN}  Frontend ready${NC}"
echo ""

# ========================================
# STEP 5: Done!
# ========================================
echo -e "${BLUE}============================================================${NC}"
echo -e "${GREEN}   Platform is running!${NC}"
echo -e "${BLUE}============================================================${NC}"
echo ""
echo -e "  Website:    ${GREEN}http://localhost:5173${NC}"
echo -e "  Backend:    ${GREEN}http://localhost:8080${NC}"
echo -e "  API Docs:   ${GREEN}http://localhost:8080/swagger-ui.html${NC}"
echo ""
echo -e "  Logs:"
echo -e "    Backend:  $SCRIPT_DIR/backend/backend.log"
echo -e "    Frontend: $SCRIPT_DIR/frontend/frontend.log"
echo ""
echo -e "  Stop all:   ${YELLOW}lsof -ti:8080,5173 | xargs kill${NC}"
echo ""
echo -e "${BLUE}============================================================${NC}"
