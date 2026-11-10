"use strict";
var __defProp = Object.defineProperty;
var __getOwnPropDesc = Object.getOwnPropertyDescriptor;
var __getOwnPropNames = Object.getOwnPropertyNames;
var __hasOwnProp = Object.prototype.hasOwnProperty;
var __export = (target, all) => {
  for (var name in all)
    __defProp(target, name, { get: all[name], enumerable: true });
};
var __copyProps = (to, from, except, desc) => {
  if (from && typeof from === "object" || typeof from === "function") {
    for (let key of __getOwnPropNames(from))
      if (!__hasOwnProp.call(to, key) && key !== except)
        __defProp(to, key, { get: () => from[key], enumerable: !(desc = __getOwnPropDesc(from, key)) || desc.enumerable });
  }
  return to;
};
var __toCommonJS = (mod) => __copyProps(__defProp({}, "__esModule", { value: true }), mod);

// frontend/src/mock/examQuestions.ts
var examQuestions_exports = {};
__export(examQuestions_exports, {
  QUESTION_TYPE_LABEL: () => QUESTION_TYPE_LABEL,
  QUESTION_TYPE_ORDER: () => QUESTION_TYPE_ORDER,
  calcTotalScore: () => calcTotalScore,
  examMetaMap: () => examMetaMap,
  examPaperList: () => examPaperList,
  examQuestionsMap: () => examQuestionsMap,
  getExamMeta: () => getExamMeta,
  getExamQuestions: () => getExamQuestions,
  groupQuestionsByType: () => groupQuestionsByType,
  scratchJrSet: () => scratchJrSet
});
module.exports = __toCommonJS(examQuestions_exports);
var QUESTION_TYPE_LABEL = {
  choice: "\u9009\u62E9\u9898",
  fill: "\u586B\u7A7A\u9898",
  judge: "\u5224\u65AD\u9898",
  code: "\u7F16\u7A0B\u9898",
  scratch: "Scratch\u9898",
  scratchjr: "ScratchJr\u9898"
};
var QUESTION_TYPE_ORDER = [
  "choice",
  "fill",
  "judge",
  "code",
  "scratch",
  "scratchjr"
];
var pythonSet = [
  { id: 1031, type: "choice", title: "\u4E0B\u5217\u54EA\u4E2A\u8F6F\u4EF6\u4E0D\u80FD\u8FD0\u884C Python \u7A0B\u5E8F\uFF1F\uFF08\u3000\uFF09", options: ["Jupyter Notebook", "Pycharm", "\u539F\u7248\u7684 Scratch", "IDLE"], answer: "C", analysis: "\u539F\u7248 Scratch \u662F\u56FE\u5F62\u5316\u7F16\u7A0B\u8F6F\u4EF6\uFF0C\u4E0D\u662F Python \u7F16\u7A0B\u8F6F\u4EF6\u3002", score: 5 },
  { id: 1032, type: "choice", title: "\u4E0B\u9762 print \u8BED\u53E5\uFF0C\u54EA\u4E00\u4E2A\u662F\u6B63\u786E\u7684\u7528\u6CD5\uFF1F\uFF08\u3000\uFF09", options: ["print \u9F99\u817E\u864E\u8DC3", "print(\u9F99\u817E\u864E\u8DC3)", "print('\u9F99\u817E\u864E\u8DC3')", "print'\u9F99\u817E\u864E\u8DC3'"], answer: "C", analysis: "print \u662F\u51FD\u6570\uFF0C\u5FC5\u987B\u5E26\u62EC\u53F7\uFF1B\u5B57\u7B26\u4E32\u5185\u5BB9\u9700\u8981\u7528\u5F15\u53F7\u5305\u88F9\u3002", score: 5 },
  { id: 1033, type: "choice", title: "\u5728 Python \u4E2D\uFF0C\u4E0B\u5217\u54EA\u4E00\u6761\u8BED\u53E5\u53EF\u4EE5\u6539\u53D8\u753B\u7B14\u5F53\u524D\u4F4D\u7F6E\uFF1F\uFF08\u3000\uFF09", options: ["dot(100)", "setheading(100)", "left(100)", "goto(100,0)"], answer: "D", analysis: "dot \u753B\u5B9E\u5FC3\u5706\u3001setheading \u8BBE\u7F6E\u65B9\u5411\u3001left \u5DE6\u8F6C\u90FD\u4E0D\u6539\u53D8\u4F4D\u7F6E\uFF1Bgoto(100,0) \u8BA9\u753B\u7B14\u79FB\u52A8\u5230\u5750\u6807 (100,0)\u3002", score: 5 },
  { id: 1034, type: "choice", title: "\u4E0B\u5217\u54EA\u4E00\u4E2A\u53EF\u4EE5\u8FDB\u884C\u6570\u5B66\u4E2D\u7684\u6C42\u4F59\u6570\u8FD0\u7B97\uFF1F\uFF08\u3000\uFF09", options: ["**", "%", "/", "//"], answer: "B", analysis: "** \u662F\u5E42\u8FD0\u7B97\uFF0C% \u662F\u53D6\u4F59\uFF0C/ \u662F\u9664\u6CD5\uFF0C// \u662F\u6574\u9664\u3002", score: 5 },
  { id: 1035, type: "choice", title: "\u8868\u8FBE\u5F0F 15>=8 \u7684\u8FD0\u7B97\u7ED3\u679C\u662F\uFF1F\uFF08\u3000\uFF09", options: ["15", "8", "False", "True"], answer: "D", analysis: "15>=8 \u6210\u7ACB\uFF0C\u6BD4\u8F83\u8FD0\u7B97\u7ED3\u679C\u4E3A\u5E03\u5C14\u503C True\u3002", score: 5 },
  { id: 1036, type: "choice", title: "\u6267\u884C\u8BED\u53E5 print(10==10.0) \u7684\u7ED3\u679C\u4E3A\uFF1F\uFF08\u3000\uFF09", options: ["10", "10.0", "True", "False"], answer: "C", analysis: "10 \u4E0E 10.0 \u6570\u503C\u76F8\u7B49\uFF0C\u6BD4\u8F83\u8FD0\u7B97\u7ED3\u679C\u4E3A True\u3002", score: 5 },
  { id: 1037, type: "choice", title: "\u6267\u884C\u8BED\u53E5 y=4**3 \u540E\uFF0C\u53D8\u91CF y \u7684\u503C\u4E3A\uFF1F\uFF08\u3000\uFF09", options: ["0", "12", "64", "81"], answer: "C", analysis: "** \u662F\u5E42\u8FD0\u7B97\uFF0C4**3 = 4\xD74\xD74 = 64\u3002", score: 5 },
  { id: 1038, type: "choice", title: "Python \u7F16\u7A0B\u8BED\u8A00\u7684\u6CE8\u91CA\u8BED\u53E5\u4EE5\uFF08\u3000\uFF09\u5F00\u5934\uFF1F", options: ["'", "//", "#", "{"], answer: "C", analysis: "Python \u5355\u884C\u6CE8\u91CA\u4EE5 # \u5F00\u5934\u3002// \u662F C++/Java \u7684\u6CE8\u91CA\u3002", score: 5 },
  { id: 1039, type: "choice", title: "\u6267\u884C print(12.34-1.34) \u7684\u8F93\u51FA\u7ED3\u679C\u662F\uFF1F\uFF08\u3000\uFF09", options: ["11", "11.0", "11.00", "12.34-1.34"], answer: "B", analysis: "12.34-1.34 = 11.0\uFF0C\u6D6E\u70B9\u6570\u8FD0\u7B97\u7ED3\u679C\u4E3A 11.0\u3002", score: 5 },
  { id: 1040, type: "choice", title: "\u5DF2\u77E5\u53D8\u91CF a=2\uFF0Cb=3\uFF0C\u6267\u884C\u8BED\u53E5 a%=a+b \u540E\uFF0C\u53D8\u91CF a \u7684\u503C\u4E3A\uFF1F\uFF08\u3000\uFF09", options: ["0", "2", "3", "12"], answer: "B", analysis: "a%=a+b \u5373 a = a % (a+b) = 2 % 5 = 2\u3002", score: 5 },
  { id: 1041, type: "judge", title: "\u5728 Python \u4E2D\uFF0C\u7F29\u8FDB\u7528\u6765\u8868\u793A\u4EE3\u7801\u5757\u7684\u5C42\u7EA7\u5173\u7CFB\uFF0C\u6BCF\u589E\u52A0\u4E00\u4E2A\u5C42\u5757\u5C31\u589E\u52A0\u4E00\u4E2A\u7F29\u8FDB\u3002\uFF08\u3000\uFF09", answer: "true", analysis: "Python \u4F9D\u8D56\u7F29\u8FDB\u5212\u5206\u4EE3\u7801\u5757\uFF0C\u7F29\u8FDB\u4E0D\u6B63\u786E\u4F1A\u5BFC\u81F4\u8BED\u6CD5\u9519\u8BEF\u3002", score: 5 },
  {
    id: 1042,
    type: "code",
    title: '\u7F16\u7A0B\u9898\uFF1A\u7F16\u5199\u4E00\u4E2A\u51FD\u6570 `is_palindrome(s)`\uFF0C\u5224\u65AD\u5B57\u7B26\u4E32\u662F\u5426\u4E3A\u56DE\u6587\u4E32\n\n\u56DE\u6587\u4E32\u662F\u6307\u6B63\u8BFB\u548C\u53CD\u8BFB\u90FD\u76F8\u540C\u7684\u5B57\u7B26\u4E32\uFF0C\u5982 "abcba"\u3001"12321"\u3002\n\n\u8981\u6C42\uFF1A\n- \u51FD\u6570\u63A5\u6536\u4E00\u4E2A\u5B57\u7B26\u4E32\u53C2\u6570 s\n- \u8FD4\u56DE True \u5982\u679C\u662F\u56DE\u6587\uFF0C\u5426\u5219\u8FD4\u56DE False\n- \u4E0D\u533A\u5206\u5927\u5C0F\u5199\n\n\u793A\u4F8B\uFF1A\n- is_palindrome("abcba") -> True\n- is_palindrome("Hello") -> False\n- is_palindrome("12321") -> True',
    language: "python",
    score: 30,
    templateCode: 'def is_palindrome(s):\n    """\u5224\u65AD\u5B57\u7B26\u4E32\u662F\u5426\u4E3A\u56DE\u6587\u4E32"""\n    # \u8BF7\u5728\u4E0B\u65B9\u7F16\u5199\u4F60\u7684\u4EE3\u7801\n    \n'
  }
];
var cppSet = [
  { id: 1051, type: "choice", title: "\u4E00\u4E2A\u5B8C\u6574\u7684 C++ \u7A0B\u5E8F\uFF0C\u5FC5\u987B\u5305\u542B\u4EE5\u4E0B\u54EA\u4E2A\u51FD\u6570\u4F5C\u4E3A\u7A0B\u5E8F\u6267\u884C\u7684\u5165\u53E3\uFF1F\uFF08\u3000\uFF09", options: ["main()", "\u5F00\u59CB()", "begin()", "init()"], answer: "A", analysis: "C++ \u7A0B\u5E8F\u7684\u6267\u884C\u5165\u53E3\u7531\u6807\u51C6\u89C4\u5B9A\u4E3A main() \u51FD\u6570\u3002", score: 5 },
  { id: 1052, type: "choice", title: "C++ \u6E90\u4EE3\u7801\u7ECF\u8FC7\u4EE5\u4E0B\u54EA\u4E2A\u6B65\u9AA4\u540E\uFF0C\u4F1A\u751F\u6210\u53EF\u76F4\u63A5\u5728 Windows \u7CFB\u7EDF\u8FD0\u884C\u7684 .exe \u6587\u4EF6\uFF1F\uFF08\u3000\uFF09", options: ["\u4EC5\u7F16\u5199\u4EE3\u7801", "\u4EC5\u7F16\u8BD1", "\u7F16\u8BD1+\u94FE\u63A5", "\u4EC5\u8C03\u8BD5"], answer: "C", analysis: "\u6E90\u4EE3\u7801\u9700\u7ECF\u8FC7\u7F16\u8BD1\uFF08\u751F\u6210\u76EE\u6807\u6587\u4EF6\uFF09\u548C\u94FE\u63A5\uFF08\u751F\u6210\u53EF\u6267\u884C\u6587\u4EF6\uFF09\u4E24\u6B65\u3002", score: 5 },
  { id: 1053, type: "choice", title: "\u4E00\u4E2A C++ \u6E90\u7A0B\u5E8F\u6587\u4EF6\uFF0C\u4EE5\u4E0B\u54EA\u4E2A\u540E\u7F00\u540D\u662F\u6807\u51C6\u5408\u6CD5\u7684\uFF1F\uFF08\u3000\uFF09", options: [".txt", ".cpp", ".doc", ".exe"], answer: "B", analysis: "C++ \u6E90\u6587\u4EF6\u901A\u5E38\u4F7F\u7528 .cpp \u6269\u5C55\u540D\uFF1B.exe \u662F\u53EF\u6267\u884C\u6587\u4EF6\u3002", score: 5 },
  { id: 1054, type: "choice", title: "C++ \u8868\u8FBE\u5F0F 15 - 6 / 2 * 3 \u7684\u8BA1\u7B97\u7ED3\u679C\u662F\uFF1F\uFF08\u3000\uFF09", options: ["6", "9", "14", "0"], answer: "A", analysis: "\u6309\u4F18\u5148\u7EA7\uFF1A6/2=3\uFF0C3*3=9\uFF0C15-9=6\u3002", score: 5 },
  { id: 1055, type: "choice", title: "C++ \u8868\u8FBE\u5F0F 8 + 3 * 5 % 2 \u7684\u8BA1\u7B97\u7ED3\u679C\u662F\uFF1F\uFF08\u3000\uFF09", options: ["9", "13", "55", "0"], answer: "A", analysis: "\u6309\u4F18\u5148\u7EA7\uFF1A3*5=15\uFF0C15%2=1\uFF0C8+1=9\u3002", score: 5 },
  { id: 1056, type: "choice", title: "\u4EE5\u4E0B C++ \u53D8\u91CF\u547D\u540D\u4E2D\uFF0C\u5408\u6CD5\u7684\u4E00\u9879\u662F\uFF1F\uFF08\u3000\uFF09", options: ["123num", "my-age", "int", "_score_2026"], answer: "D", analysis: "\u53D8\u91CF\u4E0D\u80FD\u4EE5\u6570\u5B57\u5F00\u5934\u3001\u4E0D\u80FD\u542B\u7279\u6B8A\u7B26\u53F7\u3001\u4E0D\u80FD\u662F\u5173\u952E\u5B57\uFF1B\u53EF\u542B\u5B57\u6BCD\u6570\u5B57\u4E0B\u5212\u7EBF\u3002", score: 5 },
  { id: 1057, type: "choice", title: "\u4EE5\u4E0B\u54EA\u4E2A\u8F6F\u4EF6\u4E0D\u80FD\u7528\u4E8E C++ \u7A0B\u5E8F\u7684\u7F16\u5199\u4E0E\u7F16\u8BD1\u8FD0\u884C\uFF1F\uFF08\u3000\uFF09", options: ["Dev-C++", "Visual Studio", "\u5FAE\u8F6F Word", "Code::Blocks"], answer: "C", analysis: "Word \u662F\u6587\u5B57\u5904\u7406\u8F6F\u4EF6\uFF0C\u4E0D\u80FD\u7F16\u8BD1\u8FD0\u884C C++ \u7A0B\u5E8F\u3002", score: 5 },
  { id: 1058, type: "judge", title: "do-while \u5FAA\u73AF\u7684\u5FAA\u73AF\u4F53\u81F3\u5C11\u4F1A\u6267\u884C\u4E00\u6B21\uFF0C\u65E0\u8BBA\u5FAA\u73AF\u6761\u4EF6\u662F\u5426\u6210\u7ACB\u3002\uFF08\u3000\uFF09", answer: "true", analysis: "do-while \u5148\u6267\u884C\u5FAA\u73AF\u4F53\u518D\u5224\u65AD\u6761\u4EF6\uFF0C\u6240\u4EE5\u81F3\u5C11\u6267\u884C\u4E00\u6B21\u3002", score: 5 },
  { id: 1059, type: "judge", title: "while \u5FAA\u73AF\u7684\u5FAA\u73AF\u4F53\u81F3\u5C11\u4F1A\u6267\u884C\u4E00\u6B21\u3002\uFF08\u3000\uFF09", answer: "false", analysis: "while \u5148\u5224\u65AD\u6761\u4EF6\u518D\u6267\u884C\u5FAA\u73AF\u4F53\uFF0C\u6761\u4EF6\u4E00\u5F00\u59CB\u4E0D\u6210\u7ACB\u5219\u4E00\u6B21\u90FD\u4E0D\u6267\u884C\u3002", score: 5 },
  { id: 1060, type: "judge", title: "\u8868\u8FBE\u5F0F !(5 > 3) \u7684\u903B\u8F91\u7ED3\u679C\u4E3A true\u3002\uFF08\u3000\uFF09", answer: "false", analysis: "5>3 \u4E3A true\uFF0C\u53D6\u53CD ! \u540E\u7ED3\u679C\u4E3A false\u3002", score: 5 },
  { id: 1061, type: "judge", title: "for(int i=0;;i++) \u662F\u4E00\u4E2A\u65E0\u9650\u5FAA\u73AF\u3002\uFF08\u3000\uFF09", answer: "true", analysis: "for \u5FAA\u73AF\u7F3A\u5C11\u4E2D\u95F4\u7684\u6761\u4EF6\u5224\u65AD\u8868\u8FBE\u5F0F\uFF0C\u6761\u4EF6\u6052\u4E3A\u771F\uFF0C\u6784\u6210\u65E0\u9650\u5FAA\u73AF\u3002", score: 5 },
  {
    id: 1062,
    type: "code",
    title: "\u7F16\u7A0B\u9898\uFF1A\u7F16\u5199\u4E00\u4E2A C++ \u7A0B\u5E8F\uFF0C\u5B9E\u73B0\u5192\u6CE1\u6392\u5E8F\n\n\u8981\u6C42\uFF1A\n- \u4ECE\u952E\u76D8\u8F93\u5165 N \u4E2A\u6574\u6570\uFF08N <= 100\uFF09\n- \u4F7F\u7528\u5192\u6CE1\u6392\u5E8F\u7B97\u6CD5\u5C06\u5B83\u4EEC\u4ECE\u5C0F\u5230\u5927\u6392\u5E8F\n- \u8F93\u51FA\u6392\u5E8F\u540E\u7684\u7ED3\u679C\n\n\u793A\u4F8B\u8F93\u5165\uFF1A\n5\n3 1 4 1 5\n\n\u793A\u4F8B\u8F93\u51FA\uFF1A\n1 1 3 4 5",
    language: "cpp",
    score: 30,
    templateCode: '#include <iostream>\nusing namespace std;\n\nint main() {\n    int n;\n    cin >> n;\n    \n    int arr[100];\n    for (int i = 0; i < n; i++) {\n        cin >> arr[i];\n    }\n    \n    // \u8BF7\u5728\u4E0B\u65B9\u7F16\u5199\u5192\u6CE1\u6392\u5E8F\u4EE3\u7801\n    \n    // \u8F93\u51FA\u6392\u5E8F\u7ED3\u679C\n    for (int i = 0; i < n; i++) {\n        cout << arr[i];\n        if (i < n - 1) cout << " ";\n    }\n    cout << endl;\n    \n    return 0;\n}\n'
  }
];
var scratchSet = [
  { id: 1021, type: "choice", title: "\u70B9\u51FB\u4E0B\u5217\u54EA\u4E2A\u9009\u9879\u53EF\u4EE5\u8FD0\u884C\u5F53\u524D\u7F16\u5199\u7684\u7A0B\u5E8F\uFF1F\uFF08\u3000\uFF09", options: ["\u505C\u6B62\u6309\u94AE", "\u7EFF\u65D7\u6309\u94AE", "\u5207\u6362\u821E\u53F0\u5168\u5C4F\u6309\u94AE", "\u4FDD\u5B58\u9879\u76EE\u6309\u94AE"], answer: "B", analysis: "Scratch \u4E2D\u7EFF\u65D7\u662F\u7A0B\u5E8F\u8FD0\u884C\u7684\u6838\u5FC3\u89E6\u53D1\u6309\u94AE\uFF1B\u505C\u6B62\u6309\u94AE\u505C\u6B62\u7A0B\u5E8F\uFF0C\u5168\u5C4F\u6309\u94AE\u5207\u6362\u663E\u793A\uFF0C\u4FDD\u5B58\u6309\u94AE\u4FDD\u5B58\u9879\u76EE\u3002", score: 5 },
  { id: 1022, type: "choice", title: '\u60F3\u8BA9\u82F9\u679C\u5728\u821E\u53F0\u4E0A\u770B\u8D77\u6765\u6BD4\u73B0\u5728\u5927\u4E00\u500D\uFF0C\u5E94\u8BE5\u5C06"\u5927\u5C0F"\u6570\u503C\u4FEE\u6539\u4E3A\u591A\u5C11\uFF1F\uFF08\u3000\uFF09', options: ["50", "200", "10", "150"], answer: "B", analysis: '\u89D2\u8272"\u5927\u5C0F"\u539F\u59CB\u5C3A\u5BF8\u662F 100\uFF0C\u5927\u4E00\u500D\u5373 2 \u500D = 200\u3002', score: 5 },
  { id: 1023, type: "choice", title: '\u9ED8\u8BA4\u5C0F\u732B\u89D2\u8272\uFF0C\u521D\u59CB\u65B9\u5411\u4E3A\u6C34\u5E73\u5411\u53F3\uFF0C\u8FD0\u884C"\u79FB\u52A8 200 \u6B65\uFF0C\u518D\u79FB\u52A8 -100 \u6B65"\u540E\uFF0C\u8BF4\u6CD5\u6B63\u786E\u7684\u662F\uFF1F\uFF08\u3000\uFF09', options: ["\u5C0F\u732B\u5148\u9762\u671D\u53F3\u79FB\u52A8\uFF0C\u518D\u9762\u671D\u5DE6\u79FB\u52A8", "\u5C0F\u732B\u9762\u671D\u53F3\u524D\u8FDB\uFF0C\u518D\u9762\u671D\u53F3\u540E\u9000", "\u5C0F\u732B\u5148\u5411\u5DE6\u79FB\u52A8\uFF0C\u518D\u5411\u53F3\u79FB\u52A8", "\u5C0F\u732B\u4E00\u5171\u5411\u53F3\u79FB\u52A8 200 \u6B65"], answer: "B", analysis: "\u9762\u5411 90 \u5EA6\u5373\u9762\u671D\u53F3\uFF0C\u79FB\u52A8 200 \u6B65\u5411\u53F3\u524D\u8FDB\uFF0C\u79FB\u52A8 -100 \u6B65\u8868\u793A\u540E\u9000 100 \u6B65\uFF0C\u9762\u5411\u4E0D\u53D8\u3002", score: 5 },
  { id: 1024, type: "choice", title: "\u89C2\u5BDF\u6570\u5B57\u5E8F\u5217\uFF1A1, 1, 2, 3, 5, 8, ?\uFF0C\u95EE\u53F7\u5904\u5E94\u586B\uFF1F\uFF08\u3000\uFF09", options: ["11", "12", "13", "14"], answer: "C", analysis: "\u89C4\u5F8B\u662F\u524D\u4E24\u4E2A\u6570\u76F8\u52A0\u7B49\u4E8E\u7B2C\u4E09\u4E2A\u6570\uFF1A5+8=13\u3002", score: 5 },
  { id: 1025, type: "choice", title: "\u505A\u65E9\u64CD\u65F6\u5C0F\u670B\u53CB\u4EEC\u5728\u6392\u961F\uFF0C\u4ECE\u524D\u9762\u6570\u5C0F\u7EA2\u662F\u7B2C 4 \u4E2A\uFF0C\u4ECE\u540E\u9762\u6570\u5C0F\u7EA2\u662F\u7B2C 3 \u4E2A\uFF0C\u961F\u4F0D\u91CC\u4E00\u5171\u6709\u591A\u5C11\u4E2A\u5C0F\u670B\u53CB\uFF1F\uFF08\u3000\uFF09", options: ["5", "6", "7", "8"], answer: "B", analysis: "\u4ECE\u524D\u9762\u6570\u5C0F\u7EA2\u7B2C 4\u3001\u4ECE\u540E\u9762\u6570\u7B2C 3\uFF0C\u5C0F\u7EA2\u88AB\u7B97\u4E86\u4E24\u6B21\uFF0C\u603B\u4EBA\u6570 4+3-1=6\u3002", score: 5 },
  { id: 1026, type: "choice", title: '\u5F53\u6267\u884C\u5230"\u505C\u6B62\u6240\u6709\u58F0\u97F3"\u79EF\u6728\u65F6\uFF0C\u4F1A\u53D1\u751F\u4EC0\u4E48\uFF1F\uFF08\u3000\uFF09', options: ["\u7A0B\u5E8F\u7ED3\u675F", "\u89D2\u8272\u58F0\u97F3\u548C\u80CC\u666F\u97F3\u4E50\u7684\u97F3\u91CF\u53D8\u4E3A 0", "\u89D2\u8272\u58F0\u97F3\u548C\u80CC\u666F\u97F3\u4E50\u5168\u90E8\u505C\u6B62\u64AD\u653E", "\u7A0B\u5E8F\u6682\u505C"], answer: "C", analysis: "\u8BE5\u79EF\u6728\u7ACB\u5373\u505C\u6B62\u6240\u6709\u6B63\u5728\u64AD\u653E\u7684\u58F0\u97F3\uFF0C\u4F46\u4E0D\u5F71\u54CD\u5176\u4ED6\u4EE3\u7801\u6267\u884C\uFF0C\u4E5F\u4E0D\u6539\u53D8\u97F3\u91CF\u3002", score: 5 },
  { id: 1027, type: "choice", title: '\u5C0F\u9E2D\u5B50\u7684\u521D\u59CB\u65B9\u5411\u662F\u9762\u5411 90 \u5EA6\uFF0C\u5C06"\u65B9\u5411"\u6570\u503C\u4FEE\u6539\u4E3A\u591A\u5C11\uFF0C\u5C0F\u9E2D\u5B50\u80FD\u9762\u5411\u6B63\u4E0B\u65B9\uFF1F\uFF08\u3000\uFF09', options: ["0", "180", "270", "-90"], answer: "B", analysis: "0\xB0 \u6B63\u4E0A\u300190\xB0 \u6B63\u53F3\u3001180\xB0 \u6B63\u4E0B\u3001-90\xB0/270\xB0 \u6B63\u5DE6\u3002", score: 5 },
  { id: 1028, type: "judge", title: "\u9690\u85CF\u89D2\u8272\u4EE5\u540E\uFF0C\u7A0B\u5E8F\u5C31\u4E0D\u518D\u6267\u884C\u3002\uFF08\u3000\uFF09", answer: "false", analysis: "\u9690\u85CF\u89D2\u8272\u53EA\u4F1A\u9690\u85CF\u5B83\u7684\u5916\u89C2\uFF0C\u4E0D\u4F1A\u505C\u6B62\u6267\u884C\u89D2\u8272\u7684\u4EE3\u7801\u3002", score: 5 },
  { id: 1029, type: "judge", title: '\u5C06\u89D2\u8272\u7684\u65CB\u8F6C\u6A21\u5F0F\u8BBE\u7F6E\u4E3A\u5DE6\u53F3\u7FFB\u8F6C\u540E\uFF0C\u6267\u884C"\u5DE6\u8F6C 90 \u5EA6"\u79EF\u6728\uFF0C\u89D2\u8272\u4F1A\u51FA\u73B0\u5012\u7ACB\u7684\u60C5\u51B5\u3002\uFF08\u3000\uFF09', answer: "false", analysis: "\u5DE6\u53F3\u7FFB\u8F6C\u6A21\u5F0F\u53EA\u4F1A\u6C34\u5E73\u65B9\u5411\u5DE6\u53F3\u7FFB\u8F6C\uFF0C\u4E0D\u4F1A\u4E0A\u4E0B\u5012\u7ACB\uFF1B\u53EA\u6709\u81EA\u7531\u65CB\u8F6C\u6A21\u5F0F\u624D\u4F1A\u5012\u7ACB\u3002", score: 5 },
  {
    id: 1030,
    type: "scratch",
    title: '\u7F16\u7A0B\u6311\u6218\uFF1A\u5236\u4F5C\u4E00\u4E2A"\u63A5\u82F9\u679C"\u5C0F\u6E38\u620F\n\n\u8981\u6C42\uFF1A\n1. \u521B\u5EFA\u4E00\u4E2A\u7BEE\u5B50\u89D2\u8272\uFF08\u7528\u89D2\u8272\u7ED8\u5236\uFF09\n2. \u82F9\u679C\u4ECE\u821E\u53F0\u9876\u90E8\u968F\u673A\u4F4D\u7F6E\u6389\u843D\n3. \u7528\u952E\u76D8\u5DE6\u53F3\u65B9\u5411\u952E\u63A7\u5236\u7BEE\u5B50\u79FB\u52A8\n4. \u7BEE\u5B50\u63A5\u5230\u82F9\u679C\u65F6\u5F97\u5206+1\uFF0C\u5E76\u663E\u793A\u5728\u821E\u53F0\u4E0A\n5. \u82F9\u679C\u6389\u5230\u5E95\u90E8\u540E\u6D88\u5931\uFF0C\u65B0\u82F9\u679C\u4ECE\u9876\u90E8\u6389\u843D\n6. \u6E38\u620F\u65F6\u95F460\u79D2\uFF0C\u5012\u8BA1\u65F6\u7ED3\u675F\u540E\u663E\u793A\u6700\u7EC8\u5F97\u5206\n\n\u63D0\u793A\uFF1A\u4F7F\u7528"x\u5750\u6807""y\u5750\u6807""\u78B0\u5230\u8FB9\u7F18""\u53D8\u91CF""\u968F\u673A\u6570"\u7B49\u79EF\u6728',
    language: "scratch3",
    score: 30
  }
];
var gocSet = [
  { id: 401, type: "choice", title: "GoC \u4E2D\u8BA9\u7B14\u540E\u9000 90 \u7684\u547D\u4EE4\u662F\uFF1F\uFF08\u3000\uFF09", options: ["rt(90)", "lt(90)", "fd(90)", "bk(90)"], answer: "D", analysis: "bk \u662F\u540E\u9000\u547D\u4EE4\uFF0Cbk(90) \u8868\u793A\u540E\u9000 90\u3002", score: 5 },
  { id: 402, type: "choice", title: "\u753B\u7B14\u6700\u521D\u7684\u65B9\u5411\u4E3A\u7AD6\u76F4\u5411\u4E0A\uFF0C\u60F3\u7ED8\u5236\u4E00\u6761\u957F\u5EA6\u4E3A 100 \u7684\u6C34\u5E73\u7EBF\uFF0C\u6B63\u786E\u4EE3\u7801\u662F\uFF1F\uFF08\u3000\uFF09", options: ["p.lt(90).fd(100)", "p.rt(90).fd(100)", "p.lt(180).fd(100)", "p.rt(180).fd(100)"], answer: "B", analysis: "\u521D\u59CB\u5411\u4E0A\uFF0C\u53F3\u8F6C 90 \u5EA6\u540E\u671D\u53F3\uFF0C\u518D\u524D\u8FDB 100 \u753B\u6C34\u5E73\u7EBF\u3002", score: 5 },
  { id: 403, type: "choice", title: "\u5173\u4E8E GoC \u547D\u4EE4\u7684\u63CF\u8FF0\uFF0C\u6B63\u786E\u7684\u662F\uFF1F\uFF08\u3000\uFF09", options: ["fd \u662F\u524D\u8FDB\u547D\u4EE4", "bk \u662F\u524D\u8FDB\u547D\u4EE4", "rt \u662F\u5DE6\u8F6C\u547D\u4EE4", "pen \u662F\u989C\u8272\u547D\u4EE4"], answer: "A", analysis: "fd \u524D\u8FDB\u3001bk \u540E\u9000\u3001rt \u53F3\u8F6C\u3001lt \u5DE6\u8F6C\u3002", score: 5 },
  { id: 404, type: "choice", title: "\u8BED\u53E5 p.rt(90) \u4E0E\u4EE5\u4E0B\u54EA\u4E2A\u8BED\u53E5\u6267\u884C\u7ED3\u679C\u4E00\u6837\uFF1F\uFF08\u3000\uFF09", options: ["p.lt(270)", "p.rt(180)", "p.rt(270)", "p.lt(90)"], answer: "A", analysis: "\u53F3\u8F6C 90 \u5EA6\u7B49\u4EF7\u4E8E\u5DE6\u8F6C 270 \u5EA6\uFF08360-90=270\uFF09\u3002", score: 5 },
  { id: 405, type: "choice", title: "\u4E0B\u5217\u662F\u62AC\u7B14\u547D\u4EE4\u7684\u662F\uFF1F\uFF08\u3000\uFF09", options: ["p.up()", "p.down()", "p.lt()", "p.rt()"], answer: "A", analysis: "up() \u662F\u62AC\u7B14\uFF0Cdown() \u662F\u843D\u7B14\u3002", score: 5 },
  { id: 406, type: "choice", title: "\u60F3\u8BA9\u753B\u7B14\u540E\u9000 100 \u6B65\uFF0C\u4E0B\u5217\u9009\u9879\u6B63\u786E\u7684\u662F\uFF1F\uFF08\u3000\uFF09", options: ["p.fd(100)", "p.bk(100)", "p.rt(100)", "p.lt(100)"], answer: "B", analysis: "bk(100) \u8868\u793A\u540E\u9000 100 \u6B65\u3002", score: 5 },
  { id: 407, type: "choice", title: "rr() \u662F\u753B\u5B9E\u5FC3\u77E9\u5F62\u547D\u4EE4\uFF0C\u753B\u8FB9\u957F 100 \u7684\u7EA2\u8272\uFF08\u989C\u8272\u53F7 1\uFF09\u6B63\u65B9\u5F62\uFF0C\u6B63\u786E\u4EE3\u7801\u662F\uFF1F\uFF08\u3000\uFF09", options: ["p.rr(100,1)", "p.rr(100,100)", "p.rr(1,100,100)", "p.rr(100,100,1)"], answer: "D", analysis: "rr(\u5BBD,\u9AD8,\u989C\u8272\u53F7)\uFF0C\u6B63\u65B9\u5F62\u5BBD\u9AD8\u90FD\u662F 100\uFF0C\u989C\u8272\u53F7 1\u3002", score: 5 },
  { id: 408, type: "choice", title: "GoC \u4E2D\u8BA9\u7B14\u5DE6\u8F6C 90\xB0 \u7684\u547D\u4EE4\u662F\uFF1F\uFF08\u3000\uFF09", options: ["p.rt(90)", "p.lt(90)", "p.bk(90)", "p.fd(90)"], answer: "B", analysis: "lt \u662F\u5DE6\u8F6C\u547D\u4EE4\uFF0Clt(90) \u5DE6\u8F6C 90 \u5EA6\u3002", score: 5 },
  { id: 409, type: "choice", title: "for \u5FAA\u73AF\u7684\u6B63\u786E\u683C\u5F0F\u662F\uFF1F\uFF08\u3000\uFF09", options: ["for(int i=0;i<;i++)", "for(int i=0;i<4;i++)", "for(int i=;i<=4;i++)", "for(int i=0;i<4;i+)"], answer: "B", analysis: "for \u5FAA\u73AF\u4E09\u90E8\u5206\uFF1A\u521D\u59CB\u5316\u3001\u6761\u4EF6\u3001\u6B65\u8FDB\uFF0C\u5747\u9700\u5B8C\u6574\u3002", score: 5 },
  { id: 410, type: "judge", title: "\u6B63\u56DB\u8FB9\u5F62\u6BCF\u6761\u8FB9\u957F\u5EA6\u76F8\u7B49\uFF0C\u6BCF\u6B21\u8F6C\u89D2 90\xB0\uFF0C4 \u6B21\u8F6C\u89D2\u89D2\u5EA6\u4E4B\u548C\u662F 360\xB0\u3002\uFF08\u3000\uFF09", answer: "true", analysis: "\u6B63\u65B9\u5F62\u6BCF\u6B21\u8F6C 90\xB0\uFF0C\u5171 4 \u6B21\uFF0C\u5408\u8BA1 360\xB0\u3002", score: 5 },
  {
    id: 411,
    type: "code",
    title: "\u7F16\u7A0B\u9898\uFF1A\u4F7F\u7528 GoC \u753B\u4E00\u4E2A\u7B49\u8FB9\u4E09\u89D2\u5F62\n\n\u8981\u6C42\uFF1A\n- \u8FB9\u957F 100\n- \u7EBF\u6761\u4E3A\u7EA2\u8272\n- \u753B\u5B8C\u540E\u505C\u7559\n\n\u63D0\u793A\uFF1A\n- \u7B49\u8FB9\u4E09\u89D2\u5F62\u6BCF\u4E2A\u5185\u89D2 60\xB0\uFF0C\u5916\u89D2 120\xB0\n- \u4F7F\u7528 fd() \u524D\u8FDB\u3001rt() \u53F3\u8F6C\u3001pen.color() \u8BBE\u7F6E\u989C\u8272",
    language: "goc",
    score: 30,
    templateCode: "#include <graphics.h>\nint main() {\n    initGraph(800, 600);\n    pen.color(255, 0, 0);\n    // \u8BF7\u5728\u4E0B\u65B9\u7F16\u5199\u753B\u7B49\u8FB9\u4E09\u89D2\u5F62\u7684\u4EE3\u7801\n    \n    closeGraph();\n    return 0;\n}\n"
  }
];
var turtleSet = [
  { id: 1061, type: "choice", title: "\u8981\u4F7F\u7528 Python \u7684\u6D77\u9F9F\u753B\u56FE\u529F\u80FD\uFF0C\u9996\u5148\u9700\u8981\u5BFC\u5165\u7684\u6A21\u5757\u662F\uFF1F\uFF08\u3000\uFF09", options: ["turtle", "pygame", "math", "random"], answer: "A", analysis: "\u6D77\u9F9F\u7ED8\u56FE\u4F7F\u7528\u5185\u7F6E\u7684 turtle \u6A21\u5757\uFF0C\u5BFC\u5165\u8BED\u53E5\u4E3A import turtle\u3002", score: 5 },
  { id: 1062, type: "choice", title: "\u6267\u884C turtle.forward(100) \u547D\u4EE4\u540E\uFF0C\u6D77\u9F9F\u4F1A\uFF1F\uFF08\u3000\uFF09", options: ["\u5411\u540E\u79FB\u52A8 100 \u50CF\u7D20", "\u5411\u524D\u79FB\u52A8 100 \u50CF\u7D20", "\u5411\u5DE6\u8F6C 100 \u5EA6", "\u5411\u53F3\u8F6C 100 \u5EA6"], answer: "B", analysis: "forward(100) \u8BA9\u6D77\u9F9F\u6CBF\u5F53\u524D\u671D\u5411\u5411\u524D\u79FB\u52A8 100 \u50CF\u7D20\u3002", score: 5 },
  { id: 1063, type: "choice", title: "\u547D\u4EE4 turtle.left(90) \u7684\u4F5C\u7528\u662F\uFF1F\uFF08\u3000\uFF09", options: ["\u6D77\u9F9F\u987A\u65F6\u9488\u65CB\u8F6C 90 \u5EA6", "\u6D77\u9F9F\u9006\u65F6\u9488\u65CB\u8F6C 90 \u5EA6", "\u6D77\u9F9F\u79FB\u52A8 90 \u50CF\u7D20", "\u753B\u7B14\u62AC\u8D77"], answer: "B", analysis: "left(90) \u8BA9\u6D77\u9F9F\u9006\u65F6\u9488\uFF08\u5411\u5DE6\uFF09\u65CB\u8F6C 90 \u5EA6\u3002", score: 5 },
  { id: 1064, type: "choice", title: "\u8981\u8BA9\u6D77\u9F9F\u7684\u753B\u7B14\u62AC\u8D77\u3001\u505C\u6B62\u7ED8\u5236\u7EBF\u6761\uFF0C\u5E94\u8BE5\u4F7F\u7528\u7684\u547D\u4EE4\u662F\uFF1F\uFF08\u3000\uFF09", options: ["turtle.pendown()", "turtle.penup()", "turtle.pensize()", "turtle.color()"], answer: "B", analysis: "penup() \u62AC\u8D77\u753B\u7B14\uFF08\u79FB\u52A8\u4E0D\u753B\u7EBF\uFF09\uFF0Cpendown() \u843D\u7B14\u3002", score: 5 },
  { id: 1065, type: "choice", title: "\u4EE5\u4E0B\u54EA\u4E2A\u547D\u4EE4\u53EF\u4EE5\u8BBE\u7F6E\u753B\u7B14\u7684\u989C\u8272\u4E3A\u7EA2\u8272\uFF1F\uFF08\u3000\uFF09", options: ['turtle.pensize("red")', 'turtle.color("red")', 'turtle.bgcolor("red")', 'turtle.fillcolor("red")'], answer: "B", analysis: 'color("red") \u8BBE\u7F6E\u753B\u7B14\u989C\u8272\uFF1Bbgcolor \u8BBE\u7F6E\u80CC\u666F\u8272\uFF0Cfillcolor \u8BBE\u7F6E\u586B\u5145\u8272\u3002', score: 5 },
  { id: 1066, type: "choice", title: "\u6267\u884C turtle.circle(50) \u547D\u4EE4\uFF0C\u4F1A\u7ED8\u5236\u4E00\u4E2A\uFF1F\uFF08\u3000\uFF09", options: ["\u534A\u5F84\u4E3A 50 \u50CF\u7D20\u7684\u5706", "\u76F4\u5F84\u4E3A 50 \u50CF\u7D20\u7684\u5706", "\u8FB9\u957F\u4E3A 50 \u7684\u6B63\u65B9\u5F62", "\u8FB9\u957F\u4E3A 50 \u7684\u4E09\u89D2\u5F62"], answer: "A", analysis: "circle(50) \u7684\u53C2\u6570\u662F\u534A\u5F84\uFF0C\u7ED8\u5236\u534A\u5F84\u4E3A 50 \u7684\u5706\u3002", score: 5 },
  { id: 1067, type: "choice", title: "\u8981\u8BBE\u7F6E\u6D77\u9F9F\u753B\u56FE\u7A97\u53E3\u7684\u80CC\u666F\u989C\u8272\u4E3A\u84DD\u8272\uFF0C\u4F7F\u7528\u7684\u547D\u4EE4\u662F\uFF1F\uFF08\u3000\uFF09", options: ['turtle.color("blue")', 'turtle.bgcolor("blue")', 'turtle.fillcolor("blue")', 'turtle.pencolor("blue")'], answer: "B", analysis: 'bgcolor("blue") \u8BBE\u7F6E\u7A97\u53E3\u80CC\u666F\u8272\u4E3A\u84DD\u8272\u3002', score: 5 },
  { id: 1068, type: "choice", title: "\u547D\u4EE4 turtle.goto(100, 200) \u7684\u4F5C\u7528\u662F\uFF1F\uFF08\u3000\uFF09", options: ["\u6D77\u9F9F\u65CB\u8F6C\u5230 (100,200) \u65B9\u5411", "\u6D77\u9F9F\u79FB\u52A8\u5230\u5750\u6807 (100,200) \u7684\u4F4D\u7F6E", "\u6D77\u9F9F\u7ED8\u5236\u4E00\u6761\u66F2\u7EBF", "\u4EE5\u4E0A\u90FD\u4E0D\u5BF9"], answer: "B", analysis: "goto(x,y) \u8BA9\u6D77\u9F9F\u79FB\u52A8\u5230\u6307\u5B9A\u5750\u6807\u4F4D\u7F6E\u3002", score: 5 },
  { id: 1069, type: "choice", title: "Turtle \u5E93\u4E2D\uFF0C\u753B\u7B14\u7ED8\u5236\u901F\u5EA6\u7684\u8303\u56F4\u4E3A\uFF1F\uFF08\u3000\uFF09", options: ["\u4EFB\u610F\u5927\u5C0F", "0 \u5230 10 \u4E4B\u95F4\u7684\u6574\u6570\uFF08\u542B 0 \u548C 10\uFF09", "1 \u5230 10 \u4E4B\u95F4\u7684\u6574\u6570", "0 \u5230 100 \u4E4B\u95F4\u7684\u6574\u6570"], answer: "B", analysis: "Turtle \u901F\u5EA6\u53D6\u503C\u4E3A 0 \u5230 10 \u7684\u6574\u6570\uFF0C0 \u8868\u793A\u6700\u5FEB\u3002", score: 5 },
  { id: 1070, type: "judge", title: "turtle.backward(100) \u4F1A\u8BA9\u6D77\u9F9F\u8F6C\u5411\u76F8\u53CD\u65B9\u5411\uFF0C\u518D\u5411\u524D\u79FB\u52A8 100 \u50CF\u7D20\u3002\uFF08\u3000\uFF09", answer: "false", analysis: "backward(100) \u662F\u8BA9\u6D77\u9F9F\u6CBF\u5F53\u524D\u671D\u5411\u7684\u53CD\u65B9\u5411\u540E\u9000 100\uFF0C\u4E0D\u4F1A\u6539\u53D8\u6D77\u9F9F\u7684\u671D\u5411\u3002", score: 5 },
  {
    id: 1071,
    type: "code",
    title: "\u7ED8\u56FE\u6311\u6218\uFF1A\u4F7F\u7528 Python Turtle \u7ED8\u5236\u4E00\u4E2A\u5F69\u8272\u6B63\u65B9\u5F62\u87BA\u65CB\u56FE\u6848\n\n\u8981\u6C42\uFF1A\n1. \u7ED8\u5236\u4E00\u4E2A\u7531\u591A\u4E2A\u6B63\u65B9\u5F62\u7EC4\u6210\u7684\u87BA\u65CB\u56FE\u6848\n2. \u6BCF\u4E2A\u6B63\u65B9\u5F62\u6BD4\u524D\u4E00\u4E2A\u7A0D\u5927\n3. \u6BCF\u4E2A\u6B63\u65B9\u5F62\u4F7F\u7528\u4E0D\u540C\u7684\u989C\u8272\n4. \u81F3\u5C11\u7ED8\u5236 10 \u4E2A\u6B63\u65B9\u5F62\n\n\u63D0\u793A\uFF1A\n- \u4F7F\u7528 for \u5FAA\u73AF\u63A7\u5236\u6B63\u65B9\u5F62\u6570\u91CF\n- \u6BCF\u6B21\u5FAA\u73AF\u589E\u52A0\u8FB9\u957F\n- \u4F7F\u7528 turtle.pencolor() \u5207\u6362\u989C\u8272",
    language: "python",
    score: 30,
    templateCode: 'import turtle\n\nscreen = turtle.Screen()\nscreen.bgcolor("black")\n\nt = turtle.Turtle()\nt.speed(5)\n\ncolors = ["red", "orange", "yellow", "green", "blue",\n          "purple", "pink", "cyan", "magenta", "gold"]\n\n# \u8BF7\u5728\u4E0B\u65B9\u7F16\u5199\u4F60\u7684\u4EE3\u7801\n\n\nturtle.done()\n'
  }
];
var scratchJrSet = [
  { id: 1011, type: "choice", title: "\u5728 ScratchJr \u4E2D\uFF0C\u7F16\u5199\u7A0B\u5E8F\u7684\u5730\u65B9\u53EB\u4EC0\u4E48\uFF1F\uFF08\u3000\uFF09", options: ["\u821E\u53F0", "\u753B\u5E03", "\u80CC\u666F", "\u9875\u9762"], answer: "A", analysis: "ScratchJr \u4E2D\u7F16\u5199\u7A0B\u5E8F\u7684\u5730\u65B9\u79F0\u4E3A\u821E\u53F0\uFF08Stage\uFF09\uFF0C\u53EF\u4EE5\u6DFB\u52A0\u89D2\u8272\u3001\u80CC\u666F\u5E76\u521B\u4F5C\u4EA4\u4E92\u6545\u4E8B\u3002", score: 5 },
  { id: 1012, type: "choice", title: "\u5728 ScratchJr \u4E2D\uFF0C\u6267\u884C\u6240\u6709\u52A8\u4F5C\u7684\u89D2\u8272\u53EB\u4EC0\u4E48\uFF1F\uFF08\u3000\uFF09", options: ["\u80CC\u666F", "\u79EF\u6728", "\u7CBE\u7075\uFF08Sprite\uFF09", "\u6309\u94AE"], answer: "C", analysis: "\u6267\u884C\u52A8\u4F5C\u7684\u89D2\u8272\u79F0\u4E3A\u7CBE\u7075\uFF08Sprite\uFF09\u3002", score: 5 },
  { id: 1013, type: "choice", title: "\u65B0\u6DFB\u52A0\u7684\u89D2\u8272\u5728 ScratchJr \u821E\u53F0\u4E2D\u9ED8\u8BA4\u51FA\u73B0\u5728\u4EC0\u4E48\u4F4D\u7F6E\uFF1F\uFF08\u3000\uFF09", options: ["\u4E2D\u592E", "\u5DE6\u4E0A\u89D2", "\u53F3\u4E0A\u89D2", "\u5DE6\u4E0B\u89D2"], answer: "A", analysis: "\u65B0\u6DFB\u52A0\u7684\u89D2\u8272\u9ED8\u8BA4\u51FA\u73B0\u5728\u821E\u53F0\u4E2D\u592E\u3002", score: 5 },
  { id: 1014, type: "choice", title: "ScratchJr \u821E\u53F0\u7684\u80CC\u666F\u9ED8\u8BA4\u662F\u4EC0\u4E48\u989C\u8272\uFF1F\uFF08\u3000\uFF09", options: ["\u9ED1\u8272", "\u84DD\u8272", "\u7EFF\u8272", "\u767D\u8272"], answer: "D", analysis: "ScratchJr \u821E\u53F0\u80CC\u666F\u9ED8\u8BA4\u4E3A\u767D\u8272\u3002", score: 5 },
  { id: 1015, type: "choice", title: "ScratchJr \u7684\u4E3B\u89D2\u9ED8\u8BA4\u662F\u54EA\u4E2A\u89D2\u8272\uFF1F\uFF08\u3000\uFF09", options: ["\u5C0F\u72D7", "\u5C0F\u732B", "\u5C0F\u5154\u5B50", "\u5C0F\u4EBA"], answer: "B", analysis: "ScratchJr \u9ED8\u8BA4\u4E3B\u89D2\u662F\u5C0F\u732B\u3002", score: 5 },
  { id: 1016, type: "choice", title: "\u54EA\u4E2A\u6309\u94AE\u53EF\u4EE5\u7ED9\u9879\u76EE\u6DFB\u52A0\u6807\u9898\u548C\u6587\u5B57\uFF1F\uFF08\u3000\uFF09", options: ["\u6DFB\u52A0\u6587\u5B57\u6309\u94AE", "\u6DFB\u52A0\u89D2\u8272\u6309\u94AE", "\u64AD\u653E\u6309\u94AE", "\u4FDD\u5B58\u6309\u94AE"], answer: "A", analysis: "\u6DFB\u52A0\u6587\u5B57\u6309\u94AE\u7528\u4E8E\u7ED9\u9879\u76EE\u6DFB\u52A0\u6807\u9898\u548C\u6587\u5B57\u8BF4\u660E\u3002", score: 5 },
  { id: 1017, type: "judge", title: "ScratchJr \u9002\u5408 5-7 \u5C81\u4F4E\u9F84\u513F\u7AE5\uFF0C\u901A\u8FC7\u62FC\u56FE\u5F0F\u79EF\u6728\u5B66\u4E60\u57FA\u7840\u7F16\u7A0B\u903B\u8F91\u3002\uFF08\u3000\uFF09", answer: "true", analysis: "ScratchJr \u4E13\u4E3A 5-7 \u5C81\u513F\u7AE5\u8BBE\u8BA1\uFF0C\u901A\u8FC7\u56FE\u5F62\u5316\u79EF\u6728\u5757\u5B66\u4E60\u57FA\u7840\u7F16\u7A0B\u3002", score: 5 },
  { id: 1018, type: "judge", title: "\u5728 ScratchJr \u4E2D\uFF0C\u7EFF\u65D7\u79EF\u6728\u7684\u4F5C\u7528\u662F\u505C\u6B62\u7A0B\u5E8F\u8FD0\u884C\u3002\uFF08\u3000\uFF09", answer: "false", analysis: "\u7EFF\u65D7\u79EF\u6728\u662F\u7A0B\u5E8F\u7684\u542F\u52A8\u89E6\u53D1\u5757\uFF0C\u70B9\u51FB\u7EFF\u65D7\u5F00\u59CB\u8FD0\u884C\u7A0B\u5E8F\u3002", score: 5 },
  {
    id: 1019,
    type: "scratchjr",
    title: '\u521B\u610F\u6311\u6218\uFF1A\u4F7F\u7528 ScratchJr \u5236\u4F5C\u4E00\u4E2A"\u5C0F\u732B\u6349\u8FF7\u85CF"\u52A8\u753B\n\n\u8981\u6C42\uFF1A\n1. \u5C0F\u732B\u4ECE\u821E\u53F0\u5DE6\u4FA7\u8D70\u5230\u53F3\u4FA7\n2. \u8D70\u5230\u53F3\u4FA7\u540E\u9690\u85CF\u8D77\u6765\n3. \u7B49\u5F85 2 \u79D2\u540E\u91CD\u65B0\u51FA\u73B0\u5728\u5DE6\u4FA7\n4. \u5FAA\u73AF\u6267\u884C\u4EE5\u4E0A\u52A8\u4F5C\n\n\u63D0\u793A\uFF1A\u4F7F\u7528"\u79FB\u52A8""\u9690\u85CF""\u7B49\u5F85""\u663E\u793A"\u79EF\u6728\u5757',
    language: "scratchjr",
    score: 30
  }
];
var fallbackSet = [
  { id: 1, type: "choice", title: "Python \u4E2D\u4EE5\u4E0B\u54EA\u4E2A\u662F\u6B63\u786E\u7684\u53D8\u91CF\u540D\uFF1F", options: ["2name", "my_var", "class", "for"], answer: "B", score: 4 },
  { id: 2, type: "choice", title: "Python \u4E2D\u7528\u4E8E\u8F93\u51FA\u5185\u5BB9\u5230\u63A7\u5236\u53F0\u7684\u5185\u7F6E\u51FD\u6570\u662F\uFF1F", options: ["print()", "input()", "len()", "type()"], answer: "A", score: 4 },
  { id: 3, type: "judge", title: "\u7F16\u7A0B\u53EF\u4EE5\u57F9\u517B\u903B\u8F91\u601D\u7EF4\u80FD\u529B\u3002\uFF08\u3000\uFF09", answer: "true", score: 4 },
  { id: 4, type: "code", title: "\u7F16\u5199\u4E00\u4E2A\u51FD\u6570\uFF0C\u5B9E\u73B0\u4E24\u6570\u4EA4\u6362", language: "python", score: 10, templateCode: "# \u5728\u8FD9\u91CC\u7F16\u5199\u4EE3\u7801\ndef swap(a, b):\n    # TODO\n    pass\n" },
  { id: 5, type: "code", title: "\u4F7F\u7528 C++ \u8F93\u51FA Hello World", language: "cpp", score: 10, templateCode: "#include <iostream>\nusing namespace std;\n\nint main() {\n    // TODO\n    return 0;\n}\n" },
  { id: 6, type: "code", title: "\u4F7F\u7528 GoC \u753B\u4E00\u4E2A\u7B49\u8FB9\u4E09\u89D2\u5F62", language: "goc", score: 10, templateCode: "#include <graphics.h>\nint main() {\n    initGraph(800, 600);\n    // TODO: \u753B\u7B49\u8FB9\u4E09\u89D2\u5F62\n    closeGraph();\n    return 0;\n}\n" }
];
var python2Set = [
  { id: 2011, type: "choice", title: '\u5DF2\u77E5 s = "Python"\uFF0C\u5219 s[0] \u7684\u503C\u662F\uFF1F\uFF08\u3000\uFF09', options: ["P", "y", "t", "n"], answer: "A", analysis: "\u5B57\u7B26\u4E32\u7D22\u5F15\u4ECE 0 \u5F00\u59CB\uFF0Cs[0] \u662F\u7B2C\u4E00\u4E2A\u5B57\u7B26 P\u3002", score: 5 },
  { id: 2012, type: "choice", title: '\u6267\u884C print(len("Python")) \u7684\u8F93\u51FA\u7ED3\u679C\u662F\uFF1F\uFF08\u3000\uFF09', options: ["5", "6", "7", "\u62A5\u9519"], answer: "B", analysis: 'len() \u8FD4\u56DE\u5B57\u7B26\u4E32\u957F\u5EA6\uFF0C"Python" \u6709 6 \u4E2A\u5B57\u7B26\u3002', score: 5 },
  { id: 2013, type: "choice", title: '\u6267\u884C print("Hello".upper()) \u7684\u8F93\u51FA\u7ED3\u679C\u662F\uFF1F\uFF08\u3000\uFF09', options: ["hello", "HELLO", "Hello", "\u62A5\u9519"], answer: "B", analysis: "upper() \u65B9\u6CD5\u628A\u5B57\u7B26\u4E32\u4E2D\u7684\u6240\u6709\u5B57\u6BCD\u8F6C\u6362\u4E3A\u5927\u5199\u3002", score: 5 },
  { id: 2014, type: "choice", title: "\u5DF2\u77E5 lst = [1, 2, 3]\uFF0C\u6267\u884C lst.append(4) \u540E\uFF0Clst \u7684\u503C\u662F\uFF1F\uFF08\u3000\uFF09", options: ["[1, 2, 3, 4]", "[4, 1, 2, 3]", "[1, 2, 3]", "\u62A5\u9519"], answer: "A", analysis: "append() \u5728\u5217\u8868\u672B\u5C3E\u8FFD\u52A0\u5143\u7D20\uFF0C\u53D8\u4E3A [1, 2, 3, 4]\u3002", score: 5 },
  { id: 2015, type: "choice", title: "\u6267\u884C for i in range(1, 4): print(i) \u7684\u8F93\u51FA\u7ED3\u679C\u662F\uFF1F\uFF08\u3000\uFF09", options: ["1 2 3", "1 2 3 4", "0 1 2 3", "1 2"], answer: "A", analysis: "range(1, 4) \u751F\u6210 1\u30012\u30013\uFF08\u4E0D\u5305\u542B 4\uFF09\u3002", score: 5 },
  { id: 2016, type: "choice", title: "\u8868\u8FBE\u5F0F 3 > 2 and 5 < 4 \u7684\u8FD0\u7B97\u7ED3\u679C\u662F\uFF1F\uFF08\u3000\uFF09", options: ["True", "False", "\u62A5\u9519", "None"], answer: "B", analysis: "3>2 \u4E3A True\uFF0C5<4 \u4E3A False\uFF0CTrue and False = False\u3002", score: 5 },
  { id: 2017, type: "choice", title: '\u6267\u884C print(int("25") + 5) \u7684\u8F93\u51FA\u7ED3\u679C\u662F\uFF1F\uFF08\u3000\uFF09', options: ["30", '"255"', "\u62A5\u9519", "25"], answer: "A", analysis: 'int("25") \u628A\u5B57\u7B26\u4E32\u8F6C\u4E3A\u6574\u6570 25\uFF0C25 + 5 = 30\u3002', score: 5 },
  { id: 2018, type: "choice", title: '\u6267\u884C print("abcdef"[1:4]) \u7684\u8F93\u51FA\u7ED3\u679C\u662F\uFF1F\uFF08\u3000\uFF09', options: ["bcd", "abc", "cde", "bcde"], answer: "A", analysis: '\u5207\u7247 [1:4] \u53D6\u7D22\u5F15 1 \u5230 3 \u7684\u5B57\u7B26\uFF08\u4E0D\u542B 4\uFF09\uFF0C\u5373 "bcd"\u3002', score: 5 },
  { id: 2019, type: "judge", title: "\u5728 Python \u4E2D\uFF0C\u5217\u8868\u7684\u7D22\u5F15\u4ECE 1 \u5F00\u59CB\u3002\uFF08\u3000\uFF09", answer: "false", analysis: "\u5217\u8868\uFF08\u53CA\u5B57\u7B26\u4E32\uFF09\u7684\u7D22\u5F15\u4ECE 0 \u5F00\u59CB\uFF0C\u4E0D\u662F 1\u3002", score: 5 },
  { id: 2020, type: "judge", title: "range(3) \u4F1A\u4F9D\u6B21\u751F\u6210 0\u30011\u30012 \u8FD9\u4E09\u4E2A\u6574\u6570\u3002\uFF08\u3000\uFF09", answer: "true", analysis: "range(3) \u751F\u6210 0\u30011\u30012\uFF0C\u5171 3 \u4E2A\u6570\u3002", score: 5 },
  {
    id: 2021,
    type: "code",
    title: "\u7F16\u7A0B\u9898\uFF1A\u7F16\u5199\u7A0B\u5E8F\uFF0C\u8BA1\u7B97 1 \u5230 N \u7684\u6240\u6709\u6574\u6570\u4E4B\u548C\n\n\u8981\u6C42\uFF1A\n- \u4ECE\u952E\u76D8\u8F93\u5165\u4E00\u4E2A\u6B63\u6574\u6570 N\n- \u4F7F\u7528 for \u5FAA\u73AF\u7D2F\u52A0 1 \u5230 N \u7684\u6240\u6709\u6574\u6570\n- \u8F93\u51FA\u7D2F\u52A0\u7ED3\u679C\n\n\u793A\u4F8B\u8F93\u5165\uFF1A\n10\n\n\u793A\u4F8B\u8F93\u51FA\uFF1A\n55",
    language: "python",
    score: 30,
    templateCode: "# \u8BF7\u5728\u4E0B\u65B9\u7F16\u5199\u4F60\u7684\u4EE3\u7801\nn = int(input())\n\n"
  }
];
var cpp2Set = [
  { id: 2051, type: "choice", title: "\u5DF2\u77E5 int a[5] = {1, 2, 3, 4, 5};\uFF0C\u5219 a[0] \u7684\u503C\u662F\uFF1F\uFF08\u3000\uFF09", options: ["1", "2", "0", "\u62A5\u9519"], answer: "A", analysis: "\u6570\u7EC4\u4E0B\u6807\u4ECE 0 \u5F00\u59CB\uFF0Ca[0] \u662F\u7B2C\u4E00\u4E2A\u5143\u7D20 1\u3002", score: 5 },
  { id: 2052, type: "choice", title: "\u5DF2\u77E5 int a[5] = {1, 2, 3, 4, 5};\uFF0C\u5219 a[4] \u7684\u503C\u662F\uFF1F\uFF08\u3000\uFF09", options: ["4", "5", "0", "\u62A5\u9519"], answer: "B", analysis: "a[4] \u662F\u6570\u7EC4\u7B2C 5 \u4E2A\u5143\u7D20\uFF08\u4E0B\u6807 4\uFF09\uFF0C\u503C\u4E3A 5\u3002", score: 5 },
  { id: 2053, type: "choice", title: '\u5DF2\u77E5 string s = "Hello";\uFF0C\u5219 s.length() \u7684\u503C\u662F\uFF1F\uFF08\u3000\uFF09', options: ["4", "5", "6", "\u62A5\u9519"], answer: "B", analysis: 'length() \u8FD4\u56DE\u5B57\u7B26\u4E32\u957F\u5EA6\uFF0C"Hello" \u6709 5 \u4E2A\u5B57\u7B26\u3002', score: 5 },
  { id: 2054, type: "choice", title: "\u6267\u884C int a = 5; if (a > 3) a = 10; else a = 0; \u540E\uFF0Ca \u7684\u503C\u662F\uFF1F\uFF08\u3000\uFF09", options: ["10", "0", "5", "\u62A5\u9519"], answer: "A", analysis: "5 > 3 \u6210\u7ACB\uFF0C\u6267\u884C a = 10\u3002", score: 5 },
  { id: 2055, type: "choice", title: '\u6267\u884C for(int i=0;i<3;i++) for(int j=0;j<2;j++) cout << "*"; \u4E00\u5171\u8F93\u51FA\u51E0\u4E2A *\uFF1F\uFF08\u3000\uFF09', options: ["5", "6", "3", "2"], answer: "B", analysis: "\u5916\u5C42\u5FAA\u73AF 3 \u6B21\uFF0C\u5185\u5C42\u5FAA\u73AF 2 \u6B21\uFF0C\u5171 3\xD72=6 \u6B21\u3002", score: 5 },
  { id: 2056, type: "choice", title: "\u5DF2\u77E5 char c = 'A';\uFF0C\u8868\u8FBE\u5F0F c + 1 \u7684\u503C\u4E3A\uFF1F\uFF08\u3000\uFF09", options: ["'A'", "'B'", "66", "\u62A5\u9519"], answer: "C", analysis: "\u5B57\u7B26 'A' \u7684 ASCII \u7801\u4E3A 65\uFF0Cc + 1 \u5373 65 + 1 = 66\uFF08\u4F5C\u4E3A\u6574\u6570\uFF09\u3002", score: 5 },
  { id: 2057, type: "choice", title: "\u6267\u884C int a = 7; int b = a % 3; \u540E\uFF0Cb \u7684\u503C\u662F\uFF1F\uFF08\u3000\uFF09", options: ["2", "1", "3", "0"], answer: "B", analysis: "7 % 3 = 1\uFF087 \u9664\u4EE5 3 \u4F59 1\uFF09\u3002", score: 5 },
  { id: 2058, type: "choice", title: "\u5224\u65AD\u6574\u6570 n \u662F\u5426\u4E3A\u5076\u6570\uFF0C\u6B63\u786E\u7684\u6761\u4EF6\u8868\u8FBE\u5F0F\u662F\uFF1F\uFF08\u3000\uFF09", options: ["n % 2 == 0", "n / 2 == 0", "n % 2 == 1", "n * 2 == 0"], answer: "A", analysis: "\u5076\u6570\u9664\u4EE5 2 \u4F59\u6570\u4E3A 0\uFF0C\u5373 n % 2 == 0\u3002", score: 5 },
  { id: 2059, type: "judge", title: "C++ \u4E2D\u6570\u7EC4\u5143\u7D20\u7684\u4E0B\u6807\u4ECE 0 \u5F00\u59CB\u3002\uFF08\u3000\uFF09", answer: "true", analysis: "C++ \u6570\u7EC4\u4E0B\u6807\u4ECE 0 \u5F00\u59CB\uFF0C\u6700\u540E\u4E00\u4E2A\u5143\u7D20\u4E0B\u6807\u4E3A\u957F\u5EA6\u51CF 1\u3002", score: 5 },
  { id: 2060, type: "judge", title: "int a[3] = {1, 2, 3, 4}; \u662F\u5408\u6CD5\u7684\u6570\u7EC4\u5B9A\u4E49\uFF0C\u80FD\u6B63\u5E38\u7F16\u8BD1\u3002\uFF08\u3000\uFF09", answer: "false", analysis: "\u6570\u7EC4 a \u957F\u5EA6\u4E3A 3\uFF0C\u5374\u521D\u59CB\u5316\u4E86 4 \u4E2A\u5143\u7D20\uFF0C\u4F1A\u5BFC\u81F4\u7F16\u8BD1\u9519\u8BEF\u3002", score: 5 },
  {
    id: 2061,
    type: "code",
    title: "\u7F16\u7A0B\u9898\uFF1A\u7F16\u5199 C++ \u7A0B\u5E8F\uFF0C\u7EDF\u8BA1\u6570\u7EC4\u4E2D\u5076\u6570\u7684\u4E2A\u6570\n\n\u8981\u6C42\uFF1A\n- \u4ECE\u952E\u76D8\u8F93\u5165 N \u4E2A\u6574\u6570\uFF08N <= 100\uFF09\n- \u7EDF\u8BA1\u5176\u4E2D\u5076\u6570\u7684\u4E2A\u6570\n- \u8F93\u51FA\u5076\u6570\u7684\u4E2A\u6570\n\n\u793A\u4F8B\u8F93\u5165\uFF1A\n5\n1 2 3 4 5\n\n\u793A\u4F8B\u8F93\u51FA\uFF1A\n2",
    language: "cpp",
    score: 30,
    templateCode: "#include <iostream>\nusing namespace std;\n\nint main() {\n    int n;\n    cin >> n;\n    int arr[100];\n    for (int i = 0; i < n; i++) {\n        cin >> arr[i];\n    }\n    // \u8BF7\u5728\u4E0B\u65B9\u7F16\u5199\u7EDF\u8BA1\u5076\u6570\u7684\u4EE3\u7801\n    \n    return 0;\n}\n"
  }
];
var scratch2Set = [
  { id: 2041, type: "choice", title: "Scratch \u821E\u53F0\u4E2D\u592E\u7684\u5750\u6807\u662F\uFF1F\uFF08\u3000\uFF09", options: ["(0, 0)", "(100, 100)", "(-100, -100)", "(240, 180)"], answer: "A", analysis: "\u821E\u53F0\u5750\u6807\u7CFB\u539F\u70B9\u5728\u6B63\u4E2D\u592E\uFF0C\u5750\u6807\u4E3A (0, 0)\u3002", score: 5 },
  { id: 2042, type: "choice", title: '\u53D8\u91CF"\u5F97\u5206"\u521D\u59CB\u503C\u4E3A 0\uFF0C\u6267\u884C"\u5C06\u5F97\u5206\u589E\u52A0 10"2 \u6B21\u540E\uFF0C\u5F97\u5206\u7684\u503C\u662F\uFF1F\uFF08\u3000\uFF09', options: ["10", "20", "0", "\u62A5\u9519"], answer: "B", analysis: "\u6BCF\u6B21\u589E\u52A0 10\uFF0C\u6267\u884C 2 \u6B21\u5171\u589E\u52A0 20\u3002", score: 5 },
  { id: 2043, type: "choice", title: '\u4F7F\u7528"\u5F53\u4F5C\u4E3A\u514B\u9686\u4F53\u542F\u52A8\u65F6"\u79EF\u6728\uFF0C\u514B\u9686\u4F53\u4F1A\u6267\u884C\uFF1F\uFF08\u3000\uFF09', options: ["\u514B\u9686\u4F53\u81EA\u5DF1\u7684\u811A\u672C", "\u539F\u89D2\u8272\u7684\u811A\u672C", "\u4E0D\u6267\u884C\u4EFB\u4F55\u811A\u672C", "\u505C\u6B62\u6240\u6709\u811A\u672C"], answer: "A", analysis: '"\u5F53\u4F5C\u4E3A\u514B\u9686\u4F53\u542F\u52A8\u65F6"\u79EF\u6728\u4E0B\u9762\u7684\u811A\u672C\u7531\u514B\u9686\u4F53\u6267\u884C\u3002', score: 5 },
  { id: 2044, type: "choice", title: '\u4E00\u4E2A\u89D2\u8272"\u5E7F\u64AD\u6D88\u606F1"\u540E\uFF0C\u4F1A\u53D1\u751F\u4EC0\u4E48\uFF1F\uFF08\u3000\uFF09', options: ["\u6240\u6709\u63A5\u6536\u8BE5\u6D88\u606F\u7684\u89D2\u8272\u6267\u884C\u5BF9\u5E94\u811A\u672C", "\u53EA\u6709\u5E7F\u64AD\u8005\u81EA\u5DF1\u6267\u884C", "\u505C\u6B62\u6240\u6709\u811A\u672C", "\u5220\u9664\u8BE5\u89D2\u8272"], answer: "A", analysis: '\u5E7F\u64AD\u6D88\u606F\u4F1A\u89E6\u53D1\u6240\u6709"\u5F53\u63A5\u6536\u5230\u6D88\u606F1"\u79EF\u6728\u4E0B\u7684\u811A\u672C\u3002', score: 5 },
  { id: 2045, type: "choice", title: '\u79EF\u6728"\u5728 1 \u5230 10 \u4E4B\u95F4\u53D6\u968F\u673A\u6570"\u53EF\u80FD\u53D6\u5230\u4E0B\u5217\u54EA\u4E2A\u503C\uFF1F\uFF08\u3000\uFF09', options: ["0", "1", "11", "15"], answer: "B", analysis: "\u53D6\u503C\u8303\u56F4\u5305\u542B 1 \u548C 10\uFF08\u95ED\u533A\u95F4\uFF09\uFF0C1 \u662F\u53EF\u80FD\u503C\u3002", score: 5 },
  { id: 2046, type: "choice", title: "\u60F3\u8BA9\u89D2\u8272\u79FB\u5230\u821E\u53F0\u53F3\u4E0A\u89D2\uFF0C\u5750\u6807\u5E94\u63A5\u8FD1\uFF1F\uFF08\u3000\uFF09", options: ["(240, 180)", "(-240, -180)", "(0, 0)", "(-240, 180)"], answer: "A", analysis: "\u821E\u53F0\u53F3\u4E0A\u89D2 x \u4E3A\u6B63\u3001y \u4E3A\u6B63\uFF0C\u6700\u5927\u7EA6 (240, 180)\u3002", score: 5 },
  { id: 2047, type: "choice", title: '\u79EF\u6728"\u5982\u679C <\u78B0\u5230\u8FB9\u7F18> \u90A3\u4E48..."\u5C5E\u4E8E\u54EA\u79CD\u7ED3\u6784\uFF1F\uFF08\u3000\uFF09', options: ["\u6761\u4EF6\u5224\u65AD", "\u5FAA\u73AF", "\u4E8B\u4EF6", "\u8FD0\u7B97"], answer: "A", analysis: '"\u5982\u679C...\u90A3\u4E48"\u662F\u6761\u4EF6\u5224\u65AD\u7ED3\u6784\u3002', score: 5 },
  { id: 2048, type: "judge", title: 'Scratch \u4E2D"\u91CD\u590D\u6267\u884C"\u79EF\u6728\u5C5E\u4E8E\u5FAA\u73AF\u7ED3\u6784\u3002\uFF08\u3000\uFF09', answer: "true", analysis: '"\u91CD\u590D\u6267\u884C"\u4F1A\u8BA9\u79EF\u6728\u5185\u7684\u811A\u672C\u5FAA\u73AF\u8FD0\u884C\u3002', score: 5 },
  { id: 2049, type: "judge", title: "\u514B\u9686\u4F53\u4E0D\u80FD\u62E5\u6709\u81EA\u5DF1\u7684\u811A\u672C\u3002\uFF08\u3000\uFF09", answer: "false", analysis: '\u514B\u9686\u4F53\u53EF\u4EE5\u901A\u8FC7"\u5F53\u4F5C\u4E3A\u514B\u9686\u4F53\u542F\u52A8\u65F6"\u79EF\u6728\u62E5\u6709\u81EA\u5DF1\u7684\u811A\u672C\u3002', score: 5 },
  {
    id: 2050,
    type: "scratch",
    title: '\u7F16\u7A0B\u6311\u6218\uFF1A\u5236\u4F5C\u4E00\u4E2A"\u514B\u9686\u5C0F\u7403"\u52A8\u753B\n\n\u8981\u6C42\uFF1A\n1. \u521B\u5EFA\u4E00\u4E2A\u5706\u5F62\u89D2\u8272\uFF08\u5C0F\u7403\uFF09\n2. \u70B9\u51FB\u7EFF\u65D7\u540E\uFF0C\u514B\u9686 10 \u4E2A\u5C0F\u7403\n3. \u6BCF\u4E2A\u514B\u9686\u4F53\u4ECE\u821E\u53F0\u4E2D\u592E\u51FA\u53D1\uFF0C\u671D\u968F\u673A\u65B9\u5411\u79FB\u52A8\n4. \u5C0F\u7403\u78B0\u5230\u8FB9\u7F18\u540E\u53CD\u5F39\n5. \u5C0F\u7403\u989C\u8272\u968F\u673A\u53D8\u5316\n\n\u63D0\u793A\uFF1A\u4F7F\u7528"\u514B\u9686""\u968F\u673A\u6570""\u78B0\u5230\u8FB9\u7F18\u5C31\u53CD\u5F39""\u989C\u8272\u7279\u6548"\u7B49\u79EF\u6728',
    language: "scratch3",
    score: 30
  }
];
var scratchJr2Set = [
  { id: 10211, type: "choice", title: "ScratchJr \u4E2D\u8BA9\u89D2\u8272\u5411\u53F3\u79FB\u52A8\u7684\u79EF\u6728\u5C5E\u4E8E\u54EA\u4E2A\u7C7B\u522B\uFF1F\uFF08\u3000\uFF09", options: ["\u84DD\u8272\uFF08\u8FD0\u52A8\uFF09", "\u9EC4\u8272\uFF08\u89E6\u53D1\uFF09", "\u7D2B\u8272\uFF08\u5916\u89C2\uFF09", "\u7EFF\u8272\uFF08\u58F0\u97F3\uFF09"], answer: "A", analysis: "\u8FD0\u52A8\u7C7B\u79EF\u6728\u662F\u84DD\u8272\u7684\uFF0C\u7528\u4E8E\u63A7\u5236\u89D2\u8272\u79FB\u52A8\u3002", score: 5 },
  { id: 10212, type: "choice", title: "\u60F3\u8BA9\u89D2\u8272\u6682\u65F6\u9690\u85CF\u8D77\u6765\uFF0C\u5E94\u8BE5\u4F7F\u7528\u54EA\u4E2A\u79EF\u6728\uFF1F\uFF08\u3000\uFF09", options: ["\u9690\u85CF\u79EF\u6728", "\u663E\u793A\u79EF\u6728", "\u79FB\u52A8\u79EF\u6728", "\u8BF4\u8BDD\u79EF\u6728"], answer: "A", analysis: "\u9690\u85CF\u79EF\u6728\u8BA9\u89D2\u8272\u6682\u65F6\u4E0D\u53EF\u89C1\uFF0C\u663E\u793A\u79EF\u6728\u518D\u8BA9\u5176\u51FA\u73B0\u3002", score: 5 },
  { id: 10213, type: "choice", title: '"\u7B49\u5F85"\u79EF\u6728\u7684\u4F5C\u7528\u662F\uFF1F\uFF08\u3000\uFF09', options: ["\u6682\u505C\u4E00\u6BB5\u65F6\u95F4", "\u7ED3\u675F\u7A0B\u5E8F", "\u52A0\u5FEB\u7A0B\u5E8F\u8FD0\u884C", "\u5FAA\u73AF\u6267\u884C"], answer: "A", analysis: "\u7B49\u5F85\u79EF\u6728\u8BA9\u7A0B\u5E8F\u6682\u505C\u6307\u5B9A\u7684\u65F6\u95F4\u540E\u7EE7\u7EED\u6267\u884C\u3002", score: 5 },
  { id: 10214, type: "choice", title: '"\u91CD\u590D"\u79EF\u6728\u53EF\u4EE5\u505A\u4EC0\u4E48\uFF1F\uFF08\u3000\uFF09', options: ["\u91CD\u590D\u6267\u884C\u5176\u5185\u7684\u79EF\u6728", "\u505C\u6B62\u7A0B\u5E8F", "\u64AD\u653E\u58F0\u97F3", "\u79FB\u52A8\u89D2\u8272"], answer: "A", analysis: "\u91CD\u590D\u79EF\u6728\u8BA9\u5B83\u91CC\u9762\u7684\u79EF\u6728\u5757\u5FAA\u73AF\u6267\u884C\u6307\u5B9A\u6B21\u6570\u3002", score: 5 },
  { id: 10215, type: "choice", title: "\u70B9\u51FB\u821E\u53F0\u4E0A\u7684\u67D0\u4E2A\u89D2\u8272\uFF0C\u53EF\u4EE5\uFF1F\uFF08\u3000\uFF09", options: ["\u67E5\u770B\u5E76\u7F16\u8F91\u8BE5\u89D2\u8272\u7684\u7A0B\u5E8F", "\u5220\u9664\u6574\u4E2A\u9879\u76EE", "\u4FDD\u5B58\u9879\u76EE", "\u5173\u95ED\u5E94\u7528"], answer: "A", analysis: "\u70B9\u51FB\u89D2\u8272\u4F1A\u9009\u4E2D\u5B83\uFF0C\u53EF\u4EE5\u67E5\u770B\u548C\u7F16\u8F91\u8BE5\u89D2\u8272\u4E13\u5C5E\u7684\u7A0B\u5E8F\u3002", score: 5 },
  { id: 10216, type: "choice", title: "\u5728 ScratchJr \u4E2D\uFF0C\u54EA\u4E2A\u6309\u94AE\u53EF\u4EE5\u5F00\u59CB\u64AD\u653E\u4F60\u7684\u9879\u76EE\uFF1F\uFF08\u3000\uFF09", options: ["\u7EFF\u65D7\u6309\u94AE", "\u7EA2\u8272\u505C\u6B62\u6309\u94AE", "\u9EC4\u8272\u6309\u94AE", "\u84DD\u8272\u6309\u94AE"], answer: "A", analysis: "\u7EFF\u65D7\u6309\u94AE\u7528\u4E8E\u542F\u52A8\uFF08\u64AD\u653E\uFF09\u9879\u76EE\u3002", score: 5 },
  { id: 10217, type: "judge", title: "\u5728 ScratchJr \u4E2D\uFF0C\u53EF\u4EE5\u4E3A\u89D2\u8272\u6DFB\u52A0\u58F0\u97F3\u3002\uFF08\u3000\uFF09", answer: "true", analysis: "ScratchJr \u63D0\u4F9B\u5F55\u97F3\u548C\u5185\u7F6E\u58F0\u97F3\uFF0C\u53EF\u4E3A\u89D2\u8272\u6DFB\u52A0\u58F0\u97F3\u3002", score: 5 },
  { id: 10218, type: "judge", title: "ScratchJr \u4E2D\u4E00\u4E2A\u9879\u76EE\u53EA\u80FD\u6709\u4E00\u4E2A\u89D2\u8272\u3002\uFF08\u3000\uFF09", answer: "false", analysis: "\u4E00\u4E2A ScratchJr \u9879\u76EE\u53EF\u4EE5\u6DFB\u52A0\u591A\u4E2A\u89D2\u8272\u3002", score: 5 },
  {
    id: 10219,
    type: "scratchjr",
    title: '\u521B\u610F\u6311\u6218\uFF1A\u4F7F\u7528 ScratchJr \u5236\u4F5C"\u5C0F\u732B\u6765\u56DE\u8D70\u52A8"\u52A8\u753B\n\n\u8981\u6C42\uFF1A\n1. \u5C0F\u732B\u4ECE\u821E\u53F0\u5DE6\u4FA7\u5411\u53F3\u8D70\n2. \u8D70\u5230\u53F3\u4FA7\u540E\u8F6C\u8EAB\n3. \u518D\u8D70\u56DE\u5DE6\u4FA7\n4. \u5FAA\u73AF\u6267\u884C\u4EE5\u4E0A\u52A8\u4F5C\n\n\u63D0\u793A\uFF1A\u4F7F\u7528"\u79FB\u52A8""\u8F6C\u8EAB""\u91CD\u590D"\u7B49\u79EF\u6728\u5757',
    language: "scratchjr",
    score: 30
  }
];
var turtle2Set = [
  { id: 10711, type: "choice", title: "turtle.speed(0) \u8868\u793A\u4EC0\u4E48\uFF1F\uFF08\u3000\uFF09", options: ["\u6700\u5FEB\u901F\u5EA6", "\u6700\u6162\u901F\u5EA6", "\u505C\u6B62\u753B\u56FE", "\u7A0B\u5E8F\u62A5\u9519"], answer: "A", analysis: "speed \u53D6\u503C\u8303\u56F4 0-10\uFF0C0 \u8868\u793A\u6700\u5FEB\uFF08\u65E0\u52A8\u753B\uFF09\u3002", score: 5 },
  { id: 10712, type: "choice", title: '\u753B\u4E00\u4E2A\u6B63\u65B9\u5F62\uFF0C\u9700\u8981\u91CD\u590D\u6267\u884C\u51E0\u6B21"\u524D\u8FDB + \u53F3\u8F6C 90 \u5EA6"\uFF1F\uFF08\u3000\uFF09', options: ["4", "3", "5", "6"], answer: "A", analysis: "\u6B63\u65B9\u5F62\u6709 4 \u6761\u8FB9\uFF0C\u9700\u8981\u91CD\u590D 4 \u6B21\u3002", score: 5 },
  { id: 10713, type: "choice", title: "\u753B\u4E00\u4E2A\u6B63\u4E94\u8FB9\u5F62\uFF0C\u6D77\u9F9F\u6BCF\u6B21\u9700\u8981\u8F6C\u591A\u5C11\u5EA6\uFF1F\uFF08\u3000\uFF09", options: ["72", "60", "90", "120"], answer: "A", analysis: "\u6B63\u591A\u8FB9\u5F62\u5916\u89D2 = 360/\u8FB9\u6570\uFF0C\u6B63\u4E94\u8FB9\u5F62\u5916\u89D2 = 360/5 = 72 \u5EA6\u3002", score: 5 },
  { id: 10714, type: "choice", title: "turtle.begin_fill() \u548C turtle.end_fill() \u7684\u4F5C\u7528\u662F\uFF1F\uFF08\u3000\uFF09", options: ["\u586B\u5145\u95ED\u5408\u56FE\u5F62", "\u5F00\u59CB\u753B\u56FE", "\u505C\u6B62\u753B\u56FE", "\u4FDD\u5B58\u56FE\u5F62"], answer: "A", analysis: "begin_fill() \u548C end_fill() \u4E4B\u95F4\u7684\u95ED\u5408\u533A\u57DF\u4F1A\u88AB\u586B\u5145\u989C\u8272\u3002", score: 5 },
  { id: 10715, type: "choice", title: "turtle.pensize(5) \u8BBE\u7F6E\u7684\u662F\uFF1F\uFF08\u3000\uFF09", options: ["\u753B\u7B14\u7C97\u7EC6", "\u753B\u7B14\u989C\u8272", "\u753B\u5E03\u5927\u5C0F", "\u7ED8\u5236\u901F\u5EA6"], answer: "A", analysis: "pensize() \u8BBE\u7F6E\u753B\u7B14\u7684\u7C97\u7EC6\uFF08\u7EBF\u6761\u5BBD\u5EA6\uFF09\u3002", score: 5 },
  { id: 10716, type: "choice", title: "\u60F3\u8BA9\u6D77\u9F9F\u56FE\u6807\u9690\u85CF\uFF08\u4E0D\u663E\u793A\uFF09\uFF0C\u5E94\u4F7F\u7528\uFF1F\uFF08\u3000\uFF09", options: ["turtle.hideturtle()", "turtle.showturtle()", "turtle.penup()", "turtle.clear()"], answer: "A", analysis: "hideturtle() \u9690\u85CF\u6D77\u9F9F\uFF0Cshowturtle() \u663E\u793A\u6D77\u9F9F\u3002", score: 5 },
  { id: 10717, type: "choice", title: "turtle.reset() \u7684\u4F5C\u7528\u662F\uFF1F\uFF08\u3000\uFF09", options: ["\u6E05\u5C4F\u5E76\u91CD\u7F6E\u6D77\u9F9F\u5230\u521D\u59CB\u72B6\u6001", "\u53EA\u6E05\u5C4F", "\u53EA\u79FB\u52A8\u6D77\u9F9F", "\u5173\u95ED\u7A97\u53E3"], answer: "A", analysis: "reset() \u6E05\u7A7A\u753B\u5E03\u5E76\u628A\u6D77\u9F9F\u91CD\u7F6E\u5230\u521D\u59CB\u4F4D\u7F6E\u548C\u671D\u5411\u3002", score: 5 },
  { id: 10718, type: "choice", title: "\u8981\u753B\u4E00\u4E2A\u5706\uFF0C\u5E94\u4F7F\u7528\u7684\u547D\u4EE4\u662F\uFF1F\uFF08\u3000\uFF09", options: ["turtle.circle()", "turtle.square()", "turtle.rect()", "turtle.polygon()"], answer: "A", analysis: "turtle.circle(\u534A\u5F84) \u7528\u4E8E\u753B\u5706\u3002", score: 5 },
  { id: 10719, type: "judge", title: "Python turtle \u6D77\u9F9F\u7684\u9ED8\u8BA4\u671D\u5411\u662F\u5411\u53F3\uFF08\u6B63\u4E1C\u65B9\u5411\uFF09\u3002\uFF08\u3000\uFF09", answer: "true", analysis: "turtle \u9ED8\u8BA4\u671D\u5411 0 \u5EA6\uFF08\u5411\u53F3/\u6B63\u4E1C\uFF09\u3002", score: 5 },
  { id: 10720, type: "judge", title: "turtle.done() \u7684\u4F5C\u7528\u662F\u8BA9\u7ED8\u56FE\u7A97\u53E3\u4FDD\u6301\u4E0D\u81EA\u52A8\u5173\u95ED\u3002\uFF08\u3000\uFF09", answer: "true", analysis: "turtle.done() \u8FDB\u5165\u4E8B\u4EF6\u5FAA\u73AF\uFF0C\u4FDD\u6301\u7A97\u53E3\u6253\u5F00\u7B49\u5F85\u5173\u95ED\u3002", score: 5 },
  {
    id: 10721,
    type: "code",
    title: "\u7ED8\u56FE\u6311\u6218\uFF1A\u4F7F\u7528 Python Turtle \u7ED8\u5236\u4E00\u4E2A\u4E94\u89D2\u661F\n\n\u8981\u6C42\uFF1A\n1. \u4F7F\u7528 for \u5FAA\u73AF\u7ED8\u5236\u4E94\u89D2\u661F\n2. \u4E94\u89D2\u661F\u4E3A\u7EA2\u8272\n3. \u6BCF\u6761\u8FB9\u957F\u5EA6 200\n\n\u63D0\u793A\uFF1A\n- \u4E94\u89D2\u661F\u6BCF\u6B21\u8F6C\u89D2\u4E3A 144 \u5EA6\n- \u5171 5 \u6761\u8FB9",
    language: "python",
    score: 30,
    templateCode: 'import turtle\n\nt = turtle.Turtle()\nt.color("red")\nt.speed(3)\n\n# \u8BF7\u5728\u4E0B\u65B9\u7F16\u5199\u753B\u4E94\u89D2\u661F\u7684\u4EE3\u7801\n\n\nturtle.done()\n'
  }
];
var goc2Set = [
  { id: 4011, type: "choice", title: "GoC \u4E2D\u753B\u5B9E\u5FC3\u5706\uFF08\u586B\u5145\u5706\uFF09\u7684\u547D\u4EE4\u662F\uFF1F\uFF08\u3000\uFF09", options: ["oo()", "o()", "circle()", "dot()"], answer: "A", analysis: "oo(\u534A\u5F84, \u989C\u8272) \u753B\u5B9E\u5FC3\u5706\uFF0Co() \u753B\u7A7A\u5FC3\u5706\u3002", score: 5 },
  { id: 4012, type: "choice", title: "\u8BBE\u7F6E\u753B\u7B14\u989C\u8272\u4E3A\u7EA2\u8272\uFF0C\u6B63\u786E\u7684\u4EE3\u7801\u662F\uFF1F\uFF08\u3000\uFF09", options: ["pen.color(255, 0, 0)", "pen.color(0, 255, 0)", "pen.color(0, 0, 255)", "pen.red()"], answer: "A", analysis: "RGB \u989C\u8272\u4E2D\u7EA2\u8272\u4E3A (255, 0, 0)\u3002", score: 5 },
  { id: 4013, type: "choice", title: "\u8BBE\u7F6E\u753B\u7B14\u7C97\u7EC6\u4E3A 3\uFF0C\u6B63\u786E\u7684\u4EE3\u7801\u662F\uFF1F\uFF08\u3000\uFF09", options: ["pen.size(3)", "pen.width(3)", "pen.bold(3)", "pen.thick(3)"], answer: "A", analysis: "pen.size(n) \u8BBE\u7F6E\u753B\u7B14\u7684\u7C97\u7EC6\u3002", score: 5 },
  { id: 4014, type: "choice", title: "\u753B\u4E00\u4E2A\u6B63\u516D\u8FB9\u5F62\uFF0C\u6BCF\u6B21\u9700\u8981\u53F3\u8F6C\u7684\u5EA6\u6570\u662F\uFF1F\uFF08\u3000\uFF09", options: ["60", "90", "120", "30"], answer: "A", analysis: "\u6B63\u516D\u8FB9\u5F62\u5916\u89D2 = 360/6 = 60 \u5EA6\u3002", score: 5 },
  { id: 4015, type: "choice", title: "\u8BA9\u753B\u7B14\u79FB\u5230\u5750\u6807 (100, 100) \u4F46\u4E0D\u753B\u7EBF\uFF0C\u5E94\u8BE5\uFF1F\uFF08\u3000\uFF09", options: ["\u5148 pen.up() \u518D\u79FB\u52A8", "\u76F4\u63A5\u79FB\u52A8", "\u5148 pen.down() \u518D\u79FB\u52A8", "\u65E0\u6CD5\u5B9E\u73B0"], answer: "A", analysis: "\u5148\u62AC\u7B14 pen.up()\uFF0C\u79FB\u52A8\u65F6\u5C31\u4E0D\u4F1A\u7559\u4E0B\u7EBF\u6761\u3002", score: 5 },
  { id: 4016, type: "choice", title: "\u4F7F\u7528 for \u5FAA\u73AF\u753B\u6B63\u4E94\u8FB9\u5F62\uFF0C\u5FAA\u73AF\u4F53\u5185\u6B63\u786E\u7684\u4EE3\u7801\u662F\uFF1F\uFF08\u3000\uFF09", options: ["fd(\u8FB9\u957F) + rt(72)", "fd(\u8FB9\u957F) + rt(90)", "bk(\u8FB9\u957F) + lt(72)", "fd(\u8FB9\u957F) + rt(60)"], answer: "A", analysis: "\u6B63\u4E94\u8FB9\u5F62\u6BCF\u6B21\u524D\u8FDB\u8FB9\u957F\u540E\u53F3\u8F6C 72 \u5EA6\u3002", score: 5 },
  { id: 4017, type: "choice", title: "GoC \u4E2D o(50, 1) \u753B\u7684\u662F\uFF1F\uFF08\u3000\uFF09", options: ["\u534A\u5F84\u4E3A 50 \u7684\u7EA2\u8272\u7A7A\u5FC3\u5706", "\u534A\u5F84\u4E3A 50 \u7684\u7EA2\u8272\u5B9E\u5FC3\u5706", "\u76F4\u5F84\u4E3A 50 \u7684\u5706", "\u8FB9\u957F\u4E3A 50 \u7684\u5706"], answer: "A", analysis: "o(\u534A\u5F84, \u989C\u8272) \u753B\u7A7A\u5FC3\u5706\uFF0C\u989C\u8272\u53F7 1 \u662F\u7EA2\u8272\u3002", score: 5 },
  { id: 4018, type: "judge", title: "GoC \u4E2D rr(100, 50, 2) \u753B\u7684\u662F\u5BBD 100\u3001\u9AD8 50 \u7684\u5B9E\u5FC3\u77E9\u5F62\u3002\uFF08\u3000\uFF09", answer: "true", analysis: "rr(\u5BBD, \u9AD8, \u989C\u8272) \u753B\u5B9E\u5FC3\u77E9\u5F62\u3002", score: 5 },
  { id: 4019, type: "judge", title: "GoC \u4E2D pen.up() \u62AC\u7B14\u540E\uFF0C\u518D\u6267\u884C fd(100) \u4ECD\u4F1A\u753B\u51FA\u7EBF\u6761\u3002\uFF08\u3000\uFF09", answer: "false", analysis: "\u62AC\u7B14\u540E\u79FB\u52A8\u4E0D\u4F1A\u7559\u4E0B\u7EBF\u6761\uFF0C\u843D\u7B14 pen.down() \u540E\u624D\u4F1A\u753B\u7EBF\u3002", score: 5 },
  {
    id: 4020,
    type: "code",
    title: '\u7F16\u7A0B\u9898\uFF1A\u4F7F\u7528 GoC \u7ED8\u5236\u4E00\u4E2A"\u98CE\u8F66"\u56FE\u6848\n\n\u8981\u6C42\uFF1A\n- \u98CE\u8F66\u7531 4 \u4E2A\u7B49\u8FB9\u4E09\u89D2\u5F62\u7EC4\u6210\n- \u6BCF\u4E2A\u4E09\u89D2\u5F62\u7ED5\u4E2D\u5FC3\u65CB\u8F6C 90 \u5EA6\n- \u4F7F\u7528 for \u5FAA\u73AF\u7ED8\u5236\n\n\u63D0\u793A\uFF1A\n- \u7B49\u8FB9\u4E09\u89D2\u5F62\u6BCF\u6B21\u53F3\u8F6C 120 \u5EA6\n- \u6BCF\u753B\u5B8C\u4E00\u4E2A\u4E09\u89D2\u5F62\uFF0C\u53F3\u8F6C 90 \u5EA6',
    language: "goc",
    score: 30,
    templateCode: "#include <graphics.h>\nint main() {\n    initGraph(800, 600);\n    // \u8BF7\u5728\u4E0B\u65B9\u7F16\u5199\u753B\u98CE\u8F66\u7684\u4EE3\u7801\n    \n    closeGraph();\n    return 0;\n}\n"
  }
];
var python3Set = [
  { id: 3011, type: "choice", title: "\u5DF2\u77E5 def f(x): return x * 2\uFF0C\u5219 f(5) \u7684\u503C\u662F\uFF1F\uFF08\u3000\uFF09", options: ["5", "10", "25", "\u62A5\u9519"], answer: "B", analysis: "\u51FD\u6570 f \u8FD4\u56DE\u53C2\u6570\u7684 2 \u500D\uFF0Cf(5) = 5 * 2 = 10\u3002", score: 5 },
  { id: 3012, type: "choice", title: "\u51FD\u6570\u81EA\u5DF1\u8C03\u7528\u81EA\u5DF1\uFF0C\u8FD9\u79CD\u7F16\u7A0B\u65B9\u5F0F\u53EB\u505A\uFF1F\uFF08\u3000\uFF09", options: ["\u9012\u5F52", "\u5FAA\u73AF", "\u8FED\u4EE3", "\u8DF3\u8F6C"], answer: "A", analysis: "\u51FD\u6570\u76F4\u63A5\u6216\u95F4\u63A5\u8C03\u7528\u81EA\u8EAB\u79F0\u4E3A\u9012\u5F52\u3002", score: 5 },
  { id: 3013, type: "choice", title: '\u5DF2\u77E5 d = {"a": 1, "b": 2}\uFF0C\u5219 d["b"] \u7684\u503C\u662F\uFF1F\uFF08\u3000\uFF09', options: ["1", "2", "a", "\u62A5\u9519"], answer: "B", analysis: '\u5B57\u5178\u901A\u8FC7\u952E\u8BBF\u95EE\u503C\uFF0Cd["b"] = 2\u3002', score: 5 },
  { id: 3014, type: "choice", title: "\u6267\u884C print([x * x for x in range(4)]) \u7684\u8F93\u51FA\u7ED3\u679C\u662F\uFF1F\uFF08\u3000\uFF09", options: ["[0, 1, 4, 9]", "[1, 4, 9, 16]", "[0, 1, 2, 3]", "[0, 2, 4, 6]"], answer: "A", analysis: "\u5217\u8868\u63A8\u5BFC\u5F0F\u5BF9 0\u30011\u30012\u30013 \u5206\u522B\u5E73\u65B9\uFF0C\u5F97\u5230 [0, 1, 4, 9]\u3002", score: 5 },
  { id: 3015, type: "choice", title: "\u5DF2\u77E5 f = lambda x: x + 1\uFF0C\u5219 f(3) \u7684\u503C\u662F\uFF1F\uFF08\u3000\uFF09", options: ["3", "4", "1", "\u62A5\u9519"], answer: "B", analysis: "lambda \u662F\u533F\u540D\u51FD\u6570\uFF0Cf(3) = 3 + 1 = 4\u3002", score: 5 },
  { id: 3016, type: "choice", title: '\u6267\u884C print("a,b,c".split(",")) \u7684\u8F93\u51FA\u7ED3\u679C\u662F\uFF1F\uFF08\u3000\uFF09', options: ["['a', 'b', 'c']", "['a,b,c']", '["a,b,c"]', "\u62A5\u9519"], answer: "A", analysis: `split(",") \u6309\u9017\u53F7\u5206\u5272\u5B57\u7B26\u4E32\uFF0C\u5F97\u5230\u5217\u8868 ['a', 'b', 'c']\u3002`, score: 5 },
  { id: 3017, type: "choice", title: "\u6267\u884C print(set([1, 2, 2, 3])) \u7684\u8F93\u51FA\u7ED3\u679C\u662F\uFF1F\uFF08\u3000\uFF09", options: ["{1, 2, 3}", "{1, 2, 2, 3}", "[1, 2, 3]", "\u62A5\u9519"], answer: "A", analysis: "\u96C6\u5408\u81EA\u52A8\u53BB\u9664\u91CD\u590D\u5143\u7D20\uFF0Cset([1,2,2,3]) = {1, 2, 3}\u3002", score: 5 },
  { id: 3018, type: "choice", title: "\u8981\u6355\u83B7\u4EE3\u7801\u8FD0\u884C\u65F6\u7684\u5F02\u5E38\u5E76\u907F\u514D\u7A0B\u5E8F\u5D29\u6E83\uFF0C\u5E94\u4F7F\u7528\uFF1F\uFF08\u3000\uFF09", options: ["try...except", "for...in", "if...else", "while...else"], answer: "A", analysis: "try...except \u7528\u4E8E\u6355\u83B7\u5E76\u5904\u7406\u5F02\u5E38\u3002", score: 5 },
  { id: 3019, type: "judge", title: "Python 3.7 \u53CA\u4EE5\u4E0A\u7248\u672C\u4E2D\uFF0C\u5B57\u5178\u4F1A\u4FDD\u6301\u952E\u503C\u5BF9\u7684\u63D2\u5165\u987A\u5E8F\u3002\uFF08\u3000\uFF09", answer: "true", analysis: "Python 3.7 \u8D77\u5B57\u5178\u662F\u6709\u5E8F\u7684\uFF0C\u4FDD\u6301\u63D2\u5165\u987A\u5E8F\u3002", score: 5 },
  { id: 3020, type: "judge", title: "\u9012\u5F52\u51FD\u6570\u5FC5\u987B\u5305\u542B\u9012\u5F52\u51FA\u53E3\uFF08\u7EC8\u6B62\u6761\u4EF6\uFF09\uFF0C\u5426\u5219\u53EF\u80FD\u9677\u5165\u65E0\u9650\u9012\u5F52\u3002\uFF08\u3000\uFF09", answer: "true", analysis: "\u6CA1\u6709\u7EC8\u6B62\u6761\u4EF6\u7684\u9012\u5F52\u4F1A\u65E0\u9650\u8C03\u7528\u81EA\u8EAB\uFF0C\u6700\u7EC8\u5BFC\u81F4\u6808\u6EA2\u51FA\u3002", score: 5 },
  {
    id: 3021,
    type: "code",
    title: "\u7F16\u7A0B\u9898\uFF1A\u7F16\u5199\u7A0B\u5E8F\uFF0C\u8BA1\u7B97\u6590\u6CE2\u90A3\u5951\u6570\u5217\u7684\u7B2C N \u9879\n\n\u8981\u6C42\uFF1A\n- \u4ECE\u952E\u76D8\u8F93\u5165\u6B63\u6574\u6570 N\uFF08N <= 30\uFF09\n- \u6590\u6CE2\u90A3\u5951\u6570\u5217\uFF1A1, 1, 2, 3, 5, 8, ...\uFF08\u7B2C 1\u30012 \u9879\u4E3A 1\uFF09\n- \u4F7F\u7528\u5FAA\u73AF\u6216\u9012\u5F52\u8BA1\u7B97\u7B2C N \u9879\u5E76\u8F93\u51FA\n\n\u793A\u4F8B\u8F93\u5165\uFF1A\n7\n\n\u793A\u4F8B\u8F93\u51FA\uFF1A\n13",
    language: "python",
    score: 30,
    templateCode: "# \u8BF7\u5728\u4E0B\u65B9\u7F16\u5199\u4F60\u7684\u4EE3\u7801\nn = int(input())\n\n"
  }
];
var cpp3Set = [
  { id: 3051, type: "choice", title: "\u5DF2\u77E5 int add(int a, int b) { return a + b; }\uFF0C\u5219 add(3, 4) \u7684\u503C\u662F\uFF1F\uFF08\u3000\uFF09", options: ["7", "34", "12", "\u62A5\u9519"], answer: "A", analysis: "\u51FD\u6570 add \u8FD4\u56DE\u4E24\u6570\u4E4B\u548C\uFF0Cadd(3, 4) = 7\u3002", score: 5 },
  { id: 3052, type: "choice", title: "\u5DF2\u77E5 int a = 10; int *p = &a;\uFF0C\u5219 *p \u7684\u503C\u662F\uFF1F\uFF08\u3000\uFF09", options: ["10", "&a", "\u5730\u5740\u503C", "\u62A5\u9519"], answer: "A", analysis: "p \u6307\u5411\u53D8\u91CF a\uFF0C*p \u89E3\u5F15\u7528\u53D6 a \u7684\u503C 10\u3002", score: 5 },
  { id: 3053, type: "choice", title: "\u5B9A\u4E49\u4E00\u4E2A\u5305\u542B\u59D3\u540D\u548C\u5E74\u9F84\u7684\u7ED3\u6784\u4F53\uFF0C\u6B63\u786E\u5199\u6CD5\u662F\uFF1F\uFF08\u3000\uFF09", options: ["struct Student { string name; int age; };", "Student struct { string name; int age; }", "class Student { string name; int age; };", "struct { name, age } Student;"], answer: "A", analysis: "struct \u5173\u952E\u5B57\u5B9A\u4E49\u7ED3\u6784\u4F53\uFF0C\u6210\u5458\u7528\u5206\u53F7\u5206\u9694\u3002", score: 5 },
  { id: 3054, type: "choice", title: '\u5DF2\u77E5 string s = "abcdef";\uFF0C\u5219 s.substr(2, 3) \u7684\u503C\u662F\uFF1F\uFF08\u3000\uFF09', options: ["cde", "abc", "bcd", "def"], answer: "A", analysis: 'substr(2, 3) \u4ECE\u4E0B\u6807 2 \u5F00\u59CB\u53D6 3 \u4E2A\u5B57\u7B26\uFF0C\u5373 "cde"\u3002', score: 5 },
  { id: 3055, type: "choice", title: "\u8BA1\u7B97 n \u7684\u9636\u4E58\u5E38\u7528\u54EA\u79CD\u7B97\u6CD5\u7ED3\u6784\uFF1F\uFF08\u3000\uFF09", options: ["\u9012\u5F52\u6216\u5FAA\u73AF", "\u53EA\u80FD\u987A\u5E8F", "\u53EA\u80FD\u8DF3\u8F6C", "\u53EA\u80FD\u5224\u65AD"], answer: "A", analysis: "\u9636\u4E58\u53EF\u901A\u8FC7\u9012\u5F52\uFF08n! = n*(n-1)!\uFF09\u6216\u5FAA\u73AF\u5B9E\u73B0\u3002", score: 5 },
  { id: 3056, type: "choice", title: "\u5DF2\u77E5 vector<int> v; \u6267\u884C v.push_back(5); \u540E\uFF0Cv.size() \u7684\u503C\u662F\uFF1F\uFF08\u3000\uFF09", options: ["1", "5", "0", "\u62A5\u9519"], answer: "A", analysis: "push_back \u5411 vector \u672B\u5C3E\u6DFB\u52A0\u5143\u7D20\uFF0Csize \u53D8\u4E3A 1\u3002", score: 5 },
  { id: 3057, type: "choice", title: "\u4F7F\u7528 sort() \u51FD\u6570\u5BF9\u6570\u7EC4\u6392\u5E8F\uFF0C\u9700\u8981\u5305\u542B\u54EA\u4E2A\u5934\u6587\u4EF6\uFF1F\uFF08\u3000\uFF09", options: ["#include <algorithm>", "#include <iostream>", "#include <vector>", "#include <string>"], answer: "A", analysis: "sort() \u51FD\u6570\u5B9A\u4E49\u5728 <algorithm> \u5934\u6587\u4EF6\u4E2D\u3002", score: 5 },
  { id: 3058, type: "choice", title: "\u5DF2\u77E5 int a = 5; int &r = a; \u6267\u884C r = 10; \u540E\uFF0Ca \u7684\u503C\u662F\uFF1F\uFF08\u3000\uFF09", options: ["10", "5", "0", "\u62A5\u9519"], answer: "A", analysis: "r \u662F a \u7684\u5F15\u7528\uFF08\u522B\u540D\uFF09\uFF0C\u4FEE\u6539 r \u5373\u4FEE\u6539 a\u3002", score: 5 },
  { id: 3059, type: "judge", title: "\u9012\u5F52\u51FD\u6570\u5FC5\u987B\u5305\u542B\u9012\u5F52\u51FA\u53E3\uFF08\u7EC8\u6B62\u6761\u4EF6\uFF09\uFF0C\u5426\u5219\u4F1A\u65E0\u9650\u9012\u5F52\u5BFC\u81F4\u6808\u6EA2\u51FA\u3002\uFF08\u3000\uFF09", answer: "true", analysis: "\u7F3A\u5C11\u7EC8\u6B62\u6761\u4EF6\u7684\u9012\u5F52\u4F1A\u65E0\u9650\u8C03\u7528\u81EA\u8EAB\u3002", score: 5 },
  { id: 3060, type: "judge", title: "C++ \u4E2D struct \u7ED3\u6784\u4F53\u7684\u6210\u5458\u9ED8\u8BA4\u8BBF\u95EE\u6743\u9650\u662F public\uFF08\u516C\u6709\uFF09\u3002\uFF08\u3000\uFF09", answer: "true", analysis: "struct \u6210\u5458\u9ED8\u8BA4 public\uFF0C\u800C class \u6210\u5458\u9ED8\u8BA4 private\u3002", score: 5 },
  {
    id: 3061,
    type: "code",
    title: "\u7F16\u7A0B\u9898\uFF1A\u7F16\u5199 C++ \u7A0B\u5E8F\uFF0C\u8BA1\u7B97\u6590\u6CE2\u90A3\u5951\u6570\u5217\u7684\u7B2C N \u9879\n\n\u8981\u6C42\uFF1A\n- \u4ECE\u952E\u76D8\u8F93\u5165\u6B63\u6574\u6570 N\uFF08N <= 40\uFF09\n- \u6590\u6CE2\u90A3\u5951\u6570\u5217\uFF1A1, 1, 2, 3, 5, 8, ...\uFF08\u7B2C 1\u30012 \u9879\u4E3A 1\uFF09\n- \u4F7F\u7528\u5FAA\u73AF\u8BA1\u7B97\u7B2C N \u9879\u5E76\u8F93\u51FA\n\n\u793A\u4F8B\u8F93\u5165\uFF1A\n7\n\n\u793A\u4F8B\u8F93\u51FA\uFF1A\n13",
    language: "cpp",
    score: 30,
    templateCode: "#include <iostream>\nusing namespace std;\n\nint main() {\n    int n;\n    cin >> n;\n    // \u8BF7\u5728\u4E0B\u65B9\u7F16\u5199\u8BA1\u7B97\u6590\u6CE2\u90A3\u5951\u7B2C n \u9879\u7684\u4EE3\u7801\n    \n    return 0;\n}\n"
  }
];
var scratch3Set = [
  { id: 3041, type: "choice", title: "Scratch \u4E2D\u7528\u4E8E\u5B58\u50A8\u4E00\u7EC4\u6570\u636E\uFF08\u5982\u591A\u4E2A\u59D3\u540D\uFF09\u7684\u7ED3\u6784\u662F\uFF1F\uFF08\u3000\uFF09", options: ["\u5217\u8868", "\u53D8\u91CF", "\u5E7F\u64AD", "\u514B\u9686"], answer: "A", analysis: "\u5217\u8868\u53EF\u4EE5\u4E00\u6B21\u5B58\u50A8\u591A\u4E2A\u6570\u636E\uFF0C\u53D8\u91CF\u53EA\u80FD\u5B58\u4E00\u4E2A\u503C\u3002", score: 5 },
  { id: 3042, type: "choice", title: 'Scratch \u4E2D"\u81EA\u5236\u79EF\u6728"\uFF08\u81EA\u5B9A\u4E49\u79EF\u6728\uFF09\u7684\u4F5C\u7528\u7C7B\u4F3C\u4E8E\u7F16\u7A0B\u4E2D\u7684\uFF1F\uFF08\u3000\uFF09', options: ["\u51FD\u6570", "\u53D8\u91CF", "\u5FAA\u73AF", "\u5217\u8868"], answer: "A", analysis: "\u81EA\u5236\u79EF\u6728\u628A\u4E00\u6BB5\u811A\u672C\u5C01\u88C5\u6210\u53EF\u91CD\u590D\u8C03\u7528\u7684\u5757\uFF0C\u7C7B\u4F3C\u51FD\u6570\u3002", score: 5 },
  { id: 3043, type: "choice", title: '\u628A\u4E24\u4E2A\u5B57\u7B26\u4E32"\u4F60\u597D"\u548C"\u4E16\u754C"\u8FDE\u63A5\u8D77\u6765\uFF0C\u7ED3\u679C\u662F\uFF1F\uFF08\u3000\uFF09', options: ["\u4F60\u597D\u4E16\u754C", "\u4F60\u597D \u4E16\u754C", "\u4E16\u754C\u4F60\u597D", "\u4F60\u597D\uFF0C\u4E16\u754C"], answer: "A", analysis: '\u5B57\u7B26\u4E32\u8FDE\u63A5\u628A\u4E24\u4E2A\u5B57\u7B26\u4E32\u9996\u5C3E\u76F8\u8FDE\uFF0C\u5F97\u5230"\u4F60\u597D\u4E16\u754C"\u3002', score: 5 },
  { id: 3044, type: "choice", title: "\u4ECE\u5217\u8868 [3, 8, 5, 2] \u4E2D\u627E\u51FA\u6700\u5927\u503C\uFF0C\u7ED3\u679C\u662F\uFF1F\uFF08\u3000\uFF09", options: ["8", "5", "3", "2"], answer: "A", analysis: "\u9010\u4E2A\u6BD4\u8F83\uFF0C\u6700\u5927\u503C\u662F 8\u3002", score: 5 },
  { id: 3045, type: "choice", title: '"\u5982\u679C <x> \u90A3\u4E48...\u5426\u5219..."\u79EF\u6728\u5C5E\u4E8E\uFF1F\uFF08\u3000\uFF09', options: ["\u6761\u4EF6\u5224\u65AD", "\u5FAA\u73AF", "\u4E8B\u4EF6", "\u8FD0\u7B97"], answer: "A", analysis: "\u8BE5\u79EF\u6728\u662F\u53CC\u5206\u652F\u6761\u4EF6\u5224\u65AD\u7ED3\u6784\u3002", score: 5 },
  { id: 3046, type: "choice", title: "\u5728 Scratch \u821E\u53F0\u4E2D\uFF0Cx \u5750\u6807\u589E\u5927\u4F1A\u8BA9\u89D2\u8272\u5411\u54EA\u4E2A\u65B9\u5411\u79FB\u52A8\uFF1F\uFF08\u3000\uFF09", options: ["\u5411\u53F3", "\u5411\u5DE6", "\u5411\u4E0A", "\u5411\u4E0B"], answer: "A", analysis: "x \u5750\u6807\u4EE3\u8868\u6C34\u5E73\u65B9\u5411\uFF0C\u589E\u5927\u5373\u5411\u53F3\u79FB\u52A8\u3002", score: 5 },
  { id: 3047, type: "choice", title: "\u60F3\u8BA9\u53D8\u91CF\u53EA\u88AB\u5F53\u524D\u89D2\u8272\u4F7F\u7528\uFF0C\u5E94\u5C06\u5176\u8BBE\u7F6E\u4E3A\uFF1F\uFF08\u3000\uFF09", options: ["\u4EC5\u9002\u7528\u4E8E\u5F53\u524D\u89D2\u8272", "\u9002\u7528\u4E8E\u6240\u6709\u89D2\u8272", "\u5168\u5C40\u53D8\u91CF", "\u7CFB\u7EDF\u53D8\u91CF"], answer: "A", analysis: '\u53D8\u91CF\u53EF\u8BBE\u4E3A"\u9002\u7528\u4E8E\u6240\u6709\u89D2\u8272"\u6216"\u4EC5\u9002\u7528\u4E8E\u5F53\u524D\u89D2\u8272"\u3002', score: 5 },
  { id: 3048, type: "judge", title: "Scratch \u7684\u5217\u8868\u53EF\u4EE5\u540C\u65F6\u5B58\u50A8\u6570\u5B57\u548C\u6587\u672C\u3002\uFF08\u3000\uFF09", answer: "true", analysis: "Scratch \u5217\u8868\u5143\u7D20\u53EF\u4EE5\u662F\u6570\u5B57\u6216\u6587\u672C\uFF0C\u7C7B\u578B\u4E0D\u56FA\u5B9A\u3002", score: 5 },
  { id: 3049, type: "judge", title: "Scratch \u7684\u81EA\u5236\u79EF\u6728\u53EF\u4EE5\u5E26\u53C2\u6570\uFF08\u8F93\u5165\u9879\uFF09\u3002\uFF08\u3000\uFF09", answer: "true", analysis: "\u81EA\u5236\u79EF\u6728\u53EF\u4EE5\u6DFB\u52A0\u6570\u5B57\u3001\u6587\u672C\u7B49\u8F93\u5165\u53C2\u6570\u3002", score: 5 },
  {
    id: 3050,
    type: "scratch",
    title: '\u7F16\u7A0B\u6311\u6218\uFF1A\u5236\u4F5C\u4E00\u4E2A"\u627E\u6700\u5927\u503C"\u7A0B\u5E8F\n\n\u8981\u6C42\uFF1A\n1. \u521B\u5EFA\u4E00\u4E2A\u5217\u8868\uFF0C\u5B58\u5165 5 \u4E2A\u968F\u673A\u6570\n2. \u4F7F\u7528\u53D8\u91CF\u8BB0\u5F55\u5F53\u524D\u6700\u5927\u503C\n3. \u9010\u4E2A\u6BD4\u8F83\u5217\u8868\u4E2D\u7684\u6570\n4. \u627E\u5230\u6700\u5927\u503C\u540E\uFF0C\u89D2\u8272\u8BF4\u51FA"\u6700\u5927\u503C\u662F X"\n\n\u63D0\u793A\uFF1A\u4F7F\u7528"\u5217\u8868""\u53D8\u91CF""\u91CD\u590D\u6267\u884C""\u5982\u679C...\u90A3\u4E48"\u7B49\u79EF\u6728',
    language: "scratch3",
    score: 30
  }
];
var scratchJr3Set = [
  { id: 10311, type: "choice", title: 'ScratchJr \u4E2D\u8BA9\u89D2\u8272"\u8BF4\u8BDD"\u7684\u79EF\u6728\u5C5E\u4E8E\u54EA\u4E2A\u7C7B\u522B\uFF1F\uFF08\u3000\uFF09', options: ["\u7D2B\u8272\uFF08\u5916\u89C2\uFF09", "\u84DD\u8272\uFF08\u8FD0\u52A8\uFF09", "\u9EC4\u8272\uFF08\u89E6\u53D1\uFF09", "\u7EFF\u8272\uFF08\u58F0\u97F3\uFF09"], answer: "A", analysis: "\u8BF4\u8BDD\u79EF\u6728\u5C5E\u4E8E\u5916\u89C2\u7C7B\uFF08\u7D2B\u8272\uFF09\uFF0C\u7528\u4E8E\u663E\u793A\u6587\u5B57\u6C14\u6CE1\u3002", score: 5 },
  { id: 10312, type: "choice", title: "\u60F3\u8BA9\u89D2\u8272\u53D8\u5927\uFF0C\u5E94\u4F7F\u7528\u54EA\u4E2A\u79EF\u6728\uFF1F\uFF08\u3000\uFF09", options: ["\u653E\u5927\u79EF\u6728", "\u7F29\u5C0F\u79EF\u6728", "\u79FB\u52A8\u79EF\u6728", "\u9690\u85CF\u79EF\u6728"], answer: "A", analysis: "\u653E\u5927\u79EF\u6728\u8BA9\u89D2\u8272\u5C3A\u5BF8\u53D8\u5927\u3002", score: 5 },
  { id: 10313, type: "choice", title: "ScratchJr \u4E2D\u7ED3\u675F\u7A0B\u5E8F\u7684\u79EF\u6728\u662F\uFF1F\uFF08\u3000\uFF09", options: ['\u7EA2\u8272"\u7ED3\u675F"\u79EF\u6728', "\u7EFF\u65D7\u79EF\u6728", "\u9EC4\u8272\u79EF\u6728", "\u84DD\u8272\u79EF\u6728"], answer: "A", analysis: "\u7EA2\u8272\u7ED3\u675F\u79EF\u6728\u7528\u4E8E\u7EC8\u6B62\u7A0B\u5E8F\u8FD0\u884C\u3002", score: 5 },
  { id: 10314, type: "choice", title: "\u9EC4\u8272\u89E6\u53D1\u7C7B\u79EF\u6728\u7684\u4F5C\u7528\u662F\uFF1F\uFF08\u3000\uFF09", options: ["\u542F\u52A8\u4E00\u6BB5\u7A0B\u5E8F", "\u79FB\u52A8\u89D2\u8272", "\u6539\u53D8\u5916\u89C2", "\u64AD\u653E\u58F0\u97F3"], answer: "A", analysis: "\u89E6\u53D1\u7C7B\u79EF\u6728\uFF08\u5982\u7EFF\u65D7\uFF09\u4F5C\u4E3A\u7A0B\u5E8F\u542F\u52A8\u7684\u5165\u53E3\u3002", score: 5 },
  { id: 10315, type: "choice", title: "\u8BA9\u4E00\u6BB5\u52A8\u4F5C\u91CD\u590D\u6267\u884C\uFF0C\u5E94\u4F7F\u7528\uFF1F\uFF08\u3000\uFF09", options: ["\u5FAA\u73AF\uFF08\u91CD\u590D\uFF09\u79EF\u6728", "\u7ED3\u675F\u79EF\u6728", "\u8BF4\u8BDD\u79EF\u6728", "\u79FB\u52A8\u79EF\u6728"], answer: "A", analysis: "\u91CD\u590D\u79EF\u6728\u8BA9\u5B83\u5185\u90E8\u7684\u79EF\u6728\u5FAA\u73AF\u6267\u884C\u3002", score: 5 },
  { id: 10316, type: "choice", title: "\u5207\u6362\u821E\u53F0\u80CC\u666F\uFF0C\u5E94\u4F7F\u7528\uFF1F\uFF08\u3000\uFF09", options: ["\u66F4\u6362\u80CC\u666F\u79EF\u6728", "\u6DFB\u52A0\u89D2\u8272\u79EF\u6728", "\u79FB\u52A8\u79EF\u6728", "\u5F55\u97F3\u79EF\u6728"], answer: "A", analysis: "\u66F4\u6362\u80CC\u666F\u79EF\u6728\u53EF\u4EE5\u5207\u6362\u4E0D\u540C\u7684\u821E\u53F0\u80CC\u666F\u3002", score: 5 },
  { id: 10317, type: "judge", title: "ScratchJr \u4E2D\u53EF\u4EE5\u4E3A\u89D2\u8272\u5F55\u5236\u81EA\u5DF1\u7684\u58F0\u97F3\u3002\uFF08\u3000\uFF09", answer: "true", analysis: "ScratchJr \u63D0\u4F9B\u5F55\u97F3\u529F\u80FD\uFF0C\u53EF\u4E3A\u89D2\u8272\u6DFB\u52A0\u81EA\u5B9A\u4E49\u58F0\u97F3\u3002", score: 5 },
  { id: 10318, type: "judge", title: "ScratchJr \u4E2D\u89D2\u8272\u7684\u5927\u5C0F\u53EF\u4EE5\u6539\u53D8\u3002\uFF08\u3000\uFF09", answer: "true", analysis: "\u53EF\u901A\u8FC7\u653E\u5927/\u7F29\u5C0F\u79EF\u6728\u6539\u53D8\u89D2\u8272\u5927\u5C0F\u3002", score: 5 },
  {
    id: 10319,
    type: "scratchjr",
    title: '\u521B\u610F\u6311\u6218\uFF1A\u4F7F\u7528 ScratchJr \u5236\u4F5C"\u5C0F\u732B\u53D8\u5927\u53D8\u5C0F"\u52A8\u753B\n\n\u8981\u6C42\uFF1A\n1. \u5C0F\u732B\u521D\u59CB\u4E3A\u6B63\u5E38\u5927\u5C0F\n2. \u5148\u53D8\u5927\u518D\u53D8\u5C0F\n3. \u5FAA\u73AF\u6267\u884C\u4EE5\u4E0A\u52A8\u4F5C\n\n\u63D0\u793A\uFF1A\u4F7F\u7528"\u653E\u5927""\u7F29\u5C0F""\u91CD\u590D"\u7B49\u79EF\u6728\u5757',
    language: "scratchjr",
    score: 30
  }
];
var turtle3Set = [
  { id: 10811, type: "choice", title: 'turtle.fillcolor("red") \u8BBE\u7F6E\u7684\u662F\uFF1F\uFF08\u3000\uFF09', options: ["\u586B\u5145\u989C\u8272", "\u753B\u7B14\u989C\u8272", "\u80CC\u666F\u989C\u8272", "\u7EBF\u6761\u7C97\u7EC6"], answer: "A", analysis: "fillcolor() \u8BBE\u7F6E\u586B\u5145\u989C\u8272\uFF0Cpencolor() \u8BBE\u7F6E\u753B\u7B14\u989C\u8272\u3002", score: 5 },
  { id: 10812, type: "choice", title: "\u5728 begin_fill() \u548C end_fill() \u4E4B\u95F4\u753B\u7684\u95ED\u5408\u56FE\u5F62\u4F1A\uFF1F\uFF08\u3000\uFF09", options: ["\u88AB\u586B\u5145\u989C\u8272", "\u88AB\u5220\u9664", "\u53D8\u6210\u865A\u7EBF", "\u81EA\u52A8\u4FDD\u5B58"], answer: "A", analysis: "\u4E24\u8005\u4E4B\u95F4\u7684\u95ED\u5408\u533A\u57DF\u4F1A\u7528 fillcolor \u989C\u8272\u586B\u5145\u3002", score: 5 },
  { id: 10813, type: "choice", title: "turtle.circle(50, 180) \u4F1A\u753B\u51FA\uFF1F\uFF08\u3000\uFF09", options: ["\u534A\u5706\uFF08\u5F27\uFF09", "\u5B8C\u6574\u5706", "\u56DB\u5206\u4E4B\u4E00\u5706", "\u76F4\u7EBF"], answer: "A", analysis: "\u7B2C\u4E8C\u4E2A\u53C2\u6570\u662F\u5F27\u5EA6\u89D2\u5EA6\uFF0C180 \u5EA6\u5373\u534A\u5706\u3002", score: 5 },
  { id: 10814, type: "choice", title: "turtle.goto(100, 100) \u7684\u4F5C\u7528\u662F\uFF1F\uFF08\u3000\uFF09", options: ["\u79FB\u52A8\u5230\u5750\u6807 (100,100)", "\u65CB\u8F6C 100 \u5EA6", "\u524D\u8FDB 100", "\u753B\u5706"], answer: "A", analysis: "goto(x, y) \u8BA9\u6D77\u9F9F\u79FB\u52A8\u5230\u6307\u5B9A\u5750\u6807\u3002", score: 5 },
  { id: 10815, type: "choice", title: "turtle.home() \u7684\u4F5C\u7528\u662F\uFF1F\uFF08\u3000\uFF09", options: ["\u56DE\u5230\u539F\u70B9 (0,0) \u5E76\u671D\u5411\u521D\u59CB\u65B9\u5411", "\u5173\u95ED\u7A97\u53E3", "\u6E05\u5C4F", "\u753B\u623F\u5B50"], answer: "A", analysis: "home() \u8BA9\u6D77\u9F9F\u56DE\u5230\u539F\u70B9\u5E76\u6062\u590D\u9ED8\u8BA4\u671D\u5411\u3002", score: 5 },
  { id: 10816, type: "choice", title: "turtle.seth(90) \u8BBE\u7F6E\u7684\u662F\uFF1F\uFF08\u3000\uFF09", options: ["\u6D77\u9F9F\u671D\u5411", "\u753B\u7B14\u989C\u8272", "\u753B\u5E03\u5927\u5C0F", "\u901F\u5EA6"], answer: "A", analysis: "seth(\u89D2\u5EA6) \u8BBE\u7F6E\u6D77\u9F9F\u7684\u7EDD\u5BF9\u671D\u5411\u3002", score: 5 },
  { id: 10817, type: "choice", title: 'turtle.dot(10, "blue") \u7684\u4F5C\u7528\u662F\uFF1F\uFF08\u3000\uFF09', options: ["\u753B\u4E00\u4E2A\u84DD\u8272\u5706\u70B9", "\u753B\u4E00\u6761\u7EBF", "\u79FB\u52A8\u6D77\u9F9F", "\u8BBE\u7F6E\u901F\u5EA6"], answer: "A", analysis: "dot(\u76F4\u5F84, \u989C\u8272) \u5728\u5F53\u524D\u4F4D\u7F6E\u753B\u4E00\u4E2A\u5B9E\u5FC3\u5706\u70B9\u3002", score: 5 },
  { id: 10818, type: "choice", title: "turtle \u753B\u5E03\u5750\u6807\u7CFB\u7684\u9ED8\u8BA4\u8303\u56F4\uFF08\u4EE5\u539F\u70B9\u4E3A\u4E2D\u5FC3\uFF09\u5927\u81F4\u662F\uFF1F\uFF08\u3000\uFF09", options: ["\u6B63\u8D1F\u5404\u6570\u767E\u50CF\u7D20", "0 \u5230 100", "\u65E0\u9650\u5927", "\u53EA\u6709\u6B63\u6570"], answer: "A", analysis: "\u9ED8\u8BA4\u7A97\u53E3\u7EA6 400\xD7300\uFF0C\u5750\u6807\u4ECE\u4E2D\u5FC3\u5411\u56DB\u5468\u5EF6\u4F38\u6570\u767E\u50CF\u7D20\u3002", score: 5 },
  { id: 10819, type: "judge", title: "Python turtle \u753B\u5E03\u7684\u5750\u6807\u539F\u70B9\u9ED8\u8BA4\u5728\u753B\u5E03\u6B63\u4E2D\u592E\u3002\uFF08\u3000\uFF09", answer: "true", analysis: "turtle \u5750\u6807\u539F\u70B9 (0,0) \u9ED8\u8BA4\u4F4D\u4E8E\u7A97\u53E3\u4E2D\u592E\u3002", score: 5 },
  { id: 10820, type: "judge", title: "turtle.circle(50, 360) \u4F1A\u753B\u51FA\u4E00\u4E2A\u5B8C\u6574\u7684\u5706\u3002\uFF08\u3000\uFF09", answer: "true", analysis: "360 \u5EA6\u5F27\u7EBF\u5373\u5B8C\u6574\u5706\u5468\u3002", score: 5 },
  {
    id: 10821,
    type: "code",
    title: '\u7ED8\u56FE\u6311\u6218\uFF1A\u4F7F\u7528 Python Turtle \u7ED8\u5236\u4E00\u6735"\u82B1"\n\n\u8981\u6C42\uFF1A\n1. \u82B1\u7531\u591A\u4E2A\u5706\u5F62\u82B1\u74E3\u7EC4\u6210\n2. \u4F7F\u7528 for \u5FAA\u73AF\u7ED8\u5236\n3. \u6BCF\u753B\u5B8C\u4E00\u4E2A\u82B1\u74E3\u65CB\u8F6C\u4E00\u5B9A\u89D2\u5EA6\n\n\u63D0\u793A\uFF1A\n- \u4F7F\u7528 turtle.circle() \u753B\u82B1\u74E3\n- \u82B1\u74E3\u6570\u91CF\u5EFA\u8BAE 6-8 \u4E2A\uFF0C\u6BCF\u6B21\u65CB\u8F6C 360/\u82B1\u74E3\u6570 \u5EA6',
    language: "python",
    score: 30,
    templateCode: 'import turtle\n\nt = turtle.Turtle()\nt.speed(5)\nt.color("pink")\n\n# \u8BF7\u5728\u4E0B\u65B9\u7F16\u5199\u753B\u82B1\u7684\u4EE3\u7801\n\n\nturtle.done()\n'
  }
];
var goc3Set = [
  { id: 4031, type: "choice", title: "GoC \u4E2D\u753B\u7A7A\u5FC3\u5706\u7684\u547D\u4EE4\u662F\uFF1F\uFF08\u3000\uFF09", options: ["o()", "oo()", "circle()", "round()"], answer: "A", analysis: "o(\u534A\u5F84, \u989C\u8272) \u753B\u7A7A\u5FC3\u5706\uFF0Coo() \u753B\u5B9E\u5FC3\u5706\u3002", score: 5 },
  { id: 4032, type: "choice", title: "GoC \u4E2D oo(50, 1) \u753B\u7684\u662F\uFF1F\uFF08\u3000\uFF09", options: ["\u534A\u5F84\u4E3A 50 \u7684\u7EA2\u8272\u5B9E\u5FC3\u5706", "\u534A\u5F84\u4E3A 50 \u7684\u7EA2\u8272\u7A7A\u5FC3\u5706", "\u76F4\u5F84\u4E3A 50 \u7684\u5706", "\u8FB9\u957F\u4E3A 50 \u7684\u5706"], answer: "A", analysis: "oo(\u534A\u5F84, \u989C\u8272\u53F7) \u753B\u5B9E\u5FC3\u5706\uFF0C\u989C\u8272\u53F7 1 \u662F\u7EA2\u8272\u3002", score: 5 },
  { id: 4033, type: "choice", title: "\u8BBE\u7F6E\u753B\u7B14\u7C97\u7EC6\u7684\u547D\u4EE4\u662F\uFF1F\uFF08\u3000\uFF09", options: ["pen.size()", "pen.color()", "pen.speed()", "pen.width2()"], answer: "A", analysis: "pen.size(n) \u8BBE\u7F6E\u753B\u7B14\u7C97\u7EC6\u3002", score: 5 },
  { id: 4034, type: "choice", title: "\u5148\u753B\u4E00\u4E2A\u7A7A\u5FC3\u5706\uFF0C\u518D\u5728\u91CC\u9762\u753B\u4E00\u4E2A\u5B9E\u5FC3\u5706\uFF0C\u53EF\u4EE5\u7EC4\u6210\uFF1F\uFF08\u3000\uFF09", options: ["\u5706\u73AF", "\u6B63\u65B9\u5F62", "\u4E09\u89D2\u5F62", "\u76F4\u7EBF"], answer: "A", analysis: "\u7A7A\u5FC3\u5706\u5957\u5B9E\u5FC3\u5706\u5F62\u6210\u5706\u73AF\u6548\u679C\u3002", score: 5 },
  { id: 4035, type: "choice", title: "\u8BBE\u7F6E\u989C\u8272\u4E3A\u7EFF\u8272\uFF0C\u6B63\u786E\u7684 RGB \u662F\uFF1F\uFF08\u3000\uFF09", options: ["pen.color(0, 255, 0)", "pen.color(255, 0, 0)", "pen.color(0, 0, 255)", "pen.color(255, 255, 0)"], answer: "A", analysis: "\u7EFF\u8272 RGB \u4E3A (0, 255, 0)\u3002", score: 5 },
  { id: 4036, type: "choice", title: "\u4F7F\u7528 for \u5FAA\u73AF\u753B 6 \u4E2A\u5706\u6392\u6210\u4E00\u5708\uFF0C\u6BCF\u753B\u4E00\u4E2A\u5706\u9700\u8981\u65CB\u8F6C\u591A\u5C11\u5EA6\uFF1F\uFF08\u3000\uFF09", options: ["60", "90", "120", "45"], answer: "A", analysis: "360/6 = 60 \u5EA6\uFF0C\u6BCF\u4E2A\u5706\u4E4B\u95F4\u65CB\u8F6C 60 \u5EA6\u3002", score: 5 },
  { id: 4037, type: "choice", title: "\u9690\u85CF\u753B\u7B14\uFF08\u4E0D\u663E\u793A\u6D77\u9F9F\u56FE\u6807\uFF09\uFF0C\u5E94\u4F7F\u7528\uFF1F\uFF08\u3000\uFF09", options: ["pen.hide()", "pen.show()", "pen.up()", "pen.down()"], answer: "A", analysis: "pen.hide() \u9690\u85CF\u753B\u7B14\u56FE\u6807\uFF0Cshow() \u663E\u793A\u3002", score: 5 },
  { id: 4038, type: "judge", title: "GoC \u4E2D oo(50, 1) \u753B\u7684\u662F\u5B9E\u5FC3\u5706\u3002\uFF08\u3000\uFF09", answer: "true", analysis: "oo \u662F\u753B\u5B9E\u5FC3\u5706\u7684\u547D\u4EE4\u3002", score: 5 },
  { id: 4039, type: "judge", title: "GoC \u4E2D bk(100) \u8868\u793A\u524D\u8FDB 100\u3002\uFF08\u3000\uFF09", answer: "false", analysis: "bk \u662F\u540E\u9000\u547D\u4EE4\uFF0Cbk(100) \u8868\u793A\u540E\u9000 100\u3002", score: 5 },
  {
    id: 4040,
    type: "code",
    title: '\u7F16\u7A0B\u9898\uFF1A\u4F7F\u7528 GoC \u7ED8\u5236\u4E00\u6735"\u82B1"\n\n\u8981\u6C42\uFF1A\n- \u82B1\u7531 6 \u4E2A\u5706\u5F62\u82B1\u74E3\u7EC4\u6210\n- \u4F7F\u7528 for \u5FAA\u73AF\u7ED8\u5236\n- \u6BCF\u4E2A\u82B1\u74E3\u7ED5\u4E2D\u5FC3\u65CB\u8F6C 60 \u5EA6\n\n\u63D0\u793A\uFF1A\n- \u4F7F\u7528 o() \u6216 oo() \u753B\u82B1\u74E3\n- \u6BCF\u753B\u4E00\u4E2A\u82B1\u74E3\uFF0C\u65CB\u8F6C 60 \u5EA6',
    language: "goc",
    score: 30,
    templateCode: "#include <graphics.h>\nint main() {\n    initGraph(800, 600);\n    // \u8BF7\u5728\u4E0B\u65B9\u7F16\u5199\u753B\u82B1\u7684\u4EE3\u7801\n    \n    closeGraph();\n    return 0;\n}\n"
  }
];
var examQuestionsMap = {
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
  105: pythonSet
};
var examPaperList = [
  { id: 201, title: "2025\u5E74 GESP \u8BA4\u8BC1 Python \u4E00\u7EA7\u771F\u9898", language: "python", languageLabel: "Python", level: "\u4E00\u7EA7", duration: 60, totalScore: 85, passScore: 51, questionCount: 12, tag: "GESP \u8BA4\u8BC1" },
  { id: 202, title: "2025\u5E74 GESP \u8BA4\u8BC1 C++ \u4E00\u7EA7\u771F\u9898", language: "cpp", languageLabel: "C++", level: "\u4E00\u7EA7", duration: 60, totalScore: 85, passScore: 51, questionCount: 12, tag: "GESP \u8BA4\u8BC1" },
  { id: 203, title: "2025\u5E74 GESP \u8BA4\u8BC1 Scratch \u4E00\u7EA7\u771F\u9898", language: "scratch3", languageLabel: "Scratch", level: "\u4E00\u7EA7", duration: 60, totalScore: 75, passScore: 45, questionCount: 10, tag: "GESP \u8BA4\u8BC1" },
  { id: 204, title: "ScratchJr \u57FA\u7840\u6D4B\u8BC4", language: "scratchjr", languageLabel: "ScratchJr", level: "\u57FA\u7840", duration: 45, totalScore: 70, passScore: 42, questionCount: 9, tag: "\u57FA\u7840\u6D4B\u8BC4" },
  { id: 205, title: "Python Turtle \u7ED8\u56FE\u57FA\u7840\u6D4B\u8BD5", language: "pythonturtle", languageLabel: "Python Turtle", level: "\u57FA\u7840", duration: 45, totalScore: 80, passScore: 48, questionCount: 11, tag: "\u57FA\u7840\u6D4B\u8BC4" },
  { id: 206, title: "GoC \u56FE\u5F62\u7F16\u7A0B\u4E00\u7EA7\u9898\u76EE", language: "goc", languageLabel: "GoC", level: "\u4E00\u7EA7", duration: 45, totalScore: 80, passScore: 48, questionCount: 11, tag: "\u57FA\u7840\u6D4B\u8BC4" },
  { id: 207, title: "2025\u5E74 GESP \u8BA4\u8BC1 Python \u4E8C\u7EA7\u771F\u9898", language: "python", languageLabel: "Python", level: "\u4E8C\u7EA7", duration: 60, totalScore: 85, passScore: 51, questionCount: 11, tag: "GESP \u8BA4\u8BC1" },
  { id: 208, title: "2025\u5E74 GESP \u8BA4\u8BC1 C++ \u4E8C\u7EA7\u771F\u9898", language: "cpp", languageLabel: "C++", level: "\u4E8C\u7EA7", duration: 60, totalScore: 85, passScore: 51, questionCount: 11, tag: "GESP \u8BA4\u8BC1" },
  { id: 209, title: "2025\u5E74 GESP \u8BA4\u8BC1 Scratch \u4E8C\u7EA7\u771F\u9898", language: "scratch3", languageLabel: "Scratch", level: "\u4E8C\u7EA7", duration: 60, totalScore: 75, passScore: 45, questionCount: 10, tag: "GESP \u8BA4\u8BC1" },
  { id: 210, title: "ScratchJr \u57FA\u7840\u8FDB\u9636\u6D4B\u8BC4", language: "scratchjr", languageLabel: "ScratchJr", level: "\u8FDB\u9636", duration: 45, totalScore: 70, passScore: 42, questionCount: 9, tag: "\u8FDB\u9636\u6D4B\u8BC4" },
  { id: 211, title: "Python Turtle \u7ED8\u56FE\u8FDB\u9636\u6D4B\u8BD5", language: "pythonturtle", languageLabel: "Python Turtle", level: "\u8FDB\u9636", duration: 45, totalScore: 80, passScore: 48, questionCount: 11, tag: "\u8FDB\u9636\u6D4B\u8BC4" },
  { id: 212, title: "GoC \u56FE\u5F62\u7F16\u7A0B\u8FDB\u9636\u9898\u76EE", language: "goc", languageLabel: "GoC", level: "\u8FDB\u9636", duration: 45, totalScore: 80, passScore: 48, questionCount: 10, tag: "\u8FDB\u9636\u6D4B\u8BC4" },
  { id: 213, title: "2025\u5E74 GESP \u8BA4\u8BC1 Python \u4E09\u7EA7\u771F\u9898", language: "python", languageLabel: "Python", level: "\u4E09\u7EA7", duration: 60, totalScore: 85, passScore: 51, questionCount: 11, tag: "GESP \u8BA4\u8BC1" },
  { id: 214, title: "2025\u5E74 GESP \u8BA4\u8BC1 C++ \u4E09\u7EA7\u771F\u9898", language: "cpp", languageLabel: "C++", level: "\u4E09\u7EA7", duration: 60, totalScore: 85, passScore: 51, questionCount: 11, tag: "GESP \u8BA4\u8BC1" },
  { id: 215, title: "2025\u5E74 GESP \u8BA4\u8BC1 Scratch \u4E09\u7EA7\u771F\u9898", language: "scratch3", languageLabel: "Scratch", level: "\u4E09\u7EA7", duration: 60, totalScore: 75, passScore: 45, questionCount: 10, tag: "GESP \u8BA4\u8BC1" },
  { id: 216, title: "ScratchJr \u7EFC\u5408\u6D4B\u8BC4", language: "scratchjr", languageLabel: "ScratchJr", level: "\u7EFC\u5408", duration: 45, totalScore: 70, passScore: 42, questionCount: 9, tag: "\u7EFC\u5408\u6D4B\u8BC4" },
  { id: 217, title: "Python Turtle \u7ED8\u56FE\u7EFC\u5408\u6D4B\u8BD5", language: "pythonturtle", languageLabel: "Python Turtle", level: "\u7EFC\u5408", duration: 45, totalScore: 80, passScore: 48, questionCount: 11, tag: "\u7EFC\u5408\u6D4B\u8BC4" },
  { id: 218, title: "GoC \u56FE\u5F62\u7F16\u7A0B\u7EFC\u5408\u9898\u76EE", language: "goc", languageLabel: "GoC", level: "\u7EFC\u5408", duration: 45, totalScore: 80, passScore: 48, questionCount: 10, tag: "\u7EFC\u5408\u6D4B\u8BC4" }
];
var paperQuestionMap = {
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
  218: goc3Set
};
function getExamQuestions(examId) {
  return examQuestionsMap[examId] || paperQuestionMap[examId] || fallbackSet;
}
function groupQuestionsByType(questions) {
  const buckets = {};
  for (const q of questions) {
    ;
    (buckets[q.type] ||= []).push(q);
  }
  return QUESTION_TYPE_ORDER.filter((t) => buckets[t]?.length).map((t) => ({ type: t, label: QUESTION_TYPE_LABEL[t], items: buckets[t] }));
}
function calcTotalScore(questions) {
  return questions.reduce((sum, q) => sum + (q.score || 0), 0);
}
var examMetaMap = {
  1: { id: 1, title: "Python \u7B97\u6CD5\u6311\u6218\u8D5B", duration: 120, passScore: 60 },
  2: { id: 2, title: "Python Turtle \u521B\u610F\u7F16\u7A0B\u5927\u8D5B", duration: 150, passScore: 60 },
  3: { id: 3, title: "Scratch \u56FE\u5F62\u5316\u7F16\u7A0B\u5927\u8D5B", duration: 90, passScore: 60 },
  4: { id: 4, title: "GoC \u56FE\u5F62\u7F16\u7A0B\u9080\u8BF7\u8D5B", duration: 120, passScore: 60 },
  5: { id: 5, title: "C++ \u4FE1\u606F\u5B66\u5965\u8D5B\uFF08CSP-J/S\uFF09", duration: 240, passScore: 60 },
  6: { id: 6, title: "\u84DD\u6865\u676F\u591A\u8BED\u8A00\u7F16\u7A0B\u5927\u8D5B", duration: 240, passScore: 60 },
  7: { id: 7, title: "AI \u4EBA\u5DE5\u667A\u80FD\u521B\u65B0\u5927\u8D5B", duration: 180, passScore: 60 },
  8: { id: 8, title: "C++ \u7B97\u6CD5\u7CBE\u82F1\u8D5B", duration: 180, passScore: 60 },
  101: { id: 101, title: "Python \u7F16\u7A0B\u4F53\u9A8C\u8D5B", duration: 60, passScore: 30 },
  102: { id: 102, title: "Scratch \u521B\u610F\u4F53\u9A8C\u8D5B", duration: 45, passScore: 24 },
  103: { id: 103, title: "C++ \u7B97\u6CD5\u4F53\u9A8C\u8D5B", duration: 60, passScore: 30 },
  104: { id: 104, title: "GoC \u56FE\u5F62\u7F16\u7A0B\u4F53\u9A8C\u8D5B", duration: 45, passScore: 24 },
  105: { id: 105, title: "AI \u4EBA\u5DE5\u667A\u80FD\u4F53\u9A8C\u8D5B", duration: 60, passScore: 30 }
};
function getExamMeta(examId) {
  if (examMetaMap[examId]) return examMetaMap[examId];
  const paper = examPaperList.find((p) => p.id === examId);
  if (paper) {
    return { id: paper.id, title: paper.title, duration: paper.duration, passScore: paper.passScore };
  }
  return { id: examId, title: "\u5728\u7EBF\u8003\u8BD5", duration: 60, passScore: 60 };
}
// Annotate the CommonJS export names for ESM import in node:
0 && (module.exports = {
  QUESTION_TYPE_LABEL,
  QUESTION_TYPE_ORDER,
  calcTotalScore,
  examMetaMap,
  examPaperList,
  examQuestionsMap,
  getExamMeta,
  getExamQuestions,
  groupQuestionsByType,
  scratchJrSet
});
