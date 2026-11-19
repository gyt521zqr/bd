<template>
  <!-- 题库中心：展示所有真题试卷（覆盖 6 个编程工具），点击进入模拟练习 -->
  <div class="question-bank-page">
    <!-- 顶部 Banner -->
    <section class="bank-banner">
      <div class="banner-bg">
        <div class="banner-circle banner-circle-1"></div>
        <div class="banner-circle banner-circle-2"></div>
        <div class="banner-grid"></div>
      </div>
      <div class="banner-content">
        <div class="banner-badge">码高教育 · 题库中心</div>
        <h2 class="banner-title">真题实战 · 以练促学</h2>
        <p class="banner-subtitle">汇聚 GESP 认证、等级考试等真实真题，覆盖六大编程工具，模拟正式竞赛环境</p>
        <div class="banner-stats">
          <div class="stat-item">
            <span class="stat-num">{{ papers.length }}</span>
            <span class="stat-label">套真题试卷</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-num">6</span>
            <span class="stat-label">种编程工具</span>
          </div>
          <div class="stat-divider"></div>
          <div class="stat-item">
            <span class="stat-num">{{ totalQuestions }}</span>
            <span class="stat-label">道题目</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 顶部工具条（错题本入口） -->
    <section class="bank-filter">
      <div class="filter-row">
        <router-link to="/wrong-book" class="wrong-book-entry">
          <el-icon><Notebook /></el-icon>
          错题本
          <span v-if="wrongCount > 0" class="entry-badge">{{ wrongCount }}</span>
        </router-link>
      </div>
    </section>

    <!-- 试卷列表 -->
    <section class="bank-list">
      <div class="paper-grid">
        <div v-for="paper in papers" :key="paper.id" class="paper-card">
          <div class="paper-cover" :style="{ backgroundImage: `url(${langCover()})` }">
            <div class="paper-cover-overlay"></div>
            <span class="paper-lang-badge">模拟练习</span>
            <span class="paper-cover-emoji">{{ langEmoji() }}</span>
          </div>
          <div class="paper-body">
            <h3 class="paper-title">{{ paper.paperName }}</h3>
            <div class="paper-meta">
              <span class="meta-item">
                <el-icon><Document /></el-icon>
                {{ paper.questionCount }} 题
              </span>
              <span class="meta-item">
                <el-icon><DataAnalysis /></el-icon>
                {{ paper.totalScore }} 分
              </span>
            </div>
            <div class="paper-actions">
              <button class="paper-preview-btn" @click="openPreview(paper)">
                <el-icon><View /></el-icon>
                预览
              </button>
              <button class="paper-start-btn" :disabled="!paper.examId" @click="startPractice(paper)">
                开始练习
                <el-icon><ArrowRight /></el-icon>
              </button>
            </div>
          </div>
        </div>
      </div>

      <!-- 空状态 -->
      <el-empty v-if="papers.length === 0" description="暂无试卷，请先到后台录入" :image-size="100" />
    </section>

    <!-- 试卷预览弹窗 -->
    <PaperPreviewDialog
      v-model="previewVisible"
      :paper-id="previewPaperId"
      :title="papers.find((p) => p.id === previewPaperId)?.paperName"
      @start="startPractice(papers.find((p) => p.id === previewPaperId))"
    />
  </div>
</template>

<script setup lang="ts">
/**
 * QuestionBankPage - 题库中心
 * 展示所有真题试卷（覆盖 6 个编程工具），点击「开始练习」进入模拟练习（复用考试中心做题页）
 */
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document, DataAnalysis, ArrowRight, Notebook, View } from '@element-plus/icons-vue'
import { getPaperList } from '@/api/exam'
import type { ExamPaper } from '@/types'
import { getWrongBook } from '@/utils/wrongBook'
import PaperPreviewDialog from '@/components/exam/PaperPreviewDialog.vue'

const router = useRouter()

/** 试卷列表（来自后端真实数据，无本地假题库） */
const papers = ref<ExamPaper[]>([])

onMounted(async () => {
  try {
    const res = await getPaperList()
    if (res?.code === 200 && Array.isArray(res.data)) papers.value = res.data
  } catch (e) {
    console.warn('[QuestionBankPage] 试卷列表加载失败', e)
  }
})

/** 错题本数量 */
const wrongCount = computed(() => getWrongBook().length)

/** 试卷预览弹窗 */
const previewVisible = ref(false)
const previewPaperId = ref(0)
function openPreview(paper: ExamPaper) {
  previewPaperId.value = paper.id
  previewVisible.value = true
}

const totalQuestions = computed(() => papers.value.reduce((s, p) => s + (p.questionCount || 0), 0))

/** 固定封面（后端试卷无语言维度，统一使用通用封面） */
function langCover(): string {
  return '/images/contest/contest-blue.png'
}

/** 语言 emoji（后端试卷无语言维度，统一展示） */
function langEmoji(): string {
  return '📝'
}

/** 开始练习 → 进入该试卷关联考试的模拟练习（复用考试中心做题页） */
function startPractice(paper: ExamPaper | undefined) {
  if (!paper) return
  // 试卷关联的考试ID（练习复用考试流程）；未关联考试则无法进入
  const examId = paper.examId
  if (!examId) {
    ElMessage.warning('该试卷未关联考试，暂不支持练习')
    return
  }
  router.push(`/exam-taking/${examId}`)
}
</script>

<style scoped lang="scss">
.question-bank-page {
  background: $bg-page;
  min-height: 100vh;
  padding-bottom: 60px;
}

// ========== Banner ==========
.bank-banner {
  position: relative;
  overflow: hidden;
  background: linear-gradient(135deg, $primary-color 0%, #4080FF 60%, #7B61FF 100%);
  padding: 64px 24px 56px;
  text-align: center;
}

.banner-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;
}

.banner-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.08);

  &.banner-circle-1 {
    width: 400px;
    height: 400px;
    top: -140px;
    right: -80px;
  }

  &.banner-circle-2 {
    width: 260px;
    height: 260px;
    bottom: -80px;
    left: -60px;
  }
}

.banner-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(255, 255, 255, 0.04) 1px, transparent 1px),
    linear-gradient(90deg, rgba(255, 255, 255, 0.04) 1px, transparent 1px);
  background-size: 40px 40px;
}

.banner-content {
  position: relative;
  z-index: 1;
  max-width: 760px;
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
  margin-bottom: 20px;
}

.banner-title {
  font-size: 40px;
  font-weight: 800;
  color: #fff;
  margin: 0 0 14px;
  letter-spacing: 2px;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
}

.banner-subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.85);
  margin: 0 0 30px;
  line-height: 1.7;
}

.banner-stats {
  display: inline-flex;
  align-items: center;
  gap: 24px;
  background: rgba(255, 255, 255, 0.12);
  backdrop-filter: blur(10px);
  border-radius: 40px;
  padding: 12px 32px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;
}

.stat-num {
  font-size: 24px;
  font-weight: 800;
  color: #fff;
}

.stat-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
}

.stat-divider {
  width: 1px;
  height: 34px;
  background: rgba(255, 255, 255, 0.2);
}

// ========== 语言筛选 ==========
.bank-filter {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 24px 0;
}

.filter-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.wrong-book-entry {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 9px 20px;
  border-radius: 24px;
  background: rgba(245, 63, 63, 0.08);
  border: 2px solid rgba(245, 63, 63, 0.25);
  color: $danger-color;
  font-size: 14px;
  font-weight: 600;
  text-decoration: none;
  cursor: pointer;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.25s;

  &:hover {
    background: rgba(245, 63, 63, 0.14);
    border-color: $danger-color;
    transform: translateY(-1px);
  }

  .entry-badge {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    min-width: 20px;
    height: 20px;
    padding: 0 6px;
    border-radius: 10px;
    background: $danger-color;
    color: #fff;
    font-size: 12px;
    font-weight: 700;
  }
}

.filter-pills {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

.pill {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 9px 20px;
  border: 2px solid #e5e6eb;
  border-radius: 24px;
  background: $bg-white;
  font-size: 14px;
  font-weight: 500;
  color: $text-body;
  cursor: pointer;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.25s;

  &:hover {
    border-color: $primary-light;
    color: $primary-color;
  }

  &.active {
    border-color: $primary-color;
    background: rgba($primary-color, 0.08);
    color: $primary-color;
    font-weight: 600;
    box-shadow: 0 2px 8px rgba($primary-color, 0.15);
  }

  .pill-emoji {
    font-size: 16px;
  }
}

// ========== 试卷列表 ==========
.bank-list {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px;
}

.paper-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.paper-card {
  background: $bg-white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  display: flex;
  flex-direction: column;

  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
  }
}

.paper-cover {
  height: 118px;
  position: relative;
  overflow: hidden;
  background-size: cover;
  background-position: center;
  flex-shrink: 0;
}

.paper-cover-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(180deg, rgba(0, 0, 0, 0.05) 0%, rgba(0, 0, 0, 0.45) 100%);
}

.paper-level-badge {
  position: absolute;
  left: 12px;
  top: 10px;
  color: #fff;
  font-size: 12px;
  font-weight: 700;
  background: rgba(22, 93, 255, 0.85);
  padding: 2px 10px;
  border-radius: 8px;
  letter-spacing: 1px;
  backdrop-filter: blur(4px);
}

.paper-lang-badge {
  position: absolute;
  left: 12px;
  bottom: 10px;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  background: rgba(0, 0, 0, 0.35);
  padding: 3px 10px;
  border-radius: 8px;
  backdrop-filter: blur(4px);
}

.paper-cover-emoji {
  position: absolute;
  right: 12px;
  top: 10px;
  font-size: 26px;
  line-height: 1;
  filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
}

.paper-body {
  padding: 16px 18px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.paper-tag-row {
  margin-bottom: 8px;
}

.paper-title {
  font-size: 15px;
  font-weight: 700;
  color: $text-title;
  margin: 0 0 10px;
  line-height: 1.45;
  min-height: 44px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.paper-meta {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 14px;

  .meta-item {
    display: inline-flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    color: $text-muted;

    .el-icon {
      font-size: 14px;
      color: $primary-color;
    }
  }
}

.paper-actions {
  margin-top: auto;
  display: flex;
  gap: 10px;
}

.paper-preview-btn {
  flex-shrink: 0;
  height: 40px;
  padding: 0 16px;
  border: 2px solid #e5e6eb;
  border-radius: 10px;
  background: $bg-white;
  color: $text-body;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.25s;

  &:hover {
    border-color: $primary-color;
    color: $primary-color;
    background: rgba(22, 93, 255, 0.04);
    transform: translateY(-2px);
  }
}

.paper-start-btn {
  flex: 1;
  height: 40px;
  border: none;
  border-radius: 10px;
  background: linear-gradient(135deg, $primary-color 0%, #4080FF 100%);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.25s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(22, 93, 255, 0.35);
    background: linear-gradient(135deg, #0E42D2 0%, #165DFF 100%);
  }

  &:disabled {
    background: #c0c4cc;
    color: #fff;
    cursor: not-allowed;
    transform: none;
    box-shadow: none;
  }
}

// ========== 响应式 ==========
@media (max-width: 992px) {
  .paper-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .bank-banner {
    padding: 40px 16px 36px;
  }

  .banner-title {
    font-size: 28px;
  }

  .banner-stats {
    gap: 16px;
    padding: 10px 20px;
  }

  .paper-grid {
    grid-template-columns: 1fr;
  }

  .paper-title {
    min-height: auto;
  }
}
</style>
