<template>
  <!-- 设置页：修改密码/绑定家长/消息偏好 -->
  <div class="settings-page">
    <div class="page-header">
      <div class="header-bar"></div>
      <h2 class="page-title">账号设置</h2>
    </div>

    <div class="settings-content">
      <!-- 修改密码 -->
      <div class="settings-card">
        <div class="card-header">
          <div class="header-accent"></div>
          <h3>修改密码</h3>
        </div>
        <el-form ref="pwdFormRef" :model="pwdForm" :rules="pwdRules" label-width="100px" size="large">
          <el-form-item label="当前密码" prop="oldPassword">
            <el-input v-model="pwdForm.oldPassword" type="password" show-password placeholder="请输入当前密码" />
          </el-form-item>
          <el-form-item label="新密码" prop="newPassword">
            <el-input v-model="pwdForm.newPassword" type="password" show-password placeholder="请输入新密码" />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input v-model="pwdForm.confirmPassword" type="password" show-password placeholder="请再次输入新密码" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleChangePwd" :loading="pwdLoading" class="save-btn">
              保存修改
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 绑定家长 -->
      <div class="settings-card">
        <div class="card-header">
          <div class="header-accent accent-green"></div>
          <h3>绑定家长手机</h3>
        </div>
        <p class="card-desc">绑定家长手机号后，可接收孩子的学习报告通知</p>
        <el-form :model="parentForm" label-width="100px" size="large">
          <el-form-item label="家长手机">
            <el-input v-model="parentForm.phone" placeholder="请输入家长手机号" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleBindParent" :loading="bindLoading" class="save-btn">
              绑定
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 消息偏好 -->
      <div class="settings-card">
        <div class="card-header">
          <div class="header-accent accent-orange"></div>
          <h3>消息通知设置</h3>
        </div>
        <div class="notify-list">
          <div class="notify-item" v-for="(item, idx) in notifyList" :key="idx">
            <div class="notify-info">
              <span class="notify-name">{{ item.name }}</span>
              <span class="notify-desc">{{ item.desc }}</span>
            </div>
            <label class="switch-wrapper">
              <input
                type="checkbox"
                :checked="item.model"
                @change="item.model = ($event.target as HTMLInputElement).checked"
                class="switch-input"
              />
              <span class="switch-slider">
                <span class="switch-knob"></span>
              </span>
            </label>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * SettingsPage - 设置页
 * 修改密码、绑定家长手机、消息通知偏好
 */
import { ref, reactive, computed } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { changePassword, bindParentPhone } from '@/api/user'

const pwdFormRef = ref<FormInstance>()
const pwdLoading = ref(false)
const bindLoading = ref(false)

const pwdForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const validateConfirm = (_r: any, v: string, cb: any) => {
  if (v !== pwdForm.newPassword) cb(new Error('两次密码不一致'))
  else cb()
}

const pwdRules: FormRules = {
  oldPassword: [{ required: true, message: '请输入当前密码', trigger: 'blur' }],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }, { min: 6, message: '密码至少6位', trigger: 'blur' }],
  confirmPassword: [{ required: true, message: '请确认密码', trigger: 'blur' }, { validator: validateConfirm, trigger: 'blur' }],
}

const parentForm = reactive({ phone: '' })

const notifySettings = reactive({
  courseUpdate: true,
  examReminder: true,
  weeklyReport: true,
  promotion: false,
})

const notifyList = computed(() => [
  { name: '课程更新通知', desc: '当有新课程发布时通知我', model: notifySettings.courseUpdate },
  { name: '考试提醒', desc: '考试开始前提醒我', model: notifySettings.examReminder },
  { name: '学习报告', desc: '每周发送学习进度报告', model: notifySettings.weeklyReport },
  { name: '营销活动', desc: '接收优惠活动和促销信息', model: notifySettings.promotion },
])

async function handleChangePwd() {
  const valid = await pwdFormRef.value?.validate().catch(() => false)
  if (!valid) return
  pwdLoading.value = true
  try {
    await changePassword(pwdForm)
    ElMessage.success('密码修改成功')
    pwdForm.oldPassword = ''
    pwdForm.newPassword = ''
    pwdForm.confirmPassword = ''
  } catch (error: any) {
    ElMessage.error(error.message || '修改失败')
  } finally {
    pwdLoading.value = false
  }
}

async function handleBindParent() {
  if (!parentForm.phone) {
    ElMessage.warning('请输入家长手机号')
    return
  }
  bindLoading.value = true
  try {
    await bindParentPhone(parentForm.phone)
    ElMessage.success('绑定成功')
  } catch (error: any) {
    ElMessage.error(error.message || '绑定失败')
  } finally {
    bindLoading.value = false
  }
}
</script>

<style scoped lang="scss">
$primary: #165DFF;
$action: #FF7D00;
$success: #36D399;

.settings-page {
  max-width: 800px;
  margin: 0 auto;
  padding: 20px;
}

// 页面标题 + 左侧色条
.page-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 24px;

  .header-bar {
    width: 5px;
    height: 28px;
    background: linear-gradient(180deg, $primary, $action);
    border-radius: 3px;
  }
}

.page-title {
  font-size: 24px;
  color: #1D2129;
  font-weight: 700;
  margin: 0;
}

.settings-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

// 设置卡片
.settings-card {
  background: $bg-white;
  border-radius: 16px;
  padding: 28px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.06);
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s ease;

  &:hover {
    box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  }
}

// 卡片标题 + 左侧色条
.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;

  .header-accent {
    width: 4px;
    height: 20px;
    background: linear-gradient(180deg, $primary, #4080FF);
    border-radius: 2px;

    &.accent-green {
      background: linear-gradient(180deg, $success, #36D399);
    }

    &.accent-orange {
      background: linear-gradient(180deg, $action, #FFB84D);
    }
  }

  h3 {
    font-size: 17px;
    color: #1D2129;
    margin: 0;
    font-weight: 700;
  }
}

.card-desc {
  font-size: 13px;
  color: #86909C;
  margin-bottom: 20px;
  margin-top: -8px;
  padding-left: 16px;
}

.save-btn {
  border-radius: 10px;
  font-weight: 500;
  padding: 10px 28px;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s cubic-bezier(0.34, 1.56, 0.64, 1);

  &:hover {
    transform: scale(1.03);
    box-shadow: 0 4px 16px rgba(22, 93, 255, 0.3);
  }
}

// ==================== 开关动画 ====================
.notify-list {
  display: flex;
  flex-direction: column;
}

.notify-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
  border-bottom: 1px solid #F2F3F5;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.25s ease;

  &:hover {
    background: rgba(22, 93, 255, 0.02);
    margin: 0 -16px;
    padding-left: 16px;
    padding-right: 16px;
    border-radius: 8px;
  }

  &:last-child {
    border-bottom: none;
  }
}

.notify-info {
  display: flex;
  flex-direction: column;
  gap: 4px;

  .notify-name {
    font-size: 15px;
    color: #1D2129;
    font-weight: 500;
  }

  .notify-desc {
    font-size: 13px;
    color: #86909C;
  }
}

// 自定义开关
.switch-wrapper {
  position: relative;
  display: inline-block;
  width: 52px;
  height: 28px;
  cursor: pointer;
  flex-shrink: 0;
}

.switch-input {
  opacity: 0;
  width: 0;
  height: 0;

  &:checked + .switch-slider {
    background: linear-gradient(135deg, $primary, #4080FF);
    box-shadow: 0 2px 8px rgba(22, 93, 255, 0.3);

    .switch-knob {
      transform: translateX(24px);
      background: $bg-white;
    }
  }

  &:focus + .switch-slider {
    box-shadow: 0 0 0 3px rgba(22, 93, 255, 0.15);
  }
}

.switch-slider {
  position: absolute;
  inset: 0;
  background: #E5E6EB;
  border-radius: 28px;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
  display: flex;
  align-items: center;
  padding: 2px;
}

.switch-knob {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background: $bg-white;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
  transform: translateX(0);
}

// 输入框样式
:deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px #E5E6EB inset;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s ease;

  &:hover, &:focus, &.is-focus {
    box-shadow: 0 0 0 1px $primary inset;
  }
}

:deep(.el-form-item__label) {
  font-weight: 500;
  color: #4E5969;
}

// ==================== 响应式 ====================
@media (max-width: 768px) {
  .settings-page {
    padding: 12px;
  }

  .settings-card {
    padding: 20px;
  }

  .page-title {
    font-size: 20px;
  }
}
</style>
