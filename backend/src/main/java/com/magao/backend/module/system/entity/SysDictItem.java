package com.magao.backend.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 数据字典项实体类
 * <p>
 * 对应数据库表：sys_dict_item
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Data
@TableName("sys_dict_item")
public class SysDictItem implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    /** 字典ID */
    private Long dictId;
    /** 字典标签 */
    private String dictLabel;
    /** 字典值 */
    private String dictValue;
    /** 排序号 */
    private Integer sortOrder;
    private LocalDateTime createTime;
}
