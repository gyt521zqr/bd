<template>
  <!-- 学生前台布局：顶部导航 + 主内容区（已移除侧边栏） -->
  <div class="default-layout">
    <!-- 全局系统公告条（顶部，位于导航之上） -->
    <GlobalNoticeBar />
    <!-- 顶部导航栏 -->
    <AppHeader />
    <!-- 主内容区 -->
    <main class="layout-main">
      <!-- 面包屑导航 -->
      <BreadcrumbNav />
      <!-- 路由视图（支持 keep-alive 缓存） -->
      <router-view v-slot="{ Component, route }">
        <keep-alive :include="cachedViews">
          <component :is="Component" :key="route.path" v-if="route.meta.keepAlive" />
        </keep-alive>
        <component :is="Component" :key="route.path" v-if="!route.meta.keepAlive" />
      </router-view>
    </main>
    <!-- 底部页脚（编辑器页面隐藏以获得更大编辑空间） -->
    <AppFooter v-if="!hideFooter" />
    <!-- 全局悬浮客服 -->
    <CustomerService />
    <!-- 维护模式全屏遮罩 -->
    <MaintenanceOverlay />
  </div>
</template>

<script setup lang="ts">
/**
 * DefaultLayout - 学生前台布局组件
 * 包含顶部导航、主内容区、底部页脚和全局悬浮客服
 */
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from '@/components/common/AppHeader.vue'
import AppFooter from '@/components/common/AppFooter.vue'
import BreadcrumbNav from '@/components/common/BreadcrumbNav.vue'
import CustomerService from '@/components/common/CustomerService.vue'
import GlobalNoticeBar from '@/components/common/GlobalNoticeBar.vue'
import MaintenanceOverlay from '@/components/common/MaintenanceOverlay.vue'

const route = useRoute()

/** 是否隐藏页脚（编辑器页面等） */
const hideFooter = computed(() => !!route.meta.hideFooter)

/** 需要缓存的页面列表 */
const cachedViews = computed(() => {
  return route.matched
    .filter((item) => item.meta.keepAlive)
    .map((item) => item.name as string)
})
</script>

<style scoped lang="scss">
.default-layout {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background:
    radial-gradient(circle at 20% 0%, rgba(22, 93, 255, 0.03) 0%, transparent 50%),
    radial-gradient(circle at 80% 100%, rgba(54, 211, 153, 0.03) 0%, transparent 50%),
    #F7F8FA;
}

.layout-main {
  flex: 1;
  padding: 64px 0 0;
  min-height: calc(100vh - 64px);
}
</style>
