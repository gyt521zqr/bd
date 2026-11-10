<template>
  <!-- 课程卡片组件（新版） -->
  <div class="course-card-new">
    <!-- 封面区域 -->
    <div class="card-cover" @click="$router.push(`/course/${id}`)">
      <img v-if="cover" :src="cover" :alt="title" />
      <div v-else class="cover-placeholder">
        <el-icon :size="40"><Reading /></el-icon>
      </div>
      <!-- 适合年龄标签 -->
      <span class="age-tag">{{ ageRange }}</span>
    </div>
    <!-- 内容区域 -->
    <div class="card-body">
      <h4 class="card-title" @click="$router.push(`/course/${id}`)">{{ title }}</h4>
      <!-- 课时数 -->
      <div class="card-meta">
        <div class="meta-item">
          <el-icon><Clock /></el-icon>
          <span>{{ lessonCount }}课时</span>
        </div>
        <div class="meta-item">
          <el-icon><UserFilled /></el-icon>
          <span>{{ studentCount }}人学习</span>
        </div>
      </div>
      <!-- 授课优势 -->
      <div class="advantage-list">
        <el-tag v-for="(adv, idx) in advantages.slice(0, 2)" :key="idx" size="small" type="info" effect="plain">
          {{ adv }}
        </el-tag>
      </div>
      <!-- 底部：试听预约按钮 -->
      <div class="card-bottom">
        <span class="age-label">{{ ageRange }}</span>
        <el-button
          type="warning"
          size="small"
          round
          class="trial-btn"
          @click.stop="$emit('trial', id)"
        >
          试听预约
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * CourseCardNew - 课程卡片组件（新版）
 * 支持适合年龄标签、课时数、授课优势、试听预约按钮
 */
import { Reading, Clock, UserFilled } from '@element-plus/icons-vue'

defineProps<{
  id: number
  title: string
  cover?: string
  ageRange: string
  lessonCount: number
  studentCount: number
  advantages: string[]
}>()

defineEmits<{
  trial: [id: number]
}>()
</script>

<style scoped lang="scss">
.course-card-new {
  background: $bg-white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.2s ease;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }
}

.card-cover {
  position: relative;
  height: 160px;
  overflow: hidden;
  background: #F7F8FA;
  cursor: pointer;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    transition: transform 0.3s ease;
  }

  &:hover img {
    transform: scale(1.05);
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

.age-tag {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 3px 10px;
  border-radius: 6px;
  font-size: 12px;
  font-weight: 600;
  color: #ffffff;
  background: #165DFF;
  line-height: 1.4;
}

.card-body {
  padding: 14px 16px 16px;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: #1D2129;
  margin: 0 0 10px;
  line-height: 1.4;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  cursor: pointer;

  &:hover {
    color: #165DFF;
  }
}

.card-meta {
  display: flex;
  gap: 14px;
  margin-bottom: 10px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #86909C;
  line-height: 1.4;

  .el-icon {
    font-size: 14px;
  }
}

.advantage-list {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 12px;

  .el-tag {
    font-size: 11px;
    color: #86909C;
    background: #F7F8FA;
    border-color: #E5E6EB;
  }
}

.card-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid #F7F8FA;
}

.age-label {
  font-size: 12px;
  color: #165DFF;
  font-weight: 600;
  background: rgba(22, 93, 255, 0.08);
  padding: 2px 8px;
  border-radius: 4px;
}

.trial-btn {
  background: #FF7D00;
  border-color: #FF7D00;
  color: #fff;
  font-weight: 600;
  font-size: 12px;

  &:hover {
    background: #E06800;
    border-color: #E06800;
  }
}

/* ====== 响应式：平板（768px - 1024px） ====== */
@include tablet {
  .course-card-new {
    .card-body {
      padding: 12px 14px 14px;
    }
  }
}

/* ====== 响应式：移动端（<768px） ====== */
@include mobile {
  .card-cover {
    height: 140px;
  }

  .card-meta {
    flex-wrap: wrap;
  }

  .card-bottom {
    padding-top: 8px;
  }

  .card-title {
    @include text-ellipsis-lines(2);
    white-space: normal;
    -webkit-line-clamp: 2;
  }
}
</style>
