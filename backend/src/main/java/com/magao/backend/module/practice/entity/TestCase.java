package com.magao.backend.module.practice.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 测试用例实体类
 * <p>
 * 对应数据库表：test_case
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@TableName("test_case")
public class TestCase implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    /** 题目ID */
    private Long questionId;
    /** 输入数据 */
    private String input;
    /** 预期输出 */
    private String expectedOutput;
    /** 是否为示例用例（0-否，1-是） */
    private Integer isExample;
    /** 分值 */
    private Integer score;
    private LocalDateTime createTime;
}
