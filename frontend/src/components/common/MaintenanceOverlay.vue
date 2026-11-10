<template>
  <!-- 维护模式全屏遮罩：由后台 system config 的 maintenance_mode 开关控制 -->
  <div v-if="siteStore.config.maintenanceMode" class="maintenance-overlay">
    <div class="maintenance-card">
      <el-icon class="maintenance-icon"><Tools /></el-icon>
      <h2 class="maintenance-title">系统维护中</h2>
      <p class="maintenance-desc">
        平台正在进行升级维护，暂时无法访问。给您带来不便，敬请谅解。
      </p>
      <el-button type="primary" round @click="goAdmin">进入后台管理</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * MaintenanceOverlay - 站点维护模式遮罩
 * 后台开启 maintenance_mode 后对前台访客生效；管理员仍可通过按钮进入后台。
 */
import { Tools } from '@element-plus/icons-vue'
import { useRouter } from 'vue-router'
import { useSiteStore } from '@/stores/site'

const siteStore = useSiteStore()
const router = useRouter()

function goAdmin() {
  router.push('/admin/login')
}
</script>

<style scoped lang="scss">
.maintenance-overlay {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #1d2129 0%, #2a2f3a 100%);
  color: #fff;

  .maintenance-card {
    text-align: center;
    padding: 40px;
    max-width: 460px;
  }

  .maintenance-icon {
    font-size: 56px;
    color: #ff7d00;
    margin-bottom: 16px;
  }

  .maintenance-title {
    font-size: 26px;
    margin: 0 0 12px;
  }

  .maintenance-desc {
    font-size: 14px;
    line-height: 1.8;
    color: rgba(255, 255, 255, 0.7);
    margin: 0 0 28px;
  }
}
</style>
