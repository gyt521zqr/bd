<template>
  <!-- 课程卡片 -->
  <div class="course-card" @click="$router.push(`/course/${course.id}`)">
    <div class="card-cover">
      <!-- 有封面图时显示图片（自动处理OSS路径） -->
      <img v-if="course.cover" :src="coverUrl" :alt="course.title" />
      <!-- 无封面图时显示渐变色占位 -->
      <div v-else :class="['cover-placeholder', `cover-${course.difficulty || 'default'}`]">
        <svg class="cover-placeholder-icon" viewBox="0 0 48 48" fill="none">
          <rect x="8" y="6" width="32" height="28" rx="4" stroke="currentColor" stroke-width="2" opacity="0.6"/>
          <path d="M8 30 L18 20 L26 26 L34 18 L40 24 L40 34 L8 34Z" fill="currentColor" opacity="0.3"/>
          <circle cx="18" cy="16" r="3" fill="currentColor" opacity="0.5"/>
        </svg>
      </div>
      <!-- 语言标签 -->
      <div class="cover-language" v-if="courseLanguage">{{ courseLanguage }}</div>
      <!-- 难度标签 -->
      <div :class="['cover-difficulty', `diff-${course.difficulty}`]" v-if="course.difficulty">
        {{ difficultyLabel }}
      </div>
    </div>
    <div class="card-body">
      <h4 class="card-title">{{ course.title }}</h4>
      <div class="card-meta">
        <div class="teacher-info">
          <span class="teacher-avatar" :style="{ background: avatarColor }">{{ teacherInitial }}</span>
          <span class="teacher-name">{{ course.teacherName }}</span>
        </div>
        <span class="student-count">
          <svg class="icon-student" viewBox="0 0 16 16" fill="currentColor">
            <path d="M8 8a3 3 0 100-6 3 3 0 000 6zm-4 6c0-2.2 1.8-4 4-4s4 1.8 4 4v1H4v-1z"/>
          </svg>
          {{ formatStudentCount }}
        </span>
      </div>
      <div class="card-bottom">
        <div class="card-rating">
          <div class="stars">
            <svg v-for="n in 5" :key="n" class="star-icon" :class="{ filled: n <= Math.round(course.rating) }" viewBox="0 0 16 16">
              <path d="M8 1l2.2 4.5 5 .7-3.6 3.5.8 5L8 12.4 3.6 14.7l.8-5L.8 6.2l5-.7z"/>
            </svg>
          </div>
          <span class="rating-num">{{ course.rating }}</span>
        </div>
        <span :class="['card-price', { free: course.price === 0 }]">
          <template v-if="course.price > 0">
            <span class="price-symbol">¥</span>{{ course.price }}
          </template>
          <template v-else>
            <span class="free-tag">免费</span>
          </template>
        </span>
      </div>
      <button class="learn-btn" @click.stop="$router.push(`/course/${course.id}`)">
        立即学习
        <svg class="btn-arrow" viewBox="0 0 16 16" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M3 8h10M9 4l4 4-4 4"/>
        </svg>
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * CourseCard - 课程卡片组件
 */
import { computed } from 'vue'
import { getOSSUrl } from '@/utils/storage'

const props = defineProps<{
  course: {
    id: number | string
    title: string
    cover: string
    difficulty?: string
    teacherName: string
    studentCount: number
    rating: number
    price: number
    language?: string
  }
}>()

const difficultyLabel = computed(() => {
  const map: Record<string, string> = { beginner: '入门', intermediate: '进阶', advanced: '高级' }
  return map[props.course.difficulty || ''] || ''
})

const courseLanguage = computed(() => {
  const title = props.course.title.toLowerCase()
  if (title.includes('python')) return 'Python'
  if (title.includes('c++') || title.includes('cpp')) return 'C++'
  if (title.includes('scratch')) return 'Scratch'
  if (title.includes('goc')) return 'GoC'
  return props.course.language || ''
})

const teacherInitial = computed(() => {
  return (props.course.teacherName || '').charAt(0)
})

const avatarColor = computed(() => {
  const colors = ['#165DFF', '#FF7D00', '#36D399', '#7B61FF', '#F53F3F', '#FF9500']
  const idx = (props.course.teacherName || '').charCodeAt(0) % colors.length
  return colors[idx]
})

const formatStudentCount = computed(() => {
  const count = props.course.studentCount
  if (count >= 10000) return (count / 10000).toFixed(1) + 'w人'
  if (count >= 1000) return (count / 1000).toFixed(1) + 'k人'
  return count + '人'
})

/** 封面图URL（本地路径直接返回，OSS 路径自动处理） */
const coverUrl = computed(() => {
  if (!props.course.cover) return ''
  // 本地/相对路径（以 / 开头）直接返回，不走 OSS 域名拼接
  if (props.course.cover.startsWith('/')) return props.course.cover
  return getOSSUrl(props.course.cover, {
    width: 400,
    height: 300,
    quality: 85,
  })
})
</script>

<style scoped lang="scss">

.course-card {
  background: $bg-white;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
  }
}

// ==================== 封面区域 ====================
.card-cover {
  position: relative;
  height: 180px;
  overflow: hidden;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.4s ease;
  }

  .course-card:hover & img {
    transform: scale(1.05);
  }
}

// 渐变色占位
.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.7);

  &.cover-beginner {
    background: linear-gradient(135deg, #36D399 0%, #36D399 50%, #7EEAC0 100%);
  }

  &.cover-intermediate {
    background: linear-gradient(135deg, #165DFF 0%, #4080FF 50%, #4080FF 100%);
  }

  &.cover-advanced {
    background: linear-gradient(135deg, #7B61FF 0%, #7B61FF 50%, #7B61FF 100%);
  }

  &.cover-default {
    background: linear-gradient(135deg, #165DFF 0%, #7B61FF 100%);
  }
}

.cover-placeholder-icon {
  width: 48px;
  height: 48px;
  opacity: 0.6;
}

// 语言标签
.cover-language {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 3px 10px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 700;
  color: #fff;
  background: rgba(0, 0, 0, 0.45);
  backdrop-filter: blur(8px);
  letter-spacing: 0.5px;
}

// 难度彩色标签
.cover-difficulty {
  position: absolute;
  top: 12px;
  left: 12px;
  padding: 3px 10px;
  border-radius: 6px;
  font-size: 11px;
  font-weight: 700;
  color: #fff;

  &.diff-beginner {
    background: linear-gradient(135deg, #36D399, #36D399);
    box-shadow: 0 2px 6px rgba(#36D399, 0.3);
  }

  &.diff-intermediate {
    background: linear-gradient(135deg, #165DFF, #4080FF);
    box-shadow: 0 2px 6px rgba($primary-color, 0.3);
  }

  &.diff-advanced {
    background: linear-gradient(135deg, #7B61FF, #7B61FF);
    box-shadow: 0 2px 6px rgba(#7B61FF, 0.3);
  }
}

// ==================== 内容区域 ====================
.card-body {
  padding: 16px 18px 18px;
}

.card-title {
  font-size: 16px;
  font-weight: 700;
  color: #1D2129;
  margin: 0 0 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  line-height: 1.4;
}

.card-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
  font-size: 13px;
  color: #86909C;
}

.teacher-info {
  display: flex;
  align-items: center;
  gap: 6px;
}

.teacher-avatar {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
}

.teacher-name {
  color: #4E5969;
  font-size: 13px;
}

.student-count {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 12px;
  color: #86909C;
}

.icon-student {
  width: 14px;
  height: 14px;
}

.card-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 14px;
}

// 评分星星
.card-rating {
  display: flex;
  align-items: center;
  gap: 4px;
}

.stars {
  display: flex;
  gap: 1px;
}

.star-icon {
  width: 14px;
  height: 14px;
  fill: #E5E6EB;
  transition: fill 0.2s;

  &.filled {
    fill: #FFB84D;
  }
}

.rating-num {
  font-size: 14px;
  font-weight: 700;
  color: #FFB84D;
  margin-left: 2px;
}

// 价格
.card-price {
  font-size: 18px;
  font-weight: 800;
  color: $danger-color;

  .price-symbol {
    font-size: 13px;
    font-weight: 600;
  }

  &.free {
    .free-tag {
      display: inline-flex;
      align-items: center;
      padding: 2px 10px;
      border-radius: 6px;
      font-size: 12px;
      font-weight: 700;
      color: #36D399;
      background: #E8FFF5;
      border: 1px solid rgba(#36D399, 0.2);
    }
  }
}

// 立即学习按钮
.learn-btn {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  padding: 10px 0;
  border-radius: 10px;
  border: none;
  background: linear-gradient(135deg, $primary-color 0%, $primary-light 100%);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s ease;
  box-shadow: 0 2px 8px rgba($primary-color, 0.25);
  letter-spacing: 0.5px;

  &:hover {
    background: linear-gradient(135deg, $primary-light 0%, #4080FF 100%);
    box-shadow: 0 4px 16px rgba($primary-color, 0.35);
    transform: translateY(-1px);
  }

  .btn-arrow {
    width: 14px;
    height: 14px;
    transition: transform 0.3s ease;
  }

  &:hover .btn-arrow {
    transform: translateX(3px);
  }
}
</style>
