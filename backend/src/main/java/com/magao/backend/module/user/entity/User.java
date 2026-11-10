package com.magao.backend.module.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.magao.backend.common.base.BaseEntity;
import com.magao.backend.common.enums.UserRoleEnum;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 用户实体类
 * <p>
 * 对应数据库表：sys_user
 * 存储用户基本信息，包括账号、密码、角色、状态等
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("sys_user")
public class User extends BaseEntity {

    /** 用户名（登录账号） */
    private String username;

    /** 密码（加密存储） */
    private String password;

    /** 昵称 */
    private String nickname;

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 家长手机号 */
    private String parentPhone;

    /** 邮箱 */
    private String email;

    /** 头像URL */
    private String avatar;

    /** 角色类型: STUDENT-学生, TEACHER-教师, OPERATOR-运营, ADMIN-管理员 */
    private String roleType;

    /** 状态（0-禁用，1-启用） */
    private Integer status;

    /** 微信OpenID（微信登录用） */
    private String wechatOpenId;

    /** 最后登录时间 */
    private java.time.LocalDateTime lastLoginTime;

    /** 最后登录IP */
    private String lastLoginIp;
}
