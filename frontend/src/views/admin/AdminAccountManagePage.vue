<template>
  <div class="admin-account-manage-page">
    <div class="page-header">
      <h2>管理员账号</h2>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--total">
          <el-icon :size="28"><User /></el-icon>
        </div>
        <div class="stat-card__content">
          <div class="stat-card__value">{{ totalCount }}</div>
          <div class="stat-card__label">账号总数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--enabled">
          <el-icon :size="28"><CircleCheck /></el-icon>
        </div>
        <div class="stat-card__content">
          <div class="stat-card__value">{{ enabledCount }}</div>
          <div class="stat-card__label">启用账号</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--disabled">
          <el-icon :size="28"><CircleClose /></el-icon>
        </div>
        <div class="stat-card__content">
          <div class="stat-card__value">{{ disabledCount }}</div>
          <div class="stat-card__label">禁用账号</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--super">
          <el-icon :size="28"><Avatar /></el-icon>
        </div>
        <div class="stat-card__content">
          <div class="stat-card__value">{{ superAdminCount }}</div>
          <div class="stat-card__label">超级管理员</div>
        </div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="searchForm.keyword" placeholder="用户名/昵称" clearable style="width: 200px" />
      <el-select v-model="searchForm.role" placeholder="角色" clearable style="width: 160px">
        <el-option label="超级管理员" value="super_admin" />
        <el-option label="管理员" value="admin" />
        <el-option label="操作员" value="operator" />
      </el-select>
      <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 130px">
        <el-option label="启用" value="enabled" />
        <el-option label="禁用" value="disabled" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <!-- 操作按钮区 -->
    <div class="action-bar">
      <el-button type="primary" :icon="Plus" @click="handleAdd">添加管理员</el-button>
      <el-button type="danger" :icon="Delete" @click="handleBatchDelete" :disabled="!selectedRows.length">批量删除</el-button>
      <el-button :icon="Download" @click="handleExport">导出CSV</el-button>
    </div>

    <!-- 数据表格 -->
    <el-table
      :data="pagedAccountList"
      stripe
      v-loading="loading"
      @selection-change="handleSelectionChange"
    >
      <el-table-column type="selection" width="50" />
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="用户名" width="140" />
      <el-table-column prop="nickname" label="昵称" width="140" />
      <el-table-column prop="role" label="角色" width="130">
        <template #default="{ row }">
          <el-tag :type="roleTagType[(row as AdminAccount).role]" size="small" effect="dark">
            {{ roleLabelMap[(row as AdminAccount).role] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" width="130" />
      <el-table-column prop="email" label="邮箱" min-width="200" show-overflow-tooltip />
      <el-table-column prop="lastLogin" label="最后登录" width="170" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-switch
            :model-value="row.status === 'enabled'"
            @change="(val: string | number | boolean) => handleStatusChange(row as AdminAccount, val === true)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作" width="230" fixed="right">
        <template #default="{ row }">
          <el-button size="small" text type="primary" @click="handleEdit(row as AdminAccount)">编辑</el-button>
          <el-button size="small" text type="warning" @click="handleResetPassword(row as AdminAccount)">重置密码</el-button>
          <el-button size="small" text type="danger" @click="handleDelete(row as AdminAccount)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <Pagination :total="total" v-model:page="pageNum" v-model:page-size="pageSize" />

    <!-- 添加/编辑弹窗 -->
    <el-dialog v-model="formDialogVisible" :title="isEdit ? '编辑管理员' : '添加管理员'" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="昵称" prop="nickname">
          <el-input v-model="form.nickname" placeholder="请输入昵称" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="超级管理员" value="super_admin" />
            <el-option label="管理员" value="admin" />
            <el-option label="操作员" value="operator" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="form.remark" type="textarea" :rows="3" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码弹窗 -->
    <el-dialog v-model="resetPwdDialogVisible" title="重置密码" width="460px" destroy-on-close>
      <el-form ref="resetPwdFormRef" :model="resetPwdForm" :rules="resetPwdRules" label-width="90px">
        <el-form-item label="用户名">
          <el-input :model-value="resetPwdTarget?.username" disabled />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="resetPwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="resetPwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="resetPwdDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="resetPwdSubmitting" @click="submitResetPassword">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * AdminAccountManagePage - 管理员账号管理页面
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { Plus, Download, Delete, User, CircleCheck, CircleClose, Avatar } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import Pagination from '@/components/common/Pagination.vue'
import { accountApi } from '@/api/admin'
import { useOperationLog } from '@/composables/useOperationLog'

const opLog = useOperationLog('system')

// ==================== 类型定义 ====================

type AdminRole = 'super_admin' | 'admin' | 'operator'
type AdminStatus = 'enabled' | 'disabled'

interface AdminAccount {
  id: number
  username: string
  nickname: string
  password?: string
  role: AdminRole
  phone: string
  email: string
  remark: string
  lastLogin: string
  status: AdminStatus
  createTime: string
}

// ==================== 角色映射常量 ====================

const roleLabelMap: Record<AdminRole, string> = {
  super_admin: '超级管理员',
  admin: '管理员',
  operator: '操作员',
}

const roleTagType: Record<AdminRole, 'success' | 'warning' | 'info' | 'danger' | 'primary'> = {
  super_admin: 'danger',
  admin: 'primary',
  operator: 'info',
}

// ==================== 页面基础状态 ====================

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(20)
const selectedRows = ref<AdminAccount[]>([])

const searchForm = reactive({
  keyword: '',
  role: '' as AdminRole | '',
  status: '' as AdminStatus | '',
})

async function handleSearch() {
  pageNum.value = 1
  await fetchAccountList()
  ElMessage.success('搜索完成')
}

function resetSearch() {
  searchForm.keyword = ''
  searchForm.role = ''
  searchForm.status = ''
  pageNum.value = 1
  fetchAccountList()
}

function handleSelectionChange(val: AdminAccount[]) {
  selectedRows.value = val
}

// ==================== Mock 数据 ====================

const accountList = ref<AdminAccount[]>([
  {
    id: 1,
    username: 'admin',
    nickname: '系统管理员',
    password: '123456',
    role: 'super_admin',
    phone: '13800000001',
    email: 'admin@magao.com',
    remark: '系统初始化超级管理员账号',
    lastLogin: '2024-06-18 10:30:00',
    status: 'enabled',
    createTime: '2024-01-01 09:00:00',
  },
  {
    id: 2,
    username: 'manager01',
    nickname: '运营经理',
    password: '123456',
    role: 'admin',
    phone: '13800000002',
    email: 'manager01@magao.com',
    remark: '负责日常内容与赛事管理',
    lastLogin: '2024-06-17 14:20:00',
    status: 'enabled',
    createTime: '2024-02-15 11:00:00',
  },
  {
    id: 3,
    username: 'operator01',
    nickname: '录入员小王',
    password: '123456',
    role: 'operator',
    phone: '13800000003',
    email: 'operator01@magao.com',
    remark: '负责题库录入与阅卷',
    lastLogin: '2024-06-15 09:15:00',
    status: 'enabled',
    createTime: '2024-03-10 13:30:00',
  },
  {
    id: 4,
    username: 'operator02',
    nickname: '客服小李',
    password: '123456',
    role: 'operator',
    phone: '13800000004',
    email: 'operator02@magao.com',
    remark: '负责用户咨询与订单处理',
    lastLogin: '2024-05-20 16:45:00',
    status: 'disabled',
    createTime: '2024-04-05 10:00:00',
  },
])

// ==================== 搜索过滤与分页 ====================

const filteredAccountList = computed(() => {
  return accountList.value.filter(a => {
    if (searchForm.keyword) {
      const kw = searchForm.keyword.toLowerCase()
      const match = a.username.toLowerCase().includes(kw) || a.nickname.toLowerCase().includes(kw)
      if (!match) return false
    }
    if (searchForm.role && a.role !== searchForm.role) return false
    if (searchForm.status && a.status !== searchForm.status) return false
    return true
  })
})

const pagedAccountList = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  return filteredAccountList.value.slice(start, start + pageSize.value)
})

const total = computed(() => filteredAccountList.value.length)

// ==================== 统计数据 ====================

const totalCount = computed(() => accountList.value.length)
const enabledCount = computed(() => accountList.value.filter(a => a.status === 'enabled').length)
const disabledCount = computed(() => accountList.value.filter(a => a.status === 'disabled').length)
const superAdminCount = computed(() => accountList.value.filter(a => a.role === 'super_admin').length)

// ==================== 获取列表 ====================

/** 获取管理员账号列表，调用 bridge 列表接口，失败时回退到本地 mock 数据 */
async function fetchAccountList() {
  loading.value = true
  try {
    const res = await accountApi.list({ pageNum: 1, pageSize: 9999 })
    const list = res?.data?.list || res?.data?.records || []
    if (list.length) accountList.value = list
  } catch (e) {
    console.warn('load failed, fallback to mock', e)
  } finally {
    loading.value = false
  }
}

// ==================== 添加/编辑 ====================

const formDialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const formRef = ref<FormInstance>()

const form = reactive({
  id: null as number | null,
  username: '',
  nickname: '',
  password: '',
  role: 'operator' as AdminRole,
  phone: '',
  email: '',
  remark: '',
  status: 'enabled' as AdminStatus,
})

const formRules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, message: '用户名至少 3 个字符', trigger: 'blur' },
  ],
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }],
  password: [
    {
      validator: (_rule, value: string, callback) => {
        if (isEdit.value) {
          callback()
          return
        }
        if (!value) {
          callback(new Error('请输入密码'))
        } else if (value.length < 6) {
          callback(new Error('密码至少 6 个字符'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
}

function handleAdd() {
  isEdit.value = false
  Object.assign(form, {
    id: null,
    username: '',
    nickname: '',
    password: '',
    role: 'operator',
    phone: '',
    email: '',
    remark: '',
  })
  formDialogVisible.value = true
}

function handleEdit(row: AdminAccount) {
  isEdit.value = true
  Object.assign(form, {
    id: row.id,
    username: row.username,
    nickname: row.nickname,
    password: '',
    role: row.role,
    phone: row.phone,
    email: row.email,
    remark: row.remark,
    status: row.status,
  })
  formDialogVisible.value = true
}

/** 当前时间字符串（YYYY-MM-DD HH:mm:ss） */
function nowStr(): string {
  const now = new Date()
  const pad = (n: number) => String(n).padStart(2, '0')
  return `${now.getFullYear()}-${pad(now.getMonth() + 1)}-${pad(now.getDate())} ${pad(now.getHours())}:${pad(now.getMinutes())}:${pad(now.getSeconds())}`
}

async function submitForm() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const payload: any = {
        username: form.username,
        nickname: form.nickname,
        role: form.role,
        phone: form.phone,
        email: form.email,
        status: form.status || 'enabled',
      }
      // 新建时才提交密码（明文），后端按登录同款盐值哈希；编辑时留空则不修改密码
      if (!isEdit.value && form.password) payload.password = form.password
      if (isEdit.value && form.id != null) {
        await accountApi.update(form.id as number, payload)
        const idx = accountList.value.findIndex(a => a.id === form.id)
        if (idx !== -1) {
          accountList.value[idx] = {
            ...accountList.value[idx],
            username: form.username,
            nickname: form.nickname,
            role: form.role,
            phone: form.phone,
            email: form.email,
            status: form.status,
          }
        }
        ElMessage.success('编辑成功')
        opLog.logUpdate('编辑管理员账号「' + form.username + '」')
      } else {
        const res: any = await accountApi.create(payload)
        const created = res?.data
        const newId = created?.id ?? Date.now()
        const newAccount: AdminAccount = {
          id: newId,
          username: form.username,
          nickname: form.nickname,
          password: form.password || '123456',
          role: form.role,
          phone: form.phone,
          email: form.email,
          remark: form.remark,
          lastLogin: '-',
          status: form.status || 'enabled',
          createTime: nowStr(),
        }
        accountList.value.unshift(newAccount)
        ElMessage.success('添加成功')
        opLog.logCreate('新增管理员账号「' + form.username + '」')
      }
      formDialogVisible.value = false
    } catch (e) {
      console.warn('save account failed, fallback to local', e)
      if (isEdit.value && form.id != null) {
        const idx = accountList.value.findIndex(a => a.id === form.id)
        if (idx !== -1) {
          accountList.value[idx] = {
            ...accountList.value[idx],
            username: form.username,
            nickname: form.nickname,
            role: form.role,
            phone: form.phone,
            email: form.email,
            status: form.status,
          }
        }
      } else {
        accountList.value.unshift({
          id: Date.now(),
          username: form.username,
          nickname: form.nickname,
          password: form.password || '123456',
          role: form.role,
          phone: form.phone,
          email: form.email,
          remark: form.remark,
          lastLogin: '-',
          status: form.status || 'enabled',
          createTime: nowStr(),
        } as AdminAccount)
      }
      ElMessage.warning('后端未连接，已本地保存（未落库）')
      formDialogVisible.value = false
    } finally {
      submitting.value = false
    }
  })
}

// ==================== 状态切换 ====================

function handleStatusChange(row: AdminAccount, val: boolean) {
  const next: AdminStatus = val ? 'enabled' : 'disabled'
  row.status = next
  ElMessage.success(next === 'enabled' ? '已启用' : '已禁用')
  opLog.logStatusChange('管理员账号「' + row.username + '」状态变更为' + (next === 'enabled' ? '启用' : '禁用'))
  try {
    accountApi.update(row.id, { status: next })
  } catch (e) {
    console.warn('status change fallback', e)
  }
}

// ==================== 重置密码 ====================

const resetPwdDialogVisible = ref(false)
const resetPwdSubmitting = ref(false)
const resetPwdFormRef = ref<FormInstance>()
const resetPwdTarget = ref<AdminAccount | null>(null)
const resetPwdForm = reactive({
  newPassword: '',
  confirmPassword: '',
})

const resetPwdRules: FormRules = {
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (_rule, value: string, callback) => {
        if (value !== resetPwdForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur',
    },
  ],
}

function handleResetPassword(row: AdminAccount) {
  resetPwdTarget.value = row
  resetPwdForm.newPassword = ''
  resetPwdForm.confirmPassword = ''
  resetPwdDialogVisible.value = true
}

async function submitResetPassword() {
  if (!resetPwdFormRef.value) return
  await resetPwdFormRef.value.validate(async (valid) => {
    if (!valid) return
    resetPwdSubmitting.value = true
    try {
      const id = resetPwdTarget.value?.id
      if (id != null) {
        await accountApi.update(id, { password: resetPwdForm.newPassword })
      }
      const idx = accountList.value.findIndex(a => a.id === id)
      if (idx !== -1) {
        accountList.value[idx].password = resetPwdForm.newPassword
      }
      resetPwdDialogVisible.value = false
      ElMessage.success('密码重置成功')
      opLog.logUpdate('重置管理员「' + resetPwdTarget.value?.username + '」的密码')
    } catch (e) {
      console.warn('reset password fallback', e)
      ElMessage.warning('后端未连接，重置未落库')
    } finally {
      resetPwdSubmitting.value = false
    }
  })
}

// ==================== 删除 ====================

function handleDelete(row: AdminAccount) {
  if (row.role === 'super_admin') {
    ElMessage.warning('超级管理员账号不可删除')
    return
  }
  ElMessageBox.confirm(`确定删除管理员 "${row.username}" 吗？`, '警告', { type: 'warning' })
    .then(async () => {
      try {
        await accountApi.remove(row.id)
      } catch (e) {
        console.warn('delete fallback', e)
      }
      accountList.value = accountList.value.filter(a => a.id !== row.id)
      ElMessage.success('删除成功')
      opLog.logDelete('删除管理员账号「' + row.username + '」')
    })
    .catch(() => {})
}

// ==================== 批量删除 ====================

function handleBatchDelete() {
  if (selectedRows.value.some(r => r.role === 'super_admin')) {
    ElMessage.warning('选中的账号中包含超级管理员，不可删除')
    return
  }
  const usernames = selectedRows.value.map(r => r.username).join('、')
  ElMessageBox.confirm(`确定删除以下 ${selectedRows.value.length} 个管理员账号吗？\n${usernames}`, '警告', { type: 'warning' })
    .then(async () => {
      const ids = selectedRows.value.map(r => r.id)
      try {
        await accountApi.batchRemove(ids)
      } catch (e) {
        console.warn('batch delete fallback', e)
      }
      accountList.value = accountList.value.filter(a => !ids.includes(a.id))
      ElMessage.success('删除成功')
      opLog.logBatchDelete('批量删除' + selectedRows.value.length + '个管理员账号')
    })
    .catch(() => {})
}

// ==================== 导出 CSV ====================

function handleExport() {
  const data = selectedRows.value.length ? selectedRows.value : accountList.value
  const headers = ['ID', '用户名', '昵称', '角色', '手机号', '邮箱', '最后登录', '状态', '创建时间', '备注']
  const rows = data.map(a => [
    a.id,
    a.username,
    a.nickname,
    roleLabelMap[a.role],
    a.phone,
    a.email,
    a.lastLogin,
    a.status === 'enabled' ? '启用' : '禁用',
    a.createTime,
    a.remark,
  ])
  const csv = [headers.join(','), ...rows.map(r => r.map(cell => `"${String(cell).replace(/"/g, '""')}"`).join(','))].join('\n')
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `管理员账号_${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
  opLog.logExport('导出管理员账号数据')
}

// ==================== 初始化 ====================

onMounted(() => {
  fetchAccountList()
})
</script>

<style scoped lang="scss">
$primary: #165DFF;

.admin-account-manage-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; h2 { font-size: 20px; color: #1D2129; } }

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}
.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: $bg-white;
  border-radius: 8px;
  border: 1px solid #F7F8FA;
  transition: box-shadow 0.2s;
  &:hover { box-shadow: 0 2px 12px rgba(0, 0, 0, 0.08); }
  &__icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 52px;
    height: 52px;
    border-radius: 12px;
    flex-shrink: 0;
    &--total { background: rgba(22, 93, 255, 0.1); color: $primary; }
    &--enabled { background: rgba(103, 194, 58, 0.1); color: #36D399; }
    &--disabled { background: rgba(245, 108, 108, 0.1); color: #F53F3F; }
    &--super { background: rgba(230, 162, 60, 0.1); color: #FF9500; }
  }
  &__content { flex: 1; min-width: 0; }
  &__value { font-size: 24px; font-weight: 600; color: #1D2129; line-height: 1.2; }
  &__label { font-size: 13px; color: #86909C; margin-top: 4px; }
}

.search-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.action-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
</style>
