<template>
  <!-- 首页配置管理后台 -->
  <div class="homepage-config-page">
    <h2 class="page-title">首页配置</h2>
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 统计数据配置 -->
      <el-tab-pane label="统计数据" name="stats">
        <el-form :model="config.stats" label-width="140px" size="default">
          <el-form-item label="平台用户量">
            <el-input v-model="config.stats.userCount" placeholder="如：100" style="width: 200px">
              <template #append>万+</template>
            </el-input>
          </el-form-item>
          <el-form-item label="活跃学员数">
            <el-input v-model="config.stats.activeStudents" placeholder="如：50" style="width: 200px">
              <template #append>万+</template>
            </el-input>
          </el-form-item>
          <el-form-item label="覆盖省市数">
            <el-input v-model="config.stats.coveredCities" placeholder="如：90" style="width: 200px">
              <template #append>%</template>
            </el-input>
          </el-form-item>
          <el-form-item label="合作机构数">
            <el-input v-model="config.stats.partnerCount" placeholder="如：500" style="width: 200px">
              <template #append>+</template>
            </el-input>
          </el-form-item>
          <el-form-item label="作品数量">
            <el-input v-model="config.stats.workCount" placeholder="如：50" style="width: 200px">
              <template #append>万+</template>
            </el-input>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="handleSave">保存统计数据</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 核心入口配置 -->
      <el-tab-pane label="核心入口" name="entries">
        <div class="section-header-row">
          <h3 class="section-subtitle">首页三大核心入口卡片配置</h3>
          <el-button type="primary" size="small" @click="handleAddEntry">新增入口</el-button>
        </div>
        <el-table :data="config.entries" stripe row-key="id" style="margin-bottom: 16px" empty-text="暂无数据">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="title" label="标题" min-width="120" />
          <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
          <el-table-column prop="icon" label="图标" width="100" />
          <el-table-column prop="link" label="链接" min-width="120" show-overflow-tooltip />
          <el-table-column prop="bgColor" label="背景色" width="120">
            <template #default="{ row }">
              <div class="color-preview" :style="{ background: row.bgColor }">
                <span>{{ row.bgColor }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row, $index }">
              <el-button size="small" text type="primary" @click="handleEditEntry(row as HomepageEntry)">编辑</el-button>
              <el-button size="small" text type="danger" @click="handleDeleteEntry($index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-button type="primary" :loading="saving" @click="handleSave">保存入口配置</el-button>

        <!-- 新增/编辑入口弹窗 -->
        <el-dialog v-model="entryDialogVisible" :title="isEditEntry ? '编辑入口' : '新增入口'" width="560px" destroy-on-close>
          <el-form ref="entryFormRef" :model="entryForm" :rules="entryFormRules" label-width="100px">
            <el-form-item label="标题" prop="title">
              <el-input v-model="entryForm.title" placeholder="如：赛事中心" maxlength="20" show-word-limit />
            </el-form-item>
            <el-form-item label="描述" prop="description">
              <el-input v-model="entryForm.description" type="textarea" :rows="3" placeholder="请输入描述文字" maxlength="100" show-word-limit />
            </el-form-item>
            <el-form-item label="图标" prop="icon">
              <el-input v-model="entryForm.icon" placeholder="如：Trophy" />
              <div class="form-tip">使用 Element Plus 图标名称</div>
            </el-form-item>
            <el-form-item label="链接" prop="link">
              <el-input v-model="entryForm.link" placeholder="如：/exam" />
            </el-form-item>
            <el-form-item label="背景色" prop="bgColor">
              <el-input v-model="entryForm.bgColor" placeholder="如：linear-gradient(135deg, #165DFF, #36D399)">
                <template #append>
                  <el-color-picker v-model="entryForm.bgColor" show-alpha />
                </template>
              </el-input>
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="entryDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitEntryForm">确定</el-button>
          </template>
        </el-dialog>
      </el-tab-pane>

      <!-- 新媒体矩阵配置 -->
      <el-tab-pane label="新媒体矩阵" name="media">
        <div class="section-header-row">
          <h3 class="section-subtitle">首页新媒体矩阵平台配置</h3>
          <el-button type="primary" size="small" @click="handleAddMedia">新增平台</el-button>
        </div>
        <el-table :data="config.media" stripe row-key="id" style="margin-bottom: 16px" empty-text="暂无数据">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="name" label="平台名称" min-width="120" />
          <el-table-column prop="icon" label="图标" width="100" />
          <el-table-column prop="link" label="链接" min-width="120" show-overflow-tooltip />
          <el-table-column prop="bgColor" label="背景色" width="120">
            <template #default="{ row }">
              <div class="color-preview" :style="{ background: row.bgColor }">
                <span>{{ row.bgColor }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row, $index }">
              <el-button size="small" text type="primary" @click="handleEditMedia(row as HomepageMedia)">编辑</el-button>
              <el-button size="small" text type="danger" @click="handleDeleteMedia($index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-button type="primary" :loading="saving" @click="handleSave">保存新媒体配置</el-button>

        <!-- 新增/编辑平台弹窗 -->
        <el-dialog v-model="mediaDialogVisible" :title="isEditMedia ? '编辑平台' : '新增平台'" width="560px" destroy-on-close>
          <el-form ref="mediaFormRef" :model="mediaForm" :rules="mediaFormRules" label-width="100px">
            <el-form-item label="平台名称" prop="name">
              <el-input v-model="mediaForm.name" placeholder="如：抖音" maxlength="20" show-word-limit />
            </el-form-item>
            <el-form-item label="图标" prop="icon">
              <el-input v-model="mediaForm.icon" placeholder="如：VideoCamera" />
              <div class="form-tip">使用 Element Plus 图标名称</div>
            </el-form-item>
            <el-form-item label="链接" prop="link">
              <el-input v-model="mediaForm.link" placeholder="如：https://douyin.com" />
            </el-form-item>
            <el-form-item label="背景色" prop="bgColor">
              <el-input v-model="mediaForm.bgColor" placeholder="如：#000">
                <template #append>
                  <el-color-picker v-model="mediaForm.bgColor" show-alpha />
                </template>
              </el-input>
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="mediaDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitMediaForm">确定</el-button>
          </template>
        </el-dialog>
      </el-tab-pane>

      <!-- 显示设置：首页区块开关与轮播参数 -->
      <el-tab-pane label="显示设置" name="display">
        <el-alert
          type="info"
          :closable="false"
          show-icon
          class="display-tip"
          title="以下开关保存后由首页实时读取，可单独隐藏区块或调整轮播行为"
        />
        <el-form label-width="160px" size="default">
          <el-form-item label="自动播放轮播">
            <el-switch v-model="config.bannerAutoPlay" />
            <span class="form-tip-inline">关闭后首页轮播不自动切换</span>
          </el-form-item>
          <el-form-item label="轮播间隔（毫秒）">
            <el-input-number v-model="config.bannerInterval" :min="1000" :max="20000" :step="500" />
            <span class="form-tip-inline">默认 4000</span>
          </el-form-item>
          <el-form-item label="显示学习中心/课程分类">
            <el-switch v-model="config.showCourseCategory" />
          </el-form-item>
          <el-form-item label="显示学术资讯">
            <el-switch v-model="config.showNews" />
          </el-form-item>
          <el-form-item label="显示合作伙伴">
            <el-switch v-model="config.showPartner" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="handleSave">保存显示设置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
/**
 * HomepageConfigPage - 首页配置管理后台
 * 统计数据、核心入口、新媒体矩阵
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { getHomepageConfig, updateHomepageConfig, type HomepageConfig, type HomepageEntry, type HomepageMedia } from '@/api/homepage'
import { setLocalData, getLocalData, STORAGE_KEYS } from '@/utils/localStore'
import { useOperationLog } from '@/composables/useOperationLog'

const opLog = useOperationLog('homepage')

const activeTab = ref('stats')
const saving = ref(false)

// 默认配置数据（降级方案，列表初始为空，接口失败不注入示例数据）
const defaultConfig: HomepageConfig = {
  stats: {
    userCount: '',
    activeStudents: '',
    coveredCities: '',
    partnerCount: '',
    workCount: '',
  },
  entries: [],
  media: [],
  bannerAutoPlay: true,
  bannerInterval: 4000,
  showCourseCategory: true,
  showNews: true,
  showPartner: true,
}

const config = reactive<HomepageConfig>({
  stats: { ...defaultConfig.stats },
  entries: [...defaultConfig.entries],
  media: [...defaultConfig.media],
  bannerAutoPlay: defaultConfig.bannerAutoPlay,
  bannerInterval: defaultConfig.bannerInterval,
  showCourseCategory: defaultConfig.showCourseCategory,
  showNews: defaultConfig.showNews,
  showPartner: defaultConfig.showPartner,
})

/** 合并开关字段（后端老配置可能没有这些键，需保留默认值） */
function mergeSwitches(src: Partial<HomepageConfig>) {
  if (typeof src.bannerAutoPlay === 'boolean') config.bannerAutoPlay = src.bannerAutoPlay
  if (typeof src.bannerInterval === 'number') config.bannerInterval = src.bannerInterval
  if (typeof src.showCourseCategory === 'boolean') config.showCourseCategory = src.showCourseCategory
  if (typeof src.showNews === 'boolean') config.showNews = src.showNews
  if (typeof src.showPartner === 'boolean') config.showPartner = src.showPartner
}

// 加载配置
async function loadConfig() {
  // 先从 localStorage 加载数据
  const localConfig = getLocalData<HomepageConfig | null>(STORAGE_KEYS.HOMEPAGE_CONFIG, null)
  if (localConfig) {
    Object.assign(config.stats, localConfig.stats || defaultConfig.stats)
    config.entries = localConfig.entries?.length ? localConfig.entries : [...defaultConfig.entries]
    config.media = localConfig.media?.length ? localConfig.media : [...defaultConfig.media]
    mergeSwitches(localConfig)
  }
  try {
    const res = await getHomepageConfig()
    if (res?.code === 200 && res.data) {
      Object.assign(config.stats, res.data.stats || defaultConfig.stats)
      config.entries = res.data.entries?.length ? res.data.entries : [...defaultConfig.entries]
      config.media = res.data.media?.length ? res.data.media : [...defaultConfig.media]
      mergeSwitches(res.data)
    }
  } catch {
    // 接口不可用，保持当前配置
  }
}

onMounted(() => loadConfig())

// 保存配置到 localStorage
function saveConfigToLocal() {
  setLocalData(STORAGE_KEYS.HOMEPAGE_CONFIG, { ...config })
}

// 保存配置（后端为唯一数据源，写入失败必须明确报错，不能假装成功）
async function handleSave() {
  saving.value = true
  try {
    const res = await updateHomepageConfig({ ...config })
    if (res?.code !== 200) throw new Error('unexpected response')
    saveConfigToLocal()
    ElMessage.success('保存成功，前台已生效')
    opLog.logConfig('保存首页配置')
  } catch {
    ElMessage.error('保存到后端失败，请检查网络或稍后重试')
  } finally {
    saving.value = false
  }
}

// ==================== 核心入口表单 ====================
const entryDialogVisible = ref(false)
const isEditEntry = ref(false)
const entryFormRef = ref<FormInstance>()
const editEntryIndex = ref<number>(-1)

const entryForm = reactive<HomepageEntry>({
  id: 0,
  title: '',
  description: '',
  icon: '',
  link: '',
  bgColor: '',
})

const entryFormRules: FormRules = {
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入描述', trigger: 'blur' }],
  icon: [{ required: true, message: '请输入图标名称', trigger: 'blur' }],
  link: [{ required: true, message: '请输入链接', trigger: 'blur' }],
  bgColor: [{ required: true, message: '请输入背景色', trigger: 'blur' }],
}

function handleAddEntry() {
  isEditEntry.value = false
  editEntryIndex.value = -1
  Object.assign(entryForm, {
    id: Date.now(),
    title: '',
    description: '',
    icon: '',
    link: '',
    bgColor: 'linear-gradient(135deg, #165DFF, #36D399)',
  })
  entryDialogVisible.value = true
}

function handleEditEntry(row: HomepageEntry) {
  isEditEntry.value = true
  editEntryIndex.value = config.entries.findIndex((item) => item.id === row.id)
  Object.assign(entryForm, { ...row })
  entryDialogVisible.value = true
}

function handleDeleteEntry(index: number) {
  config.entries.splice(index, 1)
  saveConfigToLocal()
  ElMessage.success('删除成功')
}

async function submitEntryForm() {
  if (!entryFormRef.value) return
  await entryFormRef.value.validate((valid) => {
    if (!valid) return
    if (isEditEntry.value && editEntryIndex.value >= 0) {
      config.entries[editEntryIndex.value] = { ...entryForm }
      ElMessage.success('编辑成功')
    } else {
      config.entries.push({ ...entryForm })
      ElMessage.success('添加成功')
    }
    entryDialogVisible.value = false
    saveConfigToLocal()
  })
}

// ==================== 新媒体矩阵表单 ====================
const mediaDialogVisible = ref(false)
const isEditMedia = ref(false)
const mediaFormRef = ref<FormInstance>()
const editMediaIndex = ref<number>(-1)

const mediaForm = reactive<HomepageMedia>({
  id: 0,
  name: '',
  icon: '',
  link: '',
  bgColor: '',
})

const mediaFormRules: FormRules = {
  name: [{ required: true, message: '请输入平台名称', trigger: 'blur' }],
  icon: [{ required: true, message: '请输入图标名称', trigger: 'blur' }],
  bgColor: [{ required: true, message: '请输入背景色', trigger: 'blur' }],
}

function handleAddMedia() {
  isEditMedia.value = false
  editMediaIndex.value = -1
  Object.assign(mediaForm, {
    id: Date.now(),
    name: '',
    icon: '',
    link: '',
    bgColor: '#165DFF',
  })
  mediaDialogVisible.value = true
}

function handleEditMedia(row: HomepageMedia) {
  isEditMedia.value = true
  editMediaIndex.value = config.media.findIndex((item) => item.id === row.id)
  Object.assign(mediaForm, { ...row })
  mediaDialogVisible.value = true
}

function handleDeleteMedia(index: number) {
  config.media.splice(index, 1)
  saveConfigToLocal()
  ElMessage.success('删除成功')
}

async function submitMediaForm() {
  if (!mediaFormRef.value) return
  await mediaFormRef.value.validate((valid) => {
    if (!valid) return
    if (isEditMedia.value && editMediaIndex.value >= 0) {
      config.media[editMediaIndex.value] = { ...mediaForm }
      ElMessage.success('编辑成功')
    } else {
      config.media.push({ ...mediaForm })
      ElMessage.success('添加成功')
    }
    mediaDialogVisible.value = false
    saveConfigToLocal()
  })
}
</script>

<style scoped lang="scss">
.homepage-config-page {
  padding: 20px;
}

.page-title {
  font-size: 22px;
  color: #1D2129;
  margin-bottom: 20px;
}

.section-header-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.section-subtitle {
  font-size: 16px;
  color: #4E5969;
  margin: 0;
}

.color-preview {
  width: 80px;
  height: 32px;
  border-radius: 6px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 11px;
  text-shadow: 0 1px 2px rgba(0, 0, 0, 0.3);
}

.form-tip {
  font-size: 12px;
  color: #86909C;
  margin-top: 4px;
}

.display-tip {
  margin-bottom: 20px;
}

.form-tip-inline {
  font-size: 12px;
  color: #86909C;
  margin-left: 12px;
}
</style>
