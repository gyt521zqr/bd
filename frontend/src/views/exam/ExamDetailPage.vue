<template>
  <div class="exam-detail-page">
    <template v-if="exam.id">
    <!-- 顶部大图Banner -->
    <div class="detail-hero-banner" :style="exam.cover ? { backgroundImage: `url(${exam.cover})` } : {}">
      <div class="hero-particles">
        <span v-for="i in 20" :key="i" class="hero-particle" :style="{
          left: `${Math.random() * 100}%`,
          top: `${Math.random() * 100}%`,
          animationDelay: `${Math.random() * 5}s`,
          animationDuration: `${3 + Math.random() * 4}s`
        }"></span>
      </div>
      <div class="hero-overlay"></div>
      <div class="hero-content">
        <div class="hero-badge">
          <el-icon size="14"><Trophy /></el-icon>
          码高教育 · 赛事中心
        </div>
        <h1 class="hero-title">{{ exam.title }}</h1>
        <div class="hero-meta" v-if="exam.organizer || exam.language">
          <span class="hero-meta-item" v-if="exam.organizer">
            <el-icon size="14"><OfficeBuilding /></el-icon>
            {{ exam.organizer }}
          </span>
          <span class="hero-meta-item" v-if="exam.language">
            <el-icon size="14"><Monitor /></el-icon>
            {{ exam.language }}
          </span>
        </div>
        <!-- 倒计时 -->
        <div class="countdown-section" v-if="exam.status !== 'ended' && countdownTime > 0">
          <div class="countdown-label">距离{{ exam.status === 'registering' ? '报名截止' : '比赛开始' }}还有</div>
          <div class="countdown-digits">
            <div class="countdown-unit">
              <div class="digit-card">
                <span class="digit-text">{{ countdownDays }}</span>
              </div>
              <span class="digit-label">天</span>
            </div>
            <div class="countdown-separator">:</div>
            <div class="countdown-unit">
              <div class="digit-card">
                <span class="digit-text">{{ countdownHours }}</span>
              </div>
              <span class="digit-label">时</span>
            </div>
            <div class="countdown-separator">:</div>
            <div class="countdown-unit">
              <div class="digit-card">
                <span class="digit-text">{{ countdownMinutes }}</span>
              </div>
              <span class="digit-label">分</span>
            </div>
            <div class="countdown-separator">:</div>
            <div class="countdown-unit">
              <div class="digit-card">
                <span class="digit-text">{{ countdownSeconds }}</span>
              </div>
              <span class="digit-label">秒</span>
            </div>
          </div>
        </div>
      </div>
      <!-- 返回按钮 -->
      <div class="hero-back">
        <el-button text :icon="ArrowLeft" @click="router.back()" class="back-btn">
          返回赛事中心
        </el-button>
      </div>
    </div>

    <!-- 主体内容 -->
    <div class="detail-main">
      <div class="detail-container">
        <!-- 左侧：封面 + 操作 -->
        <div class="detail-left">
          <div class="cover-card">
            <div class="cover-gradient" :style="exam.cover ? { backgroundImage: `url(${exam.cover})` } : {}">
              <div class="cover-overlay">
                <span class="cover-lang">{{ exam.language }}</span>
              </div>
            </div>
            <div class="cover-info">
              <h1 class="exam-name">{{ exam.title }}</h1>
              <div class="exam-status-row">
                <span class="status-tag" :style="{ background: statusColor }">{{ exam.statusText }}</span>
                <span class="lang-tag" v-if="exam.language">{{ exam.language }}</span>
              </div>
              <!-- 底部操作按钮 -->
              <div class="cover-action">
                <el-button
                  v-if="exam.status === 'ongoing'"
                  type="primary"
                  size="large"
                  round
                  class="action-btn pulse-btn"
                  @click="openExamDialog"
                >
                  <el-icon class="btn-icon"><VideoPlay /></el-icon>
                  进入比赛
                </el-button>
                <el-button
                  v-else-if="exam.status === 'registering'"
                  type="warning"
                  size="large"
                  round
                  class="action-btn pulse-btn"
                  @click="router.push(`/exam/register/${examId}`)"
                >
                  <el-icon class="btn-icon"><EditPen /></el-icon>
                  立即报名
                </el-button>
                <el-button
                  v-else
                  type="info"
                  size="large"
                  round
                  class="action-btn"
                  disabled
                >
                  赛事已结束
                </el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 右侧：详细信息 -->
        <div class="detail-right">
          <!-- 试卷信息条 -->
          <div class="info-card paper-info-card">
            <h3 class="card-title">
              <el-icon><DataAnalysis /></el-icon>
              试卷信息
            </h3>
            <div class="paper-info-grid">
              <div class="paper-info-item">
                <span class="paper-info-value">{{ paperTotalScore }}</span>
                <span class="paper-info-label">试卷总分</span>
              </div>
              <div class="paper-info-item">
                <span class="paper-info-value">{{ paperPassScore }}</span>
                <span class="paper-info-label">及格分</span>
              </div>
              <div class="paper-info-item">
                <span class="paper-info-value">{{ paperMeta.duration }}</span>
                <span class="paper-info-label">考试时长(分钟)</span>
              </div>
              <div class="paper-info-item">
                <span class="paper-info-value">{{ paperQuestions.length }}</span>
                <span class="paper-info-label">题目数量</span>
              </div>
            </div>
          </div>

          <!-- 赛事简介 -->
          <div class="info-card">
            <h3 class="card-title">
              <el-icon><Document /></el-icon>
              赛事简介
            </h3>
            <p class="card-text" v-if="exam.description">{{ exam.description }}</p>
            <p class="card-text info-empty" v-else>暂未公布</p>
          </div>

          <!-- 参赛对象 -->
          <div class="info-card">
            <h3 class="card-title">
              <el-icon><User /></el-icon>
              参赛对象
            </h3>
            <div class="participant-progress" v-if="exam.maxParticipants > 0">
              <div class="progress-header">
                <span class="progress-text">{{ exam.participants }} / {{ exam.maxParticipants }} 人已报名</span>
                <span class="progress-percent">{{ Math.round((exam.participants / exam.maxParticipants) * 100) }}%</span>
              </div>
              <div class="progress-bar-bg">
                <div class="progress-bar-fill" :style="{ width: `${Math.min((exam.participants / exam.maxParticipants) * 100, 100)}%` }"></div>
              </div>
            </div>
            <p class="info-empty" v-else>暂未公布</p>
          </div>

          <!-- 赛程时间轴 -->
          <div class="info-card">
            <h3 class="card-title">
              <el-icon><Clock /></el-icon>
              赛程安排
            </h3>
            <div class="custom-timeline" v-if="timeline.length">
              <div
                v-for="(item, idx) in timeline"
                :key="idx"
                class="timeline-item"
                :class="item.status"
              >
                <div class="timeline-dot"></div>
                <div class="timeline-line" v-if="idx < timeline.length - 1"></div>
                <div class="timeline-content">
                  <div class="timeline-title">{{ item.title }}</div>
                  <div class="timeline-time">{{ item.time }}</div>
                </div>
              </div>
            </div>
            <p class="info-empty" v-else>暂未公布</p>
          </div>

          <!-- 奖项设置 -->
          <div class="info-card">
            <h3 class="card-title">
              <el-icon><Medal /></el-icon>
              奖项设置
            </h3>
            <div class="award-list" v-if="exam.prizes.length">
              <div class="award-item" v-for="(a, idx) in exam.prizes" :key="idx">
                <span class="award-level" :style="{ color: a.color }">{{ a.level }}</span>
                <span class="award-desc">{{ a.desc }}</span>
              </div>
            </div>
            <p class="info-empty" v-else>暂未公布</p>
          </div>

          <!-- 赛事公告 - 比赛须知 -->
          <div class="info-card">
            <h3 class="card-title">
              <el-icon><Warning /></el-icon>
              比赛须知
            </h3>
            <div class="announcement-content" v-if="announcementLines.length">
              <p v-for="(line, idx) in announcementLines" :key="idx">{{ line }}</p>
            </div>
            <p class="info-empty" v-else>暂未公布</p>
          </div>

          <!-- 考试信息 -->
          <div class="info-card" v-if="exam.examAddress || exam.examTime">
            <h3 class="card-title">
              <el-icon><Location /></el-icon>
              考试信息
            </h3>
            <div class="exam-info-grid">
              <div class="info-row" v-if="exam.examTime">
                <span class="info-label">考试时间：</span>
                <span class="info-value">{{ exam.examTime }}</span>
              </div>
              <div class="info-row">
                <span class="info-label">考试地点：</span>
                <span class="info-value">{{ exam.examAddress || '暂未公布' }}</span>
              </div>
              <div class="info-row" v-if="exam.examDuration">
                <span class="info-label">考试时长：</span>
                <span class="info-value">{{ exam.examDuration }}</span>
              </div>
              <div class="info-row" v-if="exam.totalScore">
                <span class="info-label">满分：</span>
                <span class="info-value">{{ exam.totalScore }} 分</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 比赛须知弹窗 -->
    <el-dialog
      v-model="examDialogVisible"
      title="比赛须知"
      width="560px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      class="exam-notice-dialog"
    >
      <div class="dialog-announcement" v-if="announcementLines.length">
        <p v-for="(line, idx) in announcementLines" :key="idx">{{ line }}</p>
      </div>
      <p class="info-empty" v-else>暂未公布</p>
      <template #footer>
        <div class="dialog-footer">
          <span class="countdown-text">
            请仔细阅读以上内容，<strong class="countdown-number">{{ countdown }}</strong>秒后可进入考试
          </span>
          <el-button
            type="primary"
            :disabled="countdown > 0"
            @click="enterExam"
          >
            进入考试
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 移动端底部固定操作按钮 -->
    <div class="mobile-fixed-bar" v-if="exam.status !== 'ended'">
      <el-button
        v-if="exam.status === 'ongoing'"
        type="primary"
        size="large"
        round
        class="mobile-action-btn pulse-btn"
        @click="openExamDialog"
      >
        进入比赛
      </el-button>
      <el-button
        v-else-if="exam.status === 'registering'"
        type="warning"
        size="large"
        round
        class="mobile-action-btn pulse-btn"
        @click="router.push(`/exam/register/${examId}`)"
      >
        立即报名
      </el-button>
    </div>
    </template>
    <div v-else class="detail-empty">
      <el-empty description="暂无赛事详情" />
    </div>

  </div>
</template>

<script setup lang="ts">
/**
 * ExamDetailPage - 赛事详情页
 * 赛事基本信息来自真实接口（getExamList）；后端未下发的字段显示「暂未公布」
 * 试卷信息条依赖本地题库业务模块（@/mock/examQuestions），与做题页共用同一题源
 * 接口失败或查无此赛事时保持空态，显示「暂无赛事详情」
 */
import { computed, ref, onUnmounted, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  ArrowLeft, Document, User, Clock, Medal, Warning, Location,
  Trophy, OfficeBuilding, Monitor, VideoPlay, EditPen, DataAnalysis
} from '@element-plus/icons-vue'
import { getExamQuestions, calcTotalScore, getExamMeta } from '@/mock/examQuestions'
import { getExamList } from '@/api/exam'
import type { ExamItem } from '@/types'

const route = useRoute()
const router = useRouter()
const examId = computed(() => Number(route.params.id))

// ========== 倒计时逻辑 ==========
const countdownTime = ref(0)
const countdownDays = ref('00')
const countdownHours = ref('00')
const countdownMinutes = ref('00')
const countdownSeconds = ref('00')
let countdownTimer: ReturnType<typeof setInterval> | null = null

function initCountdown() {
  const e = exam.value
  let targetDate: Date

  if (e.status === 'registering') {
    if (!e.registerDeadline) return
    targetDate = new Date(e.registerDeadline + 'T23:59:59')
  } else if (e.status === 'ongoing') {
    if (!e.examTime) return
    targetDate = new Date(e.examTime.split(' ')[0] + 'T' + (e.examTime.split(' ')[1] || '09:00'))
  } else {
    return
  }

  // 日期缺失或非法时不启动倒计时，避免显示无效数字
  if (isNaN(targetDate.getTime())) return

  function update() {
    const now = new Date().getTime()
    const target = targetDate.getTime()
    const diff = target - now
    countdownTime.value = Math.max(0, diff)

    if (diff <= 0) {
      countdownDays.value = '00'
      countdownHours.value = '00'
      countdownMinutes.value = '00'
      countdownSeconds.value = '00'
      return
    }

    const days = Math.floor(diff / (1000 * 60 * 60 * 24))
    const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
    const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
    const seconds = Math.floor((diff % (1000 * 60)) / 1000)

    countdownDays.value = String(days).padStart(2, '0')
    countdownHours.value = String(hours).padStart(2, '0')
    countdownMinutes.value = String(minutes).padStart(2, '0')
    countdownSeconds.value = String(seconds).padStart(2, '0')
  }

  update()
  countdownTimer = setInterval(update, 1000)
}

onMounted(() => {
  loadExamDetail()
})

onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
})

// ========== 赛事数据映射 ==========
interface ExamData {
  id: number
  title: string
  cover: string
  status: 'ongoing' | 'registering' | 'ended'
  statusText: string
  language: string
  organizer: string
  description: string
  participants: number
  maxParticipants: number
  startDate: string
  endDate: string
  registerDeadline: string
  examDuration: string
  totalScore: number
  prizes: { level: string; desc: string; color: string }[]
  announcement: string
  registerAnnouncement: string
  examAddress: string
  examTime: string
}

// 无真实赛事数据时的空态对象：所有事实性字段留空，由模板显示「暂未公布」
const emptyExam: ExamData = {
  id: 0,
  title: '',
  cover: '',
  status: 'ended',
  statusText: '',
  language: '',
  organizer: '',
  description: '',
  participants: 0,
  maxParticipants: 0,
  startDate: '',
  endDate: '',
  registerDeadline: '',
  examDuration: '',
  totalScore: 0,
  prizes: [],
  announcement: '',
  registerAnnouncement: '',
  examAddress: '',
  examTime: '',
}

// 当前赛事数据：仅由真实接口填充；未加载 / 查无此赛事 / 接口失败时保持空态
const exam = ref<ExamData>({ ...emptyExam })

// 后端状态 -> 页面状态映射（与赛事中心列表一致）
const examStatusMap: Record<string, { status: 'ongoing' | 'registering' | 'ended'; statusText: string }> = {
  not_started: { status: 'registering', statusText: '报名中' },
  in_progress: { status: 'ongoing', statusText: '进行中' },
  submitted: { status: 'ended', statusText: '已结束' },
  graded: { status: 'ended', statusText: '已结束' },
}

/** 只映射后端真实下发的字段；机构、奖项、地址、须知等未下发字段一律留空 */
function normalizeExamDetail(item: ExamItem): ExamData {
  const mapped = examStatusMap[item.status] || { status: 'ended' as const, statusText: '' }
  const datePart = (s?: string) => (s || '').split(' ')[0] || ''
  return {
    ...emptyExam,
    id: item.id,
    title: item.title || '',
    description: item.description || '',
    status: mapped.status,
    statusText: mapped.statusText,
    startDate: datePart(item.startTime),
    endDate: datePart(item.endTime),
    examTime: item.startTime || '',
    examDuration: item.duration ? `${item.duration}分钟` : '',
    totalScore: item.totalScore || 0,
  }
}

/** 从真实接口加载赛事详情；失败或查无此赛事时保持空态并给出明确提示 */
async function loadExamDetail() {
  try {
    const res = await getExamList({ pageNum: 1, pageSize: 100 })
    const list = res?.data?.list
    const matched = Array.isArray(list) ? list.find((item) => item.id === examId.value) : undefined
    if (matched) {
      exam.value = normalizeExamDetail(matched)
      initCountdown()
    } else {
      ElMessage.warning('暂无该赛事详情')
    }
  } catch {
    // 接口失败：保持空态，由模板显示「暂无赛事详情」
    ElMessage.error('赛事详情加载失败，请稍后重试')
  }
}

// ========== 试卷信息条（与做题页共用同一题源） ==========
const paperQuestions = computed(() => getExamQuestions(examId.value))
const paperTotalScore = computed(() => calcTotalScore(paperQuestions.value))
const paperPassScore = computed(() => Math.round(paperTotalScore.value * 0.6))
const paperMeta = computed(() => getExamMeta(examId.value))



// 状态颜色
const statusColor = computed(() => {
  const map: Record<string, string> = {
    registering: '#FF7D00',
    ongoing: '#36D399',
    ended: '#86909C',
  }
  return map[exam.value.status] || '#86909C'
})

// 公告文本分行
const announcementLines = computed(() => {
  return exam.value.announcement.split('\n').filter((line) => line.trim())
})

// 时间轴数据
const timeline = computed(() => {
  const e = exam.value
  const now = new Date().getTime()
  const items: { title: string; time: string; status: string }[] = []

  // 后端未下发完整赛程日期时不绘制时间轴，由模板显示「暂未公布」
  if (!e.startDate || !e.registerDeadline || !e.endDate) return items

  const start = new Date(e.startDate + 'T00:00:00').getTime()
  const deadline = new Date(e.registerDeadline + 'T23:59:59').getTime()
  const end = new Date(e.endDate + 'T23:59:59').getTime()

  // 报名开始
  items.push({
    title: '报名开始',
    time: e.startDate + ' 00:00',
    status: now > start ? 'past' : now === start ? 'current' : 'future',
  })

  // 报名截止
  items.push({
    title: '报名截止',
    time: e.registerDeadline + ' 23:59',
    status: now > deadline ? 'past' : now >= start && now <= deadline ? 'current' : 'future',
  })

  if (e.status !== 'ended') {
    const examDate = e.examTime.split(' ')[0] + 'T' + (e.examTime.split(' ')[1] || '09:00')
    const examTime = new Date(examDate).getTime()
    items.push({
      title: '比赛开始',
      time: e.examTime,
      status: now > examTime ? 'past' : now >= deadline && now <= examTime ? 'current' : 'future',
    })
  }

  items.push({
    title: '成绩公布',
    time: e.endDate,
    status: now > end ? 'past' : 'future',
  })

  return items
})

// ========== 比赛须知弹窗 ==========
const examDialogVisible = ref(false)
const countdown = ref(10)
let examCountdownTimer: ReturnType<typeof setInterval> | null = null

function openExamDialog() {
  examDialogVisible.value = true
  countdown.value = 10
  examCountdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      if (examCountdownTimer) {
        clearInterval(examCountdownTimer)
        examCountdownTimer = null
      }
    }
  }, 1000)
}

function enterExam() {
  if (examCountdownTimer) {
    clearInterval(examCountdownTimer)
    examCountdownTimer = null
  }
  examDialogVisible.value = false
  router.push(`/exam-taking/${examId.value}`)
}

// 组件卸载时清除定时器
onUnmounted(() => {
  if (examCountdownTimer) {
    clearInterval(examCountdownTimer)
    examCountdownTimer = null
  }
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
})
</script>

<style scoped lang="scss">
$card-bg: #ffffff;
$border-color: #E5E6EB;

.exam-detail-page {
  background: $bg-page;
  min-height: 100vh;
  padding-bottom: 40px;
}

// 无真实赛事数据时的空态占位
.detail-empty {
  min-height: 60vh;
  display: flex;
  align-items: center;
  justify-content: center;
}

// 后端未下发字段时的中性空态
.info-empty {
  font-size: 14px;
  color: $text-muted;
  margin: 0;
}

// ========== 顶部大图Banner ==========
.detail-hero-banner {
  position: relative;
  height: 320px;
  background: linear-gradient(135deg, #0D1117 0%, #165DFF 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 0 0 24px 24px;
}

.hero-particles {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 1;
}

.hero-particle {
  position: absolute;
  width: 4px;
  height: 4px;
  background: rgba(255, 255, 255, 0.4);
  border-radius: 50%;
  animation: particleFloat linear infinite;
  will-change: transform, opacity;
}

@keyframes particleFloat {
  0% {
    transform: translateY(0) scale(1);
    opacity: 0;
  }
  20% {
    opacity: 1;
  }
  80% {
    opacity: 0.6;
  }
  100% {
    transform: translateY(-120px) scale(0.3);
    opacity: 0;
  }
}

.hero-overlay {
  position: absolute;
  inset: 0;
  background: linear-gradient(
    180deg,
    rgba(0, 0, 0, 0.1) 0%,
    rgba(0, 0, 0, 0.35) 100%
  );
  z-index: 2;
}

.hero-content {
  position: relative;
  z-index: 3;
  text-align: center;
  color: #fff;
  padding: 0 24px;
  max-width: 800px;
}

.hero-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 6px 16px;
  background: rgba(255, 255, 255, 0.15);
  border: 1px solid rgba(255, 255, 255, 0.25);
  border-radius: 20px;
  font-size: 13px;
  margin-bottom: 16px;
  backdrop-filter: blur(8px);
}

.hero-title {
  font-size: 32px;
  font-weight: 800;
  margin: 0 0 12px;
  letter-spacing: 1px;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.3);
}

.hero-meta {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-bottom: 20px;
  flex-wrap: wrap;
}

.hero-meta-item {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;
  color: rgba(255, 255, 255, 0.85);
  .el-icon {
    color: rgba(255, 255, 255, 0.7);
  }
}

// 倒计时
.countdown-section {
  margin-top: 8px;
}

.countdown-label {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.7);
  margin-bottom: 10px;
}

.countdown-digits {
  display: flex;
  justify-content: center;
  align-items: flex-start;
  gap: 8px;
}

.countdown-unit {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.digit-card {
  width: 56px;
  height: 64px;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.1);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
}

.digit-text {
  font-size: 28px;
  font-weight: 800;
  color: #fff;
  font-variant-numeric: tabular-nums;
  letter-spacing: 1px;
}

.digit-label {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
}

.countdown-separator {
  font-size: 24px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.5);
  margin-top: 12px;
}

// 返回按钮
.hero-back {
  position: absolute;
  top: 20px;
  left: 24px;
  z-index: 4;
}

.back-btn {
  color: rgba(255, 255, 255, 0.9) !important;
  background: rgba(0, 0, 0, 0.2) !important;
  backdrop-filter: blur(8px);
  border-radius: 20px;
  padding: 8px 16px;
  &:hover {
    background: rgba(0, 0, 0, 0.35) !important;
    color: #fff !important;
  }
}

// ========== 主体内容 ==========
.detail-main {
  padding: 24px 0;
}

.detail-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
  display: grid;
  grid-template-columns: 380px 1fr;
  gap: 24px;
}

// ========== 左侧封面卡片 ==========
.detail-left {
  .cover-card {
    background: $card-bg;
    border-radius: 16px;
    overflow: hidden;
    box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
    position: sticky;
    top: 24px;
    border: 1px solid $border-color;
  }
}

.cover-gradient {
  height: 200px;
  background: linear-gradient(135deg, #0D1117 0%, #165DFF 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 60px;
    background: linear-gradient(to top, rgba(0, 0, 0, 0.3), transparent);
  }
}

.cover-overlay {
  position: relative;
  z-index: 1;
}

.cover-lang {
  color: rgba(255, 255, 255, 0.9);
  font-size: 28px;
  font-weight: 800;
  letter-spacing: 2px;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.cover-info {
  padding: 20px;
}

.exam-name {
  font-size: 18px;
  font-weight: 700;
  color: $text-title;
  margin: 0 0 12px;
  line-height: 1.5;
}

.exam-status-row {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.status-tag {
  display: inline-block;
  padding: 3px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  color: #fff;
}

.lang-tag {
  display: inline-block;
  padding: 3px 12px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  color: $primary-color;
  background: rgba($primary-color, 0.1);
}

.cover-action {
  margin-top: 4px;
}

.action-btn {
  width: 100%;
  font-weight: 600;
  font-size: 15px;
  height: 46px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;

  .btn-icon {
    font-size: 18px;
  }
}

// 脉冲动画按钮
.pulse-btn {
  position: relative;
  overflow: visible;

  &::before {
    content: '';
    position: absolute;
    inset: -4px;
    border-radius: 24px;
    border: 2px solid currentColor;
    opacity: 0;
    animation: pulseRing 2s ease-out infinite;
    will-change: transform, opacity;
  }
}

@keyframes pulseRing {
  0% {
    transform: scale(1);
    opacity: 0.5;
  }
  100% {
    transform: scale(1.08);
    opacity: 0;
  }
}

// ========== 右侧信息区 ==========
.detail-right {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-card {
  background: $card-bg;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  border: 1px solid $border-color;
  transition: box-shadow 0.3s ease, transform 0.3s ease;

  &:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    transform: translateY(-1px);
  }
}

.card-title {
  font-size: 16px;
  font-weight: 700;
  color: $text-title;
  margin: 0 0 16px;
  display: flex;
  align-items: center;
  gap: 8px;

  .el-icon {
    color: $primary-color;
    font-size: 18px;
  }
}

.card-text {
  font-size: 14px;
  color: $text-body;
  line-height: 1.8;
  margin: 0;
}

.card-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;

  .card-title {
    margin: 0;
  }
}

.preview-open-btn {
  font-size: 13px;
  padding: 4px 8px;
  flex-shrink: 0;
}

// ========== 试卷信息条 ==========
.paper-info-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.paper-info-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  padding: 16px 8px;
  background: #f7f8fa;
  border-radius: 10px;

  .paper-info-value {
    font-size: 24px;
    font-weight: 800;
    color: $primary-color;
    line-height: 1;
  }

  .paper-info-label {
    font-size: 12px;
    color: $text-muted;
  }
}

// ========== 报名人数进度条 ==========
.participant-progress {
  .progress-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
  }

  .progress-text {
    font-size: 14px;
    color: $text-body;
  }

  .progress-percent {
    font-size: 14px;
    font-weight: 700;
    color: $primary-color;
  }

  .progress-bar-bg {
    height: 8px;
    background: #E5E6EB;
    border-radius: 4px;
    overflow: hidden;
  }

  .progress-bar-fill {
    height: 100%;
    background: linear-gradient(90deg, $primary-color, #4080FF);
    border-radius: 4px;
    transition: width 1s cubic-bezier(0.4, 0, 0.2, 1);
  }
}

// ========== 奖项列表 ==========
.award-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.award-item {
  display: flex;
  align-items: baseline;
  gap: 12px;
  padding: 12px 16px;
  background: #f7f8fa;
  border-radius: 10px;
  transition: background 0.2s ease;

  &:hover {
    background: #F7F8FA;
  }
}

.award-level {
  font-size: 14px;
  font-weight: 700;
  flex-shrink: 0;
  min-width: 72px;
}

.award-desc {
  font-size: 13px;
  color: $text-body;
}

// ========== 公告内容 ==========
.announcement-content {
  p {
    font-size: 14px;
    color: $text-body;
    line-height: 1.8;
    margin: 0 0 6px;

    &:last-child {
      margin-bottom: 0;
    }
  }
}

// ========== 考试信息 ==========
.exam-info-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.info-row {
  display: flex;
  align-items: baseline;
  padding: 12px 16px;
  background: #f7f8fa;
  border-radius: 10px;
  transition: background 0.2s ease;

  &:hover {
    background: #F7F8FA;
  }
}

.info-label {
  font-size: 14px;
  font-weight: 600;
  color: $text-title;
  flex-shrink: 0;
  min-width: 80px;
}

.info-value {
  font-size: 14px;
  color: $text-body;
}

// ========== 自定义时间轴 ==========
.custom-timeline {
  position: relative;
  padding-left: 24px;
}

.timeline-item {
  position: relative;
  padding-bottom: 24px;

  &.past {
    .timeline-dot {
      background: #86909C;
      box-shadow: 0 0 0 4px rgba(134, 144, 156, 0.2);
    }
    .timeline-title {
      color: #86909C;
    }
    .timeline-time {
      color: #86909C;
    }
  }

  &.current {
    .timeline-dot {
      background: #165DFF;
      box-shadow: 0 0 0 4px rgba(22, 93, 255, 0.2);
      animation: dotPulse 2s ease-in-out infinite;
      will-change: transform, opacity;
    }
    .timeline-content {
      background: linear-gradient(90deg, rgba(22, 93, 255, 0.08), rgba(22, 93, 255, 0.02), rgba(22, 93, 255, 0.08));
      background-size: 200% 100%;
      animation: marqueeFlow 2.5s linear infinite;
      will-change: transform, opacity;
      border-radius: 8px;
      padding: 10px 14px;
      border: 1px solid rgba(22, 93, 255, 0.2);
    }
    .timeline-title {
      color: #165DFF;
      font-weight: 700;
    }
    .timeline-time {
      color: #165DFF;
    }
  }

  &.future {
    .timeline-dot {
      background: $bg-white;
      border: 2px solid #E5E6EB;
    }
    .timeline-title {
      color: $text-title;
    }
    .timeline-time {
      color: $text-muted;
    }
  }
}

.timeline-dot {
  position: absolute;
  left: -24px;
  top: 4px;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  z-index: 2;
}

.timeline-line {
  position: absolute;
  left: -19px;
  top: 16px;
  bottom: 0;
  width: 2px;
  background: #E5E6EB;
}

.timeline-content {
  margin-left: 8px;
}

.timeline-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 4px;
}

.timeline-time {
  font-size: 13px;
}

@keyframes marqueeFlow {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

@keyframes dotPulse {
  0%, 100% {
    box-shadow: 0 0 0 4px rgba(22, 93, 255, 0.2);
  }
  50% {
    box-shadow: 0 0 0 8px rgba(22, 93, 255, 0.1);
  }
}

// ========== 弹窗样式 ==========
.exam-notice-dialog {
  .dialog-announcement {
    max-height: 400px;
    overflow-y: auto;
    padding: 16px;
    background: #f7f8fa;
    border-radius: 8px;

    p {
      font-size: 14px;
      color: $text-body;
      line-height: 2;
      margin: 0 0 8px;

      &:last-child {
        margin-bottom: 0;
      }
    }
  }

  .dialog-footer {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }

  .countdown-text {
    font-size: 14px;
    color: $text-muted;

    .countdown-number {
      color: $warning-color;
      font-size: 20px;
      font-weight: 700;
      display: inline-block;
      min-width: 24px;
      text-align: center;
    }
  }
}

// ========== 移动端底部固定按钮 ==========
.mobile-fixed-bar {
  display: none;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 16px;
  background: $bg-white;
  border-top: 1px solid $border-color;
  z-index: 200;
}

.mobile-action-btn {
  width: 100%;
  font-weight: 600;
  font-size: 15px;
  height: 44px;
}

</style>
