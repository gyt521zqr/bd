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

    /** 获取班级列表（currentUserId 用于教师数据隔离，ADMIN 可看全部） */
    List<TeachClass> listClasses(Long teacherId, Integer status, Long currentUserId);

    /** 获取班级详情（currentUserId 用于归属校验） */
    TeachClass getDetail(Long classId, Long currentUserId);

    /** 创建班级（teacherId 服务端强制为当前用户，防伪造） */
    TeachClass createClass(TeachClass teachClass, Long currentUserId);

    /** 添加学生到班级（currentUserId 用于归属校验） */
    void addStudent(Long classId, Long userId, Long currentUserId);

    /** 从班级移除学生（currentUserId 用于归属校验） */
    void removeStudent(Long classId, Long userId, Long currentUserId);
}
