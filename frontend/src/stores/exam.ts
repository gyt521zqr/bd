/**
 * 鑰冭瘯鐘舵€佺鐞? * 绠＄悊褰撳墠鑰冭瘯銆佸€掕鏃躲€佸垏灞忔鏁般€佷綔绛旀暟鎹? */
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { ExamItem } from '@/types'

export const useExamStore = defineStore('exam', () => {
  /** 褰撳墠鑰冭瘯淇℃伅 */
  const currentExam = ref<ExamItem | null>(null)
  /** 鑰冭瘯鍓╀綑鏃堕棿锛堢锛?*/
  const remainingTime = ref(0)
  /** 鍒囧睆娆℃暟 */
  const switchCount = ref(0)
  /** 鏈€澶у厑璁稿垏灞忔鏁?*/
  const maxSwitchCount = ref(3)
  /** 浣滅瓟鏁版嵁 */
  const answers = ref<Map<number, string>>(new Map())
  /** record id from enterExam, used for submit persist */
  const recordId = ref<number | null>(null)
  /** 交卷时的答案快照（成绩页展示用，resetExam 不清空） */
  const submittedAnswers = ref<Map<number, string>>(new Map())
  /** 错题重练题集（覆盖默认题源，供做题页使用） */
  const overrideQuestions = ref<any[] | null>(null)
  /** 错题重练标题 */
  const overrideTitle = ref('')
  /** 错题重练时长（分钟，0 表示用默认） */
  const overrideDuration = ref(0)
  /** 褰撳墠棰樼洰绱㈠紩 */
  const currentQuestionIndex = ref(0)
  /** 鏄惁宸蹭氦鍗?*/
  const submitted = ref(false)
  /** 鑰冭瘯鏄惁寮€濮?*/
  const started = ref(false)

  /** 鏄惁宸茶秴鏃?*/
  const isTimeout = computed(() => remainingTime.value <= 0)
  /** 鏄惁瓒呰繃鍒囧睆闄愬埗 */
  const isSwitchExceeded = computed(() => switchCount.value > maxSwitchCount.value)
  /** 宸茬瓟棰樻暟 */
  const answeredCount = computed(() => answers.value.size)
  /** 鏍煎紡鍖栧墿浣欐椂闂?*/
  const formattedTime = computed(() => {
    const hours = Math.floor(remainingTime.value / 3600)
    const minutes = Math.floor((remainingTime.value % 3600) / 60)
    const seconds = remainingTime.value % 60
    return {
      hours: String(hours).padStart(2, '0'),
      minutes: String(minutes).padStart(2, '0'),
      seconds: String(seconds).padStart(2, '0'),
    }
  })

  /** 璁剧疆褰撳墠鑰冭瘯 */
  function setExam(exam: ExamItem) {
    currentExam.value = exam
    remainingTime.value = exam.duration * 60
    switchCount.value = 0
    answers.value = new Map()
    submittedAnswers.value = new Map()
    currentQuestionIndex.value = 0
    submitted.value = false
    started.value = true
  }

  /** 鏇存柊鍓╀綑鏃堕棿 */
  function tick() {
    if (remainingTime.value > 0) {
      remainingTime.value--
    }
  }

  /** 璁板綍鍒囧睆 */
  function recordSwitch() {
    switchCount.value++
  }

  /** 淇濆瓨绛旀 */
  function saveAnswer(questionId: number, answer: string) {
    answers.value.set(questionId, answer)
  }

  function setRecordId(id: number) {
    recordId.value = id
  }

  /** 鑾峰彇绛旀 */
  function getAnswer(questionId: number): string | undefined {
    return answers.value.get(questionId)
  }

  /** 璁剧疆褰撳墠棰樼洰绱㈠紩 */
  function setCurrentQuestionIndex(index: number) {
    currentQuestionIndex.value = index
  }

  /** 浜ゅ嵎 */
  function submitExam() {
    submitted.value = true
    started.value = false
    submittedAnswers.value = new Map(answers.value)
  }

  /** 閲嶇疆鑰冭瘯鐘舵€?*/
  function resetExam() {
    currentExam.value = null
    remainingTime.value = 0
    switchCount.value = 0
    answers.value = new Map()
    recordId.value = null
    currentQuestionIndex.value = 0
    submitted.value = false
    started.value = false
  }

  /** 设置错题重练题集（覆盖默认题源） */
  function setOverrideQuestions(questions: any[], title?: string, duration?: number) {
    overrideQuestions.value = questions
    overrideTitle.value = title || ''
    overrideDuration.value = duration || 0
  }

  /** 清除错题重练题集 */
  function clearOverrideQuestions() {
    overrideQuestions.value = null
    overrideTitle.value = ''
    overrideDuration.value = 0
  }

  return {
    currentExam,
    remainingTime,
    switchCount,
    maxSwitchCount,
    answers,
    submittedAnswers,
    recordId,
    overrideQuestions,
    overrideTitle,
    overrideDuration,
    currentQuestionIndex,
    submitted,
    started,
    isTimeout,
    isSwitchExceeded,
    answeredCount,
    formattedTime,
    setExam,
    tick,
    recordSwitch,
    saveAnswer,
    setRecordId,
    getAnswer,
    setCurrentQuestionIndex,
    submitExam,
    resetExam,
    setOverrideQuestions,
    clearOverrideQuestions,
  }
})
