<template>
  <!-- 成绩详情 -->
  <div class="exam-result-page">
    <div v-if="loading" class="loading-wrapper">
      <el-skeleton :rows="6" animated />
    </div>
    <template v-else>
      <!-- 页面标题 -->
      <div class="page-header">
        <div class="header-icon">
          <el-icon :size="28" color="#165DFF"><DocumentChecked /></el-icon>
        </div>
        <div class="header-text">
          <h1 class="header-title">考试成绩</h1>
          <p class="header-subtitle">查看您的考试结果和答题详情</p>
        </div>
      </div>
      <ExamResult :record="record" :details="details" />
      <div class="result-actions">
        <el-button class="action-btn back-btn" @click="router.push(backPath)">
          <el-icon><ArrowLeft /></el-icon>
          返回列表
        </el-button>
        <el-button class="action-btn book-btn" @click="router.push('/wrong-book')">
          <el-icon><Notebook /></el-icon>
          错题本
        </el-button>
        <el-button class="action-btn retry-btn" @click="handleRetry">
          <el-icon><RefreshRight /></el-icon>
          继续练习
        </el-button>
        <el-button type="primary" class="action-btn primary-btn" @click="handleWrongRetry">
          <el-icon><EditPen /></el-icon>
          错题重练
        </el-button>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
/**
 * ExamResultPage - 考试成绩详情页
 * 正式考试：成绩与逐题解析来自后端（getExamResult + getExamResultDetail），不再本地假判分
 * 错题重练：无后端记录，用错题集（含标准答案）本地判分
 */
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { DocumentChecked, ArrowLeft, EditPen, RefreshRight, Notebook } from '@element-plus/icons-vue'
import { getExamResult, getExamResultDetail } from '@/api/exam'
import { useExamStore } from '@/stores/exam'
import ExamResult from '@/components/exam/ExamResult.vue'
import type { ExamRecord, ExamResultDetail } from '@/types'

const route = useRoute()
const router = useRouter()
const examStore = useExamStore()
const loading = ref(false)

const examId = computed(() => Number(route.params.examId))

// 后端返回的考试记录与逐题答题详情（正式考试）
const backendRecord = ref<ExamRecord | null>(null)
const backendDetails = ref<ExamResultDetail[]>([])

// 错题重练模式：有错题集（含标准答案），无后端记录，本地判分
const isWrongMode = computed(() => !!(examStore.overrideQuestions && examStore.overrideQuestions.length > 0))
const wrongQuestions = computed(() => examStore.overrideQuestions || [])
const userAnswers = computed(() => examStore.submittedAnswers)

/** 答案显示格式化（判断题 true/false → 正确/错误，选择题字母 → 字母（选项内容）） */
function answerText(q: { type?: string; answer?: string; options?: string[]; correctAnswer?: string }, raw?: string): string {
  if (!raw) return '未作答'
  if (q.type === 'judge') return raw === 'true' ? '正确' : '错误'
  if (q.type === 'choice') {
    const idx = raw.charCodeAt(0) - 65
    const opt = q.options?.[idx]
    return opt ? `${raw}（${opt}）` : raw
  }
  return raw
}

/** 构造答题详情（错题重练本地判分） */
function buildLocalDetails() {
  return wrongQuestions.value.map((q) => {
    const userRaw = userAnswers.value.get(q.id) || ''
    const isCorrect = !!q.answer && userRaw === q.answer
    return {
      questionId: q.id,
      title: q.title,
      userAnswer: answerText(q, userRaw),
      correctAnswer: answerText(q, q.answer),
      score: isCorrect ? q.score : 0,
      maxScore: q.score,
      analysis: q.analysis || '',
      type: q.type,
      options: q.options || [],
      answer: q.answer || '',
    }
  })
}

/** 将后端答题详情映射为组件期望结构 */
function buildBackendDetails() {
  return backendDetails.value.map((d) => ({
    questionId: d.questionId,
    title: d.title,
    userAnswer: answerText(d, d.userAnswer),
    correctAnswer: answerText(d, d.correctAnswer),
    score: d.score || 0,
    maxScore: d.maxScore || 0,
    analysis: d.analysis || '',
    type: d.type,
    options: d.options || [],
    answer: d.correctAnswer || '',
  }))
}

const details = computed(() => (isWrongMode.value ? buildLocalDetails() : buildBackendDetails()))

const totalScore = computed(() => {
  if (isWrongMode.value) return wrongQuestions.value.reduce((s, q) => s + (q.score || 0), 0)
  return details.value.reduce((s, d) => s + d.maxScore, 0)
})
const myScore = computed(() => {
  if (isWrongMode.value) return details.value.reduce((s, d) => s + d.score, 0)
  return backendRecord.value?.score ?? 0
})
const passScore = computed(() => {
  if (isWrongMode.value) return Math.round(totalScore.value * 0.6)
  return Math.round(totalScore.value * 0.6)
})

const record = computed(() => ({
  id: backendRecord.value?.id ?? String(examId.value),
  examTitle: examStore.overrideTitle || backendRecord.value?.examTitle || '在线考试',
  score: myScore.value,
  totalScore: totalScore.value,
  passed: isWrongMode.value ? myScore.value >= passScore.value : (backendRecord.value?.isPassed === 1),
  submitTime: backendRecord.value?.submitTime || '刚刚交卷',
  duration: '--',
}))

/** 返回路径：题库中心试卷（id>=201）回题库中心，否则回考试中心 */
const backPath = computed(() => (examId.value >= 201 ? '/question-bank' : '/exam'))

/** 收集错题（仅客观题，有标准答案的） */
function collectWrongQuestions() {
  return wrongQuestions.value.filter((q) => {
    const userRaw = userAnswers.value.get(q.id) || ''
    return !!q.answer && userRaw !== q.answer
  })
}

/** 继续练习：重做本卷 */
function handleRetry() {
  examStore.resetExam()
  examStore.clearOverrideQuestions()
  router.push(`/exam-taking/${examId.value}`)
}

/** 错题重练：只做错题 */
function handleWrongRetry() {
  const wrong = collectWrongQuestions()
  if (wrong.length === 0) {
    ElMessage.success('太棒了！本次没有错题')
    return
  }
  examStore.resetExam()
  examStore.setOverrideQuestions(wrong, '错题重练', Math.max(15, wrong.length * 2))
  router.push(`/exam-taking/${examId.value}?mode=wrong`)
}

onMounted(async () => {
  loading.value = false
  // 正式考试：从后端拉取成绩与逐题解析
  if (!isWrongMode.value && examStore.recordId) {
    try {
      const [recordRes, detailRes] = await Promise.all([
        getExamResult(examStore.recordId),
        getExamResultDetail(examStore.recordId),
      ])
      if (recordRes?.data) backendRecord.value = recordRes.data
      if (detailRes?.data && Array.isArray(detailRes.data)) backendDetails.value = detailRes.data
    } catch (e) {
      console.warn('[ExamResultPage] 成绩加载失败', e)
      ElMessage.error('成绩加载失败，请稍后重试')
    }
  }
})
</script>

<style scoped lang="scss">

.exam-result-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-bottom: 24px;
  padding: 20px 24px;
  background: linear-gradient(135deg, #fff 0%, #E8F0FF 100%);
  border-radius: 16px;
  border: 1px solid rgba(22, 93, 255, 0.1);
}

.header-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(22, 93, 255, 0.1) 0%, rgba(22, 93, 255, 0.05) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.header-title {
  font-size: 20px;
  font-weight: 700;
  color: $text-title;
  margin: 0 0 4px;
}

.header-subtitle {
  font-size: 13px;
  color: $text-muted;
  margin: 0;
}

.result-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-top: 28px;
  padding-bottom: 20px;
}

.action-btn {
  padding: 12px 28px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 10px;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s;

  &:hover {
    transform: translateY(-2px);
  }
}

.back-btn {
  border-color: #e5e6eb;
  color: $text-title;

  &:hover {
    border-color: $primary-color;
    color: $primary-color;
    background: rgba(22, 93, 255, 0.04);
  }
}

.primary-btn {
  background: linear-gradient(135deg, $primary-color 0%, #4080FF 100%);
  border: none;
  box-shadow: 0 4px 14px rgba(22, 93, 255, 0.3);

  &:hover {
    background: linear-gradient(135deg, #0E42D2 0%, #165DFF 100%);
    box-shadow: 0 6px 20px rgba(22, 93, 255, 0.4);
  }
}

.retry-btn {
  border-color: $success-color;
  color: $success-color;

  &:hover {
    border-color: $success-color;
    color: $success-color;
    background: rgba(54, 211, 153, 0.08);
  }
}

.book-btn {
  border-color: $warning-color;
  color: $warning-color;

  &:hover {
    border-color: $warning-color;
    color: $warning-color;
    background: rgba(255, 149, 0, 0.08);
  }
}

.loading-wrapper {
  padding: 40px 0;
}
</style>
