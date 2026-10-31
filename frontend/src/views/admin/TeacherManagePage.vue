<template>
  <div class="teacher-manage-page">
    <div class="page-header">
      <h2>教师管理</h2>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--total">
          <el-icon :size="28"><User /></el-icon>
        </div>
        <div class="stat-card__content">
          <div class="stat-card__value">{{ totalTeacherCount }}</div>
          <div class="stat-card__label">总教师数</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--active">
          <el-icon :size="28"><CircleCheck /></el-icon>
        </div>
        <div class="stat-card__content">
          <div class="stat-card__value">{{ enabledCount }}</div>
          <div class="stat-card__label">启用中</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--disabled">
          <el-icon :size="28"><CircleClose /></el-icon>
        </div>
        <div class="stat-card__content">
          <div class="stat-card__value">{{ disabledCount }}</div>
          <div class="stat-card__label">已禁用</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-card__icon stat-card__icon--students">
          <el-icon :size="28"><UserFilled /></el-icon>
        </div>
        <div class="stat-card__content">
          <div class="stat-card__value">{{ totalStudentCount }}</div>
          <div class="stat-card__label">总学员数</div>
        </div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="searchForm.name" placeholder="姓名" clearable style="width: 160px" />
      <el-select v-model="searchForm.subject" placeholder="科目" clearable style="width: 140px">
        <el-option label="数学" value="数学" />
        <el-option label="语文" value="语文" />
        <el-option label="英语" value="英语" />
        <el-option label="物理" value="物理" />
        <el-option label="化学" value="化学" />
        <el-option label="生物" value="生物" />
        <el-option label="信息学" value="信息学" />
      </el-select>
      <el-select v-model="searchForm.status" placeholder="状态" clearable style="width: 120px">
        <el-option label="启用" value="enabled" />
        <el-option label="禁用" value="disabled" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <!-- 操作按钮区 -->
    <div class="action-bar">
      <el-button type="primary" :icon="Plus" @click="handleAdd">添加教师</el-button>
      <el-button :icon="Download" @click="handleExport">批量导出</el-button>
      <el-button type="danger" :icon="Delete" @click="handleBatchDelete" :disabled="!selectedRows.length">批量删除</el-button>
    </div>

    <!-- 数据表格 -->
    <el-table :data="pagedTeacherList" stripe v-loading="loading" @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="姓名" width="100" />
      <el-table-column prop="subject" label="科目" width="100" />
      <el-table-column prop="title" label="职称" width="120" />
      <el-table-column prop="phone" label="手机号" width="120" />
      <el-table-column prop="email" label="邮箱" min-width="180" />
      <el-table-column prop="courseCount" label="授课课程数" width="100" />
      <el-table-column prop="studentCount" label="学员数" width="80" />
      <el-table-column label="权限" min-width="260">
        <template #default="{ row }">
          <template v-if="row.permissions && row.permissions.length > 0">
            <el-tag
              v-for="perm in row.permissions"
              :key="perm"
              size="small"
              :type="getPermissionTagType(perm)"
              class="permission-tag"
            >
              {{ perm }}
            </el-tag>
          </template>
          <span v-else class="no-permission">暂无权限</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 'enabled' ? 'success' : 'danger'" size="small">
            {{ row.status === 'enabled' ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="340" fixed="right">
        <template #default="{ row }">
          <el-button size="small" text type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" text type="primary" @click="handleViewCourses(row)">查看授课记录</el-button>
          <el-button size="small" text type="warning" @click="handleResetPassword(row)">重置密码</el-button>
          <el-button size="small" text type="primary" @click="handleAssignPermission(row)">分配权限</el-button>
          <el-button size="small" text :type="row.status === 'enabled' ? 'danger' : 'success'" @click="handleToggleStatus(row)">
            {{ row.status === 'enabled' ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <Pagination :total="total" v-model:page="pageNum" v-model:page-size="pageSize" />

    <!-- 添加/编辑弹窗 -->
    <el-dialog v-model="formDialogVisible" :title="isEdit ? '编辑教师' : '添加教师'" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="90px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="科目" prop="subject">
          <el-select v-model="form.subject" placeholder="选择科目" style="width: 100%">
            <el-option label="数学" value="数学" />
            <el-option label="语文" value="语文" />
            <el-option label="英语" value="英语" />
            <el-option label="物理" value="物理" />
            <el-option label="化学" value="化学" />
            <el-option label="生物" value="生物" />
            <el-option label="信息学" value="信息学" />
          </el-select>
        </el-form-item>
        <el-form-item label="职称" prop="title">
          <el-select v-model="form.title" placeholder="选择职称" style="width: 100%">
            <el-option label="助教" value="助教" />
            <el-option label="讲师" value="讲师" />
            <el-option label="副教授" value="副教授" />
            <el-option label="教授" value="教授" />
          </el-select>
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="简介" prop="intro">
          <el-input v-model="form.intro" type="textarea" :rows="4" placeholder="请输入教师简介" />
        </el-form-item>
        <el-form-item label="头像" prop="avatar">
          <div class="avatar-uploader">
            <div v-if="form.avatar" class="avatar-preview">
              <img :src="form.avatar" alt="avatar" />
              <div class="avatar-actions">
                <el-icon @click="form.avatar = ''"><Delete /></el-icon>
              </div>
            </div>
            <div v-else class="avatar-placeholder" @click="handleUploadAvatar">
              <el-icon><Plus /></el-icon>
              <span>点击上传</span>
            </div>
          </div>
          <div class="upload-tip">头像上传占位，后续对接云存储</div>
        </el-form-item>
        <el-form-item label="功能权限">
          <div class="permission-section">
            <div class="permission-template-bar">
              <span class="template-label">权限模板：</span>
              <el-button size="small" @click="applyTemplate('principal')">校长</el-button>
              <el-button size="small" @click="applyTemplate('dean')">教务主任</el-button>
              <el-button size="small" @click="applyTemplate('teacher')">普通教师</el-button>
              <el-button size="small" @click="applyTemplate('assistant')">助教</el-button>
            </div>
            <el-checkbox-group v-model="form.permissions">
              <el-checkbox
                v-for="item in permissionOptions"
                :key="item.value"
                :label="item.value"
              >
                {{ item.label }}
                <span class="permission-desc">（{{ item.desc }}）</span>
              </el-checkbox>
            </el-checkbox-group>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限弹窗 -->
    <el-dialog v-model="permDialogVisible" title="分配权限" width="560px" destroy-on-close>
      <div class="perm-dialog-header">
        <span>教师：<strong>{{ permTargetTeacher?.name }}</strong></span>
      </div>
      <div class="permission-section">
        <div class="permission-template-bar">
          <span class="template-label">权限模板：</span>
          <el-button size="small" @click="applyTemplate('principal')">校长</el-button>
          <el-button size="small" @click="applyTemplate('dean')">教务主任</el-button>
          <el-button size="small" @click="applyTemplate('teacher')">普通教师</el-button>
          <el-button size="small" @click="applyTemplate('assistant')">助教</el-button>
        </div>
        <el-checkbox-group v-model="permForm.permissions">
          <el-checkbox
            v-for="item in permissionOptions"
            :key="item.value"
            :label="item.value"
          >
            {{ item.label }}
            <span class="permission-desc">（{{ item.desc }}）</span>
          </el-checkbox>
        </el-checkbox-group>
      </div>
      <template #footer>
        <el-button @click="permDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="permSubmitting" @click="submitPermission">确定</el-button>
      </template>
    </el-dialog>

    <!-- 授课记录弹窗 -->
    <el-dialog v-model="courseDialogVisible" title="授课记录" width="600px">
      <el-table :data="courseRecords" stripe size="small">
        <el-table-column prop="courseName" label="课程名称" min-width="180" />
        <el-table-column prop="studentCount" label="学员数" width="80" />
        <el-table-column prop="startDate" label="开课日期" width="120" />
        <el-table-column prop="status" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.status === '进行中' ? 'success' : 'info'" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <el-button @click="courseDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * TeacherManagePage - 教师管理页面
 */
import { ref, reactive, computed, onMounted } from 'vue'
import { Plus, Download, Delete, User, CircleCheck, CircleClose, UserFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import Pagination from '@/components/common/Pagination.vue'
import { teacherApi } from '@/api/admin'
import { useOperationLog } from '@/composables/useOperationLog'

const opLog = useOperationLog('teacher')

// ==================== 权限相关常量 ====================

/** 权限模块选项 */
const permissionOptions = [
  { value: '课程管理', label: '课程管理', desc: '管理课程、上传视频' },
  { value: '学员管理', label: '学员管理', desc: '查看学员、编辑学员信息' },
  { value: '赛事管理', label: '赛事管理', desc: '创建赛事、审核报名' },
  { value: '题库管理', label: '题库管理', desc: '添加题目、导入导出' },
  { value: '阅卷管理', label: '阅卷管理', desc: '自动阅卷、手动批改、成绩审核' },
  { value: '数据查看', label: '数据查看', desc: '查看统计报表、导出数据' },
]

/** 权限模板定义 */
const permissionTemplates: Record<string, string[]> = {
  principal: ['课程管理', '学员管理', '赛事管理', '题库管理', '阅卷管理', '数据查看'],
  dean: ['课程管理', '学员管理', '赛事管理', '题库管理'],
  teacher: ['课程管理', '学员管理', '数据查看'],
  assistant: ['数据查看'],
}

/** 权限标签颜色映射 */
const permissionTagTypeMap: Record<string, string> = {
  '课程管理': 'primary',
  '学员管理': 'success',
  '赛事管理': 'warning',
  '题库管理': 'danger',
  '阅卷管理': 'info',
  '数据查看': 'primary',
}

function getPermissionTagType(perm: string) {
  return (permissionTagTypeMap[perm] || 'info') as 'primary' | 'success' | 'warning' | 'danger' | 'info'
}

// ==================== 页面基础状态 ====================

const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(20)

const searchForm = reactive({
  name: '',
  subject: '',
  status: '',
})

function handleSearch() {
  pageNum.value = 1
  ElMessage.success('搜索完成')
}
function resetSearch() {
  searchForm.name = ''
  searchForm.subject = ''
  searchForm.status = ''
  pageNum.value = 1
}

// ==================== Mock 数据 ====================

const teacherList = ref([
  { id: 1, name: '张老师', subject: '信息学', title: '教授', phone: '13800138101', email: 'zhang@example.com', courseCount: 5, studentCount: 128, status: 'enabled', avatar: '', intro: '资深信息学竞赛教练', permissions: ['课程管理', '学员管理', '赛事管理', '题库管理', '阅卷管理', '数据查看'] },
  { id: 2, name: '李老师', subject: '数学', title: '副教授', phone: '13800138102', email: 'li@example.com', courseCount: 4, studentCount: 96, status: 'enabled', avatar: '', intro: '数学奥林匹克金牌教练', permissions: ['课程管理', '学员管理', '赛事管理', '题库管理'] },
  { id: 3, name: '王老师', subject: '英语', title: '讲师', phone: '13800138103', email: 'wang@example.com', courseCount: 3, studentCount: 64, status: 'enabled', avatar: '', intro: '雅思托福高分导师', permissions: ['课程管理', '学员管理', '数据查看'] },
  { id: 4, name: '赵老师', subject: '物理', title: '教授', phone: '13800138104', email: 'zhao@example.com', courseCount: 6, studentCount: 152, status: 'enabled', avatar: '', intro: '物理竞赛国家级评委', permissions: ['课程管理', '学员管理', '赛事管理', '题库管理', '阅卷管理', '数据查看'] },
  { id: 5, name: '陈老师', subject: '化学', title: '讲师', phone: '13800138105', email: 'chen@example.com', courseCount: 2, studentCount: 48, status: 'disabled', avatar: '', intro: '化学实验教学专家', permissions: ['课程管理', '数据查看'] },
  { id: 6, name: '刘老师', subject: '语文', title: '副教授', phone: '13800138106', email: 'liu@example.com', courseCount: 4, studentCount: 112, status: 'enabled', avatar: '', intro: '古典文学研究学者', permissions: ['课程管理', '学员管理', '赛事管理', '题库管理'] },
  { id: 7, name: '孙老师', subject: '生物', title: '讲师', phone: '13800138107', email: 'sun@example.com', courseCount: 3, studentCount: 72, status: 'enabled', avatar: '', intro: '生物科学博士', permissions: ['课程管理', '学员管理', '数据查看'] },
  { id: 8, name: '周老师', subject: '信息学', title: '讲师', phone: '13800138108', email: 'zhou@example.com', courseCount: 2, studentCount: 56, status: 'enabled', avatar: '', intro: 'ACM-ICPC 区域赛银牌', permissions: ['课程管理', '题库管理', '阅卷管理'] },
  { id: 9, name: '吴老师', subject: '数学', title: '教授', phone: '13800138109', email: 'wu@example.com', courseCount: 7, studentCount: 180, status: 'enabled', avatar: '', intro: '国家数学奥林匹克主教练', permissions: ['课程管理', '学员管理', '赛事管理', '题库管理', '阅卷管理', '数据查看'] },
  { id: 10, name: '郑老师', subject: '英语', title: '助教', phone: '13800138110', email: 'zheng@example.com', courseCount: 1, studentCount: 32, status: 'disabled', avatar: '', intro: '英语教育硕士', permissions: ['数据查看'] },
])

// ==================== 搜索过滤与分页 ====================

// 按搜索条件筛选后的列表（name/subject/status）
const filteredTeacherList = computed(() => {
  return teacherList.value.filter(t => {
    if (searchForm.name && !t.name.toLowerCase().includes(searchForm.name.toLowerCase())) return false
    if (searchForm.subject && t.subject !== searchForm.subject) return false
    if (searchForm.status && t.status !== searchForm.status) return false
    return true
  })
})

// 总条数：跟随筛选结果动态变化
const total = computed(() => filteredTeacherList.value.length)

const selectedRows = ref<any[]>([])

const totalTeacherCount = computed(() => teacherList.value.length)
const enabledCount = computed(() => teacherList.value.filter(t => t.status === 'enabled').length)
const disabledCount = computed(() => teacherList.value.filter(t => t.status === 'disabled').length)
const totalStudentCount = computed(() => teacherList.value.reduce((sum, t) => sum + (t.studentCount || 0), 0))

function handleSelectionChange(val: any[]) {
  selectedRows.value = val
}

function handleBatchDelete() {
  const names = selectedRows.value.map(r => r.name).join('、')
  ElMessageBox.confirm(`确定删除以下 ${selectedRows.value.length} 名教师吗？\n${names}`, '警告', { type: 'warning' })
    .then(async () => {
      const ids = selectedRows.value.map(r => r.id)
      try {
        await teacherApi.batchRemove(ids)
      } catch (e) {
        console.warn('batch delete fallback', e)
      }
      teacherList.value = teacherList.value.filter(t => !ids.includes(t.id))
      ElMessage.success('删除成功')
      opLog.logBatchDelete('批量删除' + selectedRows.value.length + '名教师')
    })
    .catch(() => {})
}

// 分页后的列表：按当前页码与每页条数对筛选结果进行切片
const pagedTeacherList = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  return filteredTeacherList.value.slice(start, start + pageSize.value)
})

/** 获取教师列表，调用 bridge 列表接口，失败时回退到本地 mock 数据 */
async function fetchTeacherList() {
  loading.value = true
  try {
    const res = await teacherApi.list({ pageNum: 1, pageSize: 9999 })
    const list = res?.data?.list || res?.data?.records || []
    if (list.length) teacherList.value = list
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
  name: '',
  subject: '',
  title: '',
  phone: '',
  email: '',
  intro: '',
  avatar: '',
  status: 'enabled' as string,
  permissions: [] as string[],
})
const formRules: FormRules = {
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  subject: [{ required: true, message: '请选择科目', trigger: 'change' }],
  title: [{ required: true, message: '请选择职称', trigger: 'change' }],
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
}

function handleAdd() {
  isEdit.value = false
  Object.assign(form, { id: null, name: '', subject: '', title: '', phone: '', email: '', intro: '', avatar: '', status: 'enabled', permissions: [] })
  formDialogVisible.value = true
}
function handleEdit(row: any) {
  isEdit.value = true
  Object.assign(form, row)
  formDialogVisible.value = true
}
async function submitForm() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      // 可落库字段：姓名/手机/邮箱/头像/状态（写 sys_user）。科目/职称/简介/权限等富档案
      // 后端暂无独立表，保留本地展示，属已知缺口。
      const payload: any = {
        name: form.name,
        phone: form.phone,
        email: form.email,
        avatar: form.avatar,
        status: form.status || 'enabled',
      }
      if (isEdit.value && form.id != null) {
        await teacherApi.update(form.id as number, payload)
        const idx = teacherList.value.findIndex(t => t.id === form.id)
        if (idx !== -1) teacherList.value[idx] = { ...teacherList.value[idx], ...form } as any
        ElMessage.success('编辑成功')
        opLog.logUpdate('编辑教师「' + form.name + '」')
      } else {
        const res: any = await teacherApi.create(payload)
        const created = res?.data
        const newId = created?.id ?? Date.now()
        teacherList.value.unshift({ ...form, id: newId, courseCount: 0, studentCount: 0, status: form.status || 'enabled' } as any)
        ElMessage.success('添加成功')
        opLog.logCreate('新增教师「' + form.name + '」')
      }
      formDialogVisible.value = false
    } catch (e) {
      console.warn('save teacher failed, fallback to local', e)
      if (isEdit.value && form.id != null) {
        const idx = teacherList.value.findIndex(t => t.id === form.id)
        if (idx !== -1) teacherList.value[idx] = { ...teacherList.value[idx], ...form } as any
      } else {
        teacherList.value.unshift({ ...form, id: Date.now(), courseCount: 0, studentCount: 0, status: form.status || 'enabled' } as any)
      }
      ElMessage.warning('后端未连接，已本地保存（未落库）')
      formDialogVisible.value = false
    } finally {
      submitting.value = false
    }
  })
}

// ==================== 权限模板应用 ====================

function applyTemplate(templateKey: string) {
  const perms = permissionTemplates[templateKey]
  if (!perms) return
  // 根据当前激活的弹窗，设置对应的权限数据
  if (permDialogVisible.value) {
    permForm.permissions = [...perms]
  } else {
    form.permissions = [...perms]
  }
  const templateNames: Record<string, string> = {
    principal: '校长',
    dean: '教务主任',
    teacher: '普通教师',
    assistant: '助教',
  }
  ElMessage.success(`已应用「${templateNames[templateKey]}」权限模板`)
}

// ==================== 分配权限弹窗 ====================

const permDialogVisible = ref(false)
const permSubmitting = ref(false)
const permTargetTeacher = ref<any>(null)
const permForm = reactive({
  permissions: [] as string[],
})

function handleAssignPermission(row: any) {
  permTargetTeacher.value = row
  permForm.permissions = [...(row.permissions || [])]
  permDialogVisible.value = true
}

async function submitPermission() {
  permSubmitting.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 500))
    const idx = teacherList.value.findIndex(t => t.id === permTargetTeacher.value.id)
    if (idx !== -1) {
      teacherList.value[idx].permissions = [...permForm.permissions]
    }
    permDialogVisible.value = false
    ElMessage.success(`已更新「${permTargetTeacher.value.name}」的权限`)
  } finally {
    permSubmitting.value = false
  }
}

// ==================== 查看授课记录 ====================

const courseDialogVisible = ref(false)
const courseRecords = ref<any[]>([])
function handleViewCourses(_row: any) {
  courseRecords.value = [
    { courseName: 'Python 基础入门', studentCount: 45, startDate: '2024-03-01', status: '进行中' },
    { courseName: 'C++ 算法竞赛', studentCount: 38, startDate: '2024-02-15', status: '进行中' },
    { courseName: 'GoC 图形编程', studentCount: 28, startDate: '2023-09-01', status: '已结课' },
  ]
  courseDialogVisible.value = true
}

// ==================== 重置密码 ====================

async function handleResetPassword(row: any) {
  try {
    await ElMessageBox.confirm(`确定将教师 "${row.name}" 的密码重置为 123456 吗？`, '提示', { type: 'warning' })
  } catch { return }
  try {
    await teacherApi.update(row.id, { password: '123456' })
    ElMessage.success('密码已重置为 123456')
    opLog.logUpdate('重置教师「' + row.name + '」的密码')
  } catch (e) {
    console.warn('reset password fallback', e)
    ElMessage.warning('后端未连接，重置未落库')
  }
}

// ==================== 禁用/启用 ====================

async function handleToggleStatus(row: any) {
  const next = row.status === 'enabled' ? 'disabled' : 'enabled'
  const label = next === 'enabled' ? '启用' : '禁用'
  try {
    await ElMessageBox.confirm(`确定${label}教师 "${row.name}" 吗？`, '提示', { type: 'warning' })
  } catch { return }
  row.status = next
  ElMessage.success(`${label}成功`)
  try {
    await teacherApi.update(row.id, { status: next })
  } catch (e) {
    console.warn('toggle status fallback', e)
  }
}

// ==================== 头像上传占位 ====================

function handleUploadAvatar() {
  ElMessage.info('头像上传功能占位，后续对接云存储 API')
}

// ==================== 批量导出 ====================

function handleExport() {
  const headers = ['ID', '姓名', '科目', '职称', '手机号', '邮箱', '授课课程数', '学员数', '权限', '状态']
  const rows = teacherList.value.map(t => [
    t.id, t.name, t.subject, t.title, t.phone, t.email, t.courseCount, t.studentCount,
    (t.permissions || []).join('|'),
    t.status === 'enabled' ? '启用' : '禁用'
  ])
  const csv = [headers.join(','), ...rows.map(r => r.join(','))].join('\n')
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `教师列表_${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
  opLog.logExport('导出教师数据')
}

onMounted(() => {
  fetchTeacherList()
})
</script>

<style scoped lang="scss">
$primary: #165DFF;

.teacher-manage-page { padding: 20px; }
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
  &__icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 52px;
    height: 52px;
    border-radius: 12px;
    flex-shrink: 0;
    &--total { background: rgba(22, 93, 255, 0.1); color: $primary; }
    &--active { background: rgba(103, 194, 58, 0.1); color: #36D399; }
    &--disabled { background: rgba(245, 108, 108, 0.1); color: #F53F3F; }
    &--students { background: rgba(230, 162, 60, 0.1); color: #FF9500; }
  }
  &__content { flex: 1; min-width: 0; }
  &__value { font-size: 24px; font-weight: 600; color: #1D2129; line-height: 1.2; }
  &__label { font-size: 13px; color: #86909C; margin-top: 4px; }
}
.search-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.action-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }

// 权限标签样式
.permission-tag {
  margin: 2px 4px 2px 0;
}
.no-permission {
  font-size: 12px;
  color: #86909C;
}

// 权限配置区域
.permission-section {
  width: 100%;
  .permission-template-bar {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 12px;
    padding-bottom: 10px;
    border-bottom: 1px solid #F0F0F0;
    .template-label {
      font-size: 13px;
      color: #4E5969;
      white-space: nowrap;
    }
  }
  .el-checkbox-group {
    display: flex;
    flex-direction: column;
    gap: 8px;
    .el-checkbox {
      margin-right: 0;
      height: auto;
      align-items: flex-start;
    }
  }
  .permission-desc {
    font-size: 12px;
    color: #86909C;
    margin-left: 4px;
  }
}

// 分配权限弹窗头部
.perm-dialog-header {
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid #F0F0F0;
  font-size: 14px;
  color: #4E5969;
  strong {
    color: #1D2129;
  }
}

.avatar-uploader {
  .avatar-preview {
    position: relative;
    width: 120px;
    height: 120px;
    border-radius: 8px;
    overflow: hidden;
    img { width: 100%; height: 100%; object-fit: cover; }
    .avatar-actions {
      position: absolute;
      top: 0; left: 0; right: 0; bottom: 0;
      background: rgba(0,0,0,0.4);
      display: flex;
      align-items: center;
      justify-content: center;
      opacity: 0;
      transition: opacity 0.2s;
      cursor: pointer;
      .el-icon { color: #fff; font-size: 20px; }
    }
    &:hover .avatar-actions { opacity: 1; }
  }
  .avatar-placeholder {
    width: 120px;
    height: 120px;
    border: 2px dashed #E5E6EB;
    border-radius: 8px;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: #86909C;
    transition: border-color 0.2s;
    &:hover { border-color: #165DFF; color: #165DFF; }
    .el-icon { font-size: 28px; margin-bottom: 8px; }
    span { font-size: 12px; }
  }
}
.upload-tip { font-size: 12px; color: #86909C; margin-top: 8px; }
</style>
