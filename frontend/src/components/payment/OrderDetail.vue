<template>
  <!-- 订单详情 -->
  <div class="order-detail" :class="`status-${order.status}`">
    <div class="order-header">
      <div class="header-left">
        <div class="order-icon" :class="order.status">
          <el-icon :size="20">
            <CircleCheck v-if="order.status === 'paid'" />
            <Timer v-else-if="order.status === 'pending'" />
            <CircleClose v-else />
          </el-icon>
        </div>
        <div class="header-info">
          <h3 class="product-name">{{ order.productName }}</h3>
          <span class="order-no">{{ order.orderNo }}</span>
        </div>
      </div>
      <div class="status-tag" :class="order.status">
        <el-icon v-if="order.status === 'paid'" :size="14"><CircleCheck /></el-icon>
        <el-icon v-else-if="order.status === 'pending'" :size="14"><Timer /></el-icon>
        <el-icon v-else :size="14"><CircleClose /></el-icon>
        {{ statusLabel }}
      </div>
    </div>

    <div class="order-body">
      <div class="info-grid">
        <div class="info-cell">
          <span class="cell-label">商品类型</span>
          <span class="cell-value">
            <el-tag size="small" effect="plain" :type="productTypeType">{{ productTypeLabel }}</el-tag>
          </span>
        </div>
        <div class="info-cell">
          <span class="cell-label">创建时间</span>
          <span class="cell-value">{{ order.createTime }}</span>
        </div>
        <div class="info-cell" v-if="order.payTime">
          <span class="cell-label">支付时间</span>
          <span class="cell-value">{{ order.payTime }}</span>
        </div>
        <div class="info-cell">
          <span class="cell-label">支付金额</span>
          <span class="cell-value amount">
            <span class="amount-currency">¥</span>
            {{ (order.payAmount / 100).toFixed(2) }}
          </span>
        </div>
      </div>
    </div>

    <div class="order-footer">
      <div class="footer-left">
        <span class="time-hint" v-if="order.status === 'pending'">
          <el-icon><Clock /></el-icon>
          请在30分钟内完成支付
        </span>
      </div>
      <div class="order-actions">
        <el-button
          v-if="order.status === 'pending'"
          type="primary"
          size="small"
          class="action-btn pay-btn"
          @click="$emit('pay', order)"
        >
          <el-icon><Wallet /></el-icon>
          去支付
        </el-button>
        <el-button
          v-if="order.status === 'pending'"
          size="small"
          class="action-btn cancel-btn"
          @click="$emit('cancel', order)"
        >
          <el-icon><Close /></el-icon>
          取消订单
        </el-button>
        <el-button
          v-if="order.status === 'paid'"
          size="small"
          class="action-btn detail-btn"
        >
          <el-icon><Document /></el-icon>
          查看详情
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * OrderDetail - 订单详情组件
 */
import { computed } from 'vue'
import { CircleCheck, Timer, CircleClose, Wallet, Close, Document, Clock } from '@element-plus/icons-vue'

const props = defineProps<{
  order: { id: number; orderNo: string; productName: string; productType: string; payAmount: number; status: string; createTime: string; payTime: string | null }
}>()

defineEmits<{ pay: [order: any], cancel: [order: any] }>()

const statusLabel = computed(() => {
  const map: Record<string, string> = { pending: '待支付', paid: '已支付', cancelled: '已取消', refunded: '已退款' }
  return map[props.order.status] || props.order.status
})

const productTypeLabel = computed(() => {
  const map: Record<string, string> = { course: '课程', membership: '会员', goods: '商品' }
  return map[props.order.productType] || props.order.productType
})

const productTypeType = computed(() => {
  const map: Record<string, 'primary' | 'warning' | 'success' | 'info'> = { course: 'primary', membership: 'warning', goods: 'success' }
  return map[props.order.productType] || 'info'
})
</script>

<style scoped lang="scss">
.order-detail {
  background: $bg-white;
  border-radius: 14px;
  padding: 20px 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  animation: orderSlideIn 0.4s ease forwards;
  opacity: 0;
  transform: translateY(12px);
  border-left: 4px solid transparent;

  &:hover {
    transform: translateY(-3px);
    box-shadow: 0 8px 28px rgba(0, 0, 0, 0.1);
  }

  &.status-pending {
    border-left-color: #FF9500;
  }

  &.status-paid {
    border-left-color: #36D399;
  }

  &.status-cancelled {
    border-left-color: #86909C;
  }
}

@keyframes orderSlideIn {
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.order-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.header-left {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.order-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;

  &.paid {
    background: rgba(54, 211, 153, 0.1);
    color: #36D399;
  }

  &.pending {
    background: rgba(255, 149, 0, 0.1);
    color: #FF9500;
  }

  &.cancelled {
    background: rgba(148, 163, 184, 0.1);
    color: #86909C;
  }
}

.header-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.product-name {
  font-size: 15px;
  font-weight: 700;
  color: #1D2129;
  margin: 0;
  line-height: 1.4;
}

.order-no {
  font-size: 12px;
  color: #86909C;
  font-family: monospace;
}

.status-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 600;
  padding: 5px 12px;
  border-radius: 20px;
  flex-shrink: 0;

  &.paid {
    background: rgba(54, 211, 153, 0.1);
    color: #36D399;
  }

  &.pending {
    background: rgba(255, 149, 0, 0.1);
    color: #FF9500;
  }

  &.cancelled {
    background: rgba(148, 163, 184, 0.1);
    color: #86909C;
  }
}

.order-body {
  margin-bottom: 16px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
}

.info-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;

  .cell-label {
    font-size: 12px;
    color: #86909C;
  }

  .cell-value {
    font-size: 13px;
    color: #4E5969;
    font-weight: 500;

    &.amount {
      font-size: 18px;
      font-weight: 800;
      color: #F53F3F;

      .amount-currency {
        font-size: 13px;
        font-weight: 600;
      }
    }
  }
}

.order-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 14px;
  border-top: 1px solid #F7F8FA;
}

.time-hint {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #FF9500;

  .el-icon {
    font-size: 14px;
  }
}

.order-actions {
  display: flex;
  gap: 8px;
}

.action-btn {
  border-radius: 8px;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.25s ease;
  font-weight: 500;

  &:hover {
    transform: translateY(-2px);
  }

  &.pay-btn {
    background: linear-gradient(135deg, #165DFF, #4080FF);
    border: none;
    color: #fff;

    &:hover {
      box-shadow: 0 4px 16px rgba(22, 93, 255, 0.3);
    }

    .el-icon {
      margin-right: 4px;
    }
  }

  &.cancel-btn:hover {
    border-color: #F53F3F;
    color: #F53F3F;
    box-shadow: 0 4px 12px rgba(245, 63, 63, 0.1);
  }

  &.detail-btn:hover {
    border-color: #165DFF;
    color: #165DFF;
    box-shadow: 0 4px 12px rgba(22, 93, 255, 0.1);
  }
}

// ==================== 响应式 ====================
@media (max-width: 768px) {
  .info-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .order-footer {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
  }

  .order-actions {
    justify-content: flex-end;
  }
}

@media (max-width: 480px) {
  .order-detail {
    padding: 16px;
  }

  .info-grid {
    grid-template-columns: 1fr 1fr;
    gap: 10px;
  }

  .order-header {
    flex-direction: column;
    gap: 10px;
  }

  .status-tag {
    align-self: flex-start;
  }
}
</style>
