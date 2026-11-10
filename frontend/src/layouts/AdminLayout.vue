<template>
  <!-- 后台管理布局：左侧菜单 + 顶部栏 + 内容区 + 面包屑 -->
  <div class="admin-layout">
    <!-- 左侧菜单 -->
    <aside class="admin-sidebar" :class="{ collapsed: appStore.sidebarCollapsed }">
      <!-- Logo 区域 -->
      <div class="sidebar-logo">
        <div class="logo-icon-wrap">
          <svg class="logo-svg" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect width="40" height="40" rx="10" fill="url(#logoGrad)" />
            <path d="M12 28V12h4.5l5.5 10.2L27.5 12H32v16h-3.5V18.4L23.8 28h-1.6l-4.7-9.6V28H12z" fill="#fff" />
            <defs>
              <linearGradient id="logoGrad" x1="0" y1="0" x2="40" y2="40">
                <stop stop-color="#165DFF" />
                <stop offset="1" stop-color="#4080FF" />
              </linearGradient>
            </defs>
          </svg>
        </div>
        <div class="logo-info" v-show="!appStore.sidebarCollapsed">
          <span class="logo-text">码高管理</span>
          <span class="logo-subtitle">Magao Admin</span>
        </div>
      </div>

      <!-- 菜单列表 -->
      <div class="sidebar-menu-wrap">
        <el-menu
          :default-active="activeMenu"
          :collapse="appStore.sidebarCollapsed"
          :collapse-transition="false"
          background-color="transparent"
          text-color="#86909C"
          active-text-color="#fff"
          router
        >
          <!-- 数据概览 -->
          <el-menu-item index="/admin" class="menu-group-item">
            <el-icon class="menu-icon menu-icon--overview"><DataAnalysis /></el-icon>
            <span>数据概览</span>
          </el-menu-item>

          <!-- 分隔线 -->
          <div class="menu-divider"></div>

          <!-- 用户中心 -->
          <el-sub-menu index="/admin/user-center" class="menu-group--user" v-if="userStore.isAdmin">
            <template #title>
              <el-icon class="menu-icon menu-icon--user"><User /></el-icon>
              <span>用户中心</span>
            </template>
            <el-menu-item index="/admin/student">学员管理</el-menu-item>
            <el-menu-item index="/admin/teacher">教师管理</el-menu-item>
            <el-menu-item index="/admin/regcode">注册码管理</el-menu-item>
          </el-sub-menu>

          <!-- 分隔线 -->
          <div class="menu-divider"></div>

          <!-- 课程中心 -->
          <el-sub-menu index="/admin/course-center" class="menu-group--course">
            <template #title>
              <el-icon class="menu-icon menu-icon--course"><Reading /></el-icon>
              <span>课程中心</span>
            </template>
            <el-menu-item index="/admin/course">课程管理</el-menu-item>
            <el-menu-item index="/admin/category" v-if="userStore.isAdmin">分类管理</el-menu-item>
            <el-menu-item index="/admin/trial">试听预约</el-menu-item>
          </el-sub-menu>

          <!-- 分隔线 -->
          <div class="menu-divider"></div>

          <!-- 竞赛中心 -->
          <el-sub-menu index="/admin/exam-center" class="menu-group--exam">
            <template #title>
              <el-icon class="menu-icon menu-icon--exam"><Trophy /></el-icon>
              <span>竞赛中心</span>
            </template>
            <el-menu-item index="/admin/exam">赛事管理</el-menu-item>
            <el-menu-item index="/admin/banner" v-if="userStore.isAdmin">Banner管理</el-menu-item>
            <el-menu-item index="/admin/registration">报名管理</el-menu-item>
            <el-menu-item index="/admin/question-bank">题库中心</el-menu-item>
            <el-menu-item index="/admin/paper">试卷中心</el-menu-item>
            <el-menu-item index="/admin/score">成绩管理</el-menu-item>
          </el-sub-menu>

          <!-- 分隔线 -->
          <div class="menu-divider"></div>

          <!-- 内容管理 -->
          <el-sub-menu index="/admin/content-center" class="menu-group--content" v-if="userStore.isAdmin">
            <template #title>
              <el-icon class="menu-icon menu-icon--content"><Document /></el-icon>
              <span>内容管理</span>
            </template>
            <el-menu-item index="/admin/news">资讯管理</el-menu-item>
            <el-menu-item index="/admin/homepage-config">首页配置</el-menu-item>
            <el-menu-item index="/admin/about-config">关于我们配置</el-menu-item>
          </el-sub-menu>

          <!-- 分隔线 -->
          <div class="menu-divider"></div>

          <!-- 分隔线 -->
          <div class="menu-divider"></div>

          <!-- 系统管理 -->
          <el-sub-menu index="/admin/system" class="menu-group--system">
            <template #title>
              <el-icon class="menu-icon menu-icon--system"><Setting /></el-icon>
              <span>系统管理</span>
            </template>
            <el-menu-item index="/admin/class">班级管理</el-menu-item>
            <el-menu-item index="/admin/file" v-if="userStore.isAdmin">文件管理</el-menu-item>
            <el-menu-item index="/admin/marketing" v-if="userStore.isAdmin">营销管理</el-menu-item>
            <el-menu-item index="/admin/partner" v-if="userStore.isAdmin">合作伙伴</el-menu-item>
            <el-menu-item index="/admin/storage-config" v-if="userStore.isAdmin">云存储配置</el-menu-item>
            <el-menu-item index="/admin/notification" v-if="userStore.isAdmin">通知管理</el-menu-item>
            <el-menu-item index="/admin/operation-log" v-if="userStore.isAdmin">操作日志</el-menu-item>
            <el-menu-item index="/admin/data-backup" v-if="userStore.isAdmin">数据备份</el-menu-item>
            <el-menu-item index="/admin/admin-account" v-if="userStore.isAdmin">管理员账号</el-menu-item>
            <el-menu-item index="/admin/system-monitor" v-if="userStore.isAdmin">系统监控</el-menu-item>
            <el-menu-item index="/admin/settings" v-if="userStore.isAdmin">系统设置</el-menu-item>
          </el-sub-menu>
        </el-menu>
      </div>

      <!-- 底部技术支持信息 -->
      <div class="sidebar-footer" v-show="!appStore.sidebarCollapsed">
        <div class="support-info">
          <svg class="support-icon" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M10 0C4.48 0 0 4.48 0 10s4.48 10 10 10 10-4.48 10-10S15.52 0 10 0zm1 15H9v-2h2v2zm0-4H9V5h2v6z" fill="#86909C" />
          </svg>
          <span class="support-text">技术支持</span>
          <span class="support-detail">v1.0.0 | Magao Platform</span>
        </div>
      </div>
      <div class="sidebar-footer-collapsed" v-show="appStore.sidebarCollapsed">
        <el-tooltip content="技术支持 v1.0.0" placement="right" :show-after="300">
          <svg class="support-icon-collapsed" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M10 0C4.48 0 0 4.48 0 10s4.48 10 10 10 10-4.48 10-10S15.52 0 10 0zm1 15H9v-2h2v2zm0-4H9V5h2v6z" fill="#86909C" />
          </svg>
        </el-tooltip>
      </div>
    </aside>

    <!-- 右侧内容区 -->
    <div class="admin-content" :class="{ expanded: appStore.sidebarCollapsed }">
      <!-- 顶部栏 -->
      <header class="admin-header">
        <div class="header-left">
          <el-icon class="toggle-btn" @click="appStore.toggleSidebar">
            <Fold v-if="!appStore.sidebarCollapsed" />
            <Expand v-else />
          </el-icon>
          <BreadcrumbNav />
          <div class="header-page-title">
            <span class="page-title-text">{{ currentPageTitle }}</span>
          </div>
        </div>
        <div class="header-right">
          <!-- 通知铃铛 -->
          <el-tooltip content="通知消息" placement="bottom">
            <el-badge :value="3" :max="99" class="notification-badge">
              <el-icon class="header-icon notification-icon">
                <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" />
                  <path d="M13.73 21a2 2 0 0 1-3.46 0" />
                </svg>
              </el-icon>
            </el-badge>
          </el-tooltip>

          <!-- 全屏切换 -->
          <el-tooltip content="全屏" placement="bottom">
            <el-icon class="header-icon" @click="appStore.toggleFullscreen">
              <FullScreen />
            </el-icon>
          </el-tooltip>

          <!-- 用户头像下拉 -->
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="34" :src="userStore.userInfo?.avatar" class="user-avatar">
                {{ userStore.userInfo?.nickname?.charAt(0) || 'A' }}
              </el-avatar>
              <span class="user-name">{{ userStore.userInfo?.nickname || '管理员' }}</span>
              <el-icon class="user-arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>
                  个人中心
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 页面内容 -->
      <main class="admin-main">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * AdminLayout - 后台管理布局组件
 * 左侧深色菜单 + 顶部栏 + 内容区
 */
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import BreadcrumbNav from '@/components/common/BreadcrumbNav.vue'
import {
  DataAnalysis,
  User,
  Reading,
  Trophy,
  Setting,
  Fold,
  Expand,
  FullScreen,
  ArrowDown,
  SwitchButton,
  Document,
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

/** 当前激活的菜单项 */
const activeMenu = computed(() => route.path)

/** 当前页面标题 */
const currentPageTitle = computed(() => {
  const titles: Record<string, string> = {
    '/admin': '数据概览',
    '/admin/student': '学员管理',
    '/admin/teacher': '教师管理',
    '/admin/regcode': '注册码管理',
    '/admin/course': '课程管理',
    '/admin/category': '分类管理',
    '/admin/trial': '试听预约',
    '/admin/exam': '赛事管理',
    '/admin/banner': 'Banner管理',
    '/admin/registration': '报名管理',
    '/admin/question-bank': '题库中心',
    '/admin/paper': '试卷中心',
    '/admin/score': '成绩管理',
    '/admin/class': '班级管理',
    '/admin/file': '文件管理',
    '/admin/marketing': '营销管理',
    '/admin/partner': '合作伙伴',
    '/admin/storage-config': '云存储配置',
    '/admin/notification': '通知管理',
    '/admin/operation-log': '操作日志',
    '/admin/data-backup': '数据备份',
    '/admin/admin-account': '管理员账号',
    '/admin/system-monitor': '系统监控',
    '/admin/settings': '系统设置',
    '/admin/news': '资讯管理',
    '/admin/homepage-config': '首页配置',
    '/admin/about-config': '关于我们配置',
  }
  return titles[route.path] || '管理后台'
})

/** 退出登录 */
async function handleLogout() {
  await userStore.logout()
  router.push('/login')
}

/** 下拉菜单命令处理 */
function handleCommand(command: string) {
  if (command === 'profile') {
    router.push('/user/profile')
  } else if (command === 'logout') {
    handleLogout()
  }
}
</script>

<style scoped lang="scss">
/* ==================== 布局容器 ==================== */
.admin-layout {
  display: flex;
  min-height: 100vh;
  background: #F7F8FA;
}

/* ==================== 侧边栏 ==================== */
.admin-sidebar {
  width: 240px;
  background: linear-gradient(180deg, #1D2129 0%, #1D2129 100%);
  transition: width 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 200;
  box-shadow: 2px 0 12px rgba(0, 0, 0, 0.15);

  &.collapsed {
    width: 64px;

    .sidebar-logo {
      padding: 0;
      justify-content: center;
    }

    .sidebar-menu-wrap {
      :deep(.el-menu) {
        .el-menu-item,
        .el-sub-menu > .el-sub-menu__title {
          padding-left: 0 !important;
          justify-content: center;

          .el-icon {
            margin-right: 0;
          }
        }
      }
    }
  }
}

/* ==================== Logo 区域 ==================== */
.sidebar-logo {
  display: flex;
  align-items: center;
  height: 64px;
  padding: 0 16px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.06);
  background: rgba(22, 93, 255, 0.08);
  backdrop-filter: blur(10px);
  flex-shrink: 0;
  transition: padding 0.3s ease;
}

.logo-icon-wrap {
  width: 36px;
  height: 36px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  filter: drop-shadow(0 2px 4px rgba(22, 93, 255, 0.3));
}

.logo-svg {
  width: 36px;
  height: 36px;
}

.logo-info {
  display: flex;
  flex-direction: column;
  margin-left: 10px;
  white-space: nowrap;
  overflow: hidden;
}

.logo-text {
  color: #F7F8FA;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 1px;
  line-height: 1.3;
}

.logo-subtitle {
  color: #86909C;
  font-size: 11px;
  font-weight: 400;
  letter-spacing: 0.5px;
  line-height: 1.2;
  margin-top: 1px;
}

/* ==================== 菜单区域 ==================== */
.sidebar-menu-wrap {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  padding: 8px 0;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: rgba(255, 255, 255, 0.1);
    border-radius: 2px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  :deep(.el-menu) {
    border-right: none;
    background: transparent;

    /* 菜单项通用样式 */
    .el-menu-item,
    .el-sub-menu > .el-sub-menu__title {
      height: 44px;
      line-height: 44px;
      margin: 2px 8px;
      border-radius: 8px;
      padding-left: 16px !important;
      padding-right: 12px;
      transition: transform, box-shadow, background-color, border-color, color, opacity 0.25s cubic-bezier(0.4, 0, 0.2, 1);
      position: relative;
      overflow: hidden;

      &:hover {
        background: rgba(255, 255, 255, 0.06) !important;
        color: #E5E6EB !important;

        /* 左侧高亮条 hover 效果 */
        &::before {
          content: '';
          position: absolute;
          left: 0;
          top: 50%;
          transform: translateY(-50%);
          width: 3px;
          height: 20px;
          border-radius: 0 3px 3px 0;
          background: rgba(22, 93, 255, 0.5);
          transition: transform, box-shadow, background-color, border-color, color, opacity 0.25s ease;
        }
      }

      .el-icon {
        font-size: 18px;
        margin-right: 10px;
        transition: transform 0.25s ease;
      }

      &:hover .el-icon {
        transform: scale(1.1);
      }
    }

    /* 激活菜单项 - 左侧蓝色指示条 */
    .el-menu-item.is-active {
      background: rgba(22, 93, 255, 0.15) !important;
      color: #fff !important;
      font-weight: 600;

      &::before {
        content: '';
        position: absolute;
        left: 0;
        top: 50%;
        transform: translateY(-50%);
        width: 3px;
        height: 24px;
        border-radius: 0 3px 3px 0;
        background: #165DFF;
        box-shadow: 0 0 8px rgba(22, 93, 255, 0.4);
        animation: activeIndicator 0.3s ease;
      }

      &::after {
        content: '';
        position: absolute;
        left: 3px;
        top: 0;
        bottom: 0;
        width: calc(100% - 3px);
        background: linear-gradient(90deg, rgba(22, 93, 255, 0.08), transparent);
        border-radius: 0 8px 8px 0;
        pointer-events: none;
      }
    }

    /* 子菜单项样式 */
    .el-sub-menu .el-menu-item {
      height: 40px;
      line-height: 40px;
      padding-left: 48px !important;
      margin: 1px 8px 1px 8px;
      font-size: 13px;

      &:hover {
        background: rgba(255, 255, 255, 0.05) !important;
      }

      &.is-active {
        background: rgba(22, 93, 255, 0.12) !important;
        color: #fff !important;

        &::before {
          height: 18px;
        }
      }
    }

    /* 子菜单展开/收起过渡动画 */
    .el-sub-menu__icon-arrow {
      transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
      font-size: 12px;
      color: #86909C;
    }

    .el-sub-menu.is-opened > .el-sub-menu__title .el-sub-menu__icon-arrow {
      transform: rotate(180deg);
    }

    /* 子菜单展开动画 */
    .el-sub-menu .el-menu {
      overflow: hidden;
      transition: max-height 0.35s cubic-bezier(0.4, 0, 0.2, 1),
                  opacity 0.25s ease;
    }
  }
}

/* ==================== 菜单分隔线 ==================== */
.menu-divider {
  height: 1px;
  margin: 8px 16px;
  background: linear-gradient(
    90deg,
    transparent,
    rgba(255, 255, 255, 0.06) 20%,
    rgba(255, 255, 255, 0.06) 80%,
    transparent
  );
}

/* ==================== 彩色菜单图标 ==================== */
.menu-icon {
  &.menu-icon--overview {
    color: #4080FF; // 蓝色 - 数据概览
  }
  &.menu-icon--user {
    color: #36D399; // 绿色 - 用户中心
  }
  &.menu-icon--course {
    color: #FFB84D; // 黄色 - 课程中心
  }
  &.menu-icon--exam {
    color: #F76965; // 红色 - 竞赛中心
  }
  &.menu-icon--system {
    color: #FFB84D; // 橙色 - 系统管理
  }
  &.menu-icon--content {
    color: #4080FF; // 天蓝色 - 内容管理
  }
}

/* ==================== 侧边栏底部 ==================== */
.sidebar-footer {
  flex-shrink: 0;
  padding: 12px 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  background: rgba(0, 0, 0, 0.15);
}

.support-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.support-icon {
  width: 16px;
  height: 16px;
  margin-bottom: 2px;
}

.support-text {
  font-size: 12px;
  color: #86909C;
  font-weight: 500;
}

.support-detail {
  font-size: 10px;
  color: #4E5969;
}

.sidebar-footer-collapsed {
  flex-shrink: 0;
  padding: 12px 0;
  border-top: 1px solid rgba(255, 255, 255, 0.06);
  background: rgba(0, 0, 0, 0.15);
  display: flex;
  justify-content: center;
  cursor: pointer;

  .support-icon-collapsed {
    width: 18px;
    height: 18px;
    opacity: 0.5;

    &:hover {
      opacity: 0.8;
    }
  }
}

/* ==================== 右侧内容区 ==================== */
.admin-content {
  flex: 1;
  margin-left: 240px;
  transition: margin-left 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;
  min-height: 100vh;

  &.expanded {
    margin-left: 64px;
  }
}

/* ==================== 顶部栏 ==================== */
.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 64px;
  padding: 0 24px;
  background: $bg-white;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06), 0 1px 2px rgba(0, 0, 0, 0.04);
  position: sticky;
  top: 0;
  z-index: 100;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.toggle-btn {
  font-size: 20px;
  cursor: pointer;
  color: #4E5969;
  padding: 6px;
  border-radius: 6px;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.2s ease;

  &:hover {
    color: #165DFF;
    background: rgba(22, 93, 255, 0.06);
  }
}

.header-page-title {
  position: relative;
  padding-left: 14px;

  &::before {
    content: '';
    position: absolute;
    left: 0;
    top: 50%;
    transform: translateY(-50%);
    width: 1px;
    height: 20px;
    background: #E5E6EB;
  }
}

.page-title-text {
  font-size: 16px;
  font-weight: 600;
  color: #1D2129;
  letter-spacing: 0.3px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* ==================== 通知铃铛 ==================== */
.notification-badge {
  :deep(.el-badge__content) {
    top: -4px;
    right: 2px;
  }
}

.notification-icon {
  padding: 6px;
  border-radius: 6px;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.2s ease;

  &:hover {
    color: #165DFF;
    background: rgba(22, 93, 255, 0.06);
  }
}

.header-icon {
  font-size: 20px;
  cursor: pointer;
  color: #4E5969;
  padding: 6px;
  border-radius: 6px;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.2s ease;

  &:hover {
    color: #165DFF;
    background: rgba(22, 93, 255, 0.06);
  }
}

/* ==================== 用户信息 ==================== */
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px 4px 4px;
  border-radius: 8px;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.2s ease;

  &:hover {
    background: #F7F8FA;
  }

  .user-avatar {
    border: 2px solid #E5E6EB;
    transition: border-color 0.2s ease;

    &:hover {
      border-color: #165DFF;
    }
  }

  .user-name {
    font-size: 14px;
    color: #1D2129;
    font-weight: 500;
    max-width: 100px;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  .user-arrow {
    font-size: 12px;
    color: #86909C;
    transition: transform 0.2s ease;
  }

  &:hover .user-arrow {
    transform: rotate(180deg);
  }
}

/* ==================== 主内容区 ==================== */
.admin-main {
  flex: 1;
  padding: 20px;
  background: #F7F8FA;
  overflow-y: auto;
}

/* ==================== 动画 ==================== */
@keyframes activeIndicator {
  from {
    height: 0;
    opacity: 0;
  }
  to {
    height: 24px;
    opacity: 1;
  }
}

/* ==================== 折叠状态 tooltip ==================== */
.admin-sidebar.collapsed {
  .sidebar-menu-wrap {
    :deep(.el-menu) {
      .el-menu-item,
      .el-sub-menu > .el-sub-menu__title {
        position: relative;

        /* 折叠时 Element Plus 自带 popup tooltip */
      }
    }
  }
}
</style>

<style lang="scss">
/* ==================== 全局覆盖 el-menu 样式 ==================== */

/* 激活菜单项 */
.admin-sidebar .el-menu-item.is-active {
  background-color: rgba(22, 93, 255, 0.15) !important;
}

/* 子菜单展开动画 - 通过全局样式控制 */
.admin-sidebar .el-sub-menu .el-menu--inline {
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

/* 折叠状态下子菜单 popup 样式 */
.admin-sidebar.el-menu--collapse {
  > .el-sub-menu > .el-sub-menu__title {
    &::before {
      display: none !important;
    }
  }

  /* popup 菜单样式 */
  .el-menu--popup {
    background: #1D2129 !important;
    border: 1px solid rgba(255, 255, 255, 0.08);
    border-radius: 8px;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
    padding: 6px;
    min-width: 160px;

    .el-menu-item {
      border-radius: 6px;
      margin: 2px 0;
      color: #86909C;

      &:hover {
        background: rgba(255, 255, 255, 0.06) !important;
        color: #E5E6EB !important;
      }

      &.is-active {
        background: rgba(22, 93, 255, 0.15) !important;
        color: #fff !important;
      }
    }
  }
}

/* 下拉菜单项图标间距 */
.admin-header .el-dropdown-menu__item .el-icon {
  margin-right: 6px;
  vertical-align: -2px;
}
</style>
