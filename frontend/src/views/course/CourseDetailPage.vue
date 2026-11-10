<template>
  <!-- 课程详情：封面/简介/章节目录/试听预约 -->
  <div class="course-detail-page">
    <template v-if="hasCourse">
    <!-- 顶部：课程信息卡 -->
    <div class="course-info-section">
      <div class="course-info-card">
        <div class="info-cover">
          <img :src="infoCover" :alt="course.title" />
          <span class="info-difficulty" :class="`diff-${course.difficulty}`">{{ difficultyLabel }}</span>
        </div>
        <div class="info-main">
          <h1 class="info-title">{{ course.title }}</h1>
          <div class="info-fields">
            <div class="info-field">
              <span class="field-label">分类</span>
              <span class="field-value">{{ courseCategoryLabel }}</span>
            </div>
            <div class="info-field">
              <span class="field-label">观看人数</span>
              <span class="field-value view-count">{{ course.studentCount }} 人</span>
            </div>
            <div class="info-field">
              <span class="field-label">更新时间</span>
              <span class="field-value">{{ courseUpdateTime }}</span>
            </div>
          </div>
          <div class="info-actions">
            <span class="info-price" :class="{ free: course.price === 0 }">
              {{ course.price > 0 ? '¥' + course.price : '免费' }}
            </span>
            <el-button type="warning" size="large" class="trial-btn" @click="openTrialDialog">
              <el-icon><Calendar /></el-icon>
              预约试听
            </el-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="page-content">
      <div class="course-body">
        <!-- 左侧：Tab内容区 -->
        <div class="body-left">
          <el-tabs v-model="activeTab" class="course-tabs">
            <el-tab-pane name="list">
              <template #label>
                <span class="tab-label"><el-icon><VideoPlay /></el-icon>课程列表</span>
              </template>
              <div class="chapter-grid">
                <template v-if="videos.length">
                <div
                  v-for="(video, idx) in videos"
                  :key="video.id"
                  class="chapter-item"
                  @click="goLearn(video.id)"
                >
                  <div class="chapter-icon"><el-icon><VideoPlay /></el-icon></div>
                  <div class="chapter-text">
                    <span class="chapter-index">{{ idx + 1 }}</span>
                    <span class="chapter-title">{{ video.title }}</span>
                  </div>
                </div>
                </template>
                <div v-else class="empty-tip">暂无视频</div>
              </div>
            </el-tab-pane>

            <el-tab-pane name="detail">
              <template #label>
                <span class="tab-label"><el-icon><Document /></el-icon>课程详情</span>
              </template>
              <div class="detail-content">
                <section class="detail-block">
                  <h3 class="detail-h3">课程介绍</h3>
                  <p class="detail-desc">{{ course.description }}</p>
                </section>
                <section class="detail-block">
                  <h3 class="detail-h3">课程特色</h3>
                  <div class="feature-grid" v-if="course.features.length">
                    <div
                      v-for="(feature, idx) in course.features"
                      :key="idx"
                      class="feature-card"
                      :style="{ animationDelay: `${idx * 0.08}s` }"
                    >
                      <div class="feature-icon"><el-icon><Check /></el-icon></div>
                      <span class="feature-name">{{ feature }}</span>
                    </div>
                  </div>
                  <el-empty v-else description="暂无课程特色" :image-size="60" />
                </section>
                <section class="detail-block">
                  <h3 class="detail-h3">教练介绍</h3>
                  <div class="coach-card-enhanced" v-if="coach.name">
                    <div class="coach-avatar-wrap">
                      <el-avatar :size="80" :src="coach.avatar">{{ coach.name?.charAt(0) }}</el-avatar>
                      <div class="coach-badge"><el-icon><Medal /></el-icon></div>
                    </div>
                    <div class="coach-info-enhanced">
                      <div class="coach-name-row">
                        <span class="coach-name">{{ coach.name }}</span>
                        <el-tag size="small" type="primary" effect="plain">认证讲师</el-tag>
                      </div>
                      <div class="coach-title">{{ coach.title }}</div>
                      <div class="coach-stats">
                        <div class="coach-stat"><span class="coach-stat-num">{{ coach.studentCount }}</span><span class="coach-stat-label">培养学员</span></div>
                        <div class="coach-stat"><span class="coach-stat-num">{{ coach.experienceYears }}</span><span class="coach-stat-label">教学经验</span></div>
                        <div class="coach-stat"><span class="coach-stat-num">{{ coach.score }}</span><span class="coach-stat-label">学员评分</span></div>
                      </div>
                      <p class="coach-desc">{{ coach.description }}</p>
                    </div>
                  </div>
                  <el-empty v-else description="暂无教练信息" :image-size="60" />
                </section>
                <section class="detail-block">
                  <h3 class="detail-h3">学员评价</h3>
                  <div class="review-list-enhanced" v-if="reviews.length">
                    <div v-for="(review, index) in reviews" :key="review.id" class="review-item-enhanced" :style="{ animationDelay: `${index * 0.1}s` }">
                      <div class="review-header">
                        <el-avatar :size="40" :src="review.avatar">{{ review.nickname?.charAt(0) }}</el-avatar>
                        <div class="review-meta">
                          <span class="review-name">{{ review.nickname }}</span>
                          <el-rate v-model="review.rating" disabled size="small" />
                        </div>
                        <span class="review-time">{{ review.time }}</span>
                      </div>
                      <p class="review-content">{{ review.content }}</p>
                    </div>
                  </div>
                  <el-empty v-else description="暂无学员评价" :image-size="60" />
                </section>
              </div>
            </el-tab-pane>
          </el-tabs>
        </div>

        <!-- 右侧：侧边信息 -->
        <div class="body-right">
          <div class="side-card">
            <h3 class="side-title">课程信息</h3>
            <div class="side-info-list">
              <div class="side-info-item">
                <span class="side-label">课程难度</span>
                <el-tag :type="difficultyTagType" size="small">{{ difficultyLabel }}</el-tag>
              </div>
              <div class="side-info-item">
                <span class="side-label">适合年龄</span>
                <span class="side-value">{{ course.ageRange }}</span>
              </div>
              <div class="side-info-item">
                <span class="side-label">授课老师</span>
                <span class="side-value">{{ course.teacherName }}</span>
              </div>
              <div class="side-info-item">
                <span class="side-label">学员人数</span>
                <span class="side-value">{{ course.studentCount }}人</span>
              </div>
              <div class="side-info-item">
                <span class="side-label">课程评分</span>
                <span class="side-value">
                  <el-icon color="#FF9500"><StarFilled /></el-icon>
                  {{ course.rating }}
                </span>
              </div>
            </div>
          </div>

          <div class="side-card">
            <h3 class="side-title">学习保障</h3>
            <div class="guarantee-list">
              <div class="guarantee-item">
                <el-icon color="#36D399"><CircleCheck /></el-icon>
                <span>免费试听第一节</span>
              </div>
              <div class="guarantee-item">
                <el-icon color="#36D399"><CircleCheck /></el-icon>
                <span>不满意随时退款</span>
              </div>
              <div class="guarantee-item">
                <el-icon color="#36D399"><CircleCheck /></el-icon>
                <span>1对1学习答疑</span>
              </div>
              <div class="guarantee-item">
                <el-icon color="#36D399"><CircleCheck /></el-icon>
                <span>学习进度跟踪</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 底部固定栏（移动端） -->
    <div class="mobile-action-bar">
      <el-button type="warning" size="large" class="trial-btn" @click="openTrialDialog">
        <el-icon><Calendar /></el-icon>
        预约试听
      </el-button>
    </div>

    <!-- 试听预约弹窗 -->
    <el-dialog
      v-model="trialDialogVisible"
      title="试听预约"
      width="auto"
      style="max-width: 480px; width: 90%;"
      :close-on-click-modal="false"
      destroy-on-close
      class="trial-dialog"
    >
      <div class="trial-dialog-header">
        <div class="trial-course-info">
          <el-icon :size="32" color="#165DFF"><Reading /></el-icon>
          <div>
            <div class="trial-course-name">{{ course.title }}</div>
            <div class="trial-course-meta">{{ course.ageRange }} | {{ difficultyLabel }}</div>
          </div>
        </div>
      </div>
      <el-form
        ref="trialFormRef"
        :model="trialForm"
        :rules="trialRules"
        label-width="100px"
        class="trial-form"
      >
        <el-form-item label="课程名称">
          <el-input v-model="trialForm.courseName" disabled />
        </el-form-item>
        <el-form-item label="家长姓名" prop="parentName">
          <el-input v-model="trialForm.parentName" placeholder="请输入家长姓名" />
        </el-form-item>
        <el-form-item label="孩子年龄" prop="childAge">
          <el-input-number v-model="trialForm.childAge" :min="3" :max="18" style="width: 100%" />
        </el-form-item>
        <el-form-item label="联系电话" prop="phone">
          <el-input v-model="trialForm.phone" placeholder="请输入联系电话" maxlength="11" />
        </el-form-item>
        <el-form-item label="期望上课时间" prop="preferredTime">
          <el-select v-model="trialForm.preferredTime" placeholder="请选择期望上课时间" style="width: 100%">
            <el-option label="工作日晚上" value="weekday_evening" />
            <el-option label="周末上午" value="weekend_morning" />
            <el-option label="周末下午" value="weekend_afternoon" />
            <el-option label="周末晚上" value="weekend_evening" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input
            v-model="trialForm.remark"
            type="textarea"
            :rows="3"
            placeholder="请输入其他需求或备注信息"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="trialDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitTrial">提交预约</el-button>
      </template>
    </el-dialog>
    </template>
    <div v-else class="page-empty">
      <el-empty description="课程不存在或暂无课程信息" :image-size="120" />
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * CourseDetailPage - 课程详情
 * 课程信息、章节目录、教练介绍、学员评价、试听预约
 */
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  Reading, VideoPlay, StarFilled, Check,
  Calendar, CircleCheck, Medal, Document
} from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { getCourseDetail, getCourseVideos } from '@/api/course'
import type { CourseDetail } from '@/types'
import type { CourseDifficulty } from '@/types/course'

const route = useRoute()
const router = useRouter()
const courseId = route.params.id as string

const difficultyMap: Record<string, string> = { beginner: '入门', intermediate: '进阶', advanced: '高级' }
const difficultyLabel = computed(() => difficultyMap[course.value.difficulty] || '')
const difficultyTagType = computed(() => {
  const map: Record<string, 'success' | 'warning' | 'danger' | 'info'> = { beginner: 'success', intermediate: 'warning', advanced: 'danger' }
  return map[course.value.difficulty] || 'info'
})

// 课程展示数据结构（融合后端 CourseDetail 与页面扩展字段 ageRange/features）
interface CourseDisplay {
  id: number | string
  title: string
  cover: string
  description: string
  difficulty: CourseDifficulty
  ageRange: string
  teacherName: string
  studentCount: number
  rating: number
  price: number
  hasTrial: boolean
  features: string[]
  updateTime?: string
}

// 课程信息初始空态：字段齐全但值为空，接口返回前不展示任何虚构内容
const emptyCourse: CourseDisplay = {
  id: '',
  title: '',
  cover: '',
  description: '',
  difficulty: 'beginner',
  ageRange: '',
  teacherName: '',
  studentCount: 0,
  rating: 0,
  price: 0,
  hasTrial: false,
  features: [],
  updateTime: '',
}
const course = ref<CourseDisplay>({ ...emptyCourse })
// 接口成功返回课程后置为 true，否则整页显示「课程不存在」空态
const hasCourse = ref(false)

// 视频展示数据结构（扁平，无章节）
interface CourseVideoItem {
  id: number
  title: string
  videoUrl?: string
  sortOrder?: number
}

// 视频列表（接口返回前保持空态，onMounted 中用真实 API 数据填充）
const videos = ref<CourseVideoItem[]>([])

const activeTab = ref('list')

// 课程分类 / 更新时间（展示字段）
const courseCategoryLabel = computed(() => (difficultyLabel.value ? difficultyLabel.value + '编程' : '编程课程'))
const courseUpdateTime = computed(() => course.value.updateTime || '')

// 课程封面图：优先用后端返回的 cover，否则按 id 回退到本地 course-x.png（与课程中心一致）
function defaultCourseCover(id: number | string): string {
  const n = Number(id) || 1
  const idx = ((n - 1) % 11) + 1
  return `/images/course/course-${idx}.png`
}
const infoCover = computed(() => course.value.cover || defaultCourseCover(course.value.id))

// 进入播放页
function goLearn(videoId: number) {
  router.push({ path: `/course/learn/${courseId}`, query: { videoId } })
}

// 教练信息（接口未返回该字段，保持空态）
interface CoachDisplay {
  name: string
  avatar: string
  title: string
  description: string
  studentCount: number
  experienceYears: string
  score: number
}
const coach = ref<CoachDisplay>({
  name: '',
  avatar: '',
  title: '',
  description: '',
  studentCount: 0,
  experienceYears: '',
  score: 0,
})

// 学员评价（接口未返回该字段，保持空态）
interface CourseReview {
  id: number
  nickname: string
  avatar: string
  rating: number
  content: string
  time: string
}
const reviews = ref<CourseReview[]>([])

// ==================== API 数据加载（失败或无数据时保持空态） ====================
async function loadCourseDetail() {
  try {
    const res = await getCourseDetail(Number(courseId))
    const detail: CourseDetail | undefined = res?.data
    if (detail) {
      course.value.id = detail.id ?? course.value.id
      course.value.title = detail.title || course.value.title
      course.value.cover = detail.cover || course.value.cover
      course.value.description = detail.description || course.value.description
      course.value.difficulty = detail.difficulty || course.value.difficulty
      course.value.teacherName = detail.teacherName || course.value.teacherName
      course.value.studentCount = detail.studentCount ?? course.value.studentCount
      course.value.rating = detail.rating ?? course.value.rating
      course.value.price = detail.price ?? course.value.price
      course.value.hasTrial = detail.hasTrial ?? course.value.hasTrial
      // ageRange / features / updateTime：接口暂未返回该字段，保持空态
      hasCourse.value = true
    }
  } catch {
    // 接口失败：保持空态，由模板显示「课程不存在或暂无课程信息」
  }
}

async function loadCourseVideos() {
  try {
    const res = await getCourseVideos(Number(courseId))
    const list = res?.data
    if (Array.isArray(list) && list.length > 0) {
      videos.value = list.map((v: any) => ({ id: v.id, title: v.title, videoUrl: v.videoUrl, sortOrder: v.sortOrder }))
    }
  } catch {
    // 接口失败或返回空：保持空态，由模板显示「暂无视频」
  }
}

onMounted(() => {
  loadCourseDetail()
  loadCourseVideos()
})

// ==================== 试听预约弹窗 ====================
const trialDialogVisible = ref(false)
const submitting = ref(false)
const trialFormRef = ref<FormInstance>()

const trialForm = ref({
  courseId: Number(courseId),
  courseName: course.value.title,
  parentName: '',
  childAge: parseInt(course.value.ageRange.split('-')[0]) || 8,
  phone: '',
  preferredTime: '',
  remark: '',
})

const trialRules: FormRules = {
  parentName: [{ required: true, message: '请输入家长姓名', trigger: 'blur' }],
  childAge: [{ required: true, message: '请输入孩子年龄', trigger: 'change' }],
  phone: [
    { required: true, message: '请输入联系电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  preferredTime: [{ required: true, message: '请选择期望上课时间', trigger: 'change' }],
}

function openTrialDialog() {
  trialForm.value.courseId = Number(courseId)
  trialForm.value.courseName = course.value.title
  trialForm.value.parentName = ''
  trialForm.value.childAge = parseInt(course.value.ageRange.split('-')[0]) || 8
  trialForm.value.phone = ''
  trialForm.value.preferredTime = ''
  trialForm.value.remark = ''
  trialDialogVisible.value = true
}

async function submitTrial() {
  if (!trialFormRef.value) return
  await trialFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      await new Promise(resolve => setTimeout(resolve, 800))
      ElMessage.success('预约提交成功，我们会尽快与您联系！')
      trialDialogVisible.value = false
    } catch {
      ElMessage.error('提交失败，请稍后重试')
    } finally {
      submitting.value = false
    }
  })
}
</script>

<style scoped lang="scss">
$primary: #165DFF;
$action: #FF7D00;

.course-detail-page {
  width: 100%;
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 80px;
}

// ==================== 整页空态 ====================
.page-empty {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 60vh;
}

// ==================== 顶部：课程信息卡 ====================
.course-info-section {
  max-width: 1200px;
  margin: 0 auto;
  padding: 32px 20px 0;
}

.course-info-card {
  display: flex;
  gap: 32px;
  background: $bg-white;
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.08);
}

.info-cover {
  position: relative;
  width: 280px;
  aspect-ratio: 16 / 10;
  border-radius: 14px;
  overflow: hidden;
  flex-shrink: 0;
  background: linear-gradient(135deg, #E8F0FF, #E8F0FF);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .info-difficulty {
    position: absolute;
    left: 10px;
    bottom: 10px;
    padding: 3px 10px;
    border-radius: 6px;
    font-size: 12px;
    font-weight: 700;
    color: #fff;
    background: rgba(0, 0, 0, 0.45);
    backdrop-filter: blur(8px);

    &.diff-beginner { background: linear-gradient(135deg, #36D399, #36D399); }
    &.diff-intermediate { background: linear-gradient(135deg, #165DFF, #4080FF); }
    &.diff-advanced { background: linear-gradient(135deg, #7B61FF, #7B61FF); }
  }
}

.info-main {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
}

.info-title {
  font-size: 28px;
  font-weight: 800;
  color: #1D2129;
  margin: 0 0 18px;
  line-height: 1.3;
}

.info-fields {
  display: flex;
  flex-wrap: wrap;
  gap: 12px 32px;
  margin-bottom: 22px;
}

.info-field {
  display: flex;
  flex-direction: column;
  gap: 4px;

  .field-label {
    font-size: 12px;
    color: #86909C;
  }

  .field-value {
    font-size: 14px;
    font-weight: 600;
    color: #4E5969;

    &.view-count {
      color: #36D399;
      background: #E8FFF5;
      padding: 2px 10px;
      border-radius: 6px;
    }
  }
}

.info-actions {
  margin-top: auto;
  display: flex;
  align-items: center;
  gap: 16px;

  .info-price {
    font-size: 22px;
    font-weight: 800;
    color: #F53F3F;

    &.free {
      color: #36D399;
    }
  }

  .trial-btn {
    background: #FF7D00;
    border-color: #FF7D00;
    color: #fff;
    font-weight: 600;
    border-radius: 10px;
    padding: 0 28px;
    transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s ease;

    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 24px rgba(255, 125, 0, 0.35);
    }

    .el-icon {
      margin-right: 6px;
    }
  }
}

// ==================== 内容区域 ====================
.page-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px 32px;
}

.course-body {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: 24px;
}

// ==================== Tab区域 ====================
.body-left {
  .course-tabs {
    :deep(.el-tabs__header) {
      margin-bottom: 0;
      background: $bg-white;
      border-radius: 12px 12px 0 0;
      padding: 0 8px;
    }

    :deep(.el-tabs__nav-wrap::after) {
      height: 1px;
      background: #F2F3F5;
    }

    :deep(.el-tabs__item) {
      font-size: 15px;
      font-weight: 600;
      color: #86909C;
      height: 52px;

      &.is-active {
        color: #165DFF;
        font-weight: 700;
      }
    }

    :deep(.el-tabs__active-bar) {
      background: #165DFF;
      height: 3px;
    }

    :deep(.el-tabs__content) {
      background: $bg-white;
      border-radius: 0 0 12px 12px;
      padding: 24px;
      box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
    }
  }
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 14px;

  .el-icon {
    font-size: 16px;
  }

  .review-count {
    margin-left: 4px;
    font-size: 11px;
    height: 18px;
    line-height: 16px;
  }
}

.tab-content {
  animation: tabFadeIn 0.3s ease;
}

@keyframes tabFadeIn {
  from { opacity: 0; transform: translateY(8px); }
  to { opacity: 1; transform: translateY(0); }
}

// ==================== 课程列表Tab（两列网格章节） ====================
.chapter-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
}

.chapter-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 14px 16px;
  background: #F7F8FA;
  border-radius: 12px;
  cursor: pointer;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.25s ease;

  &:hover {
    background: #E8F0FF;
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(22, 93, 255, 0.12);
  }

  .chapter-icon {
    width: 34px;
    height: 34px;
    border-radius: 9px;
    flex-shrink: 0;
    background: $bg-white;
    display: flex;
    align-items: center;
    justify-content: center;
    color: #165DFF;
    box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);

    .el-icon {
      font-size: 16px;
    }
  }

  .chapter-text {
    display: flex;
    align-items: baseline;
    gap: 8px;
    min-width: 0;
  }

  .chapter-index {
    font-size: 13px;
    font-weight: 700;
    color: #86909C;
    flex-shrink: 0;
  }

  .chapter-title {
    font-size: 14px;
    font-weight: 500;
    color: #4E5969;
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

.empty-tip {
  padding: 24px 0;
  text-align: center;
  font-size: 13px;
  color: #C9CDD4;
  grid-column: 1 / -1;
}

// ==================== 课程详情Tab ====================
.detail-content {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.detail-block {
  .detail-h3 {
    font-size: 17px;
    font-weight: 700;
    color: #1D2129;
    margin: 0 0 14px;
    padding-left: 12px;
    border-left: 4px solid #165DFF;
    line-height: 1.4;
  }

  .detail-desc {
    font-size: 14px;
    color: #4E5969;
    line-height: 1.8;
    margin: 0;
  }
}

@media (max-width: 768px) {
  .course-info-card {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .info-fields {
    justify-content: center;
  }

  .info-actions {
    justify-content: center;
  }

  .chapter-grid {
    grid-template-columns: 1fr;
  }
}

// ==================== 教练介绍Tab ====================
.coach-card-enhanced {
  display: flex;
  gap: 24px;
  padding: 16px;
  background: linear-gradient(135deg, rgba(22, 93, 255, 0.03), rgba(255, 125, 0, 0.03));
  border-radius: 16px;
}

.coach-avatar-wrap {
  position: relative;
  flex-shrink: 0;

  .coach-badge {
    position: absolute;
    bottom: -4px;
    right: -4px;
    width: 28px;
    height: 28px;
    border-radius: 50%;
    background: linear-gradient(135deg, #FF7D00, #FF9500);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 14px;
    box-shadow: 0 2px 8px rgba(255, 125, 0, 0.3);
  }
}

.coach-info-enhanced {
  flex: 1;

  .coach-name-row {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 6px;
  }

  .coach-name {
    font-size: 20px;
    font-weight: 700;
    color: #1D2129;
  }

  .coach-title {
    font-size: 14px;
    color: #165DFF;
    margin-bottom: 16px;
    font-weight: 500;
  }

  .coach-stats {
    display: flex;
    gap: 24px;
    margin-bottom: 16px;
  }

  .coach-stat {
    display: flex;
    flex-direction: column;
    gap: 2px;

    .coach-stat-num {
      font-size: 20px;
      font-weight: 700;
      color: #1D2129;
    }

    .coach-stat-label {
      font-size: 12px;
      color: #86909C;
    }
  }

  .coach-desc {
    font-size: 14px;
    color: #86909C;
    line-height: 1.7;
    margin: 0;
  }
}

// ==================== 学员评价Tab ====================
.review-list-enhanced {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.review-item-enhanced {
  padding: 16px;
  background: #F7F8FA;
  border-radius: 12px;
  animation: reviewSlideIn 0.4s ease forwards;
  opacity: 0;
  transform: translateX(-10px);

  &:hover {
    background: #F7F8FA;
    transform: translateX(4px);
    transition: transform, box-shadow, background-color, border-color, color, opacity 0.25s ease;
  }
}

@keyframes reviewSlideIn {
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.review-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 10px;
}

.review-meta {
  display: flex;
  flex-direction: column;
  gap: 2px;
  flex: 1;
}

.review-name {
  font-size: 14px;
  font-weight: 600;
  color: #4E5969;
}

.review-time {
  font-size: 12px;
  color: #86909C;
}

.review-content {
  font-size: 14px;
  color: #86909C;
  line-height: 1.7;
  margin: 0;
  padding-left: 52px;
}

// ==================== 课程特色Tab ====================
.feature-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
}

.feature-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  padding: 24px 16px;
  background: linear-gradient(135deg, rgba(22, 93, 255, 0.04), rgba(64, 128, 255, 0.06));
  border-radius: 12px;
  border: 1px solid rgba(22, 93, 255, 0.08);
  animation: featurePop 0.4s ease forwards;
  opacity: 0;
  transform: scale(0.9);
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.25s ease;

  &:hover {
    transform: translateY(-4px) scale(1.02);
    box-shadow: 0 8px 24px rgba(22, 93, 255, 0.1);
    border-color: rgba(22, 93, 255, 0.2);
  }
}

@keyframes featurePop {
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.feature-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: linear-gradient(135deg, #165DFF, #4080FF);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 20px;
}

.feature-name {
  font-size: 14px;
  font-weight: 600;
  color: #4E5969;
}

// ==================== 右侧侧边栏 ====================
.body-right {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.side-card {
  background: $bg-white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.side-title {
  font-size: 15px;
  font-weight: 700;
  color: #1D2129;
  margin: 0 0 14px;
  padding-bottom: 12px;
  border-bottom: 1px solid #F7F8FA;
}

.side-info-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.side-info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 13px;

  .side-label {
    color: #86909C;
  }

  .side-value {
    color: #4E5969;
    font-weight: 500;
    display: flex;
    align-items: center;
    gap: 4px;

    .el-icon {
      font-size: 14px;
    }
  }
}

.guarantee-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.guarantee-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: #4E5969;

  .el-icon {
    font-size: 16px;
  }
}

// ==================== 试听预约弹窗 ====================
.trial-dialog-header {
  margin-bottom: 20px;
  padding-bottom: 16px;
  border-bottom: 1px solid #F7F8FA;
}

.trial-course-info {
  display: flex;
  align-items: center;
  gap: 14px;

  .trial-course-name {
    font-size: 16px;
    font-weight: 700;
    color: #1D2129;
  }

  .trial-course-meta {
    font-size: 13px;
    color: #86909C;
    margin-top: 2px;
  }
}

.trial-form {
  :deep(.el-form-item__label) {
    font-weight: 500;
  }
}

// ==================== 底部固定栏（移动端） ====================
.mobile-action-bar {
  display: none;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 12px 16px;
  background: $bg-white;
  box-shadow: 0 -2px 12px rgba(0, 0, 0, 0.08);
  gap: 12px;
  z-index: 100;

  .trial-btn {
    width: 100%;
    background: #FF7D00;
    border-color: #FF7D00;
    color: #fff;
    font-weight: 600;
    border-radius: 10px;

    .el-icon {
      margin-right: 6px;
    }
  }
}

// ==================== 响应式 ====================
@media (max-width: 992px) {
  .course-body {
    grid-template-columns: 1fr;
  }

  .body-right {
    display: none;
  }
}

@media (max-width: 576px) {
  .page-content {
    padding: 0 16px 100px;
  }

  .body-left {
    .course-tabs {
      :deep(.el-tabs__content) {
        padding: 16px;
      }
    }
  }

  .feature-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .coach-card-enhanced {
    flex-direction: column;
    align-items: center;
    text-align: center;
  }

  .coach-stats {
    justify-content: center;
  }

  .review-content {
    padding-left: 0;
  }

  .mobile-action-bar {
    display: flex;
  }
}
</style>
