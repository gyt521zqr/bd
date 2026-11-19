<template>
  <!-- 订单列表 -->
  <div class="order-list-page">
    <div class="page-header">
      <div class="header-bg"></div>
      <div class="header-content">
        <h2 class="page-title">
          <el-icon><DocumentChecked /></el-icon>
          我的订单
        </h2>
        <p class="page-subtitle">查看和管理您的所有订单</p>
      </div>
    </div>

    <div class="page-body">
      <div class="tabs-wrap">
        <el-tabs v-model="activeTab" @tab-change="handleTabChange" type="border-card">
          <el-tab-pane name="all">
            <template #label>
              <span class="tab-label">
                全部
                <el-tag size="small" type="info" effect="light" class="tab-count">{{ orders.length }}</el-tag>
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane name="pending">
            <template #label>
              <span class="tab-label">
                待支付
                <el-tag size="small" type="warning" effect="light" class="tab-count" v-if="pendingCount > 0">{{ pendingCount }}</el-tag>
              </span>
            </template>
          </el-tab-pane>
          <el-tab-pane name="paid">
            <template #label>
              <span class="tab-label">已支付</span>
            </template>
          </el-tab-pane>
          <el-tab-pane name="cancelled">
            <template #label>
              <span class="tab-label">已取消</span>
            </template>
          </el-tab-pane>
        </el-tabs>
      </div>

      <div v-loading="loading" class="order-list">
        <OrderDetail
          v-for="(order, index) in filteredOrders"
          :key="order.id"
          :order="order"
          :style="{ animationDelay: `${index * 0.08}s` }"
          @pay="handlePay"
          @cancel="handleCancel"
        />
      </div>

      <el-empty
        v-if="!loading && filteredOrders.length === 0"
        description="暂无订单"
        class="empty-state"
      >
        <template #image>
          <div class="empty-icon">
            <el-icon :size="64"><DocumentDelete /></el-icon>
          </div>
        </template>
      </el-empty>

      <div class="pagination-wrap" v-if="total > pageSize">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="prev, pager, next"
          background
          @current-change="fetchOrders"
        />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * OrderListPage - 订单列表
 * 按状态筛选订单，数据来自真实接口，失败或为空时展示空态
 */
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { DocumentChecked, DocumentDelete } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import OrderDetail from '@/components/payment/OrderDetail.vue'
import { getOrderList, cancelOrder } from '@/api/payment'

const router = useRouter()
const activeTab = ref('all')
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 订单列表：初始为空，由真实接口填充
const orders = ref<any[]>([])

const filteredOrders = computed(() => {
  if (activeTab.value === 'all') return orders.value
  return orders.value.filter(o => o.status === activeTab.value)
})

const pendingCount = computed(() => {
  return orders.value.filter(o => o.status === 'pending').length
})

/** 从API加载订单列表 */
async function fetchOrders() {
  loading.value = true
  try {
    const res = await getOrderList({
      pageNum: currentPage.value,
      pageSize: pageSize.value,
      status: activeTab.value === 'all' ? undefined : activeTab.value,
    })
    if (res.data) {
      // API成功：使用返回列表（即使为空也展示空状态）
      orders.value = (res.data.list || []) as any
      total.value = res.data.total || 0
    }
  } catch {
    // 接口失败：保持空列表，total 置 0，由模板显示「暂无订单」
    orders.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

function handleTabChange() {
  // 切换状态标签时回到第一页并重新加载
  currentPage.value = 1
  fetchOrders()
}

function handlePay(order: any) { router.push(`/pay/${order.id}`) }

async function handleCancel(order: any) {
  try {
    await cancelOrder(order.id)
    order.status = 'cancelled'
    ElMessage.success('订单已取消')
  } catch {
    ElMessage.error('取消订单失败，请稍后重试')
  }
}

onMounted(() => {
  fetchOrders()
})
</script>

<style scoped lang="scss">
.order-list-page {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 40px;
}

// ==================== 头部 ====================
.page-header {
  position: relative;
  padding: 32px 0 48px;
  overflow: hidden;
}

.header-bg {
  position: absolute;
  inset: 0;
  background: linear-gradient(135deg, #165DFF 0%, #4080FF 50%, #7B61FF 100%);

  &::after {
    content: '';
    position: absolute;
    bottom: 0;
    left: 0;
    right: 0;
    height: 60px;
    background: linear-gradient(to top, #f7f8fa, transparent);
  }
}

.header-content {
  position: relative;
  z-index: 1;
  max-width: 900px;
  margin: 0 auto;
  padding: 0 20px;
}

.page-title {
  font-size: 26px;
  font-weight: 800;
  color: #fff;
  margin: 0 0 6px;
  display: flex;
  align-items: center;
  gap: 10px;

  .el-icon {
    font-size: 28px;
  }
}

.page-subtitle {
  font-size: 14px;
  color: rgba(255, 255, 255, 0.7);
  margin: 0;
}

// ==================== 内容区域 ====================
.page-body {
  max-width: 900px;
  margin: -24px auto 0;
  padding: 0 20px;
  position: relative;
  z-index: 2;
}

.tabs-wrap {
  :deep(.el-tabs__header) {
    margin-bottom: 0;
    border-radius: 12px 12px 0 0;
    overflow: hidden;
  }

  :deep(.el-tabs__content) {
    display: none;
  }
}

.tab-label {
  display: flex;
  align-items: center;
  gap: 6px;

  .tab-count {
    font-size: 11px;
    height: 18px;
    line-height: 16px;
    padding: 0 6px;
  }
}

.order-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 16px;
}

.empty-state {
  margin-top: 60px;

  :deep(.el-empty__description) {
    font-size: 15px;
    color: #86909C;
  }
}

.empty-icon {
  color: #C9CDD4;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

// ==================== 响应式 ====================
@media (max-width: 576px) {
  .page-header {
    padding: 20px 0 36px;
  }

  .page-title {
    font-size: 20px;

    .el-icon {
      font-size: 24px;
    }
  }

  .page-body {
    padding: 0 16px;
  }
}
</style>
