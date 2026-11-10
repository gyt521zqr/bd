package com.magao.backend.common.util;

import org.apache.commons.codec.digest.DigestUtils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * 加密解密工具类
 * <p>
 * 提供常用的加密解密方法：
 * 1. MD5加密（不可逆，用于密码存储）
 * 2. SHA-256加密（不可逆，用于数据摘要）
 * 3. AES加密/解密（可逆，用于敏感数据传输）
 * 4. Base64编码/解码
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
public class CryptoUtil {

    /** 私有构造方法，防止实例化 */
    private CryptoUtil() {
    }

    /** AES加密密钥（16字节，实际项目应从配置中心获取） */
    private static final String AES_KEY = "magao-platform-ke";

    /** AES算法/模式/填充 */
    private static final String AES_ALGORITHM = "AES/ECB/PKCS5Padding";

    /**
     * MD5加密
     * <p>
     * 对输入字符串进行MD5哈希运算，返回32位十六进制字符串。
     * 注意：MD5不可逆，适用于密码存储等场景。
     * </p>
     *
     * @param input 待加密字符串
     * @return MD5哈希值（32位十六进制字符串）
     */
    public static String md5(String input) {
        if (input == null) {
            return null;
        }
        return DigestUtils.md5Hex(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * MD5加密（带盐值）
     * <p>
     * 将盐值拼接到原始字符串后进行MD5加密，增强安全性
     * </p>
     *
     * @param input 待加密字符串
     * @param salt  盐值
     * @return MD5哈希值
     */
    public static String md5WithSalt(String input, String salt) {
        if (input == null) {
            return null;
        }
        return DigestUtils.md5Hex((input + salt).getBytes(StandardCharsets.UTF_8));
    }

    /**
     * SHA-256加密
     * <p>
     * 对输入字符串进行SHA-256哈希运算，返回64位十六进制字符串。
     * </p>
     *
     * @param input 待加密字符串
     * @return SHA-256哈希值（64位十六进制字符串）
     */
    public static String sha256(String input) {
        if (input == null) {
            return null;
        }
        return DigestUtils.sha256Hex(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * AES加密
     * <p>
     * 使用AES/ECB/PKCS5Padding模式加密字符串，返回Base64编码的密文
     * </p>
     *
     * @param input 待加密字符串
     * @return Base64编码的密文
     */
    public static String aesEncrypt(String input) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(AES_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("AES加密失败", e);
        }
    }

    /**
     * AES解密
     * <p>
     * 使用AES/ECB/PKCS5Padding模式解密Base64编码的密文
     * </p>
     *
     * @param encryptedInput Base64编码的密文
     * @return 解密后的明文
     */
    public static String aesDecrypt(String encryptedInput) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(AES_KEY.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance(AES_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedInput));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES解密失败", e);
        }
    }

    /**
     * Base64编码
     *
     * @param input 待编码字符串
     * @return Base64编码字符串
     */
    public static String base64Encode(String input) {
        if (input == null) {
            return null;
        }
        return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Base64解码
     *
     * @param encoded Base64编码字符串
     * @return 解码后的字符串
     */
    public static String base64Decode(String encoded) {
        if (encoded == null) {
            return null;
        }
        return new String(Base64.getDecoder().decode(encoded), StandardCharsets.UTF_8);
    }
}
