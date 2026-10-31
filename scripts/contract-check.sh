#!/usr/bin/env bash
# ============================================================================
# 契约校验脚本（同步闭环的"自动抓漂移"）
# 用途：每次后端改动后运行，确认关键端点"路由已对齐"（非 404），且分页含 list 字段。
# 用法：bash scripts/contract-check.sh [base_url]
# 说明：无需登录即可检测路由漂移（401=路由命中，404=契约错位）。
# ============================================================================
set -u
BASE="${1:-http://localhost:8080}"
PASS=0
FAIL=0

# 期望"路由命中"（2xx 或 401 均算命中，404 算漂移）
check_route() {
  local method="$1"; local path="$2"; local desc="$3"
  local code
  code=$(curl -s -m 8 -o /dev/null -w "%{http_code}" -X "$method" "$BASE$path")
  if [ "$code" = "404" ]; then
    echo "  [漂移] $method $path -> 404 (契约错位!)  [$desc]"
    FAIL=$((FAIL+1))
  else
    echo "  [OK]   $method $path -> $code  [$desc]"
    PASS=$((PASS+1))
  fi
}

echo "===== 契约校验 @ $BASE ====="

echo "-- 考试模块（应与前端 src/api/exam.ts 对齐）--"
check_route GET  "/api/exam/list"            "考试列表"
check_route GET  "/api/exam/records"         "我的考试记录"
check_route GET  "/api/exam/result/query"    "成绩查询(公开)"
check_route POST "/api/exam/1/enter"         "进入考试(段序)"
check_route POST "/api/exam/1/submit"        "提交考试(段序)"
check_route POST "/api/exam/1/switch-screen" "切屏上报"
check_route GET  "/api/exam/grading/tasks"   "批改任务"
check_route POST "/api/exam/grading/grade"   "批改试卷"
check_route GET  "/api/exam/paper/201/questions" "试卷题目(题库单源)"
check_route GET  "/api/oss/list" "OSS文件列表"

echo "-- 成长体系（应与前端 src/api/growth.ts 对齐 /growth/*）--"
check_route GET  "/api/growth/points"        "积分概览"
check_route GET  "/api/growth/points/records" "积分记录"
check_route GET  "/api/growth/medals"        "勋章列表"
check_route GET  "/api/growth/level"          "等级信息"
check_route GET  "/api/growth/leaderboard"    "排行榜"
check_route GET  "/api/growth/weekly-report"  "学习周报"

echo "-- 支付（应与前端 src/api/payment.ts 对齐 /payment/*）--"
check_route POST "/api/payment/order/create" "创建订单"
check_route GET  "/api/payment/order/list"   "订单列表"
check_route GET  "/api/payment/order/detail/1" "订单详情"
check_route POST "/api/payment/pay"          "发起支付"
check_route GET  "/api/payment/status/1"     "支付状态"
check_route PUT  "/api/payment/order/1/cancel" "取消订单"
check_route POST "/api/payment/order/1/refund" "申请退款"

echo "-- 后台管理（聚合 AdminController：/api/admin/* 对齐前端 admin 页）--"
check_route GET "/api/admin/student/list"     "学员列表"
check_route GET "/api/admin/teacher/list"     "教师列表"
check_route GET "/api/admin/account/list"     "后台账号列表"
check_route GET "/api/admin/course/list"      "课程列表"
check_route GET "/api/admin/class/list"       "班级列表"
check_route GET "/api/admin/exam/list"        "赛事列表"
check_route GET "/api/admin/banner/list"      "Banner列表"
check_route GET "/api/admin/registration/list" "报名列表"
check_route GET "/api/admin/question/list"    "题库列表"
check_route GET "/api/admin/paper/list"       "试卷列表"
check_route GET "/api/admin/score/list"       "成绩列表"
check_route GET "/api/admin/practice/list"    "练习列表"
check_route GET "/api/admin/marketing/plans"  "营销套餐"
check_route GET "/api/admin/marketing/coupons" "优惠券"
check_route GET "/api/admin/file/list"        "文件列表"
check_route GET "/api/admin/news/list"        "资讯列表"
check_route GET "/api/admin/category/list"    "分类列表"
check_route GET "/api/admin/partner/list"     "合作伙伴列表"
check_route GET "/api/admin/notification/list" "通知列表"
check_route GET "/api/admin/dashboard/stats"  "仪表盘统计"
check_route GET "/api/admin/settings"         "系统设置"

echo "-- 后台管理写操作（对齐前端 useCrud：create / PUT {id} / DELETE {id} / batch-delete）--"
for r in course class exam registration banner news category partner notification question paper score practice file; do
  check_route POST   "/api/admin/$r/create"        "新建$r"
  check_route PUT    "/api/admin/$r/1"             "更新$r"
  check_route DELETE "/api/admin/$r/1"             "删除$r"
  check_route POST   "/api/admin/$r/batch-delete"  "批量删除$r"
done
check_route POST   "/api/admin/marketing/plans/create"        "新建营销套餐"
check_route PUT    "/api/admin/marketing/plans/1"             "更新营销套餐"
check_route DELETE "/api/admin/marketing/plans/1"             "删除营销套餐"
check_route POST   "/api/admin/marketing/plans/batch-delete"  "批量删除营销套餐"
check_route POST   "/api/admin/marketing/coupons/create"       "新建优惠券"
check_route PUT    "/api/admin/marketing/coupons/1"            "更新优惠券"
check_route DELETE "/api/admin/marketing/coupons/1"            "删除优惠券"
check_route POST   "/api/admin/marketing/coupons/batch-delete" "批量删除优惠券"

echo "-- 用户类写操作（student/teacher/account 共用 sys_user，对齐 AdminWriteController）--"
for r in student teacher account; do
  check_route POST   "/api/admin/$r/create"        "新建$r"
  check_route PUT    "/api/admin/$r/1"             "更新$r"
  check_route DELETE "/api/admin/$r/1"             "删除$r"
  check_route POST   "/api/admin/$r/batch-delete"  "批量删除$r"
done

echo "-- 系统设置写操作（PUT /api/admin/settings upsert 扁平 KV）--"
check_route PUT    "/api/admin/settings"           "保存系统设置"

echo "-- 分页信封（公开列表应含 list 字段）--"
LIST_FIELD=$(curl -s -m 8 "$BASE/api/news?page=1&size=1" | grep -o '"list"' | head -1)
if [ -n "$LIST_FIELD" ]; then
  echo "  [OK]   /api/news 分页含 list 字段（前端可读 data.list）"
  PASS=$((PASS+1))
else
  echo "  [漂移] /api/news 分页缺少 list 字段（前端将取空）"
  FAIL=$((FAIL+1))
fi

echo "===== 结果：通过 $PASS / 失败 $FAIL ====="
if [ "$FAIL" -gt 0 ]; then
  echo "存在契约漂移，请检查后端路由与前端 src/api 是否一致。"
  exit 1
fi
echo "契约校验通过：无 404 漂移，分页信封正常。"
