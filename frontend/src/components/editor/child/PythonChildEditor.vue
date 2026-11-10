<template>
  <!-- Python 子编辑器：Monaco Editor + Pyodide 运行时 + Turtle 画布 -->
  <div class="python-child-editor" ref="containerRef">
    <div class="monaco-container" ref="monacoRef"></div>
    <!-- stdin 输入面板 -->
    <div class="stdin-panel">
      <div class="stdin-header">
        <span class="stdin-title">输入 (stdin)</span>
        <span class="stdin-hint">每行对应一次 input() 调用</span>
      </div>
      <textarea
        v-model="stdin"
        class="stdin-textarea"
        placeholder="在此输入程序运行时的输入数据，每行对应一次 input() 调用"
        spellcheck="false"
      ></textarea>
    </div>
    <!-- Turtle 画布区域（仅 pythonturtle 语言显示；支持被父级 Teleport 到右栏图形输出 tab） -->
    <Teleport v-if="lang === 'pythonturtle'" :to="turtleHost" :disabled="!turtleHost">
      <div class="turtle-panel">
      <div class="turtle-header">
        <span class="turtle-title">
          <span class="turtle-dot"></span>
          Turtle 绘图
        </span>
        <div class="turtle-actions">
          <span v-if="pyodideLoading" class="pyodide-status">{{ pyodideProgressText }}</span>
          <template v-else>
            <el-button size="small" text :type="showGrid ? 'primary' : 'default'" @click="showGrid = !showGrid">网格</el-button>
            <el-button size="small" text title="缩小" @click="zoom = Math.max(0.5, +(zoom - 0.2).toFixed(1))">−</el-button>
            <span class="zoom-label">{{ Math.round(zoom * 100) }}%</span>
            <el-button size="small" text title="放大" @click="zoom = Math.min(2, +(zoom + 0.2).toFixed(1))">＋</el-button>
            <el-button size="small" text @click="resetCanvas">重置</el-button>
          </template>
        </div>
      </div>
      <div class="turtle-canvas-wrapper" ref="turtleCanvasWrapperRef">
        <canvas
          ref="turtleCanvasRef"
          :width="canvasWidth"
          :height="canvasHeight"
          class="turtle-canvas"
          :style="{ transform: `scale(${zoom})`, transformOrigin: 'center' }"
        ></canvas>
        <!-- Pyodide 初始化进度遮罩 -->
        <div v-if="pyodideLoading" class="pyodide-loading-overlay">
          <div class="loading-content">
            <el-icon class="is-loading" :size="28"><Loading /></el-icon>
            <span class="loading-text">{{ pyodideProgressText || '正在加载 Pyodide...' }}</span>
            <el-progress
              :percentage="pyodideProgress"
              :stroke-width="4"
              :show-text="false"
              style="width: 200px; margin-top: 8px;"
            />
          </div>
        </div>
        <!-- 加载失败提示 -->
        <div v-if="pyodideError && !pyodideLoading" class="pyodide-error-overlay">
          <div class="error-content">
            <el-icon :size="28" color="#F53F3F"><WarningFilled /></el-icon>
            <span class="error-text">Pyodide 加载失败</span>
            <span class="error-detail">{{ pyodideError }}</span>
          </div>
        </div>
      </div>
    </div>
    </Teleport>
    <!-- 非 Turtle 模式下也显示 Pyodide 加载状态 -->
    <div v-if="lang !== 'pythonturtle' && pyodideLoading && pyodideProgress < 100" class="pyodide-status-bar">
      <el-icon class="is-loading" :size="14"><Loading /></el-icon>
      <span>{{ pyodideProgressText || '正在加载 Pyodide...' }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * PythonChildEditor - Python / Python Turtle 子编辑器
 * 基于 Monaco Editor 提供 Python 语法高亮和智能提示
 * 集成 Pyodide（CPython WASM 运行时），支持在浏览器中直接运行完整 Python 3 代码
 * Python Turtle 通过自定义 Canvas 渲染层实现绘图
 *
 * 迁移自 Skulpt → Pyodide，获得：
 * - 完整 Python 3 语法支持（f-string、类型注解、海象运算符等）
 * - 更好的标准库兼容性
 * - WebWorker 隔离执行，不阻塞主线程
 */
import { ref, computed, onMounted, onBeforeUnmount, watch, nextTick } from 'vue'
import { Loading, WarningFilled } from '@element-plus/icons-vue'
import { translateError } from '@/utils/errorTranslator'

const props = withDefaults(defineProps<{
  modelValue?: string
  lang?: string
  readOnly?: boolean
  templateCode?: string
  height?: number | string
  theme?: string
  fontSize?: number
  /** 父级提供的图形输出宿主元素；提供时 Turtle 画布将被 Teleport 到该元素（右栏图形输出 tab） */
  turtleHost?: HTMLElement | null
}>(), {
  modelValue: '',
  readOnly: false,
  height: 400,
  theme: 'vs',
  fontSize: 14,
  turtleHost: null,
})

const emit = defineEmits<{ 'update:modelValue': [value: string] }>()

const containerRef = ref<HTMLElement>()
const monacoRef = ref<HTMLElement>()
const turtleCanvasWrapperRef = ref<HTMLElement>()
const turtleCanvasRef = ref<HTMLCanvasElement>()
let editor: any = null

/** stdin 输入内容 */
const stdin = ref('')

// ========== Canvas 配置 ==========
const canvasWidth = 480
const canvasHeight = 360
/** 是否显示网格与坐标轴 */
const showGrid = ref(true)
/** 画布缩放（仅预览缩放，不影响坐标映射） */
const zoom = ref(1)

// ========== Pyodide Worker 状态 ==========
let pyodideWorker: Worker | null = null
const pyodideReady = ref(false)
const pyodideLoading = ref(false)
const pyodideProgress = ref(0)
const pyodideProgressText = ref('')
const pyodideError = ref('')

// ========== 执行状态 ==========
let executionResolve: ((value: { success: boolean; output: string; errorMessage?: string; errorLine?: number }) => void) | null = null
let executionOutput: string[] = []
let executionTimeout: ReturnType<typeof setTimeout> | null = null
const RUN_TIMEOUT = 15000

// ========== Turtle 渲染状态 ==========
// 注：turtle 状态由 Worker 侧 Python 代码维护，主线程仅负责渲染绘图命令
// 无需在主线程重复跟踪位置/角度/可见性

/** 默认模板代码（根据语言） */
const defaultTemplate = computed(() => {
  if (props.lang === 'pythonturtle') {
    return 'import turtle\n\nt = turtle.Turtle()\nt.forward(100)\nt.right(90)\nt.forward(100)\nt.right(90)\nt.forward(100)\nt.right(90)\nt.forward(100)\n'
  }
  return '# 在这里编写 Python 代码\nprint("Hello, World!")\n'
})

// ========== Monaco Editor 初始化 ==========

onMounted(() => {
  import('monaco-editor').then(monaco => {
    if (!monacoRef.value) return
    editor = monaco.editor.create(monacoRef.value, {
      value: props.modelValue || props.templateCode || defaultTemplate.value,
      language: 'python',
      theme: props.theme,
      fontSize: 14,
      readOnly: props.readOnly,
      minimap: { enabled: false },
      automaticLayout: true,
      scrollBeyondLastLine: false,
      tabSize: 4,
      insertSpaces: true,
      wordWrap: 'on',
      lineNumbers: 'on',
      renderLineHighlight: 'line',
      suggestOnTriggerCharacters: true,
      quickSuggestions: true,
      folding: true,
      bracketPairColorization: { enabled: true },
    })

    // 同步初始值到父组件（Monaco create 不触发 onDidChangeModelContent）
    emit('update:modelValue', editor.getValue())

    editor.onDidChangeModelContent(() => {
      emit('update:modelValue', editor.getValue())
    })
  }).catch(err => {
    console.error('Monaco editor 加载失败:', err)
  })

  // 如果是 Turtle 模式，初始化画布
  if (props.lang === 'pythonturtle') {
    nextTick(() => {
      initCanvas()
    })
  }

  // 预加载 Pyodide Worker
  initWorker()
})

watch(() => props.modelValue, (val) => {
  if (editor && val !== undefined && val !== editor.getValue()) {
    editor.setValue(val)
  }
})

watch(() => props.readOnly, (val) => {
  if (editor) editor.updateOptions({ readOnly: val })
})

watch(() => props.theme, (newTheme) => {
  if (editor) {
    import('monaco-editor').then(monaco => {
      monaco.editor.setTheme(newTheme)
    })
  }
})

watch(() => props.fontSize, (val) => {
  if (editor && val) editor.updateOptions({ fontSize: val })
})

// ========== Pyodide Worker 管理 ==========

/** 初始化 WebWorker（预加载 Pyodide） */
function initWorker() {
  if (pyodideWorker) return

  pyodideLoading.value = true
  pyodideError.value = ''

  try {
    pyodideWorker = new Worker('/pyodide/worker.js')
    pyodideWorker.onmessage = handleWorkerMessage
    pyodideWorker.onerror = handleWorkerError
  } catch (err: any) {
    pyodideLoading.value = false
    pyodideError.value = 'Worker 创建失败: ' + (err?.message || String(err))
  }
}

/** Worker 消息处理 */
function handleWorkerMessage(e: MessageEvent) {
  const data = e.data
  if (!data || !data.type) return

  switch (data.type) {
    case 'ready':
      pyodideReady.value = true
      pyodideLoading.value = false
      pyodideProgress.value = 100
      break

    case 'progress':
      pyodideProgress.value = data.percent || 0
      pyodideProgressText.value = data.text || ''
      break

    case 'stdout':
      executionOutput.push(data.text || '')
      break

    case 'stderr':
      executionOutput.push(data.text || '')
      break

    case 'turtle':
      renderTurtleCommand(data.command)
      break

    case 'done':
      clearExecutionTimeout()
      if (executionResolve) {
        const output = executionOutput.join('')
        if (data.success) {
          executionResolve({ success: true, output })
        } else {
          const t = translateError(data.error || '运行错误', props.lang || 'python')
          executionResolve({ success: false, output, errorMessage: t.message, errorLine: t.line })
        }
        executionResolve = null
      }
      break

    case 'error':
      clearExecutionTimeout()
      // 初始化阶段错误
      if (data.phase === 'init') {
        pyodideLoading.value = false
        pyodideError.value = data.error || 'Pyodide 加载失败'
      }
      // 执行阶段错误
      if (executionResolve) {
        const output = executionOutput.join('')
        const t = translateError(data.error || '运行错误', props.lang || 'python')
        executionResolve({
          success: false,
          output: output,
          errorMessage: t.message,
          errorLine: t.line,
        })
        executionResolve = null
      }
      break
  }
}

/** Worker 错误处理 */
function handleWorkerError(e: ErrorEvent) {
  clearExecutionTimeout()
  pyodideLoading.value = false
  const errMsg = e.message || 'Worker 加载失败，可能是网络问题导致 Pyodide CDN 不可达'

  if (executionResolve) {
    const t = translateError(errMsg, props.lang || 'python')
    executionResolve({
      success: false,
      output: executionOutput.join(''),
      errorMessage: t.message,
      errorLine: t.line,
    })
    executionResolve = null
  } else {
    pyodideError.value = errMsg
  }
}

/** 清除执行超时计时器 */
function clearExecutionTimeout() {
  if (executionTimeout) {
    clearTimeout(executionTimeout)
    executionTimeout = null
  }
}

/** 等待 Pyodide 就绪 */
function waitForReady(timeoutMs = 30000): Promise<boolean> {
  return new Promise((resolve) => {
    if (pyodideReady.value) {
      resolve(true)
      return
    }
    if (pyodideError.value) {
      resolve(false)
      return
    }

    const startTime = Date.now()
    const checkInterval = setInterval(() => {
      if (pyodideReady.value) {
        clearInterval(checkInterval)
        resolve(true)
      } else if (pyodideError.value) {
        clearInterval(checkInterval)
        resolve(false)
      } else if (Date.now() - startTime > timeoutMs) {
        clearInterval(checkInterval)
        resolve(false)
      }
    }, 200)
  })
}

/** 终止并重建 Worker（用于超时恢复） */
function recreateWorker() {
  if (pyodideWorker) {
    pyodideWorker.terminate()
    pyodideWorker = null
  }
  pyodideReady.value = false
  pyodideLoading.value = false
  pyodideProgress.value = 0
  pyodideError.value = ''
  // 延迟重建，避免立即重建导致的问题
  setTimeout(() => {
    initWorker()
  }, 100)
}

// ========== Turtle Canvas 渲染 ==========

/** 初始化画布（白色背景 + 网格/坐标轴） */
function initCanvas() {
  const canvas = turtleCanvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  if (!ctx) return
  ctx.fillStyle = '#ffffff'
  ctx.fillRect(0, 0, canvasWidth, canvasHeight)
  drawGrid(ctx)
}

/** 绘制网格与坐标轴（淡色网格 + 深色中轴线） */
function drawGrid(ctx: CanvasRenderingContext2D) {
  if (!showGrid.value) return
  const step = 40
  ctx.save()
  ctx.lineWidth = 1
  // 浅色网格
  ctx.strokeStyle = '#EEF1F5'
  ctx.beginPath()
  for (let x = (canvasWidth / 2) % step; x < canvasWidth; x += step) {
    ctx.moveTo(x, 0); ctx.lineTo(x, canvasHeight)
  }
  for (let y = (canvasHeight / 2) % step; y < canvasHeight; y += step) {
    ctx.moveTo(0, y); ctx.lineTo(canvasWidth, y)
  }
  ctx.stroke()
  // 坐标轴
  ctx.strokeStyle = '#C9CDD4'
  ctx.lineWidth = 1.5
  ctx.beginPath()
  ctx.moveTo(0, canvasHeight / 2); ctx.lineTo(canvasWidth, canvasHeight / 2)
  ctx.moveTo(canvasWidth / 2, 0); ctx.lineTo(canvasWidth / 2, canvasHeight)
  ctx.stroke()
  // 原点标记
  ctx.fillStyle = '#C9CDD4'
  ctx.font = '10px Arial'
  ctx.fillText('(0,0)', canvasWidth / 2 + 3, canvasHeight / 2 - 3)
  ctx.restore()
}

/** 坐标变换：数学坐标 → Canvas 像素坐标 */
function toCanvasX(x: number): number {
  return canvasWidth / 2 + x
}
function toCanvasY(y: number): number {
  return canvasHeight / 2 - y
}

/** 渲染 Turtle 绘图命令 */
function renderTurtleCommand(cmd: any) {
  const canvas = turtleCanvasRef.value
  if (!canvas || !cmd) return
  const ctx = canvas.getContext('2d')
  if (!ctx) return

  switch (cmd.op) {
    case 'line': {
      ctx.strokeStyle = cmd.color || 'black'
      ctx.lineWidth = Math.max(1, cmd.width || 1)
      ctx.lineCap = 'round'
      ctx.beginPath()
      ctx.moveTo(toCanvasX(cmd.x1), toCanvasY(cmd.y1))
      ctx.lineTo(toCanvasX(cmd.x2), toCanvasY(cmd.y2))
      ctx.stroke()
      break
    }
    case 'clear': {
      ctx.fillStyle = '#ffffff'
      ctx.fillRect(0, 0, canvasWidth, canvasHeight)
      break
    }
    case 'fill': {
      if (cmd.path && cmd.path.length > 2) {
        ctx.fillStyle = cmd.color || 'black'
        ctx.beginPath()
        ctx.moveTo(toCanvasX(cmd.path[0][0]), toCanvasY(cmd.path[0][1]))
        for (let i = 1; i < cmd.path.length; i++) {
          ctx.lineTo(toCanvasX(cmd.path[i][0]), toCanvasY(cmd.path[i][1]))
        }
        ctx.closePath()
        ctx.fill()
      }
      break
    }
    case 'text': {
      ctx.fillStyle = cmd.color || 'black'
      const fontSize = cmd.size || 12
      ctx.font = `${fontSize}px Arial`
      ctx.textBaseline = 'bottom'
      ctx.fillText(cmd.text || '', toCanvasX(cmd.x), toCanvasY(cmd.y))
      break
    }
    case 'dot': {
      ctx.fillStyle = cmd.color || 'black'
      ctx.beginPath()
      ctx.arc(toCanvasX(cmd.x), toCanvasY(cmd.y), cmd.r || 3, 0, Math.PI * 2)
      ctx.fill()
      break
    }
    case 'stamp': {
      // 在指定位置绘制海龟箭头
      drawTurtleCursor(ctx, cmd.x, cmd.y, cmd.heading || 0)
      break
    }
    case 'hideturtle':
      // turtle 可见性由 Worker 侧 Python 代码维护
      break
    case 'showturtle':
      break
  }
}

/** 绘制海龟光标（小箭头） */
function drawTurtleCursor(ctx: CanvasRenderingContext2D, x: number, y: number, heading: number) {
  const cx = toCanvasX(x)
  const cy = toCanvasY(y)
  const rad = -heading * Math.PI / 180 // Canvas y 轴翻转

  ctx.save()
  ctx.translate(cx, cy)
  ctx.rotate(rad)
  ctx.fillStyle = '#36D399'
  ctx.strokeStyle = '#36D399'
  ctx.lineWidth = 1
  ctx.beginPath()
  ctx.moveTo(8, 0)
  ctx.lineTo(-4, -5)
  ctx.lineTo(-4, 5)
  ctx.closePath()
  ctx.fill()
  ctx.stroke()
  ctx.restore()
}

/** 清空 Turtle 画布（保留网格/坐标轴） */
function clearTurtleCanvas() {
  const canvas = turtleCanvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  if (!ctx) return
  ctx.fillStyle = '#ffffff'
  ctx.fillRect(0, 0, canvasWidth, canvasHeight)
  drawGrid(ctx)
}

/** 重置画布（清空绘制并恢复网格/坐标轴） */
function resetCanvas() {
  clearTurtleCanvas()
  zoom.value = 1
}

// ========== 代码操作 ==========

/** 获取当前代码 */
function getCode(): string {
  return editor ? editor.getValue() : (props.modelValue || props.templateCode || defaultTemplate.value)
}

/** 格式化代码 */
function formatCode() {
  if (editor) {
    editor.getAction('editor.action.formatDocument')?.run()
  }
}

/** 在光标处插入代码片段（供父级模板库调用） */
function insertSnippet(text: string): boolean {
  if (!editor) return false
  const selection = editor.getSelection()
  if (!selection) return false
  editor.executeEdits('base-template', [{ range: selection, text, forceMoveMarkers: true }])
  editor.pushUndoStop()
  editor.focus()
  return true
}

/** 停止运行（终止 Worker 并重建） */
function stopRun() {
  if (executionResolve) {
    clearExecutionTimeout()
    executionResolve({ success: false, output: executionOutput.join(''), errorMessage: '已手动停止' })
    executionResolve = null
  }
  recreateWorker()
}

/** 设置 stdin 输入内容（供样例一键填入） */
function setStdin(text: string) {
  stdin.value = text || ''
}

// ========== 运行 Python 代码 ==========

/** 运行 Python 代码（使用 Pyodide Worker 在浏览器中执行） */
async function runCode(): Promise<{ success: boolean; output: string; errorMessage?: string; errorLine?: number }> {
  const code = getCode()
  if (!code.trim()) {
    return { success: false, output: '', errorMessage: '代码为空，请先编写代码' }
  }

  // 确保 Worker 已创建
  if (!pyodideWorker) {
    initWorker()
  }

  // 等待 Pyodide 就绪
  if (!pyodideReady.value) {
    const ready = await waitForReady()
    if (!ready) {
      return {
        success: false,
        output: '',
        errorMessage: pyodideError.value || 'Pyodide 初始化超时，请检查网络后刷新页面重试',
      }
    }
  }

  // 重置输出和画布
  executionOutput = []
  const isTurtle = props.lang === 'pythonturtle'
  if (isTurtle) {
    clearTurtleCanvas()
  }

  // 发送代码到 Worker 执行
  return new Promise((resolve) => {
    executionResolve = resolve

    // 设置超时计时器
    executionTimeout = setTimeout(() => {
      // 超时处理：终止 Worker 并重建
      recreateWorker()
      resolve({
        success: false,
        output: executionOutput.join(''),
        errorMessage: `运行超时（超过 ${RUN_TIMEOUT / 1000} 秒），可能存在死循环`,
      })
      executionResolve = null
    }, RUN_TIMEOUT)

    pyodideWorker!.postMessage({
      type: 'run',
      code,
      isTurtle,
      stdin: stdin.value,
    })
  })
}

// ========== 生命周期清理 ==========

onBeforeUnmount(() => {
  clearExecutionTimeout()
  if (pyodideWorker) {
    pyodideWorker.terminate()
    pyodideWorker = null
  }
  editor?.dispose()
})

/** 定位到指定行（运行错误跳转） */
function revealLine(line: number): boolean {
  if (!editor) return false
  editor.revealLineInCenter(line)
  editor.setSelection({
    startLineNumber: line,
    startColumn: 1,
    endLineNumber: line,
    endColumn: 1,
  })
  editor.focus()
  return true
}

defineExpose({ runCode, getCode, formatCode, clearTurtleCanvas, insertSnippet, stopRun, setStdin, revealLine })
</script>

<style scoped lang="scss">
.python-child-editor {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.monaco-container {
  flex: 1;
  min-height: 200px;
  border-bottom: 1px solid #E5E6EB;
}

/* stdin 输入面板 */
.stdin-panel {
  display: flex;
  flex-direction: column;
  background: #F7F8FA;
  border-top: 1px solid #E5E6EB;
}

.stdin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 6px 12px;
  background: #FFF;
  border-bottom: 1px solid #E5E6EB;
}

.stdin-title {
  font-size: 13px;
  font-weight: 600;
  color: #1D2129;
}

.stdin-hint {
  font-size: 12px;
  color: #86909C;
}

.stdin-textarea {
  width: 100%;
  height: 70px;
  padding: 8px 12px;
  border: none;
  background: #F7F8FA;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 13px;
  line-height: 1.5;
  color: #1D2129;
  resize: vertical;
  outline: none;
  box-sizing: border-box;

  &::placeholder {
    color: #C9CDD4;
  }

  &:focus {
    background: #FFF;
  }
}

.turtle-panel {
  flex-shrink: 0;
  border-top: 1px solid #4E5969;
  background: #1D2129;
  display: flex;
  flex-direction: column;
}

.turtle-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 12px;
  border-bottom: 1px solid #4E5969;
}

.turtle-title {
  font-size: 12px;
  color: #86909C;
  display: flex;
  align-items: center;
  gap: 6px;
}

.turtle-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #36D399;
  display: inline-block;
}

.turtle-actions {
  display: flex;
  align-items: center;
  gap: 8px;

  .zoom-label {
    font-size: 12px;
    color: #86909C;
    min-width: 38px;
    text-align: center;
    font-family: 'Consolas', 'Monaco', monospace;
  }
}

.pyodide-status {
  font-size: 11px;
  color: #86909C;
}

.turtle-canvas-wrapper {
  padding: 8px;
  background: $bg-white;
  min-height: 360px;
  display: flex;
  justify-content: center;
  align-items: center;
  overflow: auto;
  position: relative;
}

.turtle-canvas {
  border: 1px solid #E5E6EB;
  border-radius: 4px;
  background: $bg-white;
}

/* Pyodide 加载进度遮罩 */
.pyodide-loading-overlay {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.92);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
}

.loading-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.loading-text {
  font-size: 13px;
  color: #4E5969;
  margin-top: 4px;
}

/* Pyodide 错误遮罩 */
.pyodide-error-overlay {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.92);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
}

.error-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  max-width: 300px;
  text-align: center;
}

.error-text {
  font-size: 14px;
  color: #F53F3F;
  font-weight: 500;
}

.error-detail {
  font-size: 12px;
  color: #86909C;
  word-break: break-word;
}

/* 非 Turtle 模式下的状态栏 */
.pyodide-status-bar {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 12px;
  background: #1D2129;
  border-top: 1px solid #4E5969;
  font-size: 11px;
  color: #86909C;
  flex-shrink: 0;
}
</style>
