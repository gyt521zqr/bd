package com.magao.backend.module.classmanage.service;

import com.magao.backend.module.classmanage.entity.TeachClass;

import java.util.List;

/**
 * 班级管理服务接口
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
public interface ClassService {

    /** 获取班级列表 */
    List<TeachClass> listClasses(Long teacherId, Integer status);

    /** 获取班级详情 */
    TeachClass getDetail(Long classId);

    /** 创建班级 */
    TeachClass createClass(TeachClass teachClass);

    /** 添加学生到班级 */
    void addStudent(Long classId, Long userId);

    /** 从班级移除学生 */
    void removeStudent(Long classId, Long userId);
}
