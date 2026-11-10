package com.magao.backend.module.payment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 支付回调日志实体类
 * <p>
 * 对应数据库表：pay_callback_log
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@TableName("pay_callback_log")
public class PayCallbackLog implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    /** 支付渠道 */
    private Integer payChannel;
    /** 订单号 */
    private String orderNo;
    /** 回调参数（JSON） */
    private String callbackData;
    /** 处理状态（0-待处理，1-处理成功，2-处理失败） */
    private Integer status;
    /** 错误信息 */
    private String errorMsg;
    private LocalDateTime createTime;
}
