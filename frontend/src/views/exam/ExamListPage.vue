<template>
  <!-- 考试中心：顶部科技蓝Banner + 赛事卡片列表（赛氪风格） -->
  <div class="exam-list-page">
    <!-- 顶部Banner - 科技蓝风格 -->
    <section class="exam-banner">
      <div class="banner-inner">
        <!-- 粒子画布 -->
        <canvas ref="particleCanvas" class="particle-canvas"></canvas>

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
          <div class="banner-badge">码高教育 · 赛事中心</div>
          <h2>编程赛事 · 挑战无限</h2>
          <p>汇聚全国优质编程赛事，助力学子在竞赛中成长，在挑战中突破</p>
          <div class="banner-meta">
            <span class="meta-item">
              <el-icon size="14"><Trophy /></el-icon>
              全国性编程赛事
            </span>
            <span class="meta-item">
              <el-icon size="14"><User /></el-icon>
              累计参赛选手 50万+
            </span>
          </div>
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

    <!-- 赛事筛选栏 -->
    <section class="exam-filter-bar">
      <div class="filter-container">
        <div class="filter-tabs">
          <div
            class="filter-tab"
            v-for="tab in filterTabs"
            :key="tab.key"
            :class="{ active: activeFilter === tab.key }"
            @click="activeFilter = tab.key"
          >
            {{ tab.label }}
            <span class="tab-count" v-if="tab.count">({{ tab.count }})</span>
          </div>
        </div>
        <div class="filter-search">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索赛事名称..."
            :prefix-icon="Search"
            clearable
            size="default"
            class="search-input"
          />
        </div>
      </div>
    </section>

    <!-- 赛事列表 -->
    <section class="exam-list-section">
      <div class="exam-list-container">
        <template v-if="filteredExams.length">
        <div
          class="exam-item"
          v-for="exam in filteredExams"
          :key="exam.id"
          @click="handleEnter(exam)"
        >
          <!-- 排名标签 -->
          <div class="exam-rank-badge" :class="`rank-${exam.rank}`">
            <span class="rank-label">TOP</span>
            <span class="rank-num">{{ exam.rank }}</span>
          </div>
          <!-- 状态标签 -->
          <div class="exam-status-tag" :class="exam.statusType">
            <span class="status-text">{{ exam.statusText }}</span>
            <span class="status-countdown" v-if="exam.countdown">{{ exam.countdown }}</span>
          </div>
          <!-- 缩略图 -->
          <div class="exam-poster" :style="exam.cover ? { backgroundImage: `url(${exam.cover})` } : { background: exam.posterBg }">
            <div class="poster-content">
              <div class="poster-title">{{ exam.posterTitle }}</div>
              <div class="poster-sub" v-if="exam.posterSub">{{ exam.posterSub }}</div>
              <div class="poster-deadline" v-if="exam.deadline">{{ exam.deadline }}</div>
            </div>
          </div>
          <!-- 信息区域 -->
          <div class="exam-info-area">
            <h3 class="exam-title">
              <span class="title-tag" v-if="exam.tag" :class="exam.tagType">{{ exam.tag }}</span>
              {{ exam.title }}
            </h3>
            <div class="exam-organizer">
              <el-icon size="14"><OfficeBuilding /></el-icon>
              <span>{{ exam.organizer }}</span>
            </div>
            <div class="exam-meta-row">
              <div class="meta-block">
                <span class="meta-label">报名时间</span>
                <span class="meta-value">{{ exam.regStart }} ~ {{ exam.regEnd }}</span>
              </div>
              <div class="meta-block">
                <span class="meta-label">比赛时间</span>
                <span class="meta-value">{{ exam.examStart }} ~ {{ exam.examEnd }}</span>
              </div>
            </div>
            <div class="exam-extra">
              <span class="extra-item" v-if="exam.level">
                <el-icon size="12"><Medal /></el-icon>
                {{ exam.level }}
              </span>
              <span class="extra-item" v-if="exam.participants">
                <el-icon size="12"><User /></el-icon>
                {{ exam.participants }}人已报名
              </span>
            </div>
          </div>
        </div>
        </template>

        <!-- 空状态 -->
        <div class="empty-state" v-else>
          <el-icon :size="48" color="#C9CDD4"><Document /></el-icon>
          <p>暂无符合条件的赛事</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
/**
 * ExamListPage - 考试中心（赛氪风格）
 * 顶部科技蓝Banner + 赛事卡片列表
 */
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Trophy, User, OfficeBuilding, Search, Medal, Document } from '@element-plus/icons-vue'
import { getExamList } from '@/api/exam'
import type { ExamItem } from '@/types'

const router = useRouter()

// ==================== 粒子动画 ====================
const particleCanvas = ref<HTMLCanvasElement>()
let particleRaf: number

interface Particle {
  x: number
  y: number
  vx: number
  vy: number
  radius: number
  opacity: number
}

function initParticles() {
  const canvas = particleCanvas.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  if (!ctx) return

  const dpr = window.devicePixelRatio || 1
  const parent = canvas.parentElement!
  let w = parent.clientWidth
  let h = parent.clientHeight
  canvas.width = w * dpr
  canvas.height = h * dpr
  canvas.style.width = w + 'px'
  canvas.style.height = h + 'px'
  ctx.scale(dpr, dpr)

  const particles: Particle[] = []
  const count = Math.min(60, Math.floor((w * h) / 12000))

  for (let i = 0; i < count; i++) {
    particles.push({
      x: Math.random() * w,
      y: Math.random() * h,
      vx: (Math.random() - 0.5) * 0.6,
      vy: (Math.random() - 0.5) * 0.6,
      radius: Math.random() * 2 + 1,
      opacity: Math.random() * 0.5 + 0.2,
    })
  }

  function draw() {
    if (!ctx || !canvas) return
    ctx.clearRect(0, 0, w, h)

    // 更新和绘制粒子
    for (let i = 0; i < particles.length; i++) {
      const p = particles[i]
      p.x += p.vx
      p.y += p.vy
      if (p.x < 0 || p.x > w) p.vx *= -1
      if (p.y < 0 || p.y > h) p.vy *= -1

      ctx.beginPath()
      ctx.arc(p.x, p.y, p.radius, 0, Math.PI * 2)
      ctx.fillStyle = `rgba(100, 200, 255, ${p.opacity})`
      ctx.fill()
    }

    // 连线
    for (let i = 0; i < particles.length; i++) {
      for (let j = i + 1; j < particles.length; j++) {
        const dx = particles[i].x - particles[j].x
        const dy = particles[i].y - particles[j].y
        const dist = Math.sqrt(dx * dx + dy * dy)
        if (dist < 120) {
          ctx.beginPath()
          ctx.moveTo(particles[i].x, particles[i].y)
          ctx.lineTo(particles[j].x, particles[j].y)
          ctx.strokeStyle = `rgba(100, 200, 255, ${0.12 * (1 - dist / 120)})`
          ctx.lineWidth = 0.8
          ctx.stroke()
        }
      }
    }

    particleRaf = requestAnimationFrame(draw)
  }

  draw()

  const onResize = () => {
    w = parent.clientWidth
    h = parent.clientHeight
    canvas.width = w * dpr
    canvas.height = h * dpr
    canvas.style.width = w + 'px'
    canvas.style.height = h + 'px'
    ctx.scale(dpr, dpr)
  }

  window.addEventListener('resize', onResize)
  return () => {
    window.removeEventListener('resize', onResize)
    cancelAnimationFrame(particleRaf)
  }
}

onMounted(() => {
  const cleanup = initParticles()
  onUnmounted(() => {
    if (cleanup) cleanup()
  })
  // 加载真实赛事数据（接口失败或无数据时保持空态）
  loadExamList()
})

// 筛选
const activeFilter = ref('all')
const searchKeyword = ref('')

// 筛选标签（count 动态计算，不再硬编码）
const filterTabs = computed(() => [
  { key: 'all', label: '全部赛事', count: examList.value.length },
  { key: 'registering', label: '正在报名', count: examList.value.filter(e => e.status === 'registering').length },
  { key: 'ongoing', label: '比赛进行中', count: examList.value.filter(e => e.status === 'ongoing').length },
  { key: 'ended', label: '已结束', count: examList.value.filter(e => e.status === 'ended').length },
])

// 赛事封面图片（按主题配色循环复用）
const examPosterCovers = [
  '/images/contest/contest-blue.png',
  '/images/contest/contest-purple.png',
  '/images/contest/contest-orange.png',
  '/images/contest/contest-green.png',
  '/images/contest/contest-cyan.png',
  '/images/contest/contest-magenta.png',
]

// 赛事展示数据结构
interface ExamDisplay {
  id: number
  rank: number
  title: string
  tag: string
  tagType: string
  posterTitle: string
  posterSub: string
  deadline: string
  posterBg: string
  cover?: string
  organizer: string
  regStart: string
  regEnd: string
  examStart: string
  examEnd: string
  statusType: string
  statusText: string
  countdown: string
  level: string
  participants: number
  status: string
}

// 赛事列表（接口返回前保持空态，onMounted 中用真实 API 数据填充）
const examList = ref<ExamDisplay[]>([])

// ==================== API 数据加载（失败或无数据时保持空态） ====================
// 封面渐变色板（API 未返回封面时按顺序复用）
const examPosterGradients = [
  'linear-gradient(135deg, #0D1117 0%, #0D1117 50%, #0D1117 100%)',
  'linear-gradient(135deg, #0D1117 0%, #7B61FF 50%, #7B61FF 100%)',
  'linear-gradient(135deg, #E06800 0%, #E06800 50%, #E06800 100%)',
  'linear-gradient(135deg, #36D399 0%, #36D399 50%, #36D399 100%)',
  'linear-gradient(135deg, #0D1117 0%, #0E42D2 50%, #0D1117 100%)',
  'linear-gradient(135deg, #0E42D2 0%, #165DFF 50%, #0E42D2 100%)',
  'linear-gradient(135deg, #7B61FF 0%, #7B61FF 50%, #7B61FF 100%)',
  'linear-gradient(135deg, #E06800 0%, #E06800 50%, #E06800 100%)',
]

// 后端 ExamStatus -> 页面展示状态映射
const examStatusMap: Record<string, { status: string; statusType: string; statusText: string }> = {
  not_started: { status: 'registering', statusType: 'status-registering', statusText: '正在报名' },
  in_progress: { status: 'ongoing', statusType: 'status-ongoing', statusText: '比赛进行中' },
  submitted: { status: 'ended', statusType: 'status-ended', statusText: '已结束' },
  graded: { status: 'ended', statusType: 'status-ended', statusText: '已结束' },
}

function formatExamDate(dateStr?: string): string {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  if (isNaN(d.getTime())) return dateStr
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}.${m}.${day}`
}

// 将后端 ExamItem 归一化为页面展示结构
function normalizeExam(item: ExamItem, index: number): ExamDisplay {
  const mapped = examStatusMap[item.status] || examStatusMap.not_started
  return {
    id: item.id,
    rank: index + 1,
    title: item.title || '未命名赛事',
    tag: '',
    tagType: '',
    posterTitle: (item.title || '赛事').slice(0, 8),
    posterSub: '',
    deadline: '',
    posterBg: examPosterGradients[index % examPosterGradients.length],
    cover: examPosterCovers[index % examPosterCovers.length],
    organizer: item.description || '',
    regStart: formatExamDate(item.startTime),
    regEnd: formatExamDate(item.endTime),
    examStart: formatExamDate(item.startTime),
    examEnd: formatExamDate(item.endTime),
    statusType: mapped.statusType,
    statusText: mapped.statusText,
    countdown: '',
    level: '',
    participants: 0,
    status: mapped.status,
  }
}

async function loadExamList() {
  try {
    const res = await getExamList({ pageNum: 1, pageSize: 20 })
    const list = res?.data?.list
    if (Array.isArray(list) && list.length > 0) {
      examList.value = list.map((item, index) => normalizeExam(item, index))
    }
  } catch {
    // 接口无数据或失败：保持空态，由模板显示「暂无赛事」
  }
}

// 筛选后的赛事
const filteredExams = computed(() => {
  let list = examList.value
  if (activeFilter.value !== 'all') {
    list = list.filter(e => e.status === activeFilter.value)
  }
  if (searchKeyword.value) {
    const kw = searchKeyword.value.toLowerCase()
    list = list.filter(e => e.title.toLowerCase().includes(kw) || e.organizer.toLowerCase().includes(kw))
  }
  return list
})

function handleEnter(exam: any) {
  router.push(`/exam/${exam.id}`)
}
</script>

<style scoped lang="scss">
// ==================== 变量 ====================
$cyan: #36D399;

// ==================== 页面基础 ====================
.exam-list-page {
  background: $bg-page;
  min-height: 100vh;
}

// ==================== 顶部Banner ====================
.exam-banner {
  width: 100%;
  overflow: hidden;
  border-radius: 0 0 16px 16px;
  box-shadow: 0 8px 24px rgba(22, 93, 255, 0.08);
}

.banner-inner {
  height: 360px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #fff;
  position: relative;
  overflow: hidden;
  padding: 0 60px;
  background: linear-gradient(135deg, #0D1117 0%, #0D1117 40%, #0D1117 70%, #0D1117 100%);
}

// 粒子画布
.particle-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
  z-index: 1;
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

// 左侧装饰
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

// 右侧装饰
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

// Banner内容
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

.banner-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  .meta-item {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: rgba(180, 220, 255, 0.7);
    .el-icon {
      color: rgba(100, 200, 255, 0.6);
    }
  }
}

// ==================== 筛选栏 ====================
.exam-filter-bar {
  width: 100%;
  padding: 24px 48px 0;
}

.filter-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 20px;
  padding: 16px 20px;
  background: $bg-white;
  border-radius: 12px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
}

.filter-tabs {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.filter-tab {
  padding: 8px 20px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  color: $text-body;
  background: #F7F8FA;
  border: 1px solid transparent;
  cursor: pointer;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  white-space: nowrap;
  position: relative;
  overflow: hidden;

  .tab-count {
    font-size: 12px;
    color: $text-muted;
    margin-left: 2px;
    transition: color 0.3s ease;
  }

  &:hover {
    border-color: $primary-color;
    color: $primary-color;
    background: rgba(22, 93, 255, 0.04);
    transform: translateY(-1px);
  }

  &.active {
    background: $primary-color;
    color: #fff;
    border-color: $primary-color;
    box-shadow: 0 4px 14px rgba(22, 93, 255, 0.35);
    transform: translateY(-1px);

    .tab-count {
      color: rgba(255, 255, 255, 0.8);
    }
  }
}

.search-input {
  width: 260px;
  :deep(.el-input__wrapper) {
    border-radius: 24px;
    box-shadow: 0 0 0 1px #E5E6EB inset;
    transition: box-shadow 0.3s ease;
    &:hover, &:focus-within {
      box-shadow: 0 0 0 1px $primary-color inset, 0 0 0 4px rgba(22, 93, 255, 0.08);
    }
  }
}

// ==================== 赛事列表 ====================
.exam-list-section {
  width: 100%;
  padding: 24px 48px 60px;
}

.exam-list-container {
  max-width: 1200px;
  margin: 0 auto;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.exam-item {
  display: flex;
  align-items: stretch;
  background: $bg-white;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  position: relative;
  border: 2px solid transparent;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 16px 40px rgba(22, 93, 255, 0.12);

    &.status-registering-border {
      border-color: rgba(22, 93, 255, 0.5);
    }
    &.status-ongoing-border {
      border-color: rgba(255, 149, 0, 0.5);
    }
    &.status-ended-border {
      border-color: rgba(134, 144, 156, 0.5);
    }

    .exam-poster::after {
      opacity: 1;
    }
  }
}

// 排名标签
.exam-rank-badge {
  position: absolute;
  top: 16px;
  left: 16px;
  z-index: 3;
  display: flex;
  align-items: center;
  gap: 0;
  border-radius: 6px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  .rank-label {
    background: #F53F3F;
    color: #fff;
    font-size: 11px;
    font-weight: 700;
    padding: 3px 6px;
    letter-spacing: 0.5px;
  }
  .rank-num {
    background: #F53F3F;
    color: #fff;
    font-size: 16px;
    font-weight: 800;
    padding: 2px 8px;
    min-width: 28px;
    text-align: center;
  }
  &.rank-1 .rank-label,
  &.rank-1 .rank-num { background: linear-gradient(135deg, #F53F3F, #FF7D00); }
  &.rank-2 .rank-label,
  &.rank-2 .rank-num { background: linear-gradient(135deg, #7B61FF, #7B61FF); }
  &.rank-3 .rank-label,
  &.rank-3 .rank-num { background: linear-gradient(135deg, #165DFF, #4080FF); }
  &.rank-4 .rank-label,
  &.rank-4 .rank-num,
  &.rank-5 .rank-label,
  &.rank-5 .rank-num,
  &.rank-6 .rank-label,
  &.rank-6 .rank-num,
  &.rank-7 .rank-label,
  &.rank-7 .rank-num,
  &.rank-8 .rank-label,
  &.rank-8 .rank-num { background: linear-gradient(135deg, #86909C, #C0C4CC); }
}

// 状态标签
.exam-status-tag {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 3;
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 4px;
  .status-text {
    padding: 4px 12px;
    border-radius: 12px;
    font-size: 12px;
    font-weight: 600;
  }
  .status-countdown {
    font-size: 11px;
    font-weight: 500;
  }
  &.status-registering {
    .status-text { background: rgba(22, 93, 255, 0.1); color: #165DFF; }
    .status-countdown { color: #165DFF; }
  }
  &.status-ongoing {
    .status-text { background: rgba(250, 140, 22, 0.1); color: #FF9500; }
    .status-countdown { color: #FF9500; }
  }
  &.status-ended {
    .status-text { background: rgba(134, 144, 156, 0.1); color: #86909C; }
    .status-countdown { color: #86909C; }
  }
}

// 缩略图
.exam-poster {
  width: 260px;
  min-height: 200px;
  flex-shrink: 0;
  background: linear-gradient(135deg, #0D1117 0%, #165DFF 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, rgba(255,255,255,0.1) 0%, transparent 50%);
    opacity: 0;
    transition: opacity 0.35s ease;
  }

  .poster-content {
    text-align: center;
    color: #fff;
    padding: 20px;
  }
  .poster-title {
    font-size: 28px;
    font-weight: 800;
    letter-spacing: 2px;
    text-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
    margin-bottom: 8px;
  }
  .poster-sub {
    font-size: 16px;
    font-weight: 500;
    opacity: 0.9;
    margin-bottom: 12px;
  }
  .poster-deadline {
    display: inline-block;
    padding: 4px 12px;
    background: rgba(245, 63, 63, 0.85);
    border-radius: 12px;
    font-size: 12px;
    font-weight: 600;
    letter-spacing: 0.5px;
  }
}

// 信息区域
.exam-info-area {
  flex: 1;
  padding: 24px 28px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.exam-title {
  font-size: 18px;
  font-weight: 700;
  color: $text-title;
  margin-bottom: 10px;
  line-height: 1.5;
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.title-tag {
  display: inline-block;
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  flex-shrink: 0;
  &.tag-urgent { background: rgba(245, 63, 63, 0.1); color: #F53F3F; }
  &.tag-hot { background: rgba(250, 140, 22, 0.1); color: #FF9500; }
  &.tag-new { background: rgba(22, 93, 255, 0.1); color: #165DFF; }
  &.tag-authority { background: rgba(114, 46, 209, 0.1); color: #7B61FF; }
}

.exam-organizer {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: $text-muted;
  margin-bottom: 14px;
  .el-icon {
    color: $text-muted;
  }
}

.exam-meta-row {
  display: flex;
  gap: 24px;
  margin-bottom: 12px;
  flex-wrap: wrap;
}

.meta-block {
  display: flex;
  flex-direction: column;
  gap: 2px;
  .meta-label {
    font-size: 12px;
    color: $text-muted;
  }
  .meta-value {
    font-size: 13px;
    color: $text-body;
    font-weight: 500;
  }
}

.exam-extra {
  display: flex;
  gap: 16px;
  margin-top: 4px;
}

.extra-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: $text-muted;
  .el-icon {
    color: $primary-color;
  }
}

// 空状态
.empty-state {
  text-align: center;
  padding: 80px 0;
  p {
    margin-top: 16px;
    font-size: 14px;
    color: $text-muted;
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
  .banner-meta {
    justify-content: center;
  }
  .exam-filter-bar {
    padding: 20px 24px 0;
  }
  .exam-list-section {
    padding: 20px 24px 48px;
  }
}

@media (max-width: 768px) {
  .banner-inner {
    height: 240px;
    padding: 0 20px;
  }
  .banner-content h2 {
    font-size: 22px;
  }
  .banner-content p {
    font-size: 13px;
  }
  .filter-container {
    flex-direction: column;
    align-items: stretch;
    gap: 12px;
  }
  .filter-tabs {
    overflow-x: auto;
    flex-wrap: nowrap;
    gap: 6px;
    padding-bottom: 4px;
    scrollbar-width: none;
    &::-webkit-scrollbar { display: none; }
  }
  .filter-tab {
    flex-shrink: 0;
    padding: 6px 14px;
    font-size: 13px;
  }
  .search-input {
    width: 100%;
  }
  .exam-item {
    flex-direction: column;
  }
  .exam-poster {
    width: 100%;
    min-height: 140px;
  }
  .exam-rank-badge {
    top: 12px;
    left: 12px;
  }
  .exam-status-tag {
    top: 12px;
    right: 12px;
  }
  .exam-info-area {
    padding: 20px;
  }
  .exam-title {
    font-size: 15px;
  }
  .exam-meta-row {
    flex-direction: column;
    gap: 8px;
  }
}
</style>
