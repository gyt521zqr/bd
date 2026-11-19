/**
 * Token 管理工具
 * 基于 js-cookie 实现 Token 的存取和删除
 *
 * 安全说明：
 * - httpOnly 无法通过前端 js-cookie 设置，需后端在 Set-Cookie 响应头中配置
 * - Secure 和 SameSite 由前端设置，仅生产环境启用
 * - 开发环境使用 HTTP localhost，不设置 Secure 以免 Cookie 无法写入
 */
import Cookies from 'js-cookie'

/** Token 在 Cookie 中的键名 */
const TOKEN_KEY = 'magao_token'

/** 是否为生产环境（Vite 构建时静态替换，DEV 模式为 false） */
const isProduction = import.meta.env.PROD

/**
 * Cookie 安全选项
 * - 生产环境：启用 Secure（仅 HTTPS 传输）+ SameSite=Lax（防 CSRF，允许顶层导航）
 * - 开发环境：不设置 Secure（兼容 HTTP localhost）
 */
const cookieOptions = isProduction
  ? { secure: true, sameSite: 'Lax' as const }
  : {}

/** 获取 Token */
export function getToken(): string {
  // 生产环境 token 由后端 httpOnly Cookie 管理（JS 不可读，防 XSS 窃取），返回空
  if (isProduction) return ''
  return Cookies.get(TOKEN_KEY) || ''
}

/**
 * 设置 Token
 * 生产环境不再用 js-cookie 设置（后端登录接口已下发 httpOnly Cookie），
 * 仅开发环境保留 js-cookie 便于本地联调。
 * @param token JWT Token 字符串
 * @param expires 过期天数，默认 7 天
 */
export function setToken(token: string, expires: number = 7): void {
  if (isProduction) return
  Cookies.set(TOKEN_KEY, token, { expires, ...cookieOptions })
}

/**
 * 删除 Token
 * 生产环境依赖后端 /auth/logout 清除 httpOnly Cookie；此处清理开发环境本地 Cookie 兜底。
 */
export function removeToken(): void {
  Cookies.remove(TOKEN_KEY, cookieOptions)
}
