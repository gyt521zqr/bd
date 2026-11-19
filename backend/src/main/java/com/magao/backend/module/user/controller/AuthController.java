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
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
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

    /** httpOnly Cookie 的 Secure 标志（生产 HTTPS 环境为 true） */
    @Value("${app.cookie-secure:false}")
    private boolean cookieSecure;

    /** 写 token 到 httpOnly Cookie（P1-18：防 XSS 窃取） */
    private void writeTokenCookie(HttpServletResponse response, String token) {
        ResponseCookie cookie = ResponseCookie.from("magao_token", token == null ? "" : token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(token == null ? 0 : 7 * 24 * 3600)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @Operation(summary = "用户登录", description = "支持密码登录、短信登录、微信登录")
    @PostMapping("/login")
    @RateLimit(key = "login", timeWindow = 60, maxCount = 10, message = "登录请求过于频繁")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO, HttpServletRequest request,
                                 HttpServletResponse response) {
        String loginIp = IpUtil.getClientIp(request);
        LoginVO loginVO = authService.login(loginDTO, loginIp);
        // 下发 httpOnly Cookie（前端 withCredentials 自动携带，JS 无法读取）
        writeTokenCookie(response, loginVO.getAccessToken());
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
    public Result<LoginVO> refreshToken(@RequestParam String refreshToken, HttpServletResponse response) {
        LoginVO loginVO = authService.refreshToken(refreshToken);
        writeTokenCookie(response, loginVO.getAccessToken());
        return Result.success(loginVO);
    }

    @Operation(summary = "用户登出", description = "退出登录，清除Token")
    @PostMapping("/logout")
    public Result<Void> logout(HttpServletResponse response) {
        authService.logout(getCurrentUserId());
        writeTokenCookie(response, null); // 清除 httpOnly Cookie
        return Result.success();
    }
}
