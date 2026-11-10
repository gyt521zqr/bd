<template>
  <!-- 合作伙伴管理后台 -->
  <div class="partner-manage-page">
    <div class="page-header">
      <h2>合作伙伴</h2>
      <el-button type="primary" @click="handleCreate">新增合作伙伴</el-button>
    </div>

    <!-- 字段能力提示：后端 Partner 实体无 type / link 字段 -->
    <el-alert
      type="info"
      :closable="false"
      show-icon
      title="字段说明"
      description="后端 Partner 实体的官网地址字段为 website（页面显示为「链接」）、状态为 0/1 整数；「类型」（学校/科技馆/科研机构）为页面展示字段，后端暂无对应字段，保存后不会持久化。"
      style="margin-bottom: 16px"
    />

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索合作伙伴名称" clearable style="width: 240px" />
      <el-select v-model="typeFilter" placeholder="类型筛选" clearable style="width: 140px">
        <el-option label="学校" value="school" />
        <el-option label="科技馆" value="museum" />
        <el-option label="科研机构" value="research" />
      </el-select>
      <el-select v-model="statusFilter" placeholder="状态筛选" clearable style="width: 120px">
        <el-option label="已启用" value="enabled" />
        <el-option label="已禁用" value="disabled" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button v-if="selectedIds.length > 0" type="danger" @click="handleBatchDelete">批量删除({{ selectedIds.length }})</el-button>
      <el-button @click="handleExport">导出CSV</el-button>
    </div>

    <!-- 合作伙伴列表 -->
    <el-table v-loading="loading" :data="pagedList" stripe row-key="id" empty-text="暂无数据" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="名称" min-width="160" />
      <el-table-column prop="type" label="类型" width="100">
        <template #default="{ row }">
          <el-tag size="small" :type="(typeTagMap[row.type]?.type as any)">
            {{ typeTagMap[row.type]?.label }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="logo" label="Logo" width="80" align="center">
        <template #default="{ row }">
          <div v-if="row.logo" class="partner-logo-preview">
            <img :src="row.logo" alt="logo" />
          </div>
          <div v-else class="logo-placeholder-small" :style="{ background: getLogoColor(row.name) }">
            <span>{{ row.name.charAt(0) }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="link" label="链接" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">
          <a v-if="row.link" :href="row.link" target="_blank" class="link-text">{{ row.link }}</a>
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
            @change="handleStatusChange(row as PartnerItemExt)"
          />
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="170" />
      <el-table-column label="操作" width="180" fixed="right">
        <template #default="{ row }">
          <el-button size="small" text type="primary" @click="handleView(row as PartnerItemExt)">查看</el-button>
          <el-button size="small" text type="primary" @click="handleEdit(row as PartnerItemExt)">编辑</el-button>
          <el-button size="small" text type="danger" @click="handleDelete(row as PartnerItemExt)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <Pagination :total="total" v-model:page="pageNum" v-model:page-size="pageSize" />

    <!-- 新增/编辑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑合作伙伴' : '新增合作伙伴'"
      width="560px"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入合作伙伴名称" maxlength="30" show-word-limit />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择类型" style="width: 100%">
            <el-option label="学校" value="school" />
            <el-option label="科技馆" value="museum" />
            <el-option label="科研机构" value="research" />
          </el-select>
        </el-form-item>
        <el-form-item label="Logo" prop="logo">
          <ImageUploader
            v-model="form.logo"
            type="partner-logo"
            :preview-width="160"
            :preview-height="80"
            preset="list"
          />
        </el-form-item>
        <el-form-item label="链接地址" prop="link">
          <el-input v-model="form.link" placeholder="请输入官网链接，如 https://example.com" />
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
    <el-dialog v-model="viewDialogVisible" title="合作伙伴详情" width="480px">
      <div v-if="viewRow" class="view-body" style="line-height: 2;">
        <p><strong>ID：</strong>{{ viewRow.id }}</p>
        <p><strong>名称：</strong>{{ viewRow.name }}</p>
        <p><strong>类型：</strong>{{ typeTagMap[viewRow.type]?.label || '--' }}</p>
        <p>
          <strong>Logo：</strong>
          <span v-if="viewRow.logo" class="partner-logo-preview">
            <img :src="viewRow.logo" alt="logo" />
          </span>
          <span v-else class="logo-placeholder-small" :style="{ background: getLogoColor(viewRow.name) }">
            {{ viewRow.name.charAt(0) }}
          </span>
        </p>
        <p><strong>链接：</strong>{{ viewRow.link || '--' }}</p>
        <p><strong>排序：</strong>{{ viewRow.sortOrder }}</p>
        <p><strong>状态：</strong>{{ viewRow.status === 'enabled' ? '已启用' : '已禁用' }}</p>
        <p><strong>创建时间：</strong>{{ viewRow.createdAt }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * PartnerManagePage - 合作伙伴管理后台
 * 数据来源为后端 Partner 实体（cms_partner 表）：
 *   - 列表：GET /api/admin/partner/list（含禁用记录）
 *   - 新增/编辑/删除：/api/partners（与前台公开读接口同一张表）
 *   - 批量删除：POST /api/admin/partner/batch-delete
 */
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import request from '@/api'
import ImageUploader from '@/components/common/ImageUploader.vue'
import Pagination from '@/components/common/Pagination.vue'
import { useOperationLog } from '@/composables/useOperationLog'

const opLog = useOperationLog('partner')

/** 后端 Partner 实体真实字段（status 为 Integer 0/1） */
interface PartnerEntity {
  id: number | string
  name: string
  logo: string
  website: string
  description: string
  sortOrder: number
  status: number | string
  createTime?: string
}

/** 页面展示模型 */
interface PartnerItemExt {
  id: number
  name: string
  type: 'school' | 'museum' | 'research'
  logo: string
  link: string
  sortOrder: number
  status: 'enabled' | 'disabled'
  createdAt?: string
}

/** 表单模型 */
interface PartnerForm {
  name: string
  type: 'school' | 'museum' | 'research'
  logo: string
  link: string
  sortOrder: number
  status: 'enabled' | 'disabled'
}

/** 写入后端的实体字段（不含前端无对应字段的 type） */
interface PartnerPayload {
  name: string
  logo: string
  website: string
  sortOrder: number
  status: number
}

// ==================== 字段适配（参考 admin.ts category.normalize 的双向归一手法）====================
/** 后端实体 -> 页面字段：website->link、createTime->createdAt、status 0/1->enabled/disabled */
function normalizePartner(row: PartnerEntity): PartnerItemExt {
  return {
    id: Number(row.id) || 0,
    name: row.name ?? '',
    // 后端 Partner 无 type 字段，统一按「学校」展示（见页面顶部提示）
    type: 'school',
    logo: row.logo ?? '',
    link: row.website ?? '',
    sortOrder: Number(row.sortOrder) || 0,
    status: row.status === 1 || row.status === 'enabled' ? 'enabled' : 'disabled',
    createdAt: row.createTime ?? '',
  }
}

/** 页面表单 -> 后端实体字段 */
function serializePartner(form: PartnerForm): PartnerPayload {
  return {
    name: form.name,
    logo: form.logo,
    website: form.link,
    sortOrder: form.sortOrder,
    status: form.status === 'enabled' ? 1 : 0,
  }
}

// ==================== 类型映射 ====================
const typeTagMap: Record<string, { label: string; type: string }> = {
  school: { label: '学校', type: 'primary' },
  museum: { label: '科技馆', type: 'warning' },
  research: { label: '科研机构', type: 'success' },
}

// ==================== Logo 颜色 ====================
const logoColors = ['#7B61FF', '#165DFF', '#36D399', '#F53F3F', '#36D399', '#E06800', '#165DFF', '#F53F3F', '#86909C', '#E06800', '#165DFF', '#F53F3F']
function getLogoColor(name: string) {
  let hash = 0
  for (let i = 0; i < name.length; i++) {
    hash = name.charCodeAt(i) + ((hash << 5) - hash)
  }
  return logoColors[Math.abs(hash) % logoColors.length]
}

// ==================== 搜索与筛选 ====================
const keyword = ref('')
const typeFilter = ref('')
const statusFilter = ref('')

// ==================== 分页 ====================
const pageNum = ref(1)
const pageSize = ref(20)

// ==================== 数据 ====================
const loading = ref(false)
// 初始为空，不再预制虚构机构条目；后端为空/加载失败时显示空态
const partnerList = ref<PartnerItemExt[]>([])

// ==================== 加载数据 ====================
async function loadPartners() {
  loading.value = true
  try {
    // 后台列表需含禁用记录，故用 admin 分页端点；一次取全量后由前端筛选/分页
    const res = await request.get<unknown, { code: number; data: PartnerEntity[] | { list?: PartnerEntity[] } }>(
      '/admin/partner/list',
      { params: { pageNum: 1, pageSize: 1000 } }
    )
    const data = res?.data
    const rows: PartnerEntity[] = Array.isArray(data)
      ? data
      : ((data as { list?: PartnerEntity[] } | undefined)?.list ?? [])
    partnerList.value = rows.map((row) => normalizePartner(row))
  } catch {
    partnerList.value = []
    ElMessage.warning('合作伙伴列表加载失败，请检查后端服务；当前显示为空')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadPartners()
})

// ==================== 过滤列表 ====================
const filteredList = computed(() => {
  return partnerList.value.filter((item) => {
    const matchKeyword = !keyword.value || item.name.includes(keyword.value)
    const matchType = !typeFilter.value || item.type === typeFilter.value
    const matchStatus = !statusFilter.value || item.status === statusFilter.value
    return matchKeyword && matchType && matchStatus
  }).sort((a, b) => a.sortOrder - b.sortOrder)
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

// ==================== 状态切换（启停）====================
async function handleStatusChange(row: PartnerItemExt) {
  try {
    // PUT /partners/{id} 为局部更新，只传 status，避免覆盖其它实体字段
    await request.put(`/partners/${row.id}`, { status: row.status === 'enabled' ? 1 : 0 })
    ElMessage.success(row.status === 'enabled' ? '已启用' : '已禁用')
  } catch {
    ElMessage.error('状态更新失败，已恢复原状态')
    row.status = row.status === 'enabled' ? 'disabled' : 'enabled'
  }
}

// ==================== 删除 ====================
function handleDelete(row: PartnerItemExt) {
  ElMessageBox.confirm(`确定删除合作伙伴 "${row.name}" 吗？`, '警告', { type: 'warning' })
    .then(async () => {
      try {
        await request.delete(`/partners/${row.id}`)
        ElMessage.success('删除成功')
        opLog.logDelete('删除合作伙伴')
        await loadPartners()
      } catch {
        ElMessage.error('删除失败，请检查后端服务或重试')
      }
    })
    .catch(() => {})
}

// ==================== 批量选择与删除 ====================
const selectedIds = ref<number[]>([])

function handleSelectionChange(rows: PartnerItemExt[]) {
  selectedIds.value = rows.map(r => r.id)
}

function handleBatchDelete() {
  ElMessageBox.confirm(`确定批量删除选中的 ${selectedIds.value.length} 个合作伙伴吗？`, '警告', { type: 'warning' })
    .then(async () => {
      try {
        await request.post('/admin/partner/batch-delete', { ids: selectedIds.value })
        selectedIds.value = []
        ElMessage.success('批量删除成功')
        await loadPartners()
      } catch {
        ElMessage.error('批量删除失败，请检查后端服务或重试')
      }
    })
    .catch(() => {})
}

// ==================== 导出CSV ====================
function handleExport() {
  const headers = ['ID', '名称', '类型', 'Logo', '链接', '排序', '状态', '创建时间']
  const rows = filteredList.value.map(p => [p.id, p.name, typeTagMap[p.type]?.label || p.type, p.logo || '', p.link || '', p.sortOrder, p.status === 'enabled' ? '已启用' : '已禁用', p.createdAt || ''])
  const csv = [headers.join(','), ...rows.map(r => r.map(c => `"${c}"`).join(','))].join('\n')
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `合作伙伴数据_${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

// ==================== 查看详情 ====================
const viewDialogVisible = ref(false)
const viewRow = ref<PartnerItemExt | null>(null)

function handleView(row: PartnerItemExt) {
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
  type: 'school' as 'school' | 'museum' | 'research',
  logo: '',
  link: '',
  sortOrder: 0,
  status: 'enabled' as 'enabled' | 'disabled',
}

const form = ref({ ...defaultForm })

const formRules: FormRules = {
  name: [{ required: true, message: '请输入合作伙伴名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择类型', trigger: 'change' }],
  sortOrder: [{ required: true, message: '请输入排序', trigger: 'change' }],
}

function handleCreate() {
  isEdit.value = false
  editId.value = null
  form.value = { ...defaultForm }
  dialogVisible.value = true
}

function handleEdit(row: PartnerItemExt) {
  isEdit.value = true
  editId.value = row.id
  form.value = {
    name: row.name,
    type: row.type,
    logo: row.logo || '',
    link: row.link || '',
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
      const payload = serializePartner(form.value)
      if (isEdit.value && editId.value !== null) {
        await request.put(`/partners/${editId.value}`, payload)
        ElMessage.success('编辑成功')
        opLog.logUpdate('编辑合作伙伴')
      } else {
        await request.post('/partners', payload)
        ElMessage.success('添加成功')
        opLog.logCreate('新增合作伙伴')
      }
      dialogVisible.value = false
      // 以后端为准重新拉取，避免本地与服务端不一致
      await loadPartners()
    } catch {
      ElMessage.error('保存失败，请检查后端服务或重试')
    } finally {
      submitting.value = false
    }
  })
}
</script>

<style scoped lang="scss">
$primary: #165DFF;

.partner-manage-page {
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

.partner-logo-preview {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F7F8FA;

  img {
    width: 100%;
    height: 100%;
    object-fit: contain;
  }
}

.logo-placeholder-small {
  width: 40px;
  height: 40px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 16px;
  font-weight: 600;
}

.link-text {
  color: $primary;
  text-decoration: none;

  &:hover {
    text-decoration: underline;
  }
}

.text-muted {
  color: #86909C;
}

.upload-tip {
  font-size: 12px;
  color: #86909C;
  margin-top: 4px;
}
</style>
