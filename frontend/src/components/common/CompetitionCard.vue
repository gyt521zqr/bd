<template>
  <!-- 赛事卡片组件 -->
  <div class="competition-card" @click="$router.push(`/exam/${id}`)">
    <!-- 封面区域 -->
    <div class="card-cover">
      <img v-if="cover" :src="cover" :alt="title" />
      <div v-else class="cover-placeholder">
        <el-icon :size="40"><Trophy /></el-icon>
      </div>
      <!-- 渐变遮罩 -->
      <div class="cover-overlay"></div>
      <!-- 状态角标 -->
      <span class="status-badge" :class="`status-${status}`">{{ statusLabel }}</span>
      <!-- 编程语言标签 -->
      <span v-if="language" class="language-tag">{{ language }}</span>
    </div>
    <!-- 内容区域 -->
    <div class="card-body">
      <h4 class="card-title">{{ title }}</h4>
      <div class="card-info">
        <div class="info-item">
          <el-icon><Calendar /></el-icon>
          <span>{{ date }}</span>
        </div>
        <div class="info-item">
          <el-icon><Location /></el-icon>
          <span>{{ location }}</span>
        </div>
        <div class="info-item">
          <el-icon><User /></el-icon>
          <span>{{ participants }}人参赛</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * CompetitionCard - 赛事卡片组件
 * 用于展示赛事信息，支持状态角标、封面图、基本信息展示
 * 新增：状态角标样式、渐变遮罩、hover信息上浮
 */
import { computed } from 'vue'
import { Trophy, Calendar, Location, User } from '@element-plus/icons-vue'

const props = defineProps<{
  title: string
  status: 'upcoming' | 'registering' | 'ongoing' | 'ended'
  date: string
  location: string
  participants: number
  language?: string
  cover?: string
  id: number
}>()

/** 状态标签文字映射 */
const statusLabel = computed(() => {
  const map: Record<string, string> = {
    registering: '报名中',
    upcoming: '未开始',
    ongoing: '进行中',
    ended: '已结束'
  }
  return map[props.status] || ''
})
</script>

<style scoped lang="scss">

.competition-card {
  background: $bg-white;
  border-radius: 12px;
  overflow: hidden;
  cursor: pointer;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.35s cubic-bezier(0.25, 0.8, 0.25, 1);

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 16px 40px rgba(0, 0, 0, 0.14);

    .card-body {
      transform: translateY(-4px);
    }

    .cover-overlay {
      opacity: 0.5;
    }

    .card-cover img {
      transform: scale(1.06);
    }
  }
}

.card-cover {
  position: relative;
  height: 160px;
  overflow: hidden;
  background: #F7F8FA;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.5s cubic-bezier(0.25, 0.8, 0.25, 1);
  }
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #E8F0FF, #E8F0FF);
  color: #165DFF;
}

/* 渐变遮罩 */
.cover-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  height: 60%;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.4), transparent);
  opacity: 0.3;
  transition: opacity 0.35s ease;
  pointer-events: none;
}

/* 状态角标 */
.status-badge {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  color: #ffffff;
  line-height: 1.4;
  backdrop-filter: blur(4px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);

  &.status-registering {
    background: linear-gradient(135deg, #FF7D00, #FF9500);
    animation: badgePulse 2s ease-in-out infinite;
    will-change: transform, opacity;
  }

  &.status-upcoming {
    background: linear-gradient(135deg, #165DFF, #4080FF);
  }

  &.status-ongoing {
    background: linear-gradient(135deg, #36D399, #36D399);
    animation: badgePulse 2s ease-in-out infinite;
    will-change: transform, opacity;
  }

  &.status-ended {
    background: linear-gradient(135deg, #86909C, #86909C);
  }
}

@keyframes badgePulse {
  0%, 100% {
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  }
  50% {
    box-shadow: 0 2px 16px rgba(0, 0, 0, 0.25), 0 0 8px rgba(255, 255, 255, 0.2);
  }
}

.language-tag {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 3px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 500;
  color: #165DFF;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(4px);
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.08);
}

.card-body {
  padding: 14px 16px 16px;
  transition: transform 0.35s cubic-bezier(0.25, 0.8, 0.25, 1);
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1D2129;
  margin: 0 0 10px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-info {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: #86909C;
  line-height: 1.4;
  transition: color 0.25s ease;

  .competition-card:hover & {
    color: #86909C;
  }

  .el-icon {
    font-size: 14px;
    flex-shrink: 0;
    transition: color 0.25s ease;
  }

  .competition-card:hover .el-icon {
    color: #165DFF;
  }

  span {
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}
</style>
