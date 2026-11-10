<template>
  <div class="editor-page">
    <!-- 顶部条：返回 + 标题 + 语言 -->
    <div class="editor-page-header">
      <el-button text :icon="ArrowLeft" @click="router.back()">返回</el-button>
      <span class="editor-page-title">{{ pageTitle }}</span>
      <el-tag v-if="langLabel" size="small" effect="plain" class="editor-page-lang">{{ langLabel }}</el-tag>
    </div>

    <div class="editor-page-body">
      <!-- Scratch 类（积木）：iframe 编辑器，自带保存 -->
      <ScratchEditor v-if="isScratch" @save="onScratchSave" />

      <!-- 代码类（Python / C++ / GoC 等）：通用编辑器外壳 -->
      <BaseEditor
        v-else
        :config="editorConfig"
        :initial-code="initialCode"
        @submit-success="onCodeSubmit"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * EditorPage - 独立创作编辑器页（恢复被删除的 /practice/editor、/practice/scratch/create）
 * 通过路由 query 控制：
 *   ?lang=python|scratch|goc|cpp  创作语言（映射到 EditorLang）
 *   ?mode=create                   创作模式（默认）
 *   ?work=<id> | ?workId=<id>     编辑既有作品（Scratch 用于回填标题）
 */
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import BaseEditor from '@/components/editor/BaseEditor.vue'
import ScratchEditor from '@/components/editor/ScratchEditor.vue'
import type { EditorConfig, EditorLang } from '@/types/editor'
import { LANG_LABELS } from '@/types/editor'
import { loadWork, saveWork } from '@/api/scratch'

const route = useRoute()
const router = useRouter()

/** 社区页 / 作品页传入的 lang 取值 → 统一 EditorLang */
const LANG_MAP: Record<string, EditorLang> = {
  python: 'python',
  pythonturtle: 'pythonturtle',
  cpp: 'cpp',
  goc: 'goc',
  scratch: 'scratch3',
  scratch3: 'scratch3',
  scratchjr: 'scratchjr',
}

const rawLang = computed(() => String(route.query.lang || 'python'))
const editorLang = computed<EditorLang>(() => LANG_MAP[rawLang.value] || 'python')
const isScratch = computed(() => editorLang.value === 'scratch3' || editorLang.value === 'scratchjr')
const langLabel = computed(() => LANG_LABELS[editorLang.value])

/** 既有作品 ID（兼容 work / workId 两种参数名） */
const workId = computed<number | undefined>(() => {
  const v = route.query.work ?? route.query.workId
  if (v == null) return undefined
  const n = Number(v)
  return Number.isFinite(n) && n > 0 ? n : undefined
})

const pageTitle = computed(() =>
  workId.value != null ? '继续编辑作品' : '创作新作品'
)

// 代码类编辑器配置（创作模式走 practice，展示运行/草稿/提交）
const editorConfig = computed<EditorConfig>(() => ({
  lang: editorLang.value,
  mode: 'practice',
  questionId: workId.value ?? 0,
}))
const initialCode = ref<string | undefined>(undefined)

// Scratch 标题（编辑既有作品时回填）
const scratchTitle = ref('我的 Scratch 作品')

onMounted(async () => {
  if (isScratch.value && workId.value != null) {
    try {
      const res = await loadWork(workId.value)
      if (res?.data?.title) scratchTitle.value = res.data.title
    } catch {
      // 加载失败不影响编辑器打开
    }
  }
})

// ========== 保存回调 ==========
function onScratchSave(data: unknown) {
  saveWork({
    id: workId.value,
    title: scratchTitle.value,
    projectJson: typeof data === 'string' ? data : JSON.stringify(data),
  })
    .then(() => ElMessage.success('作品已保存到社区'))
    .catch(() => ElMessage.error('保存失败，请稍后重试'))
}

function onCodeSubmit() {
  ElMessage.success('代码已提交')
}
</script>

<style scoped lang="scss">
.editor-page {
  display: flex;
  flex-direction: column;
  min-height: 100vh;
  background: #0d1117;
}

.editor-page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 16px;
  background: #161b22;
  border-bottom: 1px solid #30363d;
  color: #e6edf3;

  .editor-page-title {
    font-size: 15px;
    font-weight: 600;
  }

  .editor-page-lang {
    margin-left: auto;
  }

  :deep(.el-button.is-text) {
    color: #e6edf3;
  }
}

.editor-page-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}
</style>
