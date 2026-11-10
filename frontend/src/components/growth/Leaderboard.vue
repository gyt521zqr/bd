<template>
  <!-- 排行榜 -->
  <div class="leaderboard">
    <!-- 前三名展示 -->
    <div class="top-three" v-if="list.length >= 3">
      <div class="top-item second" v-if="list[1]">
        <div class="rank-badge">2</div>
        <el-avatar :size="48" :src="list[1].avatar">{{ list[1].nickname?.charAt(0) }}</el-avatar>
        <span class="top-name">{{ list[1].nickname }}</span>
        <span class="top-score">{{ list[1].points }}分</span>
      </div>
      <div class="top-item first" v-if="list[0]">
        <div class="rank-badge">1</div>
        <el-avatar :size="56" :src="list[0].avatar">{{ list[0].nickname?.charAt(0) }}</el-avatar>
        <span class="top-name">{{ list[0].nickname }}</span>
        <span class="top-score">{{ list[0].points }}分</span>
      </div>
      <div class="top-item third" v-if="list[2]">
        <div class="rank-badge">3</div>
        <el-avatar :size="48" :src="list[2].avatar">{{ list[2].nickname?.charAt(0) }}</el-avatar>
        <span class="top-name">{{ list[2].nickname }}</span>
        <span class="top-score">{{ list[2].points }}分</span>
      </div>
    </div>
    <!-- 4-10名列表 -->
    <div class="rank-list">
      <div v-for="item in list.slice(3)" :key="item.id" class="rank-item">
        <span class="rank-num">{{ item.rank || list.indexOf(item) + 1 }}</span>
        <el-avatar :size="32" :src="item.avatar">{{ item.nickname?.charAt(0) }}</el-avatar>
        <span class="rank-name">{{ item.nickname }}</span>
        <span class="rank-score">{{ item.points }}分</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * Leaderboard - 排行榜组件
 * 前三名特殊展示，其余列表展示
 */
defineProps<{ list: { id: number; nickname: string; avatar: string; points: number; rank?: number }[] }>()
</script>

<style scoped lang="scss">
.leaderboard { background: $bg-white; border-radius: 16px; padding: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.top-three { display: flex; justify-content: center; align-items: flex-end; gap: 16px; margin-bottom: 24px; padding: 20px 0; }
.top-item { display: flex; flex-direction: column; align-items: center; gap: 8px; padding: 16px; border-radius: 12px; &.first { background: linear-gradient(135deg, $warning-bg, $warning-light); padding: 20px 24px; } &.second { background: $bg-page; } &.third { background: $bg-page; } }
.rank-badge { width: 28px; height: 28px; border-radius: 50%; display: flex; align-items: center; justify-content: center; font-size: 13px; font-weight: 700; background: $text-muted; color: $bg-white; }
.top-name { font-size: 14px; color: $text-title; font-weight: 500; }
.top-score { font-size: 16px; font-weight: 700; color: $warning-color; }
.rank-list { display: flex; flex-direction: column; }
.rank-item { display: flex; align-items: center; gap: 12px; padding: 10px 8px; border-bottom: 1px solid $bg-page; &:last-child { border-bottom: none; } }
.rank-num { width: 24px; font-size: 13px; color: $text-muted; text-align: center; }
.rank-name { flex: 1; font-size: 14px; color: $text-primary; }
.rank-score { font-size: 14px; font-weight: 600; color: $warning-color; }
</style>
