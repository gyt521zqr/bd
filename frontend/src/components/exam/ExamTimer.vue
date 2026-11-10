<template>
  <!-- 考试倒计时，支持天时分秒、警告色变化 -->
  <div class="exam-timer" :class="{ warning: isWarning, danger: isDanger }">
    <el-icon><Clock /></el-icon>
    <span class="timer-text">{{ displayTime }}</span>
  </div>
</template>

<script setup lang="ts">
/**
 * ExamTimer - 考试倒计时组件
 * 自动倒计时，时间不足时变色警告
 */
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { Clock } from '@element-plus/icons-vue'

const props = defineProps<{ duration: number }>()
const emit = defineEmits<{ timeout: [] }>()

const remaining = ref(props.duration * 60) // 转换为秒
let timer: ReturnType<typeof setInterval> | null = null

const displayTime = computed(() => {
  const h = Math.floor(remaining.value / 3600)
  const m = Math.floor((remaining.value % 3600) / 60)
  const s = remaining.value % 60
  return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
})

const isWarning = computed(() => remaining.value <= 600 && remaining.value > 120)
const isDanger = computed(() => remaining.value <= 120)

onMounted(() => {
  timer = setInterval(() => {
    remaining.value--
    if (remaining.value <= 0) {
      clearInterval(timer!)
      emit('timeout')
    }
  }, 1000)
})

onBeforeUnmount(() => { if (timer) clearInterval(timer) })
</script>

<style scoped lang="scss">
.exam-timer { display: flex; align-items: center; gap: 6px; padding: 6px 16px; background: #F7F8FA; border-radius: 8px; font-size: 18px; font-weight: 700; color: #1D2129; &.warning { background: #FFF6E0; color: #FF9500; } &.danger { background: #FFECE8; color: #F53F3F; animation: blink 1s infinite; will-change: transform, opacity; } }
.timer-text { font-variant-numeric: tabular-nums; }
@keyframes blink { 0%, 100% { opacity: 1; } 50% { opacity: 0.5; } }
</style>
