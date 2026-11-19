<template>
  <!-- 试卷预览弹窗：试卷信息 + 按题型分节的完整试题（含显示答案/解析），供题库中心等复用 -->
  <el-dialog
    :model-value="modelValue"
    title="试卷预览"
    width="860px"
    top="4vh"
    class="paper-preview-dialog"
    :lock-scroll="true"
    :close-on-click-modal="false"
    @update:model-value="close"
  >
    <!-- 试卷信息头部 -->
    <div class="paper-preview-head">
      <div class="pp-head-title-row">
        <h2 class="pp-head-title">{{ meta.title }}</h2>
        <el-switch v-model="showAnswer" active-text="显示答案" inactive-text="隐藏答案" />
      </div>
      <div class="pp-head-meta">
        <span class="pp-head-item">试卷总分 <strong>{{ totalScore }}</strong> 分</span>
        <span class="pp-head-item">及格分 <strong>{{ passScore }}</strong> 分</span>
        <span class="pp-head-item">考试时长 <strong>{{ meta.duration }}</strong> 分钟</span>
        <span class="pp-head-item">题目数量 <strong>{{ questions.length }}</strong> 题</span>
      </div>
    </div>

    <!-- 按题型分节 -->
    <div class="paper-preview-body">
      <div v-for="(group, gi) in groups" :key="group.type" class="pp-section">
        <div class="pp-section-title">
          <span class="pp-section-name">{{ sectionLabel(gi, group.label) }}</span>
          <span class="pp-section-score">共 {{ group.items.length }} 题，{{ groupScore(group) }} 分</span>
        </div>

        <div v-for="q in group.items" :key="q.id" class="pp-question">
          <!-- 题号 + 题干 -->
          <div class="pp-q-head">
            <span class="pp-q-no">{{ questionGlobalNo(q.id) }}.</span>
            <span class="pp-q-title">{{ q.title }}</span>
            <span class="pp-q-score">（{{ q.score }} 分）</span>
          </div>

          <!-- 选择题选项 -->
          <div v-if="q.type === 'choice'" class="pp-options">
            <div
              v-for="(opt, oi) in q.options"
              :key="oi"
              class="pp-option"
              :class="{ 'is-correct': showAnswer && q.answer === String.fromCharCode(65 + oi) }"
            >
              <span class="pp-option-label">{{ String.fromCharCode(65 + oi) }}.</span>
              <span class="pp-option-text">{{ opt }}</span>
              <el-icon v-if="showAnswer && q.answer === String.fromCharCode(65 + oi)" class="pp-option-check"><CircleCheckFilled /></el-icon>
            </div>
          </div>

          <!-- 判断题 -->
          <div v-else-if="q.type === 'judge'" class="pp-options pp-options-inline">
            <span class="pp-option" :class="{ 'is-correct': showAnswer && q.answer === 'true' }">
              <span class="pp-option-label">A.</span> 正确
              <el-icon v-if="showAnswer && q.answer === 'true'" class="pp-option-check"><CircleCheckFilled /></el-icon>
            </span>
            <span class="pp-option" :class="{ 'is-correct': showAnswer && q.answer === 'false' }">
              <span class="pp-option-label">B.</span> 错误
              <el-icon v-if="showAnswer && q.answer === 'false'" class="pp-option-check"><CircleCheckFilled /></el-icon>
            </span>
          </div>

          <!-- 填空题 -->
          <div v-else-if="q.type === 'fill'" class="pp-fill">
            <span class="pp-fill-hint">作答：</span>
            <span class="pp-fill-blank">____________________</span>
          </div>

          <!-- 编程题 / Scratch 题：语言标签 -->
          <div v-if="q.language" class="pp-lang">
            <el-tag size="small" type="info" effect="plain">{{ langLabelOf(q.language) }}</el-tag>
          </div>

          <!-- 答案与解析 -->
          <div v-if="showAnswer && (q.answer || q.analysis)" class="pp-answer-box">
            <div v-if="q.answer" class="pp-answer">
              <span class="pp-answer-label">正确答案：</span>
              <span class="pp-answer-value">{{ formatAnswer(q) }}</span>
            </div>
            <div v-if="q.analysis" class="pp-analysis">
              <span class="pp-analysis-label">解析：</span>
              <span class="pp-analysis-text">{{ q.analysis }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>

    <template #footer>
      <el-button @click="close">关 闭</el-button>
      <el-button type="primary" @click="start">开始练习</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
/**
 * PaperPreviewDialog - 试卷预览弹窗
 * 展示试卷信息 + 按题型分节的完整试题，支持「显示答案/解析」
 * 供题库中心、赛事详情页等复用（通过 paperId 读取共享题源）
 */
import { ref, computed, watch } from 'vue'
import { CircleCheckFilled } from '@element-plus/icons-vue'
import { groupQuestionsByType, calcTotalScore } from '@/utils/exam'
import { fetchPaperQuestions } from '@/api/exam'
import { LANG_LABELS } from '@/types/editor'
import type { EditorLang } from '@/types/editor'
import type { ExamQuestion } from '@/types'

const props = defineProps<{
  modelValue: boolean
  paperId: number
  /** 试卷标题（调用方从后端试卷列表传入） */
  title?: string
  /** 考试时长（分钟，无则隐藏） */
  duration?: number
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', v: boolean): void
  (e: 'start'): void
}>()

const showAnswer = ref(false)

// 题库单源：仅从后端拉取真实题目，初始为空（不再预置本地假题）
const questions = ref<ExamQuestion[]>([])
const groups = computed(() => groupQuestionsByType(questions.value))
const totalScore = computed(() => calcTotalScore(questions.value))
const passScore = computed(() => Math.round(totalScore.value * 0.6))
const meta = computed(() => ({
  title: props.title || '试卷',
  duration: props.duration || 0,
}))

// 打开时重置「显示答案」开关
watch(
  () => props.modelValue,
  (v) => {
    if (v) showAnswer.value = false
  }
)

// 打开/切换试卷时，从后端拉取真实题目（失败保持空态，不再回退假题）
watch(
  () => [props.modelValue, props.paperId] as const,
  async ([open]) => {
    if (open) {
      showAnswer.value = false
      questions.value = []
      try {
        const qs = await fetchPaperQuestions(props.paperId)
        if (Array.isArray(qs)) questions.value = qs
      } catch (e) {
        console.warn('[PaperPreviewDialog] 试卷题目加载失败', e)
      }
    }
  },
  { immediate: true }
)

/** 编程语言标签 */
function langLabelOf(lang?: string): string {
  if (!lang) return ''
  return LANG_LABELS[lang as EditorLang] || lang
}

/** 格式化正确答案显示文本 */
function formatAnswer(q: { type: string; answer?: string; options?: string[] }): string {
  if (!q.answer) return ''
  if (q.type === 'judge') return q.answer === 'true' ? '正确' : '错误'
  if (q.type === 'choice') {
    const idx = q.answer.charCodeAt(0) - 65
    const opt = q.options?.[idx]
    return opt ? `${q.answer}（${opt}）` : q.answer
  }
  return q.answer
}

/** 题目全局题号（按试卷顺序） */
function questionGlobalNo(id: number): number {
  return questions.value.findIndex((q) => q.id === id) + 1
}

/** 分节标题序号（一、二、三...） */
const sectionCnNum = ['一', '二', '三', '四', '五', '六']
function sectionLabel(idx: number, label: string): string {
  return `${sectionCnNum[idx] || idx + 1}、${label}`
}

/** 题型分值小计 */
function groupScore(group: { items: { score: number }[] }): number {
  return group.items.reduce((s, q) => s + (q.score || 0), 0)
}

function close() {
  emit('update:modelValue', false)
}

function start() {
  emit('start')
  emit('update:modelValue', false)
}
</script>

<style lang="scss">
:global(.paper-preview-dialog) {
  .el-dialog__header {
    border-bottom: 1px solid #f0f0f0;
    padding-bottom: 14px;
  }

  .el-dialog__body {
    padding: 20px 24px;
    max-height: 70vh;
    overflow-y: auto;
  }
}
</style>

<style scoped lang="scss">
.paper-preview-head {
  background: linear-gradient(135deg, #f0f5ff 0%, #e8f0ff 100%);
  border: 1px solid rgba(22, 93, 255, 0.12);
  border-radius: 12px;
  padding: 20px 24px;
  margin-bottom: 20px;

  .pp-head-title-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16px;
    margin-bottom: 14px;
  }

  .pp-head-title {
    font-size: 20px;
    font-weight: 700;
    color: $text-title;
    margin: 0;
    line-height: 1.4;
  }

  .pp-head-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 12px 24px;
  }

  .pp-head-item {
    font-size: 14px;
    color: $text-muted;

    strong {
      color: $primary-color;
      font-size: 16px;
      font-weight: 700;
      margin: 0 2px;
    }
  }
}

.paper-preview-body {
  .pp-section {
    margin-bottom: 24px;
  }

  .pp-section-title {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 10px 16px;
    background: #f7f8fa;
    border-left: 4px solid $primary-color;
    border-radius: 6px;
    margin-bottom: 12px;

    .pp-section-name {
      font-size: 15px;
      font-weight: 700;
      color: $text-title;
    }

    .pp-section-score {
      font-size: 12px;
      color: $text-muted;
    }
  }

  .pp-question {
    padding: 14px 16px;
    border-bottom: 1px dashed #e5e6eb;

    &:last-child {
      border-bottom: none;
    }
  }

  .pp-q-head {
    display: flex;
    align-items: flex-start;
    gap: 6px;
    margin-bottom: 10px;

    .pp-q-no {
      font-weight: 700;
      color: $primary-color;
      flex-shrink: 0;
    }

    .pp-q-title {
      flex: 1;
      font-size: 14px;
      color: $text-body;
      line-height: 1.7;
      white-space: pre-wrap;
    }

    .pp-q-score {
      font-size: 12px;
      color: $warning-color;
      font-weight: 600;
      flex-shrink: 0;
      white-space: nowrap;
    }
  }

  .pp-options {
    display: flex;
    flex-direction: column;
    gap: 6px;
    padding-left: 24px;
  }

  .pp-options-inline {
    flex-direction: row;
    gap: 24px;
  }

  .pp-option {
    display: flex;
    align-items: flex-start;
    gap: 6px;
    font-size: 13px;
    color: $text-body;
    line-height: 1.6;

    .pp-option-label {
      font-weight: 600;
      color: $text-title;
      flex-shrink: 0;
    }

    .pp-option-text {
      flex: 1;
    }

    .pp-option-check {
      color: $success-color;
      font-size: 15px;
      flex-shrink: 0;
      margin-top: 2px;
    }

    &.is-correct {
      color: $success-color;
      font-weight: 600;

      .pp-option-label {
        color: $success-color;
      }
    }
  }

  .pp-fill {
    display: flex;
    align-items: center;
    gap: 8px;
    padding-left: 24px;
    font-size: 13px;
    color: $text-muted;

    .pp-fill-blank {
      color: #c9cdd4;
      letter-spacing: 2px;
    }
  }

  .pp-lang {
    padding-left: 24px;
    margin-top: 8px;
  }

  .pp-answer-box {
    margin-top: 12px;
    padding: 12px 16px;
    background: #f7fbf7;
    border: 1px solid rgba(54, 211, 153, 0.25);
    border-radius: 8px;
  }

  .pp-answer {
    display: flex;
    align-items: flex-start;
    gap: 6px;
    font-size: 13px;
    line-height: 1.6;

    .pp-answer-label {
      font-weight: 700;
      color: $success-color;
      flex-shrink: 0;
    }

    .pp-answer-value {
      color: $success-color;
      font-weight: 600;
    }
  }

  .pp-analysis {
    display: flex;
    align-items: flex-start;
    gap: 6px;
    margin-top: 6px;
    font-size: 13px;
    color: $text-muted;
    line-height: 1.6;

    .pp-analysis-label {
      font-weight: 700;
      color: $primary-color;
      flex-shrink: 0;
    }
  }
}
</style>
