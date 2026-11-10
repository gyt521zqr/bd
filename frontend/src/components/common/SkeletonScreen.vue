<template>
  <!-- 骨架屏组件：统一加载占位状态 -->
  <div class="skeleton-screen" :class="[`skeleton--${variant}`]" aria-hidden="true">
    <!-- 卡片列表骨架 -->
    <template v-if="variant === 'card-grid'">
      <div class="skeleton-card" v-for="n in count" :key="n">
        <div class="skeleton-card__cover"></div>
        <div class="skeleton-card__body">
          <div class="skeleton-line skeleton-line--title"></div>
          <div class="skeleton-line skeleton-line--text"></div>
          <div class="skeleton-line skeleton-line--text"></div>
          <div class="skeleton-card__footer">
            <div class="skeleton-line skeleton-line--tag"></div>
            <div class="skeleton-line skeleton-line--tag"></div>
          </div>
        </div>
      </div>
    </template>

    <!-- 表格骨架 -->
    <template v-else-if="variant === 'table'">
      <div class="skeleton-table">
        <div class="skeleton-table__header">
          <div class="skeleton-line skeleton-line--cell" v-for="n in columns" :key="'h' + n"></div>
        </div>
        <div class="skeleton-table__row" v-for="row in count" :key="'r' + row">
          <div class="skeleton-line skeleton-line--cell" v-for="col in columns" :key="'c' + col"></div>
        </div>
      </div>
    </template>

    <!-- 详情页骨架 -->
    <template v-else-if="variant === 'detail'">
      <div class="skeleton-detail">
        <div class="skeleton-line skeleton-line--title"></div>
        <div class="skeleton-line skeleton-line--text"></div>
        <div class="skeleton-line skeleton-line--text"></div>
        <div class="skeleton-line skeleton-line--half"></div>
        <div class="skeleton-detail__grid">
          <div class="skeleton-detail__card" v-for="n in count" :key="n">
            <div class="skeleton-line skeleton-line--title"></div>
            <div class="skeleton-line skeleton-line--text"></div>
          </div>
        </div>
      </div>
    </template>

    <!-- 文本骨架 -->
    <template v-else-if="variant === 'text'">
      <div class="skeleton-text">
        <div class="skeleton-line" v-for="n in count" :key="n"
          :style="{ width: n === count ? '60%' : '100%' }"></div>
      </div>
    </template>

    <!-- 通用块骨架 -->
    <template v-else>
      <div class="skeleton-block" v-for="n in count" :key="n"></div>
    </template>
  </div>
</template>

<script setup lang="ts">
/**
 * SkeletonScreen - 统一骨架屏组件
 * 支持多种布局变体：卡片网格、表格、详情页、文本、通用块
 */
withDefaults(defineProps<{
  variant?: 'card-grid' | 'table' | 'detail' | 'text' | 'block'
  count?: number
  columns?: number
}>(), {
  variant: 'card-grid',
  count: 6,
  columns: 5,
})
</script>

<style scoped lang="scss">
.skeleton-screen {
  width: 100%;
}

/* ====== 闪烁动画基类 ====== */
.skeleton-line,
.skeleton-card__cover,
.skeleton-block {
  @include skeleton-shimmer;
  border-radius: $radius-sm;
}

/* ====== 卡片网格骨架 ====== */
.skeleton-card-grid,
.skeleton-screen.skeleton--card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: $spacing-lg;

  @include tablet {
    grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
    gap: $spacing-base;
  }

  @include mobile {
    grid-template-columns: 1fr;
  }
}

.skeleton-card {
  background: $bg-white;
  border-radius: $radius-md;
  overflow: hidden;
  box-shadow: $shadow-sm;
  border: 1px solid $border-light;

  &__cover {
    height: 160px;
    border-radius: 0;
  }

  &__body {
    padding: $spacing-lg;
  }

  &__footer {
    display: flex;
    gap: $spacing-sm;
    margin-top: $spacing-base;
  }
}

.skeleton-line {
  height: 14px;
  margin-bottom: $spacing-sm;

  &--title {
    height: 20px;
    width: 70%;
    margin-bottom: $spacing-md;
  }

  &--text {
    width: 100%;
  }

  &--half {
    width: 50%;
  }

  &--tag {
    width: 60px;
    height: 24px;
    border-radius: $radius-round;
    margin: 0;
  }

  &--cell {
    height: 16px;
    flex: 1;
    margin: 0;
  }
}

/* ====== 表格骨架 ====== */
.skeleton-table {
  width: 100%;

  &__header,
  &__row {
    display: flex;
    gap: $spacing-base;
    padding: $spacing-base $spacing-lg;
    border-bottom: 1px solid $border-light;
  }

  &__header {
    background: $bg-light;
    font-weight: 600;
  }

  &__row {
    &:hover {
      background: $bg-light;
    }
  }
}

/* ====== 详情页骨架 ====== */
.skeleton-detail {
  max-width: 800px;

  &__grid {
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
    gap: $spacing-lg;
    margin-top: $spacing-xl;

    @include mobile {
      grid-template-columns: 1fr;
    }
  }

  &__card {
    padding: $spacing-lg;
    background: $bg-white;
    border-radius: $radius-md;
    border: 1px solid $border-light;
  }
}

/* ====== 文本骨架 ====== */
.skeleton-text {
  display: flex;
  flex-direction: column;
  gap: $spacing-sm;
}

/* ====== 通用块骨架 ====== */
.skeleton-block {
  height: 200px;
  border-radius: $radius-md;
}

/* ====== 无障碍：减弱动画 ====== */
@include reduce-motion {
  .skeleton-line,
  .skeleton-card__cover,
  .skeleton-block {
    animation: none;
    background: rgba(0, 0, 0, 0.08);
  }
}
</style>
