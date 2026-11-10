/**
 * 提交校验 Hook
 * 统一处理代码/Scratch 项目提交前的校验逻辑
 */
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import type { EditorLang, EditorSubmitDTO, JudgeResult } from '@/types/editor'
import { submitJudge, getJudgeResult } from '@/api/judge'

export interface UseSubmitValidationOptions {
  /** 题目ID */
  questionId: number
  /** 编程语言 */
  lang: EditorLang
  /** 考试ID（考试模式） */
  examId?: number
  /** 是否考试模式 */
  isExam?: boolean
  /** 提交成功回调 */
  onSuccess?: (result: JudgeResult) => void
  /** 提交失败回调 */
  onError?: (error: string) => void
}

export function useSubmitValidation(options: UseSubmitValidationOptions) {
  const {
    questionId,
    lang,
    examId,
    isExam = false,
    onSuccess,
    onError,
  } = options

  /** 是否正在提交 */
  const submitting = ref(false)
  /** 判题结果 */
  const judgeResult = ref<JudgeResult | null>(null)
  /** 判题轮询定时器 */
  let pollTimer: ReturnType<typeof setInterval> | null = null

  /** 校验代码/项目是否可以提交 */
  function validate(code?: string, scratchProject?: { json: string; assets?: string[] }): string | null {
    // Scratch / ScratchJr 项目校验
    if (lang === 'scratch3' || lang === 'scratchjr') {
      if (!scratchProject?.json) {
        return lang === 'scratchjr' ? '请先完成 ScratchJr 项目再提交' : '请先完成 Scratch 项目再提交'
      }
      try {
        JSON.parse(scratchProject.json)
      } catch {
        return '项目数据格式错误'
      }
      return null
    }

    // 代码类语言校验
    if (!code || !code.trim()) {
      return '请先编写代码再提交'
    }

    // 检查代码长度
    if (code.length > 65535) {
      return '代码长度超过限制（最大 65535 字符）'
    }

    return null
  }

  /** 提交判题 */
  async function submit(code?: string, scratchProject?: { json: string; assets?: string[]; cover?: string }) {
    // 校验
    const error = validate(code, scratchProject)
    if (error) {
      ElMessage.warning(error)
      onError?.(error)
      return
    }

    submitting.value = true
    judgeResult.value = null

    try {
      const isScratchLang = lang === 'scratch3' || lang === 'scratchjr'
      const dto: EditorSubmitDTO = {
        questionId,
        lang,
        code: !isScratchLang ? code : undefined,
        scratchProject: isScratchLang ? scratchProject : undefined,
        examId,
        isExam,
      }

      const res = await submitJudge(dto)
      const recordId = res.data

      // 开始轮询判题结果
      pollResult(recordId)
    } catch (err: any) {
      submitting.value = false

      const msg = err?.message || '提交失败，请稍后重试'
      ElMessage.error(msg)
      onError?.(msg)
    }
  }

  /** 轮询判题结果 */
  function pollResult(recordId: number) {
    if (pollTimer) clearInterval(pollTimer)

    pollTimer = setInterval(async () => {
      try {
        const res = await getJudgeResult(recordId)
        const result = res.data

        // 判题完成
        if (result.status !== 'PENDING' && result.status !== 'RUNNING') {
          if (pollTimer) {
            clearInterval(pollTimer)
            pollTimer = null
          }
          judgeResult.value = result
          submitting.value = false

          if (result.status === 'ACCEPTED') {
            ElMessage.success(`提交成功！通过 ${result.passedCases}/${result.totalCases} 个测试用例`)
          } else {
            ElMessage.warning(`未通过：${result.errorMessage || '请检查代码'}`)
          }

          onSuccess?.(result)
        }
      } catch {
        // 轮询失败，继续
      }
    }, 2000)

    // 超时保护：60 秒后停止轮询
    setTimeout(() => {
      if (pollTimer) {
        clearInterval(pollTimer)
        pollTimer = null
        submitting.value = false
        ElMessage.error('判题超时，请稍后查看结果')
      }
    }, 60000)
  }

  /** 停止轮询 */
  function stopPoll() {
    if (pollTimer) {
      clearInterval(pollTimer)
      pollTimer = null
    }
    submitting.value = false
  }

  return {
    submitting,
    judgeResult,
    submit,
    validate,
    stopPoll,
  }
}
