<template>
  <!-- 全局系统公告条：消费后台系统公告（GET /api/system/notices） -->
  <transition name="notice-bar-fade">
    <div v-if="visible && notices.length" class="global-notice-bar">
      <el-icon class="notice-bar-icon"><Bell /></el-icon>
      <div class="notice-bar-scroll">
        <div class="notice-bar-track" :class="{ 'is-paused': paused }" @mouseenter="paused = true" @mouseleave="paused = false">
          <span
            v-for="(n, i) in loopList"
            :key="i"
            class="notice-bar-item"
            @click="openNotice(n)"
          >
            <el-tag size="small" effect="dark" type="warning" class="notice-tag">公告</el-tag>
            {{ n.title }}
          </span>
        </div>
      </div>
      <el-icon class="notice-bar-close" @click="dismissAll"><Close /></el-icon>
    </div>
  </transition>
</template>

<script setup lang="ts">
/**
 * GlobalNoticeBar - 全局系统公告条
 * 数据来自 useSiteStore().notices（启动时拉取 + 后台可随时新增）
 */
import { computed, ref } from 'vue'
import { Bell, Close } from '@element-plus/icons-vue'
import { useSiteStore } from '@/stores/site'
import type { SysNoticeItem } from '@/api/system'

const siteStore = useSiteStore()
const notices = computed<SysNoticeItem[]>(() => siteStore.notices)
const dismissed = ref(false)
const paused = ref(false)

const visible = computed(() => !dismissed.value && notices.value.length > 0)

// 为跑马灯无缝滚动复制一份
const loopList = computed(() => (notices.value.length > 1 ? [...notices.value, ...notices.value] : notices.value))

function dismissAll() {
  dismissed.value = true
}

function openNotice(n: SysNoticeItem) {
  if (n.content) {
    // 简单呈现正文，避免引入额外依赖
    window.alert(`${n.title}\n\n${n.content}`)
  }
}
</script>

<style scoped lang="scss">
.global-notice-bar {
  display: flex;
  align-items: center;
  gap: 10px;
  height: 38px;
  padding: 0 16px;
  background: linear-gradient(90deg, #fff7e8, #fff1e6);
  border-bottom: 1px solid #ffe2c2;
  color: #8a5a00;
  font-size: 13px;
  position: relative;
  z-index: 2001;

  .notice-bar-icon {
    flex-shrink: 0;
    color: #ff7d00;
  }

  .notice-bar-scroll {
    flex: 1;
    overflow: hidden;
    -webkit-mask-image: linear-gradient(90deg, transparent, #000 24px, #000 calc(100% - 24px), transparent);
    mask-image: linear-gradient(90deg, transparent, #000 24px, #000 calc(100% - 24px), transparent);
  }

  .notice-bar-track {
    display: inline-flex;
    align-items: center;
    gap: 48px;
    white-space: nowrap;
    animation: notice-scroll 22s linear infinite;

    &.is-paused {
      animation-play-state: paused;
    }
  }

  .notice-bar-item {
    cursor: pointer;
    display: inline-flex;
    align-items: center;
    gap: 6px;

    &:hover {
      color: #ff7d00;
    }
  }

  .notice-tag {
    transform: scale(0.85);
  }

  .notice-bar-close {
    flex-shrink: 0;
    cursor: pointer;
    opacity: 0.6;

    &:hover {
      opacity: 1;
    }
  }
}

@keyframes notice-scroll {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(-50%);
  }
}

.notice-bar-fade-enter-active,
.notice-bar-fade-leave-active {
  transition: all 0.3s ease;
}
.notice-bar-fade-enter-from,
.notice-bar-fade-leave-to {
  opacity: 0;
  transform: translateY(-100%);
}
</style>
