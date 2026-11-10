#!/bin/bash
# ============================================================
#  码高少儿编程平台 - Mac 本地环境一键安装脚本 (国内镜像版)
#  解决 GitHub 访问失败问题，使用清华/中科大镜像源
# ============================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

echo ""
echo -e "${BLUE}============================================================${NC}"
echo -e "${BLUE}   码高少儿编程平台 - Mac 本地环境安装 (国内镜像版)${NC}"
echo -e "${BLUE}============================================================${NC}"
echo ""

# ------------------------------------------
# 0. 检查操作系统
# ------------------------------------------
if [[ "$(uname)" != "Darwin" ]]; then
    echo -e "${RED}此脚本仅支持 macOS 系统。${NC}"
    exit 1
fi

echo -e "${GREEN}✓ 检测到 macOS $(sw_vers -productVersion 2>/dev/null)${NC}"
echo ""

# ------------------------------------------
# 0.5 配置 Git 使用 HTTP/1.1 避免 HTTP2 framing 错误
# ------------------------------------------
echo -e "${YELLOW}[0/7] 配置网络（解决 HTTP2 framing 错误）...${NC}"
git config --global http.version HTTP/1.1 2>/dev/null || true
git config --global https.version HTTP/1.1 2>/dev/null || true
echo -e "${GREEN}✓ 已设置 Git 使用 HTTP/1.1${NC}"
echo ""

# ------------------------------------------
# 1. 安装 Homebrew（使用国内镜像）
# ------------------------------------------
echo -e "${YELLOW}[1/7] 检查 Homebrew...${NC}"
if command -v brew &> /dev/null; then
    echo -e "${GREEN}✓ Homebrew 已安装: $(brew --version | head -1)${NC}"
else
    echo -e "${YELLOW}  正在使用国内镜像安装 Homebrew...${NC}"
    echo -e "${YELLOW}  ⚠ 安装过程中可能需要输入 Mac 登录密码${NC}"
    echo ""

    # 设置 Homebrew 国内镜像环境变量
    export HOMEBREW_INSTALL_FROM_API=1
    export HOMEBREW_API_DOMAIN="https://mirrors.tuna.tsinghua.edu.cn/homebrew-bottles/api"
    export HOMEBREW_BOTTLE_DOMAIN="https://mirrors.tuna.tsinghua.edu.cn/homebrew-bottles"
    export HOMEBREW_BREW_GIT_REMOTE="https://mirrors.tuna.tsinghua.edu.cn/git/homebrew/brew.git"
    export HOMEBREW_CORE_GIT_REMOTE="https://mirrors.tuna.tsinghua.edu.cn/git/homebrew/homebrew-core.git"

    # 尝试多种方式安装 Homebrew（防止某个镜像不可用）
    BREW_INSTALLED=false

    # 方式1: 清华镜像（正确URL）
    echo -e "${YELLOW}  尝试方式1: 清华镜像...${NC}"
    if /bin/bash -c "$(curl -fsSL https://mirrors.tuna.tsinghua.edu.cn/git/homebrew/install/install.sh)" 2>/dev/null; then
        BREW_INSTALLED=true
    else
        echo -e "${YELLOW}  清华镜像方式失败，尝试方式2...${NC}"
    fi

    # 方式2: git clone 清华镜像仓库后执行
    if [ "$BREW_INSTALLED" = false ]; then
        echo -e "${YELLOW}  尝试方式2: git clone 清华镜像...${NC}"
        cd /tmp
        rm -rf brew-install 2>/dev/null
        if git clone --depth=1 https://mirrors.tuna.tsinghua.edu.cn/git/homebrew/install.git brew-install 2>/dev/null; then
            /bin/bash brew-install/install.sh
            rm -rf brew-install
            BREW_INSTALLED=true
        fi
        cd ~/Desktop
    fi

    # 方式3: Gitee 镜像（国内最稳定）
    if [ "$BREW_INSTALLED" = false ]; then
        echo -e "${YELLOW}  尝试方式3: Gitee 镜像（国内最稳定）...${NC}"
        /bin/bash -c "$(curl -fsSL https://gitee.com/cunkai/HomebrewCN/raw/master/Homebrew.sh)" <<< "1"
        BREW_INSTALLED=true
    fi

    # Apple Silicon (M1/M2/M3/M4) 配置路径
    if [[ $(uname -m) == "arm64" ]]; then
        if [ -f /opt/homebrew/bin/brew ]; then
            echo 'eval "$(/opt/homebrew/bin/brew shellenv)"' >> ~/.zprofile
            eval "$(/opt/homebrew/bin/brew shellenv)"
        fi
    else
        if [ -f /usr/local/bin/brew ]; then
            echo 'eval "$(/usr/local/bin/brew shellenv)"' >> ~/.zprofile
            eval "$(/usr/local/bin/brew shellenv)"
        fi
    fi

    # 验证安装是否成功
    if command -v brew &> /dev/null; then
        # 持久化镜像配置到 ~/.zprofile
        cat >> ~/.zprofile << 'EOF'

# Homebrew 国内镜像配置
export HOMEBREW_API_DOMAIN="https://mirrors.tuna.tsinghua.edu.cn/homebrew-bottles/api"
export HOMEBREW_BOTTLE_DOMAIN="https://mirrors.tuna.tsinghua.edu.cn/homebrew-bottles"
EOF
        echo -e "${GREEN}✓ Homebrew 安装完成${NC}"
    else
        echo -e "${RED}✗ Homebrew 安装失败！${NC}"
        echo -e "${YELLOW}  请手动安装 Homebrew：${NC}"
        echo -e "${YELLOW}  方法1: 访问 https://brew.sh 按官方指引安装${NC}"
        echo -e "${YELLOW}  方法2: 在终端执行以下命令（需科学上网）：${NC}"
        echo -e "${YELLOW}  /bin/bash -c \"\$(curl -fsSL https://raw.githubusercontent.com/Homebrew/install/HEAD/install.sh)\"${NC}"
        echo -e "${YELLOW}  方法3: 使用 Gitee 镜像：${NC}"
        echo -e "${YELLOW}  /bin/bash -c \"\$(curl -fsSL https://gitee.com/cunkai/HomebrewCN/raw/master/Homebrew.sh)\"${NC}"
        echo ""
        echo -e "${YELLOW}  安装 Homebrew 后重新运行此脚本即可继续。${NC}"
        exit 1
    fi
fi
echo ""

# ------------------------------------------
# 2. 配置 Homebrew 国内镜像（已安装的情况也重新配置）
# ------------------------------------------
echo -e "${YELLOW}[2/7] 配置 Homebrew 国内镜像...${NC}"

# 设置环境变量（当前会话）
export HOMEBREW_API_DOMAIN="https://mirrors.tuna.tsinghua.edu.cn/homebrew-bottles/api"
export HOMEBREW_BOTTLE_DOMAIN="https://mirrors.tuna.tsinghua.edu.cn/homebrew-bottles"

# 更新 Homebrew Cask 镜像
if command -v brew &> /dev/null; then
    # 替换 homebrew-cask 等仓库的 remote 为清华镜像
    for repo in homebrew-cask homebrew-cask-fonts homebrew-cask-versions; do
        tap_dir=""
        if [[ $(uname -m) == "arm64" ]]; then
            tap_dir="/opt/homebrew/Library/Taps/homebrew/$repo"
        else
            tap_dir="/usr/local/Homebrew/Library/Taps/homebrew/$repo"
        fi
        if [ -d "$tap_dir" ]; then
            git -C "$tap_dir" remote set-url origin "https://mirrors.tuna.tsinghua.edu.cn/git/homebrew/$repo.git" 2>/dev/null || true
        fi
    done
    echo -e "${GREEN}✓ Homebrew 镜像已配置为清华源${NC}"
fi
echo ""

# ------------------------------------------
# 3. 安装 Node.js
# ------------------------------------------
echo -e "${YELLOW}[3/7] 检查 Node.js...${NC}"
if command -v node &> /dev/null; then
    NODE_VERSION=$(node -v)
    NODE_MAJOR=$(echo "$NODE_VERSION" | sed 's/v//' | cut -d. -f1)
    echo -e "${GREEN}✓ Node.js 已安装: $NODE_VERSION${NC}"
    if [[ "$NODE_MAJOR" -lt 18 ]]; then
        echo -e "${YELLOW}  Node.js 版本过低（需要 >= 18），正在升级...${NC}"
        brew install node@22
        brew link --overwrite node@22
        echo -e "${GREEN}✓ Node.js 已升级到: $(node -v)${NC}"
    fi
else
    echo -e "${YELLOW}  正在安装 Node.js 22 LTS（使用国内镜像下载）...${NC}"
    brew install node@22
    brew link --overwrite node@22
    echo -e "${GREEN}✓ Node.js 安装完成: $(node -v)${NC}"
fi

# 配置 npm 国内镜像
echo -e "${YELLOW}  配置 npm 淘宝镜像...${NC}"
npm config set registry https://registry.npmmirror.com 2>/dev/null || true
echo -e "${GREEN}✓ npm 镜像已设置为淘宝源${NC}"
echo ""

# ------------------------------------------
# 4. 安装 Java 17
# ------------------------------------------
echo -e "${YELLOW}[4/7] 检查 Java 17（后端需要）...${NC}"
JAVA17_OK=false
if java -version 2>&1 | grep -q "version \"17"; then
    echo -e "${GREEN}✓ Java 17 已安装${NC}"
    JAVA17_OK=true
elif command -v java &> /dev/null; then
    CURRENT_JAVA=$(java -version 2>&1 | head -1)
    echo -e "${YELLOW}  当前 Java: $CURRENT_JAVA (需要 Java 17)${NC}"
    echo -e "${YELLOW}  正在安装 Java 17...${NC}"
    brew install openjdk@17
    if [[ $(uname -m) == "arm64" ]]; then
        sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
    else
        sudo ln -sfn /usr/local/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
    fi
    echo -e "${GREEN}✓ Java 17 安装完成${NC}"
    JAVA17_OK=true
else
    echo -e "${YELLOW}  正在安装 Java 17...${NC}"
    brew install openjdk@17
    if [[ $(uname -m) == "arm64" ]]; then
        sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
    else
        sudo ln -sfn /usr/local/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk
    fi
    echo -e "${GREEN}✓ Java 17 安装完成${NC}"
    JAVA17_OK=true
fi

# 设置 JAVA_HOME
if [ "$JAVA17_OK" = true ]; then
    JAVA_HOME_PATH=$(/usr/libexec/java_home -v 17 2>/dev/null)
    if [ -n "$JAVA_HOME_PATH" ]; then
        # 持久化 JAVA_HOME
        grep -q "JAVA_HOME" ~/.zprofile 2>/dev/null || echo "export JAVA_HOME=$(/usr/libexec/java_home -v 17)" >> ~/.zprofile
        export JAVA_HOME="$JAVA_HOME_PATH"
        echo -e "${GREEN}✓ JAVA_HOME 已设置为: $JAVA_HOME_PATH${NC}"
    fi
fi
echo ""

# ------------------------------------------
# 5. 安装 Maven
# ------------------------------------------
echo -e "${YELLOW}[5/7] 检查 Maven（后端构建工具）...${NC}"
if command -v mvn &> /dev/null; then
    echo -e "${GREEN}✓ Maven 已安装${NC}"
else
    echo -e "${YELLOW}  正在安装 Maven...${NC}"
    brew install maven
    echo -e "${GREEN}✓ Maven 安装完成${NC}"
fi

# 配置 Maven 国内镜像
MAVEN_SETTINGS="$HOME/.m2/settings.xml"
if [ ! -f "$MAVEN_SETTINGS" ]; then
    echo -e "${YELLOW}  配置 Maven 阿里云镜像...${NC}"
    mkdir -p "$HOME/.m2"
    cat > "$MAVEN_SETTINGS" << 'EOF'
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
    echo -e "${GREEN}✓ Maven 镜像已配置为阿里云源${NC}"
fi
echo ""

# ------------------------------------------
# 6. 安装 MySQL
# ------------------------------------------
echo -e "${YELLOW}[6/7] 检查 MySQL（后端数据库）...${NC}"
if command -v mysql &> /dev/null; then
    echo -e "${GREEN}✓ MySQL 已安装: $(mysql --version)${NC}"
else
    echo -e "${YELLOW}  正在安装 MySQL 8...${NC}"
    brew install mysql
    echo -e "${GREEN}✓ MySQL 安装完成${NC}"
    echo ""
    echo -e "${YELLOW}  启动 MySQL 服务并设置密码...${NC}"
    brew services start mysql
    sleep 2
    # 设置 root 密码
    mysql -u root -e "ALTER USER 'root'@'localhost' IDENTIFIED BY 'root123456'; FLUSH PRIVILEGES;" 2>/dev/null && \
        echo -e "${GREEN}  ✓ MySQL root 密码已设置为 root123456${NC}" || \
        echo -e "${YELLOW}  密码设置需手动执行: mysql -u root -e \"ALTER USER 'root'@'localhost' IDENTIFIED BY 'root123456';\"${NC}"
fi
echo ""

# ------------------------------------------
# 7. 安装 Redis
# ------------------------------------------
echo -e "${YELLOW}[7/7] 检查 Redis（后端缓存）...${NC}"
if command -v redis-server &> /dev/null; then
    if redis-cli ping &> /dev/null; then
        echo -e "${GREEN}✓ Redis 已安装且运行中${NC}"
    else
        echo -e "${GREEN}✓ Redis 已安装（未运行），正在启动...${NC}"
        brew services start redis
        echo -e "${GREEN}  ✓ Redis 已启动${NC}"
    fi
else
    echo -e "${YELLOW}  正在安装 Redis...${NC}"
    brew install redis
    brew services start redis
    echo -e "${GREEN}✓ Redis 已安装并启动${NC}"
fi
echo ""

# ------------------------------------------
# 安装完成总结
# ------------------------------------------
echo -e "${BLUE}============================================================${NC}"
echo -e "${GREEN}   ✓ 环境安装完成！${NC}"
echo -e "${BLUE}============================================================${NC}"
echo ""
echo "已安装工具版本："
echo -e "  Node.js:    $(node -v 2>/dev/null || echo '未安装')"
echo -e "  npm:        $(npm -v 2>/dev/null || echo '未安装')"
echo -e "  Java:       $(java -version 2>&1 | head -1 2>/dev/null || echo '未安装')"
echo -e "  Maven:      $(mvn -v 2>&1 | head -1 2>/dev/null || echo '未安装')"
echo -e "  MySQL:      $(mysql --version 2>/dev/null || echo '未安装')"
echo -e "  Redis:      $(redis-server --version 2>/dev/null | head -1 || echo '未安装')"
echo ""
echo -e "${BLUE}------------------------------------------------------------${NC}"
echo -e "${BLUE}  已配置的国内镜像：${NC}"
echo -e "${BLUE}------------------------------------------------------------${NC}"
echo "  Homebrew:  清华大学镜像"
echo "  npm:       淘宝镜像 (npmmirror.com)"
echo "  Maven:     阿里云镜像"
echo ""
echo -e "${BLUE}------------------------------------------------------------${NC}"
echo -e "${BLUE}  下一步操作：${NC}"
echo -e "${BLUE}------------------------------------------------------------${NC}"
echo ""
echo -e "  ${YELLOW}A. 仅启动前端（测试编辑器，最快）：${NC}"
echo ""
echo "     1. 将 frontend 文件夹复制到本地"
echo "     2. cd ~/path/to/frontend"
echo "     3. npm install"
echo "     4. npm run dev"
echo "     5. 浏览器打开 http://localhost:5173"
echo ""
echo -e "  ${YELLOW}B. 启动前端 + 后端（完整功能）：${NC}"
echo ""
echo "     1. 将整个项目复制到本地"
echo "     2. 初始化数据库："
echo "        mysql -u root -proot123456 -e \"CREATE DATABASE IF NOT EXISTS magao_platform CHARACTER SET utf8mb4;\""
echo "        mysql -u root -proot123456 magao_platform < schema_init.sql"
echo "     3. 启动后端（终端窗口1）："
echo "        cd ~/path/to/backend && mvn spring-boot:run"
echo "     4. 启动前端（终端窗口2）："
echo "        cd ~/path/to/frontend && npm install && npm run dev"
echo "     5. 浏览器打开 http://localhost:5173"
echo ""
echo -e "${BLUE}============================================================${NC}"
