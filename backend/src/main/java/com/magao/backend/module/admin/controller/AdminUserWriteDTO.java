package com.magao.backend.module.admin.controller;

import lombok.Data;

/**
 * 后台管理-用户写操作 DTO（学员/教师/账号通用）。
 * <p>
 * 与 {@code sys_user} 实体解耦：密码单独用明文 {@code password} 传递，
 * 由控制器在落库前用与登录一致的盐值 {@code CryptoUtil.md5WithSalt(pwd, PASSWORD_SALT)} 哈希，
 * 避免明文入库、并保证与 AuthServiceImpl 登录校验互通。
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
public class AdminUserWriteDTO {
    /** 主键（更新时必填） */
    private Long id;
    /** 登录账号 */
    private String username;
    /** 明文密码（新建必填；更新时填写表示重置密码，留空则不改） */
    private String password;
    /** 昵称 */
    private String nickname;
    /** 真实姓名 */
    private String realName;
    /** 手机号 */
    private String phone;
    /** 邮箱 */
    private String email;
    /** 头像URL */
    private String avatar;
    /** 角色：仅 account 管理页使用（ADMIN/OPERATOR）；student/teacher 由路径决定 */
    private String roleType;
    /** 状态（0-禁用，1-启用） */
    private Integer status;
}
