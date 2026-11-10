/**
 * turtle.h - GoC 海龟绘图库标准头文件
 * API名称、坐标系、旋转规则严格1:1对齐原版GoC
 *
 * 坐标系规则：
 *   - 初始海龟朝向向右，角度0°
 *   - rt() 顺时针旋转（正角度增加）
 *   - lt() 逆时针旋转（正角度减少）
 *   - 禁止使用数学标准逆时针坐标系
 *
 * 海龟API接口：
 *   fd(distance)        前进指定距离
 *   bk(distance)        后退指定距离
 *   rt(angle)           顺时针旋转指定角度
 *   lt(angle)           逆时针旋转指定角度
 *   pencolor(color)     设置画笔颜色（支持颜色名、十六进制、RGB）
 *   pensize(width)      设置画笔粗细
 *   penup()             抬笔（移动不绘制）
 *   pendown()           落笔（移动绘制）
 *   speed(n)            设置动画速度(0-10)
 *   circle(radius)      画圆
 *   hideturtle()        隐藏海龟
 *   showturtle()        显示海龟
 *
 * 扩展接口：
 *   forward(distance)   同 fd()
 *   backward(distance)  同 bk()
 *   right(angle)        同 rt()
 *   left(angle)         同 lt()
 *   pu()                同 penup()
 *   pd()                同 pendown()
 *   goto(x, y)          移动到指定坐标
 *   home()              回到原点
 *   dot()               画点
 *   text(str)           画文字
 *   clearscreen()       清空画布
 *
 * 颜色常量：
 *   RED, GREEN, BLUE, BLACK, WHITE, YELLOW, CYAN, MAGENTA,
 *   ORANGE, PURPLE, PINK, BROWN, GRAY, GOLD, NAVY
 */

#ifndef TURTLE_H
#define TURTLE_H

// 海龟绘图API
void fd(double distance);
void bk(double distance);
void rt(double angle);
void lt(double angle);
void pencolor(const char* color);
void pencolor(int r, int g, int b);
void pensize(int width);
void penup();
void pendown();
void speed(int n);
void circle(double radius);
void hideturtle();
void showturtle();

// 扩展API
void forward(double distance);
void backward(double distance);
void right(double angle);
void left(double angle);
void goto(double x, double y);
void home();
void dot();
void text(const char* str);
void clearscreen();

// GoC 兼容函数
void initGraph(int width, int height);
void closeGraph();
void pause(int ms);
void setTextColor(const char* color);
void setBackgroundColor(const char* color);
void clearText();
void fillRect(double x, double y, double w, double h);
void drawRect(double x, double y, double w, double h);
void line(double x1, double y1, double x2, double y2);

// GoC 原生命令（与「常见命令」面板一致）
void pen(int colorIndex);        // 画笔颜色 0-15
void pen(const char* color);     // 画笔颜色（名称 / #hex）
void pen(int r, int g, int b);   // 画笔颜色（RGB）
void ct(int colorIndex);         // 设置颜色
void size(int width);            // 画笔粗细
void up();                       // 抬笔
void down();                     // 落笔
void oc(double radius);          // 空心圆
void oo(double radius);          // 实心圆
void q(double rx, double ry);    // 空心椭圆
void ee(double rx, double ry);   // 实心椭圆
void rl(double w, double h);     // 空心长方形
void rr(double w, double h);     // 实心长方形
void moveTo(double x, double y); // 移动到坐标（不画线）
void lineTo(double x, double y); // 连线到坐标
void wait(int ms);               // 等待
void sound(int freq, int ms);    // 播放声音
int  ctime();                    // 运行耗时(ms)
void cinWin();                   // 输入窗口
void coutWin();                  // 输出窗口
void pk(int index);              // 图片（浏览器版暂不支持）

// 数学函数
int rand(int min, int max);
double abs(double n);
double sqrt(double n);
double pow(double base, double exp);
double sin(double deg);
double cos(double deg);
double tan(double deg);
int round(double n);
int floor(double n);
int ceil(double n);

// 输入函数（从底部 cinWin 输入区读取）
int inputInt();
double inputFloat();
const char* inputString();

// repeat 宏（GoC 简化循环语法）
#define repeat(n) for(int _rpt_i = 0; _rpt_i < (n); _rpt_i++)

// 颜色常量
#define RED     "#FF0000"
#define GREEN   "#00AA00"
#define BLUE    "#0000FF"
#define BLACK   "#000000"
#define WHITE   "#FFFFFF"
#define YELLOW  "#FFFF00"
#define CYAN    "#00FFFF"
#define MAGENTA "#FF00FF"
#define ORANGE  "#FFA500"
#define PURPLE  "#800080"
#define PINK    "#FFC0CB"
#define BROWN   "#8B4513"
#define GRAY    "#808080"
#define GOLD    "#FFD700"
#define NAVY    "#000080"

#endif // TURTLE_H
