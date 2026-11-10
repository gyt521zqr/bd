/**
 * 阿里云VOD视频点播 API
 * 支持视频上传、播放地址获取、视频管理
 */
import request from './index'
import type { Result } from '@/types'

/** 视频上传凭证 */
export interface UploadAuth {
  uploadAddress: string
  uploadAuth: string
  videoId: string
  requestId: string
}

/** 视频播放信息 */
export interface PlayInfo {
  videoId: string
  title: string
  duration: number
  coverUrl: string
  status: string
  createTime: string
  /** 各清晰度播放地址 */
  playUrls: {
    ld?: string
    sd?: string
    hd?: string
    fhd?: string
  }
  /** 默认播放地址 */
  defaultUrl: string
}

/** 获取视频上传凭证 */
export function getUploadAuth(params: {
  title: string
  fileName: string
  description?: string
  coverUrl?: string
  tags?: string
}) {
  return request.post<any, Result<UploadAuth>>('/vod/upload-auth', params)
}

/** 刷新上传凭证 */
export function refreshUploadAuth(videoId: string) {
  return request.post<any, Result<UploadAuth>>('/vod/refresh-upload-auth', { videoId })
}

/** 获取视频播放地址 */
export function getPlayInfo(videoId: string) {
  return request.get<any, Result<PlayInfo>>(`/vod/play-info/${videoId}`)
}

/** 批量获取视频播放地址 */
export function batchGetPlayInfo(videoIds: string[]) {
  return request.post<any, Result<Record<string, PlayInfo>>>('/vod/batch-play-info', { videoIds })
}

/** 获取视频列表 */
export function getVideoList(params: {
  pageNum: number
  pageSize: number
  keyword?: string
  status?: string
}) {
  return request.get<any, Result<{ list: PlayInfo[]; total: number }>>('/vod/list', { params })
}

/** 删除视频 */
export function deleteVideo(videoId: string) {
  return request.delete<any, Result<null>>(`/vod/${videoId}`)
}

/** 获取视频播放凭证（用于加密播放） */
export function getPlayAuth(videoId: string) {
  return request.get<any, Result<{ playAuth: string; expiration: number }>>(`/vod/play-auth/${videoId}`)
}

/** 转码任务查询 */
export function getTranscodeStatus(videoId: string) {
  return request.get<any, Result<{
    videoId: string
    status: 'Processing' | 'Success' | 'Fail'
    progress: number
    definitionList: string[]
  }>>(`/vod/transcode-status/${videoId}`)
}
