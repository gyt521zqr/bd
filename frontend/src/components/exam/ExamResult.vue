<template>
  <!-- 考试结果展示：成绩/答案对比/解析 -->
  <div class="exam-result">
    <!-- 成绩概览 -->
    <div class="result-overview">
      <div class="score-card" :class="{ passed: record.passed }">
        <!-- 环形进度条 -->
        <div class="score-circle-wrapper">
          <svg class="score-circle" viewBox="0 0 120 120">
            <circle class="score-circle-bg" cx="60" cy="60" r="52" />
            <circle
              class="score-circle-progress"
              cx="60"
              cy="60"
              r="52"
              :stroke-dasharray="`${circleCircumference} ${circleCircumference}`"
              :stroke-dashoffset="circleOffset"
            />
          </svg>
          <div class="score-circle-content">
            <div class="score-number">{{ record.score }}</div>
            <div class="score-total">/ {{ record.totalScore }}</div>
          </div>
        </div>
        <div class="score-label">考试得分</div>
        <el-tag :type="record.passed ? 'success' : 'danger'" size="large" class="pass-tag">
          <el-icon v-if="record.passed" class="pass-icon"><CircleCheck /></el-icon>
          <el-icon v-else class="pass-icon"><CircleClose /></el-icon>
          {{ record.passed ? '及格' : '不及格' }}
        </el-tag>
      </div>
      <div class="result-info">
        <div class="info-item"><span class="info-label">考试名称</span><span class="info-value">{{ record.examTitle }}</span></div>
        <div class="info-item"><span class="info-label">提交时间</span><span class="info-value">{{ record.submitTime }}</span></div>
        <div class="info-item"><span class="info-label">用时</span><span class="info-value">{{ record.duration }}</span></div>
        <div class="info-item"><span class="info-label">正确率</span>
          <span class="info-value accuracy-value">
            <el-progress :percentage="accuracy" :color="accuracyColor" :stroke-width="8" style="width: 120px;" />
          </span>
        </div>
      </div>
    </div>

    <!-- 证书区域 -->
    <div v-if="record.passed" class="certificate-section">
      <div class="certificate-card shine-effect">
        <div class="cert-shine"></div>
        <div class="cert-content">
          <div class="cert-icon">
            <el-icon :size="40" color="#FFD700"><Trophy /></el-icon>
          </div>
          <div class="cert-title">恭喜获得证书</div>
          <div class="cert-desc">您在本次考试中表现优异，已获得电子证书</div>
          <el-button type="primary" round class="cert-btn" @click="openCert">
            <el-icon><View /></el-icon>
            查看证书
          </el-button>
        </div>
      </div>
    </div>

    <!-- 题目详情 -->
    <div class="result-details">
      <h3 class="details-title">
        <el-icon><Document /></el-icon>
        答题详情
      </h3>
      <div
        v-for="detail in details"
        :key="detail.questionId"
        class="detail-item"
        :class="{ correct: detail.score === detail.maxScore, wrong: detail.score < detail.maxScore }"
      >
        <div class="detail-header">
          <div class="detail-header-left">
            <span class="detail-status-icon">
              <el-icon v-if="detail.score === detail.maxScore" class="status-correct"><CircleCheck /></el-icon>
              <el-icon v-else class="status-wrong"><CircleClose /></el-icon>
            </span>
            <span class="detail-index">第{{ detail.questionId }}题</span>
          </div>
          <span class="detail-score" :class="{ 'score-full': detail.score === detail.maxScore, 'score-partial': detail.score < detail.maxScore && detail.score > 0, 'score-zero': detail.score === 0 }">
            {{ detail.score }}/{{ detail.maxScore }}分
          </span>
        </div>
        <p class="detail-title">{{ detail.title }}</p>
        <div class="detail-answer">
          <div class="answer-item" :class="{ 'answer-correct': detail.userAnswer === detail.correctAnswer, 'answer-wrong': detail.userAnswer !== detail.correctAnswer }">
            <span class="answer-label">你的答案</span>
            <pre>{{ detail.userAnswer }}</pre>
          </div>
          <div class="answer-item answer-correct-item">
            <span class="answer-label">正确答案</span>
            <pre>{{ detail.correctAnswer }}</pre>
          </div>
        </div>
        <div v-if="detail.analysis" class="detail-analysis">
          <span class="analysis-label">解析</span>
          <span class="analysis-text">{{ detail.analysis }}</span>
        </div>
      </div>
    </div>

    <!-- 证书弹窗 -->
    <el-dialog v-model="certVisible" title="电子证书" width="560px" top="5vh" :close-on-click-modal="false">
      <div class="cert-dialog-wrapper">
        <div class="cert-preview" ref="certRef">
          <div class="cert-preview-inner">
            <div class="cert-preview-header">
              <div class="cert-preview-logo">
                <el-icon :size="26" color="#165DFF"><Trophy /></el-icon>
              </div>
              <div class="cert-preview-org">码高教育科技</div>
            </div>
            <div class="cert-preview-title">荣誉证书</div>
            <div class="cert-preview-subtitle">CERTIFICATE OF HONOR</div>
            <div class="cert-preview-body">
              <p>兹证明</p>
              <p class="cert-preview-name">{{ studentName }}</p>
              <p>在 <strong>{{ record.examTitle }}</strong> 考试中</p>
              <p>获得 <strong>{{ record.score }}</strong> 分，特发此证，以资鼓励</p>
            </div>
            <div class="cert-preview-footer">
              <div class="cert-preview-meta">
                <span>考试得分：{{ record.score }} / {{ record.totalScore }}</span>
              </div>
              <div class="cert-preview-date">{{ certDate }}</div>
            </div>
          </div>
        </div>
      </div>
      <template #footer>
        <el-button @click="certVisible = false">关 闭</el-button>
        <el-button type="primary" :icon="Download" :loading="certDownloading" @click="downloadCert">
          下载证书
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * ExamResult - 考试结果展示组件
 */
import { computed, ref } from 'vue'
import { CircleCheck, CircleClose, Trophy, View, Document, Download } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import html2canvas from 'html2canvas'

const props = defineProps<{
  record: { id: string | number; examTitle: string; score: number; totalScore: number; passed: boolean; submitTime: string; duration: string }
  details: {
    questionId: number
    title: string
    userAnswer: string
    correctAnswer: string
    score: number
    maxScore: number
    analysis?: string
    type?: string
    options?: string[]
    answer?: string
  }[]
}>()

const userStore = useUserStore()
const certVisible = ref(false)
const certDownloading = ref(false)
const certRef = ref<HTMLElement>()

const studentName = computed(() => userStore.userInfo?.nickname || userStore.userInfo?.username || '同学')
const certDate = computed(() => {
  const d = new Date()
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日`
})

function openCert() {
  certVisible.value = true
}

async function downloadCert() {
  if (!certRef.value) {
    ElMessage.warning('证书元素未找到')
    return
  }
  certDownloading.value = true
  try {
    const canvas = await html2canvas(certRef.value, { scale: 2, backgroundColor: '#ffffff', useCORS: true })
    const link = document.createElement('a')
    link.download = `证书_${studentName.value}_${props.record.examTitle}.png`
    link.href = canvas.toDataURL('image/png')
    link.click()
    ElMessage.success('证书下载成功')
  } catch (e) {
    console.error('证书下载失败:', e)
    ElMessage.error('证书下载失败，请重试')
  } finally {
    certDownloading.value = false
  }
}

const accuracy = computed(() => {
  if (!props.details.length) return 0
  const correct = props.details.filter(d => d.score === d.maxScore).length
  return Math.round((correct / props.details.length) * 100)
})

const accuracyColor = computed(() => {
  const a = accuracy.value
  if (a >= 80) return '#36D399'
  if (a >= 60) return '#FF7D00'
  return '#F53F3F'
})

// 环形进度条
const circleCircumference = 2 * Math.PI * 52
const circleOffset = computed(() => {
  const percent = props.record.totalScore > 0 ? props.record.score / props.record.totalScore : 0
  return circleCircumference * (1 - percent)
})
</script>

<style scoped lang="scss">

.exam-result {
  max-width: 800px;
}

.result-overview {
  display: flex;
  gap: 24px;
  background: $bg-white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  margin-bottom: 24px;
}

.score-card {
  width: 220px;
  text-align: center;
  padding: 24px;
  border-radius: 12px;
  background: linear-gradient(135deg, #FFECE8 0%, #FFECE8 100%);
  border: 2px solid rgba(245, 63, 63, 0.15);
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s;

  &.passed {
    background: linear-gradient(135deg, #E8FFF5 0%, #E8FFF5 100%);
    border-color: rgba(54, 211, 153, 0.25);
  }
}

.score-circle-wrapper {
  position: relative;
  width: 140px;
  height: 140px;
  margin: 0 auto 12px;
}

.score-circle {
  width: 100%;
  height: 100%;
  transform: rotate(-90deg);
}

.score-circle-bg {
  fill: none;
  stroke: #e5e7eb;
  stroke-width: 8;
}

.score-circle-progress {
  fill: none;
  stroke-width: 8;
  stroke-linecap: round;
  stroke: $danger-color;
  transition: stroke-dashoffset 1.5s ease-out;

  .passed & {
    stroke: $success-color;
  }
}

.score-circle-content {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  text-align: center;
}

.score-number {
  font-size: 42px;
  font-weight: 800;
  background: linear-gradient(135deg, $danger-color 0%, #F76965 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
  line-height: 1;

  .passed & {
    background: linear-gradient(135deg, $success-color 0%, #7EEAC0 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
}

.score-total {
  font-size: 14px;
  color: $text-muted;
  margin-top: 2px;
}

.score-label {
  font-size: 14px;
  color: $text-muted;
  margin: 4px 0 12px;
}

.pass-tag {
  font-weight: 600;

  .pass-icon {
    margin-right: 4px;
  }
}

.result-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.info-item {
  display: flex;
  align-items: center;
  padding: 10px 0;
  font-size: 14px;
  border-bottom: 1px solid #F7F8FA;

  &:last-child {
    border-bottom: none;
  }
}

.info-label {
  color: $text-muted;
  min-width: 80px;
}

.info-value {
  color: $text-title;
  font-weight: 500;
}

.accuracy-value {
  display: flex;
  align-items: center;
}

// 证书区域
.certificate-section {
  margin-bottom: 24px;
}

.certificate-card {
  position: relative;
  background: linear-gradient(135deg, $warning-bg 0%, $bg-white 50%, $warning-bg 100%);
  border: 2px solid $warning-light;
  border-radius: 16px;
  padding: 28px;
  text-align: center;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(251, 191, 36, 0.15);
}

.cert-shine {
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    45deg,
    transparent 40%,
    rgba(255, 255, 255, 0.4) 50%,
    transparent 60%
  );
  animation: shine-sweep 3s ease-in-out infinite;
  will-change: transform, opacity;
  pointer-events: none;
}

@keyframes shine-sweep {
  0% { transform: translateX(-100%) rotate(45deg); }
  100% { transform: translateX(100%) rotate(45deg); }
}

.cert-content {
  position: relative;
  z-index: 1;
}

.cert-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FFB84D 0%, #FF9500 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 12px;
  box-shadow: 0 4px 12px rgba(251, 191, 36, 0.3);
  animation: cert-bounce 2s ease-in-out infinite;
  will-change: transform, opacity;
}

@keyframes cert-bounce {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-6px); }
}

.cert-title {
  font-size: 18px;
  font-weight: 700;
  color: #E06800;
  margin-bottom: 6px;
}

.cert-desc {
  font-size: 13px;
  color: #E06800;
  margin-bottom: 16px;
}

.cert-btn {
  background: linear-gradient(135deg, #FFB84D 0%, #FF9500 100%);
  border: none;
  font-weight: 600;

  &:hover {
    background: linear-gradient(135deg, #FF9500 0%, #FF9500 100%);
  }
}

// 题目详情
.result-details {
  background: $bg-white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.details-title {
  font-size: 16px;
  font-weight: 700;
  color: $text-title;
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;

  .el-icon {
    color: $primary-color;
  }
}

.detail-item {
  padding: 16px;
  border: 1px solid #e5e6eb;
  border-radius: 12px;
  margin-bottom: 12px;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s;
  background: $bg-white;

  &:hover {
    box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
    transform: translateY(-2px);
  }

  &.correct {
    border-left: 4px solid $success-color;
    background: linear-gradient(90deg, rgba(54, 211, 153, 0.04) 0%, #fff 100%);
  }

  &.wrong {
    border-left: 4px solid $danger-color;
    background: linear-gradient(90deg, rgba(245, 63, 63, 0.04) 0%, #fff 100%);
  }
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.detail-header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.detail-status-icon {
  .status-correct {
    color: $success-color;
    font-size: 20px;
  }

  .status-wrong {
    color: $danger-color;
    font-size: 20px;
  }
}

.detail-index {
  font-size: 13px;
  color: $text-muted;
  font-weight: 500;
}

.detail-score {
  font-size: 14px;
  font-weight: 700;
  padding: 2px 10px;
  border-radius: 12px;

  &.score-full {
    color: $success-color;
    background: rgba(54, 211, 153, 0.1);
  }

  &.score-partial {
    color: $warning-color;
    background: rgba(255, 125, 0, 0.1);
  }

  &.score-zero {
    color: $danger-color;
    background: rgba(245, 63, 63, 0.1);
  }
}

.detail-title {
  font-size: 15px;
  color: $text-title;
  margin-bottom: 12px;
  font-weight: 500;
  line-height: 1.5;
}

.detail-answer {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.answer-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 12px;
  border-radius: 8px;
  background: #f7f8fa;

  &.answer-correct {
    background: rgba(54, 211, 153, 0.08);
    border: 1px solid rgba(54, 211, 153, 0.2);
  }

  &.answer-wrong {
    background: rgba(245, 63, 63, 0.08);
    border: 1px solid rgba(245, 63, 63, 0.2);
  }

  &.answer-correct-item {
    background: rgba(54, 211, 153, 0.06);
    border: 1px solid rgba(54, 211, 153, 0.15);
  }
}

.answer-label {
  font-size: 12px;
  color: $text-muted;
  font-weight: 500;
}

.detail-analysis {
  display: flex;
  align-items: flex-start;
  gap: 8px;
  margin-top: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  background: rgba(22, 93, 255, 0.04);
  border: 1px solid rgba(22, 93, 255, 0.12);
  font-size: 13px;
  line-height: 1.7;

  .analysis-label {
    flex-shrink: 0;
    font-weight: 700;
    color: $primary-color;
  }

  .analysis-text {
    flex: 1;
    color: $text-body;
  }
}

pre {
  font-family: 'Consolas', monospace;
  font-size: 13px;
  color: $text-body;
  margin: 0;
  white-space: pre-wrap;
  background: transparent;
  padding: 0;
  border-radius: 0;
}

// 证书弹窗
.cert-dialog-wrapper {
  display: flex;
  justify-content: center;
}

.cert-preview {
  width: 100%;
  max-width: 480px;
  background: linear-gradient(135deg, #FFF6E0 0%, #fff 50%, #E8F0FF 100%);
  border: 2px solid #FFB84D;
  border-radius: 16px;
  padding: 10px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
}

.cert-preview-inner {
  border: 2px solid rgba(251, 191, 36, 0.35);
  border-radius: 10px;
  padding: 30px 26px;
  text-align: center;
  position: relative;
}

.cert-preview-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  margin-bottom: 14px;
}

.cert-preview-logo {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(22, 93, 255, 0.1), rgba(22, 93, 255, 0.05));
  display: flex;
  align-items: center;
  justify-content: center;
}

.cert-preview-org {
  font-size: 13px;
  color: $text-muted;
  letter-spacing: 2px;
}

.cert-preview-title {
  font-size: 30px;
  font-weight: 800;
  color: $text-title;
  letter-spacing: 6px;
  margin-bottom: 4px;
}

.cert-preview-subtitle {
  font-size: 11px;
  color: $text-muted;
  letter-spacing: 3px;
  margin-bottom: 22px;
}

.cert-preview-body {
  font-size: 14px;
  color: $text-body;
  line-height: 2;
  margin-bottom: 22px;

  p {
    margin: 0;
  }

  .cert-preview-name {
    font-size: 24px;
    font-weight: 700;
    color: $text-title;
    margin: 8px 0;
  }
}

.cert-preview-footer {
  border-top: 1px dashed rgba(251, 191, 36, 0.35);
  padding-top: 14px;
}

.cert-preview-meta {
  font-size: 12px;
  color: $text-muted;
  margin-bottom: 8px;
}

.cert-preview-date {
  font-size: 12px;
  color: $text-muted;
}

// 响应式
@media (max-width: 768px) {
  .result-overview {
    flex-direction: column;
    align-items: center;
    padding: 24px;
  }

  .score-card {
    width: 100%;
  }

  .result-info {
    width: 100%;
  }
}
</style>
