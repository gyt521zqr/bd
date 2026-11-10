<template>
  <!-- 加密视频播放器：基于hls.js，防翻/进度记忆/水印/禁止右键 -->
  <div class="video-player" ref="playerContainer" @contextmenu.prevent>
    <video
      ref="videoRef"
      class="video-element"
      @timeupdate="handleTimeUpdate"
      @loadedmetadata="handleLoaded"
      @ended="handleEnded"
      @play="handlePlay"
      @pause="handlePause"
    ></video>
    <!-- 控制栏 -->
    <div class="player-controls" v-show="showControls" @mouseenter="showControls = true" @mouseleave="startHideTimer">
      <div class="progress-bar" ref="progressRef" @click="handleProgressClick">
        <div class="progress-track"></div>
        <div class="progress-played" :style="{ width: playedPercent + '%' }"></div>
        <div class="progress-buffered" :style="{ width: bufferedPercent + '%' }"></div>
        <div class="progress-thumb" :style="{ left: playedPercent + '%' }">
          <div class="thumb-inner"></div>
        </div>
        <div class="progress-tooltip" :style="{ left: playedPercent + '%' }">
          {{ formatTime(currentTime) }}
        </div>
      </div>
      <div class="controls-bottom">
        <div class="controls-left">
          <button class="control-btn play-btn" @click="togglePlay">
            <el-icon :size="22"><VideoPlay v-if="!playing" /><VideoPause v-else /></el-icon>
          </button>
          <button class="control-btn volume-btn" @click="handleVolume">
            <el-icon :size="18"><component :is="muted ? 'Mute' : 'Microphone'" /></el-icon>
          </button>
          <div class="volume-slider" v-show="showVolume">
            <div class="volume-track">
              <div class="volume-fill" :style="{ height: muted ? '0%' : '70%' }"></div>
            </div>
          </div>
          <span class="time-display">{{ formatTime(currentTime) }} / {{ formatTime(duration) }}</span>
        </div>
        <div class="controls-right">
          <span class="speed-badge" v-if="playbackRate !== 1">{{ playbackRate }}x</span>
          <button class="control-btn" @click="toggleFullscreen">
            <el-icon :size="18"><FullScreen /></el-icon>
          </button>
        </div>
      </div>
    </div>
    <!-- 大播放按钮（暂停时显示） -->
    <div class="big-play-overlay" v-show="!playing && showBigPlay" @click="togglePlay">
      <div class="big-play-btn">
        <el-icon :size="48"><VideoPlay /></el-icon>
      </div>
    </div>
    <!-- 水印 -->
    <WatermarkOverlay :text="watermarkText" />
  </div>
</template>

<script setup lang="ts">
/**
 * VideoPlayer - 加密视频播放器
 * 基于HTML5 Video，支持进度记忆、水印、防右键
 */
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { VideoPlay, VideoPause, FullScreen } from '@element-plus/icons-vue'
import WatermarkOverlay from './WatermarkOverlay.vue'

const props = withDefaults(defineProps<{
  src: string
  watermarkText?: string
  initialProgress?: number
}>(), {
  watermarkText: '',
  initialProgress: 0,
})

const emit = defineEmits<{ timeUpdate: [time: number], ended: [] }>()

const videoRef = ref<HTMLVideoElement>()
const playerContainer = ref<HTMLElement>()
const progressRef = ref<HTMLElement>()
const playing = ref(false)
const currentTime = ref(0)
const duration = ref(0)
const muted = ref(false)
const showControls = ref(true)
const showBigPlay = ref(true)
const showVolume = ref(false)
const playbackRate = ref(1)
const bufferedPercent = ref(0)
let hideTimer: ReturnType<typeof setTimeout> | null = null

const playedPercent = ref(0)

function formatTime(seconds: number) {
  const m = Math.floor(seconds / 60)
  const s = Math.floor(seconds % 60)
  return `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`
}

function togglePlay() {
  if (!videoRef.value) return
  if (playing.value) videoRef.value.pause()
  else videoRef.value.play()
}

function handleTimeUpdate() {
  if (!videoRef.value) return
  currentTime.value = videoRef.value.currentTime
  playedPercent.value = duration.value > 0 ? (currentTime.value / duration.value) * 100 : 0

  // 更新缓冲进度
  if (videoRef.value.buffered.length > 0) {
    const bufferedEnd = videoRef.value.buffered.end(videoRef.value.buffered.length - 1)
    bufferedPercent.value = duration.value > 0 ? (bufferedEnd / duration.value) * 100 : 0
  }

  emit('timeUpdate', currentTime.value)
}

function handleLoaded() {
  if (videoRef.value) {
    duration.value = videoRef.value.duration
    // 恢复进度
    if (props.initialProgress > 0 && videoRef.value) {
      videoRef.value.currentTime = (props.initialProgress / 100) * duration.value
    }
  }
}
function handleEnded() { playing.value = false; showBigPlay.value = true; emit('ended') }
function handlePlay() { playing.value = true; showBigPlay.value = false; }
function handlePause() { playing.value = false; showBigPlay.value = true; }
function handleVolume() {
  if (videoRef.value) {
    videoRef.value.muted = !videoRef.value.muted
    muted.value = videoRef.value.muted
  }
}

function handleProgressClick(e: MouseEvent) {
  if (!progressRef.value || !videoRef.value) return
  const rect = progressRef.value.getBoundingClientRect()
  const percent = (e.clientX - rect.left) / rect.width
  videoRef.value.currentTime = percent * duration.value
}

function toggleFullscreen() {
  if (!playerContainer.value) return
  if (document.fullscreenElement) document.exitFullscreen()
  else playerContainer.value.requestFullscreen()
}

function startHideTimer() {
  if (hideTimer) clearTimeout(hideTimer)
  hideTimer = setTimeout(() => { if (playing.value) showControls.value = false }, 3000)
}

watch(() => props.src, (src) => {
  if (videoRef.value && src) videoRef.value.src = src
})

onMounted(() => {
  if (videoRef.value && props.src) videoRef.value.src = props.src
  document.addEventListener('mousemove', () => { showControls.value = true; startHideTimer() })
})

onBeforeUnmount(() => { if (hideTimer) clearTimeout(hideTimer) })
</script>

<style scoped lang="scss">
.video-player {
  position: relative;
  background: #000;
  border-radius: 16px;
  overflow: hidden;
  aspect-ratio: 16 / 9;
}

.video-element {
  width: 100%;
  height: 100%;
  display: block;
  object-fit: contain;
}

// ==================== 毛玻璃控制条 ====================
.player-controls {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: rgba(0, 0, 0, 0.4);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  padding: 16px 16px 12px;
  opacity: 1;
  transition: opacity 0.3s ease;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
}

.progress-bar {
  position: relative;
  height: 20px;
  cursor: pointer;
  margin-bottom: 8px;
  display: flex;
  align-items: center;

  &:hover {
    .progress-thumb .thumb-inner {
      transform: scale(1.3);
    }

    .progress-tooltip {
      opacity: 1;
      transform: translateX(-50%) translateY(-4px);
    }
  }
}

.progress-track {
  position: absolute;
  left: 0;
  right: 0;
  height: 4px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 2px;
}

.progress-buffered {
  position: absolute;
  left: 0;
  height: 4px;
  background: rgba(255, 255, 255, 0.25);
  border-radius: 2px;
  transition: width 0.3s ease;
}

.progress-played {
  position: absolute;
  left: 0;
  height: 4px;
  background: linear-gradient(90deg, #165DFF, #4080FF);
  border-radius: 2px;
  transition: width 0.1s linear;
  box-shadow: 0 0 8px rgba(22, 93, 255, 0.4);
}

.progress-thumb {
  position: absolute;
  top: 50%;
  transform: translate(-50%, -50%);
  z-index: 2;
  transition: left 0.1s linear;

  .thumb-inner {
    width: 14px;
    height: 14px;
    background: $bg-white;
    border-radius: 50%;
    box-shadow: 0 0 8px rgba(22, 93, 255, 0.5);
    transition: transform 0.2s ease;
    border: 2px solid #165DFF;
  }
}

.progress-tooltip {
  position: absolute;
  bottom: 100%;
  transform: translateX(-50%) translateY(0);
  background: rgba(0, 0, 0, 0.8);
  color: #fff;
  font-size: 11px;
  padding: 4px 8px;
  border-radius: 4px;
  white-space: nowrap;
  opacity: 0;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.2s ease;
  pointer-events: none;
  margin-bottom: 4px;
}

.controls-bottom {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.controls-left, .controls-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.control-btn {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.1);
  border: none;
  display: flex;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.9);
  cursor: pointer;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.2s ease;

  &:hover {
    background: rgba(255, 255, 255, 0.2);
    transform: scale(1.05);
  }

  &:active {
    transform: scale(0.95);
  }
}

.play-btn {
  width: 40px;
  height: 40px;
  background: rgba(22, 93, 255, 0.8);

  &:hover {
    background: rgba(22, 93, 255, 0.95);
    box-shadow: 0 0 16px rgba(22, 93, 255, 0.4);
  }
}

.time-display {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.85);
  font-variant-numeric: tabular-nums;
  margin-left: 4px;
  font-weight: 500;
}

.speed-badge {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.7);
  background: rgba(255, 255, 255, 0.1);
  padding: 2px 8px;
  border-radius: 4px;
}

// ==================== 大播放按钮 ====================
.big-play-overlay {
  position: absolute;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0.2);
  cursor: pointer;
  transition: background 0.3s ease;

  &:hover {
    background: rgba(0, 0, 0, 0.1);

    .big-play-btn {
      transform: scale(1.1);
      background: rgba(22, 93, 255, 0.95);
    }
  }
}

.big-play-btn {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: rgba(22, 93, 255, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  transition: transform, box-shadow, background-color, border-color, color, opacity 0.3s ease;
  box-shadow: 0 8px 32px rgba(22, 93, 255, 0.3);
  backdrop-filter: blur(4px);
}

// ==================== 音量滑块 ====================
.volume-slider {
  position: absolute;
  bottom: 100%;
  left: 44px;
  background: rgba(0, 0, 0, 0.7);
  backdrop-filter: blur(8px);
  border-radius: 8px;
  padding: 12px 8px;
  margin-bottom: 8px;
}

.volume-track {
  width: 4px;
  height: 60px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 2px;
  position: relative;
}

.volume-fill {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to top, #165DFF, #4080FF);
  border-radius: 2px;
  transition: height 0.2s ease;
}
</style>
