<template>
  <!-- 营销管理后台：会员套餐 + 优惠券 -->
  <div class="marketing-manage-page">
    <div class="page-header">
      <h2>营销管理</h2>
    </div>

    <el-tabs v-model="activeTab">
      <!-- ==================== 会员套餐 ==================== -->
      <el-tab-pane label="会员套餐" name="membership">
        <div class="toolbar">
          <div class="search-bar">
            <el-input
              v-model="planKeyword"
              placeholder="搜索套餐名称"
              clearable
              style="width: 240px"
              @keyup.enter="handlePlanSearch"
              @clear="handlePlanSearch"
            />
            <el-select v-model="planStatusFilter" placeholder="状态筛选" clearable style="width: 130px" @change="handlePlanSearch">
              <el-option label="已上架" value="enabled" />
              <el-option label="已下架" value="disabled" />
            </el-select>
            <el-button type="primary" @click="handlePlanSearch">搜索</el-button>
            <el-button @click="resetPlanSearch">重置</el-button>
          </div>
          <el-button type="primary" @click="handlePlanCreate">新建套餐</el-button>
        </div>

        <el-table :data="pagedPlans" stripe v-loading="planLoading" row-key="id" empty-text="暂无数据">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="name" label="套餐名称" min-width="130" show-overflow-tooltip />
          <el-table-column prop="duration" label="天数" width="80" align="center" />
          <el-table-column label="价格" width="130">
            <template #default="{ row }">
              <div class="price-cell">
                <span class="price-now">¥{{ formatYuan(row.price) }}</span>
                <span v-if="row.originalPrice > row.price" class="price-orig">¥{{ formatYuan(row.originalPrice) }}</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column label="套餐特性" min-width="180" show-overflow-tooltip>
            <template #default="{ row }">
              <span>{{ (row.features || []).join('、') || '--' }}</span>
            </template>
          </el-table-column>
          <el-table-column label="推荐" width="80" align="center">
            <template #default="{ row }">
              <el-tag v-if="row.recommended" type="danger" size="small">推荐</el-tag>
              <span v-else class="text-muted">-</span>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-switch
                v-model="row.status"
                active-value="enabled"
                inactive-value="disabled"
                inline-prompt
                active-text="上架"
                inactive-text="下架"
                @change="handlePlanStatusChange(row as PlanItem)"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button size="small" text type="primary" @click="handlePlanEdit(row as PlanItem)">编辑</el-button>
              <el-button size="small" text type="danger" @click="handlePlanDelete(row as PlanItem)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrap">
          <Pagination v-model:page="planPage" v-model:pageSize="planPageSize" :total="planTotal" />
        </div>
      </el-tab-pane>

      <!-- ==================== 优惠券 ==================== -->
      <el-tab-pane label="优惠券" name="coupon">
        <div class="toolbar">
          <div class="search-bar">
            <el-input
              v-model="couponKeyword"
              placeholder="搜索优惠券名称"
              clearable
              style="width: 220px"
              @keyup.enter="handleCouponSearch"
              @clear="handleCouponSearch"
            />
            <el-select v-model="couponTypeFilter" placeholder="类型" clearable style="width: 120px" @change="handleCouponSearch">
              <el-option label="满减" value="fixed" />
              <el-option label="折扣" value="percent" />
            </el-select>
            <el-select v-model="couponStatusFilter" placeholder="状态" clearable style="width: 120px" @change="handleCouponSearch">
              <el-option label="启用" value="enabled" />
              <el-option label="停用" value="disabled" />
            </el-select>
            <el-button type="primary" @click="handleCouponSearch">搜索</el-button>
            <el-button @click="resetCouponSearch">重置</el-button>
          </div>
          <el-button type="primary" @click="handleCouponCreate">新建优惠券</el-button>
        </div>

        <el-table :data="pagedCoupons" stripe v-loading="couponLoading" row-key="id" empty-text="暂无数据">
          <el-table-column prop="id" label="ID" width="70" />
          <el-table-column prop="name" label="名称" min-width="140" show-overflow-tooltip />
          <el-table-column label="类型" width="90" align="center">
            <template #default="{ row }">
              <el-tag size="small" :type="row.type === 'fixed' ? 'primary' : 'success'">
                {{ row.type === 'fixed' ? '满减' : '折扣' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="面值" width="150">
            <template #default="{ row }">
              <div class="coupon-value">
                <span class="value-main">{{ formatCouponValue(row as CouponItem) }}</span>
                <span v-if="row.minAmount > 0" class="value-sub">满¥{{ formatYuan(row.minAmount) }}可用</span>
                <span v-else class="value-sub">无门槛</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="totalCount" label="发放量" width="90" align="center" />
          <el-table-column label="已使用" width="110" align="center">
            <template #default="{ row }">
              <span>{{ row.usedCount }}</span>
              <span class="text-muted"> / {{ row.totalCount }}</span>
            </template>
          </el-table-column>
          <el-table-column label="有效期" width="160">
            <template #default="{ row }">
              <span :class="{ 'text-muted': isExpired(row as CouponItem) }">{{ row.expireTime || '--' }}</span>
              <el-tag v-if="isExpired(row as CouponItem)" type="info" size="small" style="margin-left: 4px">已过期</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="100" align="center">
            <template #default="{ row }">
              <el-switch
                v-model="row.status"
                active-value="enabled"
                inactive-value="disabled"
                inline-prompt
                active-text="启用"
                inactive-text="停用"
                @change="handleCouponStatusChange(row as CouponItem)"
              />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button size="small" text type="primary" @click="handleCouponEdit(row as CouponItem)">编辑</el-button>
              <el-button size="small" text type="danger" @click="handleCouponDelete(row as CouponItem)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrap">
          <Pagination v-model:page="couponPage" v-model:pageSize="couponPageSize" :total="couponTotal" />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- ==================== 套餐 新增/编辑 弹窗 ==================== -->
    <el-dialog
      v-model="planDialogVisible"
      :title="planIsEdit ? '编辑套餐' : '新建套餐'"
      width="560px"
      destroy-on-close
    >
      <el-form ref="planFormRef" :model="planForm" :rules="planFormRules" label-width="100px">
        <el-form-item label="套餐名称" prop="name">
          <el-input v-model="planForm.name" placeholder="请输入套餐名称，如 月度会员" maxlength="30" show-word-limit />
        </el-form-item>
        <el-form-item label="有效天数" prop="duration">
          <el-input-number v-model="planForm.duration" :min="1" :max="3650" style="width: 200px" />
          <span class="form-unit">天</span>
        </el-form-item>
        <el-form-item label="价格" prop="price">
          <el-input-number v-model="planForm.price" :min="0" :precision="2" :step="1" style="width: 200px" />
          <span class="form-unit">元（以分为单位存储）</span>
        </el-form-item>
        <el-form-item label="原价" prop="originalPrice">
          <el-input-number v-model="planForm.originalPrice" :min="0" :precision="2" :step="1" style="width: 200px" />
          <span class="form-unit">元（划线价，0 表示不展示）</span>
        </el-form-item>
        <el-form-item label="套餐特性" prop="features">
          <el-input
            v-model="planForm.features"
            type="textarea"
            :rows="3"
            placeholder="多个特性用英文逗号分隔，如：全部课程, 课后作业, 社群答疑"
          />
        </el-form-item>
        <el-form-item label="是否推荐" prop="recommended">
          <el-switch v-model="planForm.recommended" />
          <span class="form-unit">推荐套餐将在前台高亮展示</span>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="planForm.status">
            <el-radio label="enabled">上架</el-radio>
            <el-radio label="disabled">下架</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="planDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="planSubmitting" @click="submitPlanForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- ==================== 优惠券 新增/编辑 弹窗 ==================== -->
    <el-dialog
      v-model="couponDialogVisible"
      :title="couponIsEdit ? '编辑优惠券' : '新建优惠券'"
      width="560px"
      destroy-on-close
    >
      <el-form ref="couponFormRef" :model="couponForm" :rules="couponFormRules" label-width="100px">
        <el-form-item label="优惠券名称" prop="name">
          <el-input v-model="couponForm.name" placeholder="请输入优惠券名称，如 新人优惠券" maxlength="30" show-word-limit />
        </el-form-item>
        <el-form-item label="类型" prop="type">
          <el-radio-group v-model="couponForm.type" @change="handleCouponTypeChange">
            <el-radio label="fixed">满减</el-radio>
            <el-radio label="percent">折扣</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="面值" prop="value">
          <el-input-number
            v-model="couponForm.value"
            :min="0"
            :precision="couponForm.type === 'fixed' ? 2 : 1"
            :step="couponForm.type === 'fixed' ? 1 : 0.5"
            :max="couponForm.type === 'fixed' ? 999999 : 9.9"
            style="width: 200px"
          />
          <span class="form-unit">{{ couponForm.type === 'fixed' ? '元（满减金额）' : '折（如 8.5 表示 8.5 折）' }}</span>
        </el-form-item>
        <el-form-item label="使用门槛" prop="minAmount">
          <el-input-number v-model="couponForm.minAmount" :min="0" :precision="2" :step="1" style="width: 200px" />
          <span class="form-unit">元（满此金额可用，0 为无门槛）</span>
        </el-form-item>
        <el-form-item label="发放总量" prop="totalCount">
          <el-input-number v-model="couponForm.totalCount" :min="0" :max="10000000" style="width: 200px" />
          <span class="form-unit">张</span>
        </el-form-item>
        <el-form-item label="有效期至" prop="expireTime">
          <el-date-picker
            v-model="couponForm.expireTime"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择过期日期"
            style="width: 200px"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="couponForm.status">
            <el-radio label="enabled">启用</el-radio>
            <el-radio label="disabled">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="couponDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="couponSubmitting" @click="submitCouponForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * MarketingManagePage - 营销管理后台
 * 会员套餐管理、优惠券管理（完整 CRUD）
 * 调用 /admin/marketing/plans 与 /admin/marketing/coupons，接口失败时清空列表并提示
 */
import { ref, computed, onMounted, reactive } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { planApi, couponApi } from '@/api/admin'

// ==================== 类型定义 ====================
type PlanStatus = 'enabled' | 'disabled'

interface PlanItem {
  id: number
  name: string
  duration: number
  price: number            // 分
  originalPrice: number    // 分
  features: string[]
  recommended: boolean
  status: PlanStatus
  createdAt?: string
}

type CouponType = 'fixed' | 'percent'

interface CouponItem {
  id: number
  name: string
  type: CouponType
  value: number            // fixed: 分；percent: 折（如 8.5）
  minAmount: number        // 分
  totalCount: number
  usedCount: number
  expireTime: string
  status: PlanStatus
  createdAt?: string
}

// ==================== Tab ====================
const activeTab = ref<'membership' | 'coupon'>('membership')

// ==================== 金额格式化 ====================
/** 分 -> 元（整数十进制不显示小数） */
function formatYuan(cents: number): string {
  if (cents === undefined || cents === null || isNaN(cents)) return '0'
  const yuan = cents / 100
  return Number.isInteger(yuan) ? String(yuan) : yuan.toFixed(2)
}

/** 优惠券面值展示 */
function formatCouponValue(coupon: CouponItem): string {
  if (coupon.type === 'fixed') return `¥${formatYuan(coupon.value)}`
  return `${coupon.value}折`
}

/** 优惠券是否已过期 */
function isExpired(coupon: CouponItem): boolean {
  if (!coupon.expireTime) return false
  return new Date(coupon.expireTime).getTime() < Date.now()
}

// ==================== 会员套餐：数据 ====================
const planLoading = ref(false)
const planKeyword = ref('')
const planStatusFilter = ref('')
const planPage = ref(1)
const planPageSize = ref(10)

const planList = ref<PlanItem[]>([])

const filteredPlans = computed(() => {
  const kw = planKeyword.value.trim().toLowerCase()
  return planList.value.filter((p) => {
    const matchKw = !kw || p.name.toLowerCase().includes(kw)
    const matchStatus = !planStatusFilter.value || p.status === planStatusFilter.value
    return matchKw && matchStatus
  })
})

const planTotal = computed(() => filteredPlans.value.length)

const pagedPlans = computed(() => {
  const start = (planPage.value - 1) * planPageSize.value
  return filteredPlans.value.slice(start, start + planPageSize.value)
})

// ==================== 会员套餐：加载 ====================
async function loadPlans() {
  planLoading.value = true
  try {
    const res = await planApi.list({ pageNum: 1, pageSize: 9999 })
    const data: any = res?.data
    const list = Array.isArray(data) ? data : (data?.list || data?.records || [])
    if (res?.code === 200 && Array.isArray(list)) {
      planList.value = list
    } else {
      planList.value = []
      ElMessage.error('数据加载失败，请检查网络或稍后重试')
    }
  } catch {
    planList.value = []
    ElMessage.error('数据加载失败，请检查网络或稍后重试')
  } finally {
    planLoading.value = false
  }
}

// ==================== 会员套餐：搜索 ====================
function handlePlanSearch() {
  planPage.value = 1
}

function resetPlanSearch() {
  planKeyword.value = ''
  planStatusFilter.value = ''
  planPage.value = 1
}

// ==================== 会员套餐：状态切换 ====================
async function handlePlanStatusChange(row: PlanItem) {
  const prevStatus: PlanStatus = row.status === 'enabled' ? 'disabled' : 'enabled'
  try {
    await planApi.update(row.id, { status: row.status })
    ElMessage.success(row.status === 'enabled' ? '已上架' : '已下架')
  } catch {
    row.status = prevStatus
    ElMessage.error('状态切换失败')
  }
}

// ==================== 会员套餐：弹窗表单 ====================
const planDialogVisible = ref(false)
const planIsEdit = ref(false)
const planSubmitting = ref(false)
const planFormRef = ref<FormInstance>()
const planEditId = ref<number | null>(null)

const planForm = reactive({
  name: '',
  duration: 30,
  price: 0,            // 元（表单内以元为单位输入）
  originalPrice: 0,    // 元
  features: '',        // 逗号分隔字符串
  recommended: false,
  status: 'enabled' as PlanStatus,
})

const planFormRules: FormRules = {
  name: [{ required: true, message: '请输入套餐名称', trigger: 'blur' }],
  duration: [{ required: true, message: '请输入有效天数', trigger: 'blur' }],
  price: [{ required: true, message: '请输入价格', trigger: 'blur' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

function resetPlanForm() {
  planForm.name = ''
  planForm.duration = 30
  planForm.price = 0
  planForm.originalPrice = 0
  planForm.features = ''
  planForm.recommended = false
  planForm.status = 'enabled'
}

function handlePlanCreate() {
  planIsEdit.value = false
  planEditId.value = null
  resetPlanForm()
  planDialogVisible.value = true
}

function handlePlanEdit(row: PlanItem) {
  planIsEdit.value = true
  planEditId.value = row.id
  planForm.name = row.name
  planForm.duration = row.duration
  planForm.price = row.price / 100          // 分 -> 元
  planForm.originalPrice = row.originalPrice / 100
  planForm.features = (row.features || []).join(', ')
  planForm.recommended = !!row.recommended
  planForm.status = row.status
  planDialogVisible.value = true
}

function buildPlanPayload() {
  return {
    name: planForm.name,
    duration: planForm.duration,
    price: Math.round(planForm.price * 100),            // 元 -> 分
    originalPrice: Math.round(planForm.originalPrice * 100),
    features: planForm.features.split(',').map((s) => s.trim()).filter(Boolean),
    recommended: planForm.recommended,
    status: planForm.status,
  }
}

async function submitPlanForm() {
  if (!planFormRef.value) return
  await planFormRef.value.validate(async (valid) => {
    if (!valid) return
    planSubmitting.value = true
    try {
      const payload = buildPlanPayload()

      if (planIsEdit.value && planEditId.value !== null) {
        await planApi.update(planEditId.value, payload)
        const idx = planList.value.findIndex((p) => p.id === planEditId.value)
        if (idx !== -1) {
          planList.value[idx] = { ...planList.value[idx], ...payload }
        }
        ElMessage.success('编辑成功')
      } else {
        const res = await planApi.create(payload)
        const data: any = res?.data ?? {}
        planList.value.unshift({
          id: data.id,
          features: [],
          recommended: false,
          status: 'enabled',
          ...data,
        } as PlanItem)
        ElMessage.success('添加成功')
      }
      planDialogVisible.value = false
      planPage.value = 1
    } catch {
      ElMessage.error('操作失败')
    } finally {
      planSubmitting.value = false
    }
  })
}

function handlePlanDelete(row: PlanItem) {
  ElMessageBox.confirm(`确定删除套餐 "${row.name}" 吗？`, '警告', { type: 'warning' })
    .then(async () => {
      try {
        await planApi.remove(row.id)
        const idx = planList.value.findIndex((p) => p.id === row.id)
        if (idx !== -1) planList.value.splice(idx, 1)
        // 当前页删空时回到上一页
        const maxPage = Math.max(1, Math.ceil(filteredPlans.value.length / planPageSize.value))
        if (planPage.value > maxPage) planPage.value = maxPage
        ElMessage.success('删除成功')
      } catch {
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

// ==================== 优惠券：数据 ====================
const couponLoading = ref(false)
const couponKeyword = ref('')
const couponTypeFilter = ref('')
const couponStatusFilter = ref('')
const couponPage = ref(1)
const couponPageSize = ref(10)

const couponList = ref<CouponItem[]>([])

const filteredCoupons = computed(() => {
  const kw = couponKeyword.value.trim().toLowerCase()
  return couponList.value.filter((c) => {
    const matchKw = !kw || c.name.toLowerCase().includes(kw)
    const matchType = !couponTypeFilter.value || c.type === couponTypeFilter.value
    const matchStatus = !couponStatusFilter.value || c.status === couponStatusFilter.value
    return matchKw && matchType && matchStatus
  })
})

const couponTotal = computed(() => filteredCoupons.value.length)

const pagedCoupons = computed(() => {
  const start = (couponPage.value - 1) * couponPageSize.value
  return filteredCoupons.value.slice(start, start + couponPageSize.value)
})

// ==================== 优惠券：加载 ====================
async function loadCoupons() {
  couponLoading.value = true
  try {
    const res = await couponApi.list({ pageNum: 1, pageSize: 9999 })
    const data: any = res?.data
    const list = Array.isArray(data) ? data : (data?.list || data?.records || [])
    if (res?.code === 200 && Array.isArray(list)) {
      couponList.value = list
    } else {
      couponList.value = []
      ElMessage.error('数据加载失败，请检查网络或稍后重试')
    }
  } catch {
    couponList.value = []
    ElMessage.error('数据加载失败，请检查网络或稍后重试')
  } finally {
    couponLoading.value = false
  }
}

// ==================== 优惠券：搜索 ====================
function handleCouponSearch() {
  couponPage.value = 1
}

function resetCouponSearch() {
  couponKeyword.value = ''
  couponTypeFilter.value = ''
  couponStatusFilter.value = ''
  couponPage.value = 1
}

// ==================== 优惠券：状态切换 ====================
async function handleCouponStatusChange(row: CouponItem) {
  const prevStatus: PlanStatus = row.status === 'enabled' ? 'disabled' : 'enabled'
  try {
    await couponApi.update(row.id, { status: row.status })
    ElMessage.success(row.status === 'enabled' ? '已启用' : '已停用')
  } catch {
    row.status = prevStatus
    ElMessage.error('状态切换失败')
  }
}

// ==================== 优惠券：弹窗表单 ====================
const couponDialogVisible = ref(false)
const couponIsEdit = ref(false)
const couponSubmitting = ref(false)
const couponFormRef = ref<FormInstance>()
const couponEditId = ref<number | null>(null)

const couponForm = reactive({
  name: '',
  type: 'fixed' as CouponType,
  value: 0,             // fixed: 元；percent: 折
  minAmount: 0,         // 元
  totalCount: 100,
  expireTime: '',
  status: 'enabled' as PlanStatus,
})

const couponFormRules: FormRules = {
  name: [{ required: true, message: '请输入优惠券名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择优惠券类型', trigger: 'change' }],
  value: [{ required: true, message: '请输入面值', trigger: 'blur' }],
  totalCount: [{ required: true, message: '请输入发放总量', trigger: 'blur' }],
  expireTime: [{ required: true, message: '请选择有效期', trigger: 'change' }],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }],
}

function resetCouponForm() {
  couponForm.name = ''
  couponForm.type = 'fixed'
  couponForm.value = 0
  couponForm.minAmount = 0
  couponForm.totalCount = 100
  couponForm.expireTime = ''
  couponForm.status = 'enabled'
}

function handleCouponCreate() {
  couponIsEdit.value = false
  couponEditId.value = null
  resetCouponForm()
  couponDialogVisible.value = true
}

function handleCouponEdit(row: CouponItem) {
  couponIsEdit.value = true
  couponEditId.value = row.id
  couponForm.name = row.name
  couponForm.type = row.type
  couponForm.value = row.type === 'fixed' ? row.value / 100 : row.value   // fixed: 分 -> 元；percent: 折直接使用
  couponForm.minAmount = row.minAmount / 100                                // 分 -> 元
  couponForm.totalCount = row.totalCount
  couponForm.expireTime = row.expireTime || ''
  couponForm.status = row.status
  couponDialogVisible.value = true
}

function handleCouponTypeChange() {
  // 切换类型时重置面值为合理默认值
  couponForm.value = couponForm.type === 'fixed' ? 0 : 8.5
}

function buildCouponPayload() {
  const value = couponForm.type === 'fixed' ? Math.round(couponForm.value * 100) : couponForm.value
  return {
    name: couponForm.name,
    type: couponForm.type,
    value,
    minAmount: Math.round(couponForm.minAmount * 100),   // 元 -> 分
    totalCount: couponForm.totalCount,
    expireTime: couponForm.expireTime,
    status: couponForm.status,
  }
}

async function submitCouponForm() {
  if (!couponFormRef.value) return
  await couponFormRef.value.validate(async (valid) => {
    if (!valid) return
    couponSubmitting.value = true
    try {
      const payload = buildCouponPayload()

      if (couponIsEdit.value && couponEditId.value !== null) {
        await couponApi.update(couponEditId.value, payload)
        const idx = couponList.value.findIndex((c) => c.id === couponEditId.value)
        if (idx !== -1) {
          couponList.value[idx] = { ...couponList.value[idx], ...payload }
        }
        ElMessage.success('编辑成功')
      } else {
        const res = await couponApi.create(payload)
        const data: any = res?.data ?? {}
        couponList.value.unshift({
          id: data.id,
          usedCount: 0,
          status: 'enabled',
          ...data,
        } as CouponItem)
        ElMessage.success('添加成功')
      }
      couponDialogVisible.value = false
      couponPage.value = 1
    } catch {
      ElMessage.error('操作失败')
    } finally {
      couponSubmitting.value = false
    }
  })
}

function handleCouponDelete(row: CouponItem) {
  ElMessageBox.confirm(`确定删除优惠券 "${row.name}" 吗？`, '警告', { type: 'warning' })
    .then(async () => {
      try {
        await couponApi.remove(row.id)
        const idx = couponList.value.findIndex((c) => c.id === row.id)
        if (idx !== -1) couponList.value.splice(idx, 1)
        // 当前页删空时回到上一页
        const maxPage = Math.max(1, Math.ceil(filteredCoupons.value.length / couponPageSize.value))
        if (couponPage.value > maxPage) couponPage.value = maxPage
        ElMessage.success('删除成功')
      } catch {
        ElMessage.error('删除失败')
      }
    })
    .catch(() => {})
}

// ==================== 初始化 ====================
onMounted(() => {
  loadPlans()
  loadCoupons()
})
</script>

<style scoped lang="scss">
.marketing-manage-page {
  padding: 20px;
}

.page-header {
  margin-bottom: 16px;

  h2 {
    font-size: 20px;
    color: #1D2129;
  }
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  flex-wrap: wrap;
  gap: 12px;
}

.search-bar {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  align-items: center;
}

.pagination-wrap {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.price-cell {
  display: flex;
  flex-direction: column;
  line-height: 1.4;

  .price-now {
    color: #165dff;
    font-weight: 600;
  }

  .price-orig {
    color: #86909C;
    text-decoration: line-through;
    font-size: 12px;
  }
}

.coupon-value {
  display: flex;
  flex-direction: column;
  line-height: 1.4;

  .value-main {
    font-weight: 600;
    color: #165dff;
  }

  .value-sub {
    font-size: 12px;
    color: #86909C;
  }
}

.form-unit {
  margin-left: 8px;
  color: #86909C;
  font-size: 12px;
}

.text-muted {
  color: #86909C;
}
</style>
