/**
 * v-watermark 姘村嵃鎸囦护
 * 鍦ㄥ厓绱犱笂鍙犲姞鏂囧瓧姘村嵃锛岀敤浜庨槻鎴浘/褰曞睆
 * 鐢ㄦ硶锛歷-watermark="{ text: '鐮侀珮鏁欒偛', fontSize: 16 }"
 */
import type { Directive, DirectiveBinding } from 'vue'

/** 鍒涘缓姘村嵃 Canvas */
function createWatermarkCanvas(text: string, fontSize: number = 14): HTMLCanvasElement {
  const canvas = document.createElement('canvas')
  const ctx = canvas.getContext('2d')!
  canvas.width = 300
  canvas.height = 200

  ctx.rotate((-20 * Math.PI) / 180)
  ctx.font = `${fontSize}px sans-serif`
  ctx.fillStyle = 'rgba(0, 0, 0, 0.08)'
  ctx.textAlign = 'center'
  ctx.fillText(text, 150, 100)

  return canvas
}

/** 姘村嵃鎸囦护 */
export const watermarkDirective: Directive = {
  mounted(el: HTMLElement, binding: DirectiveBinding<{ text: string; fontSize?: number }>) {
    const { text, fontSize = 14 } = binding.value
    const canvas = createWatermarkCanvas(text, fontSize)
    const watermarkDiv = document.createElement('div')
    watermarkDiv.style.cssText = `
      position: absolute;
      top: 0;
      left: 0;
      width: 100%;
      height: 100%;
      pointer-events: none;
      background-image: url(${canvas.toDataURL()});
      background-repeat: repeat;
      z-index: 9999;
    `
    el.style.position = 'relative'
    el.appendChild(watermarkDiv)
  },
}
