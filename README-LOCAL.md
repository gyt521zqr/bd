# Magao Platform - Local Deployment Guide

## Quick Start (30 seconds - no compilation needed)

### Step 1: Download and extract
```bash
# Extract the deployment package
tar xzf magao-deploy.tar.gz
cd magao-deploy
```

### Step 2: Run the quick-start script
```bash
chmod +x quick-start.sh
./quick-start.sh
```

That's it! The script will:
1. Check Java 17, MySQL, Redis, Node.js
2. Initialize the database
3. Start the backend (pre-compiled jar, no compilation needed)
4. Start the frontend
5. Open http://localhost:5173

---

## If you need to modify Java source files

Use the recompile script (handles UTF-8 encoding automatically):
```bash
chmod +x recompile.sh
./recompile.sh
```

Then run quick-start.sh again.

---

## First-time environment setup (if tools are missing)

Run the full setup script:
```bash
chmod +x mac-setup.sh
./mac-setup.sh
```

Or install manually:
```bash
# Install Homebrew (if not installed)
/bin/bash -c "$(curl -fsSL https://gitee.com/cunkai/HomebrewCN/raw/master/Homebrew.sh)"

# Install all required tools
brew install openjdk@17 maven mysql redis node@22

# Configure Java
sudo ln -sfn /opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk /Library/Java/JavaVirtualMachines/openjdk-17.jdk

# Start services
brew services start mysql
brew services start redis

# Set MySQL password
mysql -u root -e "ALTER USER 'root'@'localhost' IDENTIFIED BY 'root123456';"

# Configure npm mirror
npm config set registry https://registry.npmmirror.com
```

---

## Common Issues

### "Illegal character" compilation error
This is caused by incorrect file encoding. The recompile.sh script handles this by:
- Setting `JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"`
- Setting `LANG=en_US.UTF-8`
- Configuring Maven with UTF-8 encoding

If you still see errors, run these commands before compiling:
```bash
export JAVA_TOOL_OPTIONS="-Dfile.encoding=UTF-8"
export LANG=en_US.UTF-8
export LC_ALL=en_US.UTF-8
cd backend && mvn clean compile -Dfile.encoding=UTF-8
```

### MySQL connection error
```bash
# Start MySQL
brew services start mysql

# Set password
mysql -u root -e "ALTER USER 'root'@'localhost' IDENTIFIED BY 'root123456'; FLUSH PRIVILEGES;"
```

### Port already in use
```bash
# Kill processes on ports 8080 and 5173
lsof -ti:8080,5173 | xargs kill -9
```

---

## File Structure
```
magao-deploy/
  quick-start.sh          # Quick start (uses pre-compiled jar)
  recompile.sh            # Recompile backend (handles UTF-8)
  mac-setup.sh            # Full environment setup
  schema_init.sql         # Database schema
  backend/
    pom.xml               # Maven config
    target/
      magao-backend-1.0.0.jar  # Pre-compiled backend
    src/                  # Source code
  frontend/
    package.json
    vite.config.ts
    src/                  # Source code
```

## URLs
- Frontend: http://localhost:5173
- Backend: http://localhost:8080
- API Docs: http://localhost:8080/swagger-ui.html

## Stop the platform
```bash
lsof -ti:8080,5173 | xargs kill
```
