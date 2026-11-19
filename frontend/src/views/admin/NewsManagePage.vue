<template>
  <!-- 资讯/公告管理后台 -->
  <div class="news-manage-page">
    <div class="page-header">
      <h2>资讯管理</h2>
      <el-button type="primary" @click="handleCreate">新增资讯</el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索标题" clearable style="width: 240px" />
      <el-select v-model="categoryFilter" placeholder="分类筛选" clearable style="width: 140px">
        <el-option label="行业动态" value="industry" />
        <el-option label="教育资讯" value="education" />
        <el-option label="赛事新闻" value="contest" />
        <el-option label="平台公告" value="platform" />
      </el-select>
      <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 120px">
        <el-option label="已发布" value="published" />
        <el-option label="草稿" value="draft" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button v-if="selectedIds.length > 0" type="danger" @click="handleBatchDelete">批量删除({{ selectedIds.length }})</el-button>
      <el-button v-if="selectedIds.length > 0" type="success" @click="handleBatchPublish">批量发布</el-button>
      <el-button @click="handleExport">导出CSV</el-button>
    </div>

    <!-- 资讯列表 -->
    <el-table :data="pagedList" stripe v-loading="loading" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="标题" min-width="180" show-overflow-tooltip />
      <el-table-column prop="category" label="分类" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="categoryTagMap[row.category]?.type">
            {{ categoryTagMap[row.category]?.label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90" align="center">
        <template #default="{ row }">
          <el-switch
            v-model="row.status"
            active-value="published"
            inactive-value="draft"
            @change="handleStatusChange(row as NewsItem)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="isPinned" label="置顶" width="70" align="center">
        <template #default="{ row }">
          <el-tag v-if="row.isPinned" type="danger" size="small">置顶</el-tag>
          <span v-else class="text-muted">-</span>
        </template>
      </el-table-column>
      <el-table-column prop="publishTime" label="发布时间" width="170" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" text type="primary" @click="handleView(row as NewsItem)">查看</el-button>
          <el-button size="small" text type="primary" @click="handleEdit(row as NewsItem)">编辑</el-button>
          <el-button size="small" text :type="row.isPinned ? 'info' : 'warning'" @click="handleTogglePin(row as NewsItem)">
            {{ row.isPinned ? '取消置顶' : '置顶' }}
          </el-button>
          <el-button size="small" text type="danger" @click="handleDelete(row as NewsItem)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination-wrap">
      <el-pagination
        v-model:current-page="page"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑资讯' : '新增资讯'"
      width="720px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入资讯标题" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="分类" prop="category">
          <el-select v-model="form.category" placeholder="请选择分类" style="width: 100%">
            <el-option label="行业动态" value="industry" />
            <el-option label="教育资讯" value="education" />
            <el-option label="赛事新闻" value="contest" />
            <el-option label="平台公告" value="platform" />
          </el-select>
        </el-form-item>
        <el-form-item label="封面图" prop="coverImage">
          <ImageUploader
            v-model="form.coverImage"
            type="news-cover"
            :preview-width="240"
            :preview-height="135"
            preset="cover"
          />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            :rows="8"
            placeholder="请输入资讯内容，支持HTML"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="published">已发布</el-radio>
            <el-radio label="draft">草稿</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="是否置顶" prop="isPinned">
          <el-switch v-model="form.isPinned" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewDialogVisible" title="资讯详情" width="640px">
      <div v-if="viewRow" class="view-body" style="line-height: 2;">
        <p><strong>标题：</strong>{{ viewRow.title }}</p>
        <p><strong>分类：</strong>{{ categoryTagMap[viewRow.category]?.label }}</p>
        <p><strong>状态：</strong>{{ viewRow.status === 'published' ? '已发布' : '草稿' }}</p>
        <p><strong>置顶：</strong>{{ viewRow.isPinned ? '是' : '否' }}</p>
        <p><strong>发布时间：</strong>{{ viewRow.publishTime }}</p>
        <p><strong>内容：</strong></p>
        <div style="background: #f5f7fa; border-radius: 8px; padding: 12px 16px; margin: 8px 0; white-space: pre-wrap;" v-html="sanitizeHtml(viewRow.content)"></div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * NewsManagePage - 资讯/公告管理后台
 * 数据源：后端 /api/admin/news/* （读 AdminController，写 AdminWriteController）
 * 接口失败时清空列表并提示，不回填任何本地数据。
 */
import { ref, computed, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { newsApi } from '@/api/admin'
import { type NewsItem } from '@/api/news'
import { useOperationLog } from '@/composables/useOperationLog'
import { sanitizeHtml } from '@/utils/sanitize'
import ImageUploader from '@/components/common/ImageUploader.vue'

const opLog = useOperationLog('news')

// ==================== 分类映射 ====================
const categoryTagMap: Record<string, { label: string; type: 'primary' | 'success' | 'warning' | 'info' | 'danger' }> = {
  industry: { label: '行业动态', type: 'primary' },
  education: { label: '教育资讯', type: 'success' },
  contest: { label: '赛事新闻', type: 'warning' },
  platform: { label: '平台公告', type: 'info' },
}

// ==================== 搜索与筛选 ====================
const keyword = ref('')
const categoryFilter = ref('')
const statusFilter = ref('')

// ==================== 列表数据 ====================
const loading = ref(false)
const page = ref(1)
const pageSize = ref(10)
const total = ref(0)

const newsList = ref<NewsItem[]>([])

// ==================== 过滤列表 ====================
const filteredList = computed(() => {
  return newsList.value.filter((item) => {
    const matchKeyword = !keyword.value || (item.title || '').includes(keyword.value)
    const matchCategory = !categoryFilter.value || item.category === categoryFilter.value
    const matchStatus = !statusFilter.value || item.status === statusFilter.value
    return matchKeyword && matchCategory && matchStatus
  })
})

// 当前页展示数据（分页切片）
const pagedList = computed(() => {
  const start = (page.value - 1) * pageSize.value
  return filteredList.value.slice(start, start + pageSize.value)
})

// 监听过滤结果，同步总数，避免分页 total 与列表不一致
watch(filteredList, (list) => {
  total.value = list.length
}, { immediate: true })

// ==================== 加载数据 ====================
async function loadData() {
  loading.value = true
  try {
    const res: any = await newsApi.list({
      pageNum: page.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined,
      category: categoryFilter.value || undefined,
      status: statusFilter.value || undefined,
    })
    const data = res?.data?.list
    if (res && res.code === 200 && Array.isArray(data)) {
      newsList.value = data
      total.value = res.data.total ?? data.length
    } else {
      newsList.value = []
      total.value = 0
      ElMessage.error('数据加载失败，请检查网络或稍后重试')
    }
  } catch {
    newsList.value = []
    total.value = 0
    ElMessage.error('数据加载失败，请检查网络或稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadData()
})

// ==================== 搜索 ====================
function handleSearch() {
  page.value = 1
  loadData()
}

function handleSizeChange(val: number) {
  pageSize.value = val
  page.value = 1
  loadData()
}

function handlePageChange(val: number) {
  page.value = val
  loadData()
}

// ==================== 状态切换 ====================
async function handleStatusChange(row: NewsItem) {
  const prevStatus = row.status === 'published' ? 'draft' : 'published'
  try {
    await newsApi.update(row.id, { ...row, status: row.status })
    ElMessage.success(row.status === 'published' ? '已发布' : '已下架')
    opLog.logUpdate('切换资讯状态')
  } catch {
    row.status = prevStatus
    ElMessage.error('状态切换失败')
  }
}

// ==================== 置顶切换 ====================
async function handleTogglePin(row: NewsItem) {
  const newPinned = !row.isPinned
  try {
    await newsApi.update(row.id, { ...row, isPinned: newPinned })
    row.isPinned = newPinned
    ElMessage.success(newPinned ? '已置顶' : '已取消置顶')
    opLog.logUpdate('切换资讯置顶')
  } catch {
    ElMessage.error('置顶操作失败')
  }
}

// ==================== 删除 ====================
function handleDelete(row: NewsItem) {
  ElMessageBox.confirm(`确定删除资讯 "${row.title}" 吗？`, '警告', { type: 'warning' })
    .then(async () => {
      try {
        await newsApi.remove(row.id)
        const idx = newsList.value.findIndex((n) => n.id === row.id)
        if (idx !== -1) newsList.value.splice(idx, 1)
        ElMessage.success('删除成功')
        opLog.logDelete('删除资讯')
      } catch {
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

// ==================== 弹窗表单 ====================
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const editId = ref<number | null>(null)

const defaultForm = {
  title: '',
  category: '',
  content: '',
  coverImage: '',
  status: 'draft' as 'published' | 'draft',
  isPinned: false,
}

const form = ref({ ...defaultForm })

const formRules: FormRules = {
  title: [{ required: true, message: '请输入资讯标题', trigger: 'blur' }],
  category: [{ required: true, message: '请选择分类', trigger: 'change' }],
  content: [{ required: true, message: '请输入资讯内容', trigger: 'blur' }],
}

function handleCreate() {
  isEdit.value = false
  editId.value = null
  form.value = { ...defaultForm }
  dialogVisible.value = true
}

function handleEdit(row: NewsItem) {
  isEdit.value = true
  editId.value = row.id
  form.value = {
    title: row.title,
    category: row.category,
    content: row.content,
    coverImage: row.coverImage || '',
    status: row.status,
    isPinned: row.isPinned,
  }
  dialogVisible.value = true
}

async function submitForm() {
  if (!formRef.value) return
  await formRef.value.validate((valid) => {
    if (!valid) return
    submitting.value = true
    const now = new Date()
    const pad = (n: number) => String(n).padStart(2, '0')
    const publishTime = `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
    ;(async () => {
      try {
        if (isEdit.value && editId.value !== null) {
          await newsApi.update(editId.value, form.value)
          const idx = newsList.value.findIndex((n) => n.id === editId.value)
          if (idx !== -1) {
            newsList.value[idx] = { ...newsList.value[idx], ...form.value }
          }
          ElMessage.success('编辑成功')
          opLog.logUpdate('编辑资讯')
        } else {
          const res: any = await newsApi.create(form.value)
          const newId = res?.data?.id ?? Math.max(...newsList.value.map((n) => n.id), 0) + 1
          newsList.value.unshift({
            id: newId,
            ...form.value,
            publishTime,
            createTime: publishTime,
          } as NewsItem)
          ElMessage.success('新增成功')
          opLog.logCreate('发布资讯')
        }
        dialogVisible.value = false
      } catch {
        ElMessage.error('保存失败')
      } finally {
        submitting.value = false
      }
    })()
  })
}

// ==================== 批量操作与导出 ====================
const selectedIds = ref<number[]>([])

function handleSelectionChange(rows: NewsItem[]) {
  selectedIds.value = rows.map((r) => r.id)
}

function handleBatchDelete() {
  ElMessageBox.confirm(`确定批量删除选中的 ${selectedIds.value.length} 条资讯吗？`, '警告', { type: 'warning' })
    .then(async () => {
      try {
        await newsApi.batchRemove(selectedIds.value)
        newsList.value = newsList.value.filter((n) => !selectedIds.value.includes(n.id))
        selectedIds.value = []
        ElMessage.success('批量删除成功')
        opLog.logDelete('批量删除资讯')
      } catch {
        ElMessage.error('批量删除失败')
      }
    })
    .catch(() => {})
}

async function handleBatchPublish() {
  ElMessageBox.confirm(`确定批量发布选中的 ${selectedIds.value.length} 条资讯吗？`, '提示', { type: 'warning' })
    .then(async () => {
      const targets = newsList.value.filter((n) => selectedIds.value.includes(n.id))
      let failed = 0
      for (const n of targets) {
        try {
          await newsApi.update(n.id, { ...n, status: 'published' })
          n.status = 'published'
        } catch {
          failed++
        }
      }
      selectedIds.value = []
      if (failed > 0) {
        ElMessage.error(`批量发布失败，${failed} 条未成功`)
      } else {
        ElMessage.success('批量发布成功')
        opLog.logUpdate('批量发布资讯')
      }
    })
    .catch(() => {})
}

function handleExport() {
  const headers = ['ID', '标题', '分类', '状态', '置顶', '发布时间']
  const rows = filteredList.value.map((n) => [
    n.id,
    n.title,
    categoryTagMap[n.category]?.label || n.category,
    n.status === 'published' ? '已发布' : '草稿',
    n.isPinned ? '是' : '否',
    n.publishTime || '',
  ])
  const csv = [headers.join(','), ...rows.map((r) => r.map((c) => `"${c}"`).join(','))].join('\n')
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `资讯数据_${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

// ==================== 查看详情 ====================
const viewDialogVisible = ref(false)
const viewRow = ref<NewsItem | null>(null)

function handleView(row: NewsItem) {
  viewRow.value = row
  viewDialogVisible.value = true
}
</script>

<style lang="scss">
$primary: #165DFF;

.news-manage-page {
  padding: 20px;
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

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.text-muted {
  color: #86909C;
}
</style>
