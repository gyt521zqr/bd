/**
 * 编辑器统一类型定义
 * 覆盖所有编程语言的编辑器参数、提交结构、判题返回
 */

/** 支持的编程语言 */
export type EditorLang = 'scratch3' | 'scratchjr' | 'python' | 'pythonturtle' | 'cpp' | 'goc'

/** 语言显示名称映射 */
export const LANG_LABELS: Record<EditorLang, string> = {
  scratch3: 'Scratch',
  scratchjr: 'ScratchJr',
  python: 'Python',
  pythonturtle: 'Python Turtle',
  cpp: 'C++',
  goc: 'GoC',
}

/** 语言对应的 Monaco 编辑器语言标识 */
export const LANG_MONACO_MAP: Record<EditorLang, string> = {
  scratch3: '',
  scratchjr: '',
  python: 'python',
  pythonturtle: 'python',
  cpp: 'cpp',
  goc: 'cpp',
}

/** 是否为代码类语言（非积木） */
export function isCodeLang(lang: EditorLang): boolean {
  return lang !== 'scratch3' && lang !== 'scratchjr'
}

/** 编辑器模式 */
export type EditorMode = 'practice' | 'exam'

/** 编辑器提交数据 */
export interface EditorSubmitDTO {
  /** 题目ID */
  questionId: number
  /** 编程语言 */
  lang: EditorLang
  /** 代码文本（Python/C++/GoC） */
  code?: string
  /** Scratch 项目数据 */
  scratchProject?: ScratchProjectData
  /** 考试ID（考试模式下必填） */
  examId?: number
  /** 是否考试模式 */
  isExam: boolean
}

/** Scratch 项目数据 */
export interface ScratchProjectData {
  /** 项目 JSON */
  json: string
  /** 素材文件 URL 列表 */
  assets?: string[]
  /** 舞台缩略图（dataURL，提交时附带作封面，后端可选忽略） */
  cover?: string
}

/** 判题状态 */
export type JudgeStatus =
  | 'PENDING'
  | 'RUNNING'
  | 'ACCEPTED'
  | 'WRONG_ANSWER'
  | 'TIME_LIMIT_EXCEEDED'
  | 'MEMORY_LIMIT_EXCEEDED'
  | 'RUNTIME_ERROR'
  | 'COMPILE_ERROR'
  | 'SYSTEM_ERROR'

/** 判题返回结果 */
export interface JudgeResult {
  /** 提交记录ID */
  recordId: number
  /** 判题状态 */
  status: JudgeStatus
  /** 得分 */
  score: number
  /** 总分 */
  totalScore: number
  /** 通过用例数 */
  passedCases: number
  /** 总用例数 */
  totalCases: number
  /** 执行时间（ms） */
  timeUsed?: number
  /** 内存消耗（KB） */
  memoryUsed?: number
  /** 运行输出 */
  output?: string
  /** 错误信息 */
  errorMessage?: string
  /** 各用例详情 */
  caseResults?: JudgeCaseResult[]
}

/** 单个用例判题结果 */
export interface JudgeCaseResult {
  /** 用例ID */
  caseId: number
  /** 是否通过 */
  passed: boolean
  /** 实际输出 */
  actualOutput?: string
  /** 期望输出 */
  expectedOutput?: string
  /** 执行时间 */
  timeUsed?: number
  /** 内存消耗 */
  memoryUsed?: number
  /** 错误信息 */
  errorMessage?: string
}

/** 运行结果（非判题，仅运行） */
export interface RunResult {
  /** 是否成功 */
  success: boolean
  /** 运行输出 */
  output: string
  /** 错误信息 */
  errorMessage?: string
  /** 执行时间（ms） */
  timeUsed?: number
}

/** 编辑器运行状态 */
export type RunStatus = 'idle' | 'running' | 'success' | 'error'

/** 草稿数据 */
export interface DraftData {
  /** 题目ID */
  questionId: number
  /** 编程语言 */
  lang: EditorLang
  /** 代码文本 */
  code?: string
  /** Scratch 项目 JSON */
  scratchJson?: string
  /** 最后保存时间 */
  updatedAt: string
}

/** 编辑器配置 */
export interface EditorConfig {
  /** 编程语言 */
  lang: EditorLang
  /** 编辑器模式 */
  mode: EditorMode
  /** 题目ID */
  questionId: number
  /** 考试ID（考试模式） */
  examId?: number
  /** 是否只读 */
  readOnly?: boolean
  /** 模板代码 */
  templateCode?: string
  /** 时间限制（ms） */
  timeLimit?: number
  /** 内存限制（KB） */
  memoryLimit?: number
}

/** 判题状态中文映射 */
export const JUDGE_STATUS_LABELS: Record<JudgeStatus, string> = {
  PENDING: '等待判题',
  RUNNING: '判题中',
  ACCEPTED: '通过',
  WRONG_ANSWER: '答案错误',
  TIME_LIMIT_EXCEEDED: '超时',
  MEMORY_LIMIT_EXCEEDED: '超内存',
  RUNTIME_ERROR: '运行错误',
  COMPILE_ERROR: '编译错误',
  SYSTEM_ERROR: '系统错误',
}

/** 判题状态对应的 Element Plus Tag 类型 */
export const JUDGE_STATUS_TAG_TYPE: Record<JudgeStatus, string> = {
  PENDING: 'info',
  RUNNING: 'warning',
  ACCEPTED: 'success',
  WRONG_ANSWER: 'danger',
  TIME_LIMIT_EXCEEDED: 'warning',
  MEMORY_LIMIT_EXCEEDED: 'warning',
  RUNTIME_ERROR: 'danger',
  COMPILE_ERROR: 'danger',
  SYSTEM_ERROR: 'danger',
}

// ==================== Scratch 作品类型 ====================

/** Scratch 作品 */
export interface ScratchWork {
  /** 作品ID */
  id: number
  /** 用户ID */
  userId: number
  /** 作品名称 */
  title: string
  /** sb3 项目数据（MinIO 存储路径或 base64） */
  sb3Data: string
  /** 缩略图 URL */
  thumbnail: string
  /** 创建时间 */
  createdAt: string
  /** 最后编辑时间 */
  updatedAt: string
}

// ==================== 竞赛类型 ====================

/** 竞赛状态 */
export type CompetitionStatus = 'NOT_STARTED' | 'IN_PROGRESS' | 'ENDED' | 'CANCELLED'

/** 竞赛信息 */
export interface CompetitionInfo {
  /** 竞赛ID */
  id: number
  /** 竞赛标题 */
  title: string
  /** 竞赛描述 */
  description: string
  /** 题目描述 */
  problemDescription: string
  /** 开始时间 */
  startTime: string
  /** 结束时间 */
  endTime: string
  /** 竞赛状态 */
  status: CompetitionStatus
  /** 允许使用的积木类别 */
  allowedBlocks?: string[]
  /** 最大参与人数 */
  maxParticipants: number
  /** 当前参与人数 */
  currentParticipants: number
  /** 竞赛时长（分钟） */
  duration: number
  /** 创建者ID */
  creatorId: number
  /** 封面图 */
  coverImage?: string
  /** 难度等级 */
  difficulty?: number
}

/** 竞赛排行榜条目 */
export interface CompetitionRanking {
  /** 排名 */
  rank: number
  /** 用户ID */
  userId: number
  /** 用户昵称 */
  nickname: string
  /** 用户头像 */
  avatar?: string
  /** 得分 */
  score: number
  /** 提交时间 */
  submitTime: string
  /** 用时（秒） */
  timeUsed: number
}

/** 竞赛提交 DTO */
export interface CompetitionSubmitDTO {
  /** 竞赛ID */
  competitionId: number
  /** Scratch 项目数据 */
  scratchProject: ScratchProjectData
  /** 切屏次数 */
  switchCount: number
}

/** 竞赛实时状态（轮询接口返回） */
export interface CompetitionStatusInfo {
  /** 竞赛ID */
  competitionId: number
  /** 剩余时间（秒） */
  remainingTime: number
  /** 已提交人数 */
  submittedCount: number
  /** 当前参与人数 */
  participantCount: number
  /** 竞赛状态 */
  status: CompetitionStatus
}
