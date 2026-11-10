// 验证新端点 GET /api/exam/paper/{paperId}/questions
// 通过 /api/auth/login 获取 token，再带鉴权请求。
import http from 'http'

const BASE = '127.0.0.1'
const PORT = 8080

function req(method, path, body, token) {
  return new Promise((resolve, reject) => {
    const data = body ? JSON.stringify(body) : null
    const r = http.request(
      { host: BASE, port: PORT, path, method, headers: {
          'Content-Type': 'application/json',
          ...(token ? { Authorization: 'Bearer ' + token } : {}),
        } },
      (res) => {
        let buf = ''
        res.on('data', (c) => (buf += c))
        res.on('end', () => resolve({ status: res.statusCode, body: buf }))
      }
    )
    r.on('error', reject)
    if (data) r.write(data)
    r.end()
  })
}

const login = await req('POST', '/api/auth/login', {
  username: 'admin',
  password: 'admin123',
  loginType: 'password',
})
console.log('LOGIN status:', login.status)
console.log('LOGIN body:', login.body.slice(0, 500))

let token = ''
try {
  const j = JSON.parse(login.body)
  const d = j.data || {}
  token = d.token || d.accessToken || d.jwt || (typeof d === 'string' ? d : '')
} catch (e) {}
console.log('TOKEN len:', token.length)

if (!token) {
  console.log('NO TOKEN — cannot test authed endpoint')
  process.exit(0)
}

for (const pid of [201, 203, 206]) {
  const r = await req('GET', `/api/exam/paper/${pid}/questions`, null, token)
  let summary = 'status=' + r.status
  try {
    const j = JSON.parse(r.body)
    const list = j.data || []
    summary += ` code=${j.code} count=${list.length}`
    const first = list[0]
    if (first) {
      summary += ` firstType=${first.type}`
      summary += ` optionsIsArray=${Array.isArray(first.options)}`
      summary += ` optsLen=${first.options ? first.options.length : 0}`
    }
    const codeQ = list.find((q) => q.type === 'code')
    if (codeQ) {
      summary += ` codeQ.templateCodeLen=${codeQ.templateCode ? codeQ.templateCode.length : 0}`
      summary += ` codeQ.language=${codeQ.language}`
    }
  } catch (e) {
    summary += ' parseErr=' + e.message
  }
  console.log(`paper ${pid}:`, summary)
}
