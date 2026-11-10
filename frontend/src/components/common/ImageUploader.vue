<template>
  <!-- 图片上传组件：支持本地上传至OSS、URL输入、预览 -->
  <div class="image-uploader">
    <!-- 图片预览区域 -->
    <div v-if="modelValue" class="image-preview-wrap">
      <div class="image-preview" :style="previewStyle">
        <img :src="displayUrl" alt="preview" @error="handleImageError" />
        <div class="image-overlay">
          <el-button size="small" type="primary" @click="handleReupload">重新上传</el-button>
          <el-button size="small" @click="handleClear">移除</el-button>
        </div>
      </div>
      <div v-if="showUrl" class="image-url">{{ modelValue }}</div>
    </div>

    <!-- 上传区域 -->
    <div v-else class="upload-area">
      <el-upload
        ref="uploadRef"
        :action="uploadAction"
        :headers="uploadHeaders"
        :accept="accept"
        :limit="1"
        :show-file-list="false"
        :before-upload="beforeUpload"
        :on-success="handleSuccess"
        :on-error="handleError"
        :on-progress="handleProgress"
        :http-request="customUpload"
      >
        <div class="upload-trigger">
          <el-icon :size="32" color="#86909C"><Picture /></el-icon>
          <div class="upload-text">
            <span v-if="uploading">上传中 {{ uploadPercent }}%...</span>
            <span v-else>点击上传或拖拽图片</span>
          </div>
          <div class="upload-tip">支持 JPG、PNG、GIF、WEBP，最大 {{ maxSizeMB }}MB</div>
        </div>
      </el-upload>

      <!-- URL输入作为备选 -->
      <div class="url-input-wrap">
        <el-divider>或</el-divider>
        <el-input
          v-model="urlInput"
          placeholder="输入图片URL地址"
          @blur="handleUrlInput"
        >
          <template #append>
            <el-button @click="handleUrlInput">确认</el-button>
          </template>
        </el-input>
      </div>
    </div>

    <!-- 上传进度条 -->
    <el-progress
      v-if="uploading"
      :percentage="uploadPercent"
      :show-text="false"
      :stroke-width="3"
      class="upload-progress"
    />
  </div>
</template>

<script setup lang="ts">
/**
 * ImageUploader - 图片上传组件
 * 支持本地上传至阿里云OSS、URL输入、图片预览
 * 自动使用 storage.ts 中的 OSS URL 生成逻辑
 */
import { ref, computed, watch } from 'vue'
import { Picture } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { UploadInstance, UploadRawFile } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { uploadFileViaServer } from '@/api/oss'
import { getOSSUrl, IMAGE_PRESETS } from '@/utils/storage'

const props = withDefaults(defineProps<{
  /** 当前图片URL */
  modelValue: string
  /** 上传业务类型 */
  type: 'course-cover' | 'news-cover' | 'exam-cover' | 'avatar' | 'team-avatar' | 'partner-logo' | 'banner' | 'scratch'
  /** 关联ID */
  id?: number | string
  /** 最大文件大小(MB) */
  maxSizeMB?: number
  /** 是否显示URL文本 */
  showUrl?: boolean
  /** 预览宽度 */
  previewWidth?: number
  /** 预览高度 */
  previewHeight?: number
  /** 图片压缩预设 */
  preset?: keyof typeof IMAGE_PRESETS
}>(), {
  maxSizeMB: 10,
  showUrl: false,
  previewWidth: 200,
  previewHeight: 150,
  preset: 'list',
})

const emit = defineEmits<{
  'update:modelValue': [url: string]
  success: [url: string]
  error: [error: any]
}>()

const userStore = useUserStore()
const uploadRef = ref<UploadInstance>()
const uploading = ref(false)
const uploadPercent = ref(0)
const urlInput = ref('')
const imageError = ref(false)

/** 预览样式 */
const previewStyle = computed(() => ({
  width: `${props.previewWidth}px`,
  height: `${props.previewHeight}px`,
}))

/** 显示用的URL（自动处理OSS路径） */
const displayUrl = computed(() => {
  if (!props.modelValue) return ''
  if (imageError.value) return ''
  // 如果是完整URL直接返回，否则通过OSS工具生成
  return getOSSUrl(props.modelValue, {
    width: props.previewWidth,
    height: props.previewHeight,
    quality: 85,
  })
})

/** 上传地址 */
const uploadAction = computed(() => '/api/oss/upload')

/** 上传请求头 */
const uploadHeaders = computed(() => ({
  Authorization: `Bearer ${userStore.token}`,
}))

/** 允许的文件类型 */
const accept = 'image/jpeg,image/png,image/gif,image/webp'

/** 上传前校验 */
function beforeUpload(file: UploadRawFile) {
  const maxSize = props.maxSizeMB * 1024 * 1024
  if (file.size > maxSize) {
    ElMessage.error(`图片大小不能超过 ${props.maxSizeMB}MB`)
    return false
  }

  const validTypes = ['image/jpeg', 'image/png', 'image/gif', 'image/webp']
  if (!validTypes.includes(file.type)) {
    ElMessage.error('仅支持 JPG、PNG、GIF、WEBP 格式')
    return false
  }

  uploading.value = true
  uploadPercent.value = 0
  imageError.value = false
  return true
}

/** 自定义上传逻辑（直传OSS） */
async function customUpload(options: any) {
  try {
    const res = await uploadFileViaServer(
      options.file,
      { type: props.type, id: props.id },
      (percent) => {
        uploadPercent.value = percent
      }
    )

    if (res.code === 200 && res.data) {
      options.onSuccess(res.data)
    } else {
      options.onError(new Error(res.message || '上传失败'))
    }
  } catch (error) {
    options.onError(error)
  }
}

/** 上传成功 */
function handleSuccess(response: any) {
  uploading.value = false
  uploadPercent.value = 100

  const url = response.url || response.objectKey || ''
  if (url) {
    emit('update:modelValue', url)
    emit('success', url)
    ElMessage.success('上传成功')
  }

  // 清空上传组件
  uploadRef.value?.clearFiles()
}

/** 上传失败 */
function handleError(error: any) {
  uploading.value = false
  uploadPercent.value = 0
  emit('error', error)
  ElMessage.error('上传失败，请重试')
}

/** 上传进度 */
function handleProgress(event: any) {
  if (event.percent) {
    uploadPercent.value = Math.round(event.percent)
  }
}

/** URL输入确认 */
function handleUrlInput() {
  const url = urlInput.value.trim()
  if (!url) return

  if (!url.startsWith('http://') && !url.startsWith('https://')) {
    ElMessage.warning('请输入完整的URL地址（以 http:// 或 https:// 开头）')
    return
  }

  emit('update:modelValue', url)
  emit('success', url)
  urlInput.value = ''
}

/** 重新上传 */
function handleReupload() {
  emit('update:modelValue', '')
  imageError.value = false
}

/** 清除图片 */
function handleClear() {
  emit('update:modelValue', '')
  imageError.value = false
}

/** 图片加载失败 */
function handleImageError() {
  imageError.value = true
}

// 监听modelValue变化，同步到urlInput
watch(() => props.modelValue, (val) => {
  if (!val) {
    urlInput.value = ''
    imageError.value = false
  }
})
</script>

<style scoped lang="scss">
.image-uploader {
  width: 100%;
}

.image-preview-wrap {
  .image-preview {
    position: relative;
    border-radius: 8px;
    overflow: hidden;
    border: 1px solid #E5E6EB;
    background: #F7F8FA;

    img {
      width: 100%;
      height: 100%;
      object-fit: cover;
      display: block;
    }

    .image-overlay {
      position: absolute;
      inset: 0;
      background: rgba(0, 0, 0, 0.5);
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 12px;
      opacity: 0;
      transition: opacity 0.3s ease;
    }

    &:hover .image-overlay {
      opacity: 1;
    }
  }

  .image-url {
    margin-top: 8px;
    font-size: 12px;
    color: #86909C;
    word-break: break-all;
    line-height: 1.5;
  }
}

.upload-area {
  :deep(.el-upload) {
    width: 100%;
  }

  :deep(.el-upload-dragger) {
    width: 100%;
    padding: 24px;
    border: 2px dashed #E5E6EB;
    border-radius: 8px;
    background: #F7F8FA;
    transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s ease;

    &:hover {
      border-color: #165DFF;
      background: #E8F0FF;
    }
  }
}

.upload-trigger {
  text-align: center;

  .upload-text {
    margin-top: 8px;
    font-size: 14px;
    color: #86909C;
  }

  .upload-tip {
    margin-top: 4px;
    font-size: 12px;
    color: #86909C;
  }
}

.url-input-wrap {
  margin-top: 12px;

  :deep(.el-divider) {
    margin: 12px 0;

    .el-divider__text {
      font-size: 12px;
      color: #86909C;
    }
  }
}

.upload-progress {
  margin-top: 8px;
}
</style>
