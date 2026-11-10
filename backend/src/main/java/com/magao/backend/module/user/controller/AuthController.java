package com.magao.backend.module.user.controller;

import com.magao.backend.common.annotation.RateLimit;
import com.magao.backend.common.base.BaseController;
import com.magao.backend.common.dto.LoginDTO;
import com.magao.backend.common.dto.RegisterDTO;
import com.magao.backend.common.result.Result;
import com.magao.backend.common.util.IpUtil;
import com.magao.backend.module.user.service.AuthService;
import com.magao.backend.module.user.vo.LoginVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * <p>
 * 提供用户认证相关的API接口：登录、注册、短信验证码、Token刷新、登出
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Tag(name = "认证管理", description = "用户登录、注册、Token管理等接口")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController extends BaseController {

    private final AuthService authService;

    @Operation(summary = "用户登录", description = "支持密码登录、短信登录、微信登录")
    @PostMapping("/login")
    @RateLimit(key = "login", timeWindow = 60, maxCount = 10, message = "登录请求过于频繁")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request) {
        String loginIp = IpUtil.getClientIp(request);
        LoginVO loginVO = authService.login(loginDTO, loginIp);
        return Result.success(loginVO);
    }

    @Operation(summary = "发送短信验证码", description = "向指定手机号发送6位验证码")
    @PostMapping("/sms-code")
    @RateLimit(key = "sms", timeWindow = 60, maxCount = 1, message = "验证码发送过于频繁")
    public Result<Void> sendSmsCode(@RequestParam String phone) {
        authService.sendSmsCode(phone);
        return Result.success();
    }

    @Operation(summary = "用户注册", description = "手机号 + 短信验证码注册，自动分配学生角色")
    @PostMapping("/register")
    @RateLimit(key = "register", timeWindow = 60, maxCount = 5, message = "注册请求过于频繁")
    public Result<Void> register(@Valid @RequestBody RegisterDTO registerDTO) {
        authService.register(registerDTO);
        return Result.success();
    }

    @Operation(summary = "刷新Token", description = "使用Refresh Token获取新的Access Token")
    @PostMapping("/refresh")
    public Result<LoginVO> refreshToken(@RequestParam String refreshToken) {
        LoginVO loginVO = authService.refreshToken(refreshToken);
        return Result.success(loginVO);
    }

    @Operation(summary = "用户登出", description = "退出登录，清除Token")
    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout(getCurrentUserId());
        return Result.success();
    }
}
