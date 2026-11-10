package com.magao.backend.module.message.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 短信发送日志实体类
 * <p>
 * 对应数据库表：sms_log
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@TableName("sms_log")
public class SmsLog implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    /** 手机号 */
    private String phone;
    /** 模板编码 */
    private String templateCode;
    /** 发送内容 */
    private String content;
    /** 发送状态（0-失败，1-成功） */
    private Integer status;
    /** 第三方返回消息ID */
    private String thirdPartyMsgId;
    /** 错误信息 */
    private String errorMsg;
    private LocalDateTime createTime;
}
