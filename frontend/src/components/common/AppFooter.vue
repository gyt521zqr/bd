<template>
  <!-- 底部页脚 - 深色背景四列布局 -->
  <footer class="app-footer">
    <div class="footer-content">
      <!-- 四列主体内容 -->
      <div class="footer-columns">
        <!-- 平台简介 -->
        <div class="footer-col footer-col-about">
          <div class="footer-logo">
            <span class="footer-logo-icon">M</span>
            <span class="footer-logo-text">{{ siteStore.config.siteName }}</span>
          </div>
          <p class="footer-about-desc">
            为青少年提供编程学习、竞赛训练与能力提升的一站式服务。
          </p>
        </div>

        <!-- 快速链接 -->
        <div class="footer-col">
          <h3 class="footer-col-title">快速链接</h3>
          <ul class="footer-links">
            <li><router-link to="/">首页</router-link></li>
            <li><router-link to="/exam">赛事中心</router-link></li>
            <li><router-link to="/question-bank">题库中心</router-link></li>
            <li><router-link to="/course">课程中心</router-link></li>
            <li><router-link to="/membership">会员中心</router-link></li>
            <li><router-link to="/about">关于我们</router-link></li>
          </ul>
        </div>

        <!-- 联系我们 -->
        <div class="footer-col">
          <h3 class="footer-col-title">联系我们</h3>
          <ul class="footer-contact">
            <li v-if="siteStore.config.contactPhone">
              <el-icon size="14"><Phone /></el-icon>
              <span>咨询电话：{{ siteStore.config.contactPhone }}</span>
            </li>
            <li v-if="siteStore.config.contactEmail">
              <el-icon size="14"><Message /></el-icon>
              <span>邮箱：{{ siteStore.config.contactEmail }}</span>
            </li>
            <li v-if="siteStore.config.contactAddress">
              <el-icon size="14"><Location /></el-icon>
              <span>地址：{{ siteStore.config.contactAddress }}</span>
            </li>
            <li v-if="!siteStore.config.contactPhone && !siteStore.config.contactEmail && !siteStore.config.contactAddress">
              <span>联系方式暂未公布</span>
            </li>
          </ul>
        </div>

        <!-- 关注我们 -->
        <div class="footer-col">
          <h3 class="footer-col-title">关注我们</h3>
          <div class="footer-social">
            <div class="qrcode-placeholder">
              <el-icon size="32"><ChatDotRound /></el-icon>
              <span>微信二维码</span>
            </div>
            <div class="social-icons">
              <a href="javascript:;" class="social-icon-item" title="抖音">
                <el-icon size="20"><VideoCamera /></el-icon>
              </a>
              <a href="javascript:;" class="social-icon-item" title="B站">
                <el-icon size="20"><Monitor /></el-icon>
              </a>
            </div>
          </div>
        </div>
      </div>

      <!-- 底部版权栏 -->
      <div class="footer-bottom">
        <p class="copyright">
          {{ siteStore.config.copyright }}
        </p>
        <div class="footer-bottom-links">
          <a href="javascript:;">隐私政策</a>
          <span class="link-divider">|</span>
          <a href="javascript:;">用户协议</a>
          <template v-if="siteStore.config.icp">
            <span class="link-divider">|</span>
            <span class="icp">{{ siteStore.config.icp }}</span>
          </template>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
/**
 * AppFooter - 底部页脚组件
 * 四列布局：平台简介、快速链接、联系我们、关注我们
 * 站名/ICP/客服等来自后台站点配置（stores/site.ts），不再写死
 */
import { Phone, Message, Location, ChatDotRound, VideoCamera, Monitor } from '@element-plus/icons-vue'
import { useSiteStore } from '@/stores/site'

const siteStore = useSiteStore()
</script>

<style scoped lang="scss">
$primary: #165DFF;
$footer-bg: #1D2129;
$footer-text: rgba(255, 255, 255, 0.85);
$footer-text-muted: rgba(255, 255, 255, 0.45);
$content-max-width: 1200px;

.app-footer {
  position: relative;
  background: linear-gradient(180deg, #1D2129 0%, #1D2129 100%);
  padding: 48px 0 0;
  margin-top: auto;
  overflow: hidden;

  /* 渐变顶部边框 */
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 3px;
    background: linear-gradient(90deg, #165DFF, #36D399, #FF7D00, #165DFF);
    background-size: 300% 100%;
    animation: gradientFlow 4s ease infinite;
    will-change: transform, opacity;
  }

  /* 背景装饰光晕 */
  &::after {
    content: '';
    position: absolute;
    top: -100px;
    right: -50px;
    width: 400px;
    height: 400px;
    background: radial-gradient(circle, rgba(22, 93, 255, 0.08) 0%, transparent 70%);
    pointer-events: none;
  }
}

@keyframes gradientFlow {
  0% {
    background-position: 0% 50%;
  }
  50% {
    background-position: 100% 50%;
  }
  100% {
    background-position: 0% 50%;
  }
}

.footer-content {
  max-width: $content-max-width;
  margin: 0 auto;
  padding: 0 24px;
}

// ==================== 四列布局 ====================
.footer-columns {
  display: grid;
  grid-template-columns: 1.5fr 1fr 1fr 1fr;
  gap: 40px;
  padding-bottom: 40px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.footer-col {
  .footer-col-title {
    font-size: 16px;
    font-weight: 700;
    color: #fff;
    margin-bottom: 20px;
    position: relative;
    padding-bottom: 12px;

    &::after {
      content: '';
      position: absolute;
      bottom: 0;
      left: 0;
      width: 32px;
      height: 3px;
      background: $primary;
      border-radius: 2px;
      transition: width 0.3s ease;
    }
  }

  &:hover .footer-col-title::after {
    width: 48px;
  }
}

// ==================== 平台简介 ====================
.footer-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 20px;

  .footer-logo-icon {
    width: 40px;
    height: 40px;
    background: linear-gradient(135deg, #165DFF, #4080FF);
    color: #fff;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: bold;
    font-size: 22px;
    box-shadow: 0 4px 16px rgba(22, 93, 255, 0.35);
  }

  .footer-logo-text {
    font-size: 22px;
    font-weight: 700;
    background: linear-gradient(135deg, #fff, rgba(255, 255, 255, 0.7));
    -webkit-background-clip: text;
    -webkit-text-fill-color: transparent;
    background-clip: text;
  }
}

.footer-about-desc {
  font-size: 14px;
  color: $footer-text-muted;
  line-height: 1.8;
}

// ==================== 快速链接 ====================
.footer-links {
  list-style: none;
  padding: 0;
  margin: 0;

  li {
    margin-bottom: 12px;

    &:last-child {
      margin-bottom: 0;
    }

    a {
      position: relative;
      font-size: 14px;
      color: $footer-text-muted;
      text-decoration: none;
      transition: color 0.25s ease, padding-left 0.25s ease;
      display: inline-block;

      &:hover {
        color: $primary;
        padding-left: 8px;
      }

      /* 链接hover时左侧出现小竖线 */
      &::before {
        content: '';
        position: absolute;
        left: -4px;
        top: 50%;
        transform: translateY(-50%) scaleY(0);
        width: 2px;
        height: 14px;
        background: $primary;
        border-radius: 1px;
        transition: transform 0.25s ease, left 0.25s ease;
      }

      &:hover::before {
        transform: translateY(-50%) scaleY(1);
        left: 0;
      }
    }
  }
}

// ==================== 联系我们 ====================
.footer-contact {
  list-style: none;
  padding: 0;
  margin: 0;

  li {
    display: flex;
    align-items: flex-start;
    gap: 8px;
    margin-bottom: 14px;
    font-size: 14px;
    color: $footer-text-muted;
    line-height: 1.6;
    transition: color 0.25s ease;

    &:last-child {
      margin-bottom: 0;
    }

    &:hover {
      color: $footer-text;
    }

    .el-icon {
      flex-shrink: 0;
      margin-top: 3px;
      color: $footer-text-muted;
      transition: color 0.25s ease;
    }

    &:hover .el-icon {
      color: $primary;
    }
  }
}

// ==================== 关注我们 ====================
.footer-social {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.qrcode-placeholder {
  width: 100px;
  height: 100px;
  border: 1px dashed rgba(255, 255, 255, 0.2);
  border-radius: 8px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 6px;
  color: $footer-text-muted;
  font-size: 12px;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s ease;

  &:hover {
    border-color: rgba(22, 93, 255, 0.4);
    background: rgba(22, 93, 255, 0.05);
  }

  .el-icon {
    color: $footer-text-muted;
    transition: color 0.3s ease;
  }

  &:hover .el-icon {
    color: $primary;
  }
}

.social-icons {
  display: flex;
  gap: 12px;
}

.social-icon-item {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.06);
  display: flex;
  align-items: center;
  justify-content: center;
  color: $footer-text-muted;
  text-decoration: none;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1);
  border: 1px solid rgba(255, 255, 255, 0.05);

  &:hover {
    background: linear-gradient(135deg, #165DFF, #4080FF);
    color: #fff;
    transform: translateY(-3px) scale(1.1);
    box-shadow: 0 6px 20px rgba(22, 93, 255, 0.4);
    border-color: transparent;
  }
}

// ==================== 底部版权栏 ====================
.footer-bottom {
  position: relative;
  padding: 20px 0;
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;

  /* 版权区域分隔线 - 渐变效果 */
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    height: 1px;
    background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.15), transparent);
  }
}

.copyright {
  font-size: 13px;
  color: $footer-text-muted;
}

.footer-bottom-links {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;

  a {
    color: $footer-text-muted;
    text-decoration: none;
    transition: color 0.25s ease;

    &:hover {
      color: $primary;
    }
  }

  .link-divider {
    color: rgba(255, 255, 255, 0.15);
    margin: 0 4px;
  }

  .icp {
    color: $footer-text-muted;
  }
}

// ==================== 响应式适配 ====================
@media (max-width: 768px) {
  .app-footer {
    padding: 32px 0 0;
  }

  .footer-content {
    padding: 0 16px;
  }

  .footer-columns {
    grid-template-columns: 1fr;
    gap: 32px;
    padding-bottom: 24px;
  }

  .footer-bottom {
    flex-direction: column;
    align-items: center;
    text-align: center;
    gap: 8px;
    padding: 16px 0;
  }
}
</style>
