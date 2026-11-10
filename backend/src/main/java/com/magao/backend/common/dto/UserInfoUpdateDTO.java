package com.magao.backend.common.dto;

import lombok.Data;

/**
 * 用户资料更新数据传输对象（部分字段可空，仅更新非空字段）
 */
@Data
public class UserInfoUpdateDTO {

    private String nickname;

    private String realName;

    private String phone;

    private String email;

    private String avatar;
}
