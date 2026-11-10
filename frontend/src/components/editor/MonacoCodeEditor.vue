<template>
  <!-- Monaco Editor 代码编辑器封装 -->
  <div class="monaco-editor-wrapper" ref="editorContainer">
    <!-- 语言切换 -->
    <div class="editor-toolbar-top">
      <el-select v-model="currentLanguage" size="small" style="width: 120px" @change="handleLanguageChange">
        <el-option label="Python" value="python" />
        <el-option label="C++" value="cpp" />
        <el-option label="GoC" value="goc" />
      </el-select>
      <el-select v-model="fontSize" size="small" style="width: 80px" @change="handleFontSizeChange">
        <el-option label="12" :value="12" />
        <el-option label="14" :value="14" />
        <el-option label="16" :value="16" />
        <el-option label="18" :value="18" />
      </el-select>
    </div>
    <div class="editor-container" ref="monacoRef"></div>
  </div>
</template>

<script setup lang="ts">
/**
 * MonacoCodeEditor - Monaco代码编辑器封装
 * 支持Python/C++/GoC语法高亮
 */
import { ref, onMounted, watch, onBeforeUnmount } from 'vue'

const props = withDefaults(defineProps<{
  modelValue?: string
  language?: string
  height?: number | string
  readOnly?: boolean
}>(), {
  modelValue: '',
  language: 'python',
  height: 400,
  readOnly: false,
})

const emit = defineEmits<{ 'update:modelValue': [value: string] }>()

const editorContainer = ref<HTMLElement>()
const monacoRef = ref<HTMLElement>()
const currentLanguage = ref(props.language)
const fontSize = ref(14)
let editor: any = null

onMounted(() => {
  // 动态加载Monaco Editor
  import('monaco-editor').then(monaco => {
    editor = monaco.editor.create(monacoRef.value!, {
      value: props.modelValue,
      language: currentLanguage.value,
      theme: 'vs-dark',
      fontSize: fontSize.value,
      readOnly: props.readOnly,
      minimap: { enabled: false },
      automaticLayout: true,
      scrollBeyondLastLine: false,
    })
    editor.onDidChangeModelContent(() => {
      emit('update:modelValue', editor.getValue())
    })
  })
})

watch(() => props.modelValue, (val) => {
  if (editor && val !== editor.getValue()) editor.setValue(val)
})

function handleLanguageChange(lang: string) {
  if (editor) { const monaco = (window as any).monaco; monaco.editor.setModelLanguage(editor.getModel(), lang) }
}

function handleFontSizeChange(size: number) {
  if (editor) editor.updateOptions({ fontSize: size })
}

onBeforeUnmount(() => { editor?.dispose() })
</script>

<style scoped lang="scss">
.monaco-editor-wrapper { display: flex; flex-direction: column; border: 1px solid #E5E6EB; border-radius: 8px; overflow: hidden; }
.editor-toolbar-top { display: flex; gap: 8px; padding: 8px 12px; background: #1D2129; border-bottom: 1px solid #4E5969; }
.editor-container { flex: 1; min-height: 200px; }
</style>
