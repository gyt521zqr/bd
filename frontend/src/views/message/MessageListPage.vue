<template>
  <!-- 消息中心 -->
  <div class="message-page">
    <div class="page-header">
      <div class="header-left">
        <h2>消息中心</h2>
        <span v-if="unreadCount > 0" class="unread-count">{{ unreadCount }} 条未读</span>
      </div>
      <el-button text type="primary" @click="handleReadAll" class="read-all-btn">
        <el-icon><Check /></el-icon>
        全部已读
      </el-button>
    </div>

    <!-- 消息分类Tab -->
    <div class="tab-bar">
      <div
        v-for="tab in tabs"
        :key="tab.key"
        class="tab-item"
        :class="{ active: activeTab === tab.key }"
        @click="activeTab = tab.key"
      >
        <span class="tab-label">{{ tab.label }}</span>
        <span v-if="tab.badge > 0" class="tab-badge">{{ tab.badge }}</span>
      </div>
    </div>

    <div class="message-list">
      <div
        v-for="msg in filteredMessages"
        :key="msg.id"
        class="message-item"
        :class="{ unread: !msg.read }"
        :style="{ '--type-color': msg.typeColor }"
        @click="handleRead(msg)"
      >
        <!-- 左侧彩色竖条 -->
        <div class="type-bar"></div>

        <div class="msg-icon" :style="{ background: msg.typeColor }">
          <el-icon :size="18"><component :is="msg.icon" /></el-icon>
        </div>

        <div class="msg-content">
          <div class="msg-title-row">
            <h4>{{ msg.title }}</h4>
            <!-- 未读红点标记 -->
            <span v-if="!msg.read" class="red-dot"></span>
          </div>
          <p>{{ msg.content }}</p>
          <span class="msg-time">{{ msg.time }}</span>
        </div>

        <el-tag v-if="!msg.read" type="danger" size="small" class="unread-badge" effect="dark">未读</el-tag>
        <el-tag v-else type="info" size="small" class="read-badge" effect="plain">已读</el-tag>
      </div>
    </div>

    <el-empty v-if="filteredMessages.length === 0" description="暂无消息" class="empty-state">
      <template #image>
        <el-icon :size="64" color="#C9CDD4"><MessageBox /></el-icon>
      </template>
    </el-empty>
  </div>
</template>

<script setup lang="ts">
/**
 * MessageListPage - 消息中心
 * 系统通知、课程消息、考试消息
 */
import { ref, computed } from 'vue'
import { Check, MessageBox } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const activeTab = ref('all')

const messages = ref([
  { id: 1, type: 'system', icon: 'Bell', typeColor: '#165DFF', title: '系统公告', content: '平台将于3月20日进行系统升级，届时部分功能可能暂时不可用。', time: '2024-03-15 10:00', read: false },
  { id: 2, type: 'course', icon: 'Notebook', typeColor: '#36D399', title: '课程更新通知', content: 'Python基础入门课程新增第8章：文件操作。', time: '2024-03-14 16:00', read: false },
  { id: 3, type: 'exam', icon: 'Trophy', typeColor: '#FF7D00', title: '考试成绩发布', content: 'Python期中测试成绩已发布，你获得了88分。', time: '2024-03-14 12:00', read: true },
  { id: 4, type: 'system', icon: 'Bell', typeColor: '#165DFF', title: '欢迎加入', content: '欢迎加入码高教育平台，开始你的编程学习之旅！', time: '2024-03-01 09:00', read: true },
])

const unreadCount = computed(() => messages.value.filter(m => !m.read).length)

const tabs = computed(() => [
  { key: 'all', label: '全部', badge: 0 },
  { key: 'unread', label: '未读', badge: unreadCount.value },
  { key: 'system', label: '系统', badge: messages.value.filter(m => m.type === 'system' && !m.read).length },
])

const filteredMessages = computed(() => {
  if (activeTab.value === 'all') return messages.value
  if (activeTab.value === 'unread') return messages.value.filter(m => !m.read)
  return messages.value.filter(m => m.type === activeTab.value)
})

function handleRead(msg: any) { msg.read = true }
function handleReadAll() { messages.value.forEach(m => m.read = true); ElMessage.success('全部已读') }
</script>

<style scoped lang="scss">

.message-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 24px 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;

    h2 {
      font-size: 24px;
      font-weight: 700;
      color: $text-title;
      margin: 0;
    }
  }

  .unread-count {
    font-size: 13px;
    color: #F53F3F;
    background: rgba(245, 63, 63, 0.08);
    padding: 2px 10px;
    border-radius: 10px;
    font-weight: 500;
  }

  .read-all-btn {
    font-weight: 500;
  }
}

// 消息分类Tab
.tab-bar {
  display: flex;
  gap: 8px;
  margin-bottom: 20px;
  border-bottom: 1px solid #E5E6EB;
  padding-bottom: 12px;
}

.tab-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 16px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  color: $text-body;
  cursor: pointer;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.25s ease;
  position: relative;

  &:hover {
    background: rgba($primary-color, 0.06);
    color: $primary-color;
  }

  &.active {
    background: $primary-color;
    color: #fff;
    box-shadow: 0 4px 12px rgba($primary-color, 0.3);
  }
}

.tab-badge {
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: #F53F3F;
  color: #fff;
  font-size: 11px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
}

// 消息列表
.message-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.message-item {
  display: flex;
  align-items: flex-start;
  gap: 14px;
  background: $bg-white;
  border-radius: 14px;
  padding: 18px 20px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  cursor: pointer;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s ease;
  position: relative;
  overflow: hidden;
  border: 1px solid transparent;

  &:hover {
    background: $bg-white;
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
    transform: translateY(-2px);
    border-color: rgba($primary-color, 0.12);
  }

  &.unread {
    background: linear-gradient(135deg, #E8F0FF, #fff);
    border-color: rgba($primary-color, 0.08);

    .type-bar {
      opacity: 1;
    }
  }
}

// 左侧彩色竖条
.type-bar {
  position: absolute;
  left: 0;
  top: 12px;
  bottom: 12px;
  width: 4px;
  border-radius: 0 4px 4px 0;
  background: var(--type-color, $primary-color);
  opacity: 0.6;
  transition: opacity 0.3s ease;
}

.msg-icon {
  width: 40px;
  height: 40px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  margin-left: 4px;
}

.msg-content {
  flex: 1;
  min-width: 0;

  .msg-title-row {
    display: flex;
    align-items: center;
    gap: 8px;
    margin-bottom: 6px;

    h4 {
      font-size: 15px;
      font-weight: 600;
      color: $text-title;
      margin: 0;
    }
  }

  p {
    font-size: 13px;
    color: $text-body;
    margin-bottom: 8px;
    line-height: 1.6;
    display: -webkit-box;
    -webkit-line-clamp: 2;
    -webkit-box-orient: vertical;
    overflow: hidden;
  }

  .msg-time {
    font-size: 12px;
    color: $text-muted;
  }
}

// 未读红点标记
.red-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #F53F3F;
  flex-shrink: 0;
  animation: dotPulse 2s ease-in-out infinite;
  will-change: transform, opacity;
}

@keyframes dotPulse {
  0%, 100% { transform: scale(1); opacity: 1; }
  50% { transform: scale(1.3); opacity: 0.7; }
}

.unread-badge {
  flex-shrink: 0;
  font-weight: 600;
}

.read-badge {
  flex-shrink: 0;
}

.empty-state {
  margin-top: 60px;
}
</style>
