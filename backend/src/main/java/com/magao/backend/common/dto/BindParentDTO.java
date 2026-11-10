package com.magao.backend.common.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

/**
 * 绑定家长手机号数据传输对象
 */
@Data
public class BindParentDTO {

    /** 家长手机号 */
    @NotBlank(message = "家长手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;
}
