package com.magao.backend.module.user.service.impl;

import com.magao.backend.common.constant.RedisKeyConstant;
import com.magao.backend.common.dto.LoginDTO;
import com.magao.backend.common.dto.RegisterDTO;
import com.magao.backend.common.exception.AuthException;
import com.magao.backend.common.exception.BusinessException;
import com.magao.backend.common.result.ErrorCode;
import com.magao.backend.common.util.CryptoUtil;
import com.magao.backend.common.util.JwtUtil;
import com.magao.backend.common.util.RedisUtil;
import com.magao.backend.module.user.entity.User;
import com.magao.backend.module.user.service.AuthService;
import com.magao.backend.module.user.service.UserService;
import com.magao.backend.module.user.vo.LoginVO;
import com.magao.backend.module.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
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
    private static final String PASSWORD_SALT = "magao_salt_2024";

    @Override
    public LoginVO login(LoginDTO loginDTO, String loginIp) {
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
        String code = String.format("%06d", (int) (Math.random() * 1000000));
        String codeKey = RedisKeyConstant.USER_SMS_CODE_PREFIX + phone;
        redisUtil.set(codeKey, code, RedisKeyConstant.SMS_CODE_EXPIRE_SECONDS, TimeUnit.SECONDS);
        redisUtil.set(rateKey, "1", 60, TimeUnit.SECONDS);
        log.info("短信验证码已生成：phone={}, code={}", phone, code);
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
    }

    @Override
    public LoginVO refreshToken(String refreshToken) {
        if (!jwtUtil.validateToken(refreshToken) || !jwtUtil.isRefreshToken(refreshToken)) {
            throw new AuthException(ErrorCode.TOKEN_INVALID);
        }
        Long userId = jwtUtil.getUserId(refreshToken);
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
        log.info("用户登出：userId={}", userId);
    }

    private User loginByPassword(String username, String password) {
        User user = userService.getByUsername(username);
        if (user == null) { throw new AuthException(ErrorCode.PASSWORD_ERROR); }
        String md5 = CryptoUtil.md5WithSalt(password, PASSWORD_SALT);
        if (!md5.equals(user.getPassword())) { throw new AuthException(ErrorCode.PASSWORD_ERROR); }
        return user;
    }

    private User loginBySms(String phone, String code) {
        String codeKey = RedisKeyConstant.USER_SMS_CODE_PREFIX + phone;
        Object cached = redisUtil.get(codeKey);
        if (cached == null) { throw new AuthException(ErrorCode.SMS_CODE_EXPIRED); }
        if (!code.equals(cached.toString())) { throw new AuthException(ErrorCode.SMS_CODE_ERROR); }
        redisUtil.delete(codeKey);
        User user = userService.getByPhone(phone);
        if (user == null) { throw new BusinessException(ErrorCode.USER_NOT_FOUND); }
        return user;
    }
}
