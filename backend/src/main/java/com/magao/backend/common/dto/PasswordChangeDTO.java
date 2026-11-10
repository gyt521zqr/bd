package com.magao.backend.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 修改密码数据传输对象
 */
@Data
public class PasswordChangeDTO {

    /** 旧密码 */
    @NotBlank(message = "旧密码不能为空")
    private String oldPassword;

    /** 新密码 */
    @NotBlank(message = "新密码不能为空")
    @Size(min = 6, max = 100, message = "新密码长度为6-100个字符")
    private String newPassword;
}
