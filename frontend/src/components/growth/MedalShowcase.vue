<template>
  <!-- 勋章墙 -->
  <div class="medal-showcase">
    <div
      v-for="(medal, index) in medals"
      :key="medal.id"
      class="medal-card"
      :class="{ earned: medal.earned }"
      :style="{ animationDelay: `${index * 0.06}s` }"
      @mouseenter="hoveredMedal = medal.id"
      @mouseleave="hoveredMedal = null"
    >
      <div class="medal-icon-wrap" :class="{ 'medal-glow': medal.earned && hoveredMedal === medal.id }">
        <div class="medal-icon" :class="{ locked: !medal.earned }">
          <el-icon :size="28">
            <Trophy v-if="medal.earned" />
            <Lock v-else />
          </el-icon>
        </div>
        <div class="medal-shine" v-if="medal.earned"></div>
      </div>
      <span class="medal-name">{{ medal.name }}</span>
      <span class="medal-desc">{{ medal.description }}</span>
      <span class="medal-time" v-if="medal.earned && medal.earnedTime">
        <el-icon><Calendar /></el-icon>
        {{ medal.earnedTime }}
      </span>
      <div class="medal-status" :class="{ earned: medal.earned }">
        <el-icon :size="12"><CircleCheck v-if="medal.earned" /><Lock v-else /></el-icon>
        {{ medal.earned ? '已获得' : '未解锁' }}
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * MedalShowcase - 勋章墙组件
 * 悬停放大、发光效果
 */
import { ref } from 'vue'
import { Trophy, Lock, CircleCheck, Calendar } from '@element-plus/icons-vue'

defineProps<{
  medals: { id: number; name: string; icon: string; description: string; earned: boolean; earnedTime: string | null }[]
}>()

const hoveredMedal = ref<number | null>(null)
</script>

<style scoped lang="scss">
.medal-showcase {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.medal-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  padding: 20px 14px;
  background: #F7F8FA;
  border-radius: 14px;
  text-align: center;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  animation: medalPopIn 0.4s ease forwards;
  opacity: 0;
  transform: scale(0.9);
  cursor: pointer;
  border: 2px solid transparent;

  &:hover {
    transform: translateY(-6px) scale(1.05);
    box-shadow: 0 12px 32px rgba(0, 0, 0, 0.1);
    border-color: rgba(22, 93, 255, 0.1);
    background: $bg-white;
  }

  &.earned {
    background: linear-gradient(135deg, rgba(22, 93, 255, 0.04), rgba(64, 128, 255, 0.06));

    &:hover {
      background: linear-gradient(135deg, rgba(22, 93, 255, 0.06), rgba(64, 128, 255, 0.1));
      border-color: rgba(22, 93, 255, 0.2);
    }
  }
}

@keyframes medalPopIn {
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.medal-icon-wrap {
  position: relative;
  width: 64px;
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.35s ease;

  &.medal-glow {
    &::before {
      content: '';
      position: absolute;
      inset: -8px;
      border-radius: 50%;
      background: radial-gradient(circle, rgba(255, 215, 0, 0.3) 0%, transparent 70%);
      animation: glowPulse 1.5s ease-in-out infinite;
      will-change: transform, opacity;
    }
  }
}

@keyframes glowPulse {
  0%, 100% { transform: scale(1); opacity: 0.6; }
  50% { transform: scale(1.1); opacity: 1; }
}

.medal-icon {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, #165DFF, #4080FF);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  font-size: 24px;
  box-shadow: 0 4px 16px rgba(22, 93, 255, 0.25);
  position: relative;
  z-index: 1;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.35s ease;

  &.locked {
    background: #E5E6EB;
    color: #86909C;
    box-shadow: none;
  }

  .medal-card:hover & {
    transform: scale(1.1);
  }
}

.medal-shine {
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.3), transparent);
  border-radius: 16px;
  animation: medalShine 3s ease-in-out infinite;
  will-change: transform, opacity;
  z-index: 2;
  pointer-events: none;
}

@keyframes medalShine {
  0% { left: -100%; }
  50%, 100% { left: 100%; }
}

.medal-name {
  font-size: 14px;
  font-weight: 700;
  color: #1D2129;
}

.medal-desc {
  font-size: 12px;
  color: #86909C;
  line-height: 1.4;
  min-height: 32px;
  display: flex;
  align-items: center;
}

.medal-time {
  font-size: 11px;
  color: #36D399;
  display: flex;
  align-items: center;
  gap: 4px;

  .el-icon {
    font-size: 12px;
  }
}

.medal-status {
  font-size: 11px;
  font-weight: 500;
  padding: 3px 10px;
  border-radius: 10px;
  background: #E5E6EB;
  color: #86909C;
  display: flex;
  align-items: center;
  gap: 3px;

  &.earned {
    background: rgba(54, 211, 153, 0.1);
    color: #36D399;
  }
}

// ==================== 响应式 ====================
@media (max-width: 768px) {
  .medal-showcase {
    grid-template-columns: repeat(3, 1fr);
    gap: 12px;
  }

  .medal-card {
    padding: 16px 10px;
  }

  .medal-icon {
    width: 48px;
    height: 48px;
    border-radius: 14px;

    .el-icon {
      font-size: 22px;
    }
  }
}

@media (max-width: 480px) {
  .medal-showcase {
    grid-template-columns: repeat(2, 1fr);
    gap: 10px;
  }

  .medal-card {
    padding: 14px 10px;
  }

  .medal-name {
    font-size: 13px;
  }

  .medal-desc {
    font-size: 11px;
    min-height: 28px;
  }
}
</style>
