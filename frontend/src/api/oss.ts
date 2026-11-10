/**
 * 阿里云OSS对象存储 API
 * 支持直传、分片上传、获取签名URL
 */
import request from './index'
import type { Result } from '@/types'

/** 直传凭证 */
export interface OSSUploadToken {
  /** 临时AccessKeyId */
  accessKeyId: string
  /** 临时AccessKeySecret */
  accessKeySecret: string
  /** 安全令牌 */
  securityToken: string
  /** 过期时间（Unix时间戳） */
  expiration: number
  /** Bucket名称 */
  bucket: string
  /** 地域Endpoint */
  endpoint: string
  /** 上传目录前缀 */
  dir: string
}

/** 获取直传凭证（服务端签名后直传） */
export function getUploadToken(params?: {
  /** 文件类型：image/video/audio/document */
  fileType?: string
  /** 业务目录：course/news/user等 */
  dir?: string
}) {
  return request.get<any, Result<OSSUploadToken>>('/oss/upload-token', { params })
}

/** 获取私有Bucket文件的签名URL */
export function getSignedUrl(params: {
  /** OSS文件路径 */
  objectKey: string
  /** URL过期时间（秒），默认3600 */
  expires?: number
  /** 是否使用CDN域名 */
  useCDN?: boolean
}) {
  return request.post<any, Result<{ url: string; expiration: number }>>('/oss/signed-url', params)
}

/** 批量获取签名URL */
export function batchGetSignedUrls(params: {
  objectKeys: string[]
  expires?: number
  useCDN?: boolean
}) {
  return request.post<any, Result<Record<string, { url: string; expiration: number }>>>('/oss/batch-signed-urls', params)
}

/** 删除OSS文件 */
export function deleteOSSFile(objectKey: string) {
  return request.delete<any, Result<null>>('/oss/file', { data: { objectKey } })
}

/** 批量删除OSS文件 */
export function batchDeleteOSSFiles(objectKeys: string[]) {
  return request.post<any, Result<{ deleted: string[]; failed: string[] }>>('/oss/batch-delete', { objectKeys })
}

/** 获取OSS文件列表 */
export function getOSSFileList(params: {
  prefix?: string
  marker?: string
  maxKeys?: number
}) {
  return request.get<any, Result<{
    files: Array<{
      key: string
      size: number
      lastModified: string
      url: string
    }>
    nextMarker?: string
    isTruncated: boolean
  }>>('/oss/list', { params })
}

/** 文件上传（通过后端转发，小文件适用） */
export function uploadFileViaServer(
  file: File,
  params: {
    /** 业务类型 */
    type: string
    /** 关联ID */
    id?: number | string
  },
  onProgress?: (percent: number) => void
) {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('type', params.type)
  if (params.id !== undefined) {
    formData.append('id', String(params.id))
  }

  return request.post<any, Result<{ url: string; objectKey: string; size: number }>>('/oss/upload', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
    onUploadProgress(event) {
      if (event.total && onProgress) {
        onProgress(Math.round((event.loaded / event.total) * 100))
      }
    },
  })
}
