<template>
  <!-- 运行输出面板：支持普通输出和判题结果展示 -->
  <div class="editor-output">
    <div class="output-header">
      <span class="output-title">运行输出</span>
      <div class="output-actions">
        <!-- 判题结果标签 -->
        <template v-if="judgeResult">
          <el-tag :type="judgeTagType" size="small">
            {{ judgeStatusLabel }}
          </el-tag>
          <el-tag type="info" size="small" v-if="judgeResult.passedCases !== undefined">
            {{ judgeResult.passedCases }}/{{ judgeResult.totalCases }}
          </el-tag>
        </template>
        <!-- 运行状态标签 + 进度动画 -->
        <template v-else>
          <div class="run-status-group">
            <el-tag :type="statusType" size="small">
              <template v-if="status === 'running'">
                <span class="running-dot"></span>
                {{ statusText }}
              </template>
              <template v-else>{{ statusText }}</template>
            </el-tag>
            <!-- 运行进度条（运行时显示） -->
            <div v-if="status === 'running'" class="run-progress-bar">
              <div class="run-progress-fill"></div>
            </div>
            <span v-if="status === 'running'" class="run-timer">{{ runTimerDisplay }}</span>
          </div>
        </template>
        <span v-if="output" class="output-count">{{ lineCount }} 行 · {{ charCount }} 字符</span>
        <el-button size="small" text @click="copyOutput" title="复制输出">复制</el-button>
        <el-button size="small" text @click="$emit('clear')">清空</el-button>
      </div>
    </div>
    <div class="output-body">
      <!-- 错误行号跳转提示 -->
      <div v-if="status === 'error' && errorLine" class="error-jump">
        <el-icon><TopRight /></el-icon>
        <span>出错位置：第 <b>{{ errorLine }}</b> 行</span>
        <el-button size="small" type="primary" link @click="$emit('reveal-line', errorLine)">
          在编辑器中定位
        </el-button>
      </div>
      <!-- 判题详情 -->
      <template v-if="judgeResult && judgeResult.caseResults">
        <div v-for="c in judgeResult.caseResults" :key="c.caseId" class="case-result">
          <div class="case-header">
            <el-icon :color="c.passed ? '#36D399' : '#F53F3F'">
              <CircleCheckFilled v-if="c.passed" />
              <CircleCloseFilled v-else />
            </el-icon>
            <span>用例 {{ c.caseId }}</span>
            <span v-if="c.timeUsed" class="case-time">{{ c.timeUsed }}ms</span>
          </div>
          <div v-if="!c.passed && c.actualOutput" class="case-detail">
            <div class="case-line"><span class="label">期望：</span><pre>{{ c.expectedOutput }}</pre></div>
            <div class="case-line"><span class="label">实际：</span><pre>{{ c.actualOutput }}</pre></div>
          </div>
        </div>
      </template>
      <!-- 普通输出 -->
      <template v-else>
        <pre>{{ output || '等待运行...' }}</pre>
      </template>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * EditorOutput - 运行输出面板
 * 支持普通运行输出和判题结果展示
 */
import { computed, ref, watch, onBeforeUnmount } from 'vue'
import { CircleCheckFilled, CircleCloseFilled, TopRight } from '@element-plus/icons-vue'
import type { RunStatus, JudgeResult, JudgeStatus } from '@/types/editor'
import { JUDGE_STATUS_LABELS } from '@/types/editor'
import { ElMessage } from 'element-plus'

defineEmits<{
  clear: []
  'reveal-line': [line: number]
}>()

// ========== 输出统计与复制 ==========
const lineCount = computed(() => {
  if (!props.output) return 0
  return props.output.split('\n').length
})
const charCount = computed(() => props.output?.length || 0)

async function copyOutput() {
  if (!props.output) {
    ElMessage.warning('暂无输出可复制')
    return
  }
  try {
    await navigator.clipboard.writeText(props.output)
    ElMessage.success('输出已复制')
  } catch {
    // 降级到 execCommand
    const ta = document.createElement('textarea')
    ta.value = props.output
    document.body.appendChild(ta)
    ta.select()
    try { document.execCommand('copy') } catch { /* ignore */ }
    document.body.removeChild(ta)
    ElMessage.success('输出已复制')
  }
}

const props = withDefaults(defineProps<{
  output?: string
  status?: RunStatus
  judgeResult?: JudgeResult | null
  errorLine?: number
}>(), {
  output: '',
  status: 'idle',
  judgeResult: null,
  errorLine: undefined,
})

// ========== 运行计时器 ==========
const runTimerDisplay = ref('0.0s')
let timerInterval: ReturnType<typeof setInterval> | null = null
let timerStart = 0

watch(() => props.status, (newStatus) => {
  if (newStatus === 'running') {
    timerStart = Date.now()
    timerInterval = setInterval(() => {
      const elapsed = ((Date.now() - timerStart) / 1000).toFixed(1)
      runTimerDisplay.value = `${elapsed}s`
    }, 100)
  } else if (timerInterval) {
    clearInterval(timerInterval)
    timerInterval = null
  }
})

onBeforeUnmount(() => {
  if (timerInterval) clearInterval(timerInterval)
})

const statusType = computed(() => {
  const map: Record<string, 'info' | 'warning' | 'success' | 'danger'> = { idle: 'info', running: 'warning', success: 'success', error: 'danger' }
  return map[props.status] || 'info'
})

const statusText = computed(() => {
  const map: Record<string, string> = { idle: '就绪', running: '运行中', success: '运行成功', error: '运行错误' }
  return map[props.status] || '就绪'
})

const judgeStatusLabel = computed(() => {
  if (!props.judgeResult) return ''
  return JUDGE_STATUS_LABELS[props.judgeResult.status as JudgeStatus] || '未知'
})

const judgeTagType = computed((): 'info' | 'warning' | 'success' | 'danger' => {
  if (!props.judgeResult) return 'info'
  const typeMap: Record<string, 'info' | 'warning' | 'success' | 'danger'> = {
    PENDING: 'info', RUNNING: 'warning', ACCEPTED: 'success',
    WRONG_ANSWER: 'danger', TIME_LIMIT_EXCEEDED: 'warning', MEMORY_LIMIT_EXCEEDED: 'warning',
    RUNTIME_ERROR: 'danger', COMPILE_ERROR: 'danger', SYSTEM_ERROR: 'danger',
  }
  return typeMap[props.judgeResult.status] || 'info'
})
</script>

<style scoped lang="scss">
.editor-output {
  border-top: 1px solid #4E5969;
  background: #1D2129;
  flex-shrink: 0;
  max-height: 240px;
  display: flex;
  flex-direction: column;
}

.output-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 6px 12px;
  background: #1D2129;
  border-bottom: 1px solid #4E5969;
  flex-shrink: 0;
}

.output-title {
  font-size: 13px;
  color: #86909C;
}

.output-actions {
  display: flex;
  align-items: center;
  gap: 6px;
}

.output-count {
  font-size: 11px;
  color: #86909C;
  font-family: 'Consolas', 'Monaco', monospace;
}

// ========== 运行进度动画 ==========
.run-status-group {
  display: flex;
  align-items: center;
  gap: 8px;
}

.running-dot {
  display: inline-block;
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #FF9500;
  margin-right: 4px;
  animation: runningPulse 1s ease-in-out infinite;
  will-change: transform, opacity;
}

@keyframes runningPulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.4; transform: scale(0.7); }
}

.run-progress-bar {
  width: 80px;
  height: 4px;
  background: #4E5969;
  border-radius: 2px;
  overflow: hidden;

  .run-progress-fill {
    width: 30%;
    height: 100%;
    background: linear-gradient(90deg, #FF9500, #FFD700);
    border-radius: 2px;
    animation: progressSlide 2s ease-in-out infinite;
    will-change: transform, opacity;
  }
}

@keyframes progressSlide {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(400%); }
}

.run-timer {
  font-size: 11px;
  color: #FF9500;
  font-family: 'Consolas', 'Monaco', monospace;
  min-width: 30px;
}

.output-body {
  padding: 12px;
  overflow-y: auto;
  flex: 1;
}

.error-jump {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  margin-bottom: 8px;
  background: rgba(245, 63, 63, 0.12);
  border: 1px solid rgba(245, 63, 63, 0.4);
  border-radius: 4px;
  font-size: 12px;
  color: #FF7875;

  .el-icon {
    font-size: 14px;
  }

  b {
    color: #FFA39E;
  }

  .el-button {
    margin-left: auto;
    font-size: 12px;
  }
}

pre {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 13px;
  color: #E5E6EB;
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  line-height: 1.6;
}

.case-result {
  margin-bottom: 8px;
  padding: 6px 8px;
  background: rgba(255, 255, 255, 0.03);
  border-radius: 4px;

  .case-header {
    display: flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: #E5E6EB;
  }

  .case-time {
    font-size: 11px;
    color: #86909C;
    margin-left: auto;
  }
}

.case-detail {
  margin-top: 4px;
  padding-left: 22px;

  .case-line {
    display: flex;
    gap: 8px;
    margin-bottom: 2px;

    .label {
      font-size: 12px;
      color: #86909C;
      flex-shrink: 0;
    }

    pre {
      font-size: 12px;
      color: #86909C;
    }
  }
}
</style>
