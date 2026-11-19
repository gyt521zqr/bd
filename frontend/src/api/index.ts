/**
 * Axios 实例配置
 * 统一请求拦截器（添加Token）和响应拦截器（错误处理）
 */
import axios, { type AxiosInstance, type AxiosRequestConfig, type AxiosResponse, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, removeToken } from '@/utils/auth'
import router from '@/router'

/** 扩展 AxiosRequestConfig，支持 silent 选项 */
export interface RequestConfig extends AxiosRequestConfig {
  /** 为 true 时，请求失败不弹出 ElMessage 错误提示 */
  silent?: boolean
}

/**
 * DEV 模式标志
 * 生产构建时 import.meta.env.DEV 被静态替换为 false，所有 DEV 分支会被 Tree-shaking 移除
 * 生产环境：isDevMode === false，401 错误正常处理（清除 Token + 跳转登录页）
 */
const isDevMode = import.meta.env.DEV

/** 创建 Axios 实例 */
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_APP_BASE_API,
  timeout: 30000,
  withCredentials: true, // P1-18：携带后端下发的 httpOnly Cookie
  headers: {
    'Content-Type': 'application/json;charset=UTF-8',
  },
})

/** 请求拦截器：在请求头中添加 Token */
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = getToken()
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('请求错误：', error)
    return Promise.reject(error)
  }
)

/** 响应拦截器：统一处理响应数据和错误 */
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    const silent = (response.config as RequestConfig).silent

    // 如果返回的状态码不是 200，说明接口有问题
    if (res.code !== 200) {
      if (!silent && !isDevMode) {
        ElMessage.error(res.message || '请求失败')
      }

      // 401: Token 过期或未登录（DEV/静默模式下不处理）
      if (res.code === 401 && !silent && !isDevMode) {
        removeToken()
        router.push('/login')
      }

      // 403: 没有权限
      if (res.code === 403 && !silent) {
        ElMessage.error('您没有权限执行此操作')
      }

      // 500: 服务器错误
      if (res.code === 500 && !silent) {
        ElMessage.error('服务器内部错误，请联系管理员')
      }

      return Promise.reject(new Error(res.message || '请求失败'))
    }
    return res
  },
  (error) => {
    const silent = (error.config as RequestConfig)?.silent || false
    const url = error.config?.url || ''

    // 首页自动加载的请求静默处理（url 为相对路径，不含 baseURL 前缀）
    const isAutoLoad = url.includes('course/list') || url.includes('practice/daily') || url.includes('user/profile') || url.includes('message/unread-count')

    // DEV 模式或静默模式或自动加载：不弹窗，不打印错误，不处理 401 跳转
    if (isDevMode || silent || isAutoLoad) {
      return Promise.reject(error)
    }

    console.error('响应错误：', error)

    // HTTP 状态码错误处理
    const status = error.response?.status
    const messages: Record<number, string> = {
      400: '请求参数错误',
      401: '登录已过期，请重新登录',
      403: '没有权限访问',
      404: '请求的资源不存在',
      408: '请求超时',
      500: '服务器内部错误',
      502: '网关错误',
      503: '服务不可用',
    }
    const message = messages[status] || `请求失败（${status}）`
    ElMessage.error(message)

    // 401 跳转登录页
    if (status === 401) {
      removeToken()
      router.push('/login')
    }

    return Promise.reject(error)
  }
)

// 扩展 Axios 实例类型，使 get/post/put/delete 等方法支持 RequestConfig
// eslint-disable-next-line @typescript-eslint/no-explicit-any
interface ExtendedAxiosInstance extends AxiosInstance {
  get<T = any, R = AxiosResponse<T>>(url: string, config?: RequestConfig): Promise<R>
  delete<T = any, R = AxiosResponse<T>>(url: string, config?: RequestConfig): Promise<R>
  head<T = any, R = AxiosResponse<T>>(url: string, config?: RequestConfig): Promise<R>
  options<T = any, R = AxiosResponse<T>>(url: string, config?: RequestConfig): Promise<R>
  post<T = any, R = AxiosResponse<T>>(url: string, data?: any, config?: RequestConfig): Promise<R>
  put<T = any, R = AxiosResponse<T>>(url: string, data?: any, config?: RequestConfig): Promise<R>
  patch<T = any, R = AxiosResponse<T>>(url: string, data?: any, config?: RequestConfig): Promise<R>
}

export default service as ExtendedAxiosInstance
