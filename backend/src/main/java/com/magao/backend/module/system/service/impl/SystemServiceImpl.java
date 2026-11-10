package com.magao.backend.module.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.magao.backend.module.system.entity.SysConfig;
import com.magao.backend.module.system.entity.SysNotice;
import com.magao.backend.module.system.mapper.SysConfigMapper;
import com.magao.backend.module.system.mapper.SysNoticeMapper;
import com.magao.backend.module.system.service.SystemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * 系统服务实现类
 *
 * @author Magao开发团队
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemServiceImpl implements SystemService {

    private final SysConfigMapper sysConfigMapper;
    private final SysNoticeMapper sysNoticeMapper;

    @Override
    public String getConfigValue(String configKey) {
        SysConfig config = sysConfigMapper.selectOne(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, configKey));
        return config != null ? config.getConfigValue() : null;
    }

    @Override
    public void setConfigValue(String configKey, String configValue) {
        SysConfig config = sysConfigMapper.selectOne(
                new LambdaQueryWrapper<SysConfig>().eq(SysConfig::getConfigKey, configKey));
        if (config != null) {
            config.setConfigValue(configValue);
            sysConfigMapper.updateById(config);
        } else {
            config = new SysConfig();
            config.setConfigKey(configKey);
            config.setConfigValue(configValue);
            sysConfigMapper.insert(config);
        }
        log.info("系统配置更新：key={}, value={}", configKey, configValue);
    }

    @Override
    public List<SysNotice> listNotices() {
        return sysNoticeMapper.selectList(
                new LambdaQueryWrapper<SysNotice>().eq(SysNotice::getStatus, 1).orderByDesc(SysNotice::getCreateTime));
    }
}
