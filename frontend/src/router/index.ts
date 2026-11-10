/**
 * 璺敱瀹炰緥鍒涘缓
 */
import { createRouter, createWebHistory } from 'vue-router'
import routes from './routes'
import { setupGuards } from './guards'

/** 鍒涘缓璺敱瀹炰緥 */
const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  // 璺敱鍒囨崲鏃舵粴鍔ㄥ埌椤堕儴
  scrollBehavior(_to, _from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    }
    return { top: 0 }
  },
})

// 璁剧疆璺敱瀹堝崼
setupGuards(router)

export default router
