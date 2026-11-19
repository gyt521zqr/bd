/**
 * 用户相关 API 接口
 */
import request from './index'
import type { RequestConfig } from './index'
import type { Result, UserInfo, LoginParams, RegisterParams } from '@/types'

/** 用户登录 */
export function login(data: LoginParams) {
  if (data.loginType === 'sms') {
    return request.post<any, Result<{ accessToken: string; refreshToken: string; tokenType: string; user: UserInfo }>>('/auth/login', {
      phone: data.phone,
      code: data.password,
      loginType: 'sms',
    })
  }
  return request.post<any, Result<{ accessToken: string; refreshToken: string; tokenType: string; user: UserInfo }>>('/auth/login', {
    username: data.phone,
    password: data.password,
    loginType: data.loginType,
  })
}

/** 用户注册 */
export function register(data: RegisterParams) {
  return request.post<any, Result<null>>('/auth/register', data)
}

/** 获取当前用户信息 */
export function getUserInfo(config?: RequestConfig) {
  return request.get<any, Result<UserInfo>>('/user/profile', config)
}

/** 修改密码 */
export function changePassword(data: { oldPassword: string; newPassword: string }) {
  return request.put<any, Result<null>>('/user/password', data)
}

/** 发送短信验证码 */
export function sendSmsCode(phone: string, _type: 'login' | 'register' | 'reset') {
  return request.post<any, Result<null>>('/auth/sms-code', null, { params: { phone } })
}

/** 退出登录 */
export function logout() {
  return request.post<any, Result<null>>('/auth/logout')
}

/** 刷新 Token（accessToken 过期时静默调用） */
export function refreshToken(refreshToken: string) {
  return request.post<any, Result<{ accessToken: string; refreshToken: string; tokenType: string; user: UserInfo }>>(
    '/auth/refresh',
    null,
    { params: { refreshToken } },
  )
}

/** 更新用户信息 */
export function updateUserInfo(data: Partial<UserInfo>) {
  return request.put<any, Result<UserInfo>>('/user/info', data)
}

/** 上传头像 */
export function uploadAvatar(file: File) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<any, Result<string>>('/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  })
}

/** 绑定家长手机号 */
export function bindParentPhone(phone: string) {
  return request.post<any, Result<null>>('/user/bind-parent', { phone })
}
