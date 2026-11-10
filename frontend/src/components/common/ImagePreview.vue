<template>
  <!-- 图片预览组件 -->
  <div class="image-preview" @click="handlePreview">
    <el-image
      :src="src"
      :fit="fit"
      :lazy="lazy"
      :preview-src-list="previewSrcList"
      :initial-index="initialIndex"
      :z-index="zIndex"
      :style="{ width: width, height: height, borderRadius: radius }"
    >
      <template #error>
        <div class="image-error">
          <el-icon :size="24"><Picture /></el-icon>
          <span>加载失败</span>
        </div>
      </template>
    </el-image>
  </div>
</template>

<script setup lang="ts">
/**
 * ImagePreview - 图片预览组件
 */
import { Picture } from '@element-plus/icons-vue'

withDefaults(defineProps<{
  src: string
  fit?: string
  lazy?: boolean
  previewSrcList?: string[]
  initialIndex?: number
  zIndex?: number
  width?: string
  height?: string
  radius?: string
}>(), {
  fit: 'cover',
  lazy: true,
  previewSrcList: () => [],
  initialIndex: 0,
  zIndex: 2000,
  width: '100%',
  height: 'auto',
  radius: '8px',
})

function handlePreview() { /* 点击预览 */ }
</script>

<style scoped lang="scss">
.image-preview { cursor: pointer; }
.image-error { display: flex; flex-direction: column; align-items: center; justify-content: center; height: 100%; background: #F7F8FA; color: #86909C; gap: 4px; font-size: 12px; }
</style>
