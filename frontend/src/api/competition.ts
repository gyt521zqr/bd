/**
 * 竞赛相关 API 接口
 */
import request from './index'
import type { Result, PageResult, PageQuery } from '@/types'
import type {
  CompetitionInfo,
  CompetitionRanking,
  CompetitionSubmitDTO,
} from '@/types/editor'

/** 创建竞赛（教师/管理员） */
export function createCompetition(data: {
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
  /** 允许使用的积木类别 */
  allowedBlocks?: string[]
  /** 最大参与人数 */
  maxParticipants?: number
  /** 封面图 */
  coverImage?: string
}) {
  return request.post<any, Result<{ id: number }>>('/competition/create', data)
}

/** 获取竞赛列表（分页） */
export function getCompetitionList(params: PageQuery & { status?: string }) {
  return request.get<any, Result<PageResult<CompetitionInfo>>>('/competition/list', {
    params,
  })
}

/** 获取竞赛详情 */
export function getCompetitionDetail(competitionId: number) {
  return request.get<any, Result<CompetitionInfo>>(
    `/competition/detail/${competitionId}`
  )
}

/** 加入竞赛 */
export function joinCompetition(competitionId: number) {
  return request.post<any, Result<{ joined: boolean; message: string }>>(
    `/competition/${competitionId}/join`
  )
}

/** 提交竞赛作品 */
export function submitCompetitionWork(data: CompetitionSubmitDTO) {
  return request.post<any, Result<{ recordId: number; score?: number }>>(
    '/competition/submit',
    data
  )
}

/** 获取竞赛成绩/结果 */
export function getCompetitionResults(competitionId: number) {
  return request.get<any, Result<CompetitionRanking[]>>(
    `/competition/${competitionId}/results`
  )
}

/** 获取竞赛排行榜 */
export function getCompetitionRanking(competitionId: number) {
  return request.get<any, Result<CompetitionRanking[]>>(
    `/competition/${competitionId}/ranking`
  )
}
