package com.magao.backend.common.base;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 服务基类
 * <p>
 * 所有Service接口的父类，继承MyBatis-Plus的IService，
 * 提供通用的CRUD方法。
 * </p>
 *
 * @param <T> 实体类型
 * @author Magao开发团队
 * @since 2024-01-01
 */
public interface BaseService<T> extends IService<T> {
    // 继承MyBatis-Plus IService的所有通用方法
    // 子接口可根据需要扩展自定义方法
}
