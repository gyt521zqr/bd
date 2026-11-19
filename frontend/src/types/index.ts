/**
 * 类型统一导出
 */
export type { Result, PageQuery, PageResult } from './api'
export type { UserRole, UserInfo, LoginParams, RegisterParams, LoginResult } from './user'
export type { CourseItem, CourseDetail, Video, Course } from './course'
export type { ExamItem, ExamRecord, ExamAnswer, GradingTask, Exam, Question, ExamAnswerDetail, ExamStatus, QuestionType, ExamQuestion, ExamPaper, ExamMeta, ExamResultDetail } from './exam'
export type { OrderItem, PayForm, Order, OrderStatus, PayMethod, Coupon, CouponType, MembershipPlan } from './payment'

/** 兼容别名 */
export type LoginForm = { phone: string; password: string; loginType: 'password' | 'sms' | 'wechat' }
export type RegisterForm = { phone: string; smsCode: string; nickname: string; password: string }
export type ChangePasswordForm = { oldPassword: string; newPassword: string }
