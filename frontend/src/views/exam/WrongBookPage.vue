<template>
  <!-- 错题本：收录做错的客观题，支持重练 / 移除 / 清空 -->
  <div class="wrong-book-page">
    <!-- 顶部 Banner -->
    <section class="wb-banner">
      <div class="banner-content">
        <div class="banner-badge">错题本</div>
        <h2 class="banner-title">错题重练 · 查漏补缺</h2>
        <p class="banner-subtitle">自动收录练习与考试中做错的题目，答对后自动移除</p>
        <div class="banner-stats">
          <div class="stat-item">
            <span class="stat-num">{{ items.length }}</span>
            <span class="stat-label">道错题</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 工具栏 -->
    <section v-if="items.length" class="wb-toolbar">
      <div class="toolbar-left">
        <el-button type="primary" class="retry-all-btn" @click="retryAll">
          <el-icon><EditPen /></el-icon>
          重练全部错题
        </el-button>
        <el-button class="clear-btn" @click="clearAll">
          <el-icon><Delete /></el-icon>
          清空错题本
        </el-button>
      </div>
    </section>

    <!-- 错题列表 -->
    <section v-if="items.length" class="wb-list">
      <div class="wrong-grid">
        <div v-for="item in items" :key="item.question.id" class="wrong-card">
          <div class="wrong-card-head">
            <el-tag size="small" :type="typeTag(item.question.type)" class="type-tag">
              {{ typeLabel(item.question.type) }}
            </el-tag>
            <span class="source-paper">{{ paperTitle(item.sourcePaperId) }}</span>
          </div>
          <h3 class="wrong-title">{{ item.question.title }}</h3>

          <!-- 选择题选项 -->
          <div v-if="item.question.type === 'choice' && item.question.options" class="wrong-options">
            <div
              v-for="(opt, oi) in item.question.options"
              :key="oi"
              class="wrong-option"
              :class="{ 'is-correct': item.question.answer === String.fromCharCode(65 + oi) }"
            >
              <span class="option-label">{{ String.fromCharCode(65 + oi) }}.</span>
              <span class="option-text">{{ opt }}</span>
              <el-icon v-if="item.question.answer === String.fromCharCode(65 + oi)" class="correct-icon">
                <CircleCheckFilled />
              </el-icon>
            </div>
          </div>

          <!-- 正确答案与解析 -->
          <div class="wrong-answer">
            <span class="answer-label">正确答案</span>
            <span class="answer-value">{{ formatAnswer(item.question) }}</span>
          </div>
          <div v-if="item.question.analysis" class="wrong-analysis">
            <span class="analysis-label">解析</span>
            <span class="analysis-text">{{ item.question.analysis }}</span>
          </div>

          <div class="wrong-card-foot">
            <span class="added-time">{{ formatTime(item.addedAt) }}</span>
            <el-button text size="small" type="danger" class="remove-btn" @click="removeOne(item)">
              <el-icon><Delete /></el-icon>
              移除
            </el-button>
          </div>
        </div>
      </div>
    </section>

    <!-- 空状态 -->
    <el-empty
      v-else
      description="错题本空空如也，太棒了！"
      :image-size="140"
      class="wb-empty"
    />
  </div>
</template>

<script setup lang="ts">
/**
 * WrongBookPage - 错题本
 * 展示 localStorage 持久化的错题，支持重练 / 移除 / 清空
 */
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { EditPen, Delete, CircleCheckFilled } from '@element-plus/icons-vue'
import { getWrongBook, removeFromWrongBook, clearWrongBook, type WrongBookItem } from '@/utils/wrongBook'
import { getExamMeta, QUESTION_TYPE_LABEL, type QuestionType } from '@/mock/examQuestions'
import { useExamStore } from '@/stores/exam'

const router = useRouter()
const examStore = useExamStore()

const items = ref<WrongBookItem[]>(getWrongBook())

function refresh() {
  items.value = getWrongBook()
}

/** 题型标签 */
function typeLabel(type: string): string {
  return QUESTION_TYPE_LABEL[type as QuestionType] || '题目'
}

function typeTag(type: string): 'info' | 'primary' | 'success' | 'warning' | 'danger' {
  const map: Record<string, 'info' | 'primary' | 'success' | 'warning' | 'danger'> = {
    choice: 'info', fill: 'info', judge: 'success', code: 'warning', scratch: 'warning', scratchjr: 'warning',
  }
  return map[type] || 'info'
}

/** 来源试卷标题 */
function paperTitle(paperId: number): string {
  return getExamMeta(paperId).title
}

/** 答案格式化 */
function formatAnswer(q: { type: string; answer?: string; options?: string[] }): string {
  if (!q.answer) return '—'
  if (q.type === 'judge') return q.answer === 'true' ? '正确' : '错误'
  if (q.type === 'choice') {
    const idx = q.answer.charCodeAt(0) - 65
    const opt = q.options?.[idx]
    return opt ? `${q.answer}（${opt}）` : q.answer
  }
  return q.answer
}

/** 时间格式化 */
function formatTime(ts: number): string {
  const d = new Date(ts)
  const p = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}`
}

/** 移除单题 */
function removeOne(item: WrongBookItem) {
  removeFromWrongBook([item.question.id])
  refresh()
  ElMessage.success('已移除该错题')
}

/** 清空错题本 */
function clearAll() {
  ElMessageBox.confirm('确定要清空全部错题吗？此操作不可恢复。', '清空确认', {
    confirmButtonText: '确定清空',
    cancelButtonText: '取消',
    type: 'warning',
  })
    .then(() => {
      clearWrongBook()
      refresh()
      ElMessage.success('错题本已清空')
    })
    .catch(() => {})
}

/** 重练全部错题 */
function retryAll() {
  const questions = items.value.map((i) => i.question)
  const sourceId = items.value[0]?.sourcePaperId || 201
  examStore.resetExam()
  examStore.setOverrideQuestions(questions, '错题重练', Math.max(15, questions.length * 2))
  router.push(`/exam-taking/${sourceId}?mode=wrong`)
}
</script>

<style scoped lang="scss">
.wrong-book-page {
  background: $bg-page;
  min-height: 100vh;
  padding-bottom: 60px;
}

// ========== Banner ==========
.wb-banner {
  background: linear-gradient(135deg, #F53F3F 0%, #FF7D00 100%);
  padding: 56px 24px 48px;
  text-align: center;
}

.banner-content {
  max-width: 700px;
  margin: 0 auto;
}

.banner-badge {
  display: inline-block;
  padding: 6px 18px;
  border-radius: 20px;
  background: rgba(255, 255, 255, 0.16);
  backdrop-filter: blur(8px);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  letter-spacing: 1px;
  margin-bottom: 18px;
}

.banner-title {
  font-size: 36px;
  font-weight: 800;
  color: #fff;
  margin: 0 0 12px;
  letter-spacing: 2px;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
}

.banner-subtitle {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.85);
  margin: 0 0 24px;
}

.banner-stats {
  display: inline-flex;
  align-items: center;
  background: rgba(255, 255, 255, 0.14);
  backdrop-filter: blur(10px);
  border-radius: 40px;
  padding: 10px 32px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.stat-num {
  font-size: 28px;
  font-weight: 800;
  color: #fff;
}

.stat-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.75);
}

// ========== 工具栏 ==========
.wb-toolbar {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 24px 0;

  .toolbar-left {
    display: flex;
    gap: 12px;
  }

  .retry-all-btn {
    background: linear-gradient(135deg, $primary-color 0%, #4080FF 100%);
    border: none;
    box-shadow: 0 4px 14px rgba(22, 93, 255, 0.3);
  }

  .clear-btn {
    border-color: $danger-color;
    color: $danger-color;

    &:hover {
      border-color: $danger-color;
      color: $danger-color;
      background: rgba(245, 63, 63, 0.06);
    }
  }
}

// ========== 错题列表 ==========
.wb-list {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px 24px;
}

.wrong-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.wrong-card {
  background: $bg-white;
  border-radius: 16px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  display: flex;
  flex-direction: column;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.25s;

  &:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  }
}

.wrong-card-head {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 12px;

  .source-paper {
    font-size: 12px;
    color: $text-muted;
  }
}

.wrong-title {
  font-size: 15px;
  font-weight: 600;
  color: $text-title;
  margin: 0 0 12px;
  line-height: 1.6;
  white-space: pre-wrap;
}

.wrong-options {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin-bottom: 12px;
}

.wrong-option {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  border-radius: 8px;
  background: #f7f8fa;
  font-size: 13px;
  color: $text-body;

  &.is-correct {
    background: rgba(54, 211, 153, 0.1);
    color: $success-color;

    .option-label {
      color: $success-color;
      font-weight: 700;
    }
  }

  .option-label {
    font-weight: 600;
    color: $text-title;
  }

  .option-text {
    flex: 1;
  }

  .correct-icon {
    color: $success-color;
    font-size: 16px;
  }
}

.wrong-answer {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin-bottom: 6px;

  .answer-label {
    font-size: 12px;
    color: $text-muted;
    flex-shrink: 0;
  }

  .answer-value {
    font-size: 14px;
    font-weight: 700;
    color: $success-color;
  }
}

.wrong-analysis {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  padding: 10px 12px;
  background: rgba(22, 93, 255, 0.05);
  border-radius: 8px;
  margin-bottom: 12px;

  .analysis-label {
    font-size: 12px;
    color: $primary-color;
    font-weight: 600;
    flex-shrink: 0;
  }

  .analysis-text {
    font-size: 13px;
    color: $text-body;
    line-height: 1.6;
  }
}

.wrong-card-foot {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: auto;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;

  .added-time {
    font-size: 12px;
    color: $text-muted;
  }

  .remove-btn {
    font-size: 13px;
  }
}

.wb-empty {
  padding: 80px 0;
}

// ========== 响应式 ==========
@media (max-width: 768px) {
  .banner-title {
    font-size: 26px;
  }

  .wrong-grid {
    grid-template-columns: 1fr;
  }
}
</style>
