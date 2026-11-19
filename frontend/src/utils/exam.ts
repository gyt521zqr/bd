/**
 * 考试相关工具函数与常量（纯工具，不含任何题目数据）
 * 从原 @/mock/examQuestions 迁移而来，供做题页 / 试卷预览 / 错题本共用。
 */
import type { ExamQuestion, QuestionType } from '@/types'

/** 题型中文标签 */
export const QUESTION_TYPE_LABEL: Record<QuestionType, string> = {
  choice: '选择题',
  fill: '填空题',
  judge: '判断题',
  code: '编程题',
  scratch: 'Scratch题',
  scratchjr: 'ScratchJr题',
}

/** 题型展示顺序（用于分节） */
export const QUESTION_TYPE_ORDER: QuestionType[] = [
  'choice',
  'fill',
  'judge',
  'code',
  'scratch',
  'scratchjr',
]

/** 将题集按题型分节 */
export function groupQuestionsByType(
  questions: ExamQuestion[],
): { type: QuestionType; label: string; items: ExamQuestion[] }[] {
  const buckets: Record<string, ExamQuestion[]> = {}
  for (const q of questions) {
    ;(buckets[q.type] ||= []).push(q)
  }
  return QUESTION_TYPE_ORDER
    .filter((t) => buckets[t]?.length)
    .map((t) => ({ type: t, label: QUESTION_TYPE_LABEL[t], items: buckets[t] }))
}

/** 计算试卷总分 */
export function calcTotalScore(questions: ExamQuestion[]): number {
  return questions.reduce((sum, q) => sum + (q.score || 0), 0)
}
