package com.magao.backend.module.judge.client;

import com.magao.backend.common.config.Judge0Properties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.boot.web.client.RestTemplateBuilder;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Judge0 判题引擎 HTTP 客户端
 * <p>
 * 通过 Judge0 REST API 提交代码执行请求，支持：
 * - base64 编解码（Judge0 要求源码/输入/输出均 base64 传输）
 * - stdin（输入）与 expected_output（期望输出）比对，由 Judge0 返回 AC/WA 判定
 * - 显式设置 CPU 时间限制、内存限制、网络隔离、处理超时，满足竞赛安全与公平性
 * </p>
 *
 * <p>多实例：构造时收集 judge0.api-url / api-url-2 / api-url-3 全部实例地址，
 * 每次判题按轮询（round-robin）选择实例；若某实例连接/系统错误则自动故障转移到下一个，
 * 从而横向扩展吞吐并提升大型竞赛场景下的可用性。仅当全部实例不可用时才返回系统错误。</p>
 *
 * @author Magao开发团队
 */
@Slf4j
@Component
public class Judge0Client {

    private final Judge0Properties props;
    private final RestTemplate restTemplate;
    private final List<String> baseUrls;
    private final AtomicInteger rr = new AtomicInteger(0);

    public Judge0Client(Judge0Properties props, RestTemplateBuilder rtBuilder) {
        this.props = props;
        this.restTemplate = rtBuilder
                .setConnectTimeout(Duration.ofSeconds(5))
                .setReadTimeout(Duration.ofSeconds(props.getTimeout() + 10))
                .build();
        List<String> urls = new ArrayList<>();
        add(urls, props.getApiUrl());
        add(urls, props.getApiUrl2());
        add(urls, props.getApiUrl3());
        if (urls.isEmpty()) {
            urls.add("http://localhost:2358");
        }
        this.baseUrls = urls;
        log.info("Judge0 客户端初始化完成，可用评测实例 {} 个: {}", baseUrls.size(), baseUrls);
    }

    private static void add(List<String> list, String u) {
        if (u != null && !u.isBlank()) {
            String s = u.trim();
            if (!s.endsWith("/")) {
                s = s + "/";
            }
            list.add(s);
        }
    }

    /**
     * 提交一次判题执行，自动在多实例间轮询并实现故障转移。
     *
     * @param sourceCode       源码（原始文本，内部 base64 编码）
     * @param languageId       Judge0 语言数字 ID（如 C++=54, Python=71）
     * @param stdin            标准输入（可为 null）
     * @param expectedOutput   期望输出（可为 null；提供后 Judge0 自动判定 AC/WA）
     * @param cpuTimeLimitSec  CPU 时间限制（秒）
     * @param memoryLimitMb    内存限制（MB）
     * @return 判题结果
     */
    public Judge0Result judge(String sourceCode, int languageId, String stdin,
                              String expectedOutput, int cpuTimeLimitSec, int memoryLimitMb) {
        Judge0Result last = null;
        int n = baseUrls.size();
        for (int i = 0; i < n; i++) {
            String base = baseUrls.get(Math.floorMod(rr.getAndIncrement(), n));
            try {
                last = submit(base, sourceCode, languageId, stdin, expectedOutput, cpuTimeLimitSec, memoryLimitMb);
                if (last != null && !last.isError()) {
                    return last;
                }
                log.warn("Judge0 实例 {} 返回系统错误，尝试下一个实例: {}",
                        base, last == null ? "null" : last.getErrorMessage());
            } catch (Exception e) {
                log.warn("Judge0 实例 {} 调用异常，尝试下一个实例: {}", base, e.getMessage());
            }
        }
        return last != null ? last : Judge0Result.error("无可用判题服务实例");
    }

    /**
     * 向指定实例提交一次执行（内部方法）
     */
    private Judge0Result submit(String baseUrl, String sourceCode, int languageId, String stdin,
                                String expectedOutput, int cpuTimeLimitSec, int memoryLimitMb) {
        try {
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("source_code", b64(sourceCode));
            body.put("language_id", languageId);
            if (stdin != null && !stdin.isEmpty()) {
                body.put("stdin", b64(stdin));
            }
            if (expectedOutput != null) {
                body.put("expected_output", b64(expectedOutput));
            }
            body.put("cpu_time_limit", Math.max(cpuTimeLimitSec, 1));
            body.put("cpu_extra_time", 1);
            body.put("wall_time_limit", Math.max(cpuTimeLimitSec, 1) + 5);
            body.put("memory_limit", memoryLimitMb * 1024); // MB -> KB
            body.put("max_processing_time", Math.min(Math.max(cpuTimeLimitSec, 1) + 5, 15));
            body.put("enable_network", false);
            body.put("number_of_runs", 1);
            body.put("redirect_stderr_to_stdout", true);

            String url = baseUrl + "submissions?wait=true&base64=true";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            String key = props.getApiKey();
            if (key != null && !key.isBlank() && !key.startsWith("your-")) {
                headers.set("X-Judge0-Key", key);
            }
            HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);

            ResponseEntity<Map> resp = restTemplate.postForEntity(url, req, Map.class);
            return parse(resp.getBody());
        } catch (HttpStatusCodeException e) {
            log.error("Judge0 HTTP 错误 {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return Judge0Result.error("判题服务调用失败(" + e.getStatusCode() + ")");
        } catch (RestClientException e) {
            log.error("Judge0 连接错误", e);
            return Judge0Result.error("无法连接判题服务: " + e.getMessage());
        } catch (Exception e) {
            log.error("Judge0 未知错误", e);
            return Judge0Result.error("判题系统错误: " + e.getMessage());
        }
    }

    /**
     * 解析 Judge0 返回结果（base64 字段解码为原文）
     */
    @SuppressWarnings("unchecked")
    private Judge0Result parse(Map<String, Object> r) {
        if (r == null) {
            return Judge0Result.error("判题服务返回空");
        }
        Judge0Result res = new Judge0Result();
        Object statusObj = r.get("status");
        if (statusObj instanceof Map) {
            Map<String, Object> status = (Map<String, Object>) statusObj;
            res.setStatusId(toInt(status.get("id")));
            res.setStatusDescription(toStr(status.get("description")));
        }
        res.setStdout(decode(toStr(r.get("stdout"))));
        res.setStderr(decode(toStr(r.get("stderr"))));
        res.setCompileOutput(decode(toStr(r.get("compile_output"))));
        res.setMessage(toStr(r.get("message")));
        Object time = r.get("time");
        if (time != null) {
            try { res.setTime(Double.parseDouble(time.toString())); } catch (NumberFormatException ignore) {}
        }
        Object mem = r.get("memory");
        if (mem != null) {
            try { res.setMemory(Long.parseLong(mem.toString())); } catch (NumberFormatException ignore) {}
        }
        Object exit = r.get("exit_code");
        if (exit != null) {
            res.setExitCode(toInt(exit));
        }
        return res;
    }

    private static String b64(String s) {
        if (s == null) return "";
        return Base64.getEncoder().encodeToString(s.getBytes(StandardCharsets.UTF_8));
    }

    private static String decode(String s) {
        if (s == null || s.isEmpty()) return null;
        try {
            return new String(Base64.getDecoder().decode(s), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            return s; // 非 base64，原样返回
        }
    }

    private static String toStr(Object o) {
        return o == null ? null : o.toString();
    }

    private static int toInt(Object o) {
        if (o == null) return 0;
        if (o instanceof Number) return ((Number) o).intValue();
        try { return Integer.parseInt(o.toString()); } catch (NumberFormatException e) { return 0; }
    }
}
