<template>
  <!-- 等级徽章 -->
  <div class="level-badge" :class="`level-${level}`">
    <div class="badge-icon">{{ iconMap[level] || '?' }}</div>
    <div class="badge-info">
      <span class="badge-level">Lv.{{ level }}</span>
      <span class="badge-name">{{ name }}</span>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * LevelBadge - 等级徽章组件
 */
withDefaults(defineProps<{ level?: number; name?: string }>(), { level: 1, name: '' })

const iconMap: Record<number, string> = {
  1: '🌱', 2: '🌿', 3: '🌳', 4: '⭐', 5: '🌟',
  6: '💫', 7: '🏆', 8: '👑', 9: '💎', 10: '🔥',
}
</script>

<style scoped lang="scss">
.level-badge { display: inline-flex; align-items: center; gap: 8px; padding: 4px 12px; border-radius: 20px; font-size: 13px; }
.badge-icon { font-size: 20px; }
.badge-level { font-weight: 700; color: #FF9500; }
.badge-name { color: #86909C; }
</style>
