/**
 * 鐝骇绠＄悊鐩稿叧 API 鎺ュ彛
 */
import request from './index'
import type { Result, PageResult, PageQuery } from '@/types'

/** 鐝骇淇℃伅 */
export interface ClassItem {
  id: number
  name: string
  teacherName: string
  studentCount: number
  maxStudents: number
  createTime: string
  status: number
}

/** 瀛︾敓淇℃伅 */
export interface StudentItem {
  id: number
  name: string
  avatar: string
  phone: string
  parentPhone: string
  joinTime: string
}

/** 浣滀笟淇℃伅 */
export interface HomeworkItem {
  id: number
  title: string
  description: string
  deadline: string
  status: 'pending' | 'submitted' | 'graded'
  score?: number
}

/** 鑾峰彇鐝骇鍒楄〃 */
export function getClassList(params: PageQuery) {
  return request.get<any, Result<PageResult<ClassItem>>>('/class/list', { params })
}

/** 鑾峰彇鐝骇璇︽儏 */
export function getClassDetail(classId: number) {
  return request.get<any, Result<ClassItem>>(`/class/detail/${classId}`)
}

/** 鑾峰彇鐝骇瀛︾敓鍒楄〃 */
export function getClassStudents(classId: number, params: PageQuery) {
  return request.get<any, Result<PageResult<StudentItem>>>(`/class/${classId}/students`, { params })
}

/** 鑾峰彇浣滀笟鍒楄〃 */
export function getHomeworkList(classId: number, params: PageQuery) {
  return request.get<any, Result<PageResult<HomeworkItem>>>(`/class/${classId}/homework`, { params })
}

/** 鎻愪氦浣滀笟 */
export function submitHomework(data: { homeworkId: number; content: string; fileIds?: number[] }) {
  return request.post<any, Result<null>>('/class/homework/submit', data)
}

/** 鍒涘缓鐝骇 */
export function createClass(data: Partial<ClassItem>) {
  return request.post<any, Result<ClassItem>>('/class/create', data)
}

/** 娣诲姞瀛︾敓鍒扮彮绾?*/
export function addStudentToClass(classId: number, studentIds: number[]) {
  return request.post<any, Result<null>>(`/class/${classId}/students`, { studentIds })
}
