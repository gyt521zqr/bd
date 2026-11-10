<template>
  <!-- 云存储配置管理 -->
  <div class="storage-config-page">
    <div class="page-header">
      <h2>云存储配置</h2>
      <p class="page-desc">配置阿里云OSS和VOD参数，管理CDN加速策略</p>
    </div>

    <el-alert
      title="当前配置仅保存在本机浏览器，不会同步到服务端"
      description="「测试连接」暂未接入后端服务，操作未生效。"
      type="warning"
      :closable="false"
      show-icon
      style="margin-bottom: 16px"
    />

    <el-tabs v-model="activeTab" type="border-card">
      <!-- OSS配置 -->
      <el-tab-pane label="OSS对象存储" name="oss">
        <el-form :model="ossConfig" label-width="160px" class="config-form">
          <el-alert
            title="OSS配置说明"
            type="info"
            :closable="false"
            class="config-alert"
          >
            <p>配置阿里云OSS Bucket信息，用于存储图片、文档等静态资源。</p>
            <p>正式上线前请替换为真实配置，当前为演示配置。</p>
          </el-alert>

          <el-form-item label="Bucket域名">
            <el-input
              v-model="ossConfig.domain"
              placeholder="如 https://your-bucket.oss-cn-beijing.aliyuncs.com"
            />
          </el-form-item>

          <el-form-item label="CDN加速域名">
            <el-input
              v-model="ossConfig.cdnDomain"
              placeholder="如 https://cdn.yourdomain.com（可选）"
            />
            <div class="form-tip">配置后可大幅提升图片加载速度，需先在阿里云CDN控制台添加域名</div>
          </el-form-item>

          <el-form-item label="启用HTTPS">
            <el-switch v-model="ossConfig.secure" />
          </el-form-item>

          <el-form-item label="图片压缩预设">
            <el-table :data="imagePresetList" border size="small" style="width: 100%" empty-text="暂无数据">
              <el-table-column prop="name" label="预设名称" width="120" />
              <el-table-column prop="width" label="宽度" width="80" />
              <el-table-column prop="height" label="高度" width="80" />
              <el-table-column prop="quality" label="质量" width="80" />
              <el-table-column prop="desc" label="适用场景" />
            </el-table>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="saveOSSConfig">保存OSS配置</el-button>
            <el-button title="暂未接入后端服务" @click="testOSSConnection">测试连接</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- VOD配置 -->
      <el-tab-pane label="VOD视频点播" name="vod">
        <el-form :model="vodConfig" label-width="160px" class="config-form">
          <el-alert
            title="VOD配置说明"
            type="info"
            :closable="false"
            class="config-alert"
          >
            <p>配置阿里云VOD视频点播服务，用于课程视频存储和播放。</p>
            <p>支持多清晰度转码、CDN加速、防盗链等高级功能。</p>
          </el-alert>

          <el-form-item label="播放域名">
            <el-input
              v-model="vodConfig.playDomain"
              placeholder="如 https://vod.yourdomain.com"
            />
          </el-form-item>

          <el-form-item label="CDN加速域名">
            <el-input
              v-model="vodConfig.cdnDomain"
              placeholder="如 https://vod-cdn.yourdomain.com（可选）"
            />
          </el-form-item>

          <el-form-item label="视频清晰度预设">
            <el-table :data="videoPresetList" border size="small" style="width: 100%" empty-text="暂无数据">
              <el-table-column prop="label" label="清晰度" width="100" />
              <el-table-column prop="definition" label="标识" width="80" />
              <el-table-column prop="width" label="宽度" width="80" />
              <el-table-column prop="height" label="高度" width="80" />
              <el-table-column prop="desc" label="适用场景" />
            </el-table>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="saveVODConfig">保存VOD配置</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- CDN缓存策略 -->
      <el-tab-pane label="CDN缓存策略" name="cdn">
        <el-form :model="cdnConfig" label-width="160px" class="config-form">
          <el-alert
            title="CDN缓存策略说明"
            type="warning"
            :closable="false"
            class="config-alert"
          >
            <p>合理配置缓存策略可显著提升网站访问速度和降低带宽成本。</p>
            <p>图片资源建议缓存时间较长，动态内容建议不缓存或短时间缓存。</p>
          </el-alert>

          <el-form-item label="图片缓存时间">
            <el-input-number v-model="cdnConfig.imageCacheDays" :min="1" :max="365" />
            <span class="unit">天</span>
            <div class="form-tip">课程封面、用户头像等不常变更的图片</div>
          </el-form-item>

          <el-form-item label="视频缓存时间">
            <el-input-number v-model="cdnConfig.videoCacheDays" :min="1" :max="365" />
            <span class="unit">天</span>
            <div class="form-tip">课程视频片段，转码后内容固定</div>
          </el-form-item>

          <el-form-item label="静态资源缓存">
            <el-input-number v-model="cdnConfig.staticCacheDays" :min="1" :max="365" />
            <span class="unit">天</span>
            <div class="form-tip">CSS、JS、字体等构建产物</div>
          </el-form-item>

          <el-form-item label="启用Gzip压缩">
            <el-switch v-model="cdnConfig.enableGzip" />
            <div class="form-tip">压缩文本类资源，减少传输体积</div>
          </el-form-item>

          <el-form-item label="启用Brotli压缩">
            <el-switch v-model="cdnConfig.enableBrotli" />
            <div class="form-tip">比Gzip压缩率更高，现代浏览器均支持</div>
          </el-form-item>

          <el-form-item label="HTTP/2">
            <el-switch v-model="cdnConfig.enableHttp2" />
            <div class="form-tip">多路复用，提升并发加载性能</div>
          </el-form-item>

          <el-form-item label="浏览器缓存">
            <el-switch v-model="cdnConfig.enableBrowserCache" />
            <div class="form-tip">允许浏览器本地缓存静态资源</div>
          </el-form-item>

          <el-form-item>
            <el-button type="primary" @click="saveCDNConfig">保存缓存策略</el-button>
            <el-button @click="resetCDNConfig">恢复默认</el-button>
          </el-form-item>
        </el-form>
      </el-tab-pane>

      <!-- 资源统计 -->
      <el-tab-pane label="资源统计" name="stats">
        <div class="stats-grid">
          <el-card class="stat-card">
            <template #header>
              <div class="stat-header">
                <el-icon><PictureFilled /></el-icon>
                <span>图片资源</span>
              </div>
            </template>
            <div class="stat-value">{{ stats.imageCount }}</div>
            <div class="stat-label">张</div>
          </el-card>

          <el-card class="stat-card">
            <template #header>
              <div class="stat-header">
                <el-icon><VideoCamera /></el-icon>
                <span>视频资源</span>
              </div>
            </template>
            <div class="stat-value">{{ stats.videoCount }}</div>
            <div class="stat-label">个</div>
          </el-card>

          <el-card class="stat-card">
            <template #header>
              <div class="stat-header">
                <el-icon><Document /></el-icon>
                <span>文档资源</span>
              </div>
            </template>
            <div class="stat-value">{{ stats.documentCount }}</div>
            <div class="stat-label">个</div>
          </el-card>

          <el-card class="stat-card">
            <template #header>
              <div class="stat-header">
                <el-icon><DataLine /></el-icon>
                <span>总存储量</span>
              </div>
            </template>
            <div class="stat-value">{{ stats.totalSize }}</div>
            <div class="stat-label">GB</div>
          </el-card>
        </div>

        <el-divider />

        <h4>资源类型分布</h4>
        <el-table :data="resourceTypeList" border style="width: 100%; margin-top: 16px" empty-text="暂无数据">
          <el-table-column prop="type" label="资源类型" width="150" />
          <el-table-column prop="count" label="数量" width="100" />
          <el-table-column prop="size" label="存储大小" width="120" />
          <el-table-column prop="cdnTraffic" label="CDN流量（本月）" width="150" />
          <el-table-column prop="storagePath" label="存储路径" />
        </el-table>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
/**
 * StorageConfigPage - 云存储配置管理
 * 管理阿里云OSS、VOD配置和CDN缓存策略
 */
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { PictureFilled, VideoCamera, Document, DataLine } from '@element-plus/icons-vue'
import { setLocalData, getLocalData, STORAGE_KEYS } from '@/utils/localStore'
import { IMAGE_PRESETS, VIDEO_DEFINITIONS } from '@/utils/storage'
import type { OSSConfig, VODConfig } from '@/utils/storage'
import { useOperationLog } from '@/composables/useOperationLog'
const opLog = useOperationLog('storage')

const activeTab = ref('oss')

// ==================== OSS配置 ====================
const ossConfig = reactive<OSSConfig>({
  domain: 'https://magao-edu.oss-cn-beijing.aliyuncs.com',
  cdnDomain: 'https://cdn.magaoedu.com',
  secure: true,
})

const imagePresetList = Object.entries(IMAGE_PRESETS).map(([key, val]) => ({
  name: key,
  width: val.width,
  height: val.height,
  quality: val.quality,
  desc: key === 'thumbnail' ? '缩略图、小尺寸预览' :
        key === 'list' ? '列表页展示图' :
        key === 'cover' ? '封面大图' :
        key === 'avatar' ? '用户/团队头像' :
        key === 'banner' ? '首页轮播Banner' : '',
}))

function saveOSSConfig() {
  setLocalData(STORAGE_KEYS.OSS_CONFIG, { ...ossConfig })
  ElMessage.success('OSS配置已保存')
  opLog.logConfig('保存OSS配置')
}

function testOSSConnection() {
  ElMessage.warning('该能力暂未接入后端服务，操作未生效')
}

// ==================== VOD配置 ====================
const vodConfig = reactive<VODConfig>({
  playDomain: 'https://vod.magaoedu.com',
  cdnDomain: 'https://vod-cdn.magaoedu.com',
})

const videoPresetList = Object.entries(VIDEO_DEFINITIONS).map(([key, val]) => ({
  label: val.label,
  definition: val.definition,
  width: val.width,
  height: val.height,
  desc: key === 'ld' ? '移动端、弱网环境' :
        key === 'sd' ? '平板、一般网络' :
        key === 'hd' ? 'PC端、推荐清晰度' :
        key === 'fhd' ? '大屏、优质网络' : '',
}))

function saveVODConfig() {
  setLocalData(STORAGE_KEYS.VOD_CONFIG, { ...vodConfig })
  ElMessage.success('VOD配置已保存')
  opLog.logConfig('保存VOD配置')
}

// ==================== CDN缓存配置 ====================
interface CDNConfig {
  imageCacheDays: number
  videoCacheDays: number
  staticCacheDays: number
  enableGzip: boolean
  enableBrotli: boolean
  enableHttp2: boolean
  enableBrowserCache: boolean
}

const defaultCDNConfig: CDNConfig = {
  imageCacheDays: 30,
  videoCacheDays: 90,
  staticCacheDays: 7,
  enableGzip: true,
  enableBrotli: true,
  enableHttp2: true,
  enableBrowserCache: true,
}

const cdnConfig = reactive<CDNConfig>({ ...defaultCDNConfig })

function saveCDNConfig() {
  setLocalData('magao_cdn_config', { ...cdnConfig })
  ElMessage.success('CDN缓存策略已保存')
  opLog.logConfig('保存CDN缓存策略')
}

function resetCDNConfig() {
  Object.assign(cdnConfig, defaultCDNConfig)
  ElMessage.success('已恢复默认配置')
}

// ==================== 资源统计 ====================
const stats = reactive({
  imageCount: 0,
  videoCount: 0,
  documentCount: 0,
  totalSize: 0,
})

const resourceTypeList = [
  { type: '课程封面', count: 0, size: '0 MB', cdnTraffic: '0 GB', storagePath: 'course/covers/' },
  { type: '课程视频', count: 0, size: '0 MB', cdnTraffic: '0 GB', storagePath: 'course/videos/' },
  { type: '用户头像', count: 0, size: '0 MB', cdnTraffic: '0 GB', storagePath: 'user/avatars/' },
  { type: '资讯封面', count: 0, size: '0 MB', cdnTraffic: '0 GB', storagePath: 'news/covers/' },
  { type: '合作伙伴Logo', count: 0, size: '0 MB', cdnTraffic: '0 GB', storagePath: 'partner/logos/' },
  { type: 'Scratch项目', count: 0, size: '0 MB', cdnTraffic: '0 GB', storagePath: 'scratch/projects/' },
]

// ==================== 初始化 ====================
onMounted(() => {
  // 读取已保存的配置
  const savedOSS = getLocalData<OSSConfig | null>(STORAGE_KEYS.OSS_CONFIG, null)
  if (savedOSS) Object.assign(ossConfig, savedOSS)

  const savedVOD = getLocalData<VODConfig | null>(STORAGE_KEYS.VOD_CONFIG, null)
  if (savedVOD) Object.assign(vodConfig, savedVOD)

  const savedCDN = getLocalData<CDNConfig | null>('magao_cdn_config', null)
  if (savedCDN) Object.assign(cdnConfig, savedCDN)
})
</script>

<style scoped lang="scss">
.storage-config-page {
  padding: 24px;

  .page-header {
    margin-bottom: 24px;

    h2 {
      font-size: 20px;
      font-weight: 600;
      color: #1D2129;
      margin: 0 0 8px;
    }

    .page-desc {
      color: #86909C;
      font-size: 14px;
      margin: 0;
    }
  }

  .config-form {
    max-width: 700px;
    padding: 16px 0;
  }

  .config-alert {
    margin-bottom: 20px;

    p {
      margin: 4px 0;
      font-size: 13px;
    }
  }

  .form-tip {
    font-size: 12px;
    color: #86909C;
    margin-top: 4px;
  }

  .unit {
    margin-left: 8px;
    color: #86909C;
  }

  .stats-grid {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: 16px;
    margin-bottom: 24px;

    .stat-card {
      text-align: center;

      .stat-header {
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
        font-weight: 500;
      }

      .stat-value {
        font-size: 32px;
        font-weight: 700;
        color: #165DFF;
        margin: 12px 0 4px;
      }

      .stat-label {
        font-size: 14px;
        color: #86909C;
      }
    }
  }
}

@media (max-width: 768px) {
  .storage-config-page {
    .stats-grid {
      grid-template-columns: repeat(2, 1fr);
    }
  }
}
</style>
