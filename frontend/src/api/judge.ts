/**
 * 统一判题 API 接口
 * 提交代码/Scratch项目、查询判题结果、保存/加载草稿
 */
import request from './index'
import type { EditorSubmitDTO, JudgeResult, RunResult, EditorLang } from '@/types/editor'
import type { Result } from '@/types/api'

/** 提交判题（异步，返回 recordId 供轮询） */
export function submitJudge(data: EditorSubmitDTO) {
  // 将前端 EditorSubmitDTO 转换为后端 JudgeSubmitDTO 格式
  const backendData: Record<string, any> = {
    questionId: data.questionId,
    lang: data.lang,
    code: data.code,
    examId: data.examId,
    isExam: data.isExam ?? false,
  }
  // Scratch/ScratchJr 项目数据映射到后端的 scratchJson 字段
  if (data.scratchProject?.json) {
    backendData.scratchJson = data.scratchProject.json
    if (data.scratchProject.assets?.length) {
      backendData.scratchAssets = JSON.stringify(data.scratchProject.assets)
    }
    // 封面缩略图：可选字段，后端无该字段时自动忽略（前端增强，不改后端逻辑）
    if (data.scratchProject.cover) {
      backendData.scratchCover = data.scratchProject.cover
    }
  }
  return request.post<any, Result<number>>('/judge/submit', backendData)
}

/** 查询判题结果 */
export function getJudgeResult(recordId: number) {
  return request.get<any, Result<JudgeResult>>(`/judge/result/${recordId}`)
}

/** 运行代码（仅运行不判题） */
export function runCode(data: { questionId: number; lang: EditorLang; code: string }) {
  return request.post<any, Result<RunResult>>('/judge/run', data)
}

/** 保存草稿 */
export function saveDraft(data: { questionId: number; lang: EditorLang; code?: string; scratchJson?: string }) {
  return request.post<any, Result<null>>('/judge/draft/save', data)
}

/** 加载草稿 */
export function loadDraft(params: { questionId: number; lang: EditorLang }) {
  return request.get<any, Result<{ code?: string; scratchJson?: string; updatedAt: string } | null>>('/judge/draft/load', { params })
}

/** 获取判题记录列表 */
export function getJudgeRecords(params: { questionId: number; pageNum: number; pageSize: number }) {
  return request.get<any, Result<{ list: JudgeResult[]; total: number }>>('/judge/records', { params })
}
