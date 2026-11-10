<template>
  <div class="scratchjr-editor" :class="{ locked: isLocked, 'exam-mode': isExamMode }" :style="editorStyle">
    <!-- 加载状态 -->
    <div v-if="!isReady" class="loading-overlay">
      <div class="loading-content">
        <div class="loading-logo">🐱</div>
        <el-icon :size="32" class="is-loading"><Loading /></el-icon>
        <p>ScratchJr 编辑器加载中...</p>
        <p class="loading-sub">正在初始化积木编程环境</p>
      </div>
    </div>

    <!-- 锁定遮罩 -->
    <div v-if="isLocked" class="lock-overlay">
      <div class="lock-content">
        <el-icon :size="48"><Lock /></el-icon>
        <p>作品已提交，编辑器已锁定</p>
      </div>
    </div>

    <!-- ScratchJr 官方编辑器 iframe -->
    <iframe
      ref="scratchjrFrame"
      src="/scratch-jr/editor.html"
      class="scratchjr-iframe"
      sandbox="allow-scripts allow-same-origin allow-downloads allow-forms allow-popups"
      allowfullscreen
      @load="onIframeLoad"
    ></iframe>

    <!-- 缩略图预览卡（底部动作栏已承载 导出/缩略图 操作，避免浮层遮挡舞台） -->
    <div v-if="isReady && thumbnail" class="scratchjr-thumb-card">
      <div class="thumb-title">作品缩略图</div>
      <img v-if="isDataUrl(thumbnail)" :src="thumbnail" alt="作品缩略图" class="thumb-img" />
      <div v-else class="thumb-placeholder">封面不可用</div>
    </div>

    <!-- 新手引导（首次进入显示，可关闭并记忆） -->
    <transition name="guide-fade">
      <div v-if="showGuide" class="scratchjr-guide">
        <div class="guide-card">
          <div class="guide-title">🐱 欢迎使用 ScratchJr 创作</div>
          <ul class="guide-list">
            <li><b>运行作品</b>：点击上方绿色 ▶ 旗帜开始运行</li>
            <li><b>拼积木</b>：从下方「积木区」拖拽彩色积木到编程区</li>
            <li><b>看效果</b>：在「舞台区」观察角色动作</li>
            <li><b>存作品</b>：满意后点上方「提交作品」即可保存</li>
          </ul>
          <div class="guide-actions">
            <el-button type="primary" @click="dismissGuide">开始创作</el-button>
            <span class="guide-skip" @click="dismissGuide">不再提示</span>
          </div>
        </div>
      </div>
    </transition>
  </div>
</template>

<script setup lang="ts">
/**
 * ScratchJrChildEditor
 * 使用官方 ScratchJr 编辑器（从 open-scratch/ScratchJr 移植）
 * 通过 iframe 嵌入 /scratch-jr/editor.html，使用 postMessage 通信
 *
 * 通信协议：
 * - 接收: scratchjr:ready (stage=init|loaded), scratchjr:save, scratchjr:projectData,
 *         scratchjr:cover, scratchjr:coverData, scratchjr:runStatus
 * - 发送: scratchjr:getProject, scratchjr:runProject, scratchjr:stopProject,
 *         scratchjr:loadProject, scratchjr:getCover, scratchjr:saveProject
 */
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Lock, Loading } from '@element-plus/icons-vue'

/* ===================== Props & Emits ===================== */
const props = withDefaults(defineProps<{
  modelValue?: string
  lang?: string
  readOnly?: boolean
  templateCode?: string
  height?: number | string
  examMode?: boolean
  locked?: boolean
}>(), {
  modelValue: '',
  readOnly: false,
  height: 500,
  examMode: false,
  locked: false,
})

const emit = defineEmits<{
  'update:modelValue': [value: string]
  'save-scratch': [data: any]
  'screen-switch': []
  'asset-import-attempt': []
}>()

/* ===================== State ===================== */
const scratchjrFrame = ref<HTMLIFrameElement | null>(null)
const isReady = ref(false)
const isRunning = ref(false)
const isLocked = computed(() => props.locked || false)
const isExamMode = computed(() => props.examMode || false)

/** 新手引导：首次进入显示，关闭后写入 localStorage 不再提示 */
const showGuide = ref(false)
const GUIDE_KEY = 'scratchjr_guide_seen_v1'
function dismissGuide() {
  showGuide.value = false
  try { localStorage.setItem(GUIDE_KEY, '1') } catch { /* ignore */ }
}

/** Cached project data from the iframe (updated via postMessage) */
const cachedProjectContents = ref('')
const cachedProjectName = ref('ScratchJr项目')
/** 作品缩略图（dataURL，由 iframe 经 scratchjr:cover / coverData 回传） */
const thumbnail = ref('')
/** Whether we have loaded initial project data */
const hasLoadedInitialData = ref(false)

const editorStyle = computed(() => {
  return {
    height: '100%',
  }
})

/* ===================== PostMessage Communication ===================== */
function sendMessage(type: string, data?: any) {
  if (scratchjrFrame.value && scratchjrFrame.value.contentWindow) {
    scratchjrFrame.value.contentWindow.postMessage(
      data ? { type, ...data } : { type },
      '*'
    )
  }
}

function handleMessage(event: MessageEvent) {
  if (!event.data || !event.data.type) return

  // Only handle scratchjr: prefixed messages
  if (!event.data.type.startsWith('scratchjr:')) return

  switch (event.data.type) {
    case 'scratchjr:ready':
      if (event.data.stage === 'loaded') {
        isReady.value = true
        // Load initial project data if available
        loadInitialProject()
      }
      break

    case 'scratchjr:save':
      handleProjectSave(event.data)
      break

    case 'scratchjr:projectData':
      handleProjectData(event.data)
      break

    case 'scratchjr:cover':
    case 'scratchjr:coverData':
      // Cover image received - store for thumbnail
      if (event.data && event.data.data) {
        thumbnail.value = event.data.data
      }
      break

    case 'scratchjr:runStatus':
      isRunning.value = event.data.running === true
      break
  }
}

/** Handle received project save from the iframe */
function handleProjectSave(data: any) {
  if (!data.contents) return

  cachedProjectContents.value = data.contents
  if (data.name) {
    cachedProjectName.value = data.name
  }

  // Emit update to parent (v-model)
  emit('update:modelValue', data.contents)

  // Emit save-scratch for the BaseEditor to handle
  emit('save-scratch', {
    type: 'scratchjr',
    contents: data.contents,
    json: data.contents,
    assets: [],
    name: data.name || cachedProjectName.value,
    timestamp: Date.now(),
  })
}

/** Handle received project data response from getProject request */
function handleProjectData(data: any) {
  if (!data.contents) return

  // Skip if the data hasn't changed
  if (data.contents === cachedProjectContents.value) return

  cachedProjectContents.value = data.contents
  if (data.name) {
    cachedProjectName.value = data.name
  }

  // Emit update to parent (v-model)
  emit('update:modelValue', data.contents)

  // Emit save-scratch for the BaseEditor to handle
  emit('save-scratch', {
    type: 'scratchjr',
    contents: data.contents,
    json: data.contents,
    assets: [],
    name: data.name || cachedProjectName.value,
    timestamp: Date.now(),
  })
}

/** Load initial project data from modelValue or templateCode */
function loadInitialProject() {
  if (hasLoadedInitialData.value) return
  hasLoadedInitialData.value = true

  const projectData = props.modelValue || props.templateCode
  if (projectData) {
    // For ScratchJr, project data is in sjr format (base64 encoded zip)
    // We can't directly load it via postMessage without a URL
    // The editor will start with an empty project
    console.log('[ScratchJr] Initial project data available but cannot be loaded directly in iframe mode')
  }
}

/* ===================== Iframe Lifecycle ===================== */
function onIframeLoad() {
  // Iframe loaded, waiting for scratchjr:ready message
  console.log('[ScratchJr] iframe loaded, waiting for ready signal')
}

/* ===================== Public Methods ===================== */

/** Run the ScratchJr project (green flag) */
function runProject() {
  if (!isReady.value) {
    ElMessage.warning('ScratchJr编辑器尚未就绪，请稍候')
    return
  }
  sendMessage('scratchjr:runProject')
}

/** Stop the ScratchJr project */
function stopProject() {
  if (!isReady.value) return
  sendMessage('scratchjr:stopProject')
}

/**
 * Get project data synchronously (returns cached data).
 * For ScratchJr, this returns sjr format data.
 * If cache is empty, it triggers an async request via postMessage.
 */
function getProjectData() {
  if (cachedProjectContents.value) {
    return {
      type: 'scratchjr',
      contents: cachedProjectContents.value,
      json: cachedProjectContents.value,
      assets: [],
      name: cachedProjectName.value,
      timestamp: Date.now(),
    }
  }
  // If no cached data, request it asynchronously
  // The response will come via scratchjr:projectData message
  if (isReady.value) {
    sendMessage('scratchjr:getProject')
  }
  return null
}

/**
 * Request project data asynchronously (returns a Promise).
 * Used by the parent component when submitting projects.
 */
function requestProjectData(): Promise<any> {
  return new Promise((resolve) => {
    // Return cached data if available
    if (cachedProjectContents.value) {
      resolve({
        type: 'scratchjr',
        contents: cachedProjectContents.value,
        json: cachedProjectContents.value,
        assets: [],
        name: cachedProjectName.value,
        timestamp: Date.now(),
      })
      return
    }

    if (!isReady.value) {
      resolve(null)
      return
    }

    // Set up a one-time listener for the projectData response
    const handler = (event: MessageEvent) => {
      if (event.data?.type === 'scratchjr:projectData') {
        window.removeEventListener('message', handler)
        if (event.data.contents) {
          cachedProjectContents.value = event.data.contents
          if (event.data.name) {
            cachedProjectName.value = event.data.name
          }
          resolve({
            type: 'scratchjr',
            contents: event.data.contents,
            json: event.data.contents,
            assets: [],
            name: event.data.name || cachedProjectName.value,
            timestamp: Date.now(),
          })
        } else {
          resolve(null)
        }
      }
    }
    window.addEventListener('message', handler)

    // Request project data
    sendMessage('scratchjr:getProject')

    // Timeout after 10 seconds
    setTimeout(() => {
      window.removeEventListener('message', handler)
      resolve(null)
    }, 10000)
  })
}

/** Get project cover image (cached thumbnail from iframe) */
function getProjectCover(): string | null {
  return thumbnail.value || null
}

/** 判断字符串是否为可直接显示的 dataURL */
function isDataUrl(v: string): boolean {
  return typeof v === 'string' && v.indexOf('data:') === 0
}

/** 更新缩略图：请求 iframe 发送封面 */
function refreshThumbnail() {
  if (!isReady.value) {
    ElMessage.warning('ScratchJr编辑器尚未就绪')
    return
  }
  sendMessage('scratchjr:getCover')
}

/**
 * 捕获封面（Promise 形式，供提交前调用）。
 * 发送 scratchjr:getCover，等待 cover/coverData 回传。
 */
function captureThumbnail(): Promise<string | null> {
  return new Promise((resolve) => {
    if (thumbnail.value) {
      resolve(thumbnail.value)
      return
    }
    if (!isReady.value) {
      resolve(null)
      return
    }
    const handler = (ev: MessageEvent) => {
      if (ev.data && (ev.data.type === 'scratchjr:cover' || ev.data.type === 'scratchjr:coverData') && ev.data.data) {
        window.removeEventListener('message', handler)
        thumbnail.value = ev.data.data
        resolve(ev.data.data)
      }
    }
    window.addEventListener('message', handler)
    sendMessage('scratchjr:getCover')
    setTimeout(() => {
      window.removeEventListener('message', handler)
      resolve(thumbnail.value || null)
    }, 5000)
  })
}

/** 导出项目：下载缓存的 sjr 内容为 .sjr 文件 */
function exportProject() {
  if (!cachedProjectContents.value) {
    ElMessage.warning('暂无可导出的项目内容')
    return
  }
  try {
    const blob = new Blob([cachedProjectContents.value], {
      type: 'application/octet-stream',
    })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `${cachedProjectName.value || 'scratchjr-project'}-${Date.now()}.sjr`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    setTimeout(() => URL.revokeObjectURL(url), 1000)
    ElMessage.success('ScratchJr 项目已导出')
  } catch (e: any) {
    ElMessage.error('导出失败：' + (e?.message || e))
  }
}

/** Format code (no-op for block-based editor) */
function formatCode() {
  ElMessage.info('积木编程无需格式化代码')
}

/** Load a project from URL */
function loadProject(url: string) {
  if (!isReady.value) {
    ElMessage.warning('ScratchJr编辑器尚未就绪')
    return
  }
  sendMessage('scratchjr:loadProject', { url })
}

/** Lock the editor */
function lockEditor() {
  // The locked prop is reactive, so the overlay will show automatically
}

/* ===================== Watchers ===================== */

// Watch for external modelValue changes (e.g., from draft loading)
watch(() => props.modelValue, (newVal) => {
  if (!isReady.value || !newVal) return

  // For ScratchJr, we can't directly load sjr data via postMessage
  // The data will be used when the parent calls getProjectData()
  // Skip loading for now - the editor starts with a fresh project
})

/* ===================== Lifecycle ===================== */
onMounted(() => {
  window.addEventListener('message', handleMessage)
  try {
    if (!localStorage.getItem(GUIDE_KEY)) showGuide.value = true
  } catch { /* ignore */ }
})

onBeforeUnmount(() => {
  window.removeEventListener('message', handleMessage)
  // Stop the project when unmounting
  if (isReady.value) {
    sendMessage('scratchjr:stopProject')
  }
})

/* ===================== Expose ===================== */
defineExpose({
  runProject,
  stopProject,
  getProjectData,
  requestProjectData,
  getProjectCover,
  captureThumbnail,
  exportProject,
  exportSb3: exportProject,
  refreshThumbnail,
  formatCode,
  loadProject,
  lockEditor,
})
</script>

<style scoped lang="scss">
.scratchjr-editor {
  position: relative;
  width: 100%;
  height: 100%;
  background: #1D2129;
  overflow: hidden;

  &.locked {
    pointer-events: none;
  }
}

.scratchjr-iframe {
  width: 100%;
  height: 100%;
  border: none;
  display: block;
}

/* 浮层工具条 */
.scratchjr-fab {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 60;
  display: flex;
  gap: 8px;
  padding: 6px 8px;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.18);
}

/* 缩略图预览卡 */
.scratchjr-thumb-card {
  position: absolute;
  right: 12px;
  bottom: 12px;
  z-index: 60;
  width: 168px;
  padding: 8px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.18);

  .thumb-title {
    font-size: 12px;
    color: #4b5563;
    margin-bottom: 6px;
  }

  .thumb-img {
    width: 100%;
    border-radius: 4px;
    display: block;
    background: #fff;
  }

  .thumb-placeholder {
    font-size: 12px;
    color: #9ca3af;
    text-align: center;
    padding: 18px 0;
  }
}

/* Loading overlay */
.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: #1D2129;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 50;

  .loading-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;
    color: #86909C;

    .loading-logo {
      font-size: 48px;
    }

    p {
      font-size: 14px;
      margin: 0;
    }

    .loading-sub {
      font-size: 12px;
      color: #86909C;
    }
  }
}

/* Lock overlay */
.lock-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 100;
  backdrop-filter: blur(4px);

  .lock-content {
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 16px;
    color: #fff;

    p {
      font-size: 16px;
      margin: 0;
    }
  }
}

/* 新手引导浮层 */
.scratchjr-guide {
  position: absolute;
  inset: 0;
  z-index: 200;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(15, 23, 42, 0.5);
  backdrop-filter: blur(2px);

  .guide-card {
    width: 360px;
    max-width: calc(100% - 32px);
    padding: 22px 24px;
    background: #fff;
    border-radius: 12px;
    box-shadow: 0 12px 40px rgba(0, 0, 0, 0.3);
  }

  .guide-title {
    font-size: 17px;
    font-weight: 700;
    color: #1f2937;
    margin-bottom: 12px;
  }

  .guide-list {
    margin: 0 0 18px;
    padding-left: 18px;
    font-size: 13px;
    line-height: 1.95;
    color: #374151;

    b { color: #1677ff; }
  }

  .guide-actions {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .guide-skip {
    font-size: 12px;
    color: #9ca3af;
    cursor: pointer;

    &:hover { color: #6b7280; }
  }
}

.guide-fade-enter-active,
.guide-fade-leave-active {
  transition: opacity 0.25s ease;
}
.guide-fade-enter-from,
.guide-fade-leave-to {
  opacity: 0;
}
</style>
