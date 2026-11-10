<template>
  <!-- 视频列表（扁平，无章节层级） -->
  <div class="video-list">
    <div
      v-for="(video, index) in videos"
      :key="video.id"
      class="video-item"
      :class="{ active: activeId === video.id }"
      @click="$emit('select', video.id)"
    >
      <div class="video-index">{{ index + 1 }}</div>
      <div class="video-icon">
        <el-icon><VideoPlay /></el-icon>
      </div>
      <span class="video-title">{{ video.title }}</span>
    </div>
    <div v-if="!videos || videos.length === 0" class="empty-tip">暂无视频</div>
  </div>
</template>

<script setup lang="ts">
/**
 * VideoList - 视频列表组件（扁平结构）
 * 课程目录直接展示视频列表，无章节分组，点击选择视频。
 */
import { VideoPlay } from '@element-plus/icons-vue'

withDefaults(defineProps<{
  videos: { id: number; title: string }[]
  activeId?: number
}>(), {
  activeId: 0,
})

defineEmits<{ select: [id: number] }>()
</script>

<style scoped lang="scss">
.video-list {
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  flex: 1;
  padding: 4px;
  gap: 4px;
}

.video-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 11px 12px;
  cursor: pointer;
  border-radius: 10px;
  font-size: 13px;
  color: #4E5969;
  transition: background-color 0.2s ease, color 0.2s ease;

  &:hover {
    background: #F7F8FA;
  }

  &.active {
    background: linear-gradient(135deg, rgba(22, 93, 255, 0.08), rgba(64, 128, 255, 0.04));
    color: #165DFF;

    .video-icon {
      background: linear-gradient(135deg, #165DFF, #4080FF);
      color: #fff;
    }
  }
}

.video-index {
  width: 20px;
  text-align: center;
  font-size: 12px;
  color: #86909C;
  flex-shrink: 0;
}

.video-icon {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: #F7F8FA;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #86909C;

  .el-icon {
    font-size: 14px;
  }
}

.video-title {
  flex: 1;
  min-width: 0;
  font-weight: 500;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.empty-tip {
  padding: 24px 0;
  text-align: center;
  font-size: 13px;
  color: #C9CDD4;
}
</style>
