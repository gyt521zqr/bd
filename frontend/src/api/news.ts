/**
 * 资讯/公告相关 API 接口
 */
import request from './index'

export interface NewsItem {
  id: number
  title: string
  category: string
  content: string
  coverImage?: string
  status: 'published' | 'draft'
  isPinned: boolean
  publishTime: string
  createTime: string
}

/**
 * 获取资讯列表。
 * 后端为唯一数据源：接口失败返回 null，由调用方显示空态/错误提示，
 * 不再回退到管理员浏览器 localStorage（那会造成"后台改了、前台看不到"的假成功）。
 */
export async function getNewsList(params: {
  page?: number
  size?: number
  keyword?: string
  category?: string
  status?: string
}) {
  try {
    const res = await request.get('/news', { params })
    return res.data || res
  } catch {
    return null
  }
}

/** 创建资讯 */
export function createNews(data: Partial<NewsItem>) {
  return request.post('/news', data)
}

/** 更新资讯 */
export function updateNews(id: number, data: Partial<NewsItem>) {
  return request.put(`/news/${id}`, data)
}

/** 删除资讯 */
export function deleteNews(id: number) {
  return request.delete(`/news/${id}`)
}

/** 置顶/取消置顶资讯 */
export function toggleNewsPin(id: number, isPinned: boolean) {
  return request.put(`/news/${id}/pin`, { isPinned })
}

/** 发布/下架资讯 */
export function toggleNewsStatus(id: number, status: string) {
  return request.put(`/news/${id}/status`, { status })
}
