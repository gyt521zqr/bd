# Magao Platform - 性能调优文档

> 本文档详细说明 Magao Platform 各组件的性能调优方案，包括压测方案、MySQL/Redis/RabbitMQ/JVM/Nginx 参数调优以及弹性扩容策略。

---

## 目录

1. [1000 并发压测方案](#1-1000-并发压测方案)
2. [MySQL 参数调优](#2-mysql-参数调优)
3. [Redis 参数调优](#3-redis-参数调优)
4. [RabbitMQ 参数调优](#4-rabbitmq-参数调优)
5. [JVM 参数调优](#5-jvm-参数调优)
6. [Nginx 参数调优](#6-nginx-参数调优)
7. [Judge0 扩容策略](#7-judge0-扩容策略)
8. [弹性扩容方案](#8-弹性扩容方案)
9. [监控告警配置](#9-监控告警配置)

---

## 1. 1000 并发压测方案

### 1.1 压测环境要求

| 项目 | 要求 |
|------|------|
| 压测机 | 4 核 8GB+，与目标服务器网络延迟 < 10ms |
| 目标服务器 | 8 核 32GB，与生产环境配置一致 |
| 压测工具 | JMeter 5.5+ |
| 监控工具 | Prometheus + Grafana（推荐） |

### 1.2 JMeter 测试脚本说明

#### 测试场景设计

| 场景 | 说明 | 并发数 | 持续时间 | Ramp-Up |
|------|------|--------|---------|---------|
| 首页浏览 | 模拟用户访问首页 | 200 | 5min | 30s |
| 课程列表 | 浏览课程列表页 | 300 | 5min | 30s |
| 课程详情 | 查看课程详情和视频 | 200 | 5min | 30s |
| API 接口 | 混合 API 请求 | 200 | 5min | 30s |
| 编程提交 | 代码评测提交 | 50 | 5min | 30s |
| 在线考试 | 模拟考试答题 | 50 | 5min | 30s |

#### JMeter 脚本结构

```
magao-stress-test.jmx
├── Thread Group 1: 首页浏览 (200 threads)
│   ├── HTTP Request: GET / (首页)
│   ├── HTTP Request: GET /api/courses (课程列表)
│   └── HTTP Request: GET /api/notices (公告)
├── Thread Group 2: 课程学习 (200 threads)
│   ├── HTTP Request: GET /api/courses/{id} (课程详情)
│   ├── HTTP Request: GET /api/courses/{id}/chapters (章节)
│   └── HTTP Request: POST /api/learn/progress (学习进度)
├── Thread Group 3: 用户操作 (200 threads)
│   ├── HTTP Request: POST /api/auth/login (登录)
│   ├── HTTP Request: GET /api/user/profile (个人信息)
│   └── HTTP Request: GET /api/messages (消息列表)
├── Thread Group 4: 编程评测 (50 threads)
│   ├── HTTP Request: GET /api/practice/questions (题目列表)
│   ├── HTTP Request: POST /api/practice/submit (提交代码)
│   └── HTTP Request: GET /api/practice/result/{id} (评测结果)
└── Thread Group 5: 在线考试 (50 threads)
    ├── HTTP Request: POST /api/exam/start (开始考试)
    ├── HTTP Request: GET /api/exam/{id}/paper (获取试卷)
    └── HTTP Request: POST /api/exam/submit (提交答案)
```

#### JMeter 配置要点

```xml
<!-- HTTP 请求默认配置 -->
<HTTPSampler>
    <protocol>https</protocol>
    <domain>your-domain.com</domain>
    <port>443</port>
    <timeout>5000</timeout>
    <follow_redirects>true</follow_redirects>
    <use_keepalive>true</use_keepalive>
</HTTPSampler>

<!-- 线程组配置 -->
<ThreadGroup>
    <num_threads>200</num_threads>
    <ramp_time>30</ramp_time>
    <duration>300</duration>
    <scheduler>true</scheduler>
</ThreadGroup>
```

### 1.3 压测指标与目标

| 指标 | 目标值 | 说明 |
|------|--------|------|
| 响应时间 (P50) | < 200ms | 50% 请求响应时间 |
| 响应时间 (P95) | < 500ms | 95% 请求响应时间 |
| 响应时间 (P99) | < 1000ms | 99% 请求响应时间 |
| 吞吐量 (TPS) | > 500 | 每秒处理请求数 |
| 错误率 | < 0.1% | 请求失败比例 |
| CPU 使用率 | < 70% | 服务器 CPU |
| 内存使用率 | < 80% | 服务器内存 |

### 1.4 压测执行步骤

```bash
# 1. 确保 JMeter 已安装
jmeter --version

# 2. 非GUI模式执行压测
jmeter -n -t magao-stress-test.jmx -l results.jtl -e -o report/

# 3. 查看压测报告
# 打开 report/index.html 查看详细报告

# 4. 同时监控服务器资源
docker stats --format "table {{.Name}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.NetIO}}"
```

---

## 2. MySQL 参数调优

### 2.1 生产环境推荐配置

```ini
# 连接数配置
max_connections = 2000
max_connect_errors = 1000
wait_timeout = 600
interactive_timeout = 600

# InnoDB 缓冲池（关键参数，建议为物理内存的 50%-70%）
innodb_buffer_pool_size = 4G
innodb_buffer_pool_instances = 4

# 日志配置
innodb_log_file_size = 1G
innodb_log_buffer_size = 64M
innodb_flush_log_at_trx_commit = 2    # 性能与安全的平衡
innodb_flush_method = O_DIRECT

# 线程配置
innodb_thread_concurrency = 0         # 无限制（自动管理）
innodb_read_io_threads = 8
innodb_write_io_threads = 8
innodb_purge_threads = 4

# 查询缓存（MySQL 8.0 已移除，使用应用层缓存替代）

# 慢查询日志
slow_query_log = 1
long_query_time = 1                   # 超过 1 秒记录
slow_query_log_file = /var/lib/mysql/mysql-slow.log

# 临时表
tmp_table_size = 256M
max_heap_table_size = 256M

# 排序缓冲
sort_buffer_size = 4M
join_buffer_size = 4M
read_rnd_buffer_size = 4M

# 表缓存
table_open_cache = 4000
table_definition_cache = 2000
open_files_limit = 65535
```

### 2.2 索引优化建议

```sql
-- 查看慢查询
SELECT * FROM mysql.slow_log ORDER BY query_time DESC LIMIT 20;

-- 查看索引使用情况
SELECT * FROM sys.schema_unused_indexes;
SELECT * FROM sys.schema_redundant_indexes;

-- 分析表
ANALYZE TABLE course, exam, practice_question, pay_order, user;

-- 优化表
OPTIMIZE TABLE course, exam, practice_question;
```

### 2.3 连接池配置（HikariCP）

```yaml
# application-prod.yml
spring:
  datasource:
    hikari:
      maximum-pool-size: 50
      minimum-idle: 10
      idle-timeout: 300000
      max-lifetime: 1800000
      connection-timeout: 30000
      leak-detection-threshold: 60000
```

---

## 3. Redis 参数调优

### 3.1 内存优化

```bash
# 内存配置
maxmemory 4gb                     # 最大内存限制
maxmemory-policy allkeys-lru      # LRU 淘汰策略

# 持久化配置
appendonly yes                    # 开启 AOF 持久化
appendfsync everysec              # 每秒同步（性能与安全的平衡）
save 900 1                        # 900秒内至少1个key变更则保存
save 300 10                       # 300秒内至少10个key变更
save 60 10000                     # 60秒内至少10000个key变更
```

### 3.2 性能优化

```bash
# 网络优化
tcp-keepalive 60                  # TCP keepalive 间隔
tcp-backlog 511                   # TCP 连接队列

# 慢日志
slowlog-log-slower-than 10000     # 超过 10ms 记录慢日志
slowlog-max-len 128               # 保留最近 128 条慢日志

# 客户端输出缓冲
client-output-buffer-limit normal 0 0 0
client-output-buffer-limit replica 256mb 64mb 60
client-output-buffer-limit pubsub 32mb 8mb 60
```

### 3.3 数据结构优化建议

| 用途 | Redis 数据类型 | 过期时间 | 说明 |
|------|---------------|---------|------|
| 用户 Session | String | 30min | 登录会话 |
| 验证码 | String | 5min | 短信/邮箱验证码 |
| 接口限流 | String | 1min | 滑动窗口计数 |
| 热门课程 | Sorted Set | 1h | 排行榜数据 |
| 用户积分缓存 | Hash | 10min | 积分信息 |
| 课程详情缓存 | Hash | 30min | 课程信息 |
| 分布式锁 | String | 30s | Redisson 锁 |

---

## 4. RabbitMQ 参数调优

### 4.1 生产环境配置

```bash
# 环境变量配置（docker-compose.yml）
RABBITMQ_DEFAULT_USER=admin
RABBITMQ_DEFAULT_PASS=<strong-password>
```

启用管理插件的高级配置：

```bash
# 进入 RabbitMQ 容器
docker exec -it magao-rabbitmq bash

# 启用管理插件
rabbitmq-plugins enable rabbitmq_management
rabbitmq-plugins enable rabbitmq_tracing

# 设置虚拟主机限制
rabbitmqctl set_vm_memory_high_watermark 0.6    # 内存水位线 60%
rabbitmqctl set_disk_free_limit absolute 2GB     # 磁盘剩余限制 2GB
```

### 4.2 队列优化

```java
// 队列声明参数优化
Map<String, Object> args = new HashMap<>();
args.put("x-message-ttl", 3600000);           // 消息 TTL: 1小时
args.put("x-max-length", 10000);              // 队列最大长度
args.put("x-overflow", "reject-publish");     // 溢出策略：拒绝新消息
args.put("x-queue-mode", "lazy");             // 惰性队列（减少内存使用）
args.put("x-dead-letter-exchange", "dlx");    // 死信交换机
args.put("x-dead-letter-routing-key", "dlx-key");

channel.queueDeclare("magao.order", true, false, false, args);
```

### 4.3 消费者优化

```java
// 消费者配置
@RabbitListener(queues = "magao.order", concurrency = "5-20")
// concurrency: 最小5个消费者，最大20个消费者（自动扩缩）

// 预取数量
simpleRabbitListenerContainerFactory.setPrefetchCount(50);
// prefetchCount 不宜过大，避免消费者负载不均
```

### 4.4 消息持久化策略

| 队列类型 | 持久化 | 说明 |
|---------|--------|------|
| 订单消息 | 是 | 重要业务数据必须持久化 |
| 通知消息 | 否 | 可丢失的实时通知 |
| 评测消息 | 是 | 代码评测结果不可丢失 |
| 日志消息 | 否 | 日志可通过其他方式恢复 |

---

## 5. JVM 参数调优

### 5.1 生产环境推荐参数

```bash
# 基础 JVM 参数
JAVA_OPTS="-Xms2g -Xmx4g \
  -XX:+UseG1GC \
  -XX:MaxGCPauseMillis=200 \
  -XX:G1HeapRegionSize=8m \
  -XX:InitiatingHeapOccupancyPercent=45 \
  -XX:+HeapDumpOnOutOfMemoryError \
  -XX:HeapDumpPath=/app/logs/heapdump.hprof \
  -XX:+UseCompressedOops \
  -XX:MaxMetaspaceSize=512m \
  -XX:MetaspaceSize=256m \
  -XX:MaxDirectMemorySize=512m \
  -Djava.security.egd=file:/dev/./urandom \
  -Dfile.encoding=UTF-8 \
  -Duser.timezone=Asia/Shanghai"
```

### 5.2 参数说明

| 参数 | 值 | 说明 |
|------|-----|------|
| -Xms | 2g | 初始堆内存 |
| -Xmx | 4g | 最大堆内存 |
| UseG1GC | - | 使用 G1 垃圾收集器 |
| MaxGCPauseMillis | 200 | 目标最大 GC 停顿时间 |
| G1HeapRegionSize | 8m | G1 区域大小 |
| InitiatingHeapOccupancyPercent | 45 | 触发并发标记的堆占用率 |
| MaxMetaspaceSize | 512m | 元空间最大值 |
| MaxDirectMemorySize | 512m | 直接内存最大值 |

### 5.3 GC 日志配置

```bash
JAVA_OPTS="$JAVA_OPTS \
  -Xlog:gc*:file=/app/logs/gc.log:time,uptime,level,tags:filecount=5,filesize=50m"
```

### 5.4 线程池调优

```yaml
# application-prod.yml
server:
  tomcat:
    threads:
      max: 500                    # 最大工作线程
      min-spare: 50                # 最小空闲线程
    max-connections: 2000         # 最大连接数
    accept-count: 200             # 等待队列长度
    connection-timeout: 30000      # 连接超时 30秒
```

### 5.5 不同规模的 JVM 配置

| 并发规模 | Xms | Xmx | MaxGCPauseMillis | Tomcat Threads |
|---------|-----|-----|-------------------|----------------|
| 100 并发 | 512m | 1g | 200 | 100 |
| 500 并发 | 1g | 2g | 200 | 300 |
| 1000 并发 | 2g | 4g | 200 | 500 |
| 3000 并发 | 4g | 8g | 100 | 800 |

---

## 6. Nginx 参数调优

### 6.1 全局优化

```nginx
# worker_processes
worker_processes auto;            # 自动匹配 CPU 核心数

# worker_connections
events {
    worker_connections 2048;       # 每个 worker 最大连接数
    multi_accept on;               # 一次接受多个连接
    use epoll;                     # 高性能事件模型
}

# 内核参数调优（/etc/sysctl.conf）
# net.core.somaxconn = 65535
# net.ipv4.tcp_max_syn_backlog = 65535
# net.ipv4.ip_local_port_range = 1024 65535
# net.ipv4.tcp_tw_reuse = 1
# net.ipv4.tcp_fin_timeout = 15
```

### 6.2 Gzip 压缩优化

```nginx
gzip on;
gzip_min_length 1k;               # 最小压缩文件大小
gzip_buffers 16 64k;               # 压缩缓冲区
gzip_comp_level 6;                 # 压缩级别（1-9，6为平衡点）
gzip_types
    text/plain
    text/css
    text/javascript
    application/json
    application/javascript
    application/xml
    image/svg+xml;
gzip_vary on;                      # Vary 头
gzip_proxied any;                  # 代理请求也压缩
```

### 6.3 缓存优化

```nginx
# 开启 open_file_cache
open_file_cache max=10000 inactive=20s;
open_file_cache_valid 30s;
open_file_cache_min_uses 2;
open_file_cache_errors on;

# 静态资源缓存
location ~* \.(js|css)$ {
    expires 1y;
    add_header Cache-Control "public, immutable";
}

location ~* \.(png|jpg|jpeg|gif|ico|svg|webp)$ {
    expires 30d;
    add_header Cache-Control "public, max-age=2592000";
}
```

### 6.4 代理优化

```nginx
# 上游 keepalive
upstream backend {
    server backend:8080;
    keepalive 32;                  # 保持 32 个长连接
}

# 代理缓冲
proxy_buffer_size 64k;
proxy_buffers 4 64k;
proxy_busy_buffers_size 128k;

# 大文件上传
client_max_body_size 100m;
proxy_request_buffering off;       # 关闭请求缓冲（大文件上传）
```

### 6.5 限流配置

```nginx
# IP 限流
limit_req_zone $binary_remote_addr zone=api_limit:10m rate=100r/s;
limit_conn_zone $binary_remote_addr zone=conn_limit:10m;

server {
    # API 限流：每秒 100 个请求，突发 200
    location /api/ {
        limit_req zone=api_limit burst=200 nodelay;
        limit_conn conn_limit 50;
        proxy_pass http://backend:8080/;
    }
}
```

---

## 7. Judge0 扩容策略

### 7.1 单实例性能参数

```yaml
environment:
  ENABLE_NETWORK: "false"          # 禁用网络（安全）
  CPU_TIME_LIMIT: 5               # CPU 时间限制 5 秒
  MEMORY_LIMIT: 262144             # 内存限制 256MB
  EXTRA_POOL_SIZE: 30              # 额外进程池大小
  RUNNER_MAX_PROCESSES: 100        # 最大评测进程数
  RUNNER_MAX_QUEUE_SIZE: 200       # 最大队列长度
```

### 7.2 多实例负载均衡

```nginx
# Nginx 负载均衡配置
upstream judge0_cluster {
    least_conn;                    # 最少连接优先
    server judge0-1:2358 weight=1;
    server judge0-2:2358 weight=1;
    server judge0-3:2358 weight=1;
}

server {
    location /judge0/ {
        rewrite ^/judge0/(.*)$ /$1 break;
        proxy_pass http://judge0_cluster;
    }
}
```

### 7.3 评测队列优化

```java
// 异步评测配置
@Async("judge0Executor")
public CompletableFuture<JudgeResult> submitCode(JudgeRequest request) {
    // 异步提交评测
}

// 线程池配置
@Bean("judge0Executor")
public Executor judge0Executor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10);
    executor.setMaxPoolSize(50);
    executor.setQueueCapacity(200);
    executor.setThreadNamePrefix("judge0-");
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.initialize();
    return executor;
}
```

---

## 8. 弹性扩容方案

### 8.1 水平扩容架构

```
                    ┌──────────────┐
                    │   Nginx LB   │
                    └──────┬───────┘
                           │
              ┌────────────┼────────────┐
              │            │            │
        ┌─────┴─────┐ ┌───┴────┐ ┌────┴─────┐
        │ Backend-1 │ │Backend-2│ │Backend-3│
        └───────────┘ └────────┘ └──────────┘
              │            │            │
        ┌─────┴────────────┴────────────┴─────┐
        │          MySQL (主从复制)             │
        │    Master ──── Slave1 ──── Slave2    │
        └──────────────────────────────────────┘
```

### 8.2 Backend 水平扩容

```yaml
# docker-compose.scale.yml
version: "3.8"

services:
  backend:
    deploy:
      replicas: 3                  # 运行 3 个后端实例
      resources:
        limits:
          memory: 4G
          cpus: "2.0"
```

```nginx
# Nginx 负载均衡
upstream backend {
    least_conn;
    server backend-1:8080;
    server backend-2:8080;
    server backend-3:8080;
    keepalive 32;
}
```

### 8.3 MySQL 主从复制

```bash
# Master 配置
[mysqld]
server-id = 1
log-bin = mysql-bin
binlog-format = ROW
binlog-do-db = magao

# Slave 配置
[mysqld]
server-id = 2
relay-log = relay-bin
read-only = 1
log-bin = mysql-bin
binlog-format = ROW
```

### 8.4 Redis 哨兵模式

```bash
# Redis Sentinel 配置
sentinel monitor magao-redis 172.28.0.3 6379 2
sentinel down-after-milliseconds magao-redis 30000
sentinel failover-timeout magao-redis 180000
sentinel parallel-syncs magao-redis 1
```

### 8.5 自动扩容方案（Docker Swarm）

```bash
# 初始化 Swarm 集群
docker swarm init

# 部署服务栈
docker stack deploy -c docker/docker-compose.yml magao

# 扩容后端服务
docker service scale magao_backend=5

# 查看服务状态
docker service ls
docker service ps magao_backend
```

---

## 9. 监控告警配置

### 9.1 Prometheus 监控

```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'backend'
    metrics_path: '/actuator/prometheus'
    static_configs:
      - targets: ['backend:8080']

  - job_name: 'mysql'
    static_configs:
      - targets: ['mysql-exporter:9104']

  - job_name: 'redis'
    static_configs:
      - targets: ['redis-exporter:9121']

  - job_name: 'rabbitmq'
    static_configs:
      - targets: ['rabbitmq:15672']
```

### 9.2 关键监控指标

| 服务 | 监控指标 | 告警阈值 |
|------|---------|---------|
| Backend | JVM Heap Usage | > 85% |
| Backend | Thread Pool Active | > 80% |
| Backend | HTTP Response Time P99 | > 2000ms |
| MySQL | Connections | > 80% max_connections |
| MySQL | Slow Queries/min | > 10 |
| MySQL | InnoDB Buffer Pool Hit | < 95% |
| Redis | Memory Usage | > 80% maxmemory |
| Redis | Blocked Clients | > 100 |
| RabbitMQ | Queue Depth | > 5000 |
| RabbitMQ | Consumer Lag | > 1000 |
| Nginx | Active Connections | > 1500 |
| Judge0 | Pending Submissions | > 100 |

### 9.3 Grafana 仪表盘建议

| 仪表盘 | 内容 |
|--------|------|
| JVM 监控 | 堆内存、GC 频率、线程数、类加载 |
| API 监控 | QPS、响应时间分布、错误率、状态码分布 |
| MySQL 监控 | 连接数、查询吞吐、慢查询、缓冲池命中率 |
| Redis 监控 | 内存使用、命中率、连接数、命令统计 |
| RabbitMQ 监控 | 队列深度、消费速率、消息积压 |
| 系统监控 | CPU、内存、磁盘 IO、网络流量 |
