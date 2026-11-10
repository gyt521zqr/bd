/**
 * 表单验证规则
 * 提供常用的表单校验函数
 */

/** 手机号正则 */
const PHONE_REG = /^1[3-9]\d{9}$/

/** 邮箱正则 */
const EMAIL_REG = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/

/**
 * 验证手机号
 */
export function validatePhone(phone: string): boolean {
  return PHONE_REG.test(phone)
}

/**
 * 验证邮箱
 */
export function validateEmail(email: string): boolean {
  return EMAIL_REG.test(email)
}

/**
 * 验证密码强度（至少6位，包含字母和数字）
 */
export function validatePassword(password: string): boolean {
  if (password.length < 6) return false
  const hasLetter = /[a-zA-Z]/.test(password)
  const hasNumber = /\d/.test(password)
  return hasLetter && hasNumber
}

/**
 * 验证昵称（2-20个字符，不含特殊符号）
 */
export function validateNickname(nickname: string): boolean {
  if (nickname.length < 2 || nickname.length > 20) return false
  return /^[a-zA-Z0-9一-龥_]+$/.test(nickname)
}

/**
 * 验证验证码（6位数字）
 */
export function validateSmsCode(code: string): boolean {
  return /^\d{6}$/.test(code)
}
