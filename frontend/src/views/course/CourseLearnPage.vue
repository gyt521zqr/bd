<template>
  <!-- 课程学习页：深色播放器 + 顶栏 + UP主 + 章节导航 -->
  <div class="course-learn-page">
    <!-- courseId 无效或课程不存在时的空态 -->
    <div v-if="courseNotFound" class="course-empty">
      <el-empty description="课程信息不存在" :image-size="90" />
    </div>
    <template v-else>
    <!-- 顶部信息栏 -->
    <div class="play-topbar">
      <div class="topbar-playing">正在播放：{{ currentIndex + 1 }}·{{ currentVideo?.title || '请选择视频' }}</div>
      <div class="topbar-row">
        <div class="topbar-left">
          <span class="brand-logo">码高编程</span>
          <span class="brand-title">{{ currentVideo?.title || courseTitle }}</span>
        </div>
        <div class="topbar-right">
          <button class="top-action" :class="{ active: liked }" @click="toggleLike">
            <el-icon><Pointer /></el-icon><span>{{ likeCountText }}</span>
          </button>
          <button class="top-action" @click="scrollToComment">
            <el-icon><ChatDotRound /></el-icon><span>{{ commentCount }}</span>
          </button>
          <button class="top-action" @click="shareVideo">
            <el-icon><Share /></el-icon><span>分享</span>
          </button>
          <button class="top-action"><el-icon><MoreFilled /></el-icon></button>
        </div>
      </div>
    </div>

    <div class="learn-layout">
      <!-- 左侧：视频播放器 -->
      <div class="learn-main">
        <div class="video-player-wrap">
          <VideoPlayer
            v-if="videos.length > 0"
            :src="currentVideoUrl"
            :watermark-text="watermarkText"
            :initial-progress="0"
            @time-update="handleTimeUpdate"
            @ended="handleVideoEnded"
          />
          <div v-else class="video-empty">暂无课时</div>
        </div>

        <!-- UP主信息区 -->
        <div class="up-info">
          <el-avatar :size="44" :src="coachAvatar">{{ coachInitial }}</el-avatar>
          <div class="up-meta">
            <span class="up-name">{{ coachName }}</span>
            <span class="up-sub">讲师 · 码高认证</span>
          </div>
          <button class="follow-btn" :class="{ followed }" @click="toggleFollow">
            {{ followed ? '已关注' : '+ 关注' }}
          </button>
        </div>

        <!-- 视频信息 -->
        <div class="video-info-card">
          <div class="video-info-main">
            <div class="video-breadcrumb">
              <el-icon><Reading /></el-icon>
              <span>{{ courseTitle }}</span>
              <el-icon><ArrowRight /></el-icon>
              <span class="current">{{ currentVideo?.title || '请选择视频' }}</span>
            </div>
            <h2 class="video-title">{{ currentVideo?.title || '请选择视频' }}</h2>
            <div class="video-meta">
              <span class="meta-item">
                <el-icon><View /></el-icon>
                1,234 次学习
              </span>
            </div>
          </div>
          <div class="video-actions">
            <el-button size="small" :icon="ChatDotRound" class="action-btn">
              <span class="btn-text">发送弹幕</span>
            </el-button>
            <el-button size="small" :icon="Star" class="action-btn fav-btn">
              <span class="btn-text">收藏</span>
            </el-button>
            <el-button size="small" :icon="Share" class="action-btn">
              <span class="btn-text">分享</span>
            </el-button>
          </div>
        </div>

        <!-- 评论区 -->
        <div class="comment-section" ref="commentRef">
          <div class="comment-header">
            <h3>
              <el-icon><ChatLineRound /></el-icon>
              课程评论
              <span class="comment-count">({{ commentCount }})</span>
            </h3>
          </div>
          <div class="comment-input-wrap">
            <el-avatar :size="36" class="comment-avatar">我</el-avatar>
            <div class="comment-input-box">
              <el-input
                v-model="commentText"
                placeholder="写下你的评论，与其他同学交流..."
                type="textarea"
                :rows="3"
                class="comment-textarea"
              />
              <div class="comment-input-actions">
                <span class="input-hint">文明发言，共同进步</span>
                <el-button type="primary" size="small" @click="handleComment">
                  发表评论
                </el-button>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 右侧：章节列表 -->
      <div class="learn-sidebar">
        <div class="sidebar-header">
          <h3>
            <el-icon><List /></el-icon>
            课程目录
          </h3>
        </div>
        <VideoList
          v-if="videos.length > 0"
          :videos="videos"
          :active-id="currentVideoId"
          @select="handleSelectVideo"
        />
        <div v-else class="sidebar-empty">暂无课时</div>
      </div>
    </div>

    <!-- 底部导航 -->
    <div class="play-nav">
      <button class="nav-btn" :disabled="!prevVideo" @click="goPrev">
        <el-icon><ArrowLeft /></el-icon> 上一讲
      </button>
      <div class="nav-actions">
        <button class="nav-text-btn">报错</button>
        <button class="nav-text-btn">联系本站</button>
        <button class="nav-text-btn">高清播放</button>
      </div>
      <button class="nav-btn" :disabled="!nextVideo" @click="goNext">
        下一讲 <el-icon><ArrowRight /></el-icon>
      </button>
    </div>
    </template>
  </div>
</template>

<script setup lang="ts">
/**
 * CourseLearnPage - 课程学习页
 * 视频播放、进度记录、弹幕、评论
 * 课程详情与章节列表来自真实 API，无数据时由模板显示空态
 */
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import {
  ChatDotRound, Star, ChatLineRound, Reading, ArrowRight,
  View, List, Share, Pointer, MoreFilled, ArrowLeft
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import VideoPlayer from '@/components/course/VideoPlayer.vue'
import VideoList from '@/components/course/VideoList.vue'
import { useUserStore } from '@/stores/user'
import { getCourseDetail, getVideoUrl, recordProgress } from '@/api/course'

const route = useRoute()
const userStore = useUserStore()
const currentVideoId = ref(0)
const commentText = ref('')
const courseNotFound = ref(false)

// ====== 图2（B站深色风）相关状态 ======
const courseTitle = ref('Python基础入门')
const coachName = ref('码高讲师')
const coachAvatar = ref('')
const coachInitial = computed(() => (coachName.value || '?').charAt(0))
const followed = ref(false)
const liked = ref(false)
const likeCount = ref(11000)
const likeCountText = computed(() => formatCount(likeCount.value))
const commentCount = ref(128)
const commentRef = ref<HTMLElement>()

function formatCount(n: number): string {
  if (n >= 10000) return (n / 10000).toFixed(1) + '万'
  return String(n)
}

const currentIndex = computed(() => videos.value.findIndex(v => v.id === currentVideoId.value))
const prevVideo = computed(() => (currentIndex.value > 0 ? videos.value[currentIndex.value - 1] : null))
const nextVideo = computed(() =>
  (currentIndex.value >= 0 && currentIndex.value < videos.value.length - 1) ? videos.value[currentIndex.value + 1] : null
)

function goPrev() { if (prevVideo.value) handleSelectVideo(prevVideo.value.id) }
function goNext() { if (nextVideo.value) handleSelectVideo(nextVideo.value.id) }
function toggleLike() { liked.value = !liked.value; likeCount.value += liked.value ? 1 : -1 }
function toggleFollow() { followed.value = !followed.value }
function scrollToComment() { commentRef.value?.scrollIntoView({ behavior: 'smooth' }) }
function shareVideo() { ElMessage.success('链接已复制，快去分享吧！') }

const watermarkText = computed(() => userStore.userInfo?.nickname || '')

/** 课程视频列表：不再内置虚构课时，接口无数据时由模板显示空态 */
interface CourseVideo {
  id: number
  title: string
  videoUrl: string
  sortOrder: number
}
const videos = ref<CourseVideo[]>([])
/** 存储API返回的视频URL映射 */
const videoUrlMap = ref<Record<number, string>>({})

const currentVideo = computed(() => {
  return videos.value.find(v => v.id === currentVideoId.value) || null
})

const currentVideoUrl = computed(() => {
  if (!currentVideo.value) return ''
  // 优先使用API返回的视频URL
  if (videoUrlMap.value[currentVideo.value.id]) {
    return videoUrlMap.value[currentVideo.value.id]
  }
  // 未返回可用地址时走后端视频流接口
  return `/api/course/video/${currentVideo.value.id}`
})

/** 加载课程详情和视频 */
async function loadCourseDetail() {
  const courseId = Number(route.params.courseId)
  if (!courseId || isNaN(courseId)) {
    courseNotFound.value = true
    return
  }

  try {
    const res = await getCourseDetail(courseId)
    if (res.data) {
      if (res.data.title) courseTitle.value = res.data.title
      if (res.data.teacherName) coachName.value = res.data.teacherName
      // 从API返回的课程详情中获取视频列表（扁平，无章节）
      if (res.data.videos && res.data.videos.length > 0) {
        videos.value = res.data.videos.map((v: any) => ({
          id: v.id,
          title: v.title,
          videoUrl: v.videoUrl || '',
          sortOrder: v.sortOrder || 0,
        }))

        // 预加载第一个视频的URL
        const firstVideo = videos.value[0]
        if (firstVideo) {
          currentVideoId.value = firstVideo.id
          if (firstVideo.videoUrl) {
            videoUrlMap.value[firstVideo.id] = firstVideo.videoUrl
          }
        }
      }
    }
  } catch {
    // 接口无数据或失败：保持空态，由模板显示「暂无数据」
  }
}

/** 获取视频播放地址 */
async function fetchVideoUrl(videoId: number) {
  // 如果已经有缓存的URL，不重复请求
  if (videoUrlMap.value[videoId]) return

  try {
    const res = await getVideoUrl(videoId)
    if (res.data && res.data.url) {
      videoUrlMap.value[videoId] = res.data.url
    }
  } catch {
    // 接口无数据或失败：保持空态，不渲染虚构视频地址
  }
}

function handleSelectVideo(id: number) {
  currentVideoId.value = id
  // 切换视频时，尝试获取真实播放地址
  fetchVideoUrl(id)
}

/** 记录学习进度 */
function handleTimeUpdate(time: number) {
  if (!currentVideo.value) return
  // 防抖：每10秒记录一次进度
  if (Math.floor(time) % 10 === 0) {
    try {
      recordProgress(currentVideo.value.id, time)
    } catch (err) {
      // 进度记录失败不影响用户体验
      console.warn('记录学习进度失败', err)
    }
  }
}

function handleVideoEnded() { ElMessage.success('本节完成！') }

/** 发表评论（当前暂无评论接口） */
function handleComment() {
  if (!commentText.value.trim()) return
  ElMessage.success('评论成功')
  commentText.value = ''
}

onMounted(async () => {
  await loadCourseDetail()
  const qv = route.query.videoId
  if (qv && videos.value.some(v => v.id === Number(qv))) {
    currentVideoId.value = Number(qv)
    fetchVideoUrl(Number(qv))
  }
})
</script>

<style scoped lang="scss">
$primary: #165DFF;
$bg-page: #F7F8FA;

.course-learn-page {
  background: #0f0f0f;
  min-height: 100vh;
  padding: 0 0 40px;
  color: #e3e5e7;
}

// ==================== 顶部信息栏 ====================
.play-topbar {
  max-width: 1300px;
  margin: 0 auto;
  padding: 14px 20px 10px;
}

.topbar-playing {
  text-align: center;
  font-size: 13px;
  color: #c9cdd4;
  margin-bottom: 10px;
}

.topbar-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.topbar-left {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.brand-logo {
  font-size: 15px;
  font-weight: 800;
  color: #fff;
  background: linear-gradient(135deg, #165DFF, #4080FF);
  padding: 4px 10px;
  border-radius: 8px;
  flex-shrink: 0;
  letter-spacing: 0.5px;
}

.brand-title {
  font-size: 15px;
  font-weight: 600;
  color: #e3e5e7;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.topbar-right {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-shrink: 0;
}

.top-action {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 10px;
  border: none;
  background: rgba(255, 255, 255, 0.08);
  color: #c9cdd4;
  border-radius: 8px;
  cursor: pointer;
  font-size: 13px;
  transition: background 0.25s ease, color 0.25s ease;

  &:hover {
    background: rgba(255, 255, 255, 0.16);
    color: #fff;
  }

  &.active {
    color: #ff6699;
  }

  .el-icon {
    font-size: 16px;
  }
}

// ==================== UP主信息区 ====================
.up-info {
  display: flex;
  align-items: center;
  gap: 12px;
  background: $bg-white;
  border-radius: 12px;
  padding: 14px 20px;
  margin-top: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.3);
}

.up-meta {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.up-name {
  font-size: 15px;
  font-weight: 700;
  color: #1D2129;
}

.up-sub {
  font-size: 12px;
  color: #86909C;
}

.follow-btn {
  flex-shrink: 0;
  padding: 7px 18px;
  border-radius: 8px;
  border: 1.5px solid #165DFF;
  background: #165DFF;
  color: #fff;
  font-size: 13px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.25s ease, border-color 0.25s ease;

  &:hover {
    background: #4080FF;
    border-color: #4080FF;
  }

  &.followed {
    background: #fff;
    color: #86909C;
    border-color: #E5E6EB;
  }
}

// ==================== 底部导航 ====================
.play-nav {
  max-width: 1300px;
  margin: 20px auto 0;
  padding: 0 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.nav-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 22px;
  border-radius: 10px;
  border: none;
  background: rgba(255, 255, 255, 0.1);
  color: #e3e5e7;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.25s ease, opacity 0.25s ease;

  &:hover:not(:disabled) {
    background: rgba(22, 93, 255, 0.5);
  }

  &:disabled {
    opacity: 0.4;
    cursor: not-allowed;
  }

  .el-icon {
    font-size: 16px;
  }
}

.nav-actions {
  display: flex;
  gap: 8px;
}

.nav-text-btn {
  padding: 8px 16px;
  border-radius: 8px;
  border: 1px solid rgba(255, 255, 255, 0.18);
  background: transparent;
  color: #c9cdd4;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.25s ease, color 0.25s ease;

  &:hover {
    background: rgba(255, 255, 255, 0.1);
    color: #fff;
  }
}

@media (max-width: 768px) {
  .play-nav {
    flex-wrap: wrap;
    justify-content: center;
  }

  .nav-actions {
    order: 3;
    width: 100%;
    justify-content: center;
  }

  .topbar-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .topbar-right {
    align-self: flex-start;
  }
}

.learn-layout {
  max-width: 1300px;
  margin: 0 auto;
  padding: 0 20px;
  display: flex;
  gap: 20px;
}

.learn-main {
  flex: 1;
  min-width: 0;
}

.video-player-wrap {
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.12);
  background: #000;
}

// ==================== 空态 ====================
.course-empty {
  min-height: 70vh;
  display: flex;
  align-items: center;
  justify-content: center;
  :deep(.el-empty__description p) {
    color: #c9cdd4;
  }
}

.video-empty {
  aspect-ratio: 16 / 9;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #c9cdd4;
  font-size: 15px;
  background: #000;
}

.sidebar-empty {
  padding: 40px 16px;
  text-align: center;
  font-size: 13px;
  color: #86909C;
}

.video-info-card {
  background: $bg-white;
  border-radius: 12px;
  padding: 20px 24px;
  margin-top: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 20px;
}

.video-info-main {
  flex: 1;
  min-width: 0;
}

.video-breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #86909C;
  margin-bottom: 10px;

  .el-icon {
    font-size: 14px;
  }

  .current {
    color: #165DFF;
    font-weight: 500;
  }
}

.video-title {
  font-size: 20px;
  font-weight: 700;
  color: #1D2129;
  margin: 0 0 12px;
  line-height: 1.4;
}

.video-meta {
  display: flex;
  gap: 20px;

  .meta-item {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 13px;
    color: #86909C;

    .el-icon {
      font-size: 14px;
    }
  }
}

.video-actions {
  display: flex;
  gap: 8px;
  flex-shrink: 0;

  .action-btn {
    border-radius: 8px;
    transition: transform, box-shadow, background-color, border-color, color, opacity 0.25s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 4px 12px rgba(22, 93, 255, 0.15);
    }

    &.fav-btn:hover {
      color: #FF7D00;
      border-color: #FF7D00;
      box-shadow: 0 4px 12px rgba(255, 125, 0, 0.15);
    }
  }
}

.comment-section {
  background: $bg-white;
  border-radius: 12px;
  padding: 20px 24px;
  margin-top: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.comment-header {
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
      font-size: 18px;
    }

    .comment-count {
      font-size: 13px;
      color: #86909C;
      font-weight: 400;
    }
  }
}

.comment-input-wrap {
  display: flex;
  gap: 12px;
}

.comment-avatar {
  flex-shrink: 0;
  background: linear-gradient(135deg, #165DFF, #4080FF);
  color: #fff;
  font-size: 13px;
  font-weight: 600;
}

.comment-input-box {
  flex: 1;
  min-width: 0;
}

.comment-textarea {
  :deep(.el-textarea__inner) {
    border-radius: 10px;
    resize: none;

    &:focus {
      border-color: #165DFF;
      box-shadow: 0 0 0 3px rgba(22, 93, 255, 0.08);
    }
  }
}

.comment-input-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 10px;

  .input-hint {
    font-size: 12px;
    color: #86909C;
  }
}

.learn-sidebar {
  width: 340px;
  flex-shrink: 0;
  background: $bg-white;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  overflow: hidden;
  max-height: calc(100vh - 40px);
  display: flex;
  flex-direction: column;
}

.sidebar-header {
  padding: 16px 20px;
  border-bottom: 1px solid #F7F8FA;
  flex-shrink: 0;

  h3 {
    font-size: 15px;
    font-weight: 700;
    color: #1D2129;
    margin: 0 0 10px;
    display: flex;
    align-items: center;
    gap: 8px;

    .el-icon {
      color: #165DFF;
    }
  }
}

.sidebar-progress {
  display: flex;
  align-items: center;
  gap: 10px;

  :deep(.el-progress) {
    flex: 1;
  }

  .progress-text {
    font-size: 12px;
    font-weight: 600;
    color: #165DFF;
    min-width: 36px;
    text-align: right;
  }
}

// ==================== 响应式 ====================
@media (max-width: 992px) {
  .learn-layout {
    flex-direction: column;
  }

  .learn-sidebar {
    width: 100%;
    max-height: none;
  }

  .video-info-card {
    flex-direction: column;
    gap: 16px;
  }

  .video-actions {
    width: 100%;
    justify-content: flex-start;
  }
}

@media (max-width: 576px) {
  .course-learn-page {
    padding: 12px;
  }

  .video-info-card {
    padding: 16px;
  }

  .video-title {
    font-size: 16px;
  }

  .video-meta {
    flex-wrap: wrap;
    gap: 12px;
  }

  .comment-section {
    padding: 16px;
  }

  .comment-input-wrap {
    flex-direction: column;
  }

  .video-actions {
    .btn-text {
      display: none;
    }
  }
}
</style>
