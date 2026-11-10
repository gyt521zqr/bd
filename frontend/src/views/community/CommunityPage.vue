<template>
  <!-- 编程创作社区页面 -->
  <div class="community-page">
    <!-- Hero 区域 -->
    <section class="hero-section">
      <div class="hero-bg">
        <div class="hero-particle particle-1"></div>
        <div class="hero-particle particle-2"></div>
        <div class="hero-particle particle-3"></div>
        <div class="hero-particle particle-4"></div>
      </div>
      <div class="hero-content">
        <h1 class="hero-title">
          <span class="title-word">编程</span>
          <span class="title-word">创作</span>
          <span class="title-word">社区</span>
        </h1>
        <p class="hero-subtitle">分享你的创意作品，探索无限可能</p>
        <div class="hero-stats">
          <div class="hero-stat">
            <span class="stat-num">1,200+</span>
            <span class="stat-label">创意作品</span>
          </div>
          <div class="hero-stat-divider"></div>
          <div class="hero-stat">
            <span class="stat-num">500+</span>
            <span class="stat-label">小创作者</span>
          </div>
          <div class="hero-stat-divider"></div>
          <div class="hero-stat">
            <span class="stat-num">6</span>
            <span class="stat-label">编程环境</span>
          </div>
        </div>
      </div>
    </section>

    <div class="page-container">
      <!-- 编辑器入口区 -->
      <section class="editor-section">
        <div class="section-header">
          <div class="section-badge">
            <el-icon><EditPen /></el-icon>
          </div>
          <div class="section-header-text">
            <h2>自由创作</h2>
            <p>选择你喜欢的编程工具，自由创作属于你的作品</p>
          </div>
        </div>
        <div class="editor-grid">
          <div
            v-for="editor in editorEntries"
            :key="editor.key"
            class="editor-card"
            :style="{ backgroundImage: `url(${editor.cover})` }"
            @click="openEditor(editor)"
          >
            <div class="editor-card-inner">
              <div class="editor-icon">
                <span v-if="editor.iconText" class="icon-text">{{ editor.iconText }}</span>
                <span v-else class="icon-emoji">{{ editor.emoji }}</span>
              </div>
              <div class="editor-info">
                <h3 class="editor-name">{{ editor.name }}</h3>
                <p class="editor-desc">{{ editor.desc }}</p>
              </div>
            </div>
            <div class="editor-arrow">
              <el-icon><ArrowRight /></el-icon>
            </div>
            <div class="editor-shine"></div>
          </div>
        </div>
      </section>

      <!-- 精选作品展示区 -->
      <section class="works-section">
        <div class="section-header">
          <div class="section-badge">
            <el-icon><Star /></el-icon>
          </div>
          <div class="section-header-text">
            <h2>精选作品</h2>
            <p>来自社区小创作者们的精彩作品</p>
          </div>
        </div>
        <div class="works-waterfall" v-if="featuredWorks.length > 0">
          <div
            v-for="(work, index) in featuredWorks"
            :key="work.id"
            class="work-card"
            :style="{ animationDelay: `${index * 0.06}s` }"
          >
            <div
              class="work-cover"
              :style="work.coverImage ? { backgroundImage: `url(${work.coverImage})` } : { background: work.coverGradient }"
            >
              <div class="cover-overlay">
                <div class="cover-actions">
                  <el-tag class="work-lang-tag" size="small" effect="dark">{{ work.language }}</el-tag>
                  <button class="cover-play-btn" @click.stop="openWork(work)">
                    <el-icon><VideoPlay /></el-icon>
                  </button>
                </div>
              </div>
              <div class="cover-shine"></div>
            </div>
            <div class="work-body">
              <h4 class="work-title">{{ work.title }}</h4>
              <div class="work-meta">
                <span class="work-author">
                  <el-icon><User /></el-icon>
                  {{ work.author }}
                </span>
              </div>
              <div class="work-stats">
                <span class="stat-item" :class="{ 'liked': work.isLiked }" @click.stop="toggleLike(work)">
                  <el-icon class="like-icon"><Star /></el-icon>
                  <span class="stat-num">{{ work.likes }}</span>
                </span>
                <span class="stat-item">
                  <el-icon><VideoPlay /></el-icon>
                  <span class="stat-num">{{ work.plays }}</span>
                </span>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无作品" :image-size="70" class="works-empty" />
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * CommunityPage - 编程创作社区
 * Hero区域 + 编辑器入口 + 精选作品展示
 */
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ArrowRight, Star, VideoPlay, EditPen, User } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { loadWorks } from '@/api/scratch'
import type { ScratchWork } from '@/types/editor'

const router = useRouter()

// ==================== 编辑器入口 ====================
interface EditorEntry {
  key: string
  name: string
  desc: string
  emoji: string
  iconText: string
  gradient: string
  cover: string
  lang: string
}

const editorEntries = ref<EditorEntry[]>([
  {
    key: 'scratchjr',
    name: 'ScratchJr',
    desc: '适合5-7岁，图形化启蒙编程，自由创作动画故事',
    emoji: '\uD83D\uDC31',
    iconText: '',
    gradient: 'linear-gradient(135deg, #4080FF, #165DFF)',
    cover: '/images/contest/contest-purple.png',
    lang: 'scratchjr',
  },
  {
    key: 'scratch3',
    name: 'Scratch',
    desc: '适合6-12岁，积木式创意编程，自由创作游戏和动画',
    emoji: '\uD83E\uDDE9',
    iconText: '',
    gradient: 'linear-gradient(135deg, #FF9500, #E06800)',
    cover: '/images/contest/contest-orange.png',
    lang: 'scratch3',
  },
  {
    key: 'pythonturtle',
    name: 'Python Turtle',
    desc: 'Python Turtle 图形编程，自由绘制精彩图案',
    emoji: '\uD83D\uDC22',
    iconText: '',
    gradient: 'linear-gradient(135deg, #36D399, #36D399)',
    cover: '/images/contest/contest-magenta.png',
    lang: 'pythonturtle',
  },
  {
    key: 'python',
    name: 'Python',
    desc: '适合8-16岁，文本编程入门，自由创作程序作品',
    emoji: '',
    iconText: 'Py',
    gradient: 'linear-gradient(135deg, #165DFF, #FFB84D)',
    cover: '/images/contest/contest-blue.png',
    lang: 'python',
  },
  {
    key: 'goc',
    name: 'GoC',
    desc: '适合8-14岁，图形化编程，自由创作图形作品',
    emoji: '',
    iconText: 'GoC',
    gradient: 'linear-gradient(135deg, #165DFF, #165DFF)',
    cover: '/images/contest/contest-green.png',
    lang: 'goc',
  },
  {
    key: 'cpp',
    name: 'C++',
    desc: '适合10-18岁，算法竞赛必备，自由编写代码',
    emoji: '',
    iconText: 'C++',
    gradient: 'linear-gradient(135deg, #4080FF, #165DFF)',
    cover: '/images/contest/contest-cyan.png',
    lang: 'cpp',
  },
])

function openEditor(editor: EditorEntry) {
  router.push(`/editor?lang=${editor.lang}&mode=create`)
}

// ==================== 精选作品 ====================
interface FeaturedWork {
  id: number
  title: string
  author: string
  language: string
  difficulty: number
  likes: number
  plays: number
  coverGradient: string
  coverImage: string
  lang: string
  isLiked?: boolean
}

/** 作品封面图（复用统一风格插图，与赛事/课程/题库一致） */
const workCoverImages = [
  '/images/contest/contest-blue.png',
  '/images/contest/contest-purple.png',
  '/images/contest/contest-orange.png',
  '/images/contest/contest-green.png',
  '/images/contest/contest-cyan.png',
  '/images/contest/contest-magenta.png',
  '/images/course/course-1.png',
  '/images/course/course-2.png',
  '/images/course/course-3.png',
  '/images/course/course-4.png',
  '/images/course/course-5.png',
  '/images/course/course-6.png',
  '/images/course/course-7.png',
  '/images/course/course-8.png',
  '/images/course/course-9.png',
  '/images/course/course-10.png',
  '/images/course/course-11.png',
]

// 精选作品列表：不再内置虚构作品，接口无数据时由模板显示空态
const featuredWorks = ref<FeaturedWork[]>([])

// ==================== API 数据加载 ====================
// 封面渐变色板（API 未返回封面时按顺序复用）
const workCoverGradients = [
  'linear-gradient(135deg, #FF9500, #F53F3F)',
  'linear-gradient(135deg, #F53F3F, #7B61FF)',
  'linear-gradient(135deg, #165DFF, #FFB84D)',
  'linear-gradient(135deg, #165DFF, #165DFF)',
  'linear-gradient(135deg, #4080FF, #165DFF)',
  'linear-gradient(135deg, #36D399, #36D399)',
  'linear-gradient(135deg, #4080FF, #165DFF)',
  'linear-gradient(135deg, #E06800, #FF9500)',
  'linear-gradient(135deg, #7B61FF, #7B61FF)',
  'linear-gradient(135deg, #0E42D2, #7B61FF)',
  'linear-gradient(135deg, #165DFF, #4080FF)',
  'linear-gradient(135deg, #7B61FF, #165DFF)',
]

// 将后端 ScratchWork 归一化为页面展示结构
function normalizeWork(item: ScratchWork, index: number): FeaturedWork {
  return {
    id: item.id,
    title: item.title || '未命名作品',
    author: item.userId ? `创作者${item.userId}` : '社区创作者',
    // loadWorks 返回的是 Scratch 作品，统一标记为 Scratch
    language: 'Scratch',
    lang: 'scratch3',
    difficulty: 1,
    likes: 0,
    plays: 0,
    coverGradient: workCoverGradients[index % workCoverGradients.length],
    coverImage: workCoverImages[index % workCoverImages.length],
  }
}

async function loadFeaturedWorks() {
  try {
    const res = await loadWorks({ pageNum: 1, pageSize: 12 })
    const list = res?.data?.list
    if (Array.isArray(list) && list.length > 0) {
      featuredWorks.value = list.map((item, index) => normalizeWork(item, index))
    }
  } catch {
    // 接口无数据或失败：保持空态，由模板显示「暂无数据」
  }
}

onMounted(() => {
  loadFeaturedWorks()
})

function openWork(work: FeaturedWork) {
  router.push(`/editor?lang=${work.lang}&mode=create&work=${work.id}`)
}

function toggleLike(work: FeaturedWork) {
  work.isLiked = !work.isLiked
  work.likes += work.isLiked ? 1 : -1
  if (work.isLiked) {
    ElMessage.success('点赞成功！')
  }
}
</script>

<style scoped lang="scss">

.community-page {
  background: $bg-page;
  min-height: 100vh;
  padding-bottom: 60px;
}

// ==================== Hero 区域 ====================
.hero-section {
  position: relative;
  height: 360px;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}

.hero-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 40%, #7B61FF 100%);
  z-index: 0;

  &::before {
    content: '';
    position: absolute;
    top: -50%;
    left: -20%;
    width: 600px;
    height: 600px;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.08) 0%, transparent 70%);
    border-radius: 50%;
  }

  &::after {
    content: '';
    position: absolute;
    bottom: -30%;
    right: -10%;
    width: 500px;
    height: 500px;
    background: radial-gradient(circle, rgba(255, 255, 255, 0.06) 0%, transparent 70%);
    border-radius: 50%;
  }
}

.hero-particle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 6s ease-in-out infinite;
  will-change: transform;

  &.particle-1 {
    width: 80px;
    height: 80px;
    top: 20%;
    left: 10%;
    animation-delay: 0s;
  }

  &.particle-2 {
    width: 120px;
    height: 120px;
    top: 60%;
    right: 15%;
    animation-delay: 1.5s;
  }

  &.particle-3 {
    width: 60px;
    height: 60px;
    bottom: 20%;
    left: 25%;
    animation-delay: 3s;
  }

  &.particle-4 {
    width: 100px;
    height: 100px;
    top: 15%;
    right: 30%;
    animation-delay: 4.5s;
  }
}

@keyframes float {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-20px) scale(1.1); }
}

.hero-content {
  position: relative;
  z-index: 1;
  text-align: center;
}

.hero-title {
  font-size: 44px;
  font-weight: 800;
  color: #FFFFFF;
  margin-bottom: 16px;
  letter-spacing: 2px;
  display: flex;
  gap: 12px;
  justify-content: center;

  .title-word {
    display: inline-block;
    animation: titlePop 0.6s ease-out forwards;
    opacity: 0;
    transform: translateY(20px);

    &:nth-child(1) { animation-delay: 0.1s; }
    &:nth-child(2) { animation-delay: 0.25s; }
    &:nth-child(3) { animation-delay: 0.4s; }
  }
}

@keyframes titlePop {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.hero-subtitle {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.85);
  font-weight: 400;
  margin-bottom: 32px;
}

.hero-stats {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 24px;
}

.hero-stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;

  .stat-num {
    font-size: 24px;
    font-weight: 700;
    color: #FFFFFF;
  }

  .stat-label {
    font-size: 13px;
    color: rgba(255, 255, 255, 0.7);
  }
}

.hero-stat-divider {
  width: 1px;
  height: 40px;
  background: rgba(255, 255, 255, 0.2);
}

// ==================== 通用容器 ====================
.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px;
}

.section-header {
  margin-bottom: 28px;
  display: flex;
  align-items: center;
  gap: 14px;

  .section-badge {
    width: 40px;
    height: 40px;
    border-radius: 12px;
    background: linear-gradient(135deg, #165DFF, #4080FF);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
    font-size: 18px;
    box-shadow: 0 4px 12px rgba(22, 93, 255, 0.25);
  }

  .section-header-text {
    h2 {
      font-size: 24px;
      font-weight: 700;
      color: $text-title;
      margin-bottom: 4px;
    }

    p {
      font-size: 14px;
      color: $text-muted;
    }
  }
}

// ==================== 编辑器入口 ====================
.editor-section {
  margin-top: 48px;
}

.editor-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.editor-card {
  border-radius: 16px;
  padding: 24px;
  cursor: pointer;
  transition: transform 0.35s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  position: relative;
  overflow: hidden;
  background-size: cover;
  background-position: center;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.35);
    transition: background 0.3s ease;
  }

  &:hover {
    transform: translateY(-6px) scale(1.02);
    box-shadow: 0 16px 40px rgba(22, 93, 255, 0.25);

    &::before {
      background: rgba(0, 0, 0, 0.45);
    }

    .editor-arrow {
      opacity: 1;
      transform: translateX(0);
    }

    .editor-shine {
      transform: translateX(100%);
    }
  }
}

.editor-shine {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.15), transparent);
  transition: transform 0.6s ease;
  pointer-events: none;
}

.editor-card-inner {
  display: flex;
  align-items: center;
  gap: 16px;
  position: relative;
  z-index: 1;
}

.editor-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.2);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  backdrop-filter: blur(4px);

  .icon-text {
    font-size: 18px;
    font-weight: 800;
    color: #FFFFFF;
    letter-spacing: 0.5px;
  }

  .icon-emoji {
    font-size: 30px;
    line-height: 1;
  }
}

.editor-info {
  flex: 1;
  min-width: 0;

  .editor-name {
    font-size: 18px;
    font-weight: 700;
    color: #FFFFFF;
    margin-bottom: 4px;
  }

  .editor-desc {
    font-size: 13px;
    color: rgba(255, 255, 255, 0.8);
    white-space: nowrap;
    overflow: hidden;
    text-overflow: ellipsis;
  }
}

.editor-arrow {
  position: absolute;
  right: 24px;
  top: 50%;
  transform: translateY(-50%) translateX(-4px);
  opacity: 0;
  transition: opacity 0.25s ease, transform 0.25s ease;
  z-index: 1;

  .el-icon {
    font-size: 20px;
    color: #FFFFFF;
  }
}

// ==================== 精选作品（瀑布流） ====================
.works-section {
  margin-top: 56px;
}

// 精选作品空态
.works-empty {
  background: $bg-white;
  border-radius: 16px;
  padding: 40px 0;
}

.works-waterfall {
  column-count: 4;
  column-gap: 20px;
}

.work-card {
  background: #FFFFFF;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: transform 0.35s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  break-inside: avoid;
  margin-bottom: 20px;
  animation: cardFadeIn 0.5s ease-out forwards;
  opacity: 0;
  transform: translateY(20px);

  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 16px 40px rgba(22, 93, 255, 0.15);

    .work-cover {
      .cover-overlay {
        opacity: 1;
      }

      .cover-shine {
        transform: translateX(100%);
      }
    }
  }
}

@keyframes cardFadeIn {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.work-cover {
  height: 140px;
  position: relative;
  overflow: hidden;
  background-size: cover;
  background-position: center;

  .cover-overlay {
    position: absolute;
    inset: 0;
    background: rgba(0, 0, 0, 0.45);
    display: flex;
    align-items: flex-start;
    justify-content: flex-end;
    padding: 10px;
    opacity: 0;
    transition: opacity 0.35s ease;
  }

  .cover-actions {
    display: flex;
    flex-direction: column;
    align-items: flex-end;
    gap: 8px;
  }

  .cover-play-btn {
    width: 44px;
    height: 44px;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.9);
    border: none;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    transition: transform 0.25s ease, background 0.25s ease, box-shadow 0.25s ease;
    color: #165DFF;

    &:hover {
      transform: scale(1.1);
      background: $bg-white;
      box-shadow: 0 4px 16px rgba(22, 93, 255, 0.3);
    }

    .el-icon {
      font-size: 20px;
    }
  }

  .cover-shine {
    position: absolute;
    top: 0;
    left: -100%;
    width: 100%;
    height: 100%;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.1), transparent);
    transition: transform 0.6s ease;
    pointer-events: none;
  }
}

.work-lang-tag {
  border-radius: 6px;
}

.work-body {
  padding: 16px;
}

.work-title {
  font-size: 15px;
  font-weight: 600;
  color: $text-title;
  margin-bottom: 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.work-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.work-author {
  font-size: 13px;
  color: $text-muted;
  display: flex;
  align-items: center;
  gap: 4px;

  .el-icon {
    font-size: 14px;
  }
}

.difficulty-tag {
  font-weight: 500;
}

.work-stats {
  display: flex;
  gap: 16px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: $text-muted;
  cursor: pointer;
  transition: background-color 0.25s ease, color 0.25s ease;
  padding: 4px 8px;
  border-radius: 6px;

  .el-icon {
    font-size: 14px;
    transition: color 0.25s ease;
  }

  .stat-num {
    font-weight: 500;
  }

  &:hover {
    background: rgba(22, 93, 255, 0.06);
    color: $primary-color;
  }

  &.liked {
    color: #FF7D00;
    background: rgba(255, 125, 0, 0.08);

    .like-icon {
      animation: likeBounce 0.4s ease;
      will-change: transform;
    }
  }
}

@keyframes likeBounce {
  0% { transform: scale(1); }
  30% { transform: scale(1.4); }
  60% { transform: scale(0.9); }
  100% { transform: scale(1); }
}

// ==================== 响应式 ====================
@media (max-width: 1200px) {
  .page-container {
    padding: 0 24px;
  }

  .works-waterfall {
    column-count: 3;
  }
}

@media (max-width: 768px) {
  .hero-section {
    height: 280px;
  }

  .hero-title {
    font-size: 32px;
    gap: 8px;
  }

  .hero-subtitle {
    font-size: 15px;
  }

  .hero-stats {
    gap: 16px;
  }

  .hero-stat .stat-num {
    font-size: 20px;
  }

  .page-container {
    padding: 0 16px;
  }

  .editor-section {
    margin-top: 32px;
  }

  .editor-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .editor-card {
    padding: 18px;
  }

  .works-section {
    margin-top: 40px;
  }

  .works-waterfall {
    column-count: 2;
    column-gap: 12px;
  }

  .work-card {
    margin-bottom: 12px;
  }

  .section-header {
    h2 {
      font-size: 20px;
    }
  }
}

@media (max-width: 480px) {
  .hero-section {
    height: 240px;
  }

  .hero-title {
    font-size: 26px;
    flex-wrap: wrap;
    gap: 6px;
  }

  .hero-subtitle {
    font-size: 13px;
  }

  .hero-stats {
    display: none;
  }

  .editor-grid {
    grid-template-columns: 1fr;
  }

  .works-waterfall {
    column-count: 1;
  }
}
</style>
