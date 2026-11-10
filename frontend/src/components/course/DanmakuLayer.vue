<template>
  <!-- 弹幕层组件 -->
  <div class="danmaku-layer" ref="danmakuContainer">
    <div
      v-for="item in visibleDanmakus"
      :key="item.id"
      class="danmaku-item"
      :style="{
        top: item.top + `px`,
        animationDuration: item.duration + `s`,
        color: item.color,
      }"
    >
      {{ item.text }}
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * DanmakuLayer - 弹幕层组件
 * 在视频播放器上叠加弹幕效果
 */
import { ref, onMounted } from 'vue'

interface Danmaku {
  id: number
  text: string
  top: number
  duration: number
  color: string
}

const props = withDefaults(defineProps<{
  /** 外部传入的弹幕文本列表；未传入时使用内置示例数据 */
  danmakus?: string[]
}>(), {
  // 未传入外部数据时使用的示例弹幕（仅作演示）
  danmakus: () => ['讲得好！', '太棒了', '学到了', '老师讲得真清楚', '这个知识点很重要'],
})

const danmakuContainer = ref<HTMLElement>()
const visibleDanmakus = ref<Danmaku[]>([])

let idCounter = 0

function addDanmaku(text: string) {
  const item: Danmaku = {
    id: idCounter++,
    text,
    top: Math.random() * 80 + 10,
    duration: 6 + Math.random() * 4,
    color: ['#fff', '#4080FF', '#36D399', '#FFB84D', '#F76965'][Math.floor(Math.random() * 5)],
  }
  visibleDanmakus.value.push(item)
  setTimeout(() => {
    visibleDanmakus.value = visibleDanmakus.value.filter(d => d.id !== item.id)
  }, item.duration * 1000)
}

onMounted(() => {
  // 弹幕数据来源：优先使用外部传入的 danmakus，未传入时使用内置示例数据
  setInterval(() => {
    const list = props.danmakus
    if (list.length > 0) {
      addDanmaku(list[Math.floor(Math.random() * list.length)])
    }
  }, 3000)
})

defineExpose({ addDanmaku })
</script>

<style scoped lang="scss">
.danmaku-layer { position: absolute; top: 0; left: 0; right: 0; bottom: 40px; pointer-events: none; overflow: hidden; }
.danmaku-item { position: absolute; right: -100%; white-space: nowrap; font-size: 14px; text-shadow: 1px 1px 2px rgba(0,0,0,0.5); animation: danmaku-scroll linear forwards; }
@keyframes danmaku-scroll { from { transform: translateX(0); } to { transform: translateX(calc(-100% - 100vw)); } }
</style>
