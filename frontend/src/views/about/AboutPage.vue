<template>
  <!-- 关于我们页面 -->
  <div class="about-page">
    <!-- 顶部 Banner / Hero区域 -->
    <div class="about-banner">
      <div class="banner-content">
        <div class="banner-badge">
          <el-icon><Star /></el-icon>
          码高教育 · 关于我们
        </div>
        <h1 class="banner-title">专注少儿编程教育</h1>
        <p class="banner-subtitle">培养未来科技人才，让每个孩子都能享受优质的编程教育</p>
        <div class="banner-stats">
          <div class="stat-item">
            <span class="stat-num">10万+</span>
            <span class="stat-label">累计学员</span>
          </div>
          <div class="stat-item">
            <span class="stat-num">20+</span>
            <span class="stat-label">覆盖城市</span>
          </div>
          <div class="stat-item">
            <span class="stat-num">9年</span>
            <span class="stat-label">深耕教育</span>
          </div>
        </div>
      </div>
      <div class="banner-decoration">
        <div class="deco-circle c1"></div>
        <div class="deco-circle c2"></div>
        <div class="deco-circle c3"></div>
      </div>
    </div>

    <div class="page-container">
      <!-- 平台简介 + 使命愿景 -->
      <section class="about-section intro-section">
        <div class="section-header">
          <h2 class="section-title">平台简介</h2>
          <div class="section-line"></div>
        </div>
        <div class="intro-content">
          <div class="intro-text">
            <h3>{{ introTitle }}</h3>
            <p>{{ introContent }}</p>
            <div class="intro-values">
              <div class="value-item" v-for="(val, idx) in values" :key="idx">
                <div class="value-icon" :style="{ background: val.bg }">
                  <el-icon :size="28" :color="val.color"><component :is="val.icon" /></el-icon>
                </div>
                <div class="value-label">{{ val.label }}</div>
                <div class="value-desc">{{ val.desc }}</div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- 核心优势 -->
      <section class="about-section advantages-section">
        <div class="section-header">
          <h2 class="section-title">核心优势</h2>
          <div class="section-line"></div>
        </div>
        <div class="advantages-grid">
          <div class="advantage-card" v-for="(item, idx) in advantages" :key="idx" :style="{ animationDelay: `${idx * 0.1}s` }">
            <div class="adv-icon" :style="{ background: item.bg }">
              <el-icon :size="32" color="#fff"><component :is="item.icon" /></el-icon>
            </div>
            <h4>{{ item.title }}</h4>
            <p>{{ item.desc }}</p>
          </div>
        </div>
      </section>

      <!-- 发展历程 - 横向时间线 -->
      <section class="about-section timeline-section">
        <div class="section-header">
          <h2 class="section-title">发展历程</h2>
          <div class="section-line"></div>
        </div>
        <div v-if="timeline.length > 0" class="horizontal-timeline">
          <div class="timeline-track"></div>
          <div class="timeline-items">
            <div
              class="timeline-node"
              v-for="(item, idx) in timeline"
              :key="idx"
              :class="{ active: item.year === '2026' }"
            >
              <div class="node-dot" :style="{ background: item.color || '#165DFF' }">
                <el-icon :size="14" color="#fff"><component :is="item.icon || 'Check'" /></el-icon>
              </div>
              <div class="node-year">{{ item.year }}</div>
              <div class="node-title">{{ item.title }}</div>
              <div class="node-desc">{{ item.desc }}</div>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">暂无数据</div>
      </section>

      <!-- 团队成员 -->
      <section class="about-section team-section">
        <div class="section-header">
          <h2 class="section-title">核心团队</h2>
          <div class="section-line"></div>
        </div>
        <div v-if="teamMembers.length > 0" class="team-grid">
          <div class="team-card" v-for="(member, idx) in teamMembers" :key="idx" :style="{ animationDelay: `${idx * 0.1}s` }">
            <div class="team-avatar">
              <img v-if="member.avatar" :src="member.avatar" :alt="member.name" class="avatar-img" />
              <div v-else class="avatar-placeholder" :style="{ background: member.bg }">
                <el-icon :size="36" color="#fff"><UserFilled /></el-icon>
              </div>
              <div class="avatar-ring"></div>
            </div>
            <h4>{{ member.name }}</h4>
            <span class="team-role">{{ member.role }}</span>
            <p>{{ member.desc }}</p>
            <div class="team-social">
              <el-icon :size="16"><Share /></el-icon>
            </div>
          </div>
        </div>
        <div v-else class="empty-state">暂无数据</div>
      </section>

      <!-- 合作伙伴/合作机构 -->
      <section class="about-section partners-section">
        <div class="section-header">
          <h2 class="section-title">合作伙伴 & 合作机构</h2>
          <div class="section-line"></div>
        </div>
        <template v-if="hasPartnerData">
          <div class="cooperation-categories">
            <div class="coop-category">
              <h4 class="coop-category-title">
                <el-icon :size="18"><School /></el-icon>
                合作学校
              </h4>
              <div class="coop-cards">
                <div class="coop-card" v-for="(school, idx) in partnerSchools" :key="idx">
                  <div class="coop-icon" :style="{ background: 'linear-gradient(135deg, #165DFF, #4080FF)' }">
                    <el-icon :size="22" color="#fff"><School /></el-icon>
                  </div>
                  <span class="coop-name">{{ school.name }}</span>
                </div>
                <div v-if="partnerSchools.length === 0" class="empty-state empty-state--inline">暂无数据</div>
              </div>
            </div>
            <div class="coop-category">
              <h4 class="coop-category-title">
                <el-icon :size="18"><OfficeBuilding /></el-icon>
                合作科技馆
              </h4>
              <div class="coop-cards">
                <div class="coop-card" v-for="(museum, idx) in partnerMuseums" :key="idx">
                  <div class="coop-icon" :style="{ background: 'linear-gradient(135deg, #FF7D00, #FF9500)' }">
                    <el-icon :size="22" color="#fff"><OfficeBuilding /></el-icon>
                  </div>
                  <span class="coop-name">{{ museum.name }}</span>
                </div>
                <div v-if="partnerMuseums.length === 0" class="empty-state empty-state--inline">暂无数据</div>
              </div>
            </div>
            <div class="coop-category">
              <h4 class="coop-category-title">
                <el-icon :size="18"><DataBoard /></el-icon>
                科研机构
              </h4>
              <div class="coop-cards">
                <div class="coop-card" v-for="(inst, idx) in partnerInstitutes" :key="idx">
                  <div class="coop-icon" :style="{ background: 'linear-gradient(135deg, #36D399, #165DFF)' }">
                    <el-icon :size="22" color="#fff"><DataBoard /></el-icon>
                  </div>
                  <span class="coop-name">{{ inst.name }}</span>
                </div>
                <div v-if="partnerInstitutes.length === 0" class="empty-state empty-state--inline">暂无数据</div>
              </div>
            </div>
          </div>
        </template>
        <div v-else class="empty-state">暂无数据</div>
      </section>

      <!-- 联系我们 -->
      <section class="about-section contact-section">
        <div class="section-header">
          <h2 class="section-title">联系我们</h2>
          <div class="section-line"></div>
        </div>
        <div class="contact-content">
          <div class="contact-info">
            <div class="contact-item" v-for="(item, idx) in contactItems" :key="idx">
              <div class="contact-icon" :style="{ background: item.bg }">
                <el-icon :size="20" color="#fff"><component :is="item.icon" /></el-icon>
              </div>
              <div>
                <div class="contact-label">{{ item.label }}</div>
                <div class="contact-value">{{ item.value }}</div>
              </div>
            </div>
            <div v-if="contactItems.length === 0" class="empty-state empty-state--inline">联系方式暂未公布</div>
          </div>
          <div class="contact-map">
            <div class="map-placeholder">
              <el-icon :size="48"><MapLocation /></el-icon>
              <span>地图加载区域</span>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * AboutPage - 关于我们
 * 平台简介、核心优势、发展历程、合作伙伴、联系我们
 */
import { ref, computed, onMounted } from 'vue'
import {
  Star, UserFilled, OfficeBuilding,
  MapLocation, Share, School, DataBoard
} from '@element-plus/icons-vue'
import { getPartners, type PartnerItem } from '@/api/partner'
import { getAboutConfig, type AboutConfig } from '@/api/about'
import { useSiteStore } from '@/stores/site'
import { getOSSUrl } from '@/utils/storage'

/** 发展历程节点（展示模型） */
interface TimelineNode {
  year: string
  title: string
  desc: string
  color: string
  icon: string
}

/** 团队成员卡片（展示模型） */
interface TeamCard {
  name: string
  role: string
  desc: string
  avatar: string
  bg: string
}

/** 合作机构条目 */
interface CoopItem {
  name: string
}

/** 联系方式条目 */
interface ContactItem {
  label: string
  value: string
  icon: string
  bg: string
}

const values = [
  { label: '使命', desc: '让每个孩子都能享受优质的编程教育', icon: 'Flag', bg: 'rgba(22, 93, 255, 0.1)', color: '#165DFF' },
  { label: '愿景', desc: '成为中国最具影响力的少儿编程教育品牌', icon: 'View', bg: 'rgba(255, 125, 0, 0.1)', color: '#FF7D00' },
  { label: '价值观', desc: '专注、创新、责任、成长', icon: 'Star', bg: 'rgba(54, 211, 153, 0.1)', color: '#36D399' },
]

const advantages = [
  { title: '专业师资', desc: '提供分级编程课程与课堂辅导，注重启发式教学与学习反馈。', icon: 'UserFilled', bg: '#165DFF' },
  { title: '赛事体系', desc: '完善的竞赛培养体系，覆盖NOIP、CSP-J/S、蓝桥杯等主流赛事。', icon: 'Trophy', bg: '#FF7D00' },
  { title: '个性化教学', desc: '根据学生年龄和基础定制学习路径，小班授课，关注每位学员成长。', icon: 'Monitor', bg: '#165DFF' },
  { title: '科技赋能', desc: '自主研发的在线编程平台，支持Scratch、Python、C++多语言在线编程。', icon: 'Brush', bg: '#FF7D00' },
  { title: '项目驱动', desc: '以真实项目为载体，让孩子在实践中掌握编程思维，培养解决问题能力。', icon: 'Medal', bg: '#165DFF' },
  { title: '成长追踪', desc: '完善的学习报告系统，实时追踪学习进度，定期反馈成长情况。', icon: 'Star', bg: '#FF7D00' },
]

// 降级默认数据已清空：发展历程、核心团队、合作伙伴一律以接口数据为准，接口为空则显示「暂无数据」
const defaultTimeline: TimelineNode[] = []
const defaultTeamMembers: TeamCard[] = []
const defaultPartnerSchools: CoopItem[] = []
const defaultPartnerMuseums: CoopItem[] = []
const defaultPartnerInstitutes: CoopItem[] = []

const timeline = ref<TimelineNode[]>([...defaultTimeline])
const teamMembers = ref<TeamCard[]>([...defaultTeamMembers])

// 平台简介（平台自我介绍，保留默认值）
const introTitle = ref('码高少儿编程教育平台')
const introContent = ref('镇原码高成立于2022年，是镇原县本地领先的少儿编程教育品牌。我们致力于为4-16岁青少年提供高质量的编程教育服务，通过趣味化、项目式的教学方法，激发孩子的创造力与逻辑思维，培养面向未来的科技人才。')
const foundedYear = ref('2022')

const partnerSchools = ref<CoopItem[]>([...defaultPartnerSchools])
const partnerMuseums = ref<CoopItem[]>([...defaultPartnerMuseums])
const partnerInstitutes = ref<CoopItem[]>([...defaultPartnerInstitutes])

const hasPartnerData = computed(() =>
  partnerSchools.value.length > 0 || partnerMuseums.value.length > 0 || partnerInstitutes.value.length > 0
)

// 加载合作伙伴数据
async function loadPartners() {
  try {
    const res = await getPartners()
    if (res?.code === 200 && Array.isArray(res.data)) {
      // 后端 Partner.status 为 Integer 0/1，兼容历史字符串值
      const enabledPartners = res.data.filter(
        (p: PartnerItem) => Number(p.status) === 1 || p.status === 'enabled'
      )
      // 后端 Partner 实体无 type 字段，无分类的伙伴统一归入「合作学校」展示
      const namesByType = (type: string): CoopItem[] =>
        enabledPartners
          .filter((p: PartnerItem) => (p.type || 'school') === type)
          .map((p: PartnerItem) => ({ name: p.name }))
      partnerSchools.value = namesByType('school')
      partnerMuseums.value = namesByType('museum')
      partnerInstitutes.value = namesByType('research')
    }
  } catch {
    // 接口不可用时保持空态，不回填默认数据
  }
}

// 加载关于我们配置
async function loadAboutConfig() {
  try {
    const res = await getAboutConfig()
    if (res?.code === 200 && res.data) {
      const data: AboutConfig = res.data
      // 平台简介
      if (data.title) introTitle.value = data.title
      if (data.content) introContent.value = data.content
      if (data.foundedYear) foundedYear.value = data.foundedYear
      // 发展历程
      if (data.timeline && data.timeline.length > 0) {
        const colorPalette = ['#165DFF', '#FF7D00', '#36D399', '#165DFF', '#FF7D00']
        const iconPalette = ['School', 'Trophy', 'Check', 'TrendCharts', 'Star']
        timeline.value = data.timeline
          .sort((a, b) => a.sortOrder - b.sortOrder)
          .map((item, idx) => ({
            year: item.year,
            title: item.title,
            desc: item.description,
            color: colorPalette[idx % colorPalette.length],
            icon: iconPalette[idx % iconPalette.length],
          }))
      }
      // 核心团队
      if (data.team && data.team.length > 0) {
        const bgPalette = [
          'linear-gradient(135deg, #165DFF, #4080FF)',
          'linear-gradient(135deg, #FF7D00, #FF9500)',
          'linear-gradient(135deg, #36D399, #165DFF)',
          'linear-gradient(135deg, #F53F3F, #F76965)',
        ]
        teamMembers.value = data.team.map((member, idx) => ({
          name: member.name,
          role: member.position,
          desc: member.description,
          avatar: member.avatar ? getOSSUrl(member.avatar, { width: 200, height: 200, quality: 85 }) : '',
          bg: bgPalette[idx % bgPalette.length],
        }))
      }
    }
  } catch {
    // 接口无数据或失败时保持空态，不回填虚构内容
  }
}

onMounted(() => {
  loadPartners()
  loadAboutConfig()
})

const siteStore = useSiteStore()

// 联系方式取自站点全局配置（stores/site.ts），为空的项不展示
const contactItems = computed<ContactItem[]>(() => {
  const items: ContactItem[] = [
    { label: '学校地址', value: siteStore.config.contactAddress, icon: 'Location', bg: '#165DFF' },
    { label: '咨询电话', value: siteStore.config.contactPhone, icon: 'Phone', bg: '#36D399' },
    { label: '电子邮箱', value: siteStore.config.contactEmail, icon: 'Message', bg: '#FF7D00' },
  ]
  return items.filter((item) => (item.value || '').trim().length > 0)
})
</script>

<style scoped lang="scss">
@use "sass:color";
$accent: #FF7D00;

.about-page {
  background: $bg-page;
  min-height: 100vh;
}

.page-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 24px 60px;
}

// ==================== Hero Banner ====================
.about-banner {
  background: linear-gradient(135deg, $primary-color 0%, #0E42D2 50%, #165DFF 100%);
  padding: 80px 24px;
  text-align: center;
  color: #fff;
  margin-bottom: 48px;
  position: relative;
  overflow: hidden;
  height: 360px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  box-sizing: border-box;

  .banner-badge {
    display: inline-flex;
    align-items: center;
    gap: 6px;
    padding: 6px 18px;
    background: rgba(255, 255, 255, 0.12);
    border: 1px solid rgba(255, 255, 255, 0.2);
    border-radius: 20px;
    font-size: 13px;
    margin-bottom: 20px;
    backdrop-filter: blur(4px);
  }

  .banner-title {
    font-size: 44px;
    font-weight: 800;
    margin: 0 0 14px;
    letter-spacing: 1px;
  }

  .banner-subtitle {
    font-size: 18px;
    opacity: 0.85;
    margin: 0 0 36px;
    max-width: 600px;
    margin-left: auto;
    margin-right: auto;
  }

  .banner-stats {
    display: flex;
    justify-content: center;
    gap: 48px;

    .stat-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 6px;

      .stat-num {
        font-size: 32px;
        font-weight: 800;
      }

      .stat-label {
        font-size: 14px;
        opacity: 0.7;
      }
    }
  }
}

.banner-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
  overflow: hidden;

  .deco-circle {
    position: absolute;
    border-radius: 50%;
    border: 1px solid rgba(255, 255, 255, 0.08);

    &.c1 {
      width: 400px;
      height: 400px;
      top: -100px;
      right: -100px;
    }

    &.c2 {
      width: 300px;
      height: 300px;
      bottom: -80px;
      left: -80px;
    }

    &.c3 {
      width: 200px;
      height: 200px;
      top: 50%;
      left: 50%;
      transform: translate(-50%, -50%);
      border-color: rgba(255, 255, 255, 0.04);
    }
  }
}

// ==================== 通用区块 ====================
.about-section {
  margin-bottom: 64px;
}

.section-header {
  text-align: center;
  margin-bottom: 40px;

  .section-title {
    font-size: 28px;
    font-weight: 700;
    color: $text-title;
    margin: 0 0 12px;
  }

  .section-line {
    width: 48px;
    height: 4px;
    background: $primary-color;
    border-radius: 2px;
    margin: 0 auto;
  }
}

// ==================== 平台简介 ====================
.intro-section {
  .intro-content {
    background: $bg-white;
    border-radius: 16px;
    padding: 40px;
    box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  }

  h3 {
    font-size: 22px;
    color: $text-title;
    margin: 0 0 16px;
  }

  p {
    font-size: 15px;
    color: $text-body;
    line-height: 1.8;
    margin: 0 0 32px;
  }

  .intro-values {
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 24px;
  }

  .value-item {
    text-align: center;
    padding: 28px 20px;
    background: $bg-page;
    border-radius: 14px;
    transition: transform 0.3s ease, box-shadow 0.3s ease;

    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 24px rgba(0, 0, 0, 0.06);
    }

    .value-icon {
      width: 60px;
      height: 60px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      margin: 0 auto 14px;
    }

    .value-label {
      font-size: 17px;
      font-weight: 700;
      color: $text-title;
      margin-bottom: 8px;
    }

    .value-desc {
      font-size: 13px;
      color: $text-muted;
      line-height: 1.5;
    }
  }
}

// ==================== 核心优势 ====================
.advantages-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 24px;
}

.advantage-card {
  background: $bg-white;
  border-radius: 16px;
  padding: 32px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  animation: fadeInUp 0.5s ease forwards;
  opacity: 0;

  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
  }

  .adv-icon {
    width: 64px;
    height: 64px;
    border-radius: 16px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 16px;
    transition: transform 0.3s ease;
  }

  &:hover .adv-icon {
    transform: scale(1.1) rotate(-5deg);
  }

  h4 {
    font-size: 18px;
    color: $text-title;
    margin: 0 0 10px;
  }

  p {
    font-size: 14px;
    color: $text-body;
    line-height: 1.6;
    margin: 0;
  }
}

@keyframes fadeInUp {
  from { opacity: 0; transform: translateY(20px); }
  to { opacity: 1; transform: translateY(0); }
}

// ==================== 发展历程 - 横向时间线 ====================
.horizontal-timeline {
  position: relative;
  padding: 20px 0 40px;
  overflow-x: auto;
}

.timeline-track {
  position: absolute;
  top: 44px;
  left: 0;
  right: 0;
  height: 4px;
  background: linear-gradient(90deg, $primary-color, $success-color, $accent);
  border-radius: 2px;
  z-index: 1;
}

.timeline-items {
  display: flex;
  justify-content: space-between;
  position: relative;
  z-index: 2;
  min-width: 900px;
  padding: 0 20px;
}

.timeline-node {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  width: 180px;
  flex-shrink: 0;

  &.active {
    .node-dot {
      transform: scale(1.2);
      box-shadow: 0 0 0 6px rgba(22, 93, 255, 0.25);
    }

    .node-year {
      color: $primary-color;
      background: rgba(22, 93, 255, 0.1);
    }
  }
}

.node-dot {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 3px solid #fff;
  box-shadow: 0 0 0 4px rgba(22, 93, 255, 0.15);
  margin-bottom: 16px;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
  z-index: 2;
}

.node-year {
  font-size: 16px;
  font-weight: 800;
  color: $text-title;
  margin-bottom: 8px;
  padding: 4px 14px;
  background: $bg-white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: color 0.3s ease, background-color 0.3s ease;
}

.node-title {
  font-size: 15px;
  font-weight: 700;
  color: $text-title;
  margin-bottom: 6px;
}

.node-desc {
  font-size: 12px;
  color: $text-muted;
  line-height: 1.5;
  max-width: 160px;
}

// ==================== 团队成员 ====================
.team-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.team-card {
  background: $bg-white;
  border-radius: 16px;
  padding: 32px 24px;
  text-align: center;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  transition: transform 0.35s ease, box-shadow 0.35s ease;
  animation: fadeInUp 0.5s ease forwards;
  opacity: 0;
  position: relative;
  overflow: hidden;

  &:hover {
    transform: translateY(-8px);
    box-shadow: 0 16px 40px rgba(0, 0, 0, 0.1);

    .avatar-ring {
      transform: scale(1.15);
      opacity: 0.3;
    }

    .team-social {
      opacity: 1;
      transform: translateY(0);
    }
  }

  .team-avatar {
    position: relative;
    width: 80px;
    height: 80px;
    margin: 0 auto 16px;
  }

  .avatar-img {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    object-fit: cover;
    position: relative;
    z-index: 2;
    border: 2px solid #E5E6EB;
  }

  .avatar-placeholder {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    position: relative;
    z-index: 2;
  }

  .avatar-ring {
    position: absolute;
    inset: -6px;
    border-radius: 50%;
    border: 2px solid $primary-color;
    opacity: 0;
    transition: transform 0.35s ease, opacity 0.35s ease;
    z-index: 1;
  }

  h4 {
    font-size: 18px;
    font-weight: 700;
    color: $text-title;
    margin: 0 0 4px;
  }

  .team-role {
    font-size: 13px;
    color: $primary-color;
    font-weight: 600;
    display: block;
    margin-bottom: 10px;
  }

  p {
    font-size: 13px;
    color: $text-muted;
    line-height: 1.5;
    margin: 0;
  }

  .team-social {
    position: absolute;
    bottom: 16px;
    right: 16px;
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: $primary-color;
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    opacity: 0;
    transform: translateY(10px);
    transition: opacity 0.3s ease, transform 0.3s ease, background 0.3s ease;
    cursor: pointer;

    &:hover {
      background: color.adjust($primary-color, $lightness: -10%);
    }
  }
}

// ==================== 合作伙伴 ====================
.cooperation-categories {
  display: flex;
  flex-direction: column;
  gap: 32px;
  margin-bottom: 32px;
}

.coop-category {
  background: $bg-white;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.coop-category-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 17px;
  font-weight: 700;
  color: $text-title;
  margin: 0 0 20px;

  .el-icon {
    color: $primary-color;
  }
}

.coop-cards {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
}

.coop-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 16px;
  background: $bg-page;
  border-radius: 12px;
  transition: transform 0.3s ease, box-shadow 0.3s ease, background 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
    background: $bg-white;
  }
}

.coop-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.coop-name {
  font-size: 14px;
  font-weight: 600;
  color: $text-body;
}

.honors-list {
  display: flex;
  flex-wrap: wrap;
  justify-content: center;
  gap: 12px;
}

.honor-tag {
  display: flex;
  align-items: center;
  gap: 6px;
  background: $bg-white;
  border: 1px solid rgba($primary-color, 0.2);
  color: $primary-color;
  padding: 8px 16px;
  border-radius: 20px;
  font-size: 13px;
  transition: color 0.3s ease, background 0.3s ease, border-color 0.3s ease;

  &:hover {
    background: $primary-color;
    color: #fff;
    border-color: $primary-color;
  }

  .el-icon {
    color: $accent;
  }

  &:hover .el-icon {
    color: #fff;
  }
}

// ==================== 联系我们 ====================
.contact-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
}

.contact-info {
  background: $bg-white;
  border-radius: 16px;
  padding: 32px;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.contact-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  transition: background 0.3s ease;
  padding: 8px;
  border-radius: 10px;

  &:hover {
    background: $bg-page;
  }

  .contact-icon {
    width: 40px;
    height: 40px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .contact-label {
    font-size: 13px;
    color: $text-muted;
    margin-bottom: 4px;
  }

  .contact-value {
    font-size: 15px;
    color: $text-title;
    font-weight: 500;
  }
}

.contact-map {
  background: $bg-white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0,0,0,0.06);

  .map-placeholder {
    width: 100%;
    height: 100%;
    min-height: 280px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: $text-muted;
    background: $bg-page;
    gap: 8px;
  }
}

// ==================== 响应式 ====================
@media (max-width: 992px) {
  .intro-values {
    grid-template-columns: 1fr !important;
  }

  .advantages-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .partners-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .contact-content {
    grid-template-columns: 1fr;
  }

  .team-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .timeline-items {
    min-width: unset;
    flex-direction: column;
    align-items: flex-start;
    gap: 32px;
    padding: 0 0 0 30px;
  }

  .timeline-track {
    top: 0;
    bottom: 0;
    left: 14px;
    right: auto;
    width: 4px;
    height: auto;
    background: linear-gradient(180deg, $primary-color, $success-color, $accent);
  }

  .timeline-node {
    flex-direction: row;
    align-items: flex-start;
    text-align: left;
    width: 100%;
    gap: 16px;

    .node-dot {
      margin-bottom: 0;
      flex-shrink: 0;
    }

    .node-year {
      margin-bottom: 4px;
    }

    .node-desc {
      max-width: unset;
    }
  }
}

@media (max-width: 576px) {
  .about-banner {
    padding: 60px 20px 50px;

    .banner-title {
      font-size: 28px;
    }

    .banner-stats {
      gap: 24px;

      .stat-num {
        font-size: 24px;
      }
    }
  }

  .advantages-grid {
    grid-template-columns: 1fr;
  }

  .partners-grid {
    grid-template-columns: 1fr;
  }

  .team-grid {
    grid-template-columns: 1fr;
  }

  .page-container {
    padding: 0 16px 40px;
  }

  .intro-content {
    padding: 24px !important;
  }
}

// ==================== 空态 ====================
.empty-state {
  width: 100%;
  padding: 48px 20px;
  text-align: center;
  color: $text-muted;
  font-size: 14px;
  background: $bg-white;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
}

.empty-state--inline {
  padding: 20px;
  font-size: 13px;
  background: $bg-page;
  box-shadow: none;
  grid-column: 1 / -1;
}
</style>
