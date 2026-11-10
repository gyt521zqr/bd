<template>
  <!-- 课程中心列表：分类筛选/搜索/排序 -->
  <div class="course-list-page">
    <!-- Hero 区域 -->
    <div class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">课程中心</h1>
        <p class="hero-subtitle">从零基础到竞赛进阶，体系化编程课程，名师授课、项目驱动，让孩子学以致用</p>
      </div>
      <div class="hero-decoration">
        <div class="hero-circle hero-circle-1"></div>
        <div class="hero-circle hero-circle-2"></div>
        <div class="hero-circle hero-circle-3"></div>
      </div>
    </div>

    <div class="page-container">
      <!-- 搜索和筛选 -->
      <div class="filter-bar">
        <div class="search-box">
          <div class="search-input-wrapper">
            <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
              <circle cx="11" cy="11" r="8"/>
              <path d="m21 21-4.3-4.3"/>
            </svg>
            <el-input
              v-model="keyword"
              placeholder="搜索课程名称、教师..."
              clearable
              size="large"
              @keyup.enter="handleSearch"
              class="search-input"
            />
          </div>
        </div>

        <div class="filter-scroll">
          <div class="filter-tags">
            <span class="filter-label">年龄段：</span>
            <div class="tag-scroll">
              <div
                v-for="cat in categories"
                :key="cat.id"
                :class="['age-card', { active: selectedCategory === cat.id }]"
                @click="selectedCategory = cat.id"
              >
                <span class="age-icon">{{ getAgeIcon(cat.id) }}</span>
                <span class="age-name">{{ cat.name }}</span>
              </div>
            </div>
          </div>
          <div class="filter-tags">
            <span class="filter-label">难度：</span>
            <div class="tag-scroll">
              <span
                v-for="d in difficulties"
                :key="d.value"
                :class="['difficulty-pill', `difficulty-${d.value || 'all'}`, { active: selectedDifficulty === d.value }]"
                @click="selectedDifficulty = d.value"
              >
                {{ d.label }}
              </span>
            </div>
          </div>
        </div>

        <div class="filter-row">
          <div class="sort-options">
            <span class="filter-label">排序：</span>
            <div class="sort-buttons">
              <button
                v-for="opt in sortOptions"
                :key="opt.value"
                :class="['sort-btn', { active: sortBy === opt.value }]"
                @click="sortBy = opt.value"
              >
                <span class="sort-icon">{{ opt.icon }}</span>
                {{ opt.label }}
              </button>
            </div>
          </div>
          <div class="course-count">
            共 <span class="count-num">{{ total || courseList.length }}</span> 门课程
          </div>
        </div>
      </div>

      <!-- 课程列表 -->
      <div class="course-grid" v-if="courseList.length > 0">
        <CourseCard
          v-for="(course, index) in courseList"
          :key="course.id"
          :course="course"
          :style="{ animationDelay: `${index * 0.06}s` }"
          class="grid-item"
        />
      </div>

      <!-- 空状态 -->
      <div class="empty-state" v-if="courseList.length === 0">
        <div class="empty-illustration">
          <svg viewBox="0 0 200 160" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect x="40" y="20" width="120" height="90" rx="8" fill="#E8F0FF" stroke="#165DFF" stroke-width="1.5" stroke-dasharray="6 3"/>
            <rect x="55" y="35" width="60" height="6" rx="3" fill="#165DFF" opacity="0.3"/>
            <rect x="55" y="48" width="40" height="6" rx="3" fill="#165DFF" opacity="0.2"/>
            <rect x="55" y="61" width="50" height="6" rx="3" fill="#165DFF" opacity="0.15"/>
            <circle cx="140" cy="45" r="15" fill="#FF7D00" opacity="0.15"/>
            <path d="M135 45 L139 49 L146 41" stroke="#FF7D00" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            <rect x="60" y="120" width="80" height="8" rx="4" fill="#C9CDD4"/>
            <rect x="75" y="135" width="50" height="6" rx="3" fill="#E5E6EB"/>
          </svg>
        </div>
        <h3 class="empty-title">暂无匹配的课程</h3>
        <p class="empty-desc">换个关键词试试，或浏览全部课程</p>
        <button class="empty-btn" @click="resetFilters">查看全部课程</button>
      </div>

      <Pagination :total="total" v-model:page="pageNum" v-model:page-size="pageSize" @change="fetchCourses" />
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * CourseListPage - 课程中心列表
 * 支持分类筛选、关键词搜索、难度过滤、排序
 */
import { ref, onMounted } from 'vue'
import CourseCard from '@/components/course/CourseCard.vue'
import Pagination from '@/components/common/Pagination.vue'
import { getCourseList, getCourseCategories } from '@/api/course'

const keyword = ref('')
const selectedCategory = ref<number | string>('')
const selectedDifficulty = ref('')
const sortBy = ref('latest')
const pageNum = ref(1)
const pageSize = ref(12)
const total = ref(0)

/**
 * 课程筛选标签（前端 UI 维度）
 * 注意：这里的 dimension='age' 仅表示「年龄段分组」，不是后端 cms_category.type
 * （后者是 course/news/practice 业务域）。两者语义已拆分，不再互相借用。
 */
interface CourseCategoryTab {
  id: number
  name: string
  icon?: string
  ageRange?: string
  sortOrder: number
  status: 'enabled'
  dimension: 'age'
}

// 默认分类数据（降级方案）
const defaultCategories: CourseCategoryTab[] = [
  { id: 0, name: '全部', icon: '🌐', sortOrder: 0, status: 'enabled', dimension: 'age' },
  { id: 1, name: 'ScratchJr（5-7岁）', icon: '🧒', ageRange: '5-7岁', sortOrder: 1, status: 'enabled', dimension: 'age' },
  { id: 2, name: 'Scratch（8-10岁）', icon: '🎮', ageRange: '8-10岁', sortOrder: 2, status: 'enabled', dimension: 'age' },
  { id: 3, name: 'GoC（9-11岁）', icon: '🐢', ageRange: '9-11岁', sortOrder: 3, status: 'enabled', dimension: 'age' },
  { id: 4, name: 'Python（11-13岁）', icon: '🐍', ageRange: '11-13岁', sortOrder: 4, status: 'enabled', dimension: 'age' },
  { id: 5, name: 'C++（14-16岁）', icon: '🏆', ageRange: '14-16岁', sortOrder: 5, status: 'enabled', dimension: 'age' },
]

const categories = ref<CourseCategoryTab[]>(defaultCategories)

// 默认难度数据（降级方案）
const defaultDifficulties = [
  { value: '', label: '全部' },
  { value: 'beginner', label: '入门' },
  { value: 'intermediate', label: '进阶' },
  { value: 'advanced', label: '高级' },
]

const difficulties = ref(defaultDifficulties)

const sortOptions = ref([
  { value: 'latest', label: '最新', icon: '🕐' },
  { value: 'popular', label: '最热', icon: '🔥' },
  { value: 'rating', label: '评分最高', icon: '⭐' },
])

const courseList = ref([
  { id: 1, title: 'Python基础入门', cover: '/images/course/course-1.png', difficulty: 'beginner', teacherName: '张老师', studentCount: 1256, rating: 4.8, price: 0 },
  { id: 2, title: 'C++算法竞赛', cover: '/images/course/course-2.png', difficulty: 'advanced', teacherName: '李老师', studentCount: 856, rating: 4.9, price: 0 },
  { id: 3, title: 'Scratch趣味编程', cover: '/images/course/course-3.png', difficulty: 'beginner', teacherName: '王老师', studentCount: 2100, rating: 4.7, price: 0 },
  { id: 4, title: 'GoC图形编程', cover: '/images/course/course-4.png', difficulty: 'intermediate', teacherName: '赵老师', studentCount: 680, rating: 4.6, price: 0 },
  { id: 5, title: 'Python数据分析', cover: '/images/course/course-5.png', difficulty: 'intermediate', teacherName: '刘老师', studentCount: 520, rating: 4.5, price: 0 },
  { id: 6, title: 'C++数据结构', cover: '/images/course/course-7.png', difficulty: 'advanced', teacherName: '陈老师', studentCount: 430, rating: 4.8, price: 0 },
])

function getAgeIcon(id: number | string) {
  const cat = categories.value.find(c => String(c.id) === String(id))
  if (cat) return cat.icon
  const map: Record<string, string> = {
    '': '🌐',
    '0': '🌐',
    '1': '🧒',
    '2': '🎮',
    '3': '🐢',
    '4': '🐍',
    '5': '🏆',
  }
  return map[String(id)] || '📚'
}

function resetFilters() {
  keyword.value = ''
  selectedCategory.value = ''
  selectedDifficulty.value = ''
  sortBy.value = 'latest'
  pageNum.value = 1
  fetchCourses()
}

function handleSearch() { pageNum.value = 1; fetchCourses() }
async function fetchCourses() {
  try {
    const res = await getCourseList({
      keyword: keyword.value,
      categoryId: selectedCategory.value === '' ? undefined : Number(selectedCategory.value),
      difficulty: selectedDifficulty.value,
      sortBy: sortBy.value,
      pageNum: pageNum.value,
      pageSize: pageSize.value,
    } as any)
    if (res?.data) {
      const list = res.data.list || (res.data as any).records || []
      if (list.length > 0) {
        courseList.value = list.map((c: any) => ({
          ...c,
          cover: c.cover || `/images/course/course-${((Number(c.id) - 1) % 11) + 1}.png`,
        }))
        total.value = res.data.total || 0
      }
    }
  } catch {
    // 首次加载失败使用默认数据，不弹错误提示
  }
}

// 加载分类数据
async function loadCategories() {
  // 年龄段分类：来自课程模块自身的分类接口（/api/course/categories）
  try {
    const courseCatRes = await getCourseCategories()
    if (courseCatRes?.code === 200 && courseCatRes.data && courseCatRes.data.length > 0) {
      categories.value = [
        { id: 0, name: '全部', icon: '🌐', sortOrder: 0, status: 'enabled', dimension: 'age' },
        ...courseCatRes.data.map((item: { id: number; name: string; icon: string }) => ({
          id: item.id,
          name: item.name,
          icon: item.icon,
          sortOrder: 0,
          status: 'enabled' as const,
          dimension: 'age' as const,
        })),
      ]
    }
  } catch {
    // 使用默认年龄段数据，不弹错误提示
  }

  // 难度是课程固有枚举（beginner/intermediate/advanced），固定为前端本地维度。
  // 之前用 getCategories({type:'difficulty'}) 查询 cms_category 恒为空（后端无此业务域），已移除。
  difficulties.value = [...defaultDifficulties]
}

onMounted(() => {
  fetchCourses()
  loadCategories()
})
</script>

<style scoped lang="scss">

.course-list-page {
  background: $bg-page;
  min-height: 100vh;
  padding-bottom: 60px;
}

// ==================== Hero 区域 ====================
.hero-section {
  background: linear-gradient(135deg, #165DFF 0%, #7B61FF 100%);
  padding: 56px 24px 48px;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 360px;
  box-sizing: border-box;
}

.hero-content {
  text-align: center;
  position: relative;
  z-index: 2;
}

.hero-title {
  font-size: 40px;
  font-weight: 800;
  color: #fff;
  margin: 0 0 12px;
  letter-spacing: 2px;
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.15);
}

.hero-subtitle {
  font-size: 16px;
  color: rgba(255, 255, 255, 0.88);
  margin: 0;
  max-width: 600px;
  line-height: 1.7;
  font-weight: 400;
}

.hero-decoration {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 1;
}

.hero-circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.06);
}

.hero-circle-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  right: -50px;
}

.hero-circle-2 {
  width: 200px;
  height: 200px;
  bottom: -60px;
  left: -30px;
}

.hero-circle-3 {
  width: 120px;
  height: 120px;
  top: 20px;
  left: 20%;
  background: rgba(255, 255, 255, 0.04);
}

// ==================== 页面容器 ====================
.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 24px 0;
}

// ==================== 筛选栏 ====================
.filter-bar {
  background: $bg-white;
  border-radius: 16px;
  padding: 24px;
  margin-bottom: 28px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  margin-top: 24px;
  position: relative;
  z-index: 10;
}

.search-box {
  margin-bottom: 20px;
}

.search-input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}

.search-icon {
  position: absolute;
  left: 16px;
  width: 20px;
  height: 20px;
  color: $text-muted;
  z-index: 2;
  pointer-events: none;
}

.search-input {
  :deep(.el-input__wrapper) {
    border-radius: 12px;
    padding-left: 44px;
    box-shadow: 0 0 0 1px #E5E6EB inset;
    height: 48px;
    font-size: 15px;
    transition: box-shadow 0.3s ease;

    &:hover {
      box-shadow: 0 0 0 1px $primary-light inset;
    }

    &.is-focus {
      box-shadow: 0 0 0 2px $primary-color inset;
    }
  }

  :deep(.el-input__inner) {
    height: 48px;
    font-size: 15px;
  }

  :deep(.el-input__prefix) {
    display: none;
  }
}

.filter-scroll {
  margin-bottom: 16px;
}

.filter-tags {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;

  &:last-child {
    margin-bottom: 0;
  }
}

.filter-label {
  font-size: 14px;
  color: #86909C;
  margin-right: 4px;
  white-space: nowrap;
  flex-shrink: 0;
  font-weight: 500;
}

.tag-scroll {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

// 年龄段卡片式选择器
.age-card {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 14px;
  border-radius: 10px;
  border: 1.5px solid #E5E6EB;
  background: $bg-white;
  cursor: pointer;
  transition: border-color 0.25s ease, background 0.25s ease, color 0.25s ease, box-shadow 0.25s ease;
  font-size: 13px;
  color: $text-body;
  white-space: nowrap;

  &:hover {
    border-color: $primary-light;
    background: #E8F0FF;
  }

  &.active {
    border-color: $primary-color;
    background: linear-gradient(135deg, #E8F0FF 0%, #E8F0FF 100%);
    color: $primary-color;
    box-shadow: 0 2px 8px rgba($primary-color, 0.15);
    font-weight: 600;
  }
}

.age-icon {
  font-size: 16px;
  line-height: 1;
}

.age-name {
  line-height: 1;
}

// 难度彩色胶囊
.difficulty-pill {
  display: inline-flex;
  align-items: center;
  padding: 6px 16px;
  border-radius: 20px;
  font-size: 13px;
  cursor: pointer;
  transition: background 0.25s ease, color 0.25s ease, box-shadow 0.25s ease;
  border: none;
  font-weight: 500;
  white-space: nowrap;

  &.difficulty-all {
    background: #F2F3F5;
    color: $text-body;
    &:hover { background: #E5E6EB; }
    &.active {
      background: linear-gradient(135deg, #165DFF, #4080FF);
      color: #fff;
      box-shadow: 0 2px 8px rgba($primary-color, 0.3);
    }
  }

  &.difficulty-beginner {
    background: #E8FFF5;
    color: #36D399;
    &:hover { background: #E8FFF5; }
    &.active {
      background: linear-gradient(135deg, #36D399, #36D399);
      color: #fff;
      box-shadow: 0 2px 8px rgba(#36D399, 0.3);
    }
  }

  &.difficulty-intermediate {
    background: #E8F0FF;
    color: $primary-color;
    &:hover { background: #E8F0FF; }
    &.active {
      background: linear-gradient(135deg, #165DFF, #7B61FF);
      color: #fff;
      box-shadow: 0 2px 8px rgba($primary-color, 0.3);
    }
  }

  &.difficulty-advanced {
    background: #E8F0FF;
    color: #7B61FF;
    &:hover { background: #E8F0FF; }
    &.active {
      background: linear-gradient(135deg, #7B61FF, #7B61FF);
      color: #fff;
      box-shadow: 0 2px 8px rgba(#7B61FF, 0.3);
    }
  }
}

// 排序行
.filter-row {
  margin-top: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-top: 16px;
  border-top: 1px solid #F2F3F5;
}

.sort-options {
  display: flex;
  align-items: center;
  gap: 8px;
}

.sort-buttons {
  display: flex;
  gap: 6px;
}

.sort-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  border-radius: 8px;
  border: 1.5px solid #E5E6EB;
  background: $bg-white;
  cursor: pointer;
  font-size: 13px;
  color: $text-body;
  transition: border-color 0.25s ease, background 0.25s ease, color 0.25s ease;

  &:hover {
    border-color: $primary-light;
    color: $primary-color;
  }

  &.active {
    border-color: $primary-color;
    background: #E8F0FF;
    color: $primary-color;
    font-weight: 600;
  }

  .sort-icon {
    font-size: 14px;
  }
}

.course-count {
  font-size: 13px;
  color: $text-muted;

  .count-num {
    color: $primary-color;
    font-weight: 700;
    font-size: 16px;
  }
}

// ==================== 课程网格 ====================
.course-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  margin-bottom: 32px;
}

.grid-item {
  animation: fadeInUp 0.5s ease both;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(24px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// ==================== 空状态 ====================
.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.empty-illustration {
  margin-bottom: 20px;

  svg {
    width: 200px;
    height: 160px;
  }
}

.empty-title {
  font-size: 18px;
  color: $text-title;
  margin: 0 0 8px;
  font-weight: 600;
}

.empty-desc {
  font-size: 14px;
  color: $text-muted;
  margin: 0 0 24px;
}

.empty-btn {
  display: inline-flex;
  align-items: center;
  padding: 10px 28px;
  border-radius: 10px;
  border: none;
  background: linear-gradient(135deg, $primary-color, $primary-light);
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  box-shadow: 0 4px 12px rgba($primary-color, 0.3);

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba($primary-color, 0.4);
  }
}

// ==================== 响应式 ====================
@media (max-width: 1200px) {
  .page-container {
    padding: 24px 24px 0;
  }
  .course-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .hero-section {
    padding: 40px 16px 36px;
  }

  .hero-title {
    font-size: 28px;
  }

  .hero-subtitle {
    font-size: 14px;
  }

  .course-list-page {
    padding-bottom: 40px;
  }

  .page-container {
    padding: 16px 16px 0;
  }

  .filter-bar {
    padding: 16px;
    margin-top: -20px;
  }

  .search-input-wrapper {
    :deep(.el-input__wrapper) {
      height: 42px;
    }
  }

  .tag-scroll {
    overflow-x: auto;
    flex-wrap: nowrap;
    gap: 6px;
    padding-bottom: 4px;
    scrollbar-width: none;
    &::-webkit-scrollbar { display: none; }
  }

  .age-card,
  .difficulty-pill {
    flex-shrink: 0;
  }

  .course-grid {
    grid-template-columns: 1fr;
    gap: 16px;
  }

  .filter-row {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
}
</style>
