# Magao Platform - 支付对接配置手册

> 本文档详细说明 Magao Platform 的支付系统配置流程，包括微信支付 V3 和支付宝当面付的接入步骤、测试流程、退款流程和订单超时自动关闭配置。

---

## 目录

1. [微信支付 V3 配置](#1-微信支付-v3-配置)
2. [支付宝当面付配置](#2-支付宝当面付配置)
3. [支付测试流程](#3-支付测试流程)
4. [退款流程](#4-退款流程)
5. [订单超时自动关闭配置](#5-订单超时自动关闭配置)
6. [常见问题](#6-常见问题)

---

## 1. 微信支付 V3 配置

### 1.1 前置准备

在开始配置前，请确保已完成以下准备工作：

1. 注册微信支付商户平台账号：https://pay.weixin.qq.com
2. 拥有已认证的微信服务号（获取 AppID）
3. 已完成商户平台与公众号的绑定
4. 已开通「Native 支付」产品（扫码支付）
5. 已开通「JSAPI 支付」产品（公众号内支付）
6. 已设置 API 密钥和 APIv3 密钥
7. 已申请并下载 API 证书

### 1.2 获取必要参数

| 参数 | 说明 | 获取位置 |
|------|------|---------|
| AppID | 微信公众号/小程序的 AppID | 公众平台 -> 开发 -> 基本配置 |
| Mch ID | 商户号 | 商户平台 -> 账户中心 -> 商户信息 |
| APIv3 Key | APIv3 密钥（32位字符串） | 商户平台 -> 账户中心 -> API安全 -> APIv3密钥 |
| Serial No | 证书序列号 | 商户平台 -> 账户中心 -> API安全 -> API证书 -> 证书序列号 |
| Private Key | 商户私钥（apiclient_key.pem） | 下载的 API 证书压缩包中 |

### 1.3 APIv3 密钥设置

1. 登录微信支付商户平台
2. 进入「账户中心」->「API安全」
3. 点击「设置APIv3密钥」
4. 输入 32 位密钥（建议使用随机生成器）
5. 验证身份后保存

```bash
# 生成 32 位随机密钥
openssl rand -hex 16
```

### 1.4 证书下载与部署

1. 登录商户平台 ->「账户中心」->「API安全」
2. 点击「申请证书」
3. 按照指引完成证书申请
4. 下载证书压缩包，解压后获取以下文件：
   - `apiclient_cert.pem` - 商户证书
   - `apiclient_key.pem` - 商户私钥
   - `apiclient_cert.p12` - 商户证书 PKCS12 格式

5. 将私钥文件部署到服务器：

```bash
# 在服务器上创建证书目录
mkdir -p /data/magao-platform/certs/wechat-pay

# 上传私钥文件（安全传输，如 scp）
scp apiclient_key.pem server:/data/magao-platform/certs/wechat-pay/

# 设置文件权限（仅应用用户可读）
chmod 600 /data/magao-platform/certs/wechat-pay/apiclient_key.pem
```

### 1.5 后端配置

在 `.env` 文件中配置微信支付参数：

```env
# 微信支付配置
WECHAT_PAY_APP_ID=wx1234567890abcdef
WECHAT_PAY_MCH_ID=1234567890
WECHAT_PAY_API_KEY_V3=your-32-char-api-v3-key-here-00000000
WECHAT_PAY_SERIAL_NO=5E2A1B3C4D5E6F7A8B9C0D1E2F3A4B5C6D7E8F9
WECHAT_PAY_PRIVATE_KEY_PATH=/app/certs/wechat-pay/apiclient_key.pem
WECHAT_PAY_NOTIFY_URL=https://your-domain.com/api/payment/wechat/notify
```

在 `application-prod.yml` 中确认配置映射：

```yaml
payment:
  wechat:
    app-id: ${WECHAT_PAY_APP_ID}
    mch-id: ${WECHAT_PAY_MCH_ID}
    api-v3-key: ${WECHAT_PAY_API_KEY_V3}
    serial-no: ${WECHAT_PAY_SERIAL_NO}
    private-key-path: ${WECHAT_PAY_PRIVATE_KEY_PATH}
    notify-url: ${WECHAT_PAY_NOTIFY_URL}
```

### 1.6 回调 URL 配置

微信支付回调通知需要满足以下条件：

1. **必须是 HTTPS**：回调 URL 必须使用 HTTPS 协议
2. **必须可公网访问**：微信支付服务器需要能够访问到回调地址
3. **不能包含端口号**：标准 HTTPS 端口 443
4. **不能包含 hash 和 query**：URL 中不能有 # 和 ? 参数

回调地址示例：
```
https://your-domain.com/api/payment/wechat/notify
```

### 1.7 验证配置

```bash
# 重启后端服务使配置生效
docker compose -f docker/docker-compose.yml restart backend

# 查看启动日志确认支付模块加载成功
docker compose -f docker/docker-compose.yml logs backend | grep -i wechat
```

---

## 2. 支付宝当面付配置

### 2.1 前置准备

1. 注册支付宝开放平台账号：https://open.alipay.com
2. 创建应用并获取 AppID
3. 签约「当面付」产品
4. 生成 RSA2 密钥对
5. 上传应用公钥并获取支付宝公钥

### 2.2 创建应用并签约

1. 登录支付宝开放平台
2. 进入「开发者中心」->「我的应用」
3. 点击「创建应用」
4. 填写应用信息：
   - 应用名称：Magao Platform
   - 应用类型：网页应用
   - 应用图标：上传平台 Logo
5. 创建完成后，记录 **AppID**
6. 在应用详情页，点击「添加功能」
7. 搜索并添加「当面付」产品
8. 提交审核（审核通常 1-2 个工作日）

### 2.3 生成密钥对

```bash
# 下载支付宝密钥生成工具
# https://opendocs.alipay.com/common/02kipk

# 或使用 OpenSSL 生成 RSA2 密钥对
openssl genrsa -out app_private_key.pem 2048
openssl rsa -in app_private_key.pem -pubout -out app_public_key.pem

# 查看私钥（去除头尾行和换行）
cat app_private_key.pem | grep -v "PRIVATE KEY" | tr -d '\n'

# 查看公钥（去除头尾行和换行）
cat app_public_key.pem | grep -v "PUBLIC KEY" | tr -d '\n'
```

### 2.4 配置应用公钥

1. 进入应用详情页 ->「开发设置」
2. 找到「接口加签方式」
3. 选择「RSA2(SHA256)密钥」
4. 粘贴上一步生成的应用公钥
5. 保存后，系统会生成「支付宝公钥」
6. **记录支付宝公钥**（后续配置需要）

### 2.5 后端配置

在 `.env` 文件中配置支付宝参数：

```env
# 支付宝配置
ALIPAY_APP_ID=2021001234567890
ALIPAY_PRIVATE_KEY=MIIEvQIBADANBgkqhkiG9w0BAQEFAASCBKcwggSjAgEAAoIBAQC...
ALIPAY_PUBLIC_KEY=MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAy8s...
ALIPAY_NOTIFY_URL=https://your-domain.com/api/payment/alipay/notify
```

在 `application-prod.yml` 中确认配置映射：

```yaml
payment:
  alipay:
    app-id: ${ALIPAY_APP_ID}
    private-key: ${ALIPAY_PRIVATE_KEY}
    public-key: ${ALIPAY_PUBLIC_KEY}
    notify-url: ${ALIPAY_NOTIFY_URL}
    gateway-url: https://openapi.alipay.com/gateway.do
    sign-type: RSA2
    charset: UTF-8
    format: json
```

### 2.6 回调 URL 配置

支付宝回调地址要求：

1. **必须是 HTTPS**：回调 URL 必须使用 HTTPS 协议
2. **必须可公网访问**：支付宝服务器需要能够访问到回调地址
3. **不能是 localhost 或内网 IP**

回调地址示例：
```
https://your-domain.com/api/payment/alipay/notify
```

### 2.7 验证配置

```bash
# 重启后端服务
docker compose -f docker/docker-compose.yml restart backend

# 查看启动日志
docker compose -f docker/docker-compose.yml logs backend | grep -i alipay
```

---

## 3. 支付测试流程

### 3.1 微信支付测试

#### 测试前准备

1. 确认已配置微信支付沙箱环境（或使用正式环境小额测试）
2. 准备一个可以扫码的微信账号
3. 确认回调 URL 可公网访问

#### 测试步骤

```
1. 登录平台，进入课程详情页
2. 点击「购买课程」按钮
3. 选择「微信支付」
4. 系统生成预支付订单，返回二维码
5. 使用微信扫码支付（测试环境使用 0.01 元）
6. 支付完成后，微信回调通知后端
7. 后端验证签名并更新订单状态
8. 前端轮询订单状态，显示支付成功
9. 课程自动开通
```

#### 验证要点

| 检查项 | 预期结果 |
|--------|---------|
| 二维码生成 | 正常显示，30分钟内有效 |
| 扫码支付 | 微信支付页面正常弹出 |
| 支付回调 | 后端日志显示收到回调并验证通过 |
| 订单状态 | 自动更新为「已支付」 |
| 课程开通 | 用户课程列表中显示已购课程 |
| 积分记录 | 支付积分正确记录 |

### 3.2 支付宝测试

#### 测试步骤

```
1. 登录平台，进入课程详情页
2. 点击「购买课程」按钮
3. 选择「支付宝支付」
4. 系统生成预支付订单，返回二维码
5. 使用支付宝扫码支付（测试环境使用 0.01 元）
6. 支付完成后，支付宝回调通知后端
7. 后端验证签名并更新订单状态
8. 前端轮询订单状态，显示支付成功
9. 课程自动开通
```

### 3.3 回调测试工具

如果本地开发环境无法接收公网回调，可使用内网穿透工具：

```bash
# 使用 ngrok 内网穿透（测试用）
ngrok http 8080

# 或使用 cpolar
cpolar http 8080

# 将生成的公网 URL 设置为回调地址
# 例如：https://abc123.ngrok.io/api/payment/wechat/notify
```

---

## 4. 退款流程

### 4.1 退款规则

| 规则 | 说明 |
|------|------|
| 退款时效 | 支付后 30 天内可申请退款 |
| 退款金额 | 不超过原支付金额 |
| 退款方式 | 原路退回（微信退回微信，支付宝退回支付宝） |
| 退款周期 | 微信 1-3 个工作日，支付宝 1-5 个工作日 |
| 部分退款 | 支持部分退款（如课程部分消费后退款） |

### 4.2 管理员操作退款

```
1. 管理员登录后台
2. 进入「订单管理」页面
3. 找到需要退款的订单
4. 点击「退款」按钮
5. 填写退款金额和退款原因
6. 确认退款
7. 系统调用支付平台退款接口
8. 订单状态更新为「退款中」
9. 支付平台处理完成后，状态更新为「已退款」
```

### 4.3 微信退款接口参数

```json
{
  "out_trade_no": "订单号",
  "out_refund_no": "退款单号",
  "reason": "用户申请退款",
  "amount": {
    "refund": 100,
    "total": 100,
    "currency": "CNY"
  }
}
```

### 4.4 支付宝退款接口参数

```json
{
  "out_trade_no": "订单号",
  "refund_amount": "1.00",
  "refund_reason": "用户申请退款",
  "out_request_no": "退款请求号"
}
```

### 4.5 退款状态查询

```bash
# 查看退款日志
docker compose -f docker/docker-compose.yml logs backend | grep -i refund

# 数据库查询退款记录
docker exec magao-mysql mysql -u root -p magao -e \
  "SELECT * FROM pay_order WHERE status IN ('REFUNDING', 'REFUNDED') ORDER BY create_time DESC LIMIT 10;"
```

---

## 5. 订单超时自动关闭配置

### 5.1 超时规则

| 场景 | 超时时间 | 说明 |
|------|---------|------|
| 未支付订单 | 30 分钟 | 创建后 30 分钟未支付自动关闭 |
| 退款审核 | 48 小时 | 退款申请后 48 小时未审核自动通过 |
| 退款处理 | 72 小时 | 退款提交后 72 小时未到账需人工处理 |

### 5.2 延迟队列配置

使用 RabbitMQ 延迟队列实现订单超时自动关闭：

```java
// 延迟队列配置
@Configuration
public class OrderDelayConfig {

    public static final String ORDER_EXCHANGE = "magao.order.exchange";
    public static final String ORDER_DELAY_QUEUE = "magao.order.delay.queue";
    public static final String ORDER_DEAD_LETTER_EXCHANGE = "magao.order.dlx.exchange";
    public static final String ORDER_DEAD_LETTER_QUEUE = "magao.order.dlx.queue";

    // 死信交换机（实际处理订单超时的队列）
    @Bean
    public DirectExchange orderDeadLetterExchange() {
        return new DirectExchange(ORDER_DEAD_LETTER_EXCHANGE);
    }

    // 死信队列（消费者监听此队列）
    @Bean
    public Queue orderDeadLetterQueue() {
        return new Queue(ORDER_DEAD_LETTER_QUEUE, true);
    }

    @Bean
    public Binding orderDeadLetterBinding() {
        return BindingBuilder.bind(orderDeadLetterQueue())
                .to(orderDeadLetterExchange())
                .with("order.timeout");
    }

    // 延迟队列（消息过期后转发到死信队列）
    @Bean
    public Queue orderDelayQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", ORDER_DEAD_LETTER_EXCHANGE);
        args.put("x-dead-letter-routing-key", "order.timeout");
        args.put("x-message-ttl", 30 * 60 * 1000);  // 30 分钟
        return new Queue(ORDER_DELAY_QUEUE, true, false, false, args);
    }

    @Bean
    public DirectExchange orderExchange() {
        return new DirectExchange(ORDER_EXCHANGE);
    }

    @Bean
    public Binding orderDelayBinding() {
        return BindingBuilder.bind(orderDelayQueue())
                .to(orderExchange())
                .with("order.create");
    }
}
```

### 5.3 超时处理逻辑

```java
// 订单超时消费者
@RabbitListener(queues = OrderDelayConfig.ORDER_DEAD_LETTER_QUEUE)
public void handleOrderTimeout(Message message) {
    String orderNo = new String(message.getBody());
    log.info("Processing timeout for order: {}", orderNo);

    // 查询订单状态
    PayOrder order = orderService.getByOrderNo(orderNo);
    if (order != null && order.getStatus() == OrderStatus.UNPAID) {
        // 关闭订单
        orderService.closeOrder(orderNo);
        log.info("Order {} closed due to timeout", orderNo);
    }
}
```

### 5.4 定时任务兜底

除了延迟队列，还需要定时任务作为兜底方案：

```java
// 每 5 分钟检查一次超时订单
@Scheduled(fixedRate = 5 * 60 * 1000)
public void checkTimeoutOrders() {
    List<PayOrder> timeoutOrders = orderService.findTimeoutOrders(
        OrderStatus.UNPAID,
        LocalDateTime.now().minusMinutes(30)
    );
    for (PayOrder order : timeoutOrders) {
        orderService.closeOrder(order.getOrderNo());
    }
}
```

---

## 6. 常见问题

### Q1: 微信支付回调签名验证失败

**原因分析**：
- APIv3 密钥配置错误
- 证书序列号不匹配
- 回调数据被篡改

**解决方案**：
```bash
# 1. 确认 APIv3 密钥
docker exec magao-backend env | grep WECHAT_PAY_API_KEY_V3

# 2. 确认证书序列号
# 在商户平台查看证书序列号，与配置比对

# 3. 查看回调日志
docker compose -f docker/docker-compose.yml logs backend | grep -i "notify\|callback\|signature"
```

### Q2: 支付宝回调收不到通知

**原因分析**：
- 回调 URL 不可公网访问
- 回调 URL 配置错误
- 防火墙拦截

**解决方案**：
```bash
# 1. 测试回调 URL 可访问性
curl -X POST https://your-domain.com/api/payment/alipay/notify

# 2. 检查 Nginx 日志
docker exec magao-nginx tail -f /var/log/nginx/access.log | grep notify

# 3. 检查防火墙规则
sudo ufw status
sudo ufw allow 443/tcp
```

### Q3: 订单状态不一致

**原因分析**：
- 回调通知丢失
- 回调处理失败
- 网络超时

**解决方案**：
```bash
# 1. 手动查询支付状态
# 微信：调用查询订单 API
# 支付宝：调用 alipay.trade.query

# 2. 重新同步订单状态
docker exec magao-backend curl -X POST http://localhost:8080/api/internal/payment/sync
```

### Q4: 退款失败

**原因分析**：
- 商户余额不足
- 退款金额超过可退金额
- 证书过期

**解决方案**：
```bash
# 1. 检查商户余额（微信支付商户平台）
# 2. 检查证书有效期
openssl x509 -in apiclient_cert.pem -noout -dates
# 3. 查看退款日志
docker compose -f docker/docker-compose.yml logs backend | grep -i refund
```

### Q5: 生产环境支付金额单位

**注意**：微信支付和支付宝的金额单位均为「分」，不是「元」。

```java
// 正确：1.00 元 = 100 分
int amountInCents = (int) (amountInYuan * 100);

// 错误：直接传递元
int amountInCents = (int) amountInYuan;  // 会导致金额错误
```
