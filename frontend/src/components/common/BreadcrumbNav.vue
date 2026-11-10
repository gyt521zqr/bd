<template>
  <!-- 面包屑导航 -->
  <div class="breadcrumb-nav">
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item v-for="item in breadcrumbs" :key="item.path" :to="item.path ? { path: item.path } : undefined">
        {{ item.title }}
      </el-breadcrumb-item>
    </el-breadcrumb>
  </div>
</template>

<script setup lang="ts">
/**
 * BreadcrumbNav - 面包屑导航组件
 */
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const breadcrumbs = computed(() => {
  const matched = route.matched.filter(item => item.meta?.title)
  return matched.map(item => ({
    path: item.path,
    title: item.meta.title as string,
  }))
})
</script>

<style scoped lang="scss">
.breadcrumb-nav { padding: 12px 0; }
</style>
