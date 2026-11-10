import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'
import { useSiteStore } from './stores/site'

// Monaco Editor web worker 配置（使用 Vite ?worker 导入语法，确保开发和生产环境均正确加载）
import editorWorker from 'monaco-editor/esm/vs/editor/editor.worker?worker'
import jsonWorker from 'monaco-editor/esm/vs/language/json/json.worker?worker'
import cssWorker from 'monaco-editor/esm/vs/language/css/css.worker?worker'
import htmlWorker from 'monaco-editor/esm/vs/language/html/html.worker?worker'
import tsWorker from 'monaco-editor/esm/vs/language/typescript/ts.worker?worker'

/**
 * 浏览器兼容性说明 / Browser Compatibility Notes
 * ------------------------------------------------
 * 如需支持 IE11 或旧版浏览器，请安装以下 polyfill：
 * npm install core-js regenerator-runtime intersection-observer
 * 然后在 main.ts 顶部添加：
 *   import 'core-js/stable'
 *   import 'regenerator-runtime/runtime'
 *   import 'intersection-observer'
 * ------------------------------------------------
 */

// Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'

// 全局样式
import './styles/global.scss'

// 图片懒加载自定义指令（不引入新重型依赖）
const lazyLoadDirective = {
  mounted(el: HTMLImageElement, binding: any) {
    const src = binding.value
    if (!src) return

    const observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        if (entry.isIntersecting) {
          el.src = src
          observer.unobserve(el)
        }
      })
    }, {
      rootMargin: '50px 0px',
      threshold: 0.01,
    })

    observer.observe(el)

    // 存储 observer 以便卸载时清理
    ;(el as any).__lazyObserver = observer
  },
  unmounted(el: HTMLImageElement) {
    const observer = (el as any).__lazyObserver
    if (observer) {
      observer.disconnect()
      delete (el as any).__lazyObserver
    }
  },
}

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(ElementPlus, { locale: zhCn, zIndex: 3000 })

// 注册全局懒加载指令 v-lazy
app.directive('lazy', lazyLoadDirective)

// 启动时拉取站点全局配置（站名/ICP/导航/功能开关/公告），不阻塞挂载
useSiteStore().loadSiteConfig()

// Monaco Editor web worker 配置
;(window as any).MonacoEnvironment = {
  getWorker(_workerId: string, label: string) {
    switch (label) {
      case 'json':
        return new jsonWorker()
      case 'css':
      case 'scss':
      case 'less':
        return new cssWorker()
      case 'html':
      case 'handlebars':
      case 'razor':
        return new htmlWorker()
      case 'typescript':
      case 'javascript':
        return new tsWorker()
      default:
        return new editorWorker()
    }
  },
}

app.mount('#app')
