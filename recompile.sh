#!/bin/bash
# ============================================================
#  Magao Platform - Recompile Backend (Fixes encoding issues)
#  Use this when you modify Java source files
# ============================================================

set -e

RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR/backend"

echo ""
echo -e "${YELLOW}Recompiling backend with UTF-8 encoding...${NC}"
echo ""

# CRITICAL: Set UTF-8 encoding environment
export JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8 -Dsun.jnu.encoding=UTF-8"
export LANG=en_US.UTF-8
export LC_ALL=en_US.UTF-8
export MAVEN_OPTS="-Dfile.encoding=UTF-8"

# Set JAVA_HOME
export JAVA_HOME=$(/usr/libexec/java_home 2>/dev/null || echo "")
if [ -z "$JAVA_HOME" ]; then
    if [[ $(uname -m) == "arm64" ]]; then
        export JAVA_HOME="/opt/homebrew/opt/openjdk@17"
    else
        export JAVA_HOME="/usr/local/opt/openjdk@17"
    fi
    export PATH="$JAVA_HOME/bin:$PATH"
fi

echo "Java: $(java -version 2>&1 | head -1)"
echo "JAVA_HOME: $JAVA_HOME"
echo "JAVA_TOOL_OPTIONS: $JAVA_TOOL_OPTIONS"
echo ""

# Configure Maven settings with UTF-8
MAVEN_SETTINGS="$HOME/.m2/settings.xml"
mkdir -p "$HOME/.m2"
if [ ! -f "$MAVEN_SETTINGS" ] || ! grep -q "utf8" "$MAVEN_SETTINGS" 2>/dev/null; then
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
      <name>Aliyun Maven</name>
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
EOF
    echo -e "${GREEN}Maven settings.xml configured with UTF-8${NC}"
fi

# Clean and compile
echo -e "${YELLOW}Running: mvn clean compile -Dfile.encoding=UTF-8${NC}"
echo ""

mvn clean compile -Dfile.encoding=UTF-8 2>&1

if [ ${PIPESTATUS[0]} -eq 0 ]; then
    echo ""
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}  Compilation successful!${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo -e "Now package the jar:"
    echo -e "  ${YELLOW}mvn package -DskipTests -Dfile.encoding=UTF-8${NC}"
    echo ""
    echo -e "Then run quick-start.sh to start everything"
else
    echo ""
    echo -e "${RED}========================================${NC}"
    echo -e "${RED}  Compilation failed!${NC}"
    echo -e "${RED}========================================${NC}"
    echo ""
    echo -e "${YELLOW}Common fixes:${NC}"
    echo -e "  1. Ensure Java 17: java -version"
    echo -e "  2. Ensure UTF-8: echo \$JAVA_TOOL_OPTIONS"
    echo -e "  3. Try: mvn clean compile -Dfile.encoding=UTF-8 -e"
    echo ""
    exit 1
fi

# Package
echo -e "${YELLOW}Packaging...${NC}"
mvn package -DskipTests -Dfile.encoding=UTF-8 -q 2>&1 | tail -5

if [ ${PIPESTATUS[0]} -eq 0 ]; then
    echo -e "${GREEN}Package created: target/magao-backend-1.0.0.jar${NC}"
    echo ""
    echo -e "Run ${YELLOW}./quick-start.sh${NC} to start the platform"
else
    echo -e "${RED}Packaging failed. Run: mvn package -DskipTests -Dfile.encoding=UTF-8${NC}"
    exit 1
fi
