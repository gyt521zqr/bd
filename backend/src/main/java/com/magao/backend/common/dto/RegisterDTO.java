package com.magao.backend.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 注册数据传输对象
 * <p>
 * 用于接收用户注册请求的参数：手机号 + 短信验证码 + 昵称 + 密码
 * 注册成功后自动分配 STUDENT 角色
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
public class RegisterDTO {

    /** 手机号 */
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    /** 短信验证码 */
    @Size(min = 4, max = 6, message = "验证码长度为4-6位")
    private String smsCode;

    /** 昵称 */
    @NotBlank(message = "昵称不能为空")
    private String nickname;

    /** 密码 */
    @Size(min = 6, max = 100, message = "密码长度为6-100个字符")
    private String password;
}
