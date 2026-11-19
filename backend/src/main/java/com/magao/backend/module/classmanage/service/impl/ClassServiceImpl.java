package com.magao.backend.module.classmanage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.magao.backend.common.exception.BusinessException;
import com.magao.backend.common.result.ErrorCode;
import com.magao.backend.module.classmanage.entity.ClassStudent;
import com.magao.backend.module.classmanage.entity.TeachClass;
import com.magao.backend.module.classmanage.mapper.ClassStudentMapper;
import com.magao.backend.module.classmanage.mapper.TeachClassMapper;
import com.magao.backend.module.classmanage.service.ClassService;
import com.magao.backend.module.user.entity.User;
import com.magao.backend.module.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 班级管理服务实现类
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ClassServiceImpl implements ClassService {

    private final TeachClassMapper teachClassMapper;
    private final ClassStudentMapper classStudentMapper;
    private final UserMapper userMapper;

    /** P0-13：判断当前用户是否为 ADMIN（ADMIN 拥有全局权限，跳过归属校验） */
    private boolean isAdmin(Long userId) {
        if (userId == null) return false;
        User user = userMapper.selectById(userId);
        return user != null && "ADMIN".equalsIgnoreCase(user.getRoleType());
    }

    /** P0-13：校验班级归属，非 ADMIN 必须是该班级的 teacherId */
    private TeachClass requireOwnedClass(Long classId, Long currentUserId) {
        TeachClass tc = teachClassMapper.selectById(classId);
        if (tc == null) throw new BusinessException(ErrorCode.DATA_NOT_FOUND, "班级不存在");
        if (isAdmin(currentUserId)) return tc;
        if (currentUserId == null || !currentUserId.equals(tc.getTeacherId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "无权操作该班级");
        }
        return tc;
    }

    @Override
    public List<TeachClass> listClasses(Long teacherId, Integer status, Long currentUserId) {
        LambdaQueryWrapper<TeachClass> wrapper = new LambdaQueryWrapper<>();
        if (isAdmin(currentUserId)) {
            // ADMIN 可看全部，允许按 teacherId 过滤
            if (teacherId != null) wrapper.eq(TeachClass::getTeacherId, teacherId);
        } else {
            // 教师/运营只能看自己的班级，忽略前端传入的 teacherId
            if (currentUserId == null) return List.of();
            wrapper.eq(TeachClass::getTeacherId, currentUserId);
        }
        if (status != null) wrapper.eq(TeachClass::getStatus, status);
        return teachClassMapper.selectList(wrapper);
    }

    @Override
    public TeachClass getDetail(Long classId, Long currentUserId) {
        return requireOwnedClass(classId, currentUserId);
    }

    @Override
    public TeachClass createClass(TeachClass teachClass, Long currentUserId) {
        // P0-13：teacherId 服务端强制为当前用户，防止冒充他人建班
        if (!isAdmin(currentUserId) && currentUserId != null) {
            teachClass.setTeacherId(currentUserId);
        }
        teachClass.setCurrentStudent(0);
        teachClassMapper.insert(teachClass);
        log.info("班级创建成功：classId={}, className={}, teacherId={}",
                teachClass.getId(), teachClass.getClassName(), teachClass.getTeacherId());
        return teachClass;
    }

    @Override
    public void addStudent(Long classId, Long userId, Long currentUserId) {
        TeachClass teachClass = requireOwnedClass(classId, currentUserId);
        log.info("添加学生到班级：classId={}, userId={}", classId, userId);

        LambdaQueryWrapper<ClassStudent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClassStudent::getClassId, classId)
                .eq(ClassStudent::getUserId, userId)
                .eq(ClassStudent::getStatus, 0);
        ClassStudent existing = classStudentMapper.selectOne(wrapper);
        if (existing != null) {
            log.warn("学生已在该班级中：classId={}, userId={}", classId, userId);
            return;
        }

        ClassStudent classStudent = new ClassStudent();
        classStudent.setClassId(classId);
        classStudent.setUserId(userId);
        classStudent.setJoinTime(LocalDateTime.now());
        classStudent.setStatus(0);
        classStudentMapper.insert(classStudent);

        teachClass.setCurrentStudent(teachClass.getCurrentStudent() != null ? teachClass.getCurrentStudent() + 1 : 1);
        teachClassMapper.updateById(teachClass);
        log.info("添加学生到班级成功：classId={}, userId={}", classId, userId);
    }

    @Override
    public void removeStudent(Long classId, Long userId, Long currentUserId) {
        requireOwnedClass(classId, currentUserId);
        log.info("从班级移除学生：classId={}, userId={}", classId, userId);

        LambdaQueryWrapper<ClassStudent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClassStudent::getClassId, classId)
                .eq(ClassStudent::getUserId, userId);
        int rows = classStudentMapper.delete(wrapper);

        if (rows > 0) {
            TeachClass teachClass = teachClassMapper.selectById(classId);
            if (teachClass != null && teachClass.getCurrentStudent() != null && teachClass.getCurrentStudent() > 0) {
                teachClass.setCurrentStudent(teachClass.getCurrentStudent() - 1);
                teachClassMapper.updateById(teachClass);
            }
            log.info("从班级移除学生成功：classId={}, userId={}", classId, userId);
        } else {
            log.warn("未找到班级学生关联记录：classId={}, userId={}", classId, userId);
        }
    }
}
