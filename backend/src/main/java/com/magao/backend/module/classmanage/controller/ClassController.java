package com.magao.backend.module.classmanage.controller;

import com.magao.backend.common.annotation.LogOperation;
import com.magao.backend.common.annotation.RequireRole;
import com.magao.backend.common.base.BaseController;
import com.magao.backend.common.enums.UserRoleEnum;
import com.magao.backend.common.result.Result;
import com.magao.backend.module.classmanage.entity.TeachClass;
import com.magao.backend.module.classmanage.service.ClassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 班级管理控制器
 * <p>
 * 提供班级管理相关的API接口：班级列表、详情、创建、学生管理
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Tag(name = "班级管理", description = "班级信息查询、创建、学生管理等接口")
@RestController
@RequestMapping("/api/class")
@RequiredArgsConstructor
public class ClassController extends BaseController {

    private final ClassService classService;

    @Operation(summary = "班级列表")
    @GetMapping("/list")
    public Result<List<TeachClass>> listClasses(@RequestParam(required = false) Long teacherId,
                                                @RequestParam(required = false) Integer status) {
        return Result.success(classService.listClasses(teacherId, status));
    }

    @Operation(summary = "班级详情")
    @GetMapping("/{id}")
    public Result<TeachClass> getDetail(@PathVariable("id") Long classId) {
        return Result.success(classService.getDetail(classId));
    }

    @Operation(summary = "创建班级")
    @PostMapping("/create")
    @RequireRole({UserRoleEnum.ADMIN, UserRoleEnum.TEACHER})
    @LogOperation(module = "班级管理", type = "新增", description = "创建班级")
    public Result<TeachClass> createClass(@RequestBody TeachClass teachClass) {
        return Result.success(classService.createClass(teachClass));
    }

    @Operation(summary = "添加学生到班级")
    @PostMapping("/{classId}/student/{userId}")
    @RequireRole({UserRoleEnum.ADMIN, UserRoleEnum.TEACHER})
    @LogOperation(module = "班级管理", type = "修改", description = "添加学生到班级")
    public Result<Void> addStudent(@PathVariable("classId") Long classId,
                                  @PathVariable("userId") Long userId) {
        classService.addStudent(classId, userId);
        return Result.success();
    }

    @Operation(summary = "从班级移除学生")
    @DeleteMapping("/{classId}/student/{userId}")
    @RequireRole({UserRoleEnum.ADMIN, UserRoleEnum.TEACHER})
    @LogOperation(module = "班级管理", type = "删除", description = "从班级移除学生")
    public Result<Void> removeStudent(@PathVariable("classId") Long classId,
                                     @PathVariable("userId") Long userId) {
        classService.removeStudent(classId, userId);
        return Result.success();
    }
}
