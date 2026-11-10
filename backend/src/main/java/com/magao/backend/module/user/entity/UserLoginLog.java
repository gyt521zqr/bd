package com.magao.backend.module.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户登录日志实体类
 * <p>
 * 对应数据库表：sys_user_login_log
 * 记录用户每次登录的信息，用于安全审计
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@TableName("sys_user_login_log")
public class UserLoginLog implements Serializable {
    private static final long serialVersionUID = 1L;
    /** 日志ID */
    private Long id;
    /** 用户ID */
    private Long userId;
    /** 用户名 */
    private String username;
    /** 登录IP */
    private String loginIp;
    /** 登录地点 */
    private String loginLocation;
    /** 浏览器 */
    private String browser;
    /** 操作系统 */
    private String os;
    /** 登录状态（0-失败，1-成功） */
    private Integer status;
    /** 登录消息 */
    private String message;
    /** 登录时间 */
    private LocalDateTime loginTime;
}
