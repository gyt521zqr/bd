package com.magao.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 带鉴权的「全链路畅通」冒烟测试。
 *
 * 思路：用真实短信验证码流程注册一个唯一手机号用户 -> 从 Redis 读取验证码 ->
 * 密码登录拿到 accessToken -> 携带 Bearer 令牌逐个调用各业务模块的受保护接口，
 * 断言：不返回 404（路由漂移）、不返回 5xx（服务端异常），从而验证"鉴权链路 + 业务链路"全通。
 *
 * 与 contract-check.sh 的区别：contract-check 以 401 视为「路由存在」，本测试用真实 token
 * 进入受保护分支，能暴露仅在鉴权后才触发的服务端异常（如缺列、NPE、事务问题）。
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev")
class FullChainSmokeTest {

    @Autowired
    private TestRestTemplate rest;

    // 使用应用同款 RedisTemplate（Value 为 Jackson JSON 序列化），否则读出的验证码会带 JSON 引号
    @Autowired
    private RedisTemplate<String, Object> redis;

    private final ObjectMapper om = new ObjectMapper();

    private String token;

    @BeforeEach
    void authenticate() {
        // 唯一手机号 + 已知验证码，规避短信网关/60s 发送限流带来的时序脆弱性。
        // （sms-code 发送接口本身由 contract-check.sh 覆盖，本测试聚焦「注册写入 -> 登录 -> 带 token 调受保护接口」核心链）
        // 必须补足 8 位，否则余数较小时手机号不足 11 位，注册会被「手机号格式不正确」拦截
        String phone = String.format("139%08d", System.currentTimeMillis() % 100_000_000L);
        String password = "smoketest";
        String code = String.format("%06d", (int) (System.nanoTime() % 1_000_000));

        // 用应用同款 RedisTemplate 写入验证码（Jackson JSON 序列化，与 register 读取方式一致）
        redis.opsForValue().set("user:sms:" + phone, code, 5, java.util.concurrent.TimeUnit.MINUTES);

        // 1) 注册（register 会从 Redis 取出验证码比对，再写入用户，含 parent_phone 列）
        Map<String, String> reg = new LinkedHashMap<>();
        reg.put("phone", phone);
        reg.put("smsCode", code);
        reg.put("nickname", "smoke");
        reg.put("password", password);
        ResponseEntity<String> regR = rest.postForEntity(
                "/api/auth/register", new HttpEntity<>(reg, jsonHeaders()), String.class);
        assertEquals(200, regR.getStatusCode().value(), "注册应成功: " + regR.getBody());

        // 2) 密码登录
        Map<String, String> login = new LinkedHashMap<>();
        login.put("username", phone);
        login.put("password", password);
        login.put("loginType", "password");
        ResponseEntity<String> loginR = rest.postForEntity(
                "/api/auth/login", new HttpEntity<>(login, jsonHeaders()), String.class);
        assertEquals(200, loginR.getStatusCode().value(), "登录应成功: " + loginR.getBody());

        token = extractToken(loginR.getBody());
        assertNotNull(token, "accessToken 不应为空");
    }

    @Test
    void fullChainProtectedEndpointsShouldNotDriftOrError() throws Exception {
        // 受保护接口清单（method, path, 是否需要 body）。覆盖 user/course/message/class/
        // system/order/payment/vod/admin/marketing 等核心模块。
        List<Endpoint> endpoints = Arrays.asList(
                new Endpoint(HttpMethod.GET, "/api/user/profile"),
                new Endpoint(HttpMethod.GET, "/api/user/list"),
                new Endpoint(HttpMethod.GET, "/api/course/list"),
                new Endpoint(HttpMethod.GET, "/api/course/categories"),
                new Endpoint(HttpMethod.GET, "/api/message/list"),
                new Endpoint(HttpMethod.GET, "/api/message/unread-count"),
                new Endpoint(HttpMethod.GET, "/api/class/list"),
                new Endpoint(HttpMethod.GET, "/api/system/notices"),
                new Endpoint(HttpMethod.GET, "/api/order/list"),
                new Endpoint(HttpMethod.GET, "/api/payment/order/list"),
                new Endpoint(HttpMethod.GET, "/api/vod/list"),
                new Endpoint(HttpMethod.GET, "/api/admin/dashboard/stats"),
                new Endpoint(HttpMethod.GET, "/api/admin/course/list"),
                new Endpoint(HttpMethod.GET, "/api/admin/student/list"),
                new Endpoint(HttpMethod.GET, "/api/admin/marketing/plans"),
                new Endpoint(HttpMethod.GET, "/api/admin/marketing/coupons"),
                new Endpoint(HttpMethod.PUT, "/api/message/read-all"),
                new Endpoint(HttpMethod.POST, "/api/auth/logout")
        );

        int failures = 0;
        StringBuilder report = new StringBuilder("\n==== 全链路冒烟结果 ====\n");
        for (Endpoint ep : endpoints) {
            HttpEntity<Object> entity = new HttpEntity<>(ep.body, authHeaders());
            ResponseEntity<String> resp = rest.exchange(ep.path, ep.method, entity, String.class);
            int status = resp.getStatusCode().value();

            // 判定：404=路由漂移(失败)，5xx=服务端异常(失败)；
            // 2xx=成功；401/403=鉴权/权限拦截(接口存在且正常，视为通过)；其他 4xx=业务校验(通过)
            boolean pass = status != 404 && status < 500;
            if (!pass) {
                failures++;
            }
            report.append(String.format("%-4s %-40s -> %d %s%n",
                    ep.method, ep.path, status, pass ? "OK" : "<<< FAIL"));
        }
        System.out.println(report);
        assertEquals(0, failures, "存在路由漂移或服务端异常的接口，详见上方报告");
    }

    /**
     * 站点公开配置/公告/Banner 必须在「未携带 Token」时可访问，
     * 否则前台游客启动时拉不到导航、公告与轮播（JwtAuthFilter 白名单回归）。
     */
    @Test
    void publicSiteConfigShouldBeAccessibleWithoutToken() {
        List<String> publicPaths = Arrays.asList(
                "/api/system/site-config",
                "/api/system/notices",
                "/api/system/config/register_enabled",
                "/api/banner",
                "/api/homepage/config"
        );
        for (String path : publicPaths) {
            ResponseEntity<String> resp = rest.getForEntity(path, String.class);
            assertEquals(200, resp.getStatusCode().value(),
                    "公开端点应允许游客访问: " + path + " -> " + resp.getBody());
        }
        // 站点配置应包含前台消费的关键键
        ResponseEntity<String> cfg = rest.getForEntity("/api/system/site-config", String.class);
        assertNotNull(cfg.getBody(), "站点配置响应体不应为空");
        for (String key : Arrays.asList("siteName", "navMenu", "registerEnabled", "maintenanceMode")) {
            assertTrue(cfg.getBody().contains("\"" + key + "\""), "站点配置缺少字段: " + key);
        }
    }

    /**
     * 权限边界回归：@RequireRole 由 RequireRoleAspect 真正执行、且 JWT 白名单收紧为
     * 「仅读方法」之后，游客与普通学生都不得调用任何后台写接口。
     * 这两条曾经是真实越权：游客可直接 POST /api/banner，学生可 PUT /api/admin/settings。
     */
    @Test
    void adminWriteEndpointsShouldBeGuarded() {
        Map<String, String> bannerBody = new LinkedHashMap<>();
        bannerBody.put("title", "smoke-guard-test");

        // 1) 游客写 CMS 接口：白名单只对 GET 放行，必须被认证层拦截
        ResponseEntity<String> anon = rest.postForEntity(
                "/api/banner", new HttpEntity<>(bannerBody, jsonHeaders()), String.class);
        int anonStatus = anon.getStatusCode().value();
        assertTrue(anonStatus == 401 || anonStatus == 403,
                "游客不得新建 Banner，实际: " + anonStatus + " " + anon.getBody());

        // 2) 普通学生带 token 写 CMS：角色不足，必须 403（不是 404 / 5xx）
        ResponseEntity<String> studentWrite = rest.exchange(
                "/api/banner", HttpMethod.POST,
                new HttpEntity<>(bannerBody, authHeaders()), String.class);
        assertEquals(403, studentWrite.getStatusCode().value(),
                "普通学生不得新建 Banner，实际: " + studentWrite.getStatusCode().value());

        // 3) 全站设置写入仅管理员：学生必须 403
        Map<String, Object> settings = new LinkedHashMap<>();
        settings.put("maintenance_mode", "false");
        ResponseEntity<String> studentSettings = rest.exchange(
                "/api/admin/settings", HttpMethod.PUT,
                new HttpEntity<>(settings, authHeaders()), String.class);
        assertEquals(403, studentSettings.getStatusCode().value(),
                "普通学生不得修改系统设置，实际: " + studentSettings.getStatusCode().value());
    }

    /**
     * 管理员写链路：测试库预置 admin01（见 backend/scripts/test_accounts.sql）时，
     * 验证「管理员写 sys_config -> 游客读 site-config」闭环。
     * 账号不存在则跳过，避免测试依赖环境预置数据而变 flaky。
     */
    @Test
    void adminSettingsWriteThenPublicReadShouldWork() {
        String adminToken = tryLogin("admin01", "123456");
        Assumptions.assumeTrue(adminToken != null,
                "测试库未预置 admin01/123456，跳过管理员写链路（见 backend/scripts/test_accounts.sql）");

        HttpHeaders h = jsonHeaders();
        h.setBearerAuth(adminToken);
        Map<String, Object> settings = new LinkedHashMap<>();
        // 写同值，不改变实际配置语义，避免污染测试库
        settings.put("maintenance_mode", "false");
        ResponseEntity<String> write = rest.exchange(
                "/api/admin/settings", HttpMethod.PUT, new HttpEntity<>(settings, h), String.class);
        assertEquals(200, write.getStatusCode().value(), "管理员保存设置应成功: " + write.getBody());

        ResponseEntity<String> read = rest.getForEntity("/api/system/site-config", String.class);
        assertEquals(200, read.getStatusCode().value(), "公开读配置应成功");
        assertTrue(read.getBody().contains("\"maintenanceMode\""),
                "公开配置应包含 maintenanceMode 字段");
    }

    // ---- helpers ----

    /** 尝试用账号密码登录，失败返回 null（用于可选的管理员写链路测试） */
    private String tryLogin(String username, String password) {
        try {
            Map<String, String> login = new LinkedHashMap<>();
            login.put("username", username);
            login.put("password", password);
            login.put("loginType", "password");
            ResponseEntity<String> r = rest.postForEntity(
                    "/api/auth/login", new HttpEntity<>(login, jsonHeaders()), String.class);
            return r.getStatusCode().value() == 200 ? extractToken(r.getBody()) : null;
        } catch (Exception e) {
            return null;
        }
    }

    private static class Endpoint {
        final HttpMethod method;
        final String path;
        final Object body;

        Endpoint(HttpMethod method, String path) {
            this(method, path, null);
        }

        Endpoint(HttpMethod method, String path, Object body) {
            this.method = method;
            this.path = path;
            this.body = body;
        }
    }

    private HttpHeaders jsonHeaders() {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        return h;
    }

    private HttpHeaders authHeaders() {
        HttpHeaders h = jsonHeaders();
        h.setBearerAuth(token);
        return h;
    }

    private String extractToken(String body) {
        try {
            JsonNode root = om.readTree(body);
            JsonNode data = root.get("data");
            if (data != null && !data.isNull()) {
                JsonNode t = data.get("accessToken");
                if (t != null && !t.isNull()) {
                    return t.asText();
                }
            }
            // 兼容平铺结构
            JsonNode t = root.get("accessToken");
            return t != null ? t.asText() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
