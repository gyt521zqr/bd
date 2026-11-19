/**
 * 考试相关类型定义
 */
import type { EditorLang } from './editor'

/** 考试状态 */
export type ExamStatus = 'not_started' | 'in_progress' | 'submitted' | 'graded'

/** 题目类型 */
export type QuestionType = 'choice' | 'fill' | 'judge' | 'code' | 'scratch' | 'scratchjr'

/** 完整题目（含答案/解析/模板代码，供做题页与试卷预览使用） */
export interface ExamQuestion {
  id: number
  type: QuestionType
  title: string
  options?: string[]
  /** 正确答案（选择题 A/B/C/D；判断题 true/false；填空题答案文本） */
  answer?: string
  /** 试题解析 */
  analysis?: string
  score: number
  language?: EditorLang
  templateCode?: string
}

/** 试卷（题库中心展示用，对应后端 exam_paper 表） */
export interface ExamPaper {
  id: number
  /** 试卷名称 */
  paperName: string
  /** 考试ID */
  examId?: number
  /** 总分 */
  totalScore?: number
  /** 及格分 */
  passScore?: number
  /** 题目数量 */
  questionCount?: number
  /** 创建时间 */
  createTime?: string
}

/** 考试元信息（标题/时长/及格分） */
export interface ExamMeta {
  id: number
  title: string
  duration: number
  passScore: number
}

/** 逐题答题详情（成绩页，来自后端判分结果） */
export interface ExamResultDetail {
  questionId: number
  type: string
  title: string
  options?: string[]
  userAnswer: string
  correctAnswer: string
  analysis?: string
  score: number
  maxScore: number
  isCorrect?: number
  gradeStatus?: number
  language?: string
  code?: string
}

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

/** 考试记录（对齐后端 ExamRecordVO，含考试标题/总分/及格分） */
export interface ExamRecord {
  id: number
  examId: number
  userId: number
  paperId?: number
  startTime?: string
  submitTime?: string
  /** 考试状态（0-未开始，1-答题中，2-已交卷，3-已批改） */
  status?: number
  /** 得分 */
  score?: number
  /** 是否及格（0-不及格，1-及格） */
  isPassed?: number
  switchScreenCount?: number
  leavePageCount?: number
  violationFlag?: number
  createTime?: string
  /** 考试标题（ExamRecordVO 关联 exam_info 补齐） */
  examTitle?: string
  /** 试卷总分 */
  totalScore?: number
  /** 及格分 */
  passScore?: number
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
  /** 及格分（后端 ExamInfo 有，可能未下发） */
  passScore?: number
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
