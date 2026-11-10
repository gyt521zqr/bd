<template>
  <!-- ScratchJr iframe 嵌入封装 -->
  <div class="scratchjr-editor">
    <div class="scratchjr-toolbar">
      <span class="editor-label">ScratchJr 编辑器</span>
      <el-button size="small" type="primary" @click="handleSave">保存</el-button>
    </div>
    <iframe
      ref="iframeRef"
      :src="editorUrl"
      frameborder="0"
      class="scratchjr-iframe"
    ></iframe>
  </div>
</template>

<script setup lang="ts">
/**
 * ScratchJrEditor - ScratchJr编辑器封装
 * 适合低龄儿童的图形化编程
 */
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

withDefaults(defineProps<{ src?: string }>(), { src: '/scratchjr/index.html' })

defineEmits<{ save: [data: any] }>()
const iframeRef = ref<HTMLIFrameElement>()
const editorUrl = ref('/scratchjr/index.html')

function handleSave() {
  iframeRef.value?.contentWindow?.postMessage({ type: 'save' }, '*')
  ElMessage.success('作品已保存')
}
</script>

<style scoped lang="scss">
.scratchjr-editor { display: flex; flex-direction: column; border: 1px solid #E5E6EB; border-radius: 8px; overflow: hidden; }
.scratchjr-toolbar { display: flex; justify-content: space-between; align-items: center; padding: 8px 12px; background: #4080FF; color: #fff; }
.editor-label { font-weight: 600; font-size: 14px; }
.scratchjr-iframe { width: 100%; height: 500px; border: none; }
</style>
