#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Judge0 评测引擎端到端验证脚本
================================
使用前：
  1. 启动 Docker 与评测引擎
       docker compose -f docker/judge0/docker-compose.yml up -d
  2. 等待引擎就绪（curl http://localhost:2358/ 返回版本信息）
  3. 运行本脚本：
       python3 docker/judge0/verify.py
       或指定地址：ENGINE_URL=http://localhost:2358 python3 verify.py

说明：
  本脚本仅验证“评测引擎本身可用 + 判分语义正确”（AC / WA / TLE 等），
  不依赖业务数据库。后端 -> 评测引擎 的完整串联（建题 + 测试用例 + 提交判分）
  请见 docker/judge0/README.md 第 5 节。
"""
import base64
import json
import os
import sys
import urllib.request
import urllib.error

ENGINE_URL = os.environ.get("ENGINE_URL", "http://localhost:2358").rstrip("/")

LANG_PYTHON = 71
LANG_CPP = 54

STATUS = {
    3: "ACCEPTED",
    4: "WRONG_ANSWER",
    5: "TIME_LIMIT_EXCEEDED",
    6: "COMPILE_ERROR",
    7: "RUNTIME_ERROR (SIGSEGV)",
    8: "RUNTIME_ERROR (NZEC)",
    13: "MEMORY_LIMIT_EXCEEDED",
}


def b64(s: str) -> str:
    return base64.b64encode(s.encode("utf-8")).decode("ascii")


def post_submission(payload: dict) -> dict:
    url = ENGINE_URL + "/submissions?wait=true&base64=true"
    data = json.dumps(payload).encode("utf-8")
    req = urllib.request.Request(
        url, data=data, headers={"Content-Type": "application/json"}
    )
    with urllib.request.urlopen(req, timeout=40) as resp:
        return json.loads(resp.read().decode("utf-8"))


def ping() -> bool:
    try:
        with urllib.request.urlopen(ENGINE_URL + "/", timeout=10) as resp:
            body = resp.read().decode("utf-8", "ignore")
            info = body[:160].replace("\n", " ").replace("\r", " ")
            print("  引擎版本信息:", info)
            return resp.status == 200
    except Exception as e:  # noqa
        print("  引擎不可达:", e)
        return False


def run_case(name, source, lang_id, expected=None, cpu=5, mem_kb=262144):
    payload = {
        "source_code": b64(source),
        "language_id": lang_id,
        "cpu_time_limit": cpu,
        "memory_limit": mem_kb,
        "wall_time_limit": cpu + 2,
        "enable_network": False,
        "number_of_runs": 1,
    }
    if expected is not None:
        payload["expected_output"] = b64(expected)
    res = post_submission(payload)
    sid = res.get("status", {}).get("id")
    desc = res.get("status", {}).get("description")
    label = STATUS.get(sid, desc)
    print("  [%-11s] status.id=%s (%s)  time=%s mem=%sKB"
          % (name, sid, label, res.get("time"), res.get("memory")))
    return sid


def main():
    print("==> 1) 引擎可达性检查:", ENGINE_URL)
    if not ping():
        print("FAIL: 引擎未启动，请先 `docker compose up -d` 并等待就绪。")
        return 1

    print("==> 2) Python 正确解 (期望 ACCEPTED, id=3)")
    run_case("python-ac", "print(1+2)", LANG_PYTHON, expected="3")

    print("==> 3) Python 错误解 (期望 WRONG_ANSWER, id=4)")
    run_case("python-wa", "print(1+2)", LANG_PYTHON, expected="99")

    print("==> 4) C++ 正确解 (期望 ACCEPTED, id=3)")
    cpp = "#include <iostream>\nusing namespace std;\nint main(){cout<<\"hello\";return 0;}"
    run_case("cpp-ac", cpp, LANG_CPP, expected="hello")

    print("==> 5) 故意死循环 (期望 TIME_LIMIT_EXCEEDED, id=5)")
    run_case("python-tle", "while True:/n pass", LANG_PYTHON, cpu=2)

    print()
    print("==> 完成。判定标准：")
    print("    用例2/4 显示 id=3(ACCEPTED)、用例3 显示 id=4(WRONG_ANSWER)、")
    print("    用例5 显示 id=5(TIME_LIMIT_EXCEEDED)  =>  评测链路与判分语义正常。")
    print("    后端 AbstractJudge.mapStatus() 已将以上 status.id 映射为统一状态。")
    return 0


if __name__ == "__main__":
    sys.exit(main())
