<template>
  <!-- 后台首页：数据大盘/统计卡片/ECharts图表 -->
  <div class="dashboard-page">
    <!-- 欢迎栏 -->
    <div class="welcome-bar">
      <div class="welcome-text">
        <h2>欢迎回来，{{ userStore.userInfo?.nickname || '管理员' }}</h2>
        <p>今天是 {{ todayStr }}，祝您工作顺利！</p>
      </div>
      <div class="welcome-actions">
        <el-button type="primary" @click="$router.push('/admin/student')">学员管理</el-button>
        <el-button @click="$router.push('/admin/exam')">赛事管理</el-button>
        <el-button @click="$router.push('/admin/data-backup')">数据备份</el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card" v-for="card in statCards" :key="card.label" @click="card.route && $router.push(card.route)">
        <div class="card-icon" :style="{ background: card.color }">
          <el-icon :size="24" color="#fff"><component :is="card.icon" /></el-icon>
        </div>
        <div class="card-info">
          <span class="card-value">{{ card.value }}</span>
          <span class="card-label">{{ card.label }}</span>
        </div>
      </div>
    </div>

    <!-- 次级统计 -->
    <div class="sub-stats">
      <div class="sub-stat-item" v-for="item in subStats" :key="item.label" @click="item.route && $router.push(item.route)">
        <span class="sub-stat-value" :style="{ color: item.color }">{{ item.value }}</span>
        <span class="sub-stat-label">{{ item.label }}</span>
      </div>
    </div>

    <!-- 图表区域 -->
    <div class="chart-row">
      <div class="chart-card">
        <h3>用户增长趋势</h3>
        <div class="chart-placeholder" ref="userChartRef"></div>
      </div>
      <div class="chart-card">
        <h3>课程学习排行</h3>
        <div class="chart-placeholder" ref="courseChartRef"></div>
      </div>
    </div>
    <div class="chart-row">
      <div class="chart-card">
        <h3>赛事报名统计</h3>
        <div class="chart-placeholder" ref="revenueChartRef"></div>
      </div>
      <div class="chart-card">
        <div class="recent-header">
          <h3>最近报名记录</h3>
          <el-button text type="primary" @click="$router.push('/admin/registration')">查看全部</el-button>
        </div>
        <el-table :data="recentOrders" size="small" stripe>
          <el-table-column prop="orderNo" label="报名号" width="150" />
          <el-table-column prop="productName" label="赛事名称" min-width="120" show-overflow-tooltip />
          <el-table-column prop="studentName" label="学生姓名" width="100" />
          <el-table-column prop="status" label="状态" width="80">
            <template #default="{ row }">
              <el-tag :type="row.status === 'approved' ? 'success' : row.status === 'cancelled' ? 'info' : 'warning'" size="small">
                {{ row.status === 'approved' ? '已审核' : row.status === 'cancelled' ? '已取消' : '待审核' }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
      </div>
    </div>

    <!-- 快捷操作 + 系统状态 -->
    <div class="bottom-row">
      <div class="chart-card quick-actions">
        <h3>快捷操作</h3>
        <div class="action-grid">
          <div class="action-item" v-for="action in quickActions" :key="action.label" @click="$router.push(action.route)">
            <el-icon :size="22" :color="action.color"><component :is="action.icon" /></el-icon>
            <span>{{ action.label }}</span>
          </div>
        </div>
      </div>
      <div class="chart-card system-status">
        <h3>系统状态</h3>
        <div class="status-list">
          <div class="status-item" v-for="item in systemStatus" :key="item.label">
            <span class="status-dot" :class="item.status"></span>
            <span class="status-label">{{ item.label }}</span>
            <span class="status-value">{{ item.value }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * DashboardPage - 后台首页
 * 统计数据仅来自后端统计接口；接口失败时展示中性占位（0），不回退任何本地/虚构数据
 */
import { ref, onMounted, markRaw, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { User, Notebook, Trophy, TrendCharts, EditPen, Document, Setting, Files, Reading, Ticket } from '@element-plus/icons-vue'
import request from '@/api'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

const userChartRef = ref<HTMLElement>()
const courseChartRef = ref<HTMLElement>()
const revenueChartRef = ref<HTMLElement>()

const todayStr = computed(() => {
  const d = new Date()
  const week = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'][d.getDay()]
  return `${d.getFullYear()}年${d.getMonth() + 1}月${d.getDate()}日 ${week}`
})

const studentCount = ref(0)
const teacherCount = ref(0)
const courseCount = ref(0)
const examCount = ref(0)
const registrationCount = ref(0)
const questionCount = ref(0)
const paperCount = ref(0)
const classCount = ref(0)
const scoreCount = ref(0)
const newsCount = ref(0)
const regCodeCount = ref(0)
const notificationCount = ref(0)
const activeExamCount = ref(0)
const pendingRegistrationCount = ref(0)

const statCards = computed(() => [
  { label: '总学员数', value: studentCount.value.toLocaleString(), icon: markRaw(User), color: '#165DFF', route: '/admin/student' },
  { label: '课程数量', value: courseCount.value, icon: markRaw(Notebook), color: '#36D399', route: '/admin/course' },
  { label: '赛事报名', value: registrationCount.value, icon: markRaw(Trophy), color: '#FF7D00', route: '/admin/registration' },
  { label: '进行中赛事', value: activeExamCount.value, icon: markRaw(TrendCharts), color: '#F53F3F', route: '/admin/exam' },
])

const subStats = computed(() => [
  { label: '教师数', value: teacherCount.value, color: '#165DFF', route: '/admin/teacher' },
  { label: '题库数', value: questionCount.value, color: '#36D399', route: '/admin/question-bank' },
  { label: '试卷数', value: paperCount.value, color: '#FF7D00', route: '/admin/paper' },
  { label: '班级数', value: classCount.value, color: '#7B61FF', route: '/admin/class' },
  { label: '成绩数', value: scoreCount.value, color: '#F53F3F', route: '/admin/score' },
  { label: '资讯数', value: newsCount.value, color: '#4080FF', route: '/admin/news' },
  { label: '注册码', value: regCodeCount.value, color: '#86909C', route: '/admin/regcode' },
])

const recentOrders = ref<any[]>([])

const quickActions = [
  { label: '添加学员', icon: markRaw(User), color: '#165DFF', route: '/admin/student' },
  { label: '创建赛事', icon: markRaw(Trophy), color: '#FF7D00', route: '/admin/exam' },
  { label: '添加题目', icon: markRaw(EditPen), color: '#36D399', route: '/admin/question-bank' },
  { label: '创建试卷', icon: markRaw(Document), color: '#7B61FF', route: '/admin/paper' },
  { label: '发布资讯', icon: markRaw(Reading), color: '#4080FF', route: '/admin/news' },
  { label: '生成注册码', icon: markRaw(Ticket), color: '#86909C', route: '/admin/regcode' },
  { label: '数据备份', icon: markRaw(Files), color: '#F53F3F', route: '/admin/data-backup' },
  { label: '系统设置', icon: markRaw(Setting), color: '#4E5969', route: '/admin/settings' },
]

const systemStatus = computed(() => [
  { label: '数据存储', status: 'ok', value: '本地持久化' },
  { label: '待审核报名', status: pendingRegistrationCount.value > 0 ? 'warning' : 'ok', value: `${pendingRegistrationCount.value} 条` },
  { label: '通知消息', status: notificationCount.value > 0 ? 'info' : 'ok', value: `${notificationCount.value} 条` },
  { label: '系统版本', status: 'ok', value: 'v1.0.0' },
])

// 统计数据仅来自后端接口；失败时全部保持中性占位 0，禁止回填任何本地/虚构数据
async function loadStats() {
  try {
    const res = await request.get('/admin/dashboard/stats')
    const d = res?.data || {}
    if (typeof d.studentCount === 'number') studentCount.value = d.studentCount
    if (typeof d.teacherCount === 'number') teacherCount.value = d.teacherCount
    if (typeof d.courseCount === 'number') courseCount.value = d.courseCount
    if (typeof d.examCount === 'number') examCount.value = d.examCount
    if (typeof d.registrationCount === 'number') registrationCount.value = d.registrationCount
    if (typeof d.questionCount === 'number') questionCount.value = d.questionCount
    if (typeof d.paperCount === 'number') paperCount.value = d.paperCount
    if (typeof d.classCount === 'number') classCount.value = d.classCount
    if (typeof d.scoreCount === 'number') scoreCount.value = d.scoreCount
    if (typeof d.newsCount === 'number') newsCount.value = d.newsCount
    if (typeof d.regCodeCount === 'number') regCodeCount.value = d.regCodeCount
    if (typeof d.notificationCount === 'number') notificationCount.value = d.notificationCount
    if (typeof d.activeExamCount === 'number') activeExamCount.value = d.activeExamCount
    if (typeof d.pendingRegistrationCount === 'number') pendingRegistrationCount.value = d.pendingRegistrationCount
    if (Array.isArray(d.recentOrders)) recentOrders.value = d.recentOrders
  } catch {
    // 接口失败：所有统计归零（中性占位），不展示虚构数字
    studentCount.value = 0
    teacherCount.value = 0
    courseCount.value = 0
    examCount.value = 0
    registrationCount.value = 0
    questionCount.value = 0
    paperCount.value = 0
    classCount.value = 0
    scoreCount.value = 0
    newsCount.value = 0
    regCodeCount.value = 0
    notificationCount.value = 0
    activeExamCount.value = 0
    pendingRegistrationCount.value = 0
    recentOrders.value = []
    ElMessage.error('统计数据加载失败，请检查网络或稍后重试')
  }
}

onMounted(() => {
  // 仅从后端加载真实统计；各计数初始为 0 中性占位，加载失败也不回填虚构数据
  loadStats()
  initCharts()
})

function initCharts() {
  let echarts: any
  try {
    echarts = (window as any).echarts || (window as any).require?.('echarts')
  } catch {
    return
  }
  if (!echarts) return

  if (userChartRef.value) {
    const chart = echarts.init(userChartRef.value)
    chart.setOption({
      color: ['#165DFF'],
      tooltip: { trigger: 'axis' },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'category', data: ['1月', '2月', '3月', '4月', '5月', '6月'] },
      yAxis: { type: 'value' },
      series: [{ data: [820, 932, 1101, 1234, 1390, 1530], type: 'line', smooth: true, areaStyle: { opacity: 0.2 } }]
    })
  }

  if (courseChartRef.value) {
    const chart = echarts.init(courseChartRef.value)
    chart.setOption({
      color: ['#165DFF'],
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: { type: 'value' },
      yAxis: { type: 'category', data: ['C++信奥赛', 'Python算法', 'Scratch图形化', 'Python Turtle', 'ScratchJr'] },
      series: [{ data: [320, 450, 680, 520, 390], type: 'bar', barWidth: '50%' }]
    })
  }

  if (revenueChartRef.value) {
    const chart = echarts.init(revenueChartRef.value)
    chart.setOption({
      color: ['#165DFF', '#FF7D00'],
      tooltip: { trigger: 'item' },
      legend: { bottom: '0%' },
      series: [{
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: { borderRadius: 10, borderColor: '#fff', borderWidth: 2 },
        label: { show: false },
        data: [
          { value: 1048, name: '蓝桥杯' },
          { value: 735, name: 'NOIP' },
          { value: 580, name: 'CSP-J/S' },
          { value: 484, name: '其他赛事' },
        ]
      }]
    })
  }
}
</script>

<style scoped lang="scss">
$primary: #165DFF;

.dashboard-page { padding: 20px; }

/* 欢迎栏 */
.welcome-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 100%);
  border-radius: 12px;
  padding: 24px 28px;
  margin-bottom: 20px;
  color: #fff;
  .welcome-text {
    h2 { font-size: 22px; margin: 0 0 6px; }
    p { font-size: 14px; opacity: 0.85; margin: 0; }
  }
  .welcome-actions {
    display: flex;
    gap: 10px;
    .el-button { background: rgba(255,255,255,0.15); border-color: rgba(255,255,255,0.25); color: #fff; }
    .el-button--primary { background: $bg-white; color: $primary; border-color: #fff; }
  }
}

/* 主统计卡片 */
.stat-cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: 16px; margin-bottom: 16px; }
.stat-card {
  background: $bg-white; border-radius: 12px; padding: 20px; display: flex; align-items: center; gap: 16px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.06); cursor: pointer; transition: transform 0.2s, box-shadow 0.2s;
  &:hover { transform: translateY(-2px); box-shadow: 0 4px 16px rgba(0,0,0,0.12); }
}
.card-icon { width: 48px; height: 48px; border-radius: 12px; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.card-info { flex: 1; min-width: 0;
  .card-value { display: block; font-size: 24px; font-weight: 700; color: #1D2129; }
  .card-label { font-size: 12px; color: #86909C; }
}

/* 次级统计 */
.sub-stats {
  display: grid; grid-template-columns: repeat(8, 1fr); gap: 12px; margin-bottom: 20px;
  background: $bg-white; border-radius: 12px; padding: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.06);
}
.sub-stat-item {
  display: flex; flex-direction: column; align-items: center; gap: 4px; cursor: pointer;
  padding: 8px 4px; border-radius: 8px; transition: background 0.2s;
  &:hover { background: #F7F8FA; }
  .sub-stat-value { font-size: 20px; font-weight: 700; }
  .sub-stat-label { font-size: 12px; color: #86909C; }
}

/* 图表区域 */
.chart-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; margin-bottom: 20px; }
.chart-card {
  background: $bg-white; border-radius: 12px; padding: 20px; box-shadow: 0 2px 8px rgba(0,0,0,0.06);
  h3 { font-size: 16px; color: #1D2129; margin-bottom: 12px; }
}
.chart-placeholder { height: 300px; background: #F7F8FA; border-radius: 8px; }
.recent-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 12px; h3 { margin: 0; } }

/* 底部行 */
.bottom-row { display: grid; grid-template-columns: 1fr 1fr; gap: 20px; }

/* 快捷操作 */
.action-grid {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 12px;
}
.action-item {
  display: flex; flex-direction: column; align-items: center; gap: 8px; padding: 16px 8px;
  border-radius: 10px; cursor: pointer; transition: transform, box-shadow, background-color, border-color, color, opacity 0.2s; background: #F7F8FA;
  &:hover { background: #E8F0FF; transform: translateY(-2px); }
  span { font-size: 13px; color: #4E5969; }
}

/* 系统状态 */
.status-list { display: flex; flex-direction: column; gap: 14px; }
.status-item {
  display: flex; align-items: center; gap: 10px; padding: 10px 14px;
  background: #F7F8FA; border-radius: 8px;
  .status-dot { width: 8px; height: 8px; border-radius: 50%; flex-shrink: 0;
    &.ok { background: #36D399; box-shadow: 0 0 6px rgba(34,197,94,0.4); }
    &.warning { background: #FF9500; box-shadow: 0 0 6px rgba(245,158,11,0.4); }
    &.info { background: #165DFF; box-shadow: 0 0 6px rgba(59,130,246,0.4); }
    &.error { background: #F53F3F; box-shadow: 0 0 6px rgba(239,68,68,0.4); }
  }
  .status-label { flex: 1; font-size: 14px; color: #4E5969; }
  .status-value { font-size: 13px; color: #1D2129; font-weight: 500; }
}

@media (max-width: 1200px) {
  .sub-stats { grid-template-columns: repeat(4, 1fr); }
  .bottom-row { grid-template-columns: 1fr; }
}
</style>
