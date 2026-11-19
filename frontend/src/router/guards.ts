/**
 * 路由守卫
 * 处理登录状态跳转、角色权限校验
 *
 * 安全说明：
 * - DEV 角色提升逻辑通过 import.meta.env.DEV 静态判断
 * - Vite 生产构建时 import.meta.env.DEV 被替换为 false，整段代码会被 Tree-shaking 移除
 * - 生产环境不存在任何角色提升后门
 */
import type { Router } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useSiteStore } from '@/stores/site'
import NProgress from 'nprogress'

/** 白名单路由（不需要登录即可访问） */
const whiteList = ['/login', '/register']

/**
 * 获取用户角色（含 DEV 模式提升逻辑）
 *
 * 生产环境：import.meta.env.DEV === false，此函数直接返回用户真实角色
 * 开发环境：student 角色自动提升为 admin，方便无后端时测试后台功能
 */
function getUserRoles(): string[] {
  const userStore = useUserStore()

  // ── 生产环境：直接返回真实角色，无任何提升 ──
  if (!import.meta.env.DEV) {
    return userStore.roles
  }

  // ── 以下代码仅在开发环境执行，生产构建会被 Tree-shaking 完全移除 ──
  if (userStore.roles.length === 1 && userStore.roles[0] === 'student') {
    if (import.meta.env.DEV) {
      console.warn('[DEV] 开发环境角色提升：student → admin（生产环境不存在此逻辑）')
    }
    return ['admin']
  }

  return userStore.roles
}

/** 设置路由守卫 */
export function setupGuards(router: Router) {
  router.beforeEach(async (to, _from, next) => {
    // 开始进度条
    NProgress.start()

    const userStore = useUserStore()
    const siteStore = useSiteStore()

    // 功能开关：后台关闭注册后，拦截直接访问 /register（配置已加载完成时生效）
    if (to.path === '/register' && siteStore.loaded && !siteStore.config.registerEnabled) {
      NProgress.done()
      next({ path: '/login', replace: true })
      return
    }

    if (userStore.isLoggedIn) {
      // 已登录状态
      if (to.path === '/login') {
        // 已登录访问登录页，重定向到首页
        next({ path: '/' })
      } else {
        // 异步获取用户信息（不阻塞导航）
        if (!userStore.userInfo) {
          userStore.fetchUserInfo()
        }
        // 检查角色权限
        const requiredRoles = to.meta.roles as string[] | undefined
        if (requiredRoles && requiredRoles.length > 0) {
          const userRoles = getUserRoles()
          const hasRole = userRoles.some((role) => requiredRoles.includes(role))
          if (!hasRole) {
            next({ path: '/' })
          } else {
            next()
          }
        } else {
          next()
        }
      }
    } else {
      // 未登录状态
      // 生产环境：token 存 httpOnly Cookie（JS 读不到），先静默判断登录态
      if (import.meta.env.PROD) {
        const authed = await userStore.ensureAuth()
        if (authed) {
          if (to.path === '/login') {
            next({ path: '/' })
          } else {
            if (!userStore.userInfo) userStore.fetchUserInfo()
            next()
          }
          return
        }
      }
      if (whiteList.includes(to.path)) {
        next()
      } else {
        next(`/login?redirect=${to.path}`)
      }
    }
  })

  router.afterEach(() => {
    // 结束进度条
    NProgress.done()
  })
}
