<template>
  <!-- 竞赛房间：全屏竞赛界面，左侧 Scratch 编辑器，右侧题目/排行/状态 -->
  <div class="exam-room-page">
    <!-- 考前须知遮罩 -->
    <div v-if="showNotice" class="notice-overlay">
      <div class="notice-card">
        <div class="notice-header">
          <div class="notice-icon">
            <el-icon :size="32" color="#165DFF"><Warning /></el-icon>
          </div>
          <h2 class="notice-title">考前须知</h2>
        </div>
        <div class="notice-body">
          <div class="notice-item">
            <el-icon :size="18" color="#36D399"><Check /></el-icon>
            <span>竞赛期间禁止切换窗口，切屏超过3次将自动提交</span>
          </div>
          <div class="notice-item">
            <el-icon :size="18" color="#36D399"><Check /></el-icon>
            <span>禁止使用复制粘贴功能</span>
          </div>
          <div class="notice-item">
            <el-icon :size="18" color="#36D399"><Check /></el-icon>
            <span>请确保网络连接稳定</span>
          </div>
          <div class="notice-item">
            <el-icon :size="18" color="#36D399"><Check /></el-icon>
            <span>提交后无法继续编辑，请确认完成后再提交</span>
          </div>
          <div class="notice-item">
            <el-icon :size="18" color="#36D399"><Check /></el-icon>
            <span>竞赛倒计时结束后将自动提交作品</span>
          </div>
        </div>
        <div class="camera-preview">
          <div class="camera-label">
            <el-icon><VideoCamera /></el-icon>
            摄像头预览
          </div>
          <div class="camera-screen">
            <div class="scan-line"></div>
            <div class="camera-placeholder">
              <el-icon :size="32" color="#86909C"><VideoCamera /></el-icon>
              <span>摄像头预览区域</span>
            </div>
          </div>
        </div>
        <div class="notice-actions">
          <el-button
            type="primary"
            size="large"
            round
            class="enter-btn pulse-animation"
            @click="enterExam"
          >
            <el-icon><Right /></el-icon>
            我已阅读，进入考试
          </el-button>
        </div>
      </div>
    </div>

    <!-- 左侧：Scratch 编辑器 -->
    <div class="room-left" :class="{ 'is-locked': isLocked }">
      <BaseEditor
        ref="editorRef"
        :config="editorConfig"
        :question-no="1"
        :countdown-seconds="remainingSeconds"
        :hide-submit="true"
        @submit-success="handleSubmitSuccess"
      />
    </div>

    <!-- 右侧面板 -->
    <div class="room-right">
      <!-- 题目描述 -->
      <div class="panel-section problem-section">
        <div class="section-title">
          <el-icon><Document /></el-icon>
          <span>题目描述</span>
        </div>
        <div class="problem-content" v-html="sanitizeHtml(competitionInfo?.problemDescription || '')"></div>
      </div>

      <!-- 竞赛状态 -->
      <div class="panel-section status-section">
        <div class="section-title">
          <el-icon><Timer /></el-icon>
          <span>竞赛状态</span>
        </div>
        <div class="status-grid">
          <!-- 倒计时 -->
          <div class="status-card countdown-card" :class="{ 'is-urgent': remainingSeconds <= 300 }">
            <div class="status-label">剩余时间</div>
            <div class="status-value">
              {{ formattedTime.hours }}:{{ formattedTime.minutes }}:{{ formattedTime.seconds }}
            </div>
          </div>
          <!-- 已提交人数 -->
          <div class="status-card">
            <div class="status-label">已提交</div>
            <div class="status-value">{{ submittedCount }} / {{ competitionInfo?.currentParticipants || 0 }}</div>
          </div>
          <!-- 切屏警告 -->
          <div class="status-card" :class="{ 'is-warning': switchCount > 0 }">
            <div class="status-label">切屏次数</div>
            <div class="status-value">
              {{ switchCount }} / {{ maxSwitchCount }}
            </div>
          </div>
        </div>
      </div>

      <!-- 实时排行榜 -->
      <div class="panel-section ranking-section">
        <div class="section-title">
          <el-icon><Trophy /></el-icon>
          <span>实时排行榜</span>
          <el-tag size="small" type="success" class="live-tag">LIVE</el-tag>
        </div>
        <div class="ranking-list" v-if="rankingList.length > 0">
          <div
            v-for="item in rankingList"
            :key="item.userId"
            class="ranking-item"
            :class="{ 'is-self': item.userId === currentUserId }"
          >
            <span class="rank-badge" :class="getRankClass(item.rank)">
              {{ item.rank }}
            </span>
            <el-avatar :size="28" :src="item.avatar" class="rank-avatar">
              {{ item.nickname?.charAt(0) || '?' }}
            </el-avatar>
            <span class="rank-name">{{ item.nickname }}</span>
            <span class="rank-score">{{ item.score }} 分</span>
            <span class="rank-time">{{ formatTimeUsed(item.timeUsed) }}</span>
          </div>
        </div>
        <el-empty v-else description="暂无排名数据" :image-size="60" />
      </div>

      <!-- 底部提交按钮 -->
      <div class="panel-section submit-section">
        <el-button
          type="danger"
          size="large"
          class="submit-btn pulse-animation-soft"
          :loading="submitting"
          :disabled="isLocked"
          @click="handleSubmit"
        >
          <el-icon v-if="!submitting && !isLocked"><Check /></el-icon>
          {{ isLocked ? '已提交 - 编辑器已锁定' : '提交作品' }}
        </el-button>
        <p v-if="isLocked" class="submit-tip">作品已提交，无法继续编辑</p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * ExamRoomPage - 竞赛房间
 * 全屏竞赛界面，左侧 Scratch 编辑器，右侧题目/排行/状态面板
 * 包含防作弊：切屏检测、复制粘贴监控
 */
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Document,
  Timer,
  Trophy,
  Check,
  Warning,
  VideoCamera,
  Right,
} from '@element-plus/icons-vue'
import BaseEditor from '@/components/editor/BaseEditor.vue'
import {
  getCompetitionDetail,
  submitCompetitionWork,
  getCompetitionRanking,
} from '@/api/competition'
import { getCompetitionStatus } from '@/api/scratch'
import { sanitizeHtml } from '@/utils/sanitize'
import { useAntiCheat } from '@/hooks/useAntiCheat'
import { useUserStore } from '@/stores/user'
import type {
  CompetitionInfo,
  CompetitionRanking,
  EditorConfig,
} from '@/types/editor'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// ==================== 考前须知 ====================
const showNotice = ref(true)

function enterExam() {
  showNotice.value = false
}

// ==================== 竞赛基本信息 ====================
const competitionId = Number(route.params.competitionId)
const competitionInfo = ref<CompetitionInfo | null>(null)
const currentUserId = computed(() => userStore.userInfo?.id)

// ==================== 编辑器配置 ====================
const editorConfig = computed<EditorConfig>(() => ({
  lang: 'scratch3',
  mode: 'exam',
  questionId: competitionId,
  readOnly: isLocked.value,
}))

const editorRef = ref<InstanceType<typeof BaseEditor>>()

// ==================== 竞赛状态 ====================
const remainingSeconds = ref(0)
const submittedCount = ref(0)
const submitting = ref(false)
const isLocked = ref(false)

/** 格式化倒计时 */
const formattedTime = computed(() => {
  const h = Math.floor(remainingSeconds.value / 3600)
  const m = Math.floor((remainingSeconds.value % 3600) / 60)
  const s = remainingSeconds.value % 60
  return {
    hours: String(h).padStart(2, '0'),
    minutes: String(m).padStart(2, '0'),
    seconds: String(s).padStart(2, '0'),
  }
})

// ==================== 排行榜 ====================
const rankingList = ref<CompetitionRanking[]>([])

// ==================== 防作弊 ====================
const maxSwitchCount = ref(3)
const switchCount = ref(0)

const { switchCount: antiCheatSwitchCount } = useAntiCheat({
  examId: competitionId,
  maxSwitch: 3,
  autoFullscreen: true,
  onExceeded: () => {
    // 超过切屏限制，自动提交
    handleSubmit()
  },
})

// 同步切屏次数
import { watch } from 'vue'
watch(antiCheatSwitchCount, (val) => {
  switchCount.value = val
})

// ==================== 禁止复制粘贴 ====================
function handleCopy(e: ClipboardEvent) {
  e.preventDefault()
  ElMessage.warning('竞赛期间禁止复制')
}
function handlePaste(e: ClipboardEvent) {
  e.preventDefault()
  ElMessage.warning('竞赛期间禁止粘贴')
}
function handleCut(e: ClipboardEvent) {
  e.preventDefault()
}

// ==================== 加载竞赛详情 ====================
async function fetchCompetitionDetail() {
  try {
    const res = await getCompetitionDetail(competitionId)
    if (res?.data) {
      competitionInfo.value = res.data
    }
  } catch {
    ElMessage.error('加载竞赛信息失败')
    router.push('/exam')
  }
}

// ==================== 轮询竞赛状态 ====================
let statusTimer: ReturnType<typeof setInterval> | null = null

async function pollStatus() {
  try {
    const res = await getCompetitionStatus(competitionId)
    if (res?.data) {
      remainingSeconds.value = res.data.remainingTime
      submittedCount.value = res.data.submittedCount

      // 竞赛结束自动提交
      if (res.data.status === 'ENDED' && !isLocked.value) {
        handleSubmit()
      }
    }
  } catch {
    // 轮询失败静默处理
  }
}

// ==================== 轮询排行榜 ====================
let rankingTimer: ReturnType<typeof setInterval> | null = null

async function pollRanking() {
  try {
    const res = await getCompetitionRanking(competitionId)
    if (res?.data) {
      rankingList.value = res.data
    }
  } catch {
    // 轮询失败静默处理
  }
}

// ==================== 提交作品 ====================
async function handleSubmit() {
  if (isLocked.value) return

  try {
    await ElMessageBox.confirm(
      '确定要提交作品吗？提交后将无法继续编辑。',
      '提交确认',
      {
        confirmButtonText: '确定提交',
        cancelButtonText: '继续编辑',
        type: 'warning',
      }
    )

    submitting.value = true

    // 获取 Scratch 项目数据
    const projectData = editorRef.value?.getScratchProject?.()
    if (!projectData) {
      ElMessage.warning('请先完成 Scratch 项目再提交')
      return
    }

    await submitCompetitionWork({
      competitionId,
      scratchProject: projectData,
      switchCount: switchCount.value,
    })

    isLocked.value = true
    ElMessage.success('作品提交成功！')

    // 停止轮询
    if (statusTimer) clearInterval(statusTimer)
    if (rankingTimer) clearInterval(rankingTimer)

    // 3 秒后跳转到成绩页
    setTimeout(() => {
      router.push(`/exam/competition/${competitionId}/result`)
    }, 3000)
  } catch {
    // 用户取消或提交失败
  } finally {
    submitting.value = false
  }
}

function handleSubmitSuccess() {
  // BaseEditor 提交成功回调（备用）
}

// ==================== 排名样式 ====================
function getRankClass(rank: number): string {
  if (rank === 1) return 'rank-gold'
  if (rank === 2) return 'rank-silver'
  if (rank === 3) return 'rank-bronze'
  return ''
}

// ==================== 格式化用时 ====================
function formatTimeUsed(seconds: number): string {
  const m = Math.floor(seconds / 60)
  const s = seconds % 60
  return `${m}分${s}秒`
}

// ==================== 生命周期 ====================
onMounted(async () => {
  // 加载竞赛详情
  await fetchCompetitionDetail()

  // 初始轮询
  pollStatus()
  pollRanking()

  // 启动轮询（状态每 5 秒，排行榜每 10 秒）
  statusTimer = setInterval(pollStatus, 5000)
  rankingTimer = setInterval(pollRanking, 10000)

  // 注册复制粘贴监控
  document.addEventListener('copy', handleCopy)
  document.addEventListener('paste', handlePaste)
  document.addEventListener('cut', handleCut)
})

onUnmounted(() => {
  // 清理轮询
  if (statusTimer) clearInterval(statusTimer)
  if (rankingTimer) clearInterval(rankingTimer)

  // 移除复制粘贴监控
  document.removeEventListener('copy', handleCopy)
  document.removeEventListener('paste', handlePaste)
  document.removeEventListener('cut', handleCut)
})
</script>

<style scoped lang="scss">
$bg-panel: #ffffff;
$border-color: #E5E6EB;

.exam-room-page {
  display: flex;
  width: 100%;
  height: 100%;
  overflow: hidden;
  background: #F7F8FA;
  position: relative;
}

// ==================== 考前须知遮罩 ====================
.notice-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.6);
  backdrop-filter: blur(8px);
  z-index: 1000;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 20px;
  animation: fade-in 0.4s ease-out;
}

@keyframes fade-in {
  from { opacity: 0; }
  to { opacity: 1; }
}

.notice-card {
  background: $bg-white;
  border-radius: 20px;
  padding: 36px 40px;
  max-width: 520px;
  width: 100%;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.2);
  animation: slide-up 0.5s cubic-bezier(0.4, 0, 0.2, 1);
}

@keyframes slide-up {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.95);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.notice-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;
}

.notice-icon {
  width: 52px;
  height: 52px;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(22, 93, 255, 0.1) 0%, rgba(22, 93, 255, 0.05) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.notice-title {
  font-size: 22px;
  font-weight: 700;
  color: $text-title;
  margin: 0;
}

.notice-body {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
}

.notice-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  background: #f7f8fa;
  border-radius: 10px;
  font-size: 14px;
  color: $text-body;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s;

  &:hover {
    background: rgba(22, 93, 255, 0.04);
    transform: translateX(4px);
  }
}

// 摄像头预览区域 - 扫描线效果
.camera-preview {
  margin-bottom: 24px;
}

.camera-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: $text-muted;
  margin-bottom: 8px;
}

.camera-screen {
  position: relative;
  width: 100%;
  height: 160px;
  background: linear-gradient(135deg, #0D1117 0%, #0D1117 100%);
  border-radius: 12px;
  overflow: hidden;
  border: 2px solid #e5e6eb;
}

.scan-line {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 2px;
  background: linear-gradient(90deg, transparent 0%, rgba(54, 211, 153, 0.8) 50%, transparent 100%);
  box-shadow: 0 0 8px rgba(54, 211, 153, 0.6);
  animation: scan-move 2.5s linear infinite;
  will-change: transform, opacity;
  z-index: 2;
}

@keyframes scan-move {
  0% { top: 0; opacity: 0; }
  10% { opacity: 1; }
  90% { opacity: 1; }
  100% { top: 100%; opacity: 0; }
}

.camera-placeholder {
  position: absolute;
  inset: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: $text-muted;
  font-size: 13px;
}

// 脉冲动画
.pulse-animation {
  animation: pulse-btn 2s ease-in-out infinite;
  will-change: transform, opacity;
}

@keyframes pulse-btn {
  0%, 100% {
    box-shadow: 0 4px 14px rgba(22, 93, 255, 0.3);
  }
  50% {
    box-shadow: 0 4px 28px rgba(22, 93, 255, 0.5), 0 0 0 8px rgba(22, 93, 255, 0.1);
  }
}

.pulse-animation-soft {
  animation: pulse-soft 2s ease-in-out infinite;
  will-change: transform, opacity;
}

@keyframes pulse-soft {
  0%, 100% {
    box-shadow: 0 4px 14px rgba(245, 63, 63, 0.3);
  }
  50% {
    box-shadow: 0 4px 24px rgba(245, 63, 63, 0.5);
  }
}

.notice-actions {
  display: flex;
  justify-content: center;
}

.enter-btn {
  padding: 14px 36px;
  font-size: 16px;
  font-weight: 700;
  background: linear-gradient(135deg, $primary-color 0%, #4080FF 100%);
  border: none;
  display: flex;
  align-items: center;
  gap: 8px;

  &:hover {
    background: linear-gradient(135deg, #0E42D2 0%, #165DFF 100%);
  }
}

// ==================== 左侧编辑器 ====================
.room-left {
  flex: 1;
  min-width: 0;
  padding: 12px;
  transition: opacity 0.3s;

  &.is-locked {
    opacity: 0.6;
    pointer-events: none;

    &::after {
      content: '作品已提交，编辑器已锁定';
      position: absolute;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      font-size: 24px;
      font-weight: 700;
      color: $danger-color;
      background: rgba(255, 255, 255, 0.9);
      padding: 20px 40px;
      border-radius: 12px;
      box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
      z-index: 100;
    }
  }
}

// ==================== 右侧面板 ====================
.room-right {
  width: 380px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  background: $bg-panel;
  border-left: 1px solid $border-color;
}

.panel-section {
  padding: 16px;
  border-bottom: 1px solid $border-color;

  &:last-child {
    border-bottom: none;
  }
}

.section-title {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 15px;
  font-weight: 600;
  color: $text-title;
  margin-bottom: 12px;

  .el-icon {
    font-size: 18px;
    color: $primary-color;
  }
}

// ==================== 题目描述 ====================
.problem-content {
  font-size: 14px;
  line-height: 1.7;
  color: $text-body;
  max-height: 200px;
  overflow-y: auto;
  padding: 12px;
  background: #F7F8FA;
  border-radius: 8px;
  border: 1px solid #E5E6EB;

  :deep(img) {
    max-width: 100%;
    border-radius: 4px;
  }

  :deep(pre) {
    background: #1D2129;
    color: #E5E6EB;
    padding: 12px;
    border-radius: 8px;
    overflow-x: auto;
    font-size: 13px;
  }
}

// ==================== 竞赛状态 ====================
.status-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}

.status-card {
  text-align: center;
  padding: 12px 8px;
  background: #F7F8FA;
  border-radius: 8px;
  border: 1px solid #E5E6EB;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.06);
  }

  .status-label {
    font-size: 12px;
    color: $text-muted;
    margin-bottom: 4px;
  }

  .status-value {
    font-size: 16px;
    font-weight: 700;
    color: $text-title;
  }
}

.countdown-card {
  &.is-urgent {
    background: #FFECE8;
    border-color: #FFECE8;

    .status-value {
      color: $danger-color;
      animation: pulse 1s infinite;
      will-change: transform, opacity;
    }
  }
}

.status-card.is-warning {
  background: $warning-bg;
  border-color: $warning-light;

  .status-value {
    color: $warning-color;
  }
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

// ==================== 排行榜 ====================
.live-tag {
  margin-left: auto;
  animation: pulse 1.5s infinite;
  will-change: transform, opacity;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 6px;
  max-height: 240px;
  overflow-y: auto;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 10px;
  border-radius: 8px;
  background: #F7F8FA;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s;

  &:hover {
    background: #F7F8FA;
    transform: translateX(4px);
  }

  &.is-self {
    background: #E8F0FF;
    border: 1px solid rgba(22, 93, 255, 0.2);
  }
}

.rank-badge {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 700;
  background: #E5E6EB;
  color: $text-muted;
  flex-shrink: 0;

  &.rank-gold {
    background: linear-gradient(135deg, #FFB84D, #FF9500);
    color: #fff;
  }

  &.rank-silver {
    background: linear-gradient(135deg, #C9CDD4, #86909C);
    color: #fff;
  }

  &.rank-bronze {
    background: linear-gradient(135deg, #FF7D00, #E06800);
    color: #fff;
  }
}

.rank-avatar {
  flex-shrink: 0;
}

.rank-name {
  flex: 1;
  font-size: 13px;
  font-weight: 500;
  color: $text-title;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.rank-score {
  font-size: 13px;
  font-weight: 700;
  color: $primary-color;
  flex-shrink: 0;
}

.rank-time {
  font-size: 11px;
  color: $text-muted;
  flex-shrink: 0;
}

// ==================== 提交区域 ====================
.submit-section {
  margin-top: auto;
  padding-top: 16px;
}

.submit-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 10px;
}

.submit-tip {
  text-align: center;
  font-size: 12px;
  color: $text-muted;
  margin: 8px 0 0;
}

// ==================== 响应式 ====================
@media (max-width: 1024px) {
  .exam-room-page {
    flex-direction: column;
  }

  .room-left {
    height: 50%;
  }

  .room-right {
    width: 100%;
    height: 50%;
    border-left: none;
    border-top: 1px solid $border-color;
  }

  .notice-card {
    padding: 24px;
    max-height: 90vh;
    overflow-y: auto;
  }
}
</style>
