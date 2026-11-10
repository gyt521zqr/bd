/**
 * 成长体系相关 API 接口
 */
import request from './index'
import type { Result } from '@/types'

/** 积分记录 */
export interface PointRecord {
  id: number
  points: number
  type: string
  description: string
  createTime: string
}

/** 勋章信息 */
export interface MedalItem {
  id: number
  name: string
  icon: string
  description: string
  obtained: boolean
  obtainTime?: string
  condition: string
}

/** 等级信息 */
export interface LevelInfo {
  level: number
  name: string
  icon: string
  minPoints: number
  maxPoints: number
  currentPoints: number
}

/** 排行榜项 */
export interface LeaderboardItem {
  rank: number
  userId: number
  nickname: string
  avatar: string
  points: number
  level: number
  submitCount: number
}

/** 获取用户积分信息 */
export function getPointInfo() {
  return request.get<any, Result<{ total: number; today: number; week: number; month: number }>>('/growth/points')
}

/** 获取积分记录 */
export function getPointRecords(params: { pageNum: number; pageSize: number; type?: string }) {
  return request.get<any, Result<any>>('/growth/points/records', { params })
}

/** 获取勋章列表 */
export function getMedalList() {
  return request.get<any, Result<MedalItem[]>>('/growth/medals')
}

/** 获取等级信息 */
export function getLevelInfo() {
  return request.get<any, Result<LevelInfo>>('/growth/level')
}

/** 获取排行榜 */
export function getLeaderboard(params: { type: 'points' | 'submit' | 'streak'; period: 'week' | 'month' | 'all' }) {
  return request.get<any, Result<LeaderboardItem[]>>('/growth/leaderboard', { params })
}

/** 获取学习周报 */
export function getWeeklyReport() {
  return request.get<any, Result<any>>('/growth/weekly-report')
}
