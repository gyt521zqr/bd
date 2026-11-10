/**
 * 环境变量类型声明
 * 为 import.meta.env 中的自定义变量提供类型支持
 */

// 声明 Vite 内置的环境变量类型
interface ImportMetaEnv {
  /** 应用标题 */
  readonly VITE_APP_TITLE: string
  /** API 基础路径 */
  readonly VITE_APP_BASE_API: string
  /** 应用基础 URL */
  readonly VITE_APP_BASE_URL: string
  /** 是否开发模式 */
  readonly DEV: boolean
  /** 是否生产模式 */
  readonly PROD: boolean
  /** 当前模式 */
  readonly MODE: string
  /** 路由基础路径 */
  readonly BASE_URL: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

// 第三方模块类型声明
declare module 'element-plus/dist/locale/zh-cn.mjs' {
  const locale: any
  export default locale
}

declare module 'nprogress' {
  const nprogress: any
  export default nprogress
}

declare module 'js-cookie' {
  const Cookies: any
  export default Cookies
}
