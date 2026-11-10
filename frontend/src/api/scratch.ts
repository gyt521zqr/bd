/**
 * Scratch 作品相关 API 接口
 */
import request from './index'
import type { Result, PageResult, PageQuery } from '@/types'
import type {
  ScratchWork,
  CompetitionRanking,
  CompetitionStatusInfo,
} from '@/types/editor'

/** 保存 Scratch 作品到 MinIO */
export function saveWork(data: {
  /** 作品ID（编辑时传入，新建时不传） */
  id?: number
  /** 作品名称 */
  title: string
  /** Scratch 项目 JSON 数据 */
  projectJson: string
  /** 缩略图（base64 或 Blob） */
  thumbnail?: string
}) {
  return request.post<any, Result<{ id: number }>>('/scratch/work/save', data)
}

/** 加载用户 Scratch 作品列表（分页） */
export function loadWorks(params: PageQuery & { keyword?: string }) {
  return request.get<any, Result<PageResult<ScratchWork>>>('/scratch/work/list', { params })
}

/** 加载单个 Scratch 作品详情 */
export function loadWork(workId: number) {
  return request.get<any, Result<ScratchWork>>(`/scratch/work/${workId}`)
}

/** 删除作品 */
export function deleteWork(workId: number) {
  return request.delete<any, Result<null>>(`/scratch/work/${workId}`)
}

/** 导出 sb3 文件（返回下载链接） */
export function exportSb3(workId: number) {
  return request.get<any, Result<{ downloadUrl: string }>>(`/scratch/work/${workId}/export`)
}

/** 获取竞赛实时排行榜 */
export function getCompetitionRanking(competitionId: number) {
  return request.get<any, Result<CompetitionRanking[]>>(
    `/scratch/competition/${competitionId}/ranking`
  )
}

/** 获取竞赛状态（剩余时间、已提交人数） */
export function getCompetitionStatus(competitionId: number) {
  return request.get<any, Result<CompetitionStatusInfo>>(
    `/scratch/competition/${competitionId}/status`
  )
}
