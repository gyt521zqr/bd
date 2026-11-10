/**
 * errorTranslator.ts
 * 将浏览器内运行/编译产生的英文错误（Python traceback、C++ 模拟器、
 * GoC 转译器）翻译为通俗中文，并尝试从错误信息中提取出错行号，
 * 供输出面板渲染「跳转到第 N 行」的快捷入口。
 *
 * 纯前端工具，不依赖任何编辑器实现；各子编辑器在 runCode 返回
 * errorMessage 前调用 translateError 即可。
 */

export interface TranslatedError {
  /** 翻译后的中文错误信息 */
  message: string
  /** 从错误信息中提取的出错行号（1-based），无则为 undefined */
  line?: number
  /** 给初学者的修复建议（可选） */
  hint?: string
}

/** Python 常见异常 → 中文说明 + 建议 */
const PYTHON_ERRORS: { re: RegExp; message: string; hint?: string }[] = [
  { re: /SyntaxError/i, message: '语法错误：代码的写法不符合 Python 规则', hint: '检查括号、冒号、缩进是否配对完整' },
  { re: /IndentationError|unexpected indent|expected an indented block/i, message: '缩进错误：代码块的缩进（空格/Tab）不一致', hint: '同一代码块必须用相同数量的空格缩进，建议全部用 4 个空格' },
  { re: /NameError: name '(.+?)' is not defined/i, message: '变量未定义：$1 还没有被赋值就使用了', hint: '请先给该变量赋值，或检查拼写是否正确' },
  { re: /TypeError: (.+?)\n/i, message: '类型错误：$1', hint: '检查运算的数据类型是否匹配，例如数字与字符串不能直接相加' },
  { re: /TypeError: (.+)/i, message: '类型错误：$1', hint: '检查运算的数据类型是否匹配' },
  { re: /ZeroDivisionError/i, message: '除零错误：不能用 0 做除数', hint: '在除法前判断分母是否等于 0' },
  { re: /IndexError: list index out of range/i, message: '索引越界：访问了列表不存在的位置', hint: '列表下标从 0 开始，检查循环范围是否超过列表长度' },
  { re: /KeyError: '(.+?)'/i, message: '键不存在：字典里没有 $1 这个键', hint: '先用 in 判断键是否存在，或使用 get() 方法' },
  { re: /ValueError: (.+?)\n/i, message: '取值错误：$1', hint: '检查输入转换（如 int()）时的数据是否合法' },
  { re: /ValueError: (.+)/i, message: '取值错误：$1' },
  { re: /ModuleNotFoundError: No module named '(.+?)'/i, message: '缺少模块：找不到 $1 这个库', hint: '浏览器内环境不支持第三方库，请改用内置功能实现' },
  { re: /ImportError: (.+?)\n/i, message: '导入错误：$1' },
  { re: /FileNotFoundError/i, message: '文件不存在：打开的文件路径找不到', hint: '浏览器内无法访问本地文件，请改用输入/变量代替' },
  { re: /RecursionError/i, message: '递归层数过多：函数自己调用自己太深了', hint: '检查递归是否缺少结束条件，避免无限递归' },
  { re: /MemoryError/i, message: '内存不足：程序占用的内存太大', hint: '检查是否生成了过多数据或陷入死循环' },
  { re: /TimeoutError|运行超时|死循环/i, message: '运行超时：程序可能陷入了死循环', hint: '检查 while / for 循环是否一定能结束' },
  { re: /KeyboardInterrupt/i, message: '已手动停止运行' },
]

/** C++ 模拟器常见错误（错误信息格式取决于转译器，做宽松匹配） */
const CPP_ERRORS: { re: RegExp; message: string; hint?: string }[] = [
  { re: /division by zero|除以零/i, message: '除零错误：不能用 0 做除数', hint: '在除法前判断分母是否等于 0' },
  { re: /未定义|undefined|not declared|was not declared|undeclared/i, message: '变量/函数未声明：使用了未定义的标识符', hint: '请先 #include 对应头文件并用变量类型声明后再使用' },
  { re: /expected|语法错误|syntax error|missing ['";]/i, message: '语法错误：代码的写法不符合 C++ 规则', hint: '检查分号、括号、尖括号是否配对完整' },
  { re: /no matching|类型不匹配|cannot convert|type mismatch/i, message: '类型错误：数据类型不匹配', hint: '检查变量类型与运算/赋值是否一致' },
  { re: /out of range|越界|index out of bounds/i, message: '下标越界：访问了数组/容器不存在的位置', hint: '数组下标从 0 开始，检查循环范围是否超过长度' },
  { re: /timeout|超时|死循环|time limit/i, message: '运行超时：程序可能陷入了死循环', hint: '检查 while / for 循环是否一定能结束' },
  { re: /stack overflow/i, message: '栈溢出：递归太深或函数无限调用', hint: '检查递归是否缺少结束条件' },
]

/** GoC 运行/编译错误（worker 返回的错误文案） */
const GOC_ERRORS: { re: RegExp; message: string; hint?: string }[] = [
  { re: /expected|语法错误|syntax/i, message: '语法错误：代码的写法不符合 C++/GoC 规则', hint: '检查分号、括号是否配对完整' },
  { re: /未定义|undefined|not declared|undeclared/i, message: '命令/变量未定义：使用了未识别的 GoC 命令', hint: '确认命令拼写，例如 fd/rt/pen/ct 等是否拼写正确' },
  { re: /division by zero|除以零/i, message: '除零错误：不能用 0 做除数' },
  { re: /timeout|超时|死循环/i, message: '运行超时：程序可能陷入了死循环', hint: '检查循环是否一定能结束' },
  { re: /cin|输入/i, message: '输入错误：运行需要输入数据时，请先在下方 cinWin 输入区填写', hint: '在底部「输入」框中按要求填写数据，多个值用空格分隔' },
]

/** 行号提取正则（匹配 line 5 / 第5行 / :5: / "line": 5 等） */
const LINE_PATTERNS: RegExp[] = [
  /line\s+(\d+)\s*:/i,
  /line\s+(\d+)/i,
  /第\s*(\d+)\s*行/i,
  /[:"']?line["']?\s*[:=]\s*(\d+)/i,
  /<stdin>:(\d+):/i,
  /line\s+(\d+),/i,
]

/** 从错误信息中尝试提取行号 */
export function extractErrorLine(raw: string): number | undefined {
  for (const re of LINE_PATTERNS) {
    const m = raw.match(re)
    if (m) {
      const n = parseInt(m[1], 10)
      if (n > 0 && n < 100000) return n
    }
  }
  return undefined
}

/**
 * 翻译运行时错误为中文并提取行号。
 * @param raw 原始错误信息（可能含英文 traceback）
 * @param lang 语言标识：python / pythonturtle / cpp / goc
 */
export function translateError(raw: string, lang: string): TranslatedError {
  const text = (raw || '').trim()
  if (!text) return { message: '运行错误' }

  const line = extractErrorLine(text)

  // 先按语言匹配具体规则
  const table =
    lang === 'cpp' ? CPP_ERRORS :
    lang === 'goc' ? GOC_ERRORS :
    PYTHON_ERRORS

  for (const rule of table) {
    const m = text.match(rule.re)
    if (m) {
      return {
        message: rule.message,
        line,
        hint: rule.hint,
      }
    }
  }

  // 通用兜底：保留原文第一行，避免信息丢失
  const firstLine = text.split('\n').find(l => l.trim()) || text
  return {
    message: firstLine.length > 200 ? firstLine.slice(0, 200) + '…' : firstLine,
    line,
  }
}
