<template>
  <!-- 试卷渲染：支持选择题/填空题/代码题/Scratch题 -->
  <div class="exam-paper">
    <!-- 题目导航 -->
    <div class="question-nav">
      <div class="nav-title">题目导航</div>
      <div class="nav-grid">
        <div
          v-for="(question, index) in questions"
          :key="question.id"
          class="nav-item"
          :class="{
            active: currentIndex === index,
            answered: isAnswered(question.id),
            current: currentIndex === index,
          }"
          @click="$emit('selectQuestion', index)"
        >
          {{ index + 1 }}
        </div>
      </div>
    </div>
    <!-- 题目内容 -->
    <div class="question-content">
      <div class="question-header">
        <span class="question-number">第 {{ currentIndex + 1 }} 题</span>
        <el-tag size="small" :type="currentQuestion?.type === 'code' ? 'warning' : 'info'">
          {{ questionTypeLabel }}
        </el-tag>
        <span class="question-score">{{ currentQuestion?.score }}分</span>
      </div>
      <div class="question-body">
        <h3>{{ currentQuestion?.title }}</h3>
        <!-- 选择题 -->
        <div v-if="currentQuestion?.type === 'choice'" class="choice-options">
          <el-radio-group v-model="currentAnswer" @change="handleAnswerChange">
            <el-radio v-for="(opt, i) in currentQuestion.options" :key="i" :value="String.fromCharCode(65 + i)">
              {{ String.fromCharCode(65 + i) }}. {{ opt }}
            </el-radio>
          </el-radio-group>
        </div>
        <!-- 填空题 -->
        <div v-else-if="currentQuestion?.type === 'fill'" class="fill-input">
          <el-input v-model="currentAnswer" placeholder="请输入答案" size="large" @input="handleAnswerChange" />
        </div>
        <!-- 代码题 -->
        <div v-else-if="currentQuestion?.type === 'code'" class="code-area">
          <MonacoCodeEditor v-model="currentAnswer" :language="currentQuestion.language" :height="300" @update:model-value="handleAnswerChange" />
        </div>
        <!-- Scratch题 -->
        <div v-else-if="currentQuestion?.type === 'scratch'" class="scratch-area">
          <ScratchEditor @save="(data) => { currentAnswer = JSON.stringify(data); handleAnswerChange() }" />
        </div>
      </div>
      <!-- 上一题/下一题 -->
      <div class="question-nav-buttons">
        <el-button @click="handlePrev" :disabled="currentIndex === 0">上一题</el-button>
        <el-button type="primary" @click="handleNext" :disabled="currentIndex === questions.length - 1">下一题</el-button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * ExamPaper - 试卷渲染组件
 * 支持选择题、填空题、代码题、Scratch题
 */
import { ref, computed, watch } from 'vue'
import MonacoCodeEditor from '@/components/editor/MonacoCodeEditor.vue'
import ScratchEditor from '@/components/editor/ScratchEditor.vue'

const props = defineProps<{
  questions: any[]
  currentIndex: number
  answers: Record<number, string>
}>()

const emit = defineEmits<{ selectQuestion: [index: number], answer: [payload: { questionId: number, answer: string }] }>()

const currentQuestion = computed(() => props.questions[props.currentIndex])
const currentAnswer = ref(props.answers[currentQuestion.value?.id] || '')

const questionTypeLabel = computed(() => {
  const map: Record<string, string> = { choice: '选择题', fill: '填空题', code: '编程题', scratch: 'Scratch题' }
  return map[currentQuestion.value?.type] || ''
})

watch(() => props.currentIndex, () => {
  currentAnswer.value = props.answers[currentQuestion.value?.id] || ''
})

function isAnswered(id: number) { return !!props.answers[id] }
function handleAnswerChange() { emit('answer', { questionId: currentQuestion.value.id, answer: currentAnswer.value }) }
function handlePrev() { if (props.currentIndex > 0) emit('selectQuestion', props.currentIndex - 1) }
function handleNext() { if (props.currentIndex < props.questions.length - 1) emit('selectQuestion', props.currentIndex + 1) }
</script>

<style scoped lang="scss">
.exam-paper { display: flex; gap: 20px; height: 100%; }
.question-nav { width: 200px; flex-shrink: 0; background: $bg-white; border-radius: 12px; padding: 16px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); }
.nav-title { font-size: 14px; font-weight: 600; color: #1D2129; margin-bottom: 12px; }
.nav-grid { display: grid; grid-template-columns: repeat(5, 1fr); gap: 6px; }
.nav-item { width: 32px; height: 32px; border-radius: 6px; display: flex; align-items: center; justify-content: center; font-size: 13px; cursor: pointer; background: #F7F8FA; color: #86909C; &.answered { background: #E8FFF5; color: #36D399; } &.active { background: #7B61FF; color: #fff; } }
.question-content { flex: 1; background: $bg-white; border-radius: 12px; padding: 24px; box-shadow: 0 2px 8px rgba(0,0,0,0.06); overflow-y: auto; }
.question-header { display: flex; align-items: center; gap: 12px; margin-bottom: 16px; }
.question-number { font-size: 14px; color: #86909C; }
.question-score { font-size: 14px; color: #FF9500; font-weight: 600; }
.question-body h3 { font-size: 16px; color: #1D2129; margin-bottom: 16px; line-height: 1.6; }
.choice-options { display: flex; flex-direction: column; gap: 12px; }
.fill-input { max-width: 400px; }
.code-area, .scratch-area { margin-top: 16px; }
.question-nav-buttons { display: flex; justify-content: space-between; margin-top: 24px; padding-top: 16px; border-top: 1px solid #F7F8FA; }
</style>
