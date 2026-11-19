<template>
  <!-- 答卷页面：集成 BaseEditor，支持代码题/Scratch题动态切换编辑器 -->
  <div class="exam-taking-page">
    <!-- 顶部进度条 -->
    <div class="top-progress-bar">
      <div class="progress-track">
        <div
          class="progress-fill"
          :style="{ width: progressPercent + '%' }"
          :class="{ 'progress-complete': progressPercent >= 100 }"
        ></div>
      </div>
      <div class="progress-info">
        <span class="progress-text">答题进度 {{ answeredCount }}/{{ questions.length }}</span>
        <div class="countdown-timer" :class="{ 'countdown-warning': remainingTime <= 300, 'countdown-danger': remainingTime <= 60 }">
          <el-icon><Timer /></el-icon>
          <span>{{ formattedCountdown }}</span>
        </div>
      </div>
    </div>

    <!-- 试卷信息条：总分 / 及格分 / 时长 / 题量 -->
    <div class="exam-info-bar">
      <div class="exam-info-title">
        <el-icon><Document /></el-icon>
        {{ examMeta.title }}
      </div>
      <div class="exam-info-items">
        <div class="exam-info-item">
          <span class="exam-info-label">试卷总分</span>
          <span class="exam-info-value">{{ totalScore }} 分</span>
        </div>
        <div class="exam-info-item">
          <span class="exam-info-label">及格分</span>
          <span class="exam-info-value">{{ passScore }} 分</span>
        </div>
        <div class="exam-info-item">
          <span class="exam-info-label">考试时长</span>
          <span class="exam-info-value">{{ examDurationMin }} 分钟</span>
        </div>
        <div class="exam-info-item">
          <span class="exam-info-label">题目数量</span>
          <span class="exam-info-value">{{ questions.length }} 题</span>
        </div>
      </div>
    </div>

    <div class="exam-main">
      <!-- 题目导航（按题型分节） -->
      <div class="question-nav">
        <div class="nav-title">
          <el-icon><Grid /></el-icon>
          题目导航
        </div>
        <div class="nav-groups">
          <div v-for="group in questionGroups" :key="group.type" class="nav-group">
            <div class="nav-group-title">
              {{ group.label }}
              <span class="nav-group-count">{{ group.items.length }}</span>
            </div>
            <div class="nav-grid">
              <div
                v-for="question in group.items"
                :key="question.id"
                class="nav-item"
                :class="{
                  active: currentQuestion?.id === question.id,
                  answered: isAnswered(question.id),
                  marked: isMarked(question.id),
                }"
                @click="jumpToQuestion(question.id)"
              >
                <span class="nav-number">{{ questionIndex(question.id) + 1 }}</span>
                <span v-if="isAnswered(question.id)" class="nav-status">
                  <el-icon><Check /></el-icon>
                </span>
                <span v-else-if="isMarked(question.id)" class="nav-status marked-dot"></span>
              </div>
            </div>
          </div>
        </div>
        <div class="nav-legend">
          <div class="legend-item">
            <span class="legend-dot answered"></span>
            <span>已答</span>
          </div>
          <div class="legend-item">
            <span class="legend-dot marked"></span>
            <span>标记</span>
          </div>
          <div class="legend-item">
            <span class="legend-dot unanswered"></span>
            <span>未答</span>
          </div>
        </div>
        <div class="nav-stats">
          <div class="stats-bar">
            <div class="stats-fill" :style="{ width: progressPercent + '%' }"></div>
          </div>
          <span class="stats-text">已答 {{ answeredCount }}/{{ questions.length }}</span>
        </div>
      </div>

      <!-- 题目内容 -->
      <div class="question-content">
        <div class="question-header">
          <div class="header-left">
            <span class="question-number">第 {{ currentIndex + 1 }} 题</span>
            <el-tag size="small" :type="questionTypeTag" class="type-tag">
              {{ questionTypeLabel }}
            </el-tag>
            <el-tag size="small" v-if="currentQuestion?.language" type="info" class="lang-tag">
              {{ langLabel }}
            </el-tag>
          </div>
          <div class="header-right">
            <span class="question-score">{{ currentQuestion?.score }}分</span>
            <el-button
              text
              size="small"
              class="mark-btn"
              :type="isMarked(currentQuestion?.id) ? 'warning' : 'info'"
              @click="toggleMark(currentQuestion?.id)"
            >
              <el-icon><Flag /></el-icon>
              {{ isMarked(currentQuestion?.id) ? '取消标记' : '标记题目' }}
            </el-button>
          </div>
        </div>

        <div class="question-body">
          <h3 class="question-title">{{ currentQuestion?.title }}</h3>

          <!-- 选择题 -->
          <div v-if="currentQuestion?.type === 'choice'" class="choice-options">
            <div
              v-for="(opt, i) in currentQuestion.options"
              :key="i"
              class="option-item"
              :class="{ selected: currentAnswer === String.fromCharCode(65 + i) }"
              @click="selectOption(String.fromCharCode(65 + i))"
            >
              <div class="option-radio">
                <div class="radio-inner" :class="{ checked: currentAnswer === String.fromCharCode(65 + i) }"></div>
              </div>
              <span class="option-label">{{ String.fromCharCode(65 + i) }}</span>
              <span class="option-text">{{ opt }}</span>
            </div>
          </div>

          <!-- 填空题 -->
          <div v-else-if="currentQuestion?.type === 'fill'" class="fill-input">
            <el-input
              v-model="currentAnswer"
              placeholder="请输入答案"
              size="large"
              @input="handleAnswerChange"
              class="fill-answer-input"
            />
          </div>

          <!-- 判断题 -->
          <div v-else-if="isJudgeQuestion" class="judge-options">
            <div
              class="judge-option"
              :class="{ selected: currentAnswer === 'true' }"
              @click="selectJudge('true')"
            >
              <div class="option-radio">
                <div class="radio-inner" :class="{ checked: currentAnswer === 'true' }"></div>
              </div>
              <span class="judge-icon judge-correct">
                <el-icon><Check /></el-icon>
              </span>
              <span class="option-text">正确</span>
            </div>
            <div
              class="judge-option"
              :class="{ selected: currentAnswer === 'false' }"
              @click="selectJudge('false')"
            >
              <div class="option-radio">
                <div class="radio-inner" :class="{ checked: currentAnswer === 'false' }"></div>
              </div>
              <span class="judge-icon judge-wrong">
                <el-icon><Close /></el-icon>
              </span>
              <span class="option-text">错误</span>
            </div>
          </div>

          <!-- 代码题 / Scratch题：使用 BaseEditor -->
          <div v-else-if="isCodeQuestion" class="code-area">
            <BaseEditor
              ref="baseEditorRef"
              :config="codeEditorConfig"
              :initial-code="currentAnswer"
              :editor-height="'100%'"
              :question-no="currentIndex + 1"
              :countdown-seconds="remainingTime"
              @code-change="handleCodeAnswerChange"
              @submit-success="handleCodeSubmitSuccess"
            />
          </div>
        </div>

        <!-- 上一题/下一题 -->
        <div class="question-nav-buttons">
          <el-button @click="handlePrev" :disabled="currentIndex === 0" class="nav-btn">
            <el-icon><ArrowLeft /></el-icon>
            上一题
          </el-button>
          <el-button
            v-if="currentIndex < questions.length - 1"
            type="primary"
            @click="handleNext"
            class="nav-btn next-btn"
          >
            下一题
            <el-icon><ArrowRight /></el-icon>
          </el-button>
          <el-button
            v-else
            type="success"
            @click="handleSubmit"
            class="nav-btn submit-btn"
          >
            <el-icon><Check /></el-icon>
            提交试卷
          </el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * ExamTakingPage - 答卷页面
 * 集成 BaseEditor，通过题目类型和语言动态切换编辑器
 * 代码题和 Scratch 题统一使用 BaseEditor 组件
 */
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useExamStore } from '@/stores/exam'
import { ElMessage } from 'element-plus'
import { useExamSubmit } from '@/hooks/useExamSubmit'
import { Timer, Grid, Check, Flag, ArrowLeft, ArrowRight, Document, Close } from '@element-plus/icons-vue'
import BaseEditor from '@/components/editor/BaseEditor.vue'
import type { EditorLang, EditorConfig } from '@/types/editor'
import { LANG_LABELS } from '@/types/editor'
import { enterExam, fetchPaperQuestions } from '@/api/exam'
import { groupQuestionsByType, calcTotalScore, QUESTION_TYPE_LABEL } from '@/utils/exam'
import type { QuestionType } from '@/types'

const route = useRoute()
const router = useRouter()
const examStore = useExamStore()
const { submit: submitExamFlow } = useExamSubmit()
const baseEditorRef = ref<InstanceType<typeof BaseEditor>>()

// ========== 倒计时 ==========
const remainingTime = ref(3600) // 默认60分钟
let countdownTimer: ReturnType<typeof setInterval> | null = null

const formattedCountdown = computed(() => {
  const h = Math.floor(remainingTime.value / 3600)
  const m = Math.floor((remainingTime.value % 3600) / 60)
  const s = remainingTime.value % 60
  if (h > 0) {
    return `${String(h).padStart(2, '0')}:${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
  }
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
})

// ========== 题目数据 ==========
// 题目只来自后端（fetchPaperQuestions）或错题重练题集，不再回退本地假题库
const remoteQuestions = ref<any[]>([])

/** 是否为错题重练模式（通过 ?mode=wrong 标记） */
const isWrongMode = computed(() => route.query.mode === 'wrong')

const questions = computed(() => {
  // 错题重练：优先使用错题集
  if (isWrongMode.value && examStore.overrideQuestions && examStore.overrideQuestions.length > 0) {
    return examStore.overrideQuestions
  }
  return remoteQuestions.value
})

const currentIndex = ref(0)
const currentAnswer = ref('')
const markedQuestions = ref<Set<number>>(new Set())

// ========== 试卷信息条 ==========
const examMeta = computed(() => {
  // 错题重练模式：用自定义标题与时长
  if (isWrongMode.value && examStore.overrideTitle) {
    return {
      id: Number(route.params.examId),
      title: examStore.overrideTitle,
      duration: examStore.overrideDuration || 60,
      passScore: 60,
    }
  }
  // 正常模式：优先取当前考试信息（进入前已设置），否则用通用默认
  return {
    id: Number(route.params.examId),
    title: examStore.currentExam?.title || '在线考试',
    duration: examStore.currentExam?.duration || 60,
    passScore: 60,
  }
})
const totalScore = computed(() => calcTotalScore(questions.value))
const passScore = computed(() => Math.round(totalScore.value * 0.6))
const examDurationMin = computed(() => examMeta.value.duration)
/** 按题型分节的题目分组（选择题/填空题/判断题/编程题/Scratch题） */
const questionGroups = computed(() => groupQuestionsByType(questions.value))

// ========== 计算属性 ==========
const currentQuestion = computed(() => questions.value[currentIndex.value])

const questionTypeLabel = computed(() => {
  const t = currentQuestion.value?.type as QuestionType | undefined
  return t ? QUESTION_TYPE_LABEL[t] : ''
})

const questionTypeTag = computed((): 'info' | 'primary' | 'success' | 'warning' | 'danger' => {
  const map: Record<string, 'info' | 'primary' | 'success' | 'warning' | 'danger'> = {
    choice: 'info', fill: 'info', judge: 'success', code: 'warning', scratch: 'warning', scratchjr: 'warning',
  }
  return map[currentQuestion.value?.type] || 'info'
})

const langLabel = computed(() => {
  if (!currentQuestion.value?.language) return ''
  return LANG_LABELS[currentQuestion.value.language as EditorLang] || ''
})

const isCodeQuestion = computed(() => {
  const t = currentQuestion.value?.type
  return t === 'code' || t === 'scratch' || t === 'scratchjr'
})

const isJudgeQuestion = computed(() => currentQuestion.value?.type === 'judge')

const answeredCount = computed(() => {
  return questions.value.filter(q => examStore.getAnswer(q.id)).length
})

const progressPercent = computed(() => {
  if (questions.value.length === 0) return 0
  return Math.round((answeredCount.value / questions.value.length) * 100)
})

/** 代码题编辑器配置 */
const codeEditorConfig = computed<EditorConfig>(() => {
  const q = currentQuestion.value
  return {
    lang: (q?.language || 'python') as EditorLang,
    mode: 'exam',
    questionId: q?.id || 0,
    examId: Number(route.params.examId),
    readOnly: false,
    templateCode: q?.templateCode || '',
  }
})

// ========== 监听题目切换 ==========
watch(currentIndex, () => {
  const q = currentQuestion.value
  const saved = examStore.getAnswer(q.id)
  currentAnswer.value = saved?.toString() || q?.templateCode || ''
})

// ========== 方法 ==========

function isAnswered(id: number) {
  return !!examStore.getAnswer(id)
}

function isMarked(id?: number) {
  if (!id) return false
  return markedQuestions.value.has(id)
}

function toggleMark(id?: number) {
  if (!id) return
  if (markedQuestions.value.has(id)) {
    markedQuestions.value.delete(id)
  } else {
    markedQuestions.value.add(id)
  }
}

function selectOption(value: string) {
  currentAnswer.value = value
  handleAnswerChange()
}

function selectJudge(value: string) {
  currentAnswer.value = value
  handleAnswerChange()
}

/** 题目 id → 全局索引 */
function questionIndex(id: number): number {
  return questions.value.findIndex((q) => q.id === id)
}

/** 跳转到指定题目 */
function jumpToQuestion(id: number) {
  const i = questionIndex(id)
  if (i >= 0) currentIndex.value = i
}

function handleAnswerChange() {
  examStore.saveAnswer(currentQuestion.value.id, currentAnswer.value as any)
}

function handleCodeAnswerChange(code: string) {
  currentAnswer.value = code
  examStore.saveAnswer(currentQuestion.value.id, code as any)
}

function handleCodeSubmitSuccess() {
  // 代码提交成功
}

function handlePrev() {
  if (currentIndex.value > 0) currentIndex.value--
}

function handleNext() {
  if (currentIndex.value < questions.value.length - 1) currentIndex.value++
}

async function handleSubmit() {
  const examId = Number(route.params.examId)
  const ok = await submitExamFlow(examId)
  if (ok) {
    clearDraft()
    ElMessage.success(`交卷成功！已答 ${answeredCount.value}/${questions.value.length} 题`)
    setTimeout(() => router.push(`/exam/result/${examId}`), 1200)
  }
}

// ========== 3 分钟自动保存 ==========
let autosaveTimer: ReturnType<typeof setInterval> | null = null

function draftKey(examId: number): string {
  // 错题重练用独立草稿 key，避免污染原试卷草稿
  const suffix = route.query.mode === 'wrong' ? '-wrong' : ''
  return `exam-draft-${examId}${suffix}`
}

/** 保存答案草稿到 localStorage（断点续答） */
function autosave() {
  const examId = Number(route.params.examId)
  if (!examId) return
  const data = Object.fromEntries(examStore.answers)
  localStorage.setItem(draftKey(examId), JSON.stringify(data))
}

/** 恢复答案草稿 */
function restoreDraft() {
  const examId = Number(route.params.examId)
  if (!examId) return
  const raw = localStorage.getItem(draftKey(examId))
  if (!raw) return
  try {
    const data = JSON.parse(raw)
    const keys = Object.keys(data)
    if (keys.length === 0) return
    for (const [k, v] of Object.entries(data)) {
      examStore.saveAnswer(Number(k), String(v))
    }
    ElMessage.info(`已恢复上次保存的 ${keys.length} 道题答案`)
  } catch (e) {
    console.warn('[autosave] 草稿解析失败', e)
  }
}

/** 清除草稿 */
function clearDraft() {
  const examId = Number(route.params.examId)
  if (examId) localStorage.removeItem(draftKey(examId))
}

// ========== 初始化 ==========
// examId 从路由获取，onMounted 中调用 enterExam 获取真实试卷；失败时明确提示并继续使用本地题库
if (!examStore.started) {
  const durationMin = examMeta.value.duration || 60
  remainingTime.value = durationMin * 60
  examStore.remainingTime = durationMin * 60
  examStore.started = true
}

onMounted(async () => {
  // 启动倒计时
  countdownTimer = setInterval(() => {
    if (remainingTime.value > 0) {
      remainingTime.value--
    }
  }, 1000)

  if (isWrongMode.value) {
    // 错题重练：不恢复原试卷草稿（错题集不同），不拉取远程题目（避免覆盖）
  } else {
    // 正常考试：清理可能残留的错题重练题集，并恢复草稿
    examStore.clearOverrideQuestions()
    restoreDraft()
  }

  // 启动 3 分钟自动保存
  autosaveTimer = setInterval(autosave, 180000)

  // 获取真实试卷题目（错题重练模式跳过）
  if (!isWrongMode.value) {
    const examId = Number(route.params.examId)
    if (examId) {
      try {
        const res = await enterExam(examId)
        // 保存 recordId，供交卷时提交到后端落库
        if (res?.data?.id) {
          examStore.setRecordId(res.data.id)
        }
        // 后端返回的试卷ID（examId 与 paperId 是两个独立主键，不能混用）
        const paperId = (res?.data as any)?.paperId
        // 后端若直接返回题目则使用
        const questions = (res?.data as any)?.questions
        if (Array.isArray(questions) && questions.length > 0) {
          remoteQuestions.value = questions
        } else if (paperId) {
          // 用试卷ID拉取题目（而非考试ID）
          const qs = await fetchPaperQuestions(paperId)
          if (Array.isArray(qs)) {
            remoteQuestions.value = qs
          }
        } else {
          ElMessage.warning('该考试未关联试卷，暂无题目')
        }
      } catch (e) {
        console.warn('[ExamTaking] 获取试卷题目失败', e)
        ElMessage.error('试卷题目获取失败，请稍后重试')
      }
    }
  }
})

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
  if (autosaveTimer) {
    clearInterval(autosaveTimer)
    autosaveTimer = null
  }
  // 未交卷时离开，自动保存一次（断点续答）
  if (!examStore.submitted && examStore.answers.size > 0) {
    autosave()
  }
})
</script>

<style scoped lang="scss">

.exam-taking-page {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: $bg-page;
}

// 试卷信息条
.exam-info-bar {
  background: $bg-white;
  padding: 14px 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  flex-wrap: wrap;
  border-bottom: 1px solid #e5e6eb;

  .exam-info-title {
    display: flex;
    align-items: center;
    gap: 8px;
    font-size: 16px;
    font-weight: 700;
    color: $text-title;

    .el-icon {
      color: $primary-color;
      font-size: 18px;
    }
  }

  .exam-info-items {
    display: flex;
    gap: 16px;
    flex-wrap: wrap;
  }

  .exam-info-item {
    display: flex;
    align-items: baseline;
    gap: 6px;
    padding: 6px 14px;
    background: #f7f8fa;
    border-radius: 8px;

    .exam-info-label {
      font-size: 13px;
      color: $text-muted;
      white-space: nowrap;
    }

    .exam-info-value {
      font-size: 15px;
      font-weight: 700;
      color: $primary-color;
    }
  }
}

// 顶部进度条
.top-progress-bar {
  background: $bg-white;
  padding: 12px 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  z-index: 10;
}

.progress-track {
  height: 6px;
  background: #e5e6eb;
  border-radius: 3px;
  overflow: hidden;
  margin-bottom: 8px;
}

.progress-fill {
  height: 100%;
  background: linear-gradient(90deg, $primary-color 0%, #4080FF 100%);
  border-radius: 3px;
  transition: width 0.5s ease-out;

  &.progress-complete {
    background: linear-gradient(90deg, $success-color 0%, #7EEAC0 100%);
    animation: progress-glow 1.5s ease-in-out infinite;
    will-change: transform, opacity;
  }
}

@keyframes progress-glow {
  0%, 100% { box-shadow: 0 0 4px rgba(54, 211, 153, 0.4); }
  50% { box-shadow: 0 0 12px rgba(54, 211, 153, 0.8); }
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.progress-text {
  font-size: 13px;
  color: $text-muted;
  font-weight: 500;
}

.countdown-timer {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  font-weight: 700;
  color: $text-title;
  padding: 4px 12px;
  border-radius: 8px;
  background: #f7f8fa;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s;

  .el-icon {
    color: $primary-color;
  }

  &.countdown-warning {
    color: $warning-color;
    background: rgba(255, 149, 0, 0.1);
    animation: warning-pulse 1s ease-in-out infinite;
    will-change: transform, opacity;

    .el-icon {
      color: $warning-color;
    }
  }

  &.countdown-danger {
    color: $danger-color;
    background: rgba(245, 63, 63, 0.1);
    animation: danger-blink 0.8s ease-in-out infinite;
    will-change: transform, opacity;

    .el-icon {
      color: $danger-color;
    }
  }
}

@keyframes warning-pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.05); }
}

@keyframes danger-blink {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.6; }
}

// 主内容区
.exam-main {
  display: flex;
  gap: 16px;
  padding: 16px;
  flex: 1;
  overflow: hidden;
}

// 题目导航
.question-nav {
  width: 200px;
  flex-shrink: 0;
  background: $bg-white;
  border-radius: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  display: flex;
  flex-direction: column;
  overflow-y: auto;

  .nav-title {
    font-size: 14px;
    font-weight: 700;
    color: $text-title;
    margin-bottom: 12px;
    display: flex;
    align-items: center;
    gap: 6px;

    .el-icon {
      color: $primary-color;
    }
  }

  .nav-groups {
    display: flex;
    flex-direction: column;
    gap: 14px;
  }

  .nav-group-title {
    font-size: 12px;
    font-weight: 600;
    color: $text-muted;
    margin-bottom: 8px;
    display: flex;
    align-items: center;
    gap: 6px;

    .nav-group-count {
      font-size: 11px;
      color: $primary-color;
      background: rgba(22, 93, 255, 0.1);
      border-radius: 10px;
      padding: 1px 8px;
    }
  }

  .nav-grid {
    display: grid;
    grid-template-columns: repeat(5, 1fr);
    gap: 6px;
  }

  .nav-item {
    aspect-ratio: 1;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 13px;
    cursor: pointer;
    background: #f7f8fa;
    color: $text-muted;
    transition: transform, box-shadow, background-color, border-color, color, opacity 0.25s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    border: 2px solid transparent;

    .nav-number {
      font-weight: 500;
    }

    .nav-status {
      position: absolute;
      top: 2px;
      right: 2px;
      font-size: 10px;

      &.marked-dot {
        width: 6px;
        height: 6px;
        border-radius: 50%;
        background: $warning-color;
      }

      .el-icon {
        color: $success-color;
        font-size: 10px;
      }
    }

    &.answered {
      background: rgba(54, 211, 153, 0.12);
      color: $success-color;
      border-color: rgba(54, 211, 153, 0.3);
      font-weight: 600;
    }

    &.marked {
      border-color: rgba(255, 149, 0, 0.4);

      &:not(.answered) {
        background: rgba(255, 149, 0, 0.08);
      }
    }

    &.active {
      background: linear-gradient(135deg, $primary-color 0%, #4080FF 100%);
      color: #fff;
      box-shadow: 0 4px 12px rgba(22, 93, 255, 0.3);
      transform: scale(1.08);

      .nav-status .el-icon {
        color: #fff;
      }
    }

    &:hover:not(.active) {
      transform: scale(1.05);
      border-color: rgba(22, 93, 255, 0.2);
      box-shadow: 0 2px 8px rgba(22, 93, 255, 0.1);
    }
  }

  .nav-legend {
    display: flex;
    justify-content: center;
    gap: 12px;
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid #e5e6eb;

    .legend-item {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 11px;
      color: $text-muted;
    }

    .legend-dot {
      width: 8px;
      height: 8px;
      border-radius: 2px;

      &.answered {
        background: $success-color;
      }

      &.marked {
        background: $warning-color;
      }

      &.unanswered {
        background: #e5e6eb;
      }
    }
  }

  .nav-stats {
    margin-top: 12px;
    text-align: center;

    .stats-bar {
      height: 4px;
      background: #e5e6eb;
      border-radius: 2px;
      overflow: hidden;
      margin-bottom: 6px;
    }

    .stats-fill {
      height: 100%;
      background: linear-gradient(90deg, $primary-color 0%, #4080FF 100%);
      border-radius: 2px;
      transition: width 0.5s ease-out;
    }

    .stats-text {
      font-size: 11px;
      color: $text-muted;
    }
  }
}

// 题目内容
.question-content {
  flex: 1;
  background: $bg-white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  overflow-y: auto;
  display: flex;
  flex-direction: column;

  .question-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    flex-shrink: 0;
    flex-wrap: wrap;
    gap: 8px;
  }

  .header-left {
    display: flex;
    align-items: center;
    gap: 10px;
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .question-number {
    font-size: 15px;
    font-weight: 600;
    color: $primary-color;
  }

  .type-tag, .lang-tag {
    font-weight: 500;
  }

  .question-score {
    font-size: 14px;
    color: $warning-color;
    font-weight: 700;
    padding: 2px 10px;
    background: rgba(255, 149, 0, 0.1);
    border-radius: 8px;
  }

  .mark-btn {
    font-size: 12px;
  }
}

.question-title {
  font-size: 16px;
  color: $text-title;
  margin-bottom: 20px;
  line-height: 1.7;
  font-weight: 500;
}

// 选择题选项
.choice-options {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  border-radius: 10px;
  border: 2px solid #e5e6eb;
  cursor: pointer;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  background: $bg-white;

  &:hover {
    border-color: rgba(22, 93, 255, 0.3);
    background: rgba(22, 93, 255, 0.02);
    transform: translateX(4px);
  }

  &.selected {
    border-color: $primary-color;
    background: linear-gradient(90deg, rgba(22, 93, 255, 0.06) 0%, #fff 100%);
    box-shadow: 0 4px 16px rgba(22, 93, 255, 0.1);
    transform: scale(1.01);

    .option-radio {
      border-color: $primary-color;
      background: $primary-color;

      .radio-inner {
        transform: scale(1);
      }
    }

    .option-label {
      color: $primary-color;
      font-weight: 700;
    }
  }
}

.option-radio {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  border: 2px solid #C9CDD4;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.25s;
}

.radio-inner {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: $bg-white;
  transform: scale(0);
  transition: transform 0.25s cubic-bezier(0.4, 0, 0.2, 1);
}

.option-label {
  font-size: 15px;
  font-weight: 600;
  color: $text-muted;
  min-width: 24px;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.25s;
}

.option-text {
  font-size: 14px;
  color: $text-body;
  flex: 1;
}

.fill-input {
  max-width: 400px;

  .fill-answer-input {
    :deep(.el-input__wrapper) {
      border-radius: 10px;
      padding: 4px 16px;
    }

    :deep(.el-input__inner) {
      height: 48px;
      font-size: 15px;
    }
  }
}

// 判断题选项
.judge-options {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.judge-option {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 24px;
  border-radius: 10px;
  border: 2px solid #e5e6eb;
  cursor: pointer;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  background: $bg-white;

  &:hover {
    border-color: rgba(22, 93, 255, 0.3);
    transform: translateY(-2px);
  }

  &.selected {
    border-color: $primary-color;
    background: linear-gradient(90deg, rgba(22, 93, 255, 0.06) 0%, #fff 100%);
    box-shadow: 0 4px 16px rgba(22, 93, 255, 0.1);

    .option-radio {
      border-color: $primary-color;
      background: $primary-color;

      .radio-inner {
        transform: scale(1);
      }
    }
  }

  .judge-icon {
    width: 28px;
    height: 28px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    color: #fff;

    &.judge-correct {
      background: $success-color;
    }

    &.judge-wrong {
      background: $danger-color;
    }
  }
}

.code-area {
  margin-top: 8px;
  flex: 1;
  min-height: 480px;
  display: flex;
  flex-direction: column;
}

.question-nav-buttons {
  display: flex;
  justify-content: space-between;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #e5e6eb;
  flex-shrink: 0;
}

.nav-btn {
  padding: 10px 24px;
  font-size: 14px;
  font-weight: 600;
  border-radius: 10px;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s;

  &:hover:not(:disabled) {
    transform: translateY(-2px);
  }
}

.next-btn {
  background: linear-gradient(135deg, $primary-color 0%, #4080FF 100%);
  border: none;
  box-shadow: 0 4px 14px rgba(22, 93, 255, 0.3);

  &:hover {
    background: linear-gradient(135deg, #0E42D2 0%, #165DFF 100%);
    box-shadow: 0 6px 20px rgba(22, 93, 255, 0.4);
  }
}

.submit-btn {
  background: linear-gradient(135deg, $success-color 0%, #7EEAC0 100%);
  border: none;
  box-shadow: 0 4px 14px rgba(54, 211, 153, 0.3);

  &:hover {
    background: linear-gradient(135deg, #36D399 0%, #36D399 100%);
    box-shadow: 0 6px 20px rgba(54, 211, 153, 0.4);
  }
}

// 响应式
@media (max-width: 768px) {
  .exam-main {
    flex-direction: column;
    padding: 12px;
  }

  .question-nav {
    width: 100%;
    max-height: 200px;
  }

  .question-content {
    padding: 16px;
  }

  .question-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-right {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
