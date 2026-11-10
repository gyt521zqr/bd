<template>
  <!-- 支付页 -->
  <div class="pay-page">
    <div class="pay-header">
      <div class="pay-header-bg"></div>
      <div class="pay-header-content">
        <h2 class="page-title">
          <el-icon><WalletFilled /></el-icon>
          订单支付
        </h2>
        <p class="page-subtitle">安全便捷的支付体验</p>
      </div>
    </div>

    <div class="pay-content">
      <!-- 订单信息卡片（3D倾斜效果） -->
      <div
        v-if="orderReady"
        class="order-card"
        ref="orderCardRef"
        @mousemove="handleCardMove"
        @mouseleave="handleCardLeave"
        :style="cardStyle"
      >
        <div class="card-shine" :style="shineStyle"></div>
        <div class="card-inner">
          <div class="card-badge">
            <el-icon><Document /></el-icon>
            订单信息
          </div>
          <div class="info-rows">
            <div class="info-row">
              <span class="label">
                <el-icon><Document /></el-icon>
                订单编号
              </span>
              <span class="value order-no">{{ order.orderNo }}</span>
            </div>
            <div class="info-row">
              <span class="label">
                <el-icon><Goods /></el-icon>
                商品名称
              </span>
              <span class="value product-name">{{ order.productName }}</span>
            </div>
            <div class="divider"></div>
            <div class="info-row pay-amount-row">
              <span class="label">
                <el-icon><Money /></el-icon>
                支付金额
              </span>
              <div class="amount-wrap">
                <span class="currency">¥</span>
                <span class="amount" :class="{ 'amount-pop': amountPopped }" @click="popAmount">
                  {{ formatMoney(order.payAmount) }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>
      <div v-else class="order-empty-state">
        <el-empty :description="orderError" />
      </div>

      <!-- 支付方式 -->
      <div class="pay-method-section" v-if="orderReady">
        <h3 class="section-title">
          <el-icon><CreditCard /></el-icon>
          选择支付方式
        </h3>
        <div class="method-grid">
          <div
            v-for="method in payMethods"
            :key="method.value"
            class="method-card"
            :class="{ active: payMethod === method.value, selected: selectedMethod === method.value }"
            @click="selectMethod(method.value)"
          >
            <div class="method-radio">
              <div class="radio-inner" v-if="payMethod === method.value">
                <el-icon><Check /></el-icon>
              </div>
            </div>
            <div class="method-icon" :style="{ background: method.bgColor }">
              <el-icon :size="28"><component :is="method.icon" /></el-icon>
            </div>
            <div class="method-info">
              <span class="method-name">{{ method.label }}</span>
              <span class="method-desc">{{ method.desc }}</span>
            </div>
            <div class="method-check" v-if="payMethod === method.value">
              <el-icon><CircleCheckFilled /></el-icon>
            </div>
          </div>
        </div>
      </div>

      <QRCodePay
        v-if="orderReady && payMethod !== 'balance'"
        :visible="showQR"
        :order-no="order.orderNo"
        @close="showQR = false"
        @success="handlePaySuccess"
      />

      <!-- 支付按钮 -->
      <div class="pay-action">
        <div class="pay-summary" v-if="orderReady">
          <span class="summary-label">应付金额</span>
          <span class="summary-amount">
            <span class="summary-currency">¥</span>
            {{ formatMoney(order.payAmount) }}
          </span>
        </div>
        <div class="pay-summary" v-else>
          <span class="summary-label">订单不可用，无法支付</span>
        </div>
        <el-button
          type="primary"
          size="large"
          class="pay-btn"
          :loading="paying"
          :disabled="!orderReady"
          @click="handlePay"
        >
          <el-icon><Wallet /></el-icon>
          {{ payMethod === 'balance' ? '确认支付' : '扫码支付' }}
        </el-button>
      </div>

      <!-- 安全提示 -->
      <div class="security-tips" v-if="orderReady">
        <el-icon><Lock /></el-icon>
        <span>支付过程已加密保护，请放心支付</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * PayPage - 支付页
 * 选择支付方式，扫码或余额支付
 * 订单信息来自真实接口（订单详情 / 创建订单），无参数或加载失败时显示空态并禁用支付
 */
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  ChatDotRound, CreditCard, Wallet, WalletFilled, Document,
  Goods, Money, Check, CircleCheckFilled, Lock
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import QRCodePay from '@/components/payment/QRCodePay.vue'
import { createOrder, getOrderDetail, payOrder } from '@/api/payment'

const route = useRoute()
const router = useRouter()
const payMethod = ref('wechat')
const paying = ref(false)
const showQR = ref(false)
const selectedMethod = ref('')
const amountPopped = ref(false)

// 3D卡片效果
const orderCardRef = ref<HTMLElement>()
const cardRotateX = ref(0)
const cardRotateY = ref(0)
const shineX = ref(50)
const shineY = ref(50)

const cardStyle = computed(() => ({
  transform: `perspective(1000px) rotateX(${cardRotateX.value}deg) rotateY(${cardRotateY.value}deg)`,
}))

const shineStyle = computed(() => ({
  background: `radial-gradient(circle at ${shineX.value}% ${shineY.value}%, rgba(255,255,255,0.2) 0%, transparent 60%)`,
}))

function handleCardMove(e: MouseEvent) {
  if (!orderCardRef.value) return
  const rect = orderCardRef.value.getBoundingClientRect()
  const x = e.clientX - rect.left
  const y = e.clientY - rect.top
  const centerX = rect.width / 2
  const centerY = rect.height / 2
  cardRotateY.value = ((x - centerX) / centerX) * 5
  cardRotateX.value = ((centerY - y) / centerY) * 5
  shineX.value = (x / rect.width) * 100
  shineY.value = (y / rect.height) * 100
}

function handleCardLeave() {
  cardRotateX.value = 0
  cardRotateY.value = 0
  shineX.value = 50
  shineY.value = 50
}

function popAmount() {
  amountPopped.value = true
  setTimeout(() => { amountPopped.value = false }, 400)
}

const payMethods = [
  {
    value: 'wechat',
    label: '微信支付',
    desc: '推荐使用微信扫码支付',
    icon: ChatDotRound,
    bgColor: 'linear-gradient(135deg, #36D399, #36D399)',
  },
  {
    value: 'alipay',
    label: '支付宝',
    desc: '支持花呗分期付款',
    icon: CreditCard,
    bgColor: 'linear-gradient(135deg, #165DFF, #0E42D2)',
  },
  {
    value: 'balance',
    label: '余额支付',
    desc: '账户余额: ¥0.00',
    icon: Wallet,
    bgColor: 'linear-gradient(135deg, #FF7D00, #E06800)',
  },
]

function selectMethod(value: string) {
  selectedMethod.value = value
  payMethod.value = value
}

// 订单信息：初始为空，只有接口返回真实订单后才可支付
const order = ref({
  id: 0,
  orderNo: '',
  productName: '',
  payAmount: 0,
})

// 订单是否加载成功；未成功时显示错误空态并禁用支付
const orderReady = ref(false)
const orderError = ref('订单不存在')

function formatMoney(cents: number) {
  return (cents / 100).toFixed(2)
}

onMounted(async () => {
  // 1. 优先：路由参数携带 orderId（如从订单列表跳转 /pay/123），调用 getOrderDetail 获取订单详情
  const paramOrderId = Number(route.params.orderId)
  if (paramOrderId) {
    await loadOrderDetail(paramOrderId)
    return
  }

  // 2. 兼容 query.orderId 场景
  const queryOrderId = Number(route.query.orderId)
  if (queryOrderId) {
    await loadOrderDetail(queryOrderId)
    return
  }

  // 3. 创建新订单：有完整商品参数时调用 createOrder
  const { productType, productId, productName, amount } = route.query
  if (productType && productId && productName && amount) {
    try {
      const res = await createOrder({
        productType: productType as string,
        productId: Number(productId),
      })
      if (res.data) {
        order.value = {
          id: res.data.id,
          orderNo: res.data.orderNo,
          productName: res.data.productName || (productName as string),
          payAmount: res.data.payAmount || Number(amount),
        }
        orderReady.value = true
      } else {
        orderError.value = '订单信息获取失败'
      }
    } catch {
      // 创建失败：保持空态，由模板显示「订单信息获取失败」
      orderError.value = '订单信息获取失败'
    }
  }
  // 4. 无任何订单参数：保持空态，由模板显示「订单不存在」
})

/** 加载订单详情：失败或返回空时保持空态，不回退任何虚构订单 */
async function loadOrderDetail(id: number) {
  try {
    const res = await getOrderDetail(id)
    if (res.data) {
      order.value = {
        id: res.data.id,
        orderNo: res.data.orderNo,
        productName: res.data.productName,
        payAmount: res.data.payAmount,
      }
      orderReady.value = true
    } else {
      orderError.value = '订单信息获取失败'
    }
  } catch {
    // 接口失败或返回空：保持空态，由模板显示「订单信息获取失败」
    orderError.value = '订单信息获取失败'
  }
}

async function handlePay() {
  if (payMethod.value === 'balance') {
    paying.value = true
    try {
      // 尝试调用真实支付API
      await payOrder({
        orderId: order.value.id || 0,
        payMethod: payMethod.value as 'wechat' | 'alipay' | 'balance',
      })
      handlePaySuccess()
    } catch {
      ElMessage.error('支付失败，请稍后重试')
    } finally {
      paying.value = false
    }
  } else {
    showQR.value = true
  }
}

function handlePaySuccess() {
  ElMessage.success('支付成功')
  router.push('/order')
}
</script>

<style scoped lang="scss">
$primary: #165DFF;
$action: #FF7D00;

.pay-page {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 40px;
}

// ==================== 头部 ====================
.pay-header {
  position: relative;
  padding: 40px 0 60px;
  overflow: hidden;
}

.pay-header-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 50%, #7B61FF 100%);

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 80px;
    background: linear-gradient(to top, #f7f8fa, transparent);
  }
}

.pay-header-content {
  position: relative;
  z-index: 1;
  max-width: 600px;
  margin: 0 auto;
  padding: 0 20px;
  text-align: center;
}

.page-title {
  font-size: 28px;
  font-weight: 800;
  color: #fff;
  margin: 0 0 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;

  .el-icon {
    font-size: 32px;
  }
}

.page-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
}

// ==================== 内容区域 ====================
.pay-content {
  max-width: 560px;
  margin: -40px auto 0;
  padding: 0 20px;
  position: relative;
  z-index: 2;
}

// ==================== 3D订单卡片 ====================
.order-card {
  position: relative;
  background: linear-gradient(135deg, #ffffff 0%, #F7F8FA 100%);
  border-radius: 20px;
  padding: 28px;
  box-shadow: 0 8px 32px rgba(22, 93, 255, 0.12), 0 2px 8px rgba(0, 0, 0, 0.06);
  margin-bottom: 24px;
  transition: transform 0.15s ease-out;
  transform-style: preserve-3d;
  overflow: hidden;
  border: 1px solid rgba(22, 93, 255, 0.06);
}

// 订单不可用时的空态占位（沿用卡片风格）
.order-empty-state {
  background: linear-gradient(135deg, #ffffff 0%, #F7F8FA 100%);
  border-radius: 20px;
  padding: 24px;
  margin-bottom: 24px;
  box-shadow: 0 8px 32px rgba(22, 93, 255, 0.12), 0 2px 8px rgba(0, 0, 0, 0.06);
  border: 1px solid rgba(22, 93, 255, 0.06);
}

.card-shine {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 1;
}

.card-inner {
  position: relative;
  z-index: 2;
}

.card-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  font-weight: 600;
  color: #165DFF;
  background: rgba(22, 93, 255, 0.06);
  padding: 6px 14px;
  border-radius: 20px;
  margin-bottom: 20px;

  .el-icon {
    font-size: 14px;
  }
}

.info-rows {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 14px;

  .label {
    display: flex;
    align-items: center;
    gap: 6px;
    color: #86909C;

    .el-icon {
      font-size: 15px;
    }
  }

  .value {
    color: #4E5969;
    font-weight: 500;
    text-align: right;

    &.order-no {
      font-family: monospace;
      font-size: 13px;
      background: #F7F8FA;
      padding: 4px 10px;
      border-radius: 6px;
    }

    &.product-name {
      max-width: 240px;
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }
}

.divider {
  height: 1px;
  background: linear-gradient(90deg, transparent, #E5E6EB, transparent);
  margin: 4px 0;
}

.pay-amount-row {
  .label {
    color: #1D2129;
    font-weight: 600;
    font-size: 15px;
  }
}

.amount-wrap {
  display: flex;
  align-items: baseline;
  gap: 2px;

  .currency {
    font-size: 20px;
    font-weight: 600;
    color: #F53F3F;
  }

  .amount {
    font-size: 36px;
    font-weight: 800;
    color: #F53F3F;
    line-height: 1;
    cursor: pointer;
    transition: transform 0.3s ease;
    display: inline-block;

    &.amount-pop {
      animation: amountPop 0.4s ease;
    }
  }
}

@keyframes amountPop {
  0% { transform: scale(1); }
  40% { transform: scale(1.25); }
  70% { transform: scale(0.95); }
  100% { transform: scale(1); }
}

// ==================== 支付方式 ====================
.pay-method-section {
  margin-bottom: 24px;
}

.section-title {
  font-size: 15px;
  font-weight: 700;
  color: #1D2129;
  margin: 0 0 14px;
  display: flex;
  align-items: center;
  gap: 8px;

  .el-icon {
    color: #165DFF;
    font-size: 18px;
  }
}

.method-grid {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.method-card {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 18px;
  background: $bg-white;
  border-radius: 14px;
  border: 2px solid transparent;
  cursor: pointer;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  position: relative;
  overflow: hidden;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    border-color: rgba(22, 93, 255, 0.15);
  }

  &.active {
    border-color: #165DFF;
    background: linear-gradient(135deg, rgba(22, 93, 255, 0.03), rgba(64, 128, 255, 0.05));
    box-shadow: 0 4px 16px rgba(22, 93, 255, 0.1);

    .method-radio {
      border-color: #165DFF;
      background: #165DFF;
    }
  }

  &.selected {
    animation: methodSelect 0.4s ease;
  }
}

@keyframes methodSelect {
  0% { transform: scale(1); }
  30% { transform: scale(0.97); }
  60% { transform: scale(1.02); }
  100% { transform: scale(1); }
}

.method-radio {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  border: 2px solid #C9CDD4;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.25s ease;

  .radio-inner {
    color: #fff;
    font-size: 12px;
  }
}

.method-icon {
  width: 44px;
  height: 44px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.method-info {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.method-name {
  font-size: 15px;
  font-weight: 600;
  color: #1D2129;
}

.method-desc {
  font-size: 12px;
  color: #86909C;
}

.method-check {
  color: #165DFF;
  font-size: 20px;
  animation: checkPop 0.3s ease;
}

@keyframes checkPop {
  0% { transform: scale(0); opacity: 0; }
  60% { transform: scale(1.2); }
  100% { transform: scale(1); opacity: 1; }
}

// ==================== 支付按钮区域 ====================
.pay-action {
  background: $bg-white;
  border-radius: 16px;
  padding: 20px 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 16px;
}

.pay-summary {
  display: flex;
  flex-direction: column;
  gap: 2px;

  .summary-label {
    font-size: 13px;
    color: #86909C;
  }

  .summary-amount {
    font-size: 24px;
    font-weight: 800;
    color: #F53F3F;

    .summary-currency {
      font-size: 16px;
      font-weight: 600;
    }
  }
}

.pay-btn {
  background: linear-gradient(135deg, #165DFF, #4080FF);
  border: none;
  color: #fff;
  font-weight: 700;
  font-size: 16px;
  padding: 0 36px;
  height: 50px;
  border-radius: 12px;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s ease;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(22, 93, 255, 0.35);
    background: linear-gradient(135deg, #0E42D2, #165DFF);
  }

  &:active {
    transform: translateY(0);
  }

  .el-icon {
    margin-right: 6px;
    font-size: 18px;
  }
}

// ==================== 安全提示 ====================
.security-tips {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 12px;
  color: #86909C;
  padding: 12px;

  .el-icon {
    font-size: 14px;
    color: #36D399;
  }
}

// ==================== 响应式 ====================
@media (max-width: 576px) {
  .pay-header {
    padding: 24px 0 48px;
  }

  .page-title {
    font-size: 22px;

    .el-icon {
      font-size: 26px;
    }
  }

  .pay-content {
    margin-top: -32px;
    padding: 0 16px;
  }

  .order-card {
    padding: 20px;
  }

  .amount-wrap .amount {
    font-size: 28px;
  }

  .pay-action {
    flex-direction: column;
    align-items: stretch;
    text-align: center;
  }

  .pay-btn {
    width: 100%;
  }
}
</style>
