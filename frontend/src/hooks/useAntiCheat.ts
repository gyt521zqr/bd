/**
 * 防作弊监控 Hook
 * 监测切屏行为、全屏状态、复制粘贴，自动上报日志
 */
import { ref, onMounted, onUnmounted } from 'vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { reportSwitchScreen } from '@/api/exam'

export interface UseAntiCheatOptions {
  /** 考试ID */
  examId: number
  /** 最大允许切屏次数，默认 3 */
  maxSwitch?: number
  /** 是否自动进入全屏，默认 true */
  autoFullscreen?: boolean
  /** 超过切屏限制时的回调 */
  onExceeded?: () => void
}

export function useAntiCheat(options: UseAntiCheatOptions) {
  const {
    examId,
    maxSwitch = 3,
    autoFullscreen = true,
    onExceeded,
  } = options

  /** 切屏次数 */
  const switchCount = ref(0)
  /** 是否超过切屏限制 */
  const isExceeded = ref(false)
  /** 是否处于全屏 */
  const isFullscreen = ref(false)

  /** 处理页面可见性变化 */
  function handleVisibilityChange() {
    if (document.hidden) {
      switchCount.value++
      // 上报切屏事件
      reportSwitchScreen(examId).catch(() => {})

      if (switchCount.value >= maxSwitch) {
        isExceeded.value = true
        ElMessageBox.alert(
          `切屏次数已达上限（${maxSwitch}次），系统将自动交卷`,
          '违规警告',
          { type: 'error' }
        ).then(() => {
          onExceeded?.()
        })
      } else {
        ElMessage.warning(
          `已检测到第 ${switchCount.value} 次切屏，最多允许 ${maxSwitch} 次`
        )
      }
    }
  }

  /** 处理全屏变化 */
  function handleFullscreenChange() {
    isFullscreen.value = !!document.fullscreenElement
  }

  /** 进入全屏 */
  async function enterFullscreen() {
    try {
      await document.documentElement.requestFullscreen()
      isFullscreen.value = true
    } catch {
      // 用户拒绝全屏
      ElMessage.warning('建议进入全屏模式进行考试')
    }
  }

  /** 禁止复制粘贴 */
  function handleCopy(e: ClipboardEvent) {
    e.preventDefault()
  }

  function handlePaste(e: ClipboardEvent) {
    e.preventDefault()
  }

  function handleCut(e: ClipboardEvent) {
    e.preventDefault()
  }

  onMounted(() => {
    document.addEventListener('visibilitychange', handleVisibilityChange)
    document.addEventListener('fullscreenchange', handleFullscreenChange)
    document.addEventListener('copy', handleCopy)
    document.addEventListener('paste', handlePaste)
    document.addEventListener('cut', handleCut)
    if (autoFullscreen) {
      enterFullscreen()
    }
  })

  onUnmounted(() => {
    document.removeEventListener('visibilitychange', handleVisibilityChange)
    document.removeEventListener('fullscreenchange', handleFullscreenChange)
    document.removeEventListener('copy', handleCopy)
    document.removeEventListener('paste', handlePaste)
    document.removeEventListener('cut', handleCut)
    // 退出全屏
    if (document.fullscreenElement) {
      document.exitFullscreen().catch(() => {})
    }
  })

  return {
    switchCount,
    isExceeded,
    isFullscreen,
    enterFullscreen,
  }
}
