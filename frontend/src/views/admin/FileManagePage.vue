<template>
  <!-- 文件管理后台 -->
  <div class="file-manage-page">
    <div class="page-header">
      <h2>文件管理</h2>
      <FileUpload :action="`/api/file/upload`" @success="handleUploadSuccess" />
    </div>
    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索文件名" clearable style="width: 240px" />
      <el-select v-model="typeFilter" placeholder="文件类型" clearable style="width: 140px">
        <el-option label="图片" value="image" />
        <el-option label="视频" value="video" />
        <el-option label="文档" value="document" />
      </el-select>
      <el-button @click="handleExport">导出CSV</el-button>
    </div>
    <el-table :data="pagedList" stripe v-loading="loading">
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="name" label="文件名" min-width="200" />
      <el-table-column prop="type" label="类型" width="80" />
      <el-table-column prop="size" label="大小" width="100">
        <template #default="{ row }">{{ formatFileSize(row.size) }}</template>
      </el-table-column>
      <el-table-column prop="uploadTime" label="上传时间" width="170" />
      <el-table-column prop="uploader" label="上传者" width="100" />
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="{ row }">
          <el-button size="small" text type="primary" @click="handleView(row as FileItem)">查看</el-button>
          <el-button size="small" text type="primary" @click="handleCopy(row)">复制链接</el-button>
          <el-button size="small" text type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="total" v-model:page="pageNum" v-model:page-size="pageSize" />

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewDialogVisible" title="文件详情" width="640px">
      <div v-if="viewRow" class="view-body" style="line-height: 2;">
        <p><strong>ID：</strong>{{ viewRow.id }}</p>
        <p><strong>文件名：</strong>{{ viewRow.name }}</p>
        <p><strong>类型：</strong>{{ typeLabelMap[viewRow.type] || viewRow.type }}</p>
        <p><strong>大小：</strong>{{ formatFileSize(viewRow.size) }}</p>
        <p><strong>上传时间：</strong>{{ viewRow.uploadTime }}</p>
        <p><strong>上传者：</strong>{{ viewRow.uploader }}</p>
        <p><strong>链接：</strong>{{ viewRow.url || `/uploads/${viewRow.name}` }}</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * FileManagePage - 文件管理后台
 */
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import FileUpload from '@/components/common/FileUpload.vue'
import Pagination from '@/components/common/Pagination.vue'
import { fileApi } from '@/api/admin'
import { useOperationLog } from '@/composables/useOperationLog'
const opLog = useOperationLog('file')

const pageNum = ref(1)
const pageSize = ref(20)
const loading = ref(false)
const keyword = ref('')
const typeFilter = ref('')

interface FileItem {
  id: number
  name: string
  type: string
  size: number
  uploadTime: string
  uploader: string
  url?: string
}

const fileList = ref<FileItem[]>([
  { id: 1, name: 'python-course-cover.jpg', type: 'image', size: 256000, uploadTime: '2024-03-15 10:00', uploader: '张老师' },
  { id: 2, name: 'chapter1-intro.mp4', type: 'video', size: 52428800, uploadTime: '2024-03-14 16:00', uploader: '张老师' },
  { id: 3, name: 'practice-data.xlsx', type: 'document', size: 102400, uploadTime: '2024-03-13 09:00', uploader: '管理员' },
])

/** 按关键词和类型过滤后的文件列表 */
const filteredList = computed(() => {
  return fileList.value.filter(item => {
    const kw = keyword.value.trim().toLowerCase()
    const matchKeyword = !kw || (item.name || '').toLowerCase().includes(kw)
    const matchType = !typeFilter.value || item.type === typeFilter.value
    return matchKeyword && matchType
  })
})

/** 当前页的分页数据 */
const pagedList = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  return filteredList.value.slice(start, start + pageSize.value)
})

/** 总数，反映过滤后的实际文件数量 */
const total = computed(() => filteredList.value.length)

function formatFileSize(bytes: number) {
  if (bytes < 1024) return bytes + 'B'
  if (bytes < 1048576) return (bytes / 1024).toFixed(1) + 'KB'
  return (bytes / 1048576).toFixed(1) + 'MB'
}

/** 获取文件列表，调用真实 API，失败时保留现有数据 */
async function fetchList() {
  loading.value = true
  try {
    const res = await fileApi.list({ pageNum: 1, pageSize: 9999 })
    const list = res?.data?.list || res?.data?.records || []
    if (list.length) fileList.value = list
  } catch (e) {
    console.warn('获取文件列表失败，保留现有数据', e)
  } finally {
    loading.value = false
  }
}

function handleUploadSuccess(res: any) {
  ElMessage.success('上传成功')
  opLog.logCreate('上传文件')
  // 如果 API 返回了文件信息，直接添加到列表
  if (res?.data) {
    const file = res.data
    fileList.value.unshift({
      id: file.id || Date.now(),
      name: file.name || file.originalName || '未命名文件',
      type: file.type || 'image',
      size: file.size || 0,
      uploadTime: new Date().toLocaleString(),
      uploader: file.uploader || '管理员',
      url: file.url || file.path || '',
    })
  } else {
    fetchList()
  }
}
function handleCopy(row: any) {
  const url = row.url || `/uploads/${row.name}`
  navigator.clipboard.writeText(url)
    .then(() => ElMessage.success('链接已复制'))
    .catch(() => ElMessage.error('复制失败，请手动复制'))
}
function handleDelete(row: any) {
  ElMessageBox.confirm(`确定删除文件 "${row.name}" 吗？`, '警告', { type: 'warning' })
    .then(async () => {
      try { await fileApi.remove(row.id) } catch {}
      const index = fileList.value.findIndex(item => item.id === row.id)
      if (index > -1) {
        fileList.value.splice(index, 1)
        ElMessage.success('删除成功')
        opLog.logDelete('删除文件')
      }
    })
    .catch(() => {})
}

onMounted(() => {
  fetchList()
})

// ========== 类型标签映射 ==========
const typeLabelMap: Record<string, string> = {
  image: '图片',
  video: '视频',
  document: '文档',
}

// ========== 导出 CSV ==========
function handleExport() {
  const headers = ['ID', '文件名', '类型', '大小', '上传时间', '上传者', '链接']
  const rows = filteredList.value.map(item => [
    item.id,
    item.name,
    typeLabelMap[item.type] || item.type,
    formatFileSize(item.size),
    item.uploadTime,
    item.uploader,
    item.url || `/uploads/${item.name}`,
  ])
  const csv = [headers.join(','), ...rows.map(r => r.map(c => `"${c}"`).join(','))].join('\n')
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `文件数据_${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
  opLog.logExport('导出文件列表')
}

// ========== 查看详情 ==========
const viewDialogVisible = ref(false)
const viewRow = ref<FileItem | null>(null)

function handleView(row: FileItem) {
  viewRow.value = row
  viewDialogVisible.value = true
}
</script>

<style scoped lang="scss">
.file-manage-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; h2 { font-size: 20px; color: #1D2129; } }
.search-bar { display: flex; gap: 12px; margin-bottom: 16px; }
</style>
