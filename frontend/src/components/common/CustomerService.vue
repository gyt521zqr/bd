<template>
  <!-- 全局悬浮客服组件 -->
  <div class="customer-service" :class="{ 'is-open': isOpen, 'is-mobile': isMobile }">
    <!-- 悬浮按钮 -->
    <div class="cs-float-btn" @click="togglePanel">
      <el-icon :size="28" v-if="!isOpen"><Service /></el-icon>
      <el-icon :size="24" v-else><Close /></el-icon>
    </div>

    <!-- 咨询面板 -->
    <transition name="cs-panel">
      <div class="cs-panel" v-show="isOpen" @click.stop>
        <!-- 面板头部 -->
        <div class="cs-header">
          <div class="cs-avatar">
            <el-avatar :size="48" :src="csAvatar">码</el-avatar>
          </div>
          <div class="cs-info">
            <div class="cs-name">码高客服</div>
            <div class="cs-status">
              <span class="status-dot"></span>
              在线为您服务
            </div>
          </div>
        </div>

        <!-- 常见问题 -->
        <div class="cs-faq">
          <div class="faq-title">常见问题</div>
          <div class="faq-list">
            <div
              class="faq-item"
              v-for="(faq, idx) in faqList"
              :key="idx"
              @click="handleFaq(faq)"
            >
              <el-icon size="14"><QuestionFilled /></el-icon>
              <span>{{ faq.question }}</span>
            </div>
          </div>
        </div>

        <!-- 联系方式 -->
        <div class="cs-contact">
          <div class="contact-title">联系我们</div>
          <div class="contact-item">
            <el-icon size="16"><Phone /></el-icon>
            <span>咨询电话：17709342525 / 18793419899</span>
          </div>
          <div class="contact-item">
            <el-icon size="16"><Clock /></el-icon>
            <span>服务时间：9:00 - 21:00</span>
          </div>
        </div>

        <!-- 微信二维码 -->
        <div class="cs-qrcode">
          <div class="qrcode-title">扫码添加客服微信</div>
          <div class="qrcode-placeholder">
            <el-icon :size="32"><Picture /></el-icon>
            <span>微信二维码</span>
          </div>
        </div>
      </div>
    </transition>

    <!-- 移动端全屏遮罩 -->
    <div class="cs-mobile-mask" v-if="isMobile && isOpen" @click="togglePanel"></div>
  </div>
</template>

<script setup lang="ts">
/**
 * CustomerService - 全局悬浮客服组件
 * PC端右侧固定悬浮，移动端全屏弹窗
 */
import { ref, onMounted, onUnmounted } from 'vue'
import { Service, Close, QuestionFilled, Phone, Clock, Picture } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const isOpen = ref(false)
const isMobile = ref(window.innerWidth <= 768)

function handleResize() {
  isMobile.value = window.innerWidth <= 768
  if (!isMobile.value) isOpen.value = false
}

onMounted(() => window.addEventListener('resize', handleResize))
onUnmounted(() => window.removeEventListener('resize', handleResize))

function togglePanel() {
  isOpen.value = !isOpen.value
}

const csAvatar = ''

const faqList = [
  { question: '如何报名编程赛事？', answer: '进入赛事中心，选择感兴趣的赛事，点击立即报名即可。' },
  { question: '课程购买后如何学习？', answer: '购买后在我的课程中查看，点击继续学习即可进入课程页面。' },
  { question: '忘记密码怎么办？', answer: '在登录页面点击忘记密码，通过手机验证码重置密码。' },
  { question: '如何联系老师答疑？', answer: '在课程学习页面点击答疑按钮，或添加客服微信咨询。' },
  { question: '支持退款吗？', answer: '课程购买后7天内未学习可申请退款，具体请联系客服。' },
]

function handleFaq(faq: any) {
  ElMessage.info(faq.answer)
}
</script>

<style scoped lang="scss">
$primary: #165DFF;

.customer-service {
  position: fixed;
  right: 24px;
  bottom: 24px;
  z-index: 999;

  // 悬浮按钮
  .cs-float-btn {
    width: 56px;
    height: 56px;
    border-radius: 50%;
    background: linear-gradient(135deg, $primary, #36D399);
    color: #fff;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    box-shadow: 0 4px 20px rgba(22, 93, 255, 0.35);
    transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    z-index: 1001;

    &:hover {
      transform: scale(1.08);
      box-shadow: 0 6px 28px rgba(22, 93, 255, 0.5);
    }

    &:active {
      transform: scale(0.95);
    }
  }

  // 咨询面板
  .cs-panel {
    position: absolute;
    right: 0;
    bottom: 72px;
    width: 320px;
    background: $bg-white;
    border-radius: 16px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
    overflow: hidden;
    transform-origin: bottom right;
    z-index: 1000;
  }

  // 面板头部
  .cs-header {
    display: flex;
    align-items: center;
    gap: 12px;
    padding: 20px;
    background: linear-gradient(135deg, $primary, #36D399);
    color: #fff;

    .cs-avatar {
      :deep(.el-avatar) {
        background: $bg-white;
        color: $primary;
        font-size: 20px;
        font-weight: 700;
      }
    }

    .cs-name {
      font-size: 16px;
      font-weight: 700;
      margin-bottom: 4px;
    }

    .cs-status {
      font-size: 12px;
      opacity: 0.9;
      display: flex;
      align-items: center;
      gap: 4px;

      .status-dot {
        width: 8px;
        height: 8px;
        border-radius: 50%;
        background: #36D399;
        animation: pulse 2s ease-in-out infinite;
        will-change: transform, opacity;
      }
    }
  }

  @keyframes pulse {
    0%, 100% { opacity: 1; }
    50% { opacity: 0.5; }
  }

  // 常见问题
  .cs-faq {
    padding: 16px 20px;
    border-bottom: 1px solid #F2F3F5;

    .faq-title {
      font-size: 14px;
      font-weight: 700;
      color: #1D2129;
      margin-bottom: 10px;
    }

    .faq-list {
      display: flex;
      flex-direction: column;
      gap: 6px;
    }

    .faq-item {
      display: flex;
      align-items: center;
      gap: 8px;
      padding: 8px 10px;
      border-radius: 8px;
      font-size: 13px;
      color: #4E5969;
      cursor: pointer;
      transition: transform, box-shadow, background-color, border-color, color, opacity 0.2s ease;

      &:hover {
        background: rgba(22, 93, 255, 0.06);
        color: $primary;
      }

      .el-icon {
        color: $primary;
        flex-shrink: 0;
      }

      span {
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }
    }
  }

  // 联系方式
  .cs-contact {
    padding: 16px 20px;
    border-bottom: 1px solid #F2F3F5;

    .contact-title {
      font-size: 14px;
      font-weight: 700;
      color: #1D2129;
      margin-bottom: 10px;
    }

    .contact-item {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 13px;
      color: #4E5969;
      margin-bottom: 6px;

      .el-icon {
        color: $primary;
        flex-shrink: 0;
      }
    }
  }

  // 二维码
  .cs-qrcode {
    padding: 16px 20px 20px;
    text-align: center;

    .qrcode-title {
      font-size: 13px;
      color: #86909C;
      margin-bottom: 10px;
    }

    .qrcode-placeholder {
      width: 120px;
      height: 120px;
      margin: 0 auto;
      border: 1px dashed #C9CDD4;
      border-radius: 8px;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 6px;
      color: #C9CDD4;

      span {
        font-size: 12px;
      }
    }
  }

  // 面板动画
  .cs-panel-enter-active,
  .cs-panel-leave-active {
    transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }
  .cs-panel-enter-from,
  .cs-panel-leave-to {
    opacity: 0;
    transform: scale(0.9) translateY(10px);
  }
}

// ==================== 移动端适配 ====================
.customer-service.is-mobile {
  .cs-float-btn {
    width: 48px;
    height: 48px;
    right: 16px;
    bottom: 16px;

    .el-icon {
      font-size: 22px;
    }
  }

  .cs-panel {
    position: fixed;
    top: 50%;
    left: 50%;
    right: auto;
    bottom: auto;
    transform: translate(-50%, -50%);
    width: calc(100vw - 32px);
    max-width: 360px;
    max-height: 80vh;
    overflow-y: auto;
    z-index: 1001;
  }

  .cs-mobile-mask {
    position: fixed;
    inset: 0;
    background: rgba(0, 0, 0, 0.5);
    z-index: 1000;
  }

  .cs-panel-enter-active,
  .cs-panel-leave-active {
    transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  }
  .cs-panel-enter-from,
  .cs-panel-leave-to {
    opacity: 0;
    transform: translate(-50%, -45%) scale(0.95);
  }
}
</style>
