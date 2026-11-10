<template>
  <!-- GoC 子编辑器：iframe 嵌入独立 GoC 工程（Monaco + WASM Worker + Canvas） -->
  <div class="goc-child-editor" :class="{ locked: isLocked, 'exam-mode': isExamMode }">
    <!-- 加载状态 -->
    <div v-if="!isReady" class="loading-overlay">
      <div class="loading-content">
        <el-icon :size="40" class="is-loading"><Loading /></el-icon>
        <p>GoC 编辑器加载中...</p>
      </div>
    </div>

    <!-- 锁定遮罩 -->
    <div v-if="isLocked" class="lock-overlay">
      <div class="lock-content">
        <el-icon :size="48"><Lock /></el-icon>
        <p>作品已提交，编辑器已锁定</p>
      </div>
    </div>

    <!-- GoC 编辑器 iframe -->
    <iframe
      ref="gocFrame"
      :src="iframeSrc"
      class="goc-iframe"
      sandbox="allow-scripts allow-same-origin allow-forms allow-popups"
      allowfullscreen
      @load="onIframeLoad"
    ></iframe>
  </div>
</template>

<script setup lang="ts">
/**
 * GoCChildEditor - GoC 子编辑器（iframe 嵌入模式）
 *
 * 嵌入独立 GoC 工程（/goc/index.html），通过 postMessage 通信：
 * - Monaco Editor（C++ 语法高亮）
 * - WebWorker 隔离的 C++ 转译执行引擎
 * - HTML5 Canvas 海龟绘图
 * - 控制台面板
 *
 * 通信协议（父 → iframe）：
 *   goc:loadCode    加载代码到编辑器
 *   goc:run         运行代码
 *   goc:getCode     请求获取当前代码
 *   goc:lock        锁定编辑器
 *
 * 通信协议（iframe → 父）：
 *   goc:ready       编辑器已就绪
 *   goc:codeChange  代码变更
 *   goc:runResult   运行结果
 *   goc:codeData    代码数据响应
 *   goc:submitResult 提交结果
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
  'submit-success': [result: any]
}>()

/* ===================== State ===================== */
const gocFrame = ref<HTMLIFrameElement | null>(null)
const isReady = ref(false)
const isLocked = computed(() => props.locked || false)
const isExamMode = computed(() => props.examMode || false)

/** iframe 源地址 */
const iframeSrc = computed(() => {
  // 构建带参数的 URL，供评委预览和提交功能使用
  const params = new URLSearchParams()
  
  // 评委预览：通过 codeBase64 参数传递代码
  if (props.modelValue && props.modelValue.startsWith('codeBase64:')) {
    params.set('codeBase64', props.modelValue.substring('codeBase64:'.length))
  }
  
  // 从页面 URL 中传递 userId 和 contestId（用于提交作品）
  const pageParams = new URLSearchParams(window.location.search)
  const userId = pageParams.get('userId')
  const contestId = pageParams.get('contestId')
  if (userId) params.set('userId', userId)
  if (contestId) params.set('contestId', contestId)
  
  const query = params.toString()
  return query ? `/goc/index.html?${query}` : '/goc/index.html'
})

/** 缓存的代码内容 */
const cachedCode = ref('')
/** 是否已加载初始代码 */
const hasLoadedInitialData = ref(false)
/** 运行结果 Promise resolve 回调 */
let runResolve: ((value: any) => void) | null = null
/** 获取代码 Promise resolve 回调 */
let getCodeResolve: ((value: string) => void) | null = null

/** 防止循环更新的标志 */
const skipWatcher = ref(false)

/* ===================== PostMessage 通信 ===================== */

function sendMessage(type: string, data?: any) {
  if (gocFrame.value && gocFrame.value.contentWindow) {
    gocFrame.value.contentWindow.postMessage(
      data ? { type, ...data } : { type },
      '*'
    )
  }
}

function handleMessage(event: MessageEvent) {
  if (!event.data || !event.data.type) return

  switch (event.data.type) {
    case 'goc:ready':
      isReady.value = true
      loadInitialCode()
      break

    case 'goc:codeChange':
      handleCodeChange(event.data.code)
      break

    case 'goc:runResult':
      handleRunResult(event.data)
      break

    case 'goc:codeData':
      handleCodeData(event.data.code)
      break

    case 'goc:submitResult':
      handleChildSubmitResult(event.data)
      break
  }
}

/** 处理代码变更 */
function handleCodeChange(code: string) {
  if (!code || code === cachedCode.value) return
  cachedCode.value = code

  // 设置标志防止 watcher 循环
  skipWatcher.value = true
  emit('update:modelValue', code)
}

/** 处理运行结果 */
function handleRunResult(data: any) {
  if (runResolve) {
    runResolve({
      success: data.success,
      output: data.output || '',
      errorMessage: data.errorMessage || '',
    })
    runResolve = null
  }
}

/** 处理代码数据响应 */
function handleCodeData(code: string) {
  if (getCodeResolve) {
    getCodeResolve(code)
    getCodeResolve = null
  }
  // 同时更新缓存
  if (code && code !== cachedCode.value) {
    cachedCode.value = code
    skipWatcher.value = true
    emit('update:modelValue', code)
  }
}

/** 处理 iframe 内部提交结果 */
function handleChildSubmitResult(data: any) {
  if (data.success) {
    // 提交成功，通知父组件锁定编辑器
    emit('submit-success', {
      success: true,
      message: '作品提交成功',
      data: data.data,
    })
  }
}

/** 加载初始代码 */
function loadInitialCode() {
  if (hasLoadedInitialData.value) return
  hasLoadedInitialData.value = true

  const code = props.modelValue || props.templateCode
  if (code) {
    sendMessage('goc:loadCode', { code })
    cachedCode.value = code
  }
}

/* ===================== Iframe 生命周期 ===================== */

function onIframeLoad() {
  // iframe 加载完成，等待 goc:ready 消息
}

/* ===================== 公共方法 ===================== */

/**
 * 运行 GoC 代码
 * 发送运行指令到 iframe，等待运行结果
 */
async function runCode(): Promise<{ success: boolean; output: string; errorMessage?: string; errorLine?: number }> {
  if (!isReady.value) {
    return { success: false, output: '', errorMessage: 'GoC 编辑器尚未就绪，请稍候' }
  }

  return new Promise((resolve) => {
    // 设置超时保护
    const timeoutId = setTimeout(() => {
      if (runResolve) {
        runResolve({
          success: false,
          output: '',
          errorMessage: '运行超时（15秒），可能存在死循环',
        })
        runResolve = null
      }
    }, 15000)

    const originalResolve = resolve
    runResolve = (value) => {
      clearTimeout(timeoutId)
      originalResolve(value)
    }

    sendMessage('goc:run')
  })
}

/** 获取当前代码 */
function getCode(): string {
  return cachedCode.value || props.modelValue || ''
}

/** 异步获取代码（从 iframe 获取最新代码） */
async function getCodeAsync(): Promise<string> {
  if (!isReady.value) {
    return cachedCode.value || props.modelValue || ''
  }

  return new Promise((resolve) => {
    const timeoutId = setTimeout(() => {
      if (getCodeResolve) {
        getCodeResolve(cachedCode.value || '')
        getCodeResolve = null
      }
    }, 3000)

    const originalResolve = resolve
    getCodeResolve = (code) => {
      clearTimeout(timeoutId)
      originalResolve(code)
    }

    sendMessage('goc:getCode')
  })
}

/** 格式化代码 */
function formatCode() {
  // Monaco 编辑器内部可处理格式化，此处无需额外操作
}

/** 清空画布 */
function clearCanvas() {
  if (isReady.value) {
    sendMessage('goc:clearCanvas')
  }
}

/** 重置海龟 */
function resetTurtle() {
  if (isReady.value) {
    sendMessage('goc:resetTurtle')
  }
}

/** 锁定编辑器 */
function lockEditor() {
  if (isReady.value) {
    sendMessage('goc:lock')
  }
}

/** 提交作品（触发 iframe 内部提交流程） */
function submitWork() {
  if (!isReady.value) {
    ElMessage.warning('GoC 编辑器尚未就绪')
    return
  }
  sendMessage('goc:submit')
}

/* ===================== Watchers ===================== */

// 监听外部 modelValue 变化（如草稿加载）
watch(() => props.modelValue, (newVal) => {
  if (!isReady.value || !newVal) return

  // 跳过来自 iframe 自身的更新
  if (skipWatcher.value) {
    skipWatcher.value = false
    return
  }

  // 只在值不同时加载
  if (newVal !== cachedCode.value) {
    sendMessage('goc:loadCode', { code: newVal })
    cachedCode.value = newVal
  }
})

// 监听锁定状态
watch(() => props.locked, (newVal) => {
  if (newVal && isReady.value) {
    sendMessage('goc:lock')
  }
})

/* ===================== 生命周期 ===================== */

onMounted(() => {
  window.addEventListener('message', handleMessage)
})

onBeforeUnmount(() => {
  window.removeEventListener('message', handleMessage)
})

/* ===================== 暴露方法 ===================== */
defineExpose({
  runCode,
  getCode,
  getCodeAsync,
  formatCode,
  clearCanvas,
  resetTurtle,
  lockEditor,
  submitWork,
})
</script>

<style scoped lang="scss">
.goc-child-editor {
  position: relative;
  width: 100%;
  height: 100%;
  background: #0D1117;
  overflow: hidden;

  &.locked {
    pointer-events: none;
  }
}

.goc-iframe {
  width: 100%;
  height: 100%;
  border: none;
  display: block;
}

/* 加载遮罩 */
.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: #0D1117;
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

/* 锁定遮罩 */
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
</style>
