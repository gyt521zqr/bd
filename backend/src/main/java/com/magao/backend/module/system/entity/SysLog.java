package com.magao.backend.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统日志实体类
 * <p>
 * 对应数据库表：sys_log
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@TableName("sys_log")
public class SysLog implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    /** 操作模块 */
    private String module;
    /** 操作类型 */
    private String operationType;
    /** 操作描述 */
    private String description;
    /** 操作用户ID */
    private Long userId;
    /** 操作用户名 */
    private String username;
    /** 请求URL */
    private String requestUrl;
    /** 请求方法 */
    private String requestMethod;
    /** 请求参数 */
    private String requestParams;
    /** 返回结果 */
    private String responseResult;
    /** IP地址 */
    private String ip;
    /** 耗时（毫秒） */
    private Integer costTime;
    /** 操作状态（0-失败，1-成功） */
    private Integer status;
    private LocalDateTime createTime;
}
