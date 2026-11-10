<template>
  <!-- 首页：编程赛事平台风格 -->
  <div class="home-page">
    <!-- 轮播图区域 - 由后台 Banner 管理下发，支持图片/跳转/自动播放开关 -->
    <section class="banner-section" v-if="banners.length > 0">
      <el-carousel height="520px" :interval="bannerInterval" :autoplay="bannerAutoplay" arrow="hover">
        <el-carousel-item v-for="banner in banners" :key="banner.id">
          <div
            class="banner-slide"
            :style="banner.image ? { backgroundImage: `linear-gradient(90deg, rgba(255,255,255,.92), rgba(255,255,255,.55)), url(${banner.image})`, backgroundSize: 'cover', backgroundPosition: 'center' } : {}"
          >
            <!-- 简洁背景装饰：柔和大圆环 + 光斑 -->
            <div class="banner-bg-deco">
              <div class="deco-ring ring-1"></div>
              <div class="deco-ring ring-2"></div>
              <div class="deco-ring ring-3"></div>
              <div class="deco-blob blob-1"></div>
              <div class="deco-blob blob-2"></div>
            </div>
            <!-- 中央内容 -->
            <div class="banner-content">
              <h2 class="banner-title">{{ banner.title }}</h2>
              <div class="banner-actions" v-if="banner.link">
                <el-button type="primary" size="large" round class="banner-btn" @click="goBanner(banner)">
                  {{ banner.btn || '查看详情' }}
                </el-button>
              </div>
            </div>
          </div>
        </el-carousel-item>
      </el-carousel>
    </section>
    <!-- 后台未配置 Banner 时的空态，不再展示虚假赛事 -->
    <section class="banner-section banner-empty" v-else>
      <el-empty description="暂无轮播内容" :image-size="80" />
    </section>

    <!-- 数据统计横幅 - 全宽 -->
    <section class="stats-section" ref="statsSection">
      <div class="stats-container">
        <div class="stat-item" v-for="(stat, idx) in stats" :key="idx">
          <div class="stat-icon-wrap">
            <el-icon :size="28" class="stat-icon"><component :is="stat.icon" /></el-icon>
          </div>
          <div class="stat-number">
            <span class="stat-count" :data-target="stat.number">{{ animatedStats[idx]?.display || '0' }}</span>
            <span class="stat-unit">{{ stat.unit }}</span>
          </div>
          <div class="stat-label">{{ stat.label }}</div>
        </div>
      </div>
    </section>

    <!-- 页面内容容器 - max-width: 1200px 居中 -->
    <div class="home-content">
      <!-- 三大核心入口卡片 -->
      <section class="entry-cards-section scroll-animate">
        <div class="entry-cards-grid" v-if="entryCards.length > 0">
          <div class="entry-card" v-for="(entry, idx) in entryCards" :key="idx" @click="$router.push(entry.path)" :style="{ animationDelay: `${idx * 0.15}s` }">
            <div class="entry-card-inner">
              <div class="entry-card-icon" :style="entryIconStyle(entry)">
                <el-icon :size="36"><component :is="entry.icon" /></el-icon>
              </div>
              <h3 class="entry-card-title">{{ entry.title }}</h3>
              <p class="entry-card-desc">{{ entry.desc }}</p>
              <el-button type="primary" round class="entry-card-btn">
                {{ entry.btnText }}
              </el-button>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无入口配置" :image-size="70" />
      </section>

      <!-- 热门赛事 -->
      <section class="hot-contest-section scroll-animate">
        <div class="section-header-center">
          <h2 class="section-title">热门赛事</h2>
          <div class="section-divider"></div>
          <p class="section-subtitle">精选优质编程赛事，助力孩子站上更大的舞台</p>
        </div>
        <div class="contest-scroll-wrapper" v-if="hotContests.length > 0">
          <div class="contest-scroll-track">
            <div class="contest-card scroll-animate-item" v-for="(contest, idx) in hotContests" :key="idx" @click="$router.push(contest.path)" :style="{ animationDelay: `${idx * 0.1}s` }">
              <div class="contest-card-cover" :style="contest.cover ? { backgroundImage: `url(${contest.cover})` } : { background: contest.bg }">
                <div class="contest-status" :class="contest.statusClass">{{ contest.status }}</div>
                <div class="contest-cover-icon">
                  <el-icon :size="48"><component :is="contest.icon" /></el-icon>
                </div>
                <div class="contest-cover-tags">
                  <span class="cover-tag" v-for="(tag, tIdx) in contest.tags" :key="tIdx">{{ tag }}</span>
                </div>
              </div>
              <div class="contest-card-body">
                <h4>{{ contest.title }}</h4>
                <p>{{ contest.desc }}</p>
                <div class="contest-meta">
                  <span><el-icon size="14"><User /></el-icon> {{ contest.participants }}</span>
                  <span><el-icon size="14"><Timer /></el-icon> {{ contest.deadline }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无赛事" :image-size="70" />
      </section>

      <!-- 学习中心 - 标签切换（后台 showCourseCategory 开关控制） -->
      <section class="learning-section scroll-animate" v-if="switches.showCourseCategory">
        <div class="section-header-center">
          <h2 class="section-title">学习中心</h2>
          <div class="section-divider"></div>
        </div>
        <div class="learning-tabs">
          <div
            class="tab-item"
            v-for="tab in learningTabs"
            :key="tab.key"
            :class="{ active: activeTab === tab.key }"
            @click="activeTab = tab.key"
          >
            {{ tab.label }}
          </div>
        </div>
        <div class="course-grid scroll-animate" v-if="filteredCourses.length > 0">
          <CourseCard v-for="(course, cIdx) in filteredCourses" :key="course.id" :course="course" class="scroll-animate-item" :style="{ animationDelay: `${cIdx * 0.08}s` }" />
        </div>
        <el-empty v-else description="暂无课程" :image-size="70" />
      </section>

      <!-- 携手共创（后台 showPartner 开关控制） -->
      <section class="partner-section scroll-animate" v-if="switches.showPartner">
        <div class="section-header-center">
          <h2 class="section-title">携手共创 · 产教融通</h2>
          <p class="section-subtitle">
            码高及合作伙伴携手学校、企业与各组织单位，共同推动人才培养、科研合作、成果转化，
            着力构建产教融通新格局，形成协同育人新生态。
          </p>
        </div>
        <div class="partner-marquee" v-if="partnerLogos.length > 0">
          <div class="marquee-track">
            <div class="partner-logo" v-for="(logo, idx) in partnerLogos" :key="`a-${idx}`">
              <img v-if="logo.url" :src="logo.url" :alt="logo.name" class="logo-img" />
              <div v-else class="logo-placeholder" :style="{ background: logo.color }">
                <span>{{ logo.name }}</span>
              </div>
            </div>
            <div class="partner-logo" v-for="(logo, idx) in partnerLogos" :key="`b-${idx}`">
              <img v-if="logo.url" :src="logo.url" :alt="logo.name" class="logo-img" />
              <div v-else class="logo-placeholder" :style="{ background: logo.color }">
                <span>{{ logo.name }}</span>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无合作伙伴" :image-size="70" />
      </section>

      <!-- 学术资讯（后台 showNews 开关控制） -->
      <section class="news-section scroll-animate" v-if="switches.showNews">
        <div class="section-header-row">
          <div class="section-header-center" style="margin-bottom: 0;">
            <h2 class="section-title">学术资讯</h2>
            <p class="section-subtitle">洞察前沿动态，聚焦热点资讯，感知行业脉络，引领启智未来</p>
          </div>
          <el-button text type="primary" class="section-more" @click="$router.push('/news')">
            查看更多 <el-icon class="arrow-icon"><ArrowRight /></el-icon>
          </el-button>
        </div>
        <div class="news-grid" v-if="newsList.length > 0">
          <div class="news-card news-featured scroll-animate-item" @click="$router.push('/news')">
            <div class="news-image" style="background-image: url('/images/news/news-2.png'); background-size: cover; background-position: center;"></div>
            <div class="news-body">
              <h4>{{ newsList[0].title }}</h4>
              <p>{{ newsList[0].summary }}</p>
            </div>
          </div>
          <div class="news-list">
            <div class="news-item scroll-animate-item" v-for="(news, idx) in newsList.slice(1)" :key="idx" @click="$router.push('/news')" :style="{ animationDelay: `${idx * 0.1}s` }">
              <div class="news-thumb" :style="{ backgroundImage: `url('/images/news/news-${idx + 1}.png')` }"></div>
              <div class="news-text">
                <h5>{{ news.title }}</h5>
                <p>{{ news.summary }}</p>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无资讯" :image-size="70" />
      </section>
    </div>

    <!-- CTA号召行动区域 - 全宽 -->
    <section class="cta-section">
      <div class="cta-bg-decoration">
        <div class="cta-circle cta-circle-1"></div>
        <div class="cta-circle cta-circle-2"></div>
        <div class="cta-circle cta-circle-3"></div>
      </div>
      <div class="cta-content scroll-animate">
        <h2 class="cta-title">开启孩子的编程之旅</h2>
        <p class="cta-subtitle">立即加入码高，与百万学员一起探索编程的无限可能</p>
        <div class="cta-buttons">
          <el-button type="primary" size="large" round class="cta-btn-primary" @click="$router.push('/course')">
            <el-icon size="18"><Reading /></el-icon>
            免费试学课程
          </el-button>
          <el-button size="large" round class="cta-btn-secondary" @click="$router.push('/exam')">
            <el-icon size="18"><Trophy /></el-icon>
            浏览热门赛事
          </el-button>
        </div>
        <div class="cta-stats">
          <div class="cta-stat-item">
            <span class="cta-stat-num">多语言</span>
            <span class="cta-stat-label">Scratch / Python / C++</span>
          </div>
          <div class="cta-stat-divider"></div>
          <div class="cta-stat-item">
            <span class="cta-stat-num">学练结合</span>
            <span class="cta-stat-label">课程 · 赛事 · 创作</span>
          </div>
          <div class="cta-stat-divider"></div>
          <div class="cta-stat-item">
            <span class="cta-stat-num">在线编程</span>
            <span class="cta-stat-label">浏览器内直接练习</span>
          </div>
        </div>
      </div>
    </section>

    <!-- 新媒体矩阵 - 全宽深色背景 -->
    <section class="media-matrix-section">
      <div class="media-matrix-content">
        <div class="section-header-center">
          <h2 class="section-title">新媒体矩阵</h2>
          <p class="section-subtitle">关注码高，获取最新编程教育资讯</p>
        </div>
        <div class="media-grid" v-if="mediaMatrix.length > 0">
          <div class="media-item" v-for="(media, idx) in mediaMatrix" :key="idx">
            <div class="media-icon" :style="{ background: media.bg }">
              <el-icon :size="28"><component :is="media.icon" /></el-icon>
            </div>
            <span class="media-name">{{ media.name }}</span>
          </div>
        </div>
        <el-empty v-else description="暂无新媒体配置" :image-size="70" class="media-empty" />
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
/**
 * HomePage - 首页（编程赛事平台风格）
 * 轮播图、数据统计、三大入口、热门赛事、核心功能、学习中心、携手共创、学术资讯、新媒体矩阵
 */
import { ref, computed, onMounted, nextTick, watch } from 'vue'
import { useRouter } from 'vue-router'
import {
  ArrowRight, Trophy, Timer, User, Reading,
} from '@element-plus/icons-vue'
import CourseCard from '@/components/course/CourseCard.vue'
import { getPartners, type PartnerItem } from '@/api/partner'
import { getHomepageConfig, type HomepageConfig } from '@/api/homepage'
import { getBanners } from '@/api/banner'
import { getCourseList } from '@/api/course'
import { getCompetitionList } from '@/api/competition'
import { getNewsList } from '@/api/news'
import { getOSSUrl } from '@/utils/storage'
import type { CourseItem } from '@/types'
import type { CompetitionInfo } from '@/types/editor'

const router = useRouter()



// 轮播图 - 由后台 Banner 管理下发（GET /api/banner?position=home），不再写死
interface BannerSlide {
  id: number
  title: string
  image?: string
  link?: string
  btn?: string
}
const banners = ref<BannerSlide[]>([])

// 轮播参数（后台首页配置可开关）
const bannerAutoplay = ref(true)
const bannerInterval = ref(4000)

// 首页区块开关（后台首页配置可控制：showCourseCategory / showNews / showPartner）
const switches = ref({
  showCourseCategory: true,
  showNews: true,
  showPartner: true,
})

/** 点击轮播 CTA：站内路径走路由，外链新开窗口 */
function goBanner(b: BannerSlide) {
  const link = b.link || ''
  if (!link) return
  if (/^https?:\/\//i.test(link)) {
    window.open(link, '_blank')
  } else {
    router.push(link)
  }
}

/** 加载首页轮播（后台 cms_banner，仅启用项，按 sortOrder 升序） */
async function loadBanners() {
  try {
    const res = await getBanners('home')
    if (res?.code === 200 && Array.isArray(res.data)) {
      banners.value = res.data
        .filter((b: any) => b.status === 1 || b.status === '1')
        .sort((a: any, b: any) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0))
        .map((b: any) => ({
          id: b.id,
          title: b.title,
          image: b.imageUrl || '',
          link: b.linkUrl || '',
          btn: '查看详情',
        }))
    }
  } catch {
    // 保持为空，由模板空态占位（不再回退虚假轮播）
  }
}

// 平台统计：不预设任何虚构运营数字，后台未配置时统一显示中性占位「--」
const defaultStats = [
  { number: '--', unit: '', label: '平台用户量', icon: 'UserFilled' },
  { number: '--', unit: '', label: '活跃学员数', icon: 'TrendCharts' },
  { number: '--', unit: '', label: '覆盖全国省市', icon: 'Collection' },
  { number: '--', unit: '', label: '合作学校机构', icon: 'School' },
  { number: '--', unit: '', label: '编程作品数', icon: 'Medal' },
]

const stats = ref([...defaultStats])

// 默认核心入口卡片（降级方案）
const defaultEntryCards = [
  {
    title: '赛事中心',
    desc: '涵盖Scratch、Python、C++等多赛道编程赛事，在线报名、实时排名，助力孩子走向全国舞台。',
    icon: 'Trophy',
    iconBg: 'linear-gradient(135deg, #165DFF, #36D399)',
    iconImg: '/images/contest/contest-blue.png',
    btnText: '浏览赛事',
    path: '/exam',
  },
  {
    title: '课程中心',
    desc: '从零基础到竞赛进阶，体系化编程课程，名师授课、项目驱动，让孩子学以致用。',
    icon: 'Reading',
    iconBg: 'linear-gradient(135deg, #FF7D00, #FFB84D)',
    iconImg: '/images/contest/contest-orange.png',
    btnText: '开始学习',
    path: '/course',
  },
]

// 核心入口图标背景：有封面图用图，否则按图标名回退到本地插图，再回退渐变
const entryIconImages: Record<string, string> = {
  Trophy: '/images/contest/contest-blue.png',
  Reading: '/images/contest/contest-orange.png',
  EditPen: '/images/contest/contest-green.png',
}
function entryIconStyle(entry: { icon?: string; iconImg?: string; iconBg?: string }): Record<string, string> {
  const img = entry.iconImg || entryIconImages[entry.icon || '']
  if (img) {
    return {
      backgroundImage: `url(${img})`,
      backgroundSize: 'cover',
      backgroundPosition: 'center',
    }
  }
  return { background: entry.iconBg ?? 'linear-gradient(135deg, #165DFF, #36D399)' }
}

interface EntryCard {
  title: string
  desc: string
  icon: string
  iconBg: string
  iconImg?: string
  btnText: string
  path: string
}
const entryCards = ref<EntryCard[]>([...defaultEntryCards])

// 热门赛事：不再内置虚构赛事，接口无数据时由模板显示空态
const hotContests = ref<any[]>([])

// 竞赛状态映射
function mapContestStatus(status: string): { label: string; cls: string } {
  switch (status) {
    case 'IN_PROGRESS':
      return { label: '报名中', cls: 'status-open' }
    case 'NOT_STARTED':
      return { label: '即将开始', cls: 'status-upcoming' }
    case 'ENDED':
      return { label: '已结束', cls: 'status-preparing' }
    default:
      return { label: '筹备中', cls: 'status-preparing' }
  }
}

// 竞赛卡片封面图（按主题配色循环复用）
const contestCovers = [
  '/images/contest/contest-blue.png',
  '/images/contest/contest-purple.png',
  '/images/contest/contest-orange.png',
  '/images/contest/contest-green.png',
  '/images/contest/contest-cyan.png',
  '/images/contest/contest-magenta.png',
]

// 加载热门赛事（失败时保留默认数据）
async function loadHotContests() {
  try {
    const res = await getCompetitionList({ pageNum: 1, pageSize: 8 })
    if (res?.code === 200 && res.data?.list?.length) {
      hotContests.value = res.data.list.slice(0, 8).map((c: CompetitionInfo, idx: number) => {
        const statusInfo = mapContestStatus(c.status)
        return {
          title: c.title,
          desc: c.description,
          status: statusInfo.label,
          statusClass: statusInfo.cls,
          icon: 'Trophy',
          cover: contestCovers[idx % contestCovers.length],
          bg: 'linear-gradient(135deg, #165DFF, #36D399)',
          participants: c.currentParticipants ? `${c.currentParticipants}人已报名` : '敬请期待',
          deadline: c.endTime ? `截止 ${String(c.endTime).slice(0, 10)}` : '',
          path: `/exam/${c.id}`,
          tags: c.allowedBlocks?.slice(0, 3) || ['编程', '竞赛'],
        }
      })
    }
  } catch {
    // 接口无数据或失败：保持空态，由模板显示「暂无数据」
  }
}

const activeTab = ref('all')

// 课程数据：不再内置虚构课程，接口无数据时由模板显示空态
const allCourses = ref<any[]>([])

/**
 * 学习中心标签：从真实课程数据推导，不再借用 cms_category.type。
 * 说明：cms_category.type 是业务域（course/news/practice），与"年龄段/语言标签"语义不同，
 * 之前用 getCategories({type:'age'}) 查询恒为空，导致标签一直是硬编码。
 * 导航菜单也已独立由后台 nav_menu 配置承担，分类 type 不再被混用。
 */
const categoryLabels: Record<string, string> = {
  scratchjr: 'ScratchJr',
  scratch: 'Scratch',
  pythonturtle: 'Python Turtle',
  python: 'Python',
  goc: 'GoC',
  cpp: 'C++',
  ai: 'AI',
}
const learningTabs = computed(() => {
  const cats = Array.from(new Set(allCourses.value.map((c) => c.category).filter(Boolean))) as string[]
  return [
    { key: 'all', label: '全部' },
    { key: 'hot', label: '热门' },
    ...cats.map((key) => ({ key, label: categoryLabels[key] || key })),
  ]
})

// 根据标题推断课程分类（API未返回分类时使用）
function inferCourseCategory(title: string): string {
  const t = title.toLowerCase()
  if (t.includes('scratchjr')) return 'scratchjr'
  if (t.includes('scratch')) return 'scratch'
  if (t.includes('turtle')) return 'pythonturtle'
  if (t.includes('c++') || t.includes('cpp')) return 'cpp'
  if (t.includes('goc')) return 'goc'
  if (t.includes('ai') || t.includes('人工智能') || t.includes('数据分析')) return 'ai'
  if (t.includes('python')) return 'python'
  return 'python'
}

// 加载课程列表（失败时保留默认数据）
async function loadCourses() {
  try {
    const res = await getCourseList({ pageNum: 1, pageSize: 20 })
    if (res?.code === 200 && res.data?.list?.length) {
      allCourses.value = res.data.list.map((c: CourseItem) => ({
        id: c.id,
        title: c.title,
        cover: c.cover || '',
        difficulty: c.difficulty,
        teacherName: c.teacherName || '名师',
        studentCount: c.studentCount || 0,
        rating: c.rating || 0,
        price: c.price || 0,
        category: inferCourseCategory(c.title),
        hot: (c.rating >= 4.8) || (c.studentCount >= 1000),
      }))
    }
  } catch {
    // 接口无数据或失败：保持空态，由模板显示「暂无数据」
  }
}

const filteredCourses = computed(() => {
  if (activeTab.value === 'all') {
    return allCourses.value.slice(0, 8)
  } else if (activeTab.value === 'hot') {
    const hotCourses = allCourses.value.filter(c => c.hot)
    return hotCourses.length > 0 ? hotCourses : allCourses.value.slice().sort((a, b) => b.studentCount - a.studentCount).slice(0, 4)
  } else {
    return allCourses.value.filter(c => c.category === activeTab.value).slice(0, 8)
  }
})

// 合作伙伴：不预置任何未经核实的机构，接口无数据时由模板显示空态
const partnerLogos = ref<Array<{ name: string; color: string; url?: string }>>([])

// Logo 颜色
const logoColors = ['#7B61FF', '#165DFF', '#36D399', '#F53F3F', '#36D399', '#E06800', '#165DFF', '#F53F3F', '#86909C', '#E06800', '#165DFF', '#F53F3F', '#FFD700', '#7B61FF']
function getLogoColor(name: string) {
  let hash = 0
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash)
  }
  return logoColors[Math.abs(hash) % logoColors.length]
}

// 加载合作伙伴Logo
async function loadPartners() {
  try {
    const res = await getPartners()
    if (res?.code === 200 && res.data && res.data.length > 0) {
      partnerLogos.value = res.data
        .filter((p: PartnerItem) => p.status === 'enabled')
        .map((p: PartnerItem) => ({
          name: p.name,
          color: getLogoColor(p.name),
          url: p.logo ? getOSSUrl(p.logo, { width: 160, height: 80, quality: 85 }) : '',
        }))
    }
  } catch {
    // 接口无数据或失败：保持空态，由模板显示「暂无数据」
  }
}

// 资讯：不预置任何未经核实的新闻，接口无数据时显示空态
const newsList = ref<Array<{ title: string; summary: string }>>([])

// 加载新闻资讯
async function loadNews() {
  try {
    const res = await getNewsList({ page: 1, size: 10 })
    if (res?.code === 200 && res.data) {
      const list = res.data.list || res.data
      if (Array.isArray(list) && list.length > 0) {
        newsList.value = list.slice(0, 4).map((item: any) => ({
          title: item.title,
          summary: item.summary || item.content?.slice(0, 80) || '',
        }))
      }
    }
  } catch {
    // 接口无数据或失败：保持空态，由模板显示「暂无数据」
  }
}

// 默认新媒体矩阵（降级方案）
const defaultMediaMatrix = [
  { name: '抖音', icon: 'VideoCamera', bg: '#000' },
  { name: '小红书', icon: 'Document', bg: '#F53F3F' },
  { name: 'B站', icon: 'Monitor', bg: '#4080FF' },
  { name: '微信服务号', icon: 'ChatDotRound', bg: '#36D399' },
  { name: '微信视频号', icon: 'VideoCamera', bg: '#36D399' },
  { name: '知乎', icon: 'Link', bg: '#165DFF' },
  { name: '微博', icon: 'Microphone', bg: '#F53F3F' },
]

const mediaMatrix = ref([...defaultMediaMatrix])

// 加载首页配置（含区块开关、轮播参数）
async function loadHomepageConfig() {
  try {
    const res = await getHomepageConfig()
    if (res?.code === 200 && res.data) {
      const data: HomepageConfig = res.data

      // ===== 区块开关与轮播参数（P0-2：后台真正可控） =====
      if (typeof data.bannerAutoPlay === 'boolean') bannerAutoplay.value = data.bannerAutoPlay
      if (typeof data.bannerInterval === 'number' && data.bannerInterval > 0) bannerInterval.value = data.bannerInterval
      if (typeof data.showCourseCategory === 'boolean') switches.value.showCourseCategory = data.showCourseCategory
      if (typeof data.showNews === 'boolean') switches.value.showNews = data.showNews
      if (typeof data.showPartner === 'boolean') switches.value.showPartner = data.showPartner

      // 统计数据：后台未填的项显示「--」，绝不回落到虚构数字
      if (data.stats) {
        const mk = (v: string | undefined, unit: string) => {
          const n = (v ?? '').toString().trim()
          return { number: n || '--', unit: n ? unit : '' }
        }
        stats.value = [
          { ...mk(data.stats.userCount, '万+'), label: '平台用户量', icon: 'UserFilled' },
          { ...mk(data.stats.activeStudents, '万+'), label: '活跃学员数', icon: 'TrendCharts' },
          { ...mk(data.stats.coveredCities, '%'), label: '覆盖全国省市', icon: 'Collection' },
          { ...mk(data.stats.partnerCount, '+'), label: '合作学校机构', icon: 'School' },
          { ...mk(data.stats.workCount, '万+'), label: '编程作品数', icon: 'Medal' },
        ]
        statsAnimated.value = false
        animatedStats.value = stats.value.map(s => ({ display: Number.isFinite(parseInt(s.number)) ? '0' : '--' }))
      }
      // 核心入口：后台下发空数组时同步清空（配合空态）
      if (Array.isArray(data.entries)) {
        entryCards.value = data.entries.map((entry) => ({
          title: entry.title,
          desc: entry.description,
          icon: entry.icon,
          iconBg: entry.bgColor,
          iconImg: entry.coverUrl ?? undefined,
          btnText: '立即进入',
          path: entry.link,
        }))
      }
      // 新媒体矩阵
      if (Array.isArray(data.media)) {
        mediaMatrix.value = data.media.map((media) => ({
          name: media.name,
          icon: media.icon,
          bg: media.bgColor,
        }))
      }
    }
  } catch {
    // 接口无数据或失败：保持空态，由模板显示「暂无数据」
  }
}

onMounted(() => {
  loadBanners()
  loadPartners()
  loadHomepageConfig()
  loadNews()
  loadCourses()
  loadHotContests()
})

// ==================== 数字滚动动画 ====================
const statsSection = ref<HTMLElement | null>(null)
const animatedStats = ref<{ display: string }[]>(
  stats.value.map(s => ({ display: Number.isFinite(parseInt(s.number)) ? '0' : '--' })),
)
const statsAnimated = ref(false)

function animateCountUp() {
  if (statsAnimated.value) return
  statsAnimated.value = true
  const duration = 2000
  const startTime = performance.now()

  function update(currentTime: number) {
    const elapsed = currentTime - startTime
    const progress = Math.min(elapsed / duration, 1)
    const easeOutQuart = 1 - Math.pow(1 - progress, 4)

    stats.value.forEach((stat, idx) => {
      const target = parseInt(stat.number)
      // 后台未配置时是中性占位「--」，保持占位，不能显示 NaN
      if (!Number.isFinite(target)) {
        animatedStats.value[idx].display = '--'
        return
      }
      const current = Math.round(target * easeOutQuart)
      animatedStats.value[idx].display = String(current)
    })

    if (progress < 1) {
      requestAnimationFrame(update)
    }
  }
  requestAnimationFrame(update)
}

// ==================== 滚动触发动画 ====================
onMounted(() => {
  nextTick(() => {
    // 数字动画 IntersectionObserver
    if (statsSection.value) {
      const numObserver = new IntersectionObserver((entries) => {
        entries.forEach(entry => {
          if (entry.isIntersecting) {
            animateCountUp()
            numObserver.disconnect()
          }
        })
      }, { threshold: 0.3 })
      numObserver.observe(statsSection.value)
    }

    // fadeInUp IntersectionObserver
    const scrollObserver = new IntersectionObserver((entries) => {
      entries.forEach(entry => {
        if (entry.isIntersecting) {
          entry.target.classList.add('is-visible')
          scrollObserver.unobserve(entry.target)
        }
      })
    }, { threshold: 0.1, rootMargin: '0px 0px -50px 0px' })

    document.querySelectorAll('.scroll-animate, .scroll-animate-item').forEach(el => {
      scrollObserver.observe(el)
    })
  })
})

// 标签切换后重新观察新出现的课程卡片
watch(activeTab, () => {
  nextTick(() => {
    const courseGrid = document.querySelector('.course-grid')
    if (courseGrid) {
      courseGrid.querySelectorAll('.scroll-animate-item').forEach(el => {
        el.classList.add('is-visible')
      })
    }
  })
})
</script>

<style scoped lang="scss">
@use "sass:color";
// ==================== 变量定义 ====================
$cyan: #36D399;
$orange: #FF7D00;
$bg-dark: #0D1117;
$shadow-soft: 0 8px 24px rgba(22, 93, 255, 0.08);
$shadow-hover: 0 12px 32px rgba(22, 93, 255, 0.14);
$content-max-width: 1280px;
$section-gap: 56px;

// ==================== 页面基础 ====================
.home-page {
  background: $bg-page;
  min-height: 100vh;
}

.home-content {
  max-width: $content-max-width;
  margin: 0 auto;
  padding: $section-gap 24px 60px;
}

// ==================== Banner轮播 - 简洁大气浅色风格（全宽） ====================
.banner-section {
  width: 100%;
  overflow: hidden;

  &.banner-empty {
    min-height: 260px;
    display: flex;
    align-items: center;
    justify-content: center;
    background: linear-gradient(135deg, #f7f9ff, #eef3ff);
  }

  :deep(.el-carousel__indicators) {
    bottom: 24px;
    .el-carousel__indicator {
      padding: 0 5px;
      .el-carousel__button {
        width: 28px;
        height: 4px;
        border-radius: 2px;
        background: rgba(22, 93, 255, 0.18);
        transition: width 0.3s ease, background 0.3s ease;
      }
      &.is-active .el-carousel__button {
        width: 40px;
        background: rgba(22, 93, 255, 0.9);
      }
    }
  }
}

.banner-slide {
  height: 100%;
  display: flex;
  align-items: center;
  color: $text-title;
  position: relative;
  overflow: hidden;
  padding: 0 8%;
  background: linear-gradient(120deg, #EAF1FF 0%, #F5F8FF 45%, #EAF5FF 100%);
}

// 简洁背景装饰：柔和大圆环 + 光斑
.banner-bg-deco {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;

  .deco-ring {
    position: absolute;
    border-radius: 50%;
    border: 1px solid rgba(22, 93, 255, 0.08);
    &.ring-1 { width: 560px; height: 560px; top: -220px; right: -100px; }
    &.ring-2 { width: 420px; height: 420px; top: -150px; right: -30px; }
    &.ring-3 { width: 280px; height: 280px; top: -80px; right: 50px; }
  }
  .deco-blob {
    position: absolute;
    border-radius: 50%;
    filter: blur(70px);
    &.blob-1 { width: 440px; height: 440px; top: -140px; right: -60px; background: rgba(64, 128, 255, 0.14); }
    &.blob-2 { width: 380px; height: 380px; bottom: -160px; left: 28%; background: rgba(54, 211, 153, 0.10); }
  }
}

// 中央内容
.banner-content {
  position: relative;
  z-index: 2;
  max-width: 680px;

  .banner-badge {
    display: inline-flex;
    align-items: center;
    gap: 8px;
    padding: 6px 16px;
    background: rgba(22, 93, 255, 0.08);
    border: 1px solid rgba(22, 93, 255, 0.18);
    border-radius: 20px;
    font-size: 13px;
    color: #165DFF;
    margin-bottom: 20px;
    letter-spacing: 0.5px;
  }

  .banner-title {
    font-size: 46px;
    font-weight: 800;
    color: $text-title;
    margin-bottom: 18px;
    letter-spacing: 1px;
    line-height: 1.25;
  }

  .banner-desc {
    font-size: 16px;
    color: $text-body;
    margin-bottom: 24px;
    line-height: 1.8;
    max-width: 520px;
  }
}

.banner-actions {
  margin-top: 4px;
}

.banner-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 28px;

  .meta-item {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    font-size: 13px;
    color: $text-muted;
    .el-icon {
      color: #165DFF;
    }
  }
}

.banner-btn {
  font-size: 15px;
  font-weight: 600;
  padding: 12px 40px;
  background: #165DFF;
  color: #fff;
  border: none;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1), box-shadow 0.3s cubic-bezier(0.4, 0, 0.2, 1), background 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 6px 20px rgba(22, 93, 255, 0.28);
  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 10px 30px rgba(22, 93, 255, 0.4);
    background: #4080FF;
  }
}

// ==================== 数据统计（全宽） ====================
.stats-section {
  background: linear-gradient(180deg, #fff 0%, #E8F0FF 100%);
  padding: 56px 0;
  border-bottom: 1px solid #F2F3F5;
  position: relative;
  overflow: hidden;
}

.stats-section::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 3px;
  background: linear-gradient(90deg, $primary-color, $cyan, $orange, $success-color, $primary-color);
  background-size: 200% 100%;
  animation: gradientMove 3s linear infinite;
  will-change: transform;
}

@keyframes gradientMove {
  0% { background-position: 0% 50%; }
  100% { background-position: 200% 50%; }
}

.stats-container {
  max-width: $content-max-width;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  justify-content: space-around;
  align-items: center;
}

.stat-item {
  text-align: center;
  position: relative;
  padding: 0 16px;
  &:not(:last-child)::after {
    content: '';
    position: absolute;
    right: -20px;
    top: 50%;
    transform: translateY(-50%);
    width: 1px;
    height: 50px;
    background: linear-gradient(180deg, transparent, #E5E6EB, transparent);
  }
}

.stat-icon-wrap {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, rgba(22, 93, 255, 0.08), rgba(54, 207, 201, 0.08));
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 12px;
  .stat-icon {
    color: $primary-color;
  }
}

.stat-number {
  font-size: 40px;
  font-weight: 800;
  color: $text-title;
  line-height: 1.2;
  .stat-unit {
    font-size: 20px;
    font-weight: 600;
    color: $text-muted;
    margin-left: 2px;
  }
}

.stat-label {
  font-size: 14px;
  color: $text-muted;
  margin-top: 8px;
}

// ==================== 三大核心入口卡片 ====================
.entry-cards-section {
  margin-bottom: $section-gap;
}

.entry-cards-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
  perspective: 1000px;
}

.entry-card {
  position: relative;
  border-radius: 16px;
  cursor: pointer;
  background: $bg-white;
  border: 1px solid #EFF1F5;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.04);
  transition: transform 0.3s ease, box-shadow 0.3s ease, border-color 0.3s ease;
  overflow: hidden;

  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 16px 40px rgba(22, 93, 255, 0.12);
    border-color: rgba(22, 93, 255, 0.2);
  }
}

.entry-card-inner {
  background: $bg-white;
  padding: 40px 32px;
  text-align: center;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.entry-card-icon {
  position: relative;
  overflow: hidden;
  width: 72px;
  height: 72px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 20px;
  color: #fff;
  transition: transform 0.3s ease;
  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, rgba(15, 23, 42, 0.28), rgba(15, 23, 42, 0.52));
    z-index: 0;
  }
  .el-icon {
    position: relative;
    z-index: 1;
  }
  .entry-card:hover & {
    transform: scale(1.06);
  }
}

.entry-card-title {
  font-size: 20px;
  font-weight: 700;
  color: $text-title;
  margin-bottom: 12px;
}

.entry-card-desc {
  font-size: 14px;
  color: $text-body;
  line-height: 1.7;
  margin-bottom: 24px;
  min-height: 66px;
}

.entry-card-btn {
  background: $orange !important;
  border-color: $orange !important;
  color: #fff !important;
  font-weight: 600;
  padding: 10px 28px;
  transition: background 0.25s ease, border-color 0.25s ease, transform 0.25s ease, box-shadow 0.25s ease;

  &:hover {
    background: color.adjust($orange, $lightness: -8%) !important;
    border-color: color.adjust($orange, $lightness: -8%) !important;
    transform: translateY(-1px);
    box-shadow: 0 4px 12px rgba(255, 125, 0, 0.3);
  }
}

// ==================== 热门赛事 ====================
.hot-contest-section {
  margin-bottom: $section-gap;
}

.contest-scroll-wrapper {
  overflow-x: auto;
  padding-bottom: 8px;
  margin: 0 -12px;
  padding-left: 12px;
  padding-right: 12px;
  scrollbar-width: thin;
  scrollbar-color: rgba(22, 93, 255, 0.2) transparent;
  &::-webkit-scrollbar {
    height: 6px;
  }
  &::-webkit-scrollbar-track {
    background: transparent;
  }
  &::-webkit-scrollbar-thumb {
    background: rgba(22, 93, 255, 0.2);
    border-radius: 3px;
  }
}

.contest-scroll-track {
  display: flex;
  gap: 20px;
  width: max-content;
}

.contest-card {
  width: 280px;
  flex-shrink: 0;
  background: $bg-white;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
  transition: transform 0.35s cubic-bezier(0.34, 1.56, 0.64, 1), box-shadow 0.35s ease;

  &:hover {
    transform: translateY(-8px) scale(1.02);
    box-shadow: 0 16px 40px rgba(22, 93, 255, 0.15);
  }
}

.contest-card-cover {
  height: 160px;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  color: #fff;
  overflow: hidden;

  &::after {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(180deg, transparent 40%, rgba(0,0,0,0.3) 100%);
  }

  .contest-cover-icon {
    opacity: 0.5;
    transform: scale(1.2);
    transition: transform, box-shadow, background-color, border-color, color, opacity 0.4s ease;
    .contest-card:hover & {
      opacity: 0.8;
      transform: scale(1.4) rotate(5deg);
    }
  }
}

.contest-cover-tags {
  position: absolute;
  bottom: 12px;
  left: 12px;
  display: flex;
  gap: 6px;
  z-index: 2;

  .cover-tag {
    padding: 3px 10px;
    border-radius: 10px;
    font-size: 11px;
    font-weight: 500;
    background: rgba(255, 255, 255, 0.25);
    color: #fff;
    backdrop-filter: blur(4px);
  }
}

.contest-status {
  position: absolute;
  top: 12px;
  right: 12px;
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
  z-index: 2;

  &.status-open {
    background: rgba(255, 255, 255, 0.25);
    color: #fff;
  }
  &.status-upcoming {
    background: rgba(255, 255, 255, 0.2);
    color: #fff;
  }
  &.status-preparing {
    background: rgba(255, 255, 255, 0.15);
    color: rgba(255, 255, 255, 0.8);
  }
}

.contest-card-body {
  padding: 20px;

  h4 {
    font-size: 16px;
    font-weight: 700;
    color: $text-title;
    margin-bottom: 8px;
    line-height: 1.4;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  p {
    font-size: 13px;
    color: $text-muted;
    line-height: 1.6;
    margin-bottom: 16px;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
}

.contest-meta {
  display: flex;
  flex-direction: column;
  gap: 6px;

  span {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: $text-muted;
  }
}

// ==================== 通用标题 ====================
.section-header-center {
  text-align: center;
  margin-bottom: 40px;
  .section-title {
    font-size: 32px;
    font-weight: 800;
    color: $text-title;
    letter-spacing: 1px;
    &.light {
      color: #fff;
    }
  }
  .section-divider {
    width: 48px;
    height: 4px;
    background: linear-gradient(90deg, $primary-color, $cyan);
    border-radius: 2px;
    margin: 16px auto 0;
  }
  .section-subtitle {
    font-size: 15px;
    color: $text-muted;
    margin-top: 12px;
    line-height: 1.6;
    max-width: 600px;
    margin-left: auto;
    margin-right: auto;
    &.light {
      color: rgba(255, 255, 255, 0.7);
    }
  }
}

.section-header-row {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
  .section-header-center {
    text-align: left;
    margin-bottom: 0;
    .section-divider {
      margin: 16px 0 0;
    }
  }
}

.section-more {
  font-size: 14px;
  font-weight: 500;
  color: $primary-color;
  .arrow-icon {
    margin-left: 2px;
    transition: transform 0.25s ease;
  }
  &:hover .arrow-icon {
    transform: translateX(4px);
  }
}

// ==================== 学习中心 ====================
.learning-section {
  margin-bottom: $section-gap;
  margin-top: $section-gap;
}

.learning-tabs {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-bottom: 32px;
  flex-wrap: wrap;
}

.tab-item {
  padding: 10px 24px;
  border-radius: 24px;
  font-size: 14px;
  font-weight: 500;
  color: $text-body;
  background: $bg-white;
  border: 1px solid #E5E6EB;
  cursor: pointer;
  transition: color 0.25s ease, background 0.25s ease, border-color 0.25s ease, box-shadow 0.25s ease;
  &:hover {
    border-color: $primary-color;
    color: $primary-color;
  }
  &.active {
    background: $primary-color;
    color: #fff;
    border-color: $primary-color;
    box-shadow: 0 4px 12px rgba(22, 93, 255, 0.25);
  }
}

.course-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

// ==================== 携手共创 ====================
.partner-section {
  margin-bottom: $section-gap;
}

.partner-marquee {
  overflow: hidden;
  padding: 24px 0;
  background: $bg-white;
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
}

.marquee-track {
  display: flex;
  gap: 32px;
  animation: marquee 30s linear infinite;
  width: max-content;
  will-change: transform;
  &:hover {
    animation-play-state: paused;
  }
}

.partner-logo {
  flex-shrink: 0;
}

.logo-img {
  width: 120px;
  height: 60px;
  border-radius: 8px;
  object-fit: contain;
  background: $bg-white;
  padding: 4px;
  border: 1px solid #E5E6EB;
  opacity: 0.85;
  transition: opacity 0.25s ease;
  &:hover {
    opacity: 1;
  }
}

.logo-placeholder {
  width: 120px;
  height: 60px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 14px;
  font-weight: 600;
  opacity: 0.85;
  transition: opacity 0.25s ease;
  &:hover {
    opacity: 1;
  }
}

@keyframes marquee {
  0% { transform: translateX(0); }
  100% { transform: translateX(-50%); }
}

// ==================== 学术资讯 ====================
.news-section {
  margin-bottom: $section-gap;
}

.news-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 24px;
}

.news-featured {
  background: $bg-white;
  border-radius: 16px;
  overflow: hidden;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  &:hover {
    transform: translateY(-4px);
    box-shadow: $shadow-hover;
  }
}

.news-image {
  height: 200px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  background-size: cover;
  background-position: center;
}

.news-body {
  padding: 24px;
  h4 {
    font-size: 18px;
    font-weight: 700;
    color: $text-title;
    margin-bottom: 12px;
    line-height: 1.5;
  }
  p {
    font-size: 14px;
    color: $text-muted;
    line-height: 1.7;
  }
}

.news-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.news-item {
  display: flex;
  gap: 16px;
  padding: 20px 24px;
  background: $bg-white;
  border-radius: 16px;
  cursor: pointer;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.04);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  &:hover {
    transform: translateY(-2px);
    box-shadow: $shadow-hover;
  }
}

.news-thumb {
  width: 104px;
  height: 78px;
  border-radius: 10px;
  background-size: cover;
  background-position: center;
  flex-shrink: 0;
}

.news-text {
  h5 {
    font-size: 15px;
    font-weight: 600;
    color: $text-title;
    margin-bottom: 8px;
    line-height: 1.5;
  }
  p {
    font-size: 13px;
    color: $text-muted;
    line-height: 1.6;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }
}

// ==================== CTA 号召行动区域 ====================
.cta-section {
  position: relative;
  background: linear-gradient(120deg, #EAF1FF 0%, #F5F8FF 50%, #EAF5FF 100%);
  padding: 90px 24px;
  overflow: hidden;
}

.cta-bg-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;

  .cta-circle {
    position: absolute;
    border-radius: 50%;
    border: 1px solid rgba(22, 93, 255, 0.08);
    animation: ctaPulse 6s ease-in-out infinite;
    will-change: transform;

    &.cta-circle-1 {
      width: 400px;
      height: 400px;
      top: -100px;
      right: -100px;
      animation-delay: 0s;
    }
    &.cta-circle-2 {
      width: 300px;
      height: 300px;
      bottom: -80px;
      left: -80px;
      animation-delay: -2s;
    }
    &.cta-circle-3 {
      width: 200px;
      height: 200px;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      animation-delay: -4s;
    }
  }
}

@keyframes ctaPulse {
  0%, 100% { transform: scale(1); opacity: 0.5; }
  50% { transform: scale(1.1); opacity: 1; }
}

.cta-content {
  position: relative;
  z-index: 2;
  max-width: 700px;
  margin: 0 auto;
  text-align: center;
}

.cta-title {
  font-size: 38px;
  font-weight: 800;
  color: $text-title;
  margin-bottom: 16px;
  letter-spacing: 1px;
}

.cta-subtitle {
  font-size: 16px;
  color: $text-body;
  margin-bottom: 32px;
  line-height: 1.6;
}

.cta-buttons {
  display: flex;
  justify-content: center;
  gap: 16px;
  margin-bottom: 40px;
  flex-wrap: wrap;
}

.cta-btn-primary {
  background: #165DFF !important;
  border: none !important;
  color: #fff !important;
  font-size: 16px;
  font-weight: 600;
  padding: 14px 36px;
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1), box-shadow 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  box-shadow: 0 6px 20px rgba(22, 93, 255, 0.3);

  &:hover {
    transform: translateY(-3px) scale(1.03);
    box-shadow: 0 8px 30px rgba(22, 93, 255, 0.42);
  }
}

.cta-btn-secondary {
  background: transparent !important;
  border: 2px solid rgba(22, 93, 255, 0.35) !important;
  color: #165DFF !important;
  font-size: 16px;
  font-weight: 600;
  padding: 14px 36px;
  transition: color 0.3s ease, background 0.3s ease, border-color 0.3s ease, transform 0.3s ease;

  &:hover {
    background: rgba(22, 93, 255, 0.06) !important;
    border-color: #165DFF !important;
    color: #165DFF !important;
    transform: translateY(-2px);
  }
}

.cta-stats {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 24px;
  flex-wrap: wrap;
}

.cta-stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.cta-stat-num {
  font-size: 22px;
  font-weight: 700;
  color: $text-title;
}

.cta-stat-label {
  font-size: 13px;
  color: $text-muted;
}

.cta-stat-divider {
  width: 1px;
  height: 30px;
  background: rgba(22, 93, 255, 0.15);
}

// ==================== 新媒体矩阵 ====================
.media-matrix-section {
  background: #F5F7FA;
  padding: 72px 0;
  border-top: 1px solid #EFF1F5;

  .media-empty :deep(.el-empty__description p) {
    color: #86909c;
  }
}

.media-matrix-content {
  max-width: $content-max-width;
  margin: 0 auto;
  padding: 0 24px;
}

.media-grid {
  display: flex;
  justify-content: center;
  gap: 32px;
  flex-wrap: wrap;
}

.media-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: transform 0.25s ease;
  &:hover {
    transform: translateY(-4px);
  }
}

.media-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  transition: box-shadow 0.25s ease;
  &:hover {
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.3);
  }
}

.media-name {
  font-size: 14px;
  color: $text-body;
}

// ==================== 滚动触发动画 ====================
// 内容默认可见，避免依赖 IntersectionObserver 导致区块不可见
.scroll-animate,
.scroll-animate-item {
  opacity: 1;
  transform: none;
  transition: opacity 0.7s cubic-bezier(0.4, 0, 0.2, 1), transform 0.7s cubic-bezier(0.4, 0, 0.2, 1);
}

.scroll-animate.is-visible,
.scroll-animate-item.is-visible {
  opacity: 1;
  transform: none;
}

// ==================== 响应式适配 ====================
@media (max-width: 1200px) {
  .home-content {
    padding: 32px 24px 48px;
  }
  .stats-container {
    padding: 0 24px;
  }
  .contest-card {
    width: 260px;
  }
  .course-grid {
    grid-template-columns: repeat(2, 1fr);
  }
  .news-grid {
    grid-template-columns: 1fr;
  }
  .stat-item:not(:last-child)::after {
    right: -12px;
  }
}

@media (max-width: 768px) {
  .home-content {
    padding: 20px 16px 40px;
  }
  .stats-container {
    flex-wrap: wrap;
    gap: 24px;
    padding: 0 16px;
  }
  .stat-item:not(:last-child)::after {
    display: none;
  }
  .entry-cards-grid {
    grid-template-columns: 1fr;
  }
  .course-grid {
    grid-template-columns: 1fr;
  }
  .banner-slide {
    padding: 0 24px;
    justify-content: center;
  }
  .banner-content {
    text-align: center;
    .banner-title {
      font-size: 26px;
    }
    .banner-desc {
      font-size: 14px;
    }
    .banner-badge {
      font-size: 12px;
      padding: 4px 12px;
    }
  }
  .banner-meta {
    justify-content: center;
    .meta-item {
      font-size: 12px;
    }
  }
  .section-header-row {
    flex-direction: column;
    gap: 16px;
  }
  .media-grid {
    gap: 20px;
  }
  .media-matrix-content {
    padding: 0 16px;
  }
  .cta-title {
    font-size: 26px;
  }
  .cta-buttons {
    flex-direction: column;
    align-items: center;
  }
}

// ==================== 减少动画偏好 ====================
@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
