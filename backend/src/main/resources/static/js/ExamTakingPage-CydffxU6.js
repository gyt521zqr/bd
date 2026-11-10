import{d as re,j as ce,q as de,a6 as ue,c as v,a as n,R as j,S as C,$ as o,a0 as s,W as i,_ as f,J as G,ae as Q,aB as pe,P as A,Z as B,p as S,f as h,aj as T,o as r,y,aC as ve}from"./vue-vendor-F7EUy-sH.js";import{h as he,_ as _e}from"./index-CN52DKLq.js";import{a as me,E as ge}from"./element-plus-D557MRrU.js";import{a7 as fe,aC as ye,o as R,aD as Ce,m as Se,n as ke}from"./icons-B5DQmJk2.js";import{B as be,L as we}from"./BaseEditor-bP_Abo0K.js";const xe={class:"exam-taking-page"},Te={class:"top-progress-bar"},Pe={class:"progress-track"},Ee={class:"progress-info"},Ae={class:"progress-text"},Be={class:"exam-main"},Ne={class:"question-nav"},$e={class:"nav-title"},Ie={class:"nav-grid"},Me=["onClick"],Oe={class:"nav-number"},Ve={key:0,class:"nav-status"},Je={key:1,class:"nav-status marked-dot"},Le={class:"nav-stats"},ze={class:"stats-bar"},De={class:"stats-text"},je={class:"question-content"},Ge={class:"question-header"},Qe={class:"header-left"},Re={class:"question-number"},qe={class:"header-right"},Fe={class:"question-score"},He={class:"question-body"},Ue={class:"question-title"},We={key:0,class:"choice-options"},Ze=["onClick"],Ke={class:"option-radio"},Xe={class:"option-label"},Ye={class:"option-text"},et={key:1,class:"fill-input"},tt={key:2,class:"code-area"},nt={class:"question-nav-buttons"},at=re({__name:"ExamTakingPage",setup(st){const N=ve(),m=he(),q=S(),g=S(3600);let P=null;const F=h(()=>{const e=Math.floor(g.value/3600),t=Math.floor(g.value%3600/60),p=g.value%60;return e>0?`${String(e).padStart(2,"0")}:${String(t).padStart(2,"0")}:${String(p).padStart(2,"0")}`:`${String(t).padStart(2,"0")}:${String(p).padStart(2,"0")}`}),H={101:[{id:1011,type:"choice",title:"ScratchJr 中，小猫角色默认会做什么动作？",options:["走路","跳舞","翻跟头","唱歌"],score:5},{id:1012,type:"choice",title:"在 ScratchJr 中，如何让角色向右移动？",options:['添加"向右移动"积木块',"修改角色颜色","放大角色","添加声音"],score:5},{id:1013,type:"choice",title:'ScratchJr 的"绿旗"积木块的作用是什么？',options:["开始运行程序","停止程序","重复执行","播放声音"],score:5},{id:1014,type:"choice",title:"以下哪个不是 ScratchJr 的编程积木类别？",options:["触发","外观","控制","数据库"],score:5},{id:1015,type:"scratch",title:`创意挑战：使用 ScratchJr 制作一个"小猫捉迷藏"动画

要求：
1. 小猫从舞台左侧走到右侧
2. 走到右侧后隐藏起来
3. 等待2秒后重新出现在左侧
4. 循环执行以上动作

提示：使用"移动""隐藏""等待""显示"积木块`,language:"scratchjr",score:30}],102:[{id:1021,type:"choice",title:'Scratch 中，"重复执行10次"积木属于哪个类别？',options:["控制","事件","运动","外观"],score:5},{id:1022,type:"choice",title:"以下哪个积木可以让角色说出一段文字？",options:['说"你好"',"播放声音","播放动画","移动10步"],score:5},{id:1023,type:"choice",title:'Scratch 变量"得分"初始值为0，执行"将得分增加1"3次后，得分等于？',options:["3","0","1","2"],score:5},{id:1024,type:"choice",title:'在 Scratch 中，"如果...那么...否则"积木属于哪种结构？',options:["条件判断","循环","事件","函数"],score:5},{id:1025,type:"scratch",title:`编程挑战：制作一个"接苹果"小游戏

要求：
1. 创建一个篮子角色（用角色绘制）
2. 苹果从舞台顶部随机位置掉落
3. 用键盘左右方向键控制篮子移动
4. 篮子接到苹果时得分+1，并显示在舞台上
5. 苹果掉到底部后消失，新苹果从顶部掉落
6. 游戏时间60秒，倒计时结束后显示最终得分

提示：使用"x坐标""y坐标""碰到边缘""变量""随机数"等积木`,language:"scratch3",score:30}],103:[{id:1031,type:"choice",title:"Python 中，以下哪个关键字用于定义函数？",options:["def","func","function","define"],score:5},{id:1032,type:"choice",title:'Python 表达式 `len("hello")` 的结果是？',options:["5","4","6","报错"],score:5},{id:1033,type:"fill",title:"Python 中，用于输出内容到控制台的内置函数名是____（两个英文单词）",score:5},{id:1034,type:"choice",title:'以下 Python 代码的输出结果是什么？\n```\nfor i in range(3):\n    print(i, end=" ")\n```',options:["0 1 2","0 1 2 ","1 2 3","0 1 2 3"],score:5},{id:1035,type:"code",title:`编程题：编写一个函数 \`is_palindrome(s)\`，判断字符串是否为回文串

回文串是指正读和反读都相同的字符串，如 "abcba"、"12321"。

要求：
- 函数接收一个字符串参数 s
- 返回 True 如果是回文，否则返回 False
- 不区分大小写

示例：
- is_palindrome("abcba") -> True
- is_palindrome("Hello") -> False
- is_palindrome("12321") -> True`,language:"python",score:30,templateCode:`def is_palindrome(s):
    """判断字符串是否为回文串"""
    # 请在下方编写你的代码
    
`}],104:[{id:1041,type:"choice",title:"Python Turtle 中，让海龟前进100步的命令是？",options:["turtle.forward(100)","turtle.move(100)","turtle.go(100)","turtle.step(100)"],score:5},{id:1042,type:"choice",title:'turtle.pencolor("red") 的作用是？',options:["设置画笔颜色为红色","填充颜色为红色","改变背景色","改变海龟颜色"],score:5},{id:1043,type:"choice",title:"turtle.right(90) 的作用是？",options:["海龟向右转90度","海龟移动到右边","画一条向右的线","海龟速度加快"],score:5},{id:1044,type:"fill",title:"Python Turtle 中，抬起画笔（移动时不画线）的方法名是 turtle.____（两个英文单词）",score:5},{id:1045,type:"code",title:`绘图挑战：使用 Python Turtle 绘制一个彩色正方形螺旋图案

要求：
1. 绘制一个由多个正方形组成的螺旋图案
2. 每个正方形比前一个稍大
3. 每个正方形使用不同的颜色
4. 至少绘制10个正方形
5. 最终形成一个美观的螺旋效果

提示：
- 使用 for 循环控制正方形数量
- 每次循环增加边长
- 使用 turtle.pencolor() 切换颜色
- 颜色列表：["red", "orange", "yellow", "green", "blue", "purple", "pink", "cyan", "magenta", "gold"]`,language:"python",score:30,templateCode:`import turtle

# 设置画布
screen = turtle.Screen()
screen.bgcolor("black")
screen.title("彩色正方形螺旋")

t = turtle.Turtle()
t.speed(5)

# 颜色列表
colors = ["red", "orange", "yellow", "green", "blue", 
          "purple", "pink", "cyan", "magenta", "gold"]

# 请在下方编写你的代码


turtle.done()
`}],105:[{id:1051,type:"choice",title:"C++ 中，以下哪个头文件用于输入输出？",options:["#include <iostream>","#include <stdio>","#include <math>","#include <string>"],score:5},{id:1052,type:"choice",title:"C++ 中 `cout << 5 + 3 << endl;` 的输出是？",options:["8","53","5 + 3","编译错误"],score:5},{id:1053,type:"fill",title:"C++ 中，声明一个整型变量并赋值为10的语句是：int ____ = 10;（变量名自取）",score:5},{id:1054,type:"choice",title:`以下 C++ 代码的输出结果是什么？
\`\`\`
int sum = 0;
for (int i = 1; i <= 5; i++) {
    if (i % 2 == 0) sum += i;
}
cout << sum << endl;
\`\`\``,options:["6","10","15","4"],score:5},{id:1055,type:"code",title:`编程题：编写一个 C++ 程序，实现冒泡排序

要求：
- 从键盘输入 N 个整数（N <= 100）
- 使用冒泡排序算法将它们从小到大排序
- 输出排序后的结果

示例输入：
5
3 1 4 1 5

示例输出：
1 1 3 4 5`,language:"cpp",score:30,templateCode:`#include <iostream>
using namespace std;

int main() {
    int n;
    cin >> n;
    
    int arr[100];
    for (int i = 0; i < n; i++) {
        cin >> arr[i];
    }
    
    // 请在下方编写冒泡排序代码
    
    // 输出排序结果
    for (int i = 0; i < n; i++) {
        cout << arr[i];
        if (i < n - 1) cout << " ";
    }
    cout << endl;
    
    return 0;
}
`}]},u=h(()=>{const e=Number(N.params.examId);return H[e]||[{id:1,type:"choice",title:"Python中以下哪个是正确的变量名？",options:["2name","my_var","class","for"],score:4},{id:2,type:"fill",title:"Python中用于输出的内置函数是____",score:4},{id:3,type:"code",title:"编写一个函数，实现两数交换",language:"python",score:10,templateCode:`# 在这里编写代码
def swap(a, b):
    # TODO
    pass
`},{id:4,type:"code",title:"使用 C++ 输出 Hello World",language:"cpp",score:10,templateCode:`#include <iostream>
using namespace std;

int main() {
    // TODO
    return 0;
}
`},{id:5,type:"code",title:"使用 GoC 画一个等边三角形",language:"goc",score:10,templateCode:`#include <graphics.h>
int main() {
    initGraph(800, 600);
    // TODO: 画等边三角形
    closeGraph();
    return 0;
}
`}]}),d=S(0),_=S(""),k=S(new Set),a=h(()=>u.value[d.value]),U=h(()=>{var t;return{choice:"选择题",fill:"填空题",code:"编程题",scratch:"Scratch题",scratchjr:"ScratchJr题"}[(t=a.value)==null?void 0:t.type]||""}),W=h(()=>{var t;return{choice:"info",fill:"info",code:"warning",scratch:"warning"}[(t=a.value)==null?void 0:t.type]||"info"}),Z=h(()=>{var e;return(e=a.value)!=null&&e.language&&we[a.value.language]||""}),K=h(()=>{var e,t;return((e=a.value)==null?void 0:e.type)==="code"||((t=a.value)==null?void 0:t.type)==="scratch"}),b=h(()=>u.value.filter(e=>m.getAnswer(e.id)).length),E=h(()=>u.value.length===0?0:Math.round(b.value/u.value.length*100)),X=h(()=>{const e=a.value;return{lang:(e==null?void 0:e.language)||"python",mode:"exam",questionId:(e==null?void 0:e.id)||0,examId:Number(N.params.examId),readOnly:!1,templateCode:(e==null?void 0:e.templateCode)||""}});ce(d,()=>{const e=a.value,t=m.getAnswer(e.id);_.value=(t==null?void 0:t.toString())||(e==null?void 0:e.templateCode)||""});function $(e){return!!m.getAnswer(e)}function w(e){return e?k.value.has(e):!1}function Y(e){e&&(k.value.has(e)?k.value.delete(e):k.value.add(e))}function ee(e){_.value=e,I()}function I(){m.saveAnswer(a.value.id,_.value)}function te(e){_.value=e,m.saveAnswer(a.value.id,e)}function ne(){}function ae(){d.value>0&&d.value--}function se(){d.value<u.value.length-1&&d.value++}async function oe(){try{await me.confirm(`您已答完 ${b.value}/${u.value.length} 题，确定要提交试卷吗？`,"提交确认",{confirmButtonText:"确定提交",cancelButtonText:"继续答题",type:"warning"}),ge.success("试卷提交成功！")}catch{}}return m.started||(m.remainingTime=60*60,m.started=!0),de(()=>{P=setInterval(()=>{g.value>0&&g.value--},1e3)}),ue(()=>{P&&clearInterval(P)}),(e,t)=>{var O,V,J,L,z,D;const p=T("el-icon"),M=T("el-tag"),x=T("el-button"),le=T("el-input");return r(),v("div",xe,[n("div",Te,[n("div",Pe,[n("div",{class:C(["progress-fill",{"progress-complete":E.value>=100}]),style:j({width:E.value+"%"})},null,6)]),n("div",Ee,[n("span",Ae,"答题进度 "+o(b.value)+"/"+o(u.value.length),1),n("div",{class:C(["countdown-timer",{"countdown-warning":g.value<=300,"countdown-danger":g.value<=60}])},[s(p,null,{default:i(()=>[s(y(fe))]),_:1}),n("span",null,o(F.value),1)],2)])]),n("div",Be,[n("div",Ne,[n("div",$e,[s(p,null,{default:i(()=>[s(y(ye))]),_:1}),t[2]||(t[2]=f(" 题目导航 ",-1))]),n("div",Ie,[(r(!0),v(G,null,Q(u.value,(l,c)=>(r(),v("div",{key:l.id,class:C(["nav-item",{active:d.value===c,answered:$(l.id),marked:w(l.id)}]),onClick:ie=>d.value=c},[n("span",Oe,o(c+1),1),$(l.id)?(r(),v("span",Ve,[s(p,null,{default:i(()=>[s(y(R))]),_:1})])):w(l.id)?(r(),v("span",Je)):B("",!0)],10,Me))),128))]),t[3]||(t[3]=pe('<div class="nav-legend" data-v-26ed35e9><div class="legend-item" data-v-26ed35e9><span class="legend-dot answered" data-v-26ed35e9></span><span data-v-26ed35e9>已答</span></div><div class="legend-item" data-v-26ed35e9><span class="legend-dot marked" data-v-26ed35e9></span><span data-v-26ed35e9>标记</span></div><div class="legend-item" data-v-26ed35e9><span class="legend-dot unanswered" data-v-26ed35e9></span><span data-v-26ed35e9>未答</span></div></div>',1)),n("div",Le,[n("div",ze,[n("div",{class:"stats-fill",style:j({width:E.value+"%"})},null,4)]),n("span",De,"已答 "+o(b.value)+"/"+o(u.value.length),1)])]),n("div",je,[n("div",Ge,[n("div",Qe,[n("span",Re,"第 "+o(d.value+1)+" 题",1),s(M,{size:"small",type:W.value,class:"type-tag"},{default:i(()=>[f(o(U.value),1)]),_:1},8,["type"]),(O=a.value)!=null&&O.language?(r(),A(M,{key:0,size:"small",type:"info",class:"lang-tag"},{default:i(()=>[f(o(Z.value),1)]),_:1})):B("",!0)]),n("div",qe,[n("span",Fe,o((V=a.value)==null?void 0:V.score)+"分",1),s(x,{text:"",size:"small",class:"mark-btn",type:w((J=a.value)==null?void 0:J.id)?"warning":"info",onClick:t[0]||(t[0]=l=>{var c;return Y((c=a.value)==null?void 0:c.id)})},{default:i(()=>{var l;return[s(p,null,{default:i(()=>[s(y(Ce))]),_:1}),f(" "+o(w((l=a.value)==null?void 0:l.id)?"取消标记":"标记题目"),1)]}),_:1},8,["type"])])]),n("div",He,[n("h3",Ue,o((L=a.value)==null?void 0:L.title),1),((z=a.value)==null?void 0:z.type)==="choice"?(r(),v("div",We,[(r(!0),v(G,null,Q(a.value.options,(l,c)=>(r(),v("div",{key:c,class:C(["option-item",{selected:_.value===String.fromCharCode(65+c)}]),onClick:ie=>ee(String.fromCharCode(65+c))},[n("div",Ke,[n("div",{class:C(["radio-inner",{checked:_.value===String.fromCharCode(65+c)}])},null,2)]),n("span",Xe,o(String.fromCharCode(65+c)),1),n("span",Ye,o(l),1)],10,Ze))),128))])):((D=a.value)==null?void 0:D.type)==="fill"?(r(),v("div",et,[s(le,{modelValue:_.value,"onUpdate:modelValue":t[1]||(t[1]=l=>_.value=l),placeholder:"请输入答案",size:"large",onInput:I,class:"fill-answer-input"},null,8,["modelValue"])])):K.value?(r(),v("div",tt,[s(be,{ref_key:"baseEditorRef",ref:q,config:X.value,"initial-code":_.value,"editor-height":300,onCodeChange:te,onSubmitSuccess:ne},null,8,["config","initial-code"])])):B("",!0)]),n("div",nt,[s(x,{onClick:ae,disabled:d.value===0,class:"nav-btn"},{default:i(()=>[s(p,null,{default:i(()=>[s(y(Se))]),_:1}),t[4]||(t[4]=f(" 上一题 ",-1))]),_:1},8,["disabled"]),d.value<u.value.length-1?(r(),A(x,{key:0,type:"primary",onClick:se,class:"nav-btn next-btn"},{default:i(()=>[t[5]||(t[5]=f(" 下一题 ",-1)),s(p,null,{default:i(()=>[s(y(ke))]),_:1})]),_:1})):(r(),A(x,{key:1,type:"success",onClick:oe,class:"nav-btn submit-btn"},{default:i(()=>[s(p,null,{default:i(()=>[s(y(R))]),_:1}),t[6]||(t[6]=f(" 提交试卷 ",-1))]),_:1}))])])])])}}}),dt=_e(at,[["__scopeId","data-v-26ed35e9"]]);export{dt as default};
