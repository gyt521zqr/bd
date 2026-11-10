<template>
  <!-- 防作弊监控组件：切屏检测、全屏监控、日志上报 -->
  <div class="anti-cheat-monitor" style="display: none;">
    <!-- 切屏次数显示 -->
    <div class="switch-warning" v-if="switchCount > 0">
      <el-alert
        :title="`已检测到 ${switchCount} 次切屏，最多允许 ${maxSwitch} 次`"
        :type="switchCount >= maxSwitch ? 'error' : 'warning'"
        :closable="false"
        show-icon
      />
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * AntiCheatMonitor - 防作弊监控组件
 * 监测切屏行为、全屏状态，自动上报日志
 */
import { ref, onMounted, onUnmounted } from 'vue'
import { reportSwitchScreen } from '@/api/exam'

const props = withDefaults(defineProps<{
  /** 考试ID */
  examId: number
  /** 最大允许切屏次数 */
  maxSwitch?: number
}>(), {
  maxSwitch: 3,
})

const switchCount = ref(0)

/** 处理页面可见性变化 */
function handleVisibilityChange() {
  if (document.hidden) {
    switchCount.value++
    // 上报切屏事件
    reportSwitchScreen(props.examId).catch(() => {})
  }
}

/** 处理全屏变化 */
function handleFullscreenChange() {
  if (!document.fullscreenElement) {
    // 退出全屏，可以提示用户
  }
}

/** 进入全屏 */
async function enterFullscreen() {
  try {
    await document.documentElement.requestFullscreen()
  } catch {
    // 用户拒绝全屏
  }
}

onMounted(() => {
  document.addEventListener('visibilitychange', handleVisibilityChange)
  document.addEventListener('fullscreenchange', handleFullscreenChange)
  // 自动进入全屏
  enterFullscreen()
})

onUnmounted(() => {
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  document.removeEventListener('fullscreenchange', handleFullscreenChange)
})
</script>
