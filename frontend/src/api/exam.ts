/**
 * 考试相关 API 接口
 */
import request from './index'
import type { Result, PageResult, PageQuery, ExamItem, ExamRecord, GradingTask, ExamQuestion, ExamPaper, ExamResultDetail } from '@/types'

/** 获取考试列表 */
export function getExamList(params: PageQuery & { status?: string }) {
  return request.get<any, Result<PageResult<ExamItem>>>('/exam/list', { params })
}

/** 进入考试（获取考试记录，后端返回 ExamRecord，其 id 即 recordId） */
export function enterExam(examId: number) {
  return request.post<any, Result<{ id: number; examId: number; status: number; [key: string]: any }>>(`/exam/${examId}/enter`)
}

/** 保存答案条目 */
export interface ExamAnswerSaveItem {
  questionId: number
  userAnswer?: string
  code?: string
  language?: number
}

/** 保存考试答案 */
export function saveExamAnswer(examId: number, recordId: number, answers: ExamAnswerSaveItem[]) {
  return request.post<any, Result<null>>(`/exam/${examId}/answer`, answers, { params: { recordId } })
}

/** 提交试卷 */
export function submitExam(examId: number, recordId: number) {
  return request.post<any, Result<{ id: number; score: number; isPassed: number }>>(`/exam/${examId}/submit`, null, { params: { recordId } })
}

/** 获取考试成绩 */
export function getExamResult(recordId: number) {
  return request.get<any, Result<ExamRecord>>(`/exam/result/${recordId}`)
}

/** 获取考试记录列表 */
export function getExamRecords(params: PageQuery) {
  return request.get<any, Result<PageResult<ExamRecord>>>('/exam/records', { params })
}

/** 查询考试成绩（通过参赛号+姓名） */
export function queryExamResult(params: { registerNo: string; name: string }) {
  return request.get<any, Result<any>>('/exam/result/query', { params })
}

/** 上报切屏事件 */
export function reportSwitchScreen(examId: number) {
  return request.post<any, Result<null>>(`/exam/${examId}/switch-screen`)
}

/** 获取批改任务列表（教师/管理员） */
export function getGradingTasks(params: PageQuery & { status?: string }) {
  return request.get<any, Result<PageResult<GradingTask>>>('/exam/grading/tasks', { params })
}

/** 批改试卷 */
export function gradeExam(data: { recordId: number; questionId: number; score: number; comment?: string }) {
  return request.post<any, Result<null>>('/exam/grading/grade', data)
}

/**
 * 题库单源：仅从后端拉取试卷题目，失败直接抛错（不再回退任何本地假题）。
 * 后端端点：GET /exam/paper/{paperId}/questions -> Result<ExamQuestion[]>
 * 返回结构与前端 ExamQuestion 一致（options 为数组、templateCode 为字符串）。
 */
export async function fetchPaperQuestions(paperId: number): Promise<ExamQuestion[]> {
  const res = await request.get<any, Result<ExamQuestion[]>>(`/exam/paper/${paperId}/questions`)
  if (res?.code === 200 && Array.isArray(res.data)) {
    return res.data
  }
  throw new Error('试卷题目获取失败')
}

/** 试卷列表（题库中心，真实数据） */
export function getPaperList() {
  return request.get<any, Result<ExamPaper[]>>('/exam/paper/list')
}

/** 逐题答题详情（成绩页，含标准答案与解析，仅本人可查） */
export function getExamResultDetail(recordId: number) {
  return request.get<any, Result<ExamResultDetail[]>>(`/exam/result/${recordId}/detail`)
}
