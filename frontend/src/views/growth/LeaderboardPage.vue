<template>
  <!-- 排行榜 -->
  <div class="leaderboard-page">
    <!-- 头部区域 -->
    <div class="page-header">
      <div class="header-bg"></div>
      <div class="header-content">
        <h2 class="page-title">
          <el-icon><Trophy /></el-icon>
          编程排行榜
        </h2>
        <p class="page-subtitle">看看谁是最强的小程序员</p>
      </div>
    </div>

    <div class="page-body">
      <template v-if="leaderboard.length">
      <!-- 前三名展示区 -->
      <div class="top-three-section">
        <div class="top-three">
          <!-- 第二名 -->
          <div class="top-card silver" v-if="topThree[1]">
            <div class="medal-badge">
              <div class="medal-ring silver-ring">
                <span class="medal-num">2</span>
              </div>
            </div>
            <div class="top-avatar-wrap">
              <el-avatar :size="72" :src="topThree[1].avatar">{{ topThree[1].nickname?.charAt(0) }}</el-avatar>
              <div class="rank-glow silver-glow"></div>
            </div>
            <div class="top-info">
              <span class="top-name">{{ topThree[1].nickname }}</span>
              <span class="top-score">{{ topThree[1].score }}分</span>
            </div>
          </div>

          <!-- 第一名 -->
          <div class="top-card gold first-place" v-if="topThree[0]">
            <div class="crown-icon">
              <el-icon :size="24"><Trophy /></el-icon>
            </div>
            <div class="medal-badge">
              <div class="medal-ring gold-ring">
                <span class="medal-num">1</span>
              </div>
            </div>
            <div class="top-avatar-wrap">
              <el-avatar :size="88" :src="topThree[0].avatar">{{ topThree[0].nickname?.charAt(0) }}</el-avatar>
              <div class="rank-glow gold-glow"></div>
            </div>
            <div class="top-info">
              <span class="top-name">{{ topThree[0].nickname }}</span>
              <span class="top-score">{{ topThree[0].score }}分</span>
            </div>
          </div>

          <!-- 第三名 -->
          <div class="top-card bronze" v-if="topThree[2]">
            <div class="medal-badge">
              <div class="medal-ring bronze-ring">
                <span class="medal-num">3</span>
              </div>
            </div>
            <div class="top-avatar-wrap">
              <el-avatar :size="72" :src="topThree[2].avatar">{{ topThree[2].nickname?.charAt(0) }}</el-avatar>
              <div class="rank-glow bronze-glow"></div>
            </div>
            <div class="top-info">
              <span class="top-name">{{ topThree[2].nickname }}</span>
              <span class="top-score">{{ topThree[2].score }}分</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 排名列表 -->
      <div class="rank-list-section">
        <div class="rank-header">
          <h3>
            <el-icon><List /></el-icon>
            完整排名
          </h3>
          <el-radio-group v-model="timeRange" size="small" class="time-filter">
            <el-radio-button label="week">本周</el-radio-button>
            <el-radio-button label="month">本月</el-radio-button>
            <el-radio-button label="all">总榜</el-radio-button>
          </el-radio-group>
        </div>
        <div class="rank-list">
          <div
            v-for="(item, index) in otherRanks"
            :key="item.userId"
            class="rank-item"
            :class="{ 'alt-row': index % 2 === 1 }"
            :style="{ animationDelay: `${index * 0.05}s` }"
          >
            <div class="rank-num">
              <span class="rank-num-text">{{ index + 4 }}</span>
            </div>
            <el-avatar :size="40" :src="item.avatar">{{ item.nickname?.charAt(0) }}</el-avatar>
            <div class="rank-info">
              <span class="rank-name">{{ item.nickname }}</span>
              <span class="rank-level">Lv.{{ item.level }}</span>
            </div>
            <div class="rank-stats">
              <div class="stat">
                <span class="stat-label">积分</span>
                <span class="stat-value">{{ item.score }}</span>
              </div>
              <div class="stat">
                <span class="stat-label">作品</span>
                <span class="stat-value">{{ item.worksCount }}</span>
              </div>
              <div class="stat">
                <span class="stat-label">连续</span>
                <span class="stat-value">{{ item.streak }}天</span>
              </div>
            </div>
          </div>
        </div>
      </div>
      </template>
      <div class="leaderboard-empty" v-else>
        <el-empty description="暂无排行数据" :image-size="80" />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * LeaderboardPage - 排行榜
 * 积分排行展示，数据来自真实API，无数据时显示空态
 */
import { ref, computed, onMounted, watch } from 'vue'
import { Trophy, List } from '@element-plus/icons-vue'
import { getLeaderboard } from '@/api/growth'

const timeRange = ref('week')
const loading = ref(false)

interface LeaderboardItem {
  userId: number
  nickname: string
  avatar: string
  score: number
  level: number
  worksCount: number
  streak: number
}

// 数据来自真实API，接口无数据时保持空态
const leaderboard = ref<LeaderboardItem[]>([])

const topThree = computed(() => leaderboard.value.slice(0, 3))
const otherRanks = computed(() => leaderboard.value.slice(3))

/** 加载排行榜数据 */
async function fetchLeaderboard() {
  loading.value = true
  try {
    const res = await getLeaderboard({
      type: 'points',
      period: timeRange.value as 'week' | 'month' | 'all',
    })
    if (res.data && Array.isArray(res.data) && res.data.length > 0) {
      leaderboard.value = res.data.map((item: any) => ({
        userId: item.userId,
        nickname: item.nickname,
        avatar: item.avatar,
        score: item.points,
        level: item.level,
        worksCount: item.submitCount || 0,
        streak: 0,
      }))
    }
  } catch {
    // 接口无数据或失败：保持空态，由模板显示「暂无数据」
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchLeaderboard()
})

// 切换榜单类型（本周/本月/总榜）时重新调用API
watch(timeRange, () => {
  fetchLeaderboard()
})
</script>

<style scoped lang="scss">
.leaderboard-page {
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
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 40%, #FF7D00 100%);

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
  max-width: 800px;
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

// ==================== 前三名 ====================
.page-body {
  max-width: 800px;
  margin: 0 auto;
  padding: 0 20px;
}

// 无数据时的空态
.leaderboard-empty {
  position: relative;
  z-index: 2;
  margin-top: -48px;
  padding: 60px 0;
  background: $bg-white;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.top-three-section {
  margin-top: -48px;
  position: relative;
  z-index: 2;
  margin-bottom: 32px;
}

.top-three {
  display: flex;
  justify-content: center;
  align-items: flex-end;
  gap: 16px;
}

.top-card {
  background: $bg-white;
  border-radius: 20px;
  padding: 20px 16px 16px;
  text-align: center;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s ease;
  position: relative;
  width: 180px;

  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
  }

  &.first-place {
    width: 200px;
    padding-top: 28px;
    z-index: 1;

    .top-avatar-wrap {
      margin-bottom: 12px;
    }
  }

  &.silver {
    order: 1;
    border-top: 4px solid #C0C0C0;
  }

  &.gold {
    order: 2;
    border-top: 4px solid #FFD700;
  }

  &.bronze {
    order: 3;
    border-top: 4px solid #CD7F32;
  }
}

.crown-icon {
  position: absolute;
  top: -16px;
  left: 50%;
  transform: translateX(-50%);
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: linear-gradient(135deg, #FFD700, #FFA500);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  box-shadow: 0 4px 12px rgba(255, 215, 0, 0.4);
  animation: crownFloat 2s ease-in-out infinite;
  will-change: transform, opacity;
}

@keyframes crownFloat {
  0%, 100% { transform: translateX(-50%) translateY(0); }
  50% { transform: translateX(-50%) translateY(-4px); }
}

.medal-badge {
  position: absolute;
  top: 12px;
  right: 12px;
}

.medal-ring {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 800;
  font-size: 14px;
  color: #fff;

  &.gold-ring {
    background: linear-gradient(135deg, #FFD700, #FFA500);
    box-shadow: 0 2px 8px rgba(255, 215, 0, 0.4);
  }

  &.silver-ring {
    background: linear-gradient(135deg, #C0C0C0, #999999);
    box-shadow: 0 2px 8px rgba(192, 192, 192, 0.4);
  }

  &.bronze-ring {
    background: linear-gradient(135deg, #CD7F32, #CD7F32);
    box-shadow: 0 2px 8px rgba(205, 127, 50, 0.4);
  }
}

.top-avatar-wrap {
  position: relative;
  display: inline-block;
  margin-bottom: 10px;

  :deep(.el-avatar) {
    border: 3px solid #fff;
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
}

.rank-glow {
  position: absolute;
  inset: -6px;
  border-radius: 50%;
  z-index: -1;
  opacity: 0.3;

  &.gold-glow {
    background: radial-gradient(circle, rgba(255, 215, 0, 0.5) 0%, transparent 70%);
  }

  &.silver-glow {
    background: radial-gradient(circle, rgba(192, 192, 192, 0.5) 0%, transparent 70%);
  }

  &.bronze-glow {
    background: radial-gradient(circle, rgba(205, 127, 50, 0.5) 0%, transparent 70%);
  }
}

.top-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.top-name {
  font-size: 15px;
  font-weight: 700;
  color: #1D2129;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.top-score {
  font-size: 14px;
  font-weight: 600;
  color: #FF7D00;
}

// ==================== 排名列表 ====================
.rank-list-section {
  background: $bg-white;
  border-radius: 16px;
  padding: 20px 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.rank-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #F7F8FA;

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
}

.time-filter {
  :deep(.el-radio-button__inner) {
    border-radius: 8px;
    font-size: 13px;
  }
}

.rank-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.rank-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 16px;
  border-radius: 10px;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.25s ease;
  animation: rankSlideIn 0.4s ease forwards;
  opacity: 0;
  transform: translateX(-10px);

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

@keyframes rankSlideIn {
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.rank-num {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: #F7F8FA;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  .rank-num-text {
    font-size: 14px;
    font-weight: 700;
    color: #86909C;
  }
}

.rank-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;

  .rank-name {
    font-size: 14px;
    font-weight: 600;
    color: #4E5969;
  }

  .rank-level {
    font-size: 12px;
    color: #86909C;
  }
}

.rank-stats {
  display: flex;
  gap: 20px;
  flex-shrink: 0;
}

.stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 2px;

  .stat-label {
    font-size: 11px;
    color: #86909C;
  }

  .stat-value {
    font-size: 14px;
    font-weight: 700;
    color: #1D2129;
  }
}

// ==================== 响应式 ====================
@media (max-width: 768px) {
  .top-three {
    gap: 10px;
  }

  .top-card {
    width: 140px;
    padding: 16px 12px 12px;

    &.first-place {
      width: 160px;
    }
  }

  .top-name {
    font-size: 13px;
  }

  .rank-stats {
    gap: 12px;
  }

  .stat {
    .stat-value {
      font-size: 12px;
    }
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
  }

  .top-three {
    flex-direction: column;
    align-items: center;
    gap: 12px;
  }

  .top-card {
    width: 100%;
    max-width: 280px;
    display: flex;
    align-items: center;
    text-align: left;
    padding: 14px 16px;
    gap: 14px;

    &.first-place {
      width: 100%;
      max-width: 280px;
      order: 1;
    }

    &.silver {
      order: 2;
    }

    &.bronze {
      order: 3;
    }
  }

  .crown-icon {
    display: none;
  }

  .medal-badge {
    position: static;
    order: -1;
  }

  .top-avatar-wrap {
    margin-bottom: 0;
  }

  .rank-glow {
    display: none;
  }

  .rank-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }

  .rank-item {
    padding: 10px 12px;
  }

  .rank-stats {
    display: none;
  }
}
</style>
