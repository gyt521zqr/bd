/**
 * C++ Worker - C++ 转译器 + 执行引擎
 * 在 WebWorker 中隔离执行，不阻塞主线程
 *
 * 消息协议（主线程 → Worker）：
 *   { type: 'run', code: string }
 *
 * 消息协议（Worker → 主线程）：
 *   { type: 'ready' }
 *   { type: 'stdout', text: string }
 *   { type: 'stderr', text: string }
 *   { type: 'done', success: boolean, error?: string, duration: number }
 */

// ==================== 配置 ====================
var MAX_EXECUTIONS = 1000000;
var MAX_LOOP_ITERATIONS = 1000000;

// ==================== 输出缓冲 ====================
var stdoutBuffer = [];
var stderrBuffer = [];
var cmdCount = 0;

// ==================== 输出函数 ====================
function _stdout(text) {
  stdoutBuffer.push(String(text));
}

function _stderr(text) {
  stderrBuffer.push(String(text));
}

// ==================== C++ 标准库模拟 ====================

// 简化版 std::vector
function _Vector(initialItems) {
  var arr = initialItems ? Array.from(initialItems) : [];
  arr.size = function() { return this.length; };
  arr.push_back = function(v) { this.push(v); };
  arr.pop_back = function() { this.pop(); };
  arr.empty = function() { return this.length === 0; };
  arr.front = function() { return this[0]; };
  arr.back = function() { return this[this.length - 1]; };
  arr.clear = function() { this.length = 0; };
  arr.at = function(i) {
    if (i < 0 || i >= this.length) throw new Error('vector index out of range');
    return this[i];
  };
  return arr;
}

// 简化版 std::string 扩展
function _String(val) {
  var s = new String(val || '');
  s.size = function() { return this.length; };
  s.substr = function(start, len) {
    if (len === undefined) return this.toString().substring(start);
    return this.toString().substring(start, start + len);
  };
  s.find = function(sub) { return this.toString().indexOf(sub); };
  s.c_str = function() { return this.toString(); };
  return s;
}

// 简化版 std::map
function _Map() {
  var obj = {};
  var _data = {};
  obj.size = function() { return Object.keys(_data).length; };
  obj.empty = function() { return Object.keys(_data).length === 0; };
  obj.insert = function(k, v) { _data[k] = v; };
  obj.erase = function(k) { delete _data[k]; };
  obj.find = function(k) { return k in _data ? { first: k, second: _data[k] } : null; };
  obj.clear = function() { _data = {}; };
  obj.get = function(k) { return _data[k]; };
  obj.set = function(k, v) { _data[k] = v; };
  return obj;
}

// std::sort
function _sort(arr, cmp) {
  var realArr = Array.isArray(arr) ? arr : Array.from(arr);
  if (typeof cmp === 'function') {
    realArr.sort(cmp);
  } else {
    realArr.sort(function(a, b) { return a - b; });
  }
  return realArr;
}

// 数学函数
var _math = {
  abs: Math.abs,
  sqrt: Math.sqrt,
  pow: Math.pow,
  max: function() { return Math.max.apply(null, arguments); },
  min: function() { return Math.min.apply(null, arguments); },
  floor: Math.floor,
  ceil: Math.ceil,
  round: Math.round,
  sin: Math.sin,
  cos: Math.cos,
  tan: Math.tan,
  log: Math.log,
  log10: Math.log10 || function(x) { return Math.log(x) / Math.LN10; },
  exp: Math.exp,
  PI: Math.PI,
  E: Math.E
};

// ==================== C++ 转译器 ====================

/**
 * 将 C++ 代码转译为 JavaScript
 */
function transpile(cppCode) {
  var code = cppCode;
  var warnings = [];

  // --- 1. 提取字符串和字符字面量 ---
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

  // --- 5.5. 处理模板类型中的 >> 合并问题 ---
  // vector<vector<int>> → vector<vector<int> > (修复 >> 被误解析)
  // 已经在字符串提取后处理，这里不需要

  // --- 6. 转换 typedef ---
  code = code.replace(/\btypedef\s+(\w+)\s+(\w+)\s*;/g, 'var $2 = $1;');

  // --- 7. 转换函数定义 ---
  // 支持模板类型参数如 vector<int>&, const string&, vector<vector<int>>&
  code = code.replace(
    /^(\s*)(?:(?:inline|static|const)\s+)*(?:unsigned\s+)?(?:int|double|float|void|bool|string|long|long\s+long|short|char|size_t|auto|unsigned\s+int|(?:vector|map|set|queue|stack|deque|pair|list)<[^>]+(?:>\s*<[^>]+)*>)\s+(\w+)\s*\(([^)]*)\)\s*(?:const)?\s*\{/gm,
    function(match, indent, funcName, params) {
      var jsParams = params.split(',').map(function(p) {
        var param = p.trim();
        // 移除默认值
        param = param.replace(/\s*=.*$/, '');
        // 移除模板类型前缀（如 vector<int>, map<string,int> 等）
        // 注意：类型后可能直接跟 & 或 *（如 vector<int>& nums），需要 [\s*&]*\s* 匹配
        param = param.replace(/^(?:const\s+)?(?:unsigned\s+)?(?:int|double|float|long|long\s+long|short|char|bool|string|size_t|auto|void|(?:vector|map|set|queue|stack|deque|pair|list)<[^>]+(?:>\s*<[^>]+)*>)[\s*&]*\s*/g, '');
        // 移除指针/引用符号
        param = param.replace(/[*&]/g, '');
        // 移除数组括号
        param = param.replace(/\[\s*\d*\s*\]/g, '');
        return param.trim();
      }).filter(function(p) { return p.length > 0; }).join(', ');
      return indent + 'function ' + funcName + '(' + jsParams + ') {';
    }
  );

  // --- 7.5. 移除独立的函数声明（只有声明没有实现，以分号结尾） ---
  // 例如: int foo(int a, int b);
  code = code.replace(
    /^(\s*)(?:(?:inline|static|const)\s+)*(?:unsigned\s+)?(?:int|double|float|void|bool|string|long|long\s+long|short|char|size_t|auto|unsigned\s+int|(?:vector|map|set|queue|stack|deque|pair|list)<[^>]+(?:>\s*<[^>]+)*>)\s+(\w+)\s*\([^)]*\)\s*;$/gm,
    ''
  );

  // --- 8. 转换 for 循环中的变量声明 ---
  code = code.replace(/\bfor\s*\(\s*(?:(?:const|static)\s+)?(?:unsigned\s+)?(?:int|double|float|long|long\s+long|short|char|bool|size_t|auto)\s+/g, 'for (let ');

  // --- 9. 转换变量声明 ---
  code = code.replace(
    /^(\s*)(?:(?:const|static)\s+)*(?:unsigned\s+)?(?:int|double|float|long|long\s+long|short|char|bool|string|size_t|auto|(?:vector|map|set|queue|stack|deque|pair|list)<[^>]+(?:>\s*<[^>]+)*>)\s+(.+?);$/gm,
    function(match, indent, rest) {
      rest = rest.trim();

      // 移除引用符号 &
      rest = rest.replace(/&/g, '');

      // 检测是否为容器类型声明
      var isContainer = /(?:vector|map|set|queue|stack|deque|pair|list)\s*</.test(match);

      // 数组声明: int arr[10]
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

      // vector/容器初始化: nums = {2, 7, 11, 15} → nums = [2, 7, 11, 15]
      var vecInitMatch = rest.match(/^(\w+)\s*=\s*\{(.+)\}$/);
      if (vecInitMatch) {
        return indent + 'var ' + vecInitMatch[1] + ' = [' + vecInitMatch[2] + '];';
      }

      // 容器声明无初始化: vector<int> result; → var result = [];
      if (isContainer && /^\w+$/.test(rest)) {
        return indent + 'var ' + rest + ' = [];';
      }

      // 多变量声明: int a = 1, b = 2, c = 3
      if (rest.includes(',')) {
        return indent + 'var ' + rest + ';';
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
      var jsParts = parts.map(function(p) {
        if (p === 'endl' || p === '"\\n"') return '"\\n"';
        return 'String(' + p + ')';
      });
      if (jsParts.length === 0) return '_stdout("\\n");';
      return '_stdout(' + jsParts.join(' + ') + ');';
    }
  );

  // --- 11. 转换 cerr 语句 ---
  code = code.replace(
    /\bcerr\s*((?:<<[^;]+?)+);/g,
    function(match, exprs) {
      var parts = exprs.split('<<').map(function(s) {
        return s.trim();
      }).filter(function(s) { return s.length > 0; });
      var jsParts = parts.map(function(p) {
        if (p === 'endl' || p === '"\\n"') return '"\\n"';
        return 'String(' + p + ')';
      });
      if (jsParts.length === 0) return '_stderr("\\n");';
      return '_stderr(' + jsParts.join(' + ') + ');';
    }
  );

  // --- 12. 处理 endl ---
  code = code.replace(/\bendl\b/g, '"\\n"');

  // --- 13. 处理 cin（从 stdin 缓冲区读取） ---
  // cin >> a >> b >> c; → a = _cinRead(); b = _cinRead(); c = _cinRead();
  code = code.replace(/\bcin\s*>>\s*([\s\S]+?);/g, function (match, expr) {
    var vars = expr.split('>>').map(function (v) { return v.trim(); }).filter(Boolean);
    if (vars.length === 0) return match;
    return vars.map(function (v) { return v + ' = _cinRead();'; }).join(' ');
  });

  // --- 14. 移除 struct/class 定义中的标签 ---
  code = code.replace(/^\s*(?:public|private|protected)\s*:\s*$/gm, '');

  // --- 15. 转换 new/delete ---
  code = code.replace(/\bnew\s+(\w+)\s*\[/g, 'new Array(');
  code = code.replace(/\bdelete\s+\w+\s*;/g, '');

  // --- 16. 转换 nullptr ---
  code = code.replace(/\bnullptr\b/g, 'null');

  // --- 17. 转换 bool 字面量 ---
  // true/false 已经是 JS 兼容的，不需要转换

  // --- 18. 转换 sizeof ---
  code = code.replace(/\bsizeof\s*\(([^)]+)\)/g, '(Array.isArray($1) ? $1.length : 1)');

  // --- 18.5. 转换 return {...}; 为 return [...]; ---
  // 处理 return {}; 和 return {1, 2, 3}; 等花括号初始化列表
  code = code.replace(/\breturn\s*\{([^}]*)\}\s*;/g, function(match, content) {
    content = content.trim();
    if (content === '') return 'return [];';
    return 'return [' + content + '];';
  });

  // --- 19. 还原字符串和字符字面量 ---
  code = code.replace(/\x00STR(\d+)\x00/g, function(match, idx) {
    return literals[parseInt(idx)];
  });
  code = code.replace(/\x00CHR(\d+)\x00/g, function(match, idx) {
    return literals[parseInt(idx)];
  });

  // --- 20. 在末尾调用 main() ---
  if (/\bfunction\s+main\s*\(/.test(code)) {
    code += '\nmain();';
  }

  return { code: code, warnings: warnings };
}

// ==================== 执行引擎 ====================

function execute(cppCode, stdin) {
  // 重置状态
  stdoutBuffer = [];
  stderrBuffer = [];
  cmdCount = 0;

  // 解析 stdin 为 token 列表（按空白分割）
  var stdinTokens = (stdin || '').split(/\s+/).filter(function (t) { return t.length > 0; });
  var stdinPos = 0;

  var startTime = Date.now();

  // 转译
  var result;
  try {
    result = transpile(cppCode);
  } catch (err) {
    self.postMessage({
      type: 'stderr',
      text: '编译错误: ' + (err.message || String(err))
    });
    self.postMessage({
      type: 'done',
      success: false,
      error: '编译错误: ' + (err.message || String(err)),
      duration: Date.now() - startTime
    });
    return;
  }

  var jsCode = result.code;

  // 执行
  try {
    // 构建执行环境
    var execFn = new Function(
      '_stdout', '_stderr', '_Vector', '_String', '_Map', '_sort', '_math',
      '"use strict";\n' +
      // 为所有数组添加 vector 兼容方法
      'Array.prototype.size = function() { return this.length; };\n' +
      'Array.prototype.push_back = function(v) { this.push(v); };\n' +
      'Array.prototype.pop_back = function() { this.pop(); };\n' +
      'Array.prototype.empty = function() { return this.length === 0; };\n' +
      'Array.prototype.front = function() { return this[0]; };\n' +
      'Array.prototype.back = function() { return this[this.length - 1]; };\n' +
      'Array.prototype.clear = function() { this.length = 0; };\n' +
      'Array.prototype.at = function(i) { if (i < 0 || i >= this.length) throw new Error("index out of range"); return this[i]; };\n' +
      'var cout = { _data: "", _flush: function() { _stdout(this._data); this._data = ""; } };\n' +
      'var endl = "\\n";\n' +
      'var cin = { _pos: 0 };\n' +
      'var _stdinTokens = ' + JSON.stringify(stdinTokens) + ';\n' +
      'var _stdinPos = 0;\n' +
      'function _cinRead() {\n' +
      '  if (_stdinPos < _stdinTokens.length) {\n' +
      '    var v = _stdinTokens[_stdinPos++];\n' +
      '    if (v !== "" && !isNaN(Number(v))) return Number(v);\n' +
      '    return v;\n' +
      '  }\n' +
      '  return 0;\n' +
      '}\n' +
      'var string = _String;\n' +
      'var vector = _Vector;\n' +
      'var map = _Map;\n' +
      'var sort = _sort;\n' +
      'var abs = _math.abs;\n' +
      'var sqrt = _math.sqrt;\n' +
      'var pow = _math.pow;\n' +
      'var max = _math.max;\n' +
      'var min = _math.min;\n' +
      'var floor = _math.floor;\n' +
      'var ceil = _math.ceil;\n' +
      'var round = _math.round;\n' +
      'var PI = _math.PI;\n' +
      '\n' + jsCode
    );

    execFn(_stdout, _stderr, _Vector, _String, _Map, _sort, _math);

    var duration = Date.now() - startTime;

    // 发送 stdout
    var outputText = stdoutBuffer.join('');
    if (outputText) {
      self.postMessage({ type: 'stdout', text: outputText });
    }

    // 发送 stderr
    var errText = stderrBuffer.join('');
    if (errText) {
      self.postMessage({ type: 'stderr', text: errText });
    }

    self.postMessage({
      type: 'done',
      success: true,
      duration: duration
    });

  } catch (err) {
    var duration = Date.now() - startTime;
    var errMsg = err.message || String(err);

    // 仍然发送已产生的输出
    var outputText = stdoutBuffer.join('');
    if (outputText) {
      self.postMessage({ type: 'stdout', text: outputText });
    }

    self.postMessage({
      type: 'done',
      success: false,
      error: '运行时错误: ' + errMsg,
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
    var stdin = data.stdin || '';
    if (!code.trim()) {
      self.postMessage({
        type: 'done',
        success: false,
        error: '代码为空，请先编写代码',
        duration: 0
      });
      return;
    }
    execute(code, stdin);
  }
};

// 通知主线程 Worker 已就绪
self.postMessage({ type: 'ready' });
