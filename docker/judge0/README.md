# Judge0 评测引擎 —— 部署与判分联调指南

> 本平台后端判分引擎采用 Judge0（开源、基于 Docker 沙箱的代码评测系统）。
> 它原生支持超时限制、内存限制、网络隔离与系统调用拦截，能满足大型竞赛对
> 安全性（防恶意代码）、公平性（统一运行环境）与可扩展性（多实例）的要求。

---

## 1. 架构概览

- 前端编辑器（Python / C++ / GoC / Scratch / ScratchJr）负责编写与“运行预览”。
- 后端 `module/judge` 通过 `Judge0Client` 把代码提交到 Judge0 沙箱真实编译运行，
  并依据 `test_case` 表逐用例比对 `expected_output` 得出 AC / WA / TLE / MLE / RE / CE 等判定与得分。
- Judge0 与后端通过 REST API 通信，源码/输入/输出均以 base64 传输；
  后端在 `application*.yml` 的 `judge0` 配置块中维护其地址（`judge0.api-url` 等）。

语言映射（见 `LanguageEnum`）：

| 语言        | Judge0 数字 ID | 说明 |
|-------------|----------------|------|
| Python      | 71             | 直接按 Python3 判分 |
| Scratch 3.0 | 71             | 先转译为 Python 再判分 |
| Scratch Jr  | 71             | 先转译为 Python 再判分 |
| C++         | 54             | 按 C++(GCC) 判分 |
| GoC         | 54             | 语法兼容 C/C++，按 C++ 判分 |

---

## 2. 两种部署方式

### A. 本地开发（独立单机）

文件：`docker/judge0/docker-compose.yml`（使用 judge0/judge0-ce:latest 单容器镜像，内置 worker，
需挂载宿主机 Docker socket 并 privileged）。

```bash
docker compose -f docker/judge0/docker-compose.yml up -d
curl -s http://localhost:2358/        # 返回 Judge0 版本信息即成功
```

后端 `application-dev.yml` 已配置 `judge0.api-url: http://localhost:2358`，无需额外改动。

### B. 生产环境（内置 3 实例集群）

文件：`docker/docker-compose.yml` 已内置 `judge0-1 / judge0-2 / judge0-3` 三个实例
（均为 judge0/judge0-ce:latest，privileged + 挂载 `/var/run/docker.sock`，配 `EXTRA_POOL_SIZE` 预热沙箱池）。

```bash
docker compose -f docker/docker-compose.yml up -d mysql redis rabbitmq judge0-1 judge0-2 judge0-3 backend
```

`application-prod.yml` 已配置三实例轮询：

```yaml
judge0:
  api-url:   http://magao-judge0-1:2358
  api-url-2: http://magao-judge0-2:2358
  api-url-3: http://magao-judge0-3:2358
  api-key:   ${JUDGE0_API_KEY:prod-judge0-key}
  timeout:   60
```

后端 `Judge0Client` 在构造时收集这三个地址，每次判题按 round-robin 选择实例；
若某实例连接/系统错误则自动故障转移到下一个，从而横向扩展吞吐并提升可用性。

---

## 3. 后端配置说明

配置类 `Judge0Properties`（前缀 `judge0`）读取以下字段：

| 字段        | 默认                  | 说明 |
|-------------|-----------------------|------|
| api-url     | http://localhost:2358 | 实例1 地址（必填） |
| api-url-2   | （空）                | 实例2 地址（可选，多实例轮询用） |
| api-url-3   | （空）                | 实例3 地址（可选，多实例轮询用） |
| api-key     | （空）                | 开启 Judge0 鉴权时填写 |
| timeout     | 30                    | HTTP 读超时基数（秒），实际读超时 = timeout + 10 |

仅当 `api-key` 不以 `your-` 开头时才会在请求头附加 `X-Judge0-Key`。

---

## 4. 连通性验证

```bash
# 应返回 JSON：{"version": "...", "status": "ok", ...}
curl -s http://localhost:2358/

# 快速跑一个 Python 判题（expected_output 比对）
curl -s -X POST http://localhost:2358/submissions?wait=true&base64=true \
  -H "Content-Type: application/json" \
  -d '{"source_code":"cHJpbnQoMSsyKQ==","language_id":71,"expected_output":"Mw==","cpu_time_limit":5,"memory_limit":262144}'
# 返回 status.id == 3（Accepted）即表示判分链路打通
```

---

## 5. 建题与端到端判分测试

1. 在 `practice_question` 表建题，至少设置 `time_limit`（秒）与 `memory_limit_mb`。
2. 在 `test_case` 表为该题添加若干用例：`input`、`expected_output`、`score`。
3. 通过判分接口提交：请求体含 `code`、`questionId`、`language`；
   后端 `AbstractJudge.doJudge()` 会：
   - 查出该题全部 `test_case`；
   - 逐用例调用 `Judge0Client.judge(code, languageId, input, expected_output, timeLimit, memLimit)`；
   - 由 Judge0 的 `status.id` 映射为统一状态（3=ACCEPTED / 4=WRONG_ANSWER / 5=TLE /
     6=COMPILE_ERROR / 7,8=RUNTIME_ERROR / 13=MEMORY_LIMIT_EXCEEDED）；
   - 聚合 `score = passed * 100 / total` 并返回每个用例的 `caseResults`。
4. “运行”接口（`doRun`）只执行不判分，便于编辑器实时预览输出。

若题目未配置 `test_case`，判分会返回 `SYSTEM_ERROR` 并提示“题目未配置测试用例”。

---

## 6. 生产加固建议

- **鉴权**：在 `docker-compose.yml` 中将 `ENABLE_JUDGE0_AUTH` 设为 `true`，
  并配置 `JUDGE0_API_KEY` 环境变量与后端 `judge0.api-key` 保持一致，避免评测服务被未授权调用。
- **传输安全**：后端经内网访问 Judge0 即可；若跨网络，建议加一层 Nginx/mTLS。
- **资源限制**：`cpu_time_limit`、`memory_limit` 已在每次提交中按题目设置；
  实例级 `MEMORY_LIMIT: 262144`（256MB）、`EXTRA_POOL_SIZE: 30` 已在 compose 中预热。
- **高可用**：已支持 3 实例轮询 + 故障转移；如需更大并发，可再横向扩容实例并在
  `judge0.api-url-2/3` 之后继续追加 `api-url-4...` 并同步修改 `Judge0Properties` 与 `Judge0Client`。
- **健康检查**：Judge0 实例已配置 `curl -f http://localhost:2358/` 健康检查，
  backend 依赖其 `service_healthy` 后再启动。

---

## 7. 常见问题排查

| 现象 | 可能原因 | 处理 |
|------|----------|------|
| 拉取镜像失败 / `manifest unknown` | 镜像名写错 | 确认使用 judge0/judge0-ce:latest（或 judge0/judge0:latest） |
| 连接被拒绝 `Connection refused` | Judge0 未启动或端口不对 | `docker ps` 确认实例已 `Up`，端口 2358 已暴露 |
| 全部用例返回 SYSTEM_ERROR | 后端连不上 Judge0 | 检查 `judge0.api-url` 与容器网络是否互通（同 `magao-network`） |
| 判分一直 WA 但本地正确 | 行尾/空格/编码差异 | 统一 `expected_output` 换行符，注意 base64 编解码 |
| 偶发超时 | 并发过高 / 沙箱预热不足 | 调大 `EXTRA_POOL_SIZE`、增加实例、放宽 `cpu_time_limit` |


---

## 8. 一键端到端验证脚本

文件：`docker/judge0/verify.py`（Python 标准库实现，无第三方依赖）。

在 **Docker 已启动且评测引擎已 `up -d`** 的机器上运行：

```bash
# 先确认引擎就绪
curl -s http://localhost:2358/          # 返回版本 JSON 即成功

# 运行验证（默认连 http://localhost:2358）
python3 docker/judge0/verify.py

# 若连生产三实例中的某一个，可指定地址：
ENGINE_URL=http://localhost:2358 python3 docker/judge0/verify.py
```

脚本会依次验证：

| 用例 | 语言 | 期望 status.id | 期望判定 |
|------|------|----------------|----------|
| python-ac  | Python(71) | 3  | ACCEPTED（正确解） |
| python-wa  | Python(71) | 4  | WRONG_ANSWER（错误解） |
| cpp-ac     | C++(54)    | 3  | ACCEPTED（正确解） |
| python-tle | Python(71) | 5  | TIME_LIMIT_EXCEEDED（死循环） |

全部符合即说明“评测引擎可用 + 判分语义正确”。后端 `AbstractJudge.mapStatus()`
会把上述 `status.id` 映射为统一的 `ACCEPTED / WRONG_ANSWER / TIME_LIMIT_EXCEEDED`
等状态并据此计算得分，完整串联（建题 + 测试用例 + 提交）见第 5 节。

> 注意：本脚本不依赖业务数据库，仅验证评测引擎本身。若要验证“题目→后端→引擎→落库”
> 的完整链路，请在第 5 节建一道带 `test_case` 的题，再通过判分接口提交。
