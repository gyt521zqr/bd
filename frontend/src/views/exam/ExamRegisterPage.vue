<template>
  <!-- 赛事报名页面 -->
  <div class="exam-register-page">
    <!-- 顶部进度条 - Stepper -->
    <div class="progress-bar">
      <div class="progress-inner">
        <div class="progress-step" :class="{ active: step >= 1, current: step === 1 }">
          <div class="step-num">
            <el-icon v-if="step > 1" size="16"><Check /></el-icon>
            <span v-else>1</span>
          </div>
          <div class="step-label">填写信息</div>
          <div class="step-desc">完善个人资料</div>
        </div>
        <div class="progress-line" :class="{ active: step >= 2 }">
          <div class="progress-line-fill"></div>
        </div>
        <div class="progress-step" :class="{ active: step >= 2, current: step === 2 }">
          <div class="step-num">
            <el-icon v-if="step > 2" size="16"><Check /></el-icon>
            <span v-else>2</span>
          </div>
          <div class="step-label">确认信息</div>
          <div class="step-desc">核对并验证</div>
        </div>
        <div class="progress-line" :class="{ active: step >= 3 }">
          <div class="progress-line-fill"></div>
        </div>
        <div class="progress-step" :class="{ active: step >= 3, current: step === 3 }">
          <div class="step-num">
            <el-icon v-if="step > 3" size="16"><Check /></el-icon>
            <span v-else>3</span>
          </div>
          <div class="step-label">报名成功</div>
          <div class="step-desc">获取准考证</div>
        </div>
      </div>
    </div>

    <!-- 步骤1：填写信息 -->
    <div class="register-container" v-if="step === 1">
      <div class="register-card">
        <div class="register-header">
          <h2 class="register-title">填写报名信息</h2>
          <p class="register-subtitle">请如实填写以下信息，确保参赛资格有效</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          label-position="top"
          class="register-form"
        >
          <el-form-item label="姓名" prop="name">
            <el-input
              v-model="form.name"
              placeholder="请输入真实姓名"
              clearable
              class="glow-input"
            />
          </el-form-item>

          <el-form-item label="学校" prop="school">
            <el-input
              v-model="form.school"
              placeholder="请输入学校全称"
              clearable
              class="glow-input"
            />
          </el-form-item>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="年级" prop="grade">
                <el-select
                  v-model="form.grade"
                  placeholder="请选择年级"
                  class="w-full glow-select"
                  @change="onGradeChange"
                >
                  <el-option-group label="小学">
                    <el-option v-for="i in 6" :key="`p${i}`" :label="`小学${i}年级`" :value="`p${i}`" />
                  </el-option-group>
                  <el-option-group label="初中">
                    <el-option v-for="i in 3" :key="`j${i}`" :label="`初中${i}年级`" :value="`j${i}`" />
                  </el-option-group>
                  <el-option-group label="高中">
                    <el-option v-for="i in 3" :key="`s${i}`" :label="`高中${i}年级`" :value="`s${i}`" />
                  </el-option-group>
                </el-select>
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="参赛组别" prop="group">
                <el-select
                  v-model="form.group"
                  placeholder="请选择参赛组别"
                  class="w-full glow-select"
                  :disabled="!form.grade"
                >
                  <el-option
                    v-for="g in groupOptions"
                    :key="g.value"
                    :label="g.label"
                    :value="g.value"
                  />
                </el-select>
              </el-form-item>
            </el-col>
          </el-row>

          <el-form-item label="联系方式（手机号）" prop="phone">
            <el-input
              v-model="form.phone"
              placeholder="请输入11位手机号"
              clearable
              maxlength="11"
              class="glow-input"
            />
          </el-form-item>

          <el-form-item label="编程语言" prop="language">
            <el-radio-group v-model="form.language" class="glow-radio-group">
              <el-radio label="Scratch">Scratch</el-radio>
              <el-radio label="Python">Python</el-radio>
              <el-radio label="C++">C++</el-radio>
            </el-radio-group>
          </el-form-item>

          <el-divider content-position="left" class="section-divider">
            <span class="divider-text">紧急联系人</span>
          </el-divider>

          <el-row :gutter="16">
            <el-col :span="12">
              <el-form-item label="紧急联系人姓名" prop="emergencyName">
                <el-input
                  v-model="form.emergencyName"
                  placeholder="请输入姓名"
                  clearable
                  class="glow-input"
                />
              </el-form-item>
            </el-col>
            <el-col :span="12">
              <el-form-item label="紧急联系人电话" prop="emergencyPhone">
                <el-input
                  v-model="form.emergencyPhone"
                  placeholder="请输入电话"
                  clearable
                  maxlength="11"
                  class="glow-input"
                />
              </el-form-item>
            </el-col>
          </el-row>

          <div class="form-actions">
            <el-button
              type="primary"
              size="large"
              round
              class="submit-btn gradient-btn"
              @click="handleNext"
            >
              <span class="btn-text">下一步</span>
              <el-icon class="btn-arrow"><ArrowRight /></el-icon>
            </el-button>
          </div>
        </el-form>
      </div>
    </div>

    <!-- 步骤2：确认信息 -->
    <div class="register-container" v-if="step === 2">
      <div class="register-card">
        <div class="register-header">
          <h2 class="register-title">确认报名信息</h2>
          <p class="register-subtitle">请仔细核对以下信息，确认无误后提交</p>
        </div>

        <div class="confirm-list">
          <div class="confirm-item" v-for="(item, idx) in confirmItems" :key="idx">
            <span class="confirm-label">{{ item.label }}</span>
            <span class="confirm-value">{{ item.value }}</span>
          </div>
        </div>

        <!-- 验证码区域 -->
        <div class="captcha-area">
          <div class="captcha-row">
            <span class="captcha-label">请计算：{{ captchaQuestion }}</span>
            <el-input
              v-model="captchaInput"
              placeholder="请输入答案"
              style="width: 120px"
              class="glow-input"
              @input="captchaInput = captchaInput.replace(/[^\d]/g, '')"
            />
            <el-link type="primary" :underline="false" @click="generateCaptcha" class="refresh-link">
              <el-icon><Refresh /></el-icon>
              换一题
            </el-link>
          </div>
        </div>

        <div class="confirm-actions">
          <el-button size="large" round class="back-btn" @click="step = 1">
            <el-icon><ArrowLeft /></el-icon>
            返回修改
          </el-button>
          <el-button
            type="primary"
            size="large"
            round
            class="submit-btn gradient-btn"
            :disabled="!isCaptchaValid"
            @click="handleConfirm"
          >
            <span class="btn-text">确认提交</span>
            <el-icon class="btn-arrow"><Check /></el-icon>
          </el-button>
        </div>
      </div>
    </div>

    <!-- 步骤3：报名成功 -->
    <div class="register-container" v-if="step === 3">
      <div class="register-card success-card">
        <div class="success-animation">
          <div class="success-circle">
            <el-icon :size="48" color="#fff"><CircleCheckFilled /></el-icon>
          </div>
          <div class="success-ring ring-1"></div>
          <div class="success-ring ring-2"></div>
        </div>
        <h2 class="success-title">报名成功</h2>
        <p class="success-subtitle">恭喜您，赛事报名已成功提交！</p>

        <div class="success-info">
          <div class="info-row">
            <span class="info-label">报名号</span>
            <span class="info-value info-highlight">{{ registerNo }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">赛事名称</span>
            <span class="info-value">{{ examTitle || '暂未公布' }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">参赛组别</span>
            <span class="info-value">{{ groupLabel }}</span>
          </div>
          <div class="info-row">
            <span class="info-label">编程语言</span>
            <span class="info-value">{{ form.language }}</span>
          </div>
        </div>

        <el-alert
          title="重要提示"
          type="warning"
          :closable="false"
          show-icon
          class="success-tip"
        >
          <p>请截图保存此页面，报名号是查询成绩和下载证书的唯一凭证。</p>
        </el-alert>

        <div class="success-actions">
          <el-button type="primary" size="large" round class="gradient-btn" @click="$router.push('/exam')">
            返回赛事中心
          </el-button>
          <el-button size="large" round @click="$router.push('/exam/result-query')">
            成绩查询
          </el-button>
          <el-button type="warning" size="large" round @click="admissionTicketVisible = true">
            查看/下载准考证
          </el-button>
        </div>
      </div>
    </div>

    <!-- 报名前公告弹窗 -->
    <el-dialog
      v-model="announcementVisible"
      :title="announcementData.title"
      width="640px"
      align-center
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false"
      class="announcement-dialog"
    >
      <div class="announcement-content">
        <div class="announcement-section">
          <h3 class="announcement-section-title">
            <el-icon><InfoFilled /></el-icon>
            赛事介绍
          </h3>
          <p class="announcement-text">{{ announcementData.introduction }}</p>
        </div>

        <div class="announcement-section">
          <h3 class="announcement-section-title">
            <el-icon><List /></el-icon>
            参赛要求
          </h3>
          <ul class="announcement-list">
            <li v-for="(item, index) in announcementData.requirements" :key="`req-${index}`">
              {{ item }}
            </li>
          </ul>
        </div>

        <div class="announcement-section">
          <h3 class="announcement-section-title">
            <el-icon><Warning /></el-icon>
            注意事项
          </h3>
          <ul class="announcement-list">
            <li v-for="(item, index) in announcementData.notices" :key="`notice-${index}`">
              {{ item }}
            </li>
          </ul>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" size="large" class="gradient-btn" @click="announcementVisible = false">
          我已阅读并同意
        </el-button>
      </template>
    </el-dialog>

    <!-- 报名须知弹窗 -->
    <el-dialog
      v-model="noticeVisible"
      title="报名须知"
      width="560px"
      align-center
      :close-on-click-modal="false"
      class="notice-dialog"
    >
      <div class="notice-content">
        <p v-for="i in 5" :key="i">{{ i }}. {{ noticeTexts[i - 1] }}</p>
      </div>
      <template #footer>
        <el-button @click="noticeVisible = false">取消</el-button>
        <el-button type="primary" class="gradient-btn" @click="confirmSubmit">我已阅读并同意</el-button>
      </template>
    </el-dialog>

    <!-- 准考证弹窗 -->
    <el-dialog
      v-model="admissionTicketVisible"
      title="准考证"
      width="720px"
      align-center
      class="admission-ticket-dialog"
    >
      <div class="admission-ticket" id="admissionTicket">
        <div class="ticket-header">
          <h2 class="ticket-title">准 考 证</h2>
          <p class="ticket-exam-name">{{ examTitle || '暂未公布' }}</p>
        </div>

        <div class="ticket-body">
          <div class="ticket-photo-area">
            <div class="ticket-photo-placeholder">
              <span>考生照片</span>
            </div>
          </div>

          <div class="ticket-info-grid">
            <div class="ticket-info-row">
              <span class="ticket-label">报名号</span>
              <span class="ticket-value ticket-highlight">{{ registerNo }}</span>
            </div>
            <div class="ticket-info-row">
              <span class="ticket-label">姓名</span>
              <span class="ticket-value">{{ form.name }}</span>
            </div>
            <div class="ticket-info-row">
              <span class="ticket-label">学校</span>
              <span class="ticket-value">{{ form.school }}</span>
            </div>
            <div class="ticket-info-row">
              <span class="ticket-label">年级</span>
              <span class="ticket-value">{{ gradeLabel }}</span>
            </div>
            <div class="ticket-info-row">
              <span class="ticket-label">参赛组别</span>
              <span class="ticket-value">{{ groupLabel }}</span>
            </div>
            <div class="ticket-info-row">
              <span class="ticket-label">编程语言</span>
              <span class="ticket-value">{{ form.language }}</span>
            </div>
          </div>
        </div>

        <div class="ticket-exam-details">
          <div class="ticket-detail-row">
            <span class="ticket-label">考试时间</span>
            <span class="ticket-value">{{ examTime || '暂未公布' }}</span>
          </div>
          <div class="ticket-detail-row">
            <span class="ticket-label">考试地点</span>
            <span class="ticket-value">{{ examLocation || '暂未公布' }}<template v-if="examRoom"> {{ examRoom }}</template></span>
          </div>
        </div>

        <div class="ticket-notices">
          <h4 class="ticket-notices-title">注意事项</h4>
          <ol class="ticket-notices-list">
            <li>请提前15分钟到达考场，迟到超过15分钟不得进入考场。</li>
            <li>请携带准考证和有效身份证件（学生证或身份证）进入考场。</li>
            <li>考试期间不得使用手机、U盘等电子设备，违者取消成绩。</li>
            <li>考试采用上机考试形式，请熟悉所报编程语言的开发环境。</li>
            <li>如遇特殊情况需请假，请提前联系赛事组委会。</li>
          </ol>
        </div>
      </div>

      <template #footer>
        <el-button size="large" @click="admissionTicketVisible = false">关闭</el-button>
        <el-button type="primary" size="large" class="gradient-btn" @click="handlePrintTicket">打印准考证</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * ExamRegisterPage - 赛事报名页面
 * 三步流程：填写信息 -> 确认信息 -> 报名成功
 */
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import {
  CircleCheckFilled, Check, ArrowRight, ArrowLeft, Refresh,
  InfoFilled, List, Warning
} from '@element-plus/icons-vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { getExamList } from '@/api/exam'

const route = useRoute()
const _router = useRouter()
void _router
const examId = computed(() => Number(route.params.examId))
// 赛事信息：由真实接口（getExamList）填充，无数据时留空 / 显示「暂未公布」
const examTitle = ref('')
const examIntro = ref('')
const examTime = ref('')
const examLocation = ref('')
const examRoom = ref('')

const step = ref(1)
const noticeVisible = ref(false)
const announcementVisible = ref(true) // 报名前公告弹窗，页面加载时弹出
const admissionTicketVisible = ref(false) // 准考证弹窗
const registerNo = ref('')
const formRef = ref<FormInstance>()

// 验证码相关
const captchaQuestion = ref('')
const captchaAnswer = ref(0)
const captchaInput = ref('')
const isCaptchaValid = computed(() => captchaInput.value === String(captchaAnswer.value))

function generateCaptcha() {
  const a = Math.floor(Math.random() * 20) + 1
  const b = Math.floor(Math.random() * 20) + 1
  const isAdd = Math.random() > 0.5
  if (isAdd || a >= b) {
    captchaAnswer.value = a + b
    captchaQuestion.value = `${a} + ${b} = ?`
  } else {
    captchaAnswer.value = a - b
    captchaQuestion.value = `${a} - ${b} = ?`
  }
  captchaInput.value = ''
}

// 报名公告：仅通用条款；赛事介绍由真实接口数据驱动，不编造机构 / 日期 / 费用
const announcementData = computed(() => ({
  title: examTitle.value ? `${examTitle.value} - 报名公告` : '报名公告',
  introduction: examIntro.value || '赛事介绍暂未公布',
  requirements: [
    '请如实填写报名信息，确保参赛资格有效。',
    '每位选手仅可报名一个组别、一种编程语言。',
    '报名信息须真实有效，虚假信息将导致取消参赛资格。',
  ],
  notices: [
    '请仔细核对报名信息，提交后如需修改请联系赛事组委会。',
    '报名相关的具体时间、地点与费用以官方公布信息为准。',
    '请保持联系方式畅通，以便接收赛事通知。',
  ],
}))

/** 从真实接口加载考试详情；后端未下发考点 / 考场时保持空态 */
async function loadExamDetail() {
  try {
    const res = await getExamList({ pageNum: 1, pageSize: 100 })
    const list = res?.data?.list
    const matched = Array.isArray(list) ? list.find((item) => item.id === examId.value) : undefined
    if (matched) {
      examTitle.value = matched.title || ''
      examIntro.value = matched.description || ''
      examTime.value = matched.startTime || ''
      // 后端未下发考点 / 考场：保持空态，由模板显示「暂未公布」
    }
  } catch {
    ElMessage.warning('考试详情暂不可用，考点信息将显示为「暂未公布」')
  }
}

// 页面加载时弹出报名公告并加载真实考试详情
onMounted(() => {
  announcementVisible.value = true
  loadExamDetail()
})

const form = ref({
  name: '',
  school: '',
  grade: '',
  group: '',
  phone: '',
  language: 'C++',
  emergencyName: '',
  emergencyPhone: '',
})

// 年级联动参赛组别
const groupOptions = ref<{ label: string; value: string }[]>([])

function onGradeChange(val: string) {
  form.value.group = ''
  if (val.startsWith('p')) {
    groupOptions.value = [{ label: '小学组', value: 'primary' }]
    form.value.group = 'primary'
  } else if (val.startsWith('j')) {
    groupOptions.value = [{ label: '初中组', value: 'junior' }]
    form.value.group = 'junior'
  } else if (val.startsWith('s')) {
    groupOptions.value = [{ label: '高中组', value: 'senior' }]
    form.value.group = 'senior'
  }
}

const gradeLabel = computed(() => {
  const map: Record<string, string> = {
    p1: '小学1年级', p2: '小学2年级', p3: '小学3年级',
    p4: '小学4年级', p5: '小学5年级', p6: '小学6年级',
    j1: '初中1年级', j2: '初中2年级', j3: '初中3年级',
    s1: '高中1年级', s2: '高中2年级', s3: '高中3年级',
  }
  return map[form.value.grade] || ''
})

const groupLabel = computed(() => {
  const map: Record<string, string> = {
    primary: '小学组', junior: '初中组', senior: '高中组'
  }
  return map[form.value.group] || ''
})

// 确认信息列表
const confirmItems = computed(() => [
  { label: '姓名', value: form.value.name },
  { label: '学校', value: form.value.school },
  { label: '年级', value: gradeLabel.value },
  { label: '参赛组别', value: groupLabel.value },
  { label: '联系方式', value: form.value.phone },
  { label: '编程语言', value: form.value.language },
  { label: '紧急联系人', value: `${form.value.emergencyName} ${form.value.emergencyPhone}` },
])

// 手机号正则
const phoneReg = /^1[3-9]\d{9}$/

const rules: FormRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度2-20个字符', trigger: 'blur' },
  ],
  school: [
    { required: true, message: '请输入学校', trigger: 'blur' },
  ],
  grade: [
    { required: true, message: '请选择年级', trigger: 'change' },
  ],
  group: [
    { required: true, message: '请选择参赛组别', trigger: 'change' },
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: phoneReg, message: '请输入正确的11位手机号', trigger: 'blur' },
  ],
  language: [
    { required: true, message: '请选择编程语言', trigger: 'change' },
  ],
  emergencyName: [
    { required: true, message: '请输入紧急联系人姓名', trigger: 'blur' },
  ],
  emergencyPhone: [
    { required: true, message: '请输入紧急联系人电话', trigger: 'blur' },
    { pattern: phoneReg, message: '请输入正确的11位手机号', trigger: 'blur' },
  ],
}

const noticeTexts = [
  '报名信息须真实有效，虚假信息将导致取消参赛资格。',
  '报名成功后，参赛号将作为唯一身份标识，请妥善保管。',
  '报名截止前可申请退费，报名截止后不予退费。',
  '参赛选手需遵守考场纪律，违规者将取消成绩。',
  '比赛结果和证书将通过报名手机号通知。',
]

function handleNext() {
  formRef.value?.validate((valid) => {
    if (valid) {
      step.value = 2
      generateCaptcha()
    } else {
      ElMessage.warning('请完善表单信息')
    }
  })
}

function handleConfirm() {
  noticeVisible.value = true
}

function confirmSubmit() {
  // 验证码校验
  if (!isCaptchaValid.value) {
    ElMessage.error('验证码错误，请重新输入')
    generateCaptcha()
    return
  }
  noticeVisible.value = false
  // 模拟生成报名号
  const prefix = 'MG'
  const timestamp = Date.now().toString().slice(-8)
  const random = Math.floor(Math.random() * 9000 + 1000)
  registerNo.value = `${prefix}${timestamp}${random}`
  step.value = 3
  ElMessage.success('报名成功！')
}

function handlePrintTicket() {
  window.print()
}
</script>

<style scoped lang="scss">

.exam-register-page {
  background: $bg-page;
  min-height: 100vh;
  padding-bottom: 40px;
}

// ==================== 步骤指示器 (Stepper) ====================
.progress-bar {
  background: $bg-white;
  border-bottom: 1px solid #E5E6EB;
  padding: 32px 0;
}

.progress-inner {
  max-width: 720px;
  margin: 0 auto;
  padding: 0 24px;
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
}

.progress-step {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
  flex: 0 0 auto;
  position: relative;

  .step-num {
    width: 44px;
    height: 44px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 16px;
    font-weight: 700;
    background: #E5E6EB;
    color: $text-muted;
    transition: transform, box-shadow, background-color, border-color, color, opacity 0.4s cubic-bezier(0.4, 0, 0.2, 1);
    position: relative;
    z-index: 2;
  }

  .step-label {
    font-size: 14px;
    color: $text-muted;
    transition: transform, box-shadow, background-color, border-color, color, opacity 0.4s ease;
    font-weight: 500;
  }

  .step-desc {
    font-size: 12px;
    color: #C9CDD4;
    transition: transform, box-shadow, background-color, border-color, color, opacity 0.4s ease;
  }

  &.active .step-num {
    background: $primary-color;
    color: #fff;
    box-shadow: 0 4px 14px rgba(22, 93, 255, 0.35);
  }

  &.active .step-label {
    color: $primary-color;
    font-weight: 600;
  }

  &.active .step-desc {
    color: rgba(22, 93, 255, 0.6);
  }

  &.current .step-num {
    box-shadow: 0 0 0 4px rgba(22, 93, 255, 0.12);
    animation: stepPulse 2s ease-in-out infinite;
    will-change: transform, opacity;
  }
}

@keyframes stepPulse {
  0%, 100% { box-shadow: 0 0 0 4px rgba(22, 93, 255, 0.12); }
  50% { box-shadow: 0 0 0 8px rgba(22, 93, 255, 0.06); }
}

.progress-line {
  flex: 1;
  height: 2px;
  background: #E5E6EB;
  margin: 0 12px;
  margin-top: 21px;
  position: relative;
  overflow: hidden;

  .progress-line-fill {
    position: absolute;
    left: 0;
    top: 0;
    height: 100%;
    width: 0%;
    background: $primary-color;
    transition: width 0.5s cubic-bezier(0.4, 0, 0.2, 1);
  }

  &.active .progress-line-fill {
    width: 100%;
  }
}

// ==================== 内容区域 ====================
.register-container {
  max-width: 800px;
  margin: 0 auto;
  padding: 32px 24px 0;
}

.register-card {
  background: $bg-white;
  border-radius: 16px;
  padding: 40px 48px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
  border: 1px solid #E5E6EB;
}

.register-header {
  text-align: center;
  margin-bottom: 32px;
}

.register-title {
  font-size: 24px;
  font-weight: 700;
  color: $text-title;
  margin: 0 0 8px;
}

.register-subtitle {
  font-size: 14px;
  color: $text-muted;
  margin: 0;
}

.register-form {
  :deep(.el-form-item__label) {
    font-weight: 600;
    color: $text-title;
    padding-bottom: 6px;
  }
}

.w-full {
  width: 100%;
}

// ==================== 聚焦光效输入框 ====================
.glow-input {
  :deep(.el-input__wrapper) {
    border-radius: 10px;
    box-shadow: 0 0 0 1px #E5E6EB inset;
    transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1);
    padding: 4px 12px;

    &:hover {
      box-shadow: 0 0 0 1px rgba(22, 93, 255, 0.4) inset;
    }

    &.is-focus {
      box-shadow: 0 0 0 1px $primary-color inset, 0 0 0 4px rgba(22, 93, 255, 0.08);
    }
  }

  :deep(.el-input__inner) {
    height: 40px;
  }
}

.glow-select {
  :deep(.el-input__wrapper) {
    border-radius: 10px;
    box-shadow: 0 0 0 1px #E5E6EB inset;
    transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s cubic-bezier(0.4, 0, 0.2, 1);

    &:hover {
      box-shadow: 0 0 0 1px rgba(22, 93, 255, 0.4) inset;
    }

    &.is-focus {
      box-shadow: 0 0 0 1px $primary-color inset, 0 0 0 4px rgba(22, 93, 255, 0.08);
    }
  }
}

.glow-radio-group {
  :deep(.el-radio__input.is-checked + .el-radio__label) {
    color: $primary-color;
    font-weight: 600;
  }
  :deep(.el-radio__input.is-checked .el-radio__inner) {
    background: $primary-color;
    border-color: $primary-color;
  }
}

// 分割线
.section-divider {
  margin: 24px 0;

  :deep(.el-divider__text) {
    background: $bg-white;
    padding: 0 16px;
  }

  .divider-text {
    font-size: 14px;
    font-weight: 600;
    color: $text-title;
  }
}

// ==================== 按钮样式 ====================
.form-actions {
  display: flex;
  justify-content: center;
  margin-top: 36px;
}

.submit-btn {
  min-width: 180px;
  height: 48px;
  font-weight: 600;
  font-size: 15px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: none;

  .btn-text {
    position: relative;
    z-index: 2;
  }

  .btn-arrow {
    position: relative;
    z-index: 2;
    transition: transform 0.3s ease;
  }

  &:hover .btn-arrow {
    transform: translateX(4px);
  }
}

.gradient-btn {
  background: linear-gradient(135deg, $primary-color 0%, #4080FF 100%);
  position: relative;
  overflow: hidden;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s ease;

  &::before {
    content: '';
    position: absolute;
    inset: 0;
    background: linear-gradient(135deg, #4080FF 0%, $primary-color 100%);
    opacity: 0;
    transition: opacity 0.3s ease;
  }

  &:hover::before {
    opacity: 1;
  }

  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 24px rgba(22, 93, 255, 0.3);
  }

  &:active {
    transform: translateY(0);
  }

  &.is-disabled {
    background: #C9CDD4;
    cursor: not-allowed;
    &:hover {
      transform: none;
      box-shadow: none;
    }
    &::before {
      display: none;
    }
  }
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

// ==================== 确认信息 ====================
.confirm-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 32px;
}

.confirm-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 14px 18px;
  background: #f7f8fa;
  border-radius: 10px;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.2s ease;
  border: 1px solid transparent;

  &:hover {
    background: $bg-white;
    border-color: #E5E6EB;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
  }
}

.confirm-label {
  font-size: 14px;
  color: $text-muted;
}

.confirm-value {
  font-size: 14px;
  font-weight: 600;
  color: $text-title;
}

// 验证码区域
.captcha-area {
  margin-bottom: 28px;
  padding: 20px;
  background: linear-gradient(135deg, #f7f8fa 0%, #fff 100%);
  border-radius: 12px;
  border: 1px dashed #E5E6EB;
}

.captcha-row {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.captcha-label {
  font-size: 16px;
  font-weight: 700;
  color: $text-title;
  white-space: nowrap;
  letter-spacing: 1px;
}

.refresh-link {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
}

.confirm-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
}

// ==================== 成功页面 ====================
.success-card {
  text-align: center;
  padding: 56px 48px;
}

.success-animation {
  position: relative;
  width: 80px;
  height: 80px;
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.success-circle {
  width: 64px;
  height: 64px;
  background: linear-gradient(135deg, $success-color, #36D399);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  z-index: 2;
  box-shadow: 0 4px 16px rgba(54, 211, 153, 0.35);
  animation: successPop 0.5s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes successPop {
  0% { transform: scale(0); }
  100% { transform: scale(1); }
}

.success-ring {
  position: absolute;
  border-radius: 50%;
  border: 2px solid $success-color;
  opacity: 0;

  &.ring-1 {
    width: 80px;
    height: 80px;
    animation: ringExpand 1.5s ease-out 0.3s infinite;
    will-change: transform, opacity;
  }

  &.ring-2 {
    width: 96px;
    height: 96px;
    animation: ringExpand 1.5s ease-out 0.6s infinite;
    will-change: transform, opacity;
  }
}

@keyframes ringExpand {
  0% {
    transform: scale(0.8);
    opacity: 0.5;
  }
  100% {
    transform: scale(1.3);
    opacity: 0;
  }
}

.success-title {
  font-size: 26px;
  font-weight: 700;
  color: $text-title;
  margin: 0 0 8px;
}

.success-subtitle {
  font-size: 14px;
  color: $text-muted;
  margin: 0 0 36px;
}

.success-info {
  max-width: 420px;
  margin: 0 auto 24px;
  background: #f7f8fa;
  border-radius: 14px;
  padding: 24px 28px;
  text-align: left;
  border: 1px solid #E5E6EB;
}

.info-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px dashed #E5E6EB;
  &:last-child {
    border-bottom: none;
  }
}

.info-label {
  font-size: 14px;
  color: $text-muted;
}

.info-value {
  font-size: 14px;
  font-weight: 600;
  color: $text-title;
}

.info-highlight {
  font-size: 22px;
  color: $primary-color;
  letter-spacing: 2px;
  font-weight: 800;
  font-family: 'Courier New', monospace;
}

.success-tip {
  max-width: 420px;
  margin: 0 auto 28px;
  text-align: left;
}

.success-actions {
  display: flex;
  justify-content: center;
  gap: 16px;
  flex-wrap: wrap;
}

// ==================== 弹窗样式 ====================
.announcement-dialog {
  :deep(.el-dialog__header) {
    padding: 20px 24px;
    border-bottom: 1px solid #E5E6EB;
  }

  :deep(.el-dialog__title) {
    font-weight: 700;
    font-size: 16px;
  }
}

.announcement-content {
  max-height: 60vh;
  overflow-y: auto;
  padding-right: 8px;
}

.announcement-section {
  margin-bottom: 24px;
  &:last-child {
    margin-bottom: 0;
  }
}

.announcement-section-title {
  font-size: 16px;
  font-weight: 700;
  color: $text-title;
  margin: 0 0 12px;
  padding-left: 12px;
  border-left: 3px solid $primary-color;
  display: flex;
  align-items: center;
  gap: 8px;

  .el-icon {
    color: $primary-color;
  }
}

.announcement-text {
  font-size: 14px;
  color: $text-body;
  line-height: 1.8;
  margin: 0;
}

.announcement-list {
  margin: 0;
  padding-left: 20px;
  li {
    font-size: 14px;
    color: $text-body;
    line-height: 2;
  }
}

.notice-dialog {
  .notice-content {
    p {
      font-size: 14px;
      color: $text-body;
      line-height: 1.8;
      margin: 0 0 10px;
      &:last-child {
        margin-bottom: 0;
      }
    }
  }
}

// 准考证弹窗
.admission-ticket {
  border: 2px solid #1D2129;
  border-radius: 4px;
  padding: 24px;
  background: $bg-white;
}

.ticket-header {
  text-align: center;
  border-bottom: 2px solid #1D2129;
  padding-bottom: 16px;
  margin-bottom: 20px;
}

.ticket-title {
  font-size: 28px;
  font-weight: 700;
  color: #1D2129;
  letter-spacing: 12px;
  margin: 0 0 8px;
}

.ticket-exam-name {
  font-size: 14px;
  color: $text-muted;
  margin: 0;
}

.ticket-body {
  display: flex;
  gap: 24px;
  margin-bottom: 20px;
}

.ticket-photo-area {
  flex-shrink: 0;
}

.ticket-photo-placeholder {
  width: 100px;
  height: 130px;
  background: #E5E6EB;
  border: 1px dashed #C9CDD4;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  span {
    font-size: 12px;
    color: $text-muted;
  }
}

.ticket-info-grid {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.ticket-info-row {
  display: flex;
  align-items: center;
  border-bottom: 1px solid #F2F3F5;
  padding: 6px 0;
}

.ticket-label {
  font-size: 14px;
  color: $text-muted;
  width: 80px;
  flex-shrink: 0;
}

.ticket-value {
  font-size: 14px;
  font-weight: 600;
  color: $text-title;
}

.ticket-highlight {
  font-size: 16px;
  color: $primary-color;
  letter-spacing: 1px;
}

.ticket-exam-details {
  border-top: 1px dashed #C9CDD4;
  border-bottom: 1px dashed #C9CDD4;
  padding: 16px 0;
  margin-bottom: 16px;
}

.ticket-detail-row {
  display: flex;
  align-items: center;
  padding: 6px 0;
}

.ticket-notices {
  margin-top: 4px;
}

.ticket-notices-title {
  font-size: 14px;
  font-weight: 700;
  color: $text-title;
  margin: 0 0 8px;
}

.ticket-notices-list {
  margin: 0;
  padding-left: 20px;
  li {
    font-size: 13px;
    color: $text-body;
    line-height: 1.8;
  }
}

// 打印专用样式
@media print {
  body * {
    visibility: hidden;
  }
  #admissionTicket,
  #admissionTicket * {
    visibility: visible;
  }
  #admissionTicket {
    position: absolute;
    left: 0;
    top: 0;
    width: 100%;
    border: 2px solid #000 !important;
    padding: 20px !important;
    box-sizing: border-box;
  }
  .el-overlay,
  .el-dialog__headerbtn,
  .el-dialog__footer {
    display: none !important;
  }
  .el-dialog {
    position: static !important;
    margin: 0 !important;
    padding: 0 !important;
    box-shadow: none !important;
    border: none !important;
    max-width: 100% !important;
  }
  .el-dialog__body {
    padding: 0 !important;
    margin: 0 !important;
  }
}

// 响应式
@media (max-width: 768px) {
  .register-card {
    padding: 28px 20px;
  }

  .progress-inner {
    padding: 0 16px;
  }

  .progress-step {
    .step-desc {
      display: none;
    }
    .step-label {
      font-size: 12px;
    }
    .step-num {
      width: 36px;
      height: 36px;
      font-size: 14px;
    }
  }

  .register-container {
    padding: 20px 16px 0;
  }

  .confirm-actions,
  .success-actions {
    flex-direction: column;
    .el-button {
      width: 100%;
    }
  }

  .captcha-row {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }

  .success-info {
    padding: 16px 20px;
  }

  .info-highlight {
    font-size: 18px;
  }
}
</style>
