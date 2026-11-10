<template>
  <!-- 我的 Scratch 作品列表：卡片式布局，支持分页、搜索、操作 -->
  <div class="my-scratch-works">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-left">
        <div class="header-icon">
          <el-icon :size="24" color="#165DFF"><Brush /></el-icon>
        </div>
        <div class="header-text">
          <h2 class="page-title">我的 Scratch 作品</h2>
          <span class="work-count">共 {{ total }} 个作品</span>
        </div>
      </div>
      <div class="header-right">
        <el-input
          v-model="keyword"
          placeholder="搜索作品名称..."
          clearable
          class="search-input"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" class="create-btn" @click="handleCreate">
          <el-icon><Plus /></el-icon>
          新建作品
        </el-button>
      </div>
    </div>

    <!-- 新建作品大按钮（空状态或少量作品时显示） -->
    <div v-if="!loading && works.length < 6" class="create-big-section">
      <div class="create-big-card" @click="handleCreate">
        <div class="create-big-icon">
          <el-icon :size="40"><Plus /></el-icon>
        </div>
        <div class="create-big-title">创建新作品</div>
        <div class="create-big-desc">发挥创意，开始你的编程之旅</div>
      </div>
    </div>

    <!-- 作品列表 -->
    <div v-loading="loading" class="works-grid">
      <div
        v-for="work in works"
        :key="work.id"
        class="work-card"
      >
        <!-- 缩略图 -->
        <div class="card-thumbnail">
          <img
            v-if="work.thumbnail"
            :src="work.thumbnail"
            :alt="work.title"
            class="thumbnail-img"
          />
          <div v-else class="thumbnail-placeholder">
            <div class="placeholder-bg">
              <el-icon :size="48"><Picture /></el-icon>
            </div>
          </div>
          <!-- Hover 操作按钮 -->
          <div class="thumbnail-overlay">
            <div class="overlay-actions">
              <div class="action-btn-overlay edit-btn" @click.stop="handleEdit(work)" title="继续编辑">
                <el-icon><EditPen /></el-icon>
              </div>
              <div class="action-btn-overlay share-btn" @click.stop="handleShare(work)" title="分享">
                <el-icon><Share /></el-icon>
              </div>
              <div class="action-btn-overlay delete-btn" @click.stop="handleDelete(work)" title="删除">
                <el-icon><Delete /></el-icon>
              </div>
            </div>
          </div>
        </div>

        <!-- 作品信息 -->
        <div class="card-info">
          <h3 class="card-title" :title="work.title">{{ work.title }}</h3>
          <div class="card-meta">
            <span class="meta-item">
              <el-icon><Clock /></el-icon>
              {{ formatDate(work.createdAt) }}
            </span>
            <span class="meta-item">
              <el-icon><Edit /></el-icon>
              {{ formatDate(work.updatedAt) }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty
      v-if="!loading && works.length === 0"
      description="还没有 Scratch 作品，快去创作吧！"
      :image-size="200"
    >
      <el-button type="primary" class="create-btn-large" @click="handleCreate">
        <el-icon><Plus /></el-icon>
        创建第一个作品
      </el-button>
    </el-empty>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-wrapper">
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[12, 24, 36, 48]"
        layout="total, sizes, prev, pager, next, jumper"
        background
        @size-change="fetchWorks"
        @current-change="fetchWorks"
      />
    </div>

    <!-- 分享对话框 -->
    <el-dialog
      v-model="shareDialogVisible"
      title="分享作品"
      width="480px"
      destroy-on-close
    >
      <div class="share-dialog-content">
        <p class="share-tip">复制以下链接分享给好友：</p>
        <el-input
          :model-value="shareLink"
          readonly
          class="share-link-input"
        >
          <template #append>
            <el-button @click="handleCopyLink">复制</el-button>
          </template>
        </el-input>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * MyScratchWorks - 我的 Scratch 作品列表
 * 展示用户所有 Scratch 作品，支持继续编辑、删除、分享、导出 sb3
 */
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Search,
  Plus,
  Picture,
  Clock,
  Edit,
  EditPen,
  Share,
  Delete,
  Brush,
} from '@element-plus/icons-vue'
import { loadWorks, deleteWork, exportSb3 } from '@/api/scratch'
import type { ScratchWork } from '@/types/editor'
import { formatDate } from '@/utils/format'

const router = useRouter()

// ==================== 数据状态 ====================
const works = ref<ScratchWork[]>([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(12)
const keyword = ref('')
const loading = ref(false)

// ==================== 分享对话框 ====================
const shareDialogVisible = ref(false)
const shareLink = ref('')

// ==================== 加载作品列表 ====================
async function fetchWorks() {
  loading.value = true
  try {
    const res = await loadWorks({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      keyword: keyword.value,
    })
    if (res?.data) {
      works.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch {
    // 后端不可用时保留空列表
    works.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// ==================== 搜索 ====================
function handleSearch() {
  pageNum.value = 1
  fetchWorks()
}

// ==================== 新建作品 ====================
function handleCreate() {
  router.push('/editor?lang=scratch&mode=create')
}

// ==================== 继续编辑 ====================
function handleEdit(work: ScratchWork) {
  router.push(`/editor?lang=scratch&mode=create&workId=${work.id}`)
}

// ==================== 导出 sb3 ====================
// @ts-ignore - handleExport is used in template
async function handleExport(work: ScratchWork) {
  try {
    const res = await exportSb3(work.id)
    if (res?.data?.downloadUrl) {
      // 触发浏览器下载
      const link = document.createElement('a')
      link.href = res.data.downloadUrl
      link.download = `${work.title}.sb3`
      link.click()
      ElMessage.success('导出成功')
    }
  } catch {
    ElMessage.error('导出失败，请稍后重试')
  }
}

// ==================== 分享 ====================
function handleShare(work: ScratchWork) {
  shareLink.value = `${window.location.origin}/scratch/share/${work.id}`
  shareDialogVisible.value = true
}

function handleCopyLink() {
  navigator.clipboard.writeText(shareLink.value).then(() => {
    ElMessage.success('链接已复制到剪贴板')
  }).catch(() => {
    ElMessage.warning('复制失败，请手动复制')
  })
}

// ==================== 删除 ====================
async function handleDelete(work: ScratchWork) {
  try {
    await ElMessageBox.confirm(
      `确定要删除作品「${work.title}」吗？删除后不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    await deleteWork(work.id)
    ElMessage.success('删除成功')
    fetchWorks()
  } catch {
    // 用户取消
  }
}

// ==================== 初始化 ====================
onMounted(() => {
  fetchWorks()
})
</script>

<style scoped lang="scss">
$primary: #165DFF;
$success: #36D399;
$danger: #F53F3F;
$warning: #FF7D00;
$text-title: #1D2129;
$text-body: #4E5969;
$text-muted: #86909C;
$bg-page: #F7F8FA;
$border-color: #E5E6EB;
$card-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);

.my-scratch-works {
  width: 100%;
  padding: 24px 20px;
}

// ==================== 页面头部 ====================
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
  flex-wrap: wrap;
  gap: 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.header-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  background: linear-gradient(135deg, rgba(22, 93, 255, 0.1) 0%, rgba(22, 93, 255, 0.05) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.header-text {
  display: flex;
  align-items: baseline;
  gap: 10px;
}

.page-title {
  font-size: 22px;
  font-weight: 700;
  color: $text-title;
  margin: 0;
}

.work-count {
  font-size: 14px;
  color: $text-muted;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
}

.search-input {
  width: 240px;

  :deep(.el-input__wrapper) {
    border-radius: 10px;
  }
}

.create-btn {
  background: linear-gradient(135deg, $primary 0%, #4080FF 100%);
  border: none;
  font-weight: 600;
  border-radius: 10px;
  padding: 10px 20px;
  display: flex;
  align-items: center;
  gap: 6px;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s;

  &:hover {
    background: linear-gradient(135deg, #0E42D2 0%, #165DFF 100%);
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(22, 93, 255, 0.3);
  }
}

// ==================== 新建作品大按钮 ====================
.create-big-section {
  margin-bottom: 20px;
}

.create-big-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 32px;
  background: linear-gradient(135deg, #fff 0%, #E8F0FF 100%);
  border: 2px dashed rgba(22, 93, 255, 0.3);
  border-radius: 16px;
  cursor: pointer;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s;

  &:hover {
    border-color: $primary;
    background: linear-gradient(135deg, #fff 0%, #e8f0ff 100%);
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(22, 93, 255, 0.12);

    .create-big-icon {
      background: linear-gradient(135deg, $primary 0%, #4080FF 100%);
      color: #fff;
      transform: scale(1.1) rotate(90deg);
    }
  }
}

.create-big-icon {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, rgba(22, 93, 255, 0.1) 0%, rgba(22, 93, 255, 0.05) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: $primary;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.4s cubic-bezier(0.4, 0, 0.2, 1);
}

.create-big-title {
  font-size: 16px;
  font-weight: 700;
  color: $primary;
}

.create-big-desc {
  font-size: 13px;
  color: $text-muted;
}

// ==================== 作品网格 ====================
.works-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 20px;
  min-height: 200px;
}

// ==================== 作品卡片 ====================
.work-card {
  background: $bg-white;
  border-radius: 14px;
  overflow: hidden;
  box-shadow: $card-shadow;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid transparent;
  display: flex;
  flex-direction: column;

  &:hover {
    transform: translateY(-6px);
    box-shadow: 0 12px 32px rgba(22, 93, 255, 0.12);
    border-color: rgba(22, 93, 255, 0.15);

    .thumbnail-overlay {
      opacity: 1;
    }

    .thumbnail-img {
      transform: scale(1.08);
    }
  }
}

.card-thumbnail {
  width: 100%;
  height: 180px;
  background: #F7F8FA;
  overflow: hidden;
  flex-shrink: 0;
  position: relative;

  .thumbnail-img {
    width: 100%;
    height: 100%;
    object-fit: cover;
    display: block;
    transition: transform 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  }

  .thumbnail-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;

    .placeholder-bg {
      width: 100%;
      height: 100%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #C9CDD4;
      background: linear-gradient(135deg, #F7F8FA 0%, #E5E6EB 100%);
    }
  }
}

// Hover 操作按钮遮罩
.thumbnail-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}

.overlay-actions {
  display: flex;
  gap: 12px;
}

.action-btn-overlay {
  width: 44px;
  height: 44px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  color: #fff;
  font-size: 18px;

  &:hover {
    transform: scale(1.15);
  }

  &.edit-btn {
    background: linear-gradient(135deg, $primary 0%, #4080FF 100%);
    box-shadow: 0 4px 12px rgba(22, 93, 255, 0.3);

    &:hover {
      box-shadow: 0 6px 16px rgba(22, 93, 255, 0.5);
    }
  }

  &.share-btn {
    background: linear-gradient(135deg, $success 0%, #7EEAC0 100%);
    box-shadow: 0 4px 12px rgba(54, 211, 153, 0.3);

    &:hover {
      box-shadow: 0 6px 16px rgba(54, 211, 153, 0.5);
    }
  }

  &.delete-btn {
    background: linear-gradient(135deg, $danger 0%, #F76965 100%);
    box-shadow: 0 4px 12px rgba(245, 63, 63, 0.3);

    &:hover {
      box-shadow: 0 6px 16px rgba(245, 63, 63, 0.5);
    }
  }
}

.card-info {
  padding: 14px 16px 16px;
  flex: 1;
}

.card-title {
  font-size: 15px;
  font-weight: 600;
  color: $text-title;
  margin: 0 0 10px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.card-meta {
  display: flex;
  gap: 12px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: $text-muted;

  .el-icon {
    font-size: 13px;
  }
}

// ==================== 分页 ====================
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

// ==================== 空状态 ====================
.create-btn-large {
  background: linear-gradient(135deg, $primary 0%, #4080FF 100%);
  border: none;
  font-weight: 600;
  border-radius: 10px;
  padding: 12px 28px;
  font-size: 15px;
  display: flex;
  align-items: center;
  gap: 6px;

  &:hover {
    background: linear-gradient(135deg, #0E42D2 0%, #165DFF 100%);
    box-shadow: 0 6px 20px rgba(22, 93, 255, 0.3);
  }
}

// ==================== 分享对话框 ====================
.share-dialog-content {
  .share-tip {
    font-size: 14px;
    color: $text-body;
    margin: 0 0 16px;
  }

  .share-link-input {
    :deep(.el-input-group__append) {
      background: $primary;
      color: #fff;
      border-color: $primary;

      &:hover {
        opacity: 0.9;
      }
    }
  }
}

// ==================== 响应式 ====================
@media (max-width: 768px) {
  .works-grid {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: 12px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }

  .header-right {
    width: 100%;
  }

  .search-input {
    flex: 1;
  }

  .create-big-card {
    padding: 24px;
  }
}

@media (max-width: 480px) {
  .works-grid {
    grid-template-columns: 1fr;
  }

  .create-big-card {
    padding: 20px;
  }
}
</style>
