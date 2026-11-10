<template>
  <!-- 考试专用布局：全屏、无干扰、最小化UI -->
  <div class="exam-layout">
    <!-- 顶部考试信息栏 -->
    <header class="exam-header">
      <div class="exam-info">
        <span class="exam-title">{{ examStore.currentExam?.title || '在线考试' }}</span>
        <el-tag type="danger" size="small" v-if="examStore.switchCount > 0">
          切屏 {{ examStore.switchCount }}/{{ examStore.maxSwitchCount }} 次
        </el-tag>
      </div>
      <div class="exam-timer">
        <ExamTimer :duration="Math.ceil(examStore.remainingTime / 60)" />
      </div>
      <div class="exam-actions">
        <el-button type="danger" @click="handleSubmitExam" :loading="submitting">
          交卷
        </el-button>
      </div>
    </header>

    <!-- 防作弊监控组件 -->
    <AntiCheatMonitor :exam-id="examId" />

    <!-- 考试内容区 -->
    <main class="exam-content">
      <router-view />
    </main>
  </div>
</template>

<script setup lang="ts">
/**
 * ExamLayout - 考试专用布局
 * 全屏无干扰模式，最小化UI，包含倒计时和防作弊监控
 */
import { ref, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useExamStore } from '@/stores/exam'
import { reportSwitchScreen } from '@/api/exam'
import { useExamSubmit } from '@/hooks/useExamSubmit'
import { ElMessageBox } from 'element-plus'
import ExamTimer from '@/components/exam/ExamTimer.vue'
import AntiCheatMonitor from '@/components/exam/AntiCheatMonitor.vue'

const route = useRoute()
const router = useRouter()
const examStore = useExamStore()
const { submit } = useExamSubmit()

const examId = Number(route.params.examId)
const submitting = ref(false)
let timer: ReturnType<typeof setInterval> | null = null
/** 进入考试时的初始剩余时间（用于计算已用时间，避免硬编码 60 分钟） */
const initialTime = ref(0)

/** 交卷 */
async function handleSubmitExam() {
  submitting.value = true
  const ok = await submit(examId)
  submitting.value = false

  if (ok) {
    ElMessageBox.alert(
      `交卷成功！\n\n已答题目：${examStore.answeredCount} 题\n用时：${formatUsedTime()}`,
      '交卷结果',
      { confirmButtonText: '查看答案解析', type: 'success' }
    ).then(() => {
      router.push(`/exam/result/${examId}`)
    }).catch(() => {})
  }
}

/** 格式化已用时间（基于进入时的初始时长动态计算） */
function formatUsedTime() {
  const used = Math.max(0, initialTime.value - examStore.remainingTime)
  const m = Math.floor(used / 60)
  const s = used % 60
  return `${m}分${s}秒`
}

/** 启动倒计时 */
function startTimer() {
  timer = setInterval(() => {
    examStore.tick()
    if (examStore.isTimeout) {
      // 超时自动交卷
      handleSubmitExam()
    }
  }, 1000)
}

/** 监听切屏事件 */
function handleVisibilityChange() {
  if (document.hidden && examStore.started) {
    examStore.recordSwitch()
    reportSwitchScreen(examId)
    if (examStore.isSwitchExceeded) {
      ElMessageBox.alert('切屏次数超过限制，系统将自动交卷', '警告', {
        type: 'error',
      }).then(() => {
        handleSubmitExam()
      })
    }
  }
}

onMounted(() => {
  // 子组件 ExamTakingPage 已挂载并完成倒计时初始化，此处读取初始剩余时间
  initialTime.value = examStore.remainingTime
  startTimer()
  document.addEventListener('visibilitychange', handleVisibilityChange)
})

onUnmounted(() => {
  if (timer) clearInterval(timer)
  document.removeEventListener('visibilitychange', handleVisibilityChange)
  examStore.resetExam()
})
</script>

<style scoped lang="scss">
.exam-layout {
  width: 100vw;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #F7F8FA;
  overflow: hidden;
}

.exam-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 56px;
  padding: 0 24px;
  background: $bg-white;
  border-bottom: 2px solid #7B61FF;
  flex-shrink: 0;
}

.exam-info {
  display: flex;
  align-items: center;
  gap: 12px;

  .exam-title {
    font-size: 18px;
    font-weight: 600;
    color: #1D2129;
  }
}

.exam-timer {
  display: flex;
  align-items: center;
}

.exam-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.exam-content {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}
</style>
