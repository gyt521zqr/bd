/**
 * 课程分类管理 API 接口
 */
import request from './index'

/**
 * 内容分类（cms_category）
 * 注意：type 表示「业务域」——course 课程 / news 资讯 / practice 练习，
 * 与首页学习中心的「语言/年龄标签」不是同一概念，请勿混用。
 * 首页学习标签已从课程数据推导（见 HomePage.vue），导航菜单由后台 nav_menu 独立配置。
 */
export interface CategoryItem {
  id: number
  name: string
  icon?: string
  ageRange?: string
  sortOrder: number
  status: 'enabled' | 'disabled' | number
  type: 'course' | 'news' | 'practice' | string
}

/**
 * 获取分类列表。
 * 后端为唯一数据源：接口失败返回 null，由调用方显示空态/错误提示，
 * 不再回退到管理员浏览器 localStorage（那会造成"后台改了、前台看不到"的假成功）。
 */
export async function getCategories(params?: { type?: string }) {
  try {
    return await request.get<any, { code: number; data: CategoryItem[]; message?: string }>('/categories', { params })
  } catch {
    return null
  }
}

/** 创建分类 */
export function createCategory(data: Partial<CategoryItem>) {
  return request.post<any, { code: number; data: CategoryItem; message?: string }>('/categories', data)
}

/** 更新分类 */
export function updateCategory(id: number, data: Partial<CategoryItem>) {
  return request.put<any, { code: number; data: CategoryItem; message?: string }>(`/categories/${id}`, data)
}

/** 删除分类 */
export function deleteCategory(id: number) {
  return request.delete<any, { code: number; message?: string }>(`/categories/${id}`)
}

/** 更新分类排序 */
export function updateCategorySort(id: number, sortOrder: number) {
  return request.put<any, { code: number; message?: string }>(`/categories/${id}/sort`, { sortOrder })
}
