<template>
  <div class="scratch3-editor" :class="{ locked: isLocked, 'exam-mode': isExamMode }" :style="editorStyle">
    <!-- 加载状态 -->
    <div v-if="!isReady" class="loading-overlay">
      <div class="loading-content">
        <el-icon :size="40" class="is-loading"><Loading /></el-icon>
        <p>Scratch 编辑器加载中...</p>
      </div>
    </div>

    <!-- 锁定遮罩 -->
    <div v-if="isLocked" class="lock-overlay">
      <div class="lock-content">
        <el-icon :size="48"><Lock /></el-icon>
        <p>作品已提交，编辑器已锁定</p>
      </div>
    </div>

    <!-- Scratch 编辑器 iframe -->
    <iframe
      ref="scratchFrame"
      src="/scratch/index.html"
      class="scratch-iframe"
      sandbox="allow-scripts allow-same-origin allow-downloads allow-forms allow-popups"
      allowfullscreen
      @load="onIframeLoad"
    ></iframe>

    <!-- 缩略图预览卡（底部动作栏已承载 示例/导出/缩略图 操作，避免浮层遮挡舞台） -->
    <div v-if="isReady && thumbnail" class="scratch-thumb-card">
      <div class="thumb-title">作品缩略图</div>
      <img :src="thumbnail" alt="舞台缩略图" class="thumb-img" />
    </div>

    <!-- 示例项目对话框 -->
    <el-dialog
      v-model="exampleDialogVisible"
      title="示例项目"
      width="540px"
      append-to-body
      :close-on-click-modal="true"
    >
      <div class="example-grid">
        <div
          v-for="ex in examples"
          :key="ex.id"
          class="example-tile"
          @click="loadExample(ex.id)"
        >
          <el-icon class="example-icon"><Picture /></el-icon>
          <div class="example-name">{{ ex.name }}</div>
          <div class="example-desc">{{ ex.desc }}</div>
        </div>
      </div>
      <template #footer>
        <el-button @click="exampleDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>

    <!-- 新手引导（首次进入显示，可关闭并记忆） -->
    <transition name="guide-fade">
      <div v-if="showGuide" class="scratch-guide">
        <div class="guide-card">
          <div class="guide-title">🎨 欢迎使用 Scratch 创作</div>
          <ul class="guide-list">
            <li><b>运行作品</b>：点击舞台上方绿色 ▶ 旗帜开始运行</li>
            <li><b>搭积木</b>：从左侧「积木区」拖拽脚本到中间「脚本区」</li>
            <li><b>看效果</b>：在中间「舞台区」观察角色动作</li>
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
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Lock, Loading, Picture } from '@element-plus/icons-vue'

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
const scratchFrame = ref<HTMLIFrameElement | null>(null)
const isReady = ref(false)
const isRunning = ref(false)
const isLocked = computed(() => props.locked || false)
const isExamMode = computed(() => props.examMode || false)

/** 新手引导：首次进入显示，关闭后写入 localStorage 不再提示 */
const showGuide = ref(false)
const GUIDE_KEY = 'scratch_guide_seen_v1'
function dismissGuide() {
  showGuide.value = false
  try { localStorage.setItem(GUIDE_KEY, '1') } catch { /* ignore */ }
}

/** Cached project JSON from the iframe (updated via postMessage) */
const cachedProjectJson = ref('')
/** Whether we have loaded initial project data */
const hasLoadedInitialData = ref(false)

/** 舞台缩略图（dataURL，由 iframe 经 postMessage 回传） */
const thumbnail = ref('')
/** 示例项目对话框可见性 */
const exampleDialogVisible = ref(false)
/** 示例项目列表（JSON 文件托管于 /scratch/examples/） */
const examples = ref<{ id: string; name: string; desc: string }[]>([
  { id: 'move-cat', name: '移动小猫', desc: '绿旗启动后，小猫重复移动并转向' },
  { id: 'self-intro', name: '自我介绍', desc: '小猫说“你好，世界！”2 秒' },
  { id: 'draw-square', name: '画正方形', desc: '落笔后用重复积木画出一个正方形' },
])

/** 缩略图请求的 Promise resolver（等待 STAGE_THUMBNAIL_DATA 回传） */
let thumbResolve: ((v: string | null) => void) | null = null
let thumbTimer: number | null = null

const editorStyle = computed(() => {
  // For Scratch, always fill the parent container (100% height)
  // The parent (.editor-main in BaseEditor) already has flex:1
  return {
    height: '100%',
  }
})

/* ===================== PostMessage Communication ===================== */
function sendMessage(type: string, data?: any) {
  if (scratchFrame.value && scratchFrame.value.contentWindow) {
    scratchFrame.value.contentWindow.postMessage(
      data ? { type, data } : { type },
      '*'
    )
  }
}

function handleMessage(event: MessageEvent) {
  if (!event.data || !event.data.type) return

  switch (event.data.type) {
    case 'SCRATCH_READY':
      isReady.value = true
      // Load initial project data if available
      loadInitialProject()
      break

    case 'SCRATCH_ERROR':
      ElMessage.error(`Scratch编辑器初始化失败: ${event.data.error}`)
      break

    case 'PROJECT_JSON_DATA':
      handleProjectJson(event.data.data)
      break

    case 'PROJECT_JSON_ERROR':
      console.error('Failed to get project JSON:', event.data.error)
      break

    case 'PROJECT_SB3_DATA':
      // iframe 回传 base64 编码的 sb3 二进制，触发下载
      downloadSb3(event.data.data)
      break

    case 'PROJECT_SB3_ERROR':
      console.error('Failed to get project SB3:', event.data.error)
      ElMessage.error('导出 sb3 失败：' + (event.data.error || '未知错误'))
      break

    case 'STAGE_THUMBNAIL_DATA':
      thumbnail.value = event.data.data
      if (thumbResolve) {
        thumbResolve(event.data.data)
        thumbResolve = null
      }
      if (thumbTimer !== null) {
        clearTimeout(thumbTimer)
        thumbTimer = null
      }
      break

    case 'STAGE_THUMBNAIL_ERROR':
      console.error('Failed to get stage thumbnail:', event.data.error)
      if (thumbResolve) {
        thumbResolve(null)
        thumbResolve = null
      }
      break

    case 'LOAD_SUCCESS':
      ElMessage.success('项目加载成功')
      break

    case 'LOAD_ERROR':
      ElMessage.error(`项目加载失败: ${event.data.error}`)
      break

    case 'RUNTIME_STARTED':
      isRunning.value = true
      break

    case 'RUNTIME_STOPPED':
      isRunning.value = false
      break
  }
}

/** Flag to prevent circular updates: when we receive JSON from the editor
 *  and emit update:modelValue, the watcher would fire and send it back.
 *  This flag tells the watcher to skip the next update. */
const skipWatcher = ref(false)

/** Handle received project JSON from the iframe */
function handleProjectJson(json: string) {
  if (!json) return

  // Skip if the JSON hasn't changed (avoid unnecessary updates)
  if (json === cachedProjectJson.value) return
  cachedProjectJson.value = json

  // Set flag to prevent the watcher from sending this back to the editor
  skipWatcher.value = true

  // Emit update to parent (v-model)
  emit('update:modelValue', json)

  // Emit save-scratch for the BaseEditor to handle
  emit('save-scratch', {
    type: 'scratch3',
    contents: json,
    json: json,
    assets: [],
    name: 'Scratch项目',
    timestamp: Date.now(),
  })
}

/** Load initial project data from modelValue or templateCode */
function loadInitialProject() {
  if (hasLoadedInitialData.value) return
  hasLoadedInitialData.value = true

  const projectJson = props.modelValue || props.templateCode
  if (projectJson) {
    // Validate it's a valid JSON string
    try {
      JSON.parse(projectJson)
      sendMessage('LOAD_PROJECT_JSON', projectJson)
    } catch {
      // Not valid JSON, skip loading
      console.warn('Initial project data is not valid JSON, skipping load')
    }
  }
}

/* ===================== Iframe Lifecycle ===================== */
function onIframeLoad() {
  // Iframe loaded, waiting for SCRATCH_READY message
}

/* ===================== Public Methods ===================== */

/** Run the Scratch project (green flag) */
function runProject() {
  if (!isReady.value) {
    ElMessage.warning('Scratch编辑器尚未就绪，请稍候')
    return
  }
  sendMessage('RUN_PROJECT')
}

/** Stop the Scratch project */
function stopProject() {
  if (!isReady.value) return
  sendMessage('STOP_PROJECT')
}

/**
 * Get project data synchronously (returns cached JSON).
 * The cache is updated automatically via postMessage when the project changes.
 */
function getProjectData() {
  const json = cachedProjectJson.value
  if (!json) return null
  return {
    type: 'scratch3',
    contents: json,
    json: json,
    assets: [],
    name: 'Scratch项目',
    timestamp: Date.now(),
  }
}

/** Get project cover image (stage thumbnail captured from the iframe) */
function getProjectCover(): string | null {
  return thumbnail.value || null
}

/** 打开示例项目对话框 */
function openExamples() {
  exampleDialogVisible.value = true
}

/** 加载示例项目（fetch JSON 后通过 LOAD_PROJECT_JSON 灌入 iframe） */
async function loadExample(id: string) {
  try {
    const resp = await fetch(`/scratch/examples/${id}.json`)
    if (!resp.ok) throw new Error('示例文件不存在')
    const json = await resp.text()
    JSON.parse(json) // 校验
    sendMessage('LOAD_PROJECT_JSON', json)
    exampleDialogVisible.value = false
    ElMessage.success('示例项目已加载，可点击绿旗运行')
  } catch (e: any) {
    ElMessage.error('加载示例失败：' + (e?.message || e))
  }
}

/** 导出 sb3：请求 iframe 生成 sb3 并返回 base64，随后在 PROJECT_SB3_DATA 中下载 */
function exportSb3() {
  if (!isReady.value) {
    ElMessage.warning('Scratch编辑器尚未就绪')
    return
  }
  sendMessage('GET_PROJECT_SB3')
}

/** 将 base64 的 sb3 二进制解码并触发浏览器下载 */
function downloadSb3(base64: string) {
  try {
    const binary = atob(base64)
    const bytes = new Uint8Array(binary.length)
    for (let i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i)
    const blob = new Blob([bytes], { type: 'application/octet-stream' })
    const url = URL.createObjectURL(blob)
    const a = document.createElement('a')
    a.href = url
    a.download = `scratch-project-${Date.now()}.sb3`
    document.body.appendChild(a)
    a.click()
    document.body.removeChild(a)
    setTimeout(() => URL.revokeObjectURL(url), 1000)
    ElMessage.success('sb3 文件已导出')
  } catch (e: any) {
    ElMessage.error('导出 sb3 失败：' + (e?.message || e))
  }
}

/** 更新缩略图（发送请求，iframe 回传后自动刷新预览） */
function refreshThumbnail() {
  if (!isReady.value) {
    ElMessage.warning('Scratch编辑器尚未就绪')
    return
  }
  sendMessage('GET_STAGE_THUMBNAIL')
}

/**
 * 捕获舞台缩略图（Promise 形式，供提交前调用以附带封面）。
 * 发送 GET_STAGE_THUMBNAIL，等待 iframe 回传 STAGE_THUMBNAIL_DATA。
 */
function captureThumbnail(): Promise<string | null> {
  return new Promise((resolve) => {
    if (!isReady.value) {
      resolve(thumbnail.value || null)
      return
    }
    thumbResolve = resolve
    if (thumbTimer !== null) clearTimeout(thumbTimer)
    thumbTimer = window.setTimeout(() => {
      thumbTimer = null
      thumbResolve = null
      resolve(thumbnail.value || null)
    }, 5000)
    sendMessage('GET_STAGE_THUMBNAIL')
  })
}

/** Format code (no-op for block-based editor) */
function formatCode() {
  ElMessage.info('积木编程无需格式化代码')
}

/** Load a project from JSON string */
function loadProject(jsonString: string) {
  if (!isReady.value) {
    ElMessage.warning('Scratch编辑器尚未就绪')
    return
  }
  sendMessage('LOAD_PROJECT_JSON', jsonString)
}

/** Lock the editor */
function lockEditor() {
  // The locked prop is reactive, so the overlay will show automatically
}

/* ===================== Watchers ===================== */

// Watch for external modelValue changes (e.g., from draft loading)
watch(() => props.modelValue, (newVal) => {
  if (!isReady.value || !newVal) return
  
  // Skip if this update came from the editor itself (circular update prevention)
  if (skipWatcher.value) {
    skipWatcher.value = false
    return
  }
  
  // Only load if the value is different from what we have cached
  if (newVal !== cachedProjectJson.value) {
    try {
      JSON.parse(newVal)
      sendMessage('LOAD_PROJECT_JSON', newVal)
    } catch {
      // Invalid JSON, skip
    }
  }
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
    sendMessage('STOP_PROJECT')
  }
})

/* ===================== Expose ===================== */
defineExpose({
  runProject,
  stopProject,
  getProjectData,
  getProjectCover,
  captureThumbnail,
  exportSb3,
  openExamples,
  loadExample,
  refreshThumbnail,
  formatCode,
  loadProject,
  lockEditor,
})
</script>

<style scoped lang="scss">
.scratch3-editor {
  position: relative;
  width: 100%;
  height: 100%;
  background: #1D2129;
  overflow: hidden;

  &.locked {
    pointer-events: none;
  }
}

.scratch-iframe {
  width: 100%;
  height: 100%;
  border: none;
  display: block;
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

    p {
      font-size: 14px;
      margin: 0;
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

/* 缩略图预览卡 */
.scratch-thumb-card {
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
}

/* 示例项目网格 */
.example-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.example-tile {
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 14px 10px;
  text-align: center;
  cursor: pointer;
  transition: all 0.15s;

  &:hover {
    border-color: #4c97ff;
    box-shadow: 0 2px 8px rgba(76, 151, 255, 0.3);
    transform: translateY(-2px);
  }

  .example-icon {
    font-size: 28px;
    color: #4c97ff;
  }

  .example-name {
    font-size: 14px;
    font-weight: 600;
    margin-top: 6px;
    color: #1f2937;
  }

  .example-desc {
    font-size: 12px;
    color: #6b7280;
    margin-top: 4px;
    line-height: 1.4;
  }
}

/* 新手引导浮层 */
.scratch-guide {
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
