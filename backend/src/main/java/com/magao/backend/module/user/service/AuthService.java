package com.magao.backend.module.user.service;

import com.magao.backend.common.dto.LoginDTO;
import com.magao.backend.common.dto.RegisterDTO;
import com.magao.backend.module.user.vo.LoginVO;

/**
 * 认证服务接口
 * <p>
 * 定义认证相关的业务方法：JWT登录、短信登录、微信登录、Token刷新、登出
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
public interface AuthService {

    /** 用户登录（支持多种登录方式） */
    LoginVO login(LoginDTO loginDTO, String loginIp);

    /** 发送短信验证码 */
    void sendSmsCode(String phone);

    /** 用户注册（手机号 + 短信验证码） */
    void register(RegisterDTO registerDTO);

    /** 刷新Token */
    LoginVO refreshToken(String refreshToken);

    /** 用户登出 */
    void logout(Long userId);
}
