# 码高平台 部署验收清单（收费上线）

> 适用：`magao-platform`（bd 仓库）正式收费上线前的部署与验收。
> 配套：`GO-LIVE-FIX-LIST.md`（修复清单与源码证据）、`scripts/migration_v2_payment.sql`（数据库迁移）。
> 状态约定：☐ 未完成　✅ 已完成　⚠️ 需人工确认

---

## 一、部署前置（外部依赖，代码无法完成）

### 1.1 域名与备案
| 项 | 状态 | 说明 |
|---|---|---|
| ☐ | | `wukonghuwei.cn` 完成 ICP 备案（企业主体）。**未备案会被阿里云 RST 拦截（ERR_CONNECTION_RESET）**，这是对外服务的硬门槛 |
| ☐ | | SSL 证书签发（acme.sh + Aliyun DNS API，DNS-01 校验），并配置自动续期 |
| ☐ | | `bmdjs.cn` 备案状态同步确认（历史域名） |

### 1.2 云服务凭证（火山引擎）
| 项 | 状态 | 环境变量 | 说明 |
|---|---|---|---|
| ☐ | | `TOS_ACCESS_KEY` / `TOS_SECRET_KEY` | 火山对象存储（生产图片/作品上传，当前为空会导致上传不可用） |
| ☐ | | `TOS_ENDPOINT` / `TOS_REGION` / `TOS_BUCKET` | TOS 端点/区域/桶名 |
| ☐ | | `TOS_PUBLIC_DOMAIN` | 文件访问域名（可 CDN） |
| ☐ | | `VOD_ACCESS_KEY` / `VOD_SECRET_KEY` | 火山视频点播（付费视频，当前为空返回占位） |
| ☐ | | `VOD_PLAY_DOMAIN` / `VOD_SPACE_NAME` | VOD 播放域名/空间名 |

### 1.3 支付商户凭证
| 项 | 状态 | 环境变量 | 说明 |
|---|---|---|---|
| ☐ | | `PAYMENT_MODE` | `demo` → `wechat` / `alipay`。**生产必须切真实网关，否则走演示支付** |
| ☐ | | `WECHAT_APP_ID` / `WECHAT_MCH_ID` | 微信商户号/AppID |
| ☐ | | `WECHAT_API_V3_KEY` / `WECHAT_PRIVATE_KEY` / `WECHAT_CERT_SERIAL_NO` | 微信 APIv3 密钥/商户私钥/证书序列号 |
| ☐ | | `WECHAT_PLATFORM_CERT` | 微信平台证书（验签用） |
| ☐ | | `ALIPAY_APP_ID` / `ALIPAY_PRIVATE_KEY` / `ALIPAY_PUBLIC_KEY` | 支付宝应用/私钥/公钥 |
| ☐ | | `PAYMENT_NOTIFY_BASE_URL` | 回调公网地址（如 `https://wukonghuwei.cn/api/payment/notify`），**必须公网可达** |

> 支付 SDK 已接入代码（微信 V3 `wechatpay-java:0.2.12` + 支付宝 `alipay-easysdk:2.2.2`），缺凭证时下单会**快速失败而非静默成功**。

### 1.4 安全密钥
| 项 | 状态 | 环境变量 | 说明 |
|---|---|---|---|
| ☐ | | `JWT_SECRET` | **生产无默认值，缺失直接启动失败**。生成 64 位以上随机串 |
| ☐ | | `MYSQL_PASSWORD` / `REDIS_PASSWORD` / `RABBITMQ_PASSWORD` | 中间件强口令（**不要用默认的 `magao_*_2024`**） |
| ☐ | | `JUDGE0_API_KEY` | Judge0 判题密钥 |

### 1.5 中间件
| 项 | 状态 | 说明 |
|---|---|---|
| ☐ | | Judge0 三实例（`magao-judge0-1/2/3`）已启动，判题可用 |
| ☐ | | RabbitMQ 已启用（`mq.enabled=true`）并配置延迟消息插件（订单关单才有生产者） |

---

## 二、CI/CD Secrets 配置

在 GitHub → `gyt521zqr/bd` → Settings → Secrets and variables → Actions：

| Secret 名 | 值 | 说明 |
|---|---|---|
| ☐ `DEPLOY_HOST` | `47.108.207.180` | ECS 公网 IP |
| ☐ `DEPLOY_USER` | `root` | SSH 用户（按实际） |
| ☐ `DEPLOY_SSH_KEY` | （私钥全文） | 专用 deploy key |
| ☐ `DEPLOY_PATH` | `/opt/magao-platform` | ECS 仓库根目录（按实际） |
| ☐ `DEPLOY_DOMAIN` | `wukonghuwei.cn` | 站点域名 |
| ☐ `DEPLOY_PORT` | `22` | SSH 端口（可选） |

**ECS 主机前置**：已 `git clone` 仓库并配好可拉取的 deploy key、装好 `docker`/`docker compose v2`/`node 18+`、`/data/frontend/dist` 目录存在。

---

## 三、数据库迁移

```bash
# 1. 备份（必须）
mysqldump -uroot -p magao_platform > backup_before_v2_$(date +%F).sql

# 2. 执行迁移
mysql -uroot -p magao_platform < scripts/migration_v2_payment.sql

# 3. 验证
mysql -uroot -p magao_platform -e "DESC pay_order;"        # 应有 coupon_id 列
mysql -uroot -p magao_platform -e "SHOW TABLES LIKE 'user_membership';"  # 应存在
```

> 迁移内容：`pay_order` 加 `coupon_id` 列、新增 `user_membership` 会员权益表。
> 可选（幂等加固）：`pay_order.order_no`、`pay_callback_log(order_no,trade_no)` 唯一索引（历史有重复数据需先清理）。

---

## 四、环境变量完整清单（部署时注入）

| 变量 | 必填 | 说明 |
|---|---|---|
| `JWT_SECRET` | ✅ | JWT 签名密钥 |
| `MYSQL_PASSWORD` / `REDIS_PASSWORD` / `RABBITMQ_PASSWORD` | ✅ | 中间件口令 |
| `TOS_ACCESS_KEY` / `TOS_SECRET_KEY` | ✅ | 对象存储 |
| `VOD_ACCESS_KEY` / `VOD_SECRET_KEY` / `VOD_PLAY_DOMAIN` | ✅ | 视频点播 |
| `PAYMENT_MODE` + 微信/支付宝凭证 | ✅ | 支付网关 |
| `PAYMENT_NOTIFY_BASE_URL` | ✅ | 回调公网地址 |
| `CORS_ALLOWED_ORIGINS` | 可选 | 默认 `https://wukonghuwei.cn` |
| `JUDGE0_API_KEY` | 可选 | 判题密钥 |

---

## 五、部署步骤

1. ☐ 配置 6 个 CI/CD Secrets + ECS deploy key
2. ☐ 注入上述环境变量到 ECS 的 `.env` 或 docker-compose
3. ☐ 执行数据库迁移（第三节）
4. ☐ push `main` 触发 CI/CD，或 ECS 上手动 `git pull && docker compose up -d --build`
5. ☐ 观察 GitHub Actions：`ci`（`mvn package` + `npm run build`）必须绿
6. ☐ `deploy` 完成后，检查 `curl https://wukonghuwei.cn/actuator/health` 返回 `UP`

---

## 六、功能验收清单

### 6.1 认证与账号（P1-18 重点回归）
| 项 | 状态 | 验证点 |
|---|---|---|
| ☐ | | 密码登录成功，token 在 httpOnly Cookie（DevTools 看不到 `document.cookie` 里的 token） |
| ☐ | | **刷新页面后仍保持登录**（验证 `ensureAuth` 静默判断 httpOnly Cookie） |
| ☐ | | 登出后重新登录正常；登出后旧 refreshToken 调 `/auth/refresh` 被拒 |
| ☐ | | 连续 5 次密码错误 → 账号锁定 15 分钟（提示"登录失败次数过多"） |
| ☐ | | 修改手机号需短信验证码；无验证码被拒 |
| ☐ | | 注册需勾选协议（含监护人同意），后端留痕 |

### 6.2 资金链路（核心，必须真机验收）
| 项 | 状态 | 验证点 |
|---|---|---|
| ☐ | | 下单金额与后台商品价格一致（查库定价，非写死 199/299） |
| ☐ | | 下单 → 扫码支付 → 回调 → 订单变已支付 → 课程/会员自动开通 |
| ☐ | | 退款 → 网关真实退款 → 钱退回 → 订单变已退款 |
| ☐ | | 优惠券：下单锁定 → 支付核销 → 取消/超时回滚 |
| ☐ | | 订单超时（30 分钟）自动关单并回滚优惠券（定时任务） |
| ☐ | | 支付回调落 `pay_callback_log`，操作落 `sys_log` |

### 6.3 考试业务
| 项 | 状态 | 验证点 |
|---|---|---|
| ☐ | | 进入考试 → 答题 → 交卷 → 客观题自动判分 → 成绩页显示逐题解析 |
| ☐ | | 答题页**不泄露标准答案**（抓包确认无 answer 字段） |
| ☐ | | 成绩查询需"准考证号+姓名"双因子 |
| ☐ | | 题库中心展示后台录入的真实试卷（无假题） |

### 6.4 内容与存储
| 项 | 状态 | 验证点 |
|---|---|---|
| ☐ | | 后台录入 Banner/资讯/赛事 → 前台回显 |
| ☐ | | 图片上传/回显走 TOS（生产）正常 |
| ☐ | | 付费视频：未购买被拒，购买后能播放 |
| ☐ | | 资讯内容正常渲染（DOMPurify 过滤后格式无损） |

---

## 七、安全验收清单

| 项 | 状态 | 验证点 |
|---|---|---|
| ☐ | | 普通用户访问 `/api/admin/**` 被拒（越权） |
| ☐ | | 教师无法建管理员账号、无法看他人班级/成绩 |
| ☐ | | 改 orderId/userId 参数查不到他人订单/成绩/作品 |
| ☐ | | 上传 `../` 路径、可执行文件被拒（路径穿越/类型白名单） |
| ☐ | | 资讯/题目注入 `<script>` 被 DOMPurify 过滤（XSS） |
| ☐ | | 伪造 `X-Forwarded-For` 无法绕过限流 |
| ☐ | | 生产环境 Swagger/actuator 敏感端点关闭 |

---

## 八、资金攻击测试（必做）

| 项 | 状态 | 验证点 |
|---|---|---|
| ☐ | | **伪造回调**：不经过支付网关直接调 `/api/payment/notify/**` → 验签失败，不改变订单状态 |
| ☐ | | **改价**：下单后篡改请求金额 → 后端查库定价，篡改无效 |
| ☐ | | **重放回调**：同一回调重复发送 → 幂等，只处理一次 |
| ☐ | | **越权退款**：非本人订单调退款 → 被拒 |
| ☐ | | **白嫖**：不支付直接标记已支付 → 无此路径（`payOrder` 不再置已支付） |

---

## 九、最终上线检查表

- [ ] ICP 备案完成，`wukonghuwei.cn` 可正常访问（HTTPS）
- [ ] 所有环境变量已注入，`JWT_SECRET` 为强随机值
- [ ] `payment.mode` 已切 `wechat`/`alipay`（非 demo）
- [ ] 数据库迁移已执行且已验证
- [ ] CI/CD `ci` 门禁通过，`deploy` 成功，`/actuator/health` 返回 UP
- [ ] 第六节功能验收全部通过
- [ ] 第七节安全验收全部通过
- [ ] 第八节攻击测试全部通过（伪造回调/改价/重放均无法白嫖）
- [ ] 中间件强口令已替换默认值
- [ ] 生产 admin 密码已改为强密码并妥善保管

---

> 验收通过标准：**用真实商户号跑通「下单 → 支付 → 回调 → 开通 → 退款 → 钱退回」全链路**，且伪造回调/改价/重放/越权四种攻击均无法得逞。
