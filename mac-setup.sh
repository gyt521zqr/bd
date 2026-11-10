#!/bin/bash
# ============================================================
#  Magao Platform - Mac One-Click Setup & Run Script
#  This script does EVERYTHING: check env, init DB, build, run
# ============================================================

set -e

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# Get the directory where this script is located
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

echo ""
echo -e "${BLUE}============================================================${NC}"
echo -e "${BLUE}   Magao Platform - Mac Setup & Run${NC}"
echo -e "${BLUE}============================================================${NC}"
echo ""

# ========================================
# STEP 1: Check prerequisites
# ========================================
echo -e "${YELLOW}[1/6] Checking prerequisites...${NC}"

# Check Java 17
if ! command -v java &> /dev/null; then
    echo -e "${RED}  Java not found. Installing Java 17...${NC}"
    brew install openjdk@17
    sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk 2>/dev/null || \
    sudo ln -sfn /usr/local/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk 2>/dev/null || true
fi

JAVA_VERSION=$(java -version 2>&1 | head -1)
echo -e "  Java: $JAVA_VERSION"

if echo "$JAVA_VERSION" | grep -qE "version \"1[789]|version \"[2-9][0-9]"; then
    echo -e "${GREEN}  Java version OK${NC}"
else
    echo -e "${RED}  Java 17+ required. Current: $JAVA_VERSION${NC}"
    echo -e "${YELLOW}  Installing Java 17...${NC}"
    brew install openjdk@17
    if [[ $(uname -m) == "arm64" ]]; then
        export JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null || echo "/opt/homebrew/opt/openjdk@17")
    else
        export JAVA_HOME=$(/usr/libexec/java_home -v 17 2>/dev/null || echo "/usr/local/opt/openjdk@17")
    fi
    export PATH="$JAVA_HOME/bin:$PATH"
    echo -e "${GREEN}  Java 17 configured for this session${NC}"
fi

# Set JAVA_HOME
export JAVA_HOME=$(/usr/libexec/java_home 2>/dev/null || echo "")
if [ -z "$JAVA_HOME" ]; then
    # Fallback for Homebrew Java
    if [[ $(uname -m) == "arm64" ]]; then
        export JAVA_HOME="/opt/homebrew/opt/openjdk@17"
    else
        export JAVA_HOME="/usr/local/opt/openjdk@17"
    fi
    export PATH="$JAVA_HOME/bin:$PATH"
fi

# CRITICAL: Force UTF-8 encoding to prevent compilation errors
export JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8"
export LANG=en_US.UTF-8
export LC_ALL=en_US.UTF-8

# Check Maven
if ! command -v mvn &> /dev/null; then
    echo -e "${YELLOW}  Maven not found. Installing...${NC}"
    brew install maven
fi
echo -e "  Maven: $(mvn -v 2>&1 | head -1)"

# Check MySQL
if ! command -v mysql &> /dev/null; then
    echo -e "${RED}  MySQL not found. Installing...${NC}"
    brew install mysql
    brew services start mysql
    sleep 3
    mysql -u root -e "ALTER USER 'root'@'localhost' IDENTIFIED BY 'root123456'; FLUSH PRIVILEGES;" 2>/dev/null || true
fi

# Start MySQL if not running
if ! mysql -u root -proot123456 -e "SELECT 1" &>/dev/null; then
    echo -e "${YELLOW}  Starting MySQL...${NC}"
    brew services start mysql 2>/dev/null || mysql.server start 2>/dev/null || true
    sleep 3
    # Try without password first, then set password
    if mysql -u root -e "SELECT 1" &>/dev/null; then
        mysql -u root -e "ALTER USER 'root'@'localhost' IDENTIFIED BY 'root123456'; FLUSH PRIVILEGES;" 2>/dev/null || true
    fi
fi

if mysql -u root -proot123456 -e "SELECT 1" &>/dev/null; then
    echo -e "${GREEN}  MySQL: Running (root/root123456)${NC}"
else
    echo -e "${RED}  MySQL: Cannot connect. Please set root password to 'root123456'${NC}"
    echo -e "${YELLOW}  Run: mysql -u root -e \"ALTER USER 'root'@'localhost' IDENTIFIED BY 'root123456';\"${NC}"
    exit 1
fi

# Check Redis
if ! command -v redis-cli &> /dev/null; then
    echo -e "${YELLOW}  Redis not found. Installing...${NC}"
    brew install redis
fi

if ! redis-cli ping &>/dev/null; then
    echo -e "${YELLOW}  Starting Redis...${NC}"
    brew services start redis 2>/dev/null || redis-server --daemonize yes 2>/dev/null || true
    sleep 2
fi

if redis-cli ping &>/dev/null; then
    echo -e "${GREEN}  Redis: Running${NC}"
else
    echo -e "${RED}  Redis: Cannot start${NC}"
    exit 1
fi

# Check Node.js
if ! command -v node &> /dev/null; then
    echo -e "${YELLOW}  Node.js not found. Installing...${NC}"
    brew install node@22
    brew link --overwrite node@22
fi
echo -e "  Node.js: $(node -v)"
echo ""

# ========================================
# STEP 2: Configure Maven UTF-8 encoding
# ========================================
echo -e "${YELLOW}[2/6] Configuring Maven UTF-8 encoding...${NC}"

MAVEN_SETTINGS="$HOME/.m2/settings.xml"
mkdir -p "$HOME/.m2"

# Always overwrite settings.xml with Aliyun mirror + UTF-8
cat > "$MAVEN_SETTINGS" << 'MAVEN_EOF'
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
          https://maven.apache.org/xsd/settings-1.0.0.xsd">
  <mirrors>
    <mirror>
      <id>aliyunmaven</id>
      <mirrorOf>*</mirrorOf>
      <name>Aliyun Maven Mirror</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>
  <profiles>
    <profile>
      <id>utf8</id>
      <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
        <maven.compiler.source>17</maven.compiler.source>
        <maven.compiler.target>17</maven.compiler.target>
        <maven.compiler.encoding>UTF-8</maven.compiler.encoding>
      </properties>
    </profile>
  </profiles>
  <activeProfiles>
    <activeProfile>utf8</activeProfile>
  </activeProfiles>
</settings>
MAVEN_EOF

echo -e "${GREEN}  Maven settings.xml configured (UTF-8 + Aliyun mirror)${NC}"
echo ""

# ========================================
# STEP 3: Initialize database
# ========================================
echo -e "${YELLOW}[3/6] Initializing database...${NC}"

# Create database
mysql -u root -proot123456 -e "CREATE DATABASE IF NOT EXISTS magao_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>/dev/null
echo -e "${GREEN}  Database 'magao_platform' created${NC}"

# Import schema
if [ -f "$SCRIPT_DIR/schema_init.sql" ]; then
    mysql -u root -proot123456 magao_platform < "$SCRIPT_DIR/schema_init.sql" 2>/dev/null
    echo -e "${GREEN}  Schema imported from schema_init.sql${NC}"
elif [ -f "$SCRIPT_DIR/backend/schema_init.sql" ]; then
    mysql -u root -proot123456 magao_platform < "$SCRIPT_DIR/backend/schema_init.sql" 2>/dev/null
    echo -e "${GREEN}  Schema imported from backend/schema_init.sql${NC}"
else
    echo -e "${YELLOW}  schema_init.sql not found, skipping schema import${NC}"
fi

# Import additional SQL files
for sql_file in "$SCRIPT_DIR/backend/src/main/resources/db/"*.sql; do
    if [ -f "$sql_file" ]; then
        echo -e "${YELLOW}  Importing $(basename "$sql_file")...${NC}"
        mysql -u root -proot123456 magao_platform < "$sql_file" 2>/dev/null || true
    fi
done
echo ""

# ========================================
# STEP 4: Build backend
# ========================================
echo -e "${YELLOW}[4/6] Building backend...${NC}"
echo -e "${YELLOW}  (This may take 3-5 minutes on first run, downloading dependencies)${NC}"

cd "$SCRIPT_DIR/backend"

# Clean and compile with explicit UTF-8 encoding
mvn clean compile -Dfile.encoding=UTF-8 -Dmaven.compiler.source=17 -Dmaven.compiler.target=17 -Dmaven.compiler.encoding=UTF-8 2>&1 | tail -30

if [ ${PIPESTATUS[0]} -ne 0 ]; then
    echo -e "${RED}  Backend compilation failed!${NC}"
    echo -e "${YELLOW}  Trying to identify the error...${NC}"
    
    # Run compile again showing full error output
    mvn compile -Dfile.encoding=UTF-8 2>&1 | grep -E "ERROR|error:|illegal" | head -20
    
    echo ""
    echo -e "${YELLOW}  If the error is about 'illegal character', run:${NC}"
    echo -e "${YELLOW}  export JAVA_TOOL_OPTIONS='-Dfile.encoding=UTF-8'${NC}"
    echo -e "${YELLOW}  cd $SCRIPT_DIR/backend && mvn clean compile -Dfile.encoding=UTF-8${NC}"
    exit 1
fi

echo -e "${GREEN}  Backend compiled successfully!${NC}"

# Package the backend
mvn package -DskipTests -Dfile.encoding=UTF-8 -q 2>&1 | tail -5
echo -e "${GREEN}  Backend packaged successfully!${NC}"
echo ""

# ========================================
# STEP 5: Start backend
# ========================================
echo -e "${YELLOW}[5/6] Starting backend (port 8080)...${NC}"

# Kill any existing process on port 8080
lsof -ti:8080 | xargs kill -9 2>/dev/null || true
sleep 1

# Start backend in background
export SPRING_PROFILES_ACTIVE=dev
nohup java -Dfile.encoding=UTF-8 -jar target/magao-backend-*.jar > backend.log 2>&1 &
BACKEND_PID=$!

echo -e "${YELLOW}  Backend starting (PID: $BACKEND_PID)...${NC}"
echo -e "${YELLOW}  Waiting for backend to be ready...${NC}"

# Wait for backend to start (max 60 seconds)
for i in $(seq 1 30); do
    if curl -s http://localhost:8080/api/health &>/dev/null || curl -s http://localhost:8080 &>/dev/null; then
        echo -e "${GREEN}  Backend is running at http://localhost:8080${NC}"
        break
    fi
    if [ $i -eq 30 ]; then
        echo -e "${YELLOW}  Backend still starting... Check backend.log for details${NC}"
    fi
    sleep 2
done
echo ""

# ========================================
# STEP 6: Start frontend
# ========================================
echo -e "${YELLOW}[6/6] Starting frontend (port 5173)...${NC}"

FRONTEND_DIR="$SCRIPT_DIR/frontend"
if [ ! -d "$FRONTEND_DIR" ]; then
    FRONTEND_DIR="$SCRIPT_DIR"
fi

cd "$FRONTEND_DIR"

# Kill any existing process on port 5173
lsof -ti:5173 | xargs kill -9 2>/dev/null || true
sleep 1

# Install dependencies if needed
if [ ! -d "node_modules" ]; then
    echo -e "${YELLOW}  Installing frontend dependencies...${NC}"
    npm install 2>&1 | tail -5
fi

# Start frontend
nohup npm run dev > frontend.log 2>&1 &
FRONTEND_PID=$!

echo -e "${YELLOW}  Frontend starting (PID: $FRONTEND_PID)...${NC}"
sleep 3

echo ""
echo -e "${BLUE}============================================================${NC}"
echo -e "${GREEN}   Setup Complete!${NC}"
echo -e "${BLUE}============================================================${NC}"
echo ""
echo -e "  Frontend:  ${GREEN}http://localhost:5173${NC}"
echo -e "  Backend:   ${GREEN}http://localhost:8080${NC}"
echo -e "  API Docs:  ${GREEN}http://localhost:8080/swagger-ui.html${NC}"
echo ""
echo -e "  Backend log:   $SCRIPT_DIR/backend/backend.log"
echo -e "  Frontend log:  $FRONTEND_DIR/frontend.log"
echo ""
echo -e "${YELLOW}  To stop: kill $BACKEND_PID $FRONTEND_PID${NC}"
echo -e "${YELLOW}  Or: lsof -ti:8080,5173 | xargs kill${NC}"
echo ""
echo -e "${BLUE}============================================================${NC}"
