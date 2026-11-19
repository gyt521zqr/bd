package com.magao.backend.common.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * 统一密码哈希工具（P0-18：MD5 固定盐 -> BCrypt 渐进迁移）
 * <p>
 * 策略：
 * 1. 所有新设置/重置的密码一律用 BCrypt 加随机盐存储（{@code $2a$/$2b$/$2y$} 前缀）；
 * 2. 校验时自动识别格式：BCrypt 用 matches；存量 MD5 用旧算法校验，
 *    校验成功后可调用 {@link #needsRehash(String)} + {@link #encode(String)} 升级为 BCrypt；
 * 3. 旧的全局固定盐常量仅用于存量数据兼容，不再用于任何新密码。
 * </p>
 */
public final class PasswordUtil {

    /** 存量 MD5 数据使用的旧固定盐（仅兼容验证，禁止用于新密码） */
    private static final String LEGACY_SALT = "magao_salt_2024";
    private static final BCryptPasswordEncoder ENCODER = new BCryptPasswordEncoder();

    private PasswordUtil() {}

    /** 生成 BCrypt 哈希（新密码/重置密码统一走这里） */
    public static String encode(String rawPassword) {
        return ENCODER.encode(rawPassword);
    }

    /**
     * 校验密码：兼容 BCrypt 新格式与 MD5 存量格式。
     */
    public static boolean matches(String rawPassword, String storedHash) {
        if (storedHash == null || rawPassword == null) return false;
        if (isBcrypt(storedHash)) {
            return ENCODER.matches(rawPassword, storedHash);
        }
        // 存量 MD5 校验（恒定时间比较，避免时序侧信道）
        String legacy = CryptoUtil.md5WithSalt(rawPassword, LEGACY_SALT);
        return constantTimeEquals(legacy, storedHash);
    }

    /** 存储的是否为 BCrypt 格式 */
    public static boolean isBcrypt(String hash) {
        return hash != null && (hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$"));
    }

    /** 校验通过后，若仍是旧 MD5 格式则需要重哈希迁移 */
    public static boolean needsRehash(String storedHash) {
        return !isBcrypt(storedHash);
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a == null || b == null) return false;
        byte[] aa = a.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        byte[] bb = b.getBytes(java.nio.charset.StandardCharsets.UTF_8);
        if (aa.length != bb.length) return false;
        int diff = 0;
        for (int i = 0; i < aa.length; i++) diff |= aa[i] ^ bb[i];
        return diff == 0;
    }
}
