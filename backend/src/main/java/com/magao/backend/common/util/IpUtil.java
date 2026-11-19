package com.magao.backend.common.util;

import jakarta.servlet.http.HttpServletRequest;

/**
 * IP地址工具类
 * <p>
 * 提供获取客户端真实IP地址的方法：
 * 1. 支持通过代理服务器获取真实IP
 * 2. 支持多级代理（X-Forwarded-For）
 * 3. IP地址格式校验
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
public class IpUtil {

    /** 私有构造方法，防止实例化 */
    private IpUtil() {
    }

    /** 未知IP标识 */
    private static final String UNKNOWN = "unknown";

    /**
     * 获取客户端真实IP地址
     * <p>
     * 按以下优先级获取IP：
     * 1. X-Forwarded-For（经过代理时，第一个IP为真实IP）
     * 2. Proxy-Client-IP
     * 3. WL-Proxy-Client-IP（WebLogic代理）
     * 4. HTTP_CLIENT_IP
     * 5. HTTP_X_FORWARDED_FOR
     * 6. RemoteAddr（直接连接时的IP）
     * </p>
     *
     * @param request HTTP请求对象
     * @return 客户端IP地址
     */
    public static String getClientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();
        String normalized = normalizeIp(remoteAddr);

        // P1-17：直连场景（remoteAddr 为公网 IP）绝不信任转发头，防止伪造 X-Forwarded-For 绕过限流
        if (normalized != null && !isInternalIp(normalized)) {
            return normalized;
        }

        // 经内网可信代理（Nginx 等）时，才信任 X-Forwarded-For 的首个 IP
        String xff = request.getHeader("X-Forwarded-For");
        if (!isInvalidIp(xff)) {
            String first = xff.split(",")[0].trim();
            if (!isInvalidIp(first)) {
                return first;
            }
        }
        return normalized;
    }

    /** 归一化 IP：处理 IPv6 回环地址与多级逗号分隔 */
    private static String normalizeIp(String ip) {
        if (ip == null) return null;
        if ("0:0:0:0:0:0:0:1".equals(ip)) return "127.0.0.1";
        if (ip.contains(",")) return ip.split(",")[0].trim();
        return ip.trim();
    }

    /**
     * 判断IP是否无效（为空或等于unknown）
     *
     * @param ip IP地址
     * @return true表示无效
     */
    private static boolean isInvalidIp(String ip) {
        return ip == null || ip.isEmpty() || UNKNOWN.equalsIgnoreCase(ip);
    }

    /**
     * 判断是否为内网IP地址
     * <p>
     * 内网IP范围：
     * - 10.0.0.0 - 10.255.255.255
     * - 172.16.0.0 - 172.31.255.255
     * - 192.168.0.0 - 192.168.255.255
     * - 127.0.0.0 - 127.255.255.255
     * </p>
     *
     * @param ip IP地址
     * @return true表示是内网IP
     */
    public static boolean isInternalIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        return ip.startsWith("10.") || ip.startsWith("172.16.") || ip.startsWith("172.17.")
                || ip.startsWith("172.18.") || ip.startsWith("172.19.") || ip.startsWith("172.20.")
                || ip.startsWith("172.21.") || ip.startsWith("172.22.") || ip.startsWith("172.23.")
                || ip.startsWith("172.24.") || ip.startsWith("172.25.") || ip.startsWith("172.26.")
                || ip.startsWith("172.27.") || ip.startsWith("172.28.") || ip.startsWith("172.29.")
                || ip.startsWith("172.30.") || ip.startsWith("172.31.")
                || ip.startsWith("192.168.") || ip.startsWith("127.");
    }
}
