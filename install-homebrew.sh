#!/bin/bash
# ============================================================
#  Homebrew 国内安装脚本（最稳定版）
#  使用 Gitee 镜像，支持 Apple Silicon 和 Intel
# ============================================================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

clear
echo ""
echo -e "${BLUE}============================================================${NC}"
echo -e "${BLUE}           Homebrew 国内安装（最稳定版）${NC}"
echo -e "${BLUE}============================================================${NC}"
echo ""
echo -e "${YELLOW}  支持: macOS 12+ / Apple Silicon (M1-M4) / Intel${NC}"
echo -e "${YELLOW}  镜像: 中科大 / 清华 / 阿里 / 腾讯 / 网易 / 华为${NC}"
echo ""

# ------------------------------------------
# 0. 清理之前失败的安装残留
# ------------------------------------------
echo -e "${YELLOW}[0/5] 检查并清理旧的安装残留...${NC}"

# 清理 zprofile 中无效的 brew 配置
if [ -f ~/.zprofile ]; then
    # 备份
    cp ~/.zprofile ~/.zprofile.backup.$(date +%Y%m%d%H%M%S) 2>/dev/null || true
    # 移除无效的 brew shellenv 行（如果 brew 不存在）
    if [ ! -f /opt/homebrew/bin/brew ] && [ ! -f /usr/local/bin/brew ]; then
        sed -i '' '/eval \"\$(.*brew shellenv)\"/d' ~/.zprofile 2>/dev/null || true
        sed -i '' '/HOMEBREW_/d' ~/.zprofile 2>/dev/null || true
    fi
fi

# 清理可能残留的部分安装文件
if [[ $(uname -m) == "arm64" ]]; then
    if [ -d /opt/homebrew ] && [ ! -f /opt/homebrew/bin/brew ]; then
        echo -e "${YELLOW}  发现不完整的 Homebrew 安装，正在清理...${NC}"
        sudo rm -rf /opt/homebrew 2>/dev/null || true
    fi
else
    if [ -d /usr/local/Homebrew ] && [ ! -f /usr/local/bin/brew ]; then
        echo -e "${YELLOW}  发现不完整的 Homebrew 安装，正在清理...${NC}"
        sudo rm -rf /usr/local/Homebrew 2>/dev/null || true
    fi
fi

echo -e "${GREEN}✓ 清理完成${NC}"
echo ""

# ------------------------------------------
# 1. 选择镜像源
# ------------------------------------------
echo -e "${YELLOW}[1/5] 选择下载镜像源...${NC}"
echo ""
echo "  请选择一个镜像源（国内推荐中科大或清华）："
echo ""
echo "  1) 中科大镜像（推荐）"
echo "  2) 清华镜像"
echo "  3) 阿里镜像"
echo "  4) 腾讯镜像"
echo "  5) 网易镜像"
echo "  6) 华为镜像"
echo ""

read -p "  请输入序号 [1-6，默认1]: " MIRROR_CHOICE
MIRROR_CHOICE=${MIRROR_CHOICE:-1}

case $MIRROR_CHOICE in
    1)
        MIRROR_NAME="中科大"
        BREW_GIT="https://mirrors.ustc.edu.cn/brew.git"
        CORE_GIT="https://mirrors.ustc.edu.cn/homebrew-core.git"
        CASK_GIT="https://mirrors.ustc.edu.cn/homebrew-cask.git"
        BOTTLE_DOMAIN="https://mirrors.ustc.edu.cn/homebrew-bottles"
        ;;
    2)
        MIRROR_NAME="清华"
        BREW_GIT="https://mirrors.tuna.tsinghua.edu.cn/git/homebrew/brew.git"
        CORE_GIT="https://mirrors.tuna.tsinghua.edu.cn/git/homebrew/homebrew-core.git"
        CASK_GIT="https://mirrors.tuna.tsinghua.edu.cn/git/homebrew/homebrew-cask.git"
        BOTTLE_DOMAIN="https://mirrors.tuna.tsinghua.edu.cn/homebrew-bottles"
        ;;
    3)
        MIRROR_NAME="阿里"
        BREW_GIT="https://mirrors.aliyun.com/homebrew/brew.git"
        CORE_GIT="https://mirrors.aliyun.com/homebrew/homebrew-core.git"
        CASK_GIT="https://mirrors.aliyun.com/homebrew/homebrew-cask.git"
        BOTTLE_DOMAIN="https://mirrors.aliyun.com/homebrew/homebrew-bottles"
        ;;
    4)
        MIRROR_NAME="腾讯"
        BREW_GIT="https://mirrors.cloud.tencent.com/homebrew/brew.git"
        CORE_GIT="https://mirrors.cloud.tencent.com/homebrew/homebrew-core.git"
        CASK_GIT="https://mirrors.cloud.tencent.com/homebrew/homebrew-cask.git"
        BOTTLE_DOMAIN="https://mirrors.cloud.tencent.com/homebrew-bottles"
        ;;
    5)
        MIRROR_NAME="网易"
        BREW_GIT="https://mirrors.163.com/homebrew/brew.git"
        CORE_GIT="https://mirrors.163.com/homebrew/homebrew-core.git"
        CASK_GIT="https://mirrors.163.com/homebrew/homebrew-cask.git"
        BOTTLE_DOMAIN="https://mirrors.163.com/homebrew-bottles"
        ;;
    6)
        MIRROR_NAME="华为"
        BREW_GIT="https://mirrors.huaweicloud.com/homebrew/brew.git"
        CORE_GIT="https://mirrors.huaweicloud.com/homebrew/homebrew-core.git"
        CASK_GIT="https://mirrors.huaweicloud.com/homebrew/homebrew-cask.git"
        BOTTLE_DOMAIN="https://mirrors.huaweicloud.com/homebrew-bottles"
        ;;
    *)
        echo -e "${RED}  无效选择，使用默认（中科大）${NC}"
        MIRROR_NAME="中科大"
        BREW_GIT="https://mirrors.ustc.edu.cn/brew.git"
        CORE_GIT="https://mirrors.ustc.edu.cn/homebrew-core.git"
        CASK_GIT="https://mirrors.ustc.edu.cn/homebrew-cask.git"
        BOTTLE_DOMAIN="https://mirrors.ustc.edu.cn/homebrew-bottles"
        ;;
esac

echo -e "${GREEN}✓ 已选择: ${MIRROR_NAME}镜像${NC}"
echo ""

# ------------------------------------------
# 2. 设置环境变量
# ------------------------------------------
echo -e "${YELLOW}[2/5] 配置环境变量...${NC}"

export HOMEBREW_BREW_GIT_REMOTE="$BREW_GIT"
export HOMEBREW_CORE_GIT_REMOTE="$CORE_GIT"
export HOMEBREW_BOTTLE_DOMAIN="$BOTTLE_DOMAIN"
export HOMEBREW_NO_AUTO_UPDATE=1

echo -e "${GREEN}✓ 环境变量已设置${NC}"
echo ""

# ------------------------------------------
# 3. 安装 Homebrew
# ------------------------------------------
echo -e "${YELLOW}[3/5] 开始安装 Homebrew...${NC}"
echo ""
echo -e "${YELLOW}  ⚠  安装过程中可能需要输入 Mac 登录密码${NC}"
echo -e "${YELLOW}  ⚠  输入密码时不会显示字符，输完直接按回车${NC}"
echo ""

# 尝试多个安装脚本源
INSTALLED=false

# 方式1: Gitee ineo6 脚本（中科大默认）
echo -e "${YELLOW}  尝试方式1: Gitee ineo6 安装脚本...${NC}"
if /bin/bash -c "$(curl -fsSL https://gitee.com/ineo6/homebrew-install/raw/master/install.sh)" 2>&1; then
    INSTALLED=true
else
    echo -e "${YELLOW}  方式1失败，尝试方式2...${NC}"
fi

# 方式2: Gitee cunkai HomebrewCN
if [ "$INSTALLED" = false ]; then
    echo -e "${YELLOW}  尝试方式2: Gitee cunkai HomebrewCN...${NC}"
    # 这个脚本是交互式的，需要输入选择
    echo "1" | /bin/bash -c "$(curl -fsSL https://gitee.com/cunkai/HomebrewCN/raw/master/Homebrew.sh)" 2>&1 && INSTALLED=true || echo -e "${YELLOW}  方式2失败，尝试方式3...${NC}"
fi

# 方式3: 手动 git clone 安装
if [ "$INSTALLED" = false ]; then
    echo -e "${YELLOW}  尝试方式3: 手动 git clone 安装...${NC}"

    if [[ $(uname -m) == "arm64" ]]; then
        BREW_PREFIX="/opt/homebrew"
        sudo mkdir -p /opt/homebrew
        sudo chown -R $(whoami):admin /opt/homebrew
        cd /opt/homebrew
        echo -e "${YELLOW}  正在下载 Homebrew（${MIRROR_NAME}镜像）...${NC}"
        git clone --depth=1 "$BREW_GIT" brew-src 2>&1 && \
            mv brew-src/* . && mv brew-src/.* . 2>/dev/null; rm -rf brew-src
        cd ~/Desktop
    else
        BREW_PREFIX="/usr/local"
        sudo mkdir -p /usr/local/Homebrew
        sudo chown -R $(whoami):admin /usr/local/Homebrew
        cd /usr/local/Homebrew
        echo -e "${YELLOW}  正在下载 Homebrew（${MIRROR_NAME}镜像）...${NC}"
        git clone --depth=1 "$BREW_GIT" brew-src 2>&1 && \
            mv brew-src/* . && mv brew-src/.* . 2>/dev/null; rm -rf brew-src
        cd ~/Desktop
    fi

    if [ -f "$BREW_PREFIX/bin/brew" ]; then
        INSTALLED=true
        echo -e "${GREEN}  手动安装成功${NC}"
    fi
fi

echo ""

# ------------------------------------------
# 4. 配置 PATH
# ------------------------------------------
echo -e "${YELLOW}[4/5] 配置环境变量和 PATH...${NC}"

if [[ $(uname -m) == "arm64" ]]; then
    BREW_BIN="/opt/homebrew/bin/brew"
    BREW_SHELLENV='eval "$(/opt/homebrew/bin/brew shellenv)"'
else
    BREW_BIN="/usr/local/bin/brew"
    BREW_SHELLENV='eval "$(/usr/local/bin/brew shellenv)"'
fi

if [ -f "$BREW_BIN" ]; then
    # 添加到 zprofile
    if ! grep -q "brew shellenv" ~/.zprofile 2>/dev/null; then
        echo "$BREW_SHELLENV" >> ~/.zprofile
    fi
    eval "$($BREW_BIN shellenv)"
    echo -e "${GREEN}✓ PATH 配置完成${NC}"
else
    echo -e "${RED}✗ 未找到 brew 命令，安装可能失败${NC}"
    exit 1
fi

echo ""

# ------------------------------------------
# 5. 配置镜像源并验证
# ------------------------------------------
echo -e "${YELLOW}[5/5] 配置 ${MIRROR_NAME}镜像源并验证...${NC}"

# 持久化镜像配置
cat >> ~/.zprofile << EOF

# Homebrew ${MIRROR_NAME}镜像配置
export HOMEBREW_BOTTLE_DOMAIN="${BOTTLE_DOMAIN}"
EOF

# 应用当前会话
export HOMEBREW_BOTTLE_DOMAIN="$BOTTLE_DOMAIN"

# 设置 core tap 镜像
if brew tap 2>/dev/null | grep -q "homebrew/core"; then
    brew tap --custom-remote --force-auto-update homebrew/core "$CORE_GIT" 2>/dev/null || true
fi

# 设置 cask tap 镜像
if brew tap 2>/dev/null | grep -q "homebrew/cask"; then
    brew tap --custom-remote --force-auto-update homebrew/cask "$CASK_GIT" 2>/dev/null || true
fi

echo ""
echo -e "${BLUE}============================================================${NC}"
echo -e "${GREEN}   ✓ Homebrew 安装完成！${NC}"
echo -e "${BLUE}============================================================${NC}"
echo ""
echo "  版本信息:"
echo "    $(brew --version | head -1)"
echo ""
echo "  镜像配置:"
echo "    镜像源: ${MIRROR_NAME}"
echo "    Bottle: ${BOTTLE_DOMAIN}"
echo ""
echo "  接下来可以执行:"
echo "    brew install node@22    # 安装 Node.js"
echo "    brew install mysql      # 安装 MySQL"
echo "    brew install redis      # 安装 Redis"
echo ""
echo -e "${YELLOW}  ⚠  如果新打开终端找不到 brew 命令，请执行:${NC}"
echo -e "${YELLOW}     source ~/.zprofile${NC}"
echo ""
echo -e "${BLUE}============================================================${NC}"
