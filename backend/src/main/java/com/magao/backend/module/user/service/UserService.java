package com.magao.backend.module.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.magao.backend.common.dto.PageDTO;
import com.magao.backend.module.user.entity.User;
import com.magao.backend.module.user.vo.UserVO;

/**
 * 用户服务接口
 * <p>
 * 定义用户模块的业务方法：注册、查询、修改密码、禁用、重置密码、分页列表
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
public interface UserService extends IService<User> {

    /** 用户注册 */
    User register(User user);

    /** 根据用户名查询用户 */
    User getByUsername(String username);

    /** 根据手机号查询用户 */
    User getByPhone(String phone);

    /** 修改密码 */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /** 更新用户资料（仅更新非空字段） */
    void updateUserInfo(Long userId, String nickname, String realName, String phone, String email, String avatar, String smsCode);

    /** 绑定家长手机号 */
    void bindParentPhone(Long userId, String parentPhone);

    /** 禁用/启用用户 */
    void updateStatus(Long userId, Integer status);

    /** 重置密码 */
    void resetPassword(Long userId, String password);

    /** 分页查询用户列表 */
    Page<UserVO> pageList(PageDTO pageDTO, String keyword, Integer roleId, Integer status);

    /** 获取用户详情VO */
    UserVO getUserVO(Long userId);
}
