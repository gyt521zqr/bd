<template>
  <!-- 登录页：手机号/密码/短信验证码/微信扫码Tab切换 -->
  <div class="login-page">
    <div class="login-container">
      <!-- 左侧装饰 - 动态渐变背景 -->
      <div class="login-banner">
        <div class="gradient-bg">
          <div class="blob blob-1"></div>
          <div class="blob blob-2"></div>
          <div class="blob blob-3"></div>
        </div>
        <div class="banner-content">
          <h1>码高少儿编程</h1>
          <p>让孩子在快乐中学习编程，开启未来无限可能</p>
          <div class="banner-features">
            <div class="feature-item"><el-icon><Monitor /></el-icon>趣味互动教学</div>
            <div class="feature-item"><el-icon><Trophy /></el-icon>成长体系激励</div>
            <div class="feature-item"><el-icon><User /></el-icon>专业师资团队</div>
          </div>
        </div>
      </div>
      <!-- 右侧登录表单 - 毛玻璃效果 -->
      <div class="login-form-wrapper">
        <div class="glass-card">
          <div class="login-logo">
            <span class="logo-icon">M</span>
            <span class="logo-text">码高教育</span>
          </div>
          <h2 class="login-title">欢迎登录</h2>
          <!-- Tab 切换 -->
          <el-tabs v-model="loginType" class="login-tabs">
            <el-tab-pane label="密码登录" name="password" />
            <el-tab-pane label="短信登录" name="sms" />
            <el-tab-pane label="微信登录" name="wechat" />
          </el-tabs>
          <!-- 密码登录表单 -->
          <el-form v-if="loginType === 'password'" ref="passwordFormRef" :model="passwordForm" :rules="passwordRules" size="large">
            <el-form-item prop="phone">
              <el-input v-model="passwordForm.phone" placeholder="请输入用户名或手机号" :prefix-icon="Phone" class="underline-input" />
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="passwordForm.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password class="underline-input" />
            </el-form-item>
            <el-form-item>
              <div class="form-extra">
                <el-checkbox v-model="rememberMe">记住我</el-checkbox>
                <el-link type="primary" :underline="false">忘记密码？</el-link>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" class="login-btn" @click="handlePasswordLogin" :loading="loading" round>
                登 录
              </el-button>
            </el-form-item>
          </el-form>
          <!-- 短信登录表单 -->
          <el-form v-if="loginType === 'sms'" ref="smsFormRef" :model="smsForm" :rules="smsRules" size="large">
            <el-form-item prop="phone">
              <el-input v-model="smsForm.phone" placeholder="请输入手机号" :prefix-icon="Phone" class="underline-input" />
            </el-form-item>
            <el-form-item prop="smsCode">
              <div class="sms-input">
                <el-input v-model="smsForm.smsCode" placeholder="请输入验证码" class="underline-input" />
                <el-button :disabled="smsCountdown > 0" @click="handleSendSms" class="sms-btn">
                  {{ smsCountdown > 0 ? `${smsCountdown}s后重试` : '获取验证码' }}
                </el-button>
              </div>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" class="login-btn" @click="handleSmsLogin" :loading="loading" round>
                登 录
              </el-button>
            </el-form-item>
          </el-form>
          <!-- 微信扫码登录 -->
          <div v-if="loginType === 'wechat'" class="wechat-login">
            <div class="qrcode-placeholder">
              <el-icon :size="48"><ChatDotRound /></el-icon>
              <p>微信扫码登录</p>
            </div>
            <p class="wechat-tip">请使用微信扫一扫登录</p>
          </div>
          <!-- 底部链接 -->
          <div class="login-footer">
            <span>还没有账号？</span>
            <router-link to="/register" class="register-link">立即注册</router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * LoginPage - 登录页
 * 支持密码登录、短信验证码登录、微信扫码登录三种方式
 */
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Phone, Lock, Monitor, Trophy, User, ChatDotRound } from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { useUserStore } from '@/stores/user'
import { sendSmsCode } from '@/api/user'
import { validatePhone } from '@/utils/validator'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginType = ref('password')
const loading = ref(false)
const rememberMe = ref(false)
const passwordFormRef = ref<FormInstance>()
const smsFormRef = ref<FormInstance>()
const smsCountdown = ref(0)

/** 密码登录表单 */
const passwordForm = reactive({
  phone: '',
  password: '',
})

/** 短信登录表单 */
const smsForm = reactive({
  phone: '',
  smsCode: '',
})

/** 密码登录验证规则 */
const passwordRules: FormRules = {
  phone: [{ required: true, message: '请输入用户名或手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }],
}

/** 短信登录验证规则 */
const smsRules: FormRules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  smsCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }, { len: 6, message: '验证码为6位', trigger: 'blur' }],
}

/** 密码登录 */
async function handlePasswordLogin() {
  const valid = await passwordFormRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.login({ phone: passwordForm.phone, password: passwordForm.password, loginType: 'password' })
    ElMessage.success('登录成功')
    const redirect = (route.query.redirect as string) || '/'
    router.push(redirect)
  } catch (error: any) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

/** 短信登录 */
async function handleSmsLogin() {
  const valid = await smsFormRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userStore.login({ phone: smsForm.phone, password: smsForm.smsCode, loginType: 'sms' })
    ElMessage.success('登录成功')
    router.push('/')
  } catch (error: any) {
    ElMessage.error(error.message || '登录失败')
  } finally {
    loading.value = false
  }
}

/** 发送短信验证码 */
async function handleSendSms() {
  if (!validatePhone(smsForm.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  try {
    await sendSmsCode(smsForm.phone, 'login')
    ElMessage.success('验证码已发送')
    smsCountdown.value = 60
    const timer = setInterval(() => {
      smsCountdown.value--
      if (smsCountdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch {
    ElMessage.error('发送验证码失败')
  }
}
</script>

<style scoped lang="scss">

.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background:
    radial-gradient(circle at 20% 30%, rgba(22, 93, 255, 0.06) 0%, transparent 40%),
    radial-gradient(circle at 80% 70%, rgba(255, 125, 0, 0.04) 0%, transparent 40%),
    linear-gradient(180deg, #F7F8FA 0%, #E8F0FF 100%);
  position: relative;
  overflow: hidden;

  /* 装饰性背景粒子 */
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background-image:
      radial-gradient(circle at 15% 50%, rgba(22, 93, 255, 0.04) 0%, transparent 2%),
      radial-gradient(circle at 85% 30%, rgba(54, 211, 153, 0.03) 0%, transparent 2%),
      radial-gradient(circle at 50% 80%, rgba(255, 125, 0, 0.03) 0%, transparent 2%);
    pointer-events: none;
    z-index: 0;
  }
}

.login-container {
  display: flex;
  width: 960px;
  min-height: 560px;
  background: $bg-white;
  border-radius: 28px;
  overflow: hidden;
  box-shadow: 0 24px 80px rgba(22, 93, 255, 0.12), 0 8px 24px rgba(0, 0, 0, 0.06);
  position: relative;
  z-index: 1;
}

// ==================== 左侧动态渐变背景 ====================
.login-banner {
  width: 420px;
  position: relative;
  padding: 48px 36px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: #fff;
  overflow: hidden;
  background: linear-gradient(135deg, #165DFF 0%, #0E42D2 50%, #165DFF 100%);

  .gradient-bg {
    position: absolute;
    inset: 0;
    overflow: hidden;
    z-index: 0;
  }

  .blob {
    position: absolute;
    border-radius: 50%;
    filter: blur(60px);
    opacity: 0.6;
    animation: blobMove 8s ease-in-out infinite;
    will-change: transform, opacity;
  }

  .blob-1 {
    width: 300px;
    height: 300px;
    background: linear-gradient(135deg, #36D399, #165DFF);
    top: -80px;
    left: -60px;
    animation-delay: 0s;
  }

  .blob-2 {
    width: 250px;
    height: 250px;
    background: linear-gradient(135deg, #FF7D00, #FF9500);
    bottom: -40px;
    right: -40px;
    animation-delay: -2s;
  }

  .blob-3 {
    width: 200px;
    height: 200px;
    background: linear-gradient(135deg, #165DFF, #36D399);
    top: 50%;
    left: 50%;
    animation-delay: -4s;
  }

  @keyframes blobMove {
    0%, 100% {
      transform: translate(0, 0) scale(1);
    }
    25% {
      transform: translate(30px, -30px) scale(1.1);
    }
    50% {
      transform: translate(-20px, 20px) scale(0.95);
    }
    75% {
      transform: translate(20px, 10px) scale(1.05);
    }
  }

  .banner-content {
    position: relative;
    z-index: 1;
  }

  h1 {
    font-size: 30px;
    font-weight: 800;
    margin-bottom: 12px;
    text-shadow: 0 2px 8px rgba(0,0,0,0.15);
  }

  p {
    font-size: 14px;
    opacity: 0.9;
    margin-bottom: 36px;
    line-height: 1.6;
  }
}

.banner-features {
  display: flex;
  flex-direction: column;
  gap: 18px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  opacity: 0.95;
  padding: 10px 14px;
  background: rgba(255, 255, 255, 0.12);
  border-radius: 10px;
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.15);
  transition: background 0.3s ease, transform 0.3s ease;

  &:hover {
    background: rgba(255, 255, 255, 0.2);
    transform: translateX(4px);
  }

  .el-icon {
    font-size: 18px;
  }
}

// ==================== 右侧毛玻璃表单区 ====================
.login-form-wrapper {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 40px;
  background: linear-gradient(180deg, rgba(22, 93, 255, 0.03) 0%, rgba(255, 125, 0, 0.02) 100%);
}

.glass-card {
  width: 100%;
  max-width: 380px;
  padding: 36px 32px;
  background: rgba(255, 255, 255, 0.85);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border-radius: 20px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 0 8px 32px rgba(22, 93, 255, 0.08), inset 0 1px 0 rgba(255, 255, 255, 0.8);
}

.login-logo {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 24px;

  .logo-icon {
    width: 40px;
    height: 40px;
    background: linear-gradient(135deg, $primary-color, $action-color);
    color: #fff;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-weight: bold;
    font-size: 20px;
    box-shadow: 0 4px 12px rgba(22, 93, 255, 0.3);
  }

  .logo-text {
    font-size: 20px;
    font-weight: 700;
    color: #1D2129;
  }
}

.login-title {
  font-size: 24px;
  color: #1D2129;
  margin-bottom: 24px;
  font-weight: 700;
}

// 下划线动画输入框
.underline-input {
  :deep(.el-input__wrapper) {
    box-shadow: none !important;
    border-radius: 0;
    padding-left: 0;
    background: transparent;
    border-bottom: 2px solid #E5E6EB;
    transition: border-color 0.3s ease;

    &:hover, &:focus, &.is-focus {
      border-bottom-color: $primary-color;
    }
  }

  :deep(.el-input__inner) {
    padding-left: 8px;
  }

  :deep(.el-input__prefix) {
    color: #86909C;
  }
}

.form-extra {
  display: flex;
  justify-content: space-between;
  width: 100%;
  margin-top: 4px;
}

// 渐变登录按钮 + hover放大
.login-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, $primary-color, #4080FF);
  border: none;
  box-shadow: 0 4px 16px rgba(22, 93, 255, 0.35);
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1), box-shadow 0.3s cubic-bezier(0.34, 1.56, 0.64, 1), background 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);

  &:hover {
    transform: scale(1.03);
    box-shadow: 0 6px 24px rgba(22, 93, 255, 0.45);
    background: linear-gradient(135deg, #165DFF, #4080FF);
  }

  &:active {
    transform: scale(0.98);
  }
}

.sms-input {
  display: flex;
  gap: 8px;
  width: 100%;

  .el-input {
    flex: 1;
  }

  .sms-btn {
    flex-shrink: 0;
    border-radius: 8px;
    font-weight: 500;
    transition: background 0.3s ease, color 0.3s ease;

    &:not(:disabled):hover {
      background: rgba(22, 93, 255, 0.08);
      color: $primary-color;
    }
  }
}

.wechat-login {
  text-align: center;
  padding: 40px 0;
}

.qrcode-placeholder {
  width: 180px;
  height: 180px;
  margin: 0 auto 16px;
  background: linear-gradient(135deg, #f7f8fa, #E8F0FF);
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 8px;
  color: #86909C;
  border: 2px dashed #C9CDD4;
  transition: border-color 0.3s ease, background 0.3s ease, color 0.3s ease;

  &:hover {
    border-color: $primary-color;
    background: linear-gradient(135deg, #E8F0FF, #E8F0FF);
    color: $primary-color;
  }
}

.wechat-tip {
  font-size: 13px;
  color: #86909C;
}

.login-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #86909C;

  .register-link {
    color: $primary-color;
    font-weight: 600;
    margin-left: 4px;
    transition: color 0.3s ease;

    &:hover {
      color: $action-color;
      text-decoration: none;
    }
  }
}

// Tab样式优化
:deep(.login-tabs) {
  .el-tabs__nav-wrap::after {
    height: 1px;
    background: #E5E6EB;
  }

  .el-tabs__item {
    font-size: 15px;
    font-weight: 500;
    color: #86909C;
    transition: color 0.3s ease;

    &.is-active {
      color: $primary-color;
      font-weight: 600;
    }

    &:hover {
      color: $primary-color;
    }
  }

  .el-tabs__active-bar {
    background: linear-gradient(90deg, $primary-color, $action-color);
    height: 3px;
    border-radius: 2px;
  }
}

// ==================== 响应式 ====================
@media (max-width: 768px) {
  .login-page {
    padding: 16px;
    align-items: flex-start;
    background: $bg-white;
  }
  .login-container {
    width: 100%;
    flex-direction: column;
    min-height: auto;
    border-radius: 20px;
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  }
  .login-banner {
    width: 100%;
    padding: 28px 24px;
    min-height: 200px;

    .blob {
      filter: blur(40px);
    }

    h1 {
      font-size: 24px;
    }
    p {
      font-size: 13px;
      margin-bottom: 20px;
    }
  }
  .banner-features {
    flex-direction: row;
    flex-wrap: wrap;
    gap: 8px;
  }
  .feature-item {
    font-size: 12px;
    padding: 8px 10px;
  }
  .login-form-wrapper {
    padding: 24px 20px;
    background: $bg-white;
  }
  .glass-card {
    padding: 0;
    background: transparent;
    backdrop-filter: none;
    border: none;
    box-shadow: none;
  }
  .login-logo {
    margin-bottom: 16px;
  }
  .login-title {
    font-size: 20px;
    margin-bottom: 20px;
  }
}
</style>
