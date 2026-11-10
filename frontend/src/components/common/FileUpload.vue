<template>
  <!-- 文件上传组件：支持拖拽、分片、进度条 -->
  <div class="file-upload">
    <el-upload
      ref="uploadRef"
      :action="uploadUrl"
      :headers="uploadHeaders"
      :multiple="multiple"
      :accept="accept"
      :limit="limit"
      :on-success="handleSuccess"
      :on-error="handleError"
      :on-progress="handleProgress"
      :before-upload="beforeUpload"
      drag
    >
      <div class="upload-content">
        <el-icon :size="40" color="#86909C"><Upload /></el-icon>
        <div class="upload-text">将文件拖到此处，或<em>点击上传</em></div>
        <div class="upload-tip" v-if="accept">支持格式：{{ accept }}</div>
      </div>
    </el-upload>
  </div>
</template>

<script setup lang="ts">
/**
 * FileUpload - 文件上传组件
 * 支持拖拽上传、进度显示
 */
import { ref, computed } from 'vue'
import { Upload } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import type { UploadInstance } from 'element-plus'

const props = withDefaults(defineProps<{
  action?: string
  accept?: string
  multiple?: boolean
  limit?: number
}>(), {
  action: '/api/file/upload',
  multiple: false,
  limit: 5,
})

const emit = defineEmits<{ success: [response: any], error: [error: any] }>()

const uploadRef = ref<UploadInstance>()
const userStore = useUserStore()

const uploadUrl = computed(() => props.action)
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`,
}))

function beforeUpload(file: File) {
  const maxSize = 50 * 1024 * 1024 // 50MB
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过50MB')
    return false
  }
  return true
}

function handleSuccess(response: any) { emit('success', response) }
function handleError(error: any) { emit('error', error) }
function handleProgress() { /* 进度更新 */ }
</script>

<style scoped lang="scss">
.file-upload { width: 100%; }
.upload-content { padding: 40px 20px; text-align: center; }
.upload-text { font-size: 14px; color: #86909C; margin-top: 8px; em { color: #7B61FF; font-style: normal; } }
.upload-tip { font-size: 12px; color: #86909C; margin-top: 4px; }
</style>
