/**
 * v-debounce 防抖指令
 * 用于按钮点击防抖，防止重复提交
 * 用法：v-debounce="handleSubmit" 或 v-debounce:500="handleSubmit"
 */
import type { Directive, DirectiveBinding } from 'vue'

/** 防抖指令 */
export const debounceDirective: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding<(...args: any[]) => void>) {
    // 获取防抖延迟时间（从修饰符或默认300ms）
    let delayTime = 300
    if (binding.arg && !isNaN(Number(binding.arg))) {
      delayTime = Number(binding.arg)
    }

    let timer: ReturnType<typeof setTimeout> | null = null

    el.addEventListener('click', () => {
      if (timer) clearTimeout(timer)
      timer = setTimeout(() => {
        binding.value()
      }, delayTime)
    })
  },
  unmounted(el: HTMLElement) {
    // 清理事件监听
    el.removeEventListener('click', () => {})
  },
}
