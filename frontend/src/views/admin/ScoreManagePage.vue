<template>
  <!-- 成绩管理 -->
  <div class="score-manage-page">
    <div class="page-header">
      <h2>成绩管理</h2>
      <div class="header-actions">
        <el-button type="primary" title="暂未接入后端服务" @click="openAutoReviewDialog">
          <el-icon><Check /></el-icon>自动阅卷
        </el-button>
        <el-button
          type="warning"
          title="暂未接入后端服务"
          :disabled="selectedRows.length === 0 || !selectedRows.some(r => r.reviewStatus !== 'reviewed')"
          @click="handleBatchAutoReview"
        >
          批量自动阅卷
        </el-button>
        <el-button @click="handleBatchExport">批量导出成绩</el-button>
        <el-button type="primary" title="暂未接入后端服务" @click="handleBatchCertificate">批量生成证书</el-button>
      </div>
    </div>

    <el-alert
      title="功能提示"
      description="自动阅卷、批量自动阅卷、重新评测、证书生成（含批量）暂未接入后端服务，操作不会保存。"
      type="info"
      :closable="false"
      show-icon
      style="margin-bottom: 16px"
    />

    <!-- 统计卡片 -->
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-value">{{ stats.total }}</div>
        <div class="stat-label">参赛人数</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color: #165DFF;">{{ stats.avgScore }}</div>
        <div class="stat-label">平均分</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color: #FF9500;">{{ stats.maxScore }}</div>
        <div class="stat-label">最高分</div>
      </div>
      <div class="stat-card">
        <div class="stat-value" style="color: #36D399;">{{ stats.awardRate }}%</div>
        <div class="stat-label">获奖率</div>
      </div>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="searchForm.examName" placeholder="赛事名称" clearable style="width: 200px" />
      <el-input v-model="searchForm.studentName" placeholder="学生姓名" clearable style="width: 160px" />
      <el-input v-model="searchForm.school" placeholder="学校" clearable style="width: 180px" />
      <el-select v-model="searchForm.award" placeholder="奖项" clearable style="width: 140px">
        <el-option label="一等奖" value="一等奖" />
        <el-option label="二等奖" value="二等奖" />
        <el-option label="三等奖" value="三等奖" />
        <el-option label="优秀奖" value="优秀奖" />
        <el-option label="未获奖" value="未获奖" />
      </el-select>
      <el-select v-model="searchForm.reviewStatus" placeholder="阅卷状态" clearable style="width: 140px">
        <el-option label="已阅卷" value="reviewed" />
        <el-option label="未阅卷" value="unreviewed" />
        <el-option label="部分阅卷" value="partial" />
      </el-select>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="pagedList" stripe @selection-change="handleSelectionChange" v-loading="loading" empty-text="暂无数据">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="studentName" label="学生姓名" width="120" />
      <el-table-column prop="school" label="学校" min-width="160" />
      <el-table-column prop="grade" label="年级" width="100" />
      <el-table-column prop="examName" label="参赛赛事" min-width="160" />
      <el-table-column prop="score" label="得分" width="90">
        <template #default="{ row }">
          <span :style="{ color: row.score >= 90 ? '#36D399' : row.score >= 60 ? '#165DFF' : '#F53F3F', fontWeight: 600 }">{{ row.score }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="rank" label="排名" width="80">
        <template #default="{ row }">
          <span :class="['rank-badge', row.rank <= 3 ? 'top' : '']">{{ row.rank }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="award" label="奖项" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.award !== '未获奖'" type="success" size="small">{{ row.award }}</el-tag>
          <span v-else style="color: #999;">未获奖</span>
        </template>
      </el-table-column>
      <el-table-column prop="reviewStatus" label="阅卷状态" width="100">
        <template #default="{ row }">
          <el-tag v-if="row.reviewStatus === 'reviewed'" type="success" size="small">已阅卷</el-tag>
          <el-tag v-else-if="row.reviewStatus === 'partial'" type="warning" size="small">部分阅卷</el-tag>
          <el-tag v-else type="info" size="small">未阅卷</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="duration" label="用时(分)" width="90" />
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button size="small" text type="primary" @click="handleView(row)">查看详情</el-button>
          <el-button size="small" text type="primary" @click="handleEdit(row)">编辑成绩</el-button>
          <el-button size="small" text type="warning" title="暂未接入后端服务" @click="handleRejudge(row)">重新评测</el-button>
          <el-button size="small" text type="success" title="暂未接入后端服务" @click="handleCertificate(row)">生成证书</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="filteredList.length" v-model:page="pageNum" v-model:page-size="pageSize" />

    <!-- 成绩详情弹窗 -->
    <el-dialog v-model="detailDialogVisible" title="成绩详情" width="700px">
      <div v-if="detailRow" class="detail-body">
        <div class="detail-header">
          <div class="detail-info">
            <p><strong>学生：</strong>{{ detailRow.studentName }} | <strong>学校：</strong>{{ detailRow.school }} | <strong>年级：</strong>{{ detailRow.grade }}</p>
            <p><strong>赛事：</strong>{{ detailRow.examName }} | <strong>得分：</strong>{{ detailRow.score }} | <strong>排名：</strong>{{ detailRow.rank }} | <strong>奖项：</strong>{{ detailRow.award }}</p>
            <p><strong>用时：</strong>{{ detailRow.duration }} 分钟 | <strong>阅卷状态：</strong>
              <el-tag v-if="detailRow.reviewStatus === 'reviewed'" type="success" size="small">已阅卷</el-tag>
              <el-tag v-else-if="detailRow.reviewStatus === 'partial'" type="warning" size="small">部分阅卷</el-tag>
              <el-tag v-else type="info" size="small">未阅卷</el-tag>
            </p>
          </div>
        </div>
        <h4 class="section-title">各题得分明细</h4>
        <el-table :data="detailRow.details" size="small" border empty-text="暂无数据">
          <el-table-column prop="questionId" label="题号" width="70" />
          <el-table-column prop="type" label="题型" width="90">
            <template #default="{ row }">
              <span>{{ typeMap[row.type] }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="score" label="得分" width="80" />
          <el-table-column prop="fullScore" label="满分" width="80" />
          <el-table-column prop="answer" label="学生答案" min-width="160" show-overflow-tooltip />
          <el-table-column prop="correct" label="是否正确" width="90">
            <template #default="{ row }">
              <el-tag :type="row.correct ? 'success' : 'danger'" size="small">{{ row.correct ? '正确' : '错误' }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
        <template v-if="detailRow.code">
          <h4 class="section-title">编程题代码</h4>
          <pre class="code-block">{{ detailRow.code }}</pre>
        </template>
        <!-- 阅卷日志 -->
        <template v-if="detailRow.autoReviewLog && detailRow.autoReviewLog.length > 0">
          <h4 class="section-title">阅卷日志</h4>
          <div class="review-log-list">
            <div v-for="(log, idx) in detailRow.autoReviewLog" :key="idx" class="review-log-item">
              <div class="review-log-header">
                <el-tag :type="log.type === 'auto' ? 'primary' : 'warning'" size="small">
                  {{ log.type === 'auto' ? '自动阅卷' : '人工阅卷' }}
                </el-tag>
                <span class="review-log-time">{{ log.time }}</span>
              </div>
              <div class="review-log-body">
                <p><strong>阅卷规则：</strong>{{ log.rules }}</p>
                <p><strong>阅卷结果：</strong>{{ log.result }}</p>
                <p v-if="log.detail"><strong>详细：</strong>{{ log.detail }}</p>
              </div>
            </div>
          </div>
        </template>
        <template v-if="detailRow.reviewHistory && detailRow.reviewHistory.length > 0">
          <h4 class="section-title">批改记录</h4>
          <el-timeline>
            <el-timeline-item v-for="(item, idx) in detailRow.reviewHistory" :key="idx" :type="item.type">
              <p><strong>{{ item.time }}</strong> - {{ item.operator }}</p>
              <p>{{ item.action }}：{{ item.remark }}</p>
            </el-timeline-item>
          </el-timeline>
        </template>
      </div>
    </el-dialog>

    <!-- 编辑成绩弹窗 -->
    <el-dialog v-model="editDialogVisible" title="编辑成绩" width="500px" destroy-on-close>
      <el-form ref="editFormRef" :model="editForm" :rules="editRules" label-width="100px">
        <el-form-item label="学生姓名" prop="studentName">
          <el-input v-model="editForm.studentName" disabled />
        </el-form-item>
        <el-form-item label="得分" prop="score">
          <el-input-number v-model="editForm.score" :min="0" :max="200" style="width: 100%" />
        </el-form-item>
        <el-form-item label="排名" prop="rank">
          <el-input-number v-model="editForm.rank" :min="1" style="width: 100%" />
        </el-form-item>
        <el-form-item label="奖项" prop="award">
          <el-select v-model="editForm.award" placeholder="选择奖项" style="width: 100%">
            <el-option label="一等奖" value="一等奖" />
            <el-option label="二等奖" value="二等奖" />
            <el-option label="三等奖" value="三等奖" />
            <el-option label="优秀奖" value="优秀奖" />
            <el-option label="未获奖" value="未获奖" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input v-model="editForm.remark" type="textarea" :rows="3" placeholder="请输入批改备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitEdit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 自动阅卷配置弹窗 -->
    <el-dialog v-model="autoReviewDialogVisible" title="自动阅卷配置" width="600px" destroy-on-close>
      <el-form :model="autoReviewConfig" label-width="120px">
        <el-form-item label="选择试卷">
          <el-select v-model="autoReviewConfig.examId" placeholder="请选择要阅卷的试卷" style="width: 100%">
            <el-option
              v-for="exam in examOptions"
              :key="exam.id"
              :label="exam.name"
              :value="exam.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="阅卷范围">
          <el-radio-group v-model="autoReviewConfig.scope">
            <el-radio value="all">全部未阅卷</el-radio>
            <el-radio value="specified">指定学生</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item v-if="autoReviewConfig.scope === 'specified'" label="学生搜索">
          <el-select
            v-model="autoReviewConfig.studentIds"
            multiple
            filterable
            remote
            :remote-method="searchStudents"
            placeholder="输入学生姓名搜索"
            style="width: 100%"
          >
            <el-option
              v-for="s in studentSearchResults"
              :key="s.id"
              :label="`${s.studentName} - ${s.school}`"
              :value="s.id"
            />
          </el-select>
        </el-form-item>
        <el-divider content-position="left">阅卷规则配置</el-divider>
        <el-form-item label="客观题评分">
          <div class="rule-desc">
            <p>单选题 / 多选题 / 判断题：自动评分，答案完全匹配得分</p>
          </div>
        </el-form-item>
        <el-form-item label="填空题评分">
          <div class="rule-desc">
            <p>自动评分，忽略大小写和首尾空格</p>
          </div>
        </el-form-item>
        <el-form-item label="编程题策略">
          <el-select v-model="autoReviewConfig.programStrategy" placeholder="请选择编程题评分策略" style="width: 100%">
            <el-option label="仅编译通过（编译通过得30%分数）" value="compile_only" />
            <el-option label="通过全部测试用例（通过比例 x 满分）" value="all_tests" />
            <el-option label="通过部分测试用例（通过比例 x 满分）" value="partial_tests" />
            <el-option label="跳过编程题（0分，等待人工批改）" value="skip" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="autoReviewDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="startAutoReview">开始阅卷</el-button>
      </template>
    </el-dialog>

    <!-- 阅卷进度弹窗 -->
    <el-dialog v-model="reviewProgressDialogVisible" title="自动阅卷进度" width="700px" :close-on-click-modal="false" :close-on-press-escape="false">
      <div class="review-progress-body">
        <!-- 进度条 -->
        <div class="progress-section">
          <div class="progress-header">
            <span>阅卷进度</span>
            <span class="progress-count">{{ reviewProgress.current }} / {{ reviewProgress.total }}</span>
          </div>
          <el-progress :percentage="reviewProgress.percentage" :stroke-width="20" :text-inside="true" :status="reviewProgress.status" />
        </div>

        <!-- 实时统计 -->
        <div class="realtime-stats" v-if="reviewProgress.current > 0">
          <h4 class="section-title">实时统计</h4>
          <div class="stat-row">
            <span class="stat-label">客观题正确率：</span>
            <el-progress :percentage="reviewProgress.objectiveAccuracy" :stroke-width="14" :text-inside="true" status="success" style="flex: 1;" />
          </div>
          <div class="stat-row">
            <span class="stat-label">填空题正确率：</span>
            <el-progress :percentage="reviewProgress.fillAccuracy" :stroke-width="14" :text-inside="true" status="success" style="flex: 1;" />
          </div>
          <div class="stat-row">
            <span class="stat-label">编程题通过率：</span>
            <el-progress :percentage="reviewProgress.programPassRate" :stroke-width="14" :text-inside="true" :color="reviewProgress.programPassRate >= 60 ? '#36D399' : '#FF9500'" style="flex: 1;" />
          </div>
        </div>

        <!-- 阅卷完成汇总 -->
        <template v-if="reviewProgress.finished">
          <el-divider content-position="left">阅卷结果统计</el-divider>
          <div class="result-summary">
            <div class="summary-grid">
              <div class="summary-item">
                <div class="summary-value">{{ reviewResult.avgScore }}</div>
                <div class="summary-label">总体平均分</div>
              </div>
              <div class="summary-item">
                <div class="summary-value" style="color: #36D399;">{{ reviewResult.maxScore }}</div>
                <div class="summary-label">最高分</div>
              </div>
              <div class="summary-item">
                <div class="summary-value" style="color: #F53F3F;">{{ reviewResult.minScore }}</div>
                <div class="summary-label">最低分</div>
              </div>
            </div>

            <h4 class="section-title">各题型平均得分率</h4>
            <div class="type-score-bars">
              <div class="type-score-row">
                <span class="type-name">单选题</span>
                <el-progress :percentage="reviewResult.typeAccuracy.single" :stroke-width="16" :text-inside="true" color="#165DFF" style="flex: 1;" />
              </div>
              <div class="type-score-row">
                <span class="type-name">多选题</span>
                <el-progress :percentage="reviewResult.typeAccuracy.multiple" :stroke-width="16" :text-inside="true" color="#165DFF" style="flex: 1;" />
              </div>
              <div class="type-score-row">
                <span class="type-name">判断题</span>
                <el-progress :percentage="reviewResult.typeAccuracy.judge" :stroke-width="16" :text-inside="true" color="#165DFF" style="flex: 1;" />
              </div>
              <div class="type-score-row">
                <span class="type-name">填空题</span>
                <el-progress :percentage="reviewResult.typeAccuracy.fill" :stroke-width="16" :text-inside="true" color="#36D399" style="flex: 1;" />
              </div>
              <div class="type-score-row">
                <span class="type-name">编程题</span>
                <el-progress :percentage="reviewResult.typeAccuracy.program" :stroke-width="16" :text-inside="true" color="#FF9500" style="flex: 1;" />
              </div>
            </div>

            <h4 class="section-title">分数段分布</h4>
            <div class="score-distribution">
              <div class="dist-row" v-for="seg in reviewResult.distribution" :key="seg.range">
                <span class="dist-label">{{ seg.range }}</span>
                <div class="dist-bar-wrapper">
                  <div class="dist-bar" :style="{ width: seg.percent + '%', background: seg.color }"></div>
                </div>
                <span class="dist-count">{{ seg.count }}人</span>
              </div>
            </div>
          </div>
        </template>
      </div>
      <template #footer>
        <el-button v-if="reviewProgress.finished" type="primary" @click="reviewProgressDialogVisible = false">完成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * ScoreManagePage - 成绩管理
 */
import { ref, computed, reactive, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { Check } from '@element-plus/icons-vue'
import Pagination from '@/components/common/Pagination.vue'
import { scoreApi } from '@/api/admin'
import { useOperationLog } from '@/composables/useOperationLog'

const opLog = useOperationLog('score')

const pageNum = ref(1)
const pageSize = ref(20)
const loading = ref(false)
const selectedRows = ref<any[]>([])

const typeMap: Record<string, string> = {
  single: '单选题',
  multiple: '多选题',
  judge: '判断题',
  fill: '填空题',
  program: '编程题',
}

const searchForm = reactive({
  examName: '',
  studentName: '',
  school: '',
  award: '',
  reviewStatus: '',
})

function handleSearch() { pageNum.value = 1 }
function resetSearch() {
  searchForm.examName = ''
  searchForm.studentName = ''
  searchForm.school = ''
  searchForm.award = ''
  searchForm.reviewStatus = ''
  pageNum.value = 1
}

const scoreList = ref<any[]>([])

/** 加载成绩列表 */
async function fetchList() {
  loading.value = true
  try {
    const res = await scoreApi.list({ pageNum: 1, pageSize: 9999 })
    scoreList.value = res?.data?.list || res?.data?.records || []
  } catch {
    scoreList.value = []
    ElMessage.error('列表加载失败，请检查网络或稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchList()
})

const stats = computed(() => {
  const total = scoreList.value.length
  const avgScore = total > 0 ? Math.round(scoreList.value.reduce((s, c) => s + c.score, 0) / total) : 0
  const maxScore = total > 0 ? Math.max(...scoreList.value.map(c => c.score)) : 0
  const awarded = scoreList.value.filter(c => c.award !== '未获奖').length
  const awardRate = total > 0 ? Math.round((awarded / total) * 100) : 0
  return { total, avgScore, maxScore, awardRate }
})

const filteredList = computed(() => {
  let list = scoreList.value
  if (searchForm.examName) {
    list = list.filter(s => s.examName.includes(searchForm.examName))
  }
  if (searchForm.studentName) {
    list = list.filter(s => s.studentName.includes(searchForm.studentName))
  }
  if (searchForm.school) {
    list = list.filter(s => s.school.includes(searchForm.school))
  }
  if (searchForm.award) {
    list = list.filter(s => s.award === searchForm.award)
  }
  if (searchForm.reviewStatus) {
    list = list.filter(s => s.reviewStatus === searchForm.reviewStatus)
  }
  return list
})

const pagedList = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  return filteredList.value.slice(start, start + pageSize.value)
})

function handleSelectionChange(rows: any[]) {
  selectedRows.value = rows
}
function handleBatchExport() {
  const headers = ['ID', '学生姓名', '学校', '年级', '参赛赛事', '得分', '排名', '奖项', '阅卷状态', '用时(分)']
  const rows = filteredList.value.map(s => [s.id, s.studentName, s.school, s.grade, s.examName, s.score, s.rank, s.award, s.reviewStatus === 'reviewed' ? '已阅卷' : s.reviewStatus === 'partial' ? '部分阅卷' : '未阅卷', s.duration])
  const csv = [headers.join(','), ...rows.map(r => r.map(c => `"${c}"`).join(','))].join('\n')
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `成绩数据_${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
  opLog.logExport('导出成绩数据')
}
function handleBatchCertificate() {
  ElMessage.warning('该能力暂未接入后端服务，操作未生效')
}
function handleView(row: any) {
  detailRow.value = row
  detailDialogVisible.value = true
}
function handleEdit(row: any) {
  isEdit.value = true
  currentId.value = row.id
  editForm.value = {
    studentName: row.studentName,
    score: row.score,
    rank: row.rank,
    award: row.award,
    remark: '',
  }
  editDialogVisible.value = true
}
function handleRejudge(_row: any) {
  ElMessage.warning('该能力暂未接入后端服务，操作未生效')
}
function handleCertificate(_row: any) {
  ElMessage.warning('该能力暂未接入后端服务，操作未生效')
}

// 编辑成绩
const editDialogVisible = ref(false)
const isEdit = ref(false)
const currentId = ref(0)
const submitting = ref(false)
const editFormRef = ref<FormInstance>()
const editForm = ref({
  studentName: '',
  score: 0,
  rank: 1,
  award: '未获奖',
  remark: '',
})
const editRules: FormRules = {
  score: [{ required: true, message: '请输入得分', trigger: 'change' }],
  rank: [{ required: true, message: '请输入排名', trigger: 'change' }],
  award: [{ required: true, message: '请选择奖项', trigger: 'change' }],
}

async function submitEdit() {
  if (!editFormRef.value) return
  await editFormRef.value.validate(async (valid) => {
    if (!valid) return
    submitting.value = true
    try {
      const payload = {
        score: editForm.value.score,
        rank: editForm.value.rank,
        award: editForm.value.award,
        remark: editForm.value.remark,
      }
      await scoreApi.update(currentId.value, payload)
      const idx = scoreList.value.findIndex(s => s.id === currentId.value)
      if (idx !== -1) {
        scoreList.value[idx].score = editForm.value.score
        scoreList.value[idx].rank = editForm.value.rank
        scoreList.value[idx].award = editForm.value.award
        if (editForm.value.remark) {
          if (!scoreList.value[idx].reviewHistory) scoreList.value[idx].reviewHistory = []
          scoreList.value[idx].reviewHistory.unshift({
            time: new Date().toLocaleString(),
            operator: '管理员',
            action: '人工修改成绩',
            remark: editForm.value.remark,
            type: 'warning',
          })
        }
      }
      // 重新排序排名
      scoreList.value.sort((a, b) => b.score - a.score)
      scoreList.value.forEach((item, idx) => { item.rank = idx + 1 })
      ElMessage.success('编辑成功')
      opLog.logUpdate('编辑成绩')
      editDialogVisible.value = false
    } catch {
      ElMessage.error('操作失败，请检查网络或稍后重试')
    } finally {
      submitting.value = false
    }
  })
}

// 详情
const detailDialogVisible = ref(false)
const detailRow = ref<any>(null)

// ==================== 自动阅卷相关 ====================

// 试卷选项
const examOptions = computed(() => {
  const examMap = new Map<string, number>()
  scoreList.value.forEach(s => {
    if (!examMap.has(s.examName)) {
      examMap.set(s.examName, examMap.size + 1)
    }
  })
  return Array.from(examMap.entries()).map(([name, id]) => ({ id, name }))
})

// 学生搜索结果
const studentSearchResults = ref<any[]>([])

function searchStudents(query: string) {
  if (!query) {
    studentSearchResults.value = []
    return
  }
  studentSearchResults.value = scoreList.value.filter(
    s => s.studentName.includes(query)
  ).map(s => ({ id: s.id, studentName: s.studentName, school: s.school }))
}

// 自动阅卷配置弹窗
const autoReviewDialogVisible = ref(false)
const autoReviewConfig = reactive({
  examId: null as number | null,
  scope: 'all' as 'all' | 'specified',
  studentIds: [] as number[],
  programStrategy: 'partial_tests' as string,
})

function openAutoReviewDialog() {
  autoReviewConfig.examId = null
  autoReviewConfig.scope = 'all'
  autoReviewConfig.studentIds = []
  autoReviewConfig.programStrategy = 'partial_tests'
  autoReviewDialogVisible.value = true
}

// 阅卷进度
const reviewProgressDialogVisible = ref(false)
const reviewProgress = reactive({
  current: 0,
  total: 0,
  percentage: 0,
  status: '' as '' | 'success' | 'exception',
  finished: false,
  objectiveAccuracy: 0,
  fillAccuracy: 0,
  programPassRate: 0,
})

// 阅卷结果统计
const reviewResult = reactive({
  avgScore: 0,
  maxScore: 0,
  minScore: 0,
  typeAccuracy: {
    single: 0,
    multiple: 0,
    judge: 0,
    fill: 0,
    program: 0,
  },
  distribution: [] as Array<{ range: string; count: number; percent: number; color: string }>,
})

// 执行自动阅卷
function startAutoReview() {
  ElMessage.warning('该能力暂未接入后端服务，操作未生效')
}

// 批量自动阅卷（表格选中）
function handleBatchAutoReview() {
  ElMessage.warning('该能力暂未接入后端服务，操作未生效')
}
</script>

<style scoped lang="scss">
$primary: #165DFF;

.score-manage-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;
  h2 { font-size: 20px; color: #1D2129; }
  .header-actions { display: flex; gap: 8px; }
}

.stat-cards { display: flex; gap: 16px; margin-bottom: 20px;
  .stat-card { flex: 1; background: $bg-white; border: 1px solid #E5E7EB; border-radius: 8px; padding: 16px; text-align: center;
    .stat-value { font-size: 28px; font-weight: 600; color: #1D2129; margin-bottom: 4px; }
    .stat-label { font-size: 13px; color: #666; }
  }
}

.search-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }

.rank-badge { display: inline-flex; align-items: center; justify-content: center; width: 24px; height: 24px; border-radius: 50%; background: #f0f0f0; color: #666; font-size: 13px; font-weight: 600;
  &.top { background: $primary; color: #fff; }
}

.detail-body {
  .detail-header { margin-bottom: 16px; padding-bottom: 12px; border-bottom: 1px solid #E5E7EB;
    p { margin-bottom: 6px; color: #555; }
  }
  .section-title { font-size: 14px; color: $text-primary; margin: 16px 0 12px; padding-left: 8px; border-left: 3px solid $primary; }
  .code-block { background: #f5f7fa; padding: 12px; border-radius: 6px; overflow-x: auto; font-family: monospace; font-size: 13px; }
}

// 阅卷日志样式
.review-log-list {
  .review-log-item {
    background: #F7F8FA;
    border: 1px solid #E5E7EB;
    border-radius: 6px;
    padding: 12px;
    margin-bottom: 8px;
    &:last-child { margin-bottom: 0; }
    .review-log-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 8px;
      .review-log-time {
        font-size: 12px;
        color: #999;
      }
    }
    .review-log-body {
      p {
        margin-bottom: 4px;
        font-size: 13px;
        color: #555;
        &:last-child { margin-bottom: 0; }
      }
    }
  }
}

// 自动阅卷配置弹窗
.rule-desc {
  p {
    margin: 0;
    font-size: 13px;
    color: #666;
  }
}

// 阅卷进度弹窗
.review-progress-body {
  .progress-section {
    margin-bottom: 20px;
    .progress-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 8px;
      font-size: 14px;
      color: $text-primary;
      .progress-count {
        font-weight: 600;
        color: $primary;
      }
    }
  }

  .realtime-stats {
    .stat-row {
      display: flex;
      align-items: center;
      gap: 12px;
      margin-bottom: 10px;
      .stat-label {
        width: 120px;
        font-size: 13px;
        color: #555;
        flex-shrink: 0;
      }
    }
  }

  // 结果汇总
  .result-summary {
    .summary-grid {
      display: flex;
      gap: 16px;
      margin-bottom: 16px;
      .summary-item {
        flex: 1;
        background: #f5f7fa;
        border-radius: 8px;
        padding: 16px;
        text-align: center;
        .summary-value {
          font-size: 28px;
          font-weight: 600;
          color: #1D2129;
        }
        .summary-label {
          font-size: 13px;
          color: #666;
          margin-top: 4px;
        }
      }
    }

    .type-score-bars {
      .type-score-row {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 10px;
        .type-name {
          width: 60px;
          font-size: 13px;
          color: #555;
          flex-shrink: 0;
        }
      }
    }

    .score-distribution {
      .dist-row {
        display: flex;
        align-items: center;
        gap: 12px;
        margin-bottom: 8px;
        .dist-label {
          width: 60px;
          font-size: 13px;
          color: #555;
          flex-shrink: 0;
        }
        .dist-bar-wrapper {
          flex: 1;
          height: 20px;
          background: #f0f0f0;
          border-radius: 10px;
          overflow: hidden;
          .dist-bar {
            height: 100%;
            border-radius: 10px;
            transition: width 0.5s ease;
            min-width: 0;
          }
        }
        .dist-count {
          width: 50px;
          font-size: 13px;
          color: #555;
          text-align: right;
          flex-shrink: 0;
        }
      }
    }
  }
}
</style>
