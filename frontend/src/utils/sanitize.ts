/**
 * HTML 内容安全过滤（P1-24：防存储型 XSS）
 * 所有 v-html 渲染的富文本（资讯内容、题目描述等）必须先经此过滤。
 */
import DOMPurify from 'dompurify'

const ALLOWED_TAGS = [
  'p', 'br', 'hr', 'strong', 'em', 'b', 'i', 'u', 's', 'del', 'ins',
  'a', 'ul', 'ol', 'li', 'h1', 'h2', 'h3', 'h4', 'h5', 'h6',
  'img', 'pre', 'code', 'blockquote', 'span', 'div',
  'table', 'thead', 'tbody', 'tr', 'td', 'th',
]

const ALLOWED_ATTR = ['href', 'src', 'alt', 'title', 'target', 'rel', 'class', 'style']

/**
 * 过滤富文本 HTML，移除脚本/事件处理器/危险协议，仅保留安全标签与属性。
 */
export function sanitizeHtml(html: string): string {
  if (!html) return ''
  return DOMPurify.sanitize(html, {
    ALLOWED_TAGS,
    ALLOWED_ATTR,
    ALLOW_DATA_ATTR: false,
    // 链接只允许 http/https/mailto，杜绝 javascript: 等危险协议
    ALLOWED_URI_REGEXP: /^(?:(?:https?|mailto|tel):|[^a-z]|[a-z+.-]+(?:[^a-z+.-:]|$))/i,
  })
}
