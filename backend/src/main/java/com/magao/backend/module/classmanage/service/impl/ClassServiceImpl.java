package com.magao.backend.module.classmanage.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.magao.backend.common.exception.BusinessException;
import com.magao.backend.common.result.ErrorCode;
import com.magao.backend.module.classmanage.entity.ClassStudent;
import com.magao.backend.module.classmanage.entity.TeachClass;
import com.magao.backend.module.classmanage.mapper.ClassStudentMapper;
import com.magao.backend.module.classmanage.mapper.TeachClassMapper;
import com.magao.backend.module.classmanage.service.ClassService;
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

    @Override
    public List<TeachClass> listClasses(Long teacherId, Integer status) {
        LambdaQueryWrapper<TeachClass> wrapper = new LambdaQueryWrapper<>();
        if (teacherId != null) { wrapper.eq(TeachClass::getTeacherId, teacherId); }
        if (status != null) { wrapper.eq(TeachClass::getStatus, status); }
        return teachClassMapper.selectList(wrapper);
    }

    @Override
    public TeachClass getDetail(Long classId) {
        TeachClass teachClass = teachClassMapper.selectById(classId);
        if (teachClass == null) { throw new BusinessException(ErrorCode.DATA_NOT_FOUND); }
        return teachClass;
    }

    @Override
    public TeachClass createClass(TeachClass teachClass) {
        teachClass.setCurrentStudent(0);
        teachClassMapper.insert(teachClass);
        log.info("班级创建成功：classId={}, className={}", teachClass.getId(), teachClass.getClassName());
        return teachClass;
    }

    @Override
    public void addStudent(Long classId, Long userId) {
        log.info("添加学生到班级：classId={}, userId={}", classId, userId);

        // 检查班级是否存在
        TeachClass teachClass = teachClassMapper.selectById(classId);
        if (teachClass == null) {
            throw new BusinessException(ErrorCode.DATA_NOT_FOUND);
        }

        // 检查是否已存在该关联记录
        LambdaQueryWrapper<ClassStudent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClassStudent::getClassId, classId)
                .eq(ClassStudent::getUserId, userId)
                .eq(ClassStudent::getStatus, 0);
        ClassStudent existing = classStudentMapper.selectOne(wrapper);
        if (existing != null) {
            log.warn("学生已在该班级中：classId={}, userId={}", classId, userId);
            return;
        }

        // 创建班级学生关联记录
        ClassStudent classStudent = new ClassStudent();
        classStudent.setClassId(classId);
        classStudent.setUserId(userId);
        classStudent.setJoinTime(LocalDateTime.now());
        classStudent.setStatus(0);
        classStudentMapper.insert(classStudent);

        // 更新班级当前学生数
        teachClass.setCurrentStudent(teachClass.getCurrentStudent() != null ? teachClass.getCurrentStudent() + 1 : 1);
        teachClassMapper.updateById(teachClass);

        log.info("添加学生到班级成功：classId={}, userId={}", classId, userId);
    }

    @Override
    public void removeStudent(Long classId, Long userId) {
        log.info("从班级移除学生：classId={}, userId={}", classId, userId);

        // 删除班级学生关联记录
        LambdaQueryWrapper<ClassStudent> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ClassStudent::getClassId, classId)
                .eq(ClassStudent::getUserId, userId);
        int rows = classStudentMapper.delete(wrapper);

        if (rows > 0) {
            // 更新班级当前学生数
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
