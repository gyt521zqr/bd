<template>
  <!-- 注册页 -->
  <div class="register-page">
    <div class="register-container">
      <!-- 步骤进度条 -->
      <div class="step-progress">
        <div class="step-item" :class="{ active: currentStep >= 1, completed: currentStep > 1 }">
          <div class="step-circle">
            <el-icon v-if="currentStep > 1"><Check /></el-icon>
            <span v-else>1</span>
          </div>
          <span class="step-label">验证手机</span>
        </div>
        <div class="step-line" :class="{ active: currentStep >= 2 }"></div>
        <div class="step-item" :class="{ active: currentStep >= 2, completed: currentStep > 2 }">
          <div class="step-circle">
            <el-icon v-if="currentStep > 2"><Check /></el-icon>
            <span v-else>2</span>
          </div>
          <span class="step-label">设置信息</span>
        </div>
        <div class="step-line" :class="{ active: currentStep >= 3 }"></div>
        <div class="step-item" :class="{ active: currentStep >= 3 }">
          <div class="step-circle">
            <span>3</span>
          </div>
          <span class="step-label">完成注册</span>
        </div>
      </div>

      <div class="register-header">
        <div class="logo">
          <span class="logo-icon">M</span>
          <span class="logo-text">码高教育</span>
        </div>
        <h2>创建账号</h2>
        <p>加入码高，开启编程学习之旅</p>
      </div>

      <!-- 步骤1：验证手机 -->
      <div v-if="currentStep === 1" class="step-content">
        <el-form ref="phoneFormRef" :model="form" :rules="phoneRules" size="large" label-position="top">
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号" :prefix-icon="Phone" />
          </el-form-item>
          <el-form-item label="验证码" prop="smsCode">
            <div class="sms-row">
              <el-input v-model="form.smsCode" placeholder="请输入验证码" />
              <el-button
                :disabled="countdown > 0"
                @click="handleSendSms"
                class="sms-btn"
                :class="{ counting: countdown > 0 }"
              >
                <span v-if="countdown > 0" class="countdown-text">
                  <el-icon><Timer /></el-icon>{{ countdown }}s
                </span>
                <span v-else>获取验证码</span>
              </el-button>
            </div>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" class="next-btn" @click="goStep2" round>
              下一步 <el-icon class="btn-icon"><ArrowRight /></el-icon>
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 步骤2：设置信息 -->
      <div v-if="currentStep === 2" class="step-content">
        <el-form ref="formRef" :model="form" :rules="rules" size="large" label-position="top">
          <el-form-item label="昵称" prop="nickname">
            <el-input v-model="form.nickname" placeholder="请输入昵称" />
          </el-form-item>
          <el-form-item label="密码" prop="password">
            <el-input v-model="form.password" type="password" placeholder="8-20位，包含字母和数字" show-password />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" show-password />
          </el-form-item>
          <!-- 合规必需(P0-19)：协议勾选 + 监护人同意留痕 -->
          <el-form-item prop="agreed">
            <el-checkbox v-model="form.agreed" class="agree-checkbox">
              我已阅读并同意
              <router-link to="/legal/agreement" target="_blank">《用户协议》</router-link>
              与
              <router-link to="/legal/privacy" target="_blank">《隐私政策》</router-link>
            </el-checkbox>
          </el-form-item>
          <el-form-item prop="guardianConsent">
            <el-checkbox v-model="form.guardianConsent" class="agree-checkbox">
              本人为监护人（或已取得监护人同意），已阅读并同意
              <router-link to="/legal/children" target="_blank">《儿童个人信息保护规则》</router-link>
            </el-checkbox>
          </el-form-item>
          <el-form-item>
            <div class="form-actions">
              <el-button class="prev-btn" @click="currentStep = 1" round>
                <el-icon><ArrowLeft /></el-icon> 上一步
              </el-button>
              <el-button type="primary" class="register-btn" @click="handleRegister" :loading="loading" round>
                注 册
              </el-button>
            </div>
          </el-form-item>
        </el-form>
      </div>

      <!-- 步骤3：完成 -->
      <div v-if="currentStep === 3" class="step-content success-step">
        <div class="success-icon">
          <el-icon :size="64" color="#36D399"><CircleCheckFilled /></el-icon>
        </div>
        <h3>注册成功！</h3>
        <p>欢迎加入码高少儿编程平台</p>
        <el-button type="primary" class="login-btn" @click="$router.push('/login')" round>
          去登录
        </el-button>
      </div>

      <div class="register-footer">
        <span>已有账号？</span>
        <router-link to="/login" class="login-link">立即登录</router-link>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * RegisterPage - 注册页
 * 手机号+验证码注册，设置昵称和密码，带步骤进度条
 */
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Phone, Check, Timer, ArrowRight, ArrowLeft, CircleCheckFilled } from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { register, sendSmsCode } from '@/api/user'
import { validatePhone } from '@/utils/validator'

useRouter()
const formRef = ref<FormInstance>()
const phoneFormRef = ref<FormInstance>()
const loading = ref(false)
const countdown = ref(0)
const currentStep = ref(1)

const form = reactive({
  phone: '',
  smsCode: '',
  nickname: '',
  password: '',
  confirmPassword: '',
  agreed: false,
  guardianConsent: false,
})

const validateConfirm = (_r: any, v: string, cb: any) => {
  if (v !== form.password) cb(new Error('两次密码不一致'))
  else cb()
}

const phoneRules: FormRules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }, { validator: (_r: any, v: string, cb: any) => { if (!validatePhone(v)) cb(new Error('手机号格式不正确')); else cb() }, trigger: 'blur' }],
  smsCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }],
}

const rules: FormRules = {
  nickname: [{ required: true, message: '请输入昵称', trigger: 'blur' }, { min: 2, max: 20, message: '昵称2-20个字符', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }, { min: 8, max: 20, message: '密码8-20位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认密码', trigger: 'blur' }, { validator: validateConfirm, trigger: 'blur' }],
  agreed: [{ validator: (_r: any, v: boolean, cb: any) => v ? cb() : cb(new Error('请先阅读并同意用户协议与隐私政策')), trigger: 'change' }],
  guardianConsent: [{ validator: (_r: any, v: boolean, cb: any) => v ? cb() : cb(new Error('请确认已取得监护人同意')), trigger: 'change' }],
}

async function goStep2() {
  const valid = await phoneFormRef.value?.validate().catch(() => false)
  if (!valid) return
  currentStep.value = 2
}

async function handleRegister() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await register({
      phone: form.phone,
      smsCode: form.smsCode,
      nickname: form.nickname,
      password: form.password,
      agreed: form.agreed,
      guardianConsent: form.guardianConsent,
    })
    ElMessage.success('注册成功')
    currentStep.value = 3
  } catch (error: any) {
    ElMessage.error(error.message || '注册失败')
  } finally {
    loading.value = false
  }
}

async function handleSendSms() {
  if (!validatePhone(form.phone)) {
    ElMessage.warning('请输入正确的手机号')
    return
  }
  try {
    await sendSmsCode(form.phone, 'register')
    ElMessage.success('验证码已发送')
    countdown.value = 60
    const timer = setInterval(() => {
      countdown.value--
      if (countdown.value <= 0) clearInterval(timer)
    }, 1000)
  } catch {
    ElMessage.error('发送验证码失败')
  }
}
</script>

<style scoped lang="scss">

.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #F7F8FA;
  padding: 20px;
}

.register-container {
  width: 480px;
  background: $bg-white;
  border-radius: 24px;
  padding: 40px 44px;
  box-shadow: 0 20px 60px rgba(22, 93, 255, 0.1), 0 4px 16px rgba(0, 0, 0, 0.04);
}

// ==================== 步骤进度条 ====================
.step-progress {
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 32px;
  padding: 0 8px;
}

.step-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  position: relative;
  z-index: 1;

  .step-circle {
    width: 36px;
    height: 36px;
    border-radius: 50%;
    background: #F2F3F5;
    color: #86909C;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 14px;
    font-weight: 600;
    transition: background 0.4s cubic-bezier(0.34, 1.56, 0.64, 1), color 0.4s cubic-bezier(0.34, 1.56, 0.64, 1), box-shadow 0.4s cubic-bezier(0.34, 1.56, 0.64, 1), transform 0.4s cubic-bezier(0.34, 1.56, 0.64, 1), border-color 0.4s cubic-bezier(0.34, 1.56, 0.64, 1);
    border: 2px solid transparent;
  }

  .step-label {
    font-size: 12px;
    color: #86909C;
    font-weight: 500;
    transition: color 0.3s ease;
  }

  &.active {
    .step-circle {
      background: linear-gradient(135deg, $primary-color, #4080FF);
      color: #fff;
      box-shadow: 0 4px 12px rgba(22, 93, 255, 0.35);
      transform: scale(1.05);
    }
    .step-label {
      color: $primary-color;
      font-weight: 600;
    }
  }

  &.completed {
    .step-circle {
      background: linear-gradient(135deg, $success-color, #36D399);
      color: #fff;
      box-shadow: 0 4px 12px rgba(54, 211, 153, 0.35);
    }
    .step-label {
      color: $success-color;
    }
  }
}

.step-line {
  flex: 1;
  height: 3px;
  background: #F2F3F5;
  border-radius: 2px;
  margin: 0 8px;
  margin-bottom: 20px;
  position: relative;
  overflow: hidden;
  transition: background 0.4s ease;

  &::after {
    content: '';
    position: absolute;
    left: 0;
    top: 0;
    height: 100%;
    width: 0;
    background: linear-gradient(90deg, $primary-color, $success-color);
    border-radius: 2px;
    transition: width 0.5s ease;
  }

  &.active::after {
    width: 100%;
  }
}

// ==================== 头部 ====================
.register-header {
  text-align: center;
  margin-bottom: 28px;
  .logo {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    margin-bottom: 16px;
    .logo-icon {
      width: 40px; height: 40px;
      background: linear-gradient(135deg, $primary-color, $action-color);
      color: #fff; border-radius: 12px;
      display: flex; align-items: center; justify-content: center;
      font-weight: bold; font-size: 20px;
      box-shadow: 0 4px 12px rgba(22, 93, 255, 0.25);
    }
    .logo-text { font-size: 20px; font-weight: 700; color: #1D2129; }
  }
  h2 { font-size: 24px; color: #1D2129; margin-bottom: 8px; font-weight: 700; }
  p { font-size: 14px; color: #86909C; }
}

// ==================== 表单卡片 ====================
.step-content {
  animation: fadeInUp 0.4s ease;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(12px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.sms-row {
  display: flex;
  gap: 10px;
  width: 100%;

  .el-input {
    flex: 1;
  }
}

.sms-btn {
  flex-shrink: 0;
  min-width: 120px;
  border-radius: 10px;
  font-weight: 500;
  transition: background 0.3s ease, color 0.3s ease, border-color 0.3s ease;
  border: 1px solid #E5E6EB;

  &:not(:disabled):hover {
    background: rgba(22, 93, 255, 0.08);
    color: $primary-color;
    border-color: $primary-color;
  }

  &.counting {
    background: #F2F3F5;
    color: #86909C;
    border-color: #E5E6EB;
    cursor: not-allowed;
  }

  .countdown-text {
    display: flex;
    align-items: center;
    gap: 4px;
    font-variant-numeric: tabular-nums;
  }
}

// 按钮样式
.next-btn, .register-btn, .login-btn {
  width: 100%;
  height: 46px;
  font-size: 16px;
  font-weight: 600;
  background: linear-gradient(135deg, $primary-color, #4080FF);
  border: none;
  box-shadow: 0 4px 16px rgba(22, 93, 255, 0.3);
  transition: transform 0.3s cubic-bezier(0.34, 1.56, 0.64, 1), box-shadow 0.3s cubic-bezier(0.34, 1.56, 0.64, 1), background 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);

  &:hover {
    transform: scale(1.02);
    box-shadow: 0 6px 24px rgba(22, 93, 255, 0.4);
  }

  .btn-icon {
    margin-left: 4px;
    transition: transform 0.3s ease;
  }

  &:hover .btn-icon {
    transform: translateX(4px);
  }
}

.agree-checkbox {
  font-size: 13px;
  line-height: 1.6;
  text-align: left;
  :deep(.el-checkbox__label) { white-space: normal; }
  a {
    color: $primary-color;
    text-decoration: none;
    &:hover { text-decoration: underline; }
  }
}

.form-actions {
  display: flex;
  gap: 12px;
  width: 100%;

  .prev-btn {
    flex: 1;
    height: 46px;
    border-radius: 23px;
    font-weight: 500;
    transition: background 0.3s ease, color 0.3s ease;

    &:hover {
      background: #F2F3F5;
      color: $primary-color;
    }
  }

  .register-btn {
    flex: 2;
  }
}

// ==================== 成功步骤 ====================
.success-step {
  text-align: center;
  padding: 20px 0;

  .success-icon {
    margin-bottom: 20px;
    animation: scaleIn 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
  }

  h3 {
    font-size: 22px;
    color: #1D2129;
    margin-bottom: 8px;
    font-weight: 700;
  }

  p {
    font-size: 14px;
    color: #86909C;
    margin-bottom: 28px;
  }
}

@keyframes scaleIn {
  from {
    opacity: 0;
    transform: scale(0.5);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.register-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #86909C;

  .login-link {
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

// 表单输入框样式优化
:deep(.el-form-item__label) {
  font-weight: 500;
  color: #4E5969;
  padding-bottom: 4px;
}

:deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px #E5E6EB inset;
  transition: box-shadow 0.3s ease;

  &:hover, &:focus, &.is-focus {
    box-shadow: 0 0 0 1px $primary-color inset;
  }
}

// ==================== 响应式 ====================
@media (max-width: 768px) {
  .register-page {
    padding: 16px;
    background: $bg-white;
  }
  .register-container {
    width: 100%;
    padding: 24px 20px;
    box-shadow: none;
    border-radius: 0;
  }
  .step-progress {
    margin-bottom: 24px;
  }
  .step-label {
    font-size: 11px;
  }
  .register-header {
    margin-bottom: 20px;
    h2 { font-size: 20px; }
  }
  .form-actions {
    flex-direction: column;

    .prev-btn, .register-btn {
      width: 100%;
    }
  }
}
</style>
