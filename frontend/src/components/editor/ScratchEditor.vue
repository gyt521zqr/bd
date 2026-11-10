<template>
  <!-- Scratch3 iframe 嵌入封装，postMessage 通信 -->
  <div class="scratch-editor">
    <div class="scratch-toolbar">
      <span class="editor-label">Scratch 编辑器</span>
      <el-button size="small" type="primary" @click="handleSave">保存作品</el-button>
    </div>
    <iframe
      ref="scratchIframe"
      :src="scratchUrl"
      frameborder="0"
      class="scratch-iframe"
      @load="handleIframeLoad"
    ></iframe>
  </div>
</template>

<script setup lang="ts">
/**
 * ScratchEditor - Scratch3编辑器封装
 * 通过iframe嵌入，postMessage通信
 */
import { ref } from 'vue'
import { ElMessage } from 'element-plus'

const props = withDefaults(defineProps<{
  src?: string
}>(), {
  src: '/scratch/index.html',
})

const emit = defineEmits<{ save: [data: any], load: [data: any] }>()

const scratchIframe = ref<HTMLIFrameElement>()
const scratchUrl = ref(props.src)

function handleIframeLoad() {
  // iframe加载完成后初始化通信
  scratchIframe.value?.contentWindow?.postMessage({ type: 'init' }, '*')
}

function handleSave() {
  scratchIframe.value?.contentWindow?.postMessage({ type: 'getProject' }, '*')
  ElMessage.success('作品已保存')
}

// 监听来自iframe的消息
if (typeof window !== 'undefined') {
  window.addEventListener('message', (event) => {
    if (event.data?.type === 'projectData') {
      emit('save', event.data.data)
    }
  })
}
</script>

<style scoped lang="scss">
.scratch-editor { display: flex; flex-direction: column; border: 1px solid #E5E6EB; border-radius: 8px; overflow: hidden; }
.scratch-toolbar { display: flex; justify-content: space-between; align-items: center; padding: 8px 12px; background: #FF7D00; color: #fff; }
.editor-label { font-weight: 600; font-size: 14px; }
.scratch-iframe { width: 100%; height: 500px; border: none; }
</style>
