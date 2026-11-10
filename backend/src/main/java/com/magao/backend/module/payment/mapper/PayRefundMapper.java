package com.magao.backend.module.payment.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.magao.backend.module.payment.entity.PayRefund;
import org.apache.ibatis.annotations.Mapper;

/**
 * 退款记录Mapper接口
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Mapper
public interface PayRefundMapper extends BaseMapper<PayRefund> {
}
