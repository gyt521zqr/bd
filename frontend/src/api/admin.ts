/**
 * 后台管理端统一 API 桥接层
 * ------------------------------------------------------------------
 * 对齐后端 /api/admin/* 契约：
 *   - 读：AdminController        GET  /api/admin/{resource}/list
 *   - 写：AdminWriteController   POST /api/admin/{resource}/create
 *                                 PUT  /api/admin/{resource}/{id}
 *                               DELETE /api/admin/{resource}/{id}
 *                                POST /api/admin/{resource}/batch-delete  body { ids:number[] }
 *
 * 说明：
 *  - 营销套餐/优惠券 列表端点无 /list 后缀（后端为 /api/admin/marketing/plans、/api/admin/marketing/coupons），
 *    故 listPath 单独处理。
 *  - 响应经 axios 拦截器已是 { code, message, data }；分页 data 为 { list, total, pageNum, pageSize, pages } 信封。
 *  - 所有方法后端不可用时由调用方决定是否走本地兜底，本层只负责请求。
 */
import request from './index'

/** 分页信封（与后端 GlobalPageResultAdvice 一致） */
export interface AdminPageData<T> {
  list: T[]
  total: number
  pageNum: number
  pageSize: number
  pages: number
}

export interface AdminApi<T = Record<string, any>> {
  /** 列表（分页） */
  list: (params?: Record<string, any>) => Promise<any>
  /** 新建，返回 data = 新记录（含 id） */
  create: (data: Partial<T>) => Promise<any>
  /** 全量更新（PUT /{id}） */
  update: (id: number | string, data: Partial<T>) => Promise<any>
  /** 删除（DELETE /{id}） */
  remove: (id: number | string) => Promise<any>
  /** 批量删除（POST /batch-delete，body { ids }） */
  batchRemove: (ids: Array<number | string>) => Promise<any>
}

// ===================== 字段映射适配层 =====================
// 后台实体字段名与前端 admin 页 mock 列名存在差异：
//   读：把后端实体字段映射为前端列名（如 className->name、totalLessons->lessonCount、
//       paperName->name、membershipName->name、validDays->duration、couponName->name、
//       discountValue->value、validEndTime->expireTime、noticeType->type、
//       createTime->createdAt/uploadTime、roleType->role、lastLoginTime->lastLogin、
//       fileName->name、fileType->type、practice 计算 passRate 等）。
//   写：把前端表单字段映射回实体字段（如 name->className、maxStudents->maxStudent、
//       lessonCount->totalLessons、paperName/name 互保、membershipName/name 互保等）。
// 采用「加法映射」：只补充别名，绝不删除页面字段，未知字段由 Jackson 忽略，安全无副作用。
// 注意：部分列后端无对应字段（如 Course 的 ageRange/videoCount/hasTrial、Exam 的
//       duration/totalScore/questionCount/participantCount/registerDeadline、Score 的
//       studentName/school/grade/examName/rank/award、Notification 的 readCount/totalCount/
//       target/sendTime 等），属需联表/VO 的深层缺口，此处保持空并记录于 API_CONTRACT.md。
type RowMapper = (row: any) => any
type FormMapper = (form: any) => any
interface ResourceAdapter {
  normalize?: RowMapper
  serialize?: FormMapper
}

const RESOURCE_MAP: Record<string, ResourceAdapter> = {
  class: {
    normalize: (r) => ({
      ...r,
      name: r.name ?? r.className ?? '',
      studentCount: r.studentCount ?? r.currentStudent ?? 0,
      maxStudents: r.maxStudents ?? r.maxStudent ?? 0,
    }),
    serialize: (f) => ({
      ...f,
      className: f.className ?? f.name,
      maxStudent: f.maxStudent ?? f.maxStudents,
    }),
  },
  course: {
    normalize: (r) => ({ ...r, lessonCount: r.totalLessons ?? r.lessonCount ?? 0 }),
    serialize: (f) => ({ ...f, totalLessons: f.totalLessons ?? f.lessonCount }),
  },
  paper: {
    normalize: (r) => ({ ...r, name: r.name ?? r.paperName ?? '' }),
    serialize: (f) => ({ ...f, paperName: f.paperName ?? f.name }),
  },
  'marketing/plans': {
    normalize: (r) => ({ ...r, name: r.name ?? r.membershipName ?? '', duration: r.duration ?? r.validDays ?? 0 }),
    serialize: (f) => ({ ...f, membershipName: f.membershipName ?? f.name, validDays: f.validDays ?? f.duration }),
  },
  'marketing/coupons': {
    normalize: (r) => ({ ...r, name: r.name ?? r.couponName ?? '', value: r.value ?? r.discountValue ?? 0, expireTime: r.expireTime ?? r.validEndTime ?? null }),
    serialize: (f) => ({ ...f, couponName: f.couponName ?? f.name, discountValue: f.discountValue ?? f.value, validEndTime: f.validEndTime ?? f.expireTime }),
  },
  category: { normalize: (r) => ({ ...r, createdAt: r.createdAt ?? r.createTime ?? null }) },
  partner: { normalize: (r) => ({ ...r, createdAt: r.createdAt ?? r.createTime ?? null }) },
  banner: {
    normalize: (r) => ({ ...r, enabled: r.enabled ?? (r.status === 1), createdAt: r.createdAt ?? r.createTime ?? null }),
    serialize: (f) => ({ ...f, sortOrder: f.sortOrder ?? f.sort, status: f.status ?? (f.enabled ? 1 : 0) }),
  },
  file: {
    normalize: (r) => ({ ...r, name: r.name ?? r.fileName ?? '', type: r.type ?? r.fileType ?? '', uploadTime: r.uploadTime ?? r.createTime ?? null }),
  },
  notification: { normalize: (r) => ({ ...r, type: r.type ?? r.noticeType ?? null }) },
  news: { normalize: (r) => ({ ...r, category: r.category ?? r.categoryId ?? null }) },
  practice: {
    normalize: (r) => ({ ...r, passRate: r.submitCount ? Math.round(((r.passCount ?? 0) / r.submitCount) * 100) : 0 }),
  },
  student: {
    normalize: (r) => ({ ...r, name: r.name ?? r.realName ?? r.nickname ?? r.username, role: r.role ?? r.roleType, lastLogin: r.lastLogin ?? r.lastLoginTime }),
    serialize: (f) => ({
      ...f,
      username: f.username ?? f.name,
      realName: f.realName ?? f.name,
      status: f.status === 'enabled' ? 1 : f.status === 'disabled' ? 0 : f.status,
    }),
  },
  teacher: {
    normalize: (r) => ({ ...r, name: r.name ?? r.realName ?? r.nickname ?? r.username, role: r.role ?? r.roleType, lastLogin: r.lastLogin ?? r.lastLoginTime }),
    serialize: (f) => ({
      ...f,
      username: f.username ?? f.name,
      realName: f.realName ?? f.name,
      status: f.status === 'enabled' ? 1 : f.status === 'disabled' ? 0 : f.status,
    }),
  },
  account: {
    // 后端 roleType 取值为 ADMIN / OPERATOR；页面标签为 admin / operator
    // （super_admin 由 ADMIN 派生，后端无独立概念，展示层统一归一为 admin）。
    normalize: (r) => ({
      ...r,
      name: r.name ?? r.realName ?? r.nickname ?? r.username,
      role: r.role ?? (r.roleType === 'ADMIN' ? 'admin' : r.roleType === 'OPERATOR' ? 'operator' : (r.roleType ? r.roleType.toLowerCase() : r.role)),
      lastLogin: r.lastLogin ?? r.lastLoginTime,
    }),
    serialize: (f) => ({
      ...f,
      username: f.username ?? f.name,
      realName: f.realName ?? f.nickname ?? f.name,
      roleType: f.roleType ?? (f.role === 'super_admin' || f.role === 'admin' ? 'ADMIN' : f.role === 'operator' ? 'OPERATOR' : f.role),
      status: f.status === 'enabled' ? 1 : f.status === 'disabled' ? 0 : f.status,
    }),
  },
}

/**
 * 创建资源 API 实例。
 * @param resource 资源名，如 'news' / 'course' / 'marketing/plans'
 */
export function createAdminApi<T = Record<string, any>>(resource: string): AdminApi<T> {
  // 营销套餐/优惠券 列表端点无 /list 后缀
  const listPath = resource.startsWith('marketing/') ? resource : `${resource}/list`
  const adapter = RESOURCE_MAP[resource]
  return {
    list: async (params: Record<string, any> = {}) => {
      const res = await request.get(`/admin/${listPath}`, { params })
      // 读响应：把实体字段归一化为前端列名（兼容分页信封与纯数组两种返回形态）
      const data: any = (res as any)?.data
      const rows: any[] = Array.isArray(data) ? data : (data?.list ?? [])
      if (adapter?.normalize && rows.length) {
        const norm = rows.map(adapter.normalize)
        if (Array.isArray(data)) (res as any).data = norm
        else if (data) data.list = norm
      }
      return res
    },
    create: (data: Partial<T>) => {
      const payload = adapter?.serialize ? adapter.serialize(data) : data
      return request.post(`/admin/${resource}/create`, payload)
    },
    update: (id: number | string, data: Partial<T>) => {
      const payload = adapter?.serialize ? adapter.serialize(data) : data
      return request.put(`/admin/${resource}/${id}`, payload)
    },
    remove: (id: number | string) => request.delete(`/admin/${resource}/${id}`),
    batchRemove: (ids: Array<number | string>) =>
      request.post(`/admin/${resource}/batch-delete`, { ids }),
  }
}

// ===================== 预置资源实例 =====================
// 用户类（student/teacher/account）后端仅提供读，写操作本控制器不承载，实例仍可用读。
export const studentApi = createAdminApi('student')
export const teacherApi = createAdminApi('teacher')
export const accountApi = createAdminApi('account')
// 业务资源
export const courseApi = createAdminApi('course')
export const classApi = createAdminApi('class')
export const examApi = createAdminApi('exam') // 赛事（CompetitionInfo）
export const registrationApi = createAdminApi('registration')
export const questionApi = createAdminApi('question')
export const paperApi = createAdminApi('paper')
export const scoreApi = createAdminApi('score')
export const practiceApi = createAdminApi('practice')
export const bannerApi = createAdminApi('banner')
export const newsApi = createAdminApi('news')
export const categoryApi = createAdminApi('category')
export const partnerApi = createAdminApi('partner')
export const notificationApi = createAdminApi('notification')
export const fileApi = createAdminApi('file')
export const planApi = createAdminApi('marketing/plans')
export const couponApi = createAdminApi('marketing/coupons')
