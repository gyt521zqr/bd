/**
 * 统一考试题源（前端 Mock）
 * 供「赛事详情页」试卷预览 与「做题页」共用，按详情页考试 id 组织。
 * 题目类型：choice 选择题 / fill 填空题 / judge 判断题 / code 编程题 / scratch Scratch题 / scratchjr ScratchJr题
 * 代码题/Scratch题 通过 language 字段在 BaseEditor 中调用对应编辑器。
 *
 * 题库来源（真实真题，用于测试）：
 * - Python：青少年软件编程等级考试 Python 一级真题（乐乐题库）
 * - C++：青少年软件编程等级考试 C/C++ 一级真题
 * - Scratch：青少年软件编程等级考试 Scratch 一级真题（乐乐题库）
 * - ScratchJr：ScratchJr 基础测评题
 * - Python Turtle：Python 海龟绘图阶段测试题
 * - GoC：GoC 图形编程一级题目
 */
import type { EditorLang } from '@/types/editor'

export type QuestionType =
  | 'choice'
  | 'fill'
  | 'judge'
  | 'code'
  | 'scratch'
  | 'scratchjr'

export interface ExamQuestion {
  id: number
  type: QuestionType
  title: string
  options?: string[]
  /** 正确答案（选择题 A/B/C/D；判断题 true/false；填空题答案文本） */
  answer?: string
  /** 试题解析 */
  analysis?: string
  score: number
  language?: EditorLang
  templateCode?: string
}

/** 题型中文标签 */
export const QUESTION_TYPE_LABEL: Record<QuestionType, string> = {
  choice: '选择题',
  fill: '填空题',
  judge: '判断题',
  code: '编程题',
  scratch: 'Scratch题',
  scratchjr: 'ScratchJr题',
}

/** 题型展示顺序（用于分节） */
export const QUESTION_TYPE_ORDER: QuestionType[] = [
  'choice',
  'fill',
  'judge',
  'code',
  'scratch',
  'scratchjr',
]

// ============================================================================
// Python 题集（青少年软件编程等级考试 Python 一级真题）
// ============================================================================
const pythonSet: ExamQuestion[] = [
  { id: 1031, type: 'choice', title: '下列哪个软件不能运行 Python 程序？（　）', options: ['Jupyter Notebook', 'Pycharm', '原版的 Scratch', 'IDLE'], answer: 'C', analysis: '原版 Scratch 是图形化编程软件，不是 Python 编程软件。', score: 5 },
  { id: 1032, type: 'choice', title: '下面 print 语句，哪一个是正确的用法？（　）', options: ['print 龙腾虎跃', 'print(龙腾虎跃)', "print('龙腾虎跃')", "print'龙腾虎跃'"], answer: 'C', analysis: 'print 是函数，必须带括号；字符串内容需要用引号包裹。', score: 5 },
  { id: 1033, type: 'choice', title: '在 Python 中，下列哪一条语句可以改变画笔当前位置？（　）', options: ['dot(100)', 'setheading(100)', 'left(100)', 'goto(100,0)'], answer: 'D', analysis: 'dot 画实心圆、setheading 设置方向、left 左转都不改变位置；goto(100,0) 让画笔移动到坐标 (100,0)。', score: 5 },
  { id: 1034, type: 'choice', title: '下列哪一个可以进行数学中的求余数运算？（　）', options: ['**', '%', '/', '//'], answer: 'B', analysis: '** 是幂运算，% 是取余，/ 是除法，// 是整除。', score: 5 },
  { id: 1035, type: 'choice', title: '表达式 15>=8 的运算结果是？（　）', options: ['15', '8', 'False', 'True'], answer: 'D', analysis: '15>=8 成立，比较运算结果为布尔值 True。', score: 5 },
  { id: 1036, type: 'choice', title: '执行语句 print(10==10.0) 的结果为？（　）', options: ['10', '10.0', 'True', 'False'], answer: 'C', analysis: '10 与 10.0 数值相等，比较运算结果为 True。', score: 5 },
  { id: 1037, type: 'choice', title: '执行语句 y=4**3 后，变量 y 的值为？（　）', options: ['0', '12', '64', '81'], answer: 'C', analysis: '** 是幂运算，4**3 = 4×4×4 = 64。', score: 5 },
  { id: 1038, type: 'choice', title: 'Python 编程语言的注释语句以（　）开头？', options: ["'", '//', '#', '{'], answer: 'C', analysis: 'Python 单行注释以 # 开头。// 是 C++/Java 的注释。', score: 5 },
  { id: 1039, type: 'choice', title: '执行 print(12.34-1.34) 的输出结果是？（　）', options: ['11', '11.0', '11.00', '12.34-1.34'], answer: 'B', analysis: '12.34-1.34 = 11.0，浮点数运算结果为 11.0。', score: 5 },
  { id: 1040, type: 'choice', title: '已知变量 a=2，b=3，执行语句 a%=a+b 后，变量 a 的值为？（　）', options: ['0', '2', '3', '12'], answer: 'B', analysis: 'a%=a+b 即 a = a % (a+b) = 2 % 5 = 2。', score: 5 },
  { id: 1041, type: 'judge', title: '在 Python 中，缩进用来表示代码块的层级关系，每增加一个层块就增加一个缩进。（　）', answer: 'true', analysis: 'Python 依赖缩进划分代码块，缩进不正确会导致语法错误。', score: 5 },
  {
    id: 1042, type: 'code',
    title: '编程题：编写一个函数 `is_palindrome(s)`，判断字符串是否为回文串\n\n回文串是指正读和反读都相同的字符串，如 "abcba"、"12321"。\n\n要求：\n- 函数接收一个字符串参数 s\n- 返回 True 如果是回文，否则返回 False\n- 不区分大小写\n\n示例：\n- is_palindrome("abcba") -> True\n- is_palindrome("Hello") -> False\n- is_palindrome("12321") -> True',
    language: 'python', score: 30,
    templateCode: 'def is_palindrome(s):\n    """判断字符串是否为回文串"""\n    # 请在下方编写你的代码\n    \n',
  },
]

// ============================================================================
// C++ 题集（青少年软件编程等级考试 C/C++ 一级真题）
// ============================================================================
const cppSet: ExamQuestion[] = [
  { id: 1051, type: 'choice', title: '一个完整的 C++ 程序，必须包含以下哪个函数作为程序执行的入口？（　）', options: ['main()', '开始()', 'begin()', 'init()'], answer: 'A', analysis: 'C++ 程序的执行入口由标准规定为 main() 函数。', score: 5 },
  { id: 1052, type: 'choice', title: 'C++ 源代码经过以下哪个步骤后，会生成可直接在 Windows 系统运行的 .exe 文件？（　）', options: ['仅编写代码', '仅编译', '编译+链接', '仅调试'], answer: 'C', analysis: '源代码需经过编译（生成目标文件）和链接（生成可执行文件）两步。', score: 5 },
  { id: 1053, type: 'choice', title: '一个 C++ 源程序文件，以下哪个后缀名是标准合法的？（　）', options: ['.txt', '.cpp', '.doc', '.exe'], answer: 'B', analysis: 'C++ 源文件通常使用 .cpp 扩展名；.exe 是可执行文件。', score: 5 },
  { id: 1054, type: 'choice', title: 'C++ 表达式 15 - 6 / 2 * 3 的计算结果是？（　）', options: ['6', '9', '14', '0'], answer: 'A', analysis: '按优先级：6/2=3，3*3=9，15-9=6。', score: 5 },
  { id: 1055, type: 'choice', title: 'C++ 表达式 8 + 3 * 5 % 2 的计算结果是？（　）', options: ['9', '13', '55', '0'], answer: 'A', analysis: '按优先级：3*5=15，15%2=1，8+1=9。', score: 5 },
  { id: 1056, type: 'choice', title: '以下 C++ 变量命名中，合法的一项是？（　）', options: ['123num', 'my-age', 'int', '_score_2026'], answer: 'D', analysis: '变量不能以数字开头、不能含特殊符号、不能是关键字；可含字母数字下划线。', score: 5 },
  { id: 1057, type: 'choice', title: '以下哪个软件不能用于 C++ 程序的编写与编译运行？（　）', options: ['Dev-C++', 'Visual Studio', '微软 Word', 'Code::Blocks'], answer: 'C', analysis: 'Word 是文字处理软件，不能编译运行 C++ 程序。', score: 5 },
  { id: 1058, type: 'judge', title: 'do-while 循环的循环体至少会执行一次，无论循环条件是否成立。（　）', answer: 'true', analysis: 'do-while 先执行循环体再判断条件，所以至少执行一次。', score: 5 },
  { id: 1059, type: 'judge', title: 'while 循环的循环体至少会执行一次。（　）', answer: 'false', analysis: 'while 先判断条件再执行循环体，条件一开始不成立则一次都不执行。', score: 5 },
  { id: 1060, type: 'judge', title: '表达式 !(5 > 3) 的逻辑结果为 true。（　）', answer: 'false', analysis: '5>3 为 true，取反 ! 后结果为 false。', score: 5 },
  { id: 1061, type: 'judge', title: 'for(int i=0;;i++) 是一个无限循环。（　）', answer: 'true', analysis: 'for 循环缺少中间的条件判断表达式，条件恒为真，构成无限循环。', score: 5 },
  {
    id: 1062, type: 'code',
    title: '编程题：编写一个 C++ 程序，实现冒泡排序\n\n要求：\n- 从键盘输入 N 个整数（N <= 100）\n- 使用冒泡排序算法将它们从小到大排序\n- 输出排序后的结果\n\n示例输入：\n5\n3 1 4 1 5\n\n示例输出：\n1 1 3 4 5',
    language: 'cpp', score: 30,
    templateCode: '#include <iostream>\nusing namespace std;\n\nint main() {\n    int n;\n    cin >> n;\n    \n    int arr[100];\n    for (int i = 0; i < n; i++) {\n        cin >> arr[i];\n    }\n    \n    // 请在下方编写冒泡排序代码\n    \n    // 输出排序结果\n    for (int i = 0; i < n; i++) {\n        cout << arr[i];\n        if (i < n - 1) cout << " ";\n    }\n    cout << endl;\n    \n    return 0;\n}\n',
  },
]

// ============================================================================
// Scratch 题集（青少年软件编程等级考试 Scratch 一级真题）
// ============================================================================
const scratchSet: ExamQuestion[] = [
  { id: 1021, type: 'choice', title: '点击下列哪个选项可以运行当前编写的程序？（　）', options: ['停止按钮', '绿旗按钮', '切换舞台全屏按钮', '保存项目按钮'], answer: 'B', analysis: 'Scratch 中绿旗是程序运行的核心触发按钮；停止按钮停止程序，全屏按钮切换显示，保存按钮保存项目。', score: 5 },
  { id: 1022, type: 'choice', title: '想让苹果在舞台上看起来比现在大一倍，应该将"大小"数值修改为多少？（　）', options: ['50', '200', '10', '150'], answer: 'B', analysis: '角色"大小"原始尺寸是 100，大一倍即 2 倍 = 200。', score: 5 },
  { id: 1023, type: 'choice', title: '默认小猫角色，初始方向为水平向右，运行"移动 200 步，再移动 -100 步"后，说法正确的是？（　）', options: ['小猫先面朝右移动，再面朝左移动', '小猫面朝右前进，再面朝右后退', '小猫先向左移动，再向右移动', '小猫一共向右移动 200 步'], answer: 'B', analysis: '面向 90 度即面朝右，移动 200 步向右前进，移动 -100 步表示后退 100 步，面向不变。', score: 5 },
  { id: 1024, type: 'choice', title: '观察数字序列：1, 1, 2, 3, 5, 8, ?，问号处应填？（　）', options: ['11', '12', '13', '14'], answer: 'C', analysis: '规律是前两个数相加等于第三个数：5+8=13。', score: 5 },
  { id: 1025, type: 'choice', title: '做早操时小朋友们在排队，从前面数小红是第 4 个，从后面数小红是第 3 个，队伍里一共有多少个小朋友？（　）', options: ['5', '6', '7', '8'], answer: 'B', analysis: '从前面数小红第 4、从后面数第 3，小红被算了两次，总人数 4+3-1=6。', score: 5 },
  { id: 1026, type: 'choice', title: '当执行到"停止所有声音"积木时，会发生什么？（　）', options: ['程序结束', '角色声音和背景音乐的音量变为 0', '角色声音和背景音乐全部停止播放', '程序暂停'], answer: 'C', analysis: '该积木立即停止所有正在播放的声音，但不影响其他代码执行，也不改变音量。', score: 5 },
  { id: 1027, type: 'choice', title: '小鸭子的初始方向是面向 90 度，将"方向"数值修改为多少，小鸭子能面向正下方？（　）', options: ['0', '180', '270', '-90'], answer: 'B', analysis: '0° 正上、90° 正右、180° 正下、-90°/270° 正左。', score: 5 },
  { id: 1028, type: 'judge', title: '隐藏角色以后，程序就不再执行。（　）', answer: 'false', analysis: '隐藏角色只会隐藏它的外观，不会停止执行角色的代码。', score: 5 },
  { id: 1029, type: 'judge', title: '将角色的旋转模式设置为左右翻转后，执行"左转 90 度"积木，角色会出现倒立的情况。（　）', answer: 'false', analysis: '左右翻转模式只会水平方向左右翻转，不会上下倒立；只有自由旋转模式才会倒立。', score: 5 },
  {
    id: 1030, type: 'scratch',
    title: '编程挑战：制作一个"接苹果"小游戏\n\n要求：\n1. 创建一个篮子角色（用角色绘制）\n2. 苹果从舞台顶部随机位置掉落\n3. 用键盘左右方向键控制篮子移动\n4. 篮子接到苹果时得分+1，并显示在舞台上\n5. 苹果掉到底部后消失，新苹果从顶部掉落\n6. 游戏时间60秒，倒计时结束后显示最终得分\n\n提示：使用"x坐标""y坐标""碰到边缘""变量""随机数"等积木',
    language: 'scratch3', score: 30,
  },
]

// ============================================================================
// GoC 题集（GoC 图形编程一级题目）
// ============================================================================
const gocSet: ExamQuestion[] = [
  { id: 401, type: 'choice', title: 'GoC 中让笔后退 90 的命令是？（　）', options: ['rt(90)', 'lt(90)', 'fd(90)', 'bk(90)'], answer: 'D', analysis: 'bk 是后退命令，bk(90) 表示后退 90。', score: 5 },
  { id: 402, type: 'choice', title: '画笔最初的方向为竖直向上，想绘制一条长度为 100 的水平线，正确代码是？（　）', options: ['p.lt(90).fd(100)', 'p.rt(90).fd(100)', 'p.lt(180).fd(100)', 'p.rt(180).fd(100)'], answer: 'B', analysis: '初始向上，右转 90 度后朝右，再前进 100 画水平线。', score: 5 },
  { id: 403, type: 'choice', title: '关于 GoC 命令的描述，正确的是？（　）', options: ['fd 是前进命令', 'bk 是前进命令', 'rt 是左转命令', 'pen 是颜色命令'], answer: 'A', analysis: 'fd 前进、bk 后退、rt 右转、lt 左转。', score: 5 },
  { id: 404, type: 'choice', title: '语句 p.rt(90) 与以下哪个语句执行结果一样？（　）', options: ['p.lt(270)', 'p.rt(180)', 'p.rt(270)', 'p.lt(90)'], answer: 'A', analysis: '右转 90 度等价于左转 270 度（360-90=270）。', score: 5 },
  { id: 405, type: 'choice', title: '下列是抬笔命令的是？（　）', options: ['p.up()', 'p.down()', 'p.lt()', 'p.rt()'], answer: 'A', analysis: 'up() 是抬笔，down() 是落笔。', score: 5 },
  { id: 406, type: 'choice', title: '想让画笔后退 100 步，下列选项正确的是？（　）', options: ['p.fd(100)', 'p.bk(100)', 'p.rt(100)', 'p.lt(100)'], answer: 'B', analysis: 'bk(100) 表示后退 100 步。', score: 5 },
  { id: 407, type: 'choice', title: 'rr() 是画实心矩形命令，画边长 100 的红色（颜色号 1）正方形，正确代码是？（　）', options: ['p.rr(100,1)', 'p.rr(100,100)', 'p.rr(1,100,100)', 'p.rr(100,100,1)'], answer: 'D', analysis: 'rr(宽,高,颜色号)，正方形宽高都是 100，颜色号 1。', score: 5 },
  { id: 408, type: 'choice', title: 'GoC 中让笔左转 90° 的命令是？（　）', options: ['p.rt(90)', 'p.lt(90)', 'p.bk(90)', 'p.fd(90)'], answer: 'B', analysis: 'lt 是左转命令，lt(90) 左转 90 度。', score: 5 },
  { id: 409, type: 'choice', title: 'for 循环的正确格式是？（　）', options: ['for(int i=0;i<;i++)', 'for(int i=0;i<4;i++)', 'for(int i=;i<=4;i++)', 'for(int i=0;i<4;i+)'], answer: 'B', analysis: 'for 循环三部分：初始化、条件、步进，均需完整。', score: 5 },
  { id: 410, type: 'judge', title: '正四边形每条边长度相等，每次转角 90°，4 次转角角度之和是 360°。（　）', answer: 'true', analysis: '正方形每次转 90°，共 4 次，合计 360°。', score: 5 },
  {
    id: 411, type: 'code',
    title: '编程题：使用 GoC 画一个等边三角形\n\n要求：\n- 边长 100\n- 线条为红色\n- 画完后停留\n\n提示：\n- 等边三角形每个内角 60°，外角 120°\n- 使用 fd() 前进、rt() 右转、pen.color() 设置颜色',
    language: 'goc', score: 30,
    templateCode: '#include <graphics.h>\nint main() {\n    initGraph(800, 600);\n    pen.color(255, 0, 0);\n    // 请在下方编写画等边三角形的代码\n    \n    closeGraph();\n    return 0;\n}\n',
  },
]

// ============================================================================
// Python Turtle 题集（Python 海龟绘图阶段测试题）
// ============================================================================
const turtleSet: ExamQuestion[] = [
  { id: 1061, type: 'choice', title: '要使用 Python 的海龟画图功能，首先需要导入的模块是？（　）', options: ['turtle', 'pygame', 'math', 'random'], answer: 'A', analysis: '海龟绘图使用内置的 turtle 模块，导入语句为 import turtle。', score: 5 },
  { id: 1062, type: 'choice', title: '执行 turtle.forward(100) 命令后，海龟会？（　）', options: ['向后移动 100 像素', '向前移动 100 像素', '向左转 100 度', '向右转 100 度'], answer: 'B', analysis: 'forward(100) 让海龟沿当前朝向向前移动 100 像素。', score: 5 },
  { id: 1063, type: 'choice', title: '命令 turtle.left(90) 的作用是？（　）', options: ['海龟顺时针旋转 90 度', '海龟逆时针旋转 90 度', '海龟移动 90 像素', '画笔抬起'], answer: 'B', analysis: 'left(90) 让海龟逆时针（向左）旋转 90 度。', score: 5 },
  { id: 1064, type: 'choice', title: '要让海龟的画笔抬起、停止绘制线条，应该使用的命令是？（　）', options: ['turtle.pendown()', 'turtle.penup()', 'turtle.pensize()', 'turtle.color()'], answer: 'B', analysis: 'penup() 抬起画笔（移动不画线），pendown() 落笔。', score: 5 },
  { id: 1065, type: 'choice', title: '以下哪个命令可以设置画笔的颜色为红色？（　）', options: ['turtle.pensize("red")', 'turtle.color("red")', 'turtle.bgcolor("red")', 'turtle.fillcolor("red")'], answer: 'B', analysis: 'color("red") 设置画笔颜色；bgcolor 设置背景色，fillcolor 设置填充色。', score: 5 },
  { id: 1066, type: 'choice', title: '执行 turtle.circle(50) 命令，会绘制一个？（　）', options: ['半径为 50 像素的圆', '直径为 50 像素的圆', '边长为 50 的正方形', '边长为 50 的三角形'], answer: 'A', analysis: 'circle(50) 的参数是半径，绘制半径为 50 的圆。', score: 5 },
  { id: 1067, type: 'choice', title: '要设置海龟画图窗口的背景颜色为蓝色，使用的命令是？（　）', options: ['turtle.color("blue")', 'turtle.bgcolor("blue")', 'turtle.fillcolor("blue")', 'turtle.pencolor("blue")'], answer: 'B', analysis: 'bgcolor("blue") 设置窗口背景色为蓝色。', score: 5 },
  { id: 1068, type: 'choice', title: '命令 turtle.goto(100, 200) 的作用是？（　）', options: ['海龟旋转到 (100,200) 方向', '海龟移动到坐标 (100,200) 的位置', '海龟绘制一条曲线', '以上都不对'], answer: 'B', analysis: 'goto(x,y) 让海龟移动到指定坐标位置。', score: 5 },
  { id: 1069, type: 'choice', title: 'Turtle 库中，画笔绘制速度的范围为？（　）', options: ['任意大小', '0 到 10 之间的整数（含 0 和 10）', '1 到 10 之间的整数', '0 到 100 之间的整数'], answer: 'B', analysis: 'Turtle 速度取值为 0 到 10 的整数，0 表示最快。', score: 5 },
  { id: 1070, type: 'judge', title: 'turtle.backward(100) 会让海龟转向相反方向，再向前移动 100 像素。（　）', answer: 'false', analysis: 'backward(100) 是让海龟沿当前朝向的反方向后退 100，不会改变海龟的朝向。', score: 5 },
  {
    id: 1071, type: 'code',
    title: '绘图挑战：使用 Python Turtle 绘制一个彩色正方形螺旋图案\n\n要求：\n1. 绘制一个由多个正方形组成的螺旋图案\n2. 每个正方形比前一个稍大\n3. 每个正方形使用不同的颜色\n4. 至少绘制 10 个正方形\n\n提示：\n- 使用 for 循环控制正方形数量\n- 每次循环增加边长\n- 使用 turtle.pencolor() 切换颜色',
    language: 'python', score: 30,
    templateCode: 'import turtle\n\nscreen = turtle.Screen()\nscreen.bgcolor("black")\n\nt = turtle.Turtle()\nt.speed(5)\n\ncolors = ["red", "orange", "yellow", "green", "blue",\n          "purple", "pink", "cyan", "magenta", "gold"]\n\n# 请在下方编写你的代码\n\n\nturtle.done()\n',
  },
]

// ============================================================================
// ScratchJr 题集（ScratchJr 基础测评题）
// ============================================================================
export const scratchJrSet: ExamQuestion[] = [
  { id: 1011, type: 'choice', title: '在 ScratchJr 中，编写程序的地方叫什么？（　）', options: ['舞台', '画布', '背景', '页面'], answer: 'A', analysis: 'ScratchJr 中编写程序的地方称为舞台（Stage），可以添加角色、背景并创作交互故事。', score: 5 },
  { id: 1012, type: 'choice', title: '在 ScratchJr 中，执行所有动作的角色叫什么？（　）', options: ['背景', '积木', '精灵（Sprite）', '按钮'], answer: 'C', analysis: '执行动作的角色称为精灵（Sprite）。', score: 5 },
  { id: 1013, type: 'choice', title: '新添加的角色在 ScratchJr 舞台中默认出现在什么位置？（　）', options: ['中央', '左上角', '右上角', '左下角'], answer: 'A', analysis: '新添加的角色默认出现在舞台中央。', score: 5 },
  { id: 1014, type: 'choice', title: 'ScratchJr 舞台的背景默认是什么颜色？（　）', options: ['黑色', '蓝色', '绿色', '白色'], answer: 'D', analysis: 'ScratchJr 舞台背景默认为白色。', score: 5 },
  { id: 1015, type: 'choice', title: 'ScratchJr 的主角默认是哪个角色？（　）', options: ['小狗', '小猫', '小兔子', '小人'], answer: 'B', analysis: 'ScratchJr 默认主角是小猫。', score: 5 },
  { id: 1016, type: 'choice', title: '哪个按钮可以给项目添加标题和文字？（　）', options: ['添加文字按钮', '添加角色按钮', '播放按钮', '保存按钮'], answer: 'A', analysis: '添加文字按钮用于给项目添加标题和文字说明。', score: 5 },
  { id: 1017, type: 'judge', title: 'ScratchJr 适合 5-7 岁低龄儿童，通过拼图式积木学习基础编程逻辑。（　）', answer: 'true', analysis: 'ScratchJr 专为 5-7 岁儿童设计，通过图形化积木块学习基础编程。', score: 5 },
  { id: 1018, type: 'judge', title: '在 ScratchJr 中，绿旗积木的作用是停止程序运行。（　）', answer: 'false', analysis: '绿旗积木是程序的启动触发块，点击绿旗开始运行程序。', score: 5 },
  {
    id: 1019, type: 'scratchjr',
    title: '创意挑战：使用 ScratchJr 制作一个"小猫捉迷藏"动画\n\n要求：\n1. 小猫从舞台左侧走到右侧\n2. 走到右侧后隐藏起来\n3. 等待 2 秒后重新出现在左侧\n4. 循环执行以上动作\n\n提示：使用"移动""隐藏""等待""显示"积木块',
    language: 'scratchjr', score: 30,
  },
]

/** 通用兜底题集（无匹配 examId 时使用，覆盖多语言） */
const fallbackSet: ExamQuestion[] = [
  { id: 1, type: 'choice', title: 'Python 中以下哪个是正确的变量名？', options: ['2name', 'my_var', 'class', 'for'], answer: 'B', score: 4 },
  { id: 2, type: 'choice', title: 'Python 中用于输出内容到控制台的内置函数是？', options: ['print()', 'input()', 'len()', 'type()'], answer: 'A', score: 4 },
  { id: 3, type: 'judge', title: '编程可以培养逻辑思维能力。（　）', answer: 'true', score: 4 },
  { id: 4, type: 'code', title: '编写一个函数，实现两数交换', language: 'python', score: 10, templateCode: '# 在这里编写代码\ndef swap(a, b):\n    # TODO\n    pass\n' },
  { id: 5, type: 'code', title: '使用 C++ 输出 Hello World', language: 'cpp', score: 10, templateCode: '#include <iostream>\nusing namespace std;\n\nint main() {\n    // TODO\n    return 0;\n}\n' },
  { id: 6, type: 'code', title: '使用 GoC 画一个等边三角形', language: 'goc', score: 10, templateCode: '#include <graphics.h>\nint main() {\n    initGraph(800, 600);\n    // TODO: 画等边三角形\n    closeGraph();\n    return 0;\n}\n' },
]

// ============================================================================
// Python 二级题集（青少年软件编程等级考试 Python 二级真题）
// ============================================================================
const python2Set: ExamQuestion[] = [
  { id: 2011, type: 'choice', title: '已知 s = "Python"，则 s[0] 的值是？（　）', options: ['P', 'y', 't', 'n'], answer: 'A', analysis: '字符串索引从 0 开始，s[0] 是第一个字符 P。', score: 5 },
  { id: 2012, type: 'choice', title: '执行 print(len("Python")) 的输出结果是？（　）', options: ['5', '6', '7', '报错'], answer: 'B', analysis: 'len() 返回字符串长度，"Python" 有 6 个字符。', score: 5 },
  { id: 2013, type: 'choice', title: '执行 print("Hello".upper()) 的输出结果是？（　）', options: ['hello', 'HELLO', 'Hello', '报错'], answer: 'B', analysis: 'upper() 方法把字符串中的所有字母转换为大写。', score: 5 },
  { id: 2014, type: 'choice', title: '已知 lst = [1, 2, 3]，执行 lst.append(4) 后，lst 的值是？（　）', options: ['[1, 2, 3, 4]', '[4, 1, 2, 3]', '[1, 2, 3]', '报错'], answer: 'A', analysis: 'append() 在列表末尾追加元素，变为 [1, 2, 3, 4]。', score: 5 },
  { id: 2015, type: 'choice', title: '执行 for i in range(1, 4): print(i) 的输出结果是？（　）', options: ['1 2 3', '1 2 3 4', '0 1 2 3', '1 2'], answer: 'A', analysis: 'range(1, 4) 生成 1、2、3（不包含 4）。', score: 5 },
  { id: 2016, type: 'choice', title: '表达式 3 > 2 and 5 < 4 的运算结果是？（　）', options: ['True', 'False', '报错', 'None'], answer: 'B', analysis: '3>2 为 True，5<4 为 False，True and False = False。', score: 5 },
  { id: 2017, type: 'choice', title: '执行 print(int("25") + 5) 的输出结果是？（　）', options: ['30', '"255"', '报错', '25'], answer: 'A', analysis: 'int("25") 把字符串转为整数 25，25 + 5 = 30。', score: 5 },
  { id: 2018, type: 'choice', title: '执行 print("abcdef"[1:4]) 的输出结果是？（　）', options: ['bcd', 'abc', 'cde', 'bcde'], answer: 'A', analysis: '切片 [1:4] 取索引 1 到 3 的字符（不含 4），即 "bcd"。', score: 5 },
  { id: 2019, type: 'judge', title: '在 Python 中，列表的索引从 1 开始。（　）', answer: 'false', analysis: '列表（及字符串）的索引从 0 开始，不是 1。', score: 5 },
  { id: 2020, type: 'judge', title: 'range(3) 会依次生成 0、1、2 这三个整数。（　）', answer: 'true', analysis: 'range(3) 生成 0、1、2，共 3 个数。', score: 5 },
  {
    id: 2021, type: 'code',
    title: '编程题：编写程序，计算 1 到 N 的所有整数之和\n\n要求：\n- 从键盘输入一个正整数 N\n- 使用 for 循环累加 1 到 N 的所有整数\n- 输出累加结果\n\n示例输入：\n10\n\n示例输出：\n55',
    language: 'python', score: 30,
    templateCode: '# 请在下方编写你的代码\nn = int(input())\n\n',
  },
]

// ============================================================================
// C++ 二级题集（青少年软件编程等级考试 C/C++ 二级真题）
// ============================================================================
const cpp2Set: ExamQuestion[] = [
  { id: 2051, type: 'choice', title: '已知 int a[5] = {1, 2, 3, 4, 5};，则 a[0] 的值是？（　）', options: ['1', '2', '0', '报错'], answer: 'A', analysis: '数组下标从 0 开始，a[0] 是第一个元素 1。', score: 5 },
  { id: 2052, type: 'choice', title: '已知 int a[5] = {1, 2, 3, 4, 5};，则 a[4] 的值是？（　）', options: ['4', '5', '0', '报错'], answer: 'B', analysis: 'a[4] 是数组第 5 个元素（下标 4），值为 5。', score: 5 },
  { id: 2053, type: 'choice', title: '已知 string s = "Hello";，则 s.length() 的值是？（　）', options: ['4', '5', '6', '报错'], answer: 'B', analysis: 'length() 返回字符串长度，"Hello" 有 5 个字符。', score: 5 },
  { id: 2054, type: 'choice', title: '执行 int a = 5; if (a > 3) a = 10; else a = 0; 后，a 的值是？（　）', options: ['10', '0', '5', '报错'], answer: 'A', analysis: '5 > 3 成立，执行 a = 10。', score: 5 },
  { id: 2055, type: 'choice', title: '执行 for(int i=0;i<3;i++) for(int j=0;j<2;j++) cout << "*"; 一共输出几个 *？（　）', options: ['5', '6', '3', '2'], answer: 'B', analysis: '外层循环 3 次，内层循环 2 次，共 3×2=6 次。', score: 5 },
  { id: 2056, type: 'choice', title: '已知 char c = \'A\';，表达式 c + 1 的值为？（　）', options: ['\'A\'', '\'B\'', '66', '报错'], answer: 'C', analysis: '字符 \'A\' 的 ASCII 码为 65，c + 1 即 65 + 1 = 66（作为整数）。', score: 5 },
  { id: 2057, type: 'choice', title: '执行 int a = 7; int b = a % 3; 后，b 的值是？（　）', options: ['2', '1', '3', '0'], answer: 'B', analysis: '7 % 3 = 1（7 除以 3 余 1）。', score: 5 },
  { id: 2058, type: 'choice', title: '判断整数 n 是否为偶数，正确的条件表达式是？（　）', options: ['n % 2 == 0', 'n / 2 == 0', 'n % 2 == 1', 'n * 2 == 0'], answer: 'A', analysis: '偶数除以 2 余数为 0，即 n % 2 == 0。', score: 5 },
  { id: 2059, type: 'judge', title: 'C++ 中数组元素的下标从 0 开始。（　）', answer: 'true', analysis: 'C++ 数组下标从 0 开始，最后一个元素下标为长度减 1。', score: 5 },
  { id: 2060, type: 'judge', title: 'int a[3] = {1, 2, 3, 4}; 是合法的数组定义，能正常编译。（　）', answer: 'false', analysis: '数组 a 长度为 3，却初始化了 4 个元素，会导致编译错误。', score: 5 },
  {
    id: 2061, type: 'code',
    title: '编程题：编写 C++ 程序，统计数组中偶数的个数\n\n要求：\n- 从键盘输入 N 个整数（N <= 100）\n- 统计其中偶数的个数\n- 输出偶数的个数\n\n示例输入：\n5\n1 2 3 4 5\n\n示例输出：\n2',
    language: 'cpp', score: 30,
    templateCode: '#include <iostream>\nusing namespace std;\n\nint main() {\n    int n;\n    cin >> n;\n    int arr[100];\n    for (int i = 0; i < n; i++) {\n        cin >> arr[i];\n    }\n    // 请在下方编写统计偶数的代码\n    \n    return 0;\n}\n',
  },
]

// ============================================================================
// Scratch 二级题集（青少年软件编程等级考试 Scratch 二级真题）
// ============================================================================
const scratch2Set: ExamQuestion[] = [
  { id: 2041, type: 'choice', title: 'Scratch 舞台中央的坐标是？（　）', options: ['(0, 0)', '(100, 100)', '(-100, -100)', '(240, 180)'], answer: 'A', analysis: '舞台坐标系原点在正中央，坐标为 (0, 0)。', score: 5 },
  { id: 2042, type: 'choice', title: '变量"得分"初始值为 0，执行"将得分增加 10"2 次后，得分的值是？（　）', options: ['10', '20', '0', '报错'], answer: 'B', analysis: '每次增加 10，执行 2 次共增加 20。', score: 5 },
  { id: 2043, type: 'choice', title: '使用"当作为克隆体启动时"积木，克隆体会执行？（　）', options: ['克隆体自己的脚本', '原角色的脚本', '不执行任何脚本', '停止所有脚本'], answer: 'A', analysis: '"当作为克隆体启动时"积木下面的脚本由克隆体执行。', score: 5 },
  { id: 2044, type: 'choice', title: '一个角色"广播消息1"后，会发生什么？（　）', options: ['所有接收该消息的角色执行对应脚本', '只有广播者自己执行', '停止所有脚本', '删除该角色'], answer: 'A', analysis: '广播消息会触发所有"当接收到消息1"积木下的脚本。', score: 5 },
  { id: 2045, type: 'choice', title: '积木"在 1 到 10 之间取随机数"可能取到下列哪个值？（　）', options: ['0', '1', '11', '15'], answer: 'B', analysis: '取值范围包含 1 和 10（闭区间），1 是可能值。', score: 5 },
  { id: 2046, type: 'choice', title: '想让角色移到舞台右上角，坐标应接近？（　）', options: ['(240, 180)', '(-240, -180)', '(0, 0)', '(-240, 180)'], answer: 'A', analysis: '舞台右上角 x 为正、y 为正，最大约 (240, 180)。', score: 5 },
  { id: 2047, type: 'choice', title: '积木"如果 <碰到边缘> 那么..."属于哪种结构？（　）', options: ['条件判断', '循环', '事件', '运算'], answer: 'A', analysis: '"如果...那么"是条件判断结构。', score: 5 },
  { id: 2048, type: 'judge', title: 'Scratch 中"重复执行"积木属于循环结构。（　）', answer: 'true', analysis: '"重复执行"会让积木内的脚本循环运行。', score: 5 },
  { id: 2049, type: 'judge', title: '克隆体不能拥有自己的脚本。（　）', answer: 'false', analysis: '克隆体可以通过"当作为克隆体启动时"积木拥有自己的脚本。', score: 5 },
  {
    id: 2050, type: 'scratch',
    title: '编程挑战：制作一个"克隆小球"动画\n\n要求：\n1. 创建一个圆形角色（小球）\n2. 点击绿旗后，克隆 10 个小球\n3. 每个克隆体从舞台中央出发，朝随机方向移动\n4. 小球碰到边缘后反弹\n5. 小球颜色随机变化\n\n提示：使用"克隆""随机数""碰到边缘就反弹""颜色特效"等积木',
    language: 'scratch3', score: 30,
  },
]

// ============================================================================
// ScratchJr 进阶题集（ScratchJr 基础进阶测评）
// ============================================================================
const scratchJr2Set: ExamQuestion[] = [
  { id: 10211, type: 'choice', title: 'ScratchJr 中让角色向右移动的积木属于哪个类别？（　）', options: ['蓝色（运动）', '黄色（触发）', '紫色（外观）', '绿色（声音）'], answer: 'A', analysis: '运动类积木是蓝色的，用于控制角色移动。', score: 5 },
  { id: 10212, type: 'choice', title: '想让角色暂时隐藏起来，应该使用哪个积木？（　）', options: ['隐藏积木', '显示积木', '移动积木', '说话积木'], answer: 'A', analysis: '隐藏积木让角色暂时不可见，显示积木再让其出现。', score: 5 },
  { id: 10213, type: 'choice', title: '"等待"积木的作用是？（　）', options: ['暂停一段时间', '结束程序', '加快程序运行', '循环执行'], answer: 'A', analysis: '等待积木让程序暂停指定的时间后继续执行。', score: 5 },
  { id: 10214, type: 'choice', title: '"重复"积木可以做什么？（　）', options: ['重复执行其内的积木', '停止程序', '播放声音', '移动角色'], answer: 'A', analysis: '重复积木让它里面的积木块循环执行指定次数。', score: 5 },
  { id: 10215, type: 'choice', title: '点击舞台上的某个角色，可以？（　）', options: ['查看并编辑该角色的程序', '删除整个项目', '保存项目', '关闭应用'], answer: 'A', analysis: '点击角色会选中它，可以查看和编辑该角色专属的程序。', score: 5 },
  { id: 10216, type: 'choice', title: '在 ScratchJr 中，哪个按钮可以开始播放你的项目？（　）', options: ['绿旗按钮', '红色停止按钮', '黄色按钮', '蓝色按钮'], answer: 'A', analysis: '绿旗按钮用于启动（播放）项目。', score: 5 },
  { id: 10217, type: 'judge', title: '在 ScratchJr 中，可以为角色添加声音。（　）', answer: 'true', analysis: 'ScratchJr 提供录音和内置声音，可为角色添加声音。', score: 5 },
  { id: 10218, type: 'judge', title: 'ScratchJr 中一个项目只能有一个角色。（　）', answer: 'false', analysis: '一个 ScratchJr 项目可以添加多个角色。', score: 5 },
  {
    id: 10219, type: 'scratchjr',
    title: '创意挑战：使用 ScratchJr 制作"小猫来回走动"动画\n\n要求：\n1. 小猫从舞台左侧向右走\n2. 走到右侧后转身\n3. 再走回左侧\n4. 循环执行以上动作\n\n提示：使用"移动""转身""重复"等积木块',
    language: 'scratchjr', score: 30,
  },
]

// ============================================================================
// Python Turtle 进阶题集（海龟绘图进阶测试）
// ============================================================================
const turtle2Set: ExamQuestion[] = [
  { id: 10711, type: 'choice', title: 'turtle.speed(0) 表示什么？（　）', options: ['最快速度', '最慢速度', '停止画图', '程序报错'], answer: 'A', analysis: 'speed 取值范围 0-10，0 表示最快（无动画）。', score: 5 },
  { id: 10712, type: 'choice', title: '画一个正方形，需要重复执行几次"前进 + 右转 90 度"？（　）', options: ['4', '3', '5', '6'], answer: 'A', analysis: '正方形有 4 条边，需要重复 4 次。', score: 5 },
  { id: 10713, type: 'choice', title: '画一个正五边形，海龟每次需要转多少度？（　）', options: ['72', '60', '90', '120'], answer: 'A', analysis: '正多边形外角 = 360/边数，正五边形外角 = 360/5 = 72 度。', score: 5 },
  { id: 10714, type: 'choice', title: 'turtle.begin_fill() 和 turtle.end_fill() 的作用是？（　）', options: ['填充闭合图形', '开始画图', '停止画图', '保存图形'], answer: 'A', analysis: 'begin_fill() 和 end_fill() 之间的闭合区域会被填充颜色。', score: 5 },
  { id: 10715, type: 'choice', title: 'turtle.pensize(5) 设置的是？（　）', options: ['画笔粗细', '画笔颜色', '画布大小', '绘制速度'], answer: 'A', analysis: 'pensize() 设置画笔的粗细（线条宽度）。', score: 5 },
  { id: 10716, type: 'choice', title: '想让海龟图标隐藏（不显示），应使用？（　）', options: ['turtle.hideturtle()', 'turtle.showturtle()', 'turtle.penup()', 'turtle.clear()'], answer: 'A', analysis: 'hideturtle() 隐藏海龟，showturtle() 显示海龟。', score: 5 },
  { id: 10717, type: 'choice', title: 'turtle.reset() 的作用是？（　）', options: ['清屏并重置海龟到初始状态', '只清屏', '只移动海龟', '关闭窗口'], answer: 'A', analysis: 'reset() 清空画布并把海龟重置到初始位置和朝向。', score: 5 },
  { id: 10718, type: 'choice', title: '要画一个圆，应使用的命令是？（　）', options: ['turtle.circle()', 'turtle.square()', 'turtle.rect()', 'turtle.polygon()'], answer: 'A', analysis: 'turtle.circle(半径) 用于画圆。', score: 5 },
  { id: 10719, type: 'judge', title: 'Python turtle 海龟的默认朝向是向右（正东方向）。（　）', answer: 'true', analysis: 'turtle 默认朝向 0 度（向右/正东）。', score: 5 },
  { id: 10720, type: 'judge', title: 'turtle.done() 的作用是让绘图窗口保持不自动关闭。（　）', answer: 'true', analysis: 'turtle.done() 进入事件循环，保持窗口打开等待关闭。', score: 5 },
  {
    id: 10721, type: 'code',
    title: '绘图挑战：使用 Python Turtle 绘制一个五角星\n\n要求：\n1. 使用 for 循环绘制五角星\n2. 五角星为红色\n3. 每条边长度 200\n\n提示：\n- 五角星每次转角为 144 度\n- 共 5 条边',
    language: 'python', score: 30,
    templateCode: 'import turtle\n\nt = turtle.Turtle()\nt.color("red")\nt.speed(3)\n\n# 请在下方编写画五角星的代码\n\n\nturtle.done()\n',
  },
]

// ============================================================================
// GoC 进阶题集（GoC 图形编程进阶题目）
// ============================================================================
const goc2Set: ExamQuestion[] = [
  { id: 4011, type: 'choice', title: 'GoC 中画实心圆（填充圆）的命令是？（　）', options: ['oo()', 'o()', 'circle()', 'dot()'], answer: 'A', analysis: 'oo(半径, 颜色) 画实心圆，o() 画空心圆。', score: 5 },
  { id: 4012, type: 'choice', title: '设置画笔颜色为红色，正确的代码是？（　）', options: ['pen.color(255, 0, 0)', 'pen.color(0, 255, 0)', 'pen.color(0, 0, 255)', 'pen.red()'], answer: 'A', analysis: 'RGB 颜色中红色为 (255, 0, 0)。', score: 5 },
  { id: 4013, type: 'choice', title: '设置画笔粗细为 3，正确的代码是？（　）', options: ['pen.size(3)', 'pen.width(3)', 'pen.bold(3)', 'pen.thick(3)'], answer: 'A', analysis: 'pen.size(n) 设置画笔的粗细。', score: 5 },
  { id: 4014, type: 'choice', title: '画一个正六边形，每次需要右转的度数是？（　）', options: ['60', '90', '120', '30'], answer: 'A', analysis: '正六边形外角 = 360/6 = 60 度。', score: 5 },
  { id: 4015, type: 'choice', title: '让画笔移到坐标 (100, 100) 但不画线，应该？（　）', options: ['先 pen.up() 再移动', '直接移动', '先 pen.down() 再移动', '无法实现'], answer: 'A', analysis: '先抬笔 pen.up()，移动时就不会留下线条。', score: 5 },
  { id: 4016, type: 'choice', title: '使用 for 循环画正五边形，循环体内正确的代码是？（　）', options: ['fd(边长) + rt(72)', 'fd(边长) + rt(90)', 'bk(边长) + lt(72)', 'fd(边长) + rt(60)'], answer: 'A', analysis: '正五边形每次前进边长后右转 72 度。', score: 5 },
  { id: 4017, type: 'choice', title: 'GoC 中 o(50, 1) 画的是？（　）', options: ['半径为 50 的红色空心圆', '半径为 50 的红色实心圆', '直径为 50 的圆', '边长为 50 的圆'], answer: 'A', analysis: 'o(半径, 颜色) 画空心圆，颜色号 1 是红色。', score: 5 },
  { id: 4018, type: 'judge', title: 'GoC 中 rr(100, 50, 2) 画的是宽 100、高 50 的实心矩形。（　）', answer: 'true', analysis: 'rr(宽, 高, 颜色) 画实心矩形。', score: 5 },
  { id: 4019, type: 'judge', title: 'GoC 中 pen.up() 抬笔后，再执行 fd(100) 仍会画出线条。（　）', answer: 'false', analysis: '抬笔后移动不会留下线条，落笔 pen.down() 后才会画线。', score: 5 },
  {
    id: 4020, type: 'code',
    title: '编程题：使用 GoC 绘制一个"风车"图案\n\n要求：\n- 风车由 4 个等边三角形组成\n- 每个三角形绕中心旋转 90 度\n- 使用 for 循环绘制\n\n提示：\n- 等边三角形每次右转 120 度\n- 每画完一个三角形，右转 90 度',
    language: 'goc', score: 30,
    templateCode: '#include <graphics.h>\nint main() {\n    initGraph(800, 600);\n    // 请在下方编写画风车的代码\n    \n    closeGraph();\n    return 0;\n}\n',
  },
]

// ============================================================================
// Python 三级题集（青少年软件编程等级考试 Python 三级真题）
// ============================================================================
const python3Set: ExamQuestion[] = [
  { id: 3011, type: 'choice', title: '已知 def f(x): return x * 2，则 f(5) 的值是？（　）', options: ['5', '10', '25', '报错'], answer: 'B', analysis: '函数 f 返回参数的 2 倍，f(5) = 5 * 2 = 10。', score: 5 },
  { id: 3012, type: 'choice', title: '函数自己调用自己，这种编程方式叫做？（　）', options: ['递归', '循环', '迭代', '跳转'], answer: 'A', analysis: '函数直接或间接调用自身称为递归。', score: 5 },
  { id: 3013, type: 'choice', title: '已知 d = {"a": 1, "b": 2}，则 d["b"] 的值是？（　）', options: ['1', '2', 'a', '报错'], answer: 'B', analysis: '字典通过键访问值，d["b"] = 2。', score: 5 },
  { id: 3014, type: 'choice', title: '执行 print([x * x for x in range(4)]) 的输出结果是？（　）', options: ['[0, 1, 4, 9]', '[1, 4, 9, 16]', '[0, 1, 2, 3]', '[0, 2, 4, 6]'], answer: 'A', analysis: '列表推导式对 0、1、2、3 分别平方，得到 [0, 1, 4, 9]。', score: 5 },
  { id: 3015, type: 'choice', title: '已知 f = lambda x: x + 1，则 f(3) 的值是？（　）', options: ['3', '4', '1', '报错'], answer: 'B', analysis: 'lambda 是匿名函数，f(3) = 3 + 1 = 4。', score: 5 },
  { id: 3016, type: 'choice', title: '执行 print("a,b,c".split(",")) 的输出结果是？（　）', options: ["['a', 'b', 'c']", "['a,b,c']", '["a,b,c"]', '报错'], answer: 'A', analysis: 'split(",") 按逗号分割字符串，得到列表 [\'a\', \'b\', \'c\']。', score: 5 },
  { id: 3017, type: 'choice', title: '执行 print(set([1, 2, 2, 3])) 的输出结果是？（　）', options: ['{1, 2, 3}', '{1, 2, 2, 3}', '[1, 2, 3]', '报错'], answer: 'A', analysis: '集合自动去除重复元素，set([1,2,2,3]) = {1, 2, 3}。', score: 5 },
  { id: 3018, type: 'choice', title: '要捕获代码运行时的异常并避免程序崩溃，应使用？（　）', options: ['try...except', 'for...in', 'if...else', 'while...else'], answer: 'A', analysis: 'try...except 用于捕获并处理异常。', score: 5 },
  { id: 3019, type: 'judge', title: 'Python 3.7 及以上版本中，字典会保持键值对的插入顺序。（　）', answer: 'true', analysis: 'Python 3.7 起字典是有序的，保持插入顺序。', score: 5 },
  { id: 3020, type: 'judge', title: '递归函数必须包含递归出口（终止条件），否则可能陷入无限递归。（　）', answer: 'true', analysis: '没有终止条件的递归会无限调用自身，最终导致栈溢出。', score: 5 },
  {
    id: 3021, type: 'code',
    title: '编程题：编写程序，计算斐波那契数列的第 N 项\n\n要求：\n- 从键盘输入正整数 N（N <= 30）\n- 斐波那契数列：1, 1, 2, 3, 5, 8, ...（第 1、2 项为 1）\n- 使用循环或递归计算第 N 项并输出\n\n示例输入：\n7\n\n示例输出：\n13',
    language: 'python', score: 30,
    templateCode: '# 请在下方编写你的代码\nn = int(input())\n\n',
  },
]

// ============================================================================
// C++ 三级题集（青少年软件编程等级考试 C/C++ 三级真题）
// ============================================================================
const cpp3Set: ExamQuestion[] = [
  { id: 3051, type: 'choice', title: '已知 int add(int a, int b) { return a + b; }，则 add(3, 4) 的值是？（　）', options: ['7', '34', '12', '报错'], answer: 'A', analysis: '函数 add 返回两数之和，add(3, 4) = 7。', score: 5 },
  { id: 3052, type: 'choice', title: '已知 int a = 10; int *p = &a;，则 *p 的值是？（　）', options: ['10', '&a', '地址值', '报错'], answer: 'A', analysis: 'p 指向变量 a，*p 解引用取 a 的值 10。', score: 5 },
  { id: 3053, type: 'choice', title: '定义一个包含姓名和年龄的结构体，正确写法是？（　）', options: ['struct Student { string name; int age; };', 'Student struct { string name; int age; }', 'class Student { string name; int age; };', 'struct { name, age } Student;'], answer: 'A', analysis: 'struct 关键字定义结构体，成员用分号分隔。', score: 5 },
  { id: 3054, type: 'choice', title: '已知 string s = "abcdef";，则 s.substr(2, 3) 的值是？（　）', options: ['cde', 'abc', 'bcd', 'def'], answer: 'A', analysis: 'substr(2, 3) 从下标 2 开始取 3 个字符，即 "cde"。', score: 5 },
  { id: 3055, type: 'choice', title: '计算 n 的阶乘常用哪种算法结构？（　）', options: ['递归或循环', '只能顺序', '只能跳转', '只能判断'], answer: 'A', analysis: '阶乘可通过递归（n! = n*(n-1)!）或循环实现。', score: 5 },
  { id: 3056, type: 'choice', title: '已知 vector<int> v; 执行 v.push_back(5); 后，v.size() 的值是？（　）', options: ['1', '5', '0', '报错'], answer: 'A', analysis: 'push_back 向 vector 末尾添加元素，size 变为 1。', score: 5 },
  { id: 3057, type: 'choice', title: '使用 sort() 函数对数组排序，需要包含哪个头文件？（　）', options: ['#include <algorithm>', '#include <iostream>', '#include <vector>', '#include <string>'], answer: 'A', analysis: 'sort() 函数定义在 <algorithm> 头文件中。', score: 5 },
  { id: 3058, type: 'choice', title: '已知 int a = 5; int &r = a; 执行 r = 10; 后，a 的值是？（　）', options: ['10', '5', '0', '报错'], answer: 'A', analysis: 'r 是 a 的引用（别名），修改 r 即修改 a。', score: 5 },
  { id: 3059, type: 'judge', title: '递归函数必须包含递归出口（终止条件），否则会无限递归导致栈溢出。（　）', answer: 'true', analysis: '缺少终止条件的递归会无限调用自身。', score: 5 },
  { id: 3060, type: 'judge', title: 'C++ 中 struct 结构体的成员默认访问权限是 public（公有）。（　）', answer: 'true', analysis: 'struct 成员默认 public，而 class 成员默认 private。', score: 5 },
  {
    id: 3061, type: 'code',
    title: '编程题：编写 C++ 程序，计算斐波那契数列的第 N 项\n\n要求：\n- 从键盘输入正整数 N（N <= 40）\n- 斐波那契数列：1, 1, 2, 3, 5, 8, ...（第 1、2 项为 1）\n- 使用循环计算第 N 项并输出\n\n示例输入：\n7\n\n示例输出：\n13',
    language: 'cpp', score: 30,
    templateCode: '#include <iostream>\nusing namespace std;\n\nint main() {\n    int n;\n    cin >> n;\n    // 请在下方编写计算斐波那契第 n 项的代码\n    \n    return 0;\n}\n',
  },
]

// ============================================================================
// Scratch 三级题集（青少年软件编程等级考试 Scratch 三级真题）
// ============================================================================
const scratch3Set: ExamQuestion[] = [
  { id: 3041, type: 'choice', title: 'Scratch 中用于存储一组数据（如多个姓名）的结构是？（　）', options: ['列表', '变量', '广播', '克隆'], answer: 'A', analysis: '列表可以一次存储多个数据，变量只能存一个值。', score: 5 },
  { id: 3042, type: 'choice', title: 'Scratch 中"自制积木"（自定义积木）的作用类似于编程中的？（　）', options: ['函数', '变量', '循环', '列表'], answer: 'A', analysis: '自制积木把一段脚本封装成可重复调用的块，类似函数。', score: 5 },
  { id: 3043, type: 'choice', title: '把两个字符串"你好"和"世界"连接起来，结果是？（　）', options: ['你好世界', '你好 世界', '世界你好', '你好，世界'], answer: 'A', analysis: '字符串连接把两个字符串首尾相连，得到"你好世界"。', score: 5 },
  { id: 3044, type: 'choice', title: '从列表 [3, 8, 5, 2] 中找出最大值，结果是？（　）', options: ['8', '5', '3', '2'], answer: 'A', analysis: '逐个比较，最大值是 8。', score: 5 },
  { id: 3045, type: 'choice', title: '"如果 <x> 那么...否则..."积木属于？（　）', options: ['条件判断', '循环', '事件', '运算'], answer: 'A', analysis: '该积木是双分支条件判断结构。', score: 5 },
  { id: 3046, type: 'choice', title: '在 Scratch 舞台中，x 坐标增大会让角色向哪个方向移动？（　）', options: ['向右', '向左', '向上', '向下'], answer: 'A', analysis: 'x 坐标代表水平方向，增大即向右移动。', score: 5 },
  { id: 3047, type: 'choice', title: '想让变量只被当前角色使用，应将其设置为？（　）', options: ['仅适用于当前角色', '适用于所有角色', '全局变量', '系统变量'], answer: 'A', analysis: '变量可设为"适用于所有角色"或"仅适用于当前角色"。', score: 5 },
  { id: 3048, type: 'judge', title: 'Scratch 的列表可以同时存储数字和文本。（　）', answer: 'true', analysis: 'Scratch 列表元素可以是数字或文本，类型不固定。', score: 5 },
  { id: 3049, type: 'judge', title: 'Scratch 的自制积木可以带参数（输入项）。（　）', answer: 'true', analysis: '自制积木可以添加数字、文本等输入参数。', score: 5 },
  {
    id: 3050, type: 'scratch',
    title: '编程挑战：制作一个"找最大值"程序\n\n要求：\n1. 创建一个列表，存入 5 个随机数\n2. 使用变量记录当前最大值\n3. 逐个比较列表中的数\n4. 找到最大值后，角色说出"最大值是 X"\n\n提示：使用"列表""变量""重复执行""如果...那么"等积木',
    language: 'scratch3', score: 30,
  },
]

// ============================================================================
// ScratchJr 第三套（ScratchJr 综合测评）
// ============================================================================
const scratchJr3Set: ExamQuestion[] = [
  { id: 10311, type: 'choice', title: 'ScratchJr 中让角色"说话"的积木属于哪个类别？（　）', options: ['紫色（外观）', '蓝色（运动）', '黄色（触发）', '绿色（声音）'], answer: 'A', analysis: '说话积木属于外观类（紫色），用于显示文字气泡。', score: 5 },
  { id: 10312, type: 'choice', title: '想让角色变大，应使用哪个积木？（　）', options: ['放大积木', '缩小积木', '移动积木', '隐藏积木'], answer: 'A', analysis: '放大积木让角色尺寸变大。', score: 5 },
  { id: 10313, type: 'choice', title: 'ScratchJr 中结束程序的积木是？（　）', options: ['红色"结束"积木', '绿旗积木', '黄色积木', '蓝色积木'], answer: 'A', analysis: '红色结束积木用于终止程序运行。', score: 5 },
  { id: 10314, type: 'choice', title: '黄色触发类积木的作用是？（　）', options: ['启动一段程序', '移动角色', '改变外观', '播放声音'], answer: 'A', analysis: '触发类积木（如绿旗）作为程序启动的入口。', score: 5 },
  { id: 10315, type: 'choice', title: '让一段动作重复执行，应使用？（　）', options: ['循环（重复）积木', '结束积木', '说话积木', '移动积木'], answer: 'A', analysis: '重复积木让它内部的积木循环执行。', score: 5 },
  { id: 10316, type: 'choice', title: '切换舞台背景，应使用？（　）', options: ['更换背景积木', '添加角色积木', '移动积木', '录音积木'], answer: 'A', analysis: '更换背景积木可以切换不同的舞台背景。', score: 5 },
  { id: 10317, type: 'judge', title: 'ScratchJr 中可以为角色录制自己的声音。（　）', answer: 'true', analysis: 'ScratchJr 提供录音功能，可为角色添加自定义声音。', score: 5 },
  { id: 10318, type: 'judge', title: 'ScratchJr 中角色的大小可以改变。（　）', answer: 'true', analysis: '可通过放大/缩小积木改变角色大小。', score: 5 },
  {
    id: 10319, type: 'scratchjr',
    title: '创意挑战：使用 ScratchJr 制作"小猫变大变小"动画\n\n要求：\n1. 小猫初始为正常大小\n2. 先变大再变小\n3. 循环执行以上动作\n\n提示：使用"放大""缩小""重复"等积木块',
    language: 'scratchjr', score: 30,
  },
]

// ============================================================================
// Python Turtle 第三套（海龟绘图综合测试）
// ============================================================================
const turtle3Set: ExamQuestion[] = [
  { id: 10811, type: 'choice', title: 'turtle.fillcolor("red") 设置的是？（　）', options: ['填充颜色', '画笔颜色', '背景颜色', '线条粗细'], answer: 'A', analysis: 'fillcolor() 设置填充颜色，pencolor() 设置画笔颜色。', score: 5 },
  { id: 10812, type: 'choice', title: '在 begin_fill() 和 end_fill() 之间画的闭合图形会？（　）', options: ['被填充颜色', '被删除', '变成虚线', '自动保存'], answer: 'A', analysis: '两者之间的闭合区域会用 fillcolor 颜色填充。', score: 5 },
  { id: 10813, type: 'choice', title: 'turtle.circle(50, 180) 会画出？（　）', options: ['半圆（弧）', '完整圆', '四分之一圆', '直线'], answer: 'A', analysis: '第二个参数是弧度角度，180 度即半圆。', score: 5 },
  { id: 10814, type: 'choice', title: 'turtle.goto(100, 100) 的作用是？（　）', options: ['移动到坐标 (100,100)', '旋转 100 度', '前进 100', '画圆'], answer: 'A', analysis: 'goto(x, y) 让海龟移动到指定坐标。', score: 5 },
  { id: 10815, type: 'choice', title: 'turtle.home() 的作用是？（　）', options: ['回到原点 (0,0) 并朝向初始方向', '关闭窗口', '清屏', '画房子'], answer: 'A', analysis: 'home() 让海龟回到原点并恢复默认朝向。', score: 5 },
  { id: 10816, type: 'choice', title: 'turtle.seth(90) 设置的是？（　）', options: ['海龟朝向', '画笔颜色', '画布大小', '速度'], answer: 'A', analysis: 'seth(角度) 设置海龟的绝对朝向。', score: 5 },
  { id: 10817, type: 'choice', title: 'turtle.dot(10, "blue") 的作用是？（　）', options: ['画一个蓝色圆点', '画一条线', '移动海龟', '设置速度'], answer: 'A', analysis: 'dot(直径, 颜色) 在当前位置画一个实心圆点。', score: 5 },
  { id: 10818, type: 'choice', title: 'turtle 画布坐标系的默认范围（以原点为中心）大致是？（　）', options: ['正负各数百像素', '0 到 100', '无限大', '只有正数'], answer: 'A', analysis: '默认窗口约 400×300，坐标从中心向四周延伸数百像素。', score: 5 },
  { id: 10819, type: 'judge', title: 'Python turtle 画布的坐标原点默认在画布正中央。（　）', answer: 'true', analysis: 'turtle 坐标原点 (0,0) 默认位于窗口中央。', score: 5 },
  { id: 10820, type: 'judge', title: 'turtle.circle(50, 360) 会画出一个完整的圆。（　）', answer: 'true', analysis: '360 度弧线即完整圆周。', score: 5 },
  {
    id: 10821, type: 'code',
    title: '绘图挑战：使用 Python Turtle 绘制一朵"花"\n\n要求：\n1. 花由多个圆形花瓣组成\n2. 使用 for 循环绘制\n3. 每画完一个花瓣旋转一定角度\n\n提示：\n- 使用 turtle.circle() 画花瓣\n- 花瓣数量建议 6-8 个，每次旋转 360/花瓣数 度',
    language: 'python', score: 30,
    templateCode: 'import turtle\n\nt = turtle.Turtle()\nt.speed(5)\nt.color("pink")\n\n# 请在下方编写画花的代码\n\n\nturtle.done()\n',
  },
]

// ============================================================================
// GoC 第三套（GoC 图形编程综合题目）
// ============================================================================
const goc3Set: ExamQuestion[] = [
  { id: 4031, type: 'choice', title: 'GoC 中画空心圆的命令是？（　）', options: ['o()', 'oo()', 'circle()', 'round()'], answer: 'A', analysis: 'o(半径, 颜色) 画空心圆，oo() 画实心圆。', score: 5 },
  { id: 4032, type: 'choice', title: 'GoC 中 oo(50, 1) 画的是？（　）', options: ['半径为 50 的红色实心圆', '半径为 50 的红色空心圆', '直径为 50 的圆', '边长为 50 的圆'], answer: 'A', analysis: 'oo(半径, 颜色号) 画实心圆，颜色号 1 是红色。', score: 5 },
  { id: 4033, type: 'choice', title: '设置画笔粗细的命令是？（　）', options: ['pen.size()', 'pen.color()', 'pen.speed()', 'pen.width2()'], answer: 'A', analysis: 'pen.size(n) 设置画笔粗细。', score: 5 },
  { id: 4034, type: 'choice', title: '先画一个空心圆，再在里面画一个实心圆，可以组成？（　）', options: ['圆环', '正方形', '三角形', '直线'], answer: 'A', analysis: '空心圆套实心圆形成圆环效果。', score: 5 },
  { id: 4035, type: 'choice', title: '设置颜色为绿色，正确的 RGB 是？（　）', options: ['pen.color(0, 255, 0)', 'pen.color(255, 0, 0)', 'pen.color(0, 0, 255)', 'pen.color(255, 255, 0)'], answer: 'A', analysis: '绿色 RGB 为 (0, 255, 0)。', score: 5 },
  { id: 4036, type: 'choice', title: '使用 for 循环画 6 个圆排成一圈，每画一个圆需要旋转多少度？（　）', options: ['60', '90', '120', '45'], answer: 'A', analysis: '360/6 = 60 度，每个圆之间旋转 60 度。', score: 5 },
  { id: 4037, type: 'choice', title: '隐藏画笔（不显示海龟图标），应使用？（　）', options: ['pen.hide()', 'pen.show()', 'pen.up()', 'pen.down()'], answer: 'A', analysis: 'pen.hide() 隐藏画笔图标，show() 显示。', score: 5 },
  { id: 4038, type: 'judge', title: 'GoC 中 oo(50, 1) 画的是实心圆。（　）', answer: 'true', analysis: 'oo 是画实心圆的命令。', score: 5 },
  { id: 4039, type: 'judge', title: 'GoC 中 bk(100) 表示前进 100。（　）', answer: 'false', analysis: 'bk 是后退命令，bk(100) 表示后退 100。', score: 5 },
  {
    id: 4040, type: 'code',
    title: '编程题：使用 GoC 绘制一朵"花"\n\n要求：\n- 花由 6 个圆形花瓣组成\n- 使用 for 循环绘制\n- 每个花瓣绕中心旋转 60 度\n\n提示：\n- 使用 o() 或 oo() 画花瓣\n- 每画一个花瓣，旋转 60 度',
    language: 'goc', score: 30,
    templateCode: '#include <graphics.h>\nint main() {\n    initGraph(800, 600);\n    // 请在下方编写画花的代码\n    \n    closeGraph();\n    return 0;\n}\n',
  },
]

/**
 * 考试 id → 题集
 * 键与「赛事详情页」ExamDetailPage.examDataMap 保持一致：
 *   1=Python算法挑战赛  2=Python创意编程(→turtle)  3=Scratch图形化
 *   4=GoC邀请赛        5=CSP-J/S(C++)             6=蓝桥杯(多语言→兜底)
 *   7=AI创新(Python)    8=C++算法精英赛
 *   101=Python体验赛   102=Scratch体验赛          103=C++体验赛
 *   104=GoC体验赛      105=AI体验赛(Python)
 */
export const examQuestionsMap: Record<number, ExamQuestion[]> = {
  1: pythonSet,
  2: turtleSet,
  3: scratchSet,
  4: gocSet,
  5: cppSet,
  6: fallbackSet,
  7: pythonSet,
  8: cppSet,
  101: pythonSet,
  102: scratchSet,
  103: cppSet,
  104: gocSet,
  105: pythonSet,
}

/** 题库中心试卷元信息 */
export interface ExamPaper {
  id: number
  title: string
  language: string // 语言标识
  languageLabel: string // 语言显示名
  level: string // 级别/阶段标签（一级/二级/三级/基础/进阶/综合）
  duration: number // 分钟
  totalScore: number
  passScore: number
  questionCount: number
  tag: string // 分类标签（如 GESP 认证 / 基础测评）
}

/** 题库中心试卷列表（覆盖 6 个编程工具：GESP 认证 + 基础测评） */
export const examPaperList: ExamPaper[] = [
  { id: 201, title: '2025年 GESP 认证 Python 一级真题', language: 'python', languageLabel: 'Python', level: '一级', duration: 60, totalScore: 85, passScore: 51, questionCount: 12, tag: 'GESP 认证' },
  { id: 202, title: '2025年 GESP 认证 C++ 一级真题', language: 'cpp', languageLabel: 'C++', level: '一级', duration: 60, totalScore: 85, passScore: 51, questionCount: 12, tag: 'GESP 认证' },
  { id: 203, title: '2025年 GESP 认证 Scratch 一级真题', language: 'scratch3', languageLabel: 'Scratch', level: '一级', duration: 60, totalScore: 75, passScore: 45, questionCount: 10, tag: 'GESP 认证' },
  { id: 204, title: 'ScratchJr 基础测评', language: 'scratchjr', languageLabel: 'ScratchJr', level: '基础', duration: 45, totalScore: 70, passScore: 42, questionCount: 9, tag: '基础测评' },
  { id: 205, title: 'Python Turtle 绘图基础测试', language: 'pythonturtle', languageLabel: 'Python Turtle', level: '基础', duration: 45, totalScore: 80, passScore: 48, questionCount: 11, tag: '基础测评' },
  { id: 206, title: 'GoC 图形编程一级题目', language: 'goc', languageLabel: 'GoC', level: '一级', duration: 45, totalScore: 80, passScore: 48, questionCount: 11, tag: '基础测评' },
  { id: 207, title: '2025年 GESP 认证 Python 二级真题', language: 'python', languageLabel: 'Python', level: '二级', duration: 60, totalScore: 85, passScore: 51, questionCount: 11, tag: 'GESP 认证' },
  { id: 208, title: '2025年 GESP 认证 C++ 二级真题', language: 'cpp', languageLabel: 'C++', level: '二级', duration: 60, totalScore: 85, passScore: 51, questionCount: 11, tag: 'GESP 认证' },
  { id: 209, title: '2025年 GESP 认证 Scratch 二级真题', language: 'scratch3', languageLabel: 'Scratch', level: '二级', duration: 60, totalScore: 75, passScore: 45, questionCount: 10, tag: 'GESP 认证' },
  { id: 210, title: 'ScratchJr 基础进阶测评', language: 'scratchjr', languageLabel: 'ScratchJr', level: '进阶', duration: 45, totalScore: 70, passScore: 42, questionCount: 9, tag: '进阶测评' },
  { id: 211, title: 'Python Turtle 绘图进阶测试', language: 'pythonturtle', languageLabel: 'Python Turtle', level: '进阶', duration: 45, totalScore: 80, passScore: 48, questionCount: 11, tag: '进阶测评' },
  { id: 212, title: 'GoC 图形编程进阶题目', language: 'goc', languageLabel: 'GoC', level: '进阶', duration: 45, totalScore: 80, passScore: 48, questionCount: 10, tag: '进阶测评' },
  { id: 213, title: '2025年 GESP 认证 Python 三级真题', language: 'python', languageLabel: 'Python', level: '三级', duration: 60, totalScore: 85, passScore: 51, questionCount: 11, tag: 'GESP 认证' },
  { id: 214, title: '2025年 GESP 认证 C++ 三级真题', language: 'cpp', languageLabel: 'C++', level: '三级', duration: 60, totalScore: 85, passScore: 51, questionCount: 11, tag: 'GESP 认证' },
  { id: 215, title: '2025年 GESP 认证 Scratch 三级真题', language: 'scratch3', languageLabel: 'Scratch', level: '三级', duration: 60, totalScore: 75, passScore: 45, questionCount: 10, tag: 'GESP 认证' },
  { id: 216, title: 'ScratchJr 综合测评', language: 'scratchjr', languageLabel: 'ScratchJr', level: '综合', duration: 45, totalScore: 70, passScore: 42, questionCount: 9, tag: '综合测评' },
  { id: 217, title: 'Python Turtle 绘图综合测试', language: 'pythonturtle', languageLabel: 'Python Turtle', level: '综合', duration: 45, totalScore: 80, passScore: 48, questionCount: 11, tag: '综合测评' },
  { id: 218, title: 'GoC 图形编程综合题目', language: 'goc', languageLabel: 'GoC', level: '综合', duration: 45, totalScore: 80, passScore: 48, questionCount: 10, tag: '综合测评' },
]

/** 题库中心试卷 id → 题集 */
const paperQuestionMap: Record<number, ExamQuestion[]> = {
  201: pythonSet,
  202: cppSet,
  203: scratchSet,
  204: scratchJrSet,
  205: turtleSet,
  206: gocSet,
  207: python2Set,
  208: cpp2Set,
  209: scratch2Set,
  210: scratchJr2Set,
  211: turtle2Set,
  212: goc2Set,
  213: python3Set,
  214: cpp3Set,
  215: scratch3Set,
  216: scratchJr3Set,
  217: turtle3Set,
  218: goc3Set,
}

/** 获取指定考试的题集（带兜底，支持考试 id 与题库中心试卷 id） */
export function getExamQuestions(examId: number): ExamQuestion[] {
  return examQuestionsMap[examId] || paperQuestionMap[examId] || fallbackSet
}

/** 将题集按题型分节 */
export function groupQuestionsByType(questions: ExamQuestion[]): { type: QuestionType; label: string; items: ExamQuestion[] }[] {
  const buckets: Record<string, ExamQuestion[]> = {}
  for (const q of questions) {
    ;(buckets[q.type] ||= []).push(q)
  }
  return QUESTION_TYPE_ORDER
    .filter((t) => buckets[t]?.length)
    .map((t) => ({ type: t, label: QUESTION_TYPE_LABEL[t], items: buckets[t] }))
}

/** 计算试卷总分 */
export function calcTotalScore(questions: ExamQuestion[]): number {
  return questions.reduce((sum, q) => sum + (q.score || 0), 0)
}

/** 考试元信息（标题 / 时长 / 及格分），供做题页信息条使用 */
export interface ExamMeta {
  id: number
  title: string
  duration: number // 分钟
  passScore: number // 及格分
}

export const examMetaMap: Record<number, ExamMeta> = {
  1: { id: 1, title: 'Python 算法挑战赛', duration: 120, passScore: 60 },
  2: { id: 2, title: 'Python Turtle 创意编程大赛', duration: 150, passScore: 60 },
  3: { id: 3, title: 'Scratch 图形化编程大赛', duration: 90, passScore: 60 },
  4: { id: 4, title: 'GoC 图形编程邀请赛', duration: 120, passScore: 60 },
  5: { id: 5, title: 'C++ 信息学奥赛（CSP-J/S）', duration: 240, passScore: 60 },
  6: { id: 6, title: '蓝桥杯多语言编程大赛', duration: 240, passScore: 60 },
  7: { id: 7, title: 'AI 人工智能创新大赛', duration: 180, passScore: 60 },
  8: { id: 8, title: 'C++ 算法精英赛', duration: 180, passScore: 60 },
  101: { id: 101, title: 'Python 编程体验赛', duration: 60, passScore: 30 },
  102: { id: 102, title: 'Scratch 创意体验赛', duration: 45, passScore: 24 },
  103: { id: 103, title: 'C++ 算法体验赛', duration: 60, passScore: 30 },
  104: { id: 104, title: 'GoC 图形编程体验赛', duration: 45, passScore: 24 },
  105: { id: 105, title: 'AI 人工智能体验赛', duration: 60, passScore: 30 },
}

/** 获取考试元信息（带兜底，支持考试 id 与题库中心试卷 id） */
export function getExamMeta(examId: number): ExamMeta {
  if (examMetaMap[examId]) return examMetaMap[examId]
  const paper = examPaperList.find((p) => p.id === examId)
  if (paper) {
    return { id: paper.id, title: paper.title, duration: paper.duration, passScore: paper.passScore }
  }
  return { id: examId, title: '在线考试', duration: 60, passScore: 60 }
}
