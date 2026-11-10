package com.magao.backend.module.growth.vo;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 积分记录视图对象（对齐前端 PointRecord）
 */
@Data
public class PointRecordVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Integer points;
    private String type;
    private String description;
    private LocalDateTime createTime;
}
