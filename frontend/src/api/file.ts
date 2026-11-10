/**
 * 鏂囦欢鐩稿叧 API 鎺ュ彛
 */
import request from './index'
import type { Result } from '@/types'

/** 涓婁紶鏂囦欢 */
export function uploadFile(file: File, onProgress?: (percent: number) => void) {
  const formData = new FormData()
  formData.append('file', file)
  return request.post<any, Result<{ url: string; fileId: number }>>('/file/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress(event) {
      if (event.total && onProgress) {
        onProgress(Math.round((event.loaded / event.total) * 100))
      }
    },
  })
}

/** 涓嬭浇鏂囦欢 */
export function downloadFile(fileId: number) {
  return request.get(`/file/download/${fileId}`, {
    responseType: 'blob',
  })
}

/** 鍒犻櫎鏂囦欢 */
export function deleteFile(fileId: number) {
  return request.delete<any, Result<null>>(`/file/${fileId}`)
}

/** 鑾峰彇鏂囦欢鍒楄〃 */
export function getFileList(params: { pageNum: number; pageSize: number; type?: string }) {
  return request.get<any, Result<any>>('/file/list', { params })
}
