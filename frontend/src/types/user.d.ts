/**
 * 用户相关类型定义
 */

/** 用户角色枚举 */
export type UserRole = 'student' | 'teacher' | 'admin' | 'parent'

/** 用户信息 */
export interface UserInfo {
  /** 用户ID */
  id: number
  /** 手机号 */
  phone: string
  /** 昵称 */
  nickname: string
  /** 头像 */
  avatar: string
  /** 角色 */
  role: UserRole
  /** 角色列表（兼容后端返回数组） */
  roles?: UserRole[]
  /** 用户名（兼容旧版） */
  username?: string
  /** 等级 */
  level: number
  /** 积分 */
  points: number
  /** 性别 */
  gender?: string
  /** 生日 */
  birthday?: string
  /** 注册时间 */
  createTime: string
}

/** 登录参数 */
export interface LoginParams {
  phone: string
  password: string
  loginType: 'password' | 'sms' | 'wechat'
}

/** 注册参数 */
export interface RegisterParams {
  phone: string
  smsCode: string
  nickname: string
  password: string
  agreed?: boolean
  guardianConsent?: boolean
}

/** 登录响应 */
export interface LoginResult {
  token: string
  userInfo: UserInfo
}
