package com.magao.backend.module.user.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.magao.backend.common.annotation.LogOperation;
import com.magao.backend.common.annotation.RequireRole;
import com.magao.backend.common.base.BaseController;
import com.magao.backend.common.dto.BindParentDTO;
import com.magao.backend.common.dto.PageDTO;
import com.magao.backend.common.dto.PasswordChangeDTO;
import com.magao.backend.common.dto.UserInfoUpdateDTO;
import com.magao.backend.common.enums.UserRoleEnum;
import com.magao.backend.common.result.Result;
import com.magao.backend.module.user.service.UserService;
import com.magao.backend.module.user.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 * <p>
 * 提供用户管理相关的API接口：个人信息、用户列表、禁用、重置密码
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Tag(name = "用户管理", description = "用户信息查询、管理等接口")
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController extends BaseController {

    private final UserService userService;

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/profile")
    public Result<UserVO> getProfile() {
        return Result.success(userService.getUserVO(getCurrentUserId()));
    }

    @Operation(summary = "修改密码")
    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody PasswordChangeDTO dto) {
        userService.changePassword(getCurrentUserId(), dto.getOldPassword(), dto.getNewPassword());
        return Result.success();
    }

    @Operation(summary = "更新用户资料")
    @PutMapping("/info")
    public Result<Void> updateInfo(@RequestBody UserInfoUpdateDTO dto) {
        userService.updateUserInfo(getCurrentUserId(), dto.getNickname(), dto.getRealName(),
                dto.getPhone(), dto.getEmail(), dto.getAvatar());
        return Result.success();
    }

    @Operation(summary = "绑定家长手机号")
    @PostMapping("/bind-parent")
    public Result<Void> bindParent(@Valid @RequestBody BindParentDTO dto) {
        userService.bindParentPhone(getCurrentUserId(), dto.getPhone());
        return Result.success();
    }

    @Operation(summary = "用户列表")
    @GetMapping("/list")
    @RequireRole({UserRoleEnum.ADMIN, UserRoleEnum.TEACHER})
    public Result<Page<UserVO>> listUsers(PageDTO pageDTO, @RequestParam(required = false) String keyword,
                                          @RequestParam(required = false) Integer roleId, @RequestParam(required = false) Integer status) {
        return Result.success(userService.pageList(pageDTO, keyword, roleId, status));
    }

    @Operation(summary = "禁用/启用用户")
    @PutMapping("/disable/{id}")
    @RequireRole({UserRoleEnum.ADMIN})
    @LogOperation(module = "用户管理", type = "修改", description = "修改用户状态")
    public Result<Void> updateStatus(@PathVariable("id") Long userId, @RequestParam Integer status) {
        userService.updateStatus(userId, status);
        return Result.success();
    }

    @Operation(summary = "重置密码")
    @PutMapping("/reset-password/{id}")
    @RequireRole({UserRoleEnum.ADMIN})
    @LogOperation(module = "用户管理", type = "修改", description = "重置用户密码")
    public Result<Void> resetPassword(@PathVariable("id") Long userId, @RequestParam String password) {
        userService.resetPassword(userId, password);
        return Result.success();
    }
}
