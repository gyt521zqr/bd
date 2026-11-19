/**
 * 用户状态管理
 * 管理用户信息、token、登录登出、权限菜单 */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login as loginApi, logout as logoutApi, getUserInfo as getUserInfoApi } from '@/api/user'
import type { UserInfo, LoginParams, UserRole } from '@/types'
import { setToken, getToken, removeToken } from '@/utils/auth'
import { setLocal, getLocal, removeLocal } from '@/utils/localStore'

export const useUserStore = defineStore('user', () => {
  /**
   * Token
   * 生产环境：仅从 Cookie 读取真实 Token，无后门
   * 开发环境：Cookie 无 Token 时回退到 'dev-token'，方便无后端时测试
   * 注意：import.meta.env.DEV 在生产构建时被 Vite 静态替换为 false，三元运算的 DEV 分支会被 Tree-shaking 移除
   */
  const token = ref<string>(getToken() || (import.meta.env.DEV ? 'dev-token' : ''))
  /** 用户信息 */
  const userInfo = ref<UserInfo | null>(getLocal<UserInfo>('userInfo'))
  /** 角色列表 */
  const roles = computed<UserRole[]>(() => {
    // 优先取 roles 数组
    if (userInfo.value?.roles && Array.isArray(userInfo.value.roles) && userInfo.value.roles.length > 0) {
      return userInfo.value.roles
    }
    // 其次取 role 单值
    if (userInfo.value?.role) {
      return [userInfo.value.role]
    }
    // 兼容后端未返回角色字段的情况：admin 用户名默认赋予 admin 角色
    if (userInfo.value?.username === 'admin') {
      return ['admin']
    }
    return []
  })

  /** 是否已登录 */
  const isLoggedIn = computed(() => !!token.value)
  /** 是否是管理员 */
  const isAdmin = computed(() => roles.value.includes('admin'))
  /** 是否是教师 */
  const isTeacher = computed(() => roles.value.includes('teacher'))
  /** 是否拥有指定角色（之一） */
  function hasAnyRole(...checkRoles: UserRole[]): boolean {
    return checkRoles.some(r => roles.value.includes(r))
  }

  /** 用户登录 */
  async function login(loginForm: LoginParams) {
    const res = await loginApi(loginForm)
    // 生产环境 token 由后端 httpOnly Cookie 管理，内存仅存占位符（防 XSS 读真实 token）
    token.value = import.meta.env.PROD ? 'httpOnly' : res.data.accessToken
    setToken(res.data.accessToken)
    // 保存 refreshToken，用于 accessToken 过期时静默刷新
    if (res.data.refreshToken) {
      setLocal('refreshToken', res.data.refreshToken)
    }
    if (res.data.user) {
      userInfo.value = res.data.user
      setLocal('userInfo', res.data.user)
    }
  }

  /**
   * 静默判断登录态（生产环境刷新后 httpOnly Cookie 无法被 JS 读取）
   * 通过调用 getUserInfo 判断 Cookie 是否有效；成功则标记已登录。
   * @returns 是否已登录
   */
  async function ensureAuth(): Promise<boolean> {
    if (token.value) return true
    try {
      const res = await getUserInfoApi({ silent: true })
      userInfo.value = res.data
      setLocal('userInfo', res.data)
      token.value = 'httpOnly'
      return true
    } catch {
      return false
    }
  }

  /** 获取用户信息（静默模式，不弹错误提示，因为可能是在检查登录状态） */
  async function fetchUserInfo() {
    try {
      const res = await getUserInfoApi({ silent: true })
      userInfo.value = res.data
      setLocal('userInfo', res.data)
    } catch {
      // 获取用户信息失败不阻断流程，也不弹窗
      // 设置默认用户信息，避免路由守卫无限循环
      if (!userInfo.value) {
        const defaultInfo: UserInfo = {
          id: 0,
          username: 'dev_user',
          phone: '',
          nickname: '开发者',
          avatar: '',
          role: 'student',
          roles: ['student'],
          level: 1,
          points: 0,
          createTime: new Date().toISOString(),
        }
        userInfo.value = defaultInfo
        setLocal('userInfo', defaultInfo)
      }
    }
  }

  /** 退出登录 */
  async function logout() {
    try {
      await logoutApi()
    } catch {
      // 退出登录失败也清除本地状态
    }
    token.value = ''
    userInfo.value = null
    removeToken()
    removeLocal('userInfo')
    removeLocal('refreshToken')
  }

  /** 更新用户信息 */
  function updateUserInfo(data: Partial<UserInfo>) {
    if (userInfo.value) {
      userInfo.value = { ...userInfo.value, ...data }
      setLocal('userInfo', userInfo.value)
    }
  }

  return {
    token,
    userInfo,
    roles,
    isLoggedIn,
    isAdmin,
    isTeacher,
    hasAnyRole,
    login,
    ensureAuth,
    fetchUserInfo,
    logout,
    updateUserInfo,
  }
})
