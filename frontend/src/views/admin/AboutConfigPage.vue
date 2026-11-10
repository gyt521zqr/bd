<template>
  <!-- 关于我们配置管理后台 -->
  <div class="about-config-page">
    <h2 class="page-title">关于我们配置</h2>
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 平台简介配置 -->
      <el-tab-pane label="平台简介" name="intro">
        <el-form :model="config" label-width="140px" size="default">
          <el-form-item label="平台标题">
            <el-input v-model="config.title" placeholder="如：码高少儿编程教育平台" maxlength="50" show-word-limit />
          </el-form-item>
          <el-form-item label="成立时间">
            <el-input v-model="config.foundedYear" placeholder="如：2022" style="width: 200px" />
          </el-form-item>
          <el-form-item label="平台简介">
            <el-input
              v-model="config.content"
              type="textarea"
              :rows="6"
              placeholder="请输入平台简介内容"
              maxlength="500"
              show-word-limit
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" :loading="saving" @click="handleSave">保存简介配置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 发展历程配置 -->
      <el-tab-pane label="发展历程" name="timeline">
        <div class="section-header-row">
          <h3 class="section-subtitle">平台发展历程时间线节点配置</h3>
          <el-button type="primary" size="small" @click="handleAddTimeline">新增节点</el-button>
        </div>
        <el-table :data="sortedTimeline" stripe row-key="id" style="margin-bottom: 16px">
          <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
          <el-table-column prop="year" label="年份" width="90" />
          <el-table-column prop="title" label="标题" min-width="150" />
          <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row, $index }">
              <el-button size="small" text type="primary" @click="handleEditTimeline(row as TimelineItem)">编辑</el-button>
              <el-button size="small" text @click="handleMoveTimeline($index, -1)">上移</el-button>
              <el-button size="small" text @click="handleMoveTimeline($index, 1)">下移</el-button>
              <el-button size="small" text type="danger" @click="handleDeleteTimeline(row as TimelineItem)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-button type="primary" :loading="saving" @click="handleSave">保存发展历程</el-button>

        <!-- 新增/编辑时间线弹窗 -->
        <el-dialog v-model="timelineDialogVisible" :title="isEditTimeline ? '编辑节点' : '新增节点'" width="560px" destroy-on-close>
          <el-form ref="timelineFormRef" :model="timelineForm" :rules="timelineFormRules" label-width="100px">
            <el-form-item label="年份" prop="year">
              <el-input v-model="timelineForm.year" placeholder="如：2024" style="width: 200px" />
            </el-form-item>
            <el-form-item label="标题" prop="title">
              <el-input v-model="timelineForm.title" placeholder="请输入标题" maxlength="30" show-word-limit />
            </el-form-item>
            <el-form-item label="描述" prop="description">
              <el-input v-model="timelineForm.description" type="textarea" :rows="3" placeholder="请输入描述" maxlength="200" show-word-limit />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="timelineDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitTimelineForm">确定</el-button>
          </template>
        </el-dialog>
      </el-tab-pane>

      <!-- 核心团队配置 -->
      <el-tab-pane label="核心团队" name="team">
        <div class="section-header-row">
          <h3 class="section-subtitle">核心团队成员信息管理</h3>
          <el-button type="primary" size="small" @click="handleAddTeam">新增成员</el-button>
        </div>
        <el-table :data="config.team" stripe row-key="id" style="margin-bottom: 16px">
          <el-table-column prop="id" label="ID" width="60" />
          <el-table-column prop="name" label="姓名" width="100" />
          <el-table-column prop="position" label="职位" width="140" />
          <el-table-column prop="description" label="简介" min-width="200" show-overflow-tooltip />
          <el-table-column prop="avatar" label="头像" width="80">
            <template #default="{ row }">
              <div v-if="row.avatar" class="avatar-preview">
                <img :src="row.avatar" alt="avatar" />
              </div>
              <div v-else class="avatar-placeholder">无</div>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button size="small" text type="primary" @click="handleEditTeam(row as TeamMember)">编辑</el-button>
              <el-button size="small" text type="danger" @click="handleDeleteTeam(row as TeamMember)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-button type="primary" :loading="saving" @click="handleSave">保存团队配置</el-button>

        <!-- 新增/编辑成员弹窗 -->
        <el-dialog v-model="teamDialogVisible" :title="isEditTeam ? '编辑成员' : '新增成员'" width="560px" destroy-on-close>
          <el-form ref="teamFormRef" :model="teamForm" :rules="teamFormRules" label-width="100px">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="teamForm.name" placeholder="请输入姓名" maxlength="20" show-word-limit />
            </el-form-item>
            <el-form-item label="职位" prop="position">
              <el-input v-model="teamForm.position" placeholder="请输入职位" maxlength="30" show-word-limit />
            </el-form-item>
            <el-form-item label="简介" prop="description">
              <el-input v-model="teamForm.description" type="textarea" :rows="3" placeholder="请输入简介" maxlength="200" show-word-limit />
            </el-form-item>
            <el-form-item label="头像" prop="avatar">
              <ImageUploader
                v-model="teamForm.avatar"
                type="team-avatar"
                :preview-width="120"
                :preview-height="120"
                preset="avatar"
              />
            </el-form-item>
          </el-form>
          <template #footer>
            <el-button @click="teamDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitTeamForm">确定</el-button>
          </template>
        </el-dialog>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
/**
 * AboutConfigPage - 关于我们配置管理后台
 * 平台简介、发展历程、核心团队
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { getAboutConfig, updateAboutConfig, type AboutConfig, type TimelineItem, type TeamMember } from '@/api/about'
import { setLocalData, getLocalData, STORAGE_KEYS } from '@/utils/localStore'
import ImageUploader from '@/components/common/ImageUploader.vue'
import { useOperationLog } from '@/composables/useOperationLog'

const opLog = useOperationLog('about')

const activeTab = ref('intro')
const saving = ref(false)

// 默认配置：仅保留平台自我介绍，发展历程/核心团队不再预制虚构示例条目
const defaultConfig: AboutConfig = {
  title: '码高少儿编程教育平台',
  content: '镇原码高成立于2022年，是镇原县本地领先的少儿编程教育品牌。我们致力于为4-16岁青少年提供高质量的编程教育服务，通过趣味化、项目式的教学方法，激发孩子的创造力与逻辑思维，培养面向未来的科技人才。',
  foundedYear: '2022',
  timeline: [],
  team: [],
}

const config = reactive<AboutConfig>({
  title: defaultConfig.title,
  content: defaultConfig.content,
  foundedYear: defaultConfig.foundedYear,
  timeline: [...defaultConfig.timeline],
  team: [...defaultConfig.team],
})

const sortedTimeline = computed(() => {
  return [...config.timeline].sort((a, b) => a.sortOrder - b.sortOrder)
})

/** 把配置应用到表单；空数组保持为空，绝不回填默认示例 */
function applyConfig(data: AboutConfig) {
  config.title = data.title ?? ''
  config.content = data.content ?? ''
  config.foundedYear = data.foundedYear ?? ''
  config.timeline = Array.isArray(data.timeline) ? [...data.timeline] : []
  config.team = Array.isArray(data.team) ? [...data.team] : []
}

/** localStorage 仅作最近一次后端数据的缓存，不再是权威数据源 */
function cacheConfigToLocal() {
  setLocalData(STORAGE_KEYS.ABOUT_CONFIG, { ...config })
}

// 加载配置：后端为准；后端不可用或返回空时才读取本地缓存
async function loadConfig() {
  try {
    const res = await getAboutConfig()
    if (res?.code === 200 && res.data) {
      applyConfig(res.data)
      cacheConfigToLocal()
      return
    }
  } catch {
    // 后端接口不可用，落到本地缓存
  }
  const localConfig = getLocalData<AboutConfig | null>(STORAGE_KEYS.ABOUT_CONFIG, null)
  if (localConfig) applyConfig(localConfig)
}

onMounted(() => loadConfig())

// 保存配置：只有后端写入成功才提示成功，失败明确告知管理员
async function handleSave() {
  saving.value = true
  try {
    const res = await updateAboutConfig({ ...config })
    if (res?.code === 200) {
      cacheConfigToLocal()
      ElMessage.success('保存成功')
      opLog.logConfig('保存关于我们配置')
    } else {
      ElMessage.error(res?.message || '保存失败，请稍后重试')
    }
  } catch (e) {
    const msg = e instanceof Error ? e.message : ''
    ElMessage.error(`保存失败：${msg || '后端接口不可用，配置未写入服务器'}`)
  } finally {
    saving.value = false
  }
}

// ==================== 时间线表单 ====================
const timelineDialogVisible = ref(false)
const isEditTimeline = ref(false)
const timelineFormRef = ref<FormInstance>()
const editTimelineId = ref<number | null>(null)

const timelineForm = reactive<TimelineItem>({
  id: 0,
  year: '',
  title: '',
  description: '',
  sortOrder: 0,
})

const timelineFormRules: FormRules = {
  year: [{ required: true, message: '请输入年份', trigger: 'blur' }],
  title: [{ required: true, message: '请输入标题', trigger: 'blur' }],
  description: [{ required: true, message: '请输入描述', trigger: 'blur' }],
}

function handleAddTimeline() {
  isEditTimeline.value = false
  editTimelineId.value = null
  const maxOrder = config.timeline.length > 0
    ? Math.max(...config.timeline.map((t) => t.sortOrder))
    : 0
  Object.assign(timelineForm, {
    id: Date.now(),
    year: '',
    title: '',
    description: '',
    sortOrder: maxOrder + 1,
  })
  timelineDialogVisible.value = true
}

function handleEditTimeline(row: TimelineItem) {
  isEditTimeline.value = true
  editTimelineId.value = row.id
  Object.assign(timelineForm, { ...row })
  timelineDialogVisible.value = true
}

function handleDeleteTimeline(row: TimelineItem) {
  const idx = config.timeline.findIndex((item) => item.id === row.id)
  if (idx !== -1) {
    config.timeline.splice(idx, 1)
    ElMessage.success('已从列表移除，点击「保存发展历程」后生效')
  }
}

function handleMoveTimeline(index: number, direction: number) {
  const arr = sortedTimeline.value
  const currentItem = arr[index]
  const targetIndex = index + direction
  if (targetIndex < 0 || targetIndex >= arr.length) return

  const targetItem = arr[targetIndex]
  const temp = currentItem.sortOrder
  currentItem.sortOrder = targetItem.sortOrder
  targetItem.sortOrder = temp

  // 同步回原始数组
  const cIdx = config.timeline.findIndex((item) => item.id === currentItem.id)
  const tIdx = config.timeline.findIndex((item) => item.id === targetItem.id)
  if (cIdx !== -1) config.timeline[cIdx].sortOrder = currentItem.sortOrder
  if (tIdx !== -1) config.timeline[tIdx].sortOrder = targetItem.sortOrder
}

async function submitTimelineForm() {
  if (!timelineFormRef.value) return
  await timelineFormRef.value.validate((valid) => {
    if (!valid) return
    if (isEditTimeline.value && editTimelineId.value !== null) {
      const idx = config.timeline.findIndex((item) => item.id === editTimelineId.value)
      if (idx !== -1) {
        config.timeline[idx] = { ...timelineForm }
      }
      ElMessage.success('已更新节点，点击「保存发展历程」后生效')
    } else {
      config.timeline.push({ ...timelineForm })
      ElMessage.success('已添加节点，点击「保存发展历程」后生效')
    }
    timelineDialogVisible.value = false
  })
}

// ==================== 团队成员表单 ====================
const teamDialogVisible = ref(false)
const isEditTeam = ref(false)
const teamFormRef = ref<FormInstance>()
const editTeamId = ref<number | null>(null)

const teamForm = reactive<TeamMember>({
  id: 0,
  name: '',
  position: '',
  description: '',
  avatar: '',
})

const teamFormRules: FormRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  position: [{ required: true, message: '请输入职位', trigger: 'blur' }],
  description: [{ required: true, message: '请输入简介', trigger: 'blur' }],
}

function handleAddTeam() {
  isEditTeam.value = false
  editTeamId.value = null
  Object.assign(teamForm, {
    id: Date.now(),
    name: '',
    position: '',
    description: '',
    avatar: '',
  })
  teamDialogVisible.value = true
}

function handleEditTeam(row: TeamMember) {
  isEditTeam.value = true
  editTeamId.value = row.id
  Object.assign(teamForm, { ...row })
  teamDialogVisible.value = true
}

function handleDeleteTeam(row: TeamMember) {
  const idx = config.team.findIndex((item) => item.id === row.id)
  if (idx !== -1) {
    config.team.splice(idx, 1)
    ElMessage.success('已从列表移除，点击「保存团队配置」后生效')
  }
}

async function submitTeamForm() {
  if (!teamFormRef.value) return
  await teamFormRef.value.validate((valid) => {
    if (!valid) return
    if (isEditTeam.value && editTeamId.value !== null) {
      const idx = config.team.findIndex((item) => item.id === editTeamId.value)
      if (idx !== -1) {
        config.team[idx] = { ...teamForm }
      }
      ElMessage.success('已更新成员，点击「保存团队配置」后生效')
    } else {
      config.team.push({ ...teamForm })
      ElMessage.success('已添加成员，点击「保存团队配置」后生效')
    }
    teamDialogVisible.value = false
  })
}
</script>

<style scoped lang="scss">
.about-config-page {
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

.avatar-preview {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F7F8FA;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.avatar-placeholder {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #E5E6EB;
  color: #86909C;
  font-size: 12px;
}

.form-tip {
  font-size: 12px;
  color: #86909C;
  margin-top: 4px;
}
</style>
