<template>
  <!-- 优惠券中心 -->
  <div class="coupon-page">
    <div class="page-header">
      <h2 class="page-title">优惠券中心</h2>
      <p class="page-subtitle">领取专属优惠券，开启编程学习之旅</p>
    </div>

    <div class="tab-bar">
      <div
        v-for="tab in tabs"
        :key="tab.key"
        class="tab-item"
        :class="{ active: activeTab === tab.key }"
        @click="activeTab = tab.key"
      >
        {{ tab.label }}
      </div>
    </div>

    <div class="coupon-grid">
      <div
        v-for="coupon in filteredCoupons"
        :key="coupon.id"
        class="coupon-card"
        :class="{ used: coupon.status !== 'available', expired: coupon.status === 'expired' }"
      >
        <!-- 撕票虚线 -->
        <div class="tear-line"></div>

        <div class="coupon-left">
          <span class="coupon-value">{{ coupon.type === 'fixed' ? formatMoney(coupon.value) : coupon.value + '%' }}</span>
          <span class="coupon-condition">满{{ formatMoney(coupon.minAmount) }}可用</span>
        </div>

        <div class="coupon-divider">
          <div class="semi-circle top"></div>
          <div class="dashed-line"></div>
          <div class="semi-circle bottom"></div>
        </div>

        <div class="coupon-right">
          <h4>{{ coupon.name }}</h4>
          <p>{{ coupon.description }}</p>
          <span class="coupon-expire">
            <el-icon size="13"><Timer /></el-icon>
            有效期至 {{ coupon.expireTime }}
          </span>
        </div>

        <div class="coupon-action">
          <el-button
            v-if="coupon.status === 'available' && activeTab === 'available'"
            type="primary"
            size="small"
            class="claim-btn pulse-btn"
            @click="handleClaim(coupon)"
          >
            <el-icon><Present /></el-icon>
            立即领取
          </el-button>
          <el-button
            v-else-if="coupon.status === 'available'"
            type="primary"
            size="small"
            plain
            class="use-btn"
            @click="handleUse(coupon)"
          >
            去使用
          </el-button>
          <el-tag v-else-if="coupon.status === 'used'" type="info" size="small" effect="dark" class="status-tag">已使用</el-tag>
          <el-tag v-else type="danger" size="small" effect="dark" class="status-tag">已过期</el-tag>
        </div>

        <!-- 已过期灰色遮罩 -->
        <div v-if="coupon.status === 'expired'" class="expired-overlay">
          <div class="expired-stamp">已过期</div>
        </div>
      </div>
    </div>

    <el-empty v-if="filteredCoupons.length === 0" description="暂无优惠券" class="empty-state">
      <template #image>
        <el-icon :size="64" color="#C9CDD4"><Ticket /></el-icon>
      </template>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
/**
 * CouponPage - 优惠券中心
 * 领取和使用优惠券
 */
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Timer, Present, Ticket } from '@element-plus/icons-vue'

const activeTab = ref('available')

const tabs = [
  { key: 'available', label: '可领取' },
  { key: 'mine', label: '我的优惠券' },
]

const coupons = ref([
  { id: 1, name: '新人优惠券', description: '新用户专享，全场通用', type: 'fixed', value: 5000, minAmount: 10000, expireTime: '2024-06-30', status: 'available' },
  { id: 2, name: '课程折扣券', description: '所有课程通用，不限次数', type: 'percent', value: 20, minAmount: 0, expireTime: '2024-04-30', status: 'available' },
  { id: 3, name: '会员折扣券', description: '购买会员专用，超值优惠', type: 'fixed', value: 10000, minAmount: 30000, expireTime: '2024-05-31', status: 'available' },
  { id: 4, name: '满减优惠券', description: '满200减30，课程适用', type: 'fixed', value: 3000, minAmount: 20000, expireTime: '2024-03-15', status: 'used' },
  { id: 5, name: '限时折扣', description: '限时8折，先到先得', type: 'percent', value: 20, minAmount: 5000, expireTime: '2024-02-28', status: 'expired' },
])

const filteredCoupons = computed(() => {
  if (activeTab.value === 'available') return coupons.value.filter(c => c.status === 'available')
  return coupons.value.filter(c => c.status !== 'available')
})

function formatMoney(cents: number) { return `¥${(cents / 100).toFixed(0)}` }
function handleClaim(_coupon: any) { ElMessage.success('领取成功') }
function handleUse(_coupon: any) { ElMessage.info('跳转到课程页面') }
</script>

<style scoped lang="scss">

.coupon-page {
  max-width: 900px;
  margin: 0 auto;
  padding: 24px 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 28px;

  .page-title {
    font-size: 24px;
    font-weight: 700;
    color: $text-title;
    margin: 0 0 8px;
  }

  .page-subtitle {
    font-size: 14px;
    color: $text-muted;
    margin: 0;
  }
}

// Tab栏
.tab-bar {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-bottom: 24px;
}

.tab-item {
  padding: 8px 24px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 500;
  color: $text-body;
  background: $bg-white;
  border: 1px solid #E5E6EB;
  cursor: pointer;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.25s ease;

  &:hover {
    border-color: $primary-color;
    color: $primary-color;
  }

  &.active {
    background: $primary-color;
    color: #fff;
    border-color: $primary-color;
    box-shadow: 0 4px 12px rgba($primary-color, 0.25);
  }
}

// 优惠券网格
.coupon-grid {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.coupon-card {
  display: flex;
  align-items: stretch;
  background: $bg-white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  position: relative;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s ease;
  border: 1px solid transparent;

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 28px rgba(0, 0, 0, 0.1);
    border-color: rgba($primary-color, 0.1);
  }

  &.used {
    opacity: 0.7;
  }

  &.expired {
    opacity: 0.6;
  }
}

// 撕票虚线效果
.tear-line {
  position: absolute;
  left: 132px;
  top: 0;
  bottom: 0;
  width: 0;
  border-left: 2px dashed #E5E6EB;
  z-index: 2;
}

.coupon-left {
  width: 132px;
  padding: 24px 16px;
  background: linear-gradient(135deg, #FF7D00, #FF9500);
  color: #fff;
  text-align: center;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  flex-shrink: 0;
  position: relative;
}

.coupon-divider {
  position: relative;
  width: 16px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;

  .semi-circle {
    width: 16px;
    height: 8px;
    background: $bg-page;
    position: absolute;

    &.top {
      top: -1px;
      border-radius: 0 0 16px 16px;
      border-bottom: 1px dashed #E5E6EB;
    }

    &.bottom {
      bottom: -1px;
      border-radius: 16px 16px 0 0;
      border-top: 1px dashed #E5E6EB;
    }
  }

  .dashed-line {
    width: 0;
    height: calc(100% - 16px);
    border-left: 2px dashed #E5E6EB;
  }
}

.coupon-value {
  font-size: 32px;
  font-weight: 800;
  line-height: 1;
}

.coupon-condition {
  font-size: 12px;
  opacity: 0.9;
  font-weight: 500;
}

.coupon-right {
  flex: 1;
  padding: 20px 16px;
  display: flex;
  flex-direction: column;
  justify-content: center;

  h4 {
    font-size: 16px;
    font-weight: 600;
    color: $text-title;
    margin: 0 0 6px;
  }

  p {
    font-size: 13px;
    color: $text-muted;
    margin: 0 0 10px;
  }

  .coupon-expire {
    font-size: 12px;
    color: $text-muted;
    display: flex;
    align-items: center;
    gap: 4px;

    .el-icon {
      color: $action-color;
    }
  }
}

.coupon-action {
  padding: 20px 24px;
  display: flex;
  align-items: center;
  flex-shrink: 0;
}

// 脉冲动画按钮
.pulse-btn {
  animation: pulse 2s ease-in-out infinite;
  will-change: transform, opacity;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 0 0 0 rgba($primary-color, 0.4);
  }
  50% {
    box-shadow: 0 0 0 8px rgba($primary-color, 0);
  }
}

.use-btn {
  font-weight: 500;
}

.status-tag {
  font-weight: 600;
}

// 已过期灰色遮罩
.expired-overlay {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 16px;
  backdrop-filter: grayscale(0.8);
  pointer-events: none;
}

.expired-stamp {
  font-size: 28px;
  font-weight: 900;
  color: #86909C;
  border: 3px solid #86909C;
  padding: 4px 16px;
  border-radius: 8px;
  transform: rotate(-15deg);
  opacity: 0.7;
  letter-spacing: 4px;
}

.empty-state {
  margin-top: 60px;
}

// 响应式
@media (max-width: 768px) {
  .coupon-card {
    flex-direction: column;
  }

  .coupon-left {
    width: 100%;
    padding: 16px;
    flex-direction: row;
    justify-content: center;
    gap: 12px;
  }

  .tear-line,
  .coupon-divider {
    display: none;
  }

  .coupon-right {
    padding: 16px;
  }

  .coupon-action {
    padding: 0 16px 16px;
  }
}
</style>
