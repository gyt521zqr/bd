<template>
  <!-- 个人中心：头像/昵称/等级/积分/勋章/学习统计 -->
  <div class="profile-page">
    <!-- 用户信息卡片 -->
    <div class="profile-card">
      <div class="profile-header">
        <div class="avatar-upload-wrapper"
          @dragover.prevent="dragOver = true"
          @dragleave.prevent="dragOver = false"
          @drop.prevent="handleDrop"
          :class="{ 'drag-over': dragOver }"
        >
          <div class="avatar-ring">
            <el-avatar :size="80" :src="avatarUrl" class="profile-avatar">
              {{ userStore.userInfo?.nickname?.charAt(0) || 'U' }}
            </el-avatar>
          </div>
          <div class="upload-hint">
            <el-icon :size="16"><Upload /></el-icon>
            <span>点击或拖拽上传</span>
          </div>
          <input type="file" accept="image/*" class="file-input" @change="handleFileChange" />
        </div>
        <div class="profile-info">
          <h2>{{ userStore.userInfo?.nickname || '未设置昵称' }}</h2>
          <LevelBadge :level="userStore.userInfo?.level || 1" />
          <div class="profile-stats">
            <div class="stat">
              <span class="num">{{ userStore.userInfo?.points || 0 }}</span>
              <span class="label">积分</span>
            </div>
            <div class="stat">
              <span class="num">12</span>
              <span class="label">课程</span>
            </div>
            <div class="stat">
              <span class="num">56</span>
              <span class="label">练习</span>
            </div>
            <div class="stat">
              <span class="num">8</span>
              <span class="label">勋章</span>
            </div>
          </div>
        </div>
        <el-button type="primary" @click="$router.push(`/user/settings`)" class="edit-btn">
          <el-icon><EditPen /></el-icon> 编辑资料
        </el-button>
      </div>
    </div>

    <!-- 学习统计 -->
    <div class="stats-section section-card">
      <div class="section-header">
        <div class="header-bar"></div>
        <h3 class="section-title">学习统计</h3>
      </div>
      <div class="stats-grid">
        <div class="stat-card stat-hours">
          <div class="stat-icon-wrap">
            <el-icon :size="28"><TrendCharts /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">128</span>
            <span class="stat-label">学习时长(小时)</span>
          </div>
        </div>
        <div class="stat-card stat-courses">
          <div class="stat-icon-wrap">
            <el-icon :size="28"><Finished /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">45</span>
            <span class="stat-label">完成课程</span>
          </div>
        </div>
        <div class="stat-card stat-pass">
          <div class="stat-icon-wrap">
            <el-icon :size="28"><Trophy /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">92%</span>
            <span class="stat-label">考试通过率</span>
          </div>
        </div>
        <div class="stat-card stat-streak">
          <div class="stat-icon-wrap">
            <el-icon :size="28"><Star /></el-icon>
          </div>
          <div class="stat-info">
            <span class="stat-value">15天</span>
            <span class="stat-label">连续学习</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 勋章展示 -->
    <div class="medal-section section-card">
      <div class="section-header">
        <div class="header-bar"></div>
        <h3 class="section-title">我的勋章</h3>
      </div>
      <MedalShowcase :medals="medals" />
    </div>

    <!-- 学习周报 -->
    <div class="weekly-section section-card">
      <div class="section-header">
        <div class="header-bar"></div>
        <h3 class="section-title">本周学习</h3>
      </div>
      <div class="weekly-chart">
        <div v-for="day in weekDays" :key="day.label" class="day-item">
          <div class="day-bar-wrapper">
            <div class="day-bar" :style="{ height: day.hours * 20 + 'px' }"></div>
          </div>
          <span class="day-label">{{ day.label }}</span>
          <span class="day-value">{{ day.hours }}h</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * ProfilePage - 个人中心
 * 展示用户信息、学习统计、勋章、学习周报
 */
import { ref, computed } from 'vue'
import { TrendCharts, Finished, Trophy, Star, Upload, EditPen } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import LevelBadge from '@/components/growth/LevelBadge.vue'
import MedalShowcase from '@/components/growth/MedalShowcase.vue'
import { uploadFileViaServer } from '@/api/oss'
import { getOSSUrl } from '@/utils/storage'

const userStore = useUserStore()
const dragOver = ref(false)
const uploading = ref(false)

/** 头像URL（自动处理OSS路径） */
const avatarUrl = computed(() => {
  const avatar = userStore.userInfo?.avatar
  if (!avatar) return ''
  return getOSSUrl(avatar, { width: 200, height: 200, quality: 85 })
})

/** 上传头像 */
async function uploadAvatar(file: File) {
  if (uploading.value) return
  uploading.value = true

  try {
    const res = await uploadFileViaServer(
      file,
      { type: 'avatar', id: userStore.userInfo?.id },
    )
    if (res.code === 200 && res.data) {
      // 更新本地用户信息
      if (userStore.userInfo) {
        userStore.userInfo.avatar = res.data.url
      }
      ElMessage.success('头像上传成功')
    }
  } catch (error) {
    ElMessage.error('头像上传失败')
  } finally {
    uploading.value = false
  }
}

const medals = ref([
  { id: 1, name: '初学者', icon: 'seedling', description: '完成第一个课程', earned: true, earnedTime: '2024-01-15' },
  { id: 2, name: '勤奋学习', icon: 'book', description: '连续学习7天', earned: true, earnedTime: '2024-02-01' },
  { id: 3, name: '编程达人', icon: 'code', description: '完成50道练习', earned: true, earnedTime: '2024-03-10' },
  { id: 4, name: '竞赛之星', icon: 'trophy', description: '考试获得满分', earned: true, earnedTime: '2024-04-05' },
  { id: 5, name: '满分王者', icon: 'crown', description: '10次考试满分', earned: false, earnedTime: null },
  { id: 6, name: '知识渊博', icon: 'brain', description: '学习100小时', earned: false, earnedTime: null },
])

const weekDays = ref([
  { label: '周一', hours: 2 },
  { label: '周二', hours: 1.5 },
  { label: '周三', hours: 3 },
  { label: '周四', hours: 2 },
  { label: '周五', hours: 1 },
  { label: '周六', hours: 4 },
  { label: '周日', hours: 3.5 },
])

function handleFileChange(e: Event) {
  const target = e.target as HTMLInputElement
  if (target.files && target.files[0]) {
    uploadAvatar(target.files[0])
  }
}

function handleDrop(e: DragEvent) {
  dragOver.value = false
  const files = e.dataTransfer?.files
  if (files && files[0]) {
    uploadAvatar(files[0])
  }
}
</script>

<style scoped lang="scss">
$primary: #165DFF;
$action: #FF7D00;
$success: #36D399;
$warning: #FF9500;
$danger: #F53F3F;

.profile-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
}

// ==================== 分区卡片通用样式 ====================
.section-card {
  background: $bg-white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  margin-bottom: 20px;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s ease;

  &:hover {
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  }
}

.section-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;

  .header-bar {
    width: 4px;
    height: 22px;
    background: linear-gradient(180deg, $primary, $action);
    border-radius: 2px;
  }

  .section-title {
    font-size: 18px;
    color: #1D2129;
    font-weight: 700;
    margin: 0;
  }
}

// ==================== 用户信息卡片 ====================
.profile-card {
  background: $bg-white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  margin-bottom: 20px;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s ease;

  &:hover {
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  }
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 24px;
}

// 头像上传区域 + 拖拽提示
.avatar-upload-wrapper {
  position: relative;
  cursor: pointer;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 8px;
  border-radius: 16px;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s ease;
  border: 2px dashed transparent;

  &:hover {
    background: rgba(22, 93, 255, 0.03);

    .upload-hint {
      opacity: 1;
      transform: translateY(0);
    }
  }

  &.drag-over {
    background: rgba(22, 93, 255, 0.06);
    border-color: $primary;

    .upload-hint {
      opacity: 1;
      color: $primary;
    }
  }

  .file-input {
    position: absolute;
    inset: 0;
    opacity: 0;
    cursor: pointer;
  }
}

.avatar-ring {
  position: relative;
  display: inline-flex;
  padding: 3px;
  border-radius: 50%;
  background: linear-gradient(135deg, $primary, $success);

  &::before {
    content: '';
    position: absolute;
    inset: -2px;
    border-radius: 50%;
    background: linear-gradient(135deg, $success, $primary, $action);
    z-index: -1;
    animation: ringRotate 5s linear infinite;
    will-change: transform, opacity;
    opacity: 0.5;
  }

  .profile-avatar {
    background: linear-gradient(135deg, $primary, #36D399);
    font-size: 32px;
    font-weight: 600;
    border: 2px solid #fff;
  }
}

@keyframes ringRotate {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.upload-hint {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #86909C;
  opacity: 0;
  transform: translateY(-4px);
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s ease;
}

.profile-info {
  flex: 1;

  h2 {
    font-size: 22px;
    color: #1D2129;
    margin-bottom: 8px;
    font-weight: 700;
  }
}

.profile-stats {
  display: flex;
  gap: 28px;
  margin-top: 16px;
}

.stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  padding: 0 8px;

  &:not(:last-child)::after {
    content: '';
    position: absolute;
    right: -14px;
    top: 50%;
    transform: translateY(-50%);
    width: 1px;
    height: 24px;
    background: #E5E6EB;
  }

  .num {
    font-size: 22px;
    font-weight: 800;
    color: $primary;
    line-height: 1;
    margin-bottom: 4px;
  }

  .label {
    font-size: 12px;
    color: #86909C;
  }
}

.edit-btn {
  border-radius: 10px;
  font-weight: 500;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);

  &:hover {
    transform: scale(1.03);
    box-shadow: 0 4px 16px rgba(22, 93, 255, 0.3);
  }
}

// ==================== 学习统计 ====================
.stats-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.stat-card {
  border-radius: 14px;
  padding: 20px;
  display: flex;
  align-items: center;
  gap: 14px;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 32px rgba(0,0,0,0.1);
  }

  .stat-icon-wrap {
    width: 52px;
    height: 52px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .stat-info {
    display: flex;
    flex-direction: column;

    .stat-value {
      font-size: 24px;
      font-weight: 800;
      margin-bottom: 2px;
    }

    .stat-label {
      font-size: 12px;
      opacity: 0.8;
    }
  }

  &.stat-hours {
    background: linear-gradient(135deg, rgba(22, 93, 255, 0.08), rgba(64, 128, 255, 0.04));
    border: 1px solid rgba(22, 93, 255, 0.12);
    .stat-icon-wrap { background: linear-gradient(135deg, $primary, #4080FF); color: #fff; }
    .stat-value { color: $primary; }
    .stat-label { color: rgba(22, 93, 255, 0.7); }
  }

  &.stat-courses {
    background: linear-gradient(135deg, rgba(54, 211, 153, 0.08), rgba(92, 219, 156, 0.04));
    border: 1px solid rgba(54, 211, 153, 0.12);
    .stat-icon-wrap { background: linear-gradient(135deg, $success, #36D399); color: #fff; }
    .stat-value { color: $success; }
    .stat-label { color: rgba(54, 211, 153, 0.7); }
  }

  &.stat-pass {
    background: linear-gradient(135deg, rgba(255, 125, 0, 0.08), rgba(255, 149, 0, 0.04));
    border: 1px solid rgba(255, 125, 0, 0.12);
    .stat-icon-wrap { background: linear-gradient(135deg, $action, $warning); color: #fff; }
    .stat-value { color: $action; }
    .stat-label { color: rgba(255, 125, 0, 0.7); }
  }

  &.stat-streak {
    background: linear-gradient(135deg, rgba(245, 63, 63, 0.06), rgba(255, 100, 100, 0.03));
    border: 1px solid rgba(245, 63, 63, 0.1);
    .stat-icon-wrap { background: linear-gradient(135deg, $danger, #F76965); color: #fff; }
    .stat-value { color: $danger; }
    .stat-label { color: rgba(245, 63, 63, 0.7); }
  }
}

// ==================== 学习周报 ====================
.weekly-chart {
  display: flex;
  align-items: flex-end;
  justify-content: space-around;
  height: 140px;
  padding-top: 20px;
}

.day-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  flex: 1;
}

.day-bar-wrapper {
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: flex-end;
  height: 100px;
}

.day-bar {
  width: 32px;
  background: linear-gradient(180deg, $primary, #4080FF);
  border-radius: 6px 6px 0 0;
  min-height: 4px;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
  position: relative;

  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 40%;
    background: linear-gradient(180deg, rgba(255,255,255,0.2), transparent);
    border-radius: 6px 6px 0 0;
  }

  &:hover {
    filter: brightness(1.1);
    transform: scaleX(1.1);
  }
}

.day-label {
  font-size: 12px;
  color: #86909C;
  font-weight: 500;
}

.day-value {
  font-size: 11px;
  color: #4E5969;
  font-weight: 600;
}

// ==================== 响应式 ====================
@media (max-width: 992px) {
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .profile-stats {
    gap: 16px;
  }
}

@media (max-width: 768px) {
  .profile-page {
    padding: 12px;
  }

  .profile-header {
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }

  .profile-stats {
    justify-content: center;
    gap: 20px;
  }

  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .weekly-chart {
    height: 120px;
  }

  .day-bar {
    width: 24px;
  }
}
</style>
