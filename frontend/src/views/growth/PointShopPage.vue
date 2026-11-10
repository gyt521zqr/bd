<template>
  <!-- 积分商城 -->
  <div class="point-shop-page">
    <div class="shop-header">
      <h2>积分商城</h2>
      <div class="my-points">
        <PointCounter :points="12800" />
      </div>
    </div>
    <div class="shop-grid">
      <div
        v-for="item in shopItems"
        :key="item.id"
        class="shop-item"
        :class="{ 'out-of-stock': item.points > 12800 }"
      >
        <div class="item-cover">
          <img v-if="item.cover" :src="item.cover" :alt="item.name" />
          <div v-else class="cover-fallback">
            <el-icon :size="36"><Present /></el-icon>
          </div>
          <!-- 积分价格标签 -->
          <div class="price-tag">
            <el-icon :size="14"><Coin /></el-icon>
            <span>{{ item.points }}</span>
          </div>
          <!-- 库存不足遮罩 -->
          <div v-if="item.points > 12800" class="stock-overlay">
            <span>积分不足</span>
          </div>
        </div>
        <div class="item-body">
          <h4>{{ item.name }}</h4>
          <p>{{ item.description }}</p>
          <div class="item-bottom">
            <span class="item-price">
              <el-icon :size="16"><Coin /></el-icon>
              <span>{{ item.points }} 积分</span>
            </span>
            <button
              class="exchange-btn"
              :class="{ 'btn-disabled': item.points > 12800 }"
              :disabled="item.points > 12800"
              @click="handleExchange(item)"
            >
              兑换
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * PointShopPage - 积分商城
 * 用积分兑换奖品
 * 新增：积分价格标签、兑换按钮hover效果、库存不足灰色遮罩
 */
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { Coin, Present } from '@element-plus/icons-vue'
import PointCounter from '@/components/growth/PointCounter.vue'

const shopItems = ref([
  { id: 1, name: '编程达人勋章', description: '专属勋章，展示你的实力', cover: '', points: 500 },
  { id: 2, name: '7天VIP体验卡', description: '免费体验7天会员权益', cover: '', points: 1000 },
  { id: 3, name: '精美笔记本', description: '码高定制编程笔记本', cover: '', points: 2000 },
  { id: 4, name: '30天VIP月卡', description: '畅学所有付费课程', cover: '', points: 5000 },
  { id: 5, name: '编程机器人套件', description: '实物奖品，包邮到家', cover: '', points: 10000 },
  { id: 6, name: '课程优惠券', description: '任意课程立减50元', cover: '', points: 800 },
])

function handleExchange(item: any) {
  if (item.points > 12800) {
    ElMessage.warning('积分不足，无法兑换')
    return
  }
  ElMessage.success(`成功兑换：${item.name}`)
}
</script>

<style scoped lang="scss">
$bg: #F7F8FA;

.point-shop-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.shop-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  h2 {
    font-size: 22px;
    color: #1D2129;
  }
}

.shop-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}

.shop-item {
  background: $bg-white;
  border-radius: 16px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: transform 0.3s ease, box-shadow 0.3s ease;

  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.12);
  }

  /* 库存不足状态 */
  &.out-of-stock {
    .item-cover {
      filter: grayscale(0.6);
    }

    .item-body {
      h4, p {
        color: #86909C;
      }
    }

    .item-price {
      color: #86909C;
    }
  }
}

.item-cover {
  position: relative;
  height: 160px;
  background: linear-gradient(135deg, #E8F0FF, #E8F0FF);
  display: flex;
  align-items: center;
  justify-content: center;

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }
}

.cover-fallback {
  color: #165DFF;
  opacity: 0.5;
}

/* 积分价格标签 */
.price-tag {
  position: absolute;
  top: 10px;
  right: 10px;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: linear-gradient(135deg, #FF7D00, #FF9500);
  color: #fff;
  border-radius: 20px;
  font-size: 13px;
  font-weight: 600;
  box-shadow: 0 2px 8px rgba(255, 125, 0, 0.3);

  .el-icon {
    font-size: 14px;
  }
}

/* 库存不足灰色遮罩 */
.stock-overlay {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(2px);
  display: flex;
  align-items: center;
  justify-content: center;

  span {
    padding: 6px 16px;
    background: rgba(107, 114, 128, 0.9);
    color: #fff;
    border-radius: 20px;
    font-size: 13px;
    font-weight: 500;
  }
}

.item-body {
  padding: 12px 16px 16px;

  h4 {
    font-size: 16px;
    color: #1D2129;
    margin: 0 0 4px;
    transition: color 0.25s ease;
  }

  p {
    font-size: 13px;
    color: #86909C;
    margin: 0 0 12px;
    transition: color 0.25s ease;
  }
}

.item-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.item-price {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 16px;
  font-weight: 700;
  color: #FF7D00;
  transition: color 0.25s ease;

  .el-icon {
    color: #FF9500;
  }
}

/* 兑换按钮 */
.exchange-btn {
  padding: 6px 18px;
  border-radius: 8px;
  border: none;
  background: linear-gradient(135deg, #165DFF, #4080FF);
  color: #fff;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s ease;
  position: relative;
  overflow: hidden;

  &:hover:not(:disabled) {
    transform: translateY(-2px);
    box-shadow: 0 6px 16px rgba(22, 93, 255, 0.35);
    background: linear-gradient(135deg, #0E42D2, #165DFF);
  }

  &:active:not(:disabled) {
    transform: translateY(0);
    box-shadow: 0 2px 8px rgba(22, 93, 255, 0.25);
  }

  /* 点击时涟漪效果 */
  &::after {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    width: 0;
    height: 0;
    background: rgba(255, 255, 255, 0.3);
    border-radius: 50%;
    transform: translate(-50%, -50%);
    transition: width 0.4s ease, height 0.4s ease;
  }

  &:active:not(:disabled)::after {
    width: 200%;
    height: 200%;
  }

  &.btn-disabled {
    background: #E5E7EB;
    color: #86909C;
    cursor: not-allowed;
    transform: none;
    box-shadow: none;
  }
}

// ==================== 响应式适配 ====================
@media (max-width: 768px) {
  .shop-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
  }

  .shop-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
}

@media (max-width: 480px) {
  .shop-grid {
    grid-template-columns: 1fr;
  }
}
</style>
