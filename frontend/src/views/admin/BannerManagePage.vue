<template>
  <!-- Banner管理后台：字段与后端 cms_banner 实体一一对应 -->
  <div class="banner-manage-page">
    <div class="page-header">
      <h2>Banner管理</h2>
      <el-button type="primary" @click="handleCreate">添加Banner</el-button>
    </div>

    <el-alert
      type="info"
      :closable="false"
      show-icon
      class="banner-tip"
      title="首页轮播直接读取此处配置（按位置 home + 启用状态 + 排序权重），保存后前台即可看到"
    />

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索Banner标题" clearable style="width: 240px" />
      <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 120px">
        <el-option label="已启用" value="enabled" />
        <el-option label="已禁用" value="disabled" />
      </el-select>
      <el-select v-model="positionFilter" placeholder="展示位置" clearable style="width: 140px">
        <el-option label="首页" value="home" />
        <el-option label="其他" value="other" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button v-if="selectedIds.length > 0" type="danger" @click="handleBatchDelete">批量删除({{ selectedIds.length }})</el-button>
      <el-button @click="handleExport">导出CSV</el-button>
    </div>

    <!-- Banner列表 -->
    <el-table :data="pagedList" stripe v-loading="loading" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="标题" min-width="160" />
      <el-table-column label="图片" width="100">
        <template #default="{ row }">
          <el-image
            v-if="row.imageUrl"
            :src="row.imageUrl"
            fit="cover"
            style="width: 60px; height: 34px; border-radius: 4px"
            :preview-teleported="true"
          />
          <span v-else class="text-muted">无</span>
        </template>
      </el-table-column>
      <el-table-column prop="linkUrl" label="跳转链接" min-width="160" show-overflow-tooltip />
      <el-table-column prop="position" label="位置" width="90" />
      <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
      <el-table-column label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-switch :model-value="row.status === 1" @change="(v) => handleStatusChange(row as BannerItem, v === true)" />
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="170" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" text type="primary" @click="handleEdit(row as BannerItem)">编辑</el-button>
          <el-button size="small" text type="danger" @click="handleDelete(row as BannerItem)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <Pagination :total="total" v-model:page="pageNum" v-model:page-size="pageSize" />

    <!-- 添加/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑Banner' : '添加Banner'"
      width="600px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="110px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="首页轮播主标题" maxlength="40" show-word-limit />
        </el-form-item>
        <el-form-item label="图片地址" prop="imageUrl">
          <el-input v-model="form.imageUrl" placeholder="图片 URL，留空则使用默认渐变背景" />
          <div class="form-hint">建议尺寸 1920×520，支持 /api/file/public/... 或完整 URL</div>
        </el-form-item>
        <el-form-item label="跳转链接" prop="linkUrl">
          <el-input v-model="form.linkUrl" placeholder="站内路径如 /exam/1，或完整外链 https://..." />
          <div class="form-hint">留空则轮播不显示按钮；http(s) 外链会新开窗口打开</div>
        </el-form-item>
        <el-form-item label="展示位置" prop="position">
          <el-select v-model="form.position" placeholder="请选择展示位置" style="width: 100%">
            <el-option label="首页（home）" value="home" />
            <el-option label="其他" value="other" />
          </el-select>
          <div class="form-hint">前台目前只读取 home 位置的 Banner</div>
        </el-form-item>
        <el-form-item label="排序权重" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="是否启用" prop="status">
          <el-switch :model-value="form.status === 1" @change="(v) => (form.status = v === true ? 1 : 0)" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * BannerManagePage - Banner管理后台
 * 字段与后端 cms_banner 实体对齐：title / imageUrl / linkUrl / position / sortOrder / status
 * 首页 HomePage.vue 通过 GET /api/banner?position=home 消费。
 */
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import Pagination from '@/components/common/Pagination.vue'
import { bannerApi } from '@/api/admin'
import { useOperationLog } from '@/composables/useOperationLog'
const opLog = useOperationLog('banner')

// ==================== 搜索与筛选 ====================
const keyword = ref('')
const statusFilter = ref('')
const positionFilter = ref('')

// ==================== 分页与加载状态 ====================
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

// ==================== 数据 ====================
interface BannerItem {
  id: number
  title: string
  imageUrl: string
  linkUrl: string
  position: string
  sortOrder: number
  status: number
  createdAt?: string
}

const bannerList = ref<BannerItem[]>([])

// ==================== 过滤列表 ====================
const filteredList = computed(() => {
  return bannerList.value.filter((item) => {
    const matchKeyword = !keyword.value || item.title?.includes(keyword.value) || item.linkUrl?.includes(keyword.value)
    const matchStatus = !statusFilter.value || (statusFilter.value === 'enabled' ? item.status === 1 : item.status !== 1)
    const matchPosition = !positionFilter.value ||
      (positionFilter.value === 'home' ? item.position === 'home' : item.position !== 'home')
    return matchKeyword && matchStatus && matchPosition
  })
})

watch(filteredList, (list) => {
  total.value = list.length
  const maxPage = Math.max(1, Math.ceil(list.length / pageSize.value))
  if (pageNum.value > maxPage) pageNum.value = maxPage
}, { immediate: true })

const pagedList = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  return filteredList.value.slice(start, start + pageSize.value)
})

// ==================== 加载数据 ====================
async function fetchList() {
  loading.value = true
  try {
    const res: any = await bannerApi.list({ pageNum: 1, pageSize: 9999 })
    const list = res?.data?.list || res?.data?.records || res?.data
    bannerList.value = Array.isArray(list) ? list.map((r: any) => ({
      id: r.id,
      title: r.title || '',
      imageUrl: r.imageUrl || '',
      linkUrl: r.linkUrl || '',
      position: r.position || 'home',
      sortOrder: r.sortOrder ?? 0,
      status: r.status ?? 0,
      createdAt: r.createdAt ?? r.createTime ?? '',
    })) : []
  } catch {
    bannerList.value = []
    ElMessage.error('列表加载失败，请检查网络或稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => fetchList())

function handleSearch() {
  pageNum.value = 1
}

// ==================== 状态切换 ====================
async function handleStatusChange(row: BannerItem, v: boolean) {
  try {
    await bannerApi.update(row.id, { ...row, status: v ? 1 : 0 })
    row.status = v ? 1 : 0
    ElMessage.success(v ? '已启用' : '已禁用')
  } catch {
    ElMessage.error('操作失败，请检查网络或稍后重试')
  }
}

// ==================== 删除 ====================
function handleDelete(row: BannerItem) {
  ElMessageBox.confirm(`确定删除Banner "${row.title}" 吗？`, '警告', { type: 'warning' })
    .then(async () => {
      try {
        await bannerApi.remove(row.id)
        bannerList.value = bannerList.value.filter((b) => b.id !== row.id)
        ElMessage.success('删除成功')
        opLog.logDelete('删除Banner')
      } catch {
        ElMessage.error('操作失败，请检查网络或稍后重试')
      }
    })
    .catch(() => {})
}

// ==================== 批量选择与删除 ====================
const selectedIds = ref<number[]>([])

function handleSelectionChange(rows: BannerItem[]) {
  selectedIds.value = rows.map(r => r.id)
}

function handleBatchDelete() {
  ElMessageBox.confirm(`确定批量删除选中的 ${selectedIds.value.length} 个Banner吗？`, '警告', { type: 'warning' })
    .then(async () => {
      try {
        await bannerApi.batchRemove(selectedIds.value)
        bannerList.value = bannerList.value.filter(b => !selectedIds.value.includes(b.id))
        selectedIds.value = []
        ElMessage.success('批量删除成功')
      } catch {
        ElMessage.error('操作失败，请检查网络或稍后重试')
      }
    })
    .catch(() => {})
}

// ==================== 导出CSV ====================
function handleExport() {
  const headers = ['ID', '标题', '图片地址', '跳转链接', '位置', '排序', '状态', '创建时间']
  const rows = filteredList.value.map(b => [
    b.id, b.title, b.imageUrl, b.linkUrl, b.position, b.sortOrder,
    b.status === 1 ? '已启用' : '已禁用', b.createdAt || '',
  ])
  const csv = [headers.join(','), ...rows.map(r => r.map(c => `"${c}"`).join(','))].join('\n')
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `Banner数据_${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

// ==================== 弹窗表单 ====================
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const editId = ref<number | null>(null)

const defaultForm: Omit<BannerItem, 'id'> = {
  title: '',
  imageUrl: '',
  linkUrl: '',
  position: 'home',
  sortOrder: 0,
  status: 1,
}

const form = ref<Omit<BannerItem, 'id'>>({ ...defaultForm })

const formRules: FormRules = {
  title: [{ required: true, message: '请输入Banner标题', trigger: 'blur' }],
  position: [{ required: true, message: '请选择展示位置', trigger: 'change' }],
}

function handleCreate() {
  isEdit.value = false
  editId.value = null
  form.value = { ...defaultForm }
  dialogVisible.value = true
}

function handleEdit(row: BannerItem) {
  isEdit.value = true
  editId.value = row.id
  form.value = {
    title: row.title,
    imageUrl: row.imageUrl,
    linkUrl: row.linkUrl,
    position: row.position || 'home',
    sortOrder: row.sortOrder,
    status: row.status,
  }
  dialogVisible.value = true
}

async function submitForm() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const now = new Date()
      const pad = (n: number) => String(n).padStart(2, '0')
      const createdAt = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
      if (isEdit.value && editId.value !== null) {
        await bannerApi.update(editId.value, form.value)
        const idx = bannerList.value.findIndex((b) => b.id === editId.value)
        if (idx !== -1) bannerList.value[idx] = { ...bannerList.value[idx], ...form.value }
        ElMessage.success('编辑成功')
        opLog.logUpdate('编辑Banner')
      } else {
        const res: any = await bannerApi.create(form.value)
        const createdId = res?.code === 200 && res.data?.id ? res.data.id : Math.max(...bannerList.value.map((b) => b.id), 0) + 1
        bannerList.value.unshift({ id: createdId, ...form.value, createdAt })
        ElMessage.success('添加成功')
        opLog.logCreate('新增Banner')
      }
      dialogVisible.value = false
    } catch {
      ElMessage.error('操作失败，请检查网络或稍后重试')
    } finally {
      submitting.value = false
    }
  })
}
</script>

<style scoped lang="scss">
.banner-manage-page {
  padding: 20px;
}

.banner-tip {
  margin-bottom: 16px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;

  h2 {
    font-size: 20px;
    color: #1D2129;
  }
}

.search-bar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.form-hint {
  font-size: 12px;
  color: #86909C;
  line-height: 1.6;
  margin-top: 4px;
}

.text-muted {
  color: #C9CDD4;
}
</style>
