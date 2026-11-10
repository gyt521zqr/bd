package com.magao.backend.module.growth.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 等级信息视图对象（对齐前端 LevelInfo）
 */
@Data
public class LevelInfoVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer level;
    private String name;
    private String icon;
    private Integer minPoints;
    private Integer maxPoints;
    private Integer currentPoints;
}
