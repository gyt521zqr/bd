package com.magao.backend.module.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.magao.backend.module.payment.entity.PayCallbackLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付回调日志Mapper接口
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Mapper
public interface PayCallbackLogMapper extends BaseMapper<PayCallbackLog> {
}
