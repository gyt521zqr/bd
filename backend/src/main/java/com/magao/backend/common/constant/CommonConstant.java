package com.magao.backend.common.constant;

/**
 * 通用常量类
 * <p>
 * 定义系统中通用的常量值，包括：
 * - 状态标识（是/否、启用/禁用）
 * - 默认值
 * - 分页默认参数
 * - 编码字符集等
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
public class CommonConstant {

    /** 私有构造方法，防止实例化 */
    private CommonConstant() {
    }

    // ==================== 状态常量 ====================

    /** 是（1） */
    public static final Integer YES = 1;
    /** 否（0） */
    public static final Integer NO = 0;

    /** 启用状态 */
    public static final Integer STATUS_ENABLED = 1;
    /** 禁用状态 */
    public static final Integer STATUS_DISABLED = 0;

    /** 成功标识 */
    public static final String SUCCESS = "success";
    /** 失败标识 */
    public static final String FAIL = "fail";

    // ==================== 默认值常量 ====================

    /** 默认分页页码 */
    public static final Integer DEFAULT_PAGE_NUM = 1;
    /** 默认每页大小 */
    public static final Integer DEFAULT_PAGE_SIZE = 10;
    /** 最大每页大小 */
    public static final Integer MAX_PAGE_SIZE = 100;

    /** 默认排序字段 */
    public static final String DEFAULT_SORT_FIELD = "create_time";
    /** 默认排序方向 */
    public static final String DEFAULT_SORT_ORDER = "desc";

    // ==================== 编码常量 ====================

    /** UTF-8字符集 */
    public static final String UTF8_ENCODING = "UTF-8";
    /** ISO-8859-1字符集 */
    public static final String ISO_ENCODING = "ISO-8859-1";

    // ==================== 安全相关常量 ====================

    /** MD5加密算法 */
    public static final String MD5_ALGORITHM = "MD5";
    /** SHA-256加密算法 */
    public static final String SHA256_ALGORITHM = "SHA-256";
    /** AES加密算法 */
    public static final String AES_ALGORITHM = "AES";

    // ==================== 文件相关常量 ====================

    /** 文件分隔符 */
    public static final String FILE_SEPARATOR = "/";
    /** 文件大小单位：KB */
    public static final long FILE_SIZE_KB = 1024;
    /** 文件大小单位：MB */
    public static final long FILE_SIZE_MB = 1024 * 1024;
    /** 文件大小单位：GB */
    public static final long FILE_SIZE_GB = 1024 * 1024 * 1024;
}
