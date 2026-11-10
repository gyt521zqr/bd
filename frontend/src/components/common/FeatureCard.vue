<template>
  <!-- 功能入口卡片组件 -->
  <div
    class="feature-card"
    @click="handleClick"
    @mousemove="handleMouseMove"
    @mouseleave="handleMouseLeave"
    :style="cardStyle"
  >
    <!-- 图标区域 -->
    <div class="feature-icon" :style="{ background: `${color}15`, color: color }">
      <el-icon :size="32">
        <component :is="iconComponent" />
      </el-icon>
    </div>
    <!-- 文字区域 -->
    <div class="feature-content">
      <h4 class="feature-title">{{ title }}</h4>
      <p class="feature-desc" :class="{ 'desc-visible': descVisible }">{{ description }}</p>
    </div>
    <!-- 箭头 -->
    <el-icon class="feature-arrow" :style="{ color }">
      <ArrowRight />
    </el-icon>
  </div>
</template>

<script setup lang="ts">
/**
 * FeatureCard - 功能入口卡片组件
 * 用于首页/导航页的功能入口展示，支持自定义图标、主题色和跳转路径
 * 新增：3D倾斜效果、图标呼吸动画、描述文字淡入延迟
 */
import { computed, type Component, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight } from '@element-plus/icons-vue'
import * as ElementPlusIcons from '@element-plus/icons-vue'

const props = withDefaults(defineProps<{
  title: string
  description: string
  icon: string
  color?: string
  path: string
}>(), {
  color: '#165DFF'
})

const router = useRouter()

/** 动态解析 Element Plus 图标组件 */
const iconComponent = computed<Component | string>(() => {
  if (ElementPlusIcons[props.icon as keyof typeof ElementPlusIcons]) {
    return ElementPlusIcons[props.icon as keyof typeof ElementPlusIcons]
  }
  return 'Menu'
})

/** 点击跳转 */
function handleClick() {
  if (props.path) {
    router.push(props.path)
  }
}

// ========== 3D倾斜效果 ==========
const tiltX = ref(0)
const tiltY = ref(0)
const descVisible = ref(false)

const cardStyle = computed(() => {
  return {
    transform: `perspective(800px) rotateX(${tiltX.value}deg) rotateY(${tiltY.value}deg) scale(${tiltX.value !== 0 || tiltY.value !== 0 ? 1.02 : 1})`,
  }
})

function handleMouseMove(e: MouseEvent) {
  const card = e.currentTarget as HTMLElement
  const rect = card.getBoundingClientRect()
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top
  const centerX = rect.width / 2
  const centerY = rect.height / 2

  // 计算倾斜角度（最大±8度）
  tiltY.value = ((x - centerX) / centerX) * 8
  tiltX.value = -((y - centerY) / centerY) * 8

  // 描述文字淡入
  descVisible.value = true
}

function handleMouseLeave() {
  tiltX.value = 0
  tiltY.value = 0
  descVisible.value = false
}
</script>

<style scoped lang="scss">
.feature-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: $bg-white;
  border-radius: 12px;
  cursor: pointer;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: box-shadow 0.3s ease;
  transform-style: preserve-3d;
  will-change: transform;

  &:hover {
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.14);
  }
}

.feature-icon {
  flex-shrink: 0;
  width: 56px;
  height: 56px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.3s ease;
  /* 图标呼吸动画 */
  animation: iconBreathe 3s ease-in-out infinite;
  will-change: transform, opacity;
}

@keyframes iconBreathe {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.06);
  }
}

.feature-card:hover .feature-icon {
  animation: none;
  transform: scale(1.12);
}

.feature-content {
  flex: 1;
  min-width: 0;
}

.feature-title {
  font-size: 15px;
  font-weight: 600;
  color: #1D2129;
  margin: 0 0 4px;
  line-height: 1.4;
}

.feature-desc {
  font-size: 13px;
  color: #86909C;
  margin: 0;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
  /* 描述文字淡入延迟 */
  opacity: 0.7;
  transform: translateY(4px);
  transition: opacity 0.4s ease 0.1s, transform 0.4s ease 0.1s;

  &.desc-visible {
    opacity: 1;
    transform: translateY(0);
  }
}

.feature-arrow {
  flex-shrink: 0;
  font-size: 18px;
  opacity: 0.4;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s ease;
}

.feature-card:hover .feature-arrow {
  opacity: 1;
  transform: translateX(4px);
}
</style>
