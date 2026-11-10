<template>
  <!-- 确认弹窗组件 -->
  <el-dialog
    v-model="visible"
    :title="title"
    :width="width"
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    :show-close="false"
    class="confirm-dialog"
  >
    <div class="confirm-content">
      <el-icon :size="48" :color="type === 'warning' ? '#FF9500' : type === 'danger' ? '#F53F3F' : '#7B61FF'">
        <WarningFilled v-if="type === 'warning'" />
        <CircleCloseFilled v-else-if="type === 'danger'" />
        <QuestionFilled v-else />
      </el-icon>
      <p>{{ message }}</p>
    </div>
    <template #footer>
      <el-button @click="handleCancel">取消</el-button>
      <el-button :type="type === 'danger' ? 'danger' : 'primary'" @click="handleConfirm" :loading="loading">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup lang="ts">
/**
 * ConfirmDialog - 确认弹窗组件
 */
import { WarningFilled, CircleCloseFilled, QuestionFilled } from '@element-plus/icons-vue'

const visible = defineModel<boolean>({ default: false })

withDefaults(defineProps<{
  title?: string
  message: string
  type?: 'warning' | 'danger' | 'info'
  width?: string
  loading?: boolean
}>(), {
  title: '提示',
  type: 'warning',
  width: '420px',
  loading: false,
})

const emit = defineEmits<{ confirm: [], cancel: [] }>()

function handleConfirm() { emit('confirm') }
function handleCancel() { visible.value = false; emit('cancel') }
</script>

<style scoped lang="scss">
.confirm-content { text-align: center; padding: 20px 0; p { font-size: 15px; color: #4E5969; margin-top: 12px; } }
</style>
