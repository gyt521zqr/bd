<template>
  <!-- 系统设置 -->
  <div class="system-settings-page">
    <h2 class="page-title">系统设置</h2>
    <el-alert
      type="info"
      :closable="false"
      show-icon
      class="settings-tip"
      title="基本设置、导航菜单与功能开关保存后立即对前台生效（由后台 sys_config 统一下发）"
    />
    <el-tabs v-model="activeTab">
      <el-tab-pane label="基本设置" name="basic">
        <el-form label-width="140px" size="large">
          <el-form-item label="平台名称">
            <el-input v-model="settings.siteName" placeholder="显示在导航与页脚" />
          </el-form-item>
          <el-form-item label="平台Logo地址">
            <el-input v-model="settings.siteLogo" placeholder="图片 URL，留空则使用默认文字 Logo" />
          </el-form-item>
          <el-form-item label="备案号">
            <el-input v-model="settings.icp" placeholder="如：京ICP备XXXXXXXX号" />
          </el-form-item>
          <el-form-item label="客服电话">
            <el-input v-model="settings.servicePhone" />
          </el-form-item>
          <el-form-item label="客服邮箱">
            <el-input v-model="settings.serviceEmail" />
          </el-form-item>
          <el-form-item label="联系地址">
            <el-input v-model="settings.serviceAddress" />
          </el-form-item>
          <el-form-item label="版权信息">
            <el-input v-model="settings.copyright" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="handleSave">保存设置</el-button>
            <el-button @click="handleReset('basic')">恢复默认</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="导航与开关" name="site">
        <el-form label-width="140px" size="large">
          <el-form-item label="导航菜单">
            <el-input
              v-model="featureSettings.navMenuJson"
              type="textarea"
              :rows="10"
              placeholder='JSON 数组，如 [{"label":"首页","path":"/"}]'
            />
            <div class="form-hint">
              每项含 label（显示名）与 path（站内路径），可加 icon。留空数组 [] 表示使用默认导航。
            </div>
          </el-form-item>
          <el-form-item label="开放注册">
            <el-switch v-model="featureSettings.registerEnabled" />
            <div class="form-hint">关闭后前台隐藏注册入口，并拦截直接访问 /register。</div>
          </el-form-item>
          <el-form-item label="维护模式">
            <el-switch v-model="featureSettings.maintenanceMode" />
            <div class="form-hint form-hint-danger">
              开启后前台全屏显示"系统维护中"，请谨慎操作；后台仍可进入以关闭该开关。
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="handleSave">保存设置</el-button>
            <el-button @click="handleReset('site')">恢复默认</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="积分设置" name="points">
        <el-form label-width="140px" size="large">
          <el-form-item label="每日签到积分">
            <el-input-number v-model="pointsSettings.dailySign" :min="1" :max="100" />
          </el-form-item>
          <el-form-item label="完成课程积分">
            <el-input-number v-model="pointsSettings.finishCourse" :min="1" :max="1000" />
          </el-form-item>
          <el-form-item label="考试满分积分">
            <el-input-number v-model="pointsSettings.examPerfect" :min="1" :max="1000" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="handleSave">保存设置</el-button>
            <el-button @click="handleReset('points')">恢复默认</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <el-tab-pane label="通知设置" name="notify">
        <el-form label-width="140px" size="large">
          <el-form-item label="短信通知">
            <el-switch v-model="notifySettings.sms" />
          </el-form-item>
          <el-form-item label="邮件通知">
            <el-switch v-model="notifySettings.email" />
          </el-form-item>
          <el-form-item label="站内通知">
            <el-switch v-model="notifySettings.internal" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="handleSave">保存设置</el-button>
            <el-button @click="handleReset('notify')">恢复默认</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
/**
 * SystemSettingsPage - 系统设置
 * 基本设置 / 导航与开关 / 积分设置 / 通知设置
 *
 * 站点级配置（站名、ICP、客服、导航、功能开关）统一以 snake_case 键写入后端
 * sys_config（PUT /api/admin/settings），前台通过 GET /api/system/site-config 读取。
 * 保留 localStorage 作为接口不可用时的降级，但不再作为唯一数据源。
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { setLocalData, getLocalData, STORAGE_KEYS } from '@/utils/localStore'
import { useSiteStore, DEFAULT_NAV } from '@/stores/site'
import request from '@/api'
import { useOperationLog } from '@/composables/useOperationLog'

const opLog = useOperationLog('system')
const siteStore = useSiteStore()

const activeTab = ref('basic')

// ==================== 类型定义 ====================
interface SiteSettings {
  siteName: string
  siteLogo: string
  icp: string
  servicePhone: string
  serviceEmail: string
  serviceAddress: string
  copyright: string
}

interface FeatureSettings {
  navMenuJson: string
  registerEnabled: boolean
  maintenanceMode: boolean
}

interface PointsSettings {
  dailySign: number
  finishCourse: number
  examPerfect: number
}

interface NotifySettings {
  sms: boolean
  email: boolean
  internal: boolean
}

interface ApiResponse<T = unknown> {
  code: number
  message?: string
  data?: T
}

// ==================== 默认值 ====================
const defaultSettings: SiteSettings = {
  siteName: '码高教育',
  siteLogo: '',
  icp: '',
  servicePhone: '17709342525 / 18793419899',
  serviceEmail: 'contact@magaoedu.com',
  serviceAddress: '甘肃省镇原县码高青少年编程培训学校',
  copyright: 'Copyright 2024-2026 码高少儿编程教育平台',
}

const defaultFeatureSettings: FeatureSettings = {
  navMenuJson: JSON.stringify(DEFAULT_NAV, null, 2),
  registerEnabled: true,
  maintenanceMode: false,
}

const defaultPointsSettings: PointsSettings = {
  dailySign: 10,
  finishCourse: 50,
  examPerfect: 100,
}

const defaultNotifySettings: NotifySettings = {
  sms: true,
  email: true,
  internal: true,
}

const settings = reactive<SiteSettings>({ ...defaultSettings })
const featureSettings = reactive<FeatureSettings>({ ...defaultFeatureSettings })
const pointsSettings = reactive<PointsSettings>({ ...defaultPointsSettings })
const notifySettings = reactive<NotifySettings>({ ...defaultNotifySettings })

const saving = ref(false)

/** 校验导航 JSON，返回解析后的数组或 null */
function parseNavMenu(): Array<{ label: string; path: string; icon?: string }> | null {
  const raw = featureSettings.navMenuJson.trim()
  if (!raw) return []
  try {
    const parsed = JSON.parse(raw)
    if (!Array.isArray(parsed)) return null
    const valid = parsed.every((it) => it && typeof it.label === 'string' && typeof it.path === 'string')
    return valid ? parsed : null
  } catch {
    return null
  }
}

// ==================== 保存设置 ====================
async function handleSave() {
  const nav = parseNavMenu()
  if (nav === null) {
    ElMessage.error('导航菜单 JSON 格式有误，请检查（需为含 label / path 的数组）')
    return
  }

  saving.value = true
  const payload: Record<string, unknown> = {
    // 站点基础信息（snake_case，与后端 site-config 对齐）
    site_name: settings.siteName,
    site_logo: settings.siteLogo,
    icp: settings.icp,
    contact_phone: settings.servicePhone,
    contact_email: settings.serviceEmail,
    contact_address: settings.serviceAddress,
    copyright: settings.copyright,
    // 导航与功能开关
    nav_menu: JSON.stringify(nav),
    register_enabled: String(featureSettings.registerEnabled),
    maintenance_mode: String(featureSettings.maintenanceMode),
    // 积分 / 通知（后台内部使用，保持原键名）
    ...pointsSettings,
    ...notifySettings,
  }

  try {
    await request.put('/admin/settings', payload, { silent: true })
    ElMessage.success('设置已保存并同步至前台')
    // 立即刷新当前会话的站点配置，保证开关所见即所得
    siteStore.applyConfig({
      siteName: settings.siteName,
      siteLogo: settings.siteLogo,
      icp: settings.icp,
      contactPhone: settings.servicePhone,
      contactEmail: settings.serviceEmail,
      contactAddress: settings.serviceAddress,
      copyright: settings.copyright,
      navMenu: nav.length > 0 ? nav : DEFAULT_NAV,
      registerEnabled: featureSettings.registerEnabled,
      maintenanceMode: featureSettings.maintenanceMode,
    })
  } catch {
    ElMessage.warning('后端同步失败，已保存到本地（前台本次会话可能不生效）')
  } finally {
    setLocalData(STORAGE_KEYS.SITE_CONFIG, { ...settings })
    setLocalData(STORAGE_KEYS.POINTS_CONFIG, { ...pointsSettings })
    setLocalData(STORAGE_KEYS.NOTIFY_CONFIG, { ...notifySettings })
    opLog.logConfig('保存系统设置')
    saving.value = false
  }
}

// ==================== 恢复默认 ====================
function handleReset(tab: string) {
  switch (tab) {
    case 'basic':
      Object.assign(settings, defaultSettings)
      break
    case 'site':
      Object.assign(featureSettings, defaultFeatureSettings)
      break
    case 'points':
      Object.assign(pointsSettings, defaultPointsSettings)
      break
    case 'notify':
      Object.assign(notifySettings, defaultNotifySettings)
      break
  }
  ElMessage.success('已恢复默认设置')
}

// ==================== 加载已保存设置 ====================
async function loadSettings() {
  // 1. localStorage 降级值（仅在后端不可用时使用）
  const savedSite = getLocalData<Partial<SiteSettings> | null>(STORAGE_KEYS.SITE_CONFIG, null)
  if (savedSite) Object.assign(settings, savedSite)
  const savedPoints = getLocalData<Partial<PointsSettings> | null>(STORAGE_KEYS.POINTS_CONFIG, null)
  if (savedPoints) Object.assign(pointsSettings, savedPoints)
  const savedNotify = getLocalData<Partial<NotifySettings> | null>(STORAGE_KEYS.NOTIFY_CONFIG, null)
  if (savedNotify) Object.assign(notifySettings, savedNotify)

  // 2. 后端 sys_config 为权威数据源（覆盖本地值）
  try {
    const res = await request.get<unknown, ApiResponse<Record<string, any>>>('/admin/settings', { silent: true })
    if (res?.code === 200 && res?.data) {
      const d = res.data
      if (d.site_name !== undefined) settings.siteName = String(d.site_name)
      if (d.site_logo !== undefined) settings.siteLogo = String(d.site_logo)
      if (d.icp !== undefined) settings.icp = String(d.icp)
      if (d.contact_phone !== undefined) settings.servicePhone = String(d.contact_phone)
      if (d.contact_email !== undefined) settings.serviceEmail = String(d.contact_email)
      if (d.contact_address !== undefined) settings.serviceAddress = String(d.contact_address)
      if (d.copyright !== undefined) settings.copyright = String(d.copyright)

      if (d.nav_menu !== undefined) {
        featureSettings.navMenuJson = typeof d.nav_menu === 'string'
          ? d.nav_menu
          : JSON.stringify(d.nav_menu, null, 2)
      }
      if (d.register_enabled !== undefined) {
        featureSettings.registerEnabled = d.register_enabled === true || d.register_enabled === 'true' || d.register_enabled === 1
      }
      if (d.maintenance_mode !== undefined) {
        featureSettings.maintenanceMode = d.maintenance_mode === true || d.maintenance_mode === 'true' || d.maintenance_mode === 1
      }

      if (d.dailySign !== undefined) pointsSettings.dailySign = Number(d.dailySign)
      if (d.finishCourse !== undefined) pointsSettings.finishCourse = Number(d.finishCourse)
      if (d.examPerfect !== undefined) pointsSettings.examPerfect = Number(d.examPerfect)
      if (d.sms !== undefined) notifySettings.sms = !!d.sms
      if (d.email !== undefined) notifySettings.email = !!d.email
      if (d.internal !== undefined) notifySettings.internal = !!d.internal
    }
  } catch {
    // 后端不可用时沿用本地数据，不弹错误提示
  }
}

onMounted(() => loadSettings())
</script>

<style scoped lang="scss">
.system-settings-page { max-width: 860px; margin: 0 auto; padding: 20px; }
.page-title { font-size: 22px; color: #1D2129; margin-bottom: 16px; }
.settings-tip { margin-bottom: 16px; }
.form-hint { font-size: 12px; color: #86909c; line-height: 1.6; margin-top: 4px; }
.form-hint-danger { color: #f53f3f; }
</style>
