import { defineConfig, type Plugin } from 'vite'
import vue from '@vitejs/plugin-vue'
import path from 'path'
import fs from 'fs'
import zlib from 'zlib'
import crypto from 'crypto'
import https from 'https'
import { createRequire } from 'module'

const require = createRequire(import.meta.url)
const { HttpsProxyAgent } = require('https-proxy-agent') as any

// Scratch 资产本地服务插件
// 优先从本地 assets 目录提供资产，缺失时动态生成占位符
function scratchAssetProxyPlugin(): Plugin {
  const ASSETS_DIR = path.resolve(__dirname, 'public/scratch/assets')
  const SPRITES_JSON = path.resolve(__dirname, 'public/scratch/libraries/sprites.json')
  const BACKDROPS_JSON = path.resolve(__dirname, 'public/scratch/libraries/backdrops.json')

  // 加载库 JSON 文件，建立 md5ext -> {name, tags} 映射
  const assetInfoMap = new Map<string, { name: string; tags: string[]; type: 'sprite' | 'backdrop' }>()
  try {
    const sprites = JSON.parse(fs.readFileSync(SPRITES_JSON, 'utf-8'))
    for (const sprite of sprites) {
      for (const costume of (sprite.costumes || [])) {
        if (costume.md5ext) {
          assetInfoMap.set(costume.md5ext, { name: sprite.name, tags: sprite.tags || [], type: 'sprite' })
        }
      }
    }
  } catch (e) { /* ignore */ }
  try {
    const backdrops = JSON.parse(fs.readFileSync(BACKDROPS_JSON, 'utf-8'))
    for (const bd of backdrops) {
      if (bd.md5ext) {
        assetInfoMap.set(bd.md5ext, { name: bd.name, tags: bd.tags || [], type: 'backdrop' })
      }
    }
  } catch (e) { /* ignore */ }

  // 根据标签获取分类颜色
  const TAG_COLORS: Record<string, string> = {
    animals: '#4CAF50', animal: '#4CAF50',
    people: '#2196F3', person: '#2196F3',
    fantasy: '#9C27B0',
    dance: '#E91E63', dancing: '#E91E63',
    music: '#FF9800',
    sports: '#F44336', sport: '#F44336',
    food: '#FFC107',
    fashion: '#E91E63',
    letters: '#607D8B', letter: '#607D8B',
    outdoors: '#4CAF50',
    indoors: '#795548',
    nature: '#4CAF50',
    space: '#3F51B5',
    underwater: '#00BCD4',
    point: '#FF5722',
  }

  function colorForTags(tags: string[]): string {
    for (const tag of tags) {
      const lower = tag.toLowerCase()
      if (TAG_COLORS[lower]) return TAG_COLORS[lower]
    }
    return '#78909C' // 默认灰色
  }

  function colorForName(name: string): string {
    const hash = crypto.createHash('md5').update(name).digest('hex')
    const colors = ['#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', '#FFEAA7',
      '#DDA0DD', '#98D8C8', '#F7DC6F', '#BB8FCE', '#85C1E9']
    const idx = parseInt(hash.substring(0, 4), 16) % colors.length
    return colors[idx]
  }

  // 根据分类生成简单图标 SVG path
  function iconForTags(tags: string[], cx: number, cy: number, r: number): string {
    const tagStr = (tags || []).join(' ').toLowerCase()
    if (/animal|cat|dog|fish|bird|bear|horse|rabbit|frog|duck|chick|cow|pig|mouse|rat|snake|turtle|penguin|elephant|giraffe|lion|tiger|monkey|zebra|shark|octopus|jellyfish|crab|owl|bat|bee|butterfly|spider|squirrel|fox|wolf|deer|llama|panda|kangaroo|sheep|goat|camel|gorilla|hippo|rhino|kangaroo/.test(tagStr)) {
      // 动物图标 - 简单的猫脸
      return `<circle cx="${cx}" cy="${cy}" r="${r}" fill="rgba(255,255,255,0.9)"/>` +
        `<polygon points="${cx-r*0.6},${cy-r*0.7} ${cx-r*0.3},${cy-r*0.3} ${cx-r*0.8},${cy-r*0.2}" fill="rgba(255,255,255,0.9)"/>` +
        `<polygon points="${cx+r*0.6},${cy-r*0.7} ${cx+r*0.3},${cy-r*0.3} ${cx+r*0.8},${cy-r*0.2}" fill="rgba(255,255,255,0.9)"/>` +
        `<circle cx="${cx-r*0.3}" cy="${cy-r*0.1}" r="${r*0.12}" fill="#333"/>` +
        `<circle cx="${cx+r*0.3}" cy="${cy-r*0.1}" r="${r*0.12}" fill="#333"/>` +
        `<path d="M${cx-r*0.2} ${cy+r*0.2} L${cx} ${cy+r*0.35} L${cx+r*0.2} ${cy+r*0.2}" fill="none" stroke="#333" stroke-width="1.5"/>`
    }
    if (/people|person|human|boy|girl|man|woman|kid|child|danc/.test(tagStr)) {
      // 人物图标 - 简单的人形
      return `<circle cx="${cx}" cy="${cy-r*0.4}" r="${r*0.3}" fill="rgba(255,255,255,0.9)"/>` +
        `<rect x="${cx-r*0.25}" y="${cy-r*0.1}" width="${r*0.5}" height="${r*0.7}" rx="${r*0.1}" fill="rgba(255,255,255,0.9)"/>`
    }
    if (/fantasy|magic|witch|wizard|dragon|unicorn|fairy|elf|knight|prince|princess|mermaid|goblin|centaur|griffin/.test(tagStr)) {
      // 奇幻图标 - 星星
      return `<polygon points="${cx},${cy-r} ${cx+r*0.3},${cy-r*0.3} ${cx+r},${cy-r*0.3} ${cx+r*0.45},${cy+r*0.1} ${cx+r*0.6},${cy+r} ${cx},${cy+r*0.4} ${cx-r*0.6},${cy+r} ${cx-r*0.45},${cy+r*0.1} ${cx-r},${cy-r*0.3} ${cx-r*0.3},${cy-r*0.3}" fill="rgba(255,255,255,0.9)"/>`
    }
    if (/music|instrument|guitar|drum|piano|trumpet|saxophone|microphone|speaker/.test(tagStr)) {
      // 音乐图标 - 音符
      return `<circle cx="${cx-r*0.3}" cy="${cy+r*0.4}" r="${r*0.25}" fill="rgba(255,255,255,0.9)"/>` +
        `<rect x="${cx+r*0.05}" y="${cy-r*0.6}" width="${r*0.15}" height="${r}" fill="rgba(255,255,255,0.9)"/>` +
        `<path d="M${cx+r*0.05} ${cy-r*0.6} Q${cx+r*0.4} ${cy-r*0.4} ${cx+r*0.2} ${cy-r*0.1}" fill="none" stroke="rgba(255,255,255,0.9)" stroke-width="3"/>`
    }
    if (/food|fruit|apple|banana|cake|donut|pizza|taco|bread|milk|egg|strawberry|watermelon|orange|cherry/.test(tagStr)) {
      // 食物图标 - 苹果
      return `<circle cx="${cx}" cy="${cy+r*0.1}" r="${r*0.55}" fill="rgba(255,255,255,0.9)"/>` +
        `<path d="M${cx} ${cy-r*0.4} L${cx+r*0.1} ${cy-r*0.65}" stroke="rgba(255,255,255,0.9)" stroke-width="2" fill="none"/>`
    }
    if (/ball|sport|soccer|basketball|baseball|football|tennis|beachball/.test(tagStr)) {
      // 运动图标 - 球
      return `<circle cx="${cx}" cy="${cy}" r="${r*0.6}" fill="rgba(255,255,255,0.9)"/>` +
        `<path d="M${cx-r*0.6} ${cy} Q${cx} ${cy-r*0.3} ${cx+r*0.6} ${cy}" fill="none" stroke="#333" stroke-width="1"/>` +
        `<path d="M${cx} ${cy-r*0.6} Q${cx+r*0.3} ${cy} ${cx} ${cy+r*0.6}" fill="none" stroke="#333" stroke-width="1"/>`
    }
    // 默认图标 - 简单的圆形
    return `<circle cx="${cx}" cy="${cy}" r="${r*0.5}" fill="rgba(255,255,255,0.7)"/>`
  }

  // 生成精灵 SVG（使用库中的名称和分类信息）
  function generateSpriteSvg(md5ext: string): Buffer {
    const info = assetInfoMap.get(md5ext)
    const name = info?.name || md5ext.substring(0, 8)
    const tags = info?.tags || []
    const color = tags.length > 0 ? colorForTags(tags) : colorForName(name)
    const w = 80, h = 80
    const iconSvg = iconForTags(tags, w / 2, h / 2 - 5, 20)
    // 转义名称中的特殊字符
    const safeName = name.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;')
    return Buffer.from(
      `<svg xmlns="http://www.w3.org/2000/svg" width="${w}" height="${h}" viewBox="0 0 ${w} ${h}">` +
      `<rect width="${w}" height="${h}" fill="${color}" rx="10"/>` +
      iconSvg +
      `<text x="${w / 2}" y="${h - 6}" text-anchor="middle" font-family="sans-serif" font-size="9" font-weight="bold" fill="white" style="text-shadow:0 1px 2px rgba(0,0,0,0.5)">${safeName.substring(0, 12)}</text>` +
      `</svg>`
    )
  }

  // 生成背景 SVG（使用库中的名称和分类信息）
  function generateBackdropSvg(md5ext: string): Buffer {
    const info = assetInfoMap.get(md5ext)
    const name = info?.name || md5ext.substring(0, 8)
    const tags = info?.tags || []
    const color = tags.length > 0 ? colorForTags(tags) : colorForName(name)
    const w = 480, h = 360
    const safeName = name.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;')
    return Buffer.from(
      `<svg xmlns="http://www.w3.org/2000/svg" width="${w}" height="${h}" viewBox="0 0 ${w} ${h}">` +
      `<defs><linearGradient id="g" x1="0%" y1="0%" x2="0%" y2="100%">` +
      `<stop offset="0%" style="stop-color:${color};stop-opacity:1"/>` +
      `<stop offset="100%" style="stop-color:${color};stop-opacity:0.5"/>` +
      `</linearGradient></defs>` +
      `<rect width="${w}" height="${h}" fill="url(#g)"/>` +
      `<circle cx="${w * 0.8}" cy="${h * 0.2}" r="40" fill="rgba(255,255,255,0.2)"/>` +
      `<rect x="0" y="${h * 0.75}" width="${w}" height="${h * 0.25}" fill="rgba(0,0,0,0.15)"/>` +
      `<text x="${w / 2}" y="${h / 2}" text-anchor="middle" font-family="sans-serif" font-size="28" font-weight="bold" fill="white" opacity="0.8" style="text-shadow:0 2px 4px rgba(0,0,0,0.3)">${safeName}</text>` +
      `</svg>`
    )
  }

  // 生成最小 PNG 文件（纯色矩形，使用资产信息确定颜色）
  function generatePng(md5ext: string, width = 80, height = 80): Buffer {
    const info = assetInfoMap.get(md5ext)
    const name = info?.name || md5ext.substring(0, 8)
    const tags = info?.tags || []
    const color = tags.length > 0 ? colorForTags(tags) : colorForName(name)
    // 解析颜色
    const r = parseInt(color.substring(1, 3), 16)
    const g = parseInt(color.substring(3, 5), 16)
    const b = parseInt(color.substring(5, 7), 16)

    // PNG 签名
    const signature = Buffer.from([137, 80, 78, 71, 13, 10, 26, 10])

    // IHDR chunk
    const ihdr = Buffer.alloc(13)
    ihdr.writeUInt32BE(width, 0)
    ihdr.writeUInt32BE(height, 4)
    ihdr[8] = 8  // bit depth
    ihdr[9] = 2  // color type (RGB)
    ihdr[10] = 0 // compression
    ihdr[11] = 0 // filter
    ihdr[12] = 0 // interlace

    // IDAT chunk - 图像数据
    const rawData = Buffer.alloc((width * 3 + 1) * height)
    let offset = 0
    for (let y = 0; y < height; y++) {
      rawData[offset++] = 0 // filter byte
      for (let x = 0; x < width; x++) {
        rawData[offset++] = r
        rawData[offset++] = g
        rawData[offset++] = b
      }
    }
    const compressedData = zlib.deflateSync(rawData)

    // IEND chunk
    const iend = Buffer.alloc(0)

    // 组装 PNG
    function makeChunk(type: string, data: Buffer): Buffer {
      const typeBuf = Buffer.from(type, 'ascii')
      const lenBuf = Buffer.alloc(4)
      lenBuf.writeUInt32BE(data.length, 0)
      const crcData = Buffer.concat([typeBuf, data])
      const crc = Buffer.alloc(4)
      crc.writeUInt32BE(crc32(crcData) >>> 0, 0)
      return Buffer.concat([lenBuf, typeBuf, data, crc])
    }

    function crc32(buf: Buffer): number {
      let crc = 0xFFFFFFFF
      const table: number[] = []
      for (let n = 0; n < 256; n++) {
        let c = n
        for (let k = 0; k < 8; k++) {
          if (c & 1) c = 0xEDB88320 ^ (c >>> 1)
          else c = c >>> 1
        }
        table[n] = c
      }
      for (let i = 0; i < buf.length; i++) {
        crc = table[(crc ^ buf[i]) & 0xFF] ^ (crc >>> 8)
      }
      return crc ^ 0xFFFFFFFF
    }

    return Buffer.concat([
      signature,
      makeChunk('IHDR', ihdr),
      makeChunk('IDAT', compressedData),
      makeChunk('IEND', iend)
    ])
  }

  // 生成简单的 WAV 音效
  function generateWav(md5ext: string): Buffer {
    const sampleRate = 22050
    const duration = 0.3
    const numSamples = Math.floor(sampleRate * duration)
    const frequency = 440 + (md5ext.charCodeAt(0) % 10) * 50

    const header = Buffer.alloc(44)
    const dataSize = numSamples * 2
    const totalSize = 44 + dataSize

    header.write('RIFF', 0)
    header.writeUInt32LE(totalSize - 8, 4)
    header.write('WAVE', 8)
    header.write('fmt ', 12)
    header.writeUInt32LE(16, 16)
    header.writeUInt16LE(1, 20)
    header.writeUInt16LE(1, 22)
    header.writeUInt32LE(sampleRate, 24)
    header.writeUInt32LE(sampleRate * 2, 28)
    header.writeUInt16LE(2, 32)
    header.writeUInt16LE(16, 34)
    header.write('data', 36)
    header.writeUInt32LE(dataSize, 40)

    const audioData = Buffer.alloc(dataSize)
    for (let i = 0; i < numSamples; i++) {
      const t = i / sampleRate
      const envelope = Math.exp(-t * 3)
      const sample = Math.floor(Math.sin(2 * Math.PI * frequency * t) * envelope * 32767 * 0.5)
      audioData.writeInt16LE(sample, i * 2)
    }

    return Buffer.concat([header, audioData])
  }

  // 资产内存缓存：避免重复从 CDN 下载
  const assetCache = new Map<string, { body: Buffer; contentType: string }>()
  // 正在下载的资产：避免并发重复请求
  const pendingFetches = new Map<string, Promise<{ body: Buffer; contentType: string } | null>>()

  // 并发下载限制器：防止同时发起过多 S3 请求导致连接超时
  const MAX_CONCURRENT_DOWNLOADS = 8
  let activeDownloads = 0
  const downloadQueue: (() => void)[] = []

  function acquireDownloadSlot(): Promise<void> {
    if (activeDownloads < MAX_CONCURRENT_DOWNLOADS) {
      activeDownloads++
      return Promise.resolve()
    }
    return new Promise<void>((resolve) => {
      downloadQueue.push(() => {
        activeDownloads++
        resolve()
      })
    })
  }

  function releaseDownloadSlot() {
    activeDownloads--
    if (downloadQueue.length > 0) {
      const next = downloadQueue.shift()!
      next()
    }
  }

  // 创建代理 Agent（如果配置了 HTTPS_PROXY 环境变量）
  // 开发环境中代理可能使用自签名证书，需要跳过 TLS 验证
  // 生产环境不使用代理，直接访问 CDN，不受此设置影响
  const proxyUrl = process.env.HTTPS_PROXY || process.env.https_proxy || ''
  const proxyAgent = proxyUrl ? new HttpsProxyAgent(proxyUrl, { rejectUnauthorized: false }) : undefined

  // 使用 Node.js https 模块通过代理下载资产（带超时）
  function httpsGet(url: string, agent: any, timeoutMs = 15000): Promise<{ body: Buffer; statusCode: number; headers: any }> {
    return new Promise((resolve, reject) => {
      const options: https.RequestOptions = { agent, rejectUnauthorized: false }
      const req = https.get(url, options, (res) => {
        // 处理重定向
        if (res.statusCode === 301 || res.statusCode === 302 || res.statusCode === 307 || res.statusCode === 308) {
          const location = res.headers.location
          if (location) {
            res.resume()
            httpsGet(location, agent, timeoutMs).then(resolve).catch(reject)
            return
          }
        }
        const chunks: Buffer[] = []
        res.on('data', (chunk) => chunks.push(Buffer.from(chunk)))
        res.on('end', () => resolve({ body: Buffer.concat(chunks), statusCode: res.statusCode || 0, headers: res.headers }))
        res.on('error', reject)
      })
      req.on('error', reject)
      req.setTimeout(timeoutMs, () => {
        req.destroy(new Error(`Request timeout after ${timeoutMs}ms: ${url}`))
      })
    })
  }

  // 从 Scratch 资产服务器下载真实资产
  // 使用 S3 直链（通过代理可访问），带并发限制和超时
  // 注意：此代理仅用于开发环境，生产环境浏览器直接访问 CDN
  async function fetchAssetFromCDN(md5ext: string, ext: string): Promise<{ body: Buffer; contentType: string } | null> {
    if (pendingFetches.has(md5ext)) {
      return pendingFetches.get(md5ext)!
    }

    const promise = (async () => {
      await acquireDownloadSlot()
      try {
        // S3 直链格式：https://scratchassets.s3.amazonaws.com/{md5ext}
        const s3Url = `https://scratchassets.s3.amazonaws.com/${md5ext}`
        const { body, statusCode } = await httpsGet(s3Url, proxyAgent)
        if (statusCode !== 200 || body.length === 0) {
          console.log(`[scratch-proxy] S3 fetch failed for ${md5ext}: status ${statusCode}, body ${body.length}`)
          return null
        }

        let contentType = 'application/octet-stream'
        if (ext === 'svg') contentType = 'image/svg+xml'
        else if (ext === 'png') contentType = 'image/png'
        else if (ext === 'wav') contentType = 'audio/wav'
        else if (ext === 'mp3') contentType = 'audio/mpeg'

        // 缓存到内存 - 直接使用 CDN 返回的真实资产内容
        assetCache.set(md5ext, { body, contentType })

        // 异步写入本地文件（不阻塞响应）
        try {
          if (!fs.existsSync(ASSETS_DIR)) {
            fs.mkdirSync(ASSETS_DIR, { recursive: true })
          }
          fs.writeFile(path.join(ASSETS_DIR, md5ext), body, () => {})
        } catch {}

        return { body, contentType }
      } catch (err: any) {
        console.error(`[scratch-proxy] S3 fetch error for ${md5ext}: ${err.message}`)
        return null
      } finally {
        releaseDownloadSlot()
        pendingFetches.delete(md5ext)
      }
    })()

    pendingFetches.set(md5ext, promise)
    return promise
  }

  // 从本地文件获取资产
  function getLocalAsset(url: string): { body: Buffer; contentType: string } | null {
    // URL 格式: /{md5ext}/get/ 或 /{md5ext}/get//
    const match = url.match(/^\/([a-f0-9]+\.(svg|png|wav|mp3))\/get\/+$/i)
    if (!match) return null

    const md5ext = match[1]
    const ext = match[2].toLowerCase()

    // 检查内存缓存
    if (assetCache.has(md5ext)) {
      return assetCache.get(md5ext)!
    }

    // 尝试从本地文件加载
    const localPath = path.join(ASSETS_DIR, md5ext)
    if (fs.existsSync(localPath)) {
      const body = fs.readFileSync(localPath)
      let contentType = 'application/octet-stream'
      if (ext === 'svg') contentType = 'image/svg+xml'
      else if (ext === 'png') contentType = 'image/png'
      else if (ext === 'wav') contentType = 'audio/wav'
      else if (ext === 'mp3') contentType = 'audio/mpeg'

      const result = { body, contentType }
      assetCache.set(md5ext, result)
      return result
    }

    return null
  }

  // 生成占位符资产
  function generatePlaceholder(url: string): { body: Buffer; contentType: string } | null {
    const match = url.match(/^\/([a-f0-9]+\.(svg|png|wav|mp3))\/get\/+$/i)
    if (!match) return null

    const md5ext = match[1]
    const ext = match[2].toLowerCase()

    // 查找资产类型，区分精灵和背景
    const info = assetInfoMap.get(md5ext)
    const isBackdrop = info?.type === 'backdrop'

    if (ext === 'svg' || ext === 'png') {
      // 对于 SVG 和 PNG 资产，都返回带名称和图标的 SVG 占位符
      // 这样在 CDN 不可用时，角色和背景库仍能显示正确的名称和分类图标
      // 生产环境中 CDN 可直接访问，不会触发此占位符逻辑
      if (isBackdrop) {
        return { body: generateBackdropSvg(md5ext), contentType: 'image/svg+xml' }
      }
      return { body: generateSpriteSvg(md5ext), contentType: 'image/svg+xml' }
    } else if (ext === 'wav') {
      return { body: generateWav(md5ext), contentType: 'audio/wav' }
    } else if (ext === 'mp3') {
      return { body: generateWav(md5ext), contentType: 'audio/mpeg' }
    }

    return null
  }

  return {
    name: 'scratch-asset-proxy',
    configureServer(server) {
      // Catch-all logger for asset-related requests
      server.middlewares.use((req, res, next) => {
        const url = req.url || ''
        if (url.includes('internalapi') || url.includes('/get/') || url.match(/[a-f0-9]{32}\./)) {
          console.log(`[scratch-proxy] REQUEST: ${req.method} ${url}`)
        }
        next()
      })

      server.middlewares.use('/internalapi/asset', async (req, res) => {
        try {
          // Strip query string before URL matching (e.g. /xx.svg/get/?_t=123 -> /xx.svg/get/)
          const rawUrl = req.url || ''
          const url = rawUrl.split('?')[0]
          console.log(`[scratch-proxy] ASSET REQUEST: ${rawUrl}`)

          // 1. 先尝试从本地缓存/文件获取（同步，快速路径）
          const localResult = getLocalAsset(url)
          if (localResult) {
            console.log(`[scratch-proxy] LOCAL HIT: ${url} (${localResult.contentType}, ${localResult.body.length} bytes)`)
            res.writeHead(200, {
              'Content-Type': localResult.contentType,
              'Cache-Control': 'public, max-age=3600',
              'Access-Control-Allow-Origin': '*',
            })
            res.end(localResult.body)
            return
          }

          // 2. 从 URL 中提取资产信息
          const match = url.match(/^\/([a-f0-9]+\.(svg|png|wav|mp3))\/get\/+$/i)
          if (!match) {
            res.writeHead(404, { 'Content-Type': 'text/plain' })
            res.end('Asset not found')
            return
          }

          const md5ext = match[1]
          const ext = match[2].toLowerCase()

          // 3. 尝试从 Scratch CDN 下载真实资产
          const cdnResult = await fetchAssetFromCDN(md5ext, ext)
          if (cdnResult) {
            console.log(`[scratch-proxy] CDN hit: ${md5ext} (${cdnResult.contentType}, ${cdnResult.body.length} bytes)`)
            res.writeHead(200, {
              'Content-Type': cdnResult.contentType,
              'Cache-Control': 'public, max-age=3600',
              'Access-Control-Allow-Origin': '*',
            })
            res.end(cdnResult.body)
            return
          }

          // 4. CDN 下载失败，生成占位符
          // 注意：占位符不缓存，防止浏览器缓存旧的占位符响应
          // 当 CDN 恢复后，浏览器会重新请求获取真实资产
          console.log(`[scratch-proxy] CDN miss, generating placeholder: ${md5ext}`)
          const placeholder = generatePlaceholder(url)
          if (placeholder) {
            res.writeHead(200, {
              'Content-Type': placeholder.contentType,
              'Cache-Control': 'no-cache, no-store, must-revalidate',
              'Access-Control-Allow-Origin': '*',
            })
            res.end(placeholder.body)
          } else {
            res.writeHead(404, { 'Content-Type': 'text/plain', 'Cache-Control': 'no-cache' })
            res.end('Asset not found')
          }
        } catch (err: any) {
          console.error(`[scratch-asset-proxy] Error: ${req.url}`, err.message)
          res.writeHead(500, { 'Content-Type': 'text/plain' })
          res.end(`Asset error: ${err.message}`)
        }
      })
    },
  }
}

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue(), scratchAssetProxyPlugin()],
  base: '/',
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
    watch: {
      ignored: [
        '**/public/scratch/**',
        '**/public/scratchjr/**',
        '**/public/goc/**',
        '**/public/pyodide/**',
        '**/public/cpp/**',
        '**/node_modules/**',
      ],
    },
  },
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          'element-plus': ['element-plus'],
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          'icons': ['@element-plus/icons-vue'],
        },
        entryFileNames: 'js/[name]-[hash].js',
        chunkFileNames: 'js/[name]-[hash].js',
        assetFileNames: (assetInfo) => {
          const info = assetInfo.name || ''
          if (/\.css$/.test(info)) return 'css/[name]-[hash][extname]'
          if (/\.(png|jpe?g|gif|svg|webp|ico)$/.test(info)) return 'images/[name]-[hash][extname]'
          if (/\.(woff2?|eot|ttf|otf)$/.test(info)) return 'fonts/[name]-[hash][extname]'
          return 'assets/[name]-[hash][extname]'
        },
      },
    },
    cssCodeSplit: true,
    sourcemap: false,
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,
        drop_debugger: true,
      },
    },
    chunkSizeWarningLimit: 500,
  },
  css: {
    preprocessorOptions: {
      scss: {
        api: 'modern-compiler',
        additionalData: (source: string, fp: string) => {
          // 避免在 variables.scss / mixins.scss 自身中注入 @use 导致循环引用
          if (fp.endsWith('variables.scss') || fp.endsWith('mixins.scss')) {
            return source
          }
          return `@use "@/assets/styles/variables.scss" as *; @use "@/assets/styles/mixins.scss" as *;\n${source}`
        },
      },
    },
  },
})
