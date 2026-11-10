<template>
  <!-- 公共编辑器父组件：统一布局、工具栏、输出面板，根据 lang 动态渲染子编辑器 -->
  <div class="base-editor" :class="{ 'exam-mode': config.mode === 'exam', 'standalone-iframe': isStandaloneIframe, 'theme-dark': uiTheme === 'dark', 'theme-light': uiTheme === 'light' }">

    <!-- P0-1 顶部考试信息条：题号 / 倒计时 / 语言 / 已提交次数 -->
    <div v-if="showInfoBar" class="exam-info-bar">
      <div class="info-item">
        <span class="info-label">题号</span>
        <span class="info-value">#{{ questionNoDisplay }}</span>
      </div>
      <div v-if="countdownSeconds != null" class="info-item">
        <span class="info-label">倒计时</span>
        <span class="info-value countdown">{{ countdownText }}</span>
      </div>
      <div class="info-item">
        <span class="info-label">语言</span>
        <span class="info-value">{{ langLabel }}</span>
      </div>
      <div class="info-item">
        <span class="info-label">已提交</span>
        <span class="info-value">{{ submitCountDisplay }} 次</span>
      </div>
      <div class="info-spacer"></div>
      <div v-if="editorLocked" class="info-locked">
        <el-icon><Lock /></el-icon>
        <span>已锁定</span>
      </div>
    </div>

    <!-- 工具栏（仅工具类：格式化/字号/主题/模板/复制/下载/全屏；主操作在底栏） -->
    <EditorToolbar
      v-if="!isStandaloneIframe"
      :running="running"
      :submitting="submitting"
      :show-save="false"
      :lang-label="langLabel"
      :theme="uiTheme"
      :current-font-size="fontSize"
      @format="handleFormat"
      @fullscreen="handleFullscreen"
      @copy="handleCopy"
      @download="handleDownload"
      @font-size="handleFontSize"
      @toggle-theme="handleToggleTheme"
      @open-templates="handleOpenTemplates"
    />

    <!-- 代码类编辑器（Python / PythonTurtle / C++）：三栏 代码 | 分隔条 | 结果 -->
    <div v-if="!isStandaloneIframe && !isScratchLang" class="editor-body" ref="editorBodyRef">
      <div class="code-pane" ref="codePaneRef" :style="{ flexBasis: codePaneBasis + '%' }">
        <component
          :is="currentEditorComponent"
          ref="childEditorRef"
          v-model="code"
          :lang="config.lang"
          :read-only="config.readOnly"
          :template-code="config.templateCode"
          :height="editorHeight"
          :theme="monacoTheme"
          :font-size="fontSize"
          :exam-mode="config.mode === 'exam'"
          :locked="editorLocked"
          :turtle-host="turtleHostEl"
          @save-scratch="handleScratchSave"
          @screen-switch="handleScreenSwitch"
          @asset-import-attempt="handleAssetImportAttempt"
          @submit-success="handleChildSubmitSuccess"
        />
      </div>

      <div class="splitter" @mousedown="startSplitDrag" title="拖拽调整宽度"></div>

      <!-- P1-2 右栏标签页：控制台 / 结果对比 / 图形输出 -->
      <div class="result-pane">
        <el-tabs v-model="activeResultTab" class="result-tabs" stretch>
          <el-tab-pane label="控制台" name="console">
            <EditorOutput
              :output="runOutput"
              :status="runStatus"
              :judge-result="judgeResult"
              :error-line="runErrorLine"
              @clear="runOutput = ''"
              @reveal-line="onRevealLine"
            />
          </el-tab-pane>
          <el-tab-pane label="结果对比" name="compare">
            <div class="compare-pane">
              <template v-if="lastCompare">
                <div class="compare-row">
                  <span class="compare-label">期望输出</span>
                  <pre>{{ lastCompare.expected }}</pre>
                </div>
                <div class="compare-row">
                  <span class="compare-label">实际输出</span>
                  <pre :class="{ mismatch: !lastCompare.matched }">{{ lastCompare.actual }}</pre>
                </div>
                <div class="compare-result" :class="lastCompare.matched ? 'ok' : 'bad'">
                  {{ lastCompare.matched ? '一致 ✓' : '不一致 ✗' }}
                </div>
              </template>
              <div v-else class="compare-empty">
                在题目区点击「运行对比」后，将在此显示期望与实际输出的比对结果。
              </div>
            </div>
          </el-tab-pane>
          <!-- P2 Turtle 画布降级为右栏图形输出 tab -->
          <el-tab-pane v-if="isTurtleLang" label="图形输出" name="graph">
            <div class="turtle-host" ref="turtleHostEl"></div>
          </el-tab-pane>
        </el-tabs>
      </div>
    </div>

    <!-- Scratch / GoC：iframe 填满（GoC 为独立 iframe，自带工具栏与控制台） -->
    <div v-else class="editor-main" ref="editorMainRef">
      <component
        :is="currentEditorComponent"
        ref="childEditorRef"
        v-model="code"
        :lang="config.lang"
        :read-only="config.readOnly"
        :template-code="config.templateCode"
        :height="editorHeight"
        :theme="monacoTheme"
        :font-size="fontSize"
        :exam-mode="config.mode === 'exam'"
        :locked="editorLocked"
        @save-scratch="handleScratchSave"
        @screen-switch="handleScreenSwitch"
        @asset-import-attempt="handleAssetImportAttempt"
        @submit-success="handleChildSubmitSuccess"
      />
    </div>

    <!-- 自动保存状态提示 -->
    <div v-if="config.mode !== 'exam' && hasUnsavedChanges" class="save-indicator">
      <el-icon class="is-loading"><Loading /></el-icon>
      <span>{{ autoSaveSaving ? '保存中...' : '有未保存的更改' }}</span>
    </div>

    <!-- P0-2 底部统一动作栏：运行 / 提交 / 重置 / 草稿（Scratch 额外带 示例/导出/缩略图） -->
    <div v-if="!isStandaloneIframe" class="action-bar">
      <div class="action-left">
        <el-button type="primary" :icon="VideoPlay" @click="handleRun" :loading="running">运行</el-button>
        <el-button v-if="running" :icon="VideoPause" @click="handleStop" plain>停止</el-button>
        <el-button v-if="!hideSubmit" type="success" :icon="Upload" @click="handleSubmit" :loading="submitting">提交</el-button>
        <el-button :icon="RefreshRight" @click="handleReset" plain>重置</el-button>
        <el-button v-if="config.mode !== 'exam'" :icon="Document" @click="handleSave" plain>草稿</el-button>
      </div>
      <div v-if="isScratchLang" class="action-right">
        <el-button v-if="isScratch3" :icon="Document" @click="childScratchExample">示例项目</el-button>
        <el-button :icon="Download" @click="childScratchExport">导出 sb3</el-button>
        <el-button :icon="Picture" @click="childScratchThumb">缩略图</el-button>
      </div>
    </div>

    <!-- 代码模板库抽屉 -->
    <el-drawer
      v-model="templateDrawerVisible"
      title="插入示例代码"
      direction="rtl"
      size="360px"
    >
      <p class="tpl-hint">点击任意模板，将其插入到编辑器光标处。</p>
      <div v-if="templateList.length === 0" class="tpl-empty">
        当前语言暂不支持代码片段插入（积木类编辑器请在画布内使用示例项目）。
      </div>
      <el-collapse v-model="activeTemplateGroup">
        <el-collapse-item
          v-for="group in templateList"
          :key="group.group"
          :title="group.group"
          :name="group.group"
        >
          <div
            v-for="tpl in group.items"
            :key="tpl.name"
            class="tpl-item"
            @click="insertTemplate(tpl)"
          >
            <div class="tpl-name">{{ tpl.name }}</div>
            <pre class="tpl-code">{{ tpl.preview }}</pre>
          </div>
        </el-collapse-item>
      </el-collapse>
    </el-drawer>
  </div>
</template>

<script setup lang="ts">
/**
 * BaseEditor - 公共编辑器父组件
 * 统一布局外壳，通过 lang 动态加载子编辑器
 * 集成自动保存、提交校验、运行输出
 * 支持 Scratch/ScratchJr 舞台预览面板和提交锁定
 *
 * 布局（P0→P2 重构）：
 * - 顶部考试信息条（题号/倒计时/语言/提交次数）
 * - 代码类：左代码 + 分隔条 + 右结果（标签页：控制台/结果对比/图形输出）
 * - 底部统一动作栏：运行/提交/重置/草稿（Scratch 额外带示例/导出/缩略图）
 */
import { ref, computed, onMounted, onBeforeUnmount, watch, defineAsyncComponent, h, type Component } from 'vue'
import { ElMessage } from 'element-plus'
import { Loading, Lock, VideoPlay, VideoPause, Upload, RefreshRight, Document, Download, Picture } from '@element-plus/icons-vue'
import EditorToolbar from './EditorToolbar.vue'
import EditorOutput from './EditorOutput.vue'
import { useAutoSave } from '@/hooks/useAutoSave'
import { useSubmitValidation } from '@/hooks/useSubmitValidation'
import { runCode } from '@/api/judge'
import type { EditorConfig, EditorLang, RunStatus, JudgeResult } from '@/types/editor'
import { LANG_LABELS as langLabels } from '@/types/editor'

const props = defineProps<{
  config: EditorConfig
  /** 外部传入的初始代码 */
  initialCode?: string
  /** 编辑器高度 */
  editorHeight?: number | string
  /** 题号（信息条展示，可选） */
  questionNo?: number | string
  /** 已提交次数（信息条展示，可选） */
  submitCount?: number
  /** 倒计时秒数（父级驱动，可选；提供则在顶栏信息条显示） */
  countdownSeconds?: number
  /** 是否隐藏底栏"提交"按钮（外层已有独立提交流程时使用，如竞赛房间走 competition 接口） */
  hideSubmit?: boolean
}>()

const emit = defineEmits<{
  /** 代码变更 */
  'code-change': [code: string]
  /** 提交成功 */
  'submit-success': [result: JudgeResult]
  /** 运行完成 */
  'run-complete': [output: string]
}>()

// ========== 子编辑器动态加载 ==========
const ScratchChildEditor = defineAsyncComponent({
  loader: () => import('./child/ScratchChildEditor.vue'),
  loadingComponent: () => null,
  errorComponent: () => h('div', { style: 'padding: 20px; color: #86909C;' }, 'Scratch 编辑器加载失败'),
  timeout: 10000,
})
const ScratchJrChildEditor = defineAsyncComponent({
  loader: () => import('./child/ScratchJrChildEditor.vue'),
  loadingComponent: () => null,
  errorComponent: () => h('div', { style: 'padding: 20px; color: #86909C;' }, 'ScratchJr 编辑器加载失败'),
  timeout: 10000,
})
const PythonChildEditor = defineAsyncComponent({
  loader: () => import('./child/PythonChildEditor.vue'),
  loadingComponent: () => null,
  errorComponent: () => h('div', { style: 'padding: 20px; color: #86909C;' }, 'Python 编辑器加载失败'),
  timeout: 10000,
})
const CppChildEditor = defineAsyncComponent({
  loader: () => import('./child/CppChildEditor.vue'),
  loadingComponent: () => null,
  errorComponent: () => h('div', { style: 'padding: 20px; color: #86909C;' }, 'C++ 编辑器加载失败'),
  timeout: 10000,
})
const GoCChildEditor = defineAsyncComponent({
  loader: () => import('./child/GoCChildEditor.vue'),
  loadingComponent: () => null,
  errorComponent: () => h('div', { style: 'padding: 20px; color: #86909C;' }, 'GoC 编辑器加载失败'),
  timeout: 10000,
})

/** 根据 lang 映射子编辑器组件 */
const editorComponentMap: Record<EditorLang, Component> = {
  scratch3: ScratchChildEditor,
  scratchjr: ScratchJrChildEditor,
  python: PythonChildEditor,
  pythonturtle: PythonChildEditor,
  cpp: CppChildEditor,
  goc: GoCChildEditor,
}

const currentEditorComponent = computed(() => editorComponentMap[props.config.lang])
const langLabel = computed(() => langLabels[props.config.lang])

// ========== 外观与偏好状态 ==========
const uiTheme = ref<'dark' | 'light'>('dark')
const fontSize = ref<number>(14)
const monacoTheme = computed(() => (uiTheme.value === 'dark' ? 'vs-dark' : 'vs'))

// ========== P0-1 顶部信息条 ==========
const questionNoDisplay = computed(() => String(props.questionNo ?? ''))
const submitCountDisplay = computed(() => Number(props.submitCount ?? 0))
const showInfoBar = computed(() =>
  props.questionNo != null || props.submitCount != null || props.countdownSeconds != null
)
// P0-1 倒计时：父级驱动显示（父级负责维护剩余秒数，避免与父级倒计时双重递减）
const countdownText = computed(() => (props.countdownSeconds == null ? '' : formatCountdown(props.countdownSeconds)))
function formatCountdown(sec: number): string {
  const h = Math.floor(sec / 3600)
  const m = Math.floor((sec % 3600) / 60)
  const s = sec % 60
  const pad = (n: number) => String(n).padStart(2, '0')
  return h > 0 ? `${h}:${pad(m)}:${pad(s)}` : `${pad(m)}:${pad(s)}`
}

// ========== P1-1 三栏可拖拽分隔 ==========
const editorBodyRef = ref<HTMLElement>()
const codePaneRef = ref<HTMLElement>()
const codePaneBasis = ref<number>(58)
const isTurtleLang = computed(() => props.config.lang === 'pythonturtle')
const isScratch3 = computed(() => props.config.lang === 'scratch3')

function startSplitDrag() {
  window.addEventListener('mousemove', onSplitDrag)
  window.addEventListener('mouseup', stopSplitDrag)
  document.body.style.userSelect = 'none'
  document.body.style.cursor = 'col-resize'
}
function onSplitDrag(e: MouseEvent) {
  const body = editorBodyRef.value
  if (!body) return
  const rect = body.getBoundingClientRect()
  let pct = ((e.clientX - rect.left) / rect.width) * 100
  pct = Math.min(75, Math.max(28, pct))
  codePaneBasis.value = pct
}
function stopSplitDrag() {
  window.removeEventListener('mousemove', onSplitDrag)
  window.removeEventListener('mouseup', stopSplitDrag)
  document.body.style.userSelect = ''
  document.body.style.cursor = ''
}

// P1-2 右栏标签页状态 + 结果对比数据
const activeResultTab = ref<'console' | 'compare' | 'graph'>('console')
const turtleHostEl = ref<HTMLElement | null>(null)
const lastCompare = ref<{ expected: string; actual: string; matched: boolean } | null>(null)

// ========== 代码模板库 ==========
interface CodeTemplate { name: string; code: string; preview: string }
interface TemplateGroup { group: string; items: CodeTemplate[] }

const TEMPLATES: Record<string, TemplateGroup[]> = {
  python: [
    { group: '基础结构', items: [
      { name: 'for 循环', code: 'for i in range(10):\n    print(i)\n', preview: 'for i in range(10):\n    print(i)' },
      { name: 'while 循环', code: 'n = 0\nwhile n < 10:\n    print(n)\n    n += 1\n', preview: 'while n < 10:\n    ...' },
      { name: '函数定义', code: 'def add(a, b):\n    return a + b\n\nprint(add(1, 2))\n', preview: 'def add(a, b):\n    return a + b' },
      { name: '列表推导', code: 'squares = [x * x for x in range(10)]\nprint(squares)\n', preview: 'squares = [x*x for x in range(10)]' },
    ]},
    { group: '输入处理', items: [
      { name: '读取一行输入', code: 's = input()\nprint(s)\n', preview: 's = input()' },
      { name: '读取多个整数', code: 'a, b = map(int, input().split())\nprint(a + b)\n', preview: 'a, b = map(int, input().split())' },
    ]},
    { group: 'Turtle 绘图', items: [
      { name: '画正方形', code: "import turtle\nt = turtle.Turtle()\nfor _ in range(4):\n    t.forward(100)\n    t.right(90)\n", preview: 'for _ in range(4):\n    t.forward(100)' },
      { name: '画圆', code: "import turtle\nt = turtle.Turtle()\nt.circle(80)\n", preview: 't.circle(80)' },
    ]},
    { group: '进阶算法', items: [
      { name: '二分查找', code: 'def bisect(a, x):\n    l, r = 0, len(a) - 1\n    while l <= r:\n        m = (l + r) // 2\n        if a[m] >= x:\n            r = m - 1\n        else:\n            l = m + 1\n    return l\n', preview: 'bisect 左边界' },
      { name: '递归阶乘', code: 'def fact(n):\n    return 1 if n <= 1 else n * fact(n - 1)\n\nprint(fact(5))\n', preview: 'def fact(n)' },
      { name: '字典计数', code: "from collections import Counter\nc = Counter('abracadabra')\nprint(c['a'])\n", preview: 'Counter 计数' },
      { name: '读写文件', code: "with open('data.txt', 'r', encoding='utf-8') as f:\n    lines = f.readlines()\nprint(len(lines))\n", preview: 'with open(...) as f' },
    ]},
  ],
  pythonturtle: [
    { group: 'Turtle 绘图', items: [
      { name: '画正方形', code: "import turtle\nt = turtle.Turtle()\nfor _ in range(4):\n    t.forward(100)\n    t.right(90)\n", preview: 'for _ in range(4):\n    t.forward(100)' },
      { name: '画圆', code: "import turtle\nt = turtle.Turtle()\nt.circle(80)\n", preview: 't.circle(80)' },
      { name: '彩色螺旋', code: "import turtle\nt = turtle.Turtle()\ncolors = ['red','green','blue','yellow']\nfor i in range(60):\n    t.color(colors[i % 4])\n    t.forward(i * 3)\n    t.right(61)\n", preview: '螺旋：forward + right 重复' },
    ]},
  ],
  cpp: [
    { group: '基础结构', items: [
      { name: 'for 循环', code: 'for (int i = 0; i < 10; i++) {\n    cout << i << endl;\n}\n', preview: 'for (int i = 0; i < 10; i++)' },
      { name: 'while 循环', code: 'int n = 0;\nwhile (n < 10) {\n    cout << n << endl;\n    n++;\n}\n', preview: 'while (n < 10)' },
      { name: '函数定义', code: 'int add(int a, int b) {\n    return a + b;\n}\n\nint main() {\n    cout << add(1, 2) << endl;\n    return 0;\n}\n', preview: 'int add(int a, int b)' },
    ]},
    { group: '输入处理', items: [
      { name: '读取两个整数', code: 'int a, b;\ncin >> a >> b;\ncout << a + b << endl;\n', preview: 'cin >> a >> b;' },
      { name: '快读模板', code: 'inline int read() {\n    int x = 0, f = 1; char c = getchar();\n    while (c < \'0\' || c > \'9\') { if (c == \'-\') f = -1; c = getchar(); }\n    while (c >= \'0\' && c <= \'9\') { x = x * 10 + c - \'0\'; c = getchar(); }\n    return x * f;\n}\n', preview: 'inline int read() { ... }' },
    ]},
    { group: '常用算法', items: [
      { name: '数组排序', code: 'int a[100];\nsort(a, a + n);\n', preview: 'sort(a, a + n);' },
      { name: '最大值', code: 'int mx = *max_element(a, a + n);\n', preview: 'max_element(a, a + n)' },
      { name: '二分查找', code: 'int l = 0, r = n - 1, ans = -1;\nwhile (l <= r) {\n    int m = (l + r) / 2;\n    if (a[m] >= x) { ans = m; r = m - 1; }\n    else l = m + 1;\n}\n', preview: '>=x 的第一个位置' },
      { name: '质数判断', code: 'bool isPrime(int n) {\n    if (n < 2) return false;\n    for (int i = 2; i * i <= n; i++)\n        if (n % i == 0) return false;\n    return true;\n}\n', preview: 'bool isPrime(int n)' },
      { name: '最大公约数', code: 'int gcd(int a, int b) {\n    return b ? gcd(b, a % b) : a;\n}\n', preview: 'gcd(a, b)' },
    ]},
    { group: 'STL 容器', items: [
      { name: 'vector 使用', code: '#include <vector>\nvector<int> v;\nv.push_back(1);\nv.push_back(2);\nfor (int x : v) cout << x << " ";\n', preview: 'vector<int> v;' },
      { name: 'map 使用', code: '#include <map>\nmap<string, int> mp;\nmp["a"] = 1;\ncout << mp["a"] << endl;\n', preview: 'map<string, int> mp;' },
      { name: 'set 使用', code: '#include <set>\nset<int> s;\ns.insert(3); s.insert(1);\nfor (int x : s) cout << x << " ";\n', preview: 'set<int> s;' },
      { name: '字符串处理', code: '#include <string>\nstring s = "hello";\ncout << s.length() << " " << s.substr(1, 3) << endl;\n', preview: 'string s = "hello";' },
    ]},
    { group: '搜索与图', items: [
      { name: 'DFS 递归', code: 'void dfs(int u) {\n    vis[u] = 1;\n    for (int v : g[u])\n        if (!vis[v]) dfs(v);\n}\n', preview: 'void dfs(int u)' },
      { name: 'BFS 队列', code: '#include <queue>\nqueue<int> q; q.push(s); vis[s] = 1;\nwhile (!q.empty()) {\n    int u = q.front(); q.pop();\n    for (int v : g[u])\n        if (!vis[v]) { vis[v] = 1; q.push(v); }\n}\n', preview: 'BFS with queue' },
    ]},
  ],
  goc: [
    { group: '绘图基础', items: [
      { name: '前进并转向', code: 'fd(100);\nrt(90);\n', preview: 'fd(100); rt(90);' },
      { name: '画正方形', code: 'for (int i = 0; i < 4; i++) {\n    fd(100);\n    rt(90);\n}\n', preview: 'for ... fd(100); rt(90);' },
      { name: '画圆', code: 'for (int i = 0; i < 360; i++) {\n    fd(1);\n    rt(1);\n}\n', preview: 'for i in 360: fd(1); rt(1);' },
    ]},
    { group: '进阶绘图', items: [
      { name: '彩色正方形', code: 'pen(^2^);\nfor (int i = 0; i < 4; i++) {\n    fd(100);\n    rt(90);\n}\n', preview: 'pen(2); 彩色正方形' },
      { name: '螺旋线', code: 'for (int i = 1; i <= 100; i++) {\n    fd(i);\n    rt(20);\n}\n', preview: '螺旋：fd(i); rt(20);' },
      { name: '正多边形', code: 'int n = ^6^;\nfor (int i = 0; i < n; i++) {\n    fd(80);\n    rt(360 / n);\n}\n', preview: '正 n 边形' },
      { name: '五角星', code: 'for (int i = 0; i < 5; i++) {\n    fd(100);\n    rt(144);\n}\n', preview: '五角星 rt(144)' },
      { name: '填充圆', code: 'oo(^1^);\nfor (int i = 0; i < 360; i++) {\n    fd(1);\n    rt(1);\n}\noc();\n', preview: 'oo(1); 画圆; oc();' },
      { name: '彩色螺旋', code: 'for (int i = 0; i < 100; i++) {\n    pen(i % 16);\n    fd(i / 2);\n    rt(35);\n}\n', preview: 'pen 循环换色螺旋' },
    ]},
  ],
}

const templateDrawerVisible = ref(false)
const activeTemplateGroup = ref<string[]>([])
const templateList = computed<TemplateGroup[]>(() => {
  const list = TEMPLATES[props.config.lang] || []
  if (list.length && activeTemplateGroup.value.length === 0) {
    activeTemplateGroup.value = [list[0].group]
  }
  return list
})

// ========== 编辑器状态 ==========
const code = ref(props.initialCode || props.config.templateCode || '')
const childEditorRef = ref<any>(null)

// 监听外部 initialCode 变化：模板代码可能在组件挂载后异步加载（如后端返回或 mock 数据），
// 此时需同步到 code，否则 iframe 类编辑器（GoC/Scratch）的父级 code 为空，
// 导致点击"运行"时被 `!code.value.trim()` 拦截。仅当用户尚未编辑时同步，避免覆盖用户输入。
watch(
  () => props.initialCode,
  (newCode) => {
    if (newCode && !code.value) {
      code.value = newCode
    }
  }
)
watch(
  () => props.config.templateCode,
  (newCode) => {
    if (newCode && !code.value) {
      code.value = newCode
    }
  }
)
const editorMainRef = ref<HTMLElement>()

const running = ref(false)
const runOutput = ref('')
const runStatus = ref<RunStatus>('idle')
const runErrorLine = ref<number | undefined>(undefined)

/** 编辑器是否已锁定（提交后锁定） */
const editorLocked = ref(false)

/** 是否为 Scratch 类语言 */
const isScratchLang = computed(() => {
  return props.config.lang === 'scratch3' || props.config.lang === 'scratchjr'
})

/** 是否为独立 iframe 编辑器（自带工具栏和控制台，如 GoC） */
const isStandaloneIframe = computed(() => {
  return props.config.lang === 'goc'
})

// ========== 自动保存 ==========
const {
  saving: autoSaveSaving,
  hasUnsavedChanges,
  load: loadDraft,
  save: saveDraft,
  markDirty,
} = useAutoSave({
  questionId: props.config.questionId,
  lang: props.config.lang,
  isExam: props.config.mode === 'exam',
  interval: 30000,
})

// ========== 提交校验 ==========
const {
  submitting,
  judgeResult,
  submit,
} = useSubmitValidation({
  questionId: props.config.questionId,
  lang: props.config.lang,
  examId: props.config.examId,
  isExam: props.config.mode === 'exam',
  onSuccess: (result) => {
    // 提交成功后锁定编辑器
    lockEditor()
    emit('submit-success', result)
  },
})

// ========== 监听代码变更 ==========
watch(code, (newCode) => {
  emit('code-change', newCode)
  markDirty(newCode)
})

// ========== 操作方法 ==========

/** 本地模拟运行（开发环境后端不可用时使用） */
function mockRunLocal(lang: string, code: string): { success: boolean; output: string; errorMessage?: string } {
  const outputLines: string[] = []
  let error: string | undefined

  try {
    if (lang === 'python') {
      // 模拟 Python 运行：解析简单的 print 语句
      const lines = code.split('\n')
      for (const line of lines) {
        const trimmed = line.trim()
        if (!trimmed || trimmed.startsWith('#')) continue

        // 匹配 print("...") 或 print('...')
        const printMatch = trimmed.match(/^print\s*\(\s*["'](.+?)["']\s*\)/)
        if (printMatch) {
          outputLines.push(printMatch[1])
          continue
        }
        // 匹配 print(...)
        const printVarMatch = trimmed.match(/^print\s*\(\s*(.+?)\s*\)/)
        if (printVarMatch) {
          const expr = printVarMatch[1].trim()
          // 简单数学表达式求值
          try {
            // eslint-disable-next-line no-eval
            const val = eval(expr)
            outputLines.push(String(val))
          } catch {
            outputLines.push(expr)
          }
          continue
        }
        // 变量赋值（不输出）
        if (/^[a-zA-Z_]\w*\s*=/.test(trimmed)) {
          continue
        }
        // 不支持的语法提示
        if (trimmed) {
          error = `本地模拟模式不支持该语法: ${trimmed.substring(0, 30)}`
          break
        }
      }
    } else if (lang === 'cpp') {
      // 模拟 C++ 运行：解析 cout
      const lines = code.split('\n')
      for (const line of lines) {
        const trimmed = line.trim()
        if (!trimmed || trimmed.startsWith('//') || trimmed.startsWith('#')) continue

        const coutMatch = trimmed.match(/cout\s*<<\s*["'](.+?)["']\s*<</)
        if (coutMatch) {
          outputLines.push(coutMatch[1])
          continue
        }
        const coutEndMatch = trimmed.match(/cout\s*<<\s*["'](.+?)["']\s*<<?\s*endl/)
        if (coutEndMatch) {
          outputLines.push(coutEndMatch[1])
          continue
        }
      }
    } else if (lang === 'goc') {
      // 模拟 GoC 运行
      const lines = code.split('\n')
      for (const line of lines) {
        const trimmed = line.trim()
        if (!trimmed || trimmed.startsWith('//') || trimmed.startsWith('#')) continue
        outputLines.push(`[GoC] ${trimmed}`)
      }
    }
  } catch (e) {
    error = String(e)
  }

  return {
    success: !error,
    output: outputLines.join('\n') || '运行成功（无输出）',
    errorMessage: error,
  }
}

/** 是否为 Python 类语言（在浏览器中通过 Pyodide 直接运行，不调用后端） */
const isPythonLang = computed(() => {
  return props.config.lang === 'python' || props.config.lang === 'pythonturtle'
})

/** 是否为 GoC 语言（在浏览器中通过内置模拟器直接运行，不调用后端） */
const isGoCLang = computed(() => {
  return props.config.lang === 'goc'
})

/** 是否为 C++ 语言（在浏览器中通过内置模拟器直接运行，不调用后端） */
const isCppLang = computed(() => {
  return props.config.lang === 'cpp'
})

/** 运行代码 */
async function handleRun() {
  if (isScratchLang.value) {
    // Scratch/ScratchJr 通过 iframe 内部运行
    childEditorRef.value?.runProject?.()
    return
  }

  if (!code.value.trim()) {
    ElMessage.warning('请先编写代码再运行')
    return
  }

  // Python / Python Turtle：使用子编辑器内置的 Pyodide 运行时在浏览器中直接执行
  if (isPythonLang.value) {
    running.value = true
    runStatus.value = 'running'
    runErrorLine.value = undefined
    runOutput.value = '正在运行...'
    try {
      const result = await childEditorRef.value?.runCode?.()
      if (result?.success) {
        runOutput.value = result.output || '运行成功（无输出）'
        runStatus.value = 'success'
      } else {
        runOutput.value = result?.errorMessage || '运行错误'
        runStatus.value = 'error'
        runErrorLine.value = result?.errorLine
      }
      emit('run-complete', runOutput.value)
    } catch (err: any) {
      runOutput.value = '运行失败: ' + (err?.message || String(err))
      runStatus.value = 'error'
    } finally {
      running.value = false
    }
    return
  }

  // GoC / C++：使用子编辑器内置的浏览器模拟器直接执行，不调用后端
  if (isGoCLang.value || isCppLang.value) {
    running.value = true
    runStatus.value = 'running'
    runErrorLine.value = undefined
    runOutput.value = '正在运行...'
    try {
      const result = await childEditorRef.value?.runCode?.()
      if (result?.success) {
        runOutput.value = result.output || '运行成功（无输出）'
        runStatus.value = 'success'
      } else {
        runOutput.value = result?.errorMessage || '运行错误'
        runStatus.value = 'error'
        runErrorLine.value = result?.errorLine
      }
      emit('run-complete', runOutput.value)
    } catch (err: any) {
      runOutput.value = '运行失败: ' + (err?.message || String(err))
      runStatus.value = 'error'
    } finally {
      running.value = false
    }
    return
  }

  // 其他语言：调用后端 judge API
  running.value = true
  runStatus.value = 'running'
  runOutput.value = '正在运行...'

  try {
    const res = await runCode({
      questionId: props.config.questionId,
      lang: props.config.lang,
      code: code.value,
    })
    const result = res.data
    if (result.success) {
      runOutput.value = result.output || '运行成功（无输出）'
      runStatus.value = 'success'
    } else {
      runOutput.value = result.errorMessage || '运行错误'
      runStatus.value = 'error'
    }
    emit('run-complete', runOutput.value)
  } catch (err: any) {
    // 后端不可用或服务异常时，尝试本地模拟运行
    const isDev = import.meta.env.DEV
    const status = err.response?.status
    const isNetworkError = !err.response || err.code === 'ERR_NETWORK'
    const isServerError = status && status >= 500 && status < 600

    if (isDev && (isNetworkError || isServerError)) {
      // 开发环境且后端不可用（网络错误/5xx），使用本地模拟
      const mockResult = mockRunLocal(props.config.lang, code.value)
      if (mockResult.success) {
        runOutput.value = `[本地模拟运行]\n${mockResult.output}`
        runStatus.value = 'success'
      } else {
        runOutput.value = `[本地模拟运行]\n${mockResult.errorMessage || '运行失败'}`
        runStatus.value = 'error'
      }
      ElMessage.info(isServerError
        ? `后端服务异常 (HTTP ${status})，已切换本地模拟运行`
        : '后端服务未启动，已切换本地模拟运行')
    } else {
      // 其他错误（4xx等），显示详细信息
      const msg = err.response?.data?.message || err.message || '未知错误'
      runOutput.value = `运行失败 (${status || '网络错误'}): ${msg}`
      runStatus.value = 'error'
    }
  } finally {
    running.value = false
  }
}

/** 提交代码 */
async function handleSubmit() {
  if (editorLocked.value) {
    ElMessage.warning('作品已提交，无法再次提交')
    return
  }

  if (isScratchLang.value) {
    // Scratch/ScratchJr: 获取项目数据后提交
    let projectData = childEditorRef.value?.getProjectData?.()

    // For ScratchJr, if no cached data, request it asynchronously
    if (!projectData && props.config.lang === 'scratchjr') {
      projectData = await childEditorRef.value?.requestProjectData?.()
    }

    if (projectData) {
      // 提交前捕获舞台缩略图作为封面（Scratch3 支持；ScratchJr 回退为空）
      let cover: string | null = null
      try {
        cover = await childEditorRef.value?.captureThumbnail?.()
      } catch {
        cover = null
      }
      // 映射项目数据到 submit 函数期望的格式 { json, assets, cover }
      submit(undefined, {
        json: projectData.contents || projectData.json || '',
        assets: projectData.assets,
        cover: cover || undefined,
      })
    } else {
      ElMessage.warning('请先完成项目再提交')
    }
    return
  }

  submit(code.value)
}

/** 保存草稿 */
function handleSave() {
  saveDraft(code.value)
  ElMessage.success('草稿已保存')
}

/** 重置代码 */
function handleReset() {
  code.value = props.config.templateCode || ''
  runOutput.value = ''
  runStatus.value = 'idle'
  judgeResult.value = null
  lastCompare.value = null
}

/** 格式化代码 */
function handleFormat() {
  childEditorRef.value?.formatCode?.()
}

/** 全屏切换 */
function handleFullscreen() {
  const target = editorMainRef.value || codePaneRef.value
  if (target) {
    if (document.fullscreenElement) {
      document.exitFullscreen()
    } else {
      target.requestFullscreen()
    }
  }
}

/** Scratch 项目保存回调（处理 sb3 保存） */
function handleScratchSave(data: any) {
  if (!data) return

  // data.contents / data.json is already the project JSON string
  const jsonStr = data.contents || data.json || (typeof data === 'string' ? data : JSON.stringify(data))
  code.value = jsonStr
  markDirty(jsonStr)

  // 自动保存到后端
  if (props.config.mode !== 'exam') {
    saveDraft(jsonStr)
  }
}

/** 切屏检测事件处理 */
function handleScreenSwitch() {
  if (props.config.mode === 'exam') {
    ElMessage.warning('检测到切屏行为，请专注答题')
  }
}

/** 外部素材导入尝试处理（竞赛模式下拦截） */
function handleAssetImportAttempt() {
  if (props.config.mode === 'exam') {
    ElMessage.warning('竞赛模式下禁止导入外部素材')
  }
}

/** 锁定编辑器（提交成功后调用） */
function lockEditor() {
  editorLocked.value = true
  childEditorRef.value?.lockEditor?.()
}

/** 处理子编辑器（iframe）内部的提交成功回调 */
function handleChildSubmitSuccess(result?: any) {
  // GoC iframe 内部已发起 POST /api/contest/submit_goc 请求并成功
  // 此处仅负责锁定编辑器和通知父组件
  lockEditor()
  emit('submit-success', result || { success: true, message: '作品提交成功' })
}

// ========== 新增：外观 / 工具操作 ==========

/** 切换主题（暗 ↔ 亮） */
function handleToggleTheme() {
  uiTheme.value = uiTheme.value === 'dark' ? 'light' : 'dark'
}

/** 调整字号 */
function handleFontSize(size: number) {
  fontSize.value = size
}

/** 停止运行（转发到子编辑器） */
function handleStop() {
  if (isScratchLang.value) {
    childEditorRef.value?.stopProject?.()
    return
  }
  childEditorRef.value?.stopRun?.()
}

/** 统一调用子编辑器方法（兼容 Scratch3 / ScratchJr 方法名差异） */
function triggerChild(method: string): boolean {
  const fn = (childEditorRef.value as any)?.[method]
  if (typeof fn === 'function') {
    fn.call(childEditorRef.value)
    return true
  }
  return false
}
function childScratchExample() {
  triggerChild('openExamples')
}
function childScratchExport() {
  if (!triggerChild('exportSb3')) triggerChild('exportProject')
}
function childScratchThumb() {
  triggerChild('refreshThumbnail')
}

/** 复制代码 */
async function handleCopy() {
  const src = code.value || childEditorRef.value?.getCode?.()
  if (!src || !src.trim()) {
    ElMessage.warning('暂无可复制的代码')
    return
  }
  try {
    await navigator.clipboard.writeText(src)
    ElMessage.success('代码已复制')
  } catch {
    const ta = document.createElement('textarea')
    ta.value = src
    document.body.appendChild(ta)
    ta.select()
    try { document.execCommand('copy') } catch { /* ignore */ }
    document.body.removeChild(ta)
    ElMessage.success('代码已复制')
  }
}

/** 下载代码 */
function handleDownload() {
  const src = code.value || childEditorRef.value?.getCode?.()
  if (!src || !src.trim()) {
    ElMessage.warning('暂无可下载的代码')
    return
  }
  const extMap: Record<string, string> = {
    python: 'py', pythonturtle: 'py', cpp: 'cpp', goc: 'cpp',
  }
  const ext = extMap[props.config.lang] || 'txt'
  const blob = new Blob([src], { type: 'text/plain;charset=utf-8' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `solution_${props.config.questionId || 'code'}.${ext}`
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

/** 打开模板库抽屉 */
function handleOpenTemplates() {
  templateDrawerVisible.value = true
}

/** 插入模板到编辑器光标处 */
function insertTemplate(tpl: CodeTemplate) {
  const ok = childEditorRef.value?.insertSnippet?.(tpl.code)
  if (ok === false || ok === undefined) {
    // 不支持插入的语言（积木类等）：给出提示，不破坏现有内容
    if (isScratchLang.value) {
      ElMessage.info('积木类编辑器请在画布内使用示例项目')
    } else {
      ElMessage.warning('当前编辑器暂不支持插入代码片段')
    }
  } else {
    ElMessage.success(`已插入：${tpl.name}`)
  }
  templateDrawerVisible.value = false
}

/** 设置 stdin 输入（供样例一键填入，转发到子编辑器） */
function setStdin(text: string) {
  childEditorRef.value?.setStdin?.(text)
}

/** 运行并与期望输出对比（样例校验，仅代码类有效） */
async function runAndCompare(expected: string) {
  if (isScratchLang.value) {
    ElMessage.info('积木类题目请在舞台查看运行效果')
    handleRun()
    return
  }
  await handleRun()
  const actual = (runOutput.value || '').trim()
  const exp = (expected || '').trim()
  const matched = actual === exp
  lastCompare.value = { expected: exp, actual, matched }
  activeResultTab.value = 'compare'
  if (matched) {
    ElMessage.success('运行输出与示例输出一致 ✓')
  } else {
    ElMessage.warning('运行输出与示例输出不一致，请检查代码')
  }
}

// ========== 键盘快捷键（Ctrl/Cmd + Enter 运行，Ctrl/Cmd + S 保存） ==========
function handleKeydown(e: KeyboardEvent) {
  const isMod = e.ctrlKey || e.metaKey
  if (!isMod) return
  if (e.key === 'Enter') {
    e.preventDefault()
    handleRun()
  } else if (e.key.toLowerCase() === 's') {
    e.preventDefault()
    handleSave()
  }
}

// ========== 初始化 ==========
onMounted(async () => {
  // 尝试加载草稿
  const draft = await loadDraft()
  if (draft) {
    code.value = draft
  }
  window.addEventListener('keydown', handleKeydown)
})

onBeforeUnmount(() => {
  window.removeEventListener('keydown', handleKeydown)
  stopSplitDrag()
})

// 点击输出面板「在编辑器中定位」→ 跳转到错误行
function onRevealLine(line: number) {
  const ok = childEditorRef.value?.revealLine?.(line)
  if (ok === false) {
    ElMessage.info('当前编辑器不支持行号定位')
  }
}

// 暴露方法给父组件
defineExpose({
  getCode: () => code.value,
  setCode: (val: string) => { code.value = val },
  getScratchProject: () => childEditorRef.value?.getProjectData?.(),
  submit: handleSubmit,
  run: handleRun,
  setStdin,
  runAndCompare,
  revealLine: onRevealLine,
  lockEditor,
})
</script>

<style scoped lang="scss">
.base-editor {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #1D2129;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);

  &.standalone-iframe {
    // 独立 iframe 编辑器（GoC）自带完整 UI，移除圆角和阴影让 iframe 填满
    border-radius: 0;
    box-shadow: none;
    background: #0D1117;
  }
}

/* ===== P0-1 顶部考试信息条 ===== */
.exam-info-bar {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 6px 16px;
  background: #0D1117;
  border-bottom: 1px solid #30363D;
  flex-shrink: 0;

  .info-item {
    display: flex;
    align-items: baseline;
    gap: 6px;
  }

  .info-label {
    font-size: 12px;
    color: #86909C;
  }

  .info-value {
    font-size: 13px;
    font-weight: 600;
    color: #E5E6EB;
    font-variant-numeric: tabular-nums;

    &.countdown {
      color: #FF7D00;
    }
  }

  .info-spacer {
    flex: 1;
  }

  .info-locked {
    display: flex;
    align-items: center;
    gap: 4px;
    font-size: 12px;
    color: #FF7875;
    padding: 2px 8px;
    border: 1px solid rgba(245, 63, 63, 0.4);
    border-radius: 4px;
  }
}

.editor-main {
  flex: 1;
  min-height: 0;
  overflow: hidden;
}

/* ===== P1-1 三栏布局：代码 | 分隔条 | 结果 ===== */
.editor-body {
  flex: 1;
  min-height: 0;
  display: flex;
  overflow: hidden;
}

.code-pane {
  flex: 1 1 auto;
  min-width: 0;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.splitter {
  flex: 0 0 6px;
  cursor: col-resize;
  background: #30363D;
  position: relative;
  transition: background 0.15s;

  &:hover {
    background: #165DFF;
  }

  &::after {
    content: '';
    position: absolute;
    top: 50%;
    left: 50%;
    width: 2px;
    height: 24px;
    transform: translate(-50%, -50%);
    background: #4E5969;
    border-radius: 2px;
  }

  &:hover::after {
    background: #fff;
  }
}

.result-pane {
  flex: 1 1 40%;
  min-width: 300px;
  min-height: 0;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #1D2129;
  border-left: 1px solid #4E5969;
}

.result-tabs {
  height: 100%;
  display: flex;
  flex-direction: column;

  :deep(.el-tabs__header) {
    margin: 0;
    flex-shrink: 0;
    background: #0D1117;
  }

  :deep(.el-tabs__content) {
    flex: 1;
    min-height: 0;
    overflow: auto;
    padding: 0;
  }

  :deep(.el-tab-pane) {
    height: 100%;
  }
}

/* 让控制台输出填满结果区 */
.result-pane :deep(.editor-output) {
  max-height: none;
  flex: 1;
  border-top: none;
}

/* 结果对比面板 */
.compare-pane {
  padding: 12px;
  height: 100%;
  box-sizing: border-box;

  .compare-row {
    margin-bottom: 12px;

    .compare-label {
      display: block;
      font-size: 12px;
      color: #86909C;
      margin-bottom: 4px;
      font-weight: 600;
    }

    pre {
      margin: 0;
      font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
      font-size: 13px;
      color: #E5E6EB;
      background: #0D1117;
      border: 1px solid #30363D;
      border-radius: 6px;
      padding: 10px;
      white-space: pre-wrap;
      word-break: break-all;
      max-height: 200px;
      overflow: auto;

      &.mismatch {
        border-color: rgba(245, 63, 63, 0.6);
        color: #FFA39E;
      }
    }
  }

  .compare-result {
    font-size: 14px;
    font-weight: 600;

    &.ok { color: #36D399; }
    &.bad { color: #F53F3F; }
  }

  .compare-empty {
    font-size: 13px;
    color: #86909C;
    line-height: 1.8;
  }
}

/* Turtle 图形输出宿主 */
.turtle-host {
  height: 100%;
  overflow: auto;
  padding: 12px;
  box-sizing: border-box;
  display: flex;
  align-items: flex-start;
  justify-content: center;

  :deep(.turtle-panel) {
    width: 100%;
  }
}

// ========== Scratch 舞台预览面板（保留结构，已不再使用，留给历史兼容） ==========

// ========== 保存指示器 ==========
.save-indicator {
  position: absolute;
  bottom: 64px;
  right: 12px;
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 4px 10px;
  background: rgba(0, 0, 0, 0.6);
  border-radius: 6px;
  font-size: 12px;
  color: #86909C;
  z-index: 10;
}

.exam-mode {
  .save-indicator {
    display: none;
  }
}

// ========== P0-2 底部统一动作栏 ==========
.action-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 8px 16px;
  background: #0D1117;
  border-top: 1px solid #30363D;
  flex-shrink: 0;

  .action-left,
  .action-right {
    display: flex;
    align-items: center;
    gap: 8px;
    flex-wrap: wrap;
  }
}

// ========== 亮色主题（整体外壳）+ 代码模板库抽屉 ==========
.theme-light {
  background: #FFFFFF;

  .exam-info-bar {
    background: #F2F3F5;
    border-bottom-color: #E5E6EB;

    .info-label { color: #86909C; }
    .info-value { color: #1D2129; }
  }

  .result-pane {
    background: #FFFFFF;
    border-left-color: #E5E6EB;
  }

  .result-tabs :deep(.el-tabs__header) { background: #F2F3F5; }
  .result-pane :deep(.editor-output) { border-top: none; }

  .action-bar {
    background: #F2F3F5;
    border-top-color: #E5E6EB;
  }

  .splitter {
    background: #E5E6EB;
    &::after { background: #C9CDD4; }
  }
}

// 主题切换时，工具栏/输出面板跟随变化由组件自身 theme 控制；
// 这里仅保证 base-editor 在亮色下边框与底色协调（子组件 Monaco 已独立切换）

// 模板抽屉内容
.tpl-hint {
  font-size: 12px;
  color: #86909C;
  margin: 0 0 12px;
}

.tpl-empty {
  font-size: 13px;
  color: #86909C;
  line-height: 1.6;
}

.tpl-item {
  padding: 8px 10px;
  border: 1px solid #E5E6EB;
  border-radius: 6px;
  margin-bottom: 8px;
  cursor: pointer;
  transition: all 0.15s;
  background: #FAFBFF;

  &:hover {
    border-color: #165DFF;
    background: #F2F6FF;
  }

  .tpl-name {
    font-size: 13px;
    font-weight: 600;
    color: #1D2129;
    margin-bottom: 4px;
  }

  .tpl-code {
    margin: 0;
    font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
    font-size: 11px;
    color: #4E5969;
    white-space: pre-wrap;
    word-break: break-all;
    max-height: 80px;
    overflow: hidden;
  }
}

/* ===== P2 窄屏自适应：三栏堆叠为纵向 ===== */
@media (max-width: 860px) {
  .editor-body {
    flex-direction: column;
  }

  .code-pane {
    flex-basis: auto !important;
    flex: 1 1 55%;
  }

  .splitter {
    display: none;
  }

  .result-pane {
    flex: 1 1 45%;
    min-width: 0;
    border-left: none;
    border-top: 1px solid #4E5969;
  }
}
</style>
