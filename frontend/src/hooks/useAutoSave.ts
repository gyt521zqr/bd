/**
 * 自动保存草稿 Hook
 * 定时将编辑器内容保存到后端，支持代码和 Scratch 项目
 */
import { ref, onMounted, onUnmounted } from 'vue'
import type { EditorLang, DraftData } from '@/types/editor'
import { saveDraft, loadDraft } from '@/api/judge'

export interface UseAutoSaveOptions {
  /** 题目ID */
  questionId: number
  /** 编程语言 */
  lang: EditorLang
  /** 是否考试模式（考试模式下不自动保存） */
  isExam?: boolean
  /** 自动保存间隔（毫秒），默认 30 秒 */
  interval?: number
}

export function useAutoSave(options: UseAutoSaveOptions) {
  const {
    questionId,
    lang,
    isExam = false,
    interval = 30000,
  } = options

  /** 当前代码/项目数据 */
  const content = ref<string>('')
  /** 是否正在保存 */
  const saving = ref(false)
  /** 最后保存时间 */
  const lastSavedAt = ref<string>('')
  /** 是否有未保存的更改 */
  const hasUnsavedChanges = ref(false)

  let timer: ReturnType<typeof setInterval> | null = null

  /** 加载草稿 */
  async function load() {
    try {
      const res = await loadDraft({ questionId, lang })
      if (res.data) {
        const draft = res.data as DraftData
        content.value = draft.code || draft.scratchJson || ''
        lastSavedAt.value = draft.updatedAt
        hasUnsavedChanges.value = false
        return content.value
      }
    } catch {
      // 草稿不存在或加载失败，忽略
    }
    return null
  }

  /** 保存草稿 */
  async function save(code?: string) {
    if (code !== undefined) {
      content.value = code
    }
    if (!content.value || isExam) return

    saving.value = true
    try {
      await saveDraft({
        questionId,
        lang,
        code: lang !== 'scratch3' ? content.value : undefined,
        scratchJson: lang === 'scratch3' ? content.value : undefined,
      })
      lastSavedAt.value = new Date().toISOString()
      hasUnsavedChanges.value = false
    } catch {
      // 保存失败，标记为未保存
      hasUnsavedChanges.value = true
    } finally {
      saving.value = false
    }
  }

  /** 启动自动保存定时器 */
  function startAutoSave() {
    if (timer) clearInterval(timer)
    timer = setInterval(() => {
      if (hasUnsavedChanges.value && content.value) {
        save()
      }
    }, interval)
  }

  /** 停止自动保存 */
  function stopAutoSave() {
    if (timer) {
      clearInterval(timer)
      timer = null
    }
  }

  /** 标记内容变更 */
  function markDirty(code: string) {
    content.value = code
    hasUnsavedChanges.value = true
  }

  onMounted(() => {
    if (!isExam) {
      startAutoSave()
    }
  })

  onUnmounted(() => {
    stopAutoSave()
    // 组件卸载时如果有未保存的更改，尝试保存
    if (hasUnsavedChanges.value && content.value && !isExam) {
      save()
    }
  })

  return {
    content,
    saving,
    lastSavedAt,
    hasUnsavedChanges,
    load,
    save,
    markDirty,
    startAutoSave,
    stopAutoSave,
  }
}
