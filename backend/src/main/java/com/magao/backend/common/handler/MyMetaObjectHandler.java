package com.magao.backend.common.handler;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus自动填充处理器
 * <p>
 * 在执行insert和update操作时，自动填充以下字段：
 * - createTime：创建时间（新增时自动填充当前时间）
 * - updateTime：更新时间（新增和修改时自动填充当前时间）
 * </p>
 * <p>
 * 使用方式：在实体类字段上添加@TableField(fill = FieldFill.INSERT)或
 * @TableField(fill = FieldFill.INSERT_UPDATE)注解
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Component
public class MyMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时自动填充
     * <p>
     * 自动填充createTime和updateTime字段为当前时间
     * </p>
     *
     * @param metaObject 元对象
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }

    /**
     * 更新时自动填充
     * <p>
     * 自动填充updateTime字段为当前时间
     * </p>
     *
     * @param metaObject 元对象
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
