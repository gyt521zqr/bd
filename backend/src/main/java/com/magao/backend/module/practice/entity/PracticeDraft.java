package com.magao.backend.module.practice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 练习草稿实体类
 * <p>
 * 对应数据库表：practice_draft
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@TableName("practice_draft")
public class PracticeDraft implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private Long userId;
    private Long questionId;
    private String code;
    private Integer language;
    private LocalDateTime updateTime;
}
