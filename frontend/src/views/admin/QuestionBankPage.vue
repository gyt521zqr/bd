<template>
  <!-- 题库中心 -->
  <div class="question-bank-page">
    <div class="page-header">
      <h2>题库中心</h2>
      <div class="header-actions">
        <el-button type="primary" @click="openAddDialog">添加题目</el-button>
        <el-button title="暂未接入后端服务" @click="importDialogVisible = true">批量导入</el-button>
        <el-button @click="handleBatchExport">批量导出</el-button>
        <el-button type="success" @click="openComposeDialog">从题库组卷</el-button>
      </div>
    </div>

    <el-alert
      title="功能提示"
      description="批量导入暂未接入后端服务，操作不会保存。"
      type="info"
      :closable="false"
      show-icon
      style="margin-bottom: 16px"
    />

    <!-- 题型分类标签 -->
    <div class="type-tabs">
      <el-radio-group v-model="typeFilter" size="default" @change="handleTypeChange">
        <el-radio-button label="">全部</el-radio-button>
        <el-radio-button label="single">单选题</el-radio-button>
        <el-radio-button label="multiple">多选题</el-radio-button>
        <el-radio-button label="judge">判断题</el-radio-button>
        <el-radio-button label="fill">填空题</el-radio-button>
        <el-radio-button label="program">编程题</el-radio-button>
      </el-radio-group>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input v-model="searchForm.content" placeholder="搜索题目内容" clearable style="width: 260px" />
      <el-select v-model="searchForm.difficulty" placeholder="难度" clearable style="width: 120px">
        <el-option label="简单" value="easy" />
        <el-option label="中等" value="medium" />
        <el-option label="困难" value="hard" />
      </el-select>
      <el-select-v2
        v-model="searchForm.tags"
        :options="tagOptions"
        placeholder="知识点标签"
        multiple
        clearable
        collapse-tags
        style="width: 200px"
      />
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="resetSearch">重置</el-button>
      <el-button v-if="selectedIds.length > 0" type="danger" @click="handleBatchDelete">批量删除</el-button>
    </div>

    <!-- 表格 -->
    <el-table :data="pagedList" stripe @selection-change="handleSelectionChange" v-loading="loading" empty-text="暂无数据">
      <el-table-column type="selection" width="50" />
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="type" label="题型" width="90">
        <template #default="{ row }">
          <el-tag size="small" :type="typeTagType(row.type)">{{ typeMap[row.type] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="title" label="题目标题" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">
          {{ row.title || row.content?.substring(0, 30) }}
        </template>
      </el-table-column>
      <el-table-column prop="content" label="题目内容" min-width="260" show-overflow-tooltip />
      <el-table-column prop="score" label="分值" width="70" />
      <el-table-column prop="difficulty" label="难度" width="80">
        <template #default="{ row }">
          <el-tag size="small" :type="difficultyTagType(row.difficulty)">{{ difficultyMap[row.difficulty] }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="tags" label="知识点" min-width="160">
        <template #default="{ row }">
          <el-tag v-for="tag in row.tags" :key="tag" size="small" class="tag-item">{{ tag }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="useCount" label="使用次数" width="90" />
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button size="small" text type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button size="small" text type="primary" @click="handleView(row)">查看</el-button>
          <el-button size="small" text type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <Pagination :total="filteredList.length" v-model:page="pageNum" v-model:page-size="pageSize" />

    <!-- 添加/编辑题目弹窗 -->
    <el-dialog v-model="formDialogVisible" :title="isEdit ? '编辑题目' : '添加题目'" width="780px" destroy-on-close top="3vh">
      <el-form ref="formRef" :model="form" :rules="formRules" label-width="110px">
        <el-form-item label="题型" prop="type">
          <el-radio-group v-model="form.type" :disabled="isEdit" @change="onTypeChange">
            <el-radio-button label="single">单选题</el-radio-button>
            <el-radio-button label="multiple">多选题</el-radio-button>
            <el-radio-button label="judge">判断题</el-radio-button>
            <el-radio-button label="fill">填空题</el-radio-button>
            <el-radio-button label="program">编程题</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="题目标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入题目标题（可选，简短概括）" maxlength="100" show-word-limit />
        </el-form-item>
        <el-form-item label="题目分值" prop="score">
          <el-input-number v-model="form.score" :min="1" :max="100" :step="1" />
          <span class="form-tip" style="margin-left: 8px;">分</span>
        </el-form-item>
        <el-form-item label="题干" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="3" placeholder="请输入题目内容" />
        </el-form-item>
        <el-form-item label="难度" prop="difficulty">
          <el-radio-group v-model="form.difficulty">
            <el-radio-button label="easy">简单</el-radio-button>
            <el-radio-button label="medium">中等</el-radio-button>
            <el-radio-button label="hard">困难</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="知识点" prop="tags">
          <el-select-v2 v-model="form.tags" :options="tagOptions" multiple clearable collapse-tags placeholder="选择知识点标签" style="width: 100%" />
        </el-form-item>

        <!-- 单选题选项 -->
        <template v-if="form.type === 'single'">
          <el-divider content-position="left">选项设置</el-divider>
          <el-form-item
            v-for="(opt, idx) in form.options"
            :key="idx"
            :label="'选项 ' + opt.label"
            :prop="'options.' + idx + '.content'"
            :rules="[{ required: true, message: '选项内容不能为空', trigger: 'blur' }]"
          >
            <div class="option-row">
              <el-input v-model="opt.content" :placeholder="'请输入选项 ' + opt.label + ' 的内容'" style="flex: 1;" />
              <el-button
                v-if="form.options.length > 2"
                text
                type="danger"
                @click="removeOption(idx)"
                title="删除此选项"
              >删除</el-button>
              <el-button
                text
                :type="form.answer === opt.label ? 'primary' : 'info'"
                @click="form.answer = opt.label"
                title="设为正确答案"
              >{{ form.answer === opt.label ? '已选答案' : '设为答案' }}</el-button>
            </div>
          </el-form-item>
          <el-form-item v-if="form.options.length < 6">
            <el-button @click="addOption" :disabled="form.options.length >= 6">添加选项</el-button>
            <span class="form-tip">（最多6个选项，至少保留2个）</span>
          </el-form-item>
          <el-form-item label="正确答案" prop="answer" :rules="[{ required: true, message: '请选择正确答案', trigger: 'change' }]">
            <el-radio-group v-model="form.answer">
              <el-radio v-for="opt in form.options" :key="opt.label" :label="opt.label">{{ opt.label }}</el-radio>
            </el-radio-group>
          </el-form-item>
        </template>

        <!-- 多选题选项 -->
        <template v-if="form.type === 'multiple'">
          <el-divider content-position="left">选项设置</el-divider>
          <el-form-item
            v-for="(opt, idx) in form.options"
            :key="idx"
            :label="'选项 ' + opt.label"
            :prop="'options.' + idx + '.content'"
            :rules="[{ required: true, message: '选项内容不能为空', trigger: 'blur' }]"
          >
            <div class="option-row">
              <el-input v-model="opt.content" :placeholder="'请输入选项 ' + opt.label + ' 的内容'" style="flex: 1;" />
              <el-button
                v-if="form.options.length > 2"
                text
                type="danger"
                @click="removeOption(idx)"
                title="删除此选项"
              >删除</el-button>
              <el-button
                text
                :type="form.answers.includes(opt.label) ? 'primary' : 'info'"
                @click="toggleMultiAnswer(opt.label)"
                title="设为/取消正确答案"
              >{{ form.answers.includes(opt.label) ? '取消答案' : '设为答案' }}</el-button>
            </div>
          </el-form-item>
          <el-form-item v-if="form.options.length < 6">
            <el-button @click="addOption" :disabled="form.options.length >= 6">添加选项</el-button>
            <span class="form-tip">（最多6个选项，至少保留2个）</span>
          </el-form-item>
          <el-form-item label="正确答案" prop="answers" :rules="multiAnswerRule">
            <el-checkbox-group v-model="form.answers">
              <el-checkbox v-for="opt in form.options" :key="opt.label" :label="opt.label">{{ opt.label }}</el-checkbox>
            </el-checkbox-group>
            <div class="form-tip">请至少选择2个正确答案</div>
          </el-form-item>
        </template>

        <!-- 判断题 -->
        <template v-if="form.type === 'judge'">
          <el-divider content-position="left">判断设置</el-divider>
          <el-form-item label="题目描述" prop="judgeDescription">
            <el-input
              v-model="form.judgeDescription"
              type="textarea"
              :rows="4"
              placeholder="请输入详细的题目描述，便于学生理解判断依据"
            />
          </el-form-item>
          <el-form-item label="正确答案" prop="answer" :rules="[{ required: true, message: '请选择正确答案', trigger: 'change' }]">
            <el-radio-group v-model="form.answer">
              <el-radio label="true">
                <span style="color: #36D399; font-weight: bold;">对</span>
              </el-radio>
              <el-radio label="false">
                <span style="color: #F53F3F; font-weight: bold;">错</span>
              </el-radio>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="判断依据" prop="judgeBasis">
            <el-input
              v-model="form.judgeBasis"
              type="textarea"
              :rows="3"
              placeholder="请输入判断依据说明，解释为什么对或为什么错"
            />
          </el-form-item>
        </template>

        <!-- 填空题 -->
        <template v-if="form.type === 'fill'">
          <el-divider content-position="left">填空设置</el-divider>
          <el-form-item label="填空说明">
            <div class="form-tip">
              在题干中使用 <code>__1__</code>、<code>__2__</code> 等标记空位，系统将自动识别空位数量并生成对应答案输入框。
              <br/>例如：Python中定义函数的关键字是<code>__1__</code>，调用函数需要使用<code>__2__</code>。
            </div>
          </el-form-item>
          <el-form-item label="检测到的空位">
            <div class="blank-info">
              <template v-if="blankSlots.length > 0">
                <el-tag v-for="slot in blankSlots" :key="slot" size="small" type="info" class="tag-item">
                  空{{ slot }}
                </el-tag>
                <span class="form-tip">共 {{ blankSlots.length }} 个空位</span>
              </template>
              <span v-else class="form-tip">未检测到空位标记，请在题干中使用 __n__ 标记</span>
            </div>
          </el-form-item>
          <el-form-item
            v-for="slot in blankSlots"
            :key="slot"
            :label="'空' + slot + ' 答案'"
            :prop="'fillAnswers.' + (slot - 1)"
            :rules="[{ required: true, message: '请输入空' + slot + '的答案', trigger: 'blur' }]"
          >
            <el-input v-model="form.fillAnswers[slot - 1]" :placeholder="'请输入空' + slot + '的正确答案'" />
          </el-form-item>
          <el-form-item label="答案解析" prop="fillAnalysis">
            <el-input
              v-model="form.fillAnalysis"
              type="textarea"
              :rows="3"
              placeholder="请输入填空题的答案解析，帮助理解每个空的答案"
            />
          </el-form-item>
        </template>

        <!-- 编程题 -->
        <template v-if="form.type === 'program'">
          <el-divider content-position="left">编程设置</el-divider>
          <el-form-item label="编程语言" prop="language" :rules="[{ required: true, message: '请选择编程语言', trigger: 'change' }]">
            <el-select v-model="form.language" placeholder="请选择编程语言" style="width: 200px;">
              <el-option label="Python" value="python" />
              <el-option label="C++" value="cpp" />
              <el-option label="GoC" value="goc" />
              <el-option label="Scratch" value="scratch" />
            </el-select>
          </el-form-item>
          <el-form-item label="模板代码" prop="templateCode" :rules="[{ required: true, message: '请输入模板代码', trigger: 'blur' }]">
            <el-input
              v-model="form.templateCode"
              type="textarea"
              :rows="8"
              placeholder="请输入模板代码（学生答题时的初始代码）"
              style="font-family: 'Courier New', monospace;"
            />
          </el-form-item>
          <el-form-item label="参考答案" prop="referenceCode">
            <el-input
              v-model="form.referenceCode"
              type="textarea"
              :rows="8"
              placeholder="请输入参考答案代码（可选）"
              style="font-family: 'Courier New', monospace;"
            />
          </el-form-item>
          <el-form-item label="测试用例" prop="testCasesList" :rules="testCaseRule">
            <div class="test-case-list">
              <div v-for="(tc, idx) in form.testCasesList" :key="idx" class="test-case-item">
                <div class="test-case-header">
                  <span class="test-case-index">用例 {{ idx + 1 }}</span>
                  <el-checkbox v-model="tc.isSample" label="样例" />
                  <el-button v-if="form.testCasesList.length > 1" text type="danger" size="small" @click="removeTestCase(idx)">删除</el-button>
                </div>
                <el-input v-model="tc.input" type="textarea" :rows="2" placeholder="输入数据" style="margin-bottom: 8px;" />
                <el-input v-model="tc.expectedOutput" type="textarea" :rows="2" placeholder="预期输出" />
              </div>
              <el-button @click="addTestCase" :disabled="form.testCasesList.length >= 10">添加测试用例</el-button>
              <span class="form-tip">（最多10个，至少1个）</span>
            </div>
          </el-form-item>
          <el-form-item label="时间限制" prop="timeLimit" :rules="[{ required: true, message: '请设置时间限制', trigger: 'blur' }]">
            <el-input-number v-model="form.timeLimit" :min="100" :max="30000" :step="100" />
            <span class="form-tip" style="margin-left: 8px;">ms</span>
          </el-form-item>
          <el-form-item label="内存限制" prop="memoryLimit" :rules="[{ required: true, message: '请设置内存限制', trigger: 'blur' }]">
            <el-input-number v-model="form.memoryLimit" :min="16" :max="1024" :step="16" />
            <span class="form-tip" style="margin-left: 8px;">MB</span>
          </el-form-item>
        </template>

        <el-divider content-position="left">其他信息</el-divider>
        <el-form-item label="题目解析" prop="analysis">
          <el-input
            v-model="form.analysis"
            type="textarea"
            :rows="3"
            placeholder="请输入题目解析（支持简要说明解题思路）"
          />
          <div class="form-tip">可简要说明解题思路、关键知识点等</div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="formDialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <!-- 批量导入弹窗 -->
    <el-dialog v-model="importDialogVisible" title="批量导入题目" width="500px" destroy-on-close>
      <div class="import-body">
        <p class="import-tip">支持 Excel (.xlsx) 和 Word (.docx) 格式，可混合导入多种题型。</p>
        <el-upload drag action="#" :auto-upload="false" :on-change="handleImportFileChange" accept=".xlsx,.docx">
          <el-icon class="el-icon--upload"><upload-filled /></el-icon>
          <div class="el-upload__text">拖拽文件到此处或 <em>点击上传</em></div>
        </el-upload>
        <div class="import-actions">
          <el-button link type="primary" @click="downloadTemplate">下载导入模板</el-button>
        </div>
        <div v-if="importResult.length > 0" class="import-result">
          <h4>导入结果</h4>
          <p>成功：{{ importResult.filter(r => r.success).length }} 条</p>
          <p>失败：{{ importResult.filter(r => !r.success).length }} 条</p>
          <el-table :data="importResult.filter(r => !r.success)" size="small" v-if="importResult.some(r => !r.success)" empty-text="暂无数据">
            <el-table-column prop="row" label="行号" width="60" />
            <el-table-column prop="reason" label="失败原因" />
          </el-table>
        </div>
      </div>
      <template #footer>
        <el-button @click="importDialogVisible = false">关闭</el-button>
        <el-button type="primary" :loading="importing" @click="confirmImport">开始导入</el-button>
      </template>
    </el-dialog>

    <!-- 从题库组卷弹窗 -->
    <el-dialog v-model="composeDialogVisible" title="从题库组卷" width="900px" destroy-on-close>
      <div class="compose-body">
        <div class="compose-left">
          <h4>题库列表</h4>
          <el-input v-model="composeSearch" placeholder="搜索题目" size="small" clearable />
          <el-select v-model="composeType" placeholder="题型筛选" size="small" clearable style="width: 100%; margin-top: 8px;">
            <el-option label="单选题" value="single" />
            <el-option label="多选题" value="multiple" />
            <el-option label="判断题" value="judge" />
            <el-option label="填空题" value="fill" />
            <el-option label="编程题" value="program" />
          </el-select>
          <div class="compose-list">
            <div
              v-for="q in composeAvailableList"
              :key="q.id"
              class="compose-item"
              @click="addToPaper(q)"
            >
              <span class="compose-item-type">{{ typeMap[q.type] }}</span>
              <span class="compose-item-content" :title="q.content">{{ q.content }}</span>
              <el-button size="small" text type="primary">添加</el-button>
            </div>
          </div>
        </div>
        <div class="compose-right">
          <h4>已选题目（{{ composeSelected.length }}）</h4>
          <div class="compose-list">
            <div
              v-for="(q, idx) in composeSelected"
              :key="q.id"
              class="compose-item selected"
              draggable="true"
              @dragstart="dragStart(idx)"
              @dragover.prevent
              @drop="drop(idx)"
            >
              <span class="compose-item-index">{{ idx + 1 }}</span>
              <span class="compose-item-content" :title="q.content">{{ q.content }}</span>
              <el-input-number v-model="q.score" :min="1" :max="100" size="small" style="width: 90px" />
              <el-button size="small" text type="danger" @click="removeFromPaper(idx)">移除</el-button>
            </div>
          </div>
          <div class="compose-actions">
            <el-button size="small" @click="batchSetScore">批量设置分值</el-button>
            <el-button type="primary" size="small" @click="confirmCompose">加入试卷</el-button>
          </div>
        </div>
      </div>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog v-model="viewDialogVisible" title="题目详情" width="700px">
      <div v-if="viewRow" class="view-body">
        <div class="view-section">
          <h4>基本信息</h4>
          <p><strong>题型：</strong><el-tag size="small" :type="typeTagType(viewRow.type)">{{ typeMap[viewRow.type] }}</el-tag></p>
          <p v-if="viewRow.title"><strong>题目标题：</strong>{{ viewRow.title }}</p>
          <p><strong>题目分值：</strong>{{ viewRow.score || 5 }} 分</p>
          <p><strong>题干：</strong></p>
          <div class="view-content-block">{{ viewRow.content }}</div>
          <p><strong>难度：</strong><el-tag size="small" :type="difficultyTagType(viewRow.difficulty)">{{ difficultyMap[viewRow.difficulty] }}</el-tag></p>
          <p><strong>知识点：</strong><el-tag v-for="t in viewRow.tags" :key="t" size="small" class="tag-item">{{ t }}</el-tag></p>
        </div>

        <!-- 单选题详情 -->
        <template v-if="viewRow.type === 'single'">
          <div class="view-section">
            <h4>选项与答案</h4>
            <div v-for="opt in viewRow.options" :key="opt.label" class="view-option-item" :class="{ 'is-correct': opt.label === viewRow.answer }">
              <span class="view-option-label">{{ opt.label }}.</span>
              <span class="view-option-content">{{ opt.content }}</span>
              <el-tag v-if="opt.label === viewRow.answer" size="small" type="success">正确答案</el-tag>
            </div>
          </div>
        </template>

        <!-- 多选题详情 -->
        <template v-if="viewRow.type === 'multiple'">
          <div class="view-section">
            <h4>选项与答案</h4>
            <div v-for="opt in viewRow.options" :key="opt.label" class="view-option-item" :class="{ 'is-correct': viewRow.answers?.includes(opt.label) }">
              <span class="view-option-label">{{ opt.label }}.</span>
              <span class="view-option-content">{{ opt.content }}</span>
              <el-tag v-if="viewRow.answers?.includes(opt.label)" size="small" type="success">正确答案</el-tag>
            </div>
          </div>
        </template>

        <!-- 判断题详情 -->
        <template v-if="viewRow.type === 'judge'">
          <div class="view-section">
            <h4>判断信息</h4>
            <p v-if="viewRow.judgeDescription"><strong>题目描述：</strong>{{ viewRow.judgeDescription }}</p>
            <p><strong>正确答案：</strong>
              <el-tag size="small" :type="viewRow.answer === 'true' ? 'success' : 'danger'">
                {{ viewRow.answer === 'true' ? '对' : '错' }}
              </el-tag>
            </p>
            <p v-if="viewRow.judgeBasis"><strong>判断依据：</strong>{{ viewRow.judgeBasis }}</p>
          </div>
        </template>

        <!-- 填空题详情 -->
        <template v-if="viewRow.type === 'fill'">
          <div class="view-section">
            <h4>填空信息</h4>
            <p><strong>正确答案：</strong></p>
            <div v-for="(ans, idx) in viewRow.fillAnswers" :key="idx" class="view-blank-item">
              <el-tag size="small" type="info">空{{ idx + 1 }}</el-tag>
              <span class="view-blank-answer">{{ ans }}</span>
            </div>
            <p v-if="viewRow.fillAnalysis"><strong>答案解析：</strong>{{ viewRow.fillAnalysis }}</p>
          </div>
        </template>

        <!-- 编程题详情 -->
        <template v-if="viewRow.type === 'program'">
          <div class="view-section">
            <h4>编程信息</h4>
            <p><strong>编程语言：</strong>{{ languageMap[viewRow.language] || viewRow.language }}</p>
            <p><strong>模板代码：</strong></p>
            <pre class="code-block">{{ viewRow.templateCode }}</pre>
            <p v-if="viewRow.referenceCode"><strong>参考答案：</strong></p>
            <pre v-if="viewRow.referenceCode" class="code-block">{{ viewRow.referenceCode }}</pre>
            <p><strong>测试用例：</strong></p>
            <div v-for="(tc, idx) in viewRow.testCasesList" :key="idx" class="view-test-case">
              <div class="view-test-case-header">
                <span>用例 {{ idx + 1 }}</span>
                <el-tag v-if="tc.isSample" size="small" type="warning">样例</el-tag>
              </div>
              <div class="view-test-case-row">
                <span class="view-tc-label">输入：</span>
                <pre class="code-block code-block-inline">{{ tc.input }}</pre>
              </div>
              <div class="view-test-case-row">
                <span class="view-tc-label">预期输出：</span>
                <pre class="code-block code-block-inline">{{ tc.expectedOutput }}</pre>
              </div>
            </div>
            <p><strong>时间限制：</strong>{{ viewRow.timeLimit }} ms</p>
            <p><strong>内存限制：</strong>{{ viewRow.memoryLimit }} MB</p>
          </div>
        </template>

        <div class="view-section">
          <h4>解析</h4>
          <p>{{ viewRow.analysis || '无' }}</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
/**
 * QuestionBankPage - 题库中心
 */
import { ref, computed, reactive, watch, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance, type FormRules } from 'element-plus'
import { UploadFilled } from '@element-plus/icons-vue'
import Pagination from '@/components/common/Pagination.vue'
import { questionApi } from '@/api/admin'
import { useOperationLog } from '@/composables/useOperationLog'

const opLog = useOperationLog('question')

const pageNum = ref(1)
const pageSize = ref(20)
const loading = ref(false)
const selectedIds = ref<number[]>([])

const typeFilter = ref('')
const typeMap: Record<string, string> = {
  single: '单选题',
  multiple: '多选题',
  judge: '判断题',
  fill: '填空题',
  program: '编程题',
}
const difficultyMap: Record<string, string> = {
  easy: '简单',
  medium: '中等',
  hard: '困难',
}
const languageMap: Record<string, string> = {
  python: 'Python',
  cpp: 'C++',
  goc: 'GoC',
  scratch: 'Scratch',
}
const tagOptions = [
  { value: 'Python基础', label: 'Python基础' },
  { value: 'C++语法', label: 'C++语法' },
  { value: '算法', label: '算法' },
  { value: '数据结构', label: '数据结构' },
  { value: '字符串', label: '字符串' },
  { value: '数组', label: '数组' },
  { value: '循环', label: '循环' },
  { value: '条件判断', label: '条件判断' },
  { value: '函数', label: '函数' },
  { value: '排序', label: '排序' },
  { value: '递归', label: '递归' },
  { value: '贪心', label: '贪心' },
  { value: '动态规划', label: '动态规划' },
]

const searchForm = reactive({
  content: '',
  difficulty: '',
  tags: [] as string[],
})

function handleSearch() {
  pageNum.value = 1
  loadQuestions()
}
function resetSearch() {
  searchForm.content = ''
  searchForm.difficulty = ''
  searchForm.tags = []
  typeFilter.value = ''
  pageNum.value = 1
}
function handleTypeChange() {
  pageNum.value = 1
}

function typeTagType(type: string) {
  const map: Record<string, any> = {
    single: 'primary',
    multiple: 'success',
    judge: 'warning',
    fill: 'info',
    program: 'danger',
  }
  return map[type] || ''
}
function difficultyTagType(d: string) {
  const map: Record<string, any> = { easy: 'success', medium: 'warning', hard: 'danger' }
  return map[d] || ''
}

const questionList = ref<any[]>([])

const filteredList = computed(() => {
  let list = questionList.value
  if (typeFilter.value) {
    list = list.filter(q => q.type === typeFilter.value)
  }
  if (searchForm.content) {
    list = list.filter(q => q.content.includes(searchForm.content))
  }
  if (searchForm.difficulty) {
    list = list.filter(q => q.difficulty === searchForm.difficulty)
  }
  if (searchForm.tags.length > 0) {
    list = list.filter(q => searchForm.tags.some((t: string) => q.tags.includes(t)))
  }
  return list
})

const pagedList = computed(() => {
  const start = (pageNum.value - 1) * pageSize.value
  return filteredList.value.slice(start, start + pageSize.value)
})

/** 加载题目列表 */
async function loadQuestions() {
  loading.value = true
  try {
    const res = await questionApi.list({ pageNum: 1, pageSize: 9999 })
    questionList.value = res?.data?.list || res?.data?.records || []
  } catch {
    questionList.value = []
    ElMessage.error('列表加载失败，请检查网络或稍后重试')
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  loadQuestions()
})

function handleSelectionChange(rows: any[]) {
  selectedIds.value = rows.map(r => r.id)
}
function handleBatchDelete() {
  ElMessageBox.confirm(`确定删除选中的 ${selectedIds.value.length} 道题目吗？`, '警告', { type: 'warning' })
    .then(async () => {
      try {
        await questionApi.batchRemove(selectedIds.value)
        questionList.value = questionList.value.filter(q => !selectedIds.value.includes(q.id))
        selectedIds.value = []
        ElMessage.success('批量删除成功')
      } catch {
        ElMessage.error('操作失败，请检查网络或稍后重试')
      }
    })
    .catch(() => {})
}
function handleBatchExport() {
  const headers = ['ID', '题型', '题目标题', '题目内容', '分值', '难度', '知识点', '使用次数']
  const rows = filteredList.value.map(q => [q.id, typeMap[q.type] || q.type, q.title || '', q.content || '', q.score || 5, difficultyMap[q.difficulty] || q.difficulty, (q.tags || []).join('|'), q.useCount || 0])
  const csv = [headers.join(','), ...rows.map(r => r.map(c => `"${c}"`).join(','))].join('\n')
  const blob = new Blob(['\ufeff' + csv], { type: 'text/csv;charset=utf-8;' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `题库数据_${new Date().toISOString().slice(0, 10)}.csv`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
  ElMessage.success('导出成功')
  opLog.logExport('导出题库数据')
}
function handleEdit(row: any) {
  isEdit.value = true
  currentId.value = row.id
  // 深拷贝并规范化数据
  const formData: any = {
    type: row.type,
    title: row.title || '',
    content: row.content || '',
    score: row.score || 5,
    difficulty: row.difficulty || 'easy',
    tags: row.tags ? [...row.tags] : [],
    analysis: row.analysis || '',
    options: [],
    answer: row.answer || '',
    answers: row.answers ? [...row.answers] : [],
    judgeDescription: row.judgeDescription || '',
    judgeBasis: row.judgeBasis || '',
    fillAnswers: row.fillAnswers ? [...row.fillAnswers] : [],
    fillAnalysis: row.fillAnalysis || '',
    templateCode: row.templateCode || '',
    referenceCode: row.referenceCode || '',
    language: row.language || 'python',
    testCasesList: row.testCasesList ? row.testCasesList.map((tc: any) => ({ ...tc })) : [],
    timeLimit: row.timeLimit || 1000,
    memoryLimit: row.memoryLimit || 128,
  }
  // 统一选项格式：确保选择题都使用 [{label, content}] 格式
  if (row.type === 'single' || row.type === 'multiple') {
    if (row.options && Array.isArray(row.options) && row.options.length > 0 && row.options[0].label) {
      // 已经是标准格式
      formData.options = row.options.map((o: any) => ({ label: o.label, content: o.content }))
    } else if (row.options && Array.isArray(row.options) && typeof row.options[0] === 'string') {
      // 旧格式：字符串数组
      formData.options = row.options.map((o: string, idx: number) => ({
        label: optionLabels[idx],
        content: o,
      }))
    } else {
      // 兼容旧 optionA/optionB 格式
      const labels = ['A', 'B', 'C', 'D', 'E', 'F']
      const keys = ['optionA', 'optionB', 'optionC', 'optionD', 'optionE', 'optionF']
      formData.options = []
      for (let i = 0; i < keys.length; i++) {
        if (row[keys[i]]) {
          formData.options.push({ label: labels[i], content: row[keys[i]] })
        }
      }
      if (formData.options.length === 0) {
        formData.options = [
          { label: 'A', content: '' },
          { label: 'B', content: '' },
          { label: 'C', content: '' },
          { label: 'D', content: '' },
        ]
      }
    }
  }
  // 编程题测试用例兼容旧格式
  if (row.type === 'program' && (!row.testCasesList || row.testCasesList.length === 0) && row.testCases) {
    const lines = row.testCases.split('\n').filter((l: string) => l.trim())
    formData.testCasesList = lines.map((line: string, idx: number) => {
      const parts = line.split('|').map((s: string) => s.trim())
      return {
        input: parts[0] || '',
        expectedOutput: parts[1] || '',
        isSample: idx === 0,
      }
    })
    if (formData.testCasesList.length === 0) {
      formData.testCasesList = [{ input: '', expectedOutput: '', isSample: true }]
    }
  }
  form.value = formData
  formDialogVisible.value = true
}
function handleView(row: any) {
  viewRow.value = row
  viewDialogVisible.value = true
}
function handleDelete(row: any) {
  ElMessageBox.confirm(`确定删除题目 "${row.content.substring(0, 20)}..." 吗？`, '警告', { type: 'warning' })
    .then(async () => {
      try {
        await questionApi.remove(row.id)
        questionList.value = questionList.value.filter(q => q.id !== row.id)
        ElMessage.success('删除成功')
        opLog.logDelete('删除题目')
      } catch {
        ElMessage.error('操作失败，请检查网络或稍后重试')
      }
    })
    .catch(() => {})
}

// 表单
const formDialogVisible = ref(false)
const isEdit = ref(false)
const currentId = ref(0)
const submitting = ref(false)
const formRef = ref<FormInstance>()
const optionLabels = ['A', 'B', 'C', 'D', 'E', 'F']

interface OptionItem {
  label: string
  content: string
}

interface TestCaseItem {
  input: string
  expectedOutput: string
  isSample: boolean
}

function getDefaultForm() {
  return {
    type: 'single' as string,
    title: '' as string,
    content: '' as string,
    score: 5 as number,
    difficulty: 'easy' as string,
    tags: [] as string[],
    options: [
      { label: 'A', content: '' },
      { label: 'B', content: '' },
      { label: 'C', content: '' },
      { label: 'D', content: '' },
    ] as OptionItem[],
    answer: '' as string,
    answers: [] as string[],
    judgeDescription: '' as string,
    judgeBasis: '' as string,
    fillAnswers: [] as string[],
    fillAnalysis: '' as string,
    templateCode: '' as string,
    referenceCode: '' as string,
    language: 'python' as string,
    testCasesList: [{ input: '', expectedOutput: '', isSample: true }] as TestCaseItem[],
    timeLimit: 1000 as number,
    memoryLimit: 128 as number,
    analysis: '' as string,
  }
}

const form = ref(getDefaultForm())

// 填空题空位检测
const blankSlots = computed(() => {
  const matches = form.value.content.match(/__(\d+)__/g)
  if (!matches) return []
  const nums = matches.map(m => parseInt(m.replace(/__/g, '')))
  return [...new Set(nums)].sort((a, b) => a - b)
})

// 监听空位数量变化，自动调整 fillAnswers 数组长度
watch(blankSlots, (newSlots) => {
  const current = form.value.fillAnswers
  const newLen = newSlots.length
  if (newLen > current.length) {
    for (let i = current.length; i < newLen; i++) {
      current.push('')
    }
  } else if (newLen < current.length) {
    form.value.fillAnswers = current.slice(0, newLen)
  }
})

// 多选题验证规则
const multiAnswerRule = [
  {
    validator: (_rule: any, value: string[], callback: Function) => {
      if (!value || value.length < 2) {
        callback(new Error('多选题至少需要选择2个正确答案'))
      } else {
        callback()
      }
    },
    trigger: 'change',
  },
]

// 编程题测试用例验证规则
const testCaseRule = [
  {
    validator: (_rule: any, value: TestCaseItem[], callback: Function) => {
      if (!value || value.length === 0) {
        callback(new Error('请至少添加1个测试用例'))
      } else {
        const emptyCase = value.find(tc => !tc.input.trim() || !tc.expectedOutput.trim())
        if (emptyCase) {
          callback(new Error('每个测试用例的输入和预期输出都不能为空'))
        } else {
          callback()
        }
      }
    },
    trigger: 'change',
  },
]

const formRules: FormRules = {
  type: [{ required: true, message: '请选择题型', trigger: 'change' }],
  content: [{ required: true, message: '请输入题干', trigger: 'blur' }],
  difficulty: [{ required: true, message: '请选择难度', trigger: 'change' }],
}

function openAddDialog() {
  isEdit.value = false
  form.value = getDefaultForm()
  formDialogVisible.value = true
}
function onTypeChange() {
  const defaultForm = getDefaultForm()
  form.value.answer = defaultForm.answer
  form.value.answers = defaultForm.answers
  form.value.options = defaultForm.options
  form.value.judgeDescription = defaultForm.judgeDescription
  form.value.judgeBasis = defaultForm.judgeBasis
  form.value.fillAnswers = defaultForm.fillAnswers
  form.value.fillAnalysis = defaultForm.fillAnalysis
  form.value.templateCode = defaultForm.templateCode
  form.value.referenceCode = defaultForm.referenceCode
  form.value.language = defaultForm.language
  form.value.testCasesList = defaultForm.testCasesList
  form.value.timeLimit = defaultForm.timeLimit
  form.value.memoryLimit = defaultForm.memoryLimit
}
function addOption() {
  if (form.value.options.length < 6) {
    const nextLabel = optionLabels[form.value.options.length]
    if (nextLabel) {
      form.value.options.push({ label: nextLabel, content: '' })
    }
  }
}
function removeOption(idx: number) {
  if (form.value.options.length <= 2) return
  const removedLabel = form.value.options[idx].label
  form.value.options.splice(idx, 1)
  // 重新排列 label
  form.value.options.forEach((opt, i) => {
    opt.label = optionLabels[i]
  })
  // 清理已删除选项的答案引用
  if (form.value.type === 'single' && form.value.answer === removedLabel) {
    form.value.answer = ''
  }
  if (form.value.type === 'multiple') {
    form.value.answers = form.value.answers.filter(a => a !== removedLabel)
  }
}
function toggleMultiAnswer(label: string) {
  const idx = form.value.answers.indexOf(label)
  if (idx === -1) {
    form.value.answers.push(label)
  } else {
    form.value.answers.splice(idx, 1)
  }
}
function addTestCase() {
  if (form.value.testCasesList.length < 10) {
    form.value.testCasesList.push({ input: '', expectedOutput: '', isSample: false })
  }
}
function removeTestCase(idx: number) {
  if (form.value.testCasesList.length > 1) {
    form.value.testCasesList.splice(idx, 1)
  }
}

async function submitForm() {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (!valid) return
    // 额外的自定义验证
    const f = form.value
    if (f.type === 'single') {
      if (f.options.length < 2) {
        ElMessage.warning('单选题至少需要2个选项')
        return
      }
      if (!f.answer) {
        ElMessage.warning('请选择正确答案')
        return
      }
      const emptyOpt = f.options.find(o => !o.content.trim())
      if (emptyOpt) {
        ElMessage.warning('所有选项内容不能为空')
        return
      }
    }
    if (f.type === 'multiple') {
      if (f.options.length < 2) {
        ElMessage.warning('多选题至少需要2个选项')
        return
      }
      if (f.answers.length < 2) {
        ElMessage.warning('多选题至少需要选择2个正确答案')
        return
      }
      const emptyOpt = f.options.find(o => !o.content.trim())
      if (emptyOpt) {
        ElMessage.warning('所有选项内容不能为空')
        return
      }
    }
    if (f.type === 'judge') {
      if (!f.answer) {
        ElMessage.warning('请选择正确答案')
        return
      }
    }
    if (f.type === 'fill') {
      const blanks = f.content.match(/__(\d+)__/g)
      if (!blanks || blanks.length === 0) {
        ElMessage.warning('请在题干中使用 __n__ 标记空位')
        return
      }
      const blankCount = [...new Set(blanks.map(m => parseInt(m.replace(/__/g, ''))))].length
      if (f.fillAnswers.length !== blankCount) {
        ElMessage.warning(`题干中标记了 ${blankCount} 个空位，但只填写了 ${f.fillAnswers.length} 个答案`)
        return
      }
      const emptyAnswer = f.fillAnswers.find(a => !a.trim())
      if (emptyAnswer) {
        ElMessage.warning('所有空的答案不能为空')
        return
      }
    }
    if (f.type === 'program') {
      if (!f.templateCode.trim()) {
        ElMessage.warning('请输入模板代码')
        return
      }
      if (f.testCasesList.length === 0) {
        ElMessage.warning('请至少添加1个测试用例')
        return
      }
      const emptyTC = f.testCasesList.find(tc => !tc.input.trim() || !tc.expectedOutput.trim())
      if (emptyTC) {
        ElMessage.warning('每个测试用例的输入和预期输出都不能为空')
        return
      }
    }

    submitting.value = true
    try {
      const payload: any = {
        title: f.title || f.content,
        type: f.type,
        score: f.score,
        analysis: f.analysis,
        content: f.content,
        difficulty: f.difficulty,
        tags: f.tags,
        options: JSON.stringify(f.options),
        answer: f.answer,
        answers: f.answers,
        fillAnswers: f.fillAnswers,
        fillAnalysis: f.fillAnalysis,
        judgeDescription: f.judgeDescription,
        judgeBasis: f.judgeBasis,
        language: f.language,
        templateCode: f.templateCode,
        referenceCode: f.referenceCode,
        testCasesList: f.testCasesList,
        timeLimit: f.timeLimit,
        memoryLimit: f.memoryLimit,
      }
      if (isEdit.value) {
        await questionApi.update(currentId.value, payload)
        const idx = questionList.value.findIndex(q => q.id === currentId.value)
        if (idx !== -1) questionList.value[idx] = { ...questionList.value[idx], ...payload, id: currentId.value }
        ElMessage.success('编辑成功')
        opLog.logUpdate('编辑题目')
      } else {
        const res = await questionApi.create(payload)
        const newId = res?.data?.id ?? questionList.value.length + 1
        questionList.value.unshift({ ...payload, id: newId })
        ElMessage.success('添加成功')
        opLog.logCreate('新增题目')
      }
      formDialogVisible.value = false
    } catch {
      ElMessage.error('操作失败，请检查网络或稍后重试')
    } finally {
      submitting.value = false
    }
  })
}

// 查看详情
const viewDialogVisible = ref(false)
const viewRow = ref<any>(null)

// 批量导入
const importDialogVisible = ref(false)
const importing = ref(false)
const importFile = ref<any>(null)
const importResult = ref<any[]>([])

function handleImportFileChange(file: any) {
  importFile.value = file
}
function downloadTemplate() {
  ElMessage.success('模板下载中...')
}
function confirmImport() {
  if (!importFile.value) {
    ElMessage.warning('请先选择文件')
    return
  }
  ElMessage.warning('该能力暂未接入后端服务，操作未生效')
}

// 组卷
const composeDialogVisible = ref(false)
const composeSearch = ref('')
const composeType = ref('')
const composeSelected = ref<any[]>([])
let dragIndex = -1

const composeAvailableList = computed(() => {
  let list = questionList.value.filter(q => !composeSelected.value.some(s => s.id === q.id))
  if (composeSearch.value) {
    list = list.filter(q => q.content.includes(composeSearch.value))
  }
  if (composeType.value) {
    list = list.filter(q => q.type === composeType.value)
  }
  return list
})

function openComposeDialog() {
  composeSelected.value = []
  composeSearch.value = ''
  composeType.value = ''
  composeDialogVisible.value = true
}
function addToPaper(q: any) {
  composeSelected.value.push({ ...q, score: q.score || 5 })
}
function removeFromPaper(idx: number) {
  composeSelected.value.splice(idx, 1)
}
function dragStart(idx: number) {
  dragIndex = idx
}
function drop(idx: number) {
  if (dragIndex === -1 || dragIndex === idx) return
  const item = composeSelected.value.splice(dragIndex, 1)[0]
  composeSelected.value.splice(idx, 0, item)
  dragIndex = -1
}
function batchSetScore() {
  ElMessageBox.prompt('请输入分值', '批量设置分值', { inputType: 'number' })
    .then(({ value }) => {
      const score = Number(value)
      composeSelected.value.forEach(q => (q.score = score))
      ElMessage.success('设置成功')
    })
    .catch(() => {})
}
function confirmCompose() {
  if (composeSelected.value.length === 0) {
    ElMessage.warning('请先选择题目')
    return
  }
  ElMessage.success(`已选择 ${composeSelected.value.length} 道题目加入试卷`)
  composeDialogVisible.value = false
}
</script>

<style scoped lang="scss">
$primary: #165DFF;

.question-bank-page { padding: 20px; }
.page-header { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px;
  h2 { font-size: 20px; color: #1D2129; }
  .header-actions { display: flex; gap: 8px; }
}
.type-tabs { margin-bottom: 16px; }
.search-bar { display: flex; gap: 12px; margin-bottom: 16px; flex-wrap: wrap; }
.tag-item { margin-right: 4px; }

.form-tip {
  color: #86909C;
  font-size: 12px;
  line-height: 1.5;
  code {
    background: #f5f7fa;
    padding: 1px 4px;
    border-radius: 3px;
    color: #165DFF;
    font-family: 'Courier New', monospace;
  }
}

.option-row {
  display: flex;
  gap: 8px;
  align-items: center;
  width: 100%;
}

.blank-info {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px;
}

.test-case-list {
  width: 100%;
  .test-case-item {
    background: #F7F8FA;
    border: 1px solid #F0F0F0;
    border-radius: 6px;
    padding: 12px;
    margin-bottom: 12px;
    .test-case-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 8px;
      .test-case-index {
        font-weight: bold;
        font-size: 13px;
        color: $text-primary;
      }
    }
  }
}

.import-body { .import-tip { color: #666; margin-bottom: 12px; }
  .import-actions { margin-top: 12px; text-align: center; }
  .import-result { margin-top: 16px; padding: 12px; background: #F7F8FA; border-radius: 6px; }
}

.compose-body { display: flex; gap: 16px; height: 500px;
  .compose-left, .compose-right { flex: 1; display: flex; flex-direction: column; border: 1px solid #E5E7EB; border-radius: 6px; padding: 12px; overflow: hidden;
    h4 { margin-bottom: 8px; font-size: 14px; color: $text-primary; }
  }
  .compose-list { flex: 1; overflow-y: auto; margin-top: 8px;
    .compose-item { display: flex; align-items: center; gap: 8px; padding: 8px; border-bottom: 1px solid #f0f0f0; cursor: pointer;
      &:hover { background: #f5f7fa; }
      .compose-item-type { flex-shrink: 0; font-size: 12px; color: $primary; background: rgba($primary, 0.1); padding: 2px 6px; border-radius: 4px; }
      .compose-item-index { flex-shrink: 0; font-size: 12px; color: #666; width: 20px; }
      .compose-item-content { flex: 1; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; font-size: 13px; }
      &.selected { cursor: move; }
    }
  }
  .compose-actions { display: flex; justify-content: space-between; margin-top: 12px; }
}

.view-body {
  line-height: 1.8;
  color: $text-primary;
  p { margin-bottom: 8px; }
  .view-section {
    margin-bottom: 20px;
    padding-bottom: 16px;
    border-bottom: 1px solid #f0f0f0;
    &:last-child { border-bottom: none; margin-bottom: 0; padding-bottom: 0; }
    h4 {
      font-size: 15px;
      color: #1D2129;
      margin-bottom: 12px;
      padding-left: 8px;
      border-left: 3px solid $primary;
    }
  }
  .view-content-block {
    background: #F7F8FA;
    padding: 10px 14px;
    border-radius: 6px;
    margin: 6px 0 10px;
    line-height: 1.7;
    white-space: pre-wrap;
  }
  .view-option-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px 12px;
    margin-bottom: 6px;
    border-radius: 6px;
    background: #F7F8FA;
    border: 1px solid #F0F0F0;
    &.is-correct {
      background: #E8FFF5;
      border-color: #E8FFF5;
    }
    .view-option-label {
      font-weight: bold;
      color: $text-primary;
      flex-shrink: 0;
      width: 24px;
    }
    .view-option-content {
      flex: 1;
    }
  }
  .view-blank-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 6px 12px;
    margin-bottom: 6px;
    background: #F7F8FA;
    border-radius: 6px;
    .view-blank-answer {
      font-weight: bold;
      color: #165DFF;
    }
  }
  .view-test-case {
    background: #F7F8FA;
    border: 1px solid #F0F0F0;
    border-radius: 6px;
    padding: 12px;
    margin-bottom: 10px;
    .view-test-case-header {
      display: flex;
      align-items: center;
      gap: 8px;
      margin-bottom: 8px;
      font-weight: bold;
      font-size: 13px;
    }
    .view-test-case-row {
      display: flex;
      align-items: flex-start;
      gap: 8px;
      margin-bottom: 6px;
      .view-tc-label {
        flex-shrink: 0;
        font-size: 13px;
        color: #666;
        min-width: 60px;
      }
    }
  }
  .code-block {
    background: #1D2129;
    color: #E5E6EB;
    padding: 14px;
    border-radius: 6px;
    overflow-x: auto;
    font-family: 'Courier New', monospace;
    font-size: 13px;
    line-height: 1.6;
    margin: 6px 0;
    &.code-block-inline {
      margin: 0;
      flex: 1;
      white-space: pre-wrap;
      word-break: break-all;
    }
  }
}
</style>
