/**
 * 错题本工具（localStorage 持久化）
 * 交卷时自动收录答错的客观题，答对后自动移除，可跨会话保留。
 */
import type { ExamQuestion } from '@/types'

/** 错题本条目 */
export interface WrongBookItem {
  question: ExamQuestion
  sourcePaperId: number
  addedAt: number
}

const KEY = 'wrong-question-bank'

/** 读取错题本 */
export function getWrongBook(): WrongBookItem[] {
  try {
    const raw = localStorage.getItem(KEY)
    if (!raw) return []
    const parsed = JSON.parse(raw)
    return Array.isArray(parsed) ? parsed : []
  } catch {
    return []
  }
}

/** 写入错题本 */
function saveWrongBook(items: WrongBookItem[]) {
  try {
    localStorage.setItem(KEY, JSON.stringify(items))
  } catch {
    // localStorage 不可用（如隐私模式）时静默忽略
  }
}

/** 批量加入错题（按题目 id 去重） */
export function addToWrongBook(questions: ExamQuestion[], sourcePaperId: number) {
  if (!questions || questions.length === 0) return
  const book = getWrongBook()
  const existingIds = new Set(book.map((i) => i.question.id))
  for (const q of questions) {
    if (q && !existingIds.has(q.id)) {
      book.push({ question: q, sourcePaperId, addedAt: Date.now() })
      existingIds.add(q.id)
    }
  }
  saveWrongBook(book)
}

/** 批量移除错题（答对后调用） */
export function removeFromWrongBook(questionIds: number[]) {
  if (!questionIds || questionIds.length === 0) return
  const ids = new Set(questionIds)
  const book = getWrongBook().filter((i) => !ids.has(i.question.id))
  saveWrongBook(book)
}

/** 清空错题本 */
export function clearWrongBook() {
  try {
    localStorage.removeItem(KEY)
  } catch {
    // 忽略
  }
}
