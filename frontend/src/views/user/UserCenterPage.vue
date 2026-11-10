<template>
  <!-- 个人中心：左侧边栏导航 + 右侧内容区 -->
  <div class="user-center-page">
    <!-- 顶部个人封面Banner -->
    <div class="cover-banner">
      <div class="banner-bg">
        <div class="banner-blob blob-1"></div>
        <div class="banner-blob blob-2"></div>
        <div class="banner-blob blob-3"></div>
      </div>
      <div class="banner-content">
        <div class="banner-avatar-wrapper">
          <div class="avatar-ring">
            <el-avatar :size="72" :src="userInfo.avatar" class="user-avatar-banner">
              {{ userInfo.nickname?.charAt(0) || 'U' }}
            </el-avatar>
          </div>
        </div>
        <div class="banner-info">
          <template v-if="hasUserInfo">
            <h2>{{ userInfo.nickname }}</h2>
            <span class="level-badge">{{ userInfo.level }}</span>
          </template>
          <template v-else>
            <h2>未登录 / 暂无资料</h2>
          </template>
        </div>
      </div>
    </div>

    <div class="center-container">
      <!-- PC端左侧边栏 / 移动端顶部标签栏 -->
      <aside class="center-sidebar" :class="{ 'is-mobile': isMobile }">
        <div class="user-brief" v-if="!isMobile">
          <div class="avatar-ring-sidebar">
            <el-avatar :size="64" :src="userInfo.avatar" class="user-avatar">
              {{ userInfo.nickname?.charAt(0) || 'U' }}
            </el-avatar>
          </div>
          <div class="user-name">{{ hasUserInfo ? userInfo.nickname : '未登录 / 暂无资料' }}</div>
          <div class="user-level" v-if="hasUserInfo">{{ userInfo.level }}</div>
        </div>
        <div class="sidebar-tabs">
          <div
            class="tab-item"
            v-for="tab in sidebarTabs"
            :key="tab.key"
            :class="{ active: activeTab === tab.key }"
            @click="activeTab = tab.key"
          >
            <div class="tab-icon">
              <el-icon :size="18"><component :is="tab.icon" /></el-icon>
            </div>
            <span>{{ tab.label }}</span>
          </div>
        </div>
      </aside>

      <!-- 右侧内容区 -->
      <main class="center-content">
        <!-- 我的赛事 -->
        <section v-if="activeTab === 'contest'" class="content-section">
          <h3 class="section-title">我的赛事</h3>
          <div class="contest-list" v-if="myContests.length">
            <div class="contest-card" v-for="(item, idx) in myContests" :key="idx">
              <div class="contest-header">
                <h4>{{ item.title }}</h4>
                <el-tag :type="item.statusType" size="small">{{ item.status }}</el-tag>
              </div>
              <div class="contest-meta">
                <span><el-icon><Timer /></el-icon> {{ item.time }}</span>
                <span><el-icon><Trophy /></el-icon> {{ item.result || '待参赛' }}</span>
              </div>
              <div class="contest-actions">
                <el-button type="primary" size="small" v-if="item.status === '报名中'">查看详情</el-button>
                <el-button size="small" v-else>查看成绩</el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无赛事记录" />
        </section>

        <!-- 我的课程 -->
        <section v-if="activeTab === 'course'" class="content-section">
          <h3 class="section-title">我的课程</h3>
          <div class="course-list" v-if="myCourses.length">
            <div class="course-card" v-for="(item, idx) in myCourses" :key="idx">
              <div class="course-cover" :style="{ background: item.coverBg }">
                <el-icon :size="32"><VideoPlay /></el-icon>
              </div>
              <div class="course-info">
                <h4>{{ item.title }}</h4>
                <p>{{ item.teacher }}</p>
                <div class="course-progress">
                  <el-progress :percentage="item.progress" :stroke-width="8" :show-text="true" />
                  <span class="progress-text">已学 {{ item.learned }}/{{ item.total }} 节</span>
                </div>
              </div>
              <el-button type="primary" size="small" @click="$router.push(`/learn/${item.id}`)">继续学习</el-button>
            </div>
          </div>
          <el-empty v-else description="暂无课程记录" />
        </section>

        <!-- 我的证书 -->
        <section v-if="activeTab === 'certificate'" class="content-section">
          <h3 class="section-title">我的证书</h3>
          <div class="cert-list" v-if="myCertificates.length">
            <div class="cert-card" v-for="(item, idx) in myCertificates" :key="idx">
              <div class="cert-icon">
                <el-icon :size="40" color="#165DFF"><DocumentChecked /></el-icon>
              </div>
              <div class="cert-info">
                <h4>{{ item.name }}</h4>
                <p>获得时间：{{ item.date }}</p>
                <p>颁发机构：{{ item.issuer }}</p>
              </div>
              <div class="cert-actions">
                <el-button type="primary" size="small" text @click="previewCertificate(item)">预览</el-button>
                <el-button type="primary" size="small" text @click="downloadCertificate(item)">下载</el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无证书" />
        </section>

        <!-- 个人信息 -->
        <section v-if="activeTab === 'profile'" class="content-section">
          <h3 class="section-title">个人信息</h3>
          <div class="profile-form">
            <div class="avatar-row">
              <div class="avatar-ring-large">
                <el-avatar :size="80" :src="userInfo.avatar" class="user-avatar-large">
                  {{ userInfo.nickname?.charAt(0) || 'U' }}
                </el-avatar>
              </div>
              <el-button size="small" type="primary" plain>更换头像</el-button>
            </div>
            <el-form :model="userInfo" label-width="100px" class="info-form">
              <el-form-item label="昵称">
                <el-input v-model="userInfo.nickname" placeholder="请输入昵称" />
              </el-form-item>
              <el-form-item label="真实姓名">
                <el-input v-model="userInfo.realName" placeholder="请输入真实姓名" />
              </el-form-item>
              <el-form-item label="学校">
                <el-input v-model="userInfo.school" placeholder="请输入学校名称" />
              </el-form-item>
              <el-form-item label="年级">
                <el-select v-model="userInfo.grade" placeholder="请选择年级" style="width: 100%">
                  <el-option v-for="g in grades" :key="g" :label="g" :value="g" />
                </el-select>
              </el-form-item>
              <el-form-item label="联系电话">
                <el-input v-model="userInfo.phone" placeholder="请输入联系电话" />
              </el-form-item>
              <el-form-item label="邮箱">
                <el-input v-model="userInfo.email" placeholder="请输入邮箱" />
              </el-form-item>
              <el-form-item>
                <el-button type="primary" @click="saveProfile">保存修改</el-button>
              </el-form-item>
            </el-form>
          </div>
        </section>

        <!-- 错题集 -->
        <section v-if="activeTab === 'mistake'" class="content-section">
          <h3 class="section-title">错题集</h3>
          <div class="mistake-list" v-if="mistakeList.length">
            <div class="mistake-card" v-for="(item, idx) in mistakeList" :key="idx">
              <div class="mistake-header">
                <el-tag size="small">{{ item.language }}</el-tag>
                <span class="mistake-time">{{ item.time }}</span>
              </div>
              <h4>{{ item.title }}</h4>
              <p class="mistake-reason">错误原因：{{ item.reason }}</p>
              <div class="mistake-actions">
                <el-button type="primary" size="small" @click="retryProblem(item)">重新做题</el-button>
                <el-button size="small" text>查看解析</el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无错题记录" />
        </section>

        <!-- 编辑器记录 -->
        <section v-if="activeTab === 'editor'" class="content-section">
          <h3 class="section-title">编辑器记录</h3>
          <div class="editor-filter">
            <el-radio-group v-model="editorFilter" size="small">
              <el-radio-button value="">全部</el-radio-button>
              <el-radio-button value="scratch">Scratch</el-radio-button>
              <el-radio-button value="python">Python</el-radio-button>
              <el-radio-button value="cpp">C++</el-radio-button>
            </el-radio-group>
          </div>
          <div class="work-list" v-if="filteredWorks.length">
            <div class="work-card" v-for="(item, idx) in filteredWorks" :key="idx">
              <div class="work-preview" :style="{ background: item.previewBg }">
                <el-icon :size="32" color="#fff"><component :is="item.icon" /></el-icon>
              </div>
              <div class="work-info">
                <h4>{{ item.title }}</h4>
                <p>{{ item.lang }} · {{ item.time }}</p>
              </div>
              <div class="work-actions">
                <el-button type="primary" size="small" text>编辑</el-button>
                <el-button size="small" text>分享</el-button>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无作品记录" />
        </section>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * UserCenterPage - 个人中心
 * PC端左侧边栏导航，移动端顶部标签栏
 * 已连接真实后端API，接口失败或无数据时保持空态，不再注入虚构数据
 */
import { ref, computed, onMounted, onUnmounted } from 'vue'
import {
  Trophy, Timer, VideoPlay, DocumentChecked
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getUserInfo, updateUserInfo } from '@/api/user'
import { getExamRecords } from '@/api/exam'
import { getOrderList } from '@/api/payment'

// 响应式检测
const isMobile = ref(window.innerWidth <= 768)
function handleResize() {
  isMobile.value = window.innerWidth <= 768
}
onMounted(() => {
  window.addEventListener('resize', handleResize)
  loadUserProfile()
  loadExamRecords()
  loadOrderList()
})
onUnmounted(() => window.removeEventListener('resize', handleResize))

// 侧边栏标签
const sidebarTabs = [
  { key: 'contest', label: '我的赛事', icon: 'Trophy' },
  { key: 'course', label: '我的课程', icon: 'Reading' },
  { key: 'certificate', label: '我的证书', icon: 'DocumentChecked' },
  { key: 'profile', label: '个人信息', icon: 'User' },
  { key: 'mistake', label: '错题集', icon: 'CircleClose' },
  { key: 'editor', label: '编辑器记录', icon: 'Monitor' },
]

const activeTab = ref('contest')

// ==================== 用户信息 ====================
// 初始为空资料对象，接口失败或暂无数据时不注入虚构用户
const userInfo = ref({
  avatar: '',
  nickname: '',
  realName: '',
  level: '',
  school: '',
  grade: '',
  phone: '',
  email: '',
})

/** 是否已加载到用户资料（用于展示区空态判断） */
const hasUserInfo = computed(() => Boolean(userInfo.value.nickname))

const grades = ['一年级', '二年级', '三年级', '四年级', '五年级', '六年级', '初一', '初二', '初三', '高一', '高二', '高三']

/** 调用真实API获取用户信息 */
async function loadUserProfile() {
  try {
    const res = await getUserInfo()
    if (res.data) {
      const data = res.data
      userInfo.value = {
        avatar: data.avatar || '',
        nickname: data.nickname || '',
        realName: (data as any).realName || '',
        level: data.level ? `Lv.${data.level} 进阶学员` : '',
        school: (data as any).school || '',
        grade: (data as any).grade || '',
        phone: data.phone || '',
        email: (data as any).email || '',
      }
    }
  } catch {
    // 接口失败或无数据：用户信息保持为空，由模板显示「未登录 / 暂无资料」
  }
}

async function saveProfile() {
  try {
    await updateUserInfo({
      nickname: userInfo.value.nickname,
      avatar: userInfo.value.avatar,
    } as any)
    ElMessage.success('个人信息保存成功')
  } catch (e) {
    // 接口失败：不报成功、不本地写入，保留用户填写内容供重试
    console.warn('[UserCenter] 保存用户信息失败', e)
    ElMessage.error('个人信息保存失败，请稍后重试')
  }
}

// ==================== 我的赛事（考试记录） ====================
type ContestItem = {
  title: string
  status: string
  statusType: 'primary' | 'success' | 'warning' | 'info' | 'danger'
  time: string
  result: string
}

// 初始为空，接口无数据时由模板显示空态
const myContests = ref<ContestItem[]>([])

/** 调用真实API获取考试记录，映射为赛事列表 */
async function loadExamRecords() {
  try {
    const res = await getExamRecords({ pageNum: 1, pageSize: 20 })
    if (res.data) {
      // 接口成功：使用返回列表（即使为空也展示空态）
      myContests.value = (res.data.list || []).map((record: any) => ({
        title: record.examTitle || `考试 #${record.examId}`,
        status: record.passed ? '已结束' : '已结束',
        statusType: record.passed ? 'success' as const : 'info' as const,
        time: record.submitTime || '',
        result: record.passed ? `通过 (${record.score}/${record.totalScore}分)` : `未通过 (${record.score}/${record.totalScore}分)`,
      }))
    }
  } catch {
    // 接口失败或无数据：保持空态，由模板显示「暂无赛事记录」
  }
}

// ==================== 我的课程 ====================
// 当前后端暂无「我的课程」接口，列表保持为空，由模板显示空态
type CourseItem = {
  id: number
  title: string
  teacher: string
  progress: number
  learned: number
  total: number
  coverBg: string
}
const myCourses = ref<CourseItem[]>([])

// ==================== 我的证书 ====================
// 当前后端暂无证书接口，列表保持为空，由模板显示空态；待接口就绪后补充 load 逻辑
type CertificateItem = {
  name: string
  date: string
  issuer: string
  certNo: string
  examName: string
  score: number
}
const myCertificates = ref<CertificateItem[]>([])

/** 证书预览 - 使用ElMessage提示（完整预览需要后端返回证书图片URL） */
function previewCertificate(cert: CertificateItem) {
  const content = `证书编号: ${cert.certNo}\n姓名: ${userInfo.value.nickname}\n赛事: ${cert.examName || cert.name}\n成绩: ${cert.score || '--'}\n获得时间: ${cert.date}\n颁发机构: ${cert.issuer}`
  ElMessage.info(`证书预览：${cert.name}\n\n${content}`)
}

/** 证书下载 - 生成文本文件下载 */
function downloadCertificate(cert: CertificateItem) {
  const content = `证书编号: ${cert.certNo}\n姓名: ${userInfo.value.nickname}\n赛事: ${cert.examName || cert.name}\n成绩: ${cert.score || '--'}\n获得时间: ${cert.date}\n颁发机构: ${cert.issuer}`
  const blob = new Blob([content], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `证书_${cert.certNo}.txt`
  a.click()
  URL.revokeObjectURL(url)
  ElMessage.success('证书文件已开始下载')
}

// ==================== 我的订单 ====================
type OrderItem = {
  id: number
  title: string
  status: string
  statusType: 'success' | 'warning' | 'info' | 'danger'
  amount: string
  time: string
  orderNo: string
}

// 初始为空，接口无数据时保持空态
const myOrders = ref<OrderItem[]>([])

/** 调用真实API获取订单列表 */
async function loadOrderList() {
  try {
    const res = await getOrderList({ pageNum: 1, pageSize: 20 })
    if (res.data) {
      const statusMap: Record<string, { label: string; type: 'success' | 'warning' | 'info' | 'danger' }> = {
        paid: { label: '已支付', type: 'success' },
        pending: { label: '待支付', type: 'warning' },
        cancelled: { label: '已取消', type: 'info' },
        refunded: { label: '已退款', type: 'info' },
        expired: { label: '已过期', type: 'danger' },
      }
      // 接口成功：使用返回列表（即使为空也展示空态）
      myOrders.value = (res.data.list || []).map((order: any) => {
        const statusInfo = statusMap[order.status] || { label: order.status, type: 'info' as const }
        return {
          id: order.id,
          title: order.productName || `订单 #${order.id}`,
          status: statusInfo.label,
          statusType: statusInfo.type,
          amount: `¥${(order.payAmount / 100).toFixed(2)}`,
          time: order.createTime || '',
          orderNo: order.orderNo || '',
        }
      })
    }
  } catch {
    // 接口失败或无数据：保持空态
  }
}

// ==================== 错题集 ====================
// 当前后端暂无错题接口，列表保持为空，由模板显示空态
type MistakeItem = {
  title: string
  language: string
  time: string
  reason: string
}
const mistakeList = ref<MistakeItem[]>([])

function retryProblem(item: MistakeItem) {
  ElMessage.success(`正在打开题目：${item.title}`)
}

// ==================== 编辑器记录 ====================
// 当前后端暂无作品记录接口，列表保持为空，由模板显示空态
const editorFilter = ref('')
type WorkItem = {
  title: string
  lang: string
  time: string
  previewBg: string
  icon: string
}
const editorWorks = ref<WorkItem[]>([])

const filteredWorks = computed(() => {
  if (!editorFilter.value) return editorWorks.value
  const map: Record<string, string> = { scratch: 'Scratch', python: 'Python', cpp: 'C++' }
  return editorWorks.value.filter(w => w.lang === map[editorFilter.value])
})
</script>

<style scoped lang="scss">
$content-max-width: 1200px;

.user-center-page {
  background: $bg-page;
  min-height: 100vh;
  padding-bottom: 60px;
}

// ==================== 顶部封面Banner ====================
.cover-banner {
  position: relative;
  height: 200px;
  overflow: hidden;
  margin-bottom: 24px;

  .banner-bg {
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, $primary-color 0%, #0E42D2 50%, $primary-color 100%);
    overflow: hidden;
  }

  .banner-blob {
    position: absolute;
    border-radius: 50%;
    filter: blur(50px);
    opacity: 0.5;
    animation: bannerBlobMove 10s ease-in-out infinite;
    will-change: transform, opacity;
  }

  .blob-1 {
    width: 400px;
    height: 400px;
    background: linear-gradient(135deg, $success-color, $primary-color);
    top: -150px;
    right: 10%;
    animation-delay: 0s;
  }

  .blob-2 {
    width: 300px;
    height: 300px;
    background: linear-gradient(135deg, $action-color, $warning-color);
    bottom: -100px;
    left: 20%;
    animation-delay: -3s;
  }

  .blob-3 {
    width: 250px;
    height: 250px;
    background: linear-gradient(135deg, $primary-color, #4080FF);
    top: 20%;
    left: 60%;
    animation-delay: -6s;
  }

  @keyframes bannerBlobMove {
    0%, 100% { transform: translate(0, 0) scale(1); }
    33% { transform: translate(30px, -20px) scale(1.1); }
    66% { transform: translate(-20px, 15px) scale(0.95); }
  }

  .banner-content {
    position: relative;
    z-index: 1;
    max-width: $content-max-width;
    margin: 0 auto;
    padding: 32px 24px 0;
    display: flex;
    align-items: center;
    gap: 20px;
    height: 100%;
  }

  .banner-avatar-wrapper {
    position: relative;
  }

  .banner-info {
    color: #fff;

    h2 {
      font-size: 24px;
      font-weight: 700;
      margin-bottom: 8px;
      text-shadow: 0 2px 4px rgba(0,0,0,0.15);
    }

    .level-badge {
      display: inline-block;
      padding: 4px 14px;
      background: rgba(255, 255, 255, 0.2);
      backdrop-filter: blur(8px);
      border-radius: 20px;
      font-size: 13px;
      font-weight: 500;
      border: 1px solid rgba(255, 255, 255, 0.25);
    }
  }
}

// 头像光环效果
.avatar-ring {
  position: relative;
  display: inline-flex;
  padding: 4px;
  border-radius: 50%;
  background: linear-gradient(135deg, #fff, rgba(255,255,255,0.6));
  box-shadow: 0 0 0 3px rgba(255, 255, 255, 0.3), 0 4px 20px rgba(0,0,0,0.15);

  &::before {
    content: '';
    position: absolute;
    inset: -2px;
    border-radius: 50%;
    background: linear-gradient(135deg, $success-color, $primary-color, $action-color);
    z-index: -1;
    animation: ringRotate 4s linear infinite;
    will-change: transform, opacity;
  }

  .user-avatar-banner {
    background: linear-gradient(135deg, $primary-color, #36D399);
    font-size: 28px;
    font-weight: 600;
  }
}

.avatar-ring-sidebar {
  position: relative;
  display: inline-flex;
  padding: 3px;
  border-radius: 50%;
  background: linear-gradient(135deg, $primary-color, $success-color);
  margin-bottom: 12px;

  &::before {
    content: '';
    position: absolute;
    inset: -1px;
    border-radius: 50%;
    background: linear-gradient(135deg, $success-color, $primary-color, $action-color);
    z-index: -1;
    animation: ringRotate 5s linear infinite;
    will-change: transform, opacity;
    opacity: 0.6;
  }

  .user-avatar {
    background: linear-gradient(135deg, $primary-color, #36D399);
    font-size: 24px;
    font-weight: 600;
    border: 2px solid #fff;
  }
}

.avatar-ring-large {
  position: relative;
  display: inline-flex;
  padding: 3px;
  border-radius: 50%;
  background: linear-gradient(135deg, $primary-color, $success-color);

  &::before {
    content: '';
    position: absolute;
    inset: -1px;
    border-radius: 50%;
    background: linear-gradient(135deg, $success-color, $primary-color, $action-color);
    z-index: -1;
    animation: ringRotate 5s linear infinite;
    will-change: transform, opacity;
    opacity: 0.5;
  }

  .user-avatar-large {
    background: linear-gradient(135deg, $primary-color, #36D399);
    font-size: 32px;
    font-weight: 600;
    border: 2px solid #fff;
  }
}

@keyframes ringRotate {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

// ==================== 主体布局 ====================
.center-container {
  max-width: $content-max-width;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  gap: 24px;
}

// 左侧边栏
.center-sidebar {
  width: 220px;
  flex-shrink: 0;
  background: $bg-white;
  border-radius: 16px;
  padding: 24px 0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  height: fit-content;
  position: sticky;
  top: 90px;

  .user-brief {
    text-align: center;
    padding: 0 20px 20px;
    border-bottom: 1px solid #F2F3F5;
    margin-bottom: 12px;

    .user-name {
      font-size: 16px;
      font-weight: 700;
      color: $text-title;
      margin-bottom: 4px;
    }
    .user-level {
      font-size: 12px;
      color: $primary-color;
      background: rgba(22, 93, 255, 0.08);
      display: inline-block;
      padding: 2px 10px;
      border-radius: 12px;
    }
  }

  .sidebar-tabs {
    .tab-item {
      display: flex;
      align-items: center;
      gap: 10px;
      padding: 12px 20px;
      font-size: 14px;
      color: $text-body;
      cursor: pointer;
      transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
      border-left: 3px solid transparent;
      position: relative;
      overflow: hidden;

      .tab-icon {
        width: 32px;
        height: 32px;
        border-radius: 8px;
        display: flex;
        align-items: center;
        justify-content: center;
        background: #F7F8FA;
        transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s ease;
        color: $text-muted;
      }

      &:hover {
        color: $primary-color;
        background: rgba(22, 93, 255, 0.04);
        padding-left: 24px;

        .tab-icon {
          background: linear-gradient(135deg, rgba(22, 93, 255, 0.1), rgba(54, 211, 153, 0.1));
          color: $primary-color;
          transform: scale(1.1);
        }
      }

      &.active {
        color: $primary-color;
        background: rgba(22, 93, 255, 0.08);
        border-left-color: $primary-color;
        font-weight: 600;

        .tab-icon {
          background: linear-gradient(135deg, $primary-color, #4080FF);
          color: #fff;
          box-shadow: 0 4px 12px rgba(22, 93, 255, 0.3);
        }
      }
    }
  }
}

// 右侧内容区
.center-content {
  flex: 1;
  min-width: 0;
}

.content-section {
  background: $bg-white;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);

  .section-title {
    font-size: 20px;
    font-weight: 700;
    color: $text-title;
    margin-bottom: 20px;
    padding-bottom: 16px;
    border-bottom: 1px solid #F2F3F5;
    position: relative;

    &::after {
      content: '';
      position: absolute;
      bottom: -1px;
      left: 0;
      width: 48px;
      height: 3px;
      background: linear-gradient(90deg, $primary-color, $action-color);
      border-radius: 2px;
    }
  }
}

// 我的赛事
.contest-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.contest-card {
  border: 1px solid #E5E6EB;
  border-radius: 12px;
  padding: 20px;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  &:hover {
    border-color: rgba(22, 93, 255, 0.3);
    box-shadow: 0 8px 24px rgba(22, 93, 255, 0.1);
    transform: translateY(-2px);
  }
  .contest-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    h4 {
      font-size: 16px;
      font-weight: 600;
      color: $text-title;
    }
  }
  .contest-meta {
    display: flex;
    gap: 20px;
    margin-bottom: 16px;
    span {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;
      color: $text-muted;
    }
  }
}

// 我的课程
.course-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}
.course-card {
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid #E5E6EB;
  border-radius: 12px;
  padding: 16px;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  &:hover {
    border-color: rgba(22, 93, 255, 0.3);
    box-shadow: 0 8px 24px rgba(22, 93, 255, 0.1);
    transform: translateY(-2px);
  }
  .course-cover {
    width: 80px;
    height: 60px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    color: #fff;
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  }
  .course-info {
    flex: 1;
    min-width: 0;
    h4 {
      font-size: 15px;
      font-weight: 600;
      color: $text-title;
      margin-bottom: 4px;
    }
    p {
      font-size: 13px;
      color: $text-muted;
      margin-bottom: 8px;
    }
    .course-progress {
      display: flex;
      align-items: center;
      gap: 12px;
      :deep(.el-progress) { flex: 1; }
      .progress-text {
        font-size: 12px;
        color: $text-muted;
        white-space: nowrap;
      }
    }
  }
}

// 我的证书
.cert-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.cert-card {
  display: flex;
  align-items: center;
  gap: 16px;
  border: 1px solid #E5E6EB;
  border-radius: 12px;
  padding: 20px;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s ease;

  &:hover {
    border-color: rgba(22, 93, 255, 0.3);
    box-shadow: 0 8px 24px rgba(22, 93, 255, 0.08);
    transform: translateY(-2px);
  }

  .cert-icon {
    width: 60px;
    height: 60px;
    border-radius: 12px;
    background: linear-gradient(135deg, rgba(22, 93, 255, 0.1), rgba(54, 211, 153, 0.1));
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }
  .cert-info {
    flex: 1;
    min-width: 0;
    h4 {
      font-size: 15px;
      font-weight: 600;
      color: $text-title;
      margin-bottom: 6px;
    }
    p {
      font-size: 13px;
      color: $text-muted;
      line-height: 1.6;
    }
  }
  .cert-actions {
    display: flex;
    gap: 8px;
  }
}

// 个人信息
.profile-form {
  .avatar-row {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 24px;
  }
  .info-form {
    max-width: 480px;
  }
}

// ==================== 刷题统计卡片（带图标和彩色背景）====================
.practice-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 24px;
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

  .stat-icon-bg {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
  }

  .stat-info {
    .stat-value {
      font-size: 26px;
      font-weight: 800;
      margin-bottom: 2px;
    }
    .stat-label {
      font-size: 13px;
      opacity: 0.8;
    }
  }

  &.stat-total {
    background: linear-gradient(135deg, rgba(22, 93, 255, 0.1), rgba(64, 128, 255, 0.06));
    border: 1px solid rgba(22, 93, 255, 0.15);
    .stat-icon-bg { background: linear-gradient(135deg, $primary-color, #4080FF); color: #fff; }
    .stat-value { color: $primary-color; }
    .stat-label { color: rgba(22, 93, 255, 0.7); }
  }

  &.stat-correct {
    background: linear-gradient(135deg, rgba(54, 211, 153, 0.1), rgba(92, 219, 156, 0.06));
    border: 1px solid rgba(54, 211, 153, 0.15);
    .stat-icon-bg { background: linear-gradient(135deg, $success-color, #36D399); color: #fff; }
    .stat-value { color: $success-color; }
    .stat-label { color: rgba(54, 211, 153, 0.7); }
  }

  &.stat-accuracy {
    background: linear-gradient(135deg, rgba(255, 125, 0, 0.1), rgba(255, 149, 0, 0.06));
    border: 1px solid rgba(255, 125, 0, 0.15);
    .stat-icon-bg { background: linear-gradient(135deg, $action-color, $warning-color); color: #fff; }
    .stat-value { color: $action-color; }
    .stat-label { color: rgba(255, 125, 0, 0.7); }
  }

  &.stat-streak {
    background: linear-gradient(135deg, rgba(245, 63, 63, 0.08), rgba(255, 100, 100, 0.04));
    border: 1px solid rgba(245, 63, 63, 0.12);
    .stat-icon-bg { background: linear-gradient(135deg, $danger-color, #F76965); color: #fff; }
    .stat-value { color: $danger-color; }
    .stat-label { color: rgba(245, 63, 63, 0.7); }
  }
}

.practice-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}
.practice-record {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 16px;
  border-radius: 10px;
  background: #F7F8FA;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.25s ease;

  &:hover {
    background: #E8F0FF;
    transform: translateX(4px);
  }

  .record-left {
    display: flex;
    align-items: center;
    gap: 10px;
    .record-title {
      font-size: 14px;
      color: $text-title;
    }
  }
  .record-right {
    display: flex;
    gap: 12px;
    font-size: 12px;
    color: $text-muted;
  }
}

// 错题集
.mistake-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.mistake-card {
  border: 1px solid #E5E6EB;
  border-radius: 12px;
  padding: 20px;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s ease;

  &:hover {
    border-color: rgba(245, 63, 63, 0.2);
    box-shadow: 0 8px 24px rgba(245, 63, 63, 0.06);
    transform: translateY(-2px);
  }

  .mistake-header {
    display: flex;
    justify-content: space-between;
    margin-bottom: 10px;
    .mistake-time {
      font-size: 12px;
      color: $text-muted;
    }
  }
  h4 {
    font-size: 15px;
    font-weight: 600;
    color: $text-title;
    margin-bottom: 8px;
  }
  .mistake-reason {
    font-size: 13px;
    color: $danger-color;
    margin-bottom: 16px;
  }
  .mistake-actions {
    display: flex;
    gap: 8px;
  }
}

// 编辑器记录
.editor-filter {
  margin-bottom: 20px;
}
.work-list {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}
.work-card {
  border: 1px solid #E5E6EB;
  border-radius: 12px;
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 12px;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);
  &:hover {
    border-color: rgba(22, 93, 255, 0.3);
    box-shadow: 0 8px 24px rgba(22, 93, 255, 0.1);
    transform: translateY(-3px);
  }
  .work-preview {
    width: 60px;
    height: 60px;
    border-radius: 10px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  }
  .work-info {
    flex: 1;
    min-width: 0;
    h4 {
      font-size: 14px;
      font-weight: 600;
      color: $text-title;
      margin-bottom: 4px;
    }
    p {
      font-size: 12px;
      color: $text-muted;
    }
  }
  .work-actions {
    display: flex;
    gap: 4px;
  }
}

// ==================== 移动端适配 ====================
@media (max-width: 768px) {
  .cover-banner {
    height: 160px;
    margin-bottom: 16px;

    .banner-content {
      padding: 20px 16px 0;
    }

    .banner-info h2 {
      font-size: 20px;
    }
  }

  .user-center-page {
    padding-bottom: 40px;
  }
  .center-container {
    flex-direction: column;
    padding: 0 16px;
    gap: 16px;
  }
  .center-sidebar {
    width: 100%;
    position: static;
    padding: 12px 0;
    border-radius: 12px;

    &.is-mobile {
      .sidebar-tabs {
        display: flex;
        overflow-x: auto;
        gap: 4px;
        padding: 0 12px;
        scrollbar-width: none;
        &::-webkit-scrollbar { display: none; }

        .tab-item {
          flex-shrink: 0;
          padding: 8px 14px;
          border-left: none;
          border-radius: 20px;
          font-size: 13px;
          background: #F7F8FA;
          gap: 6px;

          .tab-icon {
            width: 24px;
            height: 24px;
            border-radius: 6px;
          }

          &.active {
            background: $primary-color;
            color: #fff;
            border-left-color: transparent;

            .tab-icon {
              background: rgba(255,255,255,0.2);
              color: #fff;
              box-shadow: none;
            }
          }

          &:hover {
            padding-left: 14px;
          }
        }
      }
    }
  }
  .content-section {
    padding: 20px 16px;
    .section-title {
      font-size: 18px;
      margin-bottom: 16px;
    }
  }
  .practice-stats {
    grid-template-columns: repeat(2, 1fr);
  }
  .work-list {
    grid-template-columns: 1fr;
  }
  .course-card {
    flex-direction: column;
    align-items: flex-start;
    .course-cover {
      width: 100%;
      height: 100px;
    }
  }
  .cert-card {
    flex-direction: column;
    align-items: flex-start;
  }
  .practice-record {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
}
</style>
