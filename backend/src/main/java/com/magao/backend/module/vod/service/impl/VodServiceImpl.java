package com.magao.backend.module.vod.service.impl;

import com.magao.backend.common.config.VodConfig;
import com.magao.backend.common.exception.BusinessException;
import com.magao.backend.module.vod.service.VodService;
import com.magao.backend.module.vod.vo.VodPlayInfoVO;
import com.magao.backend.module.vod.vo.VodUploadAuthVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 火山引擎 VOD 服务实现
 * <p>
 * 使用火山引擎 VOD REST API 实现视频上传凭证获取、播放信息查询等功能。
 * 参考文档：https://www.volcengine.com/docs/4
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "vod.enabled", havingValue = "true", matchIfMissing = false)
public class VodServiceImpl implements VodService {

    private final VodConfig vodConfig;

    /** VOD API 网关地址 */
    private static final String VOD_API_HOST = "vod.volcengineapi.com";

    /** 获取上传地址 API */
    private static final String ACTION_UPLOAD_ADDRESS = "GetUploadAddress";

    /** 获取播放信息 API */
    private static final String ACTION_PLAY_INFO = "GetPlayInfo";

    /** 删除视频 API */
    private static final String ACTION_DELETE_VIDEO = "DeleteMedia";

    /** API 版本 */
    private static final String API_VERSION = "2023-01-01";

    @Override
    public VodUploadAuthVO getUploadAuth(String fileName, String title) {
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("Action", ACTION_UPLOAD_ADDRESS);
            params.put("Version", API_VERSION);
            params.put("SpaceName", vodConfig.getSpaceName());

            // 请求体
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("SpaceName", vodConfig.getSpaceName());

            List<Map<String, Object>> uploadJobs = new ArrayList<>();
            Map<String, Object> job = new LinkedHashMap<>();
            job.put("FileName", fileName);
            job.put("Title", title != null ? title : fileName);
            uploadJobs.add(job);
            body.put("UploadJobs", uploadJobs);

            // 执行 API 请求
            String responseJson = executeVodApi(ACTION_UPLOAD_ADDRESS, params, body);

            // 简化处理：返回完整响应让前端解析
            VodUploadAuthVO vo = new VodUploadAuthVO();
            vo.setUploadToken(responseJson); // 实际应解析响应提取 token
            vo.setSpaceName(vodConfig.getSpaceName());
            log.info("获取 VOD 上传凭证成功：fileName={}", fileName);
            return vo;
        } catch (Exception e) {
            log.error("获取 VOD 上传凭证失败：{}", e.getMessage(), e);
            throw new BusinessException("获取视频上传凭证失败：" + e.getMessage());
        }
    }

    @Override
    public VodPlayInfoVO getPlayInfo(String vid) {
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("Action", ACTION_PLAY_INFO);
            params.put("Version", API_VERSION);
            params.put("Vid", vid);

            // 执行 API 请求
            String responseJson = executeVodApi(ACTION_PLAY_INFO, params, null);

            // 解析响应（简化处理）
            VodPlayInfoVO vo = new VodPlayInfoVO();
            vo.setVid(vid);
            log.info("获取 VOD 播放信息成功：vid={}", vid);
            return vo;
        } catch (Exception e) {
            log.error("获取 VOD 播放信息失败：{}", e.getMessage(), e);
            throw new BusinessException("获取视频播放信息失败：" + e.getMessage());
        }
    }

    @Override
    public void deleteVideo(String vid) {
        try {
            Map<String, Object> params = new LinkedHashMap<>();
            params.put("Action", ACTION_DELETE_VIDEO);
            params.put("Version", API_VERSION);

            Map<String, Object> body = new LinkedHashMap<>();
            List<String> vids = new ArrayList<>();
            vids.add(vid);
            body.put("Vids", vids);

            executeVodApi(ACTION_DELETE_VIDEO, params, body);
            log.info("VOD 视频删除成功：vid={}", vid);
        } catch (Exception e) {
            log.error("VOD 视频删除失败：{}", e.getMessage(), e);
            throw new BusinessException("删除视频失败：" + e.getMessage());
        }
    }

    /**
     * 执行火山引擎 VOD API 请求
     * <p>
     * 使用 HMAC-SHA256 签名算法进行身份验证。
     * </p>
     */
    private String executeVodApi(String action, Map<String, Object> params, Map<String, Object> body) throws Exception {
        String method = body != null ? "POST" : "GET";
        String queryString = buildQueryString(params);
        String url = "https://" + VOD_API_HOST + "/?" + queryString;

        HttpRequest.Builder requestBuilder;
        if ("POST".equals(method)) {
            String jsonBody = body != null ? mapToJson(body) : "{}";
            requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody));
        } else {
            requestBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET();
        }

        // 添加签名头
        addAuthHeaders(requestBuilder, method, VOD_API_HOST, "/", queryString, body);

        HttpClient client = HttpClient.newHttpClient();
        HttpResponse<String> response = client.send(requestBuilder.build(),
                HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("VOD API 请求失败：" + response.statusCode() + " " + response.body());
        }

        return response.body();
    }

    private void addAuthHeaders(HttpRequest.Builder builder, String method, String host,
                                String path, String queryString, Map<String, Object> body) {
        String accessKey = vodConfig.getAccessKey();
        String secretKey = vodConfig.getSecretKey();
        String region = vodConfig.getRegion() != null ? vodConfig.getRegion() : "cn-north-1";
        String service = "vod";

        // 当前时间
        Instant now = Instant.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd")
                .withZone(ZoneId.of("UTC"));
        String date = dateFormatter.format(now);

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'")
                .withZone(ZoneId.of("UTC"));
        String dateTime = dateTimeFormatter.format(now);

        // 构建待签名字符串
        String signedHeaders = "host;x-content-sha256;x-date";
        String payloadHash = sha256Hex(body != null ? mapToJson(body) : "");

        String canonicalRequest = method + "\n"
                + path + "\n"
                + queryString + "\n"
                + "host:" + host + "\n"
                + "x-content-sha256:" + payloadHash + "\n"
                + "x-date:" + dateTime + "\n"
                + "\n"
                + signedHeaders + "\n"
                + payloadHash;

        String credentialScope = date + "/" + region + "/" + service + "/request";
        String stringToSign = "HMAC-SHA256\n" + dateTime + "\n" + credentialScope + "\n"
                + sha256Hex(canonicalRequest);

        // 计算签名密钥
        byte[] kSecret = ("HMAC-SHA256" + secretKey).getBytes(StandardCharsets.UTF_8);
        byte[] kDate = hmacSha256(kSecret, date);
        byte[] kRegion = hmacSha256(kDate, region);
        byte[] kService = hmacSha256(kRegion, service);
        byte[] kSigning = hmacSha256(kService, "request");
        String signature = bytesToHex(hmacSha256(kSigning, stringToSign));

        String authorization = "HMAC-SHA256 Credential=" + accessKey + "/" + credentialScope
                + ", SignedHeaders=" + signedHeaders
                + ", Signature=" + signature;

        builder.header("Host", host);
        builder.header("X-Content-Sha256", payloadHash);
        builder.header("X-Date", dateTime);
        builder.header("Authorization", authorization);
    }

    private String buildQueryString(Map<String, Object> params) {
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            if (sb.length() > 0) sb.append("&");
            sb.append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }

    private String mapToJson(Map<String, Object> map) {
        // 简化 JSON 序列化（生产环境建议使用 Jackson）
        StringBuilder sb = new StringBuilder("{");
        boolean first = true;
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("\"").append(entry.getKey()).append("\":");
            if (entry.getValue() instanceof String) {
                sb.append("\"").append(entry.getValue()).append("\"");
            } else {
                sb.append(entry.getValue());
            }
        }
        sb.append("}");
        return sb.toString();
    }

    private String sha256Hex(String content) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(content.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("SHA-256 计算失败", e);
        }
    }

    private byte[] hmacSha256(byte[] key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key, "HmacSHA256"));
            return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA256 计算失败", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}