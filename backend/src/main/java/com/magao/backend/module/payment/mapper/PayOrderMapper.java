package com.magao.backend.module.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.magao.backend.module.payment.entity.PayOrder;
import org.apache.ibatis.annotations.Mapper;

/**
 * 支付订单Mapper接口
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Mapper
public interface PayOrderMapper extends BaseMapper<PayOrder> {
}
