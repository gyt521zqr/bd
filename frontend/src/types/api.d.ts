/**
 * API 通用类型定义
 * 定义所有接口请求和响应的通用数据结构
 */

/** 通用响应结果 */
export interface Result<T = any> {
  /** 状态码 */
  code: number
  /** 提示消息 */
  message: string
  /** 响应数据 */
  data: T
}

/** 分页查询参数 */
export interface PageQuery {
  /** 当前页码 */
  pageNum: number
  /** 每页条数 */
  pageSize: number
}

/** 分页查询结果 */
export interface PageResult<T = any> {
  /** 数据列表 */
  list: T[]
  /** 总条数 */
  total: number
  /** 当前页码 */
  pageNum: number
  /** 每页条数 */
  pageSize: number
}
