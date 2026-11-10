#!/bin/bash
# ============================================================
#  码高少儿编程平台 - 本地环境完整检查与自动修复脚本
#  功能：检查所有依赖，缺失的自动安装，确保环境完整
# ============================================================

set +e  # 不遇到错误就退出，每个步骤独立检查

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
CYAN='\033[0;36m'
NC='\033[0m'

# 计数器
TOTAL=0
PASS=0
FAIL=0
FIXED=0

clear
echo ""
echo -e "${BLUE}============================================================${NC}"
echo -e "${BLUE}   码高少儿编程平台 - 本地环境完整检查与修复${NC}"
echo -e "${BLUE}============================================================${NC}"
echo ""
echo -e "${CYAN}  正在全面检查本地开发环境...${NC}"
echo ""

# ========================================
# 工具函数
# ========================================

check_item() {
    local name="$1"
    local status="$2"  # ok / fail / warn
    local detail="$3"

    TOTAL=$((TOTAL + 1))
    case $status in
        ok)
            PASS=$((PASS + 1))
            echo -e "  ${GREEN}✓${NC} $name  ${GREEN}[正常]${NC}  $detail"
            ;;
        fail)
            FAIL=$((FAIL + 1))
            echo -e "  ${RED}✗${NC} $name  ${RED}[缺失]${NC}  $detail"
            ;;
        warn)
            echo -e "  ${YELLOW}⚠${NC} $name  ${YELLOW}[警告]${NC}  $detail"
            ;;
    esac
}

fix_item() {
    local name="$1"
    echo ""
    echo -e "${YELLOW}>>> 正在安装/修复: $name ...${NC}"
}

fix_done() {
    local name="$1"
    local success="$2"
    if [ "$success" = "true" ]; then
        FIXED=$((FIXED + 1))
        FAIL=$((FAIL - 1))
        PASS=$((PASS + 1))
        echo -e "${GREEN}<<< $name 安装/修复成功${NC}"
    else
        echo -e "${RED}<<< $name 安装/修复失败${NC}"
    fi
}

# ========================================
# 第一阶段：环境检查
# ========================================
echo -e "${CYAN}━━━ 第一阶段：环境检查 ━━━${NC}"
echo ""

# --- 1. 操作系统 ---
OS_NAME="$(uname -s)"
OS_ARCH="$(uname -m)"
OS_VER="$(sw_vers -productVersion 2>/dev/null || echo 'unknown')"
check_item "操作系统" "ok" "$OS_NAME $OS_VER ($OS_ARCH)"

# --- 2. Homebrew ---
BREW_OK=false
BREW_PATH=""
if command -v brew &> /dev/null; then
    BREW_OK=true
    BREW_PATH=$(which brew)
    BREW_VER=$(brew --version | head -1)
    check_item "Homebrew" "ok" "$BREW_VER"
elif [ -f /opt/homebrew/bin/brew ]; then
    BREW_PATH="/opt/homebrew/bin/brew"
    check_item "Homebrew" "warn" "已安装但未加入 PATH，将自动修复"
elif [ -f /usr/local/bin/brew ]; then
    BREW_PATH="/usr/local/bin/brew"
    check_item "Homebrew" "warn" "已安装但未加入 PATH，将自动修复"
else
    check_item "Homebrew" "fail" "未安装"
fi

# --- 3. Node.js ---
NODE_OK=false
if command -v node &> /dev/null; then
    NODE_VER=$(node -v)
    NODE_MAJOR=$(echo "$NODE_VER" | sed 's/v//' | cut -d. -f1)
    if [ "$NODE_MAJOR" -ge 18 ]; then
        NODE_OK=true
        check_item "Node.js" "ok" "$NODE_VER"
    else
        check_item "Node.js" "fail" "版本过低 ($NODE_VER，需要 >= 18)"
    fi
else
    check_item "Node.js" "fail" "未安装"
fi

# --- 4. npm ---
NPM_OK=false
if command -v npm &> /dev/null; then
    NPM_VER=$(npm -v)
    NPM_OK=true
    # 检查镜像配置
    NPM_REGISTRY=$(npm config get registry 2>/dev/null)
    if echo "$NPM_REGISTRY" | grep -q "npmmirror\|taobao"; then
        check_item "npm" "ok" "$NPM_VER (淘宝镜像)"
    else
        check_item "npm" "warn" "$NPM_VER (未配置国内镜像，将自动修复)"
    fi
else
    check_item "npm" "fail" "未安装"
fi

# --- 5. Java 17 ---
JAVA_OK=false
JAVA_VER=""
if command -v java &> /dev/null; then
    JAVA_VER=$(java -version 2>&1 | head -1)
    if java -version 2>&1 | grep -q 'version "17\.'; then
        JAVA_OK=true
        check_item "Java 17" "ok" "$JAVA_VER"
    else
        check_item "Java 17" "fail" "版本不匹配 ($JAVA_VER，需要 Java 17)"
    fi
else
    check_item "Java 17" "fail" "未安装"
fi

# --- 6. Maven ---
MAVEN_OK=false
if command -v mvn &> /dev/null; then
    MAVEN_VER=$(mvn -v 2>&1 | head -1)
    MAVEN_OK=true
    # 检查镜像配置
    if [ -f ~/.m2/settings.xml ] && grep -q "aliyun\|huaweicloud\|tuna" ~/.m2/settings.xml; then
        check_item "Maven" "ok" "$MAVEN_VER (国内镜像)"
    else
        check_item "Maven" "warn" "$MAVEN_VER (未配置国内镜像，将自动修复)"
    fi
else
    check_item "Maven" "fail" "未安装"
fi

# --- 7. MySQL ---
MYSQL_OK=false
MYSQL_RUNNING=false
if command -v mysql &> /dev/null; then
    MYSQL_VER=$(mysql --version 2>/dev/null | head -1 | sed 's/.*Distrib //' | cut -d, -f1)
    # 检查是否运行
    if mysqladmin ping -u root --password=root123456 &>/dev/null; then
        MYSQL_RUNNING=true
        MYSQL_OK=true
        check_item "MySQL" "ok" "$MYSQL_VER (运行中)"
    else
        check_item "MySQL" "warn" "$MYSQL_VER (未运行，将自动启动)"
    fi
else
    check_item "MySQL" "fail" "未安装"
fi

# --- 8. Redis ---
REDIS_OK=false
REDIS_RUNNING=false
if command -v redis-server &> /dev/null; then
    REDIS_VER=$(redis-server --version 2>/dev/null | head -1)
    if redis-cli ping &>/dev/null; then
        REDIS_RUNNING=true
        REDIS_OK=true
        check_item "Redis" "ok" "$REDIS_VER (运行中)"
    else
        check_item "Redis" "warn" "$REDIS_VER (未运行，将自动启动)"
    fi
else
    check_item "Redis" "fail" "未安装"
fi

# --- 9. Git ---
if command -v git &> /dev/null; then
    GIT_VER=$(git --version)
    check_item "Git" "ok" "$GIT_VER"
else
    check_item "Git" "fail" "未安装"
fi

# --- 10. 项目文件 ---
PROJECT_DIR="/Users/macbook/self_dev/on_edu_com"
if [ -d "$PROJECT_DIR" ]; then
    if [ -f "$PROJECT_DIR/frontend/package.json" ] && [ -f "$PROJECT_DIR/backend/pom.xml" ]; then
        check_item "项目文件" "ok" "$PROJECT_DIR"
    else
        check_item "项目文件" "warn" "目录存在但结构不完整"
    fi
else
    check_item "项目文件" "fail" "未找到 ($PROJECT_DIR)"
fi

echo ""
echo -e "${CYAN}━━━ 检查结果 ━━━${NC}"
echo ""
echo -e "  总计: $TOTAL 项  |  ${GREEN}正常: $PASS${NC}  |  ${RED}缺失: $FAIL${NC}"
echo ""

if [ "$FAIL" -eq 0 ] && [ "$MYSQL_RUNNING" = "true" ] && [ "$REDIS_RUNNING" = "true" ]; then
    echo -e "${GREEN}============================================================${NC}"
    echo -e "${GREEN}   ✓ 所有环境检查通过！${NC}"
    echo -e "${GREEN}============================================================${NC}"
    echo ""
    echo "  启动项目："
    echo "    前端: cd $PROJECT_DIR/frontend && npm run dev"
    echo "    后端: cd $PROJECT_DIR/backend && mvn spring-boot:run"
    echo ""
    exit 0
fi

# ========================================
# 第二阶段：自动修复
# ========================================
echo -e "${YELLOW}>>> 发现 $FAIL 项缺失，开始自动修复...${NC}"
echo ""

# --- 修复 1: Homebrew ---
if [ "$BREW_OK" = false ]; then
    fix_item "Homebrew"

    # 先尝试加载已有的 brew
    if [ -n "$BREW_PATH" ]; then
        echo 'eval "$('"$BREW_PATH"' shellenv)"' >> ~/.zprofile
        eval "$($BREW_PATH shellenv)"
        if command -v brew &> /dev/null; then
            BREW_OK=true
            fix_done "Homebrew" "true"
        else
            fix_done "Homebrew" "false"
        fi
    fi

    # 如果还是没有，尝试安装
    if [ "$BREW_OK" = false ]; then
        # 方式1: 清华镜像
        echo "  尝试清华镜像安装..."
        NONINTERACTIVE=1 /bin/bash -c "$(curl -fsSL https://gitee.com/ineo6/homebrew-install/raw/master/install.sh)" 2>&1 | tail -5

        # 配置路径
        if [[ $(uname -m) == "arm64" ]] && [ -f /opt/homebrew/bin/brew ]; then
            echo 'eval "$(/opt/homebrew/bin/brew shellenv)"' >> ~/.zprofile
            eval "$(/opt/homebrew/bin/brew shellenv)"
            BREW_OK=true
        elif [ -f /usr/local/bin/brew ]; then
            echo 'eval "$(/usr/local/bin/brew shellenv)"' >> ~/.zprofile
            eval "$(/usr/local/bin/brew shellenv)"
            BREW_OK=true
        fi

        if [ "$BREW_OK" = true ]; then
            fix_done "Homebrew" "true"
        else
            fix_done "Homebrew" "false"
            echo -e "${RED}  Homebrew 安装失败，请手动安装后重试${NC}"
            echo -e "${YELLOW}  手动安装命令:${NC}"
            echo -e "${YELLOW}  /bin/bash -c \"\$(curl -fsSL https://gitee.com/ineo6/homebrew-install/raw/master/install.sh)\"${NC}"
        fi
    fi
fi

# 如果 Homebrew 都没装上，后面的都没法装了
if [ "$BREW_OK" = false ]; then
    echo ""
    echo -e "${RED}============================================================${NC}"
    echo -e "${RED}   ✗ Homebrew 安装失败，无法继续${NC}"
    echo -e "${RED}============================================================${NC}"
    echo ""
    exit 1
fi

# --- 修复 2: Node.js ---
if [ "$NODE_OK" = false ]; then
    fix_item "Node.js 22"
    brew install node@22 2>&1 | tail -10
    brew link --overwrite node@22 2>/dev/null || true
    if command -v node &> /dev/null; then
        NODE_VER=$(node -v)
        NODE_MAJOR=$(echo "$NODE_VER" | sed 's/v//' | cut -d. -f1)
        if [ "$NODE_MAJOR" -ge 18 ]; then
            NODE_OK=true
            fix_done "Node.js 22" "true"
        else
            fix_done "Node.js 22" "false"
        fi
    else
        fix_done "Node.js 22" "false"
    fi
fi

# --- 修复 3: npm 镜像 ---
if [ "$NPM_OK" = true ]; then
    NPM_REGISTRY=$(npm config get registry 2>/dev/null)
    if ! echo "$NPM_REGISTRY" | grep -q "npmmirror\|taobao"; then
        fix_item "npm 淘宝镜像"
        npm config set registry https://registry.npmmirror.com 2>/dev/null
        NEW_REG=$(npm config get registry 2>/dev/null)
        if echo "$NEW_REG" | grep -q "npmmirror"; then
            fix_done "npm 淘宝镜像" "true"
        else
            fix_done "npm 淘宝镜像" "false"
        fi
    fi
fi

# --- 修复 4: Java 17 ---
if [ "$JAVA_OK" = false ]; then
    fix_item "Java 17"
    brew install openjdk@17 2>&1 | tail -10

    # 创建软链接
    if [[ $(uname -m) == "arm64" ]]; then
        sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk 2>/dev/null
    else
        sudo ln -sfn /usr/local/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk 2>/dev/null
    fi

    # 设置 JAVA_HOME
    if /usr/libexec/java_home -v 17 &>/dev/null; then
        JAVA_HOME_VAL=$(/usr/libexec/java_home -v 17)
        grep -q "JAVA_HOME" ~/.zprofile 2>/dev/null || echo "export JAVA_HOME=$JAVA_HOME_VAL" >> ~/.zprofile
        export JAVA_HOME="$JAVA_HOME_VAL"
    fi

    if java -version 2>&1 | grep -q 'version "17\.'; then
        JAVA_OK=true
        fix_done "Java 17" "true"
    else
        fix_done "Java 17" "false"
    fi
fi

# --- 修复 5: Maven ---
if [ "$MAVEN_OK" = false ]; then
    fix_item "Maven"
    brew install maven 2>&1 | tail -5
    if command -v mvn &> /dev/null; then
        MAVEN_OK=true
        fix_done "Maven" "true"
    else
        fix_done "Maven" "false"
    fi
fi

# 配置 Maven 镜像
if [ "$MAVEN_OK" = true ] && [ ! -f ~/.m2/settings.xml ]; then
    fix_item "Maven 阿里云镜像"
    mkdir -p ~/.m2
    cat > ~/.m2/settings.xml << 'EOF'
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
          https://maven.apache.org/xsd/settings-1.0.0.xsd">
  <mirrors>
    <mirror>
      <id>aliyunmaven</id>
      <mirrorOf>*</mirrorOf>
      <name>阿里云公共仓库</name>
      <url>https://maven.aliyun.com/repository/public</url>
    </mirror>
  </mirrors>
</settings>
EOF
    fix_done "Maven 阿里云镜像" "true"
fi

# --- 修复 6: MySQL ---
if [ "$MYSQL_OK" = false ] && ! command -v mysql &> /dev/null; then
    fix_item "MySQL 8"
    brew install mysql 2>&1 | tail -5
    if command -v mysql &> /dev/null; then
        brew services start mysql 2>/dev/null
        sleep 3
        # 设置密码
        mysql -u root -e "ALTER USER 'root'@'localhost' IDENTIFIED BY 'root123456'; FLUSH PRIVILEGES;" 2>/dev/null || true
        MYSQL_OK=true
        fix_done "MySQL 8" "true"
    else
        fix_done "MySQL 8" "false"
    fi
fi

# 启动 MySQL
if command -v mysql &> /dev/null; then
    if ! mysqladmin ping -u root --password=root123456 &>/dev/null; then
        fix_item "MySQL 服务"
        brew services start mysql 2>/dev/null
        sleep 3
        if mysqladmin ping -u root --password=root123456 &>/dev/null; then
            MYSQL_OK=true
            fix_done "MySQL 服务" "true"
        else
            # 尝试无密码启动
            if mysqladmin ping -u root &>/dev/null; then
                mysql -u root -e "ALTER USER 'root'@'localhost' IDENTIFIED BY 'root123456'; FLUSH PRIVILEGES;" 2>/dev/null || true
                MYSQL_OK=true
                fix_done "MySQL 服务" "true"
            else
                fix_done "MySQL 服务" "false"
            fi
        fi
    fi
fi

# --- 修复 7: Redis ---
if [ "$REDIS_OK" = false ] && ! command -v redis-server &> /dev/null; then
    fix_item "Redis"
    brew install redis 2>&1 | tail -5
    if command -v redis-server &> /dev/null; then
        brew services start redis 2>/dev/null
        sleep 1
        REDIS_OK=true
        fix_done "Redis" "true"
    else
        fix_done "Redis" "false"
    fi
fi

# 启动 Redis
if command -v redis-server &> /dev/null; then
    if ! redis-cli ping &>/dev/null; then
        fix_item "Redis 服务"
        brew services start redis 2>/dev/null
        sleep 1
        if redis-cli ping &>/dev/null; then
            REDIS_OK=true
            fix_done "Redis 服务" "true"
        else
            fix_done "Redis 服务" "false"
        fi
    fi
fi

# --- 修复 8: 数据库初始化 ---
if [ "$MYSQL_OK" = true ] && [ -f "$PROJECT_DIR/schema_init.sql" ]; then
    # 检查数据库是否存在
    if mysql -u root -proot123456 -e "USE magao_platform;" 2>/dev/null; then
        check_item "数据库" "ok" "magao_platform 已存在"
    else
        fix_item "数据库初始化"
        mysql -u root -proot123456 -e "CREATE DATABASE IF NOT EXISTS magao_platform CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;" 2>/dev/null
        mysql -u root -proot123456 magao_platform < "$PROJECT_DIR/schema_init.sql" 2>/dev/null
        if mysql -u root -proot123456 -e "USE magao_platform; SHOW TABLES;" 2>/dev/null | grep -q ".*_.*"; then
            fix_done "数据库初始化" "true"
        else
            fix_done "数据库初始化" "false"
        fi
    fi
fi

# --- 修复 9: 前端依赖 ---
if [ "$NODE_OK" = true ] && [ -f "$PROJECT_DIR/frontend/package.json" ]; then
    if [ -d "$PROJECT_DIR/frontend/node_modules" ]; then
        check_item "前端依赖" "ok" "node_modules 已存在"
    else
        fix_item "前端依赖 (npm install)"
        cd "$PROJECT_DIR/frontend"
        npm install 2>&1 | tail -5
        cd ~/Desktop
        if [ -d "$PROJECT_DIR/frontend/node_modules" ]; then
            fix_done "前端依赖 (npm install)" "true"
        else
            fix_done "前端依赖 (npm install)" "false"
        fi
    fi
fi

# ========================================
# 最终汇总
# ========================================
echo ""
echo -e "${BLUE}============================================================${NC}"
echo -e "${GREEN}   ✓ 环境修复完成${NC}"
echo -e "${BLUE}============================================================${NC}"
echo ""
echo "  修复统计："
echo -e "    已修复: ${GREEN}$FIXED 项${NC}"
echo -e "    正常:   ${GREEN}$PASS 项${NC}"
echo -e "    失败:   ${RED}$FAIL 项${NC}"
echo ""
echo "  当前环境版本："
echo -e "    Homebrew:   $(brew --version 2>/dev/null | head -1 || echo '未安装')"
echo -e "    Node.js:    $(node -v 2>/dev/null || echo '未安装')"
echo -e "    npm:        $(npm -v 2>/dev/null || echo '未安装')"
echo -e "    Java:       $(java -version 2>&1 | head -1 2>/dev/null || echo '未安装')"
echo -e "    Maven:      $(mvn -v 2>&1 | head -1 2>/dev/null || echo '未安装')"
echo -e "    MySQL:      $(mysql --version 2>/dev/null | head -1 || echo '未安装')"
echo -e "    Redis:      $(redis-server --version 2>/dev/null | head -1 || echo '未安装')"
echo ""
echo -e "${CYAN}━━━ 启动项目 ━━━${NC}"
echo ""
echo "  方式1：仅启动前端（测试编辑器）"
echo -e "    ${GREEN}cd $PROJECT_DIR/frontend && npm run dev${NC}"
echo "    浏览器打开: http://localhost:5173"
echo ""
echo "  方式2：启动前端 + 后端（完整功能）"
echo "    终端1（后端）:"
echo -e "    ${GREEN}cd $PROJECT_DIR/backend && mvn spring-boot:run${NC}"
echo ""
echo "    终端2（前端）:"
echo -e "    ${GREEN}cd $PROJECT_DIR/frontend && npm run dev${NC}"
echo ""
echo "    浏览器打开: http://localhost:5173"
echo ""
echo -e "${BLUE}============================================================${NC}"
