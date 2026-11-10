/**
 * 课程相关 API 接口
 */
import request from './index'
import type { Result, PageResult, PageQuery, CourseItem, CourseDetail, Video } from '@/types'

/** 获取课程列表（分页） */
export function getCourseList(params: PageQuery & { categoryId?: number; difficulty?: string; keyword?: string }) {
  return request.get<any, Result<PageResult<CourseItem>>>('/course/list', { params })
}

/** 获取课程详情（含视频列表） */
export function getCourseDetail(id: number) {
  return request.get<any, Result<CourseDetail>>(`/course/${id}`)
}

/** 购买课程 */
export function purchaseCourse(courseId: number, couponId?: number) {
  return request.post<any, Result<{ orderId: number }>>('/course/purchase', { courseId, couponId })
}

/** 获取课程视频列表（扁平，无章节） */
export function getCourseVideos(courseId: number) {
  return request.get<any, Result<Video[]>>(`/course/${courseId}/videos`)
}

/** 新增课程视频 */
export function addCourseVideo(data: { courseId: number; title: string; videoUrl: string; sortOrder?: number }) {
  return request.post<any, Result<Video>>('/course/video', data)
}

/** 更新课程视频 */
export function updateCourseVideo(data: { id: number; title?: string; videoUrl?: string; sortOrder?: number }) {
  return request.put<any, Result<Video>>('/course/video', data)
}

/** 删除课程视频 */
export function deleteCourseVideo(videoId: number) {
  return request.delete<any, Result<null>>(`/course/video/${videoId}`)
}

/** 获取视频播放地址（加密） */
export function getVideoUrl(videoId: number) {
  return request.get<any, Result<{ url: string; token: string }>>(`/course/video/${videoId}/play`)
}

/** 记录学习进度 */
export function recordProgress(videoId: number, progress: number) {
  return request.post<any, Result<null>>('/course/progress', { videoId, progress })
}

/** 获取课程分类列表 */
export function getCourseCategories() {
  return request.get<any, Result<{ id: number; name: string; icon: string }[]>>('/course/categories')
}
