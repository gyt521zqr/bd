package com.magao.backend.common.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 * <p>
 * 提供JWT Token的生成、解析和验证功能：
 * 1. 生成Access Token和Refresh Token
 * 2. 解析Token获取用户信息
 * 3. 验证Token有效性
 * 4. 判断Token是否过期
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Component
public class JwtUtil {

    /** JWT密钥（从配置文件读取） */
    @Value("${jwt.secret}")
    private String secret;

    /** Access Token过期时间（毫秒） */
    @Value("${jwt.access-token-expire}")
    private Long accessTokenExpire;

    /** Refresh Token过期时间（毫秒） */
    @Value("${jwt.refresh-token-expire}")
    private Long refreshTokenExpire;

    /**
     * 生成密钥
     * <p>
     * 根据配置的密钥字符串生成HMAC-SHA256密钥
     * </p>
     *
     * @return SecretKey实例
     */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成Access Token
     * <p>
     * 将用户ID和用户名等信息封装到Token中
     * </p>
     *
     * @param userId   用户ID
     * @param username 用户名
     * @return JWT Token字符串
     */
    public String generateAccessToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("type", "access");
        return generateToken(claims, accessTokenExpire);
    }

    /**
     * 生成Refresh Token
     * <p>
     * Refresh Token只包含用户ID，用于刷新Access Token
     * </p>
     *
     * @param userId 用户ID
     * @return JWT Token字符串
     */
    public String generateRefreshToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("type", "refresh");
        return generateToken(claims, refreshTokenExpire);
    }

    /**
     * 生成Token
     * <p>
     * 通用Token生成方法，设置Claims、签发时间和过期时间
     * </p>
     *
     * @param claims  自定义声明
     * @param expire  过期时间（毫秒）
     * @return JWT Token字符串
     */
    private String generateToken(Map<String, Object> claims, Long expire) {
        Date now = new Date();
        Date expireDate = new Date(now.getTime() + expire);
        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expireDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * 解析Token获取Claims
     *
     * @param token JWT Token字符串
     * @return Claims对象，包含Token中的所有声明
     */
    public Claims parseToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("解析Token失败：{}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token中获取用户ID
     *
     * @param token JWT Token字符串
     * @return 用户ID，解析失败返回null
     */
    public Long getUserId(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        return claims.get("userId", Long.class);
    }

    /**
     * 从Token中获取用户名
     *
     * @param token JWT Token字符串
     * @return 用户名，解析失败返回null
     */
    public String getUsername(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        return claims.get("username", String.class);
    }

    /**
     * 验证Token是否有效
     * <p>
     * 检查Token是否能正确解析且未过期
     * </p>
     *
     * @param token JWT Token字符串
     * @return true表示有效，false表示无效
     */
    public boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return claims != null && !isTokenExpired(token);
        } catch (Exception e) {
            log.error("验证Token失败：{}", e.getMessage());
            return false;
        }
    }

    /**
     * 判断Token是否过期
     *
     * @param token JWT Token字符串
     * @return true表示已过期，false表示未过期
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = parseToken(token);
            if (claims == null) {
                return true;
            }
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    /**
     * 判断Token是否为Refresh Token
     *
     * @param token JWT Token字符串
     * @return true表示是Refresh Token
     */
    public boolean isRefreshToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return false;
        }
        return "refresh".equals(claims.get("type"));
    }
}
