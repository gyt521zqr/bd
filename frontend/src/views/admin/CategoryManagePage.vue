<template>
  <!-- 课程分类管理后台 -->
  <div class="category-manage-page">
    <div class="page-header">
      <h2>分类管理</h2>
      <el-button type="primary" @click="handleCreate">新增分类</el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索分类名称" clearable style="width: 240px" />
      <el-select v-model="typeFilter" placeholder="分类类型" clearable style="width: 140px">
        <el-option label="课程分类" value="course" />
        <el-option label="资讯分类" value="news" />
        <el-option label="练习分类" value="practice" />
      </el-select>
      <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 120px">
        <el-option label="已启用" value="enabled" />
        <el-option label="已禁用" value="disabled" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button v-if="selectedIds.length > 0" type="danger" @click="handleBatchDelete">批量删除({{ selectedIds.length }})</el-button>
      <el-button @click="handleExport">导出CSV</el-button>
    </div>

    <!-- 分类列表 -->
    <el-table :data="pagedList" stripe row-key="id" @selection-change="handleSelectionChange" empty-text="暂无数据">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="名称" min-width="140" />
      <el-table-column prop="icon" label="图标" width="80" align="center">
        <template #default="{ row }">
          <span class="category-icon">{{ row.icon }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="type" label="类型" width="140">
        <template #default="{ row }">
          <el-tag size="small" :type="typeTag(row.type)">
            {{ typeLabel(row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="ageRange" label="适用年龄" width="100">
        <template #default="{ row }">
          <span v-if="row.ageRange">{{ row.ageRange }}</span>
          <span v-else class="text-muted">--</span>
        </template>
      </el-table-column>
      <el-table-column prop="sortOrder" label="排序" width="70" align="center" />
      <el-table-column prop="status" label="状态" width="80" align="center">
        <template #default="{ row }">
          <el-switch
            v-model="row.status"
            active-value="enabled"
            inactive-value="disabled"
            @change="handleStatusChange(row as CategoryItem)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="170" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" text type="primary" @click="handleView(row as CategoryItemExt)">查看</el-button>
          <el-button size="small" text type="primary" @click="handleEdit(row as CategoryItem)">编辑</el-button>
          <el-button size="small" text type="danger" @click="handleDelete(row as CategoryItem)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <Pagination :total="total" v-model:page="pageNum" v-model:page-size="pageSize" />

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑分类' : '新增分类'"
      width="560px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="分类类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择分类类型" style="width: 100%">
            <el-option label="课程分类" value="course" />
            <el-option label="资讯分类" value="news" />
            <el-option label="练习分类" value="practice" />
          </el-select>
        </el-form-item>
        <el-form-item label="图标/Emoji" prop="icon">
          <el-input v-model="form.icon" placeholder="请输入图标或Emoji，如 🎮 或 icon名称" maxlength="10" show-word-limit />
        </el-form-item>
        <el-form-item label="适用年龄" prop="ageRange" v-if="form.type === 'course'">
          <el-input v-model="form.ageRange" placeholder="如：8-10岁" maxlength="20" show-word-limit />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" style="width: 100%" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio label="enabled">启用</el-radio>
            <el-radio label="disabled">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewDialogVisible" title="分类详情" width="480px">
      <div v-if="viewRow" class="view-body" style="line-height: 2;">
        <p><strong>ID：</strong>{{ viewRow.id }}</p>
        <p><strong>名称：</strong>{{ viewRow.name }}</p>
        <p><strong>图标：</strong><span style="font-size:20px">{{ viewRow.icon }}</span></p>
        <p><strong>类型：</strong>{{ typeLabel(viewRow.type) }}</p>
        <p><strong>适用年龄：</strong>{{ viewRow.ageRange || '--' }}</p>
        <p><strong>排序：</strong>{{ viewRow.sortOrder }}</p>
        <p><strong>状态：</strong>{{ viewRow.status === 'enabled' ? '已启用' : '已禁用' }}</p>
        <p><strong>创建时间：</strong>{{ viewRow.createdAt }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * CategoryManagePage - 课程分类管理后台
 */
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { type CategoryItem } from '@/api/category'
import { categoryApi } from '@/api/admin'
import { setLocalData, STORAGE_KEYS } from '@/utils/localStore'
import Pagination from '@/components/common/Pagination.vue'
import { useOperationLog } from '@/composables/useOperationLog'
const opLog = useOperationLog('category')

// 扩展类型，包含前端展示字段
interface CategoryItemExt extends CategoryItem {
  createdAt?: string
}

/**
 * 分类类型 = 后端 cms_category.type 业务域，不再使用 age/difficulty。
 * 年龄分组与难度是课程模块的前端 UI 维度（见 CourseListPage），两者已拆分。
 */
const TYPE_LABELS: Record<string, string> = {
  course: '课程分类',
  news: '资讯分类',
  practice: '练习分类',
}
function typeLabel(t?: string): string {
  return (t && TYPE_LABELS[t]) || t || '未分类'
}
function typeTag(t?: string): 'primary' | 'success' | 'warning' | 'info' {
  if (t === 'course') return 'primary'
  if (t === 'news') return 'success'
  if (t === 'practice') return 'warning'
  return 'info'
}

// ==================== 搜索与筛选 ====================
const keyword = ref('')
const typeFilter = ref('')
const statusFilter = ref('')

// ==================== 分页 ====================
const pageNum = ref(1)
const pageSize = ref(20)

// ==================== 数据 ====================
const categoryList = ref<CategoryItemExt[]>([])

// ==================== 加载数据 ====================
async function loadCategories() {
  try {
    const res: any = await categoryApi.list({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value || undefined,
      type: typeFilter.value || undefined,
      status: statusFilter.value || undefined,
    })
    if (res?.code === 200 && res.data) {
      const list = res.data.list || []
      if (Array.isArray(list) && list.length > 0) {
        categoryList.value = list.map((item: CategoryItem) => ({
          ...item,
          createdAt: (item as CategoryItemExt).createdAt || '',
        }))
      }
    }
  } catch {
    // 接口不可用，保持空列表
  }
}

// 保存到 localStorage
function saveCategoriesToLocal() {
  setLocalData(STORAGE_KEYS.CATEGORIES, categoryList.value)
}

onMounted(() => {
  loadCategories()
})

// ==================== 过滤列表 ====================
const filteredList = computed(() => {
  return categoryList.value.filter((item) => {
    const matchKeyword = !keyword.value || item.name.includes(keyword.value)
    const matchType = !typeFilter.value || item.type === typeFilter.value
    const matchStatus = !statusFilter.value || item.status === statusFilter.value
    return matchKeyword && matchType && matchStatus
  }).sort((a, b) => {
    if (a.type !== b.type) return a.type === 'course' ? -1 : b.type === 'course' ? 1 : 0
    return a.sortOrder - b.sortOrder
  })
})

const total = computed(() => filteredList.value.length)

const pagedList = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  return filteredList.value.slice(start, start + pageSize.value)
})

// ==================== 搜索 ====================
function handleSearch() {
  pageNum.value = 1
}

// ==================== 状态切换 ====================
async function handleStatusChange(row: CategoryItem) {
  try {
    await categoryApi.update(row.id, { ...row, status: row.status })
    ElMessage.success(row.status === 'enabled' ? '已启用' : '已禁用')
  } catch {
    ElMessage.error('状态更新失败')
    // 回滚状态
    row.status = row.status === 'enabled' ? 'disabled' : 'enabled'
  }
  saveCategoriesToLocal()
}

// ==================== 删除 ====================
function handleDelete(row: CategoryItem) {
  ElMessageBox.confirm(`确定删除分类 "${row.name}" 吗？`, '警告', { type: 'warning' })
    .then(async () => {
      try {
        await categoryApi.remove(row.id)
      } catch {
        // 忽略 API 错误
      }
      const idx = categoryList.value.findIndex((c) => c.id === row.id)
      if (idx !== -1) categoryList.value.splice(idx, 1)
      ElMessage.success('删除成功')
      opLog.logDelete('删除分类')
      saveCategoriesToLocal()
    })
    .catch(() => {})
}

// ==================== 批量选择与删除 ====================
const selectedIds = ref<number[]>([])

function handleSelectionChange(rows: CategoryItemExt[]) {
  selectedIds.value = rows.map(r => r.id)
}

function handleBatchDelete() {
  ElMessageBox.confirm(`确定批量删除选中的 ${selectedIds.value.length} 个分类吗？`, '警告', { type: 'warning' })
    .then(() => {
      categoryList.value = categoryList.value.filter(c => !selectedIds.value.includes(c.id))
      selectedIds.value = []
      saveCategoriesToLocal()
      ElMessage.success('批量删除成功')
    })
    .catch(() => {})
}

// ==================== 导出CSV ====================
function handleExport() {
  const headers = ['ID', '名称', '图标', '类型', '适用年龄', '排序', '状态', '创建时间']
  const rows = filteredList.value.map(c => [c.id, c.name, c.icon, typeLabel(c.type), c.ageRange || '', c.sortOrder, c.status === 'enabled' ? '已启用' : '已禁用', c.createdAt || ''])
  const csv = [headers.join(','), ...rows.map(r => r.map(c => `"${c}"`).join(','))].join('\n')
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `分类数据_${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

// ==================== 查看详情 ====================
const viewDialogVisible = ref(false)
const viewRow = ref<CategoryItemExt | null>(null)

function handleView(row: CategoryItemExt) {
  viewRow.value = row
  viewDialogVisible.value = true
}

// ==================== 弹窗表单 ====================
const dialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const editId = ref<number | null>(null)

const defaultForm = {
  name: '',
  type: 'course' as 'course' | 'news' | 'practice',
  icon: '',
  ageRange: '',
  sortOrder: 0,
  status: 'enabled' as 'enabled' | 'disabled',
}

const form = ref({ ...defaultForm })

const formRules: FormRules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择分类类型', trigger: 'change' }],
  icon: [{ required: true, message: '请输入图标或Emoji', trigger: 'blur' }],
  sortOrder: [{ required: true, message: '请输入排序', trigger: 'change' }],
}

function handleCreate() {
  isEdit.value = false
  editId.value = null
  form.value = { ...defaultForm }
  dialogVisible.value = true
}

function handleEdit(row: CategoryItem) {
  isEdit.value = true
  editId.value = row.id
  const t = row.type
  form.value = {
    name: row.name,
    type: t === 'news' || t === 'practice' ? t : 'course',
    icon: row.icon || '',
    ageRange: row.ageRange || '',
    sortOrder: row.sortOrder,
    status: row.status === 1 || row.status === 'enabled' ? 'enabled' : 'disabled',
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
        try {
          await categoryApi.update(editId.value, form.value)
        } catch {
          // API不可用，仍然本地更新
        }
        const idx = categoryList.value.findIndex((c) => c.id === editId.value)
        if (idx !== -1) {
          categoryList.value[idx] = { ...categoryList.value[idx], ...form.value }
        }
        ElMessage.success('编辑成功')
        opLog.logUpdate('编辑分类')
      } else {
        try {
          const res: any = await categoryApi.create(form.value)
          if (res?.code === 200 && res.data) {
            categoryList.value.unshift({
              ...res.data,
              createdAt,
            })
            ElMessage.success('添加成功')
            opLog.logCreate('新增分类')
            dialogVisible.value = false
            saveCategoriesToLocal()
            return
          }
        } catch {
          // API不可用，仍然本地更新
        }
        const newId = Math.max(...categoryList.value.map((c) => c.id), 0) + 1
        categoryList.value.unshift({
          id: newId,
          ...form.value,
          createdAt,
        })
        ElMessage.success('添加成功')
        opLog.logCreate('新增分类')
      }
      dialogVisible.value = false
      saveCategoriesToLocal()
    } catch {
      // 其他异常（如表单校验等）
    } finally {
      submitting.value = false
    }
  })
}
</script>

<style scoped lang="scss">
$primary: #165DFF;

.category-manage-page {
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

.category-icon {
  font-size: 20px;
}

.text-muted {
  color: #86909C;
}
</style>
