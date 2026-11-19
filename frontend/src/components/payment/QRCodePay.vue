<template>
  <!-- 扫码支付弹窗：真实二维码（本地渲染）+ 倒计时 + 状态轮询 -->
  <el-dialog
    v-model="visible"
    :title="demo ? '模拟支付' : '扫码支付'"
    width="420px"
    :close-on-click-modal="false"
    :show-close="!paying"
    @close="handleClose"
  >
    <div class="qrcode-pay">
      <div class="pay-amount-hint" v-if="amount > 0">
        应付金额 <strong>¥{{ (amount / 100).toFixed(2) }}</strong>
      </div>

      <!-- 二维码区域：用后端返回的真实凭证本地渲染，不依赖任何外网服务 -->
      <div class="qrcode-area">
        <div class="qrcode-placeholder">
          <img v-if="qrDataUrl" :src="qrDataUrl" alt="支付二维码" />
          <div v-else-if="qrError" class="qrcode-error">
            <el-icon><CircleCloseFilled /></el-icon>
            <span>{{ qrError }}</span>
          </div>
          <div v-else class="qrcode-loading">
            <el-icon class="is-loading"><Loading /></el-icon>
            <span>生成支付二维码...</span>
          </div>
        </div>
      </div>

      <!-- 演示模式：提供受控的"模拟付款"按钮，走后端 demo 回调端点 -->
      <div v-if="demo" class="demo-pay-actions">
        <el-alert
          type="warning"
          :closable="false"
          title="演示模式"
          description="当前为本地联调环境，点击下方按钮模拟用户付款。生产环境不存在此按钮。"
          show-icon
        />
        <el-button type="primary" class="demo-confirm-btn" :loading="paying" @click="handleDemoConfirm">
          <el-icon><Wallet /></el-icon>
          模拟支付完成
        </el-button>
      </div>

      <!-- 倒计时（仅真实支付模式） -->
      <div class="pay-countdown" v-if="!demo">
        <span>二维码有效期：</span>
        <span class="countdown">{{ countdown }}s</span>
      </div>
      <p class="pay-tip" v-if="!demo">
        请使用{{ payMethod === 'alipay' ? '支付宝' : '微信' }}扫一扫完成支付，支付成功后自动跳转
      </p>
      <p class="pay-tip polling-tip" v-if="!demo && !qrError">
        <el-icon class="is-loading"><Loading /></el-icon>
        正在检测支付结果...
      </p>
    </div>
  </el-dialog>
</template>

<script setup lang="ts">
/**
 * QRCodePay - 扫码支付弹窗
 * 1. 二维码内容由后端 /payment/pay 返回（微信 Native code_url / 支付宝 qr_code），本地渲染
 * 2. 真实模式轮询 /payment/status/{orderId} 检测支付结果（后端含主动查单补偿）
 * 3. demo 模式下提供受控的"模拟付款"按钮，走后端 demo confirm 端点
 */
import { ref, watch, onBeforeUnmount } from 'vue'
import { Loading, CircleCloseFilled, Wallet } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import QRCode from 'qrcode'
import { pollPayStatus, confirmDemoPay } from '@/api/payment'

const visible = defineModel<boolean>({ default: false })

const props = withDefaults(defineProps<{
  orderNo?: string
  orderId?: number
  qrCode?: string
  demo?: boolean
  payMethod?: string
  amount?: number
}>(), {
  orderNo: '',
  orderId: 0,
  qrCode: '',
  demo: false,
  payMethod: 'wechat',
  amount: 0,
})

const emit = defineEmits<{ close: [], success: [] }>()

const qrDataUrl = ref('')
const qrError = ref('')
const countdown = ref(300)
const paying = ref(false)
let countdownTimer: ReturnType<typeof setInterval> | null = null
let pollTimer: ReturnType<typeof setInterval> | null = null
let stopped = false

/** 把后端返回的二维码内容渲染为本地 dataURL */
async function renderQrCode(content: string) {
  qrError.value = ''
  qrDataUrl.value = ''
  if (!content) {
    qrError.value = '支付凭证获取失败，请重新下单'
    return
  }
  try {
    qrDataUrl.value = await QRCode.toDataURL(content, {
      width: 220,
      margin: 1,
      color: { dark: '#1D2129', light: '#FFFFFF' },
    })
  } catch {
    qrError.value = '二维码生成失败，请重试'
  }
}

/** 轮询支付状态：paid 则停止并通知父组件 */
async function checkStatus() {
  if (stopped || !props.orderId) return
  try {
    const res = await pollPayStatus(props.orderId)
    if (res.data?.status === 'paid') {
      stopped = true
      emit('success')
    } else if (res.data?.status === 'cancelled' || res.data?.status === 'expired' || res.data?.status === 'refunded') {
      stopped = true
      qrError.value = '订单已关闭，请重新下单'
      clearTimers()
    }
  } catch {
    // 轮询瞬时失败不打断，下一轮继续
  }
}

/** 演示模式：受控模拟付款 */
async function handleDemoConfirm() {
  if (!props.orderNo) {
    ElMessage.error('订单号缺失，无法模拟支付')
    return
  }
  paying.value = true
  try {
    await confirmDemoPay(props.orderNo)
    // 确认后立即查一次状态
    await checkStatus()
    ElMessage.success('模拟支付完成')
  } catch {
    ElMessage.error('模拟支付失败，请重试')
  } finally {
    paying.value = false
  }
}

function clearTimers() {
  if (countdownTimer) { clearInterval(countdownTimer); countdownTimer = null }
  if (pollTimer) { clearInterval(pollTimer); pollTimer = null }
}

function handleClose() {
  clearTimers()
  stopped = true
  emit('close')
}

watch(visible, async (val) => {
  clearTimers()
  stopped = false
  if (!val) return

  countdown.value = 300
  await renderQrCode(props.qrCode)

  // 演示模式无需倒计时和轮询（由按钮驱动）
  if (props.demo) return

  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearTimers()
      qrError.value = '二维码已过期，请重新下单'
    }
  }, 1000)

  // 每 2 秒轮询一次支付状态
  await checkStatus()
  pollTimer = setInterval(checkStatus, 2000)
})

onBeforeUnmount(clearTimers)
</script>

<style scoped lang="scss">
.qrcode-pay { text-align: center; }
.pay-amount-hint { font-size: 14px; color: #4E5969; margin-bottom: 14px; strong { color: #F53F3F; font-size: 18px; } }
.qrcode-area { display: flex; justify-content: center; margin-bottom: 16px; }
.qrcode-placeholder {
  width: 220px; height: 220px; border: 2px dashed #E5E6EB; border-radius: 12px;
  display: flex; align-items: center; justify-content: center;
  img { width: 100%; height: 100%; object-fit: contain; border-radius: 8px; }
}
.qrcode-loading, .qrcode-error {
  display: flex; flex-direction: column; align-items: center; gap: 8px;
  font-size: 13px; color: #86909C;
}
.qrcode-error { color: #F53F3F; .el-icon { font-size: 28px; } }
.demo-pay-actions { margin: 8px 0 16px; .demo-confirm-btn { width: 100%; margin-top: 14px; height: 42px; font-weight: 700; } }
.pay-countdown { font-size: 14px; color: #86909C; margin-bottom: 8px; .countdown { font-weight: 700; color: #F53F3F; } }
.pay-tip { font-size: 13px; color: #86909C; }
.polling-tip { display: flex; align-items: center; justify-content: center; gap: 6px; margin-top: 8px; }
</style>
