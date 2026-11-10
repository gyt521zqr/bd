package com.magao.backend.module.system.service;

import com.magao.backend.module.system.entity.SysConfig;
import com.magao.backend.module.system.entity.SysNotice;

import java.util.List;

/**
 * 系统服务接口
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
public interface SystemService {

    /** 获取配置值 */
    String getConfigValue(String configKey);

    /** 设置配置值 */
    void setConfigValue(String configKey, String configValue);

    /** 获取公告列表 */
    List<SysNotice> listNotices();
}
