<template>
  <!-- 积分计数器动画 -->
  <div class="point-counter">
    <span class="point-icon">P</span>
    <span class="point-value">{{ displayPoints }}</span>
    <span class="point-label">积分</span>
  </div>
</template>

<script setup lang="ts">
/**
 * PointCounter - 积分计数器动画
 * 数字滚动效果
 */
import { ref, watch, onMounted } from 'vue'

const props = defineProps<{ points: number }>()
const displayPoints = ref(0)

function animateTo(target: number) {
  const start = displayPoints.value
  const diff = target - start
  const duration = 1000
  const startTime = performance.now()

  function step(currentTime: number) {
    const elapsed = currentTime - startTime
    const progress = Math.min(elapsed / duration, 1)
    const eased = 1 - Math.pow(1 - progress, 3) // easeOutCubic
    displayPoints.value = Math.round(start + diff * eased)
    if (progress < 1) requestAnimationFrame(step)
  }
  requestAnimationFrame(step)
}

watch(() => props.points, (val) => animateTo(val))
onMounted(() => animateTo(props.points))
</script>

<style scoped lang="scss">
.point-counter { display: inline-flex; align-items: center; gap: 6px; }
.point-icon { width: 28px; height: 28px; background: linear-gradient(135deg, #FF9500, #FF7D00); color: #fff; border-radius: 8px; display: flex; align-items: center; justify-content: center; font-weight: 700; font-size: 14px; }
.point-value { font-size: 24px; font-weight: 800; color: #FF9500; font-variant-numeric: tabular-nums; }
.point-label { font-size: 13px; color: #86909C; }
</style>
