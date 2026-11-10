<template>
  <!-- 代码对比查看器 -->
  <div class="code-diff-viewer">
    <div class="diff-header">
      <span class="diff-title">代码对比</span>
      <div class="diff-actions">
        <el-button size="small" text @click="toggleView">
          {{ sideBySide ? '统一视图' : '并排视图' }}
        </el-button>
      </div>
    </div>
    <div class="diff-content" :class="{ 'side-by-side': sideBySide }">
      <div class="diff-pane">
        <div class="pane-label">你的代码</div>
        <pre class="code-content">{{ userCode }}</pre>
      </div>
      <div class="diff-pane" v-if="sideBySide">
        <div class="pane-label">参考答案</div>
        <pre class="code-content">{{ referenceCode }}</pre>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * CodeDiffViewer - 代码对比查看器
 * 并排或统一视图对比用户代码和参考答案
 */
import { ref } from 'vue'

withDefaults(defineProps<{
  userCode?: string
  referenceCode?: string
}>(), {
  userCode: '',
  referenceCode: '',
})

const sideBySide = ref(true)

function toggleView() { sideBySide.value = !sideBySide.value }
</script>

<style scoped lang="scss">
.code-diff-viewer { border: 1px solid #E5E6EB; border-radius: 8px; overflow: hidden; }
.diff-header { display: flex; justify-content: space-between; align-items: center; padding: 8px 12px; background: #F7F8FA; border-bottom: 1px solid #E5E6EB; }
.diff-title { font-size: 14px; font-weight: 600; color: #1D2129; }
.diff-content { display: flex; }
.diff-content:not(.side-by-side) { flex-direction: column; }
.diff-pane { flex: 1; min-width: 0; }
.pane-label { padding: 4px 12px; font-size: 12px; color: #86909C; background: #F7F8FA; border-bottom: 1px solid #E5E6EB; }
.code-content { padding: 12px; font-family: 'Consolas', monospace; font-size: 13px; color: #4E5969; margin: 0; white-space: pre-wrap; overflow-x: auto; }
</style>
