<template>
  <!-- 课程管理后台 -->
  <div class="course-manage-page">
    <div class="page-header">
      <h2>课程管理</h2>
      <el-button type="primary" :icon="Plus" @click="handleAdd">新建课程</el-button>
    </div>
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索课程名称" clearable style="width: 240px" />
      <el-select v-model="ageFilter" placeholder="年龄段筛选" clearable style="width: 160px">
        <el-option label="4-7岁" value="4-7" />
        <el-option label="6-12岁" value="6-12" />
        <el-option label="7-10岁" value="7-10" />
        <el-option label="10-16岁" value="10-16" />
      </el-select>
      <el-select v-model="statusFilter" placeholder="状态" clearable style="width: 120px">
        <el-option label="已发布" value="published" />
        <el-option label="草稿" value="draft" />
        <el-option label="已下架" value="offline" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button v-if="selectedIds.length > 0" type="danger" @click="handleBatchDelete">批量删除({{ selectedIds.length }})</el-button>
      <el-button v-if="selectedIds.length > 0" type="success" @click="handleBatchPublish">批量发布</el-button>
      <el-button @click="handleExport">导出CSV</el-button>
    </div>
    <el-table :data="pagedList" stripe @selection-change="handleSelectionChange">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="课程名称" min-width="200" />
      <el-table-column prop="teacherName" label="授课教师" width="100" />
      <el-table-column prop="ageRange" label="适合年龄" width="100" />
      <el-table-column prop="lessonCount" label="课时数" width="80" />
      <el-table-column prop="videoCount" label="视频数" width="80" />
      <el-table-column prop="studentCount" label="学习人数" width="90" />
      <el-table-column prop="hasTrial" label="开放试听" width="90">
        <template #default="{ row }">
          <el-tag :type="row.hasTrial ? 'success' : 'info'" size="small">{{ row.hasTrial ? '是' : '否' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 'published' ? 'success' : row.status === 'draft' ? 'warning' : 'info'" size="small">
            {{ ({ published: '已发布', draft: '草稿', offline: '已下架' } as Record<string, string>)[row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="170" />
      <el-table-column label="操作" width="320" fixed="right">
        <template #default="{ row }">
          <el-button size="small" text type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" text type="primary" @click="handleVideoManage(row)">视频管理</el-button>
          <el-button size="small" text type="primary" @click="handleViewTrials(row)">试听预约</el-button>
          <el-button size="small" text type="warning" @click="handleOffline(row)" v-if="row.status === 'published'">下架</el-button>
          <el-button size="small" text type="success" @click="handlePublish(row)" v-if="row.status === 'draft'">发布</el-button>
          <el-button size="small" text type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="pageNum" v-model:page-size="pageSize" />

    <!-- 创建/编辑课程弹窗 -->
    <el-dialog v-model="createDialogVisible" :title="isEdit ? '编辑课程' : '新建课程'" width="720px" destroy-on-close>
      <el-form ref="createFormRef" :model="createForm" :rules="createRules" label-width="100px">
        <el-form-item label="课程名称" prop="title">
          <el-input v-model="createForm.title" placeholder="请输入课程名称" />
        </el-form-item>
        <el-form-item label="授课教师" prop="teacherName">
          <el-input v-model="createForm.teacherName" placeholder="请输入授课教师" />
        </el-form-item>
        <el-form-item label="适合年龄" prop="ageRange">
          <el-select v-model="createForm.ageRange" placeholder="选择适合年龄段" style="width: 100%">
            <el-option label="4-7岁" value="4-7岁" />
            <el-option label="6-12岁" value="6-12岁" />
            <el-option label="7-10岁" value="7-10岁" />
            <el-option label="10-16岁" value="10-16岁" />
          </el-select>
        </el-form-item>
        <el-form-item label="课时数" prop="lessonCount">
          <el-input-number v-model="createForm.lessonCount" :min="1" :max="200" style="width: 100%" />
        </el-form-item>
        <el-form-item label="开放试听" prop="hasTrial">
          <el-switch v-model="createForm.hasTrial" />
        </el-form-item>
        <el-form-item label="课程简介" prop="description">
          <el-input v-model="createForm.description" type="textarea" :rows="3" placeholder="请输入课程简介" />
        </el-form-item>
        <el-form-item label="课程封面">
          <ImageUploader
            v-model="createForm.cover"
            type="course-cover"
            :id="createForm.id"
            :preview-width="240"
            :preview-height="135"
            preset="cover"
          />
        </el-form-item>

        <!-- 视频列表（扁平，无章节） -->
        <el-divider content-position="left">视频列表</el-divider>
        <div class="video-section">
          <div class="vod-config-tip">
            <el-icon><InfoFilled /></el-icon>
            <span>添加视频名称与视频地址（外链或云点播地址）</span>
          </div>
          <div class="video-list">
            <div
              v-for="(video, vIdx) in createForm.videos"
              :key="vIdx"
              class="video-item"
            >
              <span class="drag-handle">&#9776;</span>
              <el-input v-model="video.title" placeholder="视频名称" style="width: 200px" size="small" />
              <el-input v-model="video.videoUrl" placeholder="视频地址(URL)" style="flex: 1" size="small" />
              <el-input-number v-model="video.sortOrder" :min="0" size="small" style="width: 90px" />
              <el-button size="small" text type="danger" @click="removeVideo(vIdx)">删除</el-button>
            </div>
            <el-button size="small" type="primary" plain @click="addVideo">+ 添加视频</el-button>
          </div>
        </div>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitCreate">确定</el-button>
      </template>
    </el-dialog>

    <!-- 视频管理弹窗 -->
    <el-dialog v-model="videoDialogVisible" title="视频管理" width="760px" destroy-on-close>
      <div v-if="currentCourse" class="video-manage">
        <h4>{{ currentCourse.title }}</h4>
        <div class="video-manage-toolbar">
          <el-input v-model="newVideoTitle" placeholder="视频名称" style="width: 200px" size="small" />
          <el-input v-model="newVideoUrl" placeholder="视频地址(URL)" style="flex: 1" size="small" />
          <el-button size="small" type="primary" @click="handleAddVideo">添加视频</el-button>
        </div>
        <el-table :data="currentCourse.videos" size="small" stripe style="margin-top: 12px">
          <el-table-column prop="title" label="视频名称" min-width="160" />
          <el-table-column prop="videoUrl" label="视频地址" min-width="220" show-overflow-tooltip />
          <el-table-column prop="sortOrder" label="排序" width="70" />
          <el-table-column label="操作" width="90">
            <template #default="{ row }">
              <el-button size="small" text type="danger" @click="handleDeleteVideo(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>
      <template #footer>
        <el-button @click="videoDialogVisible = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * CourseManagePage - 课程管理后台
 * 课程列表、新建、编辑、发布、下架、视频管理
 */
import { ref, computed, onMounted } from 'vue'
import { Plus, InfoFilled } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import Pagination from '@/components/common/Pagination.vue'
import ImageUploader from '@/components/common/ImageUploader.vue'
import { courseApi } from '@/api/admin'
import { getCourseVideos, addCourseVideo, deleteCourseVideo } from '@/api/course'
import { useOperationLog } from '@/composables/useOperationLog'

const opLog = useOperationLog('course')

const keyword = ref('')
const statusFilter = ref('')
const ageFilter = ref('')
const pageNum = ref(1)
const pageSize = ref(20)

const courseList = ref<any[]>([])

// 按搜索条件筛选后的列表
const filteredList = computed(() => {
  return courseList.value.filter((item: any) => {
    const matchKeyword = !keyword.value || item.title?.toLowerCase().includes(keyword.value.toLowerCase())
    const matchAge = !ageFilter.value || item.ageRange === ageFilter.value + '岁'
    const matchStatus = !statusFilter.value || item.status === statusFilter.value
    return matchKeyword && matchAge && matchStatus
  })
})

// 当前页展示数据（分页切片）
const pagedList = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  return filteredList.value.slice(start, start + pageSize.value)
})

// 总条数
const total = computed(() => filteredList.value.length)

/** 拉取课程列表 */
async function handleSearch() {
  try {
    const res = await courseApi.list({
      keyword: keyword.value,
      ageRange: ageFilter.value,
      status: statusFilter.value,
      pageNum: 1,
      pageSize: 9999,
    })
    courseList.value = res?.data?.list || res?.data?.records || []
  } catch {
    courseList.value = []
    ElMessage.error('列表加载失败，请检查网络或稍后重试')
  }
}
function handleAdd() {
  isEdit.value = false
  createForm.value = {
    id: null,
    title: '', teacherName: '', ageRange: '', lessonCount: 12, hasTrial: true, description: '', cover: '',
    videos: []
  }
  createDialogVisible.value = true
}
function handleEdit(row: any) {
  isEdit.value = true
  createForm.value = JSON.parse(JSON.stringify(row))
  createDialogVisible.value = true
}
function handleViewTrials(row: any) { ElMessage.info(`查看试听预约: ${row.title}`) }
async function handlePublish(row: any) {
  try {
    await courseApi.update(row.id, { status: 'published' })
    row.status = 'published'
    ElMessage.success('发布成功')
  } catch {
    ElMessage.error('操作失败，请检查网络或稍后重试')
  }
}
async function handleOffline(row: any) {
  try {
    await courseApi.update(row.id, { status: 'offline' })
    row.status = 'offline'
    ElMessage.success('已下架')
  } catch {
    ElMessage.error('操作失败，请检查网络或稍后重试')
  }
}
function handleDelete(row: any) {
  ElMessageBox.confirm(`确定删除课程 "${row.title}" 吗？`, '警告', { type: 'warning' })
    .then(async () => {
      try {
        await courseApi.remove(row.id)
        const index = courseList.value.findIndex((c: any) => c.id === row.id)
        if (index > -1) courseList.value.splice(index, 1)
        ElMessage.success('删除成功')
        opLog.logDelete('删除课程')
      } catch {
        ElMessage.error('操作失败，请检查网络或稍后重试')
      }
    })
    .catch(() => {})
}

// ==================== 批量操作与导出 ====================
const selectedIds = ref<number[]>([])

function handleSelectionChange(rows: any[]) {
  selectedIds.value = rows.map(r => r.id)
}

function handleBatchDelete() {
  ElMessageBox.confirm(`确定批量删除选中的 ${selectedIds.value.length} 门课程吗？`, '警告', { type: 'warning' })
    .then(async () => {
      try {
        await courseApi.batchRemove(selectedIds.value)
        courseList.value = courseList.value.filter((c: any) => !selectedIds.value.includes(c.id))
        selectedIds.value = []
        ElMessage.success('批量删除成功')
      } catch {
        ElMessage.error('操作失败，请检查网络或稍后重试')
      }
    })
    .catch(() => {})
}

function handleBatchPublish() {
  ElMessageBox.confirm(`确定批量发布选中的 ${selectedIds.value.length} 门课程吗？`, '提示', { type: 'warning' })
    .then(async () => {
      let failed = 0
      const succeeded: number[] = []
      for (const id of selectedIds.value) {
        try {
          await courseApi.update(id, { status: 'published' })
          succeeded.push(id)
        } catch {
          failed++
        }
      }
      courseList.value.forEach(c => {
        if (succeeded.includes(c.id)) c.status = 'published'
      })
      selectedIds.value = []
      if (failed > 0) {
        ElMessage.error(`${failed} 项发布失败，请检查网络或稍后重试`)
      } else {
        ElMessage.success('批量发布成功')
      }
    })
    .catch(() => {})
}

function handleExport() {
  const headers = ['ID', '课程名称', '授课教师', '适合年龄', '课时数', '视频数', '学习人数', '试听', '状态', '创建时间']
  const rows = filteredList.value.map((c: any) => [c.id, c.title, c.teacherName, c.ageRange, c.lessonCount, c.videoCount, c.studentCount, c.hasTrial ? '是' : '否', c.status === 'published' ? '已发布' : c.status === 'draft' ? '草稿' : '已下架', c.createTime])
  const csv = [headers.join(','), ...rows.map(r => r.map(c => `"${c}"`).join(','))].join('\n')
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `课程数据_${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
}

// 视频管理
const videoDialogVisible = ref(false)
const currentCourse = ref<any>(null)
const newVideoTitle = ref('')
const newVideoUrl = ref('')

async function handleVideoManage(row: any) {
  currentCourse.value = JSON.parse(JSON.stringify(row))
  currentCourse.value.videos = []
  videoDialogVisible.value = true
  try {
    const res = await getCourseVideos(row.id)
    currentCourse.value.videos = Array.isArray(res?.data) ? res.data : []
  } catch {
    currentCourse.value.videos = []
    ElMessage.error('列表加载失败，请检查网络或稍后重试')
  }
}

async function handleAddVideo() {
  if (!newVideoTitle.value.trim()) { ElMessage.warning('请输入视频名称'); return }
  if (!newVideoUrl.value.trim()) { ElMessage.warning('请输入视频地址'); return }
  try {
    const res = await addCourseVideo({
      courseId: currentCourse.value.id,
      title: newVideoTitle.value.trim(),
      videoUrl: newVideoUrl.value.trim(),
      sortOrder: currentCourse.value.videos.length,
    })
    currentCourse.value.videos.push(res?.data || { id: Date.now(), title: newVideoTitle.value, videoUrl: newVideoUrl.value, sortOrder: currentCourse.value.videos.length })
    newVideoTitle.value = ''
    newVideoUrl.value = ''
    ElMessage.success('添加成功')
  } catch {
    ElMessage.error('操作失败，请检查网络或稍后重试')
  }
}

async function handleDeleteVideo(row: any) {
  try {
    await deleteCourseVideo(row.id)
    currentCourse.value.videos = currentCourse.value.videos.filter((v: any) => v.id !== row.id)
    ElMessage.success('删除成功')
  } catch {
    ElMessage.error('操作失败，请检查网络或稍后重试')
  }
}

// 创建/编辑课程弹窗
const createDialogVisible = ref(false)
const isEdit = ref(false)
const submitting = ref(false)
const createFormRef = ref<FormInstance>()
const createForm = ref<any>({
  id: null,
  title: '', teacherName: '', ageRange: '', lessonCount: 12, hasTrial: true, description: '', cover: '',
  videos: []
})
const createRules: FormRules = {
  title: [{ required: true, message: '请输入课程名称', trigger: 'blur' }],
  teacherName: [{ required: true, message: '请输入授课教师', trigger: 'blur' }],
  ageRange: [{ required: true, message: '请选择适合年龄', trigger: 'change' }],
  lessonCount: [{ required: true, message: '请输入课时数', trigger: 'change' }],
}

function addVideo() {
  createForm.value.videos.push({ title: '', videoUrl: '', sortOrder: createForm.value.videos.length })
}
function removeVideo(vIdx: number) {
  createForm.value.videos.splice(vIdx, 1)
}

async function submitCreate() {
  if (!createFormRef.value) return
  await createFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const videoCount = createForm.value.videos.length
      const payload = { ...createForm.value, videoCount }
      if (isEdit.value) {
        await courseApi.update(createForm.value.id, payload)
        const idx = courseList.value.findIndex(c => c.id === createForm.value.id)
        if (idx !== -1) {
          courseList.value[idx] = { ...courseList.value[idx], ...payload }
        }
        ElMessage.success('编辑课程成功')
        opLog.logUpdate('编辑课程')
      } else {
        const res = await courseApi.create(payload)
        const newId = res?.data?.id ?? Date.now()
        courseList.value.unshift({
          ...payload,
          id: newId,
          studentCount: 0,
          status: 'draft',
          createTime: new Date().toISOString().slice(0, 10)
        })
        ElMessage.success('创建课程成功')
        opLog.logCreate('新增课程')
      }
      createDialogVisible.value = false
    } catch {
      ElMessage.error('操作失败，请检查网络或稍后重试')
    } finally {
      submitting.value = false
    }
  })
}

onMounted(() => {
  handleSearch()
})
</script>

<style scoped lang="scss">
$primary: #165DFF;

.course-manage-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; h2 { font-size: 20px; color: #1D2129; } }
.search-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }

.video-section {
  .vod-config-tip {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 10px 12px;
    background: #E8F0FF;
    border: 1px solid #E8F0FF;
    border-radius: 6px;
    color: #165DFF;
    font-size: 13px;
    margin-bottom: 12px;
  }
}

.video-list {
  .video-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px;
    margin-bottom: 6px;
    background: $bg-white;
    border-radius: 6px;
    border: 1px solid #E5E6EB;
    .drag-handle {
      color: #86909C;
      cursor: move;
      user-select: none;
    }
  }
}

.video-manage {
  h4 { margin: 0 0 12px; font-size: 16px; color: #1D2129; }
}

.video-manage-toolbar {
  display: flex;
  align-items: center;
  gap: 8px;
}
</style>
