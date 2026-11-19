package com.magao.backend.common.util;

/**
 * 敏感信息脱敏工具（P1-21：手机号/姓名全链路脱敏）
 *
 * @author Magao开发团队
 * @since 2026-01-01
 */
public final class MaskUtil {

    private MaskUtil() {
    }

    /** 手机号脱敏：138****0000（保留前3后4） */
    public static String maskPhone(String phone) {
        if (phone == null || phone.isEmpty()) return phone;
        if (phone.length() < 7) return "****";
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    /** 姓名脱敏：保留首字，其余打码（张**） */
    public static String maskName(String name) {
        if (name == null || name.isEmpty()) return name;
        if (name.length() == 1) return name + "*";
        return name.substring(0, 1) + "**";
    }
}
