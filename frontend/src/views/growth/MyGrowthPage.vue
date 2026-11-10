<template>
  <!-- 我的成长 -->
  <div class="my-growth-page">
    <!-- 头部区域 -->
    <div class="page-header">
      <div class="header-bg"></div>
      <div class="header-content">
        <h2 class="page-title">
          <el-icon><TrendCharts /></el-icon>
          我的成长
        </h2>
        <p class="page-subtitle">记录每一步进步，见证成长的力量</p>
      </div>
    </div>

    <div class="page-body">
      <!-- 等级进度卡片 -->
      <div class="level-card">
        <div class="level-header">
          <div class="level-badge">
            <span class="level-num">Lv.{{ userLevel }}</span>
            <span class="level-name">{{ levelName }}</span>
          </div>
          <div class="level-next">
            <span class="next-label">下一等级</span>
            <span class="next-name">{{ nextLevelName }}</span>
          </div>
        </div>
        <div class="level-progress-wrap">
          <div class="level-progress-track">
            <div class="level-progress-fill" :style="{ width: `${levelProgress}%` }">
              <div class="progress-shimmer"></div>
            </div>
          </div>
          <div class="level-progress-labels">
            <span class="progress-current">{{ levelProgress }}%</span>
            <span class="progress-target">{{ levelCurrentExp }}/{{ levelTargetExp }} 经验</span>
          </div>
        </div>
        <div class="level-stats">
          <div class="level-stat">
            <div class="stat-icon">
              <el-icon><StarFilled /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ totalScore }}</span>
              <span class="stat-label">总积分</span>
            </div>
          </div>
          <div class="level-stat">
            <div class="stat-icon works">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ worksCount }}</span>
              <span class="stat-label">作品数</span>
            </div>
          </div>
          <div class="level-stat">
            <div class="stat-icon streak">
              <el-icon><Calendar /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ streakDays }}</span>
              <span class="stat-label">连续打卡</span>
            </div>
          </div>
          <div class="level-stat">
            <div class="stat-icon hours">
              <el-icon><Timer /></el-icon>
            </div>
            <div class="stat-info">
              <span class="stat-value">{{ studyHours }}</span>
              <span class="stat-label">学习时长(h)</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 勋章墙 -->
      <div class="medal-section">
        <div class="section-header">
          <h3>
            <el-icon><Medal /></el-icon>
            我的勋章
          </h3>
          <span class="medal-count">已获得 {{ earnedMedals.length }}/{{ medals.length }} 枚</span>
        </div>
        <MedalShowcase v-if="medals.length > 0" :medals="medals" />
        <el-empty v-else description="暂无勋章" :image-size="70" class="medal-empty" />
      </div>

      <!-- 成长记录 -->
      <div class="record-section">
        <div class="section-header">
          <h3>
            <el-icon><Clock /></el-icon>
            成长记录
          </h3>
        </div>
        <div class="record-list" v-if="records.length > 0">
          <div
            v-for="(record, index) in records"
            :key="index"
            class="record-item"
            :class="{ 'alt-row': index % 2 === 1 }"
            :style="{ animationDelay: `${index * 0.08}s` }"
          >
            <div class="record-dot" :class="record.type"></div>
            <div class="record-content">
              <span class="record-title">{{ record.title }}</span>
              <span class="record-desc">{{ record.description }}</span>
            </div>
            <div class="record-right">
              <span class="record-score" v-if="record.score > 0">+{{ record.score }}</span>
              <span class="record-time">{{ record.time }}</span>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无成长记录" :image-size="70" class="record-empty" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * MyGrowthPage - 我的成长
 * 等级进度、勋章墙、成长记录
 * 勋章与成长记录来自真实 API，无数据时由模板显示空态
 */
import { ref, computed, onMounted } from 'vue'
import { TrendCharts, StarFilled, Document, Calendar, Timer, Medal, Clock } from '@element-plus/icons-vue'
import MedalShowcase from '@/components/growth/MedalShowcase.vue'
import { getPointInfo, getLevelInfo, getMedalList, getPointRecords } from '@/api/growth'

const userLevel = ref(5)
const levelName = ref('编程小达人')
const nextLevelName = ref('编程大师')
const levelCurrentExp = ref(450)
const levelTargetExp = ref(1000)
const totalScore = ref(2180)
const worksCount = ref(25)
const streakDays = ref(12)
const studyHours = ref(156)

const levelProgress = computed(() => {
  return Math.round((levelCurrentExp.value / levelTargetExp.value) * 100)
})

/** 勋章列表：不再内置虚构勋章，接口无数据时由模板显示空态 */
interface MedalItem {
  id: number
  name: string
  icon: string
  description: string
  earned: boolean
  earnedTime: string | null
}
const medals = ref<MedalItem[]>([])

const earnedMedals = computed(() => medals.value.filter(m => m.earned))

/** 成长记录：不再内置虚构记录，接口无数据时由模板显示空态 */
interface GrowthRecord {
  title: string
  description: string
  score: number
  time: string
  type: string
}
const records = ref<GrowthRecord[]>([])

/** 将API返回的记录类型映射为前端展示类型 */
function mapRecordType(type: string): string {
  const t = (type || '').toLowerCase()
  if (t.includes('course') || t.includes('课程')) return 'course'
  if (t.includes('work') || t.includes('作品')) return 'work'
  if (t.includes('streak') || t.includes('打卡') || t.includes('连续')) return 'streak'
  if (t.includes('challenge') || t.includes('挑战')) return 'challenge'
  if (t.includes('like') || t.includes('赞')) return 'like'
  return 'course'
}

/** 加载成长数据 */
async function fetchGrowthData() {
  try {
    const [pointRes, levelRes, medalsRes, recordsRes] = await Promise.all([
      getPointInfo(),
      getLevelInfo(),
      getMedalList(),
      getPointRecords({ pageNum: 1, pageSize: 20 }),
    ])

    // 积分信息
    if (pointRes.data) {
      totalScore.value = pointRes.data.total ?? totalScore.value
    }

    // 等级信息
    if (levelRes.data) {
      userLevel.value = levelRes.data.level || userLevel.value
      levelName.value = levelRes.data.name || levelName.value
      levelCurrentExp.value = levelRes.data.currentPoints || levelCurrentExp.value
      levelTargetExp.value = levelRes.data.maxPoints || levelTargetExp.value
    }

    // 勋章列表
    if (medalsRes.data && medalsRes.data.length > 0) {
      medals.value = medalsRes.data.map((m: any) => ({
        id: m.id,
        name: m.name,
        icon: m.icon,
        description: m.description,
        earned: m.obtained,
        earnedTime: m.obtainTime || null,
      }))
    }

    // 积分记录（兼容数组分页两种结构）
    const recordsData: any = recordsRes.data
    const recordsList = Array.isArray(recordsData) ? recordsData : (recordsData?.list || [])
    if (recordsList.length > 0) {
      records.value = recordsList.map((r: any) => ({
        title: r.description || '获得积分',
        description: r.type || '',
        score: r.points || 0,
        time: r.createTime || '',
        type: mapRecordType(r.type),
      }))
    }
  } catch {
    // 接口无数据或失败：保持空态，由模板显示「暂无数据」
  }
}

onMounted(() => {
  fetchGrowthData()
})
</script>

<style scoped lang="scss">
.my-growth-page {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 40px;
}

// ==================== 头部 ====================
.page-header {
  position: relative;
  padding: 32px 0 80px;
  overflow: hidden;
}

.header-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 50%, #36D399 100%);

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 80px;
    background: linear-gradient(to top, #f7f8fa, transparent);
  }
}

.header-content {
  position: relative;
  z-index: 1;
  max-width: 900px;
  margin: 0 auto;
  padding: 0 20px;
  text-align: center;
}

.page-title {
  font-size: 28px;
  font-weight: 800;
  color: #fff;
  margin: 0 0 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;

  .el-icon {
    font-size: 32px;
  }
}

.page-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
}

// ==================== 内容区域 ====================
.page-body {
  max-width: 900px;
  margin: -48px auto 0;
  padding: 0 20px;
  position: relative;
  z-index: 2;
  display: flex;
  flex-direction: column;
  gap: 20px;
}

// ==================== 等级进度卡片 ====================
.level-card {
  background: $bg-white;
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
}

.level-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.level-badge {
  display: flex;
  align-items: center;
  gap: 10px;

  .level-num {
    font-size: 28px;
    font-weight: 800;
    background: linear-gradient(135deg, #165DFF, #4080FF);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  .level-name {
    font-size: 16px;
    font-weight: 700;
    color: #1D2129;
  }
}

.level-next {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;

  .next-label {
    font-size: 12px;
    color: #86909C;
  }

  .next-name {
    font-size: 14px;
    font-weight: 600;
    color: #FF7D00;
  }
}

.level-progress-wrap {
  margin-bottom: 24px;
}

.level-progress-track {
  width: 100%;
  height: 14px;
  background: #E5E6EB;
  border-radius: 7px;
  overflow: hidden;
  position: relative;
}

.level-progress-fill {
  height: 100%;
  background: linear-gradient(90deg, #165DFF, #4080FF, #36D399);
  border-radius: 7px;
  transition: width 0.8s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;

  .progress-shimmer {
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
    animation: shimmer 2s ease-in-out infinite;
    will-change: transform, opacity;
  }
}

@keyframes shimmer {
  0% { left: -100%; }
  100% { left: 100%; }
}

.level-progress-labels {
  display: flex;
  justify-content: space-between;
  margin-top: 8px;

  .progress-current {
    font-size: 14px;
    font-weight: 700;
    color: #165DFF;
  }

  .progress-target {
    font-size: 13px;
    color: #86909C;
  }
}

.level-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.level-stat {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px;
  background: linear-gradient(135deg, rgba(22, 93, 255, 0.03), rgba(64, 128, 255, 0.05));
  border-radius: 12px;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.25s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(22, 93, 255, 0.08);
  }

  .stat-icon {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    background: linear-gradient(135deg, #165DFF, #4080FF);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 18px;
    flex-shrink: 0;

    &.works {
      background: linear-gradient(135deg, #FF7D00, #FF9500);
    }

    &.streak {
      background: linear-gradient(135deg, #36D399, #36D399);
    }

    &.hours {
      background: linear-gradient(135deg, #7B61FF, #7B61FF);
    }
  }

  .stat-info {
    display: flex;
    flex-direction: column;
    gap: 2px;

    .stat-value {
      font-size: 20px;
      font-weight: 800;
      color: #1D2129;
    }

    .stat-label {
      font-size: 12px;
      color: #86909C;
    }
  }
}

// ==================== 勋章区域 ====================
.medal-section {
  background: $bg-white;
  border-radius: 16px;
  padding: 20px 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  h3 {
    font-size: 16px;
    font-weight: 700;
    color: #1D2129;
    margin: 0;
    display: flex;
    align-items: center;
    gap: 8px;

    .el-icon {
      color: #165DFF;
    }
  }

  .medal-count {
    font-size: 13px;
    color: #86909C;
    font-weight: 500;
  }
}

// ==================== 成长记录 ====================
.record-section {
  background: $bg-white;
  border-radius: 16px;
  padding: 20px 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.record-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.record-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px 16px;
  border-radius: 10px;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.25s ease;
  animation: recordSlideIn 0.4s ease forwards;
  opacity: 0;
  transform: translateX(-8px);

  &:hover {
    background: #F7F8FA;
    transform: translateX(4px);
  }

  &.alt-row {
    background: #F7F8FA;

    &:hover {
      background: #F7F8FA;
    }
  }
}

@keyframes recordSlideIn {
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.record-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  flex-shrink: 0;

  &.course { background: #165DFF; }
  &.work { background: #FF7D00; }
  &.streak { background: #36D399; }
  &.challenge { background: #FF9500; }
  &.like { background: #F53F3F; }
}

.record-content {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;

  .record-title {
    font-size: 14px;
    font-weight: 600;
    color: #4E5969;
  }

  .record-desc {
    font-size: 12px;
    color: #86909C;
  }
}

.record-right {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  flex-shrink: 0;

  .record-score {
    font-size: 14px;
    font-weight: 700;
    color: #36D399;
  }

  .record-time {
    font-size: 12px;
    color: #C9CDD4;
  }
}

// ==================== 空态 ====================
.medal-empty,
.record-empty {
  padding: 16px 0;
}

// ==================== 响应式 ====================
@media (max-width: 768px) {
  .level-stats {
    grid-template-columns: repeat(2, 1fr);
  }

  .level-header {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }

  .level-next {
    align-items: flex-start;
  }
}

@media (max-width: 576px) {
  .page-header {
    padding: 20px 0 60px;
  }

  .page-title {
    font-size: 22px;

    .el-icon {
      font-size: 26px;
    }
  }

  .page-body {
    padding: 0 16px;
    margin-top: -36px;
  }

  .level-card {
    padding: 20px;
  }

  .level-badge .level-num {
    font-size: 22px;
  }

  .level-stats {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }

  .level-stat {
    padding: 10px;
    gap: 8px;

    .stat-icon {
      width: 32px;
      height: 32px;
      font-size: 14px;
    }

    .stat-info .stat-value {
      font-size: 16px;
    }
  }

  .record-item {
    padding: 10px 12px;
  }
}
</style>
