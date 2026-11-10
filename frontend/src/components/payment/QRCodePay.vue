<template>
  <!-- 扫码支付弹窗：二维码+倒计时+状态提示 -->
  <el-dialog v-model="visible" title="扫码支付" width="400px" :close-on-click-modal="false">
    <div class="qrcode-pay">
      <!-- 二维码区域 -->
      <div class="qrcode-area">
        <div class="qrcode-placeholder">
          <img v-if="qrCodeUrl" :src="qrCodeUrl" alt="支付二维码" />
          <div v-else class="qrcode-loading">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>生成支付二维码...</span>
          </div>
        </div>
      </div>
      <!-- 倒计时 -->
      <div class="pay-countdown">
        <span>二维码有效期：</span>
        <span class="countdown">{{ countdown }}s</span>
      </div>
      <!-- 提示 -->
      <p class="pay-tip">请使用{{ payMethod === 'wechat' ? '微信' : '支付宝' }}扫一扫完成支付</p>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
/**
 * QRCodePay - 扫码支付弹窗
 * 二维码展示、倒计时、状态提示
 */
import { ref, watch, onBeforeUnmount } from 'vue'
import { Loading } from '@element-plus/icons-vue'

const visible = defineModel<boolean>({ default: false })

withDefaults(defineProps<{
  orderNo?: string
  payMethod?: string
}>(), {
  payMethod: 'wechat',
})

const emit = defineEmits<{ close: [], success: [] }>()

const qrCodeUrl = ref('')
const countdown = ref(300)
let timer: ReturnType<typeof setInterval> | null = null

watch(visible, (val) => {
  if (val) {
    countdown.value = 300
    qrCodeUrl.value = '' // 模拟生成二维码
    setTimeout(() => { qrCodeUrl.value = 'https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=pay' }, 1000)
    timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) { clearInterval(timer!); visible.value = false }
    }, 1000)
  } else {
    if (timer) clearInterval(timer)
    emit('close')
  }
})

onBeforeUnmount(() => { if (timer) clearInterval(timer) })
</script>

<style scoped lang="scss">
.qrcode-pay { text-align: center; }
.qrcode-area { display: flex; justify-content: center; margin-bottom: 16px; }
.qrcode-placeholder { width: 200px; height: 200px; border: 2px dashed #E5E6EB; border-radius: 12px; display: flex; align-items: center; justify-content: center; img { width: 100%; height: 100%; object-fit: contain; border-radius: 8px; } }
.qrcode-loading { display: flex; flex-direction: column; align-items: center; gap: 8px; color: #86909C; font-size: 13px; }
.pay-countdown { font-size: 14px; color: #86909C; margin-bottom: 8px; .countdown { font-weight: 700; color: #F53F3F; } }
.pay-tip { font-size: 13px; color: #86909C; }
</style>
