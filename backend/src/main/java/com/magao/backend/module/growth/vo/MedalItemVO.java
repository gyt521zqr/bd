package com.magao.backend.module.growth.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 勋章视图对象（对齐前端 MedalItem）
 */
@Data
public class MedalItemVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String name;
    private String icon;
    private String description;
    private Boolean obtained;
    private String obtainTime;
    private String condition;
}
