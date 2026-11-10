/**
 * 课程相关类型定义
 */

/** 课程难度 */
export type CourseDifficulty = 'beginner' | 'intermediate' | 'advanced'

/** 课程状态 */
export type CourseStatus = 'draft' | 'published' | 'offline'

/** 视频（扁平结构，无章节层级） */
export interface Video {
  id: number
  title: string
  videoUrl: string
  sortOrder: number
}

/** 课程信息 */
export interface Course {
  id: number
  title: string
  cover: string
  description: string
  difficulty: CourseDifficulty
  status: CourseStatus
  teacherId: number
  teacherName: string
  studentCount: number
  rating: number
  price: number
  hasTrial: boolean
  videos: Video[]
  createTime: string
  updateTime: string
}

/** 课程列表项 */
export interface CourseItem {
  id: number
  title: string
  cover: string
  description: string
  difficulty: CourseDifficulty
  status: CourseStatus
  teacherId: number
  teacherName: string
  studentCount: number
  rating: number
  price: number
  hasTrial: boolean
  createTime: string
  updateTime: string
}

/** 课程详情 */
export interface CourseDetail extends Course {
  /** 课程详情扩展字段 */
}
