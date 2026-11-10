/**
 * GoC Worker - C++ 转译器 + 海龟API + 执行引擎
 * 在 WebWorker 中隔离执行，不阻塞主线程
 *
 * 消息协议（主线程 → Worker）：
 *   { type: 'run', code: string }
 *
 * 消息协议（Worker → 主线程）：
 *   { type: 'ready' }
 *   { type: 'log', text: string, level: 'log'|'error'|'warn' }
 *   { type: 'done', commands: array, turtle: object, count: number, duration: number }
 *   { type: 'error', error: string, phase: 'compile'|'runtime' }
 */

// ==================== 配置 ====================
var MAX_COMMANDS = 100000;
var MAX_LOOP_CHECK_INTERVAL = 50000;

// ==================== 颜色常量（全局变量，供用户代码引用） ====================
var RED = '#FF0000';
var GREEN = '#00AA00';
var BLUE = '#0000FF';
var BLACK = '#000000';
var WHITE = '#FFFFFF';
var YELLOW = '#FFFF00';
var CYAN = '#00FFFF';
var MAGENTA = '#FF00FF';
var ORANGE = '#FFA500';
var PURPLE = '#800080';
var PINK = '#FFC0CB';
var BROWN = '#8B4513';
var GRAY = '#808080';
var GREY = '#808080';
var DARKGRAY = '#404040';
var LIGHTGRAY = '#C0C0C0';
var DARKRED = '#8B0000';
var DARKGREEN = '#006400';
var DARKBLUE = '#00008B';
var GOLD = '#FFD700';
var NAVY = '#000080';

// 颜色名称映射
var COLOR_MAP = {
  'red': '#FF0000', 'green': '#00AA00', 'blue': '#0000FF',
  'black': '#000000', 'white': '#FFFFFF', 'yellow': '#FFFF00',
  'cyan': '#00FFFF', 'magenta': '#FF00FF', 'orange': '#FFA500',
  'purple': '#800080', 'pink': '#FFC0CB', 'brown': '#8B4513',
  'gray': '#808080', 'grey': '#808080', 'darkgray': '#404040',
  'lightgray': '#C0C0C0', 'darkred': '#8B0000', 'darkgreen': '#006400',
  'darkblue': '#00008B', 'gold': '#FFD700', 'navy': '#000080',
};

// GoC 16 色索引表：pencolor(0) ~ pencolor(15)
var GOC_PALETTE = [
  '#000000', '#FF0000', '#FF8000', '#FFD700',
  '#00B050', '#00B0F0', '#0070C0', '#7030A0',
  '#FF00FF', '#FF99CC', '#808080', '#800000',
  '#004000', '#000080', '#808000', '#FFFFFF'
];

// ==================== 海龟状态 ====================
var turtle = {
  x: 0,
  y: 0,
  angle: 0,      // 0° = 朝右，顺时针为正
  penDown: true,
  color: '#000000',
  size: 2,
  visible: true,
  speed: 6
};

var commands = [];
var cmdCount = 0;
var logOutput = [];

// 输入队列（cinWin / cin >> 使用）
var inputTokens = [];
var inputIndex = 0;

// 本次运行起始时间（ctime() 使用）
var startTime = 0;

// ==================== 海龟 API ====================

function _checkLimit() {
  cmdCount++;
  if (cmdCount > MAX_COMMANDS) {
    throw new Error('执行命令数超过上限(' + MAX_COMMANDS + ')，可能存在死循环');
  }
}

function _resolveColor(c) {
  if (typeof c === 'string') {
    var lower = c.toLowerCase();
    if (COLOR_MAP[lower]) return COLOR_MAP[lower];
    if (c.startsWith('#')) return c;
    return c;
  }
  if (typeof c === 'number') {
    // GoC 颜色索引 0-15
    if (c >= 0 && c <= 15 && c === Math.floor(c)) {
      return GOC_PALETTE[c];
    }
    // 其他数字按灰度处理
    var v = Math.max(0, Math.min(255, Math.floor(c)));
    var hex = v.toString(16).padStart(2, '0');
    return '#' + hex + hex + hex;
  }
  return '#000000';
}

/** 前进 distance 像素 */
function fd(distance) {
  _checkLimit();
  var d = Number(distance) || 0;
  var rad = turtle.angle * Math.PI / 180;
  var nx = turtle.x + d * Math.cos(rad);
  var ny = turtle.y + d * Math.sin(rad);
  if (turtle.penDown) {
    commands.push({
      op: 'line',
      x1: turtle.x, y1: turtle.y,
      x2: nx, y2: ny,
      color: turtle.color, width: turtle.size
    });
  }
  turtle.x = nx;
  turtle.y = ny;
}
var forward = fd;

/** 后退 distance 像素 */
function bk(distance) {
  fd(-(Number(distance) || 0));
}
var backward = bk;
var back = bk;

/** 顺时针旋转 angle 度 */
function rt(angle) {
  _checkLimit();
  turtle.angle += Number(angle) || 0;
}
var right = rt;

/** 逆时针旋转 angle 度 */
function lt(angle) {
  _checkLimit();
  turtle.angle -= Number(angle) || 0;
}
var left = lt;

/** 设置画笔颜色 */
function pencolor(color) {
  _checkLimit();
  if (arguments.length >= 3) {
    // RGB 模式: pencolor(r, g, b)
    var r = Math.max(0, Math.min(255, Math.floor(arguments[0])));
    var g = Math.max(0, Math.min(255, Math.floor(arguments[1])));
    var b = Math.max(0, Math.min(255, Math.floor(arguments[2])));
    turtle.color = '#' +
      r.toString(16).padStart(2, '0') +
      g.toString(16).padStart(2, '0') +
      b.toString(16).padStart(2, '0');
  } else {
    turtle.color = _resolveColor(color);
  }
}
var penColor = pencolor;
var setpencolor = pencolor;
var setColor = pencolor;

/** 设置画笔粗细 */
function pensize(width) {
  _checkLimit();
  turtle.size = Math.max(1, Math.floor(Number(width) || 1));
}
var penSize = pensize;
var setlinewidth = pensize;
var setLineWidth = pensize;

/** 抬笔 */
function penup() {
  _checkLimit();
  turtle.penDown = false;
}
var pu = penup;
var penUp = penup;

/** 落笔 */
function pendown() {
  _checkLimit();
  turtle.penDown = true;
}
var pd = pendown;
var penDown = pendown;

/** 设置速度 (0-10) */
function speed(s) {
  _checkLimit();
  turtle.speed = Math.max(0, Math.min(10, Math.floor(Number(s) || 0)));
}

/** 画圆 */
function circle(radius) {
  _checkLimit();
  var r = Math.abs(Number(radius) || 0);
  if (r < 0.5) return;
  commands.push({
    op: 'circle',
    x: turtle.x, y: turtle.y,
    r: r,
    color: turtle.color, width: turtle.size
  });
}

/** 画点 */
function dot() {
  _checkLimit();
  commands.push({
    op: 'dot',
    x: turtle.x, y: turtle.y,
    r: turtle.size + 1,
    color: turtle.color
  });
}

/** 隐藏海龟 */
function hideturtle() {
  _checkLimit();
  turtle.visible = false;
}
var hideturtle_ = hideturtle;
var hide = hideturtle;

/** 显示海龟 */
function showturtle() {
  _checkLimit();
  turtle.visible = true;
}
var show = showturtle;

/** 移动到坐标 (x, y) */
function goto(x, y) {
  _checkLimit();
  var nx = Number(x) || 0;
  var ny = Number(y) || 0;
  if (turtle.penDown) {
    commands.push({
      op: 'line',
      x1: turtle.x, y1: turtle.y,
      x2: nx, y2: ny,
      color: turtle.color, width: turtle.size
    });
  }
  turtle.x = nx;
  turtle.y = ny;
}
var setxy = goto;
var setXY = goto;

/** 设置角度 */
function setangle(angle) {
  _checkLimit();
  turtle.angle = Number(angle) || 0;
}
var setheading = setangle;
var setAngle = setangle;

/** 获取X坐标 */
function getx() { return turtle.x; }
var getX = getx;

/** 获取Y坐标 */
function gety() { return turtle.y; }
var getY = gety;

/** 获取角度 */
function getangle() { return turtle.angle; }
var getAngle = getangle;

/** 画文字 */
function text(str) {
  _checkLimit();
  commands.push({
    op: 'text',
    x: turtle.x, y: turtle.y,
    text: String(str),
    color: turtle.color, size: Math.max(12, turtle.size * 4)
  });
}

/** 清空画布（不重置海龟） */
function clearscreen() {
  _checkLimit();
  commands.push({ op: 'clear' });
}
var clear = clearscreen;

/** 重置海龟到原点 */
function home() {
  _checkLimit();
  if (turtle.penDown) {
    commands.push({
      op: 'line',
      x1: turtle.x, y1: turtle.y,
      x2: 0, y2: 0,
      color: turtle.color, width: turtle.size
    });
  }
  turtle.x = 0;
  turtle.y = 0;
  turtle.angle = 0;
}

/** 输出到控制台 */
function println() {
  var args = Array.prototype.slice.call(arguments);
  var text = args.map(function(a) {
    if (a === null) return 'null';
    if (a === undefined) return 'undefined';
    if (typeof a === 'object') return JSON.stringify(a);
    return String(a);
  }).join(' ');
  logOutput.push(text);
}
var print = println;
var write = println;

// ==================== GoC 扩展函数（兼容原版 GoC 语法） ====================

/** 初始化图形窗口（浏览器端无操作，自动使用 Canvas） */
function initGraph() {
  _checkLimit();
  // no-op: Canvas 自动初始化
}
var initgraph = initGraph;
var openGraph = initGraph;

/** 关闭图形窗口 */
function closeGraph() {
  _checkLimit();
  // no-op
}
var closegraph = closeGraph;

/** 暂停执行（同步等待，单位毫秒） */
function pause(ms) {
  _checkLimit();
  // 浏览器端不支持同步暂停，仅计数
  var n = Math.min(Number(ms) || 0, 1000);
  // 模拟短暂忙等（限制在 100ms 内防止卡死）
  var end = Date.now() + Math.min(n, 100);
  while (Date.now() < end) {}
}

/** 设置文本颜色 */
function setTextColor(color) {
  _checkLimit();
  // 影响后续 text() 的颜色
  turtle.textColor = _resolveColor(color);
}
var settextcolor = setTextColor;

/** 设置背景颜色 */
function setBackgroundColor(color) {
  _checkLimit();
  commands.push({ op: 'setBackground', color: _resolveColor(color) });
}
var setbackgroundcolor = setBackgroundColor;

/** 清空文本输出区 */
function clearText() {
  _checkLimit();
  // no-op: 控制台清空由主线程处理
}
var cleartext = clearText;

/** 随机数 [min, max] */
function rand(min, max) {
  _checkLimit();
  if (arguments.length === 0) return Math.floor(Math.random() * 32767);
  if (arguments.length === 1) return Math.floor(Math.random() * (Number(min) + 1));
  var lo = Number(min) || 0;
  var hi = Number(max) || 0;
  return Math.floor(Math.random() * (hi - lo + 1)) + lo;
}
var random = rand;

/** 绝对值 */
function abs(n) { return Math.abs(Number(n) || 0); }

/** 平方根 */
function sqrt(n) { return Math.sqrt(Number(n) || 0); }

/** 幂运算 */
function pow(base, exp) { return Math.pow(Number(base) || 0, Number(exp) || 0); }

/** 正弦（角度制） */
function sin(deg) { return Math.sin((Number(deg) || 0) * Math.PI / 180); }

/** 余弦（角度制） */
function cos(deg) { return Math.cos((Number(deg) || 0) * Math.PI / 180); }

/** 正切（角度制） */
function tan(deg) { return Math.tan((Number(deg) || 0) * Math.PI / 180); }

/** 四舍五入 */
function round(n) { return Math.round(Number(n) || 0); }

/** 向下取整 */
function floor(n) { return Math.floor(Number(n) || 0); }

/** 向上取整 */
function ceil(n) { return Math.ceil(Number(n) || 0); }

/** 输入整数（从 cinWin 输入队列读取） */
function inputInt() { _checkLimit(); return Math.trunc(__cinNum()); }
var getInt = inputInt;

/** 输入浮点数（从 cinWin 输入队列读取） */
function inputFloat() { _checkLimit(); return __cinNum(); }
var getFloat = inputFloat;

/** 输入字符串（从 cinWin 输入队列读取） */
function inputString() { _checkLimit(); return __cinStr(); }
var getString = inputString;

/** 画填充矩形 */
function fillRect(x, y, w, h) {
  _checkLimit();
  commands.push({
    op: 'fillRect',
    x: Number(x) || 0, y: Number(y) || 0,
    w: Number(w) || 0, h: Number(h) || 0,
    color: turtle.color
  });
}

/** 画矩形边框 */
function drawRect(x, y, w, h) {
  _checkLimit();
  commands.push({
    op: 'drawRect',
    x: Number(x) || 0, y: Number(y) || 0,
    w: Number(w) || 0, h: Number(h) || 0,
    color: turtle.color, width: turtle.size
  });
}

/** 画线段（绝对坐标） */
function line(x1, y1, x2, y2) {
  _checkLimit();
  commands.push({
    op: 'line',
    x1: Number(x1) || 0, y1: Number(y1) || 0,
    x2: Number(x2) || 0, y2: Number(y2) || 0,
    color: turtle.color, width: turtle.size
  });
}

/* ==================== GoC 原生命令（与「常见命令」面板一致） ==================== */

/** pen(c) 设置画笔颜色：0-15 索引 / 颜色名 / #hex / RGB */
function pen(color) {
  if (arguments.length >= 3) {
    pencolor(arguments[0], arguments[1], arguments[2]);
  } else {
    pencolor(color);
  }
}

/** ct(c) 设置颜色 */
function ct(color) { pencolor(color); }

/** size(n) 画笔粗细 */
function size(n) { pensize(n); }

/** up() 抬笔 */
function up() { penup(); }

/** down() 落笔 */
function down() { pendown(); }

/** oc(r) 空心圆（以海龟位置为圆心） */
function oc(r) {
  _checkLimit();
  var radius = Math.abs(Number(r) || 0);
  if (radius < 0.5) return;
  commands.push({
    op: 'circle',
    x: turtle.x, y: turtle.y,
    r: radius,
    color: turtle.color, width: turtle.size
  });
}

/** oo(r) 实心圆 */
function oo(r) {
  _checkLimit();
  var radius = Math.abs(Number(r) || 0);
  if (radius < 0.5) return;
  commands.push({
    op: 'fillCircle',
    x: turtle.x, y: turtle.y,
    r: radius,
    color: turtle.color
  });
}

/** q(rx, ry) 空心椭圆 */
function q(rx, ry) {
  _checkLimit();
  var a = Math.abs(Number(rx) || 0);
  var b = Math.abs(Number(ry) || 0) || a;
  if (a < 0.5) return;
  commands.push({
    op: 'ellipse',
    x: turtle.x, y: turtle.y,
    rx: a, ry: b,
    color: turtle.color, width: turtle.size
  });
}

/** ee(rx, ry) 实心椭圆 */
function ee(rx, ry) {
  _checkLimit();
  var a = Math.abs(Number(rx) || 0);
  var b = Math.abs(Number(ry) || 0) || a;
  if (a < 0.5) return;
  commands.push({
    op: 'fillEllipse',
    x: turtle.x, y: turtle.y,
    rx: a, ry: b,
    color: turtle.color
  });
}

/** rl(w, h) 空心长方形（以海龟位置为中心） */
function rl(w, h) {
  _checkLimit();
  var width = Math.abs(Number(w) || 0);
  var height = Math.abs(Number(h) || 0);
  if (width < 0.5 || height < 0.5) return;
  commands.push({
    op: 'drawRect',
    x: turtle.x - width / 2, y: turtle.y - height / 2,
    w: width, h: height,
    color: turtle.color, width: turtle.size
  });
}

/** rr(w, h) 实心长方形 */
function rr(w, h) {
  _checkLimit();
  var width = Math.abs(Number(w) || 0);
  var height = Math.abs(Number(h) || 0);
  if (width < 0.5 || height < 0.5) return;
  commands.push({
    op: 'fillRect',
    x: turtle.x - width / 2, y: turtle.y - height / 2,
    w: width, h: height,
    color: turtle.color
  });
}

/** moveTo(x, y) 移动到指定坐标，不画线 */
function moveTo(x, y) {
  _checkLimit();
  turtle.x = Number(x) || 0;
  turtle.y = Number(y) || 0;
}
var moveto = moveTo;

/** lineTo(x, y) 从当前位置连线到指定坐标 */
function lineTo(x, y) {
  _checkLimit();
  var nx = Number(x) || 0;
  var ny = Number(y) || 0;
  if (turtle.penDown) {
    commands.push({
      op: 'line',
      x1: turtle.x, y1: turtle.y,
      x2: nx, y2: ny,
      color: turtle.color, width: turtle.size
    });
  }
  turtle.x = nx;
  turtle.y = ny;
}
var lineto = lineTo;

/** wait(ms) 等待（浏览器端立即返回） */
function wait(ms) { _checkLimit(); }

/** sound(freq, ms) 播放声音（浏览器端记录到输出） */
function sound(freq, ms) {
  _checkLimit();
  logOutput.push('[声音] ' + (Number(freq) || 440) + 'Hz · ' + (Number(ms) || 200) + 'ms');
}

/** ctime() 返回自开始运行以来的毫秒数 */
function ctime() { return startTime ? (Date.now() - startTime) : 0; }

/** cinWin() 打开输入窗口（浏览器端固定显示底部输入区） */
function cinWin() { _checkLimit(); }

/** coutWin() 输出窗口（浏览器端固定显示底部输出区） */
function coutWin() { _checkLimit(); }

/** pk() 加载图片（浏览器版暂不支持，记录提示） */
function pk() {
  _checkLimit();
  logOutput.push('[提示] 浏览器版暂不支持加载图片');
}

/* ==================== 输入队列 ==================== */

function _nextToken() {
  if (inputIndex >= inputTokens.length) {
    logOutput.push('[cin] 输入数据不足，已使用默认值');
    return '0';
  }
  return inputTokens[inputIndex++];
}

/** cin >> x 使用：数字按数值返回，否则返回字符串 */
function __cinAuto() {
  var t = _nextToken();
  if (/^-?\d+(\.\d+)?$/.test(t)) return parseFloat(t);
  return t;
}

/** 数值输入 */
function __cinNum() {
  var v = parseFloat(_nextToken());
  return isNaN(v) ? 0 : v;
}

/** 字符串输入 */
function __cinStr() { return _nextToken(); }

// ==================== C++ 转译器 ====================

/**
 * 将 GoC C++ 代码转译为 JavaScript
 * 支持：变量声明、函数定义、for/while 循环、if/else、cout、turtle API
 */
function transpile(cppCode) {
  var code = cppCode;
  var warnings = [];

  // --- 1. 提取字符串和字符字面量，防止被误转换 ---
  var literals = [];
  code = code.replace(/"(?:[^"\\]|\\.)*"/g, function(m) {
    var id = '\x00STR' + literals.length + '\x00';
    literals.push(m);
    return id;
  });
  code = code.replace(/'(?:[^'\\]|\\.)*'/g, function(m) {
    var id = '\x00CHR' + literals.length + '\x00';
    literals.push(m);
    return id;
  });

  // --- 2. 处理 #define 常量 ---
  code = code.replace(/#\s*define\s+(\w+)\s+(.+)$/gm, function(match, name, value) {
    value = value.trim();
    // 只转换简单常量定义，跳过函数宏
    if (/^[\d.]+$/.test(value) || /^".*"$/.test(value) || /^0x[0-9a-fA-F]+$/.test(value)) {
      return 'var ' + name + ' = ' + value + ';';
    }
    return '';
  });

  // --- 3. 移除其他预处理指令 ---
  code = code.replace(/^\s*#\s*\w+.*$/gm, '');

  // --- 4. 移除 using namespace ---
  code = code.replace(/\busing\s+namespace\s+\w+\s*;/g, '');

  // --- 5. 移除 std:: 前缀 ---
  code = code.replace(/\bstd::/g, '');

  // --- 6. 移除 p. 前缀（pencolor → color 等） ---
  // 支持 p.fd(100), pen.fd(100), t.fd(100) 等写法
  var turtleFuncs = 'fd|forward|bk|backward|back|rt|right|lt|left|pencolor|penColor|setpencolor|setColor|pensize|penSize|setlinewidth|setLineWidth|penup|pu|penUp|pendown|pd|penDown|speed|circle|dot|hideturtle|hide|showturtle|show|goto|setxy|setXY|setangle|setAngle|setheading|getx|getX|gety|getY|getangle|getAngle|text|clearscreen|clear|home|println|print|write|initGraph|initgraph|openGraph|closeGraph|closegraph|pause|setTextColor|settextcolor|setBackgroundColor|setbackgroundcolor|clearText|cleartext|rand|random|abs|sqrt|pow|sin|cos|tan|round|floor|ceil|inputInt|getInt|inputFloat|getFloat|inputString|getString|pen|ct|size|up|down|oc|oo|q|ee|rl|rr|moveTo|moveto|lineTo|lineto|wait|sound|ctime|cinWin|coutWin|pk|fillRect|drawRect|line';
  code = code.replace(new RegExp('\\b(?:p|pen|t)\\.(' + turtleFuncs + ')\\s*\\(', 'g'), '$1(');

  // --- 6.5. 转换 GoC repeat(n) { ... } 语法为标准 for 循环 ---
  // repeat(4) { ... } → for (var _rpt0 = 0; _rpt0 < (4); _rpt0++) { ... }
  var repeatCounter = 0;
  code = code.replace(/\brepeat\s*\(([^)]+)\)\s*\{/g, function(match, expr) {
    var varName = '_rpt' + (repeatCounter++);
    return 'for (var ' + varName + ' = 0; ' + varName + ' < (' + expr.trim() + '); ' + varName + '++) {';
  });

  // --- 7. 转换函数定义 ---
  // int/double/void/bool/string/long/short/char/float funcName(params) {
  code = code.replace(
    /^(\s*)(?:(?:inline|static|const)\s+)*(?:unsigned\s+)?(?:int|double|float|void|bool|string|long|short|char|size_t|auto)\s+(\w+)\s*\(([^)]*)\)\s*(?:const)?\s*\{/gm,
    function(match, indent, funcName, params) {
      var jsParams = params.split(',').map(function(p) {
        var param = p.trim();
        // 移除默认值
        param = param.replace(/\s*=.*$/, '');
        // 移除类型前缀和修饰符
        param = param.replace(/^(?:const\s+)?(?:unsigned\s+)?(?:int|double|float|long|short|char|bool|string|size_t|auto|void)\s+/g, '');
        // 移除指针/引用符号
        param = param.replace(/[*&]/g, '');
        return param.trim();
      }).filter(function(p) { return p.length > 0; }).join(', ');
      return indent + 'function ' + funcName + '(' + jsParams + ') {';
    }
  );

  // --- 8. 转换 for 循环中的变量声明 ---
  code = code.replace(/\bfor\s*\(\s*(?:(?:const|static)\s+)?(?:unsigned\s+)?(?:int|double|float|long|short|char|bool|size_t|auto)\s+/g, 'for (let ');

  // --- 9. 转换变量声明 ---
  // 处理: int x = 5; double y = 3.14; string s = "hello"; int a, b, c;
  code = code.replace(
    /^(\s*)(?:(?:const|static)\s+)*(?:unsigned\s+)?(?:int|double|float|long|short|char|bool|string|size_t|auto)\s+(.+?);$/gm,
    function(match, indent, rest) {
      rest = rest.trim();
      // 数组声明: int arr[10] 或 int arr[]
      var arrMatch = rest.match(/^(\w+)\s*\[(\d*|)\]$/);
      if (arrMatch) {
        var size = arrMatch[2] ? parseInt(arrMatch[2]) : 0;
        if (size > 0) {
          return indent + 'var ' + arrMatch[1] + ' = new Array(' + size + ').fill(0);';
        }
        return indent + 'var ' + arrMatch[1] + ' = [];';
      }
      // 带初始化的数组: int arr[] = {1, 2, 3}
      var arrInitMatch = rest.match(/^(\w+)\s*\[\s*\]\s*=\s*\{(.+)\}$/);
      if (arrInitMatch) {
        return indent + 'var ' + arrInitMatch[1] + ' = [' + arrInitMatch[2] + '];';
      }
      // 普通声明
      return indent + 'let ' + rest + ';';
    }
  );

  // --- 10. 转换 cout 语句 ---
  code = code.replace(
    /\bcout\s*((?:<<[^;]+?)+);/g,
    function(match, exprs) {
      var parts = exprs.split('<<').map(function(s) {
        return s.trim();
      }).filter(function(s) { return s.length > 0; });
      var jsParts = parts.filter(function(p) {
        return p !== 'endl' && p !== '"\\n"' && p !== "'\\n'";
      });
      if (jsParts.length === 0) {
        return 'println("");';
      }
      return 'println(' + jsParts.join(', ') + ');';
    }
  );

  // --- 11. 处理 endl（非 cout 上下文中的残留） ---
  code = code.replace(/\bendl\b/g, '"\\n"');

  // --- 12. 转换 cin >> 为输入队列读取 ---
  // cin >> a >> b;  ->  a = __cinAuto(); b = __cinAuto();
  code = code.replace(/\bcin\s*((?:>>\s*[^;]+?)+);/g, function(match, exprs) {
    var vars = exprs.split('>>').map(function(s) {
      return s.trim();
    }).filter(function(s) {
      return s.length > 0;
    });
    if (vars.length === 0) return '';
    return vars.map(function(v) {
      return v + ' = __cinAuto();';
    }).join(' ');
  });

  // --- 13. 移除 struct/class 定义中的 public:/private: 标签 ---
  code = code.replace(/^\s*(?:public|private|protected)\s*:\s*$/gm, '');

  // --- 14. 还原字符串和字符字面量 ---
  code = code.replace(/\x00STR(\d+)\x00/g, function(match, idx) {
    return literals[parseInt(idx)];
  });
  code = code.replace(/\x00CHR(\d+)\x00/g, function(match, idx) {
    return literals[parseInt(idx)];
  });

  // --- 15. 在末尾调用 main() ---
  if (/\bfunction\s+main\s*\(/.test(code)) {
    code += '\nmain();';
  } else if (!/\bmain\s*\(/.test(code)) {
    // 没有 main 函数，包裹整个代码直接执行
    // 代码已经可以直接执行
  }

  return { code: code, warnings: warnings };
}

// ==================== 执行引擎 ====================

function execute(cppCode, inputText) {
  // 重置状态
  turtle = {
    x: 0, y: 0, angle: 0,
    penDown: true,
    color: '#000000',
    size: 2,
    visible: true,
    speed: 6
  };
  commands = [];
  cmdCount = 0;
  logOutput = [];

  // 准备 cin 输入队列
  inputTokens = String(inputText || '').split(/\s+/).filter(function(s) {
    return s.length > 0;
  });
  inputIndex = 0;

  startTime = Date.now();

  // 转译
  var result;
  try {
    result = transpile(cppCode);
  } catch (err) {
    self.postMessage({
      type: 'error',
      error: '编译错误: ' + (err.message || String(err)),
      phase: 'compile'
    });
    return;
  }

  var jsCode = result.code;

  // 通知编译完成
  self.postMessage({ type: 'log', text: '[编译完成] 开始执行...', level: 'log' });

  // 执行
  try {
    // 使用 Function 构造器执行，turtle API 函数在全局作用域
    var fn = new Function(jsCode);
    var ret = fn();

    var duration = Date.now() - startTime;

    // 发送日志输出
    for (var i = 0; i < logOutput.length; i++) {
      self.postMessage({ type: 'log', text: logOutput[i], level: 'log' });
    }

    // 发送完成消息
    self.postMessage({
      type: 'done',
      commands: commands,
      turtle: {
        x: turtle.x,
        y: turtle.y,
        angle: turtle.angle,
        visible: turtle.visible
      },
      count: cmdCount,
      duration: duration
    });
  } catch (err) {
    // 区分超时错误和运行时错误
    var errMsg = err.message || String(err);
    var isTimeout = errMsg.indexOf('超过上限') >= 0;

    // 仍然发送已产生的命令（部分绘制）
    var duration = Date.now() - startTime;

    // 发送日志输出
    for (var i = 0; i < logOutput.length; i++) {
      self.postMessage({ type: 'log', text: logOutput[i], level: 'log' });
    }

    self.postMessage({
      type: 'error',
      error: (isTimeout ? '执行超时: ' : '运行时错误: ') + errMsg,
      phase: 'runtime',
      partialCommands: commands,
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
    if (!code.trim()) {
      self.postMessage({
        type: 'error',
        error: '代码为空，请先编写代码',
        phase: 'compile'
      });
      return;
    }
    execute(code, data.input || '');
  }
};

// 通知主线程 Worker 已就绪
self.postMessage({ type: 'ready' });
