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

    /** 修改手机号时的短信验证码（仅当 phone 变化时必填） */
    private String smsCode;

    private String email;

    private String avatar;
}
