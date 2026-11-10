<template>
  <!-- 水印覆盖层 -->
  <div class="watermark-overlay" ref="watermarkRef"></div>
</template>

<script setup lang="ts">
/**
 * WatermarkOverlay - 水印覆盖层
 * 在视频播放器上叠加文字水印
 */
import { ref, onMounted } from 'vue'

const props = withDefaults(defineProps<{ text?: string }>(), { text: '' })
const watermarkRef = ref<HTMLElement>()

onMounted(() => {
  if (!watermarkRef.value || !props.text) return
  const canvas = document.createElement('canvas')
  canvas.width = 200
  canvas.height = 100
  const ctx = canvas.getContext('2d')!
  ctx.fillStyle = 'rgba(255, 255, 255, 0.15)'
  ctx.font = '14px sans-serif'
  ctx.rotate(-15 * Math.PI / 180)
  ctx.fillText(props.text, 10, 50)
  const url = canvas.toDataURL()
  watermarkRef.value.style.backgroundImage = `repeating-linear-gradient(45deg, transparent, transparent 100px, ${url} 100px, ${url} 200px)`
})
</script>

<style scoped lang="scss">
.watermark-overlay { position: absolute; top: 0; left: 0; right: 0; bottom: 0; pointer-events: none; z-index: 10; }
</style>
