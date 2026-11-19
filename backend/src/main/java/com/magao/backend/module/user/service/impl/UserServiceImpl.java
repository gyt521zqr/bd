package com.magao.backend.module.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.magao.backend.common.constant.RedisKeyConstant;
import com.magao.backend.common.dto.PageDTO;
import com.magao.backend.common.exception.BusinessException;
import com.magao.backend.common.result.ErrorCode;
import com.magao.backend.common.util.CryptoUtil;
import com.magao.backend.common.util.MaskUtil;
import com.magao.backend.common.util.PasswordUtil;
import com.magao.backend.common.util.RedisUtil;
import com.magao.backend.module.user.entity.User;
import com.magao.backend.module.user.mapper.UserMapper;
import com.magao.backend.module.user.service.UserService;
import com.magao.backend.module.user.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.TimeUnit;

/**
 * 用户服务实现类
 * <p>
 * 实现用户模块的所有业务逻辑
 * </p>
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    private static final String PASSWORD_SALT = "magao_salt_2024";

    private final RedisUtil redisUtil;

    @Override
    public User register(User user) {
        if (getByUsername(user.getUsername()) != null) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }
        if (StringUtils.hasText(user.getPhone()) && getByPhone(user.getPhone()) != null) {
            throw new BusinessException(ErrorCode.PHONE_EXISTS);
        }
        user.setPassword(PasswordUtil.encode(user.getPassword())); // P0-18：BCrypt 随机盐
        user.setStatus(1);
        save(user);
        log.info("用户注册成功：userId={}, username={}", user.getId(), user.getUsername());
        return user;
    }

    @Override
    public User getByUsername(String username) {
        return getOne(new LambdaQueryWrapper<User>().eq(User::getUsername, username));
    }

    @Override
    public User getByPhone(String phone) {
        return getOne(new LambdaQueryWrapper<User>().eq(User::getPhone, phone));
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = getById(userId);
        if (user == null) { throw new BusinessException(ErrorCode.USER_NOT_FOUND); }
        if (!PasswordUtil.matches(oldPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.OLD_PASSWORD_ERROR);
        }
        user.setPassword(PasswordUtil.encode(newPassword));
        updateById(user);
        log.info("用户修改密码成功：userId={}", userId);
    }

    @Override
    public void updateUserInfo(Long userId, String nickname, String realName, String phone, String email, String avatar, String smsCode) {
        User user = getById(userId);
        if (user == null) { throw new BusinessException(ErrorCode.USER_NOT_FOUND); }
        LambdaUpdateWrapper<User> uw = new LambdaUpdateWrapper<>();
        uw.eq(User::getId, userId);
        if (StringUtils.hasText(nickname)) uw.set(User::getNickname, nickname);
        if (StringUtils.hasText(realName)) uw.set(User::getRealName, realName);
        if (StringUtils.hasText(email)) uw.set(User::getEmail, email);
        if (StringUtils.hasText(avatar)) uw.set(User::getAvatar, avatar);

        // P1-20：修改手机号需短信验证码 + 唯一性校验（手机号即登录名，可被用于账号接管）
        if (StringUtils.hasText(phone)) {
            if (!phone.equals(user.getPhone())) {
                if (!StringUtils.hasText(smsCode)) {
                    throw new BusinessException(ErrorCode.PARAM_ERROR, "修改手机号需短信验证码");
                }
                String codeKey = RedisKeyConstant.USER_SMS_CODE_PREFIX + phone;
                Object cached = redisUtil.get(codeKey);
                if (cached == null) {
                    throw new BusinessException(ErrorCode.SMS_CODE_EXPIRED);
                }
                if (!smsCode.equals(cached.toString())) {
                    throw new BusinessException(ErrorCode.SMS_CODE_ERROR);
                }
                redisUtil.delete(codeKey);
                User existing = getByPhone(phone);
                if (existing != null && !existing.getId().equals(userId)) {
                    throw new BusinessException(ErrorCode.PARAM_ERROR, "该手机号已被其他账号使用");
                }
            }
            uw.set(User::getPhone, phone);
        }
        update(uw);
        log.info("用户资料更新：userId={}", userId);
    }

    @Override
    public void bindParentPhone(Long userId, String parentPhone) {
        User user = getById(userId);
        if (user == null) { throw new BusinessException(ErrorCode.USER_NOT_FOUND); }
        LambdaUpdateWrapper<User> uw = new LambdaUpdateWrapper<>();
        uw.eq(User::getId, userId).set(User::getParentPhone, parentPhone);
        update(uw);
        log.info("绑定家长手机号：userId={}, parentPhone={}", userId, MaskUtil.maskPhone(parentPhone));
    }

    @Override
    public void updateStatus(Long userId, Integer status) {
        User user = getById(userId);
        if (user == null) { throw new BusinessException(ErrorCode.USER_NOT_FOUND); }
        user.setStatus(status);
        updateById(user);
        log.info("用户状态更新：userId={}, status={}", userId, status);
    }

    @Override
    public void resetPassword(Long userId, String password) {
        User user = getById(userId);
        if (user == null) { throw new BusinessException(ErrorCode.USER_NOT_FOUND); }
        user.setPassword(PasswordUtil.encode(password));
        updateById(user);
        log.info("用户密码重置：userId={}", userId);
    }

    @Override
    public Page<UserVO> pageList(PageDTO pageDTO, String keyword, Integer roleId, Integer status) {
        Page<User> page = new Page<>(pageDTO.getPageNum(), pageDTO.getPageSize());
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(User::getUsername, keyword).or().like(User::getPhone, keyword).or().like(User::getRealName, keyword));
        }
        if (status != null) { wrapper.eq(User::getStatus, status); }
        wrapper.orderByDesc(User::getCreateTime);
        Page<User> userPage = page(page, wrapper);
        Page<UserVO> voPage = new Page<>(userPage.getCurrent(), userPage.getSize(), userPage.getTotal());
        java.util.List<UserVO> voList = userPage.getRecords().stream().map(user -> {
            UserVO vo = new UserVO();
            BeanUtils.copyProperties(user, vo);
            vo.setPassword(null);
            // P1-21：手机号/姓名脱敏后返回
            vo.setPhone(MaskUtil.maskPhone(user.getPhone()));
            vo.setRealName(MaskUtil.maskName(user.getRealName()));
            return vo;
        }).collect(java.util.stream.Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public UserVO getUserVO(Long userId) {
        User user = getById(userId);
        if (user == null) { throw new BusinessException(ErrorCode.USER_NOT_FOUND); }
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        vo.setPassword(null);
        return vo;
    }
}
