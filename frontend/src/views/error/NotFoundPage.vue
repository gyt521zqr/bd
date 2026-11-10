<template>
  <!-- 404 页面 - 宇航员迷路主题 -->
  <div class="not-found-page">
    <div class="stars-bg">
      <div v-for="i in 50" :key="i" class="star" :style="{
        left: `${Math.random() * 100}%`,
        top: `${Math.random() * 100}%`,
        animationDelay: `${Math.random() * 3}s`,
        animationDuration: `${2 + Math.random() * 3}s`
      }"></div>
    </div>

    <div class="error-container">
      <!-- 左侧装饰 -->
      <div class="deco-left">
        <div class="planet planet-1"></div>
        <div class="planet planet-2"></div>
        <div class="orbit-ring"></div>
      </div>

      <!-- 中央内容 -->
      <div class="error-content">
        <!-- 宇航员插画 -->
        <div class="astronaut">
          <svg viewBox="0 0 200 200" class="astronaut-svg">
            <!-- 头盔 -->
            <circle cx="100" cy="70" r="45" fill="#F7F8FA" stroke="#E5E6EB" stroke-width="2"/>
            <circle cx="100" cy="70" r="38" fill="#0D1117" opacity="0.9"/>
            <!-- 面罩反光 -->
            <ellipse cx="90" cy="62" rx="12" ry="8" fill="rgba(100,200,255,0.3)" transform="rotate(-20 90 62)"/>
            <!-- 身体 -->
            <ellipse cx="100" cy="140" rx="35" ry="40" fill="#F7F8FA" stroke="#E5E6EB" stroke-width="2"/>
            <!-- 胸前设备 -->
            <rect x="85" y="125" width="30" height="20" rx="4" fill="#165DFF" opacity="0.8"/>
            <circle cx="92" cy="135" r="3" fill="#36D399"/>
            <circle cx="100" cy="135" r="3" fill="#FF7D00"/>
            <circle cx="108" cy="135" r="3" fill="#F53F3F"/>
            <!-- 手臂 -->
            <ellipse cx="55" cy="135" rx="12" ry="25" fill="#F7F8FA" stroke="#E5E6EB" stroke-width="2" transform="rotate(30 55 135)"/>
            <ellipse cx="145" cy="135" rx="12" ry="25" fill="#F7F8FA" stroke="#E5E6EB" stroke-width="2" transform="rotate(-30 145 135)"/>
            <!-- 天线 -->
            <line x1="100" y1="25" x2="100" y2="10" stroke="#E5E6EB" stroke-width="2"/>
            <circle cx="100" cy="8" r="4" fill="#FF7D00"/>
            <!-- 问号 -->
            <text x="155" y="55" font-size="32" font-weight="bold" fill="#FF7D00" opacity="0.8">?</text>
          </svg>
        </div>

        <div class="error-code">
          <span class="code-num">4</span>
          <div class="code-planet">
            <svg viewBox="0 0 100 100" class="planet-svg">
              <circle cx="50" cy="50" r="35" fill="none" stroke="rgba(100,200,255,0.3)" stroke-width="1.5" stroke-dasharray="6 4"/>
              <circle cx="50" cy="50" r="20" fill="rgba(100,200,255,0.1)"/>
              <circle cx="50" cy="50" r="8" fill="rgba(100,200,255,0.2)"/>
              <!-- 行星环 -->
              <ellipse cx="50" cy="50" rx="50" ry="12" fill="none" stroke="rgba(100,200,255,0.15)" stroke-width="1" transform="rotate(-20 50 50)"/>
            </svg>
          </div>
          <span class="code-num">4</span>
        </div>

        <h2 class="error-title">哎呀，页面迷路了</h2>
        <p class="error-desc">
          就像宇航员在太空中迷失方向一样<br>
          您访问的页面似乎不存在或已被移除
        </p>

        <div class="error-actions">
          <el-button type="primary" size="large" round class="home-btn" @click="$router.push('/')">
            <el-icon><HomeFilled /></el-icon>
            返回首页
          </el-button>
          <el-button size="large" round plain class="back-btn" @click="$router.back()">
            <el-icon><ArrowLeft /></el-icon>
            返回上一页
          </el-button>
        </div>
      </div>

      <!-- 右侧装饰 -->
      <div class="deco-right">
        <div class="floating-rock rock-1"></div>
        <div class="floating-rock rock-2"></div>
        <div class="floating-rock rock-3"></div>
        <div class="satellite">
          <svg viewBox="0 0 60 60" class="satellite-svg">
            <rect x="20" y="25" width="20" height="10" rx="2" fill="#E5E6EB"/>
            <rect x="5" y="20" width="12" height="20" rx="1" fill="#165DFF" opacity="0.6"/>
            <rect x="43" y="20" width="12" height="20" rx="1" fill="#165DFF" opacity="0.6"/>
            <line x1="30" y1="5" x2="30" y2="20" stroke="#E5E6EB" stroke-width="1.5"/>
            <circle cx="30" cy="3" r="3" fill="#FF7D00"/>
          </svg>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * NotFoundPage - 404 错误页面
 * 宇航员迷路主题，有趣的插画和动画
 */
import { HomeFilled, ArrowLeft } from '@element-plus/icons-vue'
</script>

<style scoped lang="scss">
@use "sass:color";
$primary: #165DFF;
$accent: #FF7D00;

.not-found-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #0D1117 0%, #0D1117 40%, #0D1117 70%, #0D1117 100%);
  position: relative;
  overflow: hidden;
}

// 星空背景
.stars-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.star {
  position: absolute;
  width: 2px;
  height: 2px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.6);
  animation: twinkle ease-in-out infinite;
  will-change: transform;
}

@keyframes twinkle {
  0%, 100% { opacity: 0.2; transform: scale(1); }
  50% { opacity: 1; transform: scale(1.5); }
}

.error-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 60px;
  padding: 0 40px;
  position: relative;
  z-index: 2;
}

// 左侧装饰
.deco-left {
  position: relative;
  width: 150px;
  height: 300px;
  flex-shrink: 0;
  pointer-events: none;
}

.planet {
  position: absolute;
  border-radius: 50%;

  &.planet-1 {
    width: 60px;
    height: 60px;
    top: 30px;
    left: 20px;
    background: linear-gradient(135deg, rgba(100, 200, 255, 0.2), rgba(100, 200, 255, 0.05));
    animation: floatPlanet 6s ease-in-out infinite;
    will-change: transform;
  }

  &.planet-2 {
    width: 30px;
    height: 30px;
    bottom: 60px;
    right: 10px;
    background: linear-gradient(135deg, rgba(255, 125, 0, 0.2), rgba(255, 125, 0, 0.05));
    animation: floatPlanet 8s ease-in-out infinite reverse;
    will-change: transform;
  }
}

.orbit-ring {
  position: absolute;
  width: 100px;
  height: 100px;
  border: 1px solid rgba(100, 200, 255, 0.1);
  border-radius: 50%;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  animation: orbitSpin 20s linear infinite;
  will-change: transform;
}

@keyframes floatPlanet {
  0%, 100% { transform: translateY(0); }
  50% { transform: translateY(-15px); }
}

// 中央内容
.error-content {
  text-align: center;
  position: relative;
  z-index: 2;
}

// 宇航员插画
.astronaut {
  width: 140px;
  height: 140px;
  margin: 0 auto 20px;
  animation: astronautFloat 4s ease-in-out infinite;
  will-change: transform;

  .astronaut-svg {
    width: 100%;
    height: 100%;
    filter: drop-shadow(0 4px 20px rgba(100, 200, 255, 0.2));
  }
}

@keyframes astronautFloat {
  0%, 100% { transform: translateY(0) rotate(-3deg); }
  50% { transform: translateY(-12px) rotate(3deg); }
}

.error-code {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  margin-bottom: 20px;

  .code-num {
    font-size: 100px;
    font-weight: 900;
    color: rgba(100, 200, 255, 0.85);
    line-height: 1;
    text-shadow: 0 0 40px rgba(100, 200, 255, 0.3);
    animation: codeGlow 3s ease-in-out infinite;
    will-change: transform;
  }

  .code-planet {
    width: 70px;
    height: 70px;

    .planet-svg {
      width: 100%;
      height: 100%;
      animation: planetSpin 25s linear infinite;
      will-change: transform;
    }
  }
}

@keyframes codeGlow {
  0%, 100% { text-shadow: 0 0 40px rgba(100, 200, 255, 0.3); }
  50% { text-shadow: 0 0 60px rgba(100, 200, 255, 0.5); }
}

@keyframes planetSpin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-title {
  font-size: 26px;
  font-weight: 700;
  color: #fff;
  margin-bottom: 12px;
  letter-spacing: 2px;
}

.error-desc {
  font-size: 15px;
  color: rgba(200, 230, 255, 0.7);
  line-height: 1.8;
  margin-bottom: 36px;
}

.error-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;

  .home-btn {
    font-size: 15px;
    font-weight: 600;
    padding: 12px 32px;
    display: inline-flex;
    align-items: center;
    gap: 6px;
    background: linear-gradient(135deg, $primary, #4080FF);
    border: none;
    box-shadow: 0 4px 20px rgba($primary, 0.35);
    transition: background 0.3s ease, box-shadow 0.3s ease, transform 0.3s ease;

    &:hover {
      background: linear-gradient(135deg, color.adjust($primary, $lightness: 5%), color.adjust(#4080FF, $lightness: 5%));
      box-shadow: 0 8px 28px rgba($primary, 0.5);
      transform: translateY(-2px);
    }

    &:active {
      transform: translateY(0);
    }
  }

  .back-btn {
    font-size: 15px;
    font-weight: 500;
    padding: 12px 32px;
    display: inline-flex;
    align-items: center;
    gap: 6px;
    border-color: rgba(255, 255, 255, 0.3);
    color: rgba(255, 255, 255, 0.8);
    transition: color 0.3s ease, background 0.3s ease, border-color 0.3s ease, transform 0.3s ease;

    &:hover {
      border-color: rgba(255, 255, 255, 0.6);
      color: #fff;
      background: rgba(255, 255, 255, 0.08);
      transform: translateY(-2px);
    }
  }
}

// 右侧装饰
.deco-right {
  position: relative;
  width: 150px;
  height: 300px;
  flex-shrink: 0;
  pointer-events: none;
}

.floating-rock {
  position: absolute;
  border-radius: 30% 70% 70% 30% / 30% 30% 70% 70%;
  background: rgba(100, 200, 255, 0.08);

  &.rock-1 {
    width: 40px;
    height: 35px;
    top: 40px;
    right: 20px;
    animation: rockFloat 5s ease-in-out infinite;
    will-change: transform;
  }

  &.rock-2 {
    width: 25px;
    height: 22px;
    bottom: 100px;
    left: 10px;
    animation: rockFloat 7s ease-in-out infinite reverse;
    will-change: transform;
  }

  &.rock-3 {
    width: 18px;
    height: 16px;
    bottom: 40px;
    right: 40px;
    animation: rockFloat 6s ease-in-out infinite 1s;
    will-change: transform;
  }
}

@keyframes rockFloat {
  0%, 100% { transform: translateY(0) rotate(0deg); }
  50% { transform: translateY(-10px) rotate(10deg); }
}

.satellite {
  position: absolute;
  top: 50%;
  right: 0;
  width: 50px;
  height: 50px;
  animation: satelliteOrbit 15s linear infinite;
  will-change: transform;

  .satellite-svg {
    width: 100%;
    height: 100%;
  }
}

@keyframes satelliteOrbit {
  0% { transform: translateY(-50%) rotate(0deg) translateX(20px) rotate(0deg); }
  100% { transform: translateY(-50%) rotate(360deg) translateX(20px) rotate(-360deg); }
}

// ==================== 响应式 ====================
@media (max-width: 768px) {
  .error-container {
    gap: 20px;
    padding: 0 20px;
  }

  .deco-left,
  .deco-right {
    display: none;
  }

  .astronaut {
    width: 100px;
    height: 100px;
  }

  .error-code {
    .code-num {
      font-size: 70px;
    }
    .code-planet {
      width: 50px;
      height: 50px;
    }
  }

  .error-title {
    font-size: 20px;
  }

  .error-desc {
    font-size: 13px;
  }

  .error-actions {
    flex-direction: column;
    align-items: center;

    .el-button {
      width: 200px;
      justify-content: center;
    }
  }
}

// ==================== 减少动画偏好 ====================
@media (prefers-reduced-motion: reduce) {
  *, *::before, *::after {
    animation-duration: 0.01ms !important;
    animation-iteration-count: 1 !important;
    transition-duration: 0.01ms !important;
  }
}
</style>
