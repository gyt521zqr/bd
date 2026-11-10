// 题库种子生成器：用 esbuild 抽取 frontend/src/mock/examQuestions.ts 的真实数据，
// 生成 exam_question / exam_paper(201-218) / exam_paper_question 的 INSERT SQL。
// 以 TS 题目对象引用去重，保证相同对象只插一条、并可被多套试卷引用。
import { build } from '../frontend/node_modules/esbuild/lib/main.js'
import { fileURLToPath } from 'url'
import path from 'path'

const root = path.resolve(path.dirname(fileURLToPath(import.meta.url)), '..')
const src = path.join(root, 'frontend/src/mock/examQuestions.ts')
const out = path.join(root, 'scripts/.examq.cjs')

await build({
  entryPoints: [src],
  bundle: true,
  format: 'cjs',
  platform: 'node',
  outfile: out,
  logLevel: 'silent',
})

const m = await import('file://' + out)
const { examPaperList, getExamQuestions } = m

// 试卷 id 范围（前端题库中心 201-218）
const paperIds = examPaperList.map((p) => p.id)
console.log('paperIds:', paperIds.join(','))
console.log('paper count:', paperIds.length)

// 以对象引用去重收集题目；DB id 优先复用前端 TS 题号（保证错题本/答案落库与前端 mock 同源对齐），
// 仅当题号冲突（同一 id 不同内容，如 1061/1062）时改用合成 id（900001+）。
const seen = new Map() // objRef -> dbId
const usedDbIds = new Set()
const questions = [] // {dbId, type, title, options, answer, analysis, score, language, templateCode}
let synth = 900001

const paperLinks = [] // {paperId, questionId, sortOrder, type, language, templateCode, score}

for (const p of examPaperList) {
  const qs = getExamQuestions(p.id)
  if (!Array.isArray(qs)) {
    console.error('NO QUESTIONS for paper', p.id)
    continue
  }
  qs.forEach((q, idx) => {
    let dbId = seen.get(q)
    if (dbId === undefined) {
      dbId = q.id
      if (usedDbIds.has(dbId)) dbId = synth++
      usedDbIds.add(dbId)
      seen.set(q, dbId)
      questions.push({
        dbId,
        type: q.type,
        title: q.title,
        options: q.options ? JSON.stringify(q.options) : null,
        answer: q.answer ?? null,
        analysis: q.analysis ?? null,
        score: q.score ?? 0,
        language: q.language ?? null,
        templateCode: q.templateCode ?? null,
      })
    }
    paperLinks.push({
      paperId: p.id,
      questionId: dbId,
      sortOrder: idx + 1,
      type: q.type,
      language: q.language ?? null,
      templateCode: q.templateCode ?? null,
      score: q.score ?? 0,
    })
  })
}

console.log('unique questions:', questions.length)
console.log('remapped (id collision) count:', synth - 900001)
console.log('paper-question links:', paperLinks.length)

// 校验题型分布
const typeCount = {}
for (const q of questions) typeCount[q.type] = (typeCount[q.type] || 0) + 1
console.log('type distribution:', JSON.stringify(typeCount))

// 校验每套题数是否与前端 questionCount 一致
const mismatch = []
const perPaper = {}
for (const l of paperLinks) perPaper[l.paperId] = (perPaper[l.paperId] || 0) + 1
for (const p of examPaperList) {
  if (perPaper[p.id] !== p.questionCount) {
    mismatch.push(`paper ${p.id}: DB=${perPaper[p.id]} vs meta=${p.questionCount}`)
  }
}
console.log('questionCount mismatch:', mismatch.length ? mismatch.join('; ') : 'NONE')

// ---- 生成 SQL ----
function esc(v) {
  if (v === null || v === undefined) return 'NULL'
  return "'" + String(v).replace(/\\/g, '\\\\').replace(/'/g, "\\'").replace(/\n/g, '\\n').replace(/\r/g, '\\r').replace(/\t/g, '\\t') + "'"
}

let sql = '-- 题库种子（由 scripts/gen-exam-questions-seed.mjs 生成，请勿手改）\n'
sql += '-- 幂等：先清掉 201-218 区间再插入\n'
sql += 'SET NAMES utf8mb4;\n'
sql += 'SET FOREIGN_KEY_CHECKS = 0;\n'
sql += 'DELETE FROM exam_paper_question WHERE paper_id BETWEEN 201 AND 218;\n'
sql += 'DELETE FROM exam_question;\n'
sql += 'DELETE FROM exam_paper WHERE id BETWEEN 201 AND 218;\n\n'

sql += '-- 试卷元信息（题库中心 201-218）\n'
sql += 'INSERT INTO exam_paper (id, paper_name, exam_id, total_score, pass_score, question_count) VALUES\n'
sql += examPaperList.map((p) =>
  `(${p.id}, ${esc(p.title)}, NULL, ${p.totalScore}, ${p.passScore}, ${p.questionCount})`
).join(',\n') + ';\n\n'

sql += '-- 题目内容（exam_question）\n'
sql += 'INSERT INTO exam_question (id, type, title, options, answer, analysis, score, language, template_code) VALUES\n'
sql += questions.map((q) =>
  `(${q.dbId}, ${esc(q.type)}, ${esc(q.title)}, ${esc(q.options)}, ${esc(q.answer)}, ${esc(q.analysis)}, ${q.score}, ${esc(q.language)}, ${esc(q.templateCode)})`
).join(',\n') + ';\n\n'

sql += '-- 试卷-题目关联（exam_paper_question）\n'
sql += 'INSERT INTO exam_paper_question (paper_id, question_id, question_type, language, template_code, score, sort_order) VALUES\n'
sql += paperLinks.map((l) =>
  `(${l.paperId}, ${l.questionId}, ${typeToInt(l.type)}, ${esc(l.language)}, ${esc(l.templateCode)}, ${l.score}, ${l.sortOrder})`
).join(',\n') + ';\n'
sql += 'SET FOREIGN_KEY_CHECKS = 1;\n'

function typeToInt(t) {
  const map = { choice: 1, fill: 2, judge: 3, code: 4, scratch: 5, scratchjr: 6 }
  return map[t] || 0
}

const sqlPath = path.join(root, 'scripts/seed_exam_questions.sql')
import fs from 'fs'
fs.writeFileSync(sqlPath, sql)
console.log('WROTE', sqlPath, 'bytes=', sql.length)
