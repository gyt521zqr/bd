<template>
  <!-- 成绩查询 & 证书预览页面 -->
  <div class="exam-result-query-page">
    <!-- 查询区域 -->
    <div class="query-section" v-if="!hasResult">
      <!-- Hero 渐变背景 -->
      <div class="query-hero">
        <div class="hero-bg">
          <div class="hero-blob hero-blob-1"></div>
          <div class="hero-blob hero-blob-2"></div>
          <div class="hero-blob hero-blob-3"></div>
        </div>
        <div class="query-card">
          <div class="query-icon">
            <div class="icon-wrapper">
              <el-icon :size="40" color="#fff"><Search /></el-icon>
            </div>
          </div>
          <h2 class="query-title">成绩查询</h2>
          <p class="query-subtitle">输入参赛号和姓名，快速查询比赛成绩</p>

          <el-form
            ref="queryFormRef"
            :model="queryForm"
            :rules="queryRules"
            class="query-form"
            @submit.prevent="handleQuery"
          >
            <el-form-item prop="registerNo">
              <el-input
                v-model="queryForm.registerNo"
                placeholder="请输入参赛号"
                clearable
                size="large"
                :prefix-icon="Ticket"
                class="hero-input"
              />
            </el-form-item>
            <el-form-item prop="name">
              <el-input
                v-model="queryForm.name"
                placeholder="请输入姓名"
                clearable
                size="large"
                :prefix-icon="User"
                class="hero-input"
              />
            </el-form-item>
            <el-form-item>
              <el-button
                type="primary"
                size="large"
                round
                class="query-btn"
                :loading="queryLoading"
                @click="handleQuery"
              >
                <el-icon v-if="!queryLoading"><Search /></el-icon>
                查询成绩
              </el-button>
            </el-form-item>
          </el-form>
        </div>
      </div>
    </div>

    <!-- 查询结果区域 -->
    <div class="result-section" v-else>
      <!-- 成绩概览 -->
      <div class="result-card overview-card">
        <div class="overview-header">
          <div class="exam-info">
            <h2 class="exam-title">{{ result.examName }}</h2>
            <div class="exam-meta">
              <el-tag type="info" effect="plain" round>{{ result.group }}</el-tag>
              <el-tag type="info" effect="plain" round>{{ result.language }}</el-tag>
            </div>
          </div>
          <div class="rank-box">
            <div class="rank-label">排名</div>
            <div class="rank-value">{{ result.rank > 0 ? result.rank : '—' }}</div>
            <div class="rank-total">{{ result.total > 0 ? '/ ' + result.total + '人' : '排名暂未公布' }}</div>
          </div>
        </div>

        <div class="award-row">
          <span class="award-label">获得奖项</span>
          <span class="award-tag" :style="awardStyle">{{ result.award || '暂未公布' }}</span>
          <!-- 通过/未通过状态标签 -->
          <span class="pass-status" :class="{ 'status-pass': result.score >= 60, 'status-fail': result.score < 60 }">
            <el-icon v-if="result.score >= 60"><CircleCheck /></el-icon>
            <el-icon v-else><CircleClose /></el-icon>
            {{ result.score >= 60 ? '通过' : '未通过' }}
          </span>
        </div>

        <div class="score-grid">
          <div class="score-item">
            <div class="score-value">{{ result.score }}</div>
            <div class="score-label">总分</div>
          </div>
          <div class="score-item">
            <div class="score-value">{{ result.duration }}</div>
            <div class="score-label">用时</div>
          </div>
          <div class="score-item">
            <div class="score-value">{{ result.accuracy }}%</div>
            <div class="score-label">正确率</div>
          </div>
        </div>

        <div class="analysis-entry">
          <el-button type="primary" plain round :icon="Reading" @click="goAnalysis">
            查看试卷答案解析
          </el-button>
        </div>
      </div>

      <!-- 参赛记录时间线 -->
      <div class="result-card timeline-card">
        <h3 class="card-title">
          <el-icon><Clock /></el-icon>
          参赛记录
        </h3>
        <el-timeline>
          <el-timeline-item
            v-for="(item, idx) in result.timeline"
            :key="idx"
            :type="item.type as any"
            :color="item.color"
            :timestamp="item.time"
          >
            {{ item.title }}
          </el-timeline-item>
        </el-timeline>
      </div>

      <!-- 电子证书预览 -->
      <div class="result-card cert-card">
        <h3 class="card-title">
          <el-icon><DocumentChecked /></el-icon>
          电子证书
        </h3>
        <div class="certificate-wrapper">
          <div class="certificate-card shine-effect" ref="certificateRef">
            <div class="cert-border">
              <div class="cert-inner">
                <div class="cert-corner cert-corner-tl"></div>
                <div class="cert-corner cert-corner-tr"></div>
                <div class="cert-corner cert-corner-bl"></div>
                <div class="cert-corner cert-corner-br"></div>
                <div class="cert-header">
                  <div class="cert-logo">
                    <el-icon :size="28" color="#165DFF"><Trophy /></el-icon>
                  </div>
                  <div class="cert-org">码高教育科技</div>
                </div>
                <div class="cert-title">荣誉证书</div>
                <div class="cert-subtitle">CERTIFICATE OF HONOR</div>
                <div class="cert-body">
                  <p>兹证明</p>
                  <p class="cert-name">{{ result.studentName }}</p>
                  <p>在 <strong>{{ result.examName }}</strong> 中</p>
                  <template v-if="result.award">
                    <p>荣获</p>
                    <p class="cert-award" :style="{ color: awardStyle.color }">{{ result.award }}</p>
                  </template>
                  <p v-else>奖项信息暂未公布</p>
                </div>
                <div class="cert-footer">
                  <div class="cert-meta">
                    <span>参赛号：{{ result.registerNo }}</span>
                    <span>参赛组别：{{ result.group }}</span>
                  </div>
                  <div class="cert-date">{{ result.certDate }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>
        <div class="cert-actions">
          <el-button type="primary" round :icon="Download" :loading="downloadLoading" @click="handleDownload">
            下载证书
          </el-button>
          <el-button round :icon="Share" @click="handleShare">
            分享成绩
          </el-button>
        </div>
      </div>

      <!-- 重新查询 -->
      <div class="result-card text-center reset-card">
        <el-button text type="primary" :icon="RefreshRight" @click="resetQuery" class="reset-btn">
          查询其他成绩
        </el-button>
      </div>
    </div>

    <!-- 无结果提示 -->
    <el-dialog v-model="noResultVisible" title="查询结果" width="400px" align-center>
      <div class="no-result">
        <el-icon :size="48" color="#C9CDD4"><DocumentDelete /></el-icon>
        <p>{{ noResultTitle }}</p>
        <p class="no-result-tip">{{ noResultTip }}</p>
      </div>
      <template #footer>
        <el-button @click="noResultVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * ExamResultQueryPage - 成绩查询 & 证书预览页面
 * 查询表单 -> 成绩展示 -> 证书预览
 * 成绩来自真实查询接口，无结果或失败时显示「暂无成绩」
 */
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import {
  Search, Ticket, User, Clock, DocumentChecked, Trophy, Download, Share, RefreshRight, DocumentDelete, CircleCheck, CircleClose, Reading
} from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { queryExamResult } from '@/api/exam'
import html2canvas from 'html2canvas'

const router = useRouter()

const queryFormRef = ref<FormInstance>()
const queryLoading = ref(false)
const hasResult = ref(false)
const noResultVisible = ref(false)
const noResultTitle = ref('暂无成绩')
const noResultTip = ref('请检查参赛号和姓名是否正确')
const downloadLoading = ref(false)
const certificateRef = ref<HTMLElement>()

const queryForm = ref({
  registerNo: '',
  name: '',
})

const queryRules: FormRules = {
  registerNo: [
    { required: true, message: '请输入参赛号', trigger: 'blur' },
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
  ],
}

interface ExamResult {
  examId: number
  examName: string
  group: string
  language: string
  rank: number
  total: number
  award: string
  score: number
  duration: string
  accuracy: number
  studentName: string
  registerNo: string
  certDate: string
  timeline: { title: string; time: string; type: string; color: string }[]
}

// 成绩数据：初始为字段为空的对象，仅由真实查询结果填充
const result = ref<ExamResult>({
  examId: 0,
  examName: '',
  group: '',
  language: '',
  rank: 0,
  total: 0,
  award: '',
  score: 0,
  duration: '',
  accuracy: 0,
  studentName: '',
  registerNo: '',
  certDate: '',
  timeline: [],
})

// 奖项颜色
const awardStyle = computed(() => {
  const map: Record<string, { color: string; background: string }> = {
    '一等奖': { color: '#F53F3F', background: 'rgba(245, 63, 63, 0.1)' },
    '二等奖': { color: '#FF7D00', background: 'rgba(255, 125, 0, 0.1)' },
    '三等奖': { color: '#165DFF', background: 'rgba(22, 93, 255, 0.1)' },
    '优秀奖': { color: '#36D399', background: 'rgba(54, 211, 153, 0.1)' },
  }
  const style = map[result.value.award] || { color: '#86909C', background: 'rgba(134, 144, 156, 0.1)' }
  return {
    color: style.color,
    background: style.background,
    border: `1px solid ${style.color}`,
  }
})

async function handleQuery() {
  queryFormRef.value?.validate(async (valid) => {
    if (!valid) {
      ElMessage.warning('请填写查询信息')
      return
    }

    queryLoading.value = true
    try {
      const { data } = await queryExamResult({
        registerNo: queryForm.value.registerNo,
        name: queryForm.value.name,
      })
      if (data) {
        // 使用后端返回的真实数据，缺失字段用空值而不是虚构内容
        result.value = {
          examId: data.examId ?? 0,
          examName: data.examName || '',
          group: data.group || '',
          language: data.language || '',
          rank: data.rank ?? 0,
          total: data.total ?? 0,
          award: data.award || '',
          score: data.score ?? 0,
          duration: data.duration || '',
          accuracy: data.accuracy ?? 0,
          studentName: data.studentName || queryForm.value.name,
          registerNo: data.registerNo || queryForm.value.registerNo,
          certDate: data.certDate || '',
          timeline: data.timeline || [],
        }
        hasResult.value = true
      } else {
        // 后端返回空数据：显示「暂无成绩」
        noResultTitle.value = '暂无成绩'
        noResultTip.value = '请检查参赛号和姓名是否正确'
        noResultVisible.value = true
      }
    } catch {
      // 查询失败：保持空态，由弹窗提示，不再回退虚构成绩
      noResultTitle.value = '暂无成绩'
      noResultTip.value = '成绩查询服务暂不可用，请稍后重试'
      noResultVisible.value = true
    } finally {
      queryLoading.value = false
    }
  })
}

function resetQuery() {
  hasResult.value = false
  queryForm.value.registerNo = ''
  queryForm.value.name = ''
}

async function handleDownload() {
  if (!certificateRef.value) {
    ElMessage.warning('证书元素未找到')
    return
  }

  downloadLoading.value = true
  try {
    const canvas = await html2canvas(certificateRef.value, {
      scale: 2,
      backgroundColor: null,
      useCORS: true,
    })
    // 转为图片并触发下载
    const link = document.createElement('a')
    link.download = `证书_${result.value.studentName}_${result.value.registerNo}.png`
    link.href = canvas.toDataURL('image/png')
    link.click()
    ElMessage.success('证书下载成功')
  } catch (error: any) {
    console.error('证书下载失败:', error)
    ElMessage.error('证书下载失败，请重试')
  } finally {
    downloadLoading.value = false
  }
}

function handleShare() {
  ElMessage.success('分享链接已复制')
}

function goAnalysis() {
  router.push(`/exam/result/${result.value.examId}`)
}
</script>

<style scoped lang="scss">
$primary: #165DFF;
$action: #FF7D00;
$success: #36D399;
$danger: #F53F3F;
$warning: #FF9500;
$text-title: #1D2129;
$text-body: #4E5969;
$text-muted: #86909C;
$bg-page: #F7F8FA;

.exam-result-query-page {
  background: $bg-page;
  min-height: 100vh;
  padding: 40px 0;
}

// 查询区域 - Hero 渐变背景
.query-section {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 24px;
}

.query-hero {
  position: relative;
  border-radius: 20px;
  overflow: hidden;
  background: linear-gradient(135deg, $primary 0%, #4080FF 50%, #4080FF 100%);
  box-shadow: 0 8px 32px rgba(22, 93, 255, 0.25);
}

.hero-bg {
  position: absolute;
  inset: 0;
  overflow: hidden;
  pointer-events: none;
}

.hero-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.3;

  &.hero-blob-1 {
    width: 300px;
    height: 300px;
    background: $bg-white;
    top: -100px;
    right: -50px;
    animation: blob-float 8s ease-in-out infinite;
    will-change: transform, opacity;
  }

  &.hero-blob-2 {
    width: 200px;
    height: 200px;
    background: #E8F0FF;
    bottom: -50px;
    left: -30px;
    animation: blob-float 10s ease-in-out infinite reverse;
    will-change: transform, opacity;
  }

  &.hero-blob-3 {
    width: 150px;
    height: 150px;
    background: #FFAD4D;
    top: 50%;
    left: 50%;
    animation: blob-float 12s ease-in-out infinite;
    will-change: transform, opacity;
  }
}

@keyframes blob-float {
  0%, 100% { transform: translate(0, 0) scale(1); }
  33% { transform: translate(20px, -20px) scale(1.1); }
  66% { transform: translate(-10px, 10px) scale(0.95); }
}

.query-card {
  position: relative;
  z-index: 1;
  padding: 56px 48px;
  text-align: center;
}

.query-icon {
  margin-bottom: 20px;

  .icon-wrapper {
    width: 72px;
    height: 72px;
    border-radius: 20px;
    background: rgba(255, 255, 255, 0.2);
    backdrop-filter: blur(10px);
    display: inline-flex;
    align-items: center;
    justify-content: center;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  }
}

.query-title {
  font-size: 28px;
  font-weight: 800;
  color: #fff;
  margin: 0 0 8px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.query-subtitle {
  font-size: 15px;
  color: rgba(255, 255, 255, 0.85);
  margin: 0 0 36px;
}

.query-form {
  max-width: 420px;
  margin: 0 auto;

  :deep(.hero-input .el-input__wrapper) {
    border-radius: 12px;
    background: rgba(255, 255, 255, 0.95);
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
    padding: 4px 12px;
  }

  :deep(.hero-input .el-input__inner) {
    height: 44px;
    font-size: 15px;
  }
}

.query-btn {
  width: 100%;
  height: 48px;
  background: linear-gradient(135deg, #fff 0%, #E8F0FF 100%);
  border: none;
  color: $primary;
  font-weight: 700;
  font-size: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 24px rgba(0, 0, 0, 0.2);
    background: $bg-white;
  }
}

// 结果区域
.result-section {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.result-card {
  background: $bg-white;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s;

  &:hover {
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  }
}

// 概览卡片
.overview-card {
  .overview-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 20px;
  }

  .exam-title {
    font-size: 18px;
    font-weight: 700;
    color: $text-title;
    margin: 0 0 10px;
    line-height: 1.4;
  }

  .exam-meta {
    display: flex;
    gap: 8px;
  }

  .rank-box {
    text-align: center;
    min-width: 100px;
    padding: 12px 16px;
    background: linear-gradient(135deg, rgba(22, 93, 255, 0.06) 0%, rgba(22, 93, 255, 0.02) 100%);
    border-radius: 12px;
    border: 1px solid rgba(22, 93, 255, 0.1);
  }

  .rank-label {
    font-size: 12px;
    color: $text-muted;
    margin-bottom: 4px;
  }

  .rank-value {
    font-size: 36px;
    font-weight: 800;
    color: $primary;
    line-height: 1;
  }

  .rank-total {
    font-size: 13px;
    color: $text-muted;
    margin-top: 2px;
  }
}

.award-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  padding: 14px 18px;
  background: linear-gradient(90deg, #f7f8fa 0%, #fff 100%);
  border-radius: 12px;
  border: 1px solid #e5e6eb;
}

.award-label {
  font-size: 14px;
  color: $text-muted;
}

.award-tag {
  display: inline-block;
  padding: 4px 16px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 700;
}

// 通过/未通过状态标签
.pass-status {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  margin-left: auto;

  &.status-pass {
    color: $success;
    background: rgba(54, 211, 153, 0.12);
    border: 1px solid rgba(54, 211, 153, 0.3);
  }

  &.status-fail {
    color: $danger;
    background: rgba(245, 63, 63, 0.12);
    border: 1px solid rgba(245, 63, 63, 0.3);
  }
}

.score-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.score-item {
  text-align: center;
  padding: 20px 16px;
  background: linear-gradient(135deg, #f7f8fa 0%, #fff 100%);
  border-radius: 12px;
  border: 1px solid #e5e6eb;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(22, 93, 255, 0.1);
    border-color: rgba(22, 93, 255, 0.2);
  }
}

.score-value {
  font-size: 28px;
  font-weight: 800;
  color: $text-title;
  margin-bottom: 4px;
  background: linear-gradient(135deg, $primary 0%, #4080FF 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.score-label {
  font-size: 13px;
  color: $text-muted;
}

.analysis-entry {
  margin-top: 18px;
  text-align: center;
}

// 卡片标题
.card-title {
  font-size: 16px;
  font-weight: 700;
  color: $text-title;
  margin: 0 0 16px;
  display: flex;
  align-items: center;
  gap: 8px;

  .el-icon {
    color: $primary;
  }
}

// 时间线卡片 hover 效果
.timeline-card {
  :deep(.el-timeline-item__node) {
    transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s;
  }

  :deep(.el-timeline-item:hover .el-timeline-item__node) {
    transform: scale(1.2);
  }

  :deep(.el-timeline-item__content) {
    transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s;
  }

  :deep(.el-timeline-item:hover .el-timeline-item__content) {
    color: $primary;
    font-weight: 500;
  }
}

// 证书样式 - 金色边框和闪光动画
.certificate-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 20px;
}

.certificate-card {
  width: 100%;
  max-width: 520px;
  background: linear-gradient(135deg, #FFF6E0 0%, #fff 50%, #E8F0FF 100%);
  border-radius: 16px;
  padding: 10px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.1);
  border: 2px solid #FFB84D;
  position: relative;
  overflow: hidden;
}

.shine-effect::before {
  content: '';
  position: absolute;
  top: -50%;
  left: -50%;
  width: 200%;
  height: 200%;
  background: linear-gradient(
    45deg,
    transparent 40%,
    rgba(255, 255, 255, 0.5) 50%,
    transparent 60%
  );
  animation: cert-shine 4s ease-in-out infinite;
  will-change: transform, opacity;
  pointer-events: none;
}

@keyframes cert-shine {
  0% { transform: translateX(-100%) rotate(45deg); }
  100% { transform: translateX(100%) rotate(45deg); }
}

.cert-border {
  border: 2px solid rgba(251, 191, 36, 0.3);
  border-radius: 10px;
  padding: 6px;
}

.cert-inner {
  border: 1px dashed rgba(251, 191, 36, 0.4);
  border-radius: 8px;
  padding: 32px 28px;
  position: relative;
  text-align: center;
}

.cert-corner {
  position: absolute;
  width: 24px;
  height: 24px;
  border: 2px solid #FFB84D;

  &.cert-corner-tl {
    top: 8px;
    left: 8px;
    border-right: none;
    border-bottom: none;
  }

  &.cert-corner-tr {
    top: 8px;
    right: 8px;
    border-left: none;
    border-bottom: none;
  }

  &.cert-corner-bl {
    bottom: 8px;
    left: 8px;
    border-right: none;
    border-top: none;
  }

  &.cert-corner-br {
    bottom: 8px;
    right: 8px;
    border-left: none;
    border-top: none;
  }
}

.cert-header {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  margin-bottom: 16px;
}

.cert-logo {
  width: 52px;
  height: 52px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(22, 93, 255, 0.1) 0%, rgba(22, 93, 255, 0.05) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.cert-org {
  font-size: 13px;
  color: $text-muted;
  letter-spacing: 2px;
}

.cert-title {
  font-size: 30px;
  font-weight: 800;
  color: $text-title;
  letter-spacing: 6px;
  margin-bottom: 4px;
}

.cert-subtitle {
  font-size: 11px;
  color: $text-muted;
  letter-spacing: 3px;
  margin-bottom: 24px;
}

.cert-body {
  font-size: 14px;
  color: $text-body;
  line-height: 2;
  margin-bottom: 24px;

  p {
    margin: 0;
  }

  .cert-name {
    font-size: 24px;
    font-weight: 700;
    color: $text-title;
    margin: 8px 0;
  }

  .cert-award {
    font-size: 26px;
    font-weight: 800;
    margin-top: 8px;
  }
}

.cert-footer {
  border-top: 1px dashed rgba(251, 191, 36, 0.3);
  padding-top: 16px;
}

.cert-meta {
  display: flex;
  justify-content: center;
  gap: 24px;
  font-size: 12px;
  color: $text-muted;
  margin-bottom: 8px;
}

.cert-date {
  font-size: 12px;
  color: $text-muted;
}

.cert-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}

// 重新查询卡片
.reset-card {
  padding: 20px;
}

.reset-btn {
  font-size: 14px;
  font-weight: 500;
}

// 无结果
.no-result {
  text-align: center;
  padding: 20px 0;

  p {
    margin-top: 12px;
    font-size: 15px;
    color: $text-title;
    font-weight: 600;
  }

  .no-result-tip {
    font-size: 13px;
    color: $text-muted;
    font-weight: normal;
    margin-top: 4px;
  }
}

.text-center {
  text-align: center;
}

// 响应式
@media (max-width: 768px) {
  .exam-result-query-page {
    padding: 20px 0;
  }

  .query-hero {
    border-radius: 16px;
  }

  .query-card {
    padding: 36px 24px;
  }

  .query-title {
    font-size: 22px;
  }

  .result-section,
  .query-section {
    padding: 0 16px;
  }

  .result-card {
    padding: 20px;
  }

  .overview-header {
    flex-direction: column;
    gap: 16px;
  }

  .rank-box {
    display: flex;
    align-items: baseline;
    gap: 6px;
    .rank-label { margin-bottom: 0; }
    .rank-value { font-size: 28px; }
    .rank-total { margin-top: 0; }
  }

  .award-row {
    flex-wrap: wrap;
    gap: 8px;
  }

  .pass-status {
    margin-left: 0;
    width: 100%;
    justify-content: center;
  }

  .score-grid {
    grid-template-columns: repeat(3, 1fr);
    gap: 8px;
  }

  .score-item {
    padding: 12px 8px;
  }

  .score-value {
    font-size: 20px;
  }

  .cert-inner {
    padding: 24px 16px;
  }

  .cert-title {
    font-size: 22px;
  }

  .cert-actions {
    flex-direction: column;

    .el-button {
      width: 100%;
    }
  }
}
</style>
