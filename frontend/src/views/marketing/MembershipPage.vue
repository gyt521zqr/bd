<template>
  <!-- 会员套餐 -->
  <div class="membership-page">
    <div class="page-header">
      <div class="crown-icon">
        <el-icon :size="48" color="#FF9500"><Trophy /></el-icon>
      </div>
      <h2>会员中心</h2>
      <p>开通会员，畅学所有课程，享受专属权益</p>
    </div>

    <div class="plan-grid">
      <div
        v-for="(plan, idx) in plans"
        :key="plan.id"
        class="plan-card"
        :class="{ recommended: plan.recommended }"
        :style="{ animationDelay: `${idx * 0.1}s` }"
      >
        <!-- 推荐标签 -->
        <div class="recommend-badge" v-if="plan.recommended">
          <el-icon><StarFilled /></el-icon>
          超值推荐
        </div>

        <!-- 皇冠图标 -->
        <div class="plan-crown" :class="`crown-level-${plan.id}`">
          <el-icon :size="32"><component :is="plan.id === 3 ? 'Medal' : 'Trophy'" /></el-icon>
        </div>

        <h3>{{ plan.name }}</h3>

        <div class="plan-price">
          <span class="price">{{ formatMoney(plan.price) }}</span>
          <span class="duration">/ {{ plan.duration }}天</span>
        </div>

        <div class="plan-features">
          <div v-for="feature in plan.features" :key="feature" class="feature-item">
            <span class="check-icon">
              <el-icon><CircleCheckFilled /></el-icon>
            </span>
            <span>{{ feature }}</span>
          </div>
        </div>

        <el-button
          :type="plan.recommended ? 'primary' : 'default'"
          size="large"
          round
          class="plan-btn"
          @click="handleBuy(plan)"
        >
          <el-icon v-if="plan.recommended"><Lightning /></el-icon>
          {{ plan.price > 0 ? '立即购买' : '免费体验' }}
        </el-button>
      </div>
    </div>

    <!-- 会员权益说明 -->
    <div class="benefits-section">
      <h3 class="benefits-title">会员专属权益</h3>
      <div class="benefits-grid">
        <div v-for="(benefit, idx) in benefits" :key="idx" class="benefit-card">
          <div class="benefit-icon" :style="{ background: benefit.bg }">
            <el-icon :size="24" color="#fff"><component :is="benefit.icon" /></el-icon>
          </div>
          <h4>{{ benefit.title }}</h4>
          <p>{{ benefit.desc }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * MembershipPage - 会员套餐
 * 月卡、季卡、年卡
 */
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import {
  CircleCheckFilled, Trophy, StarFilled, Lightning
} from '@element-plus/icons-vue'

const router = useRouter()

const plans = ref([
  { id: 3, name: '季度会员', price: 59800, duration: 90, recommended: false, features: ['畅学所有课程', '完整练习题库', '考试模拟', '学习报告', '1对1答疑', '优先客服'] },
  { id: 4, name: '年度会员', price: 169800, duration: 365, recommended: true, features: ['畅学所有课程', '完整练习题库', '考试模拟', '学习报告', '1对1答疑', '优先客服', '线下活动', '证书颁发'] },
])

const benefits = [
  { title: '全站课程', desc: '所有课程无限畅学', icon: 'VideoPlay', bg: '#165DFF' },
  { title: '专属答疑', desc: '1对1在线答疑服务', icon: 'ChatDotRound', bg: '#36D399' },
  { title: '学习报告', desc: '个性化学习数据分析', icon: 'DataLine', bg: '#FF7D00' },
  { title: '优先服务', desc: '客服优先响应处理', icon: 'User', bg: '#F53F3F' },
]

function formatMoney(cents: number) {
  return cents > 0 ? `¥${(cents / 100).toFixed(0)}` : '免费'
}

function handleBuy(plan: any) {
  if (plan.price > 0) {
    router.push(`/pay?productId=${plan.id}&type=membership`)
  }
}
</script>

<style scoped lang="scss">

.membership-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 20px;
}

.page-header {
  text-align: center;
  margin-bottom: 48px;

  .crown-icon {
    width: 80px;
    height: 80px;
    border-radius: 50%;
    background: linear-gradient(135deg, #FFF6E0, #FFF);
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 16px;
    box-shadow: 0 4px 16px rgba(255, 149, 0, 0.15);
    animation: float 3s ease-in-out infinite;
    will-change: transform, opacity;
  }

  h2 {
    font-size: 28px;
    font-weight: 700;
    color: $text-title;
    margin: 0 0 10px;
  }

  p {
    font-size: 15px;
    color: $text-muted;
    margin: 0;
  }
}

@keyframes float {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-8px); }
}

// 会员卡片网格
.plan-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 24px;
  max-width: 800px;
  margin: 0 auto 64px;
}

.plan-card {
  background: $bg-white;
  border-radius: 20px;
  padding: 32px 24px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  text-align: center;
  position: relative;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.35s ease;
  border: 2px solid transparent;
  animation: fadeInUp 0.6s ease forwards;
  opacity: 0;

  &:hover {
    transform: translateY(-8px);
    box-shadow: 0 16px 40px rgba(0, 0, 0, 0.1);
  }

  &.recommended {
    border: 2px solid transparent;
    background: linear-gradient(#fff, #fff) padding-box,
                linear-gradient(135deg, $primary-color, #4080FF) border-box;
    transform: scale(1.05);

    &:hover {
      transform: scale(1.05) translateY(-8px);
    }
  }
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

// 推荐标签
.recommend-badge {
  position: absolute;
  top: -14px;
  left: 50%;
  transform: translateX(-50%);
  background: linear-gradient(135deg, $primary-color, #4080FF);
  color: #fff;
  padding: 4px 18px;
  border-radius: 14px;
  font-size: 12px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 4px;
  box-shadow: 0 4px 12px rgba($primary-color, 0.3);
  white-space: nowrap;
}

// 皇冠图标
.plan-crown {
  width: 56px;
  height: 56px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 16px;
  color: #fff;
  transition: transform 0.3s ease;

  .plan-card:hover & {
    transform: rotate(10deg) scale(1.1);
  }

  &.crown-level-1 { background: linear-gradient(135deg, #C9CDD4, #86909C); }
  &.crown-level-2 { background: linear-gradient(135deg, #36D399, #165DFF); }
  &.crown-level-3 { background: linear-gradient(135deg, #FF7D00, #FF9500); }
  &.crown-level-4 { background: linear-gradient(135deg, #165DFF, #4080FF); }
}

.plan-card h3 {
  font-size: 18px;
  font-weight: 700;
  color: $text-title;
  margin: 0 0 12px;
}

.plan-price {
  margin-bottom: 24px;

  .price {
    font-size: 36px;
    font-weight: 800;
    color: #F53F3F;
  }

  .duration {
    font-size: 14px;
    color: $text-muted;
  }
}

.plan-features {
  text-align: left;
  margin-bottom: 28px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 13px;
  color: $text-body;
  margin-bottom: 10px;
  padding: 4px 0;

  .check-icon {
    width: 20px;
    height: 20px;
    border-radius: 50%;
    background: rgba($success-color, 0.1);
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    color: $success-color;
    animation: checkPop 0.4s ease forwards;
    animation-delay: calc(var(--idx, 0) * 0.05s);

    .el-icon {
      font-size: 12px;
    }
  }
}

@keyframes checkPop {
  0% { transform: scale(0); }
  60% { transform: scale(1.2); }
  100% { transform: scale(1); }
}

.plan-btn {
  width: 100%;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s ease;

  &:hover {
    transform: scale(1.02);
  }
}

// 权益说明区域
.benefits-section {
  text-align: center;

  .benefits-title {
    font-size: 22px;
    font-weight: 700;
    color: $text-title;
    margin-bottom: 32px;
  }
}

.benefits-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 24px;
}

.benefit-card {
  background: $bg-white;
  border-radius: 16px;
  padding: 28px 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.06);
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s ease;

  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  }

  .benefit-icon {
    width: 52px;
    height: 52px;
    border-radius: 14px;
    display: flex;
    align-items: center;
    justify-content: center;
    margin: 0 auto 14px;
  }

  h4 {
    font-size: 16px;
    font-weight: 600;
    color: $text-title;
    margin: 0 0 6px;
  }

  p {
    font-size: 13px;
    color: $text-muted;
    margin: 0;
  }
}

// 响应式
@media (max-width: 1024px) {
  .plan-grid {
    grid-template-columns: repeat(2, 1fr);
  }

  .plan-card.recommended {
    transform: scale(1);

    &:hover {
      transform: scale(1) translateY(-8px);
    }
  }

  .benefits-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 576px) {
  .plan-grid {
    grid-template-columns: 1fr;
  }

  .benefits-grid {
    grid-template-columns: 1fr;
  }
}
</style>
