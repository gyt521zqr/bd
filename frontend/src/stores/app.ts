/**
 * 应用全局状态管理
 * 管理侧边栏折叠、全屏、设备类型等全局 UI 状态
 */
import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useAppStore = defineStore('app', () => {
  /** 侧边栏是否折叠 */
  const sidebarCollapsed = ref(false)
  /** 是否全屏 */
  const isFullscreen = ref(false)
  /** 设备类型 */
  const device = ref<'desktop' | 'mobile' | 'tablet'>('desktop')
  /** 页面加载状态 */
  const loading = ref(false)
  /** 全局主题（light/dark） */
  const theme = ref<'light' | 'dark'>('light')

  /** 切换侧边栏折叠 */
  function toggleSidebar() {
    sidebarCollapsed.value = !sidebarCollapsed.value
  }

  /** 设置设备类型 */
  function setDevice(type: 'desktop' | 'mobile' | 'tablet') {
    device.value = type
  }

  /** 切换全屏 */
  async function toggleFullscreen() {
    try {
      if (!document.fullscreenElement) {
        await document.documentElement.requestFullscreen()
        isFullscreen.value = true
      } else {
        await document.exitFullscreen()
        isFullscreen.value = false
      }
    } catch (error) {
      console.error('全屏切换失败', error)
    }
  }

  /** 设置加载状态 */
  function setLoading(status: boolean) {
    loading.value = status
  }

  /** 切换主题 */
  function toggleTheme() {
    theme.value = theme.value === 'light' ? 'dark' : 'light'
    document.documentElement.classList.toggle('dark')
  }

  return {
    sidebarCollapsed,
    isFullscreen,
    device,
    loading,
    theme,
    toggleSidebar,
    setDevice,
    toggleFullscreen,
    setLoading,
    toggleTheme,
  }
})
