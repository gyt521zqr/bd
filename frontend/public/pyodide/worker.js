/**
 * Pyodide Worker - 浏览器端 Python 3 运行时
 * 基于 Pyodide (CPython WASM) 提供完整的 Python 3 兼容性
 * 使用 WebWorker 隔离执行，不阻塞主线程
 *
 * 消息协议（主线程 → Worker）：
 *   { type: 'run', code: string, isTurtle: boolean }
 *
 * 消息协议（Worker → 主线程）：
 *   { type: 'ready' }
 *   { type: 'progress', percent: number, text: string }
 *   { type: 'stdout', text: string }
 *   { type: 'stderr', text: string }
 *   { type: 'turtle', command: object }
 *   { type: 'done', success: boolean, error?: string, duration: number }
 */

// ==================== Pyodide 加载 ====================

var pyodide = null;
var pyodideReady = false;

// Pyodide CDN 版本
var PYODIDE_VERSION = '0.26.2';
var PYODIDE_CDN = 'https://fastly.jsdelivr.net/pyodide/v' + PYODIDE_VERSION + '/full/';
// CDN 多镜像 fallback（按可达性排序）
var PYODIDE_CDN_LIST = [
  'https://fastly.jsdelivr.net/pyodide/v' + PYODIDE_VERSION + '/full/',
  'https://testingcf.jsdelivr.net/pyodide/v' + PYODIDE_VERSION + '/full/',
  'https://gcore.jsdelivr.net/pyodide/v' + PYODIDE_VERSION + '/full/',
  'https://cdn.jsdelivr.net/pyodide/v' + PYODIDE_VERSION + '/full/',
];

// ==================== Turtle 模块 Python 代码 ====================

var TURTLE_MODULE_CODE = [
  'import math',
  'import json',
  'import builtins',
  '',
  '# JavaScript 桥接函数，由 Worker 注入',
  '# def _py_send_cmd(cmd_json_str): ...',
  '',
  'class _Turtle:',
  '    def __init__(self):',
  '        self._x = 0.0',
  '        self._y = 0.0',
  '        self._heading = 0.0  # 0=east, CCW positive (standard mode)',
  '        self._pen_down = True',
  '        self._pen_color = "black"',
  '        self._pen_size = 1',
  '        self._speed_val = 6',
  '        self._visible = True',
  '        self._filling = False',
  '        self._fill_color = "black"',
  '        self._fill_path = []',
  '',
  '    def forward(self, distance):',
  '        rad = math.radians(self._heading)',
  '        nx = self._x + distance * math.cos(rad)',
  '        ny = self._y + distance * math.sin(rad)',
  '        if self._pen_down:',
  '            _py_send_cmd(json.dumps({"op":"line","x1":self._x,"y1":self._y,"x2":nx,"y2":ny,"color":self._pen_color,"width":self._pen_size}))',
  '        if self._filling:',
  '            self._fill_path.append((nx, ny))',
  '        self._x = nx',
  '        self._y = ny',
  '',
  '    fd = forward',
  '',
  '    def backward(self, distance):',
  '        self.forward(-distance)',
  '',
  '    bk = backward',
  '    back = backward',
  '',
  '    def right(self, angle):',
  '        self._heading = (self._heading - angle) % 360',
  '',
  '    rt = right',
  '',
  '    def left(self, angle):',
  '        self._heading = (self._heading + angle) % 360',
  '',
  '    lt = left',
  '',
  '    def penup(self):',
  '        self._pen_down = False',
  '',
  '    pu = penup',
  '    up = penup',
  '',
  '    def pendown(self):',
  '        self._pen_down = True',
  '',
  '    pd = pendown',
  '    down = pendown',
  '',
  '    def pencolor(self, *args):',
  '        if len(args) == 1:',
  '            self._pen_color = str(args[0])',
  '        elif len(args) == 3:',
  '            r, g, b = int(args[0]), int(args[1]), int(args[2])',
  '            self._pen_color = f"#{r:02x}{g:02x}{b:02x}"',
  '',
  '    def pensize(self, width):',
  '        self._pen_size = max(1, int(width))',
  '',
  '    width = pensize',
  '',
  '    def speed(self, s):',
  '        self._speed_val = max(0, min(10, int(s)))',
  '',
  '    def circle(self, radius, extent=360, steps=None):',
  '        if steps is None:',
  '            steps = max(10, int(abs(extent) / 5)) if abs(extent) < 360 else max(36, int(abs(radius) / 5))',
  '        step_angle = float(extent) / steps',
  '        step_len = 2 * math.pi * abs(radius) * abs(extent) / 360.0 / steps',
  '        for _ in range(steps):',
  '            self.forward(step_len)',
  '            if radius > 0:',
  '                self.left(step_angle)',
  '            else:',
  '                self.right(step_angle)',
  '',
  '    def hideturtle(self):',
  '        self._visible = False',
  '        _py_send_cmd(json.dumps({"op":"hideturtle"}))',
  '',
  '    ht = hideturtle',
  '',
  '    def showturtle(self):',
  '        self._visible = True',
  '        _py_send_cmd(json.dumps({"op":"showturtle"}))',
  '',
  '    st = showturtle',
  '',
  '    def goto(self, x, y):',
  '        if self._pen_down:',
  '            _py_send_cmd(json.dumps({"op":"line","x1":self._x,"y1":self._y,"x2":float(x),"y2":float(y),"color":self._pen_color,"width":self._pen_size}))',
  '        if self._filling:',
  '            self._fill_path.append((float(x), float(y)))',
  '        self._x = float(x)',
  '        self._y = float(y)',
  '',
  '    setpos = goto',
  '    setposition = goto',
  '',
  '    def setheading(self, angle):',
  '        self._heading = float(angle) % 360',
  '',
  '    seth = setheading',
  '',
  '    def heading(self):',
  '        return self._heading',
  '',
  '    def position(self):',
  '        return (self._x, self._y)',
  '',
  '    pos = position',
  '',
  '    def xcor(self):',
  '        return self._x',
  '',
  '    def ycor(self):',
  '        return self._y',
  '',
  '    def home(self):',
  '        self.goto(0, 0)',
  '        self._heading = 0.0',
  '',
  '    def begin_fill(self):',
  '        self._filling = True',
  '        self._fill_path = [(self._x, self._y)]',
  '',
  '    def end_fill(self):',
  '        self._filling = False',
  '        if len(self._fill_path) > 2:',
  '            _py_send_cmd(json.dumps({"op":"fill","path":self._fill_path,"color":self._fill_color}))',
  '        self._fill_path = []',
  '',
  '    def fillcolor(self, *args):',
  '        if len(args) == 1:',
  '            self._fill_color = str(args[0])',
  '        elif len(args) == 3:',
  '            r, g, b = int(args[0]), int(args[1]), int(args[2])',
  '            self._fill_color = f"#{r:02x}{g:02x}{b:02x}"',
  '',
  '    def write(self, text, move=False, align="left", font=("Arial", 8, "normal")):',
  '        sz = font[1] if isinstance(font, tuple) and len(font) > 1 else 12',
  '        _py_send_cmd(json.dumps({"op":"text","x":self._x,"y":self._y,"text":str(text),"color":self._pen_color,"size":sz}))',
  '',
  '    def dot(self, size=None, color=None):',
  '        if size is None:',
  '            size = max(self._pen_size + 4, self._pen_size * 2)',
  '        if color is None:',
  '            color = self._pen_color',
  '        _py_send_cmd(json.dumps({"op":"dot","x":self._x,"y":self._y,"r":float(size)/2,"color":color}))',
  '',
  '    def stamp(self):',
  '        _py_send_cmd(json.dumps({"op":"stamp","x":self._x,"y":self._y,"heading":self._heading}))',
  '',
  '    def isvisible(self):',
  '        return self._visible',
  '',
  '    def isdown(self):',
  '        return self._pen_down',
  '',
  '    def distance(self, x, y):',
  '        return math.sqrt((self._x - x)**2 + (self._y - y)**2)',
  '',
  '    def towards(self, x, y):',
  '        return math.degrees(math.atan2(y - self._y, x - self._x)) % 360',
  '',
  '# 模块级单例',
  '_turtle = _Turtle()',
  '',
  '# 函数式 API',
  'def forward(d): _turtle.forward(d)',
  'fd = forward',
  'def backward(d): _turtle.backward(d)',
  'bk = backward',
  'def right(a): _turtle.right(a)',
  'rt = right',
  'def left(a): _turtle.left(a)',
  'lt = left',
  'def penup(): _turtle.penup()',
  'pu = penup',
  'def pendown(): _turtle.pendown()',
  'pd = pendown',
  'def pencolor(*a): _turtle.pencolor(*a)',
  'def pensize(w): _turtle.pensize(w)',
  'def speed(s): _turtle.speed(s)',
  'def circle(r, e=360, s=None): _turtle.circle(r, e, s)',
  'def hideturtle(): _turtle.hideturtle()',
  'def showturtle(): _turtle.showturtle()',
  'def goto(x, y): _turtle.goto(x, y)',
  'def setheading(a): _turtle.setheading(a)',
  'def heading(): return _turtle.heading()',
  'def position(): return _turtle.position()',
  'def xcor(): return _turtle.xcor()',
  'def ycor(): return _turtle.ycor()',
  'def home(): _turtle.home()',
  'def begin_fill(): _turtle.begin_fill()',
  'def end_fill(): _turtle.end_fill()',
  'def fillcolor(*a): _turtle.fillcolor(*a)',
  'def write(t, m=False, a="left", f=("Arial",8,"normal")): _turtle.write(t, m, a, f)',
  'def dot(s=None, c=None): _turtle.dot(s, c)',
  'def clear(): _py_send_cmd(json.dumps({"op":"clear"}))',
  'def reset(): _turtle.__init__(); _py_send_cmd(json.dumps({"op":"clear"}))',
  'def done(): pass',
  'mainloop = done',
  'bye = done',
  'def Turtle(): return _turtle',
  'def setup(w, h, sx=None, sy=None): pass',
  'def title(t): pass',
  'def bgcolor(*a): pass',
  'def screensize(w=None, h=None, bg=None): pass',
  'def mode(m=None): return "standard"',
  'def colormode(c=None): return 1.0',
].join('\n');

// ==================== 初始化 Pyodide ====================

async function initPyodide() {
  try {
    self.postMessage({ type: 'progress', percent: 10, text: '正在下载 Pyodide 运行时...' });

    // 动态 import Pyodide（多 CDN 镜像 fallback，逐个尝试）
    var loadedCDN = null;
    for (var ci = 0; ci < PYODIDE_CDN_LIST.length; ci++) {
      try {
        importScripts(PYODIDE_CDN_LIST[ci] + 'pyodide.js');
        loadedCDN = PYODIDE_CDN_LIST[ci];
        break;
      } catch (e) {
        // 尝试下一个 CDN 镜像
      }
    }
    if (!loadedCDN) {
      throw new Error('Pyodide 加载失败：所有 CDN 镜像均不可达，请检查网络');
    }
    PYODIDE_CDN = loadedCDN;

    self.postMessage({ type: 'progress', percent: 40, text: '正在初始化 Python 解释器...' });

    pyodide = await loadPyodide({
      indexURL: loadedCDN,
    });

    self.postMessage({ type: 'progress', percent: 70, text: '正在加载标准库...' });

    // 配置 stdout / stderr（batched 模式按行刷新，手动补换行符）
    pyodide.setStdout({
      batched: function(text) {
        self.postMessage({ type: 'stdout', text: text + '\n' });
      },
    });
    pyodide.setStderr({
      batched: function(text) {
        self.postMessage({ type: 'stderr', text: text + '\n' });
      },
    });

    self.postMessage({ type: 'progress', percent: 85, text: '正在注入 Turtle 模块...' });

    // 注入桥接函数：Python 调用此函数发送绘图命令到主线程
    pyodide.globals.set('_py_send_cmd', function(cmdJsonStr) {
      try {
        var cmd = JSON.parse(cmdJsonStr);
        self.postMessage({ type: 'turtle', command: cmd });
      } catch (e) {
        // ignore parse errors
      }
    });

    // 注册 turtle 模块
    await pyodide.runPythonAsync(TURTLE_MODULE_CODE);

    // 将 turtle 模块注册到 sys.modules
    await pyodide.runPythonAsync([
      'import sys',
      'import types',
      '# 将当前命名空间中的 turtle 相关定义封装为模块',
      '_turtle_mod = types.ModuleType("turtle")',
      'for _name in dir():',
      '    if not _name.startswith("_") or _name in ("_turtle",):',
      '        setattr(_turtle_mod, _name, globals()[_name])',
      'sys.modules["turtle"] = _turtle_mod',
      'del _name',
    ].join('\n'));

    // 替换 input() 从 stdin 缓冲区按行读取（WebWorker 无法弹出对话框）
    await pyodide.runPythonAsync([
      'import builtins',
      '_stdin_lines = []',
      '_stdin_idx = 0',
      'def _mock_input(prompt=""):',
      '    global _stdin_idx',
      '    print(prompt, end="")',
      '    if _stdin_idx < len(_stdin_lines):',
      '        line = _stdin_lines[_stdin_idx]',
      '        _stdin_idx += 1',
      '        return line',
      '    raise EOFError("没有可用的输入：请在下方「输入 (stdin)」面板填写输入数据")',
      'builtins.input = _mock_input',
    ].join('\n'));

    pyodideReady = true;
    self.postMessage({ type: 'progress', percent: 100, text: 'Pyodide 就绪' });
    self.postMessage({ type: 'ready' });

  } catch (err) {
    self.postMessage({
      type: 'error',
      error: 'Pyodide 加载失败: ' + (err.message || String(err)),
      phase: 'init'
    });
  }
}

// ==================== 代码执行 ====================

async function execute(code, isTurtle, stdin) {
  if (!pyodideReady) {
    self.postMessage({
      type: 'error',
      error: 'Pyodide 尚未就绪，请稍候',
      phase: 'init'
    });
    return;
  }

  var startTime = Date.now();

  // 设置 stdin 缓冲区（按行分割）
  try {
    // 按行分割，过滤掉纯空白行（避免空输入被当作一次 input）
    var stdinLines = (stdin || '').split('\n').filter(function (l) { return l.trim().length > 0; });
    await pyodide.runPythonAsync(
      '_stdin_lines = ' + JSON.stringify(stdinLines) + '\n' +
      '_stdin_idx = 0'
    );
  } catch (e) {
    // 忽略 stdin 设置错误
  }

  // 如果是 turtle 模式，先清空画布
  if (isTurtle) {
    self.postMessage({ type: 'turtle', command: { op: 'clear' } });
  }

  try {
    // 执行 Python 代码
    await pyodide.runPythonAsync(code);

    var duration = Date.now() - startTime;
    self.postMessage({
      type: 'done',
      success: true,
      duration: duration
    });

  } catch (err) {
    var duration = Date.now() - startTime;
    var errMsg = '';

    // 提取 Python 异常信息
    if (err && err.message) {
      errMsg = err.message;
    } else {
      errMsg = String(err);
    }

    // 清理 Pyodide 内部错误前缀
    errMsg = errMsg.replace(/^PythonError: /, '');

    self.postMessage({
      type: 'done',
      success: false,
      error: errMsg,
      duration: duration
    });
  }
}

// ==================== 消息处理 ====================

self.onmessage = function(e) {
  var data = e.data;
  if (!data || !data.type) return;

  if (data.type === 'run') {
    var code = data.code || '';
    var isTurtle = data.isTurtle || false;
    var stdin = data.stdin || '';
    if (!code.trim()) {
      self.postMessage({
        type: 'error',
        error: '代码为空，请先编写代码',
        phase: 'compile'
      });
      return;
    }
    execute(code, isTurtle, stdin);
  }
};

// 启动初始化
initPyodide();
