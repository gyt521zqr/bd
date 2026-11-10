<template>
  <!-- 编辑器工具栏（工具类）：格式化、复制、下载、字号、主题、模板、全屏 -->
  <div class="editor-toolbar" :class="`theme-${theme || 'dark'}`">
    <div class="toolbar-left">
      <el-tag size="small" type="info" class="lang-tag">{{ langLabel }}</el-tag>
      <el-button :icon="MagicStick" @click="$emit('format')" size="small" text>
        格式化
      </el-button>
    </div>
    <div class="toolbar-right">
      <!-- 字号 -->
      <el-dropdown trigger="click" @command="$emit('font-size', $event)">
        <el-button size="small" text :icon="Sort">
          字号
        </el-button>
        <template #dropdown>
          <el-dropdown-menu>
            <el-dropdown-item
              v-for="s in fontSizes"
              :key="s"
              :command="s"
              :class="{ 'is-active': s === currentFontSize }"
            >{{ s }}px</el-dropdown-item>
          </el-dropdown-menu>
        </template>
      </el-dropdown>

      <!-- 主题切换 -->
      <el-button
        size="small"
        text
        :icon="theme === 'light' ? Moon : Sunny"
        @click="$emit('toggle-theme')"
        :title="theme === 'light' ? '切换为暗色主题' : '切换为亮色主题'"
      >
        {{ theme === 'light' ? '暗色' : '亮色' }}
      </el-button>

      <!-- 代码模板库 -->
      <el-button :icon="Files" @click="$emit('open-templates')" size="small" text title="插入示例代码">
        模板
      </el-button>

      <el-button :icon="DocumentCopy" @click="$emit('copy')" size="small" text title="复制代码">
        复制
      </el-button>
      <el-button :icon="Download" @click="$emit('download')" size="small" text title="下载代码">
        下载
      </el-button>
      <el-button :icon="FullScreen" text @click="$emit('fullscreen')" size="small" title="全屏">全屏</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * EditorToolbar - 编辑器工具类工具栏
 * 主操作（运行/提交/重置/草稿）已下沉到底部统一动作栏，此处仅保留视图与工具类按钮
 */
import {
  FullScreen, DocumentCopy, Download, Moon, Sunny, Files, Sort, MagicStick,
} from '@element-plus/icons-vue'

defineProps<{
  running?: boolean
  submitting?: boolean
  showSave?: boolean
  langLabel?: string
  theme?: 'light' | 'dark'
  currentFontSize?: number
}>()

defineEmits<{
  format: []
  fullscreen: []
  copy: []
  download: []
  'font-size': [size: number]
  'toggle-theme': []
  'open-templates': []
}>()

const fontSizes = [12, 14, 16, 18, 20]
</script>

<style scoped lang="scss">
.editor-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  background: #1D2129;
  border-bottom: 1px solid #4E5969;
  flex-shrink: 0;
  gap: 8px;
  flex-wrap: wrap;

  &.theme-light {
    background: #F2F3F5;
    border-bottom-color: #E5E6EB;

    .lang-tag { /* Element Plus 自管颜色，无需覆盖 */ }
  }
}

.toolbar-left,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.lang-tag {
  margin-right: 4px;
}

:deep(.el-dropdown-menu__item.is-active) {
  color: #165DFF;
  font-weight: 600;
}
</style>
