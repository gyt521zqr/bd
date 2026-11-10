/**
 * GoC 编程主应用
 * - Monaco Editor（C++ 语法高亮）+ Canvas 海龟绘图
 * - 常见命令面板：鼠标点击把代码片段插入到编辑器光标处
 * - WebWorker 隔离执行（C++ 转译 + 海龟 API）
 * - cinWin / coutWin 输入输出
 *
 * 与父窗口通信协议保持不变：
 *   父 -> iframe：goc:loadCode / goc:run / goc:getCode / goc:lock /
 *                 goc:clearCanvas / goc:resetTurtle / goc:submit
 *   iframe -> 父：goc:ready / goc:codeChange / goc:runResult /
 *                 goc:codeData / goc:submitResult
 */

(function () {
  'use strict';

  /* ==================== 默认示例代码 ==================== */

  var DEFAULT_CODE = [
    '#include "turtle.h"',
    '',
    '// 点击下方「常见命令」可以快速插入代码',
    'int main()',
    '{',
    '    pen(1);              // 红色画笔',
    '    for (int i = 0; i < 4; i++) {',
    '        fd(120);',
    '        rt(90);',
    '    }',
    '',
    '    pen(6);              // 蓝色画笔',
    '    moveTo(-80, -80);',
    '    text("Hello GoC");',
    '',
    '    return 0;',
    '}'
  ].join('\n');

  /* ==================== GoC 16 色 ==================== */

  var GOC_COLORS = [
    { i: 0,  hex: '#000000', name: '黑' },
    { i: 1,  hex: '#ff0000', name: '红' },
    { i: 2,  hex: '#ff8000', name: '橙' },
    { i: 3,  hex: '#ffd700', name: '黄' },
    { i: 4,  hex: '#00b050', name: '绿' },
    { i: 5,  hex: '#00b0f0', name: '青' },
    { i: 6,  hex: '#0070c0', name: '蓝' },
    { i: 7,  hex: '#7030a0', name: '紫' },
    { i: 8,  hex: '#ff00ff', name: '品红' },
    { i: 9,  hex: '#ff99cc', name: '粉' },
    { i: 10, hex: '#808080', name: '灰' },
    { i: 11, hex: '#800000', name: '深红' },
    { i: 12, hex: '#004000', name: '深绿' },
    { i: 13, hex: '#000080', name: '深蓝' },
    { i: 14, hex: '#808000', name: '橄榄' },
    { i: 15, hex: '#ffffff', name: '白' }
  ];

  /* ==================== 常见命令数据 ==================== */
  /* 片段标记：^...^ 表示插入后被选中的占位内容；单个 ^ 表示光标位置 */

  var DRAW_COMMANDS = [
    { label: 'pen',      desc: '笔',        insert: 'pen(^1^);' },
    { label: 'fd()',     desc: '前进',      insert: 'fd(^100^);' },
    { label: 'rt()',     desc: '右转',      insert: 'rt(^90^);' },
    { label: 'ct()',     desc: '颜色',      insert: 'ct(^1^);' },
    { label: 'up()',     desc: '抬笔',      insert: 'up();' },
    { label: 'oc()',     desc: '圆',        insert: 'oc(^50^);' },
    { label: 'q()',      desc: '椭圆',      insert: 'q(^60^, 40);' },
    { label: 'rl()',     desc: '长方形',    insert: 'rl(^100^, 60);' },
    { label: 'pk()',     desc: '图图片',    insert: 'pk(^0^);' },
    { label: 'text()',   desc: '文字',      insert: 'text("^文字^");' },
    { label: 'hide()',   desc: '隐藏',      insert: 'hide();' },
    { label: 'moveTo()', desc: '移动',      insert: 'moveTo(^0^, 0);' },
    { label: 'wait()',   desc: '等待',      insert: 'wait(^500^);' },
    { label: 'sound()',  desc: '播放',      insert: 'sound(^500^, 200);' },
    { label: 'pause()',  desc: '暂停',      insert: 'pause();' }
  ];

  var CONTROL_COMMANDS = [
    { label: 'bk()',     desc: '后退',      insert: 'bk(^100^);' },
    { label: 'lt()',     desc: '左转',      insert: 'lt(^90^);' },
    { label: 'size()',   desc: '粗细',      insert: 'size(^3^);' },
    { label: 'down()',   desc: '落笔',      insert: 'down();' },
    { label: 'oo()',     desc: '实心圆',    insert: 'oo(^50^);' },
    { label: 'ee()',     desc: '实心椭圆',  insert: 'ee(^60^, 40);' },
    { label: 'rr()',     desc: '实心长方形', insert: 'rr(^100^, 60);' },
    { label: 'speed()',  desc: '速度',      insert: 'speed(^6^);' },
    { label: 'show()',   desc: '显示',      insert: 'show();' },
    { label: 'lineTo()', desc: '连线',      insert: 'lineTo(^100^, 0);' },
    { label: 'ctime()',  desc: '计时器',    insert: 'ctime();' },
    { label: 'sound()',  desc: '播放声音',  insert: 'sound(^500^, 200);' }
  ];

  var CPP_COMMANDS = [
    { label: '构架',                  desc: '框架',  insert: 'int main()\n{\n    ^\n    return 0;\n}' },
    { label: 'if()',                  desc: '如果',  insert: 'if (^condition^) {\n    \n}' },
    { label: 'else',                  desc: '否则',  insert: 'else {\n    ^\n}' },
    { label: 'for(;;)',               desc: '循环',  insert: 'for (;;) {\n    ^\n}' },
    { label: 'for(int i=0;i<n;i++)',  desc: '循环',  insert: 'for (int i = 0; i < ^n^; i++) {\n    \n}' },
    { label: '=',                     desc: '赋值',  insert: '^ = ;' },
    { label: '复合',                  desc: '复合',  insert: '^ += ;' },
    { label: 'cin>>',                 desc: '输入',  insert: 'cin >> ^;' },
    { label: 'cout<<',                desc: '输出',  insert: 'cout << ^;' },
    { label: 'cinWin()',              desc: '窗口输入', insert: 'cinWin();' },
    { label: 'double',                desc: '浮点数', insert: 'double ^ = 0;' },
    { label: 'string',                desc: '字符串', insert: 'string ^ = "";' },
    { label: '数组',                  desc: '数组',  insert: 'int ^arr[10];' }
  ];

  /* ==================== DOM 元素 ==================== */

  var elements = {
    app: document.getElementById('gocApp'),
    btnCopy: document.getElementById('btnCopy'),
    btnDownload: document.getElementById('btnDownload'),
    btnGotoCode: document.getElementById('btnGotoCode'),
    btnRun: document.getElementById('btnRun'),
    btnClearCanvas: document.getElementById('btnClearCanvas'),
    btnResetTurtle: document.getElementById('btnResetTurtle'),
    btnSubmit: document.getElementById('btnSubmit'),
    btnClearConsole: document.getElementById('btnClearConsole'),
    projectName: document.getElementById('projectName'),
    autoSave: document.getElementById('autoSave'),
    statusBadge: document.getElementById('statusBadge'),
    editorHint: document.getElementById('editorHint'),
    editorContainer: document.getElementById('editorContainer'),
    monacoEditor: document.getElementById('monacoEditor'),
    codeTextarea: document.getElementById('codeTextarea'),
    rightPanel: document.querySelector('.goc-right-panel'),
    commandTables: document.querySelector('.goc-command-tables'),
    canvas: document.getElementById('drawCanvas'),
    canvasWrapper: document.getElementById('canvasWrapper'),
    cinInput: document.getElementById('cinInput'),
    coutOutput: document.getElementById('coutOutput'),
    loadingOverlay: document.getElementById('loadingOverlay')
  };

  /* ==================== 状态 ==================== */

  var state = {
    editor: null,
    useMonaco: false,
    worker: null,
    workerReady: false,
    running: false,
    locked: false,
    timeoutId: null,
    timeoutMs: 10000,
    autoSaveTimer: null,
    hintTimer: null,
    lastSavedAt: '',
    lastCommands: [],
    lastTurtle: { x: 0, y: 0, angle: 0, visible: true }
  };

  /* ==================== 通用工具 ==================== */

  function getQueryParam(name) {
    try {
      return new URLSearchParams(window.location.search).get(name) || '';
    } catch (e) {
      return '';
    }
  }

  function closestEl(el, selector) {
    while (el && el !== document) {
      if (el.nodeType === 1 && el.matches && el.matches(selector)) return el;
      el = el.parentNode;
    }
    return null;
  }

  function toast(message) {
    var el = document.createElement('div');
    el.className = 'goc-toast';
    el.textContent = message;
    document.body.appendChild(el);
    setTimeout(function () { el.classList.add('is-out'); }, 1600);
    setTimeout(function () {
      if (el.parentNode) el.parentNode.removeChild(el);
    }, 2050);
  }

  function two(n) { return (n < 10 ? '0' : '') + n; }

  /* ==================== Canvas 渲染 ==================== */

  var ctx = elements.canvas.getContext('2d');
  var canvasWidth = 600;
  var canvasHeight = 500;
  var canvasRetry = 0;

  function initCanvas() {
    var wrapper = elements.canvasWrapper;
    if (!wrapper) return;
    var w = wrapper.clientWidth;
    var h = wrapper.clientHeight;

    // 布局尚未完成时稍后重试（最多约 3 秒）
    if (w < 20 || h < 20) {
      if (canvasRetry < 50) {
        canvasRetry++;
        setTimeout(initCanvas, 60);
      }
      return;
    }
    canvasRetry = 0;

    var dpr = window.devicePixelRatio || 1;
    canvasWidth = w;
    canvasHeight = h;

    elements.canvas.width = Math.round(w * dpr);
    elements.canvas.height = Math.round(h * dpr);
    elements.canvas.style.width = w + 'px';
    elements.canvas.style.height = h + 'px';

    ctx.setTransform(dpr, 0, 0, dpr, 0, 0);
    ctx.lineCap = 'round';
    ctx.lineJoin = 'round';

    repaint();
  }

  /** 重绘画布背景（浅蓝底 + 极淡网格 + 中心线） */
  function resetCanvas() {
    ctx.save();
    ctx.setTransform(window.devicePixelRatio || 1, 0, 0, window.devicePixelRatio || 1, 0, 0);

    ctx.fillStyle = '#f3fbff';
    ctx.fillRect(0, 0, canvasWidth, canvasHeight);

    ctx.strokeStyle = '#e6f3fa';
    ctx.lineWidth = 1;
    var step = 25;
    var x, y;
    for (x = 0; x <= canvasWidth; x += step) {
      ctx.beginPath();
      ctx.moveTo(x + 0.5, 0);
      ctx.lineTo(x + 0.5, canvasHeight);
      ctx.stroke();
    }
    for (y = 0; y <= canvasHeight; y += step) {
      ctx.beginPath();
      ctx.moveTo(0, y + 0.5);
      ctx.lineTo(canvasWidth, y + 0.5);
      ctx.stroke();
    }

    // 中心十字
    ctx.strokeStyle = '#d8edf7';
    ctx.beginPath();
    ctx.moveTo(canvasWidth / 2 + 0.5, 0);
    ctx.lineTo(canvasWidth / 2 + 0.5, canvasHeight);
    ctx.stroke();
    ctx.beginPath();
    ctx.moveTo(0, canvasHeight / 2 + 0.5);
    ctx.lineTo(canvasWidth, canvasHeight / 2 + 0.5);
    ctx.stroke();

    ctx.restore();
  }

  /** 海龟坐标（原点在画布中心） -> 画布坐标 */
  function toCanvasX(v) { return canvasWidth / 2 + v; }
  function toCanvasY(v) { return canvasHeight / 2 + v; }

  /** 渲染绘图指令 */
  function renderCommands(commands) {
    for (var i = 0; i < commands.length; i++) {
      var cmd = commands[i];
      switch (cmd.op) {
        case 'line':
          ctx.beginPath();
          ctx.moveTo(toCanvasX(cmd.x1), toCanvasY(cmd.y1));
          ctx.lineTo(toCanvasX(cmd.x2), toCanvasY(cmd.y2));
          ctx.strokeStyle = cmd.color;
          ctx.lineWidth = Math.max(1, cmd.width || 1);
          ctx.stroke();
          break;

        case 'circle':
          ctx.beginPath();
          ctx.arc(toCanvasX(cmd.x), toCanvasY(cmd.y), Math.max(0.5, cmd.r), 0, Math.PI * 2);
          ctx.strokeStyle = cmd.color;
          ctx.lineWidth = Math.max(1, cmd.width || 1);
          ctx.stroke();
          break;

        case 'fillCircle':
          ctx.beginPath();
          ctx.arc(toCanvasX(cmd.x), toCanvasY(cmd.y), Math.max(0.5, cmd.r), 0, Math.PI * 2);
          ctx.fillStyle = cmd.color;
          ctx.fill();
          break;

        case 'ellipse':
          ctx.beginPath();
          ctx.ellipse(toCanvasX(cmd.x), toCanvasY(cmd.y),
            Math.max(0.5, cmd.rx), Math.max(0.5, cmd.ry), 0, 0, Math.PI * 2);
          ctx.strokeStyle = cmd.color;
          ctx.lineWidth = Math.max(1, cmd.width || 1);
          ctx.stroke();
          break;

        case 'fillEllipse':
          ctx.beginPath();
          ctx.ellipse(toCanvasX(cmd.x), toCanvasY(cmd.y),
            Math.max(0.5, cmd.rx), Math.max(0.5, cmd.ry), 0, 0, Math.PI * 2);
          ctx.fillStyle = cmd.color;
          ctx.fill();
          break;

        case 'dot':
          ctx.beginPath();
          ctx.arc(toCanvasX(cmd.x), toCanvasY(cmd.y), Math.max(1, cmd.r), 0, Math.PI * 2);
          ctx.fillStyle = cmd.color;
          ctx.fill();
          break;

        case 'text':
          ctx.fillStyle = cmd.color;
          ctx.font = Math.max(12, cmd.size || 16) + 'px "Microsoft YaHei", sans-serif';
          ctx.textAlign = 'center';
          ctx.textBaseline = 'middle';
          ctx.fillText(cmd.text, toCanvasX(cmd.x), toCanvasY(cmd.y));
          break;

        case 'fillRect':
          ctx.fillStyle = cmd.color;
          ctx.fillRect(toCanvasX(cmd.x), toCanvasY(cmd.y), cmd.w, cmd.h);
          break;

        case 'drawRect':
          ctx.strokeStyle = cmd.color;
          ctx.lineWidth = Math.max(1, cmd.width || 1);
          ctx.strokeRect(toCanvasX(cmd.x), toCanvasY(cmd.y), cmd.w, cmd.h);
          break;

        case 'setBackground':
          ctx.fillStyle = cmd.color;
          ctx.fillRect(0, 0, canvasWidth, canvasHeight);
          break;

        case 'clear':
          resetCanvas();
          break;
      }
    }
  }

  /** 绘制海龟光标 */
  function drawTurtle(t) {
    if (!t || t.visible === false) return;
    var cx = toCanvasX(t.x || 0);
    var cy = toCanvasY(t.y || 0);
    var rad = (t.angle || 0) * Math.PI / 180;

    ctx.save();
    ctx.translate(cx, cy);
    ctx.rotate(rad);

    // 四只脚
    ctx.fillStyle = '#7ec850';
    var legs = [[-7, -6], [-7, 6], [6, -6], [6, 6]];
    for (var i = 0; i < legs.length; i++) {
      ctx.beginPath();
      ctx.arc(legs[i][0], legs[i][1], 2.4, 0, Math.PI * 2);
      ctx.fill();
    }

    // 身体
    ctx.beginPath();
    ctx.ellipse(0, 0, 9, 7, 0, 0, Math.PI * 2);
    ctx.fillStyle = '#8bd450';
    ctx.fill();
    ctx.lineWidth = 1.2;
    ctx.strokeStyle = '#3f8f1f';
    ctx.stroke();

    // 龟壳
    ctx.beginPath();
    ctx.ellipse(0, 0, 5.2, 3.8, 0, 0, Math.PI * 2);
    ctx.fillStyle = '#6fbf3a';
    ctx.fill();

    // 头（朝向 +x）
    ctx.beginPath();
    ctx.arc(10, 0, 4, 0, Math.PI * 2);
    ctx.fillStyle = '#8bd450';
    ctx.fill();
    ctx.strokeStyle = '#3f8f1f';
    ctx.stroke();

    // 眼睛
    ctx.beginPath();
    ctx.arc(11.5, -1.4, 0.9, 0, Math.PI * 2);
    ctx.fillStyle = '#1f2329';
    ctx.fill();

    ctx.restore();
  }

  /** 重绘：背景 + 上一次绘图指令 + 海龟 */
  function repaint() {
    resetCanvas();
    if (state.lastCommands && state.lastCommands.length) {
      renderCommands(state.lastCommands);
    }
    if (state.lastTurtle) {
      drawTurtle(state.lastTurtle);
    }
  }

  /* ==================== 输出面板（coutWin） ==================== */

  function appendOutput(text, level) {
    if (!elements.coutOutput) return;
    var line = document.createElement('div');
    line.className = 'goc-out-line' + (level && level !== 'log' ? ' goc-out-' + level : '');
    line.textContent = text;
    elements.coutOutput.appendChild(line);

    while (elements.coutOutput.childElementCount > 500) {
      elements.coutOutput.removeChild(elements.coutOutput.firstElementChild);
    }
    elements.coutOutput.scrollTop = elements.coutOutput.scrollHeight;
  }

  function clearOutput() {
    if (elements.coutOutput) elements.coutOutput.innerHTML = '';
  }

  /* ==================== 状态显示 ==================== */

  function setStatus(status, text) {
    if (!elements.statusBadge) return;
    elements.statusBadge.className = 'goc-status-badge goc-status-' + status;
    elements.statusBadge.textContent = text;
  }

  function setRunning(running) {
    state.running = running;
    if (elements.btnRun) elements.btnRun.disabled = running;
    if (elements.btnSubmit) elements.btnSubmit.disabled = running;
    if (running) setStatus('running', '运行中...');
  }

  function flashHint(text) {
    if (!elements.editorHint) return;
    elements.editorHint.textContent = text;
    clearTimeout(state.hintTimer);
    state.hintTimer = setTimeout(function () {
      elements.editorHint.textContent = state.lastSavedAt
        ? ('已自动保存 ' + state.lastSavedAt)
        : 'C++ 代码';
    }, 2600);
  }

  /* ==================== 常见命令面板 ==================== */

  function buildSimpleTable(tbodyId, list) {
    var tbody = document.getElementById(tbodyId);
    if (!tbody) return;
    var frag = document.createDocumentFragment();
    for (var i = 0; i < list.length; i++) {
      var item = list[i];
      var tr = document.createElement('tr');
      tr.setAttribute('data-insert', item.insert);
      tr.title = item.label + ' — ' + item.desc;

      var tdCmd = document.createElement('td');
      tdCmd.className = 'goc-cmd-name';
      tdCmd.textContent = item.label;

      var tdDesc = document.createElement('td');
      tdDesc.className = 'goc-cmd-desc';
      tdDesc.textContent = item.desc;

      tr.appendChild(tdCmd);
      tr.appendChild(tdDesc);
      frag.appendChild(tr);
    }
    tbody.appendChild(frag);
  }

  function buildPalette() {
    var drawBody = document.getElementById('drawTableBody');
    if (!drawBody) return;

    var frag = document.createDocumentFragment();
    var rows = Math.max(GOC_COLORS.length, DRAW_COMMANDS.length);

    for (var i = 0; i < rows; i++) {
      var tr = document.createElement('tr');
      var color = GOC_COLORS[i];
      var cmd = DRAW_COMMANDS[i];

      // 颜色单元格
      var tdColor = document.createElement('td');
      tdColor.className = 'goc-color-cell';
      if (color) {
        tdColor.setAttribute('data-color', String(color.i));
        tdColor.title = '画笔颜色 ' + color.i + '（' + color.name + '），点击插入 pen(' + color.i + ')';
        var sw = document.createElement('span');
        sw.className = 'goc-swatch';
        sw.style.background = color.hex;
        var num = document.createElement('span');
        num.className = 'goc-color-num';
        num.textContent = String(color.i);
        tdColor.appendChild(sw);
        tdColor.appendChild(num);
      }
      tr.appendChild(tdColor);

      // 绘图命令 + 功能
      var tdCmd = document.createElement('td');
      var tdDesc = document.createElement('td');
      if (cmd) {
        tr.setAttribute('data-insert', cmd.insert);
        tr.title = cmd.label + ' — ' + cmd.desc;
        tdCmd.className = 'goc-cmd-name';
        tdCmd.textContent = cmd.label;
        tdDesc.className = 'goc-cmd-desc';
        tdDesc.textContent = cmd.desc;
      } else {
        tdCmd.className = 'goc-cmd-empty';
        tdDesc.className = 'goc-cmd-empty';
      }
      tr.appendChild(tdCmd);
      tr.appendChild(tdDesc);

      frag.appendChild(tr);
    }
    drawBody.appendChild(frag);

    buildSimpleTable('controlTableBody', CONTROL_COMMANDS);
    buildSimpleTable('cppTableBody', CPP_COMMANDS);
  }

  function setupPaletteEvents() {
    if (!elements.commandTables) return;
    elements.commandTables.addEventListener('click', function (e) {
      if (state.locked) {
        toast('作品已提交，编辑器已锁定');
        return;
      }

      var colorCell = closestEl(e.target, '[data-color]');
      if (colorCell) {
        var idx = colorCell.getAttribute('data-color');
        insertCode('pen(^' + idx + '^);');
        flashHint('已插入颜色 pen(' + idx + ')');
        return;
      }

      var row = closestEl(e.target, 'tr[data-insert]');
      if (row) {
        insertCode(row.getAttribute('data-insert'));
      }
    });
  }

  /**
   * 解析命令片段
   *   ^文字^  -> 插入后选中“文字”（占位内容，直接输入即可替换）
   *   ^       -> 插入后光标停在该位置
   *   无标记   -> 光标停在片段末尾
   */
  function parseSnippet(raw) {
    var first = raw.indexOf('^');
    if (first < 0) {
      return { text: raw, caret: raw.length, selLen: 0 };
    }
    var second = raw.indexOf('^', first + 1);
    if (second >= 0) {
      var selected = raw.slice(first + 1, second);
      return {
        text: raw.slice(0, first) + selected + raw.slice(second + 1),
        caret: first,
        selLen: selected.length
      };
    }
    return {
      text: raw.slice(0, first) + raw.slice(first + 1),
      caret: first,
      selLen: 0
    };
  }

  /** 把片段偏移量换算成 Monaco 的行列位置 */
  function offsetToPosition(baseLine, baseColumn, text, offset) {
    var before = text.slice(0, offset);
    var parts = before.split('\n');
    if (parts.length === 1) {
      return { lineNumber: baseLine, column: baseColumn + parts[0].length };
    }
    return {
      lineNumber: baseLine + parts.length - 1,
      column: parts[parts.length - 1].length + 1
    };
  }

  /**
   * 把代码片段插入到当前光标处（或替换选中内容）
   */
  function insertCode(raw) {
    if (!raw) return;
    if (state.locked) {
      toast('作品已提交，编辑器已锁定');
      return;
    }

    var parsed = parseSnippet(raw);
    var text = parsed.text;

    if (state.useMonaco && state.editor) {
      var editor = state.editor;
      var sel = editor.getSelection();
      if (!sel) return;

      editor.executeEdits('goc-palette', [{
        range: sel,
        text: text,
        forceMoveMarkers: true
      }]);

      var startPos = offsetToPosition(sel.startLineNumber, sel.startColumn, text, parsed.caret);
      var endPos = offsetToPosition(
        sel.startLineNumber, sel.startColumn, text, parsed.caret + parsed.selLen
      );

      editor.setSelection(new monaco.Selection(
        startPos.lineNumber, startPos.column,
        endPos.lineNumber, endPos.column
      ));
      editor.revealPositionInCenterIfOutsideViewport(startPos);
      editor.focus();
    } else {
      var ta = elements.codeTextarea;
      if (!ta) return;
      var start = ta.selectionStart || 0;
      var end = ta.selectionEnd || 0;
      ta.value = ta.value.slice(0, start) + text + ta.value.slice(end);
      ta.selectionStart = start + parsed.caret;
      ta.selectionEnd = start + parsed.caret + parsed.selLen;
      ta.focus();
      notifyParentCodeChange();
      scheduleAutoSave();
    }

    flashHint('已插入：' + text.split('\n')[0].slice(0, 24).trim());
  }

  function lockPalette(locked) {
    if (!elements.commandTables) return;
    var rows = elements.commandTables.querySelectorAll('tr');
    for (var i = 0; i < rows.length; i++) {
      if (locked) rows[i].classList.add('is-locked');
      else rows[i].classList.remove('is-locked');
    }
  }

  /* ==================== Monaco Editor ==================== */

  function initMonaco() {
    if (typeof require === 'undefined' || typeof require.config === 'undefined') {
      fallbackToTextarea();
      return;
    }

    try {
      require.config({
        paths: { vs: 'https://cdnjs.cloudflare.com/ajax/libs/monaco-editor/0.45.0/min/vs' }
      });

      require(['vs/editor/editor.main'], function () {
        try {
          monaco.editor.defineTheme('goc-dark', {
            base: 'vs-dark',
            inherit: true,
            rules: [
              { token: 'keyword', foreground: 'c678dd', fontStyle: 'bold' },
              { token: 'comment', foreground: '5c6370', fontStyle: 'italic' },
              { token: 'string', foreground: '98c379' },
              { token: 'number', foreground: 'd19a66' },
              { token: 'type', foreground: '61afef' },
              { token: 'function', foreground: '61afef' }
            ],
            colors: {
              'editor.background': '#1e1e1e',
              'editor.foreground': '#d7dae0',
              'editor.lineHighlightBackground': '#2a2d2e',
              'editorLineNumber.foreground': '#5c6370',
              'editorLineNumber.activeForeground': '#abb2bf',
              'editor.selectionBackground': '#3a3d41',
              'editorCursor.foreground': '#00a870'
            }
          });

          state.editor = monaco.editor.create(elements.monacoEditor, {
            value: getInitialCode(),
            language: 'cpp',
            theme: 'goc-dark',
            fontSize: 13.5,
            fontFamily: "'Consolas', 'Courier New', monospace",
            minimap: { enabled: false },
            automaticLayout: true,
            scrollBeyondLastLine: false,
            tabSize: 4,
            insertSpaces: true,
            wordWrap: 'on',
            lineNumbers: 'on',
            renderLineHighlight: 'line',
            folding: true,
            bracketPairColorization: { enabled: true },
            padding: { top: 8, bottom: 8 }
          });

          registerGoCCompletions();
          state.useMonaco = true;

          state.editor.onDidChangeModelContent(function () {
            notifyParentCodeChange();
            scheduleAutoSave();
          });

          hideLoading();
          onEditorReady();
        } catch (err) {
          console.error('Monaco 创建失败:', err);
          fallbackToTextarea();
        }
      });
    } catch (err) {
      console.error('Monaco 加载失败:', err);
      fallbackToTextarea();
    }
  }

  function fallbackToTextarea() {
    if (state.useMonaco) return;
    elements.monacoEditor.style.display = 'none';
    elements.codeTextarea.style.display = 'block';
    elements.codeTextarea.value = getInitialCode();

    elements.codeTextarea.addEventListener('input', function () {
      notifyParentCodeChange();
      scheduleAutoSave();
    });

    elements.codeTextarea.addEventListener('keydown', function (e) {
      if (e.key === 'Tab') {
        e.preventDefault();
        var start = this.selectionStart;
        var end = this.selectionEnd;
        this.value = this.value.substring(0, start) + '    ' + this.value.substring(end);
        this.selectionStart = this.selectionEnd = start + 4;
        notifyParentCodeChange();
      }
    });

    state.useMonaco = false;
    hideLoading();
    onEditorReady();
    appendOutput('Monaco 编辑器加载失败，已切换为基础文本编辑器', 'warn');
  }

  function registerGoCCompletions() {
    if (!state.useMonaco) return;

    var apiList = [
      { name: 'pen', insert: 'pen(${1:1})', doc: '设置画笔颜色（0-15）' },
      { name: 'fd', insert: 'fd(${1:100})', doc: '前进指定距离' },
      { name: 'bk', insert: 'bk(${1:100})', doc: '后退指定距离' },
      { name: 'rt', insert: 'rt(${1:90})', doc: '顺时针旋转' },
      { name: 'lt', insert: 'lt(${1:90})', doc: '逆时针旋转' },
      { name: 'ct', insert: 'ct(${1:1})', doc: '设置颜色' },
      { name: 'up', insert: 'up()', doc: '抬笔' },
      { name: 'down', insert: 'down()', doc: '落笔' },
      { name: 'size', insert: 'size(${1:3})', doc: '画笔粗细' },
      { name: 'oc', insert: 'oc(${1:50})', doc: '空心圆' },
      { name: 'oo', insert: 'oo(${1:50})', doc: '实心圆' },
      { name: 'q', insert: 'q(${1:60}, ${2:40})', doc: '空心椭圆' },
      { name: 'ee', insert: 'ee(${1:60}, ${2:40})', doc: '实心椭圆' },
      { name: 'rl', insert: 'rl(${1:100}, ${2:60})', doc: '空心长方形' },
      { name: 'rr', insert: 'rr(${1:100}, ${2:60})', doc: '实心长方形' },
      { name: 'moveTo', insert: 'moveTo(${1:0}, ${2:0})', doc: '移动到坐标（不画线）' },
      { name: 'lineTo', insert: 'lineTo(${1:100}, ${2:0})', doc: '连线到坐标' },
      { name: 'text', insert: 'text("${1:文字}")', doc: '绘制文字' },
      { name: 'hide', insert: 'hide()', doc: '隐藏海龟' },
      { name: 'show', insert: 'show()', doc: '显示海龟' },
      { name: 'speed', insert: 'speed(${1:6})', doc: '设置速度(0-10)' },
      { name: 'wait', insert: 'wait(${1:500})', doc: '等待毫秒' },
      { name: 'sound', insert: 'sound(${1:500}, ${2:200})', doc: '播放声音' },
      { name: 'ctime', insert: 'ctime()', doc: '获取运行耗时(ms)' },
      { name: 'cinWin', insert: 'cinWin()', doc: '窗口输入' }
    ];

    monaco.languages.registerCompletionItemProvider('cpp', {
      provideCompletionItems: function (model, position) {
        var word = model.getWordUntilPosition(position);
        var range = {
          startLineNumber: position.lineNumber,
          endLineNumber: position.lineNumber,
          startColumn: word.startColumn,
          endColumn: word.endColumn
        };
        var suggestions = apiList.map(function (api) {
          return {
            label: api.name,
            kind: monaco.languages.CompletionItemKind.Function,
            insertText: api.insert,
            insertTextRules: monaco.languages.CompletionItemInsertTextRule.InsertAsSnippet,
            documentation: api.doc,
            detail: 'GoC 海龟绘图 API',
            range: range
          };
        });
        return { suggestions: suggestions };
      }
    });
  }

  /* ==================== 代码读写 ==================== */

  function getCode() {
    if (state.useMonaco && state.editor) return state.editor.getValue();
    return elements.codeTextarea ? elements.codeTextarea.value : '';
  }

  function setCode(code) {
    if (state.useMonaco && state.editor) {
      state.editor.setValue(code || '');
    } else if (elements.codeTextarea) {
      elements.codeTextarea.value = code || '';
    }
  }

  function decodeBase64Utf8(base64) {
    var binary = atob(base64);
    var bytes = new Uint8Array(binary.length);
    for (var i = 0; i < binary.length; i++) bytes[i] = binary.charCodeAt(i);
    return new TextDecoder('utf-8').decode(bytes);
  }

  function loadSavedProject() {
    try {
      var raw = localStorage.getItem('goc:project');
      if (!raw) return null;
      var obj = JSON.parse(raw);
      if (!obj || typeof obj.code !== 'string') return null;
      return obj;
    } catch (e) {
      return null;
    }
  }

  function getInitialCode() {
    var base64 = getQueryParam('codeBase64');
    if (base64) {
      try {
        return decodeBase64Utf8(base64);
      } catch (e) {
        appendOutput('URL 参数 codeBase64 解码失败', 'error');
      }
    }

    var saved = loadSavedProject();
    if (saved && saved.code) {
      if (saved.name && elements.projectName) elements.projectName.value = saved.name;
      return saved.code;
    }
    return DEFAULT_CODE;
  }

  /* ==================== 自动保存 ==================== */

  function updateSavedHint() {
    var d = new Date();
    state.lastSavedAt = two(d.getHours()) + ':' + two(d.getMinutes());
    if (elements.editorHint) {
      elements.editorHint.textContent = '已自动保存 ' + state.lastSavedAt;
    }
  }

  function saveProject() {
    if (!elements.autoSave || !elements.autoSave.checked) return;
    try {
      localStorage.setItem('goc:project', JSON.stringify({
        name: (elements.projectName.value || '未命名作品').trim(),
        code: getCode(),
        ts: Date.now()
      }));
      updateSavedHint();
    } catch (e) {
      // 存储配额不足等异常忽略
    }
  }

  function scheduleAutoSave() {
    if (!elements.autoSave || !elements.autoSave.checked) return;
    clearTimeout(state.autoSaveTimer);
    state.autoSaveTimer = setTimeout(saveProject, 1200);
  }

  /* ==================== WebWorker ==================== */

  function initWorker() {
    try {
      state.worker = new Worker('./worker.js');

      state.worker.onmessage = function (e) {
        var data = e.data;
        if (!data || !data.type) return;

        switch (data.type) {
          case 'ready':
            state.workerReady = true;
            appendOutput('执行引擎已就绪', 'info');
            break;
          case 'log':
            appendOutput(data.text, data.level === 'error' ? 'error' : '');
            break;
          case 'done':
            onExecutionDone(data);
            break;
          case 'error':
            onExecutionError(data);
            break;
        }
      };

      state.worker.onerror = function (err) {
        console.error('Worker error:', err);
        appendOutput('执行引擎错误：' + (err.message || '未知错误'), 'error');
        clearTimeout(state.timeoutId);
        setRunning(false);
        setStatus('error', '引擎错误');
      };
    } catch (err) {
      console.error('Worker 创建失败:', err);
      appendOutput('无法创建执行引擎，请使用现代浏览器', 'error');
    }
  }

  function runCode() {
    if (state.running) return;
    if (state.locked) {
      toast('作品已提交，编辑器已锁定');
      return;
    }
    if (!state.workerReady) {
      appendOutput('执行引擎尚未就绪，请稍候', 'warn');
      return;
    }

    var code = getCode();
    if (!code.trim()) {
      appendOutput('代码为空，请先编写代码', 'warn');
      return;
    }

    clearOutput();
    state.lastCommands = [];
    state.lastTurtle = { x: 0, y: 0, angle: 0, visible: true };
    repaint();

    setRunning(true);
    setStatus('running', '运行中...');
    appendOutput('开始编译执行...', 'info');

    state.worker.postMessage({
      type: 'run',
      code: code,
      input: elements.cinInput ? elements.cinInput.value : ''
    });

    clearTimeout(state.timeoutId);
    state.timeoutId = setTimeout(function () {
      if (!state.running) return;
      try { state.worker.terminate(); } catch (e) { /* ignore */ }
      appendOutput('执行超时（' + (state.timeoutMs / 1000) + ' 秒），已自动终止', 'error');
      setStatus('error', '执行超时');
      setRunning(false);
      initWorker();
    }, state.timeoutMs);
  }

  function onExecutionDone(data) {
    clearTimeout(state.timeoutId);
    setRunning(false);

    state.lastCommands = data.commands || [];
    state.lastTurtle = data.turtle || { x: 0, y: 0, angle: 0, visible: true };
    repaint();

    appendOutput('执行完成 · ' + (data.count || 0) + ' 条绘图指令 · ' + (data.duration || 0) + 'ms', 'success');
    setStatus('success', '执行完成');

    notifyParent({
      type: 'goc:runResult',
      success: true,
      output: '执行完成，共 ' + (data.count || 0) + ' 条命令',
      commandCount: data.count,
      duration: data.duration
    });
  }

  // GoC 运行/编译错误中文化：将转译器英文错误转为通俗中文
  function translateGocError(raw) {
    if (!raw) return '运行错误';
    var text = String(raw);
    var rules = [
      [/expected|语法错误|syntax/i, '语法错误：代码的写法不符合 C++/GoC 规则（检查分号、括号是否配对完整）'],
      [/未定义|undefined|not declared|undeclared/i, '命令/变量未定义：使用了未识别的 GoC 命令（请检查 fd/rt/pen/ct 等拼写）'],
      [/division by zero|除以零/i, '除零错误：不能用 0 做除数'],
      [/timeout|超时|死循环/i, '运行超时：程序可能陷入了死循环（检查循环是否一定能结束）'],
      [/cin|输入/i, '输入错误：运行需要输入数据时，请先在下方「输入」区填写（多个值用空格分隔）']
    ];
    for (var i = 0; i < rules.length; i++) {
      if (rules[i][0].test(text)) return rules[i][1];
    }
    var first = text.split('\n').filter(function (l) { return l.trim(); })[0] || text;
    return first.length > 200 ? first.slice(0, 200) + '…' : first;
  }

  function onExecutionError(data) {
    clearTimeout(state.timeoutId);
    setRunning(false);

    if (data.partialCommands && data.partialCommands.length) {
      state.lastCommands = data.partialCommands;
      repaint();
    }

    var msg = translateGocError(data.error);
    appendOutput(msg, 'error');
    setStatus('error', data.phase === 'compile' ? '编译错误' : '运行错误');

    notifyParent({
      type: 'goc:runResult',
      success: false,
      errorMessage: msg,
      phase: data.phase
    });
  }

  /* ==================== 工具栏动作 ==================== */

  function setupButtons() {
    elements.btnRun.addEventListener('click', runCode);

    elements.btnClearCanvas.addEventListener('click', function () {
      state.lastCommands = [];
      state.lastTurtle = { x: 0, y: 0, angle: 0, visible: true };
      repaint();
      appendOutput('画布已清空', 'info');
    });

    elements.btnResetTurtle.addEventListener('click', function () {
      state.lastCommands = [];
      state.lastTurtle = { x: 0, y: 0, angle: 0, visible: true };
      repaint();
      appendOutput('海龟已重置到原点（角度 0°）', 'info');
    });

    elements.btnSubmit.addEventListener('click', submitWork);

    elements.btnClearConsole.addEventListener('click', function () {
      clearOutput();
    });

    elements.btnCopy.addEventListener('click', copyCode);
    elements.btnDownload.addEventListener('click', downloadCode);

    elements.btnGotoCode.addEventListener('click', function () {
      if (elements.rightPanel && elements.rightPanel.scrollIntoView) {
        elements.rightPanel.scrollIntoView({ behavior: 'smooth', block: 'nearest' });
      }
      if (state.useMonaco && state.editor) state.editor.focus();
      else if (elements.codeTextarea) elements.codeTextarea.focus();
    });

    elements.projectName.addEventListener('input', scheduleAutoSave);

    elements.autoSave.addEventListener('change', function () {
      if (elements.autoSave.checked) {
        saveProject();
        toast('已开启自动保存');
      } else {
        try { localStorage.removeItem('goc:project'); } catch (e) { /* ignore */ }
        if (elements.editorHint) elements.editorHint.textContent = 'C++ 代码';
        toast('已关闭自动保存');
      }
    });
  }

  function copyCode() {
    var code = getCode();

    function done() { toast('代码已复制到剪贴板'); }

    function fallback() {
      var ta = document.createElement('textarea');
      ta.value = code;
      ta.style.position = 'fixed';
      ta.style.left = '-9999px';
      document.body.appendChild(ta);
      ta.select();
      try {
        document.execCommand('copy');
        done();
      } catch (e) {
        toast('复制失败，请手动选择代码复制');
      }
      document.body.removeChild(ta);
    }

    if (navigator.clipboard && navigator.clipboard.writeText) {
      navigator.clipboard.writeText(code).then(done).catch(fallback);
    } else {
      fallback();
    }
  }

  function downloadCode() {
    var name = (elements.projectName.value || '').trim() || 'goc-project';
    var code = getCode();
    var blob = new Blob([code], { type: 'text/plain;charset=utf-8' });
    var url = URL.createObjectURL(blob);

    var a = document.createElement('a');
    a.href = url;
    a.download = name + '.cpp';
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);

    setTimeout(function () { URL.revokeObjectURL(url); }, 1500);
    toast('已下载 ' + name + '.cpp');
  }

  function submitWork() {
    if (state.locked) {
      appendOutput('作品已提交，无法再次提交', 'warn');
      return;
    }

    var code = getCode();
    if (!code.trim()) {
      appendOutput('代码为空，无法提交', 'warn');
      return;
    }

    appendOutput('正在提交作品...', 'info');

    var userId = getQueryParam('userId') || '';
    var contestId = getQueryParam('contestId') || '';

    fetch('/api/contest/submit_goc', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ userId: userId, contestId: contestId, code: code })
    })
      .then(function (res) {
        if (!res.ok) throw new Error('HTTP ' + res.status);
        return res.json();
      })
      .then(function (data) {
        appendOutput('作品提交成功！', 'success');
        setStatus('success', '提交成功');
        notifyParent({ type: 'goc:submitResult', success: true, data: data });
      })
      .catch(function (err) {
        appendOutput('提交失败：' + err.message, 'error');
        notifyParent({
          type: 'goc:submitResult',
          success: false,
          error: err.message,
          code: code
        });
      });
  }

  /* ==================== 父窗口通信 ==================== */

  function notifyParent(message) {
    if (window.parent && window.parent !== window) {
      window.parent.postMessage(message, '*');
    }
  }

  function notifyParentCodeChange() {
    notifyParent({ type: 'goc:codeChange', code: getCode() });
  }

  function lockEditor() {
    state.locked = true;
    lockPalette(true);
    if (state.useMonaco && state.editor) {
      state.editor.updateOptions({ readOnly: true });
    } else if (elements.codeTextarea) {
      elements.codeTextarea.readOnly = true;
    }
    appendOutput('编辑器已锁定', 'warn');
  }

  function setupParentCommunication() {
    window.addEventListener('message', function (event) {
      var data = event.data;
      if (!data || !data.type) return;

      switch (data.type) {
        case 'goc:loadCode':
          if (data.code !== undefined) setCode(data.code);
          break;
        case 'goc:run':
          runCode();
          break;
        case 'goc:clearCanvas':
          state.lastCommands = [];
          state.lastTurtle = { x: 0, y: 0, angle: 0, visible: true };
          repaint();
          break;
        case 'goc:resetTurtle':
          state.lastCommands = [];
          state.lastTurtle = { x: 0, y: 0, angle: 0, visible: true };
          repaint();
          break;
        case 'goc:getCode':
          notifyParent({ type: 'goc:codeData', code: getCode() });
          break;
        case 'goc:submit':
          submitWork();
          break;
        case 'goc:lock':
          lockEditor();
          break;
      }
    });
  }

  /* ==================== 初始化 ==================== */

  function hideLoading() {
    if (elements.loadingOverlay) {
      elements.loadingOverlay.style.display = 'none';
    }
  }

  function onEditorReady() {
    notifyParent({ type: 'goc:ready' });

    var base64 = getQueryParam('codeBase64');
    if (base64) {
      appendOutput('检测到评委预览代码，自动运行...', 'info');
      var tick = setInterval(function () {
        if (state.workerReady) {
          clearInterval(tick);
          setTimeout(runCode, 300);
        }
      }, 100);
      setTimeout(function () { clearInterval(tick); }, 10000);
    }
  }

  var resizeTimer = null;

  function init() {
    buildPalette();
    setupPaletteEvents();
    setupButtons();
    setupParentCommunication();

    initCanvas();

    window.addEventListener('resize', function () {
      clearTimeout(resizeTimer);
      resizeTimer = setTimeout(initCanvas, 120);
    });

    // 执行引擎与编辑器相互独立，先启动引擎
    initWorker();

    initMonaco();

    // Monaco 加载超时回退（5 秒）
    setTimeout(function () {
      if (!state.useMonaco && elements.loadingOverlay &&
          elements.loadingOverlay.style.display !== 'none') {
        fallbackToTextarea();
      }
    }, 5000);
  }

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', init);
  } else {
    init();
  }
})();
