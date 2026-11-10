<template>
  <!-- C++ 子编辑器：Monaco Editor + WebWorker 转译执行引擎 -->
  <div class="cpp-child-editor" ref="containerRef">
    <!-- 练习模式提示：浏览器内为模拟执行，正式判分以服务器为准 -->
    <div class="cpp-practice-notice" role="note">
      <span class="cpp-practice-notice__icon">ℹ</span>
      <span class="cpp-practice-notice__text">
        练习模式：浏览器内的 C++ 运行环境为<strong>模拟执行</strong>（非真实 GCC 编译），仅用于日常练习与即时预览；
        <strong>正式竞赛判分以服务器端评测结果为准</strong>。
      </span>
    </div>
    <div class="monaco-container" ref="monacoRef"></div>
    <!-- stdin 输入面板 -->
    <div class="stdin-panel">
      <div class="stdin-header">
        <span class="stdin-title">输入 (stdin)</span>
        <span class="stdin-hint">多个值用空格或换行分隔</span>
      </div>
      <textarea
        v-model="stdin"
        class="stdin-textarea"
        placeholder="在此输入程序运行时的输入数据，例如：&#10;3 5&#10;hello"
        spellcheck="false"
      ></textarea>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * CppChildEditor - C++ 子编辑器
 * 基于 Monaco Editor，C++ 语法高亮和智能提示
 * 使用 WebWorker 隔离的 C++ → JavaScript 转译引擎在浏览器中直接执行
 *
 * 技术方案：
 * - Monaco Editor 提供代码编辑
 * - WebWorker 隔离执行，不阻塞主线程
 * - C++ → JavaScript 转译器，支持函数定义、变量声明、循环、条件、cout/cin 等
 * - 模拟 std::vector、std::string、std::map、std::sort 等标准库
 * - 超时保护，防止死循环
 */
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { translateError } from '@/utils/errorTranslator'

const props = withDefaults(defineProps<{
  modelValue?: string
  lang?: string
  readOnly?: boolean
  templateCode?: string
  height?: number | string
  theme?: string
  fontSize?: number
}>(), {
  modelValue: '',
  readOnly: false,
  height: 400,
  theme: 'vs',
  fontSize: 14,
})

const emit = defineEmits<{ 'update:modelValue': [value: string] }>()

const containerRef = ref<HTMLElement>()
const monacoRef = ref<HTMLElement>()
let editor: any = null

/** stdin 输入内容 */
const stdin = ref('')

// ========== C++ WebWorker 管理 ==========
let cppWorker: Worker | null = null
let workerReady = false

// ========== 执行状态 ==========
let executionResolve: ((value: { success: boolean; output: string; errorMessage?: string; errorLine?: number }) => void) | null = null
let executionOutput: string[] = []
let executionTimeout: ReturnType<typeof setTimeout> | null = null
const RUN_TIMEOUT = 15000

/** C++ 默认模板代码 */
const cppTemplate = `#include <iostream>
#include <string>
using namespace std;

int main() {
    // 变量声明和赋值
    int a = 10;
    int b = 20;
    int sum = a + b;
    cout << "a + b = " << sum << endl;

    // 循环示例
    for (int i = 1; i <= 5; i++) {
        cout << "第 " << i << " 次循环" << endl;
    }

    // 条件判断
    if (sum > 25) {
        cout << "sum 大于 25" << endl;
    } else {
        cout << "sum 不大于 25" << endl;
    }

    // 字符串操作
    string name = "C++";
    cout << "Hello, " << name << "!" << endl;

    return 0;
}
`

// ========== Monaco Editor 初始化 ==========

onMounted(() => {
  import('monaco-editor').then(monaco => {
    if (!monacoRef.value) return
    editor = monaco.editor.create(monacoRef.value, {
      value: props.modelValue || props.templateCode || cppTemplate,
      language: 'cpp',
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

  // 预加载 Worker
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

// ========== WebWorker 管理 ==========

/** 初始化 WebWorker */
function initWorker() {
  if (cppWorker) return

  try {
    cppWorker = new Worker('/cpp/worker.js?v=' + Date.now())
    cppWorker.onmessage = handleWorkerMessage
    cppWorker.onerror = handleWorkerError
  } catch (err: any) {
    console.error('C++ Worker 创建失败:', err)
  }
}

/** Worker 消息处理 */
function handleWorkerMessage(e: MessageEvent) {
  const data = e.data
  if (!data || !data.type) return

  switch (data.type) {
    case 'ready':
      workerReady = true
      break

    case 'stdout':
      executionOutput.push(data.text || '')
      break

    case 'stderr':
      executionOutput.push(data.text || '')
      break

    case 'done':
      clearExecutionTimeout()
      if (executionResolve) {
        const output = executionOutput.join('')
        if (data.success) {
          executionResolve({
            success: true,
            output: output || '运行成功（无输出）',
          })
        } else {
          const t = translateError(data.error || '运行错误', 'cpp')
          executionResolve({
            success: false,
            output: output,
            errorMessage: t.message,
            errorLine: t.line,
          })
        }
        executionResolve = null
      }
      break
  }
}

/** Worker 错误处理 */
function handleWorkerError(e: ErrorEvent) {
  clearExecutionTimeout()
  const errMsg = e.message || 'Worker 加载失败'

  if (executionResolve) {
    const t = translateError(errMsg, 'cpp')
    executionResolve({
      success: false,
      output: executionOutput.join(''),
      errorMessage: t.message,
      errorLine: t.line,
    })
    executionResolve = null
  }
}

/** 清除执行超时计时器 */
function clearExecutionTimeout() {
  if (executionTimeout) {
    clearTimeout(executionTimeout)
    executionTimeout = null
  }
}

/** 终止并重建 Worker（用于超时恢复） */
function recreateWorker() {
  if (cppWorker) {
    cppWorker.terminate()
    cppWorker = null
  }
  workerReady = false
  setTimeout(() => {
    initWorker()
  }, 100)
}

/** 等待 Worker 就绪 */
function waitForReady(timeoutMs = 5000): Promise<boolean> {
  return new Promise((resolve) => {
    if (workerReady) {
      resolve(true)
      return
    }

    const startTime = Date.now()
    const checkInterval = setInterval(() => {
      if (workerReady) {
        clearInterval(checkInterval)
        resolve(true)
      } else if (Date.now() - startTime > timeoutMs) {
        clearInterval(checkInterval)
        resolve(false)
      }
    }, 100)
  })
}

// ========== 代码操作 ==========

/** 获取当前代码 */
function getCode(): string {
  return editor ? editor.getValue() : (props.modelValue || props.templateCode || cppTemplate)
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

// ========== 运行 C++ 代码 ==========

/** 运行 C++ 代码（使用 WebWorker 在浏览器中执行） */
async function runCode(): Promise<{ success: boolean; output: string; errorMessage?: string; errorLine?: number }> {
  const code = getCode()
  if (!code.trim()) {
    return { success: false, output: '', errorMessage: '代码为空，请先编写代码' }
  }

  // 确保 Worker 就绪
  const ready = await waitForReady()
  if (!ready || !cppWorker) {
    return { success: false, output: '', errorMessage: 'C++ 运行环境加载失败，请刷新页面重试' }
  }

  // 重置执行状态
  executionOutput = []

  return new Promise((resolve) => {
    // 设置超时保护
    executionTimeout = setTimeout(() => {
      if (executionResolve) {
        executionResolve({
          success: false,
          output: executionOutput.join(''),
          errorMessage: '运行超时（15秒），可能存在死循环',
        })
        executionResolve = null
        // 终止并重建 Worker（超时后 Worker 可能卡在死循环中）
        recreateWorker()
      }
    }, RUN_TIMEOUT)

    executionResolve = resolve

    // 发送运行指令
    cppWorker!.postMessage({ type: 'run', code, stdin: stdin.value })
  })
}

onBeforeUnmount(() => {
  clearExecutionTimeout()
  if (cppWorker) {
    cppWorker.terminate()
    cppWorker = null
  }
  editor?.dispose()
})

defineExpose({ runCode, getCode, formatCode, insertSnippet, stopRun, setStdin, revealLine })
</script>

<style scoped lang="scss">
.cpp-child-editor {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* 练习模式提示横幅 */
.cpp-practice-notice {
  display: flex;
  align-items: flex-start;
  gap: 6px;
  padding: 7px 12px;
  background: #FFF7E8;
  border-bottom: 1px solid #FFE4BA;
  font-size: 12.5px;
  line-height: 1.5;
  color: #8A6D3B;
}

.cpp-practice-notice__icon {
  flex: 0 0 auto;
  font-style: normal;
  font-weight: 700;
  color: #D4860B;
  line-height: 1.5;
}

.cpp-practice-notice__text {
  flex: 1;
}

.cpp-practice-notice__text strong {
  color: #B5730A;
  font-weight: 600;
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
  height: 80px;
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
</style>
