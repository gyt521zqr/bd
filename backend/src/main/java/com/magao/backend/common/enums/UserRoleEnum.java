package com.magao.backend.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户角色枚举
 * <p>
 * 定义系统中的用户角色类型：
 * - STUDENT：学生（普通用户，可学习课程、参加考试、提交练习）
 * - TEACHER：教师（可创建课程、管理考试、批改作业）
 * - OPERATOR：运营人员（可管理营销活动、处理订单）
 * - ADMIN：管理员（拥有所有权限）
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Getter
@AllArgsConstructor
public enum UserRoleEnum {

    /** 学生角色 */
    STUDENT(1, "学生"),
    /** 教师角色 */
    TEACHER(2, "教师"),
    /** 运营人员角色 */
    OPERATOR(3, "运营人员"),
    /** 管理员角色 */
    ADMIN(4, "管理员");

    /** 角色值（存储到数据库） */
    @EnumValue
    private final Integer value;
    /** 角色描述 */
    private final String description;

    /**
     * 根据角色值获取枚举
     *
     * @param value 角色值
     * @return 对应的角色枚举，未找到返回null
     */
    public static UserRoleEnum getByValue(Integer value) {
        if (value == null) {
            return null;
        }
        for (UserRoleEnum role : values()) {
            if (role.getValue().equals(value)) {
                return role;
            }
        }
        return null;
    }
}
