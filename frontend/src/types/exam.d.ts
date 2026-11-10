/**
 * 考试相关类型定义
 */

/** 考试状态 */
export type ExamStatus = 'not_started' | 'in_progress' | 'submitted' | 'graded'

/** 题目类型 */
export type QuestionType = 'choice' | 'fill' | 'code' | 'scratch'

/** 考试信息 */
export interface Exam {
  id: number
  title: string
  description: string
  duration: number
  totalScore: number
  questionCount: number
  status: ExamStatus
  startTime: string
  endTime: string
  createTime: string
}

/** 题目信息 */
export interface Question {
  id: number
  examId: number
  type: QuestionType
  title: string
  options?: string[]
  score: number
  language?: string
  sortOrder: number
}

/** 考试记录 */
export interface ExamRecord {
  id: number
  examId: number
  userId: number
  score: number
  totalScore: number
  passed: boolean
  submitTime: string
  duration: string
}

/** 考试答案详情 */
export interface ExamAnswerDetail {
  questionId: number
  title: string
  userAnswer: string
  correctAnswer: string
  score: number
  maxScore: number
}

/** 考试列表项 */
export interface ExamItem {
  id: number
  title: string
  description: string
  duration: number
  totalScore: number
  questionCount: number
  status: ExamStatus
  startTime: string
  endTime: string
  createTime: string
}

/** 考试答案 */
export interface ExamAnswer {
  questionId: number
  answer: string
}

/** 批改任务 */
export interface GradingTask {
  id: number
  recordId: number
  examId: number
  examTitle: string
  studentId: number
  studentName: string
  status: 'pending' | 'graded'
  score: number
  totalScore: number
  submitTime: string
}
