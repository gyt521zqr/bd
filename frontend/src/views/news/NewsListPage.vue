<template>
  <!-- 资讯列表：顶部Banner + 分类标签 + 公告列表 -->
  <div class="news-list-page">
    <!-- 顶部 Banner - 科技蓝风格 -->
    <section class="news-banner">
      <div class="banner-inner">
        <div class="banner-left-deco">
          <div class="tech-line tech-line-1"></div>
          <div class="tech-line tech-line-2"></div>
          <div class="tech-line tech-line-3"></div>
          <div class="tech-hexagon">
            <svg viewBox="0 0 100 100" class="hex-svg">
              <polygon points="50,5 90,25 90,75 50,95 10,75 10,25" fill="none" stroke="rgba(100,200,255,0.3)" stroke-width="1.5"/>
              <polygon points="50,20 75,35 75,65 50,80 25,65 25,35" fill="none" stroke="rgba(100,200,255,0.2)" stroke-width="1"/>
            </svg>
          </div>
          <div class="tech-dots">
            <span v-for="i in 12" :key="i" class="dot" :style="{ animationDelay: `${i * 0.15}s` }"></span>
          </div>
        </div>
        <div class="banner-content">
          <div class="banner-badge">码高教育 · 资讯中心</div>
          <h2>最新资讯</h2>
          <p>掌握编程教育前沿动态，了解赛事公告与平台资讯</p>
        </div>
        <div class="banner-right-deco">
          <div class="deco-ai">AI</div>
          <div class="deco-circuit">
            <svg viewBox="0 0 200 200" class="circuit-svg">
              <circle cx="100" cy="100" r="80" fill="none" stroke="rgba(100,200,255,0.15)" stroke-width="1" stroke-dasharray="8 4"/>
              <circle cx="100" cy="100" r="55" fill="none" stroke="rgba(100,200,255,0.2)" stroke-width="1"/>
              <circle cx="100" cy="100" r="30" fill="none" stroke="rgba(100,200,255,0.25)" stroke-width="1.5"/>
              <circle cx="100" cy="100" r="8" fill="rgba(100,200,255,0.3)"/>
              <line x1="100" y1="20" x2="100" y2="45" stroke="rgba(100,200,255,0.3)" stroke-width="1"/>
              <line x1="100" y1="155" x2="100" y2="180" stroke="rgba(100,200,255,0.3)" stroke-width="1"/>
              <line x1="20" y1="100" x2="45" y2="100" stroke="rgba(100,200,255,0.3)" stroke-width="1"/>
              <line x1="155" y1="100" x2="180" y2="100" stroke="rgba(100,200,255,0.3)" stroke-width="1"/>
              <circle cx="100" cy="20" r="3" fill="rgba(100,200,255,0.5)"/>
              <circle cx="100" cy="180" r="3" fill="rgba(100,200,255,0.5)"/>
              <circle cx="20" cy="100" r="3" fill="rgba(100,200,255,0.5)"/>
              <circle cx="180" cy="100" r="3" fill="rgba(100,200,255,0.5)"/>
            </svg>
          </div>
          <div class="deco-blocks">
            <div class="block block-1"></div>
            <div class="block block-2"></div>
            <div class="block block-3"></div>
          </div>
        </div>
        <div class="banner-grid-bg"></div>
      </div>
    </section>

    <!-- 内容区域 -->
    <div class="news-content">
      <!-- 分类标签 -->
      <section class="category-bar">
        <div class="category-tabs">
          <div
            class="category-tab"
            v-for="cat in categories"
            :key="cat.key"
            :class="{ active: activeCategory === cat.key }"
            @click="activeCategory = cat.key"
          >
            {{ cat.label }}
          </div>
        </div>
      </section>

      <!-- 公告列表 - 卡片式 -->
      <section class="news-list-section">
        <div class="news-grid" v-if="filteredNews.length">
          <div
            class="news-card"
            v-for="(news, idx) in filteredNews"
            :key="idx"
            :class="{ 'is-pinned': news.isPinned, 'is-urgent': news.isUrgent }"
            @click="openDetail(news)"
            :style="{ animationDelay: `${idx * 0.05}s` }"
          >
            <!-- 图片区域 -->
            <div class="news-image">
              <div class="image-placeholder" :style="{ background: getCategoryColor(news.category) }">
                <el-icon :size="40" color="rgba(255,255,255,0.8)"><component :is="getCategoryIcon(news.category)" /></el-icon>
              </div>
              <!-- 日期标签 -->
              <div class="date-badge">
                <span class="date-day">{{ getDay(news.time) }}</span>
                <span class="date-month">{{ getMonth(news.time) }}月</span>
              </div>
              <!-- 分类标签 -->
              <div class="category-badge" :style="{ background: getCategoryColor(news.category) }">
                {{ news.categoryLabel }}
              </div>
            </div>

            <div class="news-body">
              <div class="news-badge-group">
                <el-tag v-if="news.isPinned" type="danger" size="small" effect="dark" class="pin-badge">置顶</el-tag>
                <el-tag v-if="news.isUrgent" type="warning" size="small" effect="dark" class="urgent-badge">紧急</el-tag>
              </div>
              <h4 class="news-title">{{ news.title }}</h4>
              <p class="news-summary">{{ news.summary }}</p>
              <div class="news-footer">
                <span class="news-time">
                  <el-icon size="13"><Timer /></el-icon>
                  {{ news.time }}
                </span>
                <span class="news-views" v-if="news.views > 0">
                  <el-icon size="13"><View /></el-icon>
                  {{ formatViews(news.views) }}
                </span>
              </div>
            </div>
          </div>
        </div>
        <div class="empty-state" v-else>
          <el-icon :size="48" color="#C9CDD4"><Document /></el-icon>
          <p>暂无公告</p>
        </div>
      </section>
    </div>

    <!-- 公告详情弹窗 -->
    <el-dialog
      v-model="detailVisible"
      :title="currentNews?.title"
      width="90%"
      style="max-width: 680px;"
      class="news-detail-dialog"
      destroy-on-close
    >
      <div class="news-detail" v-if="currentNews">
        <div class="detail-meta">
          <el-tag v-if="currentNews.isPinned" type="danger" size="small" effect="dark">置顶</el-tag>
          <el-tag v-if="currentNews.isUrgent" type="warning" size="small" effect="dark">紧急</el-tag>
          <span class="detail-time">{{ currentNews.time }}</span>
          <span class="detail-views" v-if="currentNews.views > 0">阅读 {{ currentNews.views }}</span>
        </div>
        <div class="detail-body" v-html="currentNews.content"></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * NewsListPage - 资讯列表
 * 顶部科技蓝Banner + 分类筛选 + 公告卡片 + 详情弹窗
 */
import { ref, computed, onMounted } from 'vue'
import { Timer, View, Document } from '@element-plus/icons-vue'
import { getNewsList, type NewsItem } from '@/api/news'

// 分类（动态 + 全部）
const categoryMap: Record<string, { label: string; color: string; icon: string }> = {
  industry: { label: '行业动态', color: '#165DFF', icon: 'Bell' },
  education: { label: '教育资讯', color: '#36D399', icon: 'Reading' },
  contest: { label: '赛事新闻', color: '#FF7D00', icon: 'Trophy' },
  platform: { label: '平台公告', color: '#F53F3F', icon: 'Bell' },
}

const categories = ref([
  { key: 'all', label: '全部' },
  ...Object.entries(categoryMap).map(([key, val]) => ({ key, label: val.label })),
])
const activeCategory = ref('all')

function getCategoryColor(category: string) {
  return categoryMap[category]?.color || '#165DFF'
}

function getCategoryIcon(category: string) {
  return categoryMap[category]?.icon || 'Bell'
}

function getDay(time: string) {
  return time.split('-')[2] || ''
}

function getMonth(time: string) {
  return parseInt(time.split('-')[1] || '1')
}

function formatViews(views: number) {
  if (views >= 10000) {
    return (views / 10000).toFixed(1) + '万'
  }
  return views.toString()
}

// 页面展示用的资讯卡片
interface NewsCard {
  title: string
  summary: string
  content: string
  category: string
  categoryLabel: string
  time: string
  views: number
  isPinned: boolean
  isUrgent: boolean
}

// API 数据：不再内置虚构内容，接口无数据时保持空态
const newsList = ref<NewsCard[]>([])

// 转换 API 数据为页面格式（接口暂不返回浏览量，views 置 0，模板隐藏 0 值展示）
function transformNewsItem(item: NewsItem): NewsCard {
  const mapInfo = categoryMap[item.category] || { label: item.category, color: '#165DFF', icon: 'Bell' }
  return {
    title: item.title,
    summary: item.content.replace(/<[^>]+>/g, '').slice(0, 120) + '...',
    content: item.content,
    category: item.category,
    categoryLabel: mapInfo.label,
    time: item.publishTime ? item.publishTime.split(' ')[0] : item.createTime.split(' ')[0],
    views: 0,
    isPinned: item.isPinned,
    isUrgent: false,
  }
}

// 加载数据
async function loadNews() {
  try {
    const res: any = await getNewsList({ page: 1, size: 50 })
    if (res && res.code === 200 && res.data) {
      const data = res.data.list || res.data.records || res.data
      if (Array.isArray(data) && data.length > 0) {
        newsList.value = data.map(transformNewsItem)
      }
    }
  } catch {
    // 接口无数据或失败：保持空态，由模板显示「暂无数据」
  }
}

onMounted(() => {
  loadNews()
})

// 筛选后的公告
const filteredNews = computed(() => {
  if (activeCategory.value === 'all') return newsList.value
  return newsList.value.filter(n => n.category === activeCategory.value)
})

// 详情弹窗
const detailVisible = ref(false)
const currentNews = ref<any>(null)

function openDetail(news: any) {
  currentNews.value = news
  detailVisible.value = true
}
</script>

<style scoped lang="scss">
$primary: #165DFF;
$cyan: #36D399;
$text-title: #1D2129;
$text-body: #4E5969;
$text-muted: #86909C;
$bg-page: #F7F8FA;
$content-max-width: 1200px;

.news-list-page {
  background: $bg-page;
  min-height: 100vh;
}

// ==================== 顶部Banner ====================
.news-banner {
  width: 100%;
  overflow: hidden;
  border-radius: 0 0 16px 16px;
  box-shadow: 0 8px 24px rgba(22, 93, 255, 0.08);
}

.banner-inner {
  height: 280px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #fff;
  position: relative;
  overflow: hidden;
  padding: 0 60px;
  background: linear-gradient(135deg, #0D1117 0%, #0D1117 40%, #0D1117 70%, #0D1117 100%);
}

.banner-grid-bg {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(rgba(100, 200, 255, 0.03) 1px, transparent 1px),
    linear-gradient(90deg, rgba(100, 200, 255, 0.03) 1px, transparent 1px);
  background-size: 50px 50px;
  pointer-events: none;
}

.banner-left-deco {
  position: relative;
  width: 200px;
  height: 280px;
  flex-shrink: 0;
  pointer-events: none;
}

.tech-line {
  position: absolute;
  left: 0;
  width: 60px;
  height: 3px;
  background: linear-gradient(90deg, rgba(100, 200, 255, 0.6), transparent);
  border-radius: 2px;
  &.tech-line-1 { top: 40px; width: 80px; }
  &.tech-line-2 { top: 70px; width: 50px; }
  &.tech-line-3 { top: 100px; width: 65px; }
}

.tech-hexagon {
  position: absolute;
  top: 50%;
  left: 20px;
  transform: translateY(-50%);
  .hex-svg {
    width: 120px;
    height: 120px;
    animation: hexRotate 20s linear infinite;
    will-change: transform, opacity;
  }
}

@keyframes hexRotate {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.tech-dots {
  position: absolute;
  bottom: 30px;
  left: 10px;
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 10px;
  .dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    background: rgba(100, 200, 255, 0.4);
    animation: dotPulse 2s ease-in-out infinite;
    will-change: transform, opacity;
  }
}

@keyframes dotPulse {
  0%, 100% { opacity: 0.3; transform: scale(1); }
  50% { opacity: 0.8; transform: scale(1.3); }
}

.banner-right-deco {
  position: relative;
  width: 220px;
  height: 280px;
  flex-shrink: 0;
  pointer-events: none;
}

.deco-ai {
  position: absolute;
  top: 20px;
  right: 0;
  font-size: 64px;
  font-weight: 900;
  color: rgba(100, 200, 255, 0.08);
  letter-spacing: 4px;
}

.deco-circuit {
  position: absolute;
  top: 50%;
  right: 10px;
  transform: translateY(-50%);
  .circuit-svg {
    width: 160px;
    height: 160px;
    animation: circuitSpin 30s linear infinite;
    will-change: transform, opacity;
  }
}

@keyframes circuitSpin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.deco-blocks {
  position: absolute;
  bottom: 20px;
  right: 30px;
  .block {
    position: absolute;
    border-radius: 4px;
    background: rgba(100, 200, 255, 0.15);
    &.block-1 { width: 40px; height: 40px; bottom: 0; right: 0; }
    &.block-2 { width: 25px; height: 25px; bottom: 50px; right: 30px; }
    &.block-3 { width: 18px; height: 18px; bottom: 20px; right: 55px; }
  }
}

.banner-content {
  text-align: left;
  position: relative;
  z-index: 2;
  flex: 1;
  max-width: 640px;
  padding: 0 40px;

  .banner-badge {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 6px 16px;
    background: rgba(100, 200, 255, 0.12);
    border: 1px solid rgba(100, 200, 255, 0.25);
    border-radius: 20px;
    font-size: 13px;
    color: rgba(150, 220, 255, 0.9);
    margin-bottom: 16px;
    letter-spacing: 0.5px;
  }

  h2 {
    font-size: 32px;
    font-weight: 800;
    margin-bottom: 14px;
    letter-spacing: 1px;
    line-height: 1.3;
    white-space: nowrap;
    background: linear-gradient(135deg, #fff 0%, #4080FF 100%);
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }

  p {
    font-size: 15px;
    color: rgba(200, 230, 255, 0.85);
    margin-bottom: 16px;
    line-height: 1.7;
    max-width: 520px;
  }
}

// ==================== 内容区域 ====================
.news-content {
  max-width: $content-max-width;
  margin: 0 auto;
  padding: 24px 24px 60px;
}

// 分类标签
.category-bar {
  margin-bottom: 24px;
}

.category-tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.category-tab {
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  color: $text-body;
  background: $bg-white;
  border: 1px solid #E5E6EB;
  cursor: pointer;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.25s ease;
  &:hover {
    border-color: $primary;
    color: $primary;
  }
  &.active {
    background: $primary;
    color: #fff;
    border-color: $primary;
    box-shadow: 0 4px 12px rgba(22, 93, 255, 0.25);
  }
}

// 新闻卡片网格
.news-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 20px;
}

.news-card {
  background: $bg-white;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.35s ease;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  border: 1px solid transparent;
  animation: fadeInUp 0.5s ease forwards;
  opacity: 0;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
    border-color: rgba($primary, 0.1);

    .image-placeholder {
      transform: scale(1.08);
    }
  }

  &.is-pinned {
    border-color: rgba(245, 63, 63, 0.15);

    .news-image::after {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 3px;
      background: linear-gradient(90deg, #F53F3F, #F76965);
    }
  }

  &.is-urgent {
    border-color: rgba(250, 140, 22, 0.3);

    .news-image::after {
      content: '';
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      height: 3px;
      background: linear-gradient(90deg, #FF7D00, #FF9500);
    }
  }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(16px); }
  to { opacity: 1; transform: translateY(0); }
}

// 图片区域
.news-image {
  position: relative;
  height: 160px;
  overflow: hidden;

  .image-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    transition: transform 0.5s ease;
  }
}

// 日期标签
.date-badge {
  position: absolute;
  top: 12px;
  left: 12px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 10px;
  padding: 6px 10px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  backdrop-filter: blur(4px);

  .date-day {
    display: block;
    font-size: 20px;
    font-weight: 800;
    color: $text-title;
    line-height: 1;
  }

  .date-month {
    display: block;
    font-size: 11px;
    color: $text-muted;
    margin-top: 2px;
  }
}

// 分类标签
.category-badge {
  position: absolute;
  bottom: 12px;
  right: 12px;
  color: #fff;
  font-size: 12px;
  font-weight: 600;
  padding: 4px 12px;
  border-radius: 20px;
  backdrop-filter: blur(4px);
}

.news-body {
  padding: 16px 20px 20px;
}

.news-badge-group {
  display: flex;
  gap: 8px;
  margin-bottom: 10px;
  flex-wrap: wrap;
}

.pin-badge {
  font-weight: 600;
}

.urgent-badge {
  font-weight: 600;
}

.news-title {
  font-size: 16px;
  font-weight: 700;
  color: $text-title;
  margin-bottom: 8px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-summary {
  font-size: 13px;
  color: $text-body;
  line-height: 1.7;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-footer {
  display: flex;
  gap: 20px;
  span {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: $text-muted;
  }
}

.empty-state {
  text-align: center;
  padding: 80px 0;
  p {
    margin-top: 16px;
    font-size: 14px;
    color: $text-muted;
  }
}

// 详情弹窗
.news-detail {
  .detail-meta {
    display: flex;
    align-items: center;
    gap: 10px;
    margin-bottom: 20px;
    padding-bottom: 16px;
    border-bottom: 1px solid #F2F3F5;
    .detail-time, .detail-views {
      font-size: 13px;
      color: $text-muted;
    }
  }
  .detail-body {
    font-size: 15px;
    line-height: 1.8;
    color: $text-body;
    h4 {
      font-size: 16px;
      font-weight: 700;
      color: $text-title;
      margin: 20px 0 10px;
    }
    p {
      margin-bottom: 12px;
    }
  }
}

// ==================== 响应式 ====================
@media (max-width: 1200px) {
  .banner-inner {
    padding: 0 32px;
  }
  .banner-left-deco,
  .banner-right-deco {
    display: none;
  }
  .banner-content {
    text-align: center;
    padding: 0;
    h2 { white-space: normal; }
  }
  .news-content {
    padding: 20px 24px 48px;
  }
}

@media (max-width: 768px) {
  .banner-inner {
    height: 200px;
    padding: 0 20px;
  }
  .banner-content h2 {
    font-size: 24px;
  }
  .banner-content p {
    font-size: 13px;
  }
  .news-content {
    padding: 16px 16px 40px;
  }
  .category-tabs {
    overflow-x: auto;
    flex-wrap: nowrap;
    gap: 6px;
    padding-bottom: 4px;
    scrollbar-width: none;
    &::-webkit-scrollbar { display: none; }
  }
  .category-tab {
    flex-shrink: 0;
    padding: 6px 16px;
    font-size: 13px;
  }
  .news-grid {
    grid-template-columns: 1fr;
  }
  .news-card {
    .news-image {
      height: 140px;
    }
  }
}
</style>
