package com.magao.backend.module.growth.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * 排行榜项视图对象（对齐前端 LeaderboardItem）
 */
@Data
public class LeaderboardItemVO implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer rank;
    private Long userId;
    private String nickname;
    private String avatar;
    private Integer points;
    private Integer level;
    private Integer submitCount;
}
