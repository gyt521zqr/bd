package com.magao.backend.module.user.service.impl;

import com.magao.backend.common.constant.RedisKeyConstant;
import com.magao.backend.common.dto.LoginDTO;
import com.magao.backend.common.dto.RegisterDTO;
import com.magao.backend.common.exception.AuthException;
import com.magao.backend.common.exception.BusinessException;
import com.magao.backend.common.result.ErrorCode;
import com.magao.backend.common.util.CryptoUtil;
import com.magao.backend.common.util.JwtUtil;
import com.magao.backend.common.util.PasswordUtil;
import com.magao.backend.common.util.RedisUtil;
import com.magao.backend.module.user.entity.User;
import com.magao.backend.module.user.service.AuthService;
import com.magao.backend.module.user.service.UserService;
import com.magao.backend.module.user.vo.LoginVO;
import com.magao.backend.module.user.vo.UserVO;
import com.magao.backend.module.system.entity.SysLog;
import com.magao.backend.module.system.mapper.SysLogMapper;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 认证服务实现类
 * <p>
 * 实现登录认证、Token管理等业务逻辑
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final SysLogMapper sysLogMapper;
    private static final String PASSWORD_SALT = "magao_salt_2024"; // 仅存量兼容，不再用于新密码
    private static final java.security.SecureRandom SECURE_RANDOM = new java.security.SecureRandom();

    @Override
    public LoginVO login(LoginDTO loginDTO, String loginIp) {
        // P1-26：账号级失败锁定（密码登录按 username，短信登录按 phone）
        String account = "sms".equals(loginDTO.getLoginType()) ? loginDTO.getPhone() : loginDTO.getUsername();
        checkLoginLock(account);

        User user = null;
        switch (loginDTO.getLoginType()) {
            case "password": user = loginByPassword(loginDTO.getUsername(), loginDTO.getPassword()); break;
            case "sms": user = loginBySms(loginDTO.getPhone(), loginDTO.getCode()); break;
            case "wechat": throw new BusinessException("微信登录暂未开放");
            default: throw new BusinessException("不支持的登录类型");
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }
        // 登录成功：清零失败计数
        clearLoginFail(account);
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());
        String tokenKey = RedisKeyConstant.USER_TOKEN_PREFIX + user.getId();
        redisUtil.set(tokenKey, accessToken, RedisKeyConstant.TOKEN_EXPIRE_SECONDS, TimeUnit.SECONDS);
        user.setLastLoginTime(java.time.LocalDateTime.now());
        user.setLastLoginIp(loginIp);
        userService.updateById(user);
        LoginVO loginVO = new LoginVO();
        loginVO.setAccessToken(accessToken);
        loginVO.setRefreshToken(refreshToken);
        loginVO.setTokenType("Bearer");
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setPassword(null);
        loginVO.setUser(userVO);
        log.info("用户登录成功：userId={}, username={}, ip={}", user.getId(), user.getUsername(), loginIp);
        return loginVO;
    }

    @Override
    public void sendSmsCode(String phone) {
        String rateKey = RedisKeyConstant.USER_SMS_CODE_PREFIX + "rate:" + phone;
        if (redisUtil.get(rateKey) != null) { throw new BusinessException("验证码发送过于频繁，请稍后再试"); }
        // P0-16：SecureRandom 替代 Math.random，验证码绝不写入日志（手机号即登录名，泄露即可登录他人账号）
        String code = String.format("%06d", SECURE_RANDOM.nextInt(1000000));
        String codeKey = RedisKeyConstant.USER_SMS_CODE_PREFIX + phone;
        redisUtil.set(codeKey, code, RedisKeyConstant.SMS_CODE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        redisUtil.set(rateKey, "1", 60, TimeUnit.SECONDS);
        log.info("短信验证码已发送：phone={}", phone); // 仅记录发送行为，不记录 code
    }

    @Override
    public void register(RegisterDTO dto) {
        String codeKey = RedisKeyConstant.USER_SMS_CODE_PREFIX + dto.getPhone();
        Object cached = redisUtil.get(codeKey);
        if (cached == null) {
            throw new BusinessException(ErrorCode.SMS_CODE_EXPIRED);
        }
        if (!dto.getSmsCode().equals(cached.toString())) {
            throw new BusinessException(ErrorCode.SMS_CODE_ERROR);
        }
        redisUtil.delete(codeKey);

        User user = new User();
        user.setUsername(dto.getPhone());
        user.setPhone(dto.getPhone());
        user.setNickname(dto.getNickname());
        user.setPassword(dto.getPassword());
        user.setRoleType("STUDENT");
        userService.register(user);

        // 合规留痕(P0-19)：未成年人注册时监护人同意 + 协议勾选，落库可举证
        // 前端已强制勾选，DTO 的 @AssertTrue 强制校验；此处再记录审计证据
        try {
            SysLog consentLog = new SysLog();
            consentLog.setModule("注册合规");
            consentLog.setOperationType("CONSENT");
            consentLog.setDescription(String.format(
                    "用户注册：userId=%d，同意《用户协议》《隐私政策》=%s，监护人同意《儿童个人信息保护规则》=%s，协议版本=2026-11-19",
                    user.getId(), dto.isAgreed(), dto.isGuardianConsent()));
            consentLog.setUserId(user.getId());
            consentLog.setUsername(user.getUsername());
            consentLog.setRequestUrl("/api/auth/register");
            consentLog.setRequestMethod("POST");
            consentLog.setStatus(1);
            consentLog.setCreateTime(LocalDateTime.now());
            sysLogMapper.insert(consentLog);
        } catch (Exception e) {
            // 留痕失败不影响注册主流程，但必须告警以便人工追溯
            log.error("注册同意留痕写入失败：userId={}, error={}", user.getId(), e.getMessage());
        }
    }

    @Override
    public LoginVO refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            throw new AuthException(ErrorCode.TOKEN_INVALID);
        }
        Long userId = jwtUtil.getUserId(refreshToken);
        // P1-15：校验 refreshToken 是否在登出前签发（登出后旧 refreshToken 一律失效）
        Object logoutTime = redisUtil.get(LOGOUT_TIME_PREFIX + userId);
        if (logoutTime != null) {
            try {
                long logoutAt = Long.parseLong(logoutTime.toString());
                Claims claims = jwtUtil.parseToken(refreshToken);
                if (claims.getIssuedAt() != null && claims.getIssuedAt().getTime() <= logoutAt) {
                    throw new AuthException(ErrorCode.TOKEN_INVALID);
                }
            } catch (NumberFormatException ignored) {
                // 登出时间戳异常视为未登出
            }
        }
        User user = userService.getById(userId);
        if (user == null) { throw new BusinessException(ErrorCode.USER_NOT_FOUND); }
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());
        String tokenKey = RedisKeyConstant.USER_TOKEN_PREFIX + userId;
        redisUtil.set(tokenKey, newAccessToken, RedisKeyConstant.TOKEN_EXPIRE_SECONDS, TimeUnit.SECONDS);
        LoginVO loginVO = new LoginVO();
        loginVO.setAccessToken(newAccessToken);
        loginVO.setRefreshToken(newRefreshToken);
        loginVO.setTokenType("Bearer");
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        userVO.setPassword(null);
        loginVO.setUser(userVO);
        return loginVO;
    }

    @Override
    public void logout(Long userId) {
        String tokenKey = RedisKeyConstant.USER_TOKEN_PREFIX + userId;
        redisUtil.delete(tokenKey);
        // P1-15：记录登出时间戳，使已签发的 refreshToken 失效（refresh 时校验签发时间）
        redisUtil.set(LOGOUT_TIME_PREFIX + userId, String.valueOf(System.currentTimeMillis()),
                REFRESH_TOKEN_EXPIRE_SECONDS, TimeUnit.SECONDS);
        log.info("用户登出：userId={}", userId);
    }

    private User loginByPassword(String username, String password) {
        User user = userService.getByUsername(username);
        if (user == null) {
            recordLoginFail(username);
            throw new AuthException(ErrorCode.PASSWORD_ERROR);
        }
        if (!PasswordUtil.matches(password, user.getPassword())) {
            recordLoginFail(username);
            throw new AuthException(ErrorCode.PASSWORD_ERROR);
        }
        // P0-18 渐进迁移：存量 MD5 密码校验通过后自动升级为 BCrypt，用户无感
        if (PasswordUtil.needsRehash(user.getPassword())) {
            try {
                userService.resetPassword(user.getId(), password); // resetPassword 已改为 BCrypt
                log.info("密码已自动升级为BCrypt：userId={}", user.getId());
            } catch (Exception e) {
                log.warn("密码迁移失败（不影响本次登录）：userId={}, error={}", user.getId(), e.getMessage());
            }
        }
        return user;
    }

    private User loginBySms(String phone, String code) {
        String codeKey = RedisKeyConstant.USER_SMS_CODE_PREFIX + phone;
        Object cached = redisUtil.get(codeKey);
        if (cached == null) {
            recordLoginFail(phone);
            throw new AuthException(ErrorCode.SMS_CODE_EXPIRED);
        }
        if (!code.equals(cached.toString())) {
            recordLoginFail(phone);
            throw new AuthException(ErrorCode.SMS_CODE_ERROR);
        }
        redisUtil.delete(codeKey);
        User user = userService.getByPhone(phone);
        if (user == null) { throw new BusinessException(ErrorCode.USER_NOT_FOUND); }
        return user;
    }

    // ==================== 登录失败锁定（P1-26） ====================
    private static final String LOGIN_FAIL_PREFIX = "login:fail:";
    private static final int LOGIN_FAIL_MAX = 5;
    private static final int LOGIN_LOCK_SECONDS = 15 * 60;
    private static final String LOGOUT_TIME_PREFIX = "user:logout:";
    private static final long REFRESH_TOKEN_EXPIRE_SECONDS = 7 * 24 * 3600L;

    /** 检查账号是否因连续失败被锁定 */
    private void checkLoginLock(String account) {
        if (account == null || account.isBlank()) return;
        Object failCount = redisUtil.get(LOGIN_FAIL_PREFIX + account);
        if (failCount != null) {
            try {
                if (Integer.parseInt(failCount.toString()) >= LOGIN_FAIL_MAX) {
                    throw new AuthException(ErrorCode.LOGIN_FAIL_TOO_MANY);
                }
            } catch (NumberFormatException ignored) {
                // 计数异常视为未锁定
            }
        }
    }

    /** 记录一次登录失败，达到阈值后自动锁定一段时间 */
    private void recordLoginFail(String account) {
        if (account == null || account.isBlank()) return;
        try {
            String key = LOGIN_FAIL_PREFIX + account;
            Object failCount = redisUtil.get(key);
            int count = failCount == null ? 0 : Integer.parseInt(failCount.toString());
            count++;
            redisUtil.set(key, String.valueOf(count), LOGIN_LOCK_SECONDS, TimeUnit.SECONDS);
        } catch (Exception e) {
            log.warn("登录失败计数异常：account={}, error={}", account, e.getMessage());
        }
    }

    /** 登录成功后清零失败计数 */
    private void clearLoginFail(String account) {
        if (account == null || account.isBlank()) return;
        try {
            redisUtil.delete(LOGIN_FAIL_PREFIX + account);
        } catch (Exception e) {
            log.warn("清零登录失败计数异常：account={}, error={}", account, e.getMessage());
        }
    }
}
