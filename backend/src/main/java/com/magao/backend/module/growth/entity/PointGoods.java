package com.magao.backend.module.growth.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.magao.backend.common.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;

/**
 * 积分商品实体类
 * <p>
 * 对应数据库表：point_goods
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("point_goods")
public class PointGoods extends BaseEntity {
    /** 商品名称 */
    private String goodsName;
    /** 商品图片 */
    private String imageUrl;
    /** 所需积分 */
    private Integer needPoint;
    /** 原价 */
    private BigDecimal originalPrice;
    /** 库存数量 */
    private Integer stock;
    /** 已兑换数量 */
    private Integer exchangedCount;
    /** 状态（0-下架，1-上架） */
    private Integer status;
    /** 商品描述 */
    private String description;
}
