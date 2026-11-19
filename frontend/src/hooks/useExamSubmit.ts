/**
 * 交卷流程 composable
 * 统一处理：确认 → 收集答案 → 保存答案 → 提交后端（失败降级本地交卷）→ 更新 store 状态
 * 供 ExamLayout（顶部交卷按钮）与 ExamTakingPage（提交试卷按钮）共用
 */
import { useExamStore } from '@/stores/exam'
import { saveExamAnswer, submitExam } from '@/api/exam'
import { addToWrongBook, removeFromWrongBook } from '@/utils/wrongBook'
import { ElMessageBox } from 'element-plus'

export function useExamSubmit() {
  const examStore = useExamStore()

  /**
   * 交卷后同步错题本：答错客观题入本，答对（曾错过）的题移除
   *
   * 仅「错题重练」模式（overrideQuestions 来自错题本、含标准答案）可前端比对；
   * 正式考试题目来自后端（不含答案），对错由后端判分，前端不再自行比对。
   */
  function syncWrongBook(examId: number) {
    const questions = examStore.overrideQuestions || []
    if (questions.length === 0) return
    const submitted = examStore.submittedAnswers
    const wrongQuestions: any[] = []
    const correctIds: number[] = []
    for (const q of questions) {
      // 编程题 / Scratch 题无标准答案，不纳入错题本
      if (!q?.answer) continue
      const userRaw = submitted.get(q.id) || ''
      if (userRaw === q.answer) {
        correctIds.push(q.id)
      } else {
        wrongQuestions.push(q)
      }
    }
    addToWrongBook(wrongQuestions, examId)
    removeFromWrongBook(correctIds)
  }

  /**
   * 执行交卷
   * @returns true 表示已交卷（后端成功或降级本地成功），false 表示用户取消
   */
  async function submit(examId: number): Promise<boolean> {
    // 确认弹窗
    try {
      await ElMessageBox.confirm('确定要提交试卷吗？提交后无法修改。', '交卷确认', {
        confirmButtonText: '确定交卷',
        cancelButtonText: '继续答题',
        type: 'warning',
      })
    } catch {
      return false
    }

    // 收集答案（questionId -> userAnswer）
    const answers = Array.from(examStore.answers.entries()).map(([questionId, answer]) => ({
      questionId,
      userAnswer: answer,
    }))

    // 有 recordId 时优先后端落库提交；失败则降级本地交卷
    const recordId = examStore.recordId
    if (recordId) {
      try {
        await saveExamAnswer(examId, recordId, answers)
        await submitExam(examId, recordId)
      } catch (e) {
        console.warn('[useExamSubmit] 后端提交失败，降级为本地交卷', e)
      }
    }

    examStore.submitExam()

    // 同步错题本（答错入本、答对移除）
    try {
      syncWrongBook(examId)
    } catch (e) {
      console.warn('[useExamSubmit] 错题本同步失败', e)
    }

    return true
  }

  return { submit }
}
