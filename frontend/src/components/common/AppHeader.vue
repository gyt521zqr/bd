<template>
  <!-- 顶部导航栏：Logo、导航菜单、搜索框、登录/注册、个人中心下拉 -->
  <header class="app-header">
    <div class="header-inner">
      <!-- 左侧：Logo + 汉堡菜单（移动端） -->
      <div class="header-left">
        <div class="logo" @click="$router.push('/')">
          <span class="logo-icon">M</span>
          <span class="logo-text">{{ siteStore.config.siteName }}</span>
        </div>

        <!-- 桌面端导航菜单 -->
        <nav class="main-nav">
          <router-link
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="nav-link"
            active-class="active"
          >
            {{ item.label }}
          </router-link>
        </nav>
      </div>

      <!-- 右侧：搜索、用户区域 -->
      <div class="header-right">
        <!-- 搜索框（桌面端显示） -->
        <div class="search-box">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索课程、赛事..."
            :prefix-icon="Search"
            clearable
            @keyup.enter="handleSearch"
          />
        </div>

        <!-- 已登录：个人中心下拉 -->
        <template v-if="userStore.isLoggedIn">
          <el-dropdown trigger="click" @command="handleUserCommand">
            <div class="user-avatar">
              <div class="avatar-wrapper">
                <el-avatar :size="32" :src="userStore.userInfo?.avatar">
                  {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
                </el-avatar>
                <span class="online-dot"></span>
              </div>
              <span class="user-name">{{ userStore.userInfo?.nickname || '用户' }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人中心
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>设置
                </el-dropdown-item>
                <el-dropdown-item command="orders">
                  <el-icon><ShoppingCart /></el-icon>我的订单
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>

        <!-- 未登录：登录/注册按钮 -->
        <template v-else>
          <button v-if="siteStore.config.registerEnabled" class="btn-register" @click="$router.push('/register')">注册</button>
          <button class="btn-login" @click="$router.push('/login')">登录</button>
        </template>

        <!-- 汉堡菜单按钮（移动端） -->
        <button
          class="hamburger-btn"
          :class="{ active: mobileMenuOpen }"
          @click="mobileMenuOpen = !mobileMenuOpen"
          aria-label="菜单"
        >
          <svg width="22" height="22" viewBox="0 0 22 22" fill="none">
            <rect class="line1" x="2" y="5" width="18" height="2" rx="1" fill="currentColor" />
            <rect class="line2" x="2" y="10" width="18" height="2" rx="1" fill="currentColor" />
            <rect class="line3" x="2" y="15" width="18" height="2" rx="1" fill="currentColor" />
          </svg>
        </button>
      </div>
    </div>

    <!-- 移动端抽屉式导航遮罩 -->
    <transition name="fade">
      <div
        v-if="mobileMenuOpen"
        class="mobile-overlay"
        @click="mobileMenuOpen = false"
      />
    </transition>

    <!-- 移动端抽屉式导航面板 -->
    <transition name="slide-right">
      <div v-if="mobileMenuOpen" class="mobile-drawer">
        <!-- 移动端搜索框 -->
        <div class="mobile-search">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索课程、赛事..."
            :prefix-icon="Search"
            clearable
            @keyup.enter="handleSearch"
          />
        </div>

        <!-- 移动端导航链接 -->
        <nav class="mobile-nav">
          <router-link
            v-for="item in navItems"
            :key="item.path"
            :to="item.path"
            class="mobile-nav-link"
            active-class="active"
            @click="mobileMenuOpen = false"
          >
            {{ item.label }}
          </router-link>
        </nav>

        <!-- 移动端用户区域 -->
        <div class="mobile-user-area">
          <template v-if="userStore.isLoggedIn">
            <div class="mobile-user-info">
              <div class="avatar-wrapper">
                <el-avatar :size="36" :src="userStore.userInfo?.avatar">
                  {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
                </el-avatar>
                <span class="online-dot"></span>
              </div>
              <span class="mobile-user-name">{{ userStore.userInfo?.nickname || '用户' }}</span>
            </div>
            <div class="mobile-user-actions">
              <button @click="handleUserCommand('profile'); mobileMenuOpen = false">个人中心</button>
              <button @click="handleUserCommand('logout'); mobileMenuOpen = false">退出登录</button>
            </div>
          </template>
          <template v-else>
            <div class="mobile-auth-buttons">
              <button v-if="siteStore.config.registerEnabled" class="btn-register" @click="$router.push('/register'); mobileMenuOpen = false">注册</button>
              <button class="btn-login" @click="$router.push('/login'); mobileMenuOpen = false">登录</button>
            </div>
          </template>
        </div>
      </div>
    </transition>
  </header>
</template>

<script setup lang="ts">
/**
 * AppHeader - 顶部导航栏组件
 * 固定顶部，包含Logo、导航菜单、搜索框、登录/注册、个人中心下拉
 * 移动端自动折叠为汉堡菜单，点击展开抽屉式导航
 */
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Search, ArrowDown, User, Setting, ShoppingCart, SwitchButton } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useSiteStore } from '@/stores/site'
import { getUnreadCount } from '@/api/message'

const router = useRouter()
const userStore = useUserStore()
const siteStore = useSiteStore()
const searchKeyword = ref('')
const unreadCount = ref(0)
const mobileMenuOpen = ref(false)

/** 导航菜单：后台可配（stores/site.ts 启动时拉取），未配置回退默认 */
const navItems = computed(() => siteStore.config.navMenu)

/** 搜索 */
function handleSearch() {
  if (searchKeyword.value.trim()) {
    router.push(`/course?keyword=${searchKeyword.value}`)
    mobileMenuOpen.value = false
  }
}

/** 用户菜单命令处理 */
async function handleUserCommand(command: string) {
  switch (command) {
    case 'profile':
      router.push('/user/profile')
      break
    case 'settings':
      router.push('/user/settings')
      break
    case 'orders':
      router.push('/order')
      break
    case 'logout':
      await userStore.logout()
      router.push('/login')
      break
  }
}

/** 获取未读消息数 */
async function fetchUnreadCount() {
  try {
    const res = await getUnreadCount()
    unreadCount.value = res.data.count
  } catch {
    // 静默处理
  }
}

// 初始化获取未读消息数
fetchUnreadCount()
</script>

<style scoped lang="scss">
/* ====== 顶部导航栏 ====== */
.app-header {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 64px;
  background: rgba(255, 255, 255, 0.82);
  backdrop-filter: blur(20px) saturate(180%);
  -webkit-backdrop-filter: blur(20px) saturate(180%);
  z-index: 1000;
  border-bottom: 1px solid rgba(229, 231, 235, 0.6);
  box-shadow: 0 1px 12px rgba(0, 0, 0, 0.04);
  transition: box-shadow 0.3s ease, background 0.3s ease;
}

.header-inner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 100%;
  max-width: 1280px;
  margin: 0 auto;
  padding: 0 24px;
}

/* ====== 左侧区域 ====== */
.header-left {
  display: flex;
  align-items: center;
  gap: 32px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  flex-shrink: 0;
  transition: transform 0.3s ease;

  &:hover {
    transform: scale(1.03);
  }

  .logo-icon {
    width: 36px;
    height: 36px;
    background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
    color: #FFFFFF;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: bold;
    font-size: 20px;
    box-shadow: 0 4px 12px rgba(22, 93, 255, 0.3);
    transition: box-shadow 0.3s ease;
  }

  &:hover .logo-icon {
    box-shadow: 0 6px 20px rgba(22, 93, 255, 0.4);
  }

  .logo-text {
    font-size: 20px;
    font-weight: 700;
    background: linear-gradient(135deg, #165DFF, #4080FF);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
}

/* ====== 桌面端导航菜单 ====== */
.main-nav {
  display: flex;
  gap: 24px;

  .nav-link {
    position: relative;
    font-size: 14px;
    color: $text-primary;
    text-decoration: none;
    padding: 4px 0;
    white-space: nowrap;
    transition: color 0.2s ease;

    /* hover下划线动画 - 从中间向两边展开 */
    &::after {
      content: '';
      position: absolute;
      bottom: -2px;
      left: 50%;
      width: 0;
      height: 2px;
      background: #165DFF;
      border-radius: 1px;
      transition: width 0.3s ease, left 0.3s ease;
      transform: translateX(-50%);
    }

    &:hover {
      color: #165DFF;

      &::after {
        width: 100%;
      }
    }

    /* 当前页面指示 */
    &.active {
      color: #165DFF;
      font-weight: 600;

      &::before {
        content: '';
        position: absolute;
        bottom: -6px;
        left: 50%;
        transform: translateX(-50%);
        width: 4px;
        height: 4px;
        background: #165DFF;
        border-radius: 50%;
        animation: dotPulse 2s ease-in-out infinite;
        will-change: transform;
      }
    }
  }
}

@keyframes dotPulse {
  0%, 100% {
    opacity: 1;
    transform: translateX(-50%) scale(1);
  }
  50% {
    opacity: 0.6;
    transform: translateX(-50%) scale(1.3);
  }
}

/* ====== 右侧区域 ====== */
.header-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

/* ====== 搜索框 ====== */
.search-box {
  width: 220px;

  :deep(.el-input__wrapper) {
    border-radius: 8px;
    background: #F5F7FA;
    box-shadow: none;
    transition: box-shadow 0.2s ease, border-color 0.2s ease;
    border: 1px solid transparent;
  }

  :deep(.el-input__wrapper.is-focus) {
    box-shadow: 0 0 0 3px rgba(22, 93, 255, 0.15);
    border-color: rgba(22, 93, 255, 0.3);
    background: #FFFFFF;
  }

  :deep(.el-input__inner) {
    color: $text-primary;

    &::placeholder {
      color: #999999;
    }
  }
}

/* ====== 用户头像下拉 ====== */
.user-avatar {
  display: flex;
  align-items: center;
  gap: 6px;
  cursor: pointer;

  .avatar-wrapper {
    position: relative;
    display: inline-block;
  }

  .online-dot {
    position: absolute;
    bottom: 0;
    right: 0;
    width: 10px;
    height: 10px;
    background: #36D399;
    border: 2px solid #FFFFFF;
    border-radius: 50%;
    animation: onlinePulse 2s ease-in-out infinite;
    will-change: transform;
  }

  .user-name {
    font-size: 14px;
    color: $text-primary;
    max-width: 80px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}

@keyframes onlinePulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba(54, 211, 153, 0.4);
  }
  50% {
    box-shadow: 0 0 0 4px rgba(54, 211, 153, 0);
  }
}

/* ====== 登录/注册按钮 ====== */
.btn-register {
  padding: 7px 18px;
  border-radius: 8px;
  border: 1.5px solid rgba(22, 93, 255, 0.4);
  background: rgba(22, 93, 255, 0.04);
  color: #165DFF;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.3s cubic-bezier(0.4, 0, 0.2, 1), border-color 0.3s cubic-bezier(0.4, 0, 0.2, 1), transform 0.3s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.3s cubic-bezier(0.4, 0, 0.2, 1);

  &:hover {
    background: rgba(22, 93, 255, 0.1);
    border-color: #165DFF;
    transform: translateY(-1px);
    box-shadow: 0 4px 16px rgba(22, 93, 255, 0.2);
  }
}

.btn-login {
  padding: 7px 18px;
  border-radius: 8px;
  border: none;
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
  color: #FFFFFF;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.3s cubic-bezier(0.4, 0, 0.2, 1), transform 0.3s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(22, 93, 255, 0.3);

  &:hover {
    background: linear-gradient(135deg, #4080FF 0%, #4080FF 100%);
    transform: translateY(-1px);
    box-shadow: 0 6px 20px rgba(22, 93, 255, 0.4);
  }

  &:active {
    transform: translateY(0);
  }
}

/* ====== 汉堡菜单按钮（默认隐藏） ====== */
.hamburger-btn {
  display: none;
  align-items: center;
  justify-content: center;
  width: 40px;
  height: 40px;
  border: none;
  background: transparent;
  color: $text-primary;
  cursor: pointer;
  border-radius: 8px;
  transition: background 0.2s ease;

  &:hover {
    background: #F5F7FA;
  }

  /* 汉堡菜单动画 */
  .line1,
  .line2,
  .line3 {
    transition: transform 0.3s ease, opacity 0.3s ease;
    transform-origin: center;
  }

  &.active {
    .line1 {
      transform: translateY(5px) rotate(45deg);
    }

    .line2 {
      opacity: 0;
      transform: scaleX(0);
    }

    .line3 {
      transform: translateY(-5px) rotate(-45deg);
    }
  }
}

/* ====== 移动端遮罩 ====== */
.mobile-overlay {
  position: fixed;
  top: 64px;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  z-index: 999;
}

/* ====== 移动端抽屉面板 ====== */
.mobile-drawer {
  position: fixed;
  top: 64px;
  right: 0;
  bottom: 0;
  width: 280px;
  max-width: 85vw;
  background: #FFFFFF;
  z-index: 1000;
  padding: 16px 0;
  overflow-y: auto;
  box-shadow: -4px 0 12px rgba(0, 0, 0, 0.08);
}

/* 移动端搜索框 */
.mobile-search {
  padding: 0 16px 16px;

  :deep(.el-input__wrapper) {
    border-radius: 8px;
    background: #F5F7FA;
    box-shadow: none;
    border: 1px solid transparent;
  }

  :deep(.el-input__wrapper.is-focus) {
    box-shadow: 0 0 0 3px rgba(22, 93, 255, 0.15);
    border-color: rgba(22, 93, 255, 0.3);
    background: #FFFFFF;
  }
}

/* 移动端导航链接 */
.mobile-nav {
  padding: 0 8px;
  border-bottom: 1px solid #E5E7EB;

  .mobile-nav-link {
    display: block;
    padding: 12px 16px;
    font-size: 15px;
    color: $text-primary;
    text-decoration: none;
    border-radius: 8px;
    transition: background 0.2s ease, color 0.2s ease;
    position: relative;

    &:hover {
      background: #F5F7FA;
      color: #165DFF;
    }

    &.active {
      color: #165DFF;
      background: #E8F0FF;
      font-weight: 600;

      &::after {
        content: '';
        position: absolute;
        right: 16px;
        top: 50%;
        transform: translateY(-50%);
        width: 6px;
        height: 6px;
        background: #165DFF;
        border-radius: 50%;
      }
    }
  }
}

/* 移动端用户区域 */
.mobile-user-area {
  padding: 16px;

  .mobile-user-info {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 12px;

    .avatar-wrapper {
      position: relative;
      display: inline-block;
    }

    .online-dot {
      position: absolute;
      bottom: 0;
      right: 0;
      width: 10px;
      height: 10px;
      background: #36D399;
      border: 2px solid #FFFFFF;
      border-radius: 50%;
    }

    .mobile-user-name {
      font-size: 15px;
      font-weight: 600;
      color: $text-primary;
    }
  }

  .mobile-user-actions {
    display: flex;
    gap: 12px;

    button {
      flex: 1;
      padding: 8px 0;
      border-radius: 8px;
      border: 1px solid #E5E7EB;
      background: #FFFFFF;
      color: $text-primary;
      font-size: 14px;
      cursor: pointer;
      transition: background-color 0.2s ease, color 0.2s ease, border-color 0.2s ease;

      &:hover {
        background: #F5F7FA;
        color: #165DFF;
        border-color: #165DFF;
      }
    }
  }

  .mobile-auth-buttons {
    display: flex;
    gap: 12px;

    .btn-register {
      flex: 1;
      text-align: center;
    }

    .btn-login {
      flex: 1;
      text-align: center;
    }
  }
}

/* ====== 过渡动画 ====== */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.25s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-right-enter-active {
  transition: transform 0.25s ease-out;
}

.slide-right-leave-active {
  transition: transform 0.2s ease-in;
}

.slide-right-enter-from {
  transform: translateX(100%);
}

.slide-right-leave-to {
  transform: translateX(100%);
}

/* ====== 响应式：中等屏幕（768px - 1024px） ====== */
@media (max-width: 1024px) and (min-width: 768px) {
  .main-nav {
    gap: 16px;

    .nav-link {
      font-size: 13px;
    }
  }

  .search-box {
    width: 160px;
  }
}

/* ====== 响应式：移动端（<768px） ====== */
@media (max-width: 767px) {
  .header-inner {
    padding: 0 16px;
  }

  /* 隐藏桌面端导航和搜索框 */
  .main-nav {
    display: none;
  }

  .search-box {
    display: none;
  }

  /* 显示汉堡菜单按钮 */
  .hamburger-btn {
    display: flex;
  }

  /* 移动端右侧间距调整 */
  .header-right {
    gap: 8px;
  }

  /* 移动端隐藏桌面端用户区域 */
  .user-avatar {
    display: none;
  }

  .btn-login,
  .btn-register {
    display: none;
  }
}
</style>
